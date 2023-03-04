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

package org.netbeans.installer.utils.helper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Lipin
 */
public class JavaCompatibleProperties {
    private Version minVersion;
    private Version maxVersion;
    private String vendor;
    private String osName;
    private String osArch;
    
    public JavaCompatibleProperties() {        
    }
    public JavaCompatibleProperties(Version minVersion, Version maxVersion, String vendor,  String osName, String osArch) {
        setMinVersion(minVersion);
        setMaxVersion(maxVersion);
        setVendor(vendor);
        setOsName(osName);
        setOsArch(osArch);
    }
    public JavaCompatibleProperties(String minVersion, String maxVersion, String vendor,  String osName, String osArch) {        
        this(Version.getVersion(minVersion), Version.getVersion(maxVersion),vendor,osName,osArch);
    }
    public Version getMinVersion() {
        return minVersion;
    }
    
    public void setMinVersion(Version minVersion) {
        this.minVersion = minVersion;
    }
    
    public Version getMaxVersion() {
        return maxVersion;
    }
    
    public void setMaxVersion(Version maxVersion) {
        this.maxVersion = maxVersion;
    }
    
    public String getVendor() {
        return vendor;
    }
    
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    public String getOsName() {
        return osName;
    }
    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }
    public String toString() {
        String all = "";
        if(minVersion!=null) {
            all += "<min version=" + minVersion + "> ";
        }
        if(maxVersion!=null) {
            all += "<max version=" + maxVersion + "> ";
        }
        if(vendor!=null) {
            all += "<vendor=" + vendor + "> ";
        }        
        if(osArch!=null) {
            all += "<os arch=" + osArch + "> ";
        }
        if(osName!=null) {
            all += "<os name=" + osName + "> ";
        }
        return all.trim();
    }
}
