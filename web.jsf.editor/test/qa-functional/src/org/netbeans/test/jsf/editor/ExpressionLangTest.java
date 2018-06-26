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
package org.netbeans.test.jsf.editor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;

/**
 *
 * @author Vladimir Riha
 */
public class ExpressionLangTest extends GeneralJSF {

    public static String originalContent;
    
    public ExpressionLangTest(String args) {
        super(args);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(ExpressionLangTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(
               conf.addTest(
                "testOpenProject", "testBeansCompletionInsideBraces_hash", "testBeansCompletionNoClosingBrace_hash","testMixedEmbeddedJS_hash",
                "testMixedEmbeddCSS_hash", "testBeansCompletionInsideBraces_dollar", "testBeansCompletionNoClosingBrace_dollar","testMixedEmbeddedJS_dollar",
                "testMixedEmbeddCSS_dollar").enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testBeansCompletionInsideBraces_hash(){
        this.testBeansCompletionInsideBraces("#");
    }
    public void testBeansCompletionNoClosingBrace_hash(){
        this.testBeansCompletionNoClosingBrace("#");
    }
    public void testMixedEmbeddedJS_hash(){
        this.testMixedEmbeddedJS("#");
    }
    public void testMixedEmbeddCSS_hash(){
        this.testMixedEmbeddCSS("#");
    }
    public void testBeansCompletionInsideBraces_dollar(){
        this.testBeansCompletionInsideBraces("$");
    }
    public void testBeansCompletionNoClosingBrace_dollar(){
        this.testBeansCompletionNoClosingBrace("$");
    }
    public void testMixedEmbeddedJS_dollar(){
        this.testMixedEmbeddedJS("$");
    }
    public void testMixedEmbeddCSS_dollar(){
        this.testMixedEmbeddCSS("$");
    }
    
    public void testOpenProject() throws Exception {
        startTest();
        ExpressionLangTest.current_project = "sampleJSF";
        openProject(ExpressionLangTest.current_project);
        openFile("index.xhtml", ExpressionLangTest.current_project);
        EditorOperator eo = new EditorOperator("index.xhtml");
        ExpressionLangTest.originalContent = eo.getText();
        resolveServer(ExpressionLangTest.current_project);
        endTest();
    }
    
    @Override
    public void tearDown(){
        openFile("index.xhtml", ExpressionLangTest.current_project);
        EditorOperator eo = new EditorOperator("index.xhtml");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(ExpressionLangTest.originalContent);
    }

    public void testBeansCompletionInsideBraces(String elprefix) {
        startTest();
        EditorOperator eo = new EditorOperator("index.xhtml");
        eo.setCaretPositionToLine(25);
        type(eo, elprefix+"{");
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "innerBean", "session", "application"});
        eo.pressKey(KeyEvent.VK_ESCAPE);
        type(eo, "s");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "session"});
        this.clearLine(eo);
        endTest();
    }

  

    public void testBeansCompletionNoClosingBrace(String elprefix) {
        startTest();
        EditorOperator eo = new EditorOperator("index.xhtml");
        eo.setCaretPositionToLine(25);
        type(eo, elprefix+"{");
        eo.pressKey(KeyEvent.VK_DELETE);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "innerBean", "session", "application"});
        eo.pressKey(KeyEvent.VK_ESCAPE);
        type(eo, "s");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "session"});
        this.clearLine(eo);
        endTest();
    }

    public void testMixedEmbeddCSS(String elprefix) {
        startTest();
        EditorOperator eo = new EditorOperator("index.xhtml");
        eo.setCaretPositionToEndOfLine(11);
        type(eo, "\ncolor: "+elprefix+"{");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "innerBean", "session", "application"});
        eo.pressKey(KeyEvent.VK_ESCAPE);
        type(eo, "s");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "session"});
        checkCompletionDoesntContainItems(cjo, new String[]{"innerBean"});
        type(eo, "implebean.");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"ib", "msg"});
        type(eo, "ib");
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        type(eo, ";\n");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"animation", "background"});

        type(eo, "b");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionDoesntContainItems(cjo, new String[]{"animation"});
        checkCompletionItems(cjo, new String[]{"background"});

        type(eo, "ackground: ");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"no-repeat", "red"});

        endTest();
    }

    public void testMixedEmbeddedJS(String elprefix) {
        startTest();
        EditorOperator eo = new EditorOperator("index.xhtml");
        eo.setCaretPositionToEndOfLine(32);
        type(eo, "\nthis.total=\""+elprefix+"{}");
        eo.pressKey(KeyEvent.VK_LEFT);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "innerBean", "session", "application"});
        eo.pressKey(KeyEvent.VK_ESCAPE);
        type(eo, "s");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"simplebean", "session"});
        checkCompletionDoesntContainItems(cjo, new String[]{"innerBean"});
        type(eo, "implebean.");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"ib", "msg"});
        type(eo, "ib}");
        eo.pressKey(KeyEvent.VK_RIGHT);
        eo.pressKey(KeyEvent.VK_RIGHT);
        eo.pressKey(KeyEvent.VK_RIGHT);
        type(eo, ";");
        // check 0 errors
        evt.waitNoEvent(3000);
        

        // check JS completion
        eo.setCaretPositionToEndOfLine(41);
        type(eo, "a.");
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"name", "total", "print"});
        this.clearLine(eo);

        type(eo, "var d=new Number(); d.");
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"MAX_VALUE", "MIN_VALUE"});

        endTest();
    }
}
