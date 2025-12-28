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

package org.netbeans.modules.versioning;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class DeleteCreateTest extends NbTestCase {
    private File dataRootDir;

    public DeleteCreateTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        dataRootDir = getWorkDir();
        dataRootDir.mkdirs();
        File userdir = new File(dataRootDir + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        MockServices.setServices(DeleteCreateTestAnnotationProvider.class);
        // interceptor init
        DeleteCreateTestAnnotationProvider.instance.init();
    }

    @Override
    protected void tearDown() throws Exception {        
        DeleteCreateTestAnnotationProvider.instance.reset();
    }

    public void testDeleteCreateFile() throws IOException {
        
        // non atomic delete and create
        File file1 = new File(dataRootDir, "file1");
        file1 = FileUtil.normalizeFile(file1);
        file1.createNewFile();
                
        final FileObject fo1 = FileUtil.toFileObject(file1);
        fo1.delete();
        fo1.getParent().createData(fo1.getName());             
        
        // get intercepted events 
        String[] nonAtomic = DeleteCreateTestAnnotationProvider.instance.events.toArray(new String[0]);
        DeleteCreateTestAnnotationProvider.instance.events.clear();
        
        // atomic delete and create
        File file2 = new File(dataRootDir, "file2");
        file2 = FileUtil.normalizeFile(file2);
        file2.createNewFile();
        
        final FileObject fo2 = FileUtil.toFileObject(file2);
        AtomicAction a = new AtomicAction() {
            public void run() throws IOException {             
                fo2.delete();
                fo2.getParent().createData(fo2.getName());
            }
        };
        fo2.getFileSystem().runAtomicAction(a);        
        // get intercepted events 
        String[] atomic = DeleteCreateTestAnnotationProvider.instance.events.toArray(new String[0]);
        
        Logger l = Logger.getLogger(DeleteCreateTest.class.getName());
        l.info("- atomic events ----------------------------------");
        for (String s : atomic) l.info(s);        
        l.info("- non atomic events ------------------------------");
        for (String s : nonAtomic) l.info(s);
        l.info("-------------------------------");
        
        // test
        assertEquals(atomic.length, nonAtomic.length);
        for (int i = 0; i < atomic.length; i++) {
            assertEquals(atomic[i], nonAtomic[i]);            
        }        
    }  

    public void testDeleteCreateFolder() throws IOException {

        // non atomic delete and create
        File file1 = new File(dataRootDir, "folder1");
        file1 = FileUtil.normalizeFile(file1);
        file1.mkdirs();

        final FileObject fo1 = FileUtil.toFileObject(file1);
        fo1.delete();
        fo1.getParent().createFolder(fo1.getName());

        // get intercepted events
        String[] nonAtomic = DeleteCreateTestAnnotationProvider.instance.events.toArray(new String[0]);
        DeleteCreateTestAnnotationProvider.instance.events.clear();

        // atomic delete and create
        File file2 = new File(dataRootDir, "folder2");
        file2 = FileUtil.normalizeFile(file2);
        file2.mkdirs();

        final FileObject fo2 = FileUtil.toFileObject(file2);
        AtomicAction a = new AtomicAction() {
            public void run() throws IOException {
                fo2.delete();
                fo2.getParent().createFolder(fo2.getName());
            }
        };
        fo2.getFileSystem().runAtomicAction(a);
        // get intercepted events
        String[] atomic = DeleteCreateTestAnnotationProvider.instance.events.toArray(new String[0]);

        Logger l = Logger.getLogger(DeleteCreateTest.class.getName());
        l.info("- atomic events ----------------------------------");
        for (String s : atomic) l.info(s);
        l.info("- non atomic events ------------------------------");
        for (String s : nonAtomic) l.info(s);
        l.info("-------------------------------");

        // test
        assertEquals(atomic.length, nonAtomic.length);
        for (int i = 0; i < atomic.length; i++) {
            assertEquals(atomic[i], nonAtomic[i]);
        }
    }

}
