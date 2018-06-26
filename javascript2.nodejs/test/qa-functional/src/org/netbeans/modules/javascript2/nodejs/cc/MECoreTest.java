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
public class MECoreTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testCore1",
        "testCore2",
        "testCore3",
        "testCore4",
        "testCore5",
        "testCore6",
        "testCore7",
        "testCore8",
        "testCore9",
        "testCore10",
        "testCore11",
        "testCore12",
        "testCore13",
        "testCore14",
        "testCore15",
        "testCore16"
    };

    public MECoreTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(MECoreTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        downloadGlobalNodeJS();
        evt.waitNoEvent(8000);
        openFile("modex|cc|cc2.js", "SimpleNode");
        MECoreTest.currentFile = "cc2.js";
        endTest();
    }

    public void testCore1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 8);
        endTest();
    }

    public void testCore2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 10);
        endTest();
    }

    public void testCore3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 12);
        endTest();
    }

    public void testCore4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 14);
        endTest();
    }

    public void testCore5() throws Exception {
        startTest();
        openFile("modex|cc|cc8.js", "SimpleNode");
        MECoreTest.currentFile = "cc8.js";
        testCompletion(new EditorOperator("cc8.js"), 8);
        endTest();
    }

    public void testCore6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc8.js"), 10);
        endTest();
    }

    public void testCore7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc8.js"), 12);
        endTest();
    }

    public void testCore8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc8.js"), 14);
        endTest();
    }

    public void testCore9() throws Exception {
        startTest();
        openFile("modex|cc|cc9.js", "SimpleNode");
        MECoreTest.currentFile = "cc9.js";
        testCompletion(new EditorOperator("cc9.js"), 8);
        endTest();
    }

    public void testCore10() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc9.js"), 10);
        endTest();
    }

    public void testCore11() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc9.js"), 12);
        endTest();
    }

    public void testCore12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc9.js"), 14);
        endTest();
    }

    public void testCore13() throws Exception {
        startTest();
        openFile("modex|cc|cc10.js", "SimpleNode");
        MECoreTest.currentFile = "cc10.js";
        testCompletion(new EditorOperator("cc10.js"), 8);
        endTest();
    }

    public void testCore14() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc10.js"), 10);
        endTest();
    }

    public void testCore15() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc10.js"), 12);
        endTest();
    }

    public void testCore16() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc10.js"), 14);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator(MECoreTest.currentFile);
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
