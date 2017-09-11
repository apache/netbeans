/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
