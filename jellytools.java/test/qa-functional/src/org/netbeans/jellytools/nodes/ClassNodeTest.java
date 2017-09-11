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
package org.netbeans.jellytools.nodes;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jellytools.testutils.NodeUtils;

/**
 * Test of org.netbeans.jellytools.nodes.ClassNode
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class ClassNodeTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testVerifyPopup", "testProperties"
    };

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public ClassNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(ClassNodeTest.class, tests);
    }
    /**
     * ClassNode instance used in all test cases.
     */
    protected static ClassNode classNode = null;

    /** Finds data node before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        // find class node
        if (classNode == null) { // NOI18N
            Node sampleClass1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
            MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
            statusTextTracer.start();
            new CompileJavaAction().perform(sampleClass1Node);
            // wait status text "Building SampleProject (compile-single)"
            statusTextTracer.waitText("compile-single", true); // NOI18N
            // wait status text "Finished building SampleProject (compile-single).
            statusTextTracer.waitText("compile-single", true); // NOI18N
            statusTextTracer.stop();
            // create exactly (full match) and case sensitively comparing comparator to distinguish build and build.xml node
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            Node filesProjectNode = new FilesTabOperator().getProjectNode("SampleProject");
            filesProjectNode.setComparator(comparator);
            classNode = new ClassNode(filesProjectNode, "build|classes|sample1|SampleClass1.class"); // NOI18N
        }
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        classNode.verifyPopup();
    }

    /** Test properties */
    public void testProperties() {
        classNode.properties();
        NodeUtils.closeProperties("SampleClass1.class");
    }
}
