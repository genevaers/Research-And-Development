/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
//
// Java Daemon to service request from ASM/3GL and dynamically load and execute user methods and classes
//
import java.lang.reflect.Method;

// Backup statistics class
class GvbRunInfo {
  public long ncalls;
  public long nthread;

    // Constructor
    public GvbRunInfo(long ncalls, long nthread)
    {
        this.ncalls = ncalls;
        this.nthread = nthread;
    }
    public long getnCalls() { return ncalls; }
    public long getnThread() { return nthread; }
    public synchronized void addnCalls(long ncalls) { this.ncalls = this.ncalls + ncalls; }
    public synchronized void subnThread() { this.nthread = this.nthread - 1; }
    public void setnThread(long nthread) {this.nthread = nthread; }
}

// Run MR95 (or other target program)
class RunMR95 implements Runnable {
   private Thread t;
   private String threadName;
   private String strin;
   private Integer lenout;
   private String strout;

   RunMR95( String name, String stringin, Integer lengthout, String stringout) {
      threadName = name;
      strin = stringin;
      lenout = lengthout;
      strout = stringout;

      System.out.println(threadName + ":Creating");
   }
   
   public void run() {
      System.out.println(threadName + ":Running");
      String result;
      int runrc = 0;

      zOSInfo2 a = new zOSInfo2();
      GVBA2I b = new GVBA2I();

      /* --- Invoke Start GVBMR95 ---------------------- */
      result = a.showZos2(1, "RUNMR95 ", "OPTS", "DARTH", lenout);
      runrc = b.doAtoi(result, 0, 8);
      System.out.println(threadName + ":RUNMR95  option OPTS returned with rc: " + runrc);
      System.out.println(threadName + ":Exiting");
   }
   
   public void start () {
      System.out.println(threadName + ":Starting");
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}

// Run Supervisor which starts 'n' threads
class RunSupervisor implements Runnable {
    private Thread t;
    private String threadName;
    private String strin;
    private Integer lenout;
    private String strout;
    private Integer threadnmbr;
    private Integer ntrace;
    private GvbRunInfo runinfo;
  
    RunSupervisor( String name, String stringin, Integer lengthout, String stringout, Integer trace, GvbRunInfo RunInfo) {
       threadName = name;
       strin = stringin;
       lenout = lengthout;
       strout = stringout;
       ntrace = trace;
       runinfo = RunInfo;
 
    System.out.println(threadName + ":Creating");
    }
    
    public void run() {
       System.out.println(threadName + ":Running");

       zOSInfo2 a = new zOSInfo2();
       GVBA2I b = new GVBA2I();

       int flag = 0;
       int waitrc = 0;
       int postrc = 0;
       int numberOfThreads = 0;
       String result;

       RunMR95 R1 = new RunMR95( "GVBMR95TSK", "string1", 0, "string2");
       R1.start();
      
       try {
          /* --- Invoke MVS wait --------------------------- */
          do {
            result = a.showZos2(2, "WAITMR95", "GO95", "DARTH", lenout);
            System.out.println(threadName + ":Detailed diagnostics: " + result);
            waitrc = b.doAtoi(result, 0, 8);
            System.out.println(threadName + ":WAITMR95 option GO95 returned with rc: " + waitrc);
            System.out.println(threadName + ":Detailed diagnostics: " + result);

            switch( waitrc ) {
              case 2:
                /* Termination of GVBMR95 has occured without anything happening */
                System.out.println(threadName + ":GVBMR95 has completed");
                /* --- Wait a sec for GVBMR95 to properly end */
                Thread.sleep(1000);
                /* Give the worker threads a poke to finish */
                System.out.println(threadName + ":give worker tasks a poke");
                result = a.showZos2(3, "POSTMR95", "WRKT", "DARTH", lenout);
                postrc = b.doAtoi(result, 0, 8);
                System.out.println(threadName + ":POSTMR95 option WRKT returned with rc: " + postrc);
                flag = 1;
                break;
            
              case 6:
                /* Initialization of GVBMR95 has occured so go through staturp of Java worker tasks */
                numberOfThreads = b.doAtoi(result, 16, 4);
                System.out.println(threadName + ":GVBMR95 has started: " + numberOfThreads + " MVS subtask(s)");
                if (numberOfThreads > 1) {
                  System.out.println(threadName + ":is starting: " + numberOfThreads + " Java threads");
                } else {
                  System.out.println(threadName + ":is starting: " + numberOfThreads + " Java thread");
                }


                /* --- Start the workers */
                runinfo.setnThread(numberOfThreads);
                for(int i = numberOfThreads; i > 0; i--) {
                    RunWorker R3 = new RunWorker( "Worker", i, "string1", 0, "string2", ntrace, runinfo);
                    R3.start();
                }
                /* --- Wait a sec for workers to start */
                Thread.sleep(1000);

                /* --- Post MR95 to continue */
                result = a.showZos2(3, "POSTMR95", "ACKG", "DARTH", lenout);
                postrc = b.doAtoi(result, 0, 8);
                System.out.println(threadName + ":POSTMR95 option ACKG returned with rc: " + postrc);
                System.out.println(threadName + ":Detailed diagnostics: " + result);
                
                /* --- Now wait for the end to come --- */
                result = a.showZos2(2, "WAITMR95", "TERM", "DARTH", lenout);
                waitrc = b.doAtoi(result, 0, 8);
                System.out.println(threadName + ":WAITMR95 option TERM returned with rc: " + waitrc);
                System.out.println(threadName + ":Detailed diagnostics: " + result);
                
                /* Give the worker threads a poke to finish */
                System.out.println(threadName + ":give worker tasks a poke");
                result = a.showZos2(3, "POSTMR95", "WRKT", "DARTH", lenout);
                postrc = b.doAtoi(result, 0, 8);
                System.out.println(threadName + ":POSTMR95 option WRKT returned with rc: " + postrc);
                System.out.println(threadName + ":Detailed diagnostics: " + result);
                flag = 1;
                break;
            
              default:
                System.out.println(threadName + ":Unexpected return code");
                flag = 1;
                break;
            }

          } while (flag == 0);
       }
       catch (InterruptedException e) {
          System.out.println(threadName + ":Interrupted.");
       }
       System.out.println(threadName + ":Exiting");
    }
    
