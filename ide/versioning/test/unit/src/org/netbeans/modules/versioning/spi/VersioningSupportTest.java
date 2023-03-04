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
