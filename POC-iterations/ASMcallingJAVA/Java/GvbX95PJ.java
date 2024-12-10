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
 * Class for managing parameters from Geneva GVBX95PA exit calls
 */

public class GvbX95PJ {
        private int    lrID;
        private int    thrdNo;
        private String phase;
        private int    viewID;
        private String processDateTime;
        private String startupParms;

        public GvbX95PJ(int lrID, int thrdNo, String phase, int viewID, String processDateTime, String startupParms) {
            this.lrID = lrID;
            this.thrdNo = thrdNo;
            this.phase = phase;
            this.viewID = viewID;
            this.processDateTime = processDateTime;
            this.startupParms = startupParms;
        }

        public int getLrID() { return lrID; }
        public int getThrdNo() { return thrdNo; }
        public String getPhase() { return phase; }
        public int getViewID() { return viewID; }
        public String getProcessDateTime() { return processDateTime; }
        public String getStartupParms() { return startupParms; }

        public void setLrID(int lrID) {this.lrID = lrID; }
        public void setThrdNo(int thrdNo) {this.thrdNo = thrdNo; }
        public void setPhase(String phase) {this.phase = phase; }
        public void setViewID(int viewID) {this.viewID = viewID; }
        public void setProcessDateTime(String processDateTime) {this.processDateTime = processDateTime; }
        public void setStartupParms(String startupParms) {this.startupParms = startupParms; }
}
