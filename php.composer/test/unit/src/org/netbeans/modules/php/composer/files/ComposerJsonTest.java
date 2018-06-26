/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.composer.files;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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

}
