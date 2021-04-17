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
package org.netbeans.modules.java.disco;

import io.foojay.api.discoclient.pkg.Architecture;
import io.foojay.api.discoclient.pkg.OperatingSystem;
import java.io.File;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openide.util.Utilities;

public class OS {

    public static OperatingSystem getOperatingSystem() {
        if (Utilities.isMac())
            return OperatingSystem.MACOS;
        if (Utilities.isWindows())
            return OperatingSystem.WINDOWS;
        if (Utilities.isUnix()) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("sunos"))
                return OperatingSystem.SOLARIS;
            else
                return OperatingSystem.LINUX;
        }
        return OperatingSystem.NONE;
    }

    public static Architecture getArchitecture() {
        Architecture arch = Architecture.fromText(System.getProperty("os.arch"));
        if (arch == Architecture.NOT_FOUND)
            return Architecture.NONE;
        return arch;
    }

    @Nullable
    public static File getDefaultDownloadFolder() {
        String home = System.getProperty("user.home");
        if (home == null)
            return null;

        switch (getOperatingSystem()) {
            case MACOS:
                // macOS: Library/Java/JavaVirtualMachines
                File library = new File(home, "Library");
                if (library.exists() && library.isDirectory()) {
                    File d = new File(new File(library, "Java"), "JavaVirtualMachines");
                    d.mkdirs();
                    return d;
                }
                break;
            case WINDOWS:
                // Windows: %LOCALAPPDATA%/Java or AppData/Local
                String local = System.getenv("LOCALAPPDATA");
                if (local != null) {
                    File appdata = new File(local);
                    if (appdata.exists()) {
                        File d = new File(appdata, "Java");
                        d.mkdirs();
                        return d;
                    }
                }
                break;
            case LINUX:
                // Linux: $XDG_DATA_HOME/jvm/ or $HOME/.local/share
                String xdg = System.getenv("XDG_DATA_HOME");
                if (xdg != null) {
                    File xdgData = new File(xdg);
                    if (xdgData.exists()) {
                        File d = new File(xdgData, "jvm");
                        d.mkdirs();
                        return d;
                    }
                }
                //fallback
                File d = new File(home, ".local/share/jvm");
                d.mkdirs();
                return d;
        }
        return null;
    }

}
