/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cordova.platforms.ios;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Becicka
 */
public class IOSBrowser extends HtmlBrowser.Impl implements EnhancedBrowser {
    private final Kind kind;
    private Lookup projectContext;
    private static final Logger LOGGER = Logger.getLogger(IOSBrowser.class.getName());

    @Override
    public void initialize(WebBrowserFeatures browserFeatures) {
    }

    @Override
    public void close(boolean closeTab) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                WebKitDebuggingSupport.getDefault().stopDebugging(true);
            }
        });
    }

    @Override
    public void setProjectContext(Lookup projectContext) {
        this.projectContext = projectContext;
    }

    @Override
    public boolean canReloadPage() {
        return false;
    }
    
    @Override
    public boolean ignoreChange(FileObject fo) {
        return BrowserSupport.ignoreChangeDefaultImpl(fo);
    }

    public static enum Kind {
        IOS_DEVICE_DEFAULT,
        IOS_SIMULATOR_DEFAULT
    }
    

    public IOSBrowser(Kind kind) {
        this.kind = kind;
    }
    
    private URL url;

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public void reloadDocument() {
        WebKitDebuggingSupport.getDefault().reload();
    }

    @Override
    public void stopLoading() {
    }

    @NbBundle.Messages({
            "LBL_DeviceNotConnected=iOS Device Not Connected.",
            "TTL_DeviceNotConnected=Cannot Connect to Device"
    })
    @Override
    public void setURL(final URL url) {
        if (!Utilities.isMac()) {
            NotifyDescriptor not = new NotifyDescriptor(
                    Bundle.LBL_NoMac(),
                    Bundle.ERR_Title(),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    null,
                    null);
            DialogDisplayer.getDefault().notify(not);
            return;
        }
        final WebKitDebuggingSupport build = WebKitDebuggingSupport.getDefault();

        this.url = url;
        final IOSDevice dev = kind == Kind.IOS_DEVICE_DEFAULT ? IOSDevice.CONNECTED : IOSDevice.IPHONE;
        dev.openUrl(url.toExternalForm());
        final Project project = projectContext.lookup(Project.class);
        if (project == null) {
            //dont start debugging session for non project files
            return;
        }
        try {
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    if (kind == Kind.IOS_DEVICE_DEFAULT) {
                        try {
                            build.startDebugging(dev, project, new ProxyLookup(projectContext, Lookups.fixed(ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/ios/iosdevice16.png"), url)), true);
                        } catch (IllegalStateException ise) {
                            build.stopDebugging(true);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    NotifyDescriptor not = new NotifyDescriptor(
                                            Bundle.LBL_DeviceNotConnected(),
                                            Bundle.TTL_DeviceNotConnected(),
                                            NotifyDescriptor.DEFAULT_OPTION,
                                            NotifyDescriptor.ERROR_MESSAGE,
                                            null,
                                            null);
                                    DialogDisplayer.getDefault().notify(not);
                                }
                            });
                        }
                    } else {
                        build.startDebugging(dev, project, new ProxyLookup(projectContext, Lookups.fixed(ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/ios/iossimulator16.png"), url)), true);
                    }
                }
            }, kind == Kind.IOS_DEVICE_DEFAULT ? Bundle.LBL_OpeningiOS() : Bundle.LBL_Opening(), new AtomicBoolean(), true);
        } catch (IllegalStateException ise) {
            LOGGER.log(Level.FINE, "Stopping debugging.", ise);         //NOI18N
            WebKitDebuggingSupport.getDefault().stopDebugging(true);
        }
    }

    @NbBundle.Messages(
            "LBL_OpeningiOS=Connecting to iOS Device.\nMake sure, that:\n"
            + "\u2022 Device is attached\n"
            + "\u2022 Web Inspector is enabled on your device (Settings | Safari | Advanced)\n"
            + "\u2022 Mobile Safari is running\n"
            + "\u2022 Your computer and iOS device are connected to the same WiFi network")
    public static void openBrowser(String command, final Lookup context, final IOSBrowser.Kind kind, final Project project, final BrowserSupport browserSupport) throws IllegalArgumentException {
        final WebKitDebuggingSupport build = WebKitDebuggingSupport.getDefault();
        assert project != null && build != null;
        final IOSDevice dev = kind == Kind.IOS_DEVICE_DEFAULT ? IOSDevice.CONNECTED : IOSDevice.IPHONE;
        final String url1 = build.getUrl(project, context);
        FileObject f = build.getFile(project, context);
        try {
            browserSupport.load(new URL(url1), f);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String getStatusMessage() {
        return "";
    }

    @Override
    @NbBundle.Messages("LBL_SafariTitle=Safari")
    public String getTitle() {
        return Bundle.LBL_SafariTitle();
    }

    @Override
    public boolean isForward() {
        return false;
    }

    @Override
    public void forward() {
    }

    @Override
    public boolean isBackward() {
        return false;
    }

    @Override
    public void backward() {
    }

    @Override
    public boolean isHistory() {
        return false;
    }

    @Override
    public void showHistory() {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
}
