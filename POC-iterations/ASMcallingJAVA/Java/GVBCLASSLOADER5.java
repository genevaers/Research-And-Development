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
 * Class for loading Java classes: notice the specified argument list in aarg[]
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class GVBCLASSLOADER5 extends ClassLoader {
    public byte[] invokeClassMethod(String classBinName, String methodName, byte[] stuff){
        try {
            // Create a new JavaClassLoader
            ClassLoader classLoader = this.getClass().getClassLoader();

            // Load the target class using its binary name
            Class<?> loadedMyClass = classLoader.loadClass(classBinName);

            // Create a new instance from the loaded class
            Constructor<?> constructor = loadedMyClass.getConstructor();
            Object myClassObject = constructor.newInstance();

            // Create argument list for method, i.e. on string parameter
            Class aarg[] = new Class[1];
            aarg[0] = byte[].class;

            // Getting the target method from the loaded class and invoke it using its name
            Method method = loadedMyClass.getMethod(methodName,aarg);

            //method.invoke(myClassObject);
            Object returnData = method.invoke(myClassObject, stuff);
            return (byte[]) returnData;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}