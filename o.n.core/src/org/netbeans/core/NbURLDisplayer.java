/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core;

import java.awt.Component;
import java.awt.Desktop;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.core.ui.SwingBrowser;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.awt.HtmlBrowser.Impl;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of URL displayer, which shows documents in the configured web browser.
 */
@ServiceProvider(service=URLDisplayer.class)
public final class NbURLDisplayer extends URLDisplayer {

    private static final RequestProcessor RP = new RequestProcessor( "URLDisplayer" ); //NOI18N

    private NbBrowser htmlViewer;

    @Override
    public void showURL(final URL u) {
        RP.post( new Runnable() {
            @Override
            public void run() {
                //warm the browser up to avoid waiting for Lookups
                warmBrowserUp( false );
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        if (htmlViewer == null) {
                            htmlViewer = new NbBrowser();
                        }
                        htmlViewer.showUrl(u);
                    }
                });
            }
        });
    }

    @Override
    public void showURLExternal(final URL u) {
        RP.post( new Runnable() {
            @Override
            public void run() {
                //warm the browser up to avoid waiting for Lookups
                warmBrowserUp( true );
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        if (htmlViewer == null) {
                            htmlViewer = new NbBrowser();
                        }
                        htmlViewer.showUrlExternal(u);
                    }
                });
            }
        });
    }

    //#220880 - ask for browser Lookup outside the EDT
    private void warmBrowserUp( boolean externalBrowser ) {
        if( externalBrowser && (null == htmlViewer || null == htmlViewer.externalBrowser)
                || !externalBrowser && (null == htmlViewer || null == htmlViewer.brComp) ) {

            Factory browserFactory = externalBrowser ? IDESettings.getExternalWWWBrowser() : IDESettings.getWWWBrowser();
            if( null != browserFactory ) {
                HtmlBrowser.Impl browserImpl = browserFactory.createHtmlBrowserImpl();
                if( null != browserImpl ) {
                    browserImpl.getLookup();
                }
            }
        }
    }

    /**
     * Able to reuse HtmlBrowserComponent.
     */
    private static class NbBrowser {

        private HtmlBrowserComponent brComp;
        private HtmlBrowserComponent externalBrowser;
        private PreferenceChangeListener idePCL;
        private static Lookup.Result factoryResult;

        static {
            factoryResult = Lookup.getDefault().lookupResult(Factory.class);
            factoryResult.allItems();
            factoryResult.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    ((NbURLDisplayer) URLDisplayer.getDefault()).htmlViewer = null;
                }
            });
        }

        public NbBrowser() {
            setListener();
        }

        /** Show URL in browser
         * @param url URL to be shown
         */
        private void showUrl(URL url) {
            if( null == brComp )
                brComp = createDefaultBrowser();
            brComp.setURLAndOpen(url);
        }

        /**
         * Show URL in an external browser.
         * @param url URL to show
         */
        private void showUrlExternal(URL url) {
            if( null == externalBrowser )
                externalBrowser = createExternalBrowser();
            externalBrowser.setURLAndOpen(url);
        }

        private HtmlBrowserComponent createDefaultBrowser() {
            Factory browser = IDESettings.getWWWBrowser();
            if (browser == null) {
                // Fallback.
                browser = new SwingBrowser();
            }
            HtmlBrowserComponent res = new HtmlBrowserComponent(browser, true, true);
            res.putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
            return res;
        }

        private HtmlBrowserComponent createExternalBrowser() {
            Factory browser = IDESettings.getExternalWWWBrowser();
            if (browser == null) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    browser = new DesktopBrowser(desktop);
                } else {
                    //external browser is not available, fallback to swingbrowser
                    browser = new SwingBrowser();
                }
            }
            return new HtmlBrowserComponent(browser, true, true);
        }

        /**
         *  Sets listener that invalidates this as main IDE's browser if user changes the settings
         */
        private void setListener() {
            if (idePCL != null) {
                return;
            }
            try {
                // listen on preffered browser change
                idePCL = new PreferenceChangeListener() {
                    public void preferenceChange(PreferenceChangeEvent evt) {
                        if (IDESettings.PROP_WWWBROWSER.equals(evt.getKey())) {
                            ((NbURLDisplayer) URLDisplayer.getDefault()).htmlViewer = null;
                            if (idePCL != null) {
                                IDESettings.getPreferences().removePreferenceChangeListener(idePCL);
                                idePCL = null;
                                brComp = null;
                                externalBrowser = null;
                            }
                        }
                    }
                };
                IDESettings.getPreferences().addPreferenceChangeListener(idePCL);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class DesktopBrowser implements Factory {
        private final Desktop desktop;
        public DesktopBrowser(Desktop desktop) {
            this.desktop = desktop;
        }
        public @Override Impl createHtmlBrowserImpl() {
            return new Impl() {
                private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
                private URL url;
                public @Override void setURL(URL url) {
                    this.url = url;
                    URL url2 = externalize(url);
                    try {
                        desktop.browse(url2.toURI());
                    } catch (Exception x) {
                        Logger.getLogger(NbURLDisplayer.class.getName()).log(Level.INFO, "showing: " + url2, x);
                    }
                }
                public @Override URL getURL() {
                    return url;
                }
                public @Override void reloadDocument() {
                    setURL(url);
                }
                public @Override void addPropertyChangeListener(PropertyChangeListener l) {
                    pcs.addPropertyChangeListener(l);
                }
                public @Override void removePropertyChangeListener(PropertyChangeListener l) {
                    pcs.removePropertyChangeListener(l);
                }
                public @Override Component getComponent() {return null;}
                public @Override void stopLoading() {}
                public @Override String getStatusMessage() {return "";}
                public @Override String getTitle() {return "";}
                public @Override boolean isForward() {return false;}
                public @Override void forward() {}
                public @Override boolean isBackward() {return false;}
                public @Override void backward() {}
                public @Override boolean isHistory() {return false;}
                public @Override void showHistory() {}
            };
        }
        // Adapted from org.netbeans.modules.extbrowser.URLUtil. Useful in conjunction with o.n.m.httpserver.
        private static URL externalize(URL u) {
            String proto = u.getProtocol();
            // Standard browser *might* handle jar protocol, but cannot be relied upon.
            if (proto == null || proto.equals("file") || proto.equals("http") || proto.equals("https")) { // NOI18N
                return u;
            }
            // Possibly internal protocol; try to convert to an external form, useful e.g. with httpserver module.
            FileObject f = URLMapper.findFileObject(u);
            if (f == null) {
                // Oh well, hope for the best.
                return u;
            }
            URL u2 = URLMapper.findURL(f, URLMapper.NETWORK);
            if (u2 == null) {
                // Again, hope for the best.
                return u;
            }
            try {
                String query = u.getQuery();
                if (query != null) {
                    u2 = new URL(u2, "?" + query); // XXX encoding?
                }
                String anchor = u.getRef();
                if (anchor != null) {
                    u2 = new URL(u2, "#" + anchor); // XXX encoding?
                }
            } catch (MalformedURLException x) {
                // Query/anchor might have been important.
                return u;
            }
            return u2;
        }
    }

}
