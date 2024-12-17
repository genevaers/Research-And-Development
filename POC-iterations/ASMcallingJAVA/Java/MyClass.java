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
 * Example Class with simple methods
 */

import java.util.Arrays;

public class MyClass {
    public byte[] MethodX(byte[] b) {
        System.out.println("MethodX executed from class MyClass with input:");
        System.out.println("Length: " + b.length);
        for (int i = 0; i < b.length; i++)
        {
            System.out.print(String.format("%02X", b[i]));
        }
        System.out.println();
        System.out.println();

        byte[] ret = Arrays.copyOfRange(b, 1, b.length);
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = (byte) (ret[i] + 16);
        }
        return ret;
    }

    public ReturnData MethodY(byte[] b) {
        System.out.println("MethodY executed from class MyClass with input:");
        System.out.println("Length: " + b.length);
        for (int i = 0; i < b.length; i++)
        {
            System.out.print(String.format("%02X", b[i]));
        }
        System.out.println();
        System.out.println();

        for (int i = 0; i < b.length; i++)
        {
            b[i] = (byte) (b[i] + 1);
        }

        int rc = 8;
        ReturnData  returnData = new ReturnData(rc, b);
        return returnData;
    }
 
    public String MthASC1(String input) {
        System.out.println("MthASC1 executed from MyClass");
        String output = "MthASC1: " + input;
        return output;
    }

    public String MthASC2(String input) {
        System.out.println("MthASC2 executed from MyClass");
        String output = "MthASC2: " + input;
        return output;
    }

    public String MthASC3(String input) {
        System.out.println("MthASC3 executed from MyClass");
        String output = "MthASC3: " + input;
        return output;
    }

    public String MthASC4(String input) {
        System.out.println("MthASC4 executed from MyClass");
        String output = "MthASC4: " + input;
        return output;
    }

    public String MthASC5(String input) {
        System.out.println("MthASC5 executed from MyClass");
        String output = "MthASC5: " + input;
        return output;
    }

    public String MthASC6(String input) {
        System.out.println("MthASC6 executed from MyClass");
        String output = "MthASC6: " + input;
        return output;
    }

    public String MthASC7(String input) {
        System.out.println("MthASC7 executed from MyClass");
        String output = "MthASC7: " + input;
        return output;
    }

    public String MthASC8(String input) {
        System.out.println("MthASC8 executed from MyClass");
        String output = "MthASC8: " + input;
        return output;
    }

    public String MthASC9(String input) {
        System.out.println("MthASC9 executed from MyClass");
        String output = "MthASC9: " + input;
        return output;
    }

    public String MthASC0(String input) {
        System.out.println("MthASC0 executed from MyClass");
        String output = "MthASC0: " + input;
        return output;
    }

    public byte[] Method1(byte[] input) {
        System.out.println("Method1 executed from MyClass");
        input[0] = (byte) 0XF1;
        return input;
    }

    public byte[] Method2(byte[] input) {
        System.out.println("Method2 executed from MyClass");
        input[0] = (byte) 0XF2;
        return input;
    }

    public byte[] Method3(byte[] input) {
        System.out.println("Method3 executed from MyClass");
        input[0] = (byte) 0XF3;
        return input;
    }

    public byte[] Method4(byte[] input) {
        System.out.println("Method4 executed from MyClass");
        input[0] = (byte) 0XF4;
        return input;
    }

    public byte[] Method5(byte[] input) {
        System.out.println("Method5 executed from MyClass");
        input[0] = (byte) 0XF5;
        return input;
    }

    public byte[] Method6(byte[] input) {
        System.out.println("Method6 executed from MyClass");
        input[0] = (byte) 0XF6;
        return input;
    }

    public byte[] Method7(byte[] input) {
        System.out.println("Method7 executed from MyClass");
        input[0] = (byte) 0XF7;
        return input;
    }

    public byte[] Method8(byte[] input) {
        System.out.println("Method8 executed from MyClass");
        input[0] = (byte) 0XF8;
        return input;
    }

    public byte[] Method9(byte[] input) {
        System.out.println("Method9 executed from MyClass");
        input[0] = (byte) 0XF9;
        return input;
    }

    public byte[] Method0(byte[] input) {
        System.out.println("Method0 executed from MyClass");
        input[0] = (byte) 0XC1;
        return input;
    }
}