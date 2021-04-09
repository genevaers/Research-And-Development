/* -------------------------------------------------------
   callHlasm - Call ASMINF HLASM module from Java
   ------------------------------------------------------ */

class callHlasm {

   public static void main(String[] args) throws Exception {
       System.out.println("(callHlasm) Started:");
       zOSInfo a = new zOSInfo();

       /* --- Get Hostname ------------------------------ */
       System.out.print("Host: ");
       System.out.println(a.showZos("HOST"));

       /* --- Get Sysplex Name: ------------------------- */
       System.out.print("Sysplex: ");
       System.out.println(a.showZos("SYSPLEX"));

       /* --- Specify an invalid parameter -------------- */
       System.out.print("Bad Parm: ");
       System.out.println(a.showZos("JUNK"));

       System.out.println("(callHlasm) Finished");
    }
}
