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
    
    private static final String CONFIG_FILE = "tck.properties";
    
    private String  versioningSystemClassName;
    private File    rootDir;
    private VersioningSystem testedSystem;

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
    public boolean canRun() {
        return super.canRun() && getConfigFile().exists();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File propertiesFile = getConfigFile();
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        props.load(fis);
        versioningSystemClassName = props.getProperty("test.vcs");
        rootDir = new File(props.getProperty("test.root"));

        testedSystem = VersioningManager.getInstance().getOwner(VCSFileProxy.createFileProxy(rootDir));
        assertNotNull(testedSystem);
        assertEquals(versioningSystemClassName, testedSystem.getClass().getName());
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
        VersioningSystem vs;
        VCSFileProxy rootProxy = VCSFileProxy.createFileProxy(rootDir);
        vs = VersioningManager.getInstance().getOwner(rootProxy.getParentFile());
        assertNull(vs);

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
    
    private File getConfigFile() {
        return new File(getDataDir(), CONFIG_FILE);
    }
}
