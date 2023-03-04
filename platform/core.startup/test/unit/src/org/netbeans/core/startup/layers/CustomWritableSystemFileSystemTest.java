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
package org.netbeans.core.startup.layers;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Tests whether the system property 
 * <code>org.netbeans.core.systemfilesystem.custom</code> correctly
 * installs a custom filesystem.
 * 
 * @author David Strupl
 */
public class CustomWritableSystemFileSystemTest extends NbTestCase {

    private SystemFileSystem sfs;

    public CustomWritableSystemFileSystemTest(String testName) {
        super(testName);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        System.setProperty("org.netbeans.core.systemfilesystem.custom", PoohFileSystem.class.getName());
        sfs = (SystemFileSystem) Repository.getDefault().getDefaultFileSystem();
    }

    public void testCustomSFSWritableLayerPresent() throws Exception {
        FileSystem writable = sfs.createWritableOn(null);
        assertTrue(writable instanceof ModuleLayeredFileSystem);
        ModuleLayeredFileSystem mlf = (ModuleLayeredFileSystem) writable;
        assertTrue("Expected fs" + mlf.getWritableLayer(), mlf.getWritableLayer() instanceof PoohFileSystem);
        PoohFileSystem pooh = (PoohFileSystem)mlf.getWritableLayer();
        if (Utilities.isWindows()) {
            assertEquals("Proper value of nb user", 0, String.CASE_INSENSITIVE_ORDER.compare(getWorkDirPath(), pooh.netbeansUser));
        } else {
            assertEquals("Proper value of nb user", getWorkDirPath(), pooh.netbeansUser);
        }
    }

    public static class PoohFileSystem extends FileSystem {
        String netbeansUser;
        
        public PoohFileSystem() {
            netbeansUser = System.getProperty("netbeans.user");
        }
        
        public String getDisplayName() {
            return "Pooh";
        }

        public boolean isReadOnly() {
            return false;
        }

        public FileObject getRoot() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public FileObject findResource(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public SystemAction[] getActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
