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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.agent.hooks.api.TrackingHooks;
import org.netbeans.agent.hooks.api.TrackingHooks.Hooks;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 */
public class NoLockWhenRefreshIOTest extends NbTestCase {
    static {
        assertFalse("No lock & preload the code", ChildrenSupport.isLock());
    }

    
    Logger LOG;
    private AssertNoLockManager hooks;
    
    public NoLockWhenRefreshIOTest(String testName) {
        super(testName);
    }
            
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        try {
            clearWorkDir();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot clear work dir for some reason", ex);
        }
        
        File dir = new File(getWorkDir(), "dir");
        dir.mkdir();
        
        for (int i = 0; i < 100; i++) {
            new File(dir, "x" + i + ".txt").createNewFile();
            new File(dir, "d" + i).mkdir();
        }
        
        assertEquals("Two hundred", 200, dir.list().length);
    }

    private void registerHooks() {
        hooks = new AssertNoLockManager(getWorkDirPath());
        TrackingHooks.register(hooks, 0, Hooks.IO);
    }

    @Override
    protected void tearDown() throws Exception {
        if (hooks != null) {
            TrackingHooks.unregister(hooks);
        }
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testRefreshOfAFolder() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject dir = fo.getFileObject("dir");
        assertNotNull("dir found", dir);
        registerHooks();
        List<FileObject> arr = Arrays.asList(dir.getChildren());
        dir.refresh();
        List<FileObject> arr2 = Arrays.asList(dir.getChildren());
        
        assertEquals("Same results", arr, arr2);
    }
    
    
    /**
     * Test for bug 228470.
     *
     * @throws java.io.IOException
     */
    public void testGetChild() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject dir = fo.getFileObject("dir");
        assertNotNull("dir found", dir);
        registerHooks();
        FileObject fileObject = dir.getFileObject("x50.txt");
        assertNotNull(fileObject);
    }

    private static class AssertNoLockManager extends TrackingHooks {
        final String prefix;

        public AssertNoLockManager(String p) {
            prefix = p;
        }

        @Override
        public void checkFileRead(String string) {
            if (string.startsWith(prefix)) {
                assertFalse("No lock", ChildrenSupport.isLock());
            }
        }

    }
        
}
