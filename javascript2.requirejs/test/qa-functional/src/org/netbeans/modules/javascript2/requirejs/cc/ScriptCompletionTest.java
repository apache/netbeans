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
import javax.swing.SwingUtilities;
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
public class ScriptCompletionTest extends GeneralRequire {

    public ScriptCompletionTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ScriptCompletionTest.class).addTest(
                        "openProject",
                        "testNonScript",
                        "testScript",
                        "testDataMainFolderPath",
                        "testDataMainPath"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        ScriptCompletionTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testNonScript() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(13, 11);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlAttrItems(cjo, new String[]{"data-bind"});
        checkCompletionDoesntContainHtmlAttrItems(cjo, new String[]{"data-main"});
        completion.listItself.hideAll();
        eo.close(false);
        endTest();
    }

    public void testScript() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(16, 17);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlAttrItems(cjo, new String[]{"data-bind", "data-main"});
        completion.listItself.hideAll();
        eo.close(false);
        endTest();
    }

    public void testDataMainPath() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(16, 17);
        type(eo, "data-main=");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlAttrItems(cjo, new String[]{"../", "sample.js", "js/"});
        checkCompletionDoesntContainHtmlAttrItems(cjo, new String[]{"app", "main.js"});
        completion.listItself.hideAll();
        eo.close(false);
        endTest();
    }

    public void testDataMainFolderPath() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(16, 17);
        type(eo, "data-main=js/");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        completion.hideAll();

        checkCompletionHtmlAttrItems(cjo, new String[]{"app/", "bbb/", "main.js"});
        checkCompletionDoesntContainHtmlAttrItems(cjo, new String[]{"sample.js"});

        eo.typeKey(' ', InputEvent.CTRL_MASK);

        completion = getCompletion();
        cjo = completion.listItself;
        final CompletionJListOperator op = cjo;
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    op.clickOnItem(5, 2);
                }
            });
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        evt.waitNoEvent(1000);
        assertTrue("File badly inserted: "+eo.getText(eo.getLineNumber()), eo.getText(eo.getLineNumber()).contains("data-main=\"js/main\""));
        eo.close(false);
        endTest();
    }

}
