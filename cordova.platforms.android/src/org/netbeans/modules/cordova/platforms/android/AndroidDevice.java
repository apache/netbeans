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
package org.netbeans.modules.cordova.platforms.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.spi.PropertyProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class AndroidDevice implements Device {
    
    public static Collection<org.netbeans.modules.cordova.platforms.spi.Device> parse(String output) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(output));

        Pattern pattern = Pattern.compile("([-\\w]+)\\s+([\\w]+) *"); //NOI18N

        ArrayList<org.netbeans.modules.cordova.platforms.spi.Device> result = new ArrayList<org.netbeans.modules.cordova.platforms.spi.Device>();
        //ignore first line

        String line = r.readLine();

        while (((line = r.readLine()) != null)) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                final String name = m.group(1);
                AndroidDevice device = new AndroidDevice(name, Browser.DEFAULT, name.startsWith("emulator")); // NOI18N
                result.add(device);
            }
        }
        return result;
    }
    
    private Browser browser;

    @Override
    public void openUrl(String url) {
        try {
            if (browser == browser.DEFAULT) {
            ProcessUtilities.callProcess(
                    ((AndroidPlatform) getPlatform()).getAdbCommand(), 
                    true,
                    AndroidPlatform.DEFAULT_TIMEOUT,
                    isEmulator() ? "-e" : "-d", // NOI18N
                    "wait-for-device", // NOI18N
                    "shell", // NOI18N
                    "am", // NOI18N
                    "start", // NOI18N
                    "-e", // NOI18N
                    "com.android.browser.application_id", // NOI18N
                    "org.netbeans.modules.cordova", // NOI18N
                    "-a", // NOI18N
                    "android.intent.action.VIEW", // NOI18N
                    url); //NOI18N
            } else {
            ProcessUtilities.callProcess(
                    ((AndroidPlatform) getPlatform()).getAdbCommand(), 
                    true, 
                    AndroidPlatform.DEFAULT_TIMEOUT,
                    isEmulator() ? "-e" : "-d", // NOI18N
                    "wait-for-device", // NOI18N
                    "shell", // NOI18N
                    "am", // NOI18N
                    "start", // NOI18N
                    "-e", // NOI18N
                    "com.android.browser.application_id", // NOI18N
                    "org.netbeans.modules.cordova", // NOI18N
                    "-a", // NOI18N
                    "android.intent.action.VIEW", // NOI18N
                    "-n", // NOI18N
                    getPrefferedBrowser(), 
                    url); //NOI18N
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    static Device get(String name, EditableProperties props) {
        final String property = props.getProperty(Device.DEVICE_PROP);
        final boolean b = Device.EMULATOR.equals(property);
        String property1 = props.getProperty(Device.BROWSER_PROP);
        if (property1!=null && property1.equals(Browser.CHROME.getName())) {
            return new AndroidDevice(name, Browser.CHROME, b);
        }

        return new AndroidDevice(name, Browser.DEFAULT, b);
    }
    
    private final String name;
    private boolean emulator;

    public AndroidDevice(String name, Browser browser, boolean emulator) {
        this.name = name;
        this.browser = browser;
        this.emulator = emulator;
    }
    
    public boolean isEmulator() {
        return emulator; //NOI18N
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Device{" + "name=" + name + ", emulator: " + isEmulator() + '}'; //NOI18N
    }

    @Override
    public MobilePlatform getPlatform() {
        return AndroidPlatform.getDefault();
    }
    
    @Override
    public MobileDebugTransport getDebugTransport() {
        return new AndroidDebugTransport();
    }

    @Override
    public void addProperties(Properties props) {
        final MobilePlatform android = getPlatform();
        if (android.isReady()) {
            if (android.getPrefferedTarget() != null) {
                props.put("android.build.target", android.getPrefferedTarget().getName());//NOI18N
            }
            props.put("android.sdk.home", android.getSdkLocation());//NOI18N
            props.put("android.target.device.arg", isEmulator() ? "emulate" : "run");//NOI18N
        } else {
            Logger.getLogger(AndroidDevice.class.getName()).fine("Android not configured.");
        }
    }

    @Override
    public ActionProvider getActionProvider(Project p) {
        return new AndroidActionProvider(p);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer(Project project, PropertyProvider aThis) {
        return new AndroidConfigurationPanel.AndroidConfigurationCustomizer(project, aThis);
    }

    private String getPrefferedBrowser() {
        return browser.getCommand();
    }

    @Override
    public boolean isWebViewDebugSupported() {
        return AndroidPlatform.getDefault().isWebViewDebugSupported(isEmulator());
    }
}
