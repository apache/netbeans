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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import org.netbeans.modules.j2ee.deployment.impl.ServerLibraryAccessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;

/**
 * Factory creating the API representation of the library provided in SPI.
 *
 * @since 1.68
 * @author Petr Hejl
 */
public final class ServerLibraryFactory {

    private ServerLibraryFactory() {
        super();
    }
    
    /**
     * Creates the API representation of the provided SPI instance.
     * 
     * @param impl the SPI instance
     * @return the API server instance representation
     */
    public static ServerLibrary createServerLibrary(ServerLibraryImplementation impl) {
        return ServerLibraryAccessor.getDefault().createServerLibrary(impl);
    }
}
