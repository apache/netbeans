/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
