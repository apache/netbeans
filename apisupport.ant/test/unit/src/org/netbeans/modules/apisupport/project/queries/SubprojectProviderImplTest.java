/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Test subprojects.
 * @author Jesse Glick
 */
public class SubprojectProviderImplTest extends TestBase {
    
    public SubprojectProviderImplTest(String name) {
        super(name);
    }

    /* XXX too brittle:
    public void testNetBeansOrgSubprojects() throws Exception {
        checkSubprojects("o.apache.tools.ant.module", new String[] {
            "openide.filesystems",
            "openide.util",
            "openide.modules",
            "openide.nodes",
            "openide.awt",
            "openide.dialogs",
            "openide.windows",
            "openide.text",
            "openide.actions",
            "openide.execution",
            "openide.io",
            "openide.loaders",
            "api.xml",
            "spi.navigator",
            "openide.explorer",
            "options.api",
            "o.jdesktop.layout",
            "api.progress",
            "projectapi",
            "projectuiapi",
        });
        checkSubprojects("openide.util", new String[] {});
    }
     */
    
    public void testExternalSubprojects() throws Exception {
        checkSubprojects(resolveEEPPath("/suite1/action-project"), new String[] {
            resolveEEPPath("/suite1/support/lib-project"),
            file("openide.dialogs").getAbsolutePath(),
        });
        checkSubprojects(resolveEEPPath("/suite1/support/lib-project"), new String[0]);
        // No sources for beans available, so no subprojects reported:
        checkSubprojects(resolveEEPPath("/suite3/dummy-project"), new String[0]);
    }
    
    /** @see "#63824" */
    /* No examples in nb.org left; should create sample projects for it:
    public void testAdHocSubprojects() throws Exception {
        assertDepends("mdr/module", "mdr");
        assertDepends("applemenu", "applemenu/eawtstub");
    }
    */
    
    /** @see "#77533" */
    public void testSelfRefWithClassPathExts() throws Exception {
        checkSubprojects("apisupport.paintapp/PaintApp-suite/ColorChooser", new String[0]);
    }
    
    /** @see "#81878" */
    public void testInclusionOfHigherBin() throws Exception {
        checkSubprojects("servletapi", new String[0]);
    }

    public void testInclusionOfUnresolvedRef() throws Exception {
        clearWorkDir();
        initializeBuildProperties(getWorkDir(), null);
        NbModuleProject p = generateStandaloneModule("prj");
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put("cp.extra", "${unknown.jar}");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        checkSubprojects(p);
    }

    @Deprecated // relies on nb_all source root
    private void checkSubprojects(String project, String[] subprojects) throws Exception {
        Project p = project(project);
        SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have SPP in " + p, spp);
        SortedSet<String> expected = new TreeSet<String>();
        for (String sp : subprojects) {
            File f = new File(sp);
            if (!f.isAbsolute()) {
                f = file(sp);
            }
            expected.add(Utilities.toURI(f).toString());
        }
        SortedSet<String> actual = new TreeSet<String>();
        for (Project sp : spp.getSubprojects()) {
            actual.add(sp.getProjectDirectory().toURL().toExternalForm());
        }
        assertEquals("correct subprojects for " + project, expected.toString(), actual.toString());
    }

    private void checkSubprojects(Project project, String... subprojectNames) throws Exception {
        SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have SPP in " + project, spp);
        SortedSet<String> actual = new TreeSet<String>();
        for (Project sp : spp.getSubprojects()) {
            actual.add(ProjectUtils.getInformation(sp).getName());
        }
        assertEquals("correct subprojects for " + project, new TreeSet<String>(Arrays.asList(subprojectNames)).toString(), actual.toString());
    }

    private Project project(String path) throws Exception {
        FileObject dir = FileUtil.toFileObject(PropertyUtils.resolveFile(nbRootFile(), path));
//        FileObject dir = nbRoot().getFileObject(path);
        assertNotNull("have " + path, dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        assertNotNull("have project in " + path, p);
        return p;
    }
    
    private void assertDepends(String parent, String child) throws Exception {
        Project p1 = project(parent);
        Project p2 = project(child);
        SubprojectProvider spp = p1.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("have SPP in " + p1, spp);
        assertTrue(parent + " includes " + child, spp.getSubprojects().contains(p2));
    }
    
}
