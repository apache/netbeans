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

package org.netbeans.modules.localhistory;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.Filter;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.masterfs.filebasedfs.BaseFileObjectTestHid;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.*;


/**
 * @author Tomas Stupka
 */
public class LHFileSystemTest extends FileSystemFactoryHid {

    public LHFileSystemTest(Test test) {
        super(test);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = getWorkDir("userdir");
        // cleanup first
        FileUtils.deleteRecursively(f.getParentFile());

        f.mkdirs();
        System.setProperty("netbeans.user", f.getAbsolutePath());
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});

        // ensure test files are handled by LH
        System.setProperty("netbeans.localhistory.historypath", System.getProperty("work.dir"));
    }
    
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
        Filter filter = new Filter();
        filter.setExcludes(new Filter.IncludeExclude[] {
            new Filter.IncludeExclude("testFileUtilToFileObjectIsValid", "fails occassionaly")
        });
        suite.setFilter(filter);
        suite.addTestSuite(BaseFileObjectTestHid.class);            
        return new LHFileSystemTest(suite);
    }
    
    private File getWorkDir(String testName) {
        String workDirProperty = System.getProperty("work.dir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        File f = new File(workDirProperty + "/" + testName);
        f.mkdirs();
        return f;
    }

    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {                       
        FileObjectFactory.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir(testName));
        assertNotNull(workFo);
        List<File> files = new ArrayList<File>(resources.length);
        for (int i = 0; i < resources.length; i++) {            
            String res = resources[i];
            FileObject fo;
            if (res.endsWith("/")) {
                fo = FileUtil.createFolder(workFo,res);
                assertNotNull(fo);
            } else {
                fo = FileUtil.createData(workFo,res);
                assertNotNull(fo);
            }            
            files.add(FileUtil.toFile(fo));            
        }        
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    protected void destroyFileSystem(String testName) throws IOException {}    

    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir(testName)).getPath();
    }

    private String getPath(File f) {
        return FileUtils.getPath(VCSFileProxy.createFileProxy(f));
    }

}
