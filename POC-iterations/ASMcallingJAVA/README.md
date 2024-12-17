# ASMcallingJAVA Interface

The motivation for this POC iteration is to enable GenevaERS views (i.e. program) to call Java written "lookup" exits efficiently from the GenevaERS Performance Engine. For example, being able to write GenevaERS exits in Java rather than just assembler or 3GL increases its access to programmers. The Java "lookup" exit is a method or collection of methods in a Java class, and it is loaded dynamically. The JVM is started only once regardless of how many times Java is called.

However, after being successful at achieving this goal, the solution was generalized to allow any single or multi-threaded program written in assembler, 3GL or 4GL to call Java. This is exposed by the GVBUR70 interface (API) provided here. Two installation verification programs (IVP) are included, one written in assembler and another COBOL (see Generalized Interface to Java - below). This facility is independent of installing GenevaERS Performance Engine.

## Example GenevaERS Java lookup exit (Java class and methods)

The example Java class MyClass.java contains 13 methods. Each of these can be considered a separate user exit in the GenevaERS sense, although they could be combined as variations of the same exit to be used in similar roles.

The class and method names are specified in a small assembler stub exit (GVBJLENV) but will eventually be specified through the GenevaERS workbench. The example uses view number 10903 and reads the GVBDEMO customer file.

## Generalized Interface to Java (GVBUR70)

The sample assembler and COBOL programs TSTUR70.asm and TESTUR70.cbl provide examples of how to call Java from assembler/3GL/4GL using the GVBUR70 API. The programs serve as an IVP and are also useful for stress testing.

For example, TSTUR70.asm allows up to 20 MVS subtasks each to make tens of thousands of Java method calls, utilizing a pool of Java threads to service these calls simultaneously supported by the GvbJavaDaemon included. The calling assembler/3GL/4GL application makes an initialization (INIT) call via the GVBUR70 API to request the GvbJaveDaemon to start the specified number of threads. Subsequently, it makes CALL requests to invoke Java classes and methods.

# Installation

The following installation instructions apply to using this interface with GenevaERS Performance Engine and for using it as a stand-alone API. You must also install the GenevaERS Performance Engine to use the API with Performance Engine. Some steps are performed from MVS TSO and others from USS with the bash shell.

## Create directory on USS in your home directory

After logging onto USS enter "mkdir DllLib". This will later contain the GVBJDLL needed by GvbJavaDaemon. This directory must be referenced by LIBPATH for jobs using this interface. Specifically, directory /u/"your-user-id"/DllLib

## Checkout code from Git

Create the appropriate sub-directory for example /u/"your-user-id"/git/public using "mkdir" and go to this subdirectory. Then enter the following command to check out the repository:
"git clone git@github.com:genevaers/Research-And-Development.git".

## Create required MVS datasets

Either [s]ftp or copy/paste ../ASMcallingJAVA/JCL/MAKELIBS.jcl to a JCL library, modify the JCL for you site and run the job on MVS. This job will create the following datasets:

   "YOUR-TSO-PREFIX".GVBDEMOJ.ASM

   "YOUR-TSO-PREFIX".GVBDEMOJ.BTCHOBJ

   "YOUR-TSO-PREFIX".GVBDEMOJ.COBOL

   "YOUR-TSO-PREFIX".GVBDEMOJ.COPY

   "YOUR-TSO-PREFIX".GVBDEMOJ.EXP
   
   "YOUR-TSO-PREFIX".GVBDEMOJ.JCL

   "YOUR-TSO-PREFIX".GVBDEMOJ.LOADLIB

   "YOUR-TSO-PREFIX".GVBDEMOJ.MACLIB

   "YOUR-TSO-PREFIX".GVBDEMOJ.SYSTIN

   "YOUR-TSO-PREFIX".GVBDEMOJ.SYSADATA

   "YOUR-TSO-PREFIX".GVBDEMOJ.ASMLANGX

## Tailor the names of your home directory and MVS datasets to be copied to MVS

