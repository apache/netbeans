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
package org.netbeans.modules.gradle;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport.Property;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport.PropertyKind;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.projectapi.nb.NbProjectManagerAccessor;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class NbGradleProjectImpl2Test extends AbstractGradleProjectTestCase {
    private FileObject projectDir;
    private Project project;
    
    
    public NbGradleProjectImpl2Test(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
        NbProjectManagerAccessor.reset();
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    private FileObject copyProject(String relPath) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject(relPath);
        
        projectDir = src.copy(wd, src.getName(), src.getExt());
        project = ProjectManager.getDefault().findProject(projectDir);
        return projectDir;
    }

    
    /**
     * Check that an unknown project indicates priming and errors.
     * @throws Exception 
     */
    public void testFirstTimeProjectNotPrimed() throws Exception {
        copyProject("projects/priming/broken1");
        ProjectTrust.getDefault().trustProject(project);
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        assertNotNull(ap);
        assertTrue(ProjectProblems.isBroken(project));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY));
        
        NbGradleProject gp = NbGradleProject.get(project);
        assertEquals(NbGradleProject.Quality.FALLBACK, gp.getQuality());
   }
    
    /**
     * Check that a broken project stops indicating priming, but is still not full quality and indicates problems.
     */
    public void testForceLoadBrokenProject() throws Exception {
        copyProject("projects/priming/broken1");
        ProjectTrust.getDefault().trustProject(project);
        
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        assertTrue("Project should require priming", ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY));
        
        NbGradleProject gp1 = NbGradleProject.get(project);
        GradleBaseProject gbp1 = GradleBaseProject.get(project);
        NbGradleProject.Quality q1 = gp1.getQuality();
        assertEquals("Project fallback expeceted", NbGradleProject.Quality.FALLBACK, q1);
        
        NbGradleProject gp2 = gp1.toQuality("Priming", NbGradleProject.Quality.FULL_ONLINE, false).toCompletableFuture().get();
        assertEquals("Project should be reloaded with errors", NbGradleProject.Quality.SIMPLE, gp2.getQuality());

        GradleBaseProject gbp2 = GradleBaseProject.get(project);
        assertSame("Project API is not the same", gp1, gp2);
        assertNotSame("Project information was not replaced", gbp1, gbp2);
        
        assertNotNull(ap);
        assertFalse("Project was already primed", ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY));
        
        assertTrue("Project has still problems", ProjectProblems.isBroken(project));
    }
    
    public void testSpotbugsPlugin() throws Exception {
        copyProject("projects/spotbugs");
        ProjectTrust.getDefault().trustProject(project);

        NbGradleProject gp1 = NbGradleProject.get(project);
        NbGradleProject loaded =  gp1.toQuality("Load test project", NbGradleProject.Quality.FULL, false).toCompletableFuture().get();
        assertNotNull(loaded);
        assertTrue(loaded.getQuality().atLeast(NbGradleProject.Quality.FULL));
        
        BuildPropertiesSupport props = BuildPropertiesSupport.get(project);
        assertNotNull(props);
        Property rec = props.findExtensionProperty("", "recursiveProperty");
        assertNotNull(rec);
        assertEquals(PropertyKind.MAP, rec.getKind());
        
        Property k1 = props.get(rec, "key1", null);
        assertNull("Avoid recursion to same object", k1);
        
        Property k2 = props.get(rec, "key2", null);
        assertNotNull(k2);
        
        Property k3 = props.get(k2, "key3", null);
        assertNotNull(k3);
        assertEquals("Avoid loop references", PropertyKind.EXISTS, k3.getKind());
    }
}
