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

// overloaded method to statisfy different class loading requirements

public class GVBCLASSLOADER extends ClassLoader {

    public byte[] invokeClassMethod(String classBinName, String methodName, byte[] byteB){
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
            Object returnData = method.invoke(myClassObject, byteB);
            return (byte[]) returnData;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ReturnData invokeClassMethod(String classBinName, String methodName, GvbX95PJ x95, byte[] byteB) {
        try {
            // Create a new JavaClassLoader
            ClassLoader classLoader = this.getClass().getClassLoader();

            // Load the target class using its binary name
            Class<?> loadedMyClass = classLoader.loadClass(classBinName);

            // Create a new instance from the loaded class
            Constructor<?> constructor = loadedMyClass.getConstructor();
            Object myClassObject = constructor.newInstance();

            // Create argument list for method, i.e. on string parameter
            Class aarg[] = new Class[2];
            aarg[0] = GvbX95PJ.class;
            aarg[1] = byte[].class;

            // Getting the target method from the loaded class and invoke it using its name
            Method method = loadedMyClass.getMethod(methodName,aarg);

            // Invoke myClassObject)
            Object returnData = method.invoke(myClassObject, x95, byteB);
            return (ReturnData) returnData;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // (GvbX95PJ X95, String header, byte[] byteB, String threadIdentifier, Integer ntrace)
    public GvbX95PJ invokeClassMethod(String classBinName, String methodName,
                                        GvbX95PJ x95, String header, byte[] byteB, String threadIdentifier, Integer ntrace) {

        Class<?> loadedMyClass;

        try {
            // Create a new JavaClassLoader
            ClassLoader classLoader = this.getClass().getClassLoader();

            // Load the target class using its binary name
            try {
                  loadedMyClass = classLoader.loadClass(classBinName);
                } catch (ClassNotFoundException e) {
                    System.out.println("Class: " + classBinName + " method: " + methodName + " cannot be loaded" );
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    System.out.println("Error loading class: " + classBinName + " method: " + methodName + " cannot be loaded" );
                    e.printStackTrace();
                    return null;
                }

            // Create a new instance from the loaded class
            Constructor<?> constructor = loadedMyClass.getConstructor();
            Object myClassObject = constructor.newInstance();

            // Create argument list for method, i.e. on string parameter
            Class aarg[] = new Class[5];
            aarg[0] = GvbX95PJ.class;
            aarg[1] = String.class;
            aarg[2] = byte[].class;
            aarg[3] = String.class;
            aarg[4] = Integer.class;

            // Getting the target method from the loaded class and invoke it using its name
            Method method = loadedMyClass.getMethod(methodName,aarg);

            // Invoke myClassObject)
            Object X95 = method.invoke(myClassObject, x95, header, byteB, threadIdentifier, ntrace);
            return (GvbX95PJ) X95;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}