The file ../ASMcallingJAVA/SCRIPT/CPY2MVS contains your user id/home directory and MVS datasets that is used by SYSTSIN to perform OGETX. Either [s]ftp or copy/paste it to your MVS "YOUR-USER-ID".GVBDEMOJ.SYSTSIN dataset created in the previous step then tailor the file names as required. 

## Copy the GVBDEMOJ items needed on MVS

The file ../ASMcallingJAVA/JCL/CPUSSMVS.jcl contains the copy JCL which uses OGETX as defined in SYSTSIN. Either [s]ftp or copy/past this file to your MVS JCL "YOUR-USER-ID".GVBDEMOJ.JCL library and run the job on MVS.

## Build the GVBDEMOJ items needed on MVS

Run jobs ASMDEMOJ and LNKDEMOJ on MVS from the JCL library where they were copied in the previous step. Tailor them first to contain your USER-ID as &HLQ. These jobs build the following LOAD modules:

JLKUPEX, GVBJLENV, GVBUR70, TSTUR70 and GVBJGO95

## Build GVBJDLL used by GvbJavaDaemon

Back in USS set the following environment variale: export _C89_SUSRLIB="$LOGNAME.GVBDEMOJ.MACLIB" each time before running build script makegvbdll (it can be added to your .profile). Also ensure the Java "include" library in the build script referencing the jni.h headers matches the location of the Java "include" directory at your site. For example, the build script provided contains:
 ```C_DLLFLAGS  = -c -V -Wc,"LP64,EXPORTALL,CSECT($@)" -I"$(JNIINC),/Java/J8.0_64/include" $(DEFS)```

Run build script from directory ../ASMcallingJAVA directory to create an MVS DLL in your MVS dataset "YOU-USER-ID".GVBDEMOJ.LOADLIB

Enter "make -f SCRIPT/makegvbdll"

There is also version of the script for building a debug version which provides detailed diagnostics: SCRIPT/makegvbdlld.

### Pre-built load modules and libraries

The GVBJDLL is available for download if you do not have the IBM C or compatible compiler. The download comprises the following XMIT files therefore avoiding having to build any of the MVS load modules or copy JCL, sources, etc.:

"YOUR-TSO-PREFIX".GVBDEMOJ.ASM

"YOUR-TSO-PREFIX".GVBDEMOJ.COBOL

"YOUR-TSO-PREFIX".GVBDEMOJ.COPY

"YOUR-TSO-PREFIX".GVBDEMOJ.EXP

"YOUR-TSO-PREFIX".GVBDEMOJ.JCL

"YOUR-TSO-PREFIX".GVBDEMOJ.LOADLIB

"YOUR-TSO-PREFIX".GVBDEMOJ.MACLIB

"YOUR-TSO-PREFIX".GVBDEMOJ.SYSTSIN

The XMIT files should be downloaded and copied as BINARY files before a TSO RECEIVE is issued, for example:

```
//RECVSTEP EXEC PGM=IKJEFT01
//SYSPRINT DD  SYSOUT=*
//FROMF1   DD  DISP=SHR,DSN="YOUR-TSO-PREFIX".GVBDEMOJ.LOADLIB.XMIT
//SYSTSPRT DD  SYSOUT=*
//SYSTSIN  DD  *
 PROFILE NOPREFIX
 RECEIVE INFILE(FROMF1)
 DSNAME('"YOUR-TSO-PREFIX".GVBDEMOJ.LOADLIB')
//*
```

## Build GvbJavaDaemon

Go to directory ../ASMcallingJAVA/Java/GvbJavaDaemon/Java and enter:

"javac GvbJavaDaemo.java"

"javac GvbX95process.java"

The latter requires JZOS to be present and is needed to run Performance Engine calling Java lookup exits.

Similarly compile the examples Java programs MyClass.java and MyClassB.java.

## Copy profile script ASMcallingJAVAprofile

Copy profile script ../ASMcallingJAVA/SCRIPT/ASMcallingJAVAprofile to you home directory and tailor it for your user ID. This profile is referenced by jobs using the GVBUR70 interface (API).

## Running the IVP program TSTUR70

