/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
