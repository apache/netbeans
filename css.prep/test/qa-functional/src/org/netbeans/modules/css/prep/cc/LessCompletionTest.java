/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.cc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.css.prep.GeneralCSSPrep;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class LessCompletionTest extends GeneralCSSPrep {

    private static final String PROJECT_NAME = "css_prep";

    public LessCompletionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(LessCompletionTest.class).addTest(
                        "openProject",
                        "testCompletionL4",
                        "testCompletionL8",
                        "testCompletionL11",
                        "testCompletionL18",
                        "testCompletionL30",
                        "testCompletionL35",
                        "testCompletionL41",
                        "testCompletionL45",
                        "testCompletionL47"
                ).enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(LessCompletionTest.PROJECT_NAME);
        evt.waitNoEvent(3000);
        openFile("cc.less", LessCompletionTest.PROJECT_NAME);
        endTest();
    }

    public void testCompletionL4() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 4);
        endTest();
    }

    public void testCompletionL8() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 8);
        endTest();
    }

    public void testCompletionL11() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 11);
        endTest();
    }

    public void testCompletionL18() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 18);
        endTest();
    }

    public void testCompletionL30() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 30);
        endTest();
    }

    public void testCompletionL35() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 35);
        endTest();
    }

    public void testCompletionL41() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 41);
        endTest();
    }

    public void testCompletionL45() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 45);
        endTest();
    }

    public void testCompletionL47() throws Exception {
        startTest();
        doTest(new EditorOperator("cc.less"), 47);
        endTest();
    }

    public void doTest(EditorOperator eo, int lineNumber) {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("/*cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPositionToEndOfLine(Integer.parseInt(config[1]));
        type(eo, config[2]);
        
        int stop = Integer.parseInt(config[4].trim());
        for(int i=0; i < stop; i++){
            eo.pressKey(KeyEvent.VK_LEFT);
        }
        
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[3].split(","));
        completion.listItself.hideAll();
       for(int i =0; i < config[2].length();i++){
           eo.pressKey(KeyEvent.VK_BACK_SPACE);
       }
    }

}
