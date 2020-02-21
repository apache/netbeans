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

package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersRegistry;
import org.openide.filesystems.FileObject;

/**
 *
 * 
 */
public abstract class LaunchersRegistryAccessor {
    private static volatile LaunchersRegistryAccessor DEFAULT;

    public static void setDefault(LaunchersRegistryAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException(
                    "ConnectionManagerAccessor is already defined"); // NOI18N
        }

        DEFAULT = accessor;
    }

    public static synchronized LaunchersRegistryAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }

        try {
            Class.forName(LaunchersRegistry.class.getName(), true,
                    LaunchersRegistry.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
        }

        return DEFAULT;
    }
    
    public abstract void assertPrivateListenerNotNull(FileObject dir);
}
