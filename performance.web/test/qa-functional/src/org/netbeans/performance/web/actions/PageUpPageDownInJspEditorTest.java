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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006Sun
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
package org.netbeans.performance.web.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of Page Up and Page Down in opened source editor.
 *
 * @author anebuzelsky@netbeans.org
 */
public class PageUpPageDownInJspEditorTest extends PerformanceTestCase {

    private boolean pgup;
    private String file;
    private EditorOperator editorOperator;

    /**
     * Creates a new instance of PageUpPageDownInEditor
     *
     * @param testName test name
     */
    public PageUpPageDownInJspEditorTest(String testName) {
        super(testName);
        init();
    }

    /**
     * Creates a new instance of PageUpPageDownInEditor
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public PageUpPageDownInJspEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        init();
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(PageUpPageDownInJspEditorTest.class)
                .suite();
    }

    private void init() {
        expectedTime = 200;
        WAIT_AFTER_OPEN = 200;
    }

    public void testPageDownInJspEditor() {
        pgup = false;
        file = "Test.jsp";
        doMeasurement();
    }

    public void testPageDownInJspEditorWithLargeFile() {
        pgup = false;
        file = "BigJSP.jsp";
        doMeasurement();
    }

    public void testPageUpInJspEditor() {
        pgup = true;
        file = "Test.jsp";
        doMeasurement();
    }

    public void testPageUpInJspEditorWithLargeFile() {
        pgup = true;
        file = "BigJSP.jsp";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"), "Web Pages|" + file));
        editorOperator = new EditorOperator(file);
    }

    @Override
    public void prepare() {

    }

    @Override
    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        if (pgup) {
            editorOperator.setCaretPositionToLine(100);
            new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)).perform(editorOperator);
        } else {
            editorOperator.setCaretPositionToLine(1);
            new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0)).perform(editorOperator);
        }
        return null;
    }

    @Override
    protected void shutdown() {
        repaintManager().resetRegionFilters();
        editorOperator.closeDiscard();
        super.shutdown();
    }
}
