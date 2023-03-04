/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.php.hints;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.php.GeneralPHP;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Riha
 */
public class testHints extends GeneralPHP {

    static final String TEST_PHP_NAME = "PhpProject_hints_0001";

    public testHints(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testHints.class).addTest(
                "CreateApplication",
                "testAmbiguousHint",
                "testImmutableVariable",
                "testImmutableVariableSimple",
                "testUnusedUse",
                "testClassExpr",
                "testBinaryNotationIncorrect",
                "testBinaryNotationCorrect",
                "testShortArraySyntax",
                "testPhp54RelatedHint",
                "testPhp53RelatedHint",
                "testSmartySurroundWith",
                "testTooManyNestedBlocks",
                "testUnreachableStatement",
                "testEmptyStatement",
                "testDirectUseOfSuperGlobals",
                "testConditionBraces",
                "testUnnecessaryClosingDelimiter").enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }

    public void CreatePHPFile() {
        startTest();
        SetAspTags(TEST_PHP_NAME, true);
        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        endTest();
    }

    public void testClassExpr() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Class");
        startTest();
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n class Test{ \n public static function test(){\n");
        file.setCaretPosition("?>", true);
        TypeCode(file, "Bar::{'test'}();");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("error", 0, "Error annotation showed for testClassExpr", file);
        endTest();
    }

    public void testBinaryNotationIncorrect() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n $wrongBinary=0b002;");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("Syntax error: unexpected: 2", 1, "Incorrect number of error hints", file);
        endTest();
    }

    public void testBinaryNotationCorrect() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.replace("0b002", "0b001");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("Syntax error: unexpected: 2", 0, "Incorrect number of error hints", file);
        endTest();
    }

    public void testShortArraySyntax() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n $arr = [0 => \"Foo\"];");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("error", 0, "Incorrect number of error hints", file);
        endTest();
    }

    public void testPhp54RelatedHint() {
        startTest();
        SetPhpVersion(TEST_PHP_NAME, 3);
        EditorOperator file = new EditorOperator("Class.php");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("Language feature not compatible", 2, "Incorrect number of error hints", file);
        endTest();
    }

    public void testPhp53RelatedHint() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n namespace test;");
        SetPhpVersion(TEST_PHP_NAME, 2);
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("Language feature not compatible", 3, "Incorrect number of error hints", file);
        endTest();
    }

    public void testImmutableVariableSimple() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Immutable");
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n $foo=1;\n $foo=2;");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("1 assignment(s) (2 used)", 2, "Incorrect number of Immutable hints", file);
        endTest();
    }

    public void testAmbiguousHint() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "AmbiguousHint");
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n $foo == 1+1;");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("Possible accidental ", 1, "Incorrect number of Ambiguous comparison hints", file);
        endTest();
    }

    public void testImmutableVariable() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Immutable2");
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n for($i=0;$i<10;$i=$i+1){}");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("1 assignment(s) (2 used)", 0, "Incorrect number of Immutable hints", file);
        endTest();
    }

    public void testUnusedUse() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "UnusedUse");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n use \\Foo\\Bar\\Baz;");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("Unused Use Statement", 1, "Incorrect number of Unused Use Statement hints", file);
        endTest();
    }

    private void checkNumberOfAnnotationsContains(String annotation, int expectedOccurences, String failMsg, EditorOperator file) {

        final EditorOperator eo = new EditorOperator(file.getName());
        final int limit = expectedOccurences;
        try {
            new Waiter(new Waitable() {
                
                @Override
                public Object actionProduced(Object oper) {
                    return eo.getAnnotations().length > limit ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait parser annotations."); // NOI18N
                }
            }).waitAction(null);


            int numberOfErrors = 0;
            int lines = file.getText().split(System.getProperty("line.separator")).length;
            Object[] ann;
            int lineCounter = 1;
            while (lineCounter <= lines) {
                ann = file.getAnnotations(lineCounter);
                for (Object o : ann) {
                    if (EditorOperator.getAnnotationShortDescription(o).toString().contains(annotation) && !EditorOperator.getAnnotationShortDescription(o).toString().contains("HTML error checking")) {
                        numberOfErrors++;
                    }
                }
                lineCounter++;
            }
            assertEquals(failMsg, expectedOccurences, numberOfErrors);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void testSmartySurroundWith() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "Smarty Template", null);
        startTest();
        TypeCode(file, "\n<h1>SomeHeader</h1>\n");
        file.select("SomeHeader");
        file.save();
        new EventTool().waitNoEvent(1000);
        checkNumberOfAnnotationsContains("Surround with ...", 1, "Possibility to wrap code with chosen Smarty tag", file);
        endTest();
    } 
    
    public void testTooManyNestedBlocks() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "NestedBlocks");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\nclass A {\n" +
                        "    function a() {\n" +
                        "        if(true) {\n" +
                        "            if(true) {\n" +
                        "                if(true) {\n" +
                        "                    \n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        " \n" +
                        "}");
        file.save();
        new EventTool().waitNoEvent(2000);
        checkNumberOfAnnotationsContains("Too Many Nested Blocks in Function Declaration", 1, "Incorrect number of Too Many Nested Blocks hints", file);
        endTest();
    }
    
    public void testUnreachableStatement() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Unreachable");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitEvent(1000);
        TypeCode(file, "function unreachableTest() {\n" +
                        "    return 1;\n" +
                        "    echo 'unreachable';\n" +
                        "}");
        file.save();
        new EventTool().waitNoEvent(1500);
        checkNumberOfAnnotationsContains("Unreachable Statement", 1, "Incorrect number of Unreachable Statement hints", file);
        endTest();
    }
    
    public void testEmptyStatement() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "EmptyStatement");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "function emptyStatement() {\n" +
                        "    ;\n" +
                        "}");
        file.save();
        new EventTool().waitNoEvent(1500);
        checkNumberOfAnnotationsContains("Empty Statement", 1, "Incorrect number of Empty Statement hints", file);
        endTest();
    }
    
    public void testDirectUseOfSuperGlobals() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Superglobals");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "function useSuperGlobals() {\n" +
                        "    $a = $_GET['a'];\n" +
                        "    $b = $_POST['b'];\n" +
                        "    $c = $_SERVER['c'];\n" +
                        "    $d = $_COOKIE['d'];\n" +
                        "    $e = $_ENV['e'];\n" +
                        "    $f = $_REQUEST['f'];\n" +
                        "    $g = $_SESSION['g'];\n" +
                        "    $i = $_FILES['i'];\n" +
                        "}");
        file.save();
        new EventTool().waitNoEvent(1500);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_GET Array Directly.", 1, "Incorrect number of Direct Use of Superglobals hints", file);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_POST Array Directly.", 1, "Incorrect number of Direct Use of Superglobals hints", file);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_SERVER Array Directly.", 1, "Incorrect number of Direct Use of Superglobals hints", file);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_COOKIE Array Directly.", 1, "Incorrect number of Direct Use of Superglobals hints", file);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_ENV Array Directly.", 1, "Incorrect number of Direct Use of Superglobals hints", file);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_REQUEST Array Directly.", 1, "Incorrect number of Direct Use of Superglobals hints", file);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_SESSION Array Directly.", 0, "Incorrect number of Direct Use of Superglobals hints", file);
        checkNumberOfAnnotationsContains("Do not Access Superglobal $_FILES Array Directly.", 0, "Incorrect number of Direct Use of Superglobals hints", file);
        endTest();
    }
    
    public void testConditionBraces() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "ConditionBraces");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "if(true)\n" +
                        "    echo 1;\n" +
                        "else\n" +
                        "    echo 2;");
        file.save();
        new EventTool().waitNoEvent(1500);
        checkNumberOfAnnotationsContains("If-Else Statements Must Use Braces", 2, "Incorrect number of Conditions Must Use Braces hints", file);
        endTest();
    }
    
    public void testUnnecessaryClosingDelimiter() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "ClosingDelimiter");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n ?>");
        file.save();
        new EventTool().waitNoEvent(1500);
        checkNumberOfAnnotationsContains("Unnecessary Closing Delimiter", 1, "Incorrect number of Unnecessary Closing Delimiter hints", file);
        endTest();
    }
}
