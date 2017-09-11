/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.netigso;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NetigsoOSGiFragmentTest extends NetigsoHid {
    public NetigsoOSGiFragmentTest(String name) {
        super(name);
    }


    public void testOSGiFragmentDependency() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> toCleanUp = null;
        try {
            String mfFoo = ""
                    + "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo,org.bar\n" +
                "\n\n";
            
            String mfBar = "Fragment-Host: org.foo\n"
                + "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "\n\n";

            String mfDependency = "OpenIDE-Module: org.test\n"
                    + "OpenIDE-Module-Module-Dependencies: org.bar, org.foo\n" +
                "\n\n";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mfFoo);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            File j3 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfDependency);
            Module m1 = mgr.create(j1, null, false, true, false);
            Module m2 = mgr.create(j2, null, false, true, false);
            Module m3 = mgr.create(j3, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m3));
            mgr.enable(m3);
            toCleanUp = b;

            Class<?> sprclass = m3.getClassLoader().loadClass("org.foo.Something");
            Class<?> clazz = m3.getClassLoader().loadClass("org.bar.SomethingElse");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());
        } finally {
            if (toCleanUp != null) {
                mgr.disable(toCleanUp);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
