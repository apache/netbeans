/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
