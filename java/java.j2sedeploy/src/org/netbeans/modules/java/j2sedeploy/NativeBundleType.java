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
package org.netbeans.modules.java.j2sedeploy;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
@NbBundle.Messages({
    "TXT_All=All Artifacts",
    "TXT_AllInstallers=All Installers",
    "TXT_Image=Image Only",
    "TXT_DEBPackage=DEB Package",
    "TXT_RPMPackage=RPM Package",
    "TXT_DMGImage=DMG Image",
    "TXT_EXEInstaller=EXE Installer",
    "TXT_MSIInstaller=MSI Installer"
})
public enum NativeBundleType {

    ALL("native-bundle-all", "all", Bundle.TXT_All(), OS.values()), // NOI18N
    ALL_INSTALLERS("native-bundle-all-installers", "installer", Bundle.TXT_AllInstallers(), OS.values()), // NOI18N
    IMAGE("native-bundle-image", "image", Bundle.TXT_Image(), OS.values()), // NOI18N
    DMG("native-bundle-dmg-installer", "dmg", Bundle.TXT_DMGImage(), OS.OSX), // NOI18N
    EXE("native-bundle-exe-installer", "exe", Bundle.TXT_EXEInstaller(), OS.WINDOWS), // NOI18N
    MSI("native-bundle-msi-installer", "msi", Bundle.TXT_MSIInstaller(), OS.WINDOWS), // NOI18N
    DEB("native-bundle-deb-installer", "deb", Bundle.TXT_DEBPackage(), OS.LINUX), // NOI18N
    RPM("native-bundle-rpm-installer", "rpm", Bundle.TXT_RPMPackage(), OS.LINUX); // NOI18N

    private enum OS {

        OSX {
            @Override
            boolean isCurrent() {
                return Utilities.isMac();
            }
        },
        LINUX {
            @Override
            boolean isCurrent() {
                return Utilities.getOperatingSystem() == Utilities.OS_LINUX;
            }
        },
        WINDOWS {
            @Override
            boolean isCurrent() {
                return Utilities.isWindows();
            }
        };

        abstract boolean isCurrent();
    }

    private static final Map<String,NativeBundleType> commandToBundleType;
    static {
        final Map<String,NativeBundleType> m = new HashMap<>();
        for (NativeBundleType nbt : values()) {
            m.put(nbt.getCommand(),nbt);
        }
        commandToBundleType = Collections.unmodifiableMap(m);
    }

    private final String command;
    private final String antPropValue;
    private final String displayName;
    private final Set<OS> systems;

    private NativeBundleType(
            @NonNull final String command,
            @NonNull final String antPropValue,
            @NonNull final String displayName,
            @NonNull final OS... systems) {
        Parameters.notNull("command", command); //NOI18N
        Parameters.notNull("antPropValue", antPropValue);   //NOI18N
        Parameters.notNull("displayName", displayName); //NOI18N
        Parameters.notNull("systems", systems); //NOI18N
        this.command = command;
        this.antPropValue = antPropValue;
        this.displayName = displayName;
        this.systems = EnumSet.<OS>noneOf(OS.class);
        Collections.addAll(this.systems, systems);
    }

    @NonNull
    public String getDisplayName() {
        return this.displayName;
    }

    @NonNull
    public String getCommand() {
        return this.command;
    }

    @NonNull
    public String getAntProperyValue() {
        return this.antPropValue;
    }

    public boolean isSupported() {
        for (OS os : systems) {
            if (os.isCurrent()) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public static Set<NativeBundleType> getSupported() {
        final Set<NativeBundleType> res = EnumSet.<NativeBundleType>noneOf(NativeBundleType.class);
        for (NativeBundleType nbt : NativeBundleType.values()) {
            if (nbt.isSupported()) {
                res.add(nbt);
            }
        }
        return Collections.unmodifiableSet(res);
    }

    @CheckForNull
    public static NativeBundleType forCommand(@NonNull final String command) {
        Parameters.notNull("command", command); //NOI18N
        return commandToBundleType.get(command);
    }

}
