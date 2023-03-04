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
