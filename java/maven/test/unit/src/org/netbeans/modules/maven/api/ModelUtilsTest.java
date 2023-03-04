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

package org.netbeans.modules.maven.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.ModelUtils.Descriptor;
import org.netbeans.modules.maven.api.ModelUtils.LibraryDescriptor;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Repository;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

/**
 *
 * @author mkleint
 */
public class ModelUtilsTest extends NbTestCase {
    
    public ModelUtilsTest(String testName) {
        super(testName);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testCheckLibrary() throws MalformedURLException {
        URL pom = new URL("https://repo1.maven.org/maven2/junit/junit/3.8.2/junit-3.8.2.pom");
        ModelUtils.LibraryDescriptor result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("https://repo1.maven.org/maven2/", result.getRepoRoot());
        assertEquals("junit", result.getGroupId());
        assertEquals("junit", result.getArtifactId());
        assertEquals("3.8.2", result.getVersion());
        pom = new URL("http://download.java.net/maven/1/toplink.essentials/poms/toplink-essentials-agent-2.0-36.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("legacy", result.getRepoType());
        assertEquals("http://download.java.net/maven/1/", result.getRepoRoot());
        assertEquals("toplink.essentials", result.getGroupId());
        assertEquals("toplink-essentials-agent", result.getArtifactId());
        assertEquals("2.0-36", result.getVersion());

        pom = new URL("http://download.java.net/maven/1/javax.jws/poms/jsr181-api-1.0-MR1.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("legacy", result.getRepoType());
        assertEquals("http://download.java.net/maven/1/", result.getRepoRoot());
        assertEquals("javax.jws", result.getGroupId());
        assertEquals("jsr181-api", result.getArtifactId());
        assertEquals("1.0-MR1", result.getVersion());


        pom = new URL("https://repo1.maven.org/maven2/org/codehaus/mevenide/netbeans-deploy-plugin/1.2.3/netbeans-deploy-plugin-1.2.3.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("https://repo1.maven.org/maven2/", result.getRepoRoot());
        assertEquals("org.codehaus.mevenide", result.getGroupId());
        assertEquals("netbeans-deploy-plugin", result.getArtifactId());
        assertEquals("1.2.3", result.getVersion());
        
        pom = new URL("http://repository.jboss.org/maven2/junit/junit/3.8.2/junit-3.8.2.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://repository.jboss.org/maven2/", result.getRepoRoot());
        assertEquals("junit", result.getGroupId());
        assertEquals("junit", result.getArtifactId());
        assertEquals("3.8.2", result.getVersion());


        pom = new URL("https://repo1.maven.org/maven2/org/testng/testng/5.8/testng-5.8.pom#jdk15");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("https://repo1.maven.org/maven2/", result.getRepoRoot());
        assertEquals("org.testng", result.getGroupId());
        assertEquals("testng", result.getArtifactId());
        assertEquals("5.8", result.getVersion());
        assertEquals("jdk15", result.getClassifier());



        pom = new URL("http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo/org/eclipse/persistence/javax.persistence/2.0.0-M12/javax.persistence-2.0.0-M12.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo/", result.getRepoRoot());
        assertEquals("org.eclipse.persistence", result.getGroupId());
        assertEquals("javax.persistence", result.getArtifactId());
        assertEquals("2.0.0-M12", result.getVersion());

        pom = new URL("http://download.java.net/maven/glassfish/org/glassfish/extras/glassfish-embedded-all/3.0/glassfish-embedded-all-3.0.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://download.java.net/maven/glassfish/", result.getRepoRoot());
        assertEquals("org.glassfish.extras", result.getGroupId());
        assertEquals("glassfish-embedded-all", result.getArtifactId());
        assertEquals("3.0", result.getVersion());
        
        pom = new URL("http://download.eclipse.org/rt/eclipselink/maven.repo/org/eclipse/persistence/eclipselink/2.3.0/eclipselink-2.3.0.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://download.eclipse.org/rt/eclipselink/maven.repo/", result.getRepoRoot());
        assertEquals("org.eclipse.persistence", result.getGroupId());
        assertEquals("eclipselink", result.getArtifactId());
        assertEquals("2.3.0", result.getVersion());

        pom = new URL("https://repository.jboss.org/nexus/content/groups/public-jboss/org/richfaces/ui/richfaces-components-ui/4.0.0.Final/richfaces-components-ui-4.0.0.Final.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("https://repository.jboss.org/nexus/content/groups/public-jboss/", result.getRepoRoot());
        assertEquals("org.richfaces.ui", result.getGroupId());
        assertEquals("richfaces-components-ui", result.getArtifactId());
        assertEquals("4.0.0.Final", result.getVersion());
    }

    public void testAddModelRepository() throws Exception { // #212336
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "</project>\n");
        final MavenProject mp = ProjectManager.getDefault().findProject(pom.getParent()).getLookup().lookup(NbMavenProject.class).getMavenProject();
        Utilities.performPOMModelOperations(pom, Collections.singletonList(new ModelOperation<POMModel>() {
            @Override public void performOperation(POMModel model) {
                Repository added = ModelUtils.addModelRepository(mp, model, "https://repo1.maven.org/maven2/");
                assertNull(added);
                added = ModelUtils.addModelRepository(mp, model, "http://nowhere.net/maven2/");
                assertNotNull(added);
                added.setId("nowhere.net");
                added = ModelUtils.addModelRepository(mp, model, "http://nowhere.net/maven2/");
                assertNull(added);
            }
        }));
        assertEquals("<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "    <repositories>\n"
                + "        <repository>\n"
                + "            <url>http://nowhere.net/maven2/</url>\n"
                // XXX would be nice to fix IdPOMComponentImpl to put <id> first
                + "            <id>nowhere.net</id>\n"
                + "        </repository>\n"
                + "    </repositories>\n"
                + "</project>\n",
                pom.asText().replace("\r\n", "\n"));
    }
    
    
    public void testCheckLibraries() throws Exception {
        Map<String, String> props = new HashMap<String, String>();
        props.put(ModelUtils.LIBRARY_PROP_DEPENDENCIES, "a:b:1:jar b:a:2:jar\n  c:d:3:jar\t\t\td:f:4:jar");
        Descriptor res = ModelUtils.checkLibraries(props);
        assertNotNull(res);
        List<LibraryDescriptor> deps = res.getDependencies();
        assertEquals(Arrays.toString(deps.toArray()), 4, deps.size());
        for (ModelUtils.LibraryDescriptor d : deps) {
            assertEquals("jar", d.getType());
            assertTrue("abcd".contains(d.getGroupId()));
            assertTrue("abdf".contains(d.getArtifactId()));
            assertTrue("1234".contains(d.getVersion()));
        }
    }
}
