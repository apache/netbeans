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
package org.netbeans.core.browser.webview;

import java.awt.Image;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Impl;

/**
 * Creates internal browser which uses embedded native browser.
 *
 * @author S. Aubrecht
 */
public class BrowserFactory implements HtmlBrowser.Factory, EnhancedBrowserFactory {
    
    static String PROP_EXTRA_BROWSER = "ExtraBrowser"; //NOI18N

    @Override
    public Impl createHtmlBrowserImpl() {
        return createImpl();
    }
    
    protected Impl createImpl() {
        return new HtmlBrowserImpl();
    }

    public static Boolean isHidden () {
        return false;
    }

    @Override
    public BrowserFamilyId getBrowserFamilyId() {
        return BrowserFamilyId.JAVAFX_WEBVIEW;
    }

    @Override
    public Image getIconImage(boolean small) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean hasNetBeansIntegration() {
        return true;
    }

    @Override
    public boolean canCreateHtmlBrowserImpl() {
        return !isHidden();
    }
}
