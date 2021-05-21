/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Test AntArtifactProviderImpl.
 * @author Jaroslav Tulach, Jesse Glick
 */
public class AntArtifactProviderImplTest extends TestBase {
    
    public AntArtifactProviderImplTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileObject dir = nbRoot().getFileObject("java/java.project");
        assertNotNull("have java.project checked out", dir);
        javaProjectProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        dir = nbRoot().getFileObject("platform/openide.loaders");
        assertNotNull("have openide.loaders checked out", dir);
        loadersProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
    }
    
    public void testJARFileIsProduced() throws Exception {
        AntArtifact[] arts = AntArtifactQuery.findArtifactsByType(loadersProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct project", loadersProject, arts[0].getProject());
        assertEquals("correct type", JavaProjectConstants.ARTIFACT_TYPE_JAR, arts[0].getType());
        assertEquals("correct ID", "module", arts[0].getID());
        assertEquals("correct location",
            Collections.singletonList(URI.create("../../nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
        assertEquals("correct script", nbRoot().getFileObject("platform/openide.loaders/build.xml"), arts[0].getScriptFile());
        assertEquals("correct build target", "netbeans", arts[0].getTargetName());
        assertEquals("correct clean target", "clean", arts[0].getCleanTargetName());
        assertEquals("no properties", new Properties(), arts[0].getProperties());
        arts = AntArtifactQuery.findArtifactsByType(javaProjectProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct location",
            Collections.singletonList(URI.create("../../nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
    }
    
}
