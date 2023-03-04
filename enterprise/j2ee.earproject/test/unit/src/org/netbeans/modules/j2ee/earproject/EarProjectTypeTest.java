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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 * @author vkraemer
 */
public final class EarProjectTypeTest extends NbTestCase {
    
    private AntBasedProjectType prjType;
    
    public EarProjectTypeTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockLookup.init();
        Collection<? extends AntBasedProjectType> all = Lookups.forPath("Services/AntBasedProjectTypes").lookupAll(AntBasedProjectType.class);
        prjType = null;
        for (AntBasedProjectType instance : all) {
            if ("org.netbeans.modules.j2ee.earproject".equals(instance.getType()))
            prjType = instance;
        }
        MockLookup.setLayersAndInstances(prjType);
        assertNotNull(prjType);
    }
    
    public void testCreateProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "EarProjectTypeTest.testCreatProject");
        FileUtil.createFolder(prjDirF);
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        AntProjectHelper tmp = ProjectGenerator.createProject(prjDirFO, prjType.getType());
        prjType.createProject(tmp);
    }
    
    public void testCreateProjectNullArg() throws Exception {
        try {
            prjType.createProject(null);
            fail("null is an invalid argument");
        } catch (NullPointerException ex) {
            // OK we should get here
        }
    }
    
    public void testGetType() {
        assertEquals(prjType.getType(), "org.netbeans.modules.j2ee.earproject");
    }
    
}
