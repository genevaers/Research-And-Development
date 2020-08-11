#!/usr/bin/env bash
###########################################################################
#
#  VA Universal Ledger Pre-process
#
#	Copyright 2019, IBM Corporation.  All rights reserved.
#     Kip Twitchell <finsysvlogger@gmail.com>.
#                   kip.twitchell@us.ibm.com
#  Created June 2019
#
#  UPDATE: - Customize the below to be your data paths
#
#  TODO Update comments below here.
#	Shell script to transform state of VA financial data steps include:
#
#   1 Extract header records and put columns into consistent order
#   2 sort input transactions into vendor (insturment) order
#   3 it then calls rhw instIDAssign scala program to assign instrument IDs
#   4 Make backup of any existing data
#   5 Renames outputs from program as inputs to next program
#   6 Calls the instPosting scala program to post transactions to balance and update Instrument Table
#   7 Backs up inputs and renames files in preparation for next files do loop processing
#   8 After processing the Revenue File (end of every year) the balance file is renamed and new file begun
#
#   Parameters include:
#   - List of files to be processed
#	- It tests the list year to decide which format to be transformed, 2003-2011, 2012-13, 2014 on
#   - Test the appended value to determine if Expenses only, Revenue only, or Both should be processed
#	- Loop through multiple files, changing the file names based upon the supplied parameters.
#
#   Set-up Required:
#   The script and program requires these files be in the directory and have valid values in them
#       1 - maxIDFileName=:  format is maxKeySysID, maxKeyInstID, maxKeyBalID, maxKeyTranID, maxKeyTimeStamp, maxKeyComment
#       2 - instIDFileName:  format is VendorName, VendorID
#       3 - VATestBalTBL.txt can be empty
#        VATestInstTBL.txt
#        VATestInstUPD.txt
#        VATestSumTBL.txt
#        VATestTranTBL.txt
#
###########################################################################


echo "*********************************************************************"
echo "*********************************************************************"
echo "               START OF VA UNIVERSAL LEDGER PRE-PROCESS"
echo "*********************************************************************"
echo "*********************************************************************"

