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

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

/**
 *
 * @author Tomas Stupka
 */
public class ModelRunConfigTest extends NbTestCase {
    
    public ModelRunConfigTest(String testName) {
        super(testName);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testExecArgsOne() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop", args));        
    } 
    
    public void testExecArgsMore() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-lollipop2</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop -lollipop2", args));
    } 

    public void testExecArgsClasspath() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop ___CP___", args));
    } 

    public void testExecArgsClasspathMainClass() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop ___CP___ org.project.Main", args));
    } 
    
    public void testExecArgsClasspathDeps() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath>\n"
                 + "            <dependency>org.main:org.main.project</dependency>\n"                
                 + "        </classpath>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertNull(args));
    } 
    
    public void testExecArgsCPDeps() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-cp</argument>\n"
                 + "        <classpath>\n"
                 + "            <dependency>org.main:org.main.project</dependency>\n"                
                 + "        </classpath>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertNull(args));
    } 
    
    public void testExecArgsCPNoDeps() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-cp</argument>\n"
                 + "        <classpath>\n"
                 + "            <dependency></dependency>\n"                
                 + "        </classpath>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "        <argument>-lollipop2</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsCP() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop ___CP___", args));
    } 

    public void testExecArgsCPMainClass() throws Exception {
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop ___CP___ org.project.Main", args));
    } 
    
    public void testExecArgsAfterCP() throws Exception {    
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-lollipop</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "        <argument>-lollipop2</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("-lollipop ___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsUnresolvedProperty() throws Exception {    
        assertArgs("    <arguments>\n"                          
                 + "        <argument>${prop}</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "        <argument>-lollipop2</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsUnresolvedEmbProperty() throws Exception {    
        assertArgs("    <arguments>\n"                          
                 + "        <argument>emb${prop}</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "        <argument>-lollipop2</argument>\n"
                 + "    </arguments>\n", 
                (args) -> assertEquals("___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsResolvedProperty() throws Exception {    
        assertArgs("    <arguments>\n"                          
                 + "        <argument>${prop}</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "        <argument>-lollipop2</argument>\n"
                 + "    </arguments>\n",
                   "    <properties>\n"
                 + "        <prop>-propValue</prop>\n"                         
                 + "    </properties>\n",
                (args) -> assertEquals("-propValue ___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsResolvedEmbProperty() throws Exception {    
        assertArgs("    <arguments>\n"                          
                 + "        <argument>-emb${prop}</argument>\n"
                 + "        <argument>-classpath</argument>\n"
                 + "        <classpath/>\n"
                 + "        <argument>org.project.Main</argument>\n"
                 + "        <argument>-lollipop2</argument>\n"
                 + "    </arguments>\n",
                   "    <properties>\n"
                 + "        <prop>PropValue</prop>\n"                         
                 + "    </properties>\n",
                (args) -> assertEquals("-embPropValue ___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsNone() throws Exception {    
        assertArgs("",
                 (args) -> assertNull(args));        
    } 
    
    public void testExecArgsNoArgument() throws Exception {    
        assertArgs("    <arguments>\n"                                           
                 + "    </arguments>\n",
                 (args) -> assertNull(args));        
    } 
    
    public void testExecArgsBogusTag() throws Exception {        
        assertArgs("    <arguments>\n"                                           
                 +  "       <bogus/>"
                 + "    </arguments>\n",
                 (args) -> assertNull(args));        
    } 
    
    public void testExecArgsBogusValue() throws Exception {        
        assertArgs("    <arguments>\n"                                           
                 +  "       <bogus>bogus</bogus>"
                 + "    </arguments>\n",
                 (args) -> assertNull(args));        
    }
    
    private interface AssertArgs {
        void assertArgs(String args);
    }
    
    private void assertArgs(String argsString, AssertArgs a) throws IOException {
        assertArgs(argsString, "", a);
    }
    
    private void assertArgs(String argsString, String propString, AssertArgs a) throws IOException {
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                +      propString
                + "    <build>\n"
                + "        <plugins>\n"
                + "            <plugin>\n"
                + "                <groupId>org.codehaus.mojo</groupId>\n"
                + "                <artifactId>exec-maven-plugin</artifactId>\n"
                + "                <version>3.1.0</version>\n"
                + "                <configuration>\n"
                + "                    <executable>${java.home}/bin/java</executable>\n"
                +                      argsString 
                + "                </configuration>\n"
                + "            </plugin>\n"      
                + "        </plugins>\n"
                + "    </build>\n"
                + "</project>\n");
        
        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        NetbeansActionMapping mapp = ModelHandle2.getMapping("run", project, project.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration());
        a.assertArgs(ModelRunConfig.getExecArgsByPom(mapp, project));
    }
        
}
