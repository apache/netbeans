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

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class JUnitSettingsTest extends BasicTestForImport {
    public JUnitSettingsTest(String testName) {
        super(testName, "org-netbeans-modules-junit-JUnitSettings.settings");
    }
    
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/junit");
    }
    
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "fileSystem",
            "version",
            "membersPackage",
            "generateMainMethod",
            "membersProtected",
            "bodyComments",
            "bodyContent",
            "javaDoc",
            "generateTearDown",
            "generateSuiteClasses",
            "membersPublic",
            "includePackagePrivateClasses",
            "rootSuiteClassName",
            "generateSetUp",
            "generateAbstractImpl",
            "generateMainMethodBody",
            "generateExceptionClasses"
        });
    }
    
    public void testVersion() throws Exception {
        assertPropertyType("version","java.lang.Integer");
        assertProperty("version","41");
    }
    
    public void testFileSystem() throws Exception {
        assertPropertyType("fileSystem","java.lang.String");
        assertProperty("fileSystem","");
    }
    
    public void testMembersPublic() throws Exception {
        assertPropertyType("membersPublic","java.lang.Boolean");
        assertProperty("membersPublic","true");
    }
    
    public void testMembersProtected() throws Exception {
        assertPropertyType("membersProtected","java.lang.Boolean");
        assertProperty("membersProtected","true");
    }
    
    public void testMembersPackage() throws Exception {
        assertPropertyType("membersPackage","java.lang.Boolean");
        assertProperty("membersPackage","true");
    }
    
    public void testBodyComments() throws Exception {
        assertPropertyType("bodyComments","java.lang.Boolean");
        assertProperty("bodyComments","true");
    }
    
    public void testBodyContent() throws Exception {
        assertPropertyType("bodyContent","java.lang.Boolean");
        assertProperty("bodyContent","true");
    }
    
    public void testJavaDoc() throws Exception {
        assertPropertyType("javaDoc","java.lang.Boolean");
        assertProperty("javaDoc","true");
    }
    
    public void testGenerateTearDown() throws Exception {
        assertPropertyType("generateTearDown","java.lang.Boolean");
        assertProperty("generateTearDown","true");
    }
    public void testGenerateSuiteClasses() throws Exception {
        assertPropertyType("generateSuiteClasses","java.lang.Boolean");
        assertProperty("generateSuiteClasses","true");
    }
    public void testIncludePackagePrivateClasses() throws Exception {
        assertPropertyType("includePackagePrivateClasses","java.lang.Boolean");
        assertProperty("includePackagePrivateClasses","false");
    }
    public void testRootSuiteClassName() throws Exception {
        assertPropertyType("rootSuiteClassName","java.lang.String");
        assertProperty("rootSuiteClassName","RootSuite");
    }
    public void testGenerateSetUp() throws Exception {
        assertPropertyType("generateSetUp","java.lang.Boolean");
        assertProperty("generateSetUp","true");
    }
    public void testGenerateAbstractImpl() throws Exception {
        assertPropertyType("generateAbstractImpl","java.lang.Boolean");
        assertProperty("generateAbstractImpl","true");
    }
    public void testGenerateMainMethodBody() throws Exception {
        assertPropertyType("generateMainMethodBody","java.lang.String");
        assertProperty("generateMainMethodBody","junit.textui.TestRunner.run(suite());");
    }
    public void testGenerateExceptionClasses() throws Exception {
        assertPropertyType("generateExceptionClasses","java.lang.Boolean");
        assertProperty("generateExceptionClasses","false");
    }
}
