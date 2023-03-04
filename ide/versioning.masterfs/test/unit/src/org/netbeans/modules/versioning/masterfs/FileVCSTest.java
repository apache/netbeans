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
package org.netbeans.modules.versioning.masterfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.versioning.*;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class FileVCSTest extends VCSFilesystemTestFactory {
    private String workDirPath;

    public FileVCSTest(Test test) {
        super(test);
        VCSFileProxy.createFileProxy(new File("")); // init APIAccessor
    }

    @Override
    protected String getRootPath() throws IOException {
        return workDirPath;
    }
    
    @Override
    protected FileObject createFolder(String path) throws IOException {
        FileObject fo;
        File f = new File(workDirPath, path);
        if(!f.exists()) {
            f.mkdirs();
            fo = FileUtil.toFileObject(f);
        } else {
            fo = FileUtil.toFileObject(f);
        }
        return fo;
    }
    
    @Override
    protected FileObject createFile(String path) throws IOException {
        FileObject fo;
        File f = new File(workDirPath, path);
        if(!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
            fo = FileUtil.toFileObject(f);
            f.delete();
        } else {
            fo = FileUtil.toFileObject(f);
        }
        return fo;
    }

    @Override
    protected void setReadOnly(String path) throws IOException {
        File f = new File(workDirPath, path);
        assertNotNull(f);
        f.setReadOnly();
    }

    @Override
    public void delete(String path) throws IOException {
        File f = new File(workDirPath, path);
        f.delete();
    }

    public void move(String from, String to) throws IOException {
        File fromFile = new File(workDirPath, from);
        File toFile = new File(workDirPath, to);
        if(!fromFile.renameTo(toFile)) throw new IOException("wasn't able to move " + fromFile + " to " + toFile);
    }
    
    public void copy(String from, String to) throws IOException {
        File fromFile = new File(workDirPath, from);
        File toFile = new File(workDirPath, to);        
        copy(fromFile, toFile);
    }
    
    private void copy(File fromFile, File toFile) throws IOException {
        if(fromFile.isFile()) {
            InputStream is = new FileInputStream (fromFile);
            OutputStream os = new FileOutputStream(toFile);
            FileUtil.copy(is, os);
            is.close();
            os.close();
        } else {
            toFile.mkdirs();
            File[] files = fromFile.listFiles();
            if( files == null || files.length == 0) {
                return;
            }
            for(File f : files) {
                copy(f, new File(toFile, f.getName()));
            }
        }
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(VCSOwnerCacheTestCase.class);
        suite.addTestSuite(VCSOwnerTestCase.class);
        suite.addTestSuite(VCSInterceptorTestCase.class);
        suite.addTestSuite(VCSAnnotationProviderTestCase.class);
        return new FileVCSTest(suite);
    }
    
    @Override
    protected void setUp() throws Exception {
        workDirPath = System.getProperty("work.dir");//NOI18N
        workDirPath = (workDirPath != null) ? workDirPath : System.getProperty("java.io.tmpdir");//NOI18N
        if(!workDirPath.isEmpty()) {
            File f = new File(workDirPath);
            deleteRecursively(f);
        }
    }

    @Override
    protected void tearDown() throws Exception {     
        if(!workDirPath.isEmpty()) {
            File f = new File(workDirPath);
            deleteRecursively(f);
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }    
}
