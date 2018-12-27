/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package org.netbeans.api.java.source;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.openide.util.Exceptions;

/**
 * The class can we used to call methods of a class using Reflection.
 *
 *
 * @author arusinha
 */
public final class ReflectionHelper {

    /**
     *
     * @param className the fully qualified name of the desired class.
     * @param methodName the name of the method
     * @param parameterTypes the method parameter types list
     * @param obj the object the underlying method is invoked from
     * @param paramArgs the arguments used for the method call
     * @return the result of the invoked method.
     * @since 2.41
     */
    public static Object invokeMethod(String className, String methodName, List<Class> parameterTypes, Object obj, Object... paramArgs) {

        Method method = null;
        Object result = null;
        try {
            Class cls = Class.forName(className);
            if (parameterTypes == null || parameterTypes.isEmpty()) {
                method = cls.getDeclaredMethod(methodName);
            } else {
                method = cls.getDeclaredMethod(methodName, parameterTypes.toArray(new Class[0]));
            }
            result = method.invoke(obj, paramArgs);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }
}
