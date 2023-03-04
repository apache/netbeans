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

package org.netbeans.modules.web.project.classpath;

import java.io.File;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Test for {@link WebProjectClassPathModifier}.
 * @author tmysik
 */
public class WebProjectClassPathModifierTest extends NbTestCase {
    
    private FileObject scratch;
    
    public WebProjectClassPathModifierTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        scratch = TestUtil.makeScratchDir(this);
    }

    // #113390
    public void testRemoveRoots() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        FileObject projdir = FileUtil.toFileObject(f);
        WebProject webProject = (WebProject) ProjectManager.getDefault().findProject(projdir);
        
        Sources sources = ProjectUtils.getSources(webProject);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject srcJava = webProject.getSourceRoots().getRoots()[0];
        assertEquals("We should edit sources", "${src.dir}", groups[0].getName());
        String classPathProperty = webProject.getClassPathProvider().getPropertyName(groups[0], ClassPath.COMPILE)[0];
        
        AntProjectHelper helper = webProject.getAntProjectHelper();
        
        // create src folder
        final String srcFolder = "srcFolder";
        File folder = new File(getDataDir().getAbsolutePath(), srcFolder);
        if (folder.exists()) {
            folder.delete();
        }
        FileUtil.createFolder(folder);
        URL[] cpRoots = new URL[]{folder.toURL()};
        
        // init
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String cpProperty = props.getProperty(classPathProperty);
        boolean alreadyOnCp = cpProperty.indexOf(srcFolder) != -1;
        //assertFalse("srcFolder should not be on cp", alreadyInCp);
        
        // add
        boolean addRoots = ProjectClassPathModifier.addRoots(cpRoots, srcJava, ClassPath.COMPILE);
        // we do not check this - it can be already on cp (tests are created only before the 1st test starts)
        if (!alreadyOnCp) {
            assertTrue(addRoots);
        }
        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        cpProperty = props.getProperty(classPathProperty);
        assertTrue("srcFolder should be on cp", cpProperty.indexOf(srcFolder) != -1);
        
        // simulate #113390
        folder.delete();
        assertFalse("srcFolder should not exist.", folder.exists());
        
        // remove
        boolean removeRoots = ProjectClassPathModifier.removeRoots(cpRoots, srcJava, ClassPath.COMPILE);
        assertTrue(removeRoots);
        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        cpProperty = props.getProperty(classPathProperty);
        assertTrue("srcFolder should not be on cp", cpProperty.indexOf(srcFolder) == -1);
    }
}
