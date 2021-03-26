/**
 * Parser: Parse logic text and build expression tree(s)
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */
package com.ibm.safr.core

import com.ibm.safr.core.DataTypeTag._
import com.ibm.safr.core.SAFR._
import com.ibm.safr.core.SAFR.nextId

import scala.language.postfixOps
import scala.util.parsing.combinator.syntactical.StdTokenParsers
import scala.util.parsing.combinator.token.StdTokens
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.matching.Regex
import scala.util.parsing.combinator._
import scala.util.parsing.input.CharArrayReader.EofCh


trait SAFRTokens extends StdTokens {

  case class ColumnToken(chars: String) extends Token

  case class ColumnNumToken(chars: String, colnum: Int = 0) extends Token

  case class FieldNameToken(chars: String) extends Token

  case class LookupNameToken(chars: String) extends Token

}

class SAFRLexical extends StdLexical with SAFRTokens {
  def regex(r: Regex): Parser[String] = new Parser[String] {
    def apply(in: Input) = r.findPrefixMatchOf(
      in.source.subSequence(in.offset, in.source.length)) match {
      case Some(matched) =>
        Success(in.source.subSequence(in.offset,
          in.offset + matched.end).toString, in.drop(matched.end))
      case None => Failure("string matching regex `" + r +
        "' expected but " + in.first + " found", in)
    }
  }

  override def processIdent(name: String) = {
    val token = name.toUpperCase
    if (reserved contains token) Keyword(token) else Identifier(name)
  }

  override def token: Parser[Token] =
    regex("[Cc][oO][Ll][uU][mM][nN][^A-Za-z0-9_]".r) ^^ {
      ColumnToken
    } |
      regex("[Cc][Oo][Ll]\\.[0-9]+".r) ^^ { e =>
        ColumnNumToken(e, e.toString.substring(4).toInt - 1)
      } |
      regex("\\{[A-Za-z][A-Za-z0-9_]*\\}".r) ^^ {
        FieldNameToken
      } |
      regex("\\{[A-Za-z][A-Za-z0-9_]*\\.[A-Za-z][A-Za-z0-9_]*\\}|\\{[A-Za-z][A-Za-z0-9_]*\\.[A-Za-z][A-Za-z0-9_]*,\\{[A-Za-z][A-Za-z0-9_]*\\}\\}".r) ^^ {
        LookupNameToken
      } |
      regex("[a-zA-Z][a-zA-Z0-9_]*".r) ^^ {
        processIdent
      } |
      regex("0|[1-9][0-9]*".r) ^^ {
        NumericLit
      } |
      regex( """"[^"]*"""".r) ^^ {
        StringLit
      } |
      delim

  override def whitespace: Parser[Any] =
    (whitespaceChar
      | '#' ~ chrExcept(EofCh, '\n').*
      | '\'' ~ chrExcept(EofCh, '\n').*
      | '/' ~ '*' ~ comment
      | '/' ~ '/' ~ chrExcept(EofCh, '\n').*
      | '/' ~ '*' ~ failure("comment is not closed.")
      ).*
}

class Parser extends StdTokenParsers with PackratParsers {

  val extractFileNumbers: scala.collection.mutable.Map[Int, Int] = scala.collection.mutable.Map()

  type Tokens = SAFRTokens
  override val lexical = new SAFRLexical

  lexical.delimiters +=("(", ")", ",", "=", "==", ">", "<", ">=", "<=", "<>", "!=", "-", "+", "*", "/", ".")
  lexical.reserved +=(
    "AND",
    "AS",
    "AVG",
    "BY",
    "COLUMN",
    "COUNT",
    "CREATE",
    "DTE",
    "DATA",
    "DATEFORMAT",
    "DEST",
    "DESTINATION",
    "EFFECTIVE",
    "ELSE",
    "END",
    "ENDIF",
    "EXT",
    "EXTRACT",
    "FILE",
    "FROM",
    "GROUP",
    "IF",
    "INPUT",
    "INTO",
    "KEY",
    "LOGICAL",
    "LOOKUP",
    "MAX",
    "MIN",
    "MERGE",
    "NOT",
    "OPTIONS",
    "OR",
    "PARTITION",
    "PIPE",
    "PROC",
    "PROCEDURE",
    "RECORD",
    "REFERENCES",
    "SELECT",
    "SELECTIF",
    "SET",
    "SOURCE",
    "START",
    "SUM",
    "TABLE",
    "TOKEN",
    "THEN",
    "UNION",
    "USEREXIT",
    "USING",
    "VIEW",
    "WHERE",
    "WRITE")

