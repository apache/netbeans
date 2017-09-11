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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.api;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestUtil;
import org.openide.util.test.TestFileUtils;

/**
 * Test functionality of ManifestManager.
 *
 * @author Martin Krauskopf
 */
public class ManifestManagerTest extends NbTestCase {

    // XXX test also implementation version

    public ManifestManagerTest(String name) {
        super(name);
    }

    /* XXX
    private File suite1, suite2;

    protected void setUp() throws Exception {
        super.setUp();
        suite1 = resolveEEPFile("suite1");
        suite2 = resolveEEPFile("suite2");
    }

    public void testDirectManifestFile() throws Exception {
        File basedir = new File(suite2, "misc-project");
        ManifestManager mm = ManifestManager.getInstance(new File(basedir, "manifest.mf"), false);
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.misc", mm.getCodeNameBase());
        assertEquals("right release version", "1", mm.getReleaseVersion());
        assertEquals("right specification version", "1.0", mm.getSpecificationVersion());
        assertEquals("right localizing bundle", "org/netbeans/examples/modules/misc/Bundle.properties",
                mm.getLocalizingBundle());
        
        basedir = new File(suite1, "action-project");
        mm = ManifestManager.getInstance(new File(basedir, "manifest.mf"), false);
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.action", mm.getCodeNameBase());
        assertNull("no release version", mm.getReleaseVersion());
    }
    */
    
    public void testFriends() throws Exception {
        File manifest = new File(getWorkDir(), "testManifest.mf");
        String mfContent = "Manifest-Version: 1.0\n" +
                "Ant-Version: Apache Ant 1.6.5\n" +
                "Created-By: 1.4.2_10-b03 (Sun Microsystems Inc.)\n" +
                "OpenIDE-Module-Public-Packages: org.netbeans.modules.editor.hints.spi.*\n" +
                "OpenIDE-Module-Friends: org.netbeans.modules.java.hints, org.netbeans.\n" +
                " modules.j2ee.ejbcore, org.netbeans.modules.kjava.editor\n" +
                "OpenIDE-Module-Module-Dependencies: org.openide.filesystems > 6.2, org\n" +
                " .openide.util > 6.2, org.openide.modules > 6.2, org.openide.nodes > 6\n" +
                " .2, org.openide.awt > 6.2, org.openide.text > 6.2, org.openide.loader\n" +
                " s, org.netbeans.modules.editor.lib/1, org.netbeans.modules.editor.mim\n" +
                " elookup/1\n" +
                "OpenIDE-Module-Build-Version: 060123\n" +
                "OpenIDE-Module-Specification-Version: 1.10.0.1\n" +
                "OpenIDE-Module: org.netbeans.modules.editor.hints/1\n" +
                "OpenIDE-Module-Implementation-Version: 1\n" +
                "OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/editor/hints/re\n" +
                " sources/Bundle.properties\n" +
                "OpenIDE-Module-Install: org/netbeans/modules/editor/hints/HintsModule.\n" +
                " class\n" +
                "OpenIDE-Module-Requires: org.openide.modules.ModuleFormat1\n";
        TestFileUtils.writeFile(manifest, mfContent);
        ManifestManager mm = ManifestManager.getInstance(manifest, true);
        assertEquals("one public package", 1, mm.getPublicPackages().length);
    }
    
    public void testJarWithNoManifest() throws Exception {
        // See #87064.
        File jar = new File(getWorkDir(), "test.jar");
        TestUtil.createJar(jar, Collections.singletonMap("foo", "bar"), null);
        ManifestManager.getInstanceFromJAR(jar);
    }

    public void testGetJarWithGeneratedManifest() throws Exception {
        clearWorkDir();
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("OpenIDE-Module", "platform.module");
        mf.getMainAttributes().putValue("OpenIDE-Module-Layer", "platform/module/layer.xml");
        Map<String, String> contents = new HashMap<String, String>();
        contents.put("platform/module/Bundle.properties", "folder/file=English");
        contents.put("platform/module/layer.xml", "<filesystem><folder name=\"folder\"><file name=\"file\"><attr name=\"SystemFileSystem.localizingBundle\" stringvalue=\"platform.module.Bundle\"/></file></folder></filesystem>");
        File jar = new File(getWorkDir(), "test.jar");
        TestUtil.createJar(jar, contents, mf);
        ManifestManager mm = ManifestManager.getInstanceFromJAR(jar, true);
        assertNull(mm.getGeneratedLayer());
        assertEquals("One", 1, mm.getProvidedTokens().length);
        assertEquals("cnb.platform.module", mm.getProvidedTokens()[0]);

        contents.put("META-INF/generated-layer.xml", "</filesystem>");
        jar = new File(getWorkDir(), "test2.jar");
        TestUtil.createJar(jar, contents, mf);
        mm = ManifestManager.getInstanceFromJAR(jar, true);
        assertEquals("META-INF/generated-layer.xml", mm.getGeneratedLayer());
    }
}
