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
package org.netbeans.libs.jna;

import java.io.File;
import java.lang.System.Logger.Level;
import java.util.Locale;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.*;

public class Installer extends ModuleInstall {

    private static final System.Logger LOG = System.getLogger(Installer.class.getName());

    private static final String JNIDISPATCHNB = "jnidispatch-nb";
    private static final String JNA_CND = "org.netbeans.libs.jna";

    @Override
    public void validate() {
        super.validate();

        //#211655
        System.setProperty("jna.boot.library.name", JNIDISPATCHNB); //NOI18N
        System.setProperty( "jna.nosys", "false" ); //NOI18N
        InstalledFileLocator ifl = InstalledFileLocator.getDefault();
        String arch = System.getProperty("os.arch"); // NOI18N
        String system = System.getProperty("os.name").toLowerCase(Locale.ENGLISH); // NOI18N
        String mapped = System.mapLibraryName(JNIDISPATCHNB);
        String jniMapped = null;

        // See org.netbeans.StandardModule.OneModuleClassLoader#findLibrary in
        // core.startup.base for the base idea of the logic.
        File lib = null;

        if(lib == null) {
            lib = ifl.locate("modules/lib/" + mapped, JNA_CND, false); // NOI18N
        }

        if(lib == null) {
            lib = ifl.locate("modules/lib/" + arch + "/" + mapped, JNA_CND, false); // NOI18N
        }

        if(lib == null) {
            lib = ifl.locate("modules/lib/" + arch + "/" + system + "/" + mapped, JNA_CND, false); // NOI18N
        }

        if (lib == null) {
            if (BaseUtilities.isMac()) {
                jniMapped = mapped.replaceFirst("\\.dylib$", ".jnilib");
                if (lib != null) {
                    lib = ifl.locate("modules/lib/" + jniMapped, JNA_CND, false); // NOI18N
                }

                if (lib != null) {
                    lib = ifl.locate("modules/lib/" + arch + "/" + jniMapped, JNA_CND, false); // NOI18N
                }

                if (lib != null) {
                    lib = ifl.locate("modules/lib/" + arch + "/" + system + "/" + jniMapped, JNA_CND, false); // NOI18N
                }
            }
        }

        if(lib != null) {
            LOG.log(Level.DEBUG, "Found JNA library for OS in path: {0}", lib);
            System.setProperty("jna.boot.library.path", lib.getAbsoluteFile().getParent()); //NOI18N
        } else {
            LOG.log(Level.WARNING, String.format(
                    "Failed to find location for JNA library (arch: %s, system: %s, mapped: %s, jniMapped: %s)",
                    arch, system, mapped, jniMapped)
            );
        }
    }
}
