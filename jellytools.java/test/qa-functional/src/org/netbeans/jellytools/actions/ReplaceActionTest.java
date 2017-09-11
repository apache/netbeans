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
package org.netbeans.jellytools.actions;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test org.netbeans.jellytools.actions.ReplaceAction
 *
 * @author Jiri Skrivanek
 */
public class ReplaceActionTest extends JellyTestCase {

    private static final String SAMPLE_CLASS_1 = "SampleClass1";
    private static EditorOperator eo;
    public static final String[] tests = {
        "testPerformMenu",
        "testPerformAPI",
        "testPerformShortcut"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ReplaceActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition */
    public static Test suite() {
        return createModuleTest(ReplaceActionTest.class, tests);
    }

    /** Opens sample class and finds EditorOperator instance */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (eo == null) {
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            JavaNode sampleClass1 = new JavaNode(sample1, SAMPLE_CLASS_1);
            sampleClass1.open();
            eo = new EditorOperator(SAMPLE_CLASS_1);
            eo.requestFocus();
        }
    }

    /** Close open Replace dialog. */
    @Override
    public void tearDown() {
        // waits for close button (it is second button with given tooltip ebcause first one for search row is hidden)
        JButtonOperator closeButton = new JButtonOperator(eo, new JButtonOperator.JComponentByTipFinder("Close Incremental Search Sidebar"));
        closeButton.push();
        // close editor after last test case
        if (getName().equals("testPerformShortcut")) {
            eo.close();
        }
    }

    /** Test performMenu */
    public void testPerformMenu() {
        new ReplaceAction().performMenu(eo);
    }

    /** Test performAPI */
    public void testPerformAPI() {
        new ReplaceAction().performAPI(eo);
    }

    /** Test performShortcut */
    public void testPerformShortcut() {
        new ReplaceAction().performShortcut(eo);
    }
}
