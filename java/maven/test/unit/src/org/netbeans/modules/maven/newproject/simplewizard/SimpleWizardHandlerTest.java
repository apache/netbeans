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
package org.netbeans.modules.maven.newproject.simplewizard;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import net.java.html.json.Model;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.AbstractMavenExecutor;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

@Model(className = "EmptyModel", properties = {})
public class SimpleWizardHandlerTest extends NbTestCase {
    
    public SimpleWizardHandlerTest(String name) {
        super(name);
    }
    
    
    @TemplateRegistration(
            page = "SimpleWizard.html",
            folder = "Project/Test",
            displayName = "#Wizard_displayName",
            content = "SimpleWizard.archetype"
    )
    @NbBundle.Messages({
        "Wizard_displayName=SimpleWizard"
    })
    static EmptyModel simpleWizard() {
        return new EmptyModel();
    }
    
    public void testSimpleWizard() throws Exception {
        if (!EnsureJavaFXPresent.check()) {
            return;
        }

        clearWorkDir();
        File whereTo = new File(getWorkDir(), "whereTo");
        whereTo.mkdirs();
        FileObject whereToObj = FileUtil.toFileObject(whereTo);
        assertNotNull("Where obj found", whereToObj);
        MockServices.setServices(MockExecuteMaven.class);
        
        FileObject archetype = FileUtil.getConfigFile("Templates/Project/Test/SimpleWizard.archetype");
        assertNotNull(archetype);
        Object iterator = archetype.getAttribute("instantiatingIterator");
        assertNotNull("Iterator found", iterator);
        assertTrue("It is iterator", iterator instanceof WizardDescriptor.InstantiatingIterator);
        WizardDescriptor.InstantiatingIterator instIter = (WizardDescriptor.InstantiatingIterator) iterator;
    
        TemplateWizard tw = new TemplateWizard();
        tw.setTemplate(DataObject.find(archetype));
        tw.setTargetFolder(DataFolder.findFolder(whereToObj));
        tw.putProperty("version", "6.5");
        tw.putProperty("artifactId", "simplewizardtest");
        tw.putProperty("groupId", "org.netbeans.test");
        
        instIter.initialize(tw);
        Set<?> results = instIter.instantiate();
        assertEquals("One directory: " + results, 1, results.size());
        FileObject createProject = fileObject(results.iterator().next());
        assertEquals("In the same dir", whereToObj, createProject.getParent());
        assertEquals("Name as artifactId", "simplewizardtest", createProject.getNameExt());
        
        RunConfig runConfig = MockExecuteMaven.assertConfigExists();
        assertEquals("Check archetype file ArtifactId", "javafx", runConfig.getProperties().get("archetypeArtifactId"));
        assertEquals("Check archetype file GroupId", "org.codehaus.mojo.archetypes", runConfig.getProperties().get("archetypeGroupId"));
        assertEquals("Check archetype file version", "0.6", runConfig.getProperties().get("archetypeVersion"));
    }

    private FileObject fileObject(Object obj) {
        if (obj instanceof FileObject) {
            return (FileObject) obj;
        }
        return ((DataObject)obj).getPrimaryFile();
    }

    public static final class MockExecuteMaven extends MavenCommandLineExecutor.ExecuteMaven {
        private static RunConfig lastConfig;
        
        public static RunConfig assertConfigExists() {
            RunConfig c = lastConfig;
            assertNotNull("Config exists", c);
            lastConfig = null;
            return c;
        }
        
        @Override
        public ExecutorTask execute(RunConfig config, InputOutput io, AbstractMavenExecutor.TabContext tc) {
            Map<? extends String, ? extends String> props = config.getProperties();
            File basedir = config.getExecutionDirectory();
            File mavendir = new File(basedir, props.get("artifactId"));
            File pom = new File(mavendir, "pom.xml");
            pom.getParentFile().mkdirs();
            try {
                pom.createNewFile();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            assertTrue("Pom file exists", pom.exists());
            
            lastConfig = config;
            
            return new ExecutorTask(null) {
                @Override
                public void stop() {
                }
                
                @Override
                public int result() {
                    return 0;
                }
                
                @Override
                public InputOutput getInputOutput() {
                    return io;
                }
            };
        }
    }
}
