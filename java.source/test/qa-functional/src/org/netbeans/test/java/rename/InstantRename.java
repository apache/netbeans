/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.rename;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.JavaTestCase;

/**
 *
 * @author Jiri Prox
 */
public class InstantRename extends JavaTestCase {

    public InstantRename(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() {
        try {            
            openDefaultProject();
            openSourceFile("org.netbeans.test.java.rename", "Rename");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.toString(), false);
        }
    }

    @Override
    protected void tearDown() {
        EditorOperator eo = new EditorOperator("Rename");
        eo.close(false);        
    }

    private void clearEditor(EditorOperator eo) {
        String text = eo.getText();
        eo.delete(0, text.length());
    }
    
    private void perform(String input, int x, int y, String rename, String golden) {
        EditorOperator eo = new EditorOperator("Rename");
        clearEditor(eo);
        eo.insert(input);
        eo.setCaretPosition(x, y);        
        eo.pressKey(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
        eo.txtEditorPane().typeText(rename);
        String result = eo.getText();
        assertEquals(golden, result);
    }

    public void testRenameMethod() throws Exception {
        String input = "" +
                "class Rename {\n" +
                "    private void method() {\n" +
                "        method()\n"+
                "    }\n" +
                "}\n";
        String golden = "" +
                "class Rename {\n" +
                "    private void renamed() {\n" +
                "        renamed()\n"+
                "    }\n" +
                "}\n";
        perform(input, 2, 20, "renamed", golden);

    }

    public void testRenameInnerClass() throws Exception {
        String input = "" +
                "public class Rename {\n"+
                "    private class Inner {}\n"+
                "    Inner x;\n"+
                "}";
        String golden = "" +
                "public class Rename {\n"+
                "    private class Renamed {}\n"+
                "    Renamed x;\n"+
                "}";
        perform(input, 2, 23, "Renamed", golden);
    }

    public void testRenameParameter() {
        String input = "" +
            "public class Rename {\n"+
            "    /**     \n"+
            "     * @param x Parameter\n"+
            "     */\n"+
            "    public void test(int x) {\n"+
            "        x = 2;\n"+
            "    }\n"+
            "}\n";
        String golden = "" +
            "public class Rename {\n"+
            "    /**     \n"+
            "     * @param ren Parameter\n"+
            "     */\n"+
            "    public void test(int ren) {\n"+
            "        ren = 2;\n"+
            "    }\n"+
            "}\n";
        perform(input, 5, 26, "ren", golden);
    }

    public void testRenameField() {
        String input = "" +
            "public class Rename {\n"+
            "    private int field;\n"+
            "    void m() {\n"+
            "       field = 3;\n"+
            "    }\n"+
            "}\n";
        String golden = "" +
            "public class Rename {\n"+
            "    private int ren;\n"+
            "    void m() {\n"+
            "       ren = 3;\n"+
            "    }\n"+
            "}\n";
        perform(input, 2, 20, "ren", golden);
    }

    public void testRenameGenericsClass() {
        String input = "" +
            "public class Rename<X> {\n"+
            "    X i;   \n"+
            "    public void m(X a) {}\n"+
            "}\n";
        String golden = "" +
            "public class Rename<Ren> {\n"+
            "    Ren i;   \n"+
            "    public void m(Ren a) {}\n"+
            "}\n";
        perform(input, 1, 21, "Ren", golden);
    }

    public void testRenameGenericsMethod() {
        String input = "" +
            "public class Rename {\n"+
            "    public <X> X get(X a){return a;}\n"+
            "}\n";
        String golden = "" +
            "public class Rename {\n"+
            "    public <Ren> Ren get(Ren a){return a;}\n"+
            "}\n";
        perform(input, 2, 13, "Ren", golden);
    }

    public void testRenameUndo() {
        String input = "" +
            "public class Rename {\n"+
            "    public <X> X get(X a){return a;}\n"+
            "}\n";
        String golden = "" +
            "public class Rename {\n"+
            "    public <Ren> Ren get(Ren a){return a;}\n"+
            "}\n";
        perform(input, 2, 13, "Ren", golden);
        EditorOperator eo = new EditorOperator("Rename");
        eo.pressKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);        
        String result = eo.getText();
        assertEquals(input, result);
    }

    public void testRenamePart() {
        String input = "" +
            "public class Rename {\n"+
            "    private void method() {\n"+
            "        method();\n"+
            "    }\n"+
            "} \n";
        String golden = "" +
            "public class Rename {\n"+
            "    private void meXXod() {\n"+
            "        meXXod();\n"+
            "    }\n"+
            "} \n";
        EditorOperator eo = new EditorOperator("Rename");
        clearEditor(eo);
        eo.insert(input);
        eo.setCaretPosition(2, 20);
        eo.pressKey(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
        eo.pressKey(KeyEvent.VK_LEFT);
        eo.pressKey(KeyEvent.VK_RIGHT);
        eo.pressKey(KeyEvent.VK_RIGHT);
        eo.txtEditorPane().typeText("XX");
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.pressKey(KeyEvent.VK_DELETE);
        String result = eo.getText();
        assertEquals(golden, result);
    }

    public void testRenameCopyPaste() {
        String input = "" +
            "public class Rename {\n"+
            "    private void method() {\n"+
            "        method();\n"+
            "    }\n"+
            "} \n";
        String golden = "" +
            "public class Rename {\n"+
            "    private void Rename() {\n"+
            "        Rename();\n"+
            "    }\n"+
            "} \n";
        EditorOperator eo = new EditorOperator("Rename");
        clearEditor(eo);
        eo.insert(input);
        eo.setCaretPosition(1, 16);
        eo.pressKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK); // select identifier
        eo.pressKey(KeyEvent.VK_C,KeyEvent.CTRL_DOWN_MASK);
        eo.setCaretPosition(2, 20);
        eo.pressKey(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
        eo.pressKey(KeyEvent.VK_V,KeyEvent.CTRL_DOWN_MASK);
        String result = eo.getText();
        assertEquals(golden, result);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite
                .createConfiguration(InstantRename.class)
                .addTest(InstantRename.class,
                    "testRenameMethod",
                    "testRenameInnerClass",
                    "testRenameParameter",
                    "testRenameField",
                    "testRenameGenericsClass",
                    "testRenameGenericsMethod",
                    "testRenameUndo",
                    "testRenamePart",
                    "testRenameCopyPaste")
                .clusters(".*")
                .enableModules(".*")
           );
    }

}
