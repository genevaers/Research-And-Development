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
 * 
 * callHlasm - Call ASMINF64 HLASM module from Java   
 */

class zOSInfo {

    public native byte[] showZos(int fid
                                ,String parm
                                ,String opt
                                ,byte[] arrayIn
                                ,int mr95Rc);
       static {
          /* --- Get our addressing mode (31 or 64) ----- */
          String arch = 
             System.getProperty("com.ibm.vm.bitmode");

          String gvbdebug = System.getenv("GVBDEBUG");

          /* --- Load the correct JNI Module ------------ */
          if (arch.equals("64")) {
            if ( gvbdebug == null ) {
               System.loadLibrary("GVBJDLL"); // regular version
            }
            else {
               if (gvbdebug.equals("3")) {
                  System.loadLibrary("GVBJDLLD"); // debug version
               }
               else {
                  System.loadLibrary("GVBJDLL"); // regular version
               }
            }
          }
          else
            System.out.println("JNIzOS doesn't support 31 bit");
       }
}
