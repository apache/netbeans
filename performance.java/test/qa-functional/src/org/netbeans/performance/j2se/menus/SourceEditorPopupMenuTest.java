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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.performance.j2se.menus;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of popup menu on Source Editor pane.
 *
 * @author mmirilovic@netbeans.org
 */
public class SourceEditorPopupMenuTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private static String fileName;

    /**
     * Creates a new instance of SourceEditorPopupMenu
     *
     * @param testName test name
     */
    public SourceEditorPopupMenuTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Creates a new instance of SourceEditorPopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public SourceEditorPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(SourceEditorPopupMenuTest.class)
                .suite();
    }

    public void testPopupInTxt() {
        fileName = "textfile.txt";
        expectedTime = 200;
        doMeasurement();
    }

    public void testPopupInXml() {
        fileName = "xmlfile.xml";
        expectedTime = 200;
        doMeasurement();
    }

    public void testPopupInJava() {
        fileName = "Main.java";
        expectedTime = 500;
        doMeasurement();
    }

    @Override
    public void initialize() {
        MainWindowOperator.getDefault().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        Node fileNode = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);

        if (fileName.endsWith("xml")) {
            new EditAction().performAPI(fileNode);
        } else {
            new OpenAction().performAPI(fileNode);
        }
        editor = new EditorOperator(fileName);
        waitNoEvent(2000);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        editor.pushKey(java.awt.event.KeyEvent.VK_F10, java.awt.event.KeyEvent.SHIFT_MASK);
        return new JPopupMenuOperator();
    }

    @Override
    public void close() {
        editor.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    @Override
    public void shutdown() {
        MainWindowOperator.getDefault().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        EditorOperator.closeDiscardAll();
    }
}
