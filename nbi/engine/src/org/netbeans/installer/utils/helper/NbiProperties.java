/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
