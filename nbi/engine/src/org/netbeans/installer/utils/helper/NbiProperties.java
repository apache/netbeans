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

import java.util.Locale;
import java.util.Properties;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author ks152834
 */
public class NbiProperties extends Properties {
    public NbiProperties(
            ) {
        super();
    }
    
    public NbiProperties(
            final Properties properties) {
        super();
        
        putAll(properties);
    }
    
    @Override
    public String getProperty(
            final String name) {
        return getProperty(
                name,
                SystemUtils.getCurrentPlatform(),
                Locale.getDefault());
    }
    
    public String getProperty(
            final String name,
            final Platform platform,
            final Locale locale) {
        final String[] platformParts = getPlatformParts(platform);
        final String[] localeParts = getLocaleParts(locale);
        
        for (int i = platformParts.length; i >= 0; i--) {
            for (int j = localeParts.length; j >= 0; j--) {
                final String platformString =
                        StringUtils.asString(platformParts, 0, i, "-");
                final String localeString =
                        StringUtils.asString(localeParts, 0, j, "_");
                
                final String candidateName =
                        name +
                        (platformString.equals("") ? "" : "." + platformString) +
                        (localeString.equals("") ? "" : "." + localeString);
                
                final String value = super.getProperty(candidateName);
                if (value != null) {
                    return value;
                }
            }
        }
        
        return null;
    }
    
    @Override
    public Object setProperty(
            final String name,
            final String value) {
        return setProperty(
                name,
                value,
                SystemUtils.getCurrentPlatform(),
                Locale.getDefault());
    }
    
    public Object setProperty(
            final String name,
            final String value,
            final Platform platform,
            final Locale locale) {
        final String[] platformParts = getPlatformParts(platform);
        final String[] localeParts = getLocaleParts(locale);
        
        for (int i = platformParts.length; i >= 0; i--) {
            for (int j = localeParts.length; j >= 0; j--) {
                final String platformString =
                        StringUtils.asString(platformParts, 0, i, "-");
                final String localeString =
                        StringUtils.asString(localeParts, 0, j, "_");
                
                final String candidateName =
                        name +
                        (platformString.equals("") ? "" : "." + platformString) +
                        (localeString.equals("") ? "" : "." + localeString);
                
                if (super.getProperty(candidateName) != null) {                    
                    return super.setProperty(candidateName, value);
                }
            }
        }
        return super.setProperty(name, value);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private String[] getPlatformParts(
            final Platform platform) {
        if (platform == null) {
            return new String[0];
        }
        
        if (platform.getOsFamily() != null) {
            if (platform.getHardwareArch() != null) {
                if (platform.getOsVersion() != null) {
                    if (platform.getOsFlavor() != null) {
                        return new String[] {
                            platform.getOsFamily(),
                            platform.getHardwareArch(),
                            platform.getOsVersion(),
                            platform.getOsFlavor()
                        };
                    }
                    
                    return new String[] {
                        platform.getOsFamily(),
                        platform.getHardwareArch(),
                        platform.getOsVersion()
                    };
                }
                
                return new String[] {
                    platform.getOsFamily(),
                    platform.getHardwareArch()
                };
            }
            
            return new String[] {
                platform.getOsFamily()
            };
        }
        
        return new String[0];
    }
    
    private String[] getLocaleParts(
            final Locale locale) {
        if (locale == null) {
            return new String[0];
        }
        
        if (!locale.getLanguage().equals("")) {
            if (!locale.getCountry().equals("")) {
                if (!locale.getVariant().equals("")) {
                    return new String[] {
                        locale.getLanguage(),
                        locale.getCountry(),
                        locale.getVariant()
                    };
                }
                
                return new String[] {
                    locale.getLanguage(),
                    locale.getCountry()
                };
            }
            
            return new String[] {
                locale.getLanguage()
            };
        }
        
        return new String[0];
    }
}
