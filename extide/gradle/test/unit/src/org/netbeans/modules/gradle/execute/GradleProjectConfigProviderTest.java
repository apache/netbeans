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
package org.netbeans.modules.gradle.execute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author sdedic
 */
public class GradleProjectConfigProviderTest {
    private FileObject projectFolder;
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @After
    public void cleanUp() {
        mappingURL = null;
    }
    
    private File dataFile(String fn) throws IOException, URISyntaxException {
        URL u = getClass().getResource(fn);
        return new File(u.toURI());
    }
    
    private void createGradleProject() throws IOException {
        projectFolder = FileUtil.toFileObject(tempFolder.newFolder());
        TestFileUtils.writeFile(projectFolder, "build.gradle",
                "apply plugin: 'java'\n");
    }
    
    private Element file2Element(File f, String elementName, String namespace) throws IOException, SAXException {
        try (InputStream is = new FileInputStream(f)) {
            InputSource input = new InputSource(is);
            input.setSystemId(f.toURL().toString());
            Element root = XMLUtil.parse(input, false, true, /*XXX*/null, null).getDocumentElement();
            return XMLUtil.findElement(root, elementName, namespace);
        }
    }
    
    @Test
    public void testDefaultConfigurationExists() throws Exception {
        createGradleProject();
        Project p = ProjectManager.getDefault().findProject(projectFolder);
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        
        assertNotNull(pcp);
        Collection<? extends GradleExecConfiguration> configs = pcp.getConfigurations();
        assertEquals(1, configs.size());
        
        GradleExecConfiguration c = configs.iterator().next();
        ConfigPersistenceUtilsTest.assertConfiguration(c, GradleExecAccessor.createDefault());
    }
    
    @Test
    public void testSharedConfigurations() throws Exception {
        createGradleProject();
        File conf = dataFile("writeConfigurationsWithActive-shared.pass.xml");
        TestFileUtils.writeFile(projectFolder, "nb-configuration.xml",
                String.join("\n", Files.readAllLines(conf.toPath()))
        );
        Project p = ProjectManager.getDefault().findProject(projectFolder);
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        assertNotNull(pcp);
        Collection<? extends GradleExecConfiguration> configs = pcp.getConfigurations();
        assertEquals(3, configs.size());
        
        // the default must come first:
        assertEquals(GradleExecAccessor.createDefault(), configs.iterator().next());
    }
    
    @Test
    public void testDefaultConfigMergesFirst() throws Exception {
        createGradleProject();
        File conf = dataFile("configs-wthout-default.xml");
        TestFileUtils.writeFile(projectFolder, "nb-configuration.xml",
                String.join("\n", Files.readAllLines(conf.toPath()))
        );
        Project p = ProjectManager.getDefault().findProject(projectFolder);
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        assertNotNull(pcp);
        Collection<? extends GradleExecConfiguration> configs = pcp.getConfigurations();
        assertEquals(3, configs.size());
        // the default must come first:
        assertEquals(GradleExecAccessor.createDefault(), configs.iterator().next());
    }
    
    @Test
    public void testSetActiveConfiguration() throws Exception {
        createGradleProject();
        File conf = dataFile("configs-wthout-default.xml");
        TestFileUtils.writeFile(projectFolder, "nb-configuration.xml",
                String.join("\n", Files.readAllLines(conf.toPath()))
        );
        Project p = ProjectManager.getDefault().findProject(projectFolder);
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        assertNotNull(pcp);
        List<? extends GradleExecConfiguration> configs = new ArrayList<>(pcp.getConfigurations());
        
        pcp.setActiveConfiguration(configs.get(1));
        
        String s = (String)projectFolder.getAttribute("AuxilaryConfiguration");
        assertNotNull(s);
        
        
        InputSource input = new InputSource(new StringReader(s));
        Element root = XMLUtil.parse(input, false, true, /*XXX*/null, null).getDocumentElement();
        Element configurations = XMLUtil.findElement(root, "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);
        assertNotNull(configurations);
        
        String active = configurations.getAttribute("active");
        assertEquals("custom2", active);
    }
    
    @Test
    public void testProvidedConfigurationsPresent() throws Exception {
        projectFolder = FileUtil.toFileObject(tempFolder.newFolder());
        FileUtil.createFolder(projectFolder, "src/main/java");
        TestFileUtils.writeFile(projectFolder, "build.gradle",
                "apply plugin: 'java'\n"
                            
        );
        Project p = ProjectManager.getDefault().findProject(projectFolder);
        
        mappingURL = dataFile("action-mapping.xml").toURL();
        
        ProjectConfigurationProvider<GradleExecConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        List<? extends GradleExecConfiguration> configs = new ArrayList<>(pcp.getConfigurations());
        assertEquals(2, configs.size());
        
        ConfigPersistenceUtilsTest.assertConfiguration(
                GradleExecAccessor.createDefault(), configs.get(0));
        
        GradleExecConfiguration provided = configs.get(1);
        assertEquals("continuous", provided.getId());
    }
    
    static volatile URL mappingURL = null;
    
    /*
    @ProjectServiceProvider(service = GradleActionsProvider.class, 
            projectType = "org-netbeans-modules-gradle/Plugins/java")
            */
    // annotation not used, as I need generated-layer.xml for documentation purposes 
    // the annotation would overwrite it in build/ dir.
    public static GradleActionsProvider createTestProvider() {
        return new GradleActionsProvider() {
            @Override
            public boolean isActionEnabled(String action, Project project, Lookup context) {
                return true;
            }

            @Override
            public Set<String> getSupportedActions() {
                return Collections.emptySet();
            }

            @Override
            public InputStream defaultActionMapConfig() {
                try {
                    return mappingURL == null ? null : mappingURL.openStream();
                } catch (IOException ex) {
                    return null;
                }
            }
        };
    }
}
