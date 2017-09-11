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
package org.netbeans.jellytools.modules.debugger.actions;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/**
 * Test DebugJavaFileAction.
 *
 * @author Martin Schovanek
 */
public class DebugJavaFileActionTest extends JellyTestCase {

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public DebugJavaFileActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(DebugJavaFileActionTest.class);
    }
    private static final String SAMPLE_CLASS_1 = "SampleClass1";
    private static JavaNode sampleClass1 = null;
    private static final String outputTitle = "SampleProject (debug-single)";

    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
        System.out.println("### " + getName() + " ###");
        if (sampleClass1 == null) {
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            sampleClass1 = new JavaNode(sample1, SAMPLE_CLASS_1);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        new OutputTabOperator(outputTitle).waitText("BUILD SUCCESSFUL");
        super.tearDown();
    }

    /** Test performMenu() method. */
    public void testPerformMenu() {
        new DebugJavaFileAction().performMenu(sampleClass1);
    }

    /** Test performMenu() method. */
    public void testPerformPopup() {
        new DebugJavaFileAction().performPopup(sampleClass1);
    }

    /** Test performMenu() method. */
    public void testPerformShortcut() {
        new DebugJavaFileAction().performShortcut(sampleClass1);
    }
}
