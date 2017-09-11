/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.nbbuild;

/** Check the behaviour of <public-packages> in project.xml modules.
 *
 * @author Jaroslav Tulach
 */
public class PublicPackagesInProjectizedXMLTest extends TestBase {
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
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f });
            fail ("This should fail as the public package definition contains comma");
        } catch (ExecutionError ex) {
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
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f });
            fail ("This should fail as the public package definition contains *");
        } catch (ExecutionError ex) {
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
            execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f });
            fail ("This should fail as the public package definition contains *");
        } catch (ExecutionError ex) {
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
        execute ("GarbageUnderPackages.xml", new String[] { "-Dproject.file=" + f, "-Dexpected.public.packages=org.hello.**" });
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
        execute ("GarbageUnderPackages.xml", new String[] { "-Djavadoc.pac=some",  "-Dproject.file=" + f, "withjavadoc" });
    }
    
}
