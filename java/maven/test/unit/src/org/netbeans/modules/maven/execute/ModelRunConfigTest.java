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

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
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
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop", args));        
    } 
    
    public void testExecArgsMore() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-lollipop2</argument>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop -lollipop2", args));
    } 

    public void testExecArgsClasspath() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop ___CP___", args));
    } 

    public void testExecArgsClasspathMainClass() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                        <argument>org.project.Main</argument>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop ___CP___ org.project.Main", args));
    } 
    
    public void testExecArgsClasspathDeps() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-classpath</argument>
                        <classpath>
                            <dependency>org.main:org.main.project</dependency>
                        </classpath>
                        <argument>org.project.Main</argument>
                    </arguments>
                """,
                (args) -> assertNull(args));
    } 
    
    public void testExecArgsCPDeps() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-cp</argument>
                        <classpath>
                            <dependency>org.main:org.main.project</dependency>
                        </classpath>
                        <argument>org.project.Main</argument>
                    </arguments>
                """,
                (args) -> assertNull(args));
    } 
    
    public void testExecArgsCPNoDeps() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-cp</argument>
                        <classpath>
                            <dependency></dependency>
                        </classpath>
                        <argument>org.project.Main</argument>
                        <argument>-lollipop2</argument>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsCP() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop ___CP___", args));
    } 

    public void testExecArgsCPMainClass() throws Exception {
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                        <argument>org.project.Main</argument>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop ___CP___ org.project.Main", args));
    } 
    
    public void testExecArgsAfterCP() throws Exception {    
        assertArgs(
                """
                    <arguments>
                        <argument>-lollipop</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                        <argument>org.project.Main</argument>
                        <argument>-lollipop2</argument>
                    </arguments>
                """,
                (args) -> assertEquals("-lollipop ___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsUnresolvedProperty() throws Exception {    
        assertArgs(
                """
                    <arguments>
                        <argument>${prop}</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                        <argument>org.project.Main</argument>
                        <argument>-lollipop2</argument>
                    </arguments>
                """,
                (args) -> assertEquals("___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsUnresolvedEmbProperty() throws Exception {    
        assertArgs(
                """
                    <arguments>
                        <argument>emb${prop}</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                        <argument>org.project.Main</argument>
                        <argument>-lollipop2</argument>
                    </arguments>
                """,
                (args) -> assertEquals("___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsResolvedProperty() throws Exception {    
        assertArgs(
                """
                    <arguments>
                        <argument>${prop}</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                        <argument>org.project.Main</argument>
                        <argument>-lollipop2</argument>
                    </arguments>
                """,
                """
                    <properties>
                        <prop>-propValue</prop>
                    </properties>
                """,
                (args) -> assertEquals("-propValue ___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsResolvedEmbProperty() throws Exception {    
        assertArgs(
                """
                    <arguments>
                        <argument>-emb${prop}</argument>
                        <argument>-classpath</argument>
                        <classpath/>
                        <argument>org.project.Main</argument>
                        <argument>-lollipop2</argument>
                    </arguments>
                """,
                """
                    <properties>
                        <prop>PropValue</prop>
                    </properties>
                """,
                (args) -> assertEquals("-embPropValue ___CP___ org.project.Main -lollipop2", args));
    } 
    
    public void testExecArgsNone() throws Exception {    
        assertArgs(
                "",
                (args) -> assertNull(args));        
    } 
    
    public void testExecArgsNoArgument() throws Exception {    
        assertArgs(
                """
                    <arguments>
                    </arguments>
                """,
                (args) -> assertNull(args));        
    } 
    
    public void testExecArgsBogusTag() throws Exception {        
        assertArgs(
                """
                    <arguments>
                       <bogus/>
                    </arguments>
                """,
                 (args) -> assertNull(args));        
    } 
    
    public void testExecArgsBogusValue() throws Exception {        
        assertArgs(
                """
                    <arguments>
                       <bogus>bogus</bogus>
                    </arguments>
                """,
                (args) -> assertNull(args));        
    }
    
    private interface AssertArgs {
        void assertArgs(String args);
    }
    
    private void assertArgs(String argsString, AssertArgs a) throws IOException {
        assertArgs(argsString, "", a);
    }
    
    private void assertArgs(String argsString, String propString, AssertArgs a) throws IOException {
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "pom.xml", 
                """
                <project xmlns='http://maven.apache.org/POM/4.0.0'>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>grp</groupId>
                    <artifactId>art</artifactId>
                    <version>1.0</version>
                    PROPS
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.codehaus.mojo</groupId>
                                <artifactId>exec-maven-plugin</artifactId>
                                <version>3.6.3</version>
                                <configuration>
                                    <executable>${java.home}/bin/java</executable>
                                    ARGS
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.replace("PROPS", propString).replace("ARGS", argsString));
        
        Project project = ProjectManager.getDefault().findProject(pom.getParent());        
        NetbeansActionMapping mapp = ModelHandle2.getMapping("run", project, project.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration());
        a.assertArgs(ModelRunConfig.getExecArgsByPom(mapp, project));
    }
        
}
