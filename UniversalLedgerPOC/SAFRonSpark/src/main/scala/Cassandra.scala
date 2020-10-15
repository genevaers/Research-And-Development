/*
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

//import java.nio.ByteBuffer
//
//import com.datastax.spark.connector.rdd.CassandraTableScanRDD
//import org.apache.cassandra.hadoop.ConfigHelper
//import org.apache.hadoop.conf.Configuration
//import org.apache.hadoop.mapred.TextOutputFormat
//import org.apache.spark.rdd.RDD
//import org.apache.spark.{SparkConf, SparkContext}
//import com.datastax.spark.connector._
//import org.apache.cassandra.hadoop.cql3._
//import org.apache.cassandra.config.Config
//import org.apache.hadoop.mapreduce.Job
//import org.apache.hadoop.io._
//import scala.collection.JavaConverters._
//import java.util.{List => JList, ArrayList => AList}
//
//import org.apache.cassandra.db.marshal.Int32Type
//
///**
// * CASSANDRA DDL
// *
// * CREATE KEYSPACE my_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}
// *
// * create table my_keyspace.emp (name text, id int)
// *
// * create table my_keyspace.empout (name text, id int, primary key(id)) ;
// */
//object Cassandra {
//  def main(args: Array[String]): Unit = {
//    println("Hello, World")
//
//    val conf = new SparkConf(true)
//      .set("spark.cassandra.connection.host", "127.0.0.1")
//      .setAppName("Test App")
//      .setMaster("local[2]")
//      .set("spark.ui.port", "4090")
//
//    val sc = new SparkContext(conf)
//
//    readFromCassandra(sc)
//    sc.stop()
//  }
//
//  def readFromCassandra(sc: SparkContext) = {
//
//    val rdd: CassandraTableScanRDD[CassandraRow] = sc.cassandraTable("my_keyspace", "emp")
//    println(rdd.count)
//    // println(rdd.map(_.getInt("value")).sum)
//
//    import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType};
//
//    val customSchema = StructType(Array(
//      StructField("id", IntegerType, true),
//      StructField("name", StringType, true),
//      StructField("sysid", StringType, true)))
//
//    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
//
//    val df = sqlContext.
//      read
//      .format("org.apache.spark.sql.cassandra")
//      .options(Map("table" -> "emp", "keyspace" -> "my_keyspace"))
//      .schema(customSchema)
//      .load()
//
//    println("--")
//
//    df.foreach(println)
//  }
//
//
//  def writeToCassandra(sc: SparkContext) = {
//
//    val job = Job.getInstance
//
//    val conf: Configuration = job.getConfiguration
//
//    val host = "localhost"
//
//    // set the schema and insert statement
//    /*
//    keyspace = keyspace
//    schema = table CREATE TABLE ddl
//    columnFamily = table
//     */
//    ConfigHelper.setOutputColumnFamily(conf, "test", "data")
//    CqlBulkOutputFormat.setTableSchema(conf, "data", "CREATE TABLE test.data (\n    first int,\n    second int,\n    PRIMARY KEY (first, second)\n) WITH CLUSTERING ORDER BY (second ASC) AND\ncompression={'sstable_compression': ''};")
//    CqlBulkOutputFormat.setTableInsertStatement(conf, "data", "insert into test.data (first, second) values (?, ?)")
//    ConfigHelper.setOutputPartitioner(conf, "Murmur3Partitioner")
//    ConfigHelper.setOutputInitialAddress(conf, host)
//    ConfigHelper.setOutputRpcPort(conf, "9160")
//
//    Config.setClientMode(true)
//
//    // setup the hadoop job
//    // job.setOutputKeyClass(classOf[IntWritable])
//    // job.setOutputValueClass(classOf[IntWritable])
//    job.setOutputFormatClass(classOf[CqlBulkOutputFormat])
//
//    val nums: RDD[(IntWritable, AList[ByteBuffer])] = sc.makeRDD(1 to 9).map(x => {
//      val key = new IntWritable(x)
//      val value = new AList[ByteBuffer](2)
//      value.add(Int32Type.instance.decompose(x))
//      value.add(Int32Type.instance.decompose(x * 1001))
//      (key, value)
//    })
//
//    nums.saveAsNewAPIHadoopDataset(conf)
//
//    /*
//    import MultiOutputRDD._
//    import scala.reflect._
//
//    val rddbase: RDD[(String, (IntWritable, AList[ByteBuffer]))] = nums.map(e => ("one", e))
//    val rdd: MultiOutputRDD[IntWritable, AList[ByteBuffer]] = new MultiOutputRDD(rddbase)
//
//    rdd.saveAsNewHadoopMultiOutputs[CqlBulkOutputFormat]("/tmp/foo", conf)
//    */
//  }
//}
