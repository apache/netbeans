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
package org.netbeans.modules.gradle.java.spi.support;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.gradle.spi.Utils;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.platform.JavaPlatformFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Support for creating retrieving JavaPlatforms from their install location 
 * (home directory).
 * 
 * @since 1.26
 * @author Laszlo Kishalmi
 */
public final class JavaToolchainSupport {

    private final Map<File, JavaPlatform> platformCache;

    private static JavaToolchainSupport instance;
    
    private JavaToolchainSupport() {
        platformCache = new HashMap<>();
    }
    
    public static JavaToolchainSupport getDefault() {
        if (instance == null) {
            instance = new JavaToolchainSupport();
        }
        return instance;
    }
    
    /**
     * Tries to locate a registered {@linkplain JavaPlatform} from its install
     * location. If it is not registered among the NetBeans usual Java Platforms
     * then a new non-registered one will be created.
     * 
     * @param home The home directory of a Java installation
     * @return the {@linkplain JavaPlatform} representing the given directory.
     */
    public JavaPlatform platformByHome(File home) {
        return platformCache.computeIfAbsent(home, this::detectPlatform);
    }
    
    private JavaPlatform detectPlatform(File home) {
        FileObject h = FileUtil.toFileObject(home);
        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (platform.isValid()) {
                FileObject ph = platform.getInstallFolders().iterator().next();
                if (ph.equals(h)) {
                    return platform;
                }
            }
        }
        for (JavaPlatformFactory.Provider pvd : Lookup.getDefault().lookupAll(JavaPlatformFactory.Provider.class)) {
            JavaPlatformFactory factory = pvd.forType(CommonProjectUtils.J2SE_PLATFORM_TYPE);
            if (factory != null) {
                try {
                    JavaPlatform ret = factory.create(h, toolchainName(home), false);
                    return ret;
                } catch (IOException ex) {
                    
                }
            }
        }
        return null;
    }
    
    private static final Pattern GRADLE_JDK_DIST = Pattern.compile("(\\w+)-(\\d+)-(\\w+)-(\\w+)");
    @NbBundle.Messages({
        "# {0} - JDK Vendor",
        "# {1} - Java Feature Version",
        "# {2} - JDK Architecture",
        "# {3} - JDK OS",
        "GRADLE_INSTALLED_JDK_NAME=Java {1} {0} (from Java Toolchain)",
        "# {0} - JDK Install folder name",
        "# {1} - JDK Install folder path",
        "OTHER_JDK_NAME=JDK {0} from {1}"
    })
    private static String toolchainName(File home) {
        File distDir = home.getParentFile();
        if (distDir != null) {
            Matcher m = GRADLE_JDK_DIST.matcher(distDir.getName());
            if (m.matches()) {
                String vendor = Utils.capitalize(m.group(1).replace('_', ' '));
                String version = m.group(2);
                String arch = m.group(3);
                String os = m.group(4);
                return Bundle.GRADLE_INSTALLED_JDK_NAME(vendor, version, arch, os);
            }
        }
        return Bundle.OTHER_JDK_NAME(home.getName(), home.getAbsolutePath());
    }
}
