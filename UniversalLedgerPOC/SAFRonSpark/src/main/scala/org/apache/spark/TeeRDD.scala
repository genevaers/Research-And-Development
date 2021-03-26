//package org.apache.spark
//
//import org.apache.hadoop.fs.Path
//import org.apache.hadoop.io.{NullWritable, Text}
//import org.apache.hadoop.mapred.{FileOutputCommitter, FileOutputFormat, JobConf, TextOutputFormat}
//import org.apache.spark.annotation.DeveloperApi
//import org.apache.spark.rdd.RDD
//
//import scala.reflect.ClassTag
//
//
//object RDDExtension {
//
//  implicit def rddToTeeRDD[T](rdd: RDD[T])(implicit kt: ClassTag[T]): TeeRDD[T] = {
//    new TeeRDD(rdd)
//  }
//
//  class TeeRDD[T: ClassTag](prev: RDD[T]) extends Serializable with Logging {
//    logInfo("++++ " + prev.toDebugString)
//
//    @transient val sc = prev.context
//    @transient val hadoopConf = new JobConf(sc.hadoopConfiguration)
//    @transient val keyClass = implicitly[ClassTag[NullWritable]].runtimeClass
//    @transient val valueClass = implicitly[ClassTag[Text]].runtimeClass
//    @transient val of = new TextOutputFormat[NullWritable, Text]
//
//    hadoopConf.setOutputKeyClass(keyClass)
//    hadoopConf.setOutputValueClass(valueClass)
//    hadoopConf.setOutputFormat(of.getClass)
//
//    logInfo("Saving as hadoop file of type (" + keyClass.getSimpleName + ", " +
//      valueClass.getSimpleName + ")")
//
//    def tee(path: String): RDD[T] = new MyTeeRDD[T](prev, path)
//
//    def tee(path: String, pred: (T => Boolean)): RDD[T] = new MyTeeRDD[T](prev, path, Option(pred))
//
//    class MyTeeRDD[T: ClassTag](prev: RDD[T], path: String, pred: Option[T => Boolean] = None) extends RDD[T](prev) {
//
//      FileOutputFormat.setOutputPath(hadoopConf, createPathFromString(path, hadoopConf))
//
//      // Use configured output committer if already set
//      if (hadoopConf.getOutputCommitter == null) {
//        hadoopConf.setOutputCommitter(classOf[FileOutputCommitter])
//      }
//
//      val writer = new SparkHadoopWriter(hadoopConf)
//      writer.preSetup()
//
//      @DeveloperApi
//      override def compute(split: Partition, context: TaskContext): Iterator[T] = new Iterator[T] {
//
//        def close(): Unit = {
//          logInfo(s"$recordsWritten records are written to file " +
//            s"$path partition ${context.partitionId}")
//          writer.close()
//          writer.commit()
//        }
//
//        context.addTaskCompletionListener { context => close() }
//
//        val taskAttemptId = (context.taskAttemptId % Int.MaxValue).toInt
//        writer.setup(context.stageId, context.partitionId, taskAttemptId)
//        writer.open()
//
//        var recordsWritten = 0L
//
//        val it: Iterator[T] = prev.iterator(split, context)
//
//        override def hasNext: Boolean = it.hasNext
//
//        override def next(): T = {
//          val t = it.next()
//          if (pred.isEmpty || (pred.isDefined && pred.get(t))) {
//            val text = new Text()
//            text.set(t.toString)
//            logInfo("+++++ " + text)
//            writer.write(NullWritable.get(), text)
//            recordsWritten += 1
//          }
//          t
//        }
//      }
//
//      override protected def getPartitions: Array[Partition] = prev.partitions
//
//      def getWriter = writer
//    }
//
//  }
//
//  def createPathFromString(path: String, conf: JobConf): Path = {
//    if (path == null) {
//      throw new IllegalArgumentException("Output path is null")
//    }
//    val outputPath = new Path(path)
//    val fs = outputPath.getFileSystem(conf)
//    if (outputPath == null || fs == null) {
//      throw new IllegalArgumentException("Incorrectly formatted output path")
//    }
//    outputPath.makeQualified(fs)
//  }
//}