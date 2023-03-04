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
package org.netbeans.modules.extbrowser.chrome;

import java.awt.Image;
import org.netbeans.modules.extbrowser.ChromiumBrowser;
import org.netbeans.modules.extbrowser.ExtBrowserImpl;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
public class ChromiumWithPluginBrowserFactory extends ChromiumBrowser implements EnhancedBrowserFactory {

    public ChromiumWithPluginBrowserFactory() {
        super();
    }

    @NbBundle.Messages({
        "ChromiumBrowserWithPlugin.name=Chromium"
    })
    @Override
    public String getDisplayName() {
        return Bundle.ChromiumBrowserWithPlugin_name();
    }

    @Override
    public String getId() {
        return "Chromium"; // NOI18N
    }

    @Override
    public boolean hasNetBeansIntegration() {
        return false;
    }

    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        HtmlBrowser.Impl res = super.createHtmlBrowserImpl();
        assert res instanceof ExtBrowserImpl;
        return new ChromeBrowserImpl((ExtBrowserImpl)res, false);
    }

    @Override
    public BrowserFamilyId getBrowserFamilyId() {
        return BrowserFamilyId.CHROMIUM;
    }

    @Override
    public Image getIconImage(boolean small) {
        return null;
    }

    @Override
    public boolean canCreateHtmlBrowserImpl() {
        return !isHidden();
    }
}
