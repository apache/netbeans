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
package org.netbeans.modules.web.browser.api;

import java.awt.Image;
import org.netbeans.modules.extbrowser.ExtWebBrowser;
import org.netbeans.modules.extbrowser.PrivateBrowserFamilyId;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperProvider;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.loaders.DataObject;

/**
 * Descriptor providing a display name and unique ID for a browser factory.
 */
final class WebBrowserFactoryDescriptor {

    private String id;
    private String name;
    private DataObject dob;
    private boolean def;
    private HtmlBrowser.Factory factory;
    private BrowserFamilyId browserFamily;
    private boolean hasNetBeansIntegration;

    public WebBrowserFactoryDescriptor(String id, DataObject dob, boolean def, Factory factory) {
        this.id = id;
        this.dob = dob;
        this.def = def;
        this.factory = factory;
        if (factory instanceof EnhancedBrowserFactory) {
            browserFamily = ((EnhancedBrowserFactory)factory).getBrowserFamilyId();
            name = ((EnhancedBrowserFactory)factory).getDisplayName();
            hasNetBeansIntegration = ((EnhancedBrowserFactory)factory).hasNetBeansIntegration();
        } else if (factory instanceof ExtWebBrowser) {
            browserFamily = convertBrowserFamilyId(((ExtWebBrowser)factory).getPrivateBrowserFamilyId());
            hasNetBeansIntegration = false;
        } else {
            browserFamily = BrowserFamilyId.UNKNOWN;
            hasNetBeansIntegration = false;
        }
    }
    
    
    WebBrowserFactoryDescriptor(WebBrowserFactoryDescriptor delegate, String id, String name) {
        this(id, delegate.dob, delegate.def, delegate.factory);
        this.name = name;
    }
    
    /**
    * Unique ID of this browser. Should be suitable for persistence reference to this browser.
    */
    public String getId() {
        return id;
    }

    /**
    * Name of the browser eg. FireFox, WebView, ...
    *
    * @return
    */
    public String getName() {
        if (name == null) {
            // retrieve browser's name when it is really needed and not in constructor;
            // if it called too early it causes:  "IllegalStateException: Should not acquire 
            //      Children.MUTEX while holding ProjectManager.mutex()"
            name = dob.getNodeDelegate().getDisplayName();
        }
        return name;
    }

    /**
     * Is this default browser factory? That is had user selected this browser in IDE options.
     */
    public boolean isDefault() {
        return def;
    }

    void setDefault(boolean def) {
        this.def = def;
    }

    public boolean hasNetBeansIntegration() {
        return hasNetBeansIntegration;
    }

    /**
     * Browser factory.
     */
    public HtmlBrowser.Factory getFactory() {
        return factory;
    }

    public BrowserFamilyId getBrowserFamily() {
        return browserFamily;
    }

    public Image getIconImage(boolean small) {
        if (factory instanceof EnhancedBrowserFactory) {
            return ((EnhancedBrowserFactory)factory).getIconImage(small);
        }
        return null;
    }

    BrowserURLMapperImplementation getBrowserURLMapper() {
        if (factory instanceof BrowserURLMapperProvider) {
            return ((BrowserURLMapperProvider)factory).getBrowserURLMapper();
        }
        return null;
    }

    @Override
    public String toString() {
        return "WebBrowserFactoryDescriptor{" + "id=" + id + ", def=" + def + ", factory=" + factory + '}';
    }
    
    static BrowserFamilyId convertBrowserFamilyId(PrivateBrowserFamilyId privateBrowserFamilyId) {
        switch (privateBrowserFamilyId) {
            case FIREFOX:
                return BrowserFamilyId.FIREFOX;
            case MOZILLA:
                return BrowserFamilyId.MOZILLA;
            case CHROME:
                return BrowserFamilyId.CHROME;
            case CHROMIUM:
                return BrowserFamilyId.CHROMIUM;
            case SAFARI:
                return BrowserFamilyId.SAFARI;
            case IE:
                return BrowserFamilyId.IE;
            case OPERA:
                return BrowserFamilyId.OPERA;
            case EDGE:
                return BrowserFamilyId.EDGE;
            default:
                return BrowserFamilyId.UNKNOWN;
        }
    }

}
