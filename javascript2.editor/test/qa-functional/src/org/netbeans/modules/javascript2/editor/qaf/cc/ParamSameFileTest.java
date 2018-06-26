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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author vriha
 */
public class ParamSameFileTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "openProject",
        "testObjectTop",
        "testObjectMethod",
        "testObjectNested",
        "testObjectSecond",
        "testInstTop",
        "testInstMethod",
        "testInstNested",
        "testInstSecond"
    };

    public ParamSameFileTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return createModuleTest(ParamSameFileTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        GeneralJavaScript.currentLine = 0;
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("completionTest");
        evt.waitNoEvent(2000);
        openFile("same.js", "completionTest");
        endTest();
    }

    public void testObjectTop() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 36);
        endTest();
    }

    public void testObjectMethod() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 71);
        endTest();
    }

    public void testObjectNested() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 82);
        endTest();
    }

    public void testObjectSecond() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 106);
        endTest();
    }

    public void testInstTop() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 48);
        endTest();
    }

    public void testInstMethod() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 74);
        endTest();
    }

    public void testInstNested() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 85);
        endTest();
    }

    public void testInstSecond() throws Exception {
        startTest();
        testCompletion(new EditorOperator("same.js"), 119);
        endTest();
    }

    @Override
    public void tearDown() {
        if (GeneralJavaScript.currentLine < 1) {
            return;
        }
        EditorOperator eo = new EditorOperator("same.js");
        eo.setCaretPositionToEndOfLine(GeneralJavaScript.currentLine);
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }

        evt.waitNoEvent(500);

    }
}
