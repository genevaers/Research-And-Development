# This SAFR on Spark Demo takes in a Transaction File as an input, does joins to transform data to Universal Joual outputs.  It requires the following functions.
#    Multiple LFs and LRs
#    CSV data inputs
#    Lookups (single-step effective dated)
#    Simple expressions including IF-THEN-ELSE and arithmetic


# VIEW_OUTPUT determines where the view output goes. Legal values are (COUNT | COLLECT | <directory>)
#  COUNT - Print out # of rows from all views
#  COLLECT - Print the first 1000 rows (hardcoded)
#  <directory> - Top-level output direcory uri. All view outputs go under this directory.
set VIEWOUTPUT "OUTPUTDIR"

CREATE TABLE TRANS_LF
    USING com.databricks.spark.csv
#    OPTIONS (path "hdfs://localhost:9000/Users/ktwitchell001/opt/SAFRSpark/SAFRSparkDemo/Trans.csv", header "false", dateFormat "yyyymmdd")
     OPTIONS (path "file:///universal_ledger/SAFRonSpark/data/InitEnv/Trans.csv", header "true", dateFormat "yyyymmdd")

CREATE LOGICAL RECORD TRANS_LR (
    TRN_ID        INT,
    TRN_DEPCODE   CHAR(10),
    TRN_DESCRIP   CHAR(20),
    TRN_TYPE      CHAR(7),
    TRN_DATE      DATE,
    TRN_COMPANY   CHAR(2),
    TRN_AMOUNT    DOUBLE(6)
)

CREATE TABLE DEPT_LF
    USING com.databricks.spark.csv
#    OPTIONS (path "hdfs://localhost:9000/Users/ktwitchell001/opt/SAFRSparkDemo/CenterDeptCode.csv", header "true", dateFormat "yyyymmdd")
    OPTIONS (path "file:///universal_ledger/SAFRonSpark/data/InitEnv/CenterDeptCode.csv", header "true", dateFormat "yyyymmdd")

CREATE LOGICAL RECORD DEPT_LR (
    DEP_DEPCODE     CHAR(10),
    DEP_CENTER      CHAR(8),
    DEP_DEPT_TITLE  CHAR(15),
    Dep_Start_Date  DATE
 )
 KEY (DEP_DEPCODE, Dep_Start_Date AS START EFFECTIVE DTE)

#CREATE PIPE PIPE_1_LF

CREATE LOOKUP LOOKUP_HANDLE (
   TRANS_LR.TRN_DEPCODE REFERENCES DEPT_LF.DEPT_LR
)


CREATE VIEW UNV_JOURNAL
  SELECT COLUMN = "7832485732",             //INSTRUMENT ID
    COLUMN = "JE1000100",                   //JOURNAL ID
    COLUMN = "1",                           //JOURNAL LINE NO
    COLUMN = {TRN_DESCRIP},                  //DESCRIPTION
    COLUMN = "Actuals",                      //LEDGER,
    COLUMN = "FIN",                          //JOURNAL TYPE
#     IF {TRN_TYPE} = "resvesp" THEN //not sure why this is causing an error in April 2019
     IF {TRN_TYPE} > "resvesp" THEN
       COLUMN = "IFRS"
     ELSE
#        IF {TRN_TYPE} = "lgapresv" THEN //not sure why this is causing an error in April 2019
        IF {TRN_TYPE} > "lgapresv" THEN
                  COLUMN = "LGAP"
          ELSE
               COLUMN = "CORE"              //BOOKCODE
        ENDIF
     ENDIF,
    COLUMN = {TRN_COMPANY},                  //COMPANY
    COLUMN = "",                             //COMPANY TITLE
    COLUMN = {LOOKUP_HANDLE.DEP_CENTER,{TRN_DATE}},   //CENTER ID
    COLUMN = {LOOKUP_HANDLE.DEP_DEPT_TITLE,{TRN_DATE}},   //CENTER TITLE
    COLUMN =  "",                            //NOMINAL ACCOUNT
    COLUMN =  "",                            //NOMINAL ACCOUNT TITLE
    COLUMN =  "USD",                         //CURRENCY CODE SOURCE
    COLUMN =  "Transaction",                 //CURRENCY TYPE CODE SOURCE
    COLUMN =  "USD",                         //CURRENCY CODE TARGET
    COLUMN =  "Functional",                  //CURRENCY TYPE CODE TARGET
    COLUMN = {TRN_AMOUNT},                   //TRANSACTION AMOUNT
    COLUMN = {TRN_DATE},                     //ACCTG DATE
    COLUMN =  "",                            //RECONCILABLE FLAG
    COLUMN =  "",                            //ADJUSTMENT FLAG
    COLUMN =  "Y",                           //MOVEMENT FLAG
    COLUMN =  "",                            //UNIT OF MEASURE
    COLUMN =  "",                            //STATISTICAL AMOUNT
    COLUMN = {TRN_ID}                       //AUDIT TRAIL
  FROM TRANS_LF.TRANS_LR
