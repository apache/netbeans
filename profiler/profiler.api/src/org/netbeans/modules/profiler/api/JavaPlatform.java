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
package org.netbeans.modules.profiler.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.Platform;
import org.netbeans.lib.profiler.utils.MiscUtils;
import org.netbeans.modules.profiler.spi.JavaPlatformManagerProvider;
import org.netbeans.modules.profiler.spi.JavaPlatformProvider;
import org.openide.util.Lookup;

/**
 * JavaPlatform describes a java platform in a way that the profiler tools may utilize. It may serve as
 * description of the platform a profiler targets, or it may provide access to tools from the
 * particular SDK installation. It also provides information about individual platforms, for example
 * the Java platform version implemented, vendor name or implementation version.
 *
 * @author Tomas Hurka
 */

public final class JavaPlatform {
    
    private final JavaPlatformProvider provider;
    
    /**
     * finds platform with specified platform id.
     * @param platformId unique id of the platform
     * @return platform which has plarformId as unique id
     * or <code>null</code> if the is no such platform
     */
    public static JavaPlatform getJavaPlatformById(String platformId) {
        if (platformId != null) {
            List<JavaPlatform> platforms = getPlatforms();

            for (JavaPlatform platform : platforms) {
                if (platformId.equals(platform.getPlatformId())) {
                    return platform;
                }
            }
        }
        return null;
    }
    
    /** Gets an list of JavaPlatfrom objects suitable for profiling.
     * @return the array of java platform definitions.
     */
    public static List<JavaPlatform> getPlatforms() {
        List<JavaPlatformProvider> platformProviders = provider().getPlatforms();
        List<JavaPlatform> platforms = new ArrayList<>(platformProviders.size());
        
        for (JavaPlatformProvider p : platformProviders) {
            if (p.getPlatformJavaFile() != null &&
                MiscUtils.isSupportedJVM(p.getSystemProperties())) {
                platforms.add(new JavaPlatform(p));
            }
        }
        return Collections.unmodifiableList(platforms);
    }

    /**
     * Get the "default platform", meaning the JDK on which profiler itself is running.
     * @return the default platform, if it can be found, or null
     */
    public static JavaPlatform getDefaultPlatform() {
        return new JavaPlatform(provider().getDefaultPlatform());        
    }
    
    /**
     * Shows java platforms customizer
     */
    public static void showCustomizer() {
        provider().showCustomizer();                
    }
    
    private static JavaPlatformManagerProvider provider() {
        return Lookup.getDefault().lookup(JavaPlatformManagerProvider.class);
    }
    
    JavaPlatform(JavaPlatformProvider p) {
        provider = p;
    }
    
    /**
     * @return  a descriptive, human-readable name of the platform
     */
    public String getDisplayName() {
        return provider.getDisplayName();
    }

    /**
     * @return  a unique name of the platform
     */
    public String getPlatformId() {
        return provider.getPlatformId();
    }
    
    /**
     * Returns the minor version of the Java SDK
     * @return String
     */
    public int getPlatformJDKMinor() {
        return Platform.getJDKMinorNumber(getVersion());
    }
    
    /** Gets a version for JavaPlatform.
     *
     * @return Java version string
     * @see CommonConstants.JDK_15_STRING
     * @see CommonConstants.JDK_16_STRING
     * @see CommonConstants.JDK_17_STRING
     * @see CommonConstants.JDK_18_STRING
     * @see CommonConstants.JDK_19_STRING
     */
    public String getPlatformJDKVersion() {
        String ver = getVersion();

        if (ver == null) {
            return null;
        }

        if (ver.startsWith("1.5")) {
            return CommonConstants.JDK_15_STRING; // NOI18N
        } else if (ver.startsWith("1.6")) {
            return CommonConstants.JDK_16_STRING; // NOI18N
        } else if (ver.startsWith("1.7")) {
            return CommonConstants.JDK_17_STRING; // NOI18N
        } else if (ver.startsWith("1.8")) {
            return CommonConstants.JDK_18_STRING; // NOI18N
        } else if (ver.startsWith("1.9")) {
            return CommonConstants.JDK_19_STRING; // NOI18N
        } else if (ver.startsWith("9")) {
            return CommonConstants.JDK_19_STRING; // NOI18N
        } else {
            try {
                if (Integer.parseInt(ver.replaceAll("[.\\-+].*", "")) >= 10) {
                    return CommonConstants.JDK_110_BEYOND_STRING;
                }
            } catch (NumberFormatException ex) {
                //ignore
            }
            return null;
        }
    }

    /** Gets a path to java executable for specified platform. The platform passed cannot be null.
     * Errors when obtaining the java executable will be reported to the user and null will be returned.
     *
     * @param platform A JavaPlatform for which we need the java executable path
     * @return A path to java executable or null if not found
     */
    public String getPlatformJavaFile() {
        return provider.getPlatformJavaFile();
    }
    
    /** Gets the java platform system properties.
     * @return the java platform system properties
     */
    public Map<String,String> getSystemProperties() {
        return provider.getSystemProperties();
    }
    
    /** Gets the java platform properties.
     * @return the java platform system properties
     */
    public Map<String,String> getProperties() {
        return provider.getProperties();
    }

    /** Gets the java platform architecture.
     * @return the java platform architecture - 32 or 64
     */
    public int getPlatformArchitecture() {
        String arch = getSystemProperties().get("sun.arch.data.model"); // NOI18N

        if (arch == null) {
            return 32;
        }
        return Integer.parseInt(arch);
    }
    
    /**
     * Returns the version of the Java SDK
     * @return String
     */
    public String getVersion() {
        return getSystemProperties().get("java.version");
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaPlatform) {
            return getPlatformId().equals(((JavaPlatform)obj).getPlatformId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPlatformId().hashCode();
    }
    
}
