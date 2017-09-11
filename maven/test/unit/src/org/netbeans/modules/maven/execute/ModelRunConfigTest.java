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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import org.netbeans.modules.maven.api.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import static junit.framework.TestCase.assertEquals;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.ModelUtils.Descriptor;
import org.netbeans.modules.maven.api.ModelUtils.LibraryDescriptor;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Repository;
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
                + "                <version>1.4.0</version>\n"
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
