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
package org.netbeans.modules.javascript2.model;

import java.util.List;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;

/**
 *
 * @author Petr Hejl
 */
public abstract class FunctionArgumentAccessor {

    private static volatile FunctionArgumentAccessor DEFAULT;

    public static FunctionArgumentAccessor getDefault() {
        FunctionArgumentAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        // invokes static initializer of FunctionArgument.class
        // that will assign value to the DEFAULT field above
        Class c = FunctionArgument.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        return DEFAULT;
    }

    public static void setDefault(FunctionArgumentAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }

        DEFAULT = accessor;
    }

    public abstract FunctionArgument createForAnonymousObject(int order, int offset, JsObject value);

    public abstract FunctionArgument createForString(int order, int offset, String value);

    public abstract FunctionArgument createForReference(int order, int offset, List<String> value);

    public abstract FunctionArgument createForArray(int order, int offset, JsObject value);

    public abstract FunctionArgument createForUnknown(int order);
}
