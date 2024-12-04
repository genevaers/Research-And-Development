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
 * Example ClassB with simple methods
 */

import java.util.Arrays;
import com.ibm.jzos.fields.daa.BinaryUnsignedIntField;

class LocalClass {
  public int view;
  public int threadno;
  public int lrid;

  LocalClass (int view, int threadno, int lrid) {
    this.view = view;
    this.threadno = threadno;
    this.lrid = lrid;
  }

    int getView() { return view;}
    int getThreadno() { return threadno; }
    int getLrid() { return lrid;}

    void setView(int view) { this.view = view; }
    void setThrdno(int threadno) { this.threadno = threadno; }
    void setLrid(int lrid) { this.lrid = lrid; }
}


public class MyClassB {

     @SuppressWarnings("null")
    public ReturnData MethodY(GvbX95PJ X95, byte[] b) {

        BinaryUnsignedIntField bui = new BinaryUnsignedIntField( 0 );
        
        byte[] returnBytes = { 0, 0, 0, 0, 0, 0 }; // 6 bytes returned
        int input = 0;
        int partition = 0;
        byte[] partitionId = { 0, 0 };
        int rc = 0;

        // => System.out.println("Input Length: " + b.length);
        //for (int i = 0; i < b.length; i++)
        //{
        //    System.out.print(String.format("%02X", b[i]));
        //}
        //System.out.println();

        int lrID = X95.getLrID();
        int thrdNo = X95.getThrdNo();
        String phase = X95.getPhase();
        int viewID = X95.getViewID();
        String processDateTime = X95.getProcessDateTime();
        String startupParms = X95.getStartupParms();

        if (phase.equals("OP") || phase.equals("CL"))
        {
          ReturnData  returnData = new ReturnData(rc, returnBytes);
          return returnData;
        }

        //System.out.println(" lrID: " + lrID + " thrdno: " + thrdNo + " Phase: " + phase + " view: " + viewID + " processDateTime: " + processDateTime + " startupParms: " + startupParms);

        //LocalClass localClass;
        //if (phase.equals("OP")) {
        //  localClass = new LocalClass(0, thrdNo, 0);
        //}
        //else {
        //  if (localClass != null) {
        //    if (localClass.getView() == 0) {
        //        localClass.setView(viewID);
        //        localClass.setLrid(lrID);  
        //    }
        //  }
        //}

          //else {
          //  System.out.println(" LOCAL lrid: " + localClass.getLrid() + " thrdno: " + localClass.getThreadno() + " view: " + localClass.getView());
          //}        }


        input = bui.getInt( b ) % 100; // last 2 digits of input
        partition = input / 5; // 20 partitions: 0 - 4; 5 - 9; etc.

        switch ( partition ) {
            case 0:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF0;
              break;
            case 1:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF1;
              break;
            case 2:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF2;
              break;
            case 3:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF3;
              break;
            case 4:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF4;
              break;
            case 5:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF5;
              break;
            case 6:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF6;
              break;
            case 7:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF7;
              break;
            case 8:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF8;
              break;
            case 9:
              partitionId[0] = (byte) 0XF0;
              partitionId[1] = (byte) 0XF9;
              break;
            case 10:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF0;
              break;
            case 11:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF1;
              break;
            case 12:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF2;
              break;
            case 13:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF3;
              break;
            case 14:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF4;
              break;
            case 15:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF5;
              break;
            case 16:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF6;
              break;
            case 17:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF7;
              break;
            case 18:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF8;
              break;
            case 19:
              partitionId[0] = (byte) 0XF1;
              partitionId[1] = (byte) 0XF9;
              break;
            default:
              break;
        }
        
        for (int i = 0; i < 4; i++)
        {
            returnBytes[i] = b[i];
        }
        for (int i = 0; i < 2; i++)
        {
            returnBytes[ i + 4 ] = partitionId[i];
        }

        //System.out.println("Return bytes length: " + returnBytes.length);
        //for (int i = 0; i < returnBytes.length; i++)
        //{
        //    System.out.print(String.format("%02X", returnBytes[i]));
        //}
        //System.out.println();
        //System.out.println();

        ReturnData  returnData = new ReturnData(rc, returnBytes);
        return returnData;
     }
}