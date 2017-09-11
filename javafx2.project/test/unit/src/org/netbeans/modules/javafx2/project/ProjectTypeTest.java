/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
