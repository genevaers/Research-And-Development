# jzos-sparkTest

This repository contains new POC code started July 2020 at the initiation of the GenevaERS proejct.  You can read more about the scope and objectives of this test [here](https://genevaers.org/2020/07/29/organize-and-commit-to-the-spark-genevaers-poc/)

This code is combining code from the ngsafr POC (which had scala calling jzos) and the Universal Ledger POC (which called Spark) to create a single module which calls jzos from Apache Spark.

This code requires it be run on z/OS as jzos IO does not work locally.