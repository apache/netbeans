/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
        assertEquals("In the same dir", whereToObj.getParent(), createProject.getParent());
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