  def parseAll[T](p: PackratParser[T], in: String): ParseResult[T] = {
    try {
      phrase(p)(new PackratReader(new lexical.Scanner(in)))
    }
    catch {
      case e: java.lang.StackOverflowError =>
        logFatal("Stack overflow in parser. Complex language specified?")
        logFatal("Increase JVM stack size of Spark driver (e.g. -Xss4m) and retry.")
        throw new Exception("Stack overflow in parser!")
    }
  }

  // trimString: Remove leading and trailing character (typically quotes or braces)
  def trimString(s: String) = if (s.length >= 2) s.substring(1, s.length - 1) else s

  // Parse rules
  lazy val number: Parser[Expr] =
    accept("numeric literal", { case t: lexical.NumericLit => IntegerLitExpr(t.chars.toInt) })

  lazy val string: Parser[Expr] =
    accept("string literal", { case t: lexical.StringLit => StringLitExpr(trimString(t.chars)) })

  lazy val literal: Parser[Expr] = number | string

  lazy val fieldName: Parser[Expr] =
    accept("field name", { case e: lexical.FieldNameToken =>
      val name = trimString(e.chars)
      UnresolvedFieldExpr(name)
    })

  // Lookup: {Effective_Date_Start_Only.Customer_Name,{CUSTOMER_DOB_CCYYMMDD}}
  lazy val lookupName: Parser[Expr] =
    accept("field name", {
      case lnt: lexical.LookupNameToken =>
        UnresolvedLookupExpr(lnt.chars.toString)
    })

  lazy val column =
    accept("column reference", {
      case ct: lexical.ColumnToken => ColumnReference(None)
      case cnt: lexical.ColumnNumToken => ColumnReference(Option(cnt.colnum))
    })

  lazy val assignExpr = (column ~ "=" ~ expression) ^^ {
    case (x ~ _ ~ y) => UnresolvedAssignExpr(x.index, y)
  }

  lazy val assignExpr0 = ident ^^ {
    case fld => UnresolvedAssignExpr(None, UnresolvedFieldExpr(fld))
  }

  lazy val starExpr = "*" ^^ {
    case _ => Seq(StarExpr())
  }

  lazy val ifThenElseExpr = "IF" ~ expression ~ "THEN" ~ statements ~ opt("ELSE" ~ statements) ~ "ENDIF" ^^ {
    case (_ ~ relexpr ~ _ ~ thenExpr ~ elseExpr ~ _) => {
      if (elseExpr.isDefined) {
        val right = elseExpr.get match {
          case _ ~ elseExpr => elseExpr
        }
        IfThenElseExpr(relexpr, thenExpr(0), Option(right(0))) //HACK!
      } else {
        IfThenElseExpr(relexpr, thenExpr(0), None)
      }
    }
  }

  lazy val baseExpr: PackratParser[Expr] =
    literal |
      fieldName |
      column |
      lookupName |
      "(" ~> expression <~ ")" |
      function

  lazy val arithProductExpr: Parser[Expr] =
    baseExpr *
      ("*" ^^^ {
        (lhs: Expr, rhs: Expr) => ArithExpr(lhs, "*", rhs)
      }
        | "/" ^^^ {
        (lhs: Expr, rhs: Expr) => ArithExpr(lhs, "/", rhs)
      }
        )

  lazy val arithExpr: Parser[Expr] =
    arithProductExpr *
      ("+" ^^^ {
        (lhs: Expr, rhs: Expr) => ArithExpr(lhs, "+", rhs)
      }
        | "-" ^^^ {
        (lhs: Expr, rhs: Expr) => ArithExpr(lhs, "-", rhs)
      }
        )

  lazy val relop = "=" | "==" | ">" | "<" | ">=" | "<=" | "<>" | "!="

  lazy val relExpr: Parser[Expr] =
    (arithExpr ~ relop ~ arithExpr ^^ {
      case l ~ o ~ r => RelExpr(l, o, r)
    }
      | arithExpr
      )

  lazy val orExpr: Parser[Expr] =
    andExpr * ("OR" ^^^ {
      (lhs: Expr, rhs: Expr) => OrExpr(lhs, rhs)
    })

