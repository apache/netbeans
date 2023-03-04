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
package org.netbeans.modules.extexecution;

import org.netbeans.spi.extexecution.ProcessBuilderImplementation;

/**
 *
 * @author Petr Hejl
 */
public abstract class ProcessBuilderAccessor {

    private static volatile ProcessBuilderAccessor DEFAULT;

    public static ProcessBuilderAccessor getDefault() {
        ProcessBuilderAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        // invokes static initializer of ProcessBuilder.class
        // that will assign value to the DEFAULT field above
        Class c = org.netbeans.api.extexecution.ProcessBuilder.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        return DEFAULT;
    }

    public static void setDefault(ProcessBuilderAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }

        DEFAULT = accessor;
    }

    public abstract org.netbeans.api.extexecution.ProcessBuilder createProcessBuilder(
            ProcessBuilderImplementation impl, String description);
}
