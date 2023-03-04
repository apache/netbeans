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
package org.netbeans.modules.php.composer.files;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

public class ComposerJsonTest extends NbTestCase {

    public ComposerJsonTest(String name) {
        super(name);
    }

    public void testDependencies() throws Exception {
        ComposerJson composerJson = new ComposerJson(FileUtil.toFileObject(getDataDir()), "invalid-composer.json");
        assertTrue(composerJson.getFile().getAbsolutePath(), composerJson.exists());
        ComposerJson.ComposerDependencies dependencies = composerJson.getDependencies();
        assertEquals(14, dependencies.getCount());
        assertEquals(7, dependencies.dependencies.size());
        assertEquals(7, dependencies.devDependencies.size());
        Map<String, String> expectedDependencies = new HashMap<>();
        expectedDependencies.put("a/b", "1.13.1");
        expectedDependencies.put("c/d", "1");
        expectedDependencies.put("e/f", "1.5");
        expectedDependencies.put("g/h", "null");
        expectedDependencies.put("i/j", "true");
        expectedDependencies.put("k/l", "{myver=123}");
        expectedDependencies.put("m/n", "[1, 2]");
        assertEquals(expectedDependencies, dependencies.dependencies);
        Map<String, String> expectedDevDependencies = new HashMap<>();
        expectedDevDependencies.put("aa/bb", "42");
        expectedDevDependencies.put("cc/dd", "1");
        expectedDevDependencies.put("ee/ff", "1.5");
        expectedDevDependencies.put("gg/hh", "null");
        expectedDevDependencies.put("ii/jj", "true");
        expectedDevDependencies.put("kk/ll", "{myver=123}");
        expectedDevDependencies.put("mm/nn", "[1, 2]");
        assertEquals(expectedDevDependencies, dependencies.devDependencies);
    }

    public void testVendorDir() {
        ComposerJson composerJson = new ComposerJson(FileUtil.toFileObject(getDataDir()), "composer-vendor.json");
        assertTrue(composerJson.getFile().getAbsolutePath(), composerJson.exists());
        assertEquals(new File(getDataDir(), ComposerJson.DEFAULT_VENDOR_DIR), composerJson.getVendorDir());
    }

    public void testLibsDir() {
        ComposerJson composerJson = new ComposerJson(FileUtil.toFileObject(getDataDir()), "composer-libs.json");
        assertTrue(composerJson.getFile().getAbsolutePath(), composerJson.exists());
        assertEquals(new File(getDataDir(), "libs"), composerJson.getVendorDir());
    }

    public void testDefaultVendorDir() {
        ComposerJson composerJson = new ComposerJson(FileUtil.toFileObject(getDataDir()), "nonexisting-composer.json");
        assertFalse(composerJson.getFile().getAbsolutePath(), composerJson.exists());
        assertEquals(new File(getDataDir(), ComposerJson.DEFAULT_VENDOR_DIR), composerJson.getVendorDir());
    }

    public void testScripts() {
        ComposerJson composerJson = new ComposerJson(FileUtil.toFileObject(getDataDir()), "composer-scripts.json");
        assertTrue(composerJson.getFile().getAbsolutePath(), composerJson.exists());
        Set<String> scripts = composerJson.getScripts();
        assertEquals(3, scripts.size());
        Set<String> expectedScripts = new TreeSet<>();
        expectedScripts.add("ci");
        expectedScripts.add("analyze");
        expectedScripts.add("test");
        assertEquals(expectedScripts, scripts);
    }

    public void testNoScripts() {
        ComposerJson composerJson = new ComposerJson(FileUtil.toFileObject(getDataDir()), "composer-vendor.json");
        assertTrue(composerJson.getFile().getAbsolutePath(), composerJson.exists());
        Set<String> scripts = composerJson.getScripts();
        assertTrue(scripts.isEmpty());
    }

}
