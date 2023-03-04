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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * @author Milan Kubec
 */
public class FileEncodingQueryTest extends NbTestCase {
    
    /** Creates a new instance of FileEncodingQeuryTest */
    public FileEncodingQueryTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    @Override
    protected int timeOut() {
        return 300000;
    }
    
    public void testFileEncodingQuery() throws Exception {
        
        String projectFolder = "proj1";
        String projectName = "proj-1";
      
        File prjBase = new File(getWorkDir(), projectFolder);
        prjBase.mkdir();
        File antScript = new File(prjBase, "build.xml");
        antScript.createNewFile();
        
        File srcDir = new File(prjBase, "src");
        srcDir.mkdir();
        File srcFile = createFileInPackage(srcDir, "testpackage", "ClassA.java");
        
        File testDir = new File(prjBase, "test");
        testDir.mkdir();
        File testFile = createFileInPackage(testDir, "otherpackage", "ClassB.java");
        
        File libsrcDir = new File(prjBase, "libsrc");
        libsrcDir.mkdir();
        File libFile = createFileInPackage(libsrcDir, "yetanotherpackage", "ClassC.java");
        
        List<JavaProjectGenerator.SourceFolder> sources = new ArrayList<JavaProjectGenerator.SourceFolder>();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(prjBase, prjBase, projectName, null);
        
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "src";
        sf.type = "java";
        sf.style = "packages";
        sf.location = "src";
        sf.encoding = "ISO-8859-1";
        sources.add(sf);
        
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "test";
        sf.type = "java";
        sf.style = "packages";
        sf.location = "test";
        sf.encoding = "ISO-8859-2";
        sources.add(sf);
        
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "proj-1";
        sf.location = ".";
        sf.encoding = "ISO-8859-3";
        sources.add(sf);
        
        JavaProjectGenerator.putSourceFolders(helper, sources, null);
        JavaProjectGenerator.putSourceViews(helper, sources, null);
        
        FileObject prjDir = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(prjDir);
        ProjectManager.getDefault().saveProject(p);
        
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", prjDir, p.getProjectDirectory());
        
        Charset cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(srcFile));
        assertEquals("ISO-8859-1", cset.name());
        
        cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(testFile));
        assertEquals("ISO-8859-2", cset.name());
        
        // file not under any src root
        File lonelyFile = new File(prjBase, "testfile.txt");
        lonelyFile.createNewFile();
        cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(lonelyFile));
        assertEquals("ISO-8859-3", cset.name());
        
        cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(libFile));
        assertEquals("ISO-8859-3", cset.name());
        
    }
    
    private File createFileInPackage(File root, String pkgName, String fileName) throws IOException {
        File pkg = new File(root, pkgName);
        pkg.mkdir();
        File file = new File(pkg, fileName);
        file.createNewFile();
        return file;
    }
    
}