    public void start () {
       System.out.println(threadName + ":Starting");
       if (t == null) {
          t = new Thread (this, threadName);
          t.start ();
       }
    }
 }

 // Run worker that executes methods dynamically
 class RunWorker implements Runnable {
    @SuppressWarnings({ "rawtypes", "unchecked" })

    private Thread t;
    private String threadName;
    private Integer thrdNbr;
    private Integer threadNbr;
    private String strin;
    private Integer lenout;
    private String strout;
    private Integer thrdnbr;
    private String threadIdentifier;
    private Integer ntrace;
    private GvbRunInfo runinfo;

 RunWorker( String name, Integer threadNbr, String stringin, Integer lengthout, String stringout, Integer trace, GvbRunInfo RunInfo) {
    threadName = name;
    thrdNbr = threadNbr;
    strin = stringin;
    lenout = lengthout;
    strout = stringout;
    ntrace = trace;
    runinfo = RunInfo;

    threadIdentifier = String.format("%6s%04d", threadName, threadNbr);
    System.out.println(threadIdentifier + ":Creating");
 }
 
 public void run() {
    int flag = 0;
    int numberCalls = 0;
    String recvData;

    zOSInfo2 a = new zOSInfo2();
    GVBA2I b = new GVBA2I();
    GVBCLASSLOADER javaClassLoader = new GVBCLASSLOADER();           // Load and execute on the fly
    GVBCLASSLOADER2 javaClassLoader2 = new GVBCLASSLOADER2();        // Separate load and execute functions

    Class aarg[] = new Class[1];

    javaClassLoader.invokeClassMethod("MyClass", "MethodA","STUFF");

    threadIdentifier = String.format("%6s%04d", threadName, thrdNbr);
    System.out.println(threadIdentifier + ":Running");
 
    do {
        String result;
        int waitrc = 0;
        int postrc = 0;
        int lenout_wait = 64; //16;
        int lenout_post = 0;
        int i;
        int j = 999; // to give a logic error if calculated incorrectly
        String workName;
        String javaClass;
        String methodName;
        String sentData;
        int sentLen;
        
        String thisThrd = String.format("%04d", thrdNbr);

        result = a.showZos2(2, "WAITMR95", thisThrd, "DARTH", lenout_wait);
        waitrc = b.doAtoi(result, 0, 8);

        if (ntrace > 1) {
          System.out.println(threadIdentifier + ":WAITMR95 option " + thisThrd + " returned with rc: " + waitrc);
          System.out.println(threadIdentifier + ":Detailed diagnostics: " + result);
        }

        switch ( waitrc ) {
          // GVBMR95 has completed
          case 2:
            System.out.println(threadIdentifier + ":GVBMR95 has completed");
            flag = 1;
            break;

          // a request from GVBMR95
          case 4:
            sentLen = result.length();
            if (sentLen < 80) { // 80 bytes is 16 char return+reason code plus 32 char each for Class|Method
               System.out.println("Data received is too short at length: " + sentLen);
            }

            /* obtain class and method names */
            workName = result.substring(16, 47);
            javaClass = workName.trim();
            workName = result.substring(48, 79);
            methodName = workName.trim();

            /* sensible format of request */
            if (sentLen > 80) {
               workName = result.substring(80, sentLen);
               sentData = workName.trim();
            } else {
               sentData = null;
            }

            if (ntrace > 1) {
              System.out.println("Class:" + javaClass + ":method:" + methodName + ":sent:" + sentData + ".");
            }
            
            /* Process the request */
            numberCalls = numberCalls + 1;
            //recvData = javaClassLoader2.executeClassMethod(method[j], sentData);
            recvData = javaClassLoader.invokeClassMethod(javaClass, methodName, sentData);
            if (ntrace > 1 ) {
              System.out.println("Back:" + recvData);
            }
            
            result = a.showZos2(3, "POSTMR95", thisThrd, recvData, lenout_post);
            postrc = b.doAtoi(result, 0, 8);
            break;
          
          default:
            System.out.println(threadIdentifier + ":GVBMR95 has completed");
            flag = 1;
            break;
        }
    } while (flag == 0);
    System.out.println(threadIdentifier + ":About to exit");
    
    try {
    Thread.sleep(50);
    } catch (InterruptedException e) {
      System.out.println(threadIdentifier + ":Interrupted.");
    } 

    System.out.println(threadIdentifier + ":Exiting. Number of calls: " + numberCalls);
    runinfo.addnCalls(numberCalls); //serialized addition
    runinfo.subnThread();
    long nthread = runinfo.getnThread();
    if (nthread == 0) { /* last one */
      long ncalls = runinfo.getnCalls();
      System.out.println("GvbJavaDaemon exiting after servicing " + ncalls + " method calls");
   }
 }
 
