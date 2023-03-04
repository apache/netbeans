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
package org.netbeans.modules.j2ee.persistence.spi.jpql.support;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

/**
 *
 * @author sp153251
 */
public class Utils {

    public static VariableElement getField(TypeElement clazz, String fieldName) {
        for (VariableElement field : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            if (field.getSimpleName().contentEquals(fieldName)) {
                return field;
            }
        }

        return null;
    }

    // TODO: reimplement this method to take a type argument and assure 100% accuracy 
    public static ExecutableElement getAccesor(TypeElement clazz, String fieldName) {
        for (ExecutableElement method : getMethod(clazz, getAccesorName(fieldName))) {
            if (method.getParameters().isEmpty()) {
                return method;
            }
        }

        for (ExecutableElement method : getMethod(clazz, getBooleanAccesorName(fieldName))) {
            if (method.getParameters().isEmpty()) {
                return method;
            }
        }

        return null;
    }

    public static String getAccesorName(String fieldName) {
        return "get" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase()
                + fieldName.substring(1);
    }

    public static String getBooleanAccesorName(String fieldName) {
        return "is" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase()
                + fieldName.substring(1);
    }

    public static ExecutableElement[] getMethod(TypeElement clazz, String methodName) {
        List<ExecutableElement> methods = new ArrayList<>();

        for (ExecutableElement method : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (method.getSimpleName().contentEquals(methodName)) {
                methods.add(method);
            }
        }

        return methods.toArray(new ExecutableElement[0]);
    }
}
