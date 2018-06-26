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
package org.netbeans.modules.javascript2.requirejs.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.requirejs.GeneralRequire;

/**
 *
 * @author Vladimir Riha
 */
public class DependenciesTest extends GeneralRequire {

    public DependenciesTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(DependenciesTest.class).addTest(
                        "openProject",
                        "testRequire",
                        "testDefine"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        DependenciesTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testRequire() throws Exception {
        doTest("js|main.js", 19, 105);
    }

    public void testDefine() throws Exception {
        doTest("js|bbb|fs.js", 1, 10);
    }

    private void doTest(String file, int rowNumber, int columnNumber) throws Exception {
        startTest();
        EditorOperator eo = openFile(file, DependenciesTest.currentProject);
        eo.setCaretPosition(rowNumber, columnNumber);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"app", "bbb", "bbb2", "libs", "jquery", "piwik", "external"});
        checkCompletionDoesntContainItems(cjo, new String[]{"mymodule", "objectLiteral", "mymodule.js"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        type(eo, "b");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"bbb", "bbb2"});
        checkCompletionDoesntContainItems(cjo, new String[]{"app", "objectLiteral", "mymodule"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.pressKey(KeyEvent.VK_BACK_SPACE);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        cjo.clickOnItem("app");
        eo.pressKey(KeyEvent.VK_ENTER);
        completion.listItself.hideAll();

        assertTrue("Wrong inserted file based completion", eo.getText(eo.getLineNumber()).trim().endsWith("\"app/\"],"));

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"function", "mymodule", "newFunction", "objectLiteral", "stdModule"});
        checkCompletionDoesntContainItems(cjo, new String[]{"app", "def", "piwik"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);

        type(eo, "m");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"mymodule", "mytest"});
        checkCompletionDoesntContainItems(cjo, new String[]{"function", "app", "def", "piwik", "newFunction"});
        completion.listItself.hideAll();
        eo.pressKey(KeyEvent.VK_ESCAPE);

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        cjo.clickOnItem("mymodule");
        eo.pressKey(KeyEvent.VK_ENTER);
        completion.listItself.hideAll();

        assertTrue("Wrong inserted file based completion", eo.getText(eo.getLineNumber()).trim().endsWith("\"app/mymodule\"],"));

        endTest();
    }

}
