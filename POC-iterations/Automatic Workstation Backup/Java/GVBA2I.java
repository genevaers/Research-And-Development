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
 */

class GVBA2I {

	public native Integer doA2I(String stringin, Integer beginIndex, Integer len);

		public Integer showA2I(String stringin, Integer beginIndex, Integer len) {
			System.out.println("stringin: " + stringin);
			String strnum = stringin.substring(beginIndex, beginIndex+len);
			System.out.println("strnum: " + strnum);
            System.out.println("beginIndex: " + beginIndex + " len: " +len);
			return Integer.MAX_VALUE;	
		}

	    public Integer doAtoi(String stringin, Integer beginIndex, Integer len) {

			String strnum = stringin.substring(beginIndex, beginIndex+len);

			char str[] = strnum.toCharArray();

			int sign = 1, base = 0, i = 0;
		    while (str[i] == ' ')
		    {
			    i++;
		    }
		    if (str[i] == '-' || str[i] == '+')
		    {
			    sign = 1 - 2 * (str[i++] == '-' ? 1 : 0);
		    }
		    while (i < str.length
			    && i <= len
			    && str[i] >= '0'
			    && str[i] <= '9') {
			    if (base > Integer.MAX_VALUE / 10
				    || (base == Integer.MAX_VALUE / 10
					&& str[i] - '0' > 7))
			    {
				    if (sign == 1)
					    return Integer.MAX_VALUE;
				    else
					    return Integer.MIN_VALUE;
			    }
			    base = 10 * base + (str[i++] - '0');
		    }
		    return base * sign;
	    }
		
		public Integer doAtois(String stringin, Integer beginIndex) {

			int len = stringin.length() - beginIndex;
			if (len < 1) {
				return 0;
			}
			
			String strnum = stringin.substring(beginIndex, beginIndex+len);

			char str[] = strnum.toCharArray();

			int sign = 1, base = 0, i = 0;
		    while (str[i] == ' ')
		    {
			    i++;
		    }
		    if (str[i] == '-' || str[i] == '+')
		    {
			    sign = 1 - 2 * (str[i++] == '-' ? 1 : 0);
		    }
		    while (i < str.length
			    && i <= len
			    && str[i] >= '0'
			    && str[i] <= '9') {
			    if (base > Integer.MAX_VALUE / 10
				    || (base == Integer.MAX_VALUE / 10
					&& str[i] - '0' > 7))
			    {
				    if (sign == 1)
					    return Integer.MAX_VALUE;
				    else
					    return Integer.MIN_VALUE;
			    }
			    base = 10 * base + (str[i++] - '0');
		    }
		    return base * sign;
	    }
}