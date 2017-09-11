/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.api;

import java.io.File;
import java.util.Arrays;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class PluginPropertyUtilsTest extends NbTestCase {

    public PluginPropertyUtilsTest(String name) {
        super(name);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testGetPluginPropertyEvaluated() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><directory>${project.basedir}/build/maven/${project.artifactId}/target</directory>" +
                "<plugins>" +
                "<plugin><groupId>g</groupId><artifactId>p</artifactId><version>0</version><configuration><key>${project.reporting.outputDirectory}/stuff</key></configuration></plugin>" +
                "</plugins></build>" +
                "</project>");
        assertEquals(new File(getWorkDir(), "build/maven/a/target/site/stuff"), new File(PluginPropertyUtils.getPluginProperty(ProjectManager.getDefault().findProject(d), "g", "p", "key", null)));
    }

    public void testGetPluginPropertyNotString() throws Exception { // #207098
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><directory>${project.basedir}/build/maven/${project.artifactId}/target</directory>" +
                "<plugins>" +
                "<plugin><groupId>g</groupId><artifactId>p</artifactId><version>0</version><configuration><key/></configuration></plugin>" +
                "</plugins></build>" +
                "</project>");
        assertNull(null, PluginPropertyUtils.getPluginProperty(ProjectManager.getDefault().findProject(d), "g", "p", "key", null));
    }

    public void testGetReportPluginVersionM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>17</version></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("17", PluginPropertyUtils.getReportPluginVersion(ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class).getMavenProject(), "g", "r"));
    }

    public void testGetReportPluginVersionM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>17</version></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("17", PluginPropertyUtils.getReportPluginVersion(ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class).getMavenProject(), "g", "r"));
    }

    public void testGetReportPluginPropertyM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><key>value</key></configuration></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("value", PluginPropertyUtils.getReportPluginProperty(ProjectManager.getDefault().findProject(d), "g", "r", "key", null));
    }

    public void testGetReportPluginPropertyM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><key>value</key></configuration></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("value", PluginPropertyUtils.getReportPluginProperty(ProjectManager.getDefault().findProject(d), "g", "r", "key", null));
    }

    public void testGetReportPluginPropertyListM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><things><thing>one</thing><thing>two</thing></things></configuration></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("[one, two]", Arrays.toString(PluginPropertyUtils.getReportPluginPropertyList(ProjectManager.getDefault().findProject(d), "g", "r", "things", "thing", null)));
    }

    public void testGetReportPluginPropertyListM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><things><thing>one</thing><thing>two</thing></things></configuration></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("[one, two]", Arrays.toString(PluginPropertyUtils.getReportPluginPropertyList(ProjectManager.getDefault().findProject(d), "g", "r", "things", "thing", null)));
    }

}
