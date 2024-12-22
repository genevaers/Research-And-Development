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
 * 
 * Class for testing GVBA2I class
 */

class testA2I {

	public static void main(String[] args) {

		System.out.println("testA2I Started:");
        GVBA2I a = new GVBA2I();
		Integer myint;
		String mystring;
		Integer beginIndex = 16;
		Integer length = 4;
		String mynumber = "ABCDEFGHABCDEFGH0005XYZ";
		String threadIdentifier = "Worker----";

        mystring = String.format("%04d", length);
        System.out.println(mystring);

		mystring = String.format("%6s%04d", threadIdentifier, length);
        System.out.println(mystring);


		myint = a.doAtoi(mynumber, beginIndex, length);
		System.out.println("converted integer: " + myint);

		myint = a.showA2I(mynumber, beginIndex, length);
	}
}
