/* -------------------------------------------------------
   callHlasm - Call ASMINF HLASM module from Java
   ------------------------------------------------------ */

class zOSInfo {

    public native String showZos(String parm);
       static {
          /* --- Get our addressing mode (31 or 64) ----- */
          String arch =
             System.getProperty("com.ibm.vm.bitmode");

          /* --- Load the correct JNI Module ------------ */
          if (arch.equals("64"))
             System.loadLibrary("JNIzOS64");
          else
             System.loadLibrary("JNIzOS");
       }
}
