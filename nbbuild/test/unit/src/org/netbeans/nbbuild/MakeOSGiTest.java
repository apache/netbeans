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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.tools.ant.Task;
import org.netbeans.junit.NbTestCase;
import org.netbeans.nbbuild.MakeOSGi.Info;

public class MakeOSGiTest extends NbTestCase {

    public MakeOSGiTest(String n) {
        super(n);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testTranslate() throws Exception {
        assertTranslation("{Bundle-SymbolicName=m, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\n", set(), set());
        /* no longer testable:
        assertTranslation("{Bundle-SymbolicName=m}",
                "OpenIDE-Module: m\nOpenIDE-Module-Public-Packages: -\n", set(), set());
        assertTranslation("{Bundle-SymbolicName=m, Export-Package=m1, m2}",
                "OpenIDE-Module: m\nOpenIDE-Module-Public-Packages: m1.*, m2.*\n", set(), set());
        assertTranslation("{Bundle-SymbolicName=m, Export-Package=nb.help, javax.help, javax.help.basic}",
                "OpenIDE-Module: m\nOpenIDE-Module-Public-Packages: nb.help.*, javax.help.**\n", set(), set("nb.help", "javax.help", "javax.help.basic"));
        assertTranslation("{Bundle-SymbolicName=m, Bundle-Version=1.0.0.3, Export-Package=api, impl}",
                "OpenIDE-Module: m\nOpenIDE-Module-Public-Packages: api.*\n" +
                "OpenIDE-Module-Specification-Version: 1.0\nOpenIDE-Module-Implementation-Version: 3\n", set(), set("api", "impl"));
         */
        assertTranslation("{Bundle-SymbolicName=m, Import-Package=javax.swing, javax.swing.text, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\n", set("javax.swing", "javax.swing.text"), set());
        assertTranslation("{Bundle-SymbolicName=m, DynamicImport-Package=com.sun.source.tree, " +
                "Import-Package=javax.swing, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\n", set("javax.swing", "com.sun.source.tree"), set());
        assertTranslation("{Bundle-SymbolicName=m, Require-Bundle=org.netbeans.core.osgi, some.lib;bundle-version='[101.0.0,200)'}",
                "OpenIDE-Module: m\nOpenIDE-Module-Module-Dependencies: some.lib/1 > 1.0\n", set(), set());
        // XXX test hiddenPackages, that deps are not imported, etc.
        assertTranslation("{Bundle-SymbolicName=m, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\nOpenIDE-Module-Requires: org.openide.modules.ModuleFormat2\n", set(), set());
        assertTranslation("{Bundle-SymbolicName=m, OpenIDE-Module-Requires=foo, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\nOpenIDE-Module-Requires: foo, org.openide.modules.ModuleFormat1\n", set(), set());
        assertTranslation("{Bundle-SymbolicName=m, OpenIDE-Module-Requires=foo, bar, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\nOpenIDE-Module-Requires: foo, org.openide.modules.ModuleFormat1, bar\n", set(), set());
        assertTranslation("{Bundle-SymbolicName=m, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\nOpenIDE-Module-Java-Dependencies: Java > 1.5\n", set(), set());
        assertTranslation("{Bundle-RequiredExecutionEnvironment=JavaSE-1.6, Bundle-SymbolicName=m, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\nOpenIDE-Module-Java-Dependencies: Java > 1.6\n", set(), set());
        assertTranslation("{Bundle-RequiredExecutionEnvironment=JavaSE-1.7, Bundle-SymbolicName=m, Require-Bundle=org.netbeans.core.osgi}",
                "OpenIDE-Module: m\nOpenIDE-Module-Java-Dependencies: Java > 1.7\n", set(), set());
    }
    private void assertTranslation(String expectedOsgi, String netbeans, Set<String> importedPackages, Set<String> exportedPackages) throws Exception {
        assertTrue(netbeans.endsWith("\n")); // JRE bug
        Manifest nbmani = new Manifest(new ByteArrayInputStream(netbeans.getBytes()));
        Attributes nbattr = nbmani.getMainAttributes();
        Manifest osgimani = new Manifest();
        Attributes osgi = osgimani.getMainAttributes();
        Info info = new Info();
        info.importedPackages.addAll(importedPackages);
        info.exportedPackages.addAll(exportedPackages);
        new MakeOSGi().translate(nbattr, osgi, Collections.singletonMap(nbattr.getValue("OpenIDE-Module"), info));
        // boilerplate:
        assertEquals("1.0", osgi.remove(new Attributes.Name("Manifest-Version")));
        assertEquals("2", osgi.remove(new Attributes.Name("Bundle-ManifestVersion")));
        SortedMap<String,String> osgiMap = new TreeMap<String,String>();
        for (Map.Entry<Object,Object> entry : osgi.entrySet()) {
            osgiMap.put(((Attributes.Name) entry.getKey()).toString(), (String) entry.getValue());
        }
        assertEquals(expectedOsgi, osgiMap.toString().replace('"', '\''));
    }
    private static Set<String> set(String... items) {
        return new TreeSet<String>(Arrays.asList(items));
    }

    public void testTranslateDependency() throws Exception {
        assertTranslateDependency("org.openide.util;bundle-version=\"[8.0.0,100)\"", "org.openide.util > 8.0");
        assertTranslateDependency("org.netbeans.modules.lexer;bundle-version=\"[201.4.0,300)\"", "org.netbeans.modules.lexer/2 > 1.4");
        assertTranslateDependency("what.ever;bundle-version=\"[0.0.0,100)\"", "what.ever");
        assertTranslateDependency("org.netbeans.modules.java.sourceui", "org.netbeans.modules.java.sourceui = 15");
        assertTranslateDependency("editor.indent.project;bundle-version=\"[1.0.0,200)\"", "editor.indent.project/0-1 > 1.0");
        assertTranslateDependency("", "org.netbeans.libs.osgi > 1.0");
        // XXX 3 or more items in sequence
    }
    private void assertTranslateDependency(String expected, String dependency) throws Exception {
        StringBuilder b = new StringBuilder();
        MakeOSGi.translateDependency(b, dependency);
        assertEquals(expected, b.toString());
    }

    public void testFindFragmentHost() throws Exception {
        assertEquals("org.netbeans.core.windows", MakeOSGi.findFragmentHost(new File(getWorkDir(), "modules/locale/org-netbeans-core-windows_nb.jar")));
        assertEquals("org.netbeans.core.startup", MakeOSGi.findFragmentHost(new File(getWorkDir(), "core/locale/core_nb.jar")));
        assertEquals("org.netbeans.bootstrap", MakeOSGi.findFragmentHost(new File(getWorkDir(), "lib/locale/boot_nb.jar")));
    }

    public void testPrescan() throws Exception {
        File j = new File(getWorkDir(), "x.jar");
        OutputStream os = new FileOutputStream(j);
        try {
            JarOutputStream jos = new JarOutputStream(os, new Manifest(new ByteArrayInputStream("Manifest-Version: 1.0\nBundle-SymbolicName: org.eclipse.mylyn.bugzilla.core;singleton:=true\nExport-Package: org.eclipse.mylyn.internal.bugzilla.core;x-friends:=\"org.eclipse.mylyn.bugzilla.ide,org.eclipse.mylyn.bugzilla.ui\",org.eclipse.mylyn.internal.bugzilla.core.history;x-friends:=\"org.eclipse.mylyn.bugzilla.ide,org.eclipse.mylyn.bugzilla.ui\",org.eclipse.mylyn.internal.bugzilla.core.service;x-internal:=true\n".getBytes())));
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
        Info info = new MakeOSGi.Info();
        JarFile jf = new JarFile(j);
        try {
            assertEquals("org.eclipse.mylyn.bugzilla.core", MakeOSGi.prescan(jf, info, new Task() {}));
        } finally {
            jf.close();
        }
        assertEquals("[org.eclipse.mylyn.internal.bugzilla.core, org.eclipse.mylyn.internal.bugzilla.core.history, org.eclipse.mylyn.internal.bugzilla.core.service]", info.exportedPackages.toString());
    }

}
