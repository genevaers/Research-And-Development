# spark-poc

This document will try to capture a set of proposed outputs for the POC.  In order to be an accurate testimonial for the various combinations of software suggested below, the outputs must exercise the known capabilities of the combinations of both spark and geneva as well as a baseline for the geneva product.  

A proper test should cover the following conditions:

- It should be repeatable by anyone caring to run the tests
- It should have data of sufficient volume to produce reasonable volumes of output.
- It should be quantifiable and scalable and test should be run with multiple volumes of data.
- Data should be sufficient for Spark efficiencies to be apparent with some tests.
- Multithreading should be used. 
- Multiple outputs should be produced to encompass GenenvaERS one pass architecture. 


The following outputs could be produced from the VA Data:

Summary structures listed with possible tests.
All outputs should be produced where possible in one pass of the data or as with performance in mind.
Additionally, the test should be run with 1 year, 2 years and 3 years of data for 3 data points. 

1.) Balance by source and fund summarized by fiscal year.
Layout: Fiscal Year, Source, source name, fund name, balance. 
    This will test a very large reference file.

2.) Balance by source and program.
Layout: source, source name, program name, balance 
    Simplistic summarization with a lookup

3.) Balance by Vendor, program and Fiscal year.
Layout: Fiscal year, Vendor, Vendor name, program name, Balance 
    This will test a very large vendor pool. 
    It will have a lookup for the program name. 
    It will summarize balances and group by year. 

Data needed for test.
    Several years of revenue and expense data each in csv format. 
    Program reference data in fixed format. 
    Vendor reference data in fixed format.
    Source reference data in fixed format. 

    As with everything, please comment make changes 