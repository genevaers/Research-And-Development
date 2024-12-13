# POC-Iterations

The GenevaERS R&D Repo contains the results of research by the project team, exploring new spaces the project might go.  We typically work on one project at a time, and archive earlier projects to the R&D Archive Branch to avoid having to clean vulnerable dependent code in older projects.

The Current project is:

## ASMcallingJAVA

This POC builds upon the experience gained in JNI2ASM to do the opposite. That is to call a generalized Java Class and Method from a single or multi-threaded assembler program. We began by enabling GenevaERS Performance Engine to dynamically call Java written "exits".  We generalized this case to allow any application to call java in a multiple threaded mode (it is not dependent upon GenevaERS).

For more details see [Detailed Readme](POC-iterations/ASMcallingJAVA/README.md)

# Archived projects
Earlier project in reverse chronological order are:

## JNI2ASM
JNI2ASM seeks to emulate the technique for calling IBM assembler using an example shown in the Longpela web site. https://www.longpelaexpertise.com.au/ezine/HLASMfromJava.php

## Scala2ASM
This POC implements a mechanism for calling IBM assembler code from Scala and was done in 2020.

## UniversalLedger
Universal Ledger Spark POC was from Spring of 2019.

## NGSAFR CKB
This is a Scala implementation of GenevaERS common key buffer and was done in 2018.

## JZOS SPARK Test
This POC investigates integration of Spark with SAFR (GenevaERS) and was conducted in 2015. This POC provide source code and other materials relevant to the GenevaERS integration with Spark, and thus are contributed by IBM to the GenevaERS project.  

# About GenevaERS

Learn more about GenevaERS at [GenevaERS Training](https://genevaers.org/training-videos/).  Join in the conversation at the [#GenevaERS channel on Open Mainframe Project Slack](https://slack.openmainframeproject.org). After requesting access with the above link, look for the [GenevaERS channel](https://openmainframeproject.slack.com/archives/C01711931GA)

This repo is managed according to the policies listed in the [GenevaERS Community Repo](https://github.com/genevaers/community)

## Contributing
Anyone can contribute to the GenevaERS project - learn more at [CONTRIBUTING.md](https://github.com/genevaers/community/blob/master/CONTRIBUTING.md)

## Governance
GenevaERS is a project hosted by the [Open Mainframe Project](https://openmainframeproject.org). This project has established it's own processes for managing day-to-day processes in the project at [GOVERNANCE.md](https://github.com/genevaers/community/blob/master/GOVERNANCE.md).

## Reporting Issues
To report a problem, you can open an [issue](https://github.com/genevaers/gvblib/issues) in repository against a specific workflow. If the issue is sensitive in nature or a security related issue, please do not report in the issue tracker but instead email  genevaers-private@lists.openmainframeproject.org.
