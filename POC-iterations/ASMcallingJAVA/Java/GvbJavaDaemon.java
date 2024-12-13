/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2024.
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
// Java Daemon to service requests from ASM/3GL and dynamically load and execute user methods and classes
//
import java.nio.charset.StandardCharsets;
//import java.lang.reflect.Method;
import java.util.Arrays;

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
   public static final int RUNMR95  = 1;
   public static final int WAITMR95 = 2;
   public static final int POSTMR95 = 3;
   public static final int RUNMAIN  = 4;

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
      int runrc = 0;
      int dummyRc = 0;

      byte[] byteB = null;
      byte[] arrayIn = {0};
      byte[] retHeader = null;
      String header = null;

      zOSInfo a = new zOSInfo();
      GVBA2I b = new GVBA2I();

      /* --- Invoke Start GVBMR95 ---------------------- */
      byteB = a.showZos(RUNMR95, threadName, "OPTS", arrayIn, dummyRc);
      // only need first 16 (8 + 8) bytes
      retHeader = Arrays.copyOfRange(byteB, 0, 16);
      header = new String(retHeader, StandardCharsets.UTF_8);
      runrc = b.doAtoi(header, 0, 8);

      if ( runrc > 0) {
      System.out.println(threadName + ":RUNMR95  option OPTS returned with rc: " + runrc);
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

// Run Supervisor which starts 'n' threads
class RunSupervisor implements Runnable {
    public static final int RUNMR95  = 1;
    public static final int WAITMR95 = 2;
    public static final int POSTMR95 = 3;
    public static final int RUNMAIN  = 4;

    private Thread t;
    private String threadName;
    private String strin;
    private Integer lenout;
    private String strout;
//    private Integer threadnmbr;
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

       zOSInfo a = new zOSInfo();
       GVBA2I b = new GVBA2I();

       int flag = 0;
       int waitrc = 0;
       int postrc = 0;
       int dummyRc = 0;
       int numberOfThreads = 0;

       byte[] byteB = null;
       byte[] arrayIn = {0};
       byte[] retHeader = null;
       String header = null;

       RunMR95 R1 = new RunMR95( "GVBAPPLTSK", "string1", 0, "string2");
       R1.start();
      
       try {
          /* --- Invoke MVS wait --------------------------- */
          do {
            byteB = a.showZos(WAITMR95, threadName, "GO95", arrayIn, dummyRc);
            // only need first 20 (8 + 8 + 4) bytes
            retHeader = Arrays.copyOfRange(byteB, 0, 20);
            header = new String(retHeader, StandardCharsets.UTF_8);
            waitrc = b.doAtoi(header, 0, 8);

            if (ntrace > 1 ) {
              System.out.println(threadName + ":WAITMR95 option GO95 returned with rc: " + waitrc);
            }

            switch( waitrc ) {
              case 2:
                /* Termination of GVBMR95 has occured without anything happening */
                System.out.println(threadName + ":Application has terminated");
                /* --- Wait a sec for GVBMR95 to properly end */
                Thread.sleep(1000);
                /* Give the worker threads a poke to finish */
                System.out.println(threadName + ":Notify worker tasks to end");
                byteB = a.showZos(POSTMR95, threadName, "WRKT", arrayIn, dummyRc);
                // only need first 16 (8 + 8) bytes
                retHeader = Arrays.copyOfRange(byteB, 0, 16);
                header = new String(retHeader, StandardCharsets.UTF_8);
                postrc = b.doAtoi(header, 0, 8);

                if (ntrace > 1 ) {
                  System.out.println(threadName + ":POSTMR95 option WRKT returned with rc: " + postrc);
                }

                flag = 1;
                break;
            
              case 6:
                /* Initialization of GVBMR95 has occured so go through staturp of Java worker tasks */
                numberOfThreads = b.doAtoi(header, 16, 4);
                System.out.println(threadName + ":Application has requested service for: " + numberOfThreads + " MVS subtask(s)");
                if (numberOfThreads > 1) {
                  System.out.println(threadName + ":starting: " + numberOfThreads + " Java threads");
                } else {
                  System.out.println(threadName + ":starting: " + numberOfThreads + " Java thread");
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
                byteB = a.showZos(POSTMR95, threadName, "ACKG", arrayIn, dummyRc);
                // only need first 16 (8 + 8) bytes
                retHeader = Arrays.copyOfRange(byteB, 0, 16);
                header = new String(retHeader, StandardCharsets.UTF_8);
                postrc = b.doAtoi(header, 0, 8);

                if (ntrace > 1 ) {
                  System.out.println(threadName + ":POSTMR95 option ACKG returned with rc: " + postrc);
                }
                
                /* --- Now wait for MR95 to end --- */
                byteB = a.showZos(WAITMR95, threadName, "TERM", arrayIn, dummyRc);
                // only need first 16 (8 + 8) bytes
                retHeader = Arrays.copyOfRange(byteB, 0, 16);
                header = new String(retHeader, StandardCharsets.UTF_8);
                waitrc = b.doAtoi(header, 0, 8);

                if (ntrace > 1 ) {
                  System.out.println(threadName + ":WAITMR95 option TERM returned with rc: " + waitrc);
                }
                
                /* Give the worker threads a poke to finish */
                System.out.println(threadName + ":Notify worker tasks to end as application has terminated");
                byteB = a.showZos(POSTMR95, threadName, "WRKT", arrayIn, dummyRc);
                // only need first 16 (8 + 8) bytes
                retHeader = Arrays.copyOfRange(byteB, 0, 16);
                header = new String(retHeader, StandardCharsets.UTF_8);
                postrc = b.doAtoi(header, 0, 8);

                if (ntrace > 1 ) {
                  System.out.println(threadName + ":POSTMR95 option WRKT returned with rc: " + postrc);
                }

                flag = 1;
                break;
            
              default:
                System.out.println(threadName + ":Unexpected return code rc: " + waitrc);
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

    public static final int RUNMR95  = 1;
    public static final int WAITMR95 = 2;
    public static final int POSTMR95 = 3;
    public static final int RUNMAIN  = 4;

    private Thread t;
    private String threadName;
    private Integer thrdNbr;
//    private Integer threadNbr;
    private String strin;
    private Integer lenout;
    private String strout;
//    private Integer thrdnbr;
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
    int waitrc = 0;
    String waitreason;
    char flag1;
    char flag2;
    int postrc = 0;
    int dummyRc = 0;
    byte[] dummyRetPayload = {0,0,0,0};
    String workName;
    String javaClass;
    String methodName;
    byte[] byteB = null;
    byte[] arrayIn = {0};
    byte[] retHeader = null;
    String header = null;
    byte[] payload = null;
    byte[] returnPayload = null;
    String strMR95 = "MR95";
    String strUR70 = "UR70";
    byte[] arrayMR95 = strMR95.getBytes(); // major part of WAIT reason code is invoker (GVBMR95)
    byte[] arrayUR70 = strUR70.getBytes(); // major part of WAIT reason code is invoker (GVBUR70 generalized API  )
    byte[] arrayReason = null;
    int caller_ID = 0;

    GVBCLASSLOADER javaClassLoader = new GVBCLASSLOADER();
    zOSInfo a = new zOSInfo();
    GVBA2I b = new GVBA2I();

    GvbX95PJ X95 = new GvbX95PJ(0, 0, null, 0, null, null); // for MR95 use

    threadIdentifier = String.format("%6s%04d", threadName, thrdNbr);
    System.out.println(threadIdentifier + ":Running");

    String thisThrd = String.format("%04d", thrdNbr);
 
    do {
        int exitRc = 0;

        byteB = a.showZos(WAITMR95, threadIdentifier, thisThrd, arrayIn, dummyRc);

        // In this situation we can do nothing other than terminate the daemon
        if (byteB.length < 136) {
          System.out.println(threadName + ":Request byteB length insufficient when returning from WAIT: " + byteB.length);
          System.out.println(threadName + ":Cannot continue. Notify worker tasks to end");
          byteB = a.showZos(POSTMR95, threadName, "WRKT", arrayIn, dummyRc);
          // only need first 16 (8 + 8) bytes
          retHeader = Arrays.copyOfRange(byteB, 0, 16);
          header = new String(retHeader, StandardCharsets.UTF_8);
          postrc = b.doAtoi(header, 0, 8);
          if (ntrace > 1 ) {
            System.out.println(threadName + ":POSTMR95 option WRKT returned with rc: " + postrc);
          }
          break; //exit the loop immediately
        }

        retHeader = Arrays.copyOfRange(byteB, 0, 136);     // Full length of Pass2Struct
        header = new String(retHeader, StandardCharsets.UTF_8);
        waitrc = b.doAtoi(header, 0, 8);
        waitreason = header.substring( 8, 12); // major part of reason code: caller
        flag1     = header.charAt(12);                       // Minor part of reason code: flags
        flag2     = header.charAt(13);

        if (ntrace > 1) {
          System.out.println(threadIdentifier + ":WAIT returned with rc: " + waitrc + " reason: " + waitreason + " flag1: " + flag1 + " flag2: " +flag2);
          System.out.println(threadIdentifier + ":Header: " + header.substring(0,80) + "(length: " + retHeader.length + ") bytes: " + byteB.length);
        }

        switch ( waitrc ) {
          // ASM/3GL/etc application has completed
          case 2:
            System.out.println(threadIdentifier + ":Calling application has terminated. Worker thread completing");
            flag = 1;
            break;

          // Process request from ASM/3GL/etc application
          case 4:
            /* obtain class and method names */
            workName = header.substring(16, 48);
            javaClass = workName.trim();
            workName = header.substring(48, 80);
            methodName = workName.trim();

            /* Process the request */
            numberCalls = numberCalls + 1;
            arrayReason = waitreason.getBytes(); // major part of WAIT reason

            // Identity of caller... an add more caller_ID's
            if (Arrays.equals(arrayReason, 0, 4, arrayMR95, 0, 4)) {
              caller_ID = 1;
            } else {
              if (Arrays.equals(arrayReason, 0, 4, arrayUR70, 0, 4)) {
                caller_ID = 2;
              } else {
                caller_ID = 0; // unrecognized
              }
            }

            // Process request types
            switch ( caller_ID ) {
              // When request comes from GVBMR95 logic path
              case 1:
                if (byteB.length <= 148) {
                  System.out.println(threadIdentifier + ":GVBMR95 request is too short to be valid, VIEW disabled");
                  exitRc = 12;            // Too short to be valid request, disable view
                  returnPayload = Arrays.copyOfRange(dummyRetPayload,0,dummyRetPayload.length);

                } else {                  // valid request length. Try to call GvbX95process method requiring JZOS

                  X95 =javaClassLoader.invokeClassMethod("GvbX95process", "GvbX95prepare",
                                                         X95, header, byteB, threadIdentifier, ntrace);
                  if (X95 == null) {
                    System.out.println(threadIdentifier + ":Class GvbX95process not found. Cannot process GVBMR95 request, VIEW disabled");
                    exitRc = 12;          // Class GvbX95process not available, disable view: RC = 12
                    returnPayload = Arrays.copyOfRange(dummyRetPayload,0,dummyRetPayload.length);

                  } else {                // Required class GvbX95process is available

                    payload = Arrays.copyOfRange(byteB, 148, byteB.length);
 
                    if (ntrace > 1) {
                      System.out.println(threadIdentifier + ":Class: " + javaClass + " Method: " + methodName + " Payload length: " + payload.length );
                      System.out.print(threadIdentifier + ":Request payload: ");
                      int maxI = Math.min(48,payload.length);
                      for (int i = 0; i < maxI; i++)
                      {
                          System.out.print(String.format("%02X", payload[i]));
                      }
                      System.out.println();
                    }

                    // Try to call user specified classs and method
                    ReturnData returnData = javaClassLoader.invokeClassMethod(javaClass, methodName, X95, payload);

                    if (returnData == null) {
                      System.out.println(threadIdentifier + ":Class " +  javaClass + " method " + methodName + " not found, VIEW disabled");
                      exitRc = 12;        // User's class method not available, disable view: RC = 12
                      returnPayload = Arrays.copyOfRange(dummyRetPayload,0,dummyRetPayload.length);

                    } else {              // user's Java was invoked successfully}

                      returnPayload = returnData.getPayload();
                      exitRc = returnData.getRc();

                      if (ntrace > 1 ) {
                        System.out.println(threadIdentifier + ":Back from " + methodName + ": exitRc = " + exitRc + " Return payload length: " + returnPayload.length);
                        System.out.print(threadIdentifier + ":Return payload:  ");
                        for (int i = 0; i < returnPayload.length; i++)
                        {
                          System.out.print(String.format("%02X", returnPayload[i]));
                        }
                        System.out.println();
                      }
                    }
                  }
                }

                byteB = a.showZos(POSTMR95, threadIdentifier, thisThrd+flag1+flag2, returnPayload, exitRc);
                retHeader = Arrays.copyOfRange(byteB, 0, 16); // only need first 16 bytes (Return + Reason code)
                header = new String(retHeader, StandardCharsets.UTF_8);
                postrc = b.doAtoi(header, 0, 8);
                if (postrc != 0) {
                  System.out.println(threadIdentifier + ":POSTMR95 " + thisThrd + " returned with rc: " +  postrc);
                }
                break;

               // When request comes from GVBUR70 logic path 
              case 2:
                payload = Arrays.copyOfRange(byteB, 136, byteB.length);

                if (ntrace > 1) {
                  System.out.println(threadIdentifier + ":Class: " + javaClass + " Method: " + methodName + " Payload length: " + payload.length );
                  System.out.print(threadIdentifier + ":Request payload: ");
                  int maxI = Math.min(48,payload.length);
                  for (int i = 0; i < maxI; i++)
                  {
                      System.out.print(String.format("%02X", payload[i]));
                  }
                  System.out.println();
                }

                // Try to call user specified classs and method
                returnPayload = javaClassLoader.invokeClassMethod(javaClass, methodName, payload);
                if (returnPayload == null) {
                  System.out.println(threadIdentifier + ":Class " +  javaClass + " method " + methodName + " not found, cannot be executed");
                  exitRc = -1;        // User's class method not available, ensure this is known
                  returnPayload = Arrays.copyOfRange(dummyRetPayload,0,dummyRetPayload.length);
                
                } else {              // user's Java was invoked successfully}

                  exitRc = 0;         //   the only Java Rc returned from methods for UR70 calls for now
                  if (ntrace > 1 ) {
                    System.out.println(threadIdentifier + ":Back from " + methodName + ": exitRc = " + exitRc + " Return payload length: " + returnPayload.length);
                    System.out.print(threadIdentifier + ":Return payload:  ");
                    for (int i = 0; i < returnPayload.length; i++)
                    {
                      System.out.print(String.format("%02X", returnPayload[i]));
                    }
                    System.out.println();
                  }
                }

                byteB = a.showZos(POSTMR95, threadIdentifier, thisThrd+flag1+flag2, returnPayload, exitRc);
                retHeader = Arrays.copyOfRange(byteB, 0, 16); // only need first 16 bytes (Return + Reason code)
                header = new String(retHeader, StandardCharsets.UTF_8);
                postrc = b.doAtoi(header, 0, 8);
                if (postrc != 0) {
                  System.out.println(threadIdentifier + ":POSTUR70 " + thisThrd + " returned with rc: " +  postrc);
                }
                break;

              default:
                System.out.println(threadIdentifier + ":Unrecognized request -- WAIT reason: " + waitreason + ". Worker thread completing");
                returnPayload = Arrays.copyOfRange(dummyRetPayload,0,dummyRetPayload.length);
                exitRc = -2;

                byteB = a.showZos(POSTMR95, threadIdentifier, thisThrd+flag1+flag2, returnPayload, exitRc);
                retHeader = Arrays.copyOfRange(byteB, 0, 16); // only need first 16 bytes (Return + Reason code)
                header = new String(retHeader, StandardCharsets.UTF_8);
                postrc = b.doAtoi(header, 0, 8);
                if (postrc != 0) {
                  System.out.println(threadIdentifier + ":POSTXXXX " + thisThrd + " returned with rc: " +  postrc);
                }
                break;

            }   // end switch ( caller_ID )
            break;
          
          default:
            System.out.println(threadIdentifier + ":Unrecognized return code when returning from WAIT: " + waitrc + ". Worker thread completing");
            System.out.println(threadName + ":Cannot continue. Notify worker tasks to end");
            byteB = a.showZos(POSTMR95, threadName, "WRKT", arrayIn, dummyRc);
            // only need first 16 (8 + 8) bytes
            retHeader = Arrays.copyOfRange(byteB, 0, 16);
            header = new String(retHeader, StandardCharsets.UTF_8);
            postrc = b.doAtoi(header, 0, 8);
            if (ntrace > 1 ) {
              System.out.println(threadName + ":POSTMR95 option WRKT returned with rc: " + postrc);
            }
            flag = 1;
            break; //exit the loop immediately

        }   // end switch( waitrc )

    } while (flag == 0);

    System.out.println(threadIdentifier + ":Notified to exit");
    
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
  public static final int RUNMR95  = 1;
  public static final int WAITMR95 = 2;
  public static final int POSTMR95 = 3;
  public static final int RUNMAIN  = 4;
  public static final String ThreadName = "JAVAMAIN  ";

   public static void main(String args[]) {

      String gvbdebug = System.getenv("GVBDEBUG");

      System.out.println("GvbJavaDaemon Started. Environment variable GVBDEBUG: " + gvbdebug);
      zOSInfo a = new zOSInfo();
      GVBA2I b = new GVBA2I();
      GvbRunInfo runinfo = new GvbRunInfo(0,0);

      byte[] byteB = null;
      byte[] arrayIn = {0};
      byte[] retHeader = null;
      String header = null;
      int rc = 0;
      int dummyRc = 0;

      int nArgs =args.length;
      Integer trace = 0;
      int i;

      if ( gvbdebug != null ) {
        if (gvbdebug.equals("3")) {
          trace = 3;
        }
      }

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

      /* --- Call program to initialize communication memory --- */
      byteB = a.showZos(RUNMAIN, ThreadName, "OPTS", arrayIn, dummyRc);
      retHeader = Arrays.copyOfRange(byteB, 0, 16);
      header = new String(retHeader, StandardCharsets.UTF_8);
      rc = b.doAtoi(header, 0, 8);

      /* --- Run thread supervisor ----------------- */
      RunSupervisor R2 = new RunSupervisor( "Supervisor", "string1", 16, "string2", trace, runinfo);
      R2.start();
   }   
}