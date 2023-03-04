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

package org.netbeans.modules.mercurial;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.filebasedfs.BaseFileObjectTestHid;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.filesystems.*;

/**
 * @author Tomas Stupka
 */
public class MercurialFileSystemTestCase extends FileSystemFactoryHid {
        
    public MercurialFileSystemTestCase(Test test) {
        super(test);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});                
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();        
        
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);        
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);                        
        suite.addTestSuite(FileUtilJavaIOFileHidden.class);                        
        suite.addTestSuite(BaseFileObjectTestHid.class);
        return new MercurialFileSystemTestCase(suite);
    }
    
    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        return new File(workDirProperty);
    }

    @Override
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        setupWorkdir();
        setupUserdir();
        
        FileObject workFo = null;
        try {
            init();
            
            FileObjectFactory.reinitForTests();
            workFo = FileBasedFileSystem.getFileObject(getWorkDir());
            assertNotNull(workFo);
            List<File> files = new ArrayList<File>(resources.length);
            for (int i = 0; i < resources.length; i++) {                
                String res = resources[i];
                FileObject fo;
                if (res.endsWith("/")) {
                    fo = FileUtil.createFolder(workFo, res);
                    assertNotNull(fo);
                } else {
                    fo = FileUtil.createData(workFo, res);
                    assertNotNull(fo);
                }                
                files.add(FileUtil.toFile(fo));                
            }
            commit(files);
            
        } catch (HgException ex) {
            throw new IOException(ex.getMessage());
        } 
        
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    @Override
    protected void destroyFileSystem(String testName) throws IOException {
        FileUtils.deleteRecursively(getWorkDir());
    }    

    @Override
    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir()).getPath();
    }

    private void commit(List<File> files) throws HgException {       
        
        List<File> filesToAdd = new ArrayList<File>();
        for (File file : files) {
            if(findStatus(HgCommand.getStatus(getWorkDir(), Collections.singletonList(file), null, null),
                    FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
                filesToAdd.add(file);
            }
        }            
            
        HgCommand.doAdd(getWorkDir(), filesToAdd, null);
        List<File> filesToCommit = new ArrayList<File>();
        for (File file : files) {
            if(file.isFile()) {
                filesToCommit.add(file);
            }
        }
        
        HgCommand.doCommit(getWorkDir(), filesToCommit, "commit", null);
//        for (File file : filesToCommit) {
//            assertStatus(file);
//        }        
    }
    
    private void init() throws HgException {
        HgCommand.doCreate(getWorkDir(), null);
    }    

    private boolean findStatus(Map<File, FileInformation> statuses, int status) {
        for (Map.Entry<File, FileInformation> e : statuses.entrySet()) {
            if (e.getValue().getStatus() == status) {
                return true;
            }
        }
        return false;
    }

    private void setupUserdir () {
        File f = new File(getWorkDir().getParentFile(), "userdir");
        FileUtils.deleteRecursively(f);
        f.mkdirs();
        System.setProperty("netbeans.user", f.getAbsolutePath());
        // ensure test files are handled by LH
        System.setProperty("netbeans.localhistory.historypath", getWorkDir().getAbsolutePath());
    }

    private void setupWorkdir () {
        File wd = getWorkDir();
        FileUtils.deleteRecursively(wd);
        wd.mkdirs();
    }
}
