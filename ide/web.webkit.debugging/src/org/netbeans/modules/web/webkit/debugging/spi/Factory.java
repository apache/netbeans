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
package org.netbeans.modules.web.webkit.debugging.spi;

import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;

/**
 * Helper factory for SPI implementors.
 */
public final class Factory {
    
    private Factory() {}
    
    /**
     * Creates API for transport implementation which should be placed into
     * browser's lookup. See support for external browsers or WebView for usage example.
     */
    public static WebKitDebugging createWebKitDebugging(TransportImplementation impl) {
        TransportHelper helper = new TransportHelper(impl);
        return APIFactory.createWebKitDebugging(helper);
    }
}