if [ $# -gt 0 ]; then
    echo "Your command line contains $# arguments; running under Vagrant"
    #-------->Update for your data paths
    inputPath="/VAData/data"
    outputPath="/VAData/output"
else
    echo "Your command line contains no arguments; NOT running under Vagrant"
    #-------->Update for your data paths
    inputPath="/Users/ktwitchell001/workspace/universal_ledger_data/data/"
    outputPath="/Users/ktwitchell001/workspace/universal_ledger_data/output/"
fi

echo "Input Path: "$inputPath
echo "Output Path: "$outputPath

echo "Starting time:  $(date -u)"
#export CLASSPATH=/Users/ktwitchell001/workspace/cdscVADataDemoSys/target/scala-2.11/classes
#echo "Classpath: "$CLASSPATH
echo "Process ID: "$$
SECONDS=0
echo "Start Seconds: "$SECONDS

maxIDFileName="VATestMaxID.txt"  #contains the max identifiers for instruments, balances, etc.  Read and written by
 # both programs, and passed as input to the next program processing the transaction files.
instIDFileName="VATestInstID.txt" # Keyed by Vendor Name, used to ID new vendors and assign Vendor (Instrument) IDs to them
instTBLFileName="VATestInstTBL.txt" # Key by Instrument ID, the only meaningful attribute is the Vendor Name
balTBLFileName="VATestBalTBL.txt"  # After processing the Rev file, the balance file is renamed to B1xxx at bottom of script
tranTBLFileName="VATestTranTBL.txt" # this file is a temporary file.  The perm file is it the T1xxx file named below
instUPDFileName="VATestInstUPD.txt" # this is a temporary file.  the permanent file is the U1xxx file named below
sumTBLFileName="VATestSumTBL.txt"  # for future use; no use in current instance, except program writes it
agencyTabFileName="aRefAgencyTab.txt"  #used on revenue data to put the agencyTab name as the instrument ID field
agencyFileName="aRefAgencyCSV.txt"  #used on 2014 - 16 revenue data to put the agencyTab name as the instrument ID field


# file name syntax is the file name less "xp.txt" for expenses and "ev.txt" for revenue files, where the name is
# "FY" + nn for fiscal year + "q" + n for the quarter on expense files, nothing on rev files + "e" for expense, "r" revenue
fileList=$inputPath"FY*"
fileCount=0

for dirFile in $fileList
do



    file=$(basename $dirFile)
    # Set and manipulate varFables

    # these file names are not passed to the Scala Program
    # input file to the awk statements
    if [ ${#file} -ge 12 ] && [ ${file:7:1}="e" ]
     then
        filetype="E"
        inputFile=$file
        outputTRN="T1"${file:2:5}"xp.txt"
        outputUPDi="U1"${file:2:5}"xp.txt"
        outputRAW="Raw"${file:2:5}"xp.txt"
        fiscalMM="0"${file:5:1} # assumed first month of quarter

     else
        if [ ${file:5:1}="r" ]
            then
                filetype="R"
                inputFile=$file
                outputTRN="T1"${file:2:3}"ev.txt"
                outputUPDi="U1"${file:2:3}"ev.txt"
                outputRAW="Raw"${file:2:3}"ev.txt"
                outputBAL="B1"${file:2:2}"bal.txt"   # rename of file only occcurs on Revenue Files
                fiscalMM="01" # assumed first month of year
            else
                filetype=" "
                echo "ERROR IN FILETYPE; NEITHER EXPENSE OR REVENUE"
                exit
         fi
    fi
    fiscalCC="20"
    fiscalYY=${file:2:2}
    slash="/"
    fiscalPeriod=$fiscalCC$fiscalYY$slash$fiscalMM
    acctgPeriod=$fiscalCC$fiscalYY$slash$fiscalMM$slash'01'
    echo "Fiscal Date: "$fiscalPeriod
    echo "Accounting Date: "$acctgPeriod

    echo "---------------------------------------------------------------------"
    echo "---------------------------------------------------------------------"
    echo "         BEGINNING OF NON-THREAD PROCESSING FOR FILE: " + $inputFile
    echo "File Type: "$filetype" Input File: "$inputFile" Output File: "$outputTRN
    echo "---------------------------------------------------------------------"
    echo "---------------------------------------------------------------------"

    ###########################################################################
    # Operations perform
    #    1 - awk = remove the header row which begins with "
    #          reorder fields and
    #          change from tab delimited to CSV to be consistent with later years
    #          add fiscal year and accounting date and transaction date
    #          to end of each row, as specified in the variables above
    #    2 - sort  = sort the file by the reordered 1th field, the vendor name
    #        to prepare input to assign instrument IDs based upon vendor name changes
    #    3 - run the match/merge scala program to
    #           - assign instrument IDs to transactions
    #           - create new instrument IDs for new vendors
    #           - format initial transaction records
    # Enhancements to be made:
    #    - Add record count subtotals to awk statement for processing control
    ###########################################################################

    echo "Create new empty balance file for next thread processing "$outputRAW
    \touch "$inputPath"$outputRAW$"InstID0.txt"
    \touch "$inputPath"$outputRAW$"InstID1.txt"
    \touch "$inputPath"$outputRAW$"InstID2.txt"
    \touch "$inputPath"$outputRAW$"InstID3.txt"
    \touch "$inputPath"$outputRAW$"InstID4.txt"
    \touch "$inputPath"$outputRAW$"InstID5.txt"
    \touch "$inputPath"$outputRAW$"InstID6.txt"
    \touch "$inputPath"$outputRAW$"InstID7.txt"
    \touch "$inputPath"$outputRAW$"InstID8.txt"
    \touch "$inputPath"$outputRAW$"InstID9.txt"


    # Run 2003 - 2011 EXPENSES
    echo "Start Seconds: "$SECONDS
    if [ $filetype == "E" ]
        then
            case $fiscalYY in
                03|04|05|06|07|08|09|10|11)
                      echo $inputFile" 03 - 11 Expense File"
                          # Output COLS          from Inputs:
                         # Col 1 Insturment ID  from Vendor
                         # Col 2 Company        from Agency
                         # Col 3 Center         From Fund
                         # Col 4 Project        From Program
                         # Col 5 Account        from Source
                         # Col 6 Amount         from Amount
                         # Col 7 Fis Date       from File Date
                         # Col 8 Acct Date      from File Date
                         # Col 9 Tran Date      From File Date
                         # Col 10 File Type     From Constant

                      awk  \
                         -v fis="$fiscalPeriod" -v acct="$acctgPeriod"  \
                         'BEGIN{ \
                         OFS="," ; FS="\t" \
                         } \
                          (!/^\"/) \
                         { \
                         gsub(",", "-", $5); \
                         gsub("  ", "", $5); \
                         gsub(/ \"+$/, "\"", $5); \
                         gsub(/\" +/, "\"", $5); \
                         gsub("\342", " ", $5);gsub("\200", " ", $5);gsub("\271", " ", $5);  \
                         gsub("\337", " ", $5);gsub("\253", " ", $5);gsub("\334", " ", $5);  \
                         gsub("\261", " ", $5);  \
                         printf "%s,",toupper($5); \
                         printf "%s,",$1; \
                         printf "%s,",$2; \
                         printf "%s,",$4; \
                         printf "%s,",$3; \
                         printf "%s,",substr($6,1,length($6)-1); \
                         printf "%s,",fis; \
                         printf "%s,",acct; \
                         printf "%s,",acct; \
                         printf "%s","EXP"; \
                         printf("\n") \
                         }'  \
                         $inputPath$inputFile | \
                      awk  \
                         -v path="$inputPath" -v file="$outputRAW" \
                         -F, \
                         '{if($1<="\"A")print >      (path file "InstID0.txt");  \
                         else {if($1<="\"C")print > (path file "InstID1.txt");  \
                         else {if($1<="\"E")print > (path file "InstID2.txt");  \
                         else {if($1<="\"H")print > (path file "InstID3.txt");  \
                         else {if($1<="\"J")print > (path file "InstID4.txt");  \
                         else {if($1<="\"N")print > (path file "InstID5.txt");  \
                         else {if($1<="\"Q")print > (path file "InstID6.txt");  \
                         else {if($1<="\"T")print > (path file "InstID7.txt");  \
                         else {if($1<="\"W")print > (path file "InstID8.txt");  \
                         else {if($1>"\"V")print >  (path file "InstID9.txt");  \
                         }}}}}}}}}}' > /dev/null
                      ;;
                12|13)
                      echo $inputFile" 12 - 13 Expense File"
                         # -v sets variables to the fiscal period and acctg period, for use in cols 7, 8, (9)
                         # OFS changes output format to CSV flie, rather than FS input parm of tab delimited
                         # filter lines (1) removes the header row which begins with " in the first position
                         #              (2) removes any extra commas inside the Vendor Name field (input column 5)
                          # Output COLS          from Inputs:
                         # Col 1 Insturment ID  from Vendor
                         # Col 2 Company        from Agency
                         # Col 3 Center         From Fund
                         # Col 4 Project        From Program
                         # Col 5 Account        from Source
                         # Col 6 Amount         from Amount
                         # Col 7 Fis Date       from File Date
                         # Col 8 Acct Date      from File Date
                         # Col 9 Tran Date      From voucher date
                         # Col 10 File Type     From Constant

                      awk  \
                         -v fis="$fiscalPeriod" -v acct="$acctgPeriod"  \
                         'BEGIN{ \
                         OFS="," ; FS="\t" \
                         } \
                          (!/^\"/) \
                         { \
                         gsub(",", "-", $5); \
                         gsub("  ", "", $5); \
                         gsub(/ \"+$/, "\"", $5); \
                         gsub(/\" +/, "\"", $5); \
                         gsub("\254", " ", $5);gsub("\246", " ", $5);gsub("\277", " ", $5);  \
                         gsub("\307", " ", $5);gsub("\326", " ", $5);gsub("\367", " ", $6);  \
                         gsub("\324", " ", $5);gsub("\364", " ", $5); \
                         printf "%s,",toupper($5); \
                         printf "%s,",$1; \
                         printf "%s,",$2; \
                         printf "%s,",$4; \
                         printf "%s,",$3; \
                         printf "%s,",$6; \
                         printf "%s,",fis; \
                         printf "%s,",acct; \
                         printf "%s,",substr($7,1,length($7)-1); \
                         printf "%s","EXP"; \
                         printf("\n") \
                         }'  \
                         $inputPath$inputFile | \
                      awk  \
                         -v path="$inputPath" -v file="$outputRAW" \
                         -F, \
                         '{if($1<="\"A")print >      (path file "InstID0.txt");  \
                         else {if($1<="\"C")print > (path file "InstID1.txt");  \
                         else {if($1<="\"E")print > (path file "InstID2.txt");  \
                         else {if($1<="\"H")print > (path file "InstID3.txt");  \
                         else {if($1<="\"J")print > (path file "InstID4.txt");  \
                         else {if($1<="\"N")print > (path file "InstID5.txt");  \
                         else {if($1<="\"Q")print > (path file "InstID6.txt");  \
                         else {if($1<="\"T")print > (path file "InstID7.txt");  \
                         else {if($1<="\"W")print > (path file "InstID8.txt");  \
                         else {if($1>"\"V")print >  (path file "InstID9.txt");  \
                         }}}}}}}}}}' > /dev/null
                      ;;
                14|15|16|17)
                      echo $inputFile" 14 - 17 Expense File"
                         # -v sets variables to the fiscal period and acctg period, for use in cols 7, 8, (9)
                         # OFS changes output format to CSV flie, rather than FS input parm of tab delimited
                         # filter lines (1) removes the header row which begins with " in the first position
                         #              (2) removes any extra commas inside the Vendor Name field (input column 5)
                          # Output COLS          from Inputs:
                         # Col 1 Insturment ID  from Vendor
                         # Col 2 Company        from Agency
                         # Col 3 Center         From Fund
                         # Col 4 Project        From Program
                         # Col 5 Account        from Source
                         # Col 6 Amount         from Amount
                         # Col 7 Fis Date       from File Date
                         # Col 8 Acct Date      from File Date
                         # Col 9 Tran Date      From voucher date
                         # Col 10 File Type     From Constant

                      awk  \
                         -v fis="$fiscalPeriod" -v acct="$acctgPeriod"  \
                         'BEGIN{ \
                         OFS="," ; FS="," \
                         } \
                          (!/^\"/) \
                         { \
                         gsub(",", "-", $6); \
                         gsub("  ", "", $6); \
                         gsub(/ \"+$/, "\"", $6); \
                         gsub(/\" +/, "\"", $6); \
                         gsub("\254", " ", $6);gsub("\246", " ", $6);gsub("\277", " ", $6);  \
                         gsub("\307", " ", $6);gsub("\326", " ", $6);gsub("\323", "\042", $6);  \
                         gsub("\324", " ", $6);gsub("\364", " ", $6);gsub("\363", " ", $6); \
                         gsub("\311", " ", $6);gsub("\367", " ", $6);gsub("\355", " ", $6); \
                         printf "%s,",toupper($6); \
                         printf "%s,",$1; \
                         printf "%s,",$3; \
                         printf "%s,",$5; \
                         printf "%s,",$4; \
                         printf "%s,",$2; \
                         printf "%s,",fis; \
                         printf "%s,",acct; \
                         printf "%s,",substr($7,1,length($7)-1); \
                         printf "%s","EXP"; \
                         printf("\n") \
                         }'  \
                         $inputPath$inputFile | \
                      awk  \
                         -v path="$inputPath" -v file="$outputRAW" \
                         -F, \
                         '{if($1<="\"A")print >      (path file "InstID0.txt");  \
                         else {if($1<="\"C")print > (path file "InstID1.txt");  \
                         else {if($1<="\"E")print > (path file "InstID2.txt");  \
                         else {if($1<="\"H")print > (path file "InstID3.txt");  \
                         else {if($1<="\"J")print > (path file "InstID4.txt");  \
                         else {if($1<="\"N")print > (path file "InstID5.txt");  \
                         else {if($1<="\"Q")print > (path file "InstID6.txt");  \
                         else {if($1<="\"T")print > (path file "InstID7.txt");  \
                         else {if($1<="\"W")print > (path file "InstID8.txt");  \
                         else {if($1>"\"V")print >  (path file "InstID9.txt");  \
                         }}}}}}}}}}' > /dev/null


                      ;;
            esac
        else  # Revenue Files
            case $fiscalYY in
                03|04|05|06|07|08|09|10|11)
                      echo $inputFile" 03 - 11 Revenue File"
                    # Run 2003 - 2011 REVENUE
                         # Output COLS          from Inputs:
                         # Col 1 Insturment ID  from Agency
                         # Col 2 Company        from Agency (yes repeated value)
                         # Col 3 Center         From Fund
                         # Col 4 Project        Blank
                         # Col 5 Account        from Source
                         # Col 6 Amount         from Amount
                         # Col 7 Fis Date       from File Date
                         # Col 8 Acct Date      from File Date
                         # Col 9 Tran Date      From File Date
                         # Col 10 File Type     From Constant
                      awk  \
                         -v fis="$fiscalPeriod" -v acct="$acctgPeriod"  \
                         'BEGIN{ \
                         OFS="," ; FS="\t" \
                         } \
                         FNR==NR{(!/^\"/);hash[$2]=$3;next} \
                         !/^\"/ \
                         { \
                         printf "%s,","\""hash[$1]"\""; \
                         printf "%s,",$1; \
                         printf "%s,",$2; \
                         printf "%s,","\" \""; \
                         printf "%s,",$3; \
                         printf "%s,",substr($4,1,length($4)-1); \
                         printf "%s,",fis; \
                         printf "%s,",acct; \
                         printf "%s,",acct; \
                         printf "%s","REV"; \
                         printf("\n") \
                         }'  \
                         $inputPath$agencyTabFileName $inputPath$inputFile | \
                      awk  \
                         -v path="$inputPath" -v file="$outputRAW" \
                         -F, \
                         '{if($1<="\"A")print >     (path file "InstID0.txt");  \
                         else {if($1<="\"C")print > (path file "InstID1.txt");  \
                         else {if($1<="\"E")print > (path file "InstID2.txt");  \
                         else {if($1<="\"H")print > (path file "InstID3.txt");  \
                         else {if($1<="\"J")print > (path file "InstID4.txt");  \
                         else {if($1<="\"N")print > (path file "InstID5.txt");  \
                         else {if($1<="\"Q")print > (path file "InstID6.txt");  \
                         else {if($1<="\"T")print > (path file "InstID7.txt");  \
                         else {if($1<="\"W")print > (path file "InstID8.txt");  \
                         else {if($1>"\"V")print >  (path file "InstID9.txt");  \
                         }}}}}}}}}}' > /dev/null
                      ;;
                12|13)
                      echo $inputFile" 12 - 13 Revenue File"
                    # Run 2003 - 2011 REVENUE
                         # Output COLS          from Inputs:
                         # Col 1 Insturment ID  from Agency
                         # Col 2 Company        from Agency (yes repeated value)
                         # Col 3 Center         From Fund
                         # Col 4 Project        Blank
                         # Col 5 Account        from Source
                         # Col 6 Amount         from Amount
                         # Col 7 Fis Date       from File Date
                         # Col 8 Acct Date      from File Date
                         # Col 9 Tran Date      from Deposit Date
                         # Col 10 File Type     From Constant
                      awk  \
                         -v fis="$fiscalPeriod" -v acct="$acctgPeriod"  \
                         'BEGIN{ \
                         OFS="," ; FS="\t" \
                         } \
                         FNR==NR{(!/^\"/);hash[$2]=$3;next} \
                         !/^\"/ \
                         { \
                         printf "%s,","\""hash[$1]"\""; \
                         printf "%s,",$1; \
                         printf "%s,",$4; \
                         printf "%s,","\" \""; \
                         printf "%s,",substr($5,1,length($5)-1); \
                         printf "%s,",$2; \
                         printf "%s,",fis; \
                         printf "%s,",acct; \
                         printf "%s,",$3; \
                         printf "%s","REV"; \
                         printf("\n") \
                         }'  \
                         $inputPath$agencyTabFileName $inputPath$inputFile | \
                      awk  \
                         -v path="$inputPath" -v file="$outputRAW" \
                         -F, \
                         '{if($1<="\"A")print >     (path file "InstID0.txt");  \
                         else {if($1<="\"C")print > (path file "InstID1.txt");  \
                         else {if($1<="\"E")print > (path file "InstID2.txt");  \
                         else {if($1<="\"H")print > (path file "InstID3.txt");  \
                         else {if($1<="\"J")print > (path file "InstID4.txt");  \
                         else {if($1<="\"N")print > (path file "InstID5.txt");  \
                         else {if($1<="\"Q")print > (path file "InstID6.txt");  \
                         else {if($1<="\"T")print > (path file "InstID7.txt");  \
                         else {if($1<="\"W")print > (path file "InstID8.txt");  \
                         else {if($1>"\"V")print >  (path file "InstID9.txt");  \
                         }}}}}}}}}}' > /dev/null
                      ;;
                14|15|16|17)
                      echo $inputFile" 14 - 17 Revenue File"
                    # Run 2003 - 2011 REVENUE
                         # Output COLS          from Inputs:
                         # Col 1 Insturment ID  from Agency
                         # Col 2 Company        from Agency (yes repeated value)
                         # Col 3 Center         From Fund
                         # Col 4 Project        Blank
                         # Col 5 Account        from Source
                         # Col 6 Amount         from Amount
                         # Col 7 Fis Date       from File Date
                         # Col 8 Acct Date      from File Date
                         # Col 9 Tran Date      from Deposit Date
                         # Col 10 File Type     From Constant
                      awk  \
                         -v fis="$fiscalPeriod" -v acct="$acctgPeriod"  \
                         'BEGIN{ \
                         OFS="," ; FS="," \
                         } \
                         FNR==NR{(!/^\"/);hash[$2]=$3;next} \
                         !/^\"/ \
                         { \
                         printf "%s,","\""hash[$1]"\""; \
                         printf "%s,",$1; \
                         printf "%s,",$4; \
                         printf "%s,","\" \""; \
                         printf "%s,",substr($5,1,length($5)-1); \
                         printf "%s,",$2; \
                         printf "%s,",fis; \
                         printf "%s,",acct; \
                         printf "%s,",$3; \
                         printf "%s","REV"; \
                         printf("\n") \
                         }'  \
                         $inputPath$agencyFileName $inputPath$inputFile | \
                      awk  \
                         -v path="$inputPath" -v file="$outputRAW" \
                         -F, \
                         '{if($1<="\"A")print >     (path file "InstID0.txt");  \
                         else {if($1<="\"C")print > (path file "InstID1.txt");  \
                         else {if($1<="\"E")print > (path file "InstID2.txt");  \
                         else {if($1<="\"H")print > (path file "InstID3.txt");  \
                         else {if($1<="\"J")print > (path file "InstID4.txt");  \
                         else {if($1<="\"N")print > (path file "InstID5.txt");  \
                         else {if($1<="\"Q")print > (path file "InstID6.txt");  \
                         else {if($1<="\"T")print > (path file "InstID7.txt");  \
                         else {if($1<="\"W")print > (path file "InstID8.txt");  \
                         else {if($1>"\"V")print >  (path file "InstID9.txt");  \
                         }}}}}}}}}}' > /dev/null
                      ;;
          esac
    fi

    echo "Phase 0 End Seconds: "$SECONDS" "$inputFile
    echo "---------------------------------------------------------------------"
    echo "---------------------------------------------------------------------"
    echo "         BEGINNING OF THREAD PROCESSING FOR FILE: " + $inputFile
    echo "---------------------------------------------------------------------"
    echo "---------------------------------------------------------------------"



    thrdprocess () {

        echo "Phase 1 Start Seconds: "$SECONDS" "$inputFile" "$thrd

                  sort -k 1 $inputPath$outputRAW$thrd | \
              scala /Users/ktwitchell001/workspace/cdscVADataDemoSys/src/main/scala/instIDAssign.scala \
                    $inputPath $outputPath $maxIDFileName$thrd $instIDFileName$thrd \
                     $outputTRN$thrd $instUPDFileName$thrd

        echo "Phase 1 End Seconds: "$SECONDS" "$inputFile" "$thrd

        ###########################################################################
        # backup and rename routines
        ###########################################################################

        backup () {
            for file in $inputPath"$@"; do
                local new=`basename "$file"`.$(date '+%Y%m%d-%H%M%S')
                while [[ -f $new ]]; do
                    new+="~";
                done;
                printf "Backing up '%s' to '%s'\n" "$inputPath"`basename "$file"` "$new"".txt";
                #\cp -ip "$file" "$new";
                \mv "$inputPath"`basename "$file"` "$inputPath$new"."$inputFile"".txt";
            done
        }

        rename () {
            for file in $inputPath"$@"; do
                local new=`basename "$file"`
                while [[ -f $new ]]; do
                    new+="~";
                done;
                printf "Renaming '%s' to '%s'\n" "$inputPath""new"`basename "$file"` "$new";
                #\cp -ip "$file" "$new";
                \mv "$inputPath""new"`basename "$file"` "$inputPath$new";
            done
        }

        echo "Sort UPD Transaction File "$inputFile" "$thrd
        # Sort InstUPD File prior to rename step below
        sort -k 1 $outputPath"new"$instUPDFileName$thrd > $outputPath"new"$outputUPDi$thrd #the sort order on this might be wrong!!!!!!!
#        echo "End Sort UPD Transaction File"

        # backup input files to last execution, excluding the input transaction file
        backup $maxIDFileName$thrd $instIDFileName$thrd

        echo "Backup complete "$inputFile" "$thrd

        # rename output files from last execution to make them input files to this execution
        rename $maxIDFileName$thrd $instIDFileName$thrd $outputTRN$thrd $outputUPDi$thrd

        echo "Rename complete "$inputFile" "$thrd

        echo "Phase 2 Sort Start Seconds: "$SECONDS" "$inputFile" "$thrd
        sort -t ',' -k 1,1 -k 5,16 -k 18,18 $outputPath$outputTRN$thrd |
        # sort -k 1 $outputPath$tranTBLFileName > $outputPath$outputTRN
        # echo "Phase Sort End Seconds: "$SECONDS

        # echo "Phase Phase 2 Start Seconds: "$SECONDS
        scala /Users/ktwitchell001/workspace/cdscVADataDemoSys/src/main/scala/instPosting.scala \
             $inputPath $outputPath $maxIDFileName$thrd \
            $instTBLFileName$thrd $balTBLFileName$thrd $tranTBLFileName$thrd $outputUPDi$thrd $sumTBLFileName$thrd
        echo "Phase 2 End Seconds: "$SECONDS" "$inputFile" "$thrd

        # backup input files to last execution
        backup $maxIDFileName$thrd $balTBLFileName$thrd $instTBLFileName$thrd

        echo "Backup complete "$inputFile" "$thrd

        # rename output files from last execution to make them input files to this execution
        rename $maxIDFileName$thrd $balTBLFileName$thrd $instTBLFileName$thrd

        if [ $filetype == "R" ]
            then
            # Because the data only has year buckets which = the balance file, no need to re-read balance file for
            # Next input file.  No update to balances will occur across years
            # The programs are capable of update if more input data is more frequent than
            echo "******************************************************************************************************"
            echo "Renaming Annual Balance File: "$inputPath$balTBLFileName$thrd" to "$outputBAL$thrd
            \mv "$inputPath"$balTBLFileName$thrd "$inputPath"$outputBAL$thrd
            echo "******************************************************************************************************"
            echo "Create new empty balance file for next year's processing"
            \touch $inputPath$balTBLFileName$thrd
        fi

        echo "Rename complete "$inputFile" "$thrd

    }

    (thrd="InstID0.txt"; thrdprocess) &
    (thrd="InstID1.txt"; thrdprocess) &
    (thrd="InstID2.txt"; thrdprocess) &
    (thrd="InstID3.txt"; thrdprocess) &
    (thrd="InstID4.txt"; thrdprocess) &
    (thrd="InstID5.txt"; thrdprocess) &
    (thrd="InstID6.txt"; thrdprocess) &
    (thrd="InstID7.txt"; thrdprocess) &
    (thrd="InstID8.txt"; thrdprocess) &
    (thrd="InstID9.txt"; thrdprocess) &
    wait


    fileCount=$[fileCount+1]
    echo "Input File count processed: "$fileCount" "$inputFile
    echo "File: "$inputFile" end process time:  $(date -u)"



    ###########################################################################
done  # end of loop
    ###########################################################################

    echo "Ending time:  $(date -u)"

echo "*********************************************************************"
echo "*********************************************************************"
echo "               END OF VA UNIVERSAL LEDGER PRE-PROCESS"
echo "*********************************************************************"
echo "*********************************************************************"

#!/usr/bin/env bash