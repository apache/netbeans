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
    
    private transient final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
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

