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
public class ModuleLiteralTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testLiteral1",
        "testLiteral2",
        "testLiteral3",
        "testLiteral4",
        "testLiteral5",
        "testLiteral6",
        "testLiteralRef1",
        "testLiteralRef2",
        "testLiteralRef3",
        "testLiteralRef4",
        "testLiteralRef5",
        "testLiteralRef6"
    };

    public ModuleLiteralTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(ModuleLiteralTest.class, tests);
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

    public void testLiteral1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 4);
        endTest();
    }

    public void testLiteral2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 6);
        endTest();
    }

    public void testLiteral3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 8);
        endTest();
    }

    public void testLiteral4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 10);
        endTest();
    }

    public void testLiteral5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 12);
        endTest();
    }

    public void testLiteral6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 14);
        endTest();
    }

    public void testLiteralRef1() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 17);
        endTest();
    }

    public void testLiteralRef2() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 19);
        endTest();
    }

    public void testLiteralRef3() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 21);
        endTest();
    }

    public void testLiteralRef4() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 23);
        endTest();
    }

    public void testLiteralRef5() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 25);
        endTest();
    }

    public void testLiteralRef6() throws Exception {
        startTest();
        testCompletion(new EditorOperator("cc3.js"), 27);
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
