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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.embedder;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.project.MavenProject;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbPreferences;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

public class EmbedderFactoryTest extends NbTestCase {
    
    public EmbedderFactoryTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testCreateModelLineage() throws Exception {
        File pom = TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
            "<modelVersion>4.0.0</modelVersion>" +
            "<groupId>grp</groupId>" +
            "<artifactId>art</artifactId>" +
            "<packaging>jar</packaging>" +
            "<version>1.0-SNAPSHOT</version>" +
            "</project>");
        List<Model> lineage = EmbedderFactory.createProjectLikeEmbedder().createModelLineage(pom);
        assertEquals(/* second is inherited master POM */2, lineage.size());
        assertEquals("grp:art:jar:1.0-SNAPSHOT", lineage.get(0).getId());
        // #195295: JDK activation
        pom = TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
            "<modelVersion>4.0.0</modelVersion>" +
            "<groupId>grp</groupId>" +
            "<artifactId>art2</artifactId>" +
            "<packaging>jar</packaging>" +
            "<version>1.0-SNAPSHOT</version>" +
            "<profiles>" +
            "<profile>" +
            "<id>jdk15</id>" +
            "<activation>" +
            "<jdk>1.5</jdk>" +
            "</activation>" +
            "</profile>" +
            "</profiles>" +
            "</project>");
        lineage = EmbedderFactory.createProjectLikeEmbedder().createModelLineage(pom);
        assertEquals(2, lineage.size());
        assertEquals("grp:art2:jar:1.0-SNAPSHOT", lineage.get(0).getId());
        assertEquals(1, lineage.get(0).getProfiles().size());
        // #197288: groupId and version can be inherited from parents
        TestFileUtils.writeFile(new File(getWorkDir(), "parent.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
            "<modelVersion>4.0.0</modelVersion>" +
            "<groupId>grp</groupId>" +
            "<artifactId>parent</artifactId>" +
            "<version>1.0</version>" +
            "<packaging>pom</packaging>" +
            "</project>");
        pom = TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
            "<modelVersion>4.0.0</modelVersion>" +
            "<parent>" +
            "<relativePath>parent.xml</relativePath>" +
            "<groupId>grp</groupId>" +
            "<artifactId>parent</artifactId>" +
            "<version>1.0</version>" +
            "</parent>" +
            "<artifactId>art3</artifactId>" +
            "</project>");
        lineage = EmbedderFactory.createProjectLikeEmbedder().createModelLineage(pom);
        assertEquals(3, lineage.size());
        assertEquals("[inherited]:art3:jar:[inherited]", lineage.get(0).getId());
        assertEquals("grp", lineage.get(0).getParent().getGroupId());
        assertEquals("1.0", lineage.get(0).getParent().getVersion());
        assertEquals("grp:parent:pom:1.0", lineage.get(1).getId());
    }

    public void testInvalidRepositoryException() throws Exception { // #197831
        File pom = TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
            "<modelVersion>4.0.0</modelVersion>" +
            "<groupId>grp</groupId>" +
            "<artifactId>art</artifactId>" +
            "<packaging>jar</packaging>" +
            "<version>1.0-SNAPSHOT</version>" +
            "<repositories><repository><url>http://nowhere.net/</url></repository></repositories>" +
            "</project>");
        try {
            EmbedderFactory.createProjectLikeEmbedder().createModelLineage(pom);
            fail();
        } catch (ModelBuildingException x) {
            // right
        }
    }

    public void testArtifactFixer() throws Exception { // #197669
        File main = TestFileUtils.writeFile(new File(getWorkDir(), "main/pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
            "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent>" +
            "<artifactId>m</artifactId>" +
            "<dependencies><dependency><groupId>g</groupId><artifactId>s</artifactId><version>0</version></dependency></dependencies>" +
            "</project>");
        final File parent = TestFileUtils.writeFile(new File(getWorkDir(), "parent/pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
            "<groupId>g</groupId><artifactId>p</artifactId><version>0</version>" +
            "<packaging>pom</packaging>" +
            "<properties><k>v</k></properties>" +
            "</project>");
        final File sibling = TestFileUtils.writeFile(new File(getWorkDir(), "sib/pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
            "<groupId>g</groupId><artifactId>s</artifactId><version>0</version>" +
            "<dependencies><dependency><groupId>g</groupId><artifactId>b</artifactId><version>0</version></dependency></dependencies>" +
            "</project>");
        final File binary = TestFileUtils.writeZipFile(new File(getWorkDir(), "b.jar"), "g/r:stuff");
        MockLookup.setInstances(new ArtifactFixer() {
            @Override public File resolve(org.eclipse.aether.artifact.Artifact artifact) {
                String id = artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getExtension() + ':' + artifact.getVersion();
                if (id.equals("g:p:pom:0")) {
                    return parent;
                } else if (id.equals("g:s:pom:0")) {
                    return sibling;
                } else if (id.equals("g:b:jar:0")) {
                    return binary;
                } else {
                    return null;
                }
            }
        });
        MavenEmbedder e = EmbedderFactory.getProjectEmbedder();
        MavenExecutionRequest req = e.createMavenExecutionRequest();
        req.setPom(main);
        req.setOffline(true);
        MavenExecutionResult res = e.readProjectWithDependencies(req, true);
        assertEquals(Collections.emptyList(), res.getExceptions());
        MavenProject prj = res.getProject();
        assertEquals("v", prj.getProperties().getProperty("k"));
        assertEquals("[g:b:jar:0:compile, g:s:jar:0:compile]", new TreeSet<Artifact>(prj.getArtifacts()).toString());
    }

    public void testSystemProperties() throws Exception {
        Properties p = EmbedderFactory.getProjectEmbedder().getSystemProperties();
        assertEquals(System.getProperty("java.home"), p.getProperty("java.home"));
        assertEquals(System.getenv("PATH"), p.getProperty("env.PATH"));
        // XXX perhaps -Dkey=value (and -Dkey) should be honored in "Global Execution Options"?
    }
    
    public void testCustomProperties() throws Exception {
        Preferences node = NbPreferences.root().node("org/netbeans/modules/maven");
        node.put(EmbedderFactory.PROP_DEFAULT_OPTIONS, "--offline -drep -Dmilos=great -Dme=you ");
        Map<String, String> props = EmbedderFactory.getCustomGlobalUserProperties();
        assertEquals(2, props.size());
        assertEquals("great", props.get("milos"));
        assertEquals("you", props.get("me"));
    }

}
