
import org.apache.log4j._
import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.sql.SparkSession

object scratch {



  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    val conf = new SparkConf().setAppName("test").setMaster("local[*]")
    val spark = new SparkContext(conf)

    // for use with spark-sql
    //    val spark = SparkSession
//      .builder
//      .appName("Simple Application")
//      .config("spark.master", "local")
//      .getOrCreate()

    val html = scala.io.Source.fromURL("https://www.lds.org/scriptures/bofm/ether/1.1-43?lang=eng").mkString
    println("html: " + html)

    val counts: Array[String] = html.split(" ")
    println("Word Count: " + counts)
//    val list = html.split("\n").filter(_ != "")
//    val rdds = sc.parallelize(list)
//    val counts = list.flatMap(line => line.split(" "))
//    System.exit(0)

  }


}
