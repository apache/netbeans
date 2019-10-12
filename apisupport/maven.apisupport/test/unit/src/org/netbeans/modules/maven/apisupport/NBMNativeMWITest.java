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
package org.netbeans.modules.maven.apisupport;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class NBMNativeMWITest extends NbTestCase {

    private FileObject wd;

    public NBMNativeMWITest(String testName) {
        super(testName);
    }

    protected @Override
    Level logLevel() {
        return Level.FINE;
    }

    @Test
    public void testPathNoParent() throws IOException, XmlPullParserException {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        FileObject createData = wd.createFolder("test1");
        ProjectInfo pi = new ProjectInfo("my.groupid", "artefact1", "1.0", "my.packagename");
        NBMNativeMWI.instantiate(pi, FileUtil.toFile(createData), "RELEASE110", true, null);

        FileObject builtpom = createData.getFileObject("pom", "xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(FileUtil.toFile(builtpom)));

        assertEquals("nbm-maven-plugin", model.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION, model.getBuild().getPlugins().get(0).getVersion());
        assertEquals("maven-compiler-plugin", model.getBuild().getPlugins().get(1).getArtifactId());
        assertEquals("3.8.1", model.getBuild().getPlugins().get(1).getVersion());
        assertEquals(0, model.getRepositories().size());
    }

    @Test
    public void testPathNoParentSnapshot() throws IOException, XmlPullParserException {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        FileObject createData = wd.createFolder("test1");
        ProjectInfo pi = new ProjectInfo("my.groupid", "artefact1", "1.0", "my.packagename");
        NBMNativeMWI.instantiate(pi, FileUtil.toFile(createData), "dev-SNAPSHOT", true, null);
        FileObject builtpom = createData.getFileObject("pom", "xml");

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(FileUtil.toFile(builtpom)));
        assertEquals("nbm-maven-plugin", model.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION, model.getBuild().getPlugins().get(0).getVersion());
        assertEquals("maven-compiler-plugin", model.getBuild().getPlugins().get(1).getArtifactId());
        assertEquals("3.8.1", model.getBuild().getPlugins().get(1).getVersion());
        assertEquals(1, model.getRepositories().size());
    }

    @Test
    public void testPathParent() throws IOException, XmlPullParserException {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        FileObject createDatap = wd.createFolder("testp");
        FileObject parentpomFile = createDatap.createData("pom", "xml");

        FileObject createData = wd.createFolder("test2");
        ProjectInfo pi = new ProjectInfo("my.groupid", "artefact1", "1.0", "my.packagename");
        MavenProject mp = new MavenProject();
        mp.setVersion("3");
        mp.setGroupId("mm");
        mp.setArtifactId("aaa");
        mp.setFile(FileUtil.toFile(parentpomFile));
        NBMNativeMWI.instantiate(pi, FileUtil.toFile(createData), "RELEASE110", true, mp);

        MavenXpp3Reader reader = new MavenXpp3Reader();
        FileObject builtpom = createData.getFileObject("pom", "xml");
        Model model = reader.read(new FileReader(FileUtil.toFile(builtpom)));

        assertEquals("nbm-maven-plugin", model.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION, model.getBuild().getPlugins().get(0).getVersion());
        assertEquals("maven-compiler-plugin", model.getBuild().getPlugins().get(1).getArtifactId());
        assertEquals("3.8.1", model.getBuild().getPlugins().get(1).getVersion());
        assertEquals(0, model.getRepositories().size());
    }

    @Test
    public void testPathParentSnapshot() throws IOException, XmlPullParserException {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        FileObject createDatap = wd.createFolder("testp");
        FileObject parentpomFile = createDatap.createData("pom", "xml");
        FileObject createData = wd.createFolder("test2");
        ProjectInfo pi = new ProjectInfo("my.groupid", "artefact1", "1.0", "my.packagename");
        MavenProject mp = new MavenProject();
        mp.setVersion("3");
        mp.setGroupId("mm");
        mp.setArtifactId("aaa");
        mp.setFile(FileUtil.toFile(parentpomFile));
        NBMNativeMWI.instantiate(pi, FileUtil.toFile(createData), "dev-SNAPSHOT", true, mp);

        MavenXpp3Reader reader = new MavenXpp3Reader();
        FileObject builtpom = createData.getFileObject("pom", "xml");
        Model model = reader.read(new FileReader(FileUtil.toFile(builtpom)));

        assertEquals("nbm-maven-plugin", model.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION, model.getBuild().getPlugins().get(0).getVersion());
        assertEquals("maven-compiler-plugin", model.getBuild().getPlugins().get(1).getArtifactId());
        assertEquals("3.8.1", model.getBuild().getPlugins().get(1).getVersion());
        assertEquals(1, model.getRepositories().size());
    }

    @Test
    public void testPathParentCompiler() throws IOException, XmlPullParserException {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        FileObject createDatap = wd.createFolder("testp");
        FileObject parentpomFile = createDatap.createData("pom", "xml");
        try (OutputStream os = parentpomFile.getOutputStream()) {
            os.write(POMCOMPILER.getBytes("UTF-8"));
        }
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(FileUtil.toFile(parentpomFile)));
        FileObject createData = wd.createFolder("test2");
        ProjectInfo pi = new ProjectInfo("my.groupid", "artefact1", "1.0", "my.packagename");
        MavenProject mp = new MavenProject(model);
        mp.setFile(FileUtil.toFile(parentpomFile));
        NBMNativeMWI.instantiate(pi, FileUtil.toFile(createData), "RELEASE110", true, mp);

        MavenXpp3Reader readeroutput = new MavenXpp3Reader();
        FileObject builtpom = createData.getFileObject("pom", "xml");
        Model modeloutput = readeroutput.read(new FileReader(FileUtil.toFile(builtpom)));

        assertEquals("nbm-maven-plugin", modeloutput.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION, modeloutput.getBuild().getPlugins().get(0).getVersion());
        assertEquals("maven-compiler-plugin", modeloutput.getBuild().getPlugins().get(1).getArtifactId());
        assertEquals(null, modeloutput.getBuild().getPlugins().get(1).getVersion());
        assertEquals(0, model.getRepositories().size());
    }

    @Test
    public void testPathParentJar() throws IOException, XmlPullParserException {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        FileObject createDatap = wd.createFolder("testp");
        FileObject parentpomFile = createDatap.createData("pom", "xml");
        try (OutputStream os = parentpomFile.getOutputStream()) {
            os.write(POMJAR.getBytes("UTF-8"));
        }
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(FileUtil.toFile(parentpomFile)));
        FileObject createData = wd.createFolder("test2");
        ProjectInfo pi = new ProjectInfo("my.groupid", "artefact1", "1.0", "my.packagename");
        MavenProject mp = new MavenProject(model);
        mp.setFile(FileUtil.toFile(parentpomFile));
        NBMNativeMWI.instantiate(pi, FileUtil.toFile(createData), "RELEASE110", true, mp);
        MavenXpp3Reader readeroutput = new MavenXpp3Reader();
        FileObject builtpom = createData.getFileObject("pom", "xml");
        Model modeloutput = readeroutput.read(new FileReader(FileUtil.toFile(builtpom)));

        assertEquals("nbm-maven-plugin", modeloutput.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION, modeloutput.getBuild().getPlugins().get(0).getVersion());
        assertEquals("maven-compiler-plugin", modeloutput.getBuild().getPlugins().get(1).getArtifactId());
        assertEquals("3.8.1", modeloutput.getBuild().getPlugins().get(1).getVersion());
        assertEquals(0, model.getRepositories().size());
    }

    private String POMCOMPILER
            = "<project>\n"
            + "<modelVersion>4.0.0</modelVersion>"
            + "<build>"
            + "<pluginManagement>"
            + "<plugins>"
            + "<plugin>"
            + "<groupId>org.apache.maven.plugins</groupId>"
            + "<artifactId>maven-compiler-plugin</artifactId>"
            + "<version>3.3.1.1</version>"
            + "</plugin></plugins>"
            + "</pluginManagement>"
            + "</build>"
            + "</project>";

    private String POMJAR
            = "<project>\n"
            + "<modelVersion>4.0.0</modelVersion>"
            + "<build>"
            + "<pluginManagement>"
            + "<plugins>"
            + "<plugin>"
            + "<groupId>org.apache.maven.plugins</groupId>"
            + "<artifactId>maven-jar-plugin</artifactId>"
            + "<version>2.1.0</version>"
            + "</plugin></plugins>"
            + "</pluginManagement>"
            + "</build>"
            + "</project>";
}
