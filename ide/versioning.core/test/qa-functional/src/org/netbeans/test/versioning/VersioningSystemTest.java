/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.test.versioning;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.textui.TestRunner;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test Compatibility Kit xtest class.
 * 
 * @author Maros Sandor
 */
public class VersioningSystemTest extends JellyTestCase {

    private static final Logger LOG = Logger.getLogger(VersioningSystemTest.class.getName());
    
    private String  versioningSystemClassName;
    private File    rootDir;
    private VersioningSystem testedSystem;
    
    private boolean defaultTest;

    public VersioningSystemTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
                .addTest(VersioningSystemTest.class, 
                        "testOwnership",
                        "testInterceptor")
                .enableModules(".*")
                .clusters(".*")
                .suite();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            File propertiesFile = new File(getDataDir(), "tck.properties");
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propertiesFile);
            props.load(fis);
            versioningSystemClassName = props.getProperty("test.vcs");
            rootDir = new File(props.getProperty("test.root"));
            defaultTest = false;
        } catch (FileNotFoundException fileNotFoundException) {
            LOG.warning(String.format("Using default root folder and default vcs because; %s", fileNotFoundException.getMessage()));
            versioningSystemClassName = "org.netbeans.modules.versioning.DelegatingVCS";
            rootDir = getWorkDir();
            defaultTest = true;
        } finally {
            testedSystem = VersioningManager.getInstance().getOwner(VCSFileProxy.createFileProxy(rootDir));
        }
        assertNotNull(testedSystem);
        assertEquals(testedSystem.getClass().getName(), versioningSystemClassName);
    }

    public void testInterceptor() throws IOException {
        File newFile = new File(rootDir, "vcs-tck-created.txt");
        assertFalse(newFile.exists());
        FileObject fo = FileUtil.toFileObject(rootDir);

        // test creation
        FileObject newfo = fo.createData("vcs-tck-created.txt");
        
        sleep(1000);

        // test delete
        newfo.delete();
    }
    
    public void testOwnership() throws IOException {
        VCSFileProxy rootProxy = VCSFileProxy.createFileProxy(rootDir);
        VersioningSystem vs = VersioningManager.getInstance().getOwner(rootProxy.getParentFile());
        
        if (isDefaultTest()) {
            assertEquals(testedSystem, vs);
        } else {
            assertNull(vs);
        }

        testOwnershipRecursively(rootProxy);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersioningSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void testOwnershipRecursively(VCSFileProxy dir) {
        VersioningSystem vs = VersioningManager.getInstance().getOwner(dir);
        assertEquals(testedSystem, vs);
        VCSFileProxy [] children = dir.listFiles();
        if (children == null) return;
        for (VCSFileProxy child : children) {
            testOwnershipRecursively(child);
        }
    }

    private boolean isDefaultTest() {
        return defaultTest;
    }
    
}
