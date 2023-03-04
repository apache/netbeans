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

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.spi.ProvisioningProfile;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class IOSProvisioningProfile implements ProvisioningProfile {
    
    private String displayName;
    private String path;
    
    public IOSProvisioningProfile(String path) {
        displayName = "Error"; // NOI18N
        try {
            this.path = path;
            String xml = ProcessUtilities.callProcess("security", true, 2 * IOSPlatform.DEFAULT_TIMEOUT, "cms", "-D", "-i", path); // NOI18N
            try {
                NSObject root = PropertyListParser.parse(xml.getBytes());
                if (root instanceof NSDictionary) {
                    displayName = ((NSDictionary) root).objectForKey("Name").toString(); // NOI18N
                }
            } catch (Exception e) {
                Logger log = Logger.getLogger(IOSProvisioningProfile.class.getName());
                log.log(Level.INFO, "Failed to parse:\n{0}", xml);
                displayName = "Profile"; //NOI18N
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IOSProvisioningProfile other = (IOSProvisioningProfile) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return true;
    }
    
    
}
