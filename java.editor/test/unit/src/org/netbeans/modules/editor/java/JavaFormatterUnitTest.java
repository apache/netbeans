/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.java;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/**
 * Java formatter tests.
 *
 * @autor Miloslav Metelka
 */
public class JavaFormatterUnitTest extends JavaFormatterUnitTestCase {

    public JavaFormatterUnitTest(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    // indent new line tests
    
    public void testJavadocEnterNothingAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * text|\n"
                + " */\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * text\n"
                + " *|\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterTextAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * break|text\n"
                + " */\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * break\n"
                + " * |text\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterStarAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * text|*/\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * text\n"
                + " |*/\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintln() {
        setLoadDocumentText(
                "void m() {\n"
                + "    System.out.println(|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "void m() {\n"
                + "    System.out.println(\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnLineThree() {
        setLoadDocumentText(
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\"|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\"\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnAfterSemiColon() {
        setLoadDocumentText(
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\");|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\");\n"
                + "    |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineClassDeclaration() {
        setLoadDocumentText(
                "public class C\n"
                + "        implements Runnable\n"
                + "        throws Exception {|\n"
                + "}\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class C\n"
                + "        implements Runnable\n"
                + "        throws Exception {\n"
                + "    |\n"
                + "}\n"
                );
        
    }
    
    public void testEnterAfterIf() {
        setLoadDocumentText(
                "if (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterFor() {
        setLoadDocumentText(
                "if (int i = 0; i < 10; i++)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (int i = 0; i < 10; i++)\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterWhile() {
        setLoadDocumentText(
                "while (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "while (true)\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterDo() {
        setLoadDocumentText(
                "do|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "do\n"
                + "    |\n"
                );
    }
    
    
    public void testEnterAfterIfStmt() {
        setLoadDocumentText(
                "if (true)\n"
                + "    stmt;|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    stmt;\n"
                + "|\n"
                );
    }
    
    public void testEnterAfterIfElse() {
        setLoadDocumentText(
                "if (true)\n"
                + "    stmt;\n"
                + "else|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterIfElseStmt() {
        setLoadDocumentText(
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    stmt;|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    stmt;\n"
                + "|\n"
                );
    }
    
    public void testEnterAfterIfMultiLine() {
        setLoadDocumentText(
                "if (1 < 5|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (1 < 5\n"
                + "        |\n"
                );
    }
    
    public void testEnterAfterIfMultiLine2() {
        setLoadDocumentText(
                "if (1 < 5|)\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "if (1 < 5\n"
                + "        |)\n"
                );
    }
    
    // -------- Reformat tests -----------
    
    public void testReformatMultiLineSystemOutPrintln() {
        setLoadDocumentText(
                "void m() {\n"
                + "    System.out.println(\n"
                + "    \"haf\");\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\");\n"
                + "}\n"
                );
        
    }
    
    public void testReformatMultiLineClassDeclaration() {
        setLoadDocumentText(
                "public class C\n"
                + "implements Runnable\n"
                + "throws Exception {|\n"
                + "System.out.println(\"haf\");\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "public class C\n"
                + "        implements Runnable\n"
                + "        throws Exception {\n"
                + "    System.out.println(\"haf\");\n"
                + "}\n"
                );
        
    }
    
    // tests for regressions
    
    /**
     * Tests reformatting of new on two lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id6065
     */
    public void testReformatNewOnTwoLines() {
        setLoadDocumentText(
                "javax.swing.JPanel =\n" +
                "new java.swing.JPanel();");
        reformat();
        assertDocumentText("Incorrect new on two lines reformating",
                "javax.swing.JPanel =\n" +
                "        new java.swing.JPanel();");
    }
    
    /**
     * Tests reformatting of ternary conditional operators on multiple lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=23508
     */
    public void testReformatTernaryConditionalOperator() {
        setLoadDocumentText(
                "something = (someComplicatedExpression != null) ?\n" +
                "(aComplexCalculation) :\n" +
                "(anotherComplexCalculation);");
        reformat();
        assertDocumentText("Incorrect ternary conditional operator reformatting",
                "something = (someComplicatedExpression != null) ?\n" +
                "    (aComplexCalculation) :\n" +
                "    (anotherComplexCalculation);");
    }
    
    
    /**
     * Test reformatting of array initializer with newlines on
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=47069
     */
    public void testReformatArrayInitializerWithNewline() {
//        Settings.setValue(JavaKit.class, JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE, Boolean.TRUE);
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        String originalPlacement = prefs.get(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE.toString());
        assertTrue(!originalPlacement.equals(CodeStyle.BracePlacement.NEW_LINE.toString()));
        prefs.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE.toString());
        setLoadDocumentText(
                "int[] foo = new int[] {1, 2, 3};");
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "int[] foo = new int[] {1, 2, 3};");
        prefs.put(FmtOptions.methodDeclBracePlacement, originalPlacement);
    }
    
    /**
     * Test reformatting of newline braces to normal ones
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=48926
     */
    public void testReformatNewlineBracesToNormalOnes() {
        setLoadDocumentText(
                "try\n" +
                "{\n" +
                "System.out.println(\"test\");\n" +
                "}\n" +
                "catch (ClassCastException e)\n" +
                "{\n" +
                "System.err.println(\"exception\");\n" +
                "}");
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "try {\n" +
                "    System.out.println(\"test\");\n" +
                "} catch (ClassCastException e) {\n" +
                "    System.err.println(\"exception\");\n" +
                "}");
    }
    
    /**
     * Test reformatting of multiline constructors
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=49450
     */
    public void testReformatMultilineConstructor() {
        setLoadDocumentText(
                "public class Test {\n" +
                "public Test(int one,\n" +
                "int two,\n" +
                "int three,\n" +
                "int four) {\n" +
                "this.one = one;\n" +
                "}\n" +
                "}");
        reformat();
        assertDocumentText("Incorrect multiline constructor reformatting",
                "public class Test {\n" +
                "    public Test(int one,\n" +
                "            int two,\n" +
                "            int three,\n" +
                "            int four) {\n" +
                "        this.one = one;\n" +
                "    }\n" +
                "}");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets() {
        setLoadDocumentText(
                "if (count == 0)\n" +
                "return 0.0f;\n" +
                "else\n" +
                "return performanceSum / getCount()");
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (count == 0)\n" +
                "    return 0.0f;\n" +
                "else\n" +
                "    return performanceSum / getCount()");
    }
    
}
