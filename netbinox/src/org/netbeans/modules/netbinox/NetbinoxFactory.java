/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Oracle, Inc.
 */
package org.netbeans.modules.netbinox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
