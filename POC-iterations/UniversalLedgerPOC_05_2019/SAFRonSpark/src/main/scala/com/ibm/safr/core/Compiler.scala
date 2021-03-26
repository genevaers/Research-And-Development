/**
 * Compiler.scala: View (Logic Text) compiler
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 */

package com.ibm.safr.core

import com.ibm.safr.core.SAFR._
import com.ibm.safr.core.DataTypeTag.{nextId => _, _}

object Compiler extends Logger {

  /**
   * Compile all lookups
   */
  def compileLookups(metadata: Metadata): Map[String, LookupDDL] = {

    val lookups = for (lup <- metadata.unresolvedLookups.values.toSeq) yield {
      lup.resolve(metadata)
    }
    lookups.map(e => (e.name, e)).toMap
  }

  def compileQueryNode(metadata: Metadata, view: View, node: QueryNode): QueryNode = {

    // Compile child nodes, if any
    val children = node.children.map(child => compileQueryNode(metadata, view, child))

    val retval: QueryNode = node match {
      case u: UnionNode => UnionNode(children)

      /*
    case a: AggNode =>
      // Pull up aggregation functions, do this brute force right now
      val sn: ScanNode = a.child.asInstanceOf[ScanNode]
      val newcols =
        for (col <- sn.columns) yield {
          val tree = col.parseTree match {
            case List(AggFuncExpr(aggtype, child)) => List(child)
            case _ => col.parseTree
          }
          ColumnProjection(col.projType, tree)
        }
      sn.columns = newcols
      */

      case s: UnresolvedScanNode =>
        compileScan(metadata, view, s)

      case _ => ???
    }
    retval
  }

  /**
   * Compile all column expressions in all data sources of all views
   */
  def compileViews(metadata: Metadata): Seq[View] = {

    val newviews = for (view <- metadata.unresolvedViews) yield {

      // Resolve view output.
      // For some reason, matching underscores ("_") isn't working so let's remove those first.
      val output = view.output.filterNot(_ == '_')
      val r = "^[A-Za-z0-9]+$".r
      if (r.findFirstMatchIn(output).isEmpty) {
        logFatal( s"""View ${view.name}: Illegal output definition ("${output}"). Output can only contain alphanumeric characters or underscores.""")
      }

      // Compile the query plan (recursively)
      val newnode = compileQueryNode(metadata, view, view.queryNode)

      val schema: Seq[DataType] = newnode.scans.head.projSchema
      for (ds <- newnode.scans.drop(1)) {

        // Ensure column types are consistent across multiple data sources
        schema.zip(ds.projSchema).foreach { case (a, b) => assert(a.strictEquals(b)) }

        // Log it!
        // logDebug(s"Record filter for view: ${view.name} datasource: ${ds.id}\n" + ds.filterBlock)
        // logDebug(s"Column Assignments for view: ${view.name} datasource: ${ds.id}\n" + ds.projBlock)
      }

      View(view.id, view.name, newnode, Option(view.output))
    }
    newviews
  }

  /**
   * Compile all external file expressions
   */
  def compileMergeFiles(metadata: Metadata) = {
    for (mf <- metadata.mergeFiles.values; mfi <- mf.inputs) {
      logInfo( s"""Unresolved: $mf""")
      val mfiCompiled = new MergeFileInputResolved(mfi, metadata)

      logInfo( s"""Compiled: $mfiCompiled""")
    }
  }

  def isTransformableIfThenElse(ifexpr: IfThenElseExpr): Boolean = {
    /*
        IF {CUST_PREFIX} = "108" THEN COLUMN = "801"
        ELSE IF {CUST_PREFIX} = "970" THEN COLUMN = "079"
        ELSE IF {CUST_PREFIX} = "003" THEN COLUMN = "300"
        ELSE COLUMN = " . "
        ENDIF ENDIF ENDIF

      IfThenElseExpr RelExpr(FieldExpr({CUST_PREFIX}, ()),=,108)
        AssignLiteral(2,801)
        IfThenElseExpr RelExpr(FieldExpr({CUST_PREFIX}, ()),=,970)
        AssignLiteral(2,079)
        IfThenElseExpr RelExpr(FieldExpr({CUST_PREFIX}, ()),=,003)
        AssignLiteral(2,300)
        AssignLiteral(2, . )
     */

    /*
    ifexpr.condExpr match {
      case re@RelExpr(FieldExpr(_), "=", _) =>
        re.rhs.isInstanceOf[Literal] &&
          (ifexpr.thenExpr match {
            case AssignLitExpr(_, _) =>
              ifexpr.elseExpr match {
                case AssignLitExpr(_, _) => true
                case nestedITE: IfThenElseExpr => isTransformableIfThenElse(nestedITE)
                case _ => false
              }
            case _ => false
          })
      case _ => false
    }
    */
    false
  }

