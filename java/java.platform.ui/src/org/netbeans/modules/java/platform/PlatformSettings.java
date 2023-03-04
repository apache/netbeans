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
package org.netbeans.modules.java.platform;

import java.io.File;
import java.io.FilenameFilter;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;



/**
 *
 * @author Tomas  Zezula
 */
public class PlatformSettings {
    private static final PlatformSettings INSTANCE = new PlatformSettings();
    private static final String PROP_PLATFORMS_FOLDER = "platformsFolder"; //NOI18N
    private static final String[] APPLE_JAVAVM_FRAMEWORK_PATHS = new String[] {
        "/Library/Java/JavaVirtualMachines/",                       //NOI18N            // JDK bundles provided via the Developer package, developer previews, and 3rd party JVM
        "/System/Library/Java/JavaVirtualMachines/",                //NOI18N            //The location of the Java SE runtime home
        "/System/Library/Frameworks/JavaVM.framework/Versions/",    //NOI18N            //Old location
    };

    public PlatformSettings () {

    }

    public String displayName() {
        return NbBundle.getMessage(PlatformSettings.class,"TXT_PlatformSettings");
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(PlatformSettings.class);
    }

    public File getPlatformsFolder () {
        File f = null;
        final String folderName = getPreferences().get(PROP_PLATFORMS_FOLDER, null);
        if (folderName == null) {
            if (Utilities.isMac()) {
                final FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return !name.startsWith(".");   //NOI18N
                    }
                };
                for (String path : APPLE_JAVAVM_FRAMEWORK_PATHS) {
                    final File tmp = new File (path);
                    if (tmp.canRead() && tmp.list(filter).length > 0) {
                        f = tmp;
                        break;
                    }
                }
            }
            if (f == null) {
                f = new File(System.getProperty("user.home"));  //NOI18N
                File tmp;
                while ((tmp = f.getParentFile())!=null) {
                    f = tmp;
                }
            }
        } else {
            f = new File (folderName);
        }
        return f;
    }

    public void setPlatformsFolder (File file) {
        getPreferences().put(PROP_PLATFORMS_FOLDER, file.getAbsolutePath());
    }


    public static synchronized PlatformSettings getDefault () {
        return INSTANCE;
    }
}
