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

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Radek Matous
 */
public class Deadlock73332Test extends NbTestCase {
    private static FileObject folder;
    static {
        System.setProperty("org.openide.util.Lookup", Deadlock73332Test.TestLookup.class.getName());
        assertTrue(Lookup.getDefault().getClass().getName(),Lookup.getDefault() instanceof Deadlock73332Test.TestLookup);
    }
    
    
    public Deadlock73332Test(String testName) {
        super(testName);
    }
    
    public void testDeadLock() throws Exception {
        assertNotNull(folder);
        assertTrue(folder instanceof BaseFileObj);
        FileObject data = FileUtil.createData(folder, "/a/b/c/data.txt");
        assertNotNull(data);
        FileLock lock = data.lock();
        try {
            data.move(lock,folder, data.getName(), data.getExt());
        } finally {
            lock.releaseLock();
        }
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File f = this.getWorkDir();
        folder = FileUtil.toFileObject(f);
    }
    
    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            setLookups(new Lookup[] {getMetaInfLookup()});
        }
        
        private Lookup getMetaInfLookup() {
            return Lookups.metaInfServices(Thread.currentThread().getContextClassLoader());
        }
        
        protected @Override void beforeLookup(Lookup.Template<?> template) {
            if (folder != null && template.getType().isAssignableFrom(BaseAnnotationProvider.class)) {
                RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
                    public @Override void run() {
                        folder.getChildren(true);
                    }
                });
                task.waitFinished();
            }
        }
    }
    
}