  def transformIfThenElse(expr: IfThenElseExpr): Expr = {
    null
  }

  /**
   * Query rewrite: Transform suboptimal expressions into equivalent but more efficient representations
   */
  def rewriteExpr(expr: Expr): Unit = {
    val retval = expr match {
      case ite: IfThenElseExpr =>
        if (isTransformableIfThenElse(ite))
          transformIfThenElse(ite)
        else
          expr

      case _ => expr
    }
  }

  /**
   * Compile all column expressions against a data source
   */
  def compileScan(metadata: Metadata, view: View, scan: UnresolvedScanNode): ScanNode = {

    val (lf: LogicalFile, lr: LogicalRecord) = try {
      (metadata.logicalFilesByName(scan.lfname), metadata.logicalRecordsByName(scan.lrname))
    } catch {
      case ex: java.util.NoSuchElementException =>
        logFatal(s"View: ${view.name}: ${ex.toString}")
        throw ex
    }

    // Compile record filter
    val filterBlock =
      if (scan.filter != null)
        new ExprBlock(scan.selectList.length, scan.filter.map(e => compileExpr(metadata, lr, e, 0, null)))
      else
        null

    // Expand asterisk ("*"). Replace it with a list of all columns in view LR. For example, if an LR
    // has two fields (COL1, COL2), then SELECT COL2, *, COL1 => SELECT COL2, COL1, COL2, COL1
    val newSelectList: Seq[ColumnProjection] = scan.selectList.flatMap { cp =>
      cp.parseTree match {
        case Seq(StarExpr()) =>
          lr.fields.map(field =>
            ColumnProjection(Seq(UnresolvedAssignExpr(None, UnresolvedFieldExpr(field.name)))))
        case _ => Seq(cp)
      }
    }

    // Column type inference
    val coltypes = new Array[DataType](newSelectList.length)

    val exprList: Seq[Expr] = newSelectList.zipWithIndex.flatMap { case (cp, idx) =>
      cp.parseTree.map(e => compileExpr(metadata, lr, e, idx, coltypes))
    }

    // Type inference: Make sure all types have been assigned
    for ((ct, idx) <- coltypes.zipWithIndex) {
      if (ct == null) {
        logFatal(s"View ${view.name}, COL.${idx + 1} assignment missing.")
      }
    }

    val projSchema = coltypes
    val projBlock = new ExprBlock(newSelectList.length, exprList)

    ScanNode(scan.id, lf, lr, filterBlock, projBlock, projSchema)
  }

  /**
   * compileExpr: Compile an expression (including its children, if any)
   */

