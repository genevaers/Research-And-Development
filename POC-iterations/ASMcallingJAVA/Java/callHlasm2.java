/* -------------------------------------------------------
   callHlasm - Call ASMINF HLASM module from Java
   ------------------------------------------------------ */

class callHlasm2 {
  
   public static void main(String[] args) throws Exception {
       System.out.println("(callHlasm2) Started:");
       zOSInfo2 a = new zOSInfo2();

       String strin = "DARTH";
       int   lenout = 1024;

       /* --- Get Hostname ------------------------------ */
       System.out.print("Host:    ");
       System.out.println(a.showZos2("SYSINFO", "HOST", lenout));
       System.out.println(strin + lenout);

       /* --- Get Sysplex Name: ------------------------- */
       System.out.print("Sysplex: ");
       System.out.println(a.showZos2("SYSINFO", "SYSPLEX", lenout));
       System.out.println(strin + lenout);

       /* --- Get IPLTIME: ------------------------------ */
       System.out.print("Sysplex: ");
       System.out.println(a.showZos2("SYSINFO", "IPLTIME", lenout));
       System.out.println(strin + lenout);

       /* --- Invoke Start GVBMR95 ---------------------- */
       System.out.print("GVBJMR95:");
       System.out.println(a.showZos2("RUNMR95", "DARTH", lenout));
       System.out.println(strin + lenout);

       /* --- Invoke MVS wait --------------------------- */
       System.out.print("GVBJWAIT:");
       System.out.println(a.showZos2("WAITMR95", "DARTH", lenout));
       System.out.println(strin + " : " + lenout);

       /* --- Invoke MVS post --------------------------- */
       System.out.print("GVBJPOST:");
       System.out.println(a.showZos2("POSTMR95", "DARTH", lenout));
       System.out.println(strin + " : " + lenout);

       /* --- Specify an invalid parameter -------------- */
       System.out.print("Bad Parm: ");
       System.out.println(a.showZos2("JUNK", "DARTH", lenout));
       System.out.println(strin + " : " + lenout);

       System.out.println("(callHlasm2) Finished");
    }
}
