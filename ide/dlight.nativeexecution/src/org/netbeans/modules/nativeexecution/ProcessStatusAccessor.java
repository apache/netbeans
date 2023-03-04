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
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.api.ProcessStatusEx;

/**
 *
 * @author Andrew
 */
public abstract class ProcessStatusAccessor {
        private static volatile ProcessStatusAccessor DEFAULT;

    public static void setDefault(ProcessStatusAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException(
                    "ConnectionManagerAccessor is already defined"); // NOI18N
        }

        DEFAULT = accessor;
    }

    public static synchronized ProcessStatusAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }

        try {
            Class.forName(ProcessStatusEx.class.getName(), true,
                    ProcessStatusEx.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
        }

        return DEFAULT;
    }

    public abstract ProcessStatusEx create(String[] data);
}
