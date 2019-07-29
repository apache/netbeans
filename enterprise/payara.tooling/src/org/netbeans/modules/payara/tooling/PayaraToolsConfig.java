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
package org.netbeans.modules.payara.tooling;

import org.netbeans.modules.payara.tooling.logging.Logger;

/**
 * GlassFisg Tooling Library configuration.
 * <p/>
 * @author Tomas Kraus
 */
public class PayaraToolsConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(PayaraToolsConfig.class);

    /** Proxy settings usage for loopback addresses. */
    private static volatile boolean proxyForLoopback = true;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Do not use proxy settings for loopback addresses.
     */
    public static void noProxyForLoopback() {
        proxyForLoopback = false;
    }

    /**
     * Use proxy settings for loopback addresses.
     * <p/>
     * This is default behavior.
     */
    public static void useProxyForLoopback() {
        proxyForLoopback = true;
    }

    /**
     * Get proxy settings usage for loopback addresses configuration value.
     * <p/>
     * @return Proxy settings usage for loopback addresses configuration value.
     */
    public static boolean getProxyForLoopback() {
        return proxyForLoopback;
    }

}

