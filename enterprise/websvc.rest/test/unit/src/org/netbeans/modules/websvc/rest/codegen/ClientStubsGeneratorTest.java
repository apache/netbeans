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

package org.netbeans.modules.websvc.rest.codegen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.netbeans.modules.websvc.rest.support.ZipUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author ayubskhan
 */
public class ClientStubsGeneratorTest extends TestBase {
    
    public ClientStubsGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUpSrcDir();
        FileObject logDir = FileUtil.createFolder(new File(getWorkDir(), ".netbeans/var/log"));
    }
    
    public void testModelFromWadl() throws Exception {
//        String appName = "WebApplication1";
//        InputStream is = this.getClass().getResourceAsStream(appName.toLowerCase()+".xml");
//        ResourceModel m = new ClientStubModel().createModel(is);
//        m.build();
//        String url = ((WadlModeler)m).getBaseUrl();
//        assertEquals("http://localhost:8080/"+appName+"/resources/", url);
//        assertEquals(6, m.getResources().size());
    }

    private void testGenerateFromWadl(String appName) throws Exception {
        testGenerateFromWadl(appName, appName.toLowerCase());
    }
    
    private void testGenerateFromWadl(String appName, String fileName) throws Exception {
//        FileObject stubRoot = FileUtil.createFolder(getWorkDir());
//        String folder = "rest";
//        InputStream is = this.getClass().getResourceAsStream(fileName+".xml");
//        ClientStubsGenerator cs = new ClientStubsGenerator(stubRoot, folder, is, true);
//        cs.generate(null);
//
//        FileObject restFolder = stubRoot.getFileObject(folder);
//        File zipFile = new File(FileUtil.toFile(restFolder), fileName+"_1.zip");
//        if(zipFile.exists()) //clean
//            zipFile.delete();
//        FileObject appFolder = restFolder.getFileObject(appName.toLowerCase());
//        String[] sources = {
//            FileUtil.toFile(appFolder).getAbsolutePath()
//        };
//        String[] paths = {
//            ""
//        };
//        ZipUtil zipUtil = new ZipUtil();
//        zipUtil.zip(zipFile, sources, paths);
//
//        File base = new File(FileUtil.toFile(restFolder), fileName+".zip");
//        FileUtil.copy(this.getClass().getResourceAsStream(fileName+".zip"), new FileOutputStream(base));
//
//        assertEquals(base.length(), zipFile.length());
    }
    
    public void testGenerateFromWadlNonRecursiveResources() throws Exception {
        testGenerateFromWadl("WebApplication1");
    }
    
    public void testGenerateFromWadlRecursiveResources() throws Exception {
        testGenerateFromWadl("CustomerDB");
    }
    
    public void testGenerateFromWadlWithNonIdentifier() throws Exception {
        testGenerateFromWadl("foo_war");
    }
    
    public void testGenerateFromWadlRecursiveResources1() throws Exception {
        testGenerateFromWadl("_92_168_0_104_8080_smart_selection_rest_rest", "smart_selection_rest");
    }
    
    //Tests dont work due to exception from Retouche
//    public void testModelFromProject() throws Exception {
//        String appName = "WebApplication2";
//        InputStream is = this.getClass().getResourceAsStream(appName.toLowerCase()+".zip");
//        FileObject work = FileUtil.createFolder(getWorkDir());
//        ZipUtil zipUtil = new ZipUtil();
//        zipUtil.unzip(is, work, true);
//        Project p = FileOwnerQuery.getOwner(work.getFileObject(appName));
//        ResourceModel m = new ClientStubModel().createModel(p);
//        m.build();
//        assertEquals(6, m.getResources().size());
//    }
//    
//    public void testGenerateFromProject() throws Exception {
//        String appName = "WebApplication2";
//        InputStream is = this.getClass().getResourceAsStream(appName.toLowerCase()+".zip");
//        FileObject work = FileUtil.createFolder(getWorkDir());
//        ZipUtil zipUtil = new ZipUtil();
//        zipUtil.unzip(is, work, true);
//        Project p = FileOwnerQuery.getOwner(work.getFileObject(appName));
//        FileObject stubRoot = FileUtil.createFolder(getWorkDir());
//        String folder = "rest";
//        ClientStubsGenerator cs = new ClientStubsGenerator(stubRoot, folder, p, false, true);
//        cs.generate(null);
//        
//        FileObject restFolder = stubRoot.getFileObject(folder);
//        File zipFile = new File(FileUtil.toFile(restFolder), appName.toLowerCase()+"_1.zip");
//        if(zipFile.exists()) //clean
//            zipFile.delete();
//        FileObject appFolder = restFolder.getFileObject(appName.toLowerCase());
//        String[] sources = {
//            FileUtil.toFile(appFolder).getAbsolutePath()
//        };
//        String[] paths = {
//            ""
//        };
//        zipUtil.zip(zipFile, sources, paths);
//        
//        File base = new File(FileUtil.toFile(restFolder), appName.toLowerCase()+".zip");
//        FileUtil.copy(this.getClass().getResourceAsStream(appName.toLowerCase()+".zip"), new FileOutputStream(base));
//
//        assertEquals(base.length(), zipFile.length());
//    }
}
