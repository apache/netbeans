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
            if (browser == Browser.DEFAULT) {
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
