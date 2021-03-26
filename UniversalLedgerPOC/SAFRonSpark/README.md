# SAFRonSpark
Scalable Architecture for Financial Reporting on Spark Prototype

Pre-requisites include 
    Java 1.8+
    Scala 2.10+
    Spark 1.6+ (note, code not compabible with latest Spark version)
    
The following unit test case is include to test the installation:

* Simple test case definition for SAFR on Spark (UnitTest1.sql) -  update path names of the data files below
* Data files (Fact.csv, DimDeptCode.csv) - Put these somewhere on the file system (including HDFS)
* Shell script to run testcase(UnitTest1.sh) - Please update path names. Right now, this uses YARN.

