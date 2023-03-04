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
package org.netbeans.modules.javafx2.project;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.FXProjectSupport;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Somol
 */
public class ProjectTypeTest extends NbTestCase {
    
    public ProjectTypeTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws IOException {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        System.out.println("FXFXFXFX  "+getName()+"  FXFXFXFX");
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
            .addTest(ProjectTypeTest.class,
                "testFXTypeProperty",
                "testCreatedFXProjectType",
                "testCreatedSEProjectType"
            )
        .enableModules(".*").clusters(".*"));
    }
    
    /** Test FX type identifying property. */
    public void testFXTypeProperty() {
        // this is necessary to avoid SE-on-FX dependence but enable FX type test in J2SECompositePanelProvider
        assertEquals(JFXProjectProperties.JAVAFX_ENABLED, "javafx.enabled");
    }
            
    /** Test FX createProject method. */
    public void testCreatedFXProjectType() throws Exception {
        File projectParentDir = this.getWorkDir();
        Project project = (Project)FXProjectSupport.createProject(projectParentDir, "SampleFXProject");
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("Only 1 project should be opened.", 1, projects.length);
        assertSame("Created project not opened.", project, projects[0]);

        // FX project type verification through API
        assertTrue(JFXProjectUtils.isFXProject(project));
        
        J2SEPropertyEvaluator j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
        assertNotNull(j2sePropEval);
        PropertyEvaluator evaluator = j2sePropEval.evaluator();
        assertNotNull(evaluator);
        // FX project type verification through property
        assertTrue(JFXProjectProperties.isTrue(evaluator.getProperty(JFXProjectProperties.JAVAFX_ENABLED)));
        // FX projects must not interfere with pre-FX WebStart module code
        assertFalse(JFXProjectProperties.isTrue(evaluator.getProperty("jnlp.enabled")));
    }

    /** Test SE createProject method to produce non-FX project. */
    public void testCreatedSEProjectType() throws Exception {
        File projectParentDir = this.getWorkDir();
        String mainClass = null;
        String name = "SampleSEProject";
        Project project = null;
        try {
            File projectDir = new File(projectParentDir, name);
            J2SEProjectGenerator.createProject(projectDir, name, mainClass, null, null, false);
            project = (Project)ProjectSupport.openProject(projectDir);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
        assertNotNull(project);
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("Both project should be opened.", 2, projects.length);
        assertSame("Created project not opened.", project, projects[1]);

        // non-FX project type verification through API
        assertFalse(JFXProjectUtils.isFXProject(project));
        
        J2SEPropertyEvaluator j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
        assertNotNull(j2sePropEval);
        PropertyEvaluator evaluator = j2sePropEval.evaluator();
        assertNotNull(evaluator);
        // non-FX project type verification through property
        assertFalse(JFXProjectProperties.isTrue(evaluator.getProperty(JFXProjectProperties.JAVAFX_ENABLED)));
    }

}
