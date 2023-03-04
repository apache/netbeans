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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.Collection;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class UtilitiesTest extends NbTestCase {
    
    public UtilitiesTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testNameGuess1() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {toString();}}", 54, "toString");
    }

    public void testNameGuess2() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getX();} public int getX() {return 0;}}", 54, "x");
    }
    
    public void testNameGuess3() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getData();} public int getData() {return 0;}}", 54, "data");
    }
    
    public void testNameGuess4() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getProcessedData();} public int getProcessedData() {return 0;}}", 54, "processedData");
    }
    
    public void testNameGuess5() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {isEnabled();} public boolean isEnabled() {return true;}}", 54, "enabled");
    }
    
    public void testNameGuess6() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {get();} public int get() {return 0;}}", 52, "get");
    }
    
    public void testNameGuessKeyword() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getDo();} public int getDo() {return 0;}}", 52, "aDo");
    }
    
    public void testNameGuessCapitalLetters1() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getFOOBar();} public int getFOOBar() {return 0;}}", 52, "fooBar");
    }

    public void testNameGuessCapitalLetters2() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getBBar();} public int getBBar() {return 0;}}", 52, "bBar");
    }

    public void testNameGuessKeywordNoShortName() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {t(this);}}", 54, "aThis");
    }
    
    public void test225641() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getIssueDate(|);} public int getIssueDate() {return 0;}}", "issueDate");
    }
    
    public void test225532() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() { String str = \"127.|0.0.1\";} }", "string");
    }
    
    public void test225541() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() { String str = \"UPL|OAD_IMAGE_TO_FRIEND_PAGE\";} }", "upload_image_to_friend_page");
    }
    
    public void testShortName1() throws Exception {
        //TODO: better display name:
        performShortNameTest("package test; public class Test { public void t(Object... obj) { | }}", "((boolean[]) obj)[i]", "...(boolean)[]");
    }
    
    public void testShortName220031() throws Exception {
        performShortNameTest("package test; public class Test { public void t() { | }}", "new Object[0]", "...new Object[...]");
    }

    public void testToConstantName() {
        assertEquals("SOME_CONSTANT", Utilities.toConstantName("someConstant"));
        assertEquals("SOME_HTML_CONSTANT", Utilities.toConstantName("someHTMLConstant"));
        assertEquals("CAPITAL_START", Utilities.toConstantName("CapitalStart"));
        assertEquals("", Utilities.toConstantName(""));
        assertEquals("UPLOAD_IMAGE_TO_FRIEND_PAGE", Utilities.toConstantName("upload_image_to_friend_page"));
    }
    
    public void testCapturedTypeArray164543() throws Exception {
        performCapturedTypeTest("package test; public class Test {public void t() {java.util.Map m; m.getClass().getTypeParameters(|); }}",
                                "java.lang.reflect.TypeVariable<java.lang.Class<? extends java.util.Map>>[]");
    }

    public void testCapturedTypeExtends170574() throws Exception {
        performCapturedTypeTest("package test; interface Foo<T> {Foo<? extends T> foo();}" +
                "public class Test {public void t() {Foo<? extends Number> bar = null; bar.foo(|);}}",
                                "test.Foo<? extends java.lang.Number>");
    }

    public void testCapturedTypeSuper170574() throws Exception {
        performCapturedTypeTest("package test; interface Foo<T> {Foo<? super T> foo();}" +
                "public class Test {public void t() {Foo<? super Number> bar = null; bar.foo(|);}}",
                                "test.Foo<? super java.lang.Number>");
    }
    
    public void testCapturedType206536() throws Exception {
        performCapturedTypeTest("package test; interface Foo<T> {Foo<? extends ThreadLocal<? extends T>> foo();}" +
                "public class Test {public void t() {Foo<? extends Number> bar = null; bar.foo(|);}}",
                                "test.Foo<? extends java.lang.ThreadLocal<? extends java.lang.Number>>");
    }

    public void testFieldGroup1() throws Exception {
        performResolveFieldGroupTest("package test; public class Test { |private int a, b, c;| }", 3);
    }

    public void testFieldGroup2() throws Exception {
        performResolveFieldGroupTest("package test; public class Test { |int a, b, c;| }", 3);
    }

    public void testFieldGroup3() throws Exception {
        performResolveFieldGroupTest("package test; public class Test { private int a; |private int b;| private int c; }", 1);
    }
    
    public void testExitsAllBranches1() throws Exception {
        performExitsTest("package test;\n"
                + "\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n"
                + "\n"
                + "public class Test {\n"
                + "    public enum Day {\n"
                + "        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,\n"
                + "        THURSDAY, FRIDAY, SATURDAY\n"
                + "    public class DayHolder {\n"
                + "        Day day;\n"
                + "        Day getDay() {\n"
                + "            return day;\n"
                + "        }\n"
                + "    }\n"
                + "public class Test {\n"
                + "    public enum Day {\n"
                + "        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,\n"
                + "        THURSDAY, FRIDAY, SATURDAY\n"
                + "    }\n"
                + "    public class DayHolder {\n"
                + "        Day day;\n"
                + "        Day getDay() {\n"
                + "            return day;\n"
                + "        }\n"
                + "    }\n"
                + "    public void test(Map<Integer, Day> evaluateExpressions) throws Exception {\n"
                + "        List<DayHolder> params = new ArrayList<>();\n"
                + "        for (int i = 0; i < params.size(); i++) {\n"
                + "            DayHolder p = params.get(i);\n"
                + "            if (evaluateExpressions.get(i) != null) {\n"
                + "                if (evaluateExpressions.get(i) == Day.SUNDAY) {\n"
                + "                    // Do something 1\n"
                + "                } else if (evaluateExpressions.get(i) == Day.MONDAY) {\n"
                + "                    swi|tch (p.getDay()) {\n"
                + "                        case SUNDAY:\n"
                + "                            break;\n"
                + "                        default:\n"
                + "                            throw new Exception(\"xxx\");\n"
                + "                    }\n"
                + "                } else if (evaluateExpressions.get(i) == Day.TUESDAY) {\n"
                + "                    // Do something 2\n"
                + "                }\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}", 
                false);
    }

    /**
     * While may never enter its statement; return false.
     */
    public void testExitsAllBranchesWhile() throws Exception {
        performExitsTest("package test;\n"
                + "\n"
                + "public class Test {\n"
                + "    public void test(boolean param) {\n"
                + "        whil|e (param) {\n"
                + "            throw new IllegalArgumentException();\n"
                + "        }\n"
                + "    }\n"
                + "}",
                false);
    }
    
    /**
     * While may never enter its statement; return false.
     */
    public void testExitsAllBranchesDo() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(boolean param) {\n"
                + "        ex: {\n"
                + "            d|o {\n"
                + "                break;\n"
                + "            } while (true);\n"
                + "        }\n"
                + "    }\n"
                + "}",
                false);
    }
    
    /**
     * While may never enter its statement; return false.
     */
    public void testExitsAllBranchesFor() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(boolean param) {\n"
                + "        ex: {|\n"
                + "            for (int i = 0; i < 5 ; i++) {\n"
                + "                break ex;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}",
                false);
    }
    
    /**
     * While may never enter its statement; return false.
     */
    public void testExitsAllBranchesDoBreakOutside() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(boolean param) {\n"
                + "        ex: {\n"
                + "            d|o {\n"
                + "                break ex;\n"
                + "            } while (true);\n"
                + "        }\n"
                + "    }\n"
                + "}",
                true);
    }
    
    /**
     * All breaks break just the switch and continue execution
     */
    public void testExitsAllBranchesSwitch1() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(int param) {\n"
                + "        ex: {\n"
                + "            sw|itch (param) {\n"
                + "                case 0:\n"
                + "                    break;\n"
                + "                case 1:\n"
                + "                    break;\n"
                + "                default:\n"
                + "                    break;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}",
                false);
    }
    
    /**
     * all breaks break to the enclosing block, escaping from the switch
     */
    public void testExitsAllBranchesSwitchBreakOut() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(int param) {\n"
                + "        ex: {\n"
                + "            sw|itch (param) {\n"
                + "                case 0:\n"
                + "                    break ex;\n"
                + "                case 1:\n"
                + "                    break ex;\n"
                + "                default:\n"
                + "                    break ex;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}",
                true);
    }

    /**
     * two cases share the same break statement
     */
    public void testExitsAllBranchesSwitchJoinedCases() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(int param) {\n"
                + "        ex: {\n"
                + "            sw|itch (param) {\n"
                + "                case 0:\n"
                + "                case 1:\n"
                + "                    break ex;\n"
                + "                default:\n"
                + "                    break ex;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}",
                true);
    }

    /**
     * two cases share the same break statement
     */
    public void testExitsAllBranchesEnumSwitchIncomplete() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    enum Num { ONE, TWO, THREE };\n"
                + "    public int test(Num param) {\n"
                + "        swi|tch (param) {\n"
                + "            case ONE:\n"
                + "                return 1;\n"
                + "            case TWO:\n"
                + "                return 2;\n"
                + "        }\n"
                + "        return 0;"
                + "    }\n"
                + "}",
                false);
    }

    /**
     * two cases share the same break statement
     */
    public void testExitsAllBranchesEnumSwitchComplete() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    enum Num { ONE, TWO, THREE };\n"
                + "    public int test(Num param) {\n"
                + "        swi|tch (param) {\n"
                + "            case ONE:\n"
                + "                return 1;\n"
                + "            case TWO:\n"
                + "                return 2;\n"
                + "            case THREE:\n"
                + "                return 3;\n"
                + "        }\n"
                + "        return 0;"
                + "    }\n"
                + "}",
                true);
    }

    /**
     * two cases share the same break statement
     */
    public void testExitsAllBranchesEnumSwitchWithDefault() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    enum Num { ONE, TWO, THREE };\n"
                + "    public int test(Num param) {\n"
                + "        swi|tch (param) {\n"
                + "            case ONE:\n"
                + "                return 1;\n"
                + "            case TWO:\n"
                + "                return 2;\n"
                + "            default:\n"
                + "                return 3;\n"
                + "        }\n"
                + "        return 0;"
                + "    }\n"
                + "}",
                true);
    }

    /**
     * Default does not contain any break, continues normally
     */
    public void testExitsAllBranchesSwitchLastWithoutBreak() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(int param) {\n"
                + "        ex: {\n"
                + "            sw|itch (param) {\n"
                + "                case 0:\n"
                + "                    break ex;\n"
                + "                case 1:\n"
                + "                    break ex;\n"
                + "                default:\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}",
                false);
    }

    /**
     * No default present, values not enumerated causes the switch to continue
     */
    public void testExitsAllBranchesSwitchDefaultMissing() throws Exception {
        performExitsTest("package test;\n"
                + "public class Test {\n"
                + "    public void test(int param) {\n"
                + "        ex: {\n"
                + "            swi|tch (param) {\n"
                + "                case 0:\n"
                + "                    break ex;\n"
                + "                case 1:\n"
                + "                    break ex;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}",
                false);
    }

    private void performExitsTest(String code, boolean expected) throws Exception {
        int caretPos = code.indexOf('|');
        code = code.replace("|", "");
        prepareTest(code);
        TreePath tp = info.getTreeUtilities().pathFor(caretPos);
        while (tp != null && !StatementTree.class.isAssignableFrom(tp.getLeaf().getKind().asInterface())) {
            tp = tp.getParentPath();
        }
        assertNotNull(tp);
        boolean result = Utilities.exitsFromAllBranchers(info, tp);
        assertEquals(expected, result);
    }

    protected void prepareTest(String code) throws Exception {
        prepareTest(code, "Test");
    }
    
    protected void prepareTest(String code, String className) throws Exception {
        clearWorkDir();
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, "test/" + className + ".java");
        
        TestUtilities.copyStringToFile(FileUtil.toFile(data), code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private CompilationInfo info;
    
    private void performNameGuessTest(String code, String desiredName) throws Exception {
        int scopePos = code.indexOf('|');

        assertTrue(scopePos > -1);
        
        performNameGuessTest(code.replace("|", ""), scopePos, desiredName);
    }
    
    private void performNameGuessTest(String code, int position, String desiredName) throws Exception {
        prepareTest(code);
        
        TreePath tp = info.getTreeUtilities().pathFor(position);
        
        String name = Utilities.guessName(info, tp);
        
        assertEquals(desiredName, name);
    }

    private void performShortNameTest(String code, String expression, String expectedName) throws Exception {
        int scopePos = code.indexOf('|');

        prepareTest(code.replace("|", ""));

        TreePath tp = info.getTreeUtilities().pathFor(scopePos);
        Scope s = info.getTrees().getScope(tp);
        ExpressionTree parsed = info.getTreeUtilities().parseExpression(expression, new SourcePositions[1]);

        String name = Utilities.shortDisplayName(info, parsed);

        assertEquals(expectedName, name);
    }

    private void performCapturedTypeTest(String code, String golden) throws Exception {
        int[] position = new int[1];
        code = org.netbeans.modules.java.hints.spiimpl.TestUtilities.detectOffsets(code, position);

        performCapturedTypeTest(code, position[0], golden);
    }
    
    private void performCapturedTypeTest(String code, int position, String golden) throws Exception {
        prepareTest(code);

        TreePath tp = info.getTreeUtilities().pathFor(position);
        TypeMirror type = info.getTrees().getTypeMirror(tp);
        TypeMirror resolved = Utilities.resolveCapturedType(info, type);

        assertEquals(golden, org.netbeans.modules.editor.java.Utilities.getTypeName(info, resolved, true).toString());
    }

    private void performResolveFieldGroupTest(String code, int numOfElements) throws Exception {
        String[] split = code.split("\\|");
        int start = split[0].length();
        int end   = split[0].length() + split[1].length();

        code = split[0] + split[1] + split[2];

        prepareTest(code);

        TreePath tp = info.getTreeUtilities().pathFor(start + 1);

        while (tp.getLeaf().getKind() != Kind.VARIABLE) {
            tp = tp.getParentPath();
        }

        Collection<? extends TreePath> tps = Utilities.resolveFieldGroup(info, tp);

        assertFieldGroup(info, tps, start, end, numOfElements);

        for (TreePath inGroup : tps) {
            assertFieldGroup(info, Utilities.resolveFieldGroup(info, inGroup), start, end, numOfElements);
        }
    }

    private static void assertFieldGroup(CompilationInfo info, Collection<? extends TreePath> group, int goldenStart, int goldenEnd, int numOfElements) {
        assertEquals(numOfElements, group.size());
        
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), group.iterator().next().getLeaf());
        int end = -1;

        for (TreePath tp : group) {
            end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf());
        }

        assertEquals(goldenStart, start);
        assertEquals(goldenEnd, end);
    }
    
}
