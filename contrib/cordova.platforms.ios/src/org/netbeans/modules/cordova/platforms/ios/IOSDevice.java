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
package org.netbeans.modules.cordova.platforms.ios;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.spi.PropertyProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.spi.project.ActionProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public enum IOSDevice implements Device {
    
    IPHONE("iPhone", "--family iphone", true), //NOI18N
    IPHONE_RETINA("iPhone (Retina)", "--family iphone --retina", true), //NOI18N
    IPAD("iPad", "--family ipad", true), //NOI18N
    IPAD_RETINA("iPad (Retina)", "--family ipad --retina", true), //NOI18N
    CONNECTED("Connected Device", "", false); // NOI18N
    
    String displayName;
    String args;
    private boolean simulator;
    private static boolean ios_sim_bug;
    
    private Logger LOG = Logger.getLogger(IOSDevice.class.getName());

    IOSDevice(String name, String args, boolean simulator) {
        this.displayName = name;
        this.args = args;
        this.simulator = simulator;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getArgs() {
        return args;
    }

    @Override
    public boolean isEmulator() {
        return simulator;
    }

    @Override
    public MobilePlatform getPlatform() {
        return PlatformManager.getPlatform(PlatformManager.IOS_TYPE);
    }

    @Override
    public void addProperties(Properties props) {
        props.put("ios.sim.exec", getPlatform().getSimulatorPath());//NOI18N
        props.put("ios.device.args", getArgs());//NOI18N
    }

    @Override
    public ActionProvider getActionProvider(Project p) {
        return new IOSActionProvider(p);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer(Project project, PropertyProvider aThis) {
        return new IOSConfigurationPanel.IOSConfigurationCustomizer(project, aThis);
    }
    
    @Override
    public void openUrl(final String url) {
        if (!simulator) {
            //do nothing for device. Don't know how to open Safari on device.
            return;
        }
        try {
            if (ios_sim_bug) {
                return;
            }
            try {
                ProcessUtilities.callProcess("killall", true, IOSPlatform.DEFAULT_TIMEOUT, "MobileSafari"); // NOI18N
            } catch (IOException ex) {
            }
            String simctlList = ProcessUtilities.callProcess("xcrun", true, IOSPlatform.DEFAULT_TIMEOUT, "simctl", "list"); //NOI18N
            if (!simctlList.contains("Booted")) { //NOI18N
                // boot the simulator and wait until it is ready
                String sim = InstalledFileLocator.getDefault().locate("bin/ios-sim", "org.netbeans.modules.cordova.platforms.ios", false).getPath(); // NOI18N
                ProcessUtilities.callProcess(sim, true, IOSPlatform.DEFAULT_TIMEOUT, "start", "--exit"); //NOI18N
                Thread.sleep(10000); // try to wait for the simulator before loading the URL
            }
            String retVal = ProcessUtilities.callProcess("xcrun", true, IOSPlatform.DEFAULT_TIMEOUT, "simctl", "openurl", "booted", url); //NOI18N
            LOG.finest(retVal);
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalStateException ex) {
            //MobileSafari failed to load
            ios_sim_bug = true;
        }
    }

    private String getIPhoneSimName() {
        return getPlatform().getPrefferedTarget().getIdentifier().replace("p","P").replace("s", "S"); // NOI18N
    }

    @Override
    public MobileDebugTransport getDebugTransport() {
        if (simulator) {
            return new SimulatorDebugTransport();
        } else {
            return new DeviceDebugTransport();
        }
    }

    @Override
    public boolean isWebViewDebugSupported() {
        return true;
    }
}