  def compileExpr(metadata: Metadata, lr: LogicalRecord, expr: Expr, colNum: Int, coltypes: Array[DataType]): Expr = {
    val retval: Expr = expr match {
      case i: IntegerLitExpr => i
      case s: StringLitExpr => s

      case f: UnresolvedFieldExpr =>
        val field = lr(f.name)
        FieldExpr(f.name, field.ordinal, field.datatype)

      case UnresolvedAssignExpr(lhsopt: Option[Int], rhs: Expr) =>
        val lhs = if (lhsopt.isDefined) lhsopt.get else colNum
        val rhsexpr = compileExpr(metadata, lr, rhs, colNum, coltypes)
        val newexpr = AssignExpr(lhs, rhsexpr)

        if (coltypes(lhs) == null)
          coltypes(lhs) = newexpr.datatype
        else {
          if (!coltypes(lhs).equals(newexpr.datatype))
            throw new Exception(
              s"""
                 |COLUMN.${lhs + 1} is being assigned multiple times with different types: ${coltypes(lhs)} vs. ${newexpr.datatype}
                  |Make sure you're not missing a comma (,) in between COLUMN assignments.
                  |Error detected while compiling: ${newexpr.toSQLString}
               """.stripMargin)
        }
        newexpr

      case IfThenElseExpr(condExpr, thenExpr, elseExpr) =>
        val condExpr2 = compileExpr(metadata, lr, condExpr, colNum, coltypes)
        val thenExpr2 = compileExpr(metadata, lr, thenExpr, colNum, coltypes)

        val newexpr = if (elseExpr.isDefined) {
          val elseExpr2 = compileExpr(metadata, lr, elseExpr.get, colNum, coltypes)
          IfThenElseExpr(condExpr2, thenExpr2, Option(elseExpr2))
        } else {
          IfThenElseExpr(condExpr2, thenExpr2, null)
        }
        newexpr

      case le: UnresolvedLookupExpr =>
        // E.g.:  {Effective_Date_Start_Only.Customer_Name,{CUSTOMER_DOB_CCYYMMDD}}

        val splits = le.lookupName.split("[\\{\\}\\.,]").filter(_.length > 0)

        // LookupExpr(lookupName, keySlot, startEffDateSlot, targetSlot)
        //     case LookupExpr(startEffDateCol, lookupName, targetFieldName) => {

        val lookupName = splits(0) // Overwrite the lookup reference just the name of the Lookup Record
        val lookupDateFieldName = if (splits.length == 3) splits(2) else ""
        val targetFieldName: String = splits(1)

        val lookupDesc: LookupDDL = metadata.lookups(lookupName)
        val steps = lookupDesc.steps

        // Make IDs for lookup key
        val sourceLR = steps.head.sourceKeyFields.head._1
        require(lr == sourceLR)

        val lookupDateFieldIdx: Option[Int] = if (lookupDateFieldName != "") {
          val f = lr(lookupDateFieldName)
          Option(f.ordinal)
        } else {
          None
        }

        val targetField = steps.last.targetLR(targetFieldName)

        val newle = LookupExpr(lookupName, lookupDateFieldIdx, lookupDesc.compiledDDL, targetField.ordinal, targetField.datatype)
        newle

      case RelExpr(lhs, relop, rhs) =>
        val reslhs = compileExpr(metadata, lr, lhs, colNum, coltypes)
        val resrhs = compileExpr(metadata, lr, rhs, colNum, coltypes)

        if (!reslhs.datatype.equals(resrhs.datatype))
          logFatal(s"Type mismatch: ${reslhs.datatype} $relop ${resrhs.datatype}")

        val newrelop = relop match {
          case "<>" => "!="
          case "=" => "=="
          case _ => relop
        }
        RelExpr(reslhs, newrelop, resrhs)

      case fe: UnresolvedFuncExpr =>
        compileFuncExpr(metadata, lr, fe, colNum, coltypes)

      case ae: ArithExpr =>
        compileArithExpr(metadata, lr, ae, colNum, coltypes)

      case we: UnresolvedWriteExpr =>
        val filename = we.destFile
        if (filename != null) {
          val splits = filename.split("[\\{\\}\\.,]").filter(_.length > 0)
          val lfopt = metadata.getLogicalFile(splits(0))
          if (lfopt.isEmpty) {
            val errstr = s"""Malformed WRITE statement: Invalid logical file "${splits(0)}" specified."""
            throw new Exception(errstr)
          }

          // HACK: splits(1) is unused. In SAFR/z, it represents a physical file
          WriteExpr(lfopt.get.id, we.extractId, colNum, lfopt.get.isPipe)
        } else
          WriteExpr(-1, we.extractId, colNum, false)

      case OrExpr(lhs, rhs) =>
        val reslhs = compileExpr(metadata, lr, lhs, colNum, coltypes)
        val resrhs = compileExpr(metadata, lr, rhs, colNum, coltypes)
        OrExpr(reslhs, resrhs)

      case AndExpr(lhs, rhs) =>
        val reslhs = compileExpr(metadata, lr, lhs, colNum, coltypes)
        val resrhs = compileExpr(metadata, lr, rhs, colNum, coltypes)
        AndExpr(reslhs, resrhs)

      case NotExpr(lhs) =>
        val reslhs = compileExpr(metadata, lr, lhs, colNum, coltypes)
        NotExpr(reslhs)

      case _ =>
        throw new Exception(s"Expression Compiler: ${expr} not handled")
    }
    retval
  }

