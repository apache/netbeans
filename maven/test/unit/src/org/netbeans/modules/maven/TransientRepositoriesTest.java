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

package org.netbeans.modules.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class TransientRepositoriesTest extends NbTestCase {

    private static final String CENTRAL_ANON = RepositorySystem.DEFAULT_REMOTE_REPO_ID + ":" + RepositorySystem.DEFAULT_REMOTE_REPO_ID + ":" + RepositorySystem.DEFAULT_REMOTE_REPO_URL + /*MNG-5164*/"/";
    /** @see RepositoryPreferences#RepositoryPreferences() */
    private static final String CENTRAL_NAMED = RepositorySystem.DEFAULT_REMOTE_REPO_ID + ":Central Repository:" + RepositorySystem.DEFAULT_REMOTE_REPO_URL + "/";

    public TransientRepositoriesTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("no.local.settings", "true");
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");        
    }

    public void testSimpleRegistration() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "    <repositories>\n"
                + "        <repository>\n"
                + "            <id>stuff</id>\n"
                + "            <name>Stuff</name>\n"
                + "            <url>http://nowhere.net/stuff</url>\n"
                + "        </repository>\n"
                + "    </repositories>\n"
                + "</project>\n");
        NbMavenProject p = ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class);
        TransientRepositories tr = new TransientRepositories(p);
        assertRepos(CENTRAL_ANON);
        tr.register();
        assertRepos("stuff:Stuff:http://nowhere.net/stuff/", CENTRAL_NAMED);
        tr.unregister();
        assertRepos(CENTRAL_ANON);
    }

    public void testListening() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "</project>\n");
        Project p = ProjectManager.getDefault().findProject(d);
        TransientRepositories tr = new TransientRepositories(p.getLookup().lookup(NbMavenProject.class));
        assertRepos(CENTRAL_ANON);
        tr.register();
        assertRepos(CENTRAL_NAMED);
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "    <repositories>\n"
                + "        <repository>\n"
                + "            <id>stuff</id>\n"
                + "            <name>Stuff</name>\n"
                + "            <url>http://nowhere.net/stuff</url>\n"
                + "        </repository>\n"
                + "    </repositories>\n"
                + "</project>\n");
        NbMavenProject.fireMavenProjectReload(p);
        assertRepos("stuff:Stuff:http://nowhere.net/stuff/", CENTRAL_NAMED);
        tr.unregister();
        assertRepos(CENTRAL_ANON);
    }

    // XXX test mirrors; current code mistakenly suppresses <name> of a mirrored repo when mirrored 1-1

    private void assertRepos(String... expected) {
        List<String> actual = new ArrayList<String>();
        for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
            if (info.isLocal()) {
                continue;
            }
            actual.add(info.getId() + ":" + info.getName() + ":" + info.getRepositoryUrl()); // XXX add mirrored repos too?
        }
        assertEquals(Arrays.toString(expected), actual.toString());
    }

}
