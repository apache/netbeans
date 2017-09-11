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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.maven;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider.Association;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;

public class HudsonProviderImplTest extends NbTestCase {

    public HudsonProviderImplTest(String n) {
        super(n);
    }

    private Project project(String pomXml) throws Exception {
        clearWorkDir();
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml", pomXml);
        return ProjectManager.getDefault().findProject(d);
    }

    public void testFindAssociation1() throws Exception {
        final FileObject d = FileUtil.toFileObject(getWorkDir());
        assertNull(new HudsonProviderImpl().findAssociation(new Project() {
            public @Override FileObject getProjectDirectory() {
                return d;
            }
            public @Override Lookup getLookup() {
                return Lookup.EMPTY;
            }
        }));
    }

    private static final String BASIC_PROJECT_START = "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion><groupId>grp</groupId><artifactId>art</artifactId><version>1.0</version>";
    private static final String BASIC_PROJECT_END = "</project>";

    public void testFindAssociation2() throws Exception {
        assertNull(new HudsonProviderImpl().findAssociation(project(BASIC_PROJECT_START + BASIC_PROJECT_END)));
    }

    public void testFindAssociation3() throws Exception {
        Association a = new HudsonProviderImpl().findAssociation(project(BASIC_PROJECT_START + "<ciManagement><system>hudson</system><url>https://hudson.geomatys.fr/job/GeoAPI/</url></ciManagement>" + BASIC_PROJECT_END));
        assertNotNull(a);
        assertEquals("https://hudson.geomatys.fr/", a.getServerUrl());
        assertEquals("GeoAPI", a.getJobName());
    }

    public void testFindAssociation4() throws Exception {
        Association a = new HudsonProviderImpl().findAssociation(project(BASIC_PROJECT_START + "<ciManagement><system>Jenkins</system><url>https://builds.apache.org/hudson/job/maven-plugins/</url></ciManagement>" + BASIC_PROJECT_END));
        assertNotNull(a);
        assertEquals("https://builds.apache.org/hudson/", a.getServerUrl());
        assertEquals("maven-plugins", a.getJobName());
    }

    public void testRecordAssociation() throws Exception {
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");        
        Project p = project(BASIC_PROJECT_START + BASIC_PROJECT_END);
        assertTrue(new HudsonProviderImpl().recordAssociation(p, new Association("http://nowhere.net/", "foo bar")));
        assertEquals(BASIC_PROJECT_START + " <ciManagement> <system>hudson</system> <url>http://nowhere.net/job/foo%20bar/</url> </ciManagement> " + BASIC_PROJECT_END,
                p.getProjectDirectory().getFileObject("pom.xml").asText().replaceAll("\\s+", " "));
        NbMavenProject.fireMavenProjectReload(p);
        assertEquals("http://nowhere.net/job/foo%20bar/", String.valueOf(new HudsonProviderImpl().findAssociation(p)));
    }

}