  lazy val andExpr: Parser[Expr] =
    notExpr * ("AND" ^^^ {
      (lhs: Expr, rhs: Expr) => AndExpr(lhs, rhs)
    })

  lazy val notExpr: Parser[Expr] =
    opt("NOT") ~ relExpr ^^ { case maybeNot ~ e => maybeNot.map(_ => NotExpr(e)).getOrElse(e) }

  protected lazy val function: Parser[Expr] = ident ~ ("(" ~> repsep(expression, ",")) <~ ")" ^^ {
    case funcName ~ args => UnresolvedFuncExpr(funcName.toUpperCase, args)
  }

  lazy val selectif: Parser[Expr] = "SELECTIF" ~ "(" ~ orExpr ~ ")" ^^ {
    case _ ~ _ ~ pexpr ~ _ => pexpr
  }

  lazy val expression: Parser[Expr] = orExpr

  lazy val statement: Parser[Expr] = assignExpr0 | assignExpr | ifThenElseExpr | writeExpr | selectif

  lazy val statements: Parser[List[Expr]] = rep1(statement)

  lazy val logicText: PackratParser[Any] = statements

  // -------------- SQL Interface Rules  -----------------
  lazy val sqlStatement: Parser[Any] = createTableExpr |
    createPipeExpr |
    createLogicalRecordExpr |
    createMergeFileExpr |
    createLookupExpr |
    createViewExpr |
    setOptionExpr ^^ {
      case stmt =>
        stmt
    }

  lazy val sqlStatements = rep1(sqlStatement)

  lazy val sql: PackratParser[Any] = sqlStatements

  // -------------- SET OPTION  -----------------
  lazy val setOptionExpr: Parser[Expr] = "SET" ~> ident ~ (opt("=") ~> string) ^^ {
    case pkey ~ pvalue => {

      val opt = SetOptionExpr(pkey, pvalue.toString)
      opt
    }
  }

  // -------------- CREATE TABLE  -----------------


  implicit def regexToParser(regex: Regex): Parser[String] = acceptMatch(s"identifier matching regex $regex", {
    case lexical.Identifier(str) if regex.unapplySeq(str).isDefined => str
    case lexical.Keyword(str) if regex.unapplySeq(str).isDefined => str
  }
  )

  protected lazy val optionPart: Parser[String] = "[_a-zA-Z][_a-zA-Z0-9]*".r ^^ {
    case name => name
  }

  protected lazy val optionName: Parser[String] = repsep(optionPart, ".") ^^ {
    case parts => parts.mkString(".")
  }

  protected lazy val createTableOption: Parser[(String, String)] =
    optionName ~ string ^^ { case k ~ StringLitExpr(v) => (k, v) }

  lazy val createTableClassName: Parser[String] = repsep(ident, ".") ^^ {
    case str => str.mkString(".")
  }

  lazy val createTableExpr: Parser[Expr] = "CREATE" ~ ("TABLE" | ("LOGICAL" ~ "FILE")) ~ ident ~ opt("." ~> ident) ~
    ("USING" | "AS") ~ createTableClassName ~
    "OPTIONS" ~ "(" ~ repsep(createTableOption, ",") ~ ")" ^^ {
    case _ ~ _ ~ plf ~ plr ~ _ ~ pdriver ~ _ ~ _ ~ poptions ~ _ => LogicalFile(nextId, plf, plr, pdriver, poptions.map(e => (e._1 -> e._2)).toMap)
  }

  lazy val createPipeExpr: Parser[Expr] = "CREATE" ~> ("PIPE" | "TOKEN") ~> ident ^^ {
    case pname => LogicalFile(nextId, pname, None, "PIPE", Map())
  }

  // -------------- CREATE LOGICAL RECORD  -----------------