  def compileFuncExpr(metadata: Metadata, lr: LogicalRecord, fe: UnresolvedFuncExpr, exprNum: Int, coltypes: Array[DataType]): Expr = {
    val (funcName, funcArgs) = (fe.funcName, fe.args)

    funcName match {
      case "MATCH" =>
        // 	 Example usage: COLUMN = MATCH({DEP_DEPCODE}, "--", "HR", "hr"),

        // Parse arguments
        if (funcArgs.length < 4) throw new Exception(s"Function: ${funcName} needs a minimum of 4 parameter.")
        if (funcArgs.length % 2 != 0) throw new Exception(s"Function: ${funcName} has invalid # of parameter.")

        // Compile arguments
        val newargs = for (i <- funcArgs.indices) yield {
          val arg = funcArgs(i)
          val newexpr = compileExpr(metadata, lr, arg, exprNum, coltypes)

          if (i > 1) {
            if (!arg.isInstanceOf[Literal])
              throw new Exception(
                s"""
                   |Function: ${funcName}, illegal parameter #${i} specified. Expected 'literal'.
                     """.stripMargin)
          }
          newexpr
        }

        // Ensure arg #0 has same type as args #2, #4, #6 ...
        // Ensure args #1, #3, #5, ... have identical types
        for (i <- 2 to newargs.length - 1) {
          val cmpid = i % 2
          if (!newargs(cmpid).datatype.strictEquals(newargs(i).datatype)) {
            val errorText =
              s"""
                 |Function: ${funcName}, parameter ${i} has invalid type.
                                                         |Expected type ${newargs(i).datatype}, found ${newargs(cmpid).datatype}
                   """.stripMargin

            throw new Exception(errorText)
          }
        }

        // Build hash map
        val hashMapNotFound = newargs(1).asInstanceOf[Literal].value
        val keys = (2 until newargs.length by 2).map(i => newargs(i).asInstanceOf[Literal].value)
        val values = (3 until newargs.length by 2).map(i => newargs(i).asInstanceOf[Literal].value)
        val hashMap = keys.zip(values).toMap
        MatchFuncExpr(newargs, hashMap, hashMapNotFound, newargs(1).datatype)

      case "ISSPACES" =>
        if (funcArgs.length != 1)
          logFatal(s"Function $funcName needs a single parameter that evaluates to a CHAR.")

        val newexpr = compileExpr(metadata, lr, funcArgs.head, exprNum, coltypes)
        if (newexpr.datatype.datatype != CHAR)
          logFatal(s"Function $funcName needs a single parameter that evaluates to a CHAR.")

        IsSpacesFuncExpr(newexpr)

      case "PREVIOUS" =>
        funcArgs match {
          case UnresolvedFieldExpr(_) :: Nil =>
          case _ => logFatal(s"Function $funcName needs a single parameter that references a field.")
        }
        val newexpr = compileExpr(metadata, lr, funcArgs.head, exprNum, coltypes).asInstanceOf[FieldExpr]
        PreviousFieldExpr(newexpr.name, newexpr.ordinal, newexpr.datatype)

      case _ => throw new Exception(s"Invalid function: $funcName specified.")
    }
  }

  def compileArithExpr(metadata: Metadata, lr: LogicalRecord, ae: ArithExpr, exprNum: Int, coltypes: Array[DataType]): Expr = {
    val arithop = ae.arithop
    val lhsexpr = compileExpr(metadata, lr, ae.lhs, exprNum, coltypes)
    val rhsexpr = compileExpr(metadata, lr, ae.rhs, exprNum, coltypes)

    assert(lhsexpr.datatype.equals(rhsexpr.datatype))

    // Check for legal combinations
    arithop match {
      case "+" =>
      case "-" =>
      case "*" =>
      case "/" =>
      case _ => throw new Exception(s"Invalid arithmetic operation: ${arithop} specified.")
    }
    ArithExpr(lhsexpr, arithop, rhsexpr)
  }
}
