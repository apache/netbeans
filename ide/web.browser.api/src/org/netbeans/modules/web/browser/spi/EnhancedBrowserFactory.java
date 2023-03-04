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

package org.netbeans.modules.web.browser.spi;

import java.awt.Image;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;

/**
 *
 */
public interface EnhancedBrowserFactory {

    /**
     * Type of browser.
     */
    BrowserFamilyId getBrowserFamilyId();

    /**
     * Image icon representing this browser.
     * @param small return 16x16 icon if true; otherwise 24x24 size expected
     * @return can return null (in which case a fallback icon will be used as
     * implemented in WebBrowser itself)
     */
    Image getIconImage(boolean small);

    /**
     * Display name of browser.
     * 
     * @return can return null (in which case display name of the node which
     * represents registration of this browser in the SystemFileSystem will be
     * used instead)
     */
    String getDisplayName();

    /**
     * An ID which uniquely identifies this browser and can be persisted for
     * future references to this browser.
     * @return can be null (in which case browser instance is expected
     * to be registered as file in default filesystem and its Lookup.Item.getId
     * is used instead.)
     */
    String getId();

    /**
     * Is this factory producing browsers with tight NetBeans integration (Connector).
     * @return
     */
    boolean hasNetBeansIntegration();

    /**
     * Can this factory produce a browser for current OS? This is an API alternative
     * to using "hidden" attribute in registrations via module layer.
     */
    boolean canCreateHtmlBrowserImpl();
}
