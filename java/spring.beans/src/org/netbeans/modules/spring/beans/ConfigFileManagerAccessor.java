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

package org.netbeans.modules.spring.beans;

import org.netbeans.modules.spring.api.beans.ConfigFileManager;

/**
 *
 * @author Andrei Badea
 */
public abstract class ConfigFileManagerAccessor {

    private static volatile ConfigFileManagerAccessor accessor;

    public static void setDefault(ConfigFileManagerAccessor accessor) {
        if (ConfigFileManagerAccessor.accessor != null) {
            throw new IllegalStateException();
        }
        ConfigFileManagerAccessor.accessor = accessor;
    }

    public static ConfigFileManagerAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        try {
            Class.forName(ConfigFileManager.class.getName(), true, ConfigFileManager.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        return accessor;
    }

    public abstract ConfigFileManager createConfigFileManager(ConfigFileManagerImplementation impl);
}
