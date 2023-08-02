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
 * Class for loading Java classes with performance improvements
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@SuppressWarnings({ "rawtypes", "unchecked" })

public class GVBCLASSLOADER2 extends ClassLoader {

    public ClassLoader classLoader = null;
    public Constructor<?> constructor = null;
    public Class<?> loadedMyClass = null;
    public Object myClassObject = null;

      public Method obtainClassMethod(String classBinName, String methodName, Class aarg[]){

        Method method = null;

        try {

            // Create a new JavaClassLoader
            classLoader = this.getClass().getClassLoader();

            // Load the target class using its binary name
            loadedMyClass = classLoader.loadClass(classBinName);
            //System.out.println("Loaded class name: " + loadedMyClass.getName());

            // Create a new instance from the loaded class
            constructor = loadedMyClass.getConstructor();
            myClassObject = this.constructor.newInstance();

            // Create a new instance of method
            //System.out.println("methodName:" + methodName);
            method = loadedMyClass.getMethod(methodName,aarg);
           //System.out.println("Invoked method name: " + method.getName());

         } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return myClassObject;
        return method;
    }

    public String executeClassMethod(Method method, String stuff){
        String result = " ";

        try {
            Object methodObject = method.invoke(myClassObject, stuff);
            //System.out.println("Object aaaa:" + aaaa);
            result = methodObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }    
}