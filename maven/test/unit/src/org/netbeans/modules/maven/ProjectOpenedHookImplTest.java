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

package org.netbeans.modules.maven;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;

public class ProjectOpenedHookImplTest extends NbTestCase {

    public ProjectOpenedHookImplTest(String name) {
        super(name);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    @Override protected String logRoot() {
        return "org.netbeans.modules.maven";
    }

    /* XXX
    public void testDoubleOpen() throws Exception { // #200500
        final AtomicInteger openCount = new AtomicInteger();
        ProjectOpenedHookImpl.USG_LOGGER.addHandler(new Handler() {
            @Override public void publish(LogRecord record) {
                openCount.incrementAndGet();
            }
            @Override public void flush() {}
            @Override public void close() throws SecurityException {}
        });
        TestFileUtils.writeFile(d, "pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
                "<groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "</project>");
        Method projectOpened = ProjectOpenedHook.class.getDeclaredMethod("projectOpened");
        projectOpened.setAccessible(true);
        Method projectClosed = ProjectOpenedHook.class.getDeclaredMethod("projectClosed");
        projectClosed.setAccessible(true);
        for (ProjectOpenedHook hook : ProjectManager.getDefault().findProject(d).getLookup().lookupAll(ProjectOpenedHook.class)) {
            projectOpened.invoke(hook);
        }
        for (ProjectOpenedHook hook : ProjectManager.getDefault().findProject(d).getLookup().lookupAll(ProjectOpenedHook.class)) {
            projectClosed.invoke(hook);
        }
        assertEquals(1, openCount.get());
    }
    */

    public void testGeneratedSources() throws Exception { // #187595
        FileObject p = d.createFolder("p");
        TestFileUtils.writeFile(p,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "<build>" +
                "<plugins>" +
                "<plugin>" +
                "<groupId>org.codehaus.mojo</groupId>" +
                "<artifactId>build-helper-maven-plugin</artifactId>" +
                "<version>1.2</version>" +
                "<executions>" +
                "<execution>" +
                "<id>add-src</id>" +
                "<phase>generate-sources</phase>" +
                "<goals>" +
                "<goal>add-source</goal>" +
                "</goals>" +
                "<configuration>" +
                "<sources>" +
                "<source>../src</source>" +
                "</sources>" +
                "</configuration>" +
                "</execution>" +
                "<execution>" +
                "<id>add-test-src</id>" +
                "<phase>generate-test-sources</phase>" +
                "<goals>" +
                "<goal>add-test-source</goal>" +
                "</goals>" +
                "<configuration>" +
                "<sources>" +
                "<source>../tsrc</source>" +
                "</sources>" +
                "</configuration>" +
                "</execution>" +
                "</executions>" +
                "</plugin>" +
                "</plugins>" +
                "</build>" +
                "</project>");
        FileObject src = d.createFolder("src");
        FileObject tsrc = d.createFolder("tsrc");
        Project prj = ProjectManager.getDefault().findProject(p);
        ProjectOpenedHookImpl pohi = new ProjectOpenedHookImpl((NbMavenProjectImpl) prj);
        pohi.projectOpened();
        try {
            assertEquals(prj, FileOwnerQuery.getOwner(src));
            assertEquals(prj, FileOwnerQuery.getOwner(tsrc));
        } finally {
            pohi.projectClosed();
        }
    }

    public void testRegistrationOfSubmodules() throws Exception { // #200445
        TestFileUtils.writeFile(d, "pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
                "<groupId>g</groupId><artifactId>p</artifactId><version>0</version>" +
                "<packaging>pom</packaging><profiles><profile><id>special</id><modules><module>p2</module></modules></profile></profiles>" +
                "</project>");
        TestFileUtils.writeFile(d, "p2/pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
                "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent><artifactId>p2</artifactId>" +
                "<packaging>pom</packaging><modules><module>m</module></modules>" +
                "</project>");
        TestFileUtils.writeFile(d, "p2/m/pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
                "<groupId>g</groupId><properties><my.name>m</my.name></properties><artifactId>${my.name}</artifactId><version>0</version>" +
                "</project>");
        Project p = ProjectManager.getDefault().findProject(d);
        ProjectOpenedHookImpl pohi = new ProjectOpenedHookImpl((NbMavenProjectImpl) p);
        pohi.projectOpened();
        try {
            File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
            File mArt = new File(repo, "g/m/0/m-0.jar");
            // XXX verify that p2 has not yet been loaded
            Project m = FileOwnerQuery.getOwner(Utilities.toURI(mArt));
            assertNotNull(m);
            assertEquals(d.getFileObject("p2/m"), m.getProjectDirectory());
            File p2Art = new File(repo, "g/p2/0/p2-0.pom");
            Project p2 = FileOwnerQuery.getOwner(Utilities.toURI(p2Art));
            assertNotNull(p2);
            assertEquals(d.getFileObject("p2"), p2.getProjectDirectory());
        } finally {
            pohi.projectClosed();
        }
    }

}
