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
package org.netbeans.modules.cordova.platforms.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.spi.PropertyProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class AVD implements Device {

    private String name;
    private Map<String, String> props;

    private AVD() {
        this.props = new HashMap<>();
    }
    
    public static Collection<Device> parse(String output) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(output));
        
        Pattern pattern = Pattern.compile(" *([\\w]*): (.*)"); //NOI18N
        
        ArrayList<Device> result = new ArrayList<Device>();
        //ignore first line
        String line = r.readLine();
        
        line = r.readLine();
        
        AVD current = new AVD();
        String lastProp = null;
        while (line != null) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                if ("Name".equals(m.group(1))) { //NOI18N
                    current.name = m.group(2);
                } else {
                    current.props.put(m.group(1), m.group(2));
                    lastProp = m.group(1);
                }
            } else {
                if (line.contains("---------")) { //NOI18N
                    result.add(current);
                    current = new AVD();
                } else {
                    current.props.put(lastProp, current.props.get(lastProp) + line);
                }
            }
            line = r.readLine();
            if (line == null && current.name != null) {
                result.add(current);
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AVD{" + "name=" + name + ", props=" + props + '}'; //NOI18N
    }

    @Override
    public boolean isEmulator() {
        return true;
    }

    @Override
    public MobilePlatform getPlatform() {
        return AndroidPlatform.getDefault();
    }

    @Override
    public void addProperties(Properties props) {
        final MobilePlatform android = getPlatform();
        props.put("android.build.target", android.getPrefferedTarget().getName());//NOI18N
        props.put("android.sdk.home", android.getSdkLocation());//NOI18N
        props.put("android.target.device.arg", isEmulator() ? "emulate" : "run");//NOI18N
    }

    @Override
    public ActionProvider getActionProvider(Project p) {
        return new AndroidActionProvider(p);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer(Project project, PropertyProvider aThis) {
        return new AndroidConfigurationPanel.AndroidConfigurationCustomizer(project, aThis);
    }
    
    @Override
    public void openUrl(String url) {
        try {
            String s = ProcessUtilities.callProcess(
                    ((AndroidPlatform) getPlatform()).getAdbCommand(), 
                    false, 
                    AndroidPlatform.DEFAULT_TIMEOUT, 
                    isEmulator()?"-d":"-e", // NOI18N
                    "wait-for-device", // NOI18N
                    "shell", "am", "start", "-a", "android.intent.action.VIEW", // NOI18N
                    "-n", "com.android.browser/.BrowserActivity", url); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public MobileDebugTransport getDebugTransport() {
        return new AndroidDebugTransport();
    }

    @Override
    public boolean isWebViewDebugSupported() {
        return AndroidPlatform.getDefault().isWebViewDebugSupported(isEmulator());
    }

}
