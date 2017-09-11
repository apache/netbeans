/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;

/**
 * Test of org.netbeans.jellytools.NewFileNameLocationStepOperator.
 *
 * @author tb115823
 */
public class NewJavaFileNameLocationStepOperatorTest extends JellyTestCase {

    public static NewJavaFileNameLocationStepOperator op;
    public static String[] tests = new String[]{
        "testInvoke", "testComponents"};

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NewJavaFileNameLocationStepOperatorTest.class, tests);
    }

    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewJavaFileNameLocationStepOperatorTest(String testName) {
        super(testName);
    }

    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() {
        NewFileWizardOperator wop = NewFileWizardOperator.invoke();
        wop.selectProject("SampleProject"); //NOI18N
        // Java
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        wop.selectCategory(javaClassesLabel);
        wop.selectFileType("Java Class");
        wop.next();
        op = new NewJavaFileNameLocationStepOperator();
    }

    public void testComponents() {
        op.txtObjectName().setText("NewObject"); // NOI18N
        assertEquals("Project name not propagated from previous step", "SampleProject", op.txtProject().getText()); // NOI18N
        op.selectSourcePackagesLocation();
        op.selectPackage("sample1"); // NOI18N
        String filePath = op.txtCreatedFile().getText();
        assertTrue("Created file path doesn't contain SampleProject.", filePath.indexOf("SampleProject") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain sample1 package name.", filePath.indexOf("sample1") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain NewObject name.", filePath.indexOf("NewObject") > 0);  //NOI18N
        op.cancel();
    }
}
