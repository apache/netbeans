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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Versioning SPI unit tests.
 *
 * @author Maros Sandor
 */
public class VersioningSupportTest extends NbTestCase {
    
    private File dataRootDir;

    public VersioningSupportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getWorkDir();
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
    }
    
    public void testGetPreferences() {
        Preferences prefs = VersioningSupport.getPreferences();
        assertNotNull(prefs);
        prefs.putBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, true);
        assertTrue(prefs.getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false));
        prefs.putBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        assertFalse(prefs.getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, true));
    }

    public void testGetOwner() {
        File aRoot = File.listRoots()[0];
        assertNull(VersioningSupport.getOwner(aRoot));
        aRoot = dataRootDir;
        assertNull(VersioningSupport.getOwner(aRoot));
        aRoot = new File(dataRootDir, "workdir");
        assertNull(VersioningSupport.getOwner(aRoot));
        aRoot = new File(dataRootDir, "workdir/root-test-versioned/a.txt");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
        aRoot = new File(dataRootDir, "workdir/root-test-versioned");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
        aRoot = new File(dataRootDir, "workdir/root-test-versioned/b-test-versioned");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
        aRoot = new File(dataRootDir, "workdir/root-test-versioned/nonexistent-file");
        assertTrue(VersioningSupport.getOwner(aRoot) instanceof TestVCS);
    }

    public void testFlat() {
        File aRoot = File.listRoots()[0];
        assertFalse(VersioningSupport.isFlat(aRoot));
        File file = VersioningSupport.getFlat(aRoot.getAbsolutePath());
        assertTrue(VersioningSupport.isFlat(file));
    }
}
