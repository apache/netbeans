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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.core.IDESettings;
import org.netbeans.modules.extbrowser.ExtWebBrowser;
import org.netbeans.modules.extbrowser.PrivateBrowserFamilyId;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;

/**
 * Access to browsers available in the IDE.
 * @see WebBrowserSupport
 */
public final class WebBrowsers {

    /**
     * Property fired when list of browsers has changed.
     */
    public static final String PROP_BROWSERS = "browsers"; // NOI18N
    
    /**
     * Property fired when default browser has changed.
     */
    public static final String PROP_DEFAULT_BROWSER = "browser"; // NOI18N
    
    private static final WebBrowsers INST = new WebBrowsers();
    private static final String BROWSERS_FOLDER = "Services/Browsers"; // NOI18N
    private static final String BROWSERS2_FOLDER = "Services/Browsers2"; // NOI18N
    
    //private WebBrowserFactories fact;
    private PropertyChangeSupport sup = new PropertyChangeSupport(this);
    private PropertyChangeListener l;
    private FileChangeListener lis;
    private PreferenceChangeListener lis2;
    
    static final String DEFAULT = "default"; // NOI18N
    
    private WebBrowsers() {
        sup = new PropertyChangeSupport(this);
        FileObject servicesBrowsers = getConfigFolder();
        if (servicesBrowsers != null) {
            lis = new FileChangeListener() {

                @Override
                public void fileFolderCreated(FileEvent fe) {
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    fireBrowsersChange();
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    fireBrowsersChange();
                }
            };
            servicesBrowsers.addRecursiveListener(lis);
        }
        lis2 = new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (IDESettings.PROP_WWWBROWSER.equals(evt.getKey())) {
                    fireDefaultBrowserChange();
                }
            }
        };
        NbPreferences.forModule(IDESettings.class).addPreferenceChangeListener(lis2);
    }

    /**
     * Singleton instance of WebBrowsers class
     */
    public static WebBrowsers getInstance() {
        return INST;
    }
    
    /**
     * Returns browser corresponding to user's choice in IDE options.
     */
    public WebBrowser getPreferred() {
        WebBrowserFactoryDescriptor someFactory = null;
        for (WebBrowserFactoryDescriptor desc : getFactories(true)) {
            if (someFactory == null) {
                someFactory = desc;
            }
            if (!desc.isDefault()) {
                continue;
            }
            return new WebBrowser(desc);
        }
        // if none of the browsers is marked as default one then simply use first one:
        if (someFactory != null) {
            return new WebBrowser(someFactory);
        }
        assert false : "there are no browsers registered on your classpath. can you fix that"; // NOI18N
        return null;
    }

    /**
     * Get default embedded browser.
     * @return can be null if none installed
     */
    public WebBrowser getEmbedded() {
        for (WebBrowserFactoryDescriptor desc : getFactories(true)) {
            if (desc.getBrowserFamily() != BrowserFamilyId.JAVAFX_WEBVIEW) {
                continue;
            }
            return new WebBrowser(desc);
        }
        return null;
    }

    /**
     * Returns all browsers registered in the IDE.
     */
    public List<WebBrowser> getAll(boolean includeSystemDefaultBrowser,
            boolean includeIDEGlobalBrowserOption,
            boolean sortBrowsers) {
        return getAll(includeSystemDefaultBrowser, 
                includeIDEGlobalBrowserOption, false, sortBrowsers);
    }

    /**
     * Returns all browsers registered in the IDE.
     */
    public List<WebBrowser> getAll(boolean includeSystemDefaultBrowser,
            boolean includeIDEGlobalBrowserOption,
            boolean includePhoneGap,
            boolean sortBrowsers) {
        if (sortBrowsers) {
            return getSortedBrowsers(includeSystemDefaultBrowser, 
                    includeIDEGlobalBrowserOption, includePhoneGap);
        } else {
            return getUnsortedBrowsers(includeSystemDefaultBrowser, 
                    includeIDEGlobalBrowserOption, includePhoneGap);
        }
    }

    private List<WebBrowser> getSortedBrowsers(boolean includeSystemDefaultBrowser, 
            boolean includeIDEGlobalBrowserOption,
            boolean includePhoneGap) {
        List<BrowserWrapper> browsers = new ArrayList<BrowserWrapper>();
        int chrome = 200;
        int chromium = 300;
        int others = 1000;
        int android = 1400;
        int ios = 1500;
        int phonegap = 1600;
        for (WebBrowserFactoryDescriptor desc : getFactories(includeSystemDefaultBrowser)) {
            if (desc.getBrowserFamily().equals(BrowserFamilyId.PHONEGAP) && !includePhoneGap) {
                continue;
            }
            WebBrowser browser = new WebBrowser(desc);
            if (browser.getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW) {
                browsers.add(new BrowserWrapper(browser, 100));
            } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROME || browser.getId().endsWith("ChromeBrowser")) { // NOI18N
                BrowserWrapper wrapper = new BrowserWrapper(browser, chrome++);
                browsers.add(wrapper);
            } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROMIUM || browser.getId().endsWith("ChromiumBrowser")) { // NOI18N
                BrowserWrapper wrapper = new BrowserWrapper(browser, chromium++);
                browsers.add(wrapper);
            } else if (browser.getBrowserFamily() == BrowserFamilyId.ANDROID) { // NOI18N
                browsers.add(new BrowserWrapper(browser, android++));
            } else if (browser.getBrowserFamily() == BrowserFamilyId.IOS) { // NOI18N
                browsers.add(new BrowserWrapper(browser, ios++));
            } else if (browser.getBrowserFamily() == BrowserFamilyId.PHONEGAP) { // NOI18N
                browsers.add(new BrowserWrapper(browser, phonegap++));
            } else {
                browsers.add(new BrowserWrapper(browser, others++));
            }
        }
        Collections.sort(browsers, new Comparator<BrowserWrapper>() {
            @Override
            public int compare(BrowserWrapper o1, BrowserWrapper o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        List<WebBrowser> result = new ArrayList<WebBrowser>();
        if (includeIDEGlobalBrowserOption) {
            result.add(createIDEGlobalDelegate());
        }
        for (BrowserWrapper bw : browsers) {
            result.add(bw.getBrowser());
        }
        return result;
    }


    @NbBundle.Messages({
        "WebBrowsers.idebrowser=IDE's default browser"
    })
    private WebBrowser createIDEGlobalDelegate() {
        return WebBrowser.createIDEGlobalDelegate();
    }

    private List<WebBrowser> getUnsortedBrowsers(boolean includeSystemDefaultBrowser, 
            boolean includeIDEGlobalBrowserOption,
            boolean includeMobileBrowsers) {
        List<WebBrowser> browsers = new ArrayList<WebBrowser>();
        if (includeIDEGlobalBrowserOption) {
            browsers.add(createIDEGlobalDelegate());
        }
        for (WebBrowserFactoryDescriptor desc : getFactories(includeSystemDefaultBrowser)) {
            if (desc.getBrowserFamily().isMobile() && !includeMobileBrowsers) {
                continue;
            }
            WebBrowser browser = new WebBrowser(desc);
            browsers.add(browser);
        }
        return browsers;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        sup.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        sup.removePropertyChangeListener(l);
    }
    
    private void fireBrowsersChange() {
        sup.firePropertyChange(PROP_BROWSERS, null, null);
    }
    
    private void fireDefaultBrowserChange() {
        sup.firePropertyChange(PROP_DEFAULT_BROWSER, null, null);
    }

    
    private FileObject getConfigFolder() {
        return FileUtil.getConfigFile(BROWSERS_FOLDER);
    }
    
    private List<WebBrowserFactoryDescriptor> getFactories(boolean includeSystemDefaultBrowser) {
        List<WebBrowserFactoryDescriptor> browsers = new ArrayList<>();
        List<WebBrowserFactoryDescriptor> browsersToUpdate = new ArrayList<>();

        WebBrowserFactoryDescriptor advancedChrome = null;
        WebBrowserFactoryDescriptor advancedChromium = null;
        Lookup l = Lookups.forPath(BROWSERS2_FOLDER);
        for (HtmlBrowser.Factory f : l.lookupAll(HtmlBrowser.Factory.class)) {
            if (!(f instanceof EnhancedBrowserFactory)) {
                continue;
            }
            EnhancedBrowserFactory fact = (EnhancedBrowserFactory)f;
            if (!fact.canCreateHtmlBrowserImpl()) {
                continue;
            }
            String browserId = fact.getId();
            if (browserId == null) {
                // fallback:
                browserId = fact.getBrowserFamilyId().toString();
            }
            WebBrowserFactoryDescriptor desc = new WebBrowserFactoryDescriptor(
                    browserId,
                    null,
                    false,
                    f);
            browsers.add(desc);
            if (fact.getBrowserFamilyId() == BrowserFamilyId.CHROME && !fact.hasNetBeansIntegration()) {
                advancedChrome = desc;
            }
            if (fact.getBrowserFamilyId() == BrowserFamilyId.CHROMIUM && !fact.hasNetBeansIntegration()) {
                advancedChromium = desc;
            }
            browsersToUpdate.add(desc);
        }

        FileObject servicesBrowsers = getConfigFolder();
        if (servicesBrowsers == null) {
            return browsers;
        }

        DataFolder folder = DataFolder.findFolder(servicesBrowsers);
        // force object creation:
        Lookup.getDefault ().lookupAll(HtmlBrowser.Factory.class).toArray();
        for (DataObject browserSetting : folder.getChildren()) {
            if (Boolean.TRUE.equals(browserSetting.getPrimaryFile().getAttribute("hidden"))) {
                continue;
            }
            if (!includeSystemDefaultBrowser && isSystemDefaultBrowser(browserSetting)) {
                continue;
            }
            InstanceCookie cookie = browserSetting.getCookie(InstanceCookie.class);
            if (cookie == null) {
                continue;
            }
            HtmlBrowser.Factory fact;
            try {
                fact = (HtmlBrowser.Factory) cookie.instanceCreate();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            
            Lookup.Item<HtmlBrowser.Factory> item =
                    Lookup.getDefault ().lookupItem (
                            new Lookup.Template<HtmlBrowser.Factory> (
                                    HtmlBrowser.Factory.class, null, fact));
            if (item == null) {
                continue;
            }
            ExtWebBrowser extFact = fact instanceof ExtWebBrowser ? (ExtWebBrowser)fact : null;
            boolean isDefault;
            if (IDESettings.getWWWBrowser() == null){
                isDefault = false;
            }
            else {
                isDefault = IDESettings.getWWWBrowser().equals(fact);
            }
            if (extFact != null && !isSystemDefaultBrowser(browserSetting)) {
                // if this is Chrome browser instance which is configurable in
                // the Options UI then its browser executable configuration should be
                // used by "Chrome with NB integration"; similarly for Chromium
                if (extFact.getPrivateBrowserFamilyId() == PrivateBrowserFamilyId.CHROME) {
                    setBrowserExecutableDelegate(extFact, browsersToUpdate, BrowserFamilyId.CHROME);
                } else if (extFact.getPrivateBrowserFamilyId() == PrivateBrowserFamilyId.CHROMIUM) {
                    setBrowserExecutableDelegate(extFact, browsersToUpdate, BrowserFamilyId.CHROMIUM);
                }
            }

            // ignore default Chrome browser if advanced version is available;
            // this will also cover case when "Default System Browser" is configured
            // in the IDE Options and Chrome is OS's default browser - even in this case
            // the SystemDefaultBrowser instance is going to be replaced with advanced
            // version of Chrome otherwise some features would not work (eg. saving changes in CDT)
            // if replaced browser was a default one then advanced browser version
            // is marked as default;
            if (advancedChrome != null && extFact != null &&
                    extFact.getPrivateBrowserFamilyId() == PrivateBrowserFamilyId.CHROME) {
                if (isDefault) {
                    advancedChrome.setDefault(true);
                }
                continue;
            }
            if (advancedChromium != null && extFact != null &&
                    extFact.getPrivateBrowserFamilyId() == PrivateBrowserFamilyId.CHROMIUM) {
                if (isDefault) {
                    advancedChromium.setDefault(true);
                }
                continue;
            }
            
            browsers.add(
                new WebBrowserFactoryDescriptor(
                    item.getId(), 
                    browserSetting, 
                    isDefault,
                    fact));
        }
        return browsers;
    }

    static BrowserFamilyId getIDEOptionsBrowserFamily() {
        HtmlBrowser.Factory factory = IDESettings.getWWWBrowser();
        if (factory != null && factory instanceof ExtWebBrowser) {
            return WebBrowserFactoryDescriptor.convertBrowserFamilyId(((ExtWebBrowser)factory).getPrivateBrowserFamilyId());
        } else if (factory != null && factory instanceof EnhancedBrowserFactory) {
            return ((EnhancedBrowserFactory)factory).getBrowserFamilyId();
        }
        return BrowserFamilyId.UNKNOWN;
    }

    private boolean isSystemDefaultBrowser(DataObject browserSetting) {
        return browserSetting.getPrimaryFile().getName().startsWith("SystemDefaultBrowser");
    }

    private void setBrowserExecutableDelegate(ExtWebBrowser fact, List<WebBrowserFactoryDescriptor> browsersToUpdate, BrowserFamilyId family) {
        for (WebBrowserFactoryDescriptor desc : browsersToUpdate) {
            if (desc.getBrowserFamily() == family && desc.getFactory() instanceof ExtWebBrowser) {
                ((ExtWebBrowser)desc.getFactory()).useBrowserExecutableDelegate(fact);
            }
        }
    }

    /**
     * Wrapper class for {@link WebBrowser}.
     * <p>
     * This class is thread-safe.
     */
    private static final class BrowserWrapper {

        private final WebBrowser browser;
        private final int order;

        public BrowserWrapper(WebBrowser browser, int order) {
            this.browser = browser;
            this.order = order;
        }

        public WebBrowser getBrowser() {
            return browser;
        }

        int getOrder() {
            return order;
        }

    }


}
