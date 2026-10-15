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

package org.netbeans.nbbuild;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileRule;
import org.junit.Rule;

/** Check the behaviour of <public-packages> in project.xml modules.
 *
 * @author Jaroslav Tulach
 */
public class PublicPackagesInProjectizedXMLTest extends TestBase {
    
    @Rule
    public final BuildFileRule buildRule = new BuildFileRule();
        
    public PublicPackagesInProjectizedXMLTest (String name) {
        super (name);
    }
    
    public void testPackageCannotContainComma () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <package>org,org.apache.bsf</package>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            System.setProperty("project.file", f.getAbsolutePath());
            buildRule.configureProject(getBuildFileInClassPath("GarbageUnderPackages.xml"));
            buildRule.executeTarget("all");
            
            fail ("This should fail as the public package definition contains comma");
        } catch (BuildException ex) {
            // ok, this should fail on exit code
        }
    }

    public void testPackageCannotContainStar () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <package>org.**</package>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            System.setProperty("project.file", f.getAbsolutePath());
            buildRule.configureProject(getBuildFileInClassPath("GarbageUnderPackages.xml"));
            buildRule.executeTarget("all");
            fail ("This should fail as the public package definition contains *");
        } catch (BuildException ex) {
            // ok, this should fail on exit code
        }
    }

    public void testPublicPackagesCannotContainGarbageSubelements () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <pkgs>org.hello</pkgs>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            System.setProperty("project.file", f.getAbsolutePath());
            buildRule.configureProject(getBuildFileInClassPath("GarbageUnderPackages.xml"));
            buildRule.executeTarget("all");
            fail ("This should fail as the public package definition contains *");
        } catch (BuildException ex) {
            // ok, this should fail on exit code
        }
    }
    
    public void testItIsPossibleToDefineSubpackages () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <module-dependencies/>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "   </data></configuration>" +
            "</project>"
        );
        System.setProperty("project.file", f.getAbsolutePath());
        System.setProperty("expected.public.packages", "org.hello.**");
        buildRule.configureProject(getBuildFileInClassPath("GarbageUnderPackages.xml"));
        buildRule.executeTarget("all");
    }
    
    /* DISABLED because of fix for #52135:
    public void testSubpackagesDoNotWorkForJavadocNow () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "       <javadoc/>" +
            "   </data></configuration>" +
            "</project>"
        );
        try {
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f, "withjavadoc" });
            fail ("We do not support <subpackage> when javadoc packages are requested, so the execution should fail");
        } catch (ExecutionError ex) {
            // ok
        }
    }
     */

    public void testSubpackagesDoNotWorkForJavadocNowButThisWorksWhenSpecifiedByHand () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" +
            "   <type>org.netbeans.modules.apisupport.project</type>" +
            "   <configuration><data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">" +
            "       <code-name-base>org.netbeans.modules.scripting.bsf</code-name-base>" +
            "       <module-dependencies/>" +
            "       <public-packages>" +
            "           <subpackages>org.hello</subpackages>" +
            "       </public-packages>" +
            "   </data></configuration>" +
            "</project>"
        );
        System.setProperty("project.file", f.getAbsolutePath());
        System.setProperty("javadoc.pac", "some");
        buildRule.configureProject(getBuildFileInClassPath("GarbageUnderPackages.xml"));
        buildRule.executeTarget("withjavadoc");
            
    }
    
}
