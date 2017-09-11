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

import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.openide.filesystems.FileObject;

/**
 * How does OSGi integration deals with layer registration? Can we read
 * it without resolving the bundle?
 *
 * @author Jaroslav Tulach
 */
public class ExportedIfPresentNegTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;

    public ExportedIfPresentNegTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        // changes minimal start level to 10
        Locale.setDefault(new Locale("def", "EX"));
        clearWorkDir();

        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        File simpleModule = createTestJAR("simple-module", null);
        File dependsOnSimpleModule = createTestJAR("depends-on-simple-module", null, simpleModule);

        if (System.getProperty("netbeans.user") == null) {
            File ud = new File(getWorkDir(), "ud");
            ud.mkdirs();

            System.setProperty("netbeans.user", ud.getPath());


            ModuleSystem ms = Main.getModuleSystem();
            mgr = ms.getManager();
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                m1 = mgr.create(simpleModule, null, false, false, false);
                mgr.enable(Collections.<Module>singleton(m1));
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }

    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
    public void testCannotLoadClassFromContextClassLoaderWhenSomeExportPackageIsAvailable() throws Exception {
        FileObject fo;
        Module m2;
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            String mfBar = "Bundle-SymbolicName: org.bar2\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Import-Package: org.foo\n" +
                "Export-Package: org.foo\n" +
                "\n\n";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                Class<?> c = mgr.getClassLoader().loadClass("org.bar.SomethingElse");
                fail("The class should not have been found: " + c);
            } catch (ClassNotFoundException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().contains("SomethingElse"));
            }
        } finally {
            mgr.disable(m2);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