 public void start () {
    threadIdentifier = String.format("%6s%04d", threadName, thrdNbr);
    System.out.println(threadIdentifier + ":Starting" );
    if (t == null) {
       t = new Thread (this, threadIdentifier);
       t.start ();
    }
  }
 }

public class GvbJavaDaemon {

   public static void main(String args[]) {

      System.out.println("GvbJavaDaemon Started:");
      zOSInfo2 a = new zOSInfo2();
      GvbRunInfo runinfo = new GvbRunInfo(0,0);

      int nArgs =args.length;
      String strin = "DARTH";
      int   lenout = 1024;
      Integer trace = 0;
      int i;

      for (i = 0; i < nArgs; i++) {
        if (args[i].substring(0,1).equals("-"))
        {
          switch( args[i].substring(1,2)) {
            case "D":
              trace = 3;
              break;
            default:
              break;
          }
        }
      }
      System.out.println("GvbJavaDaemon trace level: " + trace);

      /* --- Do some class loading --------------------- */
      GVBCLASSLOADER javaClassLoader = new GVBCLASSLOADER();
      GVBCLASSLOADER2 javaClassLoader2 = new GVBCLASSLOADER2();
      javaClassLoader.invokeClassMethod("MyClass", "MethodA","STUFF");
      javaClassLoader.invokeClassMethod("MyClass", "MethodB", "STUFF");
      javaClassLoader.invokeClassMethod("MyClass", "MethodC", "STUFF");
      System.out.println("Done class loader tests!!");

      /* --- Get Hostname ------------------------------ */
      System.out.print("Host:    ");
      System.out.println(a.showZos2(0, "SYSINFO ", "OPTS", "HOST", lenout));
      System.out.println(strin + lenout);

      /* --- Get Sysplex Name: ------------------------- */
      System.out.print("Sysplex: ");
      System.out.println(a.showZos2(0, "SYSINFO ", "OPTS", "SYSPLEX", lenout));
      System.out.println(strin + lenout);

      /* --- Get IPL time: ------------------------- */
      System.out.print("Ipltime: ");
      System.out.println(a.showZos2(0, "SYSINFO ", "OPTS", "IPLTIME", lenout));
      System.out.println(strin + lenout);

      /* --- Run GVBMAIN --------------------------- */
      System.out.print("Gvbmain: ");
      System.out.println(a.showZos2(4, "RUNMAIN ", "OPTS", "DARTH", lenout));
      System.out.println(strin + lenout);

      /* --- Run thread supervisor ----------------- */
      RunSupervisor R2 = new RunSupervisor( "Supervisor", "string1", 16, "string2", trace, runinfo);
      R2.start();
   }   
}