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

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFreeformFileBuiltQueryTest extends TestBase {
    
    public JavaFreeformFileBuiltQueryTest(String testName) {
        super(testName);
    }
    
    private Project simpleProject;

    private void deepCopy(FileObject source, FileObject target) throws IOException {
        List toCopy = new LinkedList();
        
        FileObject[] children = source.getChildren();
        
        for (int cntr = 0; cntr < children.length; cntr++) {
            toCopy.add(children[cntr].getNameExt());
        }
        
        while (toCopy.size() > 0) {
            String copying = (String) toCopy.remove(0);
            FileObject toCopyFO = source.getFileObject(copying);
            FileObject targetParent   = target.getFileObject(FileUtil.getRelativePath(source, toCopyFO.getParent()));
            
            if (toCopyFO.isFolder()) {
                targetParent.createFolder(toCopyFO.getNameExt());
                
                children = toCopyFO.getChildren();
                
                for (int cntr = 0; cntr < children.length; cntr++) {
                    toCopy.add(FileUtil.getRelativePath(source, children[cntr]));
                }
            } else {
                FileUtil.copyFile(toCopyFO, targetParent, toCopyFO.getName(), toCopyFO.getExt());
            }
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        assertNotNull("Must have built ant/freeform unit tests first, INCLUDING copying non-*.java resources to the classes build directory",
            JavaFreeformFileBuiltQueryTest.class.getResource("/META-INF/services/org.openide.modules.InstalledFileLocator"));
        
        FileObject root = simple.getProjectDirectory();
        clearWorkDir();
        File workdir = getWorkDir();
        FileObject workdirFO = FileUtil.toFileObject(workdir);
        FileObject simpleFO = workdirFO.createFolder("simple");
        
        deepCopy(root, simpleFO);
        
        simpleProject = ProjectManager.getDefault().findProject(simpleFO);
    }
    
    private void createEmptyFile(File f) throws IOException {
        f.getParentFile().mkdirs();
        new FileOutputStream(f).close();
    }
    
    private void refreshRecursively(FileObject of) {
        List toRefresh = new LinkedList();
        
        toRefresh.add(of);
        
        while (toRefresh.size() > 0) {
            FileObject f = (FileObject) toRefresh.remove(0);
            
            f.refresh();
            
            FileObject[] children = f.getChildren();
            
            for (int cntr = 0; cntr < children.length; cntr++) {
                toRefresh.add(children[cntr]);
            }
        }
    }
    
    public void testStatusUpdated() throws Exception {
        //src->build/classes
        //antsrc->build/antclasses
        //src: org/foo/myapp/MyApp.java
        //                  /SomeFile.java
        //antsrc: org/foo/ant/SpecialTask.java
        FileObject src = simpleProject.getProjectDirectory().getFileObject("src");
        FileObject antsrc = simpleProject.getProjectDirectory().getFileObject("antsrc");
        
        FileObject MyAppJava = src.getFileObject("org/foo/myapp/MyApp.java");
        FileObject SomeFileJava = src.getFileObject("org/foo/myapp/SomeFile.java");
        FileObject SpecialTaskJava = antsrc.getFileObject("org/foo/ant/SpecialTask.java");
        
        File MyAppClass = new File(FileUtil.toFile(simpleProject.getProjectDirectory()), "build/classes/org/foo/myapp/MyApp.class".replace('/', File.separatorChar));
        File SomeFileClass = new File(FileUtil.toFile(simpleProject.getProjectDirectory()), "build/classes/org/foo/myapp/SomeFile.class".replace('/', File.separatorChar));
        File SpecialTaskClass = new File(FileUtil.toFile(simpleProject.getProjectDirectory()), "build/antclasses/org/foo/ant/SpecialTask.class".replace('/', File.separatorChar));
        
        FileBuiltQueryImplementation fbqi = simpleProject.getLookup().lookup(FileBuiltQueryImplementation.class);
        
        assertNotNull("have FileBuiltQueryImplementation in lookup", fbqi);
        
        FileBuiltQuery.Status MyAppStatus = fbqi.getStatus(MyAppJava);
        FileBuiltQuery.Status SomeFileStatus = fbqi.getStatus(SomeFileJava);
        FileBuiltQuery.Status SpecialTaskStatus = fbqi.getStatus(SpecialTaskJava);
        
        assertFalse("MyApp.java is not built", MyAppStatus.isBuilt());
        assertFalse("SomeFile.java is not built", SomeFileStatus.isBuilt());
        assertFalse("SpecialTask.java is not built", SpecialTaskStatus.isBuilt());
        
        createEmptyFile(MyAppClass);
        createEmptyFile(SpecialTaskClass);
        
        refreshRecursively(simpleProject.getProjectDirectory());

        assertTrue("MyApp.java is built", MyAppStatus.isBuilt());
        assertFalse("SomeFile.java is not built", SomeFileStatus.isBuilt());
        assertTrue("SpecialTask.java is built", SpecialTaskStatus.isBuilt());
        
        MyAppClass.delete();
        
        refreshRecursively(simpleProject.getProjectDirectory());
        
        assertFalse("MyApp.java is built", MyAppStatus.isBuilt());
        assertFalse("SomeFile.java is not built", SomeFileStatus.isBuilt());
        assertTrue("SpecialTask.java is built", SpecialTaskStatus.isBuilt());
        
        SpecialTaskClass.delete();
        createEmptyFile(MyAppClass);
        createEmptyFile(SomeFileClass);
        
        refreshRecursively(simpleProject.getProjectDirectory());
        
        assertTrue("MyApp.java is built", MyAppStatus.isBuilt());
        assertTrue("SomeFile.java is not built", SomeFileStatus.isBuilt());
        assertFalse("SpecialTask.java is built", SpecialTaskStatus.isBuilt());
    }
}
