/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.netigso;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.MockModuleInstaller;
import org.netbeans.MockEvents;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.RandomlyFails;

/**
 * Basic tests to verify the basic interaction between NetBeans module
 * system and OSGi.
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // assert framework != null or OverlappingFileLockException in BundleCache.<init>
public class NetigsoTest extends NetigsoHid {

    public NetigsoTest(String name) {
        super(name);
    }

    public void testFactoryCreatesOurModules() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2;
        Module m1;
        HashSet<Module> both = null;
        try {
            String mf = "Bundle-SymbolicName: org.foo;singleton:=true\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo";
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Name: Depends on bar test module\n" +
                "OpenIDE-Module-Module-Dependencies: org.foo\n" +
                "some";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mf);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m1 = mgr.create(j1, null, false, false, false);
            m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;

            
            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");
            Class<?> sprclass = m2.getClassLoader().loadClass("org.foo.Something");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());

        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }

    }

    public void testDashnames() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2;
        Module m1;
        HashSet<Module> both = null;
        try {
            String mf = "Bundle-SymbolicName: org.foo-bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo";
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Name: Depends on bar test module\n" +
                "OpenIDE-Module-Module-Dependencies: org.foo_bar\n" +
                "some";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mf);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m1 = mgr.create(j1, null, false, false, false);
            m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;


            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");
            Class<?> sprclass = m2.getClassLoader().loadClass("org.foo.Something");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());

        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }

    }

    public void testFactoryCreatesOurModulesWithDeps() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> both = null;
        try {
            String mf = "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo";
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Name: Depends on bar test module\n" +
                "OpenIDE-Module-Module-Dependencies: org.foo > 1.0\n" +
                "some";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mf);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            Module m1 = mgr.create(j1, null, false, false, false);
            Module m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;

            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");
            Class<?> sprclass = m2.getClassLoader().loadClass("org.foo.Something");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());
        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }


    public void testLongDepsAreShortened() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2 = null;
        try {
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Specification-Version: 2.3.0.42.2\n" +
                "OpenIDE-Module-Name: Too many dots in version\n" +
                "OpenIDE-Module-Public-Packages: org.bar.*\n" +
                "some";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
        } finally {
            if (m2 != null) {
                mgr.disable(m2);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testNonNumericVersionNumberIsOK() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2 = null;
        try {
            String mfBar =
                "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 2.3.0.Prelude-rel24\n" +
                "Export-Packages: org.bar.*\n" +
                "some";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
            assertEquals("2.3.0", m2.getSpecificationVersion().toString());
            assertEquals("2.3.0.Prelude-rel24", m2.getImplementationVersion());
        } finally {
            if (m2 != null) {
                mgr.disable(m2);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
}
