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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.junit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.openide.util.test.TestFileUtils;

public class NbModuleSuiteTurnClassPathModulesTest extends NbTestCase {

    public NbModuleSuiteTurnClassPathModulesTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testTurnClassPathModules() throws Exception {
        File randomJar = new File(getWorkDir(), "random.jar");
        TestFileUtils.writeZipFile(randomJar, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\n\n");
        File plainModule = new File(getWorkDir(), "plainModule.jar");
        TestFileUtils.writeZipFile(plainModule, "META-INF/MANIFEST.MF:OpenIDE-Module: plain.modul\n e/2\n\n");
        File pseudoModule = new File(getWorkDir(), "pseudoModule.jar");
        TestFileUtils.writeZipFile(pseudoModule, "META-INF/MANIFEST.MF:OpenIDE-Module: org.netbeans.core.startup/1\n\n");
        File mavenLibWrapper = new File(getWorkDir(), "mavenLibWrapper.jar");
        TestFileUtils.writeZipFile(mavenLibWrapper, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\nOpenIDE-Module: maven.lib.wrapper\nMaven-Class-Path: some.group:lib:1.0\n\n");
        File mavenLib = new File(getWorkDir(), "repo/some/group/lib/1.0/lib-1.0.jar");
        if (!mavenLib.getParentFile().mkdirs()) {
            throw new IOException();
        }
        TestFileUtils.writeZipFile(mavenLib, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\n\n");
        File ud = new File(getWorkDir(), "ud");
        File[] jars = {randomJar, plainModule, pseudoModule, mavenLibWrapper, mavenLib};
        StringBuilder jcp = new StringBuilder();
        List<URL> urls = new ArrayList<URL>(jars.length);
        for (File jar : jars) {
            if (jcp.length() > 0) {
                jcp.append(File.pathSeparatorChar);
            }
            jcp.append(jar);
            urls.add(jar.toURI().toURL());
        }
        System.setProperty("java.class.path", jcp.toString());
        NbModuleSuite.S.turnClassPathModules(ud, new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader().getParent()));
        File configModules = new File(ud, "config/Modules");
        assertEquals("[maven-lib-wrapper.xml, plain-module.xml]", new TreeSet<String>(Arrays.asList(configModules.list())).toString());
        assertEquals(plainModule, jarForConfig(new File(configModules, "plain-module.xml")));
        File mavenLibWrapper2 = jarForConfig(new File(configModules, "maven-lib-wrapper.xml"));
        assertFalse(mavenLibWrapper2.equals(mavenLibWrapper));
        Attributes attr;
        JarFile jf = new JarFile(mavenLibWrapper2);
        try {
            attr = jf.getManifest().getMainAttributes();
        } finally {
            jf.close();
        }
        assertEquals(mavenLibWrapper2.getAbsolutePath(), "maven.lib.wrapper", attr.getValue("OpenIDE-Module"));
        String cp = attr.getValue("Class-Path");
        assertNotNull(attr.toString(), cp);
        File mavenLib2 = new File(mavenLibWrapper2.getParentFile(), cp);
        assertEquals(mavenLib.length(), mavenLib2.length());
    }
    private static File jarForConfig(File configFile) throws IOException {
        return new File(TestFileUtils.readFile(configFile).replaceFirst("(?s).+<param name=\"jar\">([^<]+).+", "$1"));
    }

}
