/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.sun.validation.util;

import java.lang.reflect.Constructor;

/**
 * This class is a generic Factory that employes Java reflection to
 * create Objects.
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ObjectFactory {
    /* A class implementation comment can go here. */

    /** Create an instance of the class with the specified name by calling the
      * no-argument constructor.
     */
    public static Object newInstance(String className){

        Utils utils = new Utils();
        return utils.createObject(className);
    }


    /** Create an instance of the class with the specified name by calling the
      * a constructor that takes an String.
     */
    public static Object newInstance(String className, String argument){
        Class classObject = null;
        Utils utils = new Utils();

        Class[] argumentTypes = new Class[] {String.class};
        Constructor constructor =
            utils.getConstructor(className, argumentTypes);

        Object[] argumentValues = new Object[] {argument};

        return utils.createObject(constructor, argumentValues);
    }


    /** Create an instance of the class with the specified name by calling the
      * a constructor that takes an String.
     */
    public static Object newInstance(String className, Object argument){
        Class classObject = null;
        Utils utils = new Utils();

        Class[] argumentTypes = new Class[] {Object.class};
        Constructor constructor =
            utils.getConstructor(className, argumentTypes);

        Object[] argumentValues = new Object[] {argument};

        return utils.createObject(constructor, argumentValues);
    }    
}