From MVS run the IVP program using JCL RUNUR70T to call Java, after tailoring the following items:

```
1: Your USER-ID as specified by &HLQ
2: COPYDLL step DD SYSTSIN statement to contain USS "your-user-id".
3: TSTUR70 step DD STDENV statement to contain USS "your-user-id" (ensure both occurrences for this DD statement are changed).
4: TSTUR70 step DD DDEXEC statement to specify the number of threads and calls performed by each thread.
   PARM='TASKS=20,NCALL=32767 are the maximums for the IVP program.
```

## Running your own program using the GVBUR70 API to call Java

Take a copy of the RUNUR70T JCL with the edits made in the previous step, then add the additional DD statements your program requires and modify the DDEXEC statement:

PGM="YOUR-PROGRAM",PARM='...'

You own program must contain statements that call the GVBUR70 API for it to invoke Java classes and methods. See programming constructs below. If it does not invoke the GVBUR70 API your job will just run normally as if the interface were not present.

## Running GenevaERS Performance Engine to call Java

Sample JCL RUNMR95J is provided. The JCL requires the same edits as the RUNUR70T JCL except for the DDEXEC statement. The datasets required are already defined in the JCL. Running the Performance-Engine assumes you have already installed the GenevaERS DEMO.

RUNMR95 JCL is a regular GenevaERS extract job that contains some additional components (job steps), for example copying the ZOS DLL load module into the required LIBPATH and starting the JVM.

Learn more about GenevaERS at [GenevaERS Training](https://genevaers.org/training-videos/).  Join in the conversation at the [#GenevaERS channel on Open Mainframe Project Slack](https://slack.openmainframeproject.org). After requesting access with the above link, look for the [GenevaERS channel](https://openmainframeproject.slack.com/archives/C01711931GA)

This repo is managed according to the policies listed in the [GenevaERS Community Repo](https://github.com/genevaers/community)

# Programming constructs

Two verbs are currently supported by the GVBUR70 API:

INIT: This function requests the initialization of the GVBUR70 interface and specifies the number of concurrent Java threads to be provided. The number of threads is provided as a half word binary integer (two bytes) and must be between 1 and 99.

CALL: This function requests the invocation of a specified Java class and method and supplies a SEND and RECEIVE buffer to communicate data with Java.

```
UR70VERS: version of GVBUR70 half word of 1.
UR70FLG1: character value 'U' must be specified to distinguish the caller from GVBMR95 Performance Engine
UR70FLG2: character value '0' means the payload data is transferred as-is without converting from EBCDIC to ASCII and back
          character value '1' means the payload data is converted from EBCDIC to ASCII and back
UR70CLSS: specifies the Java class to be loaded (32 bytes)
UR70METH: specifies the Java method to be executed (32 bytes)
UR70LSND: full word indicates the length of the caller's send buffer in bytes
UR70LRCV: full word indicates the number of bytes available in the caller's receive buffer
UR70LRET: full word return code from GVBUR70 (same as R15)
UR70ANCH: archor full word -- do not alter or reset this field
UR70JRET: return code from the Java method, where available
UR70LREQ: if truncation occurs (LRET=4) this field indicates the receive buffer length required to receive all the data
```

Return codes are documented in the source code of GVBUR70.

Assembler program TSTUR70 and COBOL program TESTUR70 show examples of these calls and the API data areas.

# Contributing
Anyone can contribute to the GenevaERS project - learn more at [CONTRIBUTING.md](https://github.com/genevaers/community/blob/master/CONTRIBUTING.md)

## Governance
GenevaERS is a project hosted by the [Open Mainframe Project](https://openmainframeproject.org). This project has established its own processes for managing day-to-day processes in the project at [GOVERNANCE.md](https://github.com/genevaers/community/blob/master/GOVERNANCE.md).

## Reporting Issues
To report a problem, you can open an [issue](https://github.com/genevaers/gvblib/issues) in repository against a specific workflow. If the issue is sensitive in nature or a security related issue, please do not report in the issue tracker but instead email  genevaers-private@lists.openmainframeproject.org.
