# spark-poc

This project will test intersections and interactions between GenevaERS and Apache Spark. When complete, this repo will include the following:

- An overview of the POC (here in this document)
- Source data assets, including links to publicly available data files and scripts to convert them into appropriate formats
- Spark code to produce a baseline output
- Spark code leveraging jzos, the z/OS java IO routines
- Spark code leveraging genlib, GenevaERS encapsulated utlities to perform GenevaERS function in a stand alone mode
- GenevaERS XML defining GenevaERS processes to produce the same outputs.

This project has been built with the hope that others can download, run, and analyze the outputs.  This will give a sense of GenevaERS capabilities, and how they compare to the more well-known Apache Spark capabilities.  

Here is the framework being considered for the next generation GenevaERS.
![Slide3](https://user-images.githubusercontent.com/29467627/88852211-95280300-d1b3-11ea-8eec-f106e61bcefa.jpeg)

This outlines the Spark POC approach
![Slide4](https://user-images.githubusercontent.com/29467627/88852298-b4269500-d1b3-11ea-857a-8998ae55b04c.jpeg)

And this starts to describe what will be learned from this POC
![GenevaERS and Spark POC](https://user-images.githubusercontent.com/29467627/88857602-3915ac80-d1bc-11ea-8905-a8041c566a2e.jpg)

More information about the initiative can be found at the [GenevaERS.org website](https://genevaers.org/2020/07/29/organize-and-commit-to-the-spark-genevaers-poc/) activity entry.

The plan for the POC is as follows:

Week Ending-Tasks to be complete as of end of week

8/4/20

  [ ] Maven build of Spark jzos on z/OS

  [ ] Initial design of outputs

  [ ] Data design complete, ASCII, ftp, zip, etc.

8/11/20

  [ ] Have UR20 working under Spark

	[ ] Final design of outputs to be produced

	[ ] Sample data ready for use

8/18/20

  [ ] Spark code complete

	[ ] UR45 conversion complete

	[ ] Full data conversion with partiitioning

	[ ] GenevaERS views built and run

8/25/20

  [ ] Initial runs on cloud, and z/OS

	[ ] Repartitioning, performance tuning

	[ ] Execution of following configs
	- [ ] Spark on Cloud
	- [ ] Spark/Jzos
	- [ ] Spark UR20
	- [ ] Spark UR45
	- [ ] GenevaERS

9/1/20

  [ ] Execution, tuning, testing, rerun

9/8/20

  [ ] End of technical work, build presentation

9/15/20

  [ ] OMP Presentation
