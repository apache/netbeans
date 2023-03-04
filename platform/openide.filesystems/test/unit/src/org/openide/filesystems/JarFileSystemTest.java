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

package org.openide.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;

/**
 * @author  rm111737
 */
public class JarFileSystemTest extends FileSystemFactoryHid {
     JarFileSystem jfs;
    /** Creates new JarFileSystemTest */
    public JarFileSystemTest(Test test) {
        super(test);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RepositoryTestHid.class);                
        suite.addTestSuite(FileSystemTestHid.class);        
        suite.addTestSuite(FileObjectTestHid.class);
        /*failing tests*/
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(URLMapperTestInternalHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);
        suite.addTestSuite(FileUtilJavaIOFileHidden.class);
        suite.addTestSuite(JarFileSystemHidden.class);
        
        return new JarFileSystemTest(suite);
    }
    @Override
    protected void destroyFileSystem (String testName) throws IOException {}
    
    @Override
    protected FileSystem[] createFileSystem (String testName, String[] resources) throws IOException{
        File jar = TestUtilHid.locationOfTempFolder("jfstest");
        jar.mkdir();
        
        File f = new File (jar,"jfstest.jar");
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        JarOutputStream jos = new JarOutputStream (new FileOutputStream (f));        
        
        for (int i = 0; i < resources.length; i++) {
            String entryName = resources[i];
            if (entryName.startsWith("/")) entryName = entryName.substring(1);             
            jos.putNextEntry(new ZipEntry (entryName));
        }
        
        
       jos.close();        
        
        jfs = new JarFileSystem  ();
        try {
            jfs.setJarFile(f);
        } catch (Exception ex) {}
                
        return new FileSystem[] {jfs};
    }
}
