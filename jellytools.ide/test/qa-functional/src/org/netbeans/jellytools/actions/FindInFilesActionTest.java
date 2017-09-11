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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.jellytools.actions;

import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.actions.FindInFilesAction.
 *
 * @author Jiri Skrivanek
 */
public class FindInFilesActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testPerformPopup",
        "testPerformMenu",
        "testPerformAPI"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FindInFilesActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition */
    public static NbTestSuite suite() {
        return (NbTestSuite) createModuleTest(FindInFilesActionTest.class, tests);
    }

    /** Redirect output to log files, wait before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Test of performPopup method. */
    public void testPerformPopup() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performPopup(node);
        new FindInFilesOperator().close();
    }

    /** Test of performMenu method. */
    public void testPerformMenu() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performMenu(node);
        new FindInFilesOperator().close();
        // need to wait here because next menu can disappear
        new EventTool().waitNoEvent(500);
        new FindInFilesAction().performMenu();
        new FindInFilesOperator().close();
    }

    /** Test of performAPI method. */
    public void testPerformAPI() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performAPI(node);
        new FindInFilesOperator().close();
        new FindInFilesAction().performAPI(new ProjectsTabOperator());
        new FindInFilesOperator().close();
        new FindInFilesAction().performAPI();
        new FindInFilesOperator().close();
    }
}
