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

package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;

/**
 * Most general way to create {@link WebModule} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link WebModuleImplementation} and use this factory.
 *
 * @author Pavel Buzek
 */
public final class WebModuleFactory {

    private WebModuleFactory () {}

    /**
     * Creates an API web module instance for the given SPI web module.
     *
     * @param  spiWebmodule an instance of a SPI web module.
     * @return an instance of a API web module.
     * @deprecated use {@link #createWebModule(org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2)}
     */
    @Deprecated
    public static WebModule createWebModule(WebModuleImplementation spiWebmodule) {
        return WebModuleAccessor.getDefault().createWebModule (spiWebmodule);
    }

    public static WebModule createWebModule(WebModuleImplementation2 spiWebmodule) {
        return WebModuleAccessor.getDefault().createWebModule (spiWebmodule);
    }
}
