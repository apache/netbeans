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
package org.netbeans.performance.j2ee.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of Project Properties Window
 *
 * @author mmirilovic@netbeans.org
 */
public class SelectJ2EEModuleDialogTest extends PerformanceTestCase {

    private static Node testNode;

    /**
     * Creates a new instance of SelectJ2EEModuleDialogTest
     *
     * @param testName
     */
    public SelectJ2EEModuleDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of SelectJ2EEModuleDialogTest
     *
     * @param testName
     * @param performanceDataName
     */
    public SelectJ2EEModuleDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(SelectJ2EEModuleDialogTest.class).suite();
    }

    public void testSelectJ2EEModuleDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        JTreeOperator tree = new ProjectsTabOperator().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        String JAVA_EE_MODULES = Bundle.getStringTrimmed(
                "org.netbeans.modules.j2ee.earproject.ui.Bundle",
                "LBL_LogicalViewNode");
        testNode = new Node(new ProjectRootNode(tree, "TestApplication"), JAVA_EE_MODULES);
    }

    public void prepare() {
        // do nothing
    }

    public ComponentOperator open() {
        testNode.performPopupActionNoBlock("Add Java EE Module...");
        return new NbDialogOperator("Add Java EE Module");
    }
}
