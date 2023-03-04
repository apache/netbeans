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
package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryImplementation;

/**
 *
 * @author Petr Hejl
 */
public abstract class ServerLibraryAccessor {

    private static volatile ServerLibraryAccessor accessor;

    public static void setDefault(ServerLibraryAccessor accessor) {
        if (ServerLibraryAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor"); // NOI18N
        }
        ServerLibraryAccessor.accessor = accessor;
    }

    public static ServerLibraryAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }

        // invokes static initializer of ServerLibrary.class
        // that will assign value to the DEFAULT field above
        Class c = ServerLibrary.class;
        try {
            Class.forName(c.getName(), true, ServerLibraryAccessor.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }

        return accessor;
    }

    /**
     * Creates the API instance.
     *
     * @param impl the SPI instance
     * @return the API instance
     */
    public abstract ServerLibrary createServerLibrary(ServerLibraryImplementation impl);

}
