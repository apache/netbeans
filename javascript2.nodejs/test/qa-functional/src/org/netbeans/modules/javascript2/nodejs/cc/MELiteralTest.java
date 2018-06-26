/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.nodejs.cc;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.nodejs.GeneralNodeJs;

/**
 *
 * @author vriha
 */
public class MELiteralTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testExports1",
        "testExports2",
        "testExports3",
        "testExports4",
        "testExports5",
        "testExports6",
        "testExports7",
        "testExports8",
        "testExports9",
        "testExports10",
        "testExports11",
        "testExports12",
        "testExports13",
        "testExports14",
        "testExports15",
        "testExports16",
        "testExports17",
        "testExports18",
        "testExports19",
        "testExports20",
        "testExports21",
        "testExports22",
        "testExports23",
        "testExports24"
    };

    public MELiteralTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(MELiteralTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("modex|cc|cc1.js", "SimpleNode");
        MELiteralTest.currentFile = "cc1.js";
        endTest();
    }

    public void testExports1() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports2() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports3() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports4() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports5() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports6() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    public void testExports7() throws Exception {
        startTest();
        openFile("modex|cc|cc5.js", "SimpleNode");
        MELiteralTest.currentFile = "cc5.js";
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports8() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports9() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports10() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports11() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports12() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    public void testExports13() throws Exception {
        startTest();
        openFile("modex|cc|cc6.js", "SimpleNode");
        MELiteralTest.currentFile = "cc6.js";
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports14() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports15() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports16() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports17() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports18() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    public void testExports19() throws Exception {
        startTest();
        openFile("modex|cc|cc7.js", "SimpleNode");
        MELiteralTest.currentFile = "cc7.js";
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 4);
        endTest();
    }

    public void testExports20() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 6);
        endTest();
    }

    public void testExports21() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 8);
        endTest();
    }

    public void testExports22() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 10);
        endTest();
    }

    public void testExports23() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 12);
        endTest();
    }

    public void testExports24() throws Exception {
        startTest();
        testCompletion(new EditorOperator(MELiteralTest.currentFile), 14);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(MELiteralTest.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }

}
