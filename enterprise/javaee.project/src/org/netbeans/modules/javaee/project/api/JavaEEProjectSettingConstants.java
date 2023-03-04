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
package org.netbeans.modules.javaee.project.api;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;

/**
 * General constants used by Java EE project types.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 * @since 1.5
 */
public final class JavaEEProjectSettingConstants {
    
    private JavaEEProjectSettingConstants() {
    }

    /**
     * Values according to the {@link Profile} class.
     *
     * @see Profile
     */
    public static final String J2EE_PLATFORM = "j2ee.platform";               //NOI18N

    /**
     * Server instance ID. Typically path to the location where certain server instance is installed.
     */
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N

    /**
     * Selected browser. Typically gets via {@link WebBrowser#getId()}.
     *
     * @see WebBrowsers
     * @see WebBrowser#getId()
     */
    public static final String SELECTED_BROWSER = "selected.browser";         //NOI18N
}
