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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory.ProjectDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.test.MockLookup;

public class ClassPathContainerResolverTest extends NbTestCase {
    
    public ClassPathContainerResolverTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(new ProjectTypeFactory() {
            public boolean canHandle(ProjectDescriptor descriptor) {
                return true;
            }
            public Project createProject(ProjectImportModel model, List<String> importProblems) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public Icon getProjectTypeIcon() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public String getProjectTypeName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public boolean prepare() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public File getProjectFileLocation(ProjectDescriptor descriptor, String token) {
                if (ProjectTypeFactory.FILE_LOCATION_TOKEN_WEBINF.equals(token)) {
                    return new File(descriptor.getEclipseProjectFolder(), "web/WEB-INF");
                }
                return null;
            }

            public List<Panel<WizardDescriptor>> getAdditionalImportWizardPanels() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }
    
    public void testIsJUnit() {
        assertTrue(ClassPathContainerResolver.isJUnit(EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "con",
                        "path", "org.eclipse.jdt.junit.JUNIT_CONTAINER/")));
        assertTrue(ClassPathContainerResolver.isJUnit(EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "var",
                        "path", "MAVEN_REPO/junit/jars/junit-3.8.1.jar")));
        assertTrue(ClassPathContainerResolver.isJUnit(EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "lib/junit.jar")));
        assertFalse(ClassPathContainerResolver.isJUnit(EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "some_folder/")));
    }

    private static EclipseProject getTestableProject(int version, File proj, Workspace w, String name) throws IOException {
        List<DotClassPathEntry> classpath = null;
        if (version == 1) {
            classpath = Arrays.asList(new DotClassPathEntry[]{
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "con",
                        "path", "org.eclipse.jst.j2ee.internal.web.container/projB"),
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/home/dev/smth/other.jar"),
            });
        } else if (version == 2) {
            classpath = Arrays.asList(new DotClassPathEntry[]{
                EclipseProjectTestUtils.createDotClassPathEntry(
                        "kind", "lib",
                        "path", "/home/dev/hibernate-annotations-3.3.1.GA/lib/ejb3-persistence.jar"),
            });
        }
        List<DotClassPathEntry> sources = Arrays.asList(new DotClassPathEntry[]{
            EclipseProjectTestUtils.createDotClassPathEntry(
                    "kind", "src",
                    "path", "src"),
            EclipseProjectTestUtils.createDotClassPathEntry(
                    "kind", "src",
                    "path", "test"),
        });
        DotClassPathEntry output = null;
        DotClassPathEntry jre = null;
        DotClassPath dcp = new DotClassPath(classpath, sources, output, jre);
        File f = new File(proj, "eclipse-"+name);
        f.mkdir();
        new File(f,"src").mkdir();
        if (version == 2) {
            File lib = new File(f,"web/WEB-INF/lib");
            lib.mkdirs();
            new File(lib, "a.jar").createNewFile();
            new File(f,"web/WEB-INF/classes").mkdir();
        }
        EclipseProject ep = EclipseProjectTestUtils.createEclipseProject(f, dcp, w, name);
        return ep;
    }
    
    public void testReplaceContainerEntry() throws IOException {
        File w = new File(getWorkDir(), "workspace");
        w.mkdir();
        Workspace workspace = EclipseProjectTestUtils.createWorkspace(w);
        EclipseProject eclipse1 = getTestableProject(1, getWorkDir(), workspace, "projA");
        EclipseProject eclipse2 = getTestableProject(2, getWorkDir(), workspace, "projB");
        List<DotClassPathEntry> l = ClassPathContainerResolver.replaceContainerEntry(eclipse1, workspace, eclipse1.getClassPathEntries().get(1), new ArrayList<String>());
        assertNotNull(l);
        assertEquals(2, l.size());
        assertEquals(new File(eclipse2.getDirectory(), "web/WEB-INF/lib/a.jar").getPath(), l.get(0).getRawPath());
        assertEquals(new File(eclipse2.getDirectory(), "web/WEB-INF/classes").getPath(), l.get(1).getRawPath());
    }
    
}
