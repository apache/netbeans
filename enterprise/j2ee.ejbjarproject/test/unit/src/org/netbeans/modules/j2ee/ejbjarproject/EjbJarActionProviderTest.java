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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

// XXX much more to test

/**
 * Test {@link EjbJarActionProvider}.
 *
 * @author Martin Krauskopf, Andrei Badea
 */
public class EjbJarActionProviderTest extends NbTestCase {
    
    private Project project;
    private ActionProvider ap;
    
    public EjbJarActionProviderTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        ap = project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have ActionProvider", ap);
    }
    
    public void testDebugSingle() throws Exception { // #72733
        FileObject test = project.getProjectDirectory().getFileObject("test/pkg/NewClassTest.java");
        assertNotNull("have test/pkg/NewClassTest.java", test);
        assertTrue("Debug File is enabled on test", ap.isActionEnabled(
                ActionProvider.COMMAND_DEBUG_SINGLE,
                Lookups.singleton(DataObject.find(test))));
        
        // Test removed from suite since it accesses MDR repository and as such
        // it must be executed by ide executor, see issue #82795
        // FileObject source = project.getProjectDirectory().getFileObject("src/java/pkg/NewClass.java");
        // assertNotNull("have src/java/pkg/NewClass.java", source);
        // assertFalse("Debug File is disabled on source file", ap.isActionEnabled(
        //         ActionProvider.COMMAND_DEBUG_SINGLE,
        //         Lookups.singleton(DataObject.find(source))));
    }
    
    public void testCompileSingle() throws Exception { // #79581
        assertFalse("Compile Single is disabled on empty context", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookup.EMPTY));
        assertFalse("Compile Single is disabled on project directory", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(project.getProjectDirectory()))));
        
        FileObject testPackage = project.getProjectDirectory().getFileObject("test/pkg");
        assertNotNull("have test/pkg", testPackage);
        assertTrue("Compile Single is enabled on test package", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(testPackage))));
        FileObject test = project.getProjectDirectory().getFileObject("test/pkg/NewClassTest.java");
        assertNotNull("have test/pkg/NewClassTest.java", test);
        assertTrue("Compile Single is enabled on test", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(test))));

        FileObject srcPackage = project.getProjectDirectory().getFileObject("src/java/pkg");
        assertNotNull("have src/java/pkg", srcPackage);
        assertTrue("Compile Single is enabled on source package", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(srcPackage))));
        FileObject src = project.getProjectDirectory().getFileObject("src/java/pkg/NewClass.java");
        assertNotNull("have src/java/pkg/NewClass.java", src);
        assertTrue("Compile Single is enabled on source", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(src))));
    }
}
