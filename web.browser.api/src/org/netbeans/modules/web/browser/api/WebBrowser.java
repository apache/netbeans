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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.core.IDESettings;
import org.netbeans.modules.extbrowser.ExtWebBrowser;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Single browser registered in the IDE.
 */
public final class WebBrowser {

    private WebBrowserFactoryDescriptor factoryDesc;
    private Preferences prefs = null;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private static RequestProcessor RP = new RequestProcessor();

    WebBrowser(WebBrowserFactoryDescriptor factoryDesc) {
        this.factoryDesc = factoryDesc;
    }

    private WebBrowser() {
        refreshDelegate();
        addListener();
    }

    private void refreshDelegate() {
        WebBrowser ideBrowser = WebBrowsers.getInstance().getPreferred();
        this.factoryDesc = new WebBrowserFactoryDescriptor(
                ideBrowser.getFactoryDesc(), WebBrowsers.DEFAULT, Bundle.WebBrowsers_idebrowser());
        changeSupport.fireChange();
    }

    private void addListener() {
        assert prefs == null;
        prefs = NbPreferences.forModule(IDESettings.class);
        prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (IDESettings.PROP_WWWBROWSER.equals(evt.getKey())) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshDelegate();
                        }
                    });
                }
            }
        });
        if (factoryDesc.getFactory() instanceof ExtWebBrowser) {
            ExtWebBrowser fa = (ExtWebBrowser)factoryDesc.getFactory();
            fa.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ExtWebBrowser.PROP_PRIVATE_BROWSER_FAMILY)) {
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshDelegate();
                            }
                        });
                    }
                }
            });
        }
    }

    static WebBrowser createIDEGlobalDelegate() {
        return new WebBrowser();
    }

    
    /**
     * Unique ID of browser. Useful for example to store per project reference to
     * user's browser choice.
     */
    public String getId() {
        return factoryDesc.getId();
    }

    public boolean hasNetBeansIntegration() {
        return factoryDesc.hasNetBeansIntegration();
    }

    /**
     * Name eg. FireFox, WebView, ...
     *
     * @return
     */
    public String getName() {
        return factoryDesc.getName();
    }

    public Image getIconImage(boolean small) {
        Image im = factoryDesc.getIconImage(small);
        if (im == null) {
            ImageIcon icon = ImageUtilities.loadImageIcon( getIconFile(getBrowserFamily(), small), true );
            im = ImageUtilities.icon2Image( icon );
        }
        if (hasNetBeansIntegration() && factoryDesc.getBrowserFamily() != BrowserFamilyId.JAVAFX_WEBVIEW) {
//            im = ImageUtilities.mergeImages(
//                im,
//                ImageUtilities.loadImage("org/netbeans/modules/web/browser/ui/resources/nb-badge.png"),
//            12, 12);
        }
        return im;
    }
    
    public BrowserFamilyId getBrowserFamily() {
        return factoryDesc.getBrowserFamily();
    }

    /**
     * Is IDE embedded browser or external browser.
     */
    public boolean isEmbedded() {
        return getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW;
    }

    private static Map<Project, BrowserURLMapperImplementation.BrowserURLMapper> browserMappings =
            new WeakHashMap<>();

    /**
     * Let browser implementation convert given URL into a browser specific URL
     * before opening it in browser. For example Android device Chrome browser
     * converts localhost URL into IP address so that Android device can access
     * the locahost.
     * @return converted or original URL
     */
    public URL toBrowserURL(Project p, FileObject projectFile, URL serverURL) {
        BrowserURLMapperImplementation impl = factoryDesc.getBrowserURLMapper();
        if (impl != null) {
            BrowserURLMapperImplementation.BrowserURLMapper m = impl.toBrowser(p, projectFile, serverURL);
            if (m != null) {
                browserMappings.put(p, m);
                String url = WebUtils.urlToString(serverURL);
                if (url.startsWith(m.getServerURLRoot())) {
                    url = m.getBrowserURLRoot() + url.substring(m.getServerURLRoot().length());
                    return WebUtils.stringToUrl(url);
                }
            } else {
                browserMappings.remove(p);
            }
        }
        return serverURL;
    }

    /**
     * Let browser implementation convert given browser URL into an URL which
     * can be translated back to project's source file. This is counter part for
     * {@link #toBrowserURL} method which translates browser specific URL back
     * to a URL which can be translated into project's source file.
     * @return converted or original URL
     */
    public URL fromBrowserURL(Project p, URL serverURL) {
        BrowserURLMapperImplementation.BrowserURLMapper m = browserMappings.get(p);
        if (m != null) {
            String url = WebUtils.urlToString(serverURL);
            if (url.startsWith(m.getBrowserURLRoot())) {
                url = m.getServerURLRoot()+ url.substring(m.getBrowserURLRoot().length());
            }
            return WebUtils.stringToUrl(url);
        }
        return serverURL;
    }

    /**
     * This methods creates new browser "pane", that is tab in external browser
     * or TopComponent for embedded browser. Through this method clients have control 
     * how many browser panes are opened. In case of embedded browser it is 
     * straightforward - each call of this method will result into a new TopComponent. 
     * In case of external browser situation depends on availability of NetBeans 
     * browser plugins. If browser plugins are available then the same behaviour as 
     * in the case of embedded browser is possible and user can via this method 
     * create multiple tabs in external browser or keep single tab and open all 
     * URLs in the single tab.
     */
    public WebBrowserPane createNewBrowserPane() {
        return createNewBrowserPane(true);
    }
    
    public WebBrowserPane createNewBrowserPane(WebBrowserFeatures features) {
        return createNewBrowserPane(features, true);
    }
    /**
     * The only difference from createNewBrowserPane() is that automatic TopComponent
     * creation in case of embedded browser can be prevented by setting 
     * wrapEmbeddedBrowserInTopComponent to false. Doing that means that client
     * of WebBrowserPane must call WebBrowserPane.getComponent method and 
     * take care about showing browser component in IDE. This is useful for example
     * in case when HTML file editor has multiview and one of its tabs is "Preview"
     * showing rendered view of the HTML document.
     */
    public WebBrowserPane createNewBrowserPane(boolean wrapEmbeddedBrowserInTopComponent) {
        return createNewBrowserPane(new WebBrowserFeatures(), wrapEmbeddedBrowserInTopComponent);
    }

    public WebBrowserPane createNewBrowserPane(WebBrowserFeatures features, boolean wrapEmbeddedBrowserInTopComponent) {
        return new WebBrowserPane(features, factoryDesc, wrapEmbeddedBrowserInTopComponent);
    }

    /**
     * Retrieve HTMLBrowser factory wrapped in this instance.
     * @return HtmlBrowser factory.
     */
    public HtmlBrowser.Factory getHtmlBrowserFactory() {
        return factoryDesc.getFactory();
    }

    WebBrowserFactoryDescriptor getFactoryDesc() {
        return factoryDesc;
    }

    private static final @StaticResource String CHROME_SMALL = "org/netbeans/modules/web/browser/ui/resources/browser_chrome_16x.png"; // NOI18N
    private static final @StaticResource String CHROME_LARGE = "org/netbeans/modules/web/browser/ui/resources/browser_chrome_24x.png"; // NOI18N
    private static final @StaticResource String FIREFOX_SMALL = "org/netbeans/modules/web/browser/ui/resources/browser_firefox_16x.png"; // NOI18N
    private static final @StaticResource String FIREFOX_LARGE = "org/netbeans/modules/web/browser/ui/resources/browser_firefox_24x.png"; // NOI18N
    private static final @StaticResource String CHROMIUM_SMALL = "org/netbeans/modules/web/browser/ui/resources/browser_chromium_16x.png"; // NOI18N
    private static final @StaticResource String CHROMIUM_LARGE = "org/netbeans/modules/web/browser/ui/resources/browser_chromium_24x.png"; // NOI18N
    private static final @StaticResource String IE_SMALL = "org/netbeans/modules/web/browser/ui/resources/browser_ie_16x.png"; // NOI18N
    private static final @StaticResource String IE_LARGE = "org/netbeans/modules/web/browser/ui/resources/browser_ie_24x.png"; // NOI18N
    private static final @StaticResource String EDGE_SMALL = "org/netbeans/modules/web/browser/ui/resources/browser_edge_16x.png"; // NOI18N
    private static final @StaticResource String EDGE_LARGE = "org/netbeans/modules/web/browser/ui/resources/browser_edge_24x.png"; // NOI18N
    private static final @StaticResource String SAFARI_SMALL = "org/netbeans/modules/web/browser/ui/resources/browser_safari_16x.png"; // NOI18N
    private static final @StaticResource String SAFARI_LARGE = "org/netbeans/modules/web/browser/ui/resources/browser_safari_24x.png"; // NOI18N
    private static final @StaticResource String GENERIC_SMALL = "org/netbeans/modules/web/browser/ui/resources/browser_generic_16x.png"; // NOI18N
    private static final @StaticResource String GENERIC_LARGE = "org/netbeans/modules/web/browser/ui/resources/browser_generic_24x.png"; // NOI18N

    private static String getIconFile(BrowserFamilyId browserFamily, boolean small) {
        switch (browserFamily) {
            case CHROME:
                return small ? CHROME_SMALL : CHROME_LARGE;
            case FIREFOX:
                return small ? FIREFOX_SMALL : FIREFOX_LARGE;
            case CHROMIUM:
                return small ? CHROMIUM_SMALL : CHROMIUM_LARGE;
            case IE:
                return small ? IE_SMALL : IE_LARGE;
            case SAFARI:
                return small ? SAFARI_SMALL : SAFARI_LARGE;
            case EDGE:
                return small ? EDGE_SMALL : EDGE_LARGE;
            default:
                return small ? GENERIC_SMALL : GENERIC_LARGE;
        }
       
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
}
