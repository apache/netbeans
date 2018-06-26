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
package org.netbeans.test.syntax;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import javax.swing.KeyStroke;

/**
 *
 * @author Vladimir Riha
 */
public class MiscellaneousSanityTest extends GeneralJSP {

    public MiscellaneousSanityTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(MiscellaneousSanityTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(conf.addTest(
                "openProject",
                "testNavigateForward",
                "testNavigateInclude",
                "testNavigateTaglib",
                "testCommentHTML",
                "testUncommentHTML",
                "testCommentScriptlet",
                "testUncommentScriptlet"
        ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        MiscellaneousSanityTest.current_project = "sampleJSP";
        openProject(MiscellaneousSanityTest.current_project);
        resolveServer(MiscellaneousSanityTest.current_project);
        openFile("nav.jsp", MiscellaneousSanityTest.current_project);
        endTest();
    }

    public void testNavigateForward() {
        startTest();
        navigate("nav.jsp", "newtag_file.tag", 9, 44, 1, 1);
        endTest();
    }

    public void testNavigateInclude() {
        startTest();
        navigate("nav.jsp", "index.jsp", 10, 30, 1, 1);
        endTest();
    }

    public void testNavigateTaglib() {
        startTest();
        navigate("nav.jsp", "newtag_file.tag", 12, 19, 1, 1);
        endTest();
    }

    public void testCommentHTML() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(11, 18);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect HTML comment", "        <!--<h1>Hello World!</h1>-->\n", eo.getText(eo.getLineNumber()));
        endTest();
    }
    public void testUncommentHTML() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(11, 18);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect HTML comment", "        <h1>Hello World!</h1>\n", eo.getText(eo.getLineNumber()));
        endTest();
    }

    
    public void testCommentScriptlet() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(14, 23);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect scriptlet comment", "//            String a = \"\";\n", eo.getText(eo.getLineNumber()));
        endTest();
    }
    public void testUncommentScriptlet() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(14, 23);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect scriptlet comment", "            String a = \"\";\n", eo.getText(eo.getLineNumber()));
        endTest();
    }
    
    public void navigate(String fromFile, String toFile, int fromLine, int fromColumn, int toLine, int toColumn) {
        EditorOperator eo = new EditorOperator(fromFile);
        eo.setCaretPosition(fromLine, fromColumn);
        evt.waitNoEvent(200);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_B, 2)).performShortcut(eo);
        evt.waitNoEvent(500);
        try {
            EditorOperator ed = new EditorOperator(toFile);
            int position = ed.txtEditorPane().getCaretPosition();
            ed.setCaretPosition(toLine, toColumn);
            int expectedPosition = ed.txtEditorPane().getCaretPosition();
            assertTrue("Incorrect caret position. Expected position " + expectedPosition + " but was " + position, position == expectedPosition);
            if (!fromFile.equals(toFile)) {
                ed.close(false);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

}
