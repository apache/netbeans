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

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * SPI extending the original {@link J2eePlatformImpl} adding methods to handle
 * platform directories in a cleaner way.
 * 
 * @author Petr Hejl
 * @since 1.72
 * @see J2eePlatformImpl
 */
public abstract class J2eePlatformImpl2 extends J2eePlatformImpl {

    /**
     * Returns the server installation directory or <code>null</code> if not
     * specified or unknown.
     * 
     * @return the server installation directory or <code>null</code> if not
     *            specified or unknown
     */
    @CheckForNull
    public abstract File getServerHome();

    /**
     * Returns the domain directory or <code>null</code> if not
     * specified or unknown. Many Java EE servers allows usage of multiple
     * server instances using single binaries. In such case this method should
     * return the installation/configuration directory of such instance.
     * 
     * @return the domain directory or <code>null</code> if not
     *            specified or unknown
     */    
    @CheckForNull
    public abstract File getDomainHome();
    
    /**
     * Returns the middleware directory or <code>null</code> if not
     * specified or unknown. Some servers share certain binaries on higher level
     * with other products of the same vendor. In such case this method should
     * return the appropriate directory.
     * 
     * @return the middleware directory or <code>null</code> if not
     *            specified or unknown
     */
    @CheckForNull
    public abstract File getMiddlewareHome();
    
}
