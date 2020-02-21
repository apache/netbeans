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
package org.netbeans.modules.cnd.debugger.common2;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;


/**
 *
 */
public abstract class APIAccessor {

    private static APIAccessor INSTANCE;

    public static synchronized APIAccessor get() {
        if (INSTANCE == null) {
            Class<?> c = CndRemote.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }

        assert INSTANCE != null : "There is no API package accessor available!"; //NOI18N
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - otherwise it throws IllegalStateException.
     *
     * @param accessor instance.
     */
    public static void register(APIAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }

    public abstract boolean syncValidate(final String name);

    
}