  lazy val createLogicalRecordExpr: Parser[Expr] = "CREATE" ~> "LOGICAL" ~> "RECORD" ~>
    ident ~ "(" ~ repsep(lrField, ",") ~ ")" ~ opt(lrKey) ^^ {
    case pname ~ _ ~ pcols ~ _ ~ pkeycols => {

      // Create fields with ordinal numbers
      val ordcols = pcols.zipWithIndex.map { case ((pname, pdatatype), idx) =>
        val id = nextId
        val name = if (pname == "*") s"COL${id}" else pname
        LRField(id, idx, name, pdatatype)
      }

      // Create a map to lookup columns by name
      val colmap: Map[String, LRField] = ordcols.map(col => (col.name, col)).toMap

      // Build index fields using aforementioned map
      val (indexcols, startEffDate, stopEffDate) = if (pkeycols.isDefined) {
        val splitcols: Map[Option[String], List[LRField]] = pkeycols.get.map(f => (colmap(f._1), f._2)).groupBy(_._2).mapValues(f => f.map(e => e._1))

        val idxcols = splitcols(None)
        val start = splitcols.getOrElse(Option("START"), Seq())
        val stop = splitcols.getOrElse(Option("END"), Seq())

        if (start.length > 1 || stop.length > 1)
          logFatal(s"Logical record ($pname) has more than one START/STOP EFFECTIVE DATE field.")

        (idxcols, if (start.isEmpty) None else Option(start.head), if (stop.isEmpty) None else Option(stop.head))

      } else (Seq(), None, None)


      val lr = LogicalRecord(nextId, pname, ordcols, indexcols, startEffDate, stopEffDate)
      lr
    }
  }


  lazy val dateformat = "DATEFORMAT" ~> string ^^ {
    case StringLitExpr(strval) => strval
  }

  lazy val datatypelen = "(" ~> number <~ ")" ^^ {
    case IntegerLitExpr(intval) => intval
  }

  lazy val datatypename =
    accept("datatype", { case lexical.Identifier(pname) => {
      datatypeStringToEnum(pname)
    }
    })

  lazy val datatypedef = datatypename ~ opt(datatypelen) ~ opt(dateformat) ^^ {
    case pdt ~ plen ~ pdatef => {
      new DataType(pdt, if (plen.isDefined) plen.get else -1, if (pdatef.isDefined) pdatef.get else "")
    }
  }

  lazy val lrField: Parser[(String, DataType)] = (ident | "*") ~ datatypedef ^^ {
    case pname ~ pdatatype => (pname, pdatatype)
  }

  lazy val lrKeyFieldEffectiveDate = "AS" ~> ("START" | "END") <~ "EFFECTIVE" <~ "DTE" ^^ {
    case strval => strval
  }

  lazy val lrKeyField = ident ~ opt(lrKeyFieldEffectiveDate) ^^ {
    case pid ~ peffdate => (pid, peffdate)
  }

  lazy val lrKey = "KEY" ~> "(" ~> repsep(lrKeyField, ",") <~ ")" ^^ {
    case pkeycols => {
      pkeycols
    }
  }

  // -------------- CREATE MERGE FILE -----------------
  lazy val createMergeFileInputExpr: Parser[MergeFileInput] = ("INPUT" ~> ident) ~ ("." ~> ident) ~ lrKey ^^ {
    case lf ~ lr ~ key => new MergeFileInput(lf, lr, key.toSeq.map(_._1))
  }


  lazy val createMergeFileExpr: Parser[MergeFile] = "CREATE" ~> "MERGE" ~>
    ident ~ opt("AS") ~ "(" ~ repsep(createMergeFileInputExpr, ",") ~ ")" ~
    "OPTIONS" ~ "(" ~ repsep(createTableOption, ",") ~ ")" ^^ {
    case id ~ _ ~ _ ~ inputs ~ _ ~ _ ~ _ ~ poptions ~ _ => {
      val ef = MergeFile(nextId, id, inputs.toSeq, poptions.map(e => (e._1 -> e._2)).toMap)
      ef
    }
  }

  // -------------- CREATE LOOKUP -----------------
  lazy val createLookupExpr: Parser[Expr] = "CREATE" ~> "LOOKUP" ~> ident ~ ("(" ~> repsep(lookupStep, ",")) <~ ")" ^^ {
    case lupname ~ lupsteps => UnresolvedLookupDDL(nextId, lupname, lupsteps)
  }

  lazy val lookupKey: Parser[(String, String)] = ident ~ ("." ~> ident) ^^ {
    case lrname ~ keyname => (lrname, keyname)
  }

  lazy val lookupStep = opt("(") ~> (repsep(lookupKey, ",") <~ opt(")") <~ "REFERENCES") ~ (ident <~ ".") ~ ident ^^ {
    case sourcekeycols ~ targetlf ~ targetlr =>
      UnresolvedLookupStepDDL(sourcekeycols, targetlf, targetlr)
  }

  // -------------- CREATE VIEW -----------------

