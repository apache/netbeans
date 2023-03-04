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

package org.netbeans.spi.server;

import org.netbeans.api.server.ServerInstance;

/**
 * Factory creating the API representation of the instance provided in SPI.
 *
 * @author Petr Hejl
 */
public final class ServerInstanceFactory {

    private ServerInstanceFactory() {
        super();
    }
    
    /**
     * Creates the API representation of the provided SPI instance.
     * 
     * @param impl the SPI instance
     * @return the API server instance representation
     */
    public static ServerInstance createServerInstance(ServerInstanceImplementation impl) {
        return Accessor.DEFAULT.createServerInstance(impl);
    }
    
    /**
     * The accessor pattern class.
     */
    public abstract static class Accessor {

        /** The default accessor. */
        public static Accessor DEFAULT;

        static {
            // invokes static initializer of ReaderManager.class
            // that will assign value to the DEFAULT field above
            Class c = ServerInstance.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }


        public abstract ServerInstance createServerInstance(ServerInstanceImplementation impl);

    }
}
