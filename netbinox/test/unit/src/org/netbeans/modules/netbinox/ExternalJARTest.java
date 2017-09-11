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

import java.util.Enumeration;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Do we correctly call the BundleActivators?
 *
 * @author Jaroslav Tulach
 */
public class ExternalJARTest extends SetupHid {
    private static ModuleManager mgr;
    private File simpleModule;
    private File dependsOnSimple;

    public ExternalJARTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());

        data = new File(getDataDir(), "jars");
        File activate = new File(data, "activate");
        assertTrue("Directory exists", activate.isDirectory());
        
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        File activateLib = SetupHid.createTestJAR(data, jars, "activate", null);
        System.setProperty("ext.jar", activateLib.getPath());
        
        String bundleMan = "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Bundle-ClassPath: external:$ext.jar$\n" +
                "Import-Package: org.osgi.framework\n" +
                "Export-Package: org.activate\n\n\n";
        simpleModule = NetigsoHid.changeManifest(
            getWorkDir(), 
            SetupHid.createTestJAR(data, jars, "simple-module", null),
            bundleMan
        );
        String depMan = "Manifest-Version: 1.0\n" +
            "OpenIDE-Module: org.bar2/1\n" +
            "OpenIDE-Module-Module-Dependencies: org.foo\n\n\n";
        dependsOnSimple = NetigsoHid.changeManifest(
            getWorkDir(),
            SetupHid.createTestJAR(data, jars, "depends-on-simple-module", null, simpleModule),
            depMan
        );
    }

    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        
        try {
            Module m1 = mgr.create(simpleModule, null, false, true, false);
            Module m2 = mgr.create(dependsOnSimple, null, false, false, false);
            mgr.enable(m2);

            {
                Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
                assertNotNull("m1 can load class from external library of m1", main);
            }
            
            {
                Class<?> main = m2.getClassLoader().loadClass("org.activate.Main");
                assertNotNull("m2 can load class from external library of m1", main);
            }

            mgr.disable(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
}
