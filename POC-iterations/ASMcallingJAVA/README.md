# ASMcallingJAVA

The motivation for this POC interation is to enable a GenevaERS view to call Java written "lookup" exits from the performance engine. For example being able to write GenevaERS exits in Java rather than just assembler or 3GL increases its access to programmers. The Java program is a method or collection of methods in a class and is loaded dynamically.

The scope was later widened to call Java from any single or multi-threaded assembler or 3GL program, via the GVBUR70 interface provided by GenevaERS.

## Example GenevaERS Java lookup exit (Java class and methods)

The example Java class MyClass.java contains 13 methods. Each of these can be considered a separate user exit in the GenevaERS sense, although they could be combined as variations of the same exit to be used in similar roles.

Compile with: javac MyClass.java

 The class and method names are specified in a small assembler stub exit (GVBJLENV) but will eventually be specified through the GenevaERS workbench. The example uses view number 10903 and reads the GVBDEMO customer file.

## Sample assembler program to call Java

TSTUR70.ASM provides an example of how to call Java from assembler/3GL. They are also useful test harnesses when developing Java exits for GenevaERS. The GVBUR70 INIT call will start the requested number of threads.

## The GvbJavaDaemon

This is a multi-threaded Java daemon that starts the requested number of threads to service calls to Java methods.

Compile with: javac GvbJavaDaemon2.java

## The ZOS DLL JNIASM

This DLL provides the communications between Java and assembler. It comprises modules written in C and assembler, using the JNI interface.

Build with: make -f makejni2

## Assembling and linking GVBUR70 and other assembler written portions

The JCL asmjv.jcl assembles GVBUR70, GVBJLENV and TSTUR70. lnkjv.jcl link edits these modules.

## Running the assembler GVBUR70 test program

runur70t.jcl runs the test program tstur70 (main program) which calls Java methods and classes, passing and returning data, according to what's specified in the calling interface parameters.

## Running performance engine and calling Java exits

runmr9j.jcl is a regular GenevaERS extract job that contains some additional components (job steps), for example copying the ZOS DLL load module into the required LIBPATH and starting the JVM.


Learn more about GenevaERS at [GenevaERS Training](https://genevaers.org/training-videos/).  Join in the conversation at the [#GenevaERS channel on Open Mainframe Project Slack](https://slack.openmainframeproject.org). After requesting access with the above link, look for the [GenevaERS channel](https://openmainframeproject.slack.com/archives/C01711931GA)

This repo is managed according to the policies listed in the [GenevaERS Community Repo](https://github.com/genevaers/community)

## Contributing
Anyone can contribute to the GenevaERS project - learn more at [CONTRIBUTING.md](https://github.com/genevaers/community/blob/master/CONTRIBUTING.md)

## Governance
GenevaERS is a project hosted by the [Open Mainframe Project](https://openmainframeproject.org). This project has established it's own processes for managing day-to-day processes in the project at [GOVERNANCE.md](https://github.com/genevaers/community/blob/master/GOVERNANCE.md).

## Reporting Issues
To report a problem, you can open an [issue](https://github.com/genevaers/gvblib/issues) in repository against a specific workflow. If the issue is sensitive in nature or a security related issue, please do not report in the issue tracker but instead email  genevaers-private@lists.openmainframeproject.org.
