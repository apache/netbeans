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

package org.netbeans.modules.j2ee.clientproject.queries;

import java.io.File;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.test.MockLookup;

/**
 * @author Andrei Badea
 */
public class CompiledSourceForBinaryQueryTest extends NbTestCase {
    
    private Project project;
    private AntProjectHelper helper;
    
    public CompiledSourceForBinaryQueryTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception {
        MockLookup.setLayersAndInstances();

        File f = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        project = (Project) ProjectSupport.openProject(f);
        // XXX should not cast a Project
        helper = ((AppClientProject) project).getAntProjectHelper();
    }
    
    public void testSourceRootsFoundForNonExistingBinaryRootIssue65733() throws Exception {
        File buildClassesDir  = helper.resolveFile(helper.getStandardPropertyEvaluator().getProperty(ProjectProperties.BUILD_CLASSES_DIR));
        // the file must not exist
        assertFalse("Cannot test, the project should be cleaned first!", buildClassesDir .exists());
        URL buildClassesDirURL = new URL(buildClassesDir.toURI().toURL().toExternalForm() + "/");
        SourceForBinaryQueryImplementation s4bqi = project.getLookup().lookup(SourceForBinaryQueryImplementation.class);
        assertNotNull(s4bqi.findSourceRoots(buildClassesDirURL));
    }
    
}
