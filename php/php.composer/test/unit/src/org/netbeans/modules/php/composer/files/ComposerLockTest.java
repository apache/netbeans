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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

public class ComposerLockTest extends NbTestCase {

    public ComposerLockTest(String name) {
        super(name);
    }

    public void testPackages() throws Exception {
        ComposerLock composerLock = new ComposerLock(FileUtil.toFileObject(getDataDir()), "invalid-composer.lock");
        assertTrue(composerLock.getFile().getAbsolutePath(), composerLock.exists());
        ComposerLock.ComposerPackages packages = composerLock.getPackages();
        assertEquals(28, packages.getCount());
        assertEquals(14, packages.packages.size());
        assertEquals(14, packages.packagesDev.size());
        Map<String, String> expectedPackages = new HashMap<>();
        expectedPackages.put("a/b", "1.13.1");
        expectedPackages.put("1", "1.13.1");
        expectedPackages.put("1.5", "1.13.1");
        expectedPackages.put("true", "1.13.1");
        expectedPackages.put("{myver=123}", "1.13.1");
        expectedPackages.put("[1, 2]", "1.13.1");
        expectedPackages.put("c/d", "1");
        expectedPackages.put("e/f", "1.5");
        expectedPackages.put("g/h", "null");
        expectedPackages.put("i/j", "true");
        expectedPackages.put("k/l", "{myver=123}");
        expectedPackages.put("m/n", "[1, 2]");
        expectedPackages.put("xyz/zyx", "null");
        expectedPackages.put("null", "147");
        assertEquals(expectedPackages, packages.packages);
        Map<String, String> expectedPackagesDev = new HashMap<>();
        expectedPackagesDev.put("aa/bb", "42");
        expectedPackagesDev.put("1", "1.13.1");
        expectedPackagesDev.put("1.5", "1.13.1");
        expectedPackagesDev.put("true", "1.13.1");
        expectedPackagesDev.put("{myver=123}", "1.13.1");
        expectedPackagesDev.put("[1, 2]", "1.13.1");
        expectedPackagesDev.put("cc/dd", "1");
        expectedPackagesDev.put("ee/ff", "1.5");
        expectedPackagesDev.put("gg/hh", "null");
        expectedPackagesDev.put("ii/jj", "true");
        expectedPackagesDev.put("kk/ll", "{myver=123}");
        expectedPackagesDev.put("mm/nn", "[1, 2]");
        expectedPackagesDev.put("xxyyzz/zzyyxx", "null");
        expectedPackagesDev.put("null", "741");
        assertEquals(expectedPackagesDev, packages.packagesDev);
    }

}
