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
package org.netbeans.modules.netbinox;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.osgi.launch.EquinoxFactory;
import org.netbeans.core.netigso.spi.NetigsoArchive;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
@ServiceProvider(
    service=FrameworkFactory.class,
    supersedes={ "org.eclipse.osgi.launch.EquinoxFactory" },
    position=-10
)
public class NetbinoxFactory implements FrameworkFactory {
    static final Logger LOG = Logger.getLogger("org.netbeans.modules.netbinox"); // NOI18N

    @Override
    @SuppressWarnings("unchecked")
    public Framework newFramework(Map map) {
        Map<String,Object> configMap = new HashMap<String,Object>();
        configMap.putAll(map);
//        configMap.put("osgi.hook.configurators.exclude", // NOI18N
//            "org.eclipse.core.runtime.internal.adaptor.EclipseLogHook" // NOI18N
////            + ",org.eclipse.core.runtime.internal.adaptor.EclipseClassLoadingHook" // NOI18N
//        );
        configMap.put("org.osgi.framework.bundle.parent", "ext"); // NOI18N
        configMap.put("osgi.hook.configurators.include", NetbinoxHooks.class.getName()); // NOI18N
        final String userArea = toFileURL(System.getProperty("netbeans.user"));
        configMap.put("osgi.user.area.default", userArea); // NOI18N
        configMap.put("osgi.user.area", userArea); // NOI18N
        configMap.put("osgi.instance.area", userArea); // NOI18N
        configMap.put("osgi.instance.area.default", userArea); // NOI18N
        final String installArea = toFileURL(findInstallArea());
        LOG.log(Level.INFO, "Install area set to {0}", installArea); // NOI18N
        configMap.put("osgi.install.area", installArea); // NOI18N
        // some useless value
        configMap.put("osgi.framework.properties", System.getProperty("netbeans.user")); // NOI18N
        // don't change classloader when getting XMLParsers
        configMap.put("eclipse.parsers.setTCCL", "false"); // NOI18N
        configMap.put(Constants.FRAMEWORK_STORAGE, toFileURL(
            (String)map.get(Constants.FRAMEWORK_STORAGE)
        ));
        if (System.getProperty("osgi.locking") == null) { // NOI18N
            configMap.put("osgi.locking", "none"); // NOI18N
        }

        // Ensure that the org.osgi.framework.executionenvironment holds all
        // JavaSE entries that match till the current JDK. The dynamic approach
        // will work also for newly released JDKs.
        Integer javaSpecificationMajorVersion = null;
        try {
            // java >= 9
            Object runtimeVersion = Runtime.class.getMethod("version").invoke(null);
            javaSpecificationMajorVersion = (int) runtimeVersion.getClass().getMethod("major").invoke(runtimeVersion);
        } catch (ReflectiveOperationException ignore) {
            // java < 9
            LOG.log(
                    Level.FINE,
                    "Failed to invoke Runtime#version or Runtime.Version#major to determine JavaSE major version",
                    ignore
            );
        }
        if(javaSpecificationMajorVersion != null && javaSpecificationMajorVersion > 8) {
            List<String> values = new ArrayList<>();
            values.add("OSGi/Minimum-1.0");
            values.add("OSGi/Minimum-1.1");
            values.add("OSGi/Minimum-1.2");
            values.add("JavaSE/compact1-1.8");
            values.add("JavaSE/compact2-1.8");
            values.add("JavaSE/compact3-1.8");
            values.add("JRE-1.1");
            values.add("J2SE-1.2");
            values.add("J2SE-1.3");
            values.add("J2SE-1.4");
            values.add("J2SE-1.5");
            values.add("JavaSE-1.6");
            values.add("JavaSE-1.7");
            values.add("JavaSE-1.8");
            for (int i = 9; i <= javaSpecificationMajorVersion; i++) {
                values.add("JavaSE-" + i);
            }
            configMap.put(
                    "org.osgi.framework.executionenvironment",
                    values.stream().collect(Collectors.joining(", "))
            );
        }

        Object rawBundleMap = configMap.get("felix.bootdelegation.classloaders"); // NOI18N

        Map<Bundle,ClassLoader> bundleMap;
        if (rawBundleMap == null) {
            bundleMap = null;
        } else {
            bundleMap = (Map<Bundle,ClassLoader>)rawBundleMap;
        }

        NetbinoxHooks.registerMap(bundleMap);
        NetbinoxHooks.registerArchive((NetigsoArchive)configMap.get("netigso.archive")); // NOI18N

        String loc = EquinoxFactory.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
        int file = loc.indexOf("file:");
        if (file > 0) {
            loc = loc.substring(file);
        }
        int exclaim = loc.indexOf("!");
        if (exclaim > 0) {
            loc = loc.substring(0, exclaim);
        }
        configMap.put("osgi.framework", loc);
        return new Netbinox(configMap);
    }
    private static String findInstallArea() {
        String ia = System.getProperty("netbeans.home"); // NOI18N
        LOG.log(Level.FINE, "Home is {0}", ia);
        String rest = System.getProperty("netbeans.dirs"); // NOI18N
        if (rest != null) {
            for (String c : rest.split(File.pathSeparator)) {
                File cf = new File(c);
                if (!cf.isAbsolute() || !cf.exists()) {
                    LOG.log(Level.FINE, "Skipping non-existent {0}", c);
                    continue;
                }
                int prefix = findCommonPrefix(ia, c);
                if (prefix == ia.length()) {
                    LOG.log(Level.FINE, "No change to prefix by {0}", c);
                    continue;
                }
                if (prefix <= 3) {
                    LOG.log(Level.WARNING, "Cannot compute install area. No common prefix between {0} and {1}", new Object[]{ia, c});
                } else {
                    LOG.log(Level.FINE, "Prefix shortened by {0} to {1} chars", new Object[]{c, prefix});
                    ia = ia.substring(0, prefix);
                    LOG.log(Level.FINE, "New prefix {0}", ia);
                }
            }
        } else {
            LOG.fine("No dirs");
        }
        return ia;
    }

    static int findCommonPrefix(String s1, String s2) {
        if (Utilities.isWindows() || Utilities.isMac()) {
            s1 = s1.toUpperCase();
            s2 = s2.toUpperCase();
        }
        int len = Math.min(s1.length(), s2.length());
        int max = 0;
        for (int i = 0; i < len; i++) {
            final char ch = s1.charAt(i);
            if (ch != s2.charAt(i)) {
                return max;
            }
            if (ch == '/' || ch == File.separatorChar) {
                max = i + 1;
            }
        }
        return len;
    }
    
    private static String toFileURL(String file) {
        if (file == null) {
            return null;
        }
        if (file.startsWith("file:")) { // NOI18N
            return file;
        }
        if (file.startsWith("/")) { // NOI18N
            return "file:" + file; // NOI18N
        } else {
            return "file:/" + file.replace(File.separatorChar, '/'); // NOI18N
        }
    }
}
