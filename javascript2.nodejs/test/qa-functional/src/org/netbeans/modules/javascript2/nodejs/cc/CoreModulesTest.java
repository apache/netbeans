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
public class CoreModulesTest extends GeneralNodeJs {

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
        "testCore16",
        "testCore17",
        "testCore18",
        "testCore19",
        "testCore20",
        "testCore21",
        "testCore22",
        "testCore23",
        "testCore24",
        "testCore25",
        "testCore26",
        "testCore27",
        "testCore28",
        "testCore29"
    };

    public CoreModulesTest(String args) {
        super(args);
    }

    public static Test suite() {       
        return createModuleTest(CoreModulesTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralNodeJs.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        downloadGlobalNodeJS();
        evt.waitNoEvent(8000);
        openFile("cc|cc2.js", "SimpleNode");
        endTest();
    }

    public void testCore1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 11);
        endTest();
    }

    public void testCore2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 13);
        endTest();
    }

    public void testCore3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 15);
        endTest();
    }

    public void testCore4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 18);
        endTest();
    }

    public void testCore5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 21);
        endTest();
    }

    public void testCore6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 24);
        endTest();
    }

    public void testCore7() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 27);
        endTest();
    }

    public void testCore8() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 30);
        endTest();
    }

    public void testCore9() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 33);
        endTest();
    }

    public void testCore10() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 36);
        endTest();
    }

    public void testCore11() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 39);
        endTest();
    }

    public void testCore12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 42);
        endTest();
    }

    public void testCore13() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 45);
        endTest();
    }

    public void testCore14() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 48);
        endTest();
    }

    public void testCore15() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 51);
        endTest();
    }

    public void testCore16() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 54);
        endTest();
    }

    public void testCore17() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 57);
        endTest();
    }

    public void testCore18() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 60);
        endTest();
    }

    public void testCore19() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 63);
        endTest();
    }

    public void testCore20() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 66);
        endTest();
    }

    public void testCore21() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 69);
        endTest();
    }

    public void testCore22() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 76);
        endTest();
    }

    public void testCore23() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 79);
        endTest();
    }

    public void testCore24() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 82);
        endTest();
    }

    public void testCore25() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 85);
        endTest();
    }

    public void testCore26() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 88);
        endTest();
    }

    public void testCore27() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 91);
        endTest();
    }

    public void testCore28() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 94);
        endTest();
    }

    public void testCore29() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc2.js"), 97);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralNodeJs.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("cc2.js");
        eo.setCaretPositionToEndOfLine(GeneralNodeJs.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(1000);

    }
}
