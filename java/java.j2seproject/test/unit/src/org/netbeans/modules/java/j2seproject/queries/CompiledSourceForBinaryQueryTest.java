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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.IOException;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.SourceRootsTest;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

/**
 * Tests for CompiledSourceForBinaryQuery
 *
 * @author Tomas Zezula
 */
public class CompiledSourceForBinaryQueryTest extends NbTestCase {
    
    public CompiledSourceForBinaryQueryTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject buildClasses;
    private ProjectManager pm;
    private Project pp;
    AntProjectHelper helper;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(
            new org.netbeans.modules.java.project.ProjectSourceForBinaryQuery(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        );
        Properties p = System.getProperties();
    }

    @Override
    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }
    
    
    private void prepareProject () throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false);
        J2SEProjectGenerator.setDefaultSourceLevel(null);   //NOI18N
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        sources = projdir.getFileObject("src");
        FileObject fo = projdir.createFolder("build");
        buildClasses = fo.createFolder("classes");        
    }
    
    public void testSourceForBinaryQuery() throws Exception {
        this.prepareProject();
        FileObject folder = scratch.createFolder("SomeFolder");
        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(folder.toURL());
        assertEquals("Non-project folder does not have any source folder", 0, result.getRoots().length);
        folder = projdir.createFolder("SomeFolderInProject");
        result = SourceForBinaryQuery.findSourceRoots(folder.toURL());
        assertEquals("Project non build folder does not have any source folder", 0, result.getRoots().length);
        result = SourceForBinaryQuery.findSourceRoots(buildClasses.toURL());
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",sources,result.getRoots()[0]);
    }               
    
    
    public void testSourceForBinaryQueryListening () throws Exception {
        this.prepareProject();
        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(buildClasses.toURL());
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",sources,result.getRoots()[0]);
        TestListener tl = new TestListener ();
        result.addChangeListener(tl);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        FileObject sources2 = projdir.createFolder("src2");
        props.put ("src.dir","src2");        
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertTrue (tl.wasEvent());
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",sources2,result.getRoots()[0]);
    }

    public void testSourceForBinaryQueryMultipleSourceRoots () throws Exception {
        this.prepareProject();
        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(buildClasses.toURL());
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",sources,result.getRoots()[0]);
        TestListener tl = new TestListener ();
        result.addChangeListener(tl);
        FileObject newRoot = SourceRootsTest.addSourceRoot(helper,projdir,"src.other.dir","other");
        assertTrue (tl.wasEvent());
        assertEquals("Project build folder must have 2 source folders", 2, result.getRoots().length);
        assertEquals("Project build folder must have the first source folder",sources,result.getRoots()[0]);
        assertEquals("Project build folder must have the second source folder",newRoot,result.getRoots()[1]);
    }

    private static class TestListener implements ChangeListener {
        
        private boolean gotEvent;
        
        public void stateChanged(ChangeEvent changeEvent) {
            this.gotEvent = true;
        }      
        
        public void reset () {
            this.gotEvent = false;
        }
        
        public boolean wasEvent () {
            return this.gotEvent;
        }
    }
        
}
