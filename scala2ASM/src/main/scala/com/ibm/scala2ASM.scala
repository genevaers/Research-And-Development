package com.ibm.scala2ASM

// test using this approach https://medium.com/@bschlining/a-simple-java-native-interface-jni-example-in-java-and-scala-68fdafe76f5f
// These commands didn't complete:

//# Compile
//scalac Sample1.scala# javah needs access to scala-library.jar
//LIBS_HOME=/usr/local/Cellar/scala/2.12.4/libexec/lib
//CP=$LIBS_HOME/scala-library.jarjavah -cp $CP:. Sample1

// LIBS_HOME is LIBS_HOME=/usr/local/Cellar/scala/2.12.1/libexec/lib
// CP=$LIBS_HOME/scala-library.jar
// could not find

// never got CP to update for outputs from scalac (classes).  is CP class path?  does it have to point to
// a jar file, not a directory?

//ktwitchell001@kips-mbp ibm % echo $CP
///usr/local/Cellar/scala/2.12.1/libexec/lib/scala-library.jar
//ktwitchell001@kips-mbp ibm % javah -cp $CP:. scala2ASM
//Error: Could not find class file for 'scala2ASM'.
//ktwitchell001@kips-mbp ibm % CP=$CP;/com/ibm/scala2ASM
//zsh: no such file or directory: /com/ibm/scala2ASM
//ktwitchell001@kips-mbp ibm % CP=$CP;/Users/ktwitchell001/workspace/IBMSAFR-Spark/scala2ASM/src/main/scala/com/ibm/com/ibm/scala2AS
//zsh: no such file or directory: /Users/ktwitchell001/workspace/IBMSAFR-Spark/scala2ASM/src/main/scala/com/ibm/com/ibm/scala2AS




class scala2ASM {
    // -- UR20 code
    //    &PRE.FC   DS    HL02            FUNCTION CODE
    //    &PRE.RC   DS    HL02            RETURN   CODE
    //    &PRE.ERRC DS    HL02            ERROR    CODE
    //    &PRE.RECL DS    HL02            RECORD   LENGTH
    //    &PRE.RECA DS    AL04            RECORD   AREA      ADDRESS
    //      &PRE.RBN  DS    FL04            RELATIVE BLOCK     NUMBER
    //      &PRE.DDN  DS    CL08            FILE     DDNAME
    //    &PRE.OPT1 DS    CL01            I/O MODE (I=IN,O=OUT,D=DIRECT,X=EXCP)
    //    &PRE.OPT2 DS    CL01
    //    &PRE.NBUF DS    HL02            NUMBER   OF I/O    BUFFERS
    //    &PRE.WPTR DS    AL04            WORK     AREA      POINTER
    //      &PRE.MEMS DS    fL04            Size of memory used by GVBUR20

    // -- UR20 code
    //    &PRE.FC   DS    HL02            FUNCTION CODE
    //    &PRE.RC   DS    HL02            RETURN   CODE
    //    &PRE.ERRC DS    HL02            ERROR    CODE
    //    &PRE.RECL DS    HL02            RECORD   LENGTH
    //    &PRE.RECA DS    AL04            RECORD   AREA      ADDRESS
    //      &PRE.RBN  DS    FL04            RELATIVE BLOCK     NUMBER
    //      &PRE.DDN  DS    CL08            FILE     DDNAME
    @native def OPT1(s: String): String // DS    CL01  I/O MODE (I=IN,O=OUT,D=DIRECT,X=EXCP)
    //    &PRE.OPT2 DS    CL01
    //    &PRE.NBUF DS    HL02            NUMBER   OF I/O    BUFFERS
    //    &PRE.WPTR DS    AL04            WORK     AREA      POINTER
    //      &PRE.MEMS DS    fL04            Size of memory used by GVBUR20

    // --- Native methods
    @native def intMethod(n: Int): Int
    @native def booleanMethod(b: Boolean): Boolean
    @native def stringMethod(s: String): String
    @native def intArrayMethod(a: Array[Int]): Int
  }

  object scala2ASM {

    // --- Main method to test our native library
    def main(args: Array[String]): Unit = {
      System.loadLibrary("scala2ASM")
      val ur20 = new scala2ASM
      val OPT1 = ur20.OPT1("I")
      val square = ur20.intMethod(5)
      val bool = ur20.booleanMethod(true)
      val text = ur20.stringMethod("java")
      val sum = ur20.intArrayMethod(Array(1, 1, 2, 3, 5, 8, 13))

      println(s"OPT1: $OPT1")
      println(s"intMethod: $square")
      println(s"booleanMethod: $bool")
      println(s"stringMethod: $text")
      println(s"intArrayMethod: $sum")
    }
  }