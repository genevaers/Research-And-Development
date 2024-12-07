# ASMcallingJAVA Interface

The motivation for this POC iteration is to enable a GenevaERS view to call Java written "lookup" exits from the GenevaERS performance engine. For example being able to write GenevaERS exits in Java rather than just assembler or 3GL increases its access to programmers. The Java "lookup" exit is a method or collection of methods in a Java class and it is loaded dynamically.

However, after being successful at achieving this goal the solution was generalized to allow any single or multi-threaded program written in assembler, 3GL or 4GL to call Java. This is exposed by the GVBUR70 interface provided here. Two installation verification programs (IVP) are provided one written in assembler and another COBOL (see Generalized Interface to Java, below). This facility is independent of installing GenevaERS Performance Engine.

## Example GenevaERS Java lookup exit (Java class and methods)

The example Java class MyClass.java contains 13 methods. Each of these can be considered a separate user exit in the GenevaERS sense, although they could be combined as variations of the same exit to be used in similar roles.

Compile with: javac MyClass.java

 The class and method names are specified in a small assembler stub exit (GVBJLENV) but will eventually be specified through the GenevaERS workbench. The example uses view number 10903 and reads the GVBDEMO customer file.

## Generalized Interface to Java (GVBUR70)

The sample assembler and COBOL programs TSTUR70.asm and TESTUR70.cbl provide examples of how to call Java from assembler/3GL/4GL using the GVBUR70 API. The programs serve as an IVP and are also useful for stress testing.

For example TSTUR70.asm allows up to 20 MVS subtasks each to make tens of thousands of Java method calls, utilizing a pool of Java threads to service these calls simultaneously using the provided GvbJavaDaemon. The calling assembler/3GL/4GL application makes an initialization (INIT) call via GVBUR70 INIT so the request the daemon to start the specified number of threads.

# Installation

The following installtion instructions apply to using this interface with GenevaERS Performance Engine in addition to using it as a stand-alone API. You must also install the GenevaERS Performance Engine to use the API with Performance Engine.

## Checkout code from Git

Use the following command from Gitbash to check out the repository: git clone git@github.com:genevaers/Research-And-Development.git

## Create directory on USS in you home directory

Use "mkdir DllLib". This will later contain GVBJDLL which is needed by GvbJavaDaemon. This directory must be referenced by LIBPATH for jobs using this interface. For example, directory /u/<your-user-id>/DllLib

## Create MVS datasets required

Either [s]ftp or copy/paste ../ASMcallingJAVA/JCL/MAKELIBS.jcl to your JCL library, modify the JCL for you site and run the job.

## Tailor the names of your home directory and MVS datasets to be copied to MVS

The file ../ASMcallingJAVA/SCRIPT/CPY2MVS contains your user id/home directory and MVS datasets used by SYSTSIN. Either [s]ftp or copy/paste it to your MVS <YOUR-USER-ID>.GVBDEMOJ.SYSTSIN dataset created in the step above. 

## Copy the GVBDEMOJ items needed on MVS

The file ../ASMcallingJAVA/JCL/CPUSSMVS.jcl contains the copy JCL which uses OGETX. Either [s]ftp or copy/past this to your MVS JCL <YOUR-USER-ID>.GVBDEMOJ.JCL library.

## Build GVBJDLL used by GvbJavaDaemon

Set export _C89_SUSRLIB="$LOGNAME.GVBDEMOJ.MACLIB" each time before running build script makegvbdll

Run build script from directory ../ASMcallingJAVA directory to create an MVS DLL in your MVS dataset <YOU-USER-ID>.GVBDEMOJ.LOADLIB:

Enter "make -f SCRIPT/makegvbdll"

There is also version of the script for building a debug version which provides detailed diagnostics SCRIPT/makegvbdlld.

## Build GvbJavaDaemon

Go to directory ../ASMcallingJAVA/Java/GvbJavaDaemon and enter "javac GvbJavaDaemo.java" and similarly compile the examples Java programs MyClass.java and MyClassB.java

## The GvbJavaDaemon

This is a multi-threaded Java daemon that starts the requested number of threads to service calls to Java methods.

Compile with: javac GvbJavaDaemon2.java

## The ZOS DLL JNIASM

This DLL provides the communications between Java and assembler. It comprises modules written in C and assembler, using the JNI interface.

## Perform assemble and link of MVS only components

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
