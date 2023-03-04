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
package org.netbeans.modules.cordova.platforms.spi;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import org.openide.util.EditableProperties;

/**
 *
 * @author Jan Becicka
 */
public interface MobilePlatform {
    
    /**
     * Getter for collection of connected devices
     * @return
     * @throws IOException 
     */
    Collection<? extends Device> getConnectedDevices() throws IOException;
    
    /**
     * Getter for collection of virtual devices
     * @return
     * @throws IOException 
     */
    Collection<? extends Device> getVirtualDevices() throws IOException;
    
    /**
     * Getter for device according given parameters.
     * @param name
     * @param props
     * @return 
     */
    Device getDevice(String name, EditableProperties props);

    /**
     * Get available SDKs
     * @return
     * @throws IOException 
     */
    Collection<? extends SDK> getSDKs() throws IOException;
    
    /**
     * Get prefferred SDK
     * @return 
     */
    SDK getPrefferedTarget();

    /**
     * Get sdk location
     * @return 
     */
    String getSdkLocation();

    /**
     * Is platform ready
     * @return 
     */
    boolean isReady();

    /**
     * Invokes Manage Devices UI
     */
    void manageDevices();

    /**
     * Set SDK location
     * @param sdkLocation 
     */
    void setSdkLocation(String sdkLocation);

    /**
     * Wait for emulator.
     * @param timeout
     * @return 
     */
    boolean waitEmulatorReady(int timeout);
    
    
    /**
     * Returns type
     * @see PlatformManager#ANDROID_TYPE
     * @see PlatformManager#IOS_TYPE
     * @return 
     */
    String getType();
    
    /**
     * Path for simulator
     * @return 
     */
    String getSimulatorPath();
    
    String getCodeSignIdentity();
    
    String getProvisioningProfilePath();

    void setCodeSignIdentity(String identity);
    
    void setProvisioningProfilePath(String path);

    void removePropertyChangeListener(PropertyChangeListener listener);
    
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    Collection<? extends ProvisioningProfile> getProvisioningProfiles();
}
