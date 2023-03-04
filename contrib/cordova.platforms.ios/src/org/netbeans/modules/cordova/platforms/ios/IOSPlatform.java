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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.spi.ProvisioningProfile;
import org.netbeans.modules.cordova.platforms.spi.SDK;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 * 
 */
@ServiceProvider(service=MobilePlatform.class)
public class IOSPlatform implements MobilePlatform {
    
    private static String IOS_SIGN_IDENTITY_PREF = "ios.sign.identity"; //NOI18N
    private static String IOS_PROVISIONING_PROFILE_PREF = "ios.provisioning.profile"; //NOI18N
    public static final int DEFAULT_TIMEOUT = 30000;
    
    private boolean isReady = false;
    
    private final transient java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private String sdkLocation;
    private SDK DEFAULT = new IOSSDK("Simulator - iOS 6.0", "iphonesimulator6.0"); // NOI18N
    
    public String getType() {
        return PlatformManager.IOS_TYPE;
    }
    
    public IOSPlatform() {
    }
    
    @Override
    public Collection<SDK> getSDKs()  {
        try {
            String listSdks = ProcessUtilities.callProcess("xcodebuild", true, 60*1000, "-showsdks"); //NOI18N
            return IOSSDK.parse(listSdks);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }
    
    @Override
    public synchronized boolean isReady() {
        if (!isReady) {
            try {
                String path = ProcessUtilities.callProcess("xcode-select", true, DEFAULT_TIMEOUT, "--print-path");
                if (!"/Applications/Xcode.app/Contents/Developer".equals(path.trim())) {
                    return false;
                }
                File f = new File("/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform"); // NOI18N
                if (!f.exists()) {
                    return false;
                }
                String version = ProcessUtilities.callProcess("xcodebuild", true, DEFAULT_TIMEOUT, "-version");
                String[] lines = version.split(System.getProperty("line.separator"));
                if (lines.length<1) {
                    return false;
                }
                if (!lines[0].startsWith("Xcode")) {
                    return false;
                }
            } catch (IOException ex) {
                return false;
            }
            isReady = true;
        }
        return isReady;
    }
    
    @Override
    public String getSimulatorPath() {
        return InstalledFileLocator.getDefault().locate("bin/ios-sim", "org.netbeans.modules.cordova.platforms.ios", false).getPath(); // NOI18N
    }

    @Override
    public Collection<? extends Device> getConnectedDevices() throws IOException {
        final WebInspectorJNIBinding inspector = WebInspectorJNIBinding.getDefault();
        if (inspector.isDeviceConnected()) {
            return Collections.singleton(IOSDevice.CONNECTED);
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public SDK getPrefferedTarget() {
        String preffered = System.getProperty("ios.preffered.sdk");
        if (preffered != null ) {
            return new IOSSDK(preffered, preffered);
        }
        if (Utilities.isMac()) {
            final Collection<SDK> sDKs = getSDKs();
            if (!sDKs.isEmpty()) {
                return (SDK) sDKs.toArray()[sDKs.size()-1];
            }
        }
        return DEFAULT;
    }

    @Override
    public String getSdkLocation() {
        return sdkLocation;
    }


    @Override
    public void manageDevices() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( listener );
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( listener );
    }
    
 
    @Override
    public void setSdkLocation(String sdkLocation) {
    }

    @Override
    public boolean waitEmulatorReady(int timeout) {
        return true;
    }

    @Override
    public Device getDevice(String name, EditableProperties props) {
        Parameters.notNull("name", name);
        Parameters.notNull("props", props);
        if (Device.DEVICE.equals(props.getProperty(Device.DEVICE_PROP))) {
            return IOSDevice.CONNECTED;
        } else {
            return IOSDevice.IPHONE;
        }
    }

    @Override
    public Collection<? extends Device> getVirtualDevices() throws IOException {
        return EnumSet.allOf(IOSDevice.class);
    }

    @Override
    public String getCodeSignIdentity() {
        return NbPreferences.forModule(IOSPlatform.class).get(IOS_SIGN_IDENTITY_PREF, "iPhone Developer"); // NOI18N
    }

    @Override
    public String getProvisioningProfilePath() {
        String def = null;
        File f = new File(System.getProperty("user.home") + "/Library/MobileDevice/Provisioning Profiles/");
        if (f.exists() && f.isDirectory()) {
            File[] listFiles = f.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".mobileprovision"); // NOI18N
                }
            });
            if (listFiles.length > 0) {
                def = listFiles[0].getAbsolutePath();
            }
        }
        return NbPreferences.forModule(IOSPlatform.class).get(IOS_PROVISIONING_PROFILE_PREF, def);
    }

    @Override
    public void setCodeSignIdentity(String identity) {
        NbPreferences.forModule(IOSPlatform.class).put(IOS_SIGN_IDENTITY_PREF, identity);
        propertyChangeSupport.firePropertyChange("SIGN_IDENTITY", null, identity);//NOI18N
    }

    @Override
    public void setProvisioningProfilePath(String path) {
        NbPreferences.forModule(IOSPlatform.class).put(IOS_PROVISIONING_PROFILE_PREF, path);
        propertyChangeSupport.firePropertyChange("PROVISIONING_PROFILE", null, path);//NOI18N
    }

    @Override
    public Collection<? extends ProvisioningProfile> getProvisioningProfiles() {
        ArrayList result = new ArrayList();
        File f = new File(System.getProperty("user.home") + "/Library/MobileDevice/Provisioning Profiles/");
        if (f.exists() && f.isDirectory()) {
            File[] listFiles = f.listFiles(new FilenameFilter() {

                                   @Override
                                   public boolean accept(File dir, String name) {
                                       return name.endsWith(".mobileprovision"); // NOI18N
                                   }
                               });
            for (File prov: listFiles) {
                result.add(new IOSProvisioningProfile(prov.getAbsolutePath()));
            }
        }
        return result;
    }
}

