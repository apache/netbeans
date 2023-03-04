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

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.providers.CheckProviders;
import org.openide.filesystems.*;

/**
 * @author rmatous
 */
public class FileBasedFileSystemTest extends FileSystemFactoryHid {
    public FileBasedFileSystemTest(Test test) {
        super(test);
    }
    
    @Override
    protected void setServices(Class<?>... services) {
        List<Class<?>> arr = new ArrayList<Class<?>>();
        arr.addAll(Arrays.asList(services));
        arr.add(FileBasedURLMapper.class);
        MockServices.setServices(arr.toArray(new Class<?>[0]));
    }

    public static Test suite() {
        return new FileBasedFileSystemTest(suite(false));
    }
    static NbTestSuite suite(boolean created) {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);
        suite.addTestSuite(FileUtilJavaIOFileHidden.class);
        suite.addTestSuite(BaseFileObjectTestHid.class);
        suite.addTestSuite(TempFileObjectTestHid.class);
        suite.addTest(new CheckProviders(created));
        return suite;
    }
        
    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        return new File(workDirProperty);
    }
            
    @Override
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        FileObjectFactory.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(workFo);
        for (int i = 0; i < resources.length; i++) {
            String res = resources[i];
            if (res.endsWith("/")) {
                assertNotNull(FileUtil.createFolder(workFo,res));
            } else {
                assertNotNull(FileUtil.createData(workFo,res));
            }
        }
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    @Override
    protected void destroyFileSystem(String testName) throws IOException {}    

    @Override
    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir()).getPath();
    }
}