  lazy val createViewExpr: Parser[View] = "CREATE" ~> "VIEW" ~> ident ~
    opt(intoClause) ~ (opt("AS") ~> repsep(selectExpr, "UNION")) ^^ {
    case pname ~ pinto ~ pdatasources =>
      val view = View(nextId, pname, UnionNode(pdatasources), pinto)
      view
  }

  lazy val intoClause = "INTO" ~> string ^^ {
    case StringLitExpr(subdir) =>
      subdir
  }

  lazy val selectExpr = "SELECT" ~>
    projectList ~
    ("FROM" ~> ident) ~ ("." ~> ident) ~
    opt(whereClause) ~
    opt(groupByClause) ^^ {
    case pplist ~ plr ~ plf ~ pwhere ~ pgroupby =>
      val ds = new UnresolvedScanNode(nextId, plr, plf,
        if (pwhere.isDefined) Seq(pwhere.get) else null, pplist)

      val ret = if (pgroupby.isDefined) null else ds
      ret
  }

  lazy val statements0: Parser[Seq[Expr]] = rep1(statement) | starExpr

  lazy val projectList = repsep(statements0, ",") ^^ {
    case plist =>
      plist.map(col => ColumnProjection(col)).toSeq
  }

  lazy val whereClause = "WHERE" ~> selectif

  lazy val groupingColumn =
    accept("column reference", {
      case cnt: lexical.ColumnNumToken => ColumnReference(Option(cnt.colnum))
    })

  lazy val groupByClause = "GROUP" ~> "BY" ~> repsep(groupingColumn, ",") ^^ {
    case collist => collist
  }

  // -------------- WRITE Expression rules -----------------
  lazy val writeSource = "SOURCE" ~ "=" ~ ("INPUT" | "DATA" | "VIEW") ^^ {
    case _ ~ _ ~ data => WriteSourceExpr(data)
  }

  lazy val writeDestFile =
    accept("writeDestFile", {
      case t: lexical.LookupNameToken => t
    })

  lazy val writeDest = ("DEST" | "DESTINATION") ~ "=" ~ ("FILE" | "EXTRACT") ~ "=" ~ (writeDestFile | number) ^^ {
    case _ ~ _ ~ destType ~ _ ~ destFile => {
      destType match {
        case "FILE" => {
          val filename = destFile.asInstanceOf[lexical.LookupNameToken].chars
          WriteDestFileExpr(filename)
        }
        case "EXTRACT" => {
          WriteDestExtractExpr(destFile.asInstanceOf[IntegerLitExpr].i)
        }
      }
    }
  }

  // TODO: writeExit is not parsed correctly.
  lazy val writeExit: Parser[Any] = ("PROC" | "PROCEDURE" | "EXIT") ~ "=" ~ fieldName ^^ {
    case a ~ b ~ c => WriteExitExpr(fieldName.toString)
  }

  lazy val writeParm = writeSource | writeDest | writeExit

  lazy val writeParms: Parser[List[Any]] = repsep(writeParm, ",") ^^ {
    case l => l.toList
  }

  // WRITE(SOURCE = DATA,DESTINATION = FILE = {OUT_2_LF.OUT_1_PF})
  lazy val writeExpr: Parser[Expr] = "WRITE" ~ "(" ~ writeParms ~ ")" ^^ {
    case _ ~ _ ~ writeParms ~ _ => {
      var sourceType: String = "VIEW"
      var destType: String = null
      var destFile: String = null
      var extractId: Int = 0
      var exit: String = ""
      var isPipeParm: Boolean = false

      for (wp <- writeParms) {
        wp match {
          case WriteSourceExpr(sourceType_) =>
            sourceType = sourceType_
          case WriteDestFileExpr(filename) =>
            if (destType != null)
              logFatal("Invalid WRITE syntax: multiple destination clauses specified.")
            destType = "FILE"
            destFile = filename
          case WriteDestExtractExpr(exid) =>
            if (destType != null)
              logFatal("Invalid WRITE syntax: multiple destination clauses specified.")
            destType = "EXTRACT"
            extractId = exid

            if (extractFileNumbers.contains(extractId))
              logFatal(s"Invalid WRITE syntax: EXTRACT file number $extractId reused.")
            extractFileNumbers += (extractId -> extractId)

          case WriteExitExpr(name) =>
            exit = name
        }
      }
      if (destType == null)
        throw new Exception("Invalid WRITE statement: No destination clause specified.")

      UnresolvedWriteExpr(sourceType, destType, destFile, extractId, exit)
    }
  }
}
