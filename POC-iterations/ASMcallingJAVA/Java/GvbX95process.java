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
 * Class for preparing GVBX95PA parameters for GVBX95J object
 */

import java.util.Arrays;
import com.ibm.jzos.fields.daa.BinaryUnsignedIntField;
import com.ibm.jzos.fields.daa.BinaryUnsignedIntL2Field;

public class GvbX95process {
    // privates

    public GvbX95PJ GvbX95prepare(GvbX95PJ X95, String header, byte[] byteB, String threadIdentifier, Integer ntrace) {

        BinaryUnsignedIntField bui = new BinaryUnsignedIntField( 0 ); 

        int lrID = 0;
        int thrdNo = 0;
        int viewID = 0;
        String phase = null;
        String processDateTime = null;
        String startupParms = null;
        byte[] lr = null;
        byte[] genparm_digits = null;
      
        lr = Arrays.copyOfRange(byteB, 144, 148);            // The LRID
        genparm_digits = Arrays.copyOfRange(byteB, 136, 144);

        lrID = bui.getInt(lr);                                       // Ths LRID as Java number
        thrdNo = bui.getInt(genparm_digits);                         // The thread number as Java number
        viewID = bui.getInt(genparm_digits, 4);                      // The view ID as Java number
        phase = header.substring(80, 82);
        processDateTime = header.substring(82, 96);
        startupParms = header.substring(98, 136);

        if (ntrace > 1) {
          System.out.println(threadIdentifier +  ":lrID: " + lrID + " MR95thrd#: " + thrdNo + " View: " + viewID + " Phase: " + phase + " Proctime: " + processDateTime + " Startup: " + startupParms);
        }

        X95.setLrID(lrID);
        X95.setThrdNo(thrdNo);
        X95.setPhase(phase);
        X95.setViewID(viewID);
        X95.setProcessDateTime(processDateTime);
        X95.setStartupParms(startupParms);

        return X95;

    }
    
}
