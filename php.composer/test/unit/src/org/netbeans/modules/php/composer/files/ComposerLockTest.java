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
