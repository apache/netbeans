/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
public class ModuleInstanceTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testInstance1",
        "testInstance2",
        "testInstance3",
        "testInstance4",
        "testInstance5",
        "testInstance6",
        "testInstance7",
        "testInstance8",
        "testInstance9",
        "testReference1",
        "testReference2",
        "testReference3",
        "testReference4",
        "testReference5",
        "testReference6",
        "testReference7",
        "testReference8",
        "testReference9"
    };

    public ModuleInstanceTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(ModuleInstanceTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("cc|cc3.js", "SimpleNode");
        endTest();
    }

    public void testInstance1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 31);
        endTest();
    }

    public void testInstance2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 33);
        endTest();
    }

    public void testInstance3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 35);
        endTest();
    }

    public void testInstance4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 37);
        endTest();
    }

    public void testInstance5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 39);
        endTest();
    }

    public void testInstance6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 41);
        endTest();
    }

    public void testInstance7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 43);
        endTest();
    }

    public void testInstance8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 45);
        endTest();
    }

    public void testInstance9() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 47);
        endTest();
    }

    public void testReference1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 51);
        endTest();
    }

    public void testReference2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 53);
        endTest();
    }

    public void testReference3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 55);
        endTest();
    }

    public void testReference4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 57);
        endTest();
    }

    public void testReference5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 59);
        endTest();
    }

    public void testReference6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 61);
        endTest();
    }

    public void testReference7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 63);
        endTest();
    }

    public void testReference8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 65);
        endTest();
    }

    public void testReference9() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 67);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("cc3.js");
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }

}
