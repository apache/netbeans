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
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests new Expression Language 3.0
 *
 * @author Vladimir Riha
 */
public class ExpressionLang30Test extends GeneralJSF {

    public static String originalContent;

    public ExpressionLang30Test(String args) {
        super(args);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(ExpressionLang30Test.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(
                conf.addTest(
                "testOpenProject",
                "testNoErrors",
                "testCollection",
                "testChainedCall",
                "testCollectionDetailed",
                "testCollectionConstructor"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        ExpressionLang30Test.current_project = "sampleJSF22";
        openProject(ExpressionLang30Test.current_project);
        openFile("el30.xhtml", ExpressionLang30Test.current_project);
        EditorOperator eo = new EditorOperator("el30.xhtml");
        ExpressionLang30Test.originalContent = eo.getText();
        resolveServer(ExpressionLang30Test.current_project);
        endTest();
    }

    public void testNoErrors() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.xhtml");
        evt.waitNoEvent(1000);
        Object[] annotations = getAnnotations(eo, 0);
        assertEquals("Unexpected number of annotations", 1, annotations.length);
        endTest();
    }

    public void testCollection() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.xhtml");
        eo.setCaretPositionToEndOfLine(9);
        type(eo, "\n #{[1,2].");
        evt.waitNoEvent(500);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(2000);
        String t = eo.getText(eo.getLineNumber());
        assertTrue("Incorrect autocompletion: "+t, t.indexOf("#{[1,2].stream()}") > -1);
        this.clearLine(eo);
        endTest();
    }

    public void testCollectionDetailed() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.xhtml");
        eo.setCaretPositionToEndOfLine(9);
        type(eo, "\n #{[1,2].stream().");
        eo.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"allMatch", "average", "distinct"});
        type(eo, "m");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"map", "max", "min"});
        checkCompletionDoesntContainItems(cjo, new String[]{"distinct"});
//        type(eo, "ap()."); // issue 232901
//        eo.typeKey(' ', InputEvent.CTRL_MASK);
//        evt.waitNoEvent(1000);
//        completion = getCompletion();
//        cjo = completion.listItself;
//        checkCompletionItems(cjo, new String[]{"map", "average", "distinct"});
        this.clearLine(eo);
        endTest();
    }
    
    public void testCollectionConstructor() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.xhtml");
        eo.setCaretPositionToEndOfLine(9);
        type(eo, "\n  ${v = {\"one\":1, \"two\":2, \"three\":3}; v.");
        eo.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        assertTrue("Incorrect autocompletion: "+eo.getText(), eo.getText().indexOf("v.stream()}") > -1);
        this.clearLine(eo);
        endTest();
    }

    public void testChainedCall() {
        startTest();
        EditorOperator eo = new EditorOperator("el30.xhtml");
        eo.setCaretPositionToEndOfLine(9);
        type(eo, "\n #{simplebean[\"setMsg\"](\"test\");simplebean.");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"ib", "msg", "print"});
        this.clearLine(eo);
        endTest();
    }

    @Override
    public void tearDown() {
        openFile("el30.xhtml", ExpressionLang30Test.current_project);
        EditorOperator eo = new EditorOperator("el30.xhtml");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(ExpressionLang30Test.originalContent);
    }
}
