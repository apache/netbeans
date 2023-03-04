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
package org.netbeans.modules.web.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.openide.util.Exceptions;

/**
 * This class provides access to the {@link WebModule}'s private constructor
 * from outside in the way that this class is implemented by an inner class of
 * {@link WebModule} and the instance is set via {@link #setDefault(WebModuleAccessor)}.
 */
public abstract class WebModuleAccessor {

    private static volatile WebModuleAccessor accessor;

    public static void setDefault(WebModuleAccessor accessor) {
        if (WebModuleAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor"); // NOI18N
        }
        WebModuleAccessor.accessor = accessor;
    }

    public static WebModuleAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }

        Class c = WebModule.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return accessor;
    }

    @Deprecated
    public abstract WebModule createWebModule(WebModuleImplementation spiWebmodule);

    public abstract WebModule createWebModule(WebModuleImplementation2 spiWebmodule);
}
