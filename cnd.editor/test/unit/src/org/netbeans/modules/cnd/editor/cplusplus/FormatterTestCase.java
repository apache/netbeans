/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testReformatSimpleClass
 *
 */
public class FormatterTestCase extends EditorBase {

    public FormatterTestCase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void assertDocumentText(String msg, String expectedText) {
        super.assertDocumentText(msg, expectedText);
        reformat();
        super.assertDocumentText(msg+" (not stable)", expectedText);
    }

    // -------- Reformat tests -----------
    
    public void testReformatMultiLineSystemOutPrintln() {
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "    \"haf\");\n"
                + "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m()\n"
                + "{\n"
                + "    printf(\n"
                + "            \"haf\");\n"
                + "}\n"
                    );
    }

    public void testReformatMultiLineSystemOutPrintln2() {
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "    \"haf\");\n"
                + "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\");\n"
                + "}\n"
                );
    }
    
    public void testReformatMultiLineSystemOutPrintln3() {
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "    \"haf\");\n"
                + "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m() {\n"
                + "    printf(\n"
                + "           \"haf\");\n"
                + "}\n"
                );
    }

    public void testReformatMultiLineClassDeclaration() {
        setLoadDocumentText(
                "class C\n"
                + ": public Runnable {\n"
                + "int printf(int);\n"
                + "};\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "class C\n"
                + ": public Runnable\n"
                + "{\n"
                + "    int printf(int);\n"
                + "};\n"
                );
    }
    
    // tests for regressions
    
    /**
     * Tests reformatting of new on two lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id6065
     */
    public void testReformatNewOnTwoLines() {
        setLoadDocumentText(
                "javax::swing::JPanel* panel =\n" +
                "new java::swing::JPanel();");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect new on two lines reformating",
                "javax::swing::JPanel* panel =\n" +
                "        new java::swing::JPanel();");
    }

    /**
     * Tests reformatting of ternary conditional operators on multiple lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=23508
     */
    public void testReformatTernaryConditionalOperator() {
        setLoadDocumentText(
                "void foo()\n"+
                "{\n"+
                "something = (someComplicatedExpression != null) ?\n" +
                "(aComplexCalculation) :\n" +
                "(anotherComplexCalculation);\n"+
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect ternary conditional operator reformatting",
                "void foo()\n"+
                "{\n"+
                "    something = (someComplicatedExpression != null) ?\n" +
                "            (aComplexCalculation) :\n" +
                "            (anotherComplexCalculation);\n"+
                "}\n");
    }
    
    /**
     * Test reformatting of array initializer with newlines on
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=47069
     */
    public void testReformatArrayInitializerWithNewline() {
        setLoadDocumentText(
                "int[] foo =  {1, 2, 3};\n" +
                "int[] foo2 =  {1,\n" +
                "2, 3};\n" +
                "int[] foo3 = {\n" +
                "1, 2, 3\n" +
                "};\n" +
                "\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "int[] foo = {1, 2, 3};\n" +
                "int[] foo2 = {1,\n" +
                "    2, 3};\n" +
                "int[] foo3 = {\n" +
                "    1, 2, 3\n" +
                "};\n" +
                "\n");
    }

    /**
     * Test reformatting of array initializer with newlines on
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=47069
     */
    public void testReformatArrayInitializerWithNewline2() {
        setLoadDocumentText(
                "int[][] foo4 =  {\n" +
                "{1, 2, 3},\n" +
                "{3,4,5},\n" +
                "{7,8,9}\n" +
                "};\n" +
                "\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "int[][] foo4 = {\n" +
                "    {1, 2, 3},\n" +
                "    {3, 4, 5},\n" +
                "    {7, 8, 9}\n" +
                "};\n" +
                "\n");
    }
    
    /**
     * Test reformatting of newline braces to normal ones
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=48926
     */
    public void testReformatNewlineBracesToNormalOnes() {
        setLoadDocumentText(
                "try\n" +
                "{\n" +
                "printf(\"test\");\n" +
                "}\n" +
                "catch (Exception e)\n" +
                "{\n" +
                "printf(\"exception\");\n" +
                "}");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineCatch, true);
        reformat();
        assertDocumentText("Incorrect try-catch reformatting",
                "try {\n" +
                "    printf(\"test\");\n" +
                "}\n"+
                "catch (Exception e) {\n" +
                "    printf(\"exception\");\n" +
                "}");
    }

    public void testReformatNewlineBracesToNormalOnes1() {
        setLoadDocumentText(
                "try\n" +
                "{\n" +
                "printf(\"test\");\n" +
                "}\n" +
                "catch (Exception e)\n" +
                "{\n" +
                "printf(\"exception\");\n" +
                "}");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineCatch, true);
        reformat();
        assertDocumentText("Incorrect try-catch reformatting",
                "try {\n" +
                "    printf(\"test\");\n" +
                "}\n"+
                "catch (Exception e) {\n" +
                "    printf(\"exception\");\n" +
                "}");
    }
    
    public void testReformatNewlineBracesToNormalOnes2() {
        setLoadDocumentText(
                "	void testError(CuTest *tc){\n" +
                "		IndexReader* reader = NULL;\n" +
                "		try{\n" +
                "			RAMDirectory dir;\n" +
                "		}catch(CLuceneError& a){\n" +
                "			_CLDELETE(reader);\n" +
                "		}catch(...){\n" +
                "			CuAssert(tc,_T(\"Error did not catch properly\"),false);\n" +
                "		}\n" +
                "	}\n" +
                "\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect try-catch reformatting",
                "void testError(CuTest *tc)\n" +
                "{\n" +
                "    IndexReader* reader = NULL;\n" +
                "    try {\n" +
                "        RAMDirectory dir;\n" +
                "    } catch (CLuceneError& a) {\n" +
                "        _CLDELETE(reader);\n" +
                "    } catch (...) {\n" +
                "        CuAssert(tc, _T(\"Error did not catch properly\"), false);\n" +
                "    }\n" +
                "}\n" +
                "\n");
    }

    public void testReformatNewlineBracesToNormalOnes3() {
        setLoadDocumentText(
                "try {\n" +
                "    printf(\"test\");\n" +
                "}\n" +
                "catch ( IllegalStateException illegalStateException  ) {\n" +
                "    illegalStateException.printStackTrace();\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect tabbed catch reformatting",
                "try {\n" +
                "    printf(\"test\");\n" +
                "} catch (IllegalStateException illegalStateException) {\n" +
                "    illegalStateException.printStackTrace();\n" +
                "}\n");
    }

    
    /**
     * Test reformatting of multiline constructors
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=49450
     */
    public void testReformatMultilineConstructor() {
        setLoadDocumentText(
                "class Test {\n" +
                "Test(int one,\n" +
                "int two,\n" +
                "int three,\n" +
                "int four) {\n" +
                "this.one = one;\n" +
                "}\n" +
                "};");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect multiline constructor reformatting",
                "class Test {\n" +
                "    Test(int one,\n" +
                "            int two,\n" +
                "            int three,\n" +
                "            int four) {\n" +
                "        this.one = one;\n" +
                "    }\n" +
                "};");
    }

    /**
     * Test reformatting of multiline constructors
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=49450
     */
    public void testReformatMultilineConstructor2() {
        setLoadDocumentText(
                "class Test {\n" +
                "Test(int one,\n" +
                "int two,\n" +
                "int three,\n" +
                "int four) {\n" +
                "this.one = one;\n" +
                "}\n" +
                "};");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect multiline constructor reformatting",
                "class Test\n" +
                "{\n" +
                "    Test(int one,\n" +
                "            int two,\n" +
                "            int three,\n" +
                "            int four)\n" +
                "    {\n" +
                "        this.one = one;\n" +
                "    }\n" +
                "};");
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
                "return performanceSum / getCount();\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (count == 0)\n" +
                "    return 0.0f;\n" +
                "else\n" +
                "    return performanceSum / getCount();\n");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets2() {
        setLoadDocumentText(
                "if (count == 0)\n" +
                "return 0.0f;\n" +
                "else  {\n" +
                "return performanceSum / getCount();\n"+
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (count == 0)\n" +
                "    return 0.0f;\n" +
                "else {\n" +
                "    return performanceSum / getCount();\n" +
                "}\n");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets3() {
        setLoadDocumentText(
                "if (true) if (true) if (true)\n" +
                "else return;\n" +
                "else return;\n" +
                "else return;\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (true) if (true) if (true)\n" +
                "        else return;\n" +
                "    else return;\n" +
                "else return;\n");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets4() {
        setLoadDocumentText(
                "if (true)\n" +
                "    if (true)\n" +
                "    if (true)\n" +
                "else return;\n" +
                "else return;\n" +
                "else return;\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (true)\n" +
                "    if (true)\n" +
                "        if (true)\n" +
                "        else return;\n" +
                "    else return;\n" +
                "else return;\n");
    }
    
    /**
     * Test reformatting of class
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=97544
     */
    public void testReformatSimpleClass() {
        setLoadDocumentText(
            "class C {\n" +
            "protected:\n" +
            "int i;\n" +
            "int foo();\n" +
            "private:\n" +
            "int j;\n" +
            "public:\n" +
            "int k;\n" +
            "};\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect reformatting of simple class",
            "class C {\n" +
            "protected:\n" +
            "    int i;\n" +
            "    int foo();\n" +
            "private:\n" +
            "    int j;\n" +
            "public:\n" +
            "    int k;\n" +
            "};\n");
    }
    
    /**
     * Test reformatting of class
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=97544
     */
    public void testReformatSimpleClass2() {
        setLoadDocumentText(
            "class C {\n" +
            "protected:\n" +
            "int i;\n" +
            "int foo();\n" +
            "private:\n" +
            "int j;\n" +
            "public:\n" +
            "int k;\n" +
            "};\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect reformatting of simple class",
            "class C\n" +
            "{\n" +
            "protected:\n" +
            "    int i;\n" +
            "    int foo();\n" +
            "private:\n" +
            "    int j;\n" +
            "public:\n" +
            "    int k;\n" +
            "};\n");
    }

    /**
     * Test reformatting of For without braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=98475
     */
    public void testReformatForWithoutBraces() {
        setLoadDocumentText(
            "for (i = 0; i < MAXBUCKET; i++) {\n" +
	    "for (j = 0; j < MAXBUCKET; j++)\n" +
            "if (i != j) {\n" +
            "if (isempty()) {\n" +
            "pour(current, i, j);\n" +
            "insCnf(current);\n" +
            "}\n" +
            "}\n" +
            "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of For without braces",
            "for (i = 0; i < MAXBUCKET; i++) {\n" +
	    "    for (j = 0; j < MAXBUCKET; j++)\n" +
            "        if (i != j) {\n" +
            "            if (isempty()) {\n" +
            "                pour(current, i, j);\n" +
            "                insCnf(current);\n" +
            "            }\n" +
            "        }\n" +
            "}\n");
    }

    /**
     * Test reformatting for preprocessors directives
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=100665
     */
    public void testReformatPreprocessorsDirectives() {
        setLoadDocumentText(
            "main() {\n" +
            "#define AAA 1\n" +
            "int aaa;\n" +
            "#define BBB 2\n" +
            "long bbb;\n" +
            "int ccc;\n" +
            "int ddd;\n" +
            "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect reformatting for preprocessors directives",
            "main() {\n" +
            "#define AAA 1\n" +
            "    int aaa;\n" +
            "#define BBB 2\n" +
            "    long bbb;\n" +
            "    int ccc;\n" +
            "    int ddd;\n" +
            "}\n");
    }

    /**
     * Test reformatting for preprocessors directives
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=100665
     */
    public void testReformatPreprocessorsDirectives2() {
        setLoadDocumentText(
            "main() {\n" +
            "#define AAA 1\n" +
            "int aaa;\n" +
            "#define BBB 2\n" +
            "long bbb;\n" +
            "int ccc;\n" +
            "int ddd;\n" +
            "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting for preprocessors directives",
            "main()\n" +
            "{\n" +
            "#define AAA 1\n" +
            "    int aaa;\n" +
            "#define BBB 2\n" +
            "    long bbb;\n" +
            "    int ccc;\n" +
            "    int ddd;\n" +
            "}\n");
    }

    /**
     * Test reformatting of function arguments list
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=115628
     */
    public void testReformatFunctionArguments() {
        setLoadDocumentText(
            "int foo(int z){\n" +
            "z += myfoo(a,\n" +
            "b,\n" +
            "c);\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        reformat();
        assertDocumentText("Incorrect reformatting of function arguments list",
            "int foo(int z)\n" +
            "{\n" +
            "    z += myfoo(a,\n" +
            "               b,\n" +
            "               c);\n" +
            "}\n");
    }
    
    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatConstructorInitializer() {
        setLoadDocumentText(
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units) {\n" +
            "ComputeSupportMetric();\n" +
            "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect reformatting of constructor initializer",
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units) {\n" +
            "    ComputeSupportMetric();\n" +
            "}\n");
    }
    
    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatConstructorInitializer2() {
        setLoadDocumentText(
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units) {\n" +
            "ComputeSupportMetric();\n" +
            "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of constructor initializer",
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units)\n" +
            "{\n" +
            "    ComputeSupportMetric();\n" +
            "}\n");
    }

    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatMultilineMainDefinition() {
        setLoadDocumentText(
            "int\n" +
            "main(int argc, char** argv) {\n" +
            "return (EXIT_SUCCESS);\n" +
            "};\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect reformatting of multi line main definition",
            "int\n" +
            "main(int argc, char** argv) {\n" +
            "    return (EXIT_SUCCESS);\n" +
            "};\n");
    }

    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatMultilineMainDefinition2() {
        setLoadDocumentText(
            "int\n" +
            "main(int argc, char** argv) {\n" +
            "return (EXIT_SUCCESS);\n" +
            "};\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of multi line main definition",
            "int\n" +
            "main(int argc, char** argv)\n" +
            "{\n" +
            "    return (EXIT_SUCCESS);\n" +
            "};\n");
    }

    /**
     * Test reformatting of unbalanced braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
     */
    public void testReformatUnbalancedBraces() {
        setLoadDocumentText(
            "void foo() {\n" +
            "#if A\n" +
            "if (0) {\n" +
            "#else\n" +
            "if (1) {\n" +
            "#endif\n" +
            "}\n" +
            "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect reformatting of unbalanced braces",
            "void foo()\n" +
            "{\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "    }\n" +
            "}\n");
    }

    public void testIdentInnerEnum() {
        setLoadDocumentText(
            "class NdbTransaction {\n" +
            "#ifndef D\n" +
            "friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "enum AbortOption {\n" +
            "#ifndef D\n" +
            "AbortOnError=::AbortOnError,\n" +
            "#endif\n" +
            "AO_IgnoreError=::AO_IgnoreError,\n" +
            "AO_SkipError\n" +
            "};\n" +
            "};\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeClass, 1);
        reformat();
        assertDocumentText("Incorrect identing of inner enum",
            "class NdbTransaction {\n" +
            "#ifndef D\n" +
            "    friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    enum AbortOption {\n" +
            "#ifndef D\n" +
            "        AbortOnError = ::AbortOnError,\n" +
            "#endif\n" +
            "        AO_IgnoreError = ::AO_IgnoreError,\n" +
            "        AO_SkipError\n" +
            "    };\n" +
            "};\n"
        );
    }

    public void testIdentInnerEnum2() {
        setLoadDocumentText(
            "class NdbTransaction {\n" +
            "#ifndef D\n" +
            "friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "enum AbortOption {\n" +
            "#ifndef D\n" +
            "AbortOnError=::AbortOnError,\n" +
            "#endif\n" +
            "AO_IgnoreError=::AO_IgnoreError,\n" +
            "AO_SkipError\n" +
            "};\n" +
            "};\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeClass, 0);
        reformat();
        assertDocumentText("Incorrect identing of inner enum",
            "class NdbTransaction\n" +
            "{\n" +
            "#ifndef D\n" +
            "    friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "    enum AbortOption\n" +
            "    {\n" +
            "#ifndef D\n" +
            "        AbortOnError = ::AbortOnError,\n" +
            "#endif\n" +
            "        AO_IgnoreError = ::AO_IgnoreError,\n" +
            "        AO_SkipError\n" +
            "    };\n" +
            "};\n"
        );
    }

    public void testTemplate() {
        setLoadDocumentText(
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U> {\n" +
            "public:\n" +
            "KeyTable2(ArrayPool<U>& pool) :\n" +
            "DLHashTable2<T, U>(pool) {\n" +
            "}\n" +
            "\n" +
            "bool find(Ptr<T>& ptr, const T& rec) const {\n" +
            "return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "}\n" +
            "};\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing of template class",
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U> {\n" +
            "public:\n" +
            "    KeyTable2(ArrayPool<U>& pool) :\n" +
            "    DLHashTable2<T, U>(pool) {\n" +
            "    }\n" +
            "    bool find(Ptr<T>& ptr, const T& rec) const {\n" +
            "        return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "    }\n" +
            "};\n"
            );
    }

    public void testTemplate2() {
        setLoadDocumentText(
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U> {\n" +
            "public:\n" +
            "KeyTable2(ArrayPool<U>& pool) :\n" +
            "DLHashTable2<T, U>(pool) {\n" +
            "}\n" +
            "\n" +
            "bool find(Ptr<T>& ptr, const T& rec) const {\n" +
            "return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "}\n" +
            "};\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 1);
        reformat();
        assertDocumentText("Incorrect identing of template class",
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U>\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    KeyTable2(ArrayPool<U>& pool) :\n" +
            "    DLHashTable2<T, U>(pool)\n" +
            "    {\n" +
            "    }\n" +
            "\n" +
            "    bool find(Ptr<T>& ptr, const T& rec) const\n" +
            "    {\n" +
            "        return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "    }\n" +
            "};\n"
            );
    }
    
    public void testIdentPreprocessorElase() {
        setLoadDocumentText(
            "#if defined(USE_MB)\n" +
            "if (use_mb(cs)) {\n" +
            "result_state = IDENT_QUOTED;\n" +
            "}\n" +
            "#endif\n" +
            "{\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing of preprocessor else",
            "#if defined(USE_MB)\n" +
            "if (use_mb(cs)) {\n" +
            "    result_state = IDENT_QUOTED;\n" +
            "}\n" +
            "#endif\n" +
            "{\n" +
            "}\n"
        );
    }
    
    public void testIdentDefine() {
        setLoadDocumentText(
            "int\n" +
            "main() {\n" +
            "int z;\n" +
            "#define\tX \\\n" +
            "\ta+\\\n" +
            "\t    b+ \\\n" +
            "        c \n" +
            "z++;\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing of preprocessor else",
            "int\n" +
            "main()\n" +
            "{\n" +
            "    int z;\n" +
            "#define X \\\n" +
            "        a+\\\n" +
            "            b+ \\\n" +
            "        c \n" +
            "    z++;\n" +
            "}\n"
        );
    }

    public void testIdentMultyLineMain() {
        setLoadDocumentText(
            "long z;\n" +
            "int\n" +
            "main() {\n" +
            "short a;\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing multyline main",
            "long z;\n" +
            "int\n" +
            "main() {\n" +
            "    short a;\n" +
            "}\n"
        );
    }

    public void testIdentMultyLineMain2() {
        setLoadDocumentText(
            "long z;\n" +
            "int\n" +
            "main() {\n" +
            "short a;\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing multyline main",
            "long z;\n" +
            "int\n" +
            "main()\n" +
            "{\n" +
            "    short a;\n" +
            "}\n"
        );
    }
    
    public void testIdentMultyConstructor() {
        setLoadDocumentText(
            "Log_event::Log_event(uint flags_arg, bool using_trans)\n" +
            "        :log_pos(0), temp_buf(0), exec_time(0), flags(flags_arg), thd(thd_arg)\n" +
            "        {\n" +
            "                server_id=thd->server_id;\n" +
            "        }\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Log_event::Log_event(uint flags_arg, bool using_trans)\n" +
            ": log_pos(0), temp_buf(0), exec_time(0), flags(flags_arg), thd(thd_arg) {\n" +
            "    server_id = thd->server_id;\n" +
            "}\n"
        );
    }

    public void testIdentMultyConstructor2() {
        setLoadDocumentText(
            "Log_event::Log_event(const char* buf,\n" +
            "        const Format_description_log_event* description_event)\n" +
            "        :temp_buf(0), cache_stmt(0)\n" +
            "        {\n" +
            "                server_id=thd->server_id;\n" +
            "        }\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceAfterColon, false);
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Log_event::Log_event(const char* buf,\n" +
            "        const Format_description_log_event* description_event)\n" +
            ":temp_buf(0), cache_stmt(0)\n" +
            "{\n" +
            "    server_id = thd->server_id;\n" +
            "}\n"
        );
    }

    public void testIdentMultyConstructor3() {
        setLoadDocumentText(
            "Log_event::Log_event(const char* buf,\n" +
            "        const Format_description_log_event* description_event)\n" +
            "        :temp_buf(0), cache_stmt(0)\n" +
            "        {\n" +
            "                server_id=thd->server_id;\n" +
            "        }\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceAfterColon, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Log_event::Log_event(const char* buf,\n" +
            "                     const Format_description_log_event* description_event)\n" +
            ":temp_buf(0), cache_stmt(0)\n" +
            "{\n" +
            "    server_id = thd->server_id;\n" +
            "}\n"
        );
    }

    public void testIdentMultyConstructor4() {
        setLoadDocumentText(
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "public:\n" +
            "class IndexReaderCommitLockWith : \n" +
            "public CL_NS(store)::LuceneLockWith\n" +
            "{\n" +
            "private:\n" +
            "IndexReader* reader;\n" +
            "};\n" +
            "};\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    class IndexReaderCommitLockWith :\n" +
            "    public CL_NS(store)::LuceneLockWith\n" +
            "    {\n" +
            "    private:\n" +
            "        IndexReader* reader;\n" +
            "    };\n" +
            "};\n"
        );
    }
    

    public void testIdentDefineBrace() {
        setLoadDocumentText(
            "#define BRACE {\n" +
            "int main() {\n" +
            "if (a) {\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing define brace",
            "#define BRACE {\n" +
            "int main()\n" +
            "{\n" +
            "    if (a) {\n" +
            "    }\n" +
            "}\n"
        );
    }
    
    public void testIdentDefineBrace2() {
        setLoadDocumentText(
            "#define BRACE }\n" +
            "int main() {\n" +
            "if (a) {\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing define brace",
            "#define BRACE }\n" +
            "int main()\n" +
            "{\n" +
            "    if (a) {\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testMacroDefineWithBrace() {
        setLoadDocumentText(
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
            );
        setDefaultsOptions();
        reformat();
            assertDocumentText("Incorrect formatting for macro define with brace",
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
        );
    }

    public void testMacroDefineWithBrace1() {
        setLoadDocumentText(
            "\n"+
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
            );
        setDefaultsOptions();
        reformat();
            assertDocumentText("Incorrect formatting for macro define with brace",
            "\n"+
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
        );
    };
    
    public void testMacroDefineWithBrace2() {
        setLoadDocumentText(
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        reformat();
        assertDocumentText("Incorrect formatting for macro define with brace",
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
    }

    public void testMacroDefineWithBrace3() {
        setLoadDocumentText(
                "\n"+
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        reformat();
        assertDocumentText("Incorrect formatting for macro define with brace",
                "\n"+
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
    }

    public void testMacroDefineWithParen() {
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "M(\"test\");\n" +
                "return 0;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testMacroDefineWithParen11() {
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "M(\"test\");\n" +
                "return 0;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main()\n" +
                "{\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testMacroDefineWithParen2() {
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceBeforeMethodCallParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceBeforeMethodDeclParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main () {\n" +
                "    M (\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testMacroDefineWithParen21() {
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceBeforeMethodCallParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main()\n" +
                "{\n" +
                "    M (\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testSwitchFormatting() {
        setLoadDocumentText(
                "switch (GetTypeID()) {\n" +
                "case FAST:\n" +
                "metric += 100;\n" +
                "break;\n" +
                "case ULTRA:\n" +
                "case SLOW:\n" +
                "metric += 200;\n" +
                "break;\n" +
                "default:\n" +
                "break;\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "switch (GetTypeID()) {\n" +
                "    case FAST:\n" +
                "        metric += 100;\n" +
                "        break;\n" +
                "    case ULTRA:\n" +
                "    case SLOW:\n" +
                "        metric += 200;\n" +
                "        break;\n" +
                "    default:\n" +
                "        break;\n" +
                "}\n");
    }

    public void testSwitchFormatting2() {
        setLoadDocumentText(
                "switch (GetTypeID()) {\n" +
                "case FAST:\n" +
                "metric += 100;\n" +
                "break;\n" +
                "case ULTRA:\n" +
                "case SLOW:\n" +
                "metric += 200;\n" +
                "break;\n" +
                "default:\n" +
                "break;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "switch (GetTypeID()) {\n" +
                "case FAST:\n" +
                "    metric += 100;\n" +
                "    break;\n" +
                "case ULTRA:\n" +
                "case SLOW:\n" +
                "    metric += 200;\n" +
                "    break;\n" +
                "default:\n" +
                "    break;\n" +
                "}\n");
    }

    public void testSwitchFormatting3() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 4:\n" +
                "            if (true)return;\n" +
                "            else {\n" +
                "                break;\n" +
                "            }\n" +
                "            break;\n" +
                "        case 14:\n" +
                "        {\n" +
                "            i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "            return;\n" +
                "        default:\n" +
                "            break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "            case 1:\n" +
                "                return 1;\n" +
                "            case 2:\n" +
                "                break;\n" +
                "            case 4:\n" +
                "                i++;\n" +
                "            case 6:\n" +
                "                switch (i * 2) {\n" +
                "                    case 10:\n" +
                "                        if (true)return;\n" +
                "                        else {\n" +
                "                            break;\n" +
                "                        }\n" +
                "                    case 12:\n" +
                "                    {\n" +
                "                        break;\n" +
                "                    }\n" +
                "                }\n" +
                "            default:\n" +
                "                break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting3Half() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i)\n" +
                "      {\n" +
                "        case 1:\n" +
                "          return 1;\n" +
                "        case 4:\n" +
                "          if (true)return;\n" +
                "          else\n" +
                "            {\n" +
                "              break;\n" +
                "            }\n" +
                "          break;\n" +
                "        case 14:\n" +
                "          {\n" +
                "            i++;\n" +
                "          }\n" +
                "        case 6:\n" +
                "          return;\n" +
                "        default:\n" +
                "          break;\n" +
                "      }\n" +
                "    if (i != 8)\n" +
                "      switch (i)\n" +
                "        {\n" +
                "          case 1:\n" +
                "            return 1;\n" +
                "          case 2:\n" +
                "            break;\n" +
                "          case 4:\n" +
                "            i++;\n" +
                "          case 6:\n" +
                "            switch (i * 2)\n" +
                "              {\n" +
                "                case 10:\n" +
                "                  if (true)return;\n" +
                "                  else\n" +
                "                    {\n" +
                "                      break;\n" +
                "                    }\n" +
                "                case 12:\n" +
                "                  {\n" +
                "                    break;\n" +
                "                  }\n" +
                "              }\n" +
                "          default:\n" +
                "            break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting3HalfSQL() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 4:\n" +
                "            if (true)return;\n" +
                "            else\n" +
                "              {\n" +
                "                break;\n" +
                "              }\n" +
                "            break;\n" +
                "        case 14:\n" +
                "        {\n" +
                "            i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "            return;\n" +
                "        default:\n" +
                "            break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "      switch (i) {\n" +
                "          case 1:\n" +
                "              return 1;\n" +
                "          case 2:\n" +
                "              break;\n" +
                "          case 4:\n" +
                "              i++;\n" +
                "          case 6:\n" +
                "              switch (i * 2) {\n" +
                "                  case 10:\n" +
                "                      if (true)return;\n" +
                "                      else\n" +
                "                        {\n" +
                "                          break;\n" +
                "                        }\n" +
                "                  case 12:\n" +
                "                  {\n" +
                "                      break;\n" +
                "                  }\n" +
                "              }\n" +
                "          default:\n" +
                "              break;\n" +
                "      }\n" +
                "}\n");
    }

    public void testSwitchFormatting3SQL() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "    case 1:\n" +
                "        return 1;\n" +
                "    case 4:\n" +
                "        if (true)return;\n" +
                "        else\n" +
                "        {\n" +
                "            break;\n" +
                "        }\n" +
                "        break;\n" +
                "    case 14:\n" +
                "    {\n" +
                "        i++;\n" +
                "    }\n" +
                "    case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 2:\n" +
                "            break;\n" +
                "        case 4:\n" +
                "            i++;\n" +
                "        case 6:\n" +
                "            switch (i * 2) {\n" +
                "            case 10:\n" +
                "                if (true)return;\n" +
                "                else\n" +
                "                {\n" +
                "                    break;\n" +
                "                }\n" +
                "            case 12:\n" +
                "            {\n" +
                "                break;\n" +
                "            }\n" +
                "            }\n" +
                "        default:\n" +
                "            break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting4() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "        i++;\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   return;\n" +
                "       case 12:\n" +
                "                break;\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "    case 1:\n" +
                "        return 1;\n" +
                "    case 4:\n" +
                "        i++;\n" +
                "    case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 2:\n" +
                "            break;\n" +
                "        case 4:\n" +
                "            i++;\n" +
                "        case 6:\n" +
                "            switch (i * 2) {\n" +
                "            case 10:\n" +
                "                return;\n" +
                "            case 12:\n" +
                "                break;\n" +
                "            }\n" +
                "        default:\n" +
                "            break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting4Half() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i)\n" +
                "      {\n" +
                "      case 1:\n" +
                "        return 1;\n" +
                "      case 4:\n" +
                "        if (true)return;\n" +
                "        else\n" +
                "          {\n" +
                "            break;\n" +
                "          }\n" +
                "        break;\n" +
                "      case 14:\n" +
                "        {\n" +
                "          i++;\n" +
                "        }\n" +
                "      case 6:\n" +
                "        return;\n" +
                "      default:\n" +
                "        break;\n" +
                "      }\n" +
                "    if (i != 8)\n" +
                "      switch (i)\n" +
                "        {\n" +
                "        case 1:\n" +
                "          return 1;\n" +
                "        case 2:\n" +
                "          break;\n" +
                "        case 4:\n" +
                "          i++;\n" +
                "        case 6:\n" +
                "          switch (i * 2)\n" +
                "            {\n" +
                "            case 10:\n" +
                "              if (true)return;\n" +
                "              else\n" +
                "                {\n" +
                "                  break;\n" +
                "                }\n" +
                "            case 12:\n" +
                "              {\n" +
                "                break;\n" +
                "              }\n" +
                "            }\n" +
                "        default:\n" +
                "          break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testDoxyGenIdent() {
        setLoadDocumentText(
            "        /**\n" +
            "         * Class for accessing a compound stream.\n" +
            "         *\n" +
            "         * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            "         */\n" +
            "        class CompoundFileReader: public CL_NS(store)::Directory {\n" +
            "        }\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing doc comment",
            "/**\n" +
            " * Class for accessing a compound stream.\n" +
            " *\n" +
            " * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            " */\n" +
            "class CompoundFileReader : public CL_NS(store)::Directory\n" +
            "{\n" +
            "}\n"
        );
    }

    public void testBlockCommentIdent() {
        setLoadDocumentText(
            "        /*\n" +
            "         * Class for accessing a compound stream.\n" +
            "         *\n" +
            "         * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            "         */\n" +
            "        class CompoundFileReader: public CL_NS(store)::Directory {\n" +
            "        }\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing block comment",
            "/*\n" +
            " * Class for accessing a compound stream.\n" +
            " *\n" +
            " * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            " */\n" +
            "class CompoundFileReader : public CL_NS(store)::Directory\n" +
            "{\n" +
            "}\n"
        );
    }

    public void testIdentElse() {
        setLoadDocumentText(
            "    void FieldsWriter::addDocument(Document* doc)\n" +
            "    {\n" +
            "         if (field->stringValue() == NULL) {\n" +
            "             Reader* r = field->readerValue();\n" +
            "         }    else\n" +
            "         fieldsStream->writeString(field->stringValue(), _tcslen(field->stringValue()));\n" +
            "    }\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing eles without {}",
            "void FieldsWriter::addDocument(Document* doc)\n" +
            "{\n" +
            "    if (field->stringValue() == NULL) {\n" +
            "        Reader* r = field->readerValue();\n" +
            "    } else\n" +
            "        fieldsStream->writeString(field->stringValue(), _tcslen(field->stringValue()));\n" +
            "}\n"
        );
    }

    public void testIdentDoWhile() {
        setLoadDocumentText(
            " int foo()\n" +
            " {\n" +
            " do {\n" +
            " try {\n" +
            " op1().op2.op3().op4();\n" +
            " } catch (Throwable t) {\n" +
            " log();\n" +
            " }\n" +
            " }\n" +
            " while (this.number < 2 && number != 3);\n"+ 
            " }\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing doWhile",
            "int foo()\n" +
            "{\n" +
            "    do {\n" +
            "        try {\n" +
            "            op1().op2.op3().op4();\n" +
            "        } catch (Throwable t) {\n" +
            "            log();\n" +
            "        }\n" +
            "    } while (this.number < 2 && number != 3);\n"+ 
            "}\n"
        );
    }

    public void testIdentInlineMethod() {
        setLoadDocumentText(
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "    		CL_NS(store)::Directory* getDirectory() { return directory; }\n" +
            "};\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "    CL_NS(store)::Directory* getDirectory()\n" +
            "    {\n" +
            "        return directory;\n" +
            "    }\n" +
            "};\n"
        );
    }

    public void testIdentInlineMethod2() {
        setLoadDocumentText(
            "    		CL_NS(store)::Directory* getDirectory() { return directory; }\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "CL_NS(store)::Directory* getDirectory()\n" +
            "{\n" +
            "    return directory;\n" +
            "}\n"
        );
    }

    // end line comment should prevent move left brace on same line by design
    // RFE: move brace before end line comment in future
    public void testBraceBeforeLineComment() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "if (!line) // End of file\n" +
            "{\n" +
            "status.exit_status = 0;\n" +
            "break;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect formatting brace before line comment",
            "int foo()\n" +
            "{\n" +
            "    if (!line) // End of file\n" +
            "    {\n" +
            "        status.exit_status = 0;\n" +
            "        break;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testCaseIndentAftePreprocessor() {
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "            setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "            break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "            strmov(mysql_charsets_dir, argument);\n" +
            "            charsets_dir = mysql_charsets_dir;\n" +
            "            break;\n" +
            "        case OPT_DEFAULT_CHARSET:\n" +
            "            default_charset_used = 1;\n" +
            "            break;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testCaseIndentAftePreprocessor2() {
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "#define Y\n" +
            "        {\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "}\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "    case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "    case OPT_CHARSETS_DIR:\n" +
            "#define Y\n" +
            "    {\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    }\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "    }\n" +
            "}\n"
        );
    }
    public void testCaseIndentAftePreprocessorHalf() {
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid)\n" +
            "      {\n" +
            "#ifdef __NETWARE__\n" +
            "      case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "      case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "      case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "      }\n" +
            "}\n"
        );
    }

    public void testCaseIndentAftePreprocessorHalf2() {
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid)\n" +
            "      {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "          setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "          break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "          strmov(mysql_charsets_dir, argument);\n" +
            "          charsets_dir = mysql_charsets_dir;\n" +
            "          break;\n" +
            "        case OPT_DEFAULT_CHARSET:\n" +
            "          default_charset_used = 1;\n" +
            "          break;\n" +
            "      }\n" +
            "}\n"
        );
    }

    public void testTypedefClassNameIndent() {
        setLoadDocumentText(
            "typedef struct st_line_buffer\n" +
            "{\n" +
            "File file;\n" +
            "char *buffer;\n" +
            "/* The buffer itself, grown as needed. */\n" +
            "}LINE_BUFFER;\n" 
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "typedef struct st_line_buffer\n" +
            "{\n" +
            "    File file;\n" +
            "    char *buffer;\n" +
            "    /* The buffer itself, grown as needed. */\n" +
            "} LINE_BUFFER;\n" 
        );
    }

    public void testLabelIndent() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "end:\n" +
            "if (fd >= 0)\n" +
            "        my_close(fd, MYF(MY_WME));\n" +
            "    return error;\n" +
            "}\n" 
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect label indent",
            "int foo()\n" +
            "{\n" +
            "end:\n" +
            "    if (fd >= 0)\n" +
            "        my_close(fd, MYF(MY_WME));\n" +
            "    return error;\n" +
            "}\n" 
        );
    }

    public void testIdentBlockAfterDirective() {
        setLoadDocumentText(
            "int yyparse()\n" +
            "{\n" +
            "    yychar = - 1;\n" +
            "#if YYMAXDEPTH <= 0\n" +
            "    if (yymaxdepth <= 0) {\n" +
            "        if ((yymaxdepth = YYEXPAND(0)) <= 0) {\n" +
            "            yyerror(\"yacc initialization error\");\n" +
            "            YYABORT;\n" +
            "        }\n" +
            "    }\n" +
            "#endif\n" +
            " {\n" +
            "        register YYSTYPE *yy_pv;\n" +
            "        /* top of value stack */\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing coode block after directive",
            "int yyparse()\n" +
            "{\n" +
            "    yychar = -1;\n" +
            "#if YYMAXDEPTH <= 0\n" +
            "    if (yymaxdepth <= 0) {\n" +
            "        if ((yymaxdepth = YYEXPAND(0)) <= 0) {\n" +
            "            yyerror(\"yacc initialization error\");\n" +
            "            YYABORT;\n" +
            "        }\n" +
            "    }\n" +
            "#endif\n" +
            "    {\n" +
            "        register YYSTYPE *yy_pv;\n" +
            "        /* top of value stack */\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testMacroBeforePrepricessor() {
        setLoadDocumentText(
            "int yyparse()\n" +
            "{\n" +
            "    switch (nchar) {\n" +
            "        /* split current window in two parts, horizontally */\n" +
            "    case 'S':\n" +
            "    case 's':\n" +
            "        CHECK_CMDWIN\n" +
            "#    ifdef FEAT_VISUAL\n" +
            "reset_VIsual_and_resel();\n" +
            "        /* stop Visual mode */\n" +
            "#    endif\n" +
            "    case 'W':\n" +
            "        CHECK_CMDWIN\n" +
            "if (lastwin == firstwin && Prenum != 1) /* just one window */\n" +
            "            beep_flush();\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        reformat();
        assertDocumentText("Incorrect identing macro before preoprocessor",
            "int yyparse()\n" +
            "{\n" +
            "    switch (nchar) {\n" +
            "        /* split current window in two parts, horizontally */\n" +
            "    case 'S':\n" +
            "    case 's':\n" +
            "        CHECK_CMDWIN\n" +
            "#ifdef FEAT_VISUAL\n" +
            "                reset_VIsual_and_resel();\n" +
            "        /* stop Visual mode */\n" +
            "#endif\n" +
            "    case 'W':\n" +
            "        CHECK_CMDWIN\n" +
            "        if (lastwin == firstwin && Prenum != 1) /* just one window */\n" +
            "            beep_flush();\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentElseBeforePreprocessor() {
        setLoadDocumentText(
            "int yyparse()\n" +
            "{\n" +
            "#ifdef X\n" +
            "    if (true) {\n" +
            "        if (oldwin->w_p_wfw)\n" +
            "            win_setwidth_win(oldwin->w_width + new_size, oldwin);\n" +
            "    } else\n" +
            "#    endif\n" +
            " {\n" +
            "        layout = FR_COL;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        reformat();
        assertDocumentText("Incorrect identing else before preprocessor",
            "int yyparse()\n" +
            "{\n" +
            "#ifdef X\n" +
            "    if (true) {\n" +
            "        if (oldwin->w_p_wfw)\n" +
            "            win_setwidth_win(oldwin->w_width + new_size, oldwin);\n" +
            "    } else\n" +
            "#endif\n" +
            "    {\n" +
            "        layout = FR_COL;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentK_and_R_style() {
        setLoadDocumentText(
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "{\n" +
            "    int i;\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing K&R declaration",
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "{\n" +
            "    int i;\n" +
            "}\n"
        );
    }

    public void testIdentK_and_R_style2() {
        setLoadDocumentText(
            "extern \"C\" {\n" +
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "{\n" +
            "    int i;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "extern \"C\"\n" +
            "{\n" +
            "    static void\n" +
            "    win_init(newp, oldp)\n" +
            "    win_T *newp;\n" +
            "    win_T *oldp;\n" +
            "    {\n" +
            "        int i;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentInBlockComment() {
        setLoadDocumentText(
            "extern \"C\" {\n" +
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "           /*\n" +
            "             Preserve identation in block\n" +
            "               1.\n" +
            "               2.\n" +
            "\n" +
            "            */\n" +
            "{\n" +
            "/*\n" +
            "  Preserve identation in block\n" +
            "    1.\n" +
            "    2.\n" +
            "\n" +
            "*/\n" +
            "    int i;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing in block comment",
            "extern \"C\"\n" +
            "{\n" +
            "    static void\n" +
            "    win_init(newp, oldp)\n" +
            "    win_T *newp;\n" +
            "    win_T *oldp;\n" +
            "    /*\n" +
            "      Preserve identation in block\n" +
            "        1.\n" +
            "        2.\n" +
            "\n" +
            "     */\n" +
            "    {\n" +
            "        /*\n" +
            "          Preserve identation in block\n" +
            "            1.\n" +
            "            2.\n" +
            "\n" +
            "         */\n" +
            "        int i;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentInBlockComment2() {
        setLoadDocumentText(
            "extern \"C\" {\n" +
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "      /*\n" +
            "           * Preserve identation in block\n" +
            "          *   1.\n" +
            "       *   2.\n" +
            "*\n" +
            "   */\n" +
            "{\n" +
            "  /*\n" +
            "* Preserve identation in block\n" +
            "    *   1.\n" +
            " *   2.\n" +
            "*\n" +
            "*/\n" +
            "    int i;\n" +
            "}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing in block comment",
            "extern \"C\"\n" +
            "{\n" +
            "    static void\n" +
            "    win_init(newp, oldp)\n" +
            "    win_T *newp;\n" +
            "    win_T *oldp;\n" +
            "    /*\n" +
            "     * Preserve identation in block\n" +
            "     *   1.\n" +
            "     *   2.\n" +
            "     *\n" +
            "     */\n" +
            "    {\n" +
            "        /*\n" +
            "         * Preserve identation in block\n" +
            "         *   1.\n" +
            "         *   2.\n" +
            "         *\n" +
            "         */\n" +
            "        int i;\n" +
            "    }\n" +
            "}\n"
        );
    
    }

    public void testAddNewLineAfterSemocolon() {
        setLoadDocumentText(
            "int foo(int i)\n" +
            "{\n" +
            "if(true) if(true) if(true) i--;\n" +
            "else i++;else i++; else i++;\n" +
            " if(true) while(i>0) i--;\n" +
            " if(true) return; else break;\n" +
            " if(true) return;\n" +
            " else {break;}\n" +
            "}\n"
            );
        reformat();
        setDefaultsOptions();
        assertDocumentText("Incorrect adding new line after semocolon",
            "int foo(int i)\n" +
            "{\n" +
            "    if (true) if (true) if (true) i--;\n" +
            "            else i++;\n" +
            "        else i++;\n" +
            "    else i++;\n" +
            "    if (true) while (i > 0) i--;\n" +
            "    if (true) return;\n" +
            "    else break;\n" +
            "    if (true) return;\n" +
            "    else {\n" +
            "        break;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testAddNewLineAfterSemocolon2() {
        setLoadDocumentText(
            "int foo(int i)\n" +
            "{\n" +
            "if(true) if(true) if(true) i--;\n" +
            "else i++;else i++; else i++;\n" +
            " if(true) while(i>0) i--;\n" +
            " if(true) return; else break;\n" +
            " if(true) return;\n" +
            " else {break;}\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect adding new line after semocolon",
            "int foo(int i)\n" +
            "{\n" +
            "    if (true) if (true) if (true) i--;\n" +
            "        else i++;\n" +
            "      else i++;\n" +
            "    else i++;\n" +
            "    if (true) while (i > 0) i--;\n" +
            "    if (true) return;\n" +
            "    else break;\n" +
            "    if (true) return;\n" +
            "    else\n" +
            "      {\n" +
            "        break;\n" +
            "      }\n" +
            "}\n"
        );
    }

    public void testIdentFunctionDefinition() {
        setLoadDocumentText(
            "uchar *\n" +
            "        tokname(int n)\n" +
            "{\n" +
            "    static char buf[100];\n" +
            "    return printname[n - 257];\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing function definition",
            "uchar *\n" +
            "tokname(int n)\n" +
            "{\n" +
            "    static char buf[100];\n" +
            "    return printname[n - 257];\n" +
            "}\n"
        );
    }
    

    public void testIdentFunctionDefinition2() {
        setLoadDocumentText(
            "namespace A\n" +
            "{\n" +
            "uchar *\n" +
            "        tokname(int n)\n" +
            "{\n" +
            "    static char buf[100];\n" +
            "    return printname[n - 257];\n" +
            "}\n"+
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        reformat();
        assertDocumentText("Incorrect identing function definition",
            "namespace A\n" +
            "{\n" +
            "    uchar *\n" +
            "    tokname(int n)\n" +
            "    {\n" +
            "        static char buf[100];\n" +
            "        return printname[n - 257];\n" +
            "    }\n"+
            "}\n"
        );
    }
    
    public void testIdentElseAfterPreprocessor() {
        setLoadDocumentText(
            "getcmdline(int firstc)\n" +
            "{\n" +
            "    if (firstc == '/')\n" +
            "    {\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "	im_set_active(*b_im_ptr == B_IMODE_IM);\n" +
            "#endif\n" +
            "    }\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "    else if (p_imcmdline)\n" +
            "	im_set_active(TRUE);\n" +
            "#endif\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing else after preprocessor",
            "getcmdline(int firstc)\n" +
            "{\n" +
            "    if (firstc == '/') {\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "        im_set_active(*b_im_ptr == B_IMODE_IM);\n" +
            "#endif\n" +
            "    }\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "    else if (p_imcmdline)\n" +
            "        im_set_active(TRUE);\n" +
            "#endif\n" +
            "}\n"
        );
    }

    public void testBlankLineBeforeMethod() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "}\n" +
                "/*\n" +
                "* Call this when vim starts up, whether or not the GUI is started\n" +
                " */\n" +
                "void\n" +
                "gui_prepare(argc)\n" +
                "    int *argc;\n" +
                "{\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect blank line before method",
                "int foo()\n" +
                "{\n" +
                "}\n" +
                "\n" +
                "/*\n" +
                " * Call this when vim starts up, whether or not the GUI is started\n" +
                " */\n" +
                "void\n" +
                "gui_prepare(argc)\n" +
                "int *argc;\n" +
                "{\n" +
                "}\n"
                );
    }

    public void testBlockCodeNewLine() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  bt.setFragmentType(t->getFragmentType());\n" +
                "  { NdbDictionary::Column bc(\"PK\");\n" +
                "    bt.addColumn(bc);\n" +
                "  }\n" +
                "  { NdbDictionary::Column bc(\"DIST\");\n" +
                "    bt.addColumn(bc);\n" +
                "  }\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect block code new line",
                "int foo()\n" +
                "{\n" +
                "    bt.setFragmentType(t->getFragmentType());\n" +
                "    {\n" +
                "        NdbDictionary::Column bc(\"PK\");\n" +
                "        bt.addColumn(bc);\n" +
                "    }\n" +
                "    {\n" +
                "        NdbDictionary::Column bc(\"DIST\");\n" +
                "        bt.addColumn(bc);\n" +
                "    }\n" +
                "}\n"
                );
    }

    public void testBlankLineAfterEndLineComment() {
        setLoadDocumentText(
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    if (0){\n" +
                "        DBUG_RETURN(3);\n" +
                "    }//if\n" +
                "}//Ndb::NDB_connect()\n" +
                "NdbTransaction *\n" +
                "Ndb::getConnectedNdbTransaction(Uint32 nodeId)\n" +
                "{\n" +
                "    return next;\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect blak line after end line comment",
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    if (0) {\n" +
                "        DBUG_RETURN(3);\n" +
                "    }//if\n" +
                "}//Ndb::NDB_connect()\n" +
                "\n" +
                "NdbTransaction *\n" +
                "Ndb::getConnectedNdbTransaction(Uint32 nodeId)\n" +
                "{\n" +
                "    return next;\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
    }
    
    
    public void testReformatCodeBlocks() {
        setLoadDocumentText(
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    DBUG_ENTER(\"Ndb::startTransaction\");\n" +
                "    if (theInitState == Initialised) {\n" +
                "        NdbTableImpl* impl;\n" +
                "        if (table != 0 && keyData != 0 && (impl = &NdbTableImpl::getImpl(*table))) {\n" +
                "            Uint32 hashValue; {\n" +
                "                Uint32 buf[4];\n" +
                "            }\n" +
                "            const Uint16 *nodes;\n" +
                "            Uint32 cnt = impl->get_nodes(hashValue, &nodes);\n" +
                "        } else {\n" +
                "            nodeId = 0;\n" +
                "        }//if\n" +
                "{\n" +
                "            NdbTransaction *trans = startTransactionLocal(0, nodeId);\n" +
                "        }\n" +
                "    } else {\n" +
                "        DBUG_RETURN(NULL);\n" +
                "    }//if\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect code block formatting",
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    DBUG_ENTER(\"Ndb::startTransaction\");\n" +
                "    if (theInitState == Initialised) {\n" +
                "        NdbTableImpl* impl;\n" +
                "        if (table != 0 && keyData != 0 && (impl = &NdbTableImpl::getImpl(*table))) {\n" +
                "            Uint32 hashValue;\n" +
                "            {\n" +
                "                Uint32 buf[4];\n" +
                "            }\n" +
                "            const Uint16 *nodes;\n" +
                "            Uint32 cnt = impl->get_nodes(hashValue, &nodes);\n" +
                "        } else {\n" +
                "            nodeId = 0;\n" +
                "        }//if\n" +
                "        {\n" +
                "            NdbTransaction *trans = startTransactionLocal(0, nodeId);\n" +
                "        }\n" +
                "    } else {\n" +
                "        DBUG_RETURN(NULL);\n" +
                "    }//if\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
    }

    public void testSpaceBinaryOperator() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    bmove_upp(dst + rest+new_length, dst+tot_length, rest);\n" +
            "    if (len <= 0 ||| len >= (int)sizeof(buf) || buf[sizeof(buf)-1] != 0) return 0;\n" +
            "    lmask = (1U << state->lenbits)-1;\n" +
            "    len = BITS(4)+8;\n" +
            "    s->depth[node] = (uch)((s->depth[n] >= s->depth[m] ? s->depth[n] : s->depth[m])+1);\n" +
            "    for (i = 0; i<n; i++) return;\n" +
            "    match[1].end = match[0].end+s_length;\n" +
            "    return(0);\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect spaces in binary operators",
            "int foo()\n" +
            "{\n" +
            "    bmove_upp(dst + rest + new_length, dst + tot_length, rest);\n" +
            "    if (len <= 0 || len >= (int) sizeof (buf) || buf[sizeof (buf) - 1] != 0) return 0;\n" +
            "    lmask = (1U << state->lenbits) - 1;\n" +
            "    len = BITS(4) + 8;\n" +
            "    s->depth[node] = (uch) ((s->depth[n] >= s->depth[m] ? s->depth[n] : s->depth[m]) + 1);\n" +
            "    for (i = 0; i < n; i++) return;\n" +
            "    match[1].end = match[0].end + s_length;\n" +
            "    return (0);\n" +
            "}\n"
        );
    }

    public void testSpaceBinaryOperator2() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    BOOST_CHECK(\n" +
            "            ((nc_result.begin()-str1.begin()) == 3) &&\n" +
            "            ((nc_result.end()-str1.begin()) == 6));\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect spaces in binary operators",
            "int foo()\n" +
            "{\n" +
            "    BOOST_CHECK(\n" +
            "            ((nc_result.begin() - str1.begin()) == 3) &&\n" +
            "            ((nc_result.end() - str1.begin()) == 6));\n" +
            "}\n"
        );
    }

    public void testSpaceTemplateSeparator() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    vector<string> tokens1;\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect spaces before template separator",
            "int foo()\n" +
            "{\n" +
            "    vector<string> tokens1;\n" +
            "}\n"
        );
    }

    public void testSpaceCastOperator() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    if (m == NULL ||| *m == \'\\0\') m = (char*)ERR_MSG(s->z_err);\n" +
            "    hold += (unsigned long)(PUP(in)) << bits;\n" +
            "    state = (struct inflate_state FAR *)strm->state;\n" +
            "    if (strm->zalloc == (alloc_func)0) return;\n" +
            "    stream.zalloc = (alloc_func)0;\n" +
            "    put_short(s, (ush)len);\n" +
            "    put_short(s, (ush)~len);\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceWithinTypeCastParens, true);
        reformat();
        assertDocumentText("Incorrect spaces in cast operators",
            "int foo()\n" +
            "{\n" +
            "    if (m == NULL || *m == \'\\0\') m = ( char* ) ERR_MSG(s->z_err);\n" +
            "    hold += ( unsigned long ) (PUP(in)) << bits;\n" +
            "    state = ( struct inflate_state FAR * ) strm->state;\n" +
            "    if (strm->zalloc == ( alloc_func ) 0) return;\n" +
            "    stream.zalloc = ( alloc_func ) 0;\n" +
            "    put_short(s, ( ush ) len);\n" +
            "    put_short(s, ( ush ) ~len);\n" +
            "}\n"
        );
    }

    public void testNoSpaceBeforeUnaryOperator() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    if (s == NULL ||| s->mode != 'r') return - 1;\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect no space before unary operator",
            "int foo()\n" +
            "{\n" +
            "    if (s == NULL || s->mode != 'r') return -1;\n" +
            "}\n"
        );
    }

    public void testNoEscapedSpaceSupport() {
        setLoadDocumentText(
                "static const char* _dbname = \"TEST_DB\";\n" +
                "static void usage()\n" +
                "{\n" +
                "  char desc[] = \n" +
                "    \"[<table> <index>]+\\n\"\\\n" +
                "    \"This program will drop index(es) in Ndb\\n\";\n" +
                "    ndb_std_print_version();\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect escaped space",
                "static const char* _dbname = \"TEST_DB\";\n" +
                "\n" +
                "static void usage()\n" +
                "{\n" +
                "    char desc[] =\n" +
                "            \"[<table> <index>]+\\n\"\\\n" +
                "    \"This program will drop index(es) in Ndb\\n\";\n" +
                "    ndb_std_print_version();\n" +
                "}\n"
                );
    }
 
    public void testIfDoWhile() {
        setLoadDocumentText(
                "void foo()\n" +
                "{\n" +
                "    if (len) do {\n" +
                "            DO1;\n" +
                "    } while (--len);\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect if-do-while indent",
                "void foo()\n" +
                "{\n" +
                "    if (len) do {\n" +
                "            DO1;\n" +
                "        } while (--len);\n" +
                "}\n"
                );
    }

    public void testIfIfDoWhile() {
        setLoadDocumentText(
                "void foo()\n" +
                "{\n" +
                "    if (len) if (true) do {\n" +
                "        DO1;\n" +
                "        } while (--len);\n" +
                "    else return;\n" +
                "    else return;\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect if-if-do-while indent",
                "void foo()\n" +
                "{\n" +
                "    if (len) if (true) do {\n" +
                "                DO1;\n" +
                "            } while (--len);\n" +
                "        else return;\n" +
                "    else return;\n" +
                "}\n"
                );
    }

    public void testDoubleFunctionComment() {
        setLoadDocumentText(
                "void foo();\n" +
                "/* Stream status */\n" +
                "/* Data structure describing a single value and its code string. */\n" +
                "typedef struct ct_data_s\n" +
                "{\n" +
                "    ush code;\n" +
                "} FAR ct_data;\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect blank lines between block comments",
                "void foo();\n" +
                "/* Stream status */\n" +
                "\n" +
                "/* Data structure describing a single value and its code string. */\n" +
                "typedef struct ct_data_s\n" +
                "{\n" +
                "    ush code;\n" +
                "} FAR ct_data;\n"
                );
    }

    public void testArrayAsParameter() {
        setLoadDocumentText(
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "    ClassA() : cc({ \"A\", \"B\", \"C\", \"D\"}), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect formatting of array as parameter",
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "\n" +
                "    ClassA() : cc({\"A\", \"B\", \"C\", \"D\"}), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n"
                );
    }

    public void testArrayAsParameter2() {
        setLoadDocumentText(
                "namespace AC\n" +
                "{\n" +
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "ClassA() : cc({ \"A\", \"B\", \"C\", \"D\" }), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentNamespace, false);
        reformat();
        assertDocumentText("Incorrect formatting of array as parameter",
                "namespace AC\n" +
                "{\n" +
                "\n" +
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "\n" +
                "    ClassA() : cc({\"A\", \"B\", \"C\", \"D\"}), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n" +
                "}\n"
                );
    }

    public void testIssue129747() {
        setLoadDocumentText(
                "enum CpuArch { OPTERON, INTEL, SPARC}; // CPU architecture\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentNamespace, false);
        reformat();
        assertDocumentText("Issue 129747",
                "enum CpuArch\n" +
                "{\n" +
                "    OPTERON, INTEL, SPARC\n" +
                "}; // CPU architecture\n"
                );
    }

    public void testIssue129608() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "s = (teststruct_t)\n" +
                " {\n" +
                "    .a = 1,\n" +
                "            .b = 2,\n" +
                "            .c = 3,\n" +
                "};\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentNamespace, false);
        reformat();
        assertDocumentText("Issue 129608",
                "int foo()\n" +
                "{\n" +
                "    s = (teststruct_t){\n" +
                "        .a = 1,\n" +
                "        .b = 2,\n" +
                "        .c = 3,\n" +
                "    };\n" +
                "}\n"
                );
    }

    public void testReformatIfElseElse() {
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "  }\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect indent if-else if-else",
                 "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "    }\n" +
                "}\n");
    }

    public void testHalfIndent() {
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "        text = 1;\n" +
                "    else if (strlen(text) == 0)\n" +
                "        text = 3;\n" +
                "    else\n" +
                "        number++;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "      text = 1;\n" +
                "    else if (strlen(text) == 0)\n" +
                "      text = 3;\n" +
                "    else\n" +
                "      number++;\n" +
                "}\n");
    }

    public void testHalfIndentFull() {
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "        text = 1;\n" +
                "    else if (strlen(text) == 0)\n" +
                "        text = 3;\n" +
                "    else\n" +
                "        number++;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "  if (text == NULL)\n" +
                "    text = 1;\n" +
                "  else if (strlen(text) == 0)\n" +
                "    text = 3;\n" +
                "  else\n" +
                "    number++;\n" +
                "}\n");
    }

    public void testHalfIndent2() {
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "  }\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "      {\n" +
                "        text = 1;\n" +
                "      }\n" +
                "    else if (strlen(text) == 0)\n" +
                "      {\n" +
                "        text = 3;\n" +
                "      }\n" +
                "    else\n" +
                "      {\n" +
                "        number++;\n" +
                "      }\n" +
                "}\n");
    }

    public void testHalfIndent2Full() {
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "  }\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "  if (text == NULL)\n" +
                "    {\n" +
                "      text = 1;\n" +
                "    }\n" +
                "  else if (strlen(text) == 0)\n" +
                "    {\n" +
                "      text = 3;\n" +
                "    }\n" +
                "  else\n" +
                "    {\n" +
                "      number++;\n" +
                "    }\n" +
                "}\n");
    }

    public void testDoWhileHalf() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "  while (this.number < 2 &&\n" +
                "      number != 3)\n" +
                "    {\n" +
                "      method(12);\n" +
                "    }\n" +
                "  do\n" +
                "    {\n" +
                "      op1().op2.op3().op4();\n" +
                "    }\n" +
                "   while (this.number < 2 &&\n" +
                "   number != 3);\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineWhile, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect formatting half do-while",
                "int main(int i)\n" +
                "{\n" +
                "  while (this.number < 2 &&\n" +
                "          number != 3)\n" +
                "    {\n" +
                "      method(12);\n" +
                "    }\n" +
                "  do\n" +
                "    {\n" +
                "      op1().op2.op3().op4();\n" +
                "    }\n" +
                "  while (this.number < 2 &&\n" +
                "          number != 3);\n" +
                "}\n");
    }

    public void testDoWhileHalf2() {
        setLoadDocumentText(
                "int foo() {\n" +
                "do {\n" +
                "    i++;\n" +
                "} while(true);\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineWhile, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect formatting do-while half indent",
                "int foo()\n" +
                "{\n" +
                "  do\n" +
                "    {\n" +
                "      i++;\n" +
                "    }\n" +
                "  while (true);\n" +
                "}\n");
    }

    public void testDereferenceAfterIf() {
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "if (offset)\n" +
                "    *offset = layout->record_size/ BITS_PER_UNIT;\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect space for dereference after if",
                "int main(int i)\n" +
                "{\n" +
                "    if (offset)\n" +
                "        *offset = layout->record_size / BITS_PER_UNIT;\n" +
                "}\n");
    }

    public void testTryCatchHalf() {
        setLoadDocumentText(
                "int foo() {\n" +
                "try {\n" +
                "    i++;\n" +
                "} catch (char e){\n" +
                "    i--;\n" +
                "} catch (char e)\n" +
                "    i--;\n" +
                "if (true)try\n" +
                "    i++;\n" +
                "catch (char e)\n" +
                "    i--;\n" +
                " catch (char e){\n" +
                "    i--;}\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineCatch, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect formatting try-catch half indent",
                "int foo()\n" +
                "{\n" +
                "  try\n" +
                "    {\n" +
                "      i++;\n" +
                "    }\n" +
                "  catch (char e)\n" +
                "    {\n" +
                "      i--;\n" +
                "    }\n" +
                "  catch (char e)\n" +
                "    i--;\n" +
                "  if (true) try\n" +
                "      i++;\n" +
                "    catch (char e)\n" +
                "      i--;\n" +
                "    catch (char e)\n" +
                "      {\n" +
                "        i--;\n" +
                "      }\n" +
                "}\n");
    }

    public void testEndLineComments() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  if (strcmp (TREE_STRING_POINTER (id), \"default\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_DEFAULT;  // comment\n" +
                "  else if (strcmp (TREE_STRING_POINTER (id), \"hidden\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_HIDDEN;  \n" +
                "  else if (strcmp (TREE_STRING_POINTER (id), \"protected\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_PROTECTED;   /* comment */   \n" +
                "  else\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_PROTECTED;\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineCatch, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect unexpected new line after semicolomn",
                "int foo()\n" +
                "{\n" +
                "  if (strcmp(TREE_STRING_POINTER(id), \"default\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_DEFAULT; // comment\n" +
                "  else if (strcmp(TREE_STRING_POINTER(id), \"hidden\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_HIDDEN;\n" +
                "  else if (strcmp(TREE_STRING_POINTER(id), \"protected\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_PROTECTED; /* comment */\n" +
                "  else\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_PROTECTED;\n" +
                "}\n");
    }

    public void testLabelIndentHalf() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  start: while(true){\n" +
                "int i = 0;\n" +
                "goto start;\n" +
                "end:\n" +
                "if(true){\n" +
                "foo();\n" +
                "second:\n" +
                "foo();\n" +
                "}\n" +
                "}\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect label half indent",
                "int foo()\n" +
                "{\n" +
                "start:\n" +
                "  while (true)\n" +
                "    {\n" +
                "      int i = 0;\n" +
                "      goto start;\n" +
                "end:\n" +
                "      if (true)\n" +
                "        {\n" +
                "          foo();\n" +
                "second:\n" +
                "          foo();\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testLabelIndentHalf2() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  start: while(true){\n" +
                "int i = 0;\n" +
                "goto start;\n" +
                "end:\n" +
                "if(true){\n" +
                "foo();\n" +
                "second:\n" +
                "foo();\n" +
                "}\n" +
                "}\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.absoluteLabelIndent, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect label half indent",
                "int foo()\n" +
                "{\n" +
                "start:\n" +
                "  while (true)\n" +
                "    {\n" +
                "      int i = 0;\n" +
                "      goto start;\n" +
                "    end:\n" +
                "      if (true)\n" +
                "        {\n" +
                "          foo();\n" +
                "        second:\n" +
                "          foo();\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testLabelStatementIndent() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  start: while(true){\n" +
                "int i = 0;\n" +
                "goto start;\n" +
                "end:\n" +
                "if(true){\n" +
                "foo();\n" +
                "second:\n" +
                "foo();\n" +
                "}\n" +
                "}\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.absoluteLabelIndent, false);
        reformat();
        assertDocumentText("Incorrect label indent",
                "int foo()\n" +
                "{\n" +
                "start:\n" +
                "    while (true) {\n" +
                "        int i = 0;\n" +
                "        goto start;\n" +
                "    end:\n" +
                "        if (true) {\n" +
                "            foo();\n" +
                "        second:\n" +
                "            foo();\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testOperatorEQformatting() {
        setLoadDocumentText(
                "class real_c_float\n" +
                "{\n" +
                "  const real_c_float & operator=(long l){ from_long(l);\n" +
                "    return *this;\n" +
                "  }\n" +
                "};\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect operator = formatting",
                "class real_c_float\n" +
                "{\n" +
                "\n" +
                "    const real_c_float & operator=(long l)\n" +
                "    {\n" +
                "        from_long(l);\n" +
                "        return *this;\n" +
                "    }\n" +
                "};\n");
    }

    public void testDereferenceFormatting() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "for (DocumentFieldList* list = fieldList; list != NULL; list = list->next) {\n" +
                "TCHAR* tmp = list->field->toString();\n" +
                "}\n" +
                "CL_NS_STD(ostream)* infoStream;\n" +
                "directory->deleteFile( *itr );\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect * spacing",
                "int foo()\n" +
                "{\n" +
                "    for (DocumentFieldList* list = fieldList; list != NULL; list = list->next) {\n" +
                "        TCHAR* tmp = list->field->toString();\n" +
                "    }\n" +
                "    CL_NS_STD(ostream)* infoStream;\n" +
                "    directory->deleteFile(*itr);\n" +
                "}\n");
    }

    public void testNewStyleCastFormatting() {
        setLoadDocumentText(
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "const char* j = const_cast < const char*>(a);\n" +
                "A* c = dynamic_cast <A* > (b);\n" +
                "int i = reinterpret_cast< int > (a);\n" +
                "i = static_cast < int > (*a);\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect new style cast formating",
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "    const char* j = const_cast<const char*> (a);\n" +
                "    A* c = dynamic_cast<A*> (b);\n" +
                "    int i = reinterpret_cast<int> (a);\n" +
                "    i = static_cast<int> (*a);\n" +
                "}\n");
    }

    public void testNewStyleCastFormatting2() {
        setLoadDocumentText(
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "const char* j = const_cast < const char*>(a);\n" +
                "A* c = dynamic_cast <A* > (b);\n" +
                "int i = reinterpret_cast< int > (a);\n" +
                "i = static_cast < int > (*a);\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceWithinTypeCastParens, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceAfterTypeCast, false);
        reformat();
        assertDocumentText("Incorrect new style cast formating",
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "    const char* j = const_cast < const char* >(a);\n" +
                "    A* c = dynamic_cast < A* >(b);\n" +
                "    int i = reinterpret_cast < int >(a);\n" +
                "    i = static_cast < int >(*a);\n" +
                "}\n");
    }

    public void testConcurrentSpacing() {
        setLoadDocumentText(
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "              for (cnt = 0; domain->successor[cnt] != NULL;++cnt);\n" +
                "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect cpace after ; and befor ++",
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "    for (cnt = 0; domain->successor[cnt] != NULL; ++cnt);\n" +
                "}\n");
    }

    public void testIZ130538() {
        setLoadDocumentText(
                "int foooooooo(char* a,\n" +
                " class B* b)\n" +
                "{\n" +
                "    foo(a,\n" +
                "   b);\n" +
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceBeforeMethodCallParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceBeforeMethodDeclParen, true);
        reformat();
        assertDocumentText("Incorrect formating IZ#130538",
                "int foooooooo (char* a,\n" +
                "               class B* b)\n" +
                "{\n" +
                "    foo (a,\n" +
                "         b);\n" +
                "}\n");
    }

    //IZ#130544:Multiline alignment works wrongly with complex expressions
    //IZ#130690:IDE cann't align multi-line expression on '('
    public void testAlignOtherParen() {
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    v = (rup->ru_utime.tv_sec * 1000 + rup->ru_utime.tv_usec / 1000\n" +
            "     + rup->ru_stime.tv_sec * 1000 + rup->ru_stime.tv_usec / 1000);\n" +
            "    if ((inmode[j] == VOIDmode\n" +
            "            && (GET_MODE_SIZE (outmode[j]) > GET_MODE_SIZE (inmode[j])))\n" +
            "            ? outmode[j] : inmode[j]) a++;\n" +
            "  while ((opt = getopt_long(argc, argv, OPTION_STRING,\n" +
            "       options, NULL)) != -1)\n" +
            "    a++;\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineIfCondition, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        reformat();
        assertDocumentText("Incorrect spaces in binary operators",
            "int foo()\n" +
            "{\n" +
            "    v = (rup->ru_utime.tv_sec * 1000 + rup->ru_utime.tv_usec / 1000\n" +
            "         + rup->ru_stime.tv_sec * 1000 + rup->ru_stime.tv_usec / 1000);\n" +
            "    if ((inmode[j] == VOIDmode\n" +
            "         && (GET_MODE_SIZE(outmode[j]) > GET_MODE_SIZE(inmode[j])))\n" +
            "        ? outmode[j] : inmode[j]) a++;\n" +
            "    while ((opt = getopt_long(argc, argv, OPTION_STRING,\n" +
            "                              options, NULL)) != -1)\n" +
            "        a++;\n" +
            "}\n"
        );
    }

    //IZ#130525:Formatter should move the name of the function in column one
    public void testNewLineFunctionDefinitionName() {
        setLoadDocumentText(
            "static char *concat (char *s1, char *s2)\n" +
            "{\n" +
            "  int i;\n" +
            "   int j;\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.newLineFunctionDefinitionName, true);
        reformat();
        assertDocumentText("Formatter should move the name of the function in column one",
            "static char *\n" +
            "concat(char *s1, char *s2)\n" +
            "{\n" +
            "    int i;\n" +
            "    int j;\n" +
            "}\n"
            );
    }

    //IZ#130898:'Spaces around ternary operators' is not working
    public void testSpacesAroundTernary() {
        setLoadDocumentText(
            "static char *concat (char *s1, char *s2)\n" +
            "{\n" +
            "  int i=0;\n" +
            "  i=(i==1)?1:2;\n" +
            "  return (0);\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("\'Spaces around ternary operators\' is not working",
            "static char *concat(char *s1, char *s2)\n" +
            "{\n" +
            "    int i = 0;\n" +
            "    i = (i == 1) ? 1 : 2;\n" +
            "    return (0);\n" +
            "}\n"
            );
    }

    //IZ#130900:'Spaces around Operators|Unary Operators' doesn't work in some cases
    public void testSpaceAroundUnaryOperator() {
        setLoadDocumentText(
            "int main(int argc, char** argv)\n" +
            "{\n" +
            "    int i = 0;\n" +
            "    i = -i;\n" +
            "    i = (-i);\n" +
            "    return (0);\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceAroundUnaryOps, true);
        reformat();
        assertDocumentText("Incorrect spaces in unary operators",
            "int main(int argc, char** argv)\n" +
            "{\n" +
            "    int i = 0;\n" +
            "    i = - i;\n" +
            "    i = (- i);\n" +
            "    return (0);\n" +
            "}\n"
            );
    }

    //IZ#130901:'Blank Lines|After Class Header' text field works wrongly
    public void testNewLinesAterClassHeader() {
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }
    public void testNewLinesAterClassHeader2() {
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }

    public void testNewLinesAterClassHeader3() {
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }

    public void testNewLinesAterClassHeader4() {
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.blankLinesAfterClassHeader, 1);
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }

    //IZ#130916:'Multiline Alignment|Array Initializer' checkbox works wrongly
    public void testMultilineArrayAlignment() {
        setLoadDocumentText(
            "        int array[10] ={1, 2, 3, 4,\n" +
            "    5, 6, 7, 8, 9\n" +
            "};\n"
            );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineArrayInit, true);
        reformat();
        assertDocumentText("\'Multiline Alignment|Array Initializer\' checkbox works wrongly",
            "int array[10] = {1, 2, 3, 4,\n" +
            "                 5, 6, 7, 8, 9};\n"
            );
    }

    //IZ#131038:GNU style: reformat works wrongly with destructors
    public void testGnuStuleNewLineName() {
        setLoadDocumentText(
                "locale::~locale() throw()\n" +
                "{ _M_impl->_M_remove_reference(); }\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "locale::~locale () throw ()\n" +
                "{\n" +
                "  _M_impl->_M_remove_reference ();\n" +
                "}\n"
                );
    }

    //IZ#131043:GNU style: reformat works wrongly with function names
    public void testGnuStuleNewLineName2() {
        setLoadDocumentText(
                "void\n" +
                "__num_base::_S_format_float(const ios_base& __io, char* __fptr, char __mod)\n" +
                "{\n" +
                "return;\n" +
                "}\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "void\n" +
                "__num_base::_S_format_float (const ios_base& __io, char* __fptr, char __mod)\n" +
                "{\n" +
                "  return;\n" +
                "}\n"
                );
    }
    
    //IZ#131059:GNU style: Multiline alignment works wrongly
    public void testGnuStuleNewLineName3() {
        setLoadDocumentText(
                "int f(int a1, int a2,\n" +
                "      int a3) {\n" +
                "}\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "int\n" +
                "f (int a1, int a2,\n" +
                "   int a3) { }\n" 
                );
    }

    public void testGnuStuleNewLineName4() {
        setLoadDocumentText(
                "Db::Db (DbEnv *env, u_int32_t flags)\n" +
                ": imp_ (0)\n" +
                ", env_ (env)\n" +
                "{\n" +
                "}\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "Db::Db (DbEnv *env, u_int32_t flags)\n" +
                ": imp_ (0)\n" +
                ", env_ (env) { }\n" 
                );
    }

    public void testGnuStuleNewLineName5() {
        setLoadDocumentText(
                "tree decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "tree\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
    }

    public void testGnuStuleNewLineName6() {
        setLoadDocumentText(
                "B::tree A::\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "B::tree\n" +
                "A::decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
    }
    
    //IZ#131158:"Spaces Within Parenthesis|Braces" checkbox works wrongly
    public void testSpaceWithinBraces() {
        setLoadDocumentText(
                "int a[] = {1,(1+2),(2+ 3)};\n" +
                "int b[] = {  1,(1+2),(2+ 3)  };\n" +
                "int c[] = {  1,(1+2),(2+ 3)  \n" +
                "};\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceWithinBraces, true);
        reformat();
        assertDocumentText("Incorrect formatting array init",
                "int a[] = { 1, (1 + 2), (2 + 3) };\n" +
                "int b[] = { 1, (1 + 2), (2 + 3) };\n" +
                "int c[] = { 1, (1 + 2), (2 + 3) };\n"
                );
    }

    public void testSpaceWithinBraces2() {
        setLoadDocumentText(
                "int a[] = {1,(1+2),(2+ 3)};\n" +
                "int b[] = {  1,(1+2),(2+ 3)  };\n" +
                "int c[] = {  1,(1+2),(2+ 3)  \n" +
                "};\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect formatting array init",
                "int a[] = {1, (1 + 2), (2 + 3)};\n" +
                "int b[] = {1, (1 + 2), (2 + 3)};\n" +
                "int c[] = {1, (1 + 2), (2 + 3)};\n"
                );
    }

    public void testFunctionNameInNamespace() {
        setLoadDocumentText(
                "namespace {\n" +
                "void outCustomersList() {\n" +
                "return;\n" +
                "}\n" +
                "}\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "namespace\n" +
                "{\n" +
                "\n" +
                "  void\n" +
                "  outCustomersList ()\n" +
                "  {\n" +
                "    return;\n" +
                "  }\n" +
                "}\n"
                );
    }

    // IZ#131286:Nondeterministic behavior of formatter
    public void testIZ131286() {
        setLoadDocumentText(
                "int\n" +
                "foo() {\n" +
                "    s = (teststruct_t){\n" +
                "        .a = 1,\n" +
                "        .b = 2,\n" +
                "        .c = 3,\n" +
                "    };\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Nondeterministic behavior of formatter",
                "int\n" +
                "foo()\n" +
                "{\n" +
                "    s = (teststruct_t){\n" +
                "        .a = 1,\n" +
                "        .b = 2,\n" +
                "        .c = 3,\n" +
                "    };\n" +
                "}\n"
                );
    }
    
    // IZ#123656:Indenting behavior seems odd
    public void testIZ123656() {
        setLoadDocumentText(
                "int\n" +
                "foo() {\n" +
                "a\n" +
                "b\n" +
                "i=0;\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Indenting behavior seems odd",
                "int\n" +
                "foo()\n" +
                "{\n" +
                "    a\n" +
                "    b\n" +
                "    i = 0;\n" +
                "}\n"
                );
    }

    // IZ#123656:Indenting behavior seems odd
    public void testIZ123656_2() {
        setLoadDocumentText(
                "int\n" +
                "foo() {\n" +
                "a()\n" +
                "b\n" +
                "i=0;\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Indenting behavior seems odd",
                "int\n" +
                "foo()\n" +
                "{\n" +
                "    a()\n" +
                "    b\n" +
                "    i = 0;\n" +
                "}\n"
                );
    }

    // IZ#123656:Indenting behavior seems odd
    public void testIZ123656_3() {
        setLoadDocumentText(
            " C_MODE_START\n" +
            "#    include <decimal.h>\n" +
            "        C_MODE_END\n" +
            "\n" +
            "#    define DECIMAL_LONGLONG_DIGITS 22\n" +
            "\n" +
            "\n" +
            "        /* maximum length of buffer in our big digits (uint32) */\n" +
            "#    define DECIMAL_BUFF_LENGTH 9\n" +
            "        /*\n" +
            "        point on the border of our big digits))\n" +
            "*/\n" +
            "#    define DECIMAL_MAX_PRECISION ((DECIMAL_BUFF_LENGTH * 9) - 8*2)\n" +
            "\n"
            );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "C_MODE_START\n" +
            "#include <decimal.h>\n" +
            "C_MODE_END\n" +
            "\n" +
            "#define DECIMAL_LONGLONG_DIGITS 22\n" +
            "\n" +
            "\n" +
            "/* maximum length of buffer in our big digits (uint32) */\n" +
            "#define DECIMAL_BUFF_LENGTH 9\n" +
            "/*\n" +
            "point on the border of our big digits))\n" +
            " */\n" +
            "#define DECIMAL_MAX_PRECISION ((DECIMAL_BUFF_LENGTH * 9) - 8*2)\n" +
            "\n"
        );
    }

    public void testIdentMultyConstructor5() {
        setLoadDocumentText(
            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" + 
            "				 ulong query_length, bool using_trans,\n" +
            "				 bool suppress_use)\n" +
              ":Log_event(thd_arg,\n" +
            "	     ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
            "	      || (suppress_use          ? LOG_EVENT_SUPPRESS_USE_F    : 0)),\n" +
            "	     using_trans),\n" +
            "   data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
            "   db(thd_arg->db), q_len((uint32) query_length),\n" +
            "   error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
            "              ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
            "               0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
            "   thread_id(thd_arg->thread_id),\n" +
            "   /* save the original thread id; we already know the server id */\n" +
            "   slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
            "   flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
            "   sql_mode(thd_arg->variables.sql_mode),\n" +
            "   auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
            "   auto_increment_offset(thd_arg->variables.auto_increment_offset)\n" +
            "{\n" +
            "    time_t end_time;\n" +
            "}\n"
            );
        setDefaultsOptions("MySQL");
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceKeepExtra, false);
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" + 
            "                                 ulong query_length, bool using_trans,\n" +
            "                                 bool suppress_use)\n" +
            ": Log_event(thd_arg,\n" +
            "            ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
            "             | (suppress_use ? LOG_EVENT_SUPPRESS_USE_F : 0)),\n" +
            "            using_trans),\n" +
            "data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
            "db(thd_arg->db), q_len((uint32) query_length),\n" +
            "error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
            "           ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
            "            0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
            "thread_id(thd_arg->thread_id),\n" +
            "/* save the original thread id; we already know the server id */\n" +
            "slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
            "flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
            "sql_mode(thd_arg->variables.sql_mode),\n" +
            "auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
            "auto_increment_offset(thd_arg->variables.auto_increment_offset)\n" +
            "{\n" +
            "  time_t end_time;\n" +
            "}\n"
        );
    }

    // IZ#131379:GNU style: formatter works wrong with functions if it returns struct
    public void testIZ131379() {
        setLoadDocumentText(
                "tree\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n" +
                "\n" +
                "void\n" +
                "decl_shadowed_for_var_insert (tree from, tree to)\n" +
                "{\n" +
                "  return;\n" +
                "}\n" +
                "\n"
                );
        setDefaultsOptions("GNU");
        reformat();
        assertDocumentText("Indenting behavior seems odd",
                "tree\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n" +
                "\n" +
                "void\n" +
                "decl_shadowed_for_var_insert (tree from, tree to)\n" +
                "{\n" +
                "  return;\n" +
                "}\n" +
                "\n"
                );
    }

    // IZ#130509:Formatter should ignore empty function body
    public void testIZ130509() {
        setLoadDocumentText(
                "int foo0() { \n" +
                "  }\n" +
                "int foo1() { } \n" +
                "int foo2()\n" +
                " { } \n" +
                "int foo3(){}\n" +
                "int foo4(){\n" +
                "}\n" +
                "int foo5() { //\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.ignoreEmptyFunctionBody, true);
        reformat();
        assertDocumentText("Formatter should ignore empty function body",
                "int foo0() { }\n" +
                "\n" +
                "int foo1() { }\n" +
                "\n" +
                "int foo2() { }\n" +
                "\n" +
                "int foo3() { }\n" +
                "\n" +
                "int foo4() { }\n" +
                "\n" +
                "int foo5() { //\n" +
                "}\n"
                );
    }

    // IZ#130509:NPE on formatting unbalanced braces
    // Correct test case when macro will be taken into account
    public void testIZ135015() {
        setLoadDocumentText(
                "#define FOR(n) for (int i = 0; i < n; i++) {\n" +
                "\n" +
                "int g() {\n" +
                "    FOR(2)\n" +
                "        foo();\n" +
                "    }\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("IZ#130509:NPE on formatting unbalanced braces",
                "#define FOR(n) for (int i = 0; i < n; i++) {\n" +
                "\n" +
                "int g()\n" +
                "{\n" +
                "    FOR(2)\n" +
                "    foo();\n" +
                "}\n" +
                "}\n"
                );
    }
    
    // IZ#135205:'Spaces Before Keywords|else' option works wrongly in some cases
    public void testIZ135205() {
        setLoadDocumentText(
                "int main() {\n" +
                "    int i = 0;\n" +
                "    if (1) {\n" +
                "        i = 2;\n" +
                "    }else {\n" +
                "        i = 3;\n" +
                "    }\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("IZ#135205:'Spaces Before Keywords|else' option works wrongly in some cases",
                "int main()\n" +
                "{\n" +
                "    int i = 0;\n" +
                "    if (1) {\n" +
                "        i = 2;\n" +
                "    } else {\n" +
                "        i = 3;\n" +
                "    }\n" +
                "}\n"
                );
    }
    
    // IZ#131721:Comment moves on new line after reformat
    public void testIZ131721() {
        setLoadDocumentText(
                "char seek_scrbuf[SEEKBUFSIZE]; /* buffer for seeking */\n" +
                "int cf_debug; /* non-zero enables debug prints */\n" +
                "void *\n" +
                "cf_alloc(void *opaque, unsigned int items, unsigned int size)\n" +
                "{\n" +
                "    return (ptr);\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("IZ#131721:Comment moves on new line after reformat",
                "char seek_scrbuf[SEEKBUFSIZE]; /* buffer for seeking */\n" +
                "int cf_debug; /* non-zero enables debug prints */\n" +
                "\n" +
                "void *\n" +
                "cf_alloc(void *opaque, unsigned int items, unsigned int size)\n" +
                "{\n" +
                "    return (ptr);\n" +
                "}\n"
                );
    }

    public void testTypecast() {
        setLoadDocumentText(
                "int i = (int)'a';\n"+
                "void *\n" +
                "foo(void *ptr)\n" +
                "{\n" +
                "    ptr = *(long*)ptr +(int)ptr+ (struct A*)ptr;\n" +
                "    return(int)(ptr);\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceWithinTypeCastParens, true);
        reformat();
        assertDocumentText("Wrong type cast formatting",
                "int i = ( int ) 'a';\n"+
                "\n" +
                "void *\n" +
                "foo(void *ptr)\n" +
                "{\n" +
                "    ptr = *( long* ) ptr + ( int ) ptr + ( struct A* ) ptr;\n" +
                "    return ( int ) (ptr);\n" +
                "}\n"
                );
    }

    public void testReformatMultiLineAndSpacing() {
        setLoadDocumentText(
                  "void m(int a,\n"
                + "int b) {\n"
                + "    printf(a, \n"
                + "    \"haf\");\n"
                + "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceWithinMethodDeclParens, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceWithinMethodCallParens, true);
        reformat();
        assertDocumentText("Incorrect new-line indent",
                  "void m( int a,\n" 
                + "        int b ) {\n"
                + "    printf( a,\n"
                + "            \"haf\" );\n"
                + "}\n"
                );
    }
    
    public void testQtExtension() {
        setLoadDocumentText(
                "#define Q_OBJECT\n" +
                "#define signals private\n" +
                "#define slots\n" +
                "\n" +
                "class PrettyPopupMenu\n" +
                "{\n" +
                "};\n" +
                "\n" +
                "class Menu : public PrettyPopupMenu\n" +
                "{\n" +
                "    Q_OBJECT\n" +
                "\n" +
                "signals:\n" +
                "    void test();\n" +
                "\n" +
                "public slots:\n" +
                "    void slotActivated(int index);\n" +
                "\n" +
                "private slots:\n" +
                "    void slotAboutToShow();\n" +
                "\n" +
                "private:\n" +
                "    Menu();\n" +
                "};\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Wrong QT formatting",
                "#define Q_OBJECT\n" +
                "#define signals private\n" +
                "#define slots\n" +
                "\n" +
                "class PrettyPopupMenu\n" +
                "{\n" +
                "};\n" +
                "\n" +
                "class Menu : public PrettyPopupMenu\n" +
                "{\n" +
                "    Q_OBJECT\n" +
                "\n" +
                "signals:\n" +
                "    void test();\n" +
                "\n" +
                "public slots:\n" +
                "    void slotActivated(int index);\n" +
                "\n" +
                "private slots:\n" +
                "    void slotAboutToShow();\n" +
                "\n" +
                "private:\n" +
                "    Menu();\n" +
                "};\n"
                );
    }

    public void testExpandToTab() {
        setLoadDocumentText(
                "typedef struct pcihp {\n" +
                "\n" +
                " struct pcihp_slotinfo {\n" +
                "\t\tchar *name;\n" +
                "\t} slotinfo[10];\n" +
                "} pcihp_t;\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.expandTabToSpaces, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.tabSize, 4);
        for(int i = 0; i < 2; i++){
        reformat();
        assertDocumentText("Incorrect tab formatting",
                "typedef struct pcihp {\n" +
                "\n" +
                "\tstruct pcihp_slotinfo {\n" +
                "\t\tchar *name;\n" +
                "\t} slotinfo[10];\n" +
                "} pcihp_t;\n");
        }
    }

    public void testExpandToTab2() {
        setLoadDocumentText(
                "typedef struct pcihp {\n" +
                "\n" +
                " struct pcihp_slotinfo {\n" +
                "\t\tchar *name;\n" +
                "\t} slotinfo[10];\n" +
                "} pcihp_t;\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.expandTabToSpaces, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.tabSize, 8);
        reformat();
        assertDocumentText("Incorrect tab formatting",
                "typedef struct pcihp {\n" +
                "\n" +
                "    struct pcihp_slotinfo {\n" +
                "\tchar *name;\n" +
                "    } slotinfo[10];\n" +
                "} pcihp_t;\n");
    }

    public void testIZ145529() {
        setLoadDocumentText(
                "class Base {\n" +
                "\n" +
                "};\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceClass,
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect empty class formatting",
                "class Base {\n" +
                "};\n"
                );
    }

    public void testReformatConstructorInitializer3() {
        setLoadDocumentText(
            "class ClipCost {\n" +
            "public:\n" +
            "    ClipCost(OmFrameRate rate = omFrmRateInvalid)\n" +
            "      : m_type(Threshold::play1xThresh),\n" +
            "        m_ticksPerPane(getTicksPerPane(rate)),\n" +
            "        m_frameStart(0),\n" +
            "        m_thisFrame(~0)\n" +
            "    {\n" +
            "        // indentation should be like this\n" +
            "        for (uint i = 0; i < nCosts; i++)\n" +
            "            init(i);\n" +
            "\n" +
            "          // ide insists (strongly) on this indentation\n" +
            "          for (uint i = 0; i < nCosts; i++)\n" +
            "              init(i);\n" +
            "    }\n" +
            "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.constructorListContinuationIndent, 6);
        reformat();
        assertDocumentText("Incorrect reformatting of constructor initializer",
            "class ClipCost\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    ClipCost(OmFrameRate rate = omFrmRateInvalid)\n" +
            "          : m_type(Threshold::play1xThresh),\n" +
            "          m_ticksPerPane(getTicksPerPane(rate)),\n" +
            "          m_frameStart(0),\n" +
            "          m_thisFrame(~0)\n" +
            "    {\n" +
            "        // indentation should be like this\n" +
            "        for (uint i = 0; i < nCosts; i++)\n" +
            "            init(i);\n" +
            "\n" +
            "        // ide insists (strongly) on this indentation\n" +
            "        for (uint i = 0; i < nCosts; i++)\n" +
            "            init(i);\n" +
            "    }\n" +
            "}\n");
    }

    public void testIZ144976() {
        setLoadDocumentText(
                "int Ar[] ={\n" +
                "1, 2, 3,\n" +
                " 4, 5 };\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect arry init formatting",
                "int Ar[] = {\n" +
                "    1, 2, 3,\n" +
                "    4, 5\n" +
                "};\n"
                );
    }

    public void testIZ144976_2() {
        setLoadDocumentText(
                "int Ar[] ={1, 2, 3,\n" +
                " 4, 5 };\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect arry init formatting",
                "int Ar[] = {1, 2, 3,\n" +
                "    4, 5};\n"
                );
    }

    public void testIZ144976_3() {
        setLoadDocumentText(
                "int Ar[] ={1, 2, 3,\n" +
                "4, 5 \n" +
                " };\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect arry init formatting",
                "int Ar[] = {1, 2, 3,\n" +
                "    4, 5};\n"
                );
    }

    // IZ#156015:'Format' works wrongly with 'while'
    public void testIZ156015() {
        setLoadDocumentText(
                "    int main() {\n" +
                "    \n" +
                "    do {\n" +
                "        int i;\n" +
                "    } while(0);\n" +
                "\n" +
                "    while(0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "    \n" +
                "    return (0);\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("IZ#156015:'Format' works wrongly with 'while'",
                "int main() {\n" +
                "\n" +
                "    do {\n" +
                "        int i;\n" +
                "    } while (0);\n" +
                "\n" +
                "    while (0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "\n" +
                "    return (0);\n" +
                "}\n"
                );
    }

    // IZ#156015:'Format' works wrongly with 'while'
    public void testIZ156015_2() {
        setLoadDocumentText(
                "    int main() {\n" +
                "    \n" +
                "    if (0) do {\n" +
                "        int i;\n" +
                "    } while(0);\n" +
                "\n" +
                "    while(0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "    \n" +
                "    return (0);\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("IZ#156015:'Format' works wrongly with 'while'",
                "int main() {\n" +
                "\n" +
                "    if (0) do {\n" +
                "            int i;\n" +
                "        } while (0);\n" +
                "\n" +
                "    while (0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "\n" +
                "    return (0);\n" +
                "}\n"
                );
    }

    public void testIZ170649() {
        setLoadDocumentText(
                "switch (value) {\n" +
                "  case Foo::BAR:\n" +
                "      cout << \"Bar!\" << endl;\n" +
                "    break;\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("IZ 170649: Wrong formatting in switch-case with namespace",
                "switch (value) {\n" +
                "    case Foo::BAR:\n" +
                "        cout << \"Bar!\" << endl;\n" +
                "        break;\n" +
                "}\n"
                );
    }

    // IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)
    public void testIZ166051_1() {
        setLoadDocumentText(
                "int main() {\n" +
                "    do {\n" +
                "        size_t i = 0; while (1) {\n" +
                "    }\n" +
                "    } while (1);\n" +
                "    return 0;\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)",
                "int main() {\n" +
                "    do {\n" +
                "        size_t i = 0;\n" +
                "        while (1) {\n" +
                "        }\n" +
                "    } while (1);\n" +
                "    return 0;\n" +
                "}\n"
                );
    }

    // IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)
    public void testIZ166051_2() {
        setLoadDocumentText(
                "int main() {\n" +
                "  do{\n" +
                "    size_t i = 0;while(true){\n" +
                "  }\n" +
                "    size_t i = 0;\n" +
                "  }while(true);\n" +
                "    return 0;\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)",
                "int main() {\n" +
                "    do {\n" +
                "        size_t i = 0;\n" +
                "        while (true) {\n" +
                "        }\n" +
                "        size_t i = 0;\n" +
                "    } while (true);\n" +
                "    return 0;\n" +
                "}\n"
                );
    }

    // IZ#159334:Cannot format initialization list the way I want
    public void testIZ159334_1() {
        setLoadDocumentText(
                "MyClass::MyClass(int param1, int param2)\n" +
                "   : _var1(param1),\n" +
                "     _var2(param2)\n" +
                "{\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.NEW_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.constructorListContinuationIndent, 4);
        reformat();
        assertDocumentText("IZ#159334:Cannot format initialization list the way I want",
                "MyClass::MyClass(int param1, int param2)\n" +
                "    : _var1(param1),\n" +
                "    _var2(param2)\n" +
                "{\n" +
                "}\n"
                );
    }

    // IZ#159334:Cannot format initialization list the way I want
    public void testIZ159334_2() {
        setLoadDocumentText(
                "class Class\n" +
                "{\n" +
                "    int p, r;\n" +
                "public:\n" +
                "\n" +
                "    Class()\n" +
                "      : p(0),\n" +
                "      r(0) {\n" +
                "    }\n" +
                "    Class(const Class& orig);\n" +
                "    virtual ~Class();\n" +
                "private:\n" +
                "\n" +
                "};\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.constructorListContinuationIndent, 4);
        reformat();
        assertDocumentText("IZ#159334:Cannot format initialization list the way I want",
                "class Class\n" +
                "{\n" +
                "    int p, r;\n" +
                "public:\n" +
                "\n" +
                "    Class()\n" +
                "        : p(0),\n" +
                "        r(0) {\n" +
                "    }\n" +
                "    Class(const Class& orig);\n" +
                "    virtual ~Class();\n" +
                "private:\n" +
                "\n" +
                "};\n"
                );
    }
    
    //  Bug 180110 - Inconsistent C/C++ switch statement formatting
    public void testIZ180110() {
        setLoadDocumentText(
                "int foo(){\n"
                + "    switch(value)\n"
                + "    {\n"
                + "     case MACRO(x):\n"
                + "      {\n"
                + "        break;\n"
                + "    }\n"
                + "    case MACRO_2:\n"
                + "     {\n"
                + "        break;\n"
                + "   }\n"
                + "    case (MACRO_3):\n"
                + "   {\n"
                + "    break;\n"
                + "  }\n"
                + "    }\n"
                + "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceSwitch,
                CodeStyle.BracePlacement.NEW_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        reformat();
        assertDocumentText("Bug 180110 - Inconsistent C/C++ switch statement formatting",
                "int foo() {\n"
                + "    switch (value)\n"
                + "    {\n"
                + "    case MACRO(x):\n"
                + "    {\n"
                + "        break;\n"
                + "    }\n"
                + "    case MACRO_2:\n"
                + "    {\n"
                + "        break;\n"
                + "    }\n"
                + "    case (MACRO_3):\n"
                + "    {\n"
                + "        break;\n"
                + "    }\n"
                + "    }\n"
                + "}\n");
    }

    //  Bug 176820 -  Erroneous formatting of typecasting of reference
    public void testIZ176820() {
        setLoadDocumentText(
                "void m(char *a)\n" +
                "{\n"+
                "    int *i;\n" +
                "    i=(int *) &a;\n"+
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m(char *a)\n" +
                "{\n"+
                "    int *i;\n" +
                "    i = (int *) &a;\n"+
                "}\n"
                    );
    }

    //  Bug 188117 - "alt+shift+f" and "\"
    public void testIZ188117() {
        setLoadDocumentText(
                "printf(\"example of %s issue\", \\\n" +
                "    \"this\");\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect \\ formatting",
                "printf(\"example of %s issue\", \\\n" +
                "    \"this\");\n"
                    );
    }
    public void testIZ188117_2() {
        setLoadDocumentText(
                "printf(\"example of %s issue\",\\\n" +
                "    \"this\");\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect \\ formatting",
                "printf(\"example of %s issue\",\\\n" +
                "    \"this\");\n"
                    );
    }
    public void testIZ188117_3() {
        setLoadDocumentText(
                "if ((data[readBytes - 2] == 05) && \\\n" +
                "    (data[readBytes - 1] == 04)) {\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect \\ formatting",
                "if ((data[readBytes - 2] == 05) && \\\n" +
                "    (data[readBytes - 1] == 04)) {\n" +
                "}\n"
                );
    }

    public void test194239() {
        setLoadDocumentText(
                "class Foo\n" +
                "{\n" +
                "    Foo& operator=(const Foo& other);\n" +
                "    Foo* operator ==(const Foo& other);\n" +
                "    Foo& xxx(const Foo& other);\n" +
                "};\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect \\ formatting",
                "class Foo\n" +
                "{\n" +
                "    Foo& operator=(const Foo& other);\n" +
                "    Foo* operator==(const Foo& other);\n" +
                "    Foo& xxx(const Foo& other);\n" +
                "};\n"
                );
    }

    public void test194813() {
        setLoadDocumentText(
                "class A\n" +
                "{\n" +
                "};\n" +
                "\n" +
                "A* foo(A* t)\n" +
                "{\n" +
                "    return dynamic_cast< ::A * > (t);\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect < :: formatting",
                "class A\n" +
                "{\n" +
                "};\n" +
                "\n" +
                "A* foo(A* t)\n" +
                "{\n" +
                "    return dynamic_cast< ::A *> (t);\n" +
                "}\n"
                );
    }
    
    public void test219414() {
        setLoadDocumentText(
                "int foo() {\n" +
                "    result = ::std::accumulate(::std::begin(x), ::std::end(x), 0.0,\n" +
                "             [&m](Double a, Double x) -> Double\n" +
                "       {\n" +
                "             return a+ (x - m) * (x - m);\n" +
                "       });\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    result = ::std::accumulate(::std::begin(x), ::std::end(x), 0.0,\n" +
                "                               [&m](Double a, Double x) -> Double\n" +
                "                               {\n" +
                "                                   return a + (x - m) * (x - m);\n" +
                "                               });\n" +
                "}\n"
                );
    }
    
    public void test219414_2() {
        setLoadDocumentText(
                "int foo() {\n" +
                "  [] () {\n" +
                "      return 1;\n" +
                "  }\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    [] () {\n" +
                "        return 1;\n" +
                "    }\n" +
                "}\n"
                );
    }
    
    public void test219414_3() {
        setLoadDocumentText(
                "int foo() {\n" +
                "  [] {\n" +
                "      cout << \"Hello, my Greek friends\";\n" +
                "  } ();\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    [] {\n" +
                "        cout << \"Hello, my Greek friends\";\n" +
                "    } ();\n" +
                "}\n"
                );
    }

    public void test219414_4() {
        setLoadDocumentText(
                "int foo() {\n" +
                "auto a = [&] {\n" +
                "static int i = 0;\n" +
                "i++;\n" +
                "td::cout << i << std::endl;\n" +
                "if (i < 10)\n" +
                "    a(); //recursive call\n" +
                "  };\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    auto a = [&] {\n" +
                "        static int i = 0;\n" +
                "        i++;\n" +
                "        td::cout << i << std::endl;\n" +
                "        if (i < 10)\n" +
                "            a(); //recursive call\n" +
                "    };\n" +
                "}\n"
                );
    }
    
    public void test219414_5() {
        setLoadDocumentText(
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val) {\n" +
                "             cout << val;\n" +
                "             });\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val) {\n" +
                "                 cout << val;\n" +
                "             });\n" +
                "}\n"
                );
    }

    public void test219414_6() {
        setLoadDocumentText(
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val) {\n" +
                "             cout << val;\n" +
                "             });\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val)\n" +
                "             {\n" +
                "                 cout << val;\n" +
                "             });\n" +
                "}\n"
                );
    }
    
    public void test219414_7() {
        setLoadDocumentText(
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val) {\n" +
                "             cout << val;\n" +
                "             });\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE_FULL_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val)\n" +
                "                 {\n" +
                "                 cout << val;\n" +
                "                 });\n" +
                "}\n"
                );
    }

    public void test219414_8() {
        setLoadDocumentText(
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val) {\n" +
                "             cout << val;\n" +
                "             });\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        reformat();
        assertDocumentText("Incorrect < lambda formatting",
                "int foo() {\n" +
                "    for_each(v.begin(), v.end(),\n" +
                "             [] (int val)\n" +
                "               {\n" +
                "                 cout << val;\n" +
                "               });\n" +
                "}\n"
                );
    }
    
    public void test219417() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "    ::std::plus<Double>();\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect template formatting",
                "int foo()\n" +
                "{\n" +
                "    ::std::plus<Double>();\n" +
                "}\n"
                );
    }

    public void test216976() {
        setLoadDocumentText(
                "struct Compare\n" +
                "{\n" +
                "\n" +
                "    bool func(const Book* a, const Book* b) const\n" +
                "    {\n" +
                "        return a->word < b->word;\n" +
                "    }\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reference formatting",
                "struct Compare\n" +
                "{\n" +
                "\n" +
                "    bool func(const Book* a, const Book* b) const\n" +
                "    {\n" +
                "        return a->word < b->word;\n" +
                "    }\n" +
                "}\n"
                );
    }
    
    public void test219739() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "    if (typeid (node1).name() == typeid (struct Node).name())\n" +
                "        return 0;\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reference formatting",
                "int foo()\n" +
                "{\n" +
                "    if (typeid (node1).name() == typeid (struct Node).name())\n" +
                "        return 0;\n" +
                "}\n"
                );
    }
    
    public void test216290() {
        setLoadDocumentText(
                "void someMethod(const string& s1, const string& s2)\n" +
                "{\n" +
                "    std::get<0>(some_tuple);\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reference formatting",
                "void someMethod(const string& s1, const string& s2)\n" +
                "{\n" +
                "    std::get<0>(some_tuple);\n" +
                "}\n"
                );
    }

    public void test216290_1() {
        setLoadDocumentText(
                "void someMethod(const string& s1, const string& s2)\n" +
                "{\n" +
                "    std::get<int>(some_tuple);\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reference formatting",
                "void someMethod(const string& s1, const string& s2)\n" +
                "{\n" +
                "    std::get<int>(some_tuple);\n" +
                "}\n"
                );
    }

    public void test216290_2() {
        setLoadDocumentText(
                "void someMethod(const string& s1, const string& s2)\n" +
                "{\n" +
                "    std::get<class string>(some_tuple);\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reference formatting",
                "void someMethod(const string& s1, const string& s2)\n" +
                "{\n" +
                "    std::get<class string>(some_tuple);\n" +
                "}\n"
                );
    }

    public void test220196() {
        setLoadDocumentText(
                "template<typename N, typename TAG = struct asdfghjk>\n"
              + "              void copy(){\n"
              + "}\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect template formatting",
                "template<typename N, typename TAG = struct asdfghjk>\n"
              + "void copy()\n"
              + "{\n"
              + "}\n");
    }
    
    public void test217975() {
        setLoadDocumentText(
                "int v[] {1, 2, 3};\n"
              + "\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect uniform initialization",
                "int v[]{1, 2, 3};\n"
              + "\n");
    }
    
    public void test217975_1() {
        setLoadDocumentText(
                "int v[] {1, 2, 3};\n"
              + "\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.spaceBeforeArrayInitLeftBrace, true);
        reformat();
        assertDocumentText("Incorrect uniform initialization",
                "int v[] {1, 2, 3};\n"
              + "\n");
    }
    
    public void test217975_2() {
        setLoadDocumentText(
                "int &&a;\n" 
              + "int&& b;\n" 
              + "int && c;\n"
              + "\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect rvalue reference",
                "int &&a;\n" 
              + "int&& b;\n" 
              + "int && c;\n"
              + "\n");
    }
    
    public void test222396() {
        setLoadDocumentText(
                "if(0){\n" 
              + "        int a=0;\n" 
              + "    }//sample comment\n"
              + "else\n"
              + "    int b = 1;");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect rvalue reference",
                "if (0) {\n" 
              + "    int a = 0;\n" 
              + "}//sample comment\n"
              + "else\n"
              + "    int b = 1;");
    }
    
    public void test222887() {
        setLoadDocumentText(
                "class A {\n" 
              + "public:\n" 
              + "    int a {(int)123};\n"
              + "};\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect C++11: brace-Initialization format",
                "class A\n"
              + "{\n" 
              + "public:\n" 
              + "    int a{(int) 123};\n"
              + "};\n");
    }
    
    public void test222887_2() {
        setLoadDocumentText(
                "int main()\n"
              + "{\n" 
              + "    int a {(int) 123};\n" 
              + "};\n");
        setDefaultsOptions();
        reformat();
        assertDocumentText("ncorrect C++11: brace-Initialization format",
                "int main()\n"
              + "{\n" 
              + "    int a{(int) 123};\n" 
              + "};\n");
    }
    
    public void test238995_1() {
        setLoadDocumentText(
                "std::vector<std::shared_ptr<int>> indices = m_indices;\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect template formatting",
                "std::vector<std::shared_ptr<int>> indices = m_indices;\n"
                );
    }

    public void test238995_1_2() {
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "    std::vector<std::shared_ptr<int>> indices = m_indices;\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect template formatting",
                "int foo()\n" +
                "{\n" +
                "    std::vector<std::shared_ptr<int>> indices = m_indices;\n" +
                "}\n"
                );
    }

    public void test238995_2() {
        setLoadDocumentText(
                "ClassA::ClassA() :\n" +
                "      memberX(),\n" +
                "      memberY(),\n" +
                "      memberZ()\n" +
                "{\n" +
                "}\n" +
                "\n" +
                "namespace X\n" +
                "{\n" +
                "\n" +
                "    ClassB::ClassB() :\n" +
                "          memberX(),\n" +
                "          memberY(),\n" +
                "          memberZ()\n" +
                "    {\n" +
                "    }\n" +
                "};\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putInt(EditorOptions.constructorListContinuationIndent, 6);
        reformat();
        assertDocumentText("Incorrect constructor list in namespace",
                "ClassA::ClassA() :\n" +
                "      memberX(),\n" +
                "      memberY(),\n" +
                "      memberZ()\n" +
                "{\n" +
                "}\n" +
                "\n" +
                "namespace X\n" +
                "{\n" +
                "\n" +
                "    ClassB::ClassB() :\n" +
                "          memberX(),\n" +
                "          memberY(),\n" +
                "          memberZ()\n" +
                "    {\n" +
                "    }\n" +
                "};\n"
                );
    }
    
    public void test241497() {
        setLoadDocumentText(
                "enum class test : char\n" +
                "{\n" +
                "    valueA = 'a',\n" +
                "    valueB = 'b',\n" +
                "    valueC\n" +
                "};\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("C++ enum class with values are not formatted properly",
                "enum class test : char\n" +
                "{\n" +
                "    valueA = 'a',\n" +
                "    valueB = 'b',\n" +
                "    valueC\n" +
                "};\n"
                );
    }
    
    public void test244599() {
        setLoadDocumentText(
                "class A {\n" +
                "    value_t operator & () {}\n" +
                "    const value_t operator &() const {}\n" +
                "    value_t& operator * () {}\n" +
                "    const value_t& operator * () const {}\n" +
                "    value_t* operator -> () {}\n" +
                "    const value_t* operator -> () const {}\n" +
                "    value_t& operator [] (std::size_t index) {}\n" +
                "    const value_t& operator [] (std::size_t index) const {}\n" +
                "};\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText(" 'operator ->' and 'operator []' has not empty line between methods after reformat",
                "class A\n" +
                "{\n" +
                "\n" +
                "    value_t operator&()\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    const value_t operator&() const\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    value_t& operator*()\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    const value_t& operator*() const\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    value_t* operator->()\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    const value_t* operator->() const\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    value_t& operator[](std::size_t index)\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    const value_t& operator[](std::size_t index) const\n" +
                "    {\n" +
                "    }\n" +
                "};\n"
                );
    }
    
    public void test244600() {
        setLoadDocumentText(
                "class A\n" +
                "{\n" +
                "\n" +
                "    bool operator <= (const A& right) const {\n" +
                "    }\n" +
                "\n" +
                "    bool operator > (const A& right) const {\n" +
                "    }\n" +
                "};\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("after reformat 'operator>' and 'operator<' has not space ",
                "class A\n" +
                "{\n" +
                "\n" +
                "    bool operator<=(const A& right) const\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    bool operator>(const A& right) const\n" +
                "    {\n" +
                "    }\n" +
                "};\n"
                );
    }
    
    public void test246062() {
        setLoadDocumentText(
                "class A {\n" +
                "    int n, m = 0;\n" +
                "public:\n" +
                "\n" +
                "    A(int o) : m{o}, n{0}\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    void foo();\n" +
                "};\n" +
                "\n" +
                "void A::foo() {\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    int m{11};\n" +
                "    A a(11);\n" +
                "    a.foo();\n" +
                "    return 0;\n" +
                "}\n"
                );
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
            put(EditorOptions.newLineBeforeBraceClass, 
            CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
            put(EditorOptions.newLineBeforeBraceDeclaration, 
            CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("incorrect reformat brace-initialization",
                "class A {\n" +
                "    int n, m = 0;\n" +
                "public:\n" +
                "\n" +
                "    A(int o) : m{o}, n{0}\n" +
                "    {\n" +
                "    }\n" +
                "\n" +
                "    void foo();\n" +
                "};\n" +
                "\n" +
                "void A::foo() {\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    int m{11};\n" +
                "    A a(11);\n" +
                "    a.foo();\n" +
                "    return 0;\n" +
                "}\n"
                );
    } 
    
    public void test249953() {
        setLoadDocumentText(
                "    int main(int argc, char** argv)\n"+
                "{\n"+
                "    while (not fooBar({1, 2, 3}, variable))\n"+
                "{\n"+
                "    object.method({1, 2, 3}, ++variable);\n"+
                "}\n"+
                "\n"+
                "    return 0;\n"+
                "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        reformat();
        assertDocumentText("Incorrect uniform initialization",
                "int main(int argc, char** argv)\n"+
                "{\n"+
                "    while (not fooBar({1, 2, 3}, variable))\n"+
                "    {\n"+
                "        object.method({1, 2, 3}, ++variable);\n"+
                "    }\n"+
                "\n"+
                "    return 0;\n"+
                "}\n");
    }
    
    public void test257662() {
        setLoadDocumentText(
                "void renderScene(void)\n" +
                "{\n" +
                "    Do(5, [] {\n" +
                "        clear(100, 100, 100);\n" +
                "        clear(100, 100, 100);\n" +
                "        clear(100, 100, 100);\n" +
                "    });\n" +
                "}\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("after reformat lamda does not work",
                "void renderScene(void)\n" +
                "{\n" +
                "    Do(5, [] {\n" +
                "        clear(100, 100, 100);\n" +
                "        clear(100, 100, 100);\n" +
                "        clear(100, 100, 100);\n" +
                "    });\n" +
                "}\n"
                );
    }

    public void test257662_2() {
        setLoadDocumentText(
                "void\n" +
                "renderScene(void)\n" +
                "{\n" +
                "    using namespace D2D::draw;\n" +
                "    Do(5, []\n" +
                "    {\n" +
                "        clear(100, 100, 100);\n" +
                "       clear(100, 100, 100);\n" +
                "       clear(100, 100, 100);\n" +
                "       clear(100, 100, 100);\n" +
                "    });\n" +
                "\n" +
                "}\n"
                );
        setDefaultsOptions();
        CodeStyle codeStyle = CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument());
        EditorOptions.resetToDefault(codeStyle, EditorOptions.ANSI_PROFILE);
        reformat();
        assertDocumentText("after reformat lamda does not work",
                "void\n" +
                "renderScene(void)\n" +
                "{\n" +
                "    using namespace D2D::draw;\n" +
                "    Do(5, []\n" +
                "    {\n" +
                "        clear(100, 100, 100);\n" +
                "        clear(100, 100, 100);\n" +
                "        clear(100, 100, 100);\n" +
                "        clear(100, 100, 100);\n" +
                "    });\n" +
                "\n" +
                "}\n"
                );
    }
    
    public void test2258589() {
        setLoadDocumentText(
                "void foo() {\n" 
              + "    return;\n" 
              + "}\n"
              + "\n"
              + "/*static*/ void boo() {\n"
              + "    return;\n"
              + "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect rvalue reference",
                "void foo() {\n" 
              + "    return;\n" 
              + "}\n"
              + "\n"
              + "/*static*/ void boo() {\n"
              + "    return;\n"
              + "}\n");
    }

    public void test253386() {
        setLoadDocumentText(
                "int main() {\n" 
              + "    B::A<T...>{}.loop(std::forward<F>(body), std::make<sizeof...(T)> {}, std::forward<T>(objects)...);\n" 
              + "    int a{1};\n"
              + "    return {0};\n"
              + "}\n");
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        reformat();
        assertDocumentText("Incorrect rvalue reference",
                "int main() {\n" 
              + "    B::A<T...>{}.loop(std::forward<F>(body), std::make<sizeof...(T)>{}, std::forward<T>(objects)...);\n" 
              + "    int a{1};\n"
              + "    return {0};\n"
              + "}\n");
    }
    
}
