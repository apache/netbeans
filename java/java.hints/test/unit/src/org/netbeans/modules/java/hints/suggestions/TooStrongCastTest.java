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
package org.netbeans.modules.java.hints.suggestions;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class TooStrongCastTest extends NbTestCase {
    public static final String FIX_SPECIFIC_CATCH = "Use specific type in catch";
    private String fileName;
    
    public TooStrongCastTest(String name) {
        super(name);
    }

    private HintTest createHintTest(String n) throws Exception {
        this.fileName = n;
        return HintTest.create().input("org/netbeans/test/java/hints/TooStrongCastTest/" + n + ".java", code(n));
    }
    
    private String c() throws Exception { return code(fileName); }
    
    private String code(String name) throws IOException {
        FileObject f = FileUtil.toFileObject(getDataDir());
        return f.getFileObject("org/netbeans/test/java/hints/TooStrongCastTest/" + name + ".java").asText();
    }
    
    private String g() throws Exception { return golden(fileName); }
    
    private String golden(String name) throws IOException {
        FileObject f = FileUtil.toFileObject(getDataDir());
        return f.getFileObject("goldenfiles/org/netbeans/test/java/hints/TooStrongCastTest/" + name + ".java").asText();
    }
    
    private String f() { return f(fileName); }
    
    private String f(String name) {
        return "org/netbeans/test/java/hints/TooStrongCastTest/" + name + ".java";
    }
    
    private static final String[] ARRAY_WARNINGS = {
        "13:12-13:15:verifier:Unnecessary cast to int", 
        "15:11-15:21:verifier:Type cast to byte is too strong. int should be used instead", 
        "20:20-20:25:verifier:Unnecessary cast to float"
    };
    
    private static final String[] ASSIGNMENT_WARNINGS = {
        "9:13-9:17:verifier:Unnecessary cast to List", 
        "16:12-16:19:verifier:Type cast to List is too strong. Collection should be used instead", 
        "23:13-23:17:verifier:Unnecessary cast to long"
    };
    
    private static final String[] EXPRESSION_WARNINGS = {
        "17:13-17:16:verifier:Unnecessary cast to int", 
        "25:12-25:19:verifier:Type cast to byte is too strong. int should be used instead", 
        "41:22-41:28:verifier:Unnecessary cast to String", 
        "47:17-47:20:verifier:Unnecessary cast to int"
    };
    
    private static final String[] METHOD_WARNINGS = {
        "21:10-21:19:verifier:Unnecessary cast to JViewport", 
        "28:41-28:63:verifier:Type cast to MouseWheelEvent is too strong. MouseEvent should be used instead", 
        "34:22-34:29:verifier:Unnecessary cast to JButton", 
        "40:22-40:37:verifier:Type cast to SuperDerived is too strong. Derived should be used instead"
    };

    private static final String[] RETURN_WARNINGS = {
        "17:15-17:22:verifier:Type cast to List is too strong. Collection should be used instead", 
        "25:26-25:33:verifier:Type cast to List is too strong. Collection should be used instead", 
        "40:15-40:30:verifier:Type cast to List<String> is too strong. Collection<String> should be used instead", 
        "45:15-45:25:verifier:Type cast to List<T> is too strong. Collection<T> should be used instead"
    };
    
    private static final String[] THROW_WARNINGS = {
        "14:18-14:43:verifier:Type cast to FileNotFoundException is too strong. IOException should be used instead", 
        "16:18-16:46:verifier:Type cast to IllegalArgumentException is too strong. RuntimeException should be used instead"
    };
    
    public void testArrayAccess() throws Exception {
        createHintTest("ArrayAccess").
            run(TooStrongCast.class).
            assertWarnings(ARRAY_WARNINGS);
    }
    
    public void testAssignment() throws Exception {
        createHintTest("CastAssignment").
            run(TooStrongCast.class).
            assertWarnings(ASSIGNMENT_WARNINGS);
    }

    public void testExpression() throws Exception {
        createHintTest("CastExpressions").
            run(TooStrongCast.class).
            assertWarnings(EXPRESSION_WARNINGS);
    }
    
    public void testMethods() throws Exception {
        createHintTest("CastMethods").
            run(TooStrongCast.class).
            assertWarnings(METHOD_WARNINGS);
    }
    
    public void testReturnType() throws Exception {
        createHintTest("ReturnType").
            run(TooStrongCast.class).
            assertWarnings(RETURN_WARNINGS);
    }
    
    public void testThrowExpression() throws Exception {
        createHintTest("ThrowExpression").
            run(TooStrongCast.class).
            assertWarnings(THROW_WARNINGS);
    }
    
    /**
     * Checks that a type (null) explicitly casted to varargs array
     * type does not produce a warning
     * @throws Exception 
     */
    public void testVarargsOK() throws Exception {
        HintTest.create().
            input("package test;\n" +
                "public class Test {\n" +
                "    private void varargMethod(String s, CharSequence... args) {}\n" +
                "    \n" +
                "    public void varargsCall() {\n" +
                "        varargMethod(\"ble\", (CharSequence[])null); \n" +
                "    }\n" +
                "}\n" +
                "").
            run(TooStrongCast.class).
            assertWarnings();
    }
    
    public void testVarargRedudantCastToItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() {\n" +
            "        varargMethod(\"ble\", \"fuj\", (CharSequence)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:36-4:48:verifier:Unnecessary cast to CharSequence");
    }
    
    public void testVarargStrongCastToVarArray() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        Object arr = null;\n" +
            "        varargMethod(\"ble\", (String[])arr);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:28-5:41:verifier:Type cast to String[] is too strong. CharSequence[] should be used instead");
    }
    
    public void testVarargRedundantCastArray() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        String[] arr = null;\n" +
            "        varargMethod(\"ble\", (String[])arr);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:29-5:37:verifier:Unnecessary cast to String[]");
    }

    /**
     * It is unnecessary to cast a String- variable passed on vararg
     * position.
     * 
     * @throws Exception 
     */
    public void testVarargRedundantCastFirstItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        String s = \"\";\n" +
            "        varargMethod(\"ble\", (String)s);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:29-5:35:verifier:Unnecessary cast to String");
    }

    /**
     * It is OK to cast null item to item type to avoid 'possibly ambiguous null'
     * type warning from varargs hint
     * 
     * @throws Exception 
     */
    public void testVarargOKCastNullItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        varargMethod(\"ble\", (CharSequence)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }

    public void testVarargStrongCastNullItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        varargMethod(\"ble\", (String)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:28-4:40:verifier:Type cast to String is too strong. CharSequence should be used instead");
    }

    public void testVarargStrongCastFirstItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() { \n" +
            "        Object s = \"\";\n" +
            "        varargMethod(\"ble\", (String)s);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("5:28-5:37:verifier:Type cast to String is too strong. CharSequence should be used instead");
    }

    public void testVarargStromgCastToItem() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "public class Test {\n" +
            "    private void varargMethod(String s, CharSequence... args) {}\n" +
            "    public void varargsCall() {\n" +
            "        varargMethod(\"ble\", \"fuj\", (String)null);  \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:36-4:42:verifier:Unnecessary cast to String");
    }
    
    /**
     * Do not hint if the typecast is needed to select the appropriate method
     * @throws Exception 
     */
    public void testOKAmbiguousMethod() throws Exception {
        HintTest.create().
            input("package test;\n" +
"import java.io.Serializable;\n" +
"abstract class VarArgsCast {\n" +
"    public void varargsCall() { \n" +
"        String value = null;\n" +
"        findByPropertyValue(\"\", (Serializable)value, true);  \n" +
"        \n" +
"    }\n" +
"    abstract Object findByPropertyValue(String name, Object value);\n" +
"    abstract Object findByPropertyValue(String name, String value, boolean ignoreCase);\n" +
"    abstract Object findByPropertyValue(String name, Serializable value, boolean ignoreCase);\n" +
"}\n" +
"").
            run(TooStrongCast.class).
            assertWarnings("");
    }
    
    public void testStrongOverloadedMethd() throws Exception {
        HintTest.create().
            input("package test;\n" +
"import java.io.Serializable;\n" +
"abstract class VarArgsCast {\n" +
"    public void varargsCall() { \n" +
"        Integer value = null;\n" +
"        findByPropertyValue(\"\", (Serializable)value, true);  \n" +
"        \n" +
"    }\n" +
"    abstract Object findByPropertyValue(String name, Object value);\n" +
"    abstract Object findByPropertyValue(String name, String value, boolean ignoreCase);\n" +
"    abstract Object findByPropertyValue(String name, Serializable value, boolean ignoreCase);\n" +
"}\n" +
"").
            run(TooStrongCast.class).
            assertWarnings("5:33-5:45:verifier:Unnecessary cast to Serializable");
    }
    
    public void testNoHintInferredType() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "\n" +
            "import java.util.Collections;\n" +
            "\n" +
            "class VarArgsCast {\n" +
            "    void bu() {\n" +
            "        Collections.nCopies(10, (String)null).get(0).length();\n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }
    
    /**
     * Checks that widening in arithmetic operation does not generate a hint. E.g. (float)a/b where a and b are int
     * 
     */
    public void testArithmeticWidening() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "        int a = 2; int b = 5;\n" +
            "        double y = ((double)a) / b;   \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }
    
    /**
     * If one argument is casted to float and the other to double, it is not necessary to cast the operand to float,
     * since using double promotes the other operand (to double, wider than float) automatically
     */
    public void testArithmeticRedundantWideningCast() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "        int a = 2; int b = 5;\n" +
            "        double y = (double)a / (float)b;   \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:32-4:37:verifier:Unnecessary cast to float");
    }
    
    /**
     * If one argument is casted to float and the other is already typed as double, it is not necessary to cast the operand to float,
     * since using double promotes the other operand (to double, wider than float) automatically
     */
    public void testArithmeticRedundantWideningType() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "        int a = 2; double b = 5;\n" +
            "        double y = ((double)a) / b;   \n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:21-4:27:verifier:Unnecessary cast to double");
    }

    public void testCharToCodeConversionInt() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "        char c = 'a';\n" +
            "        String s = \"Character \" + c + \" has ASCII code \" + (int)c;\n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }
    
    /**
     * Prints the char's code but in floating point notation
     */
    public void testCharToCodeConversionFloat() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "        char c = 'a';\n" +
            "        String s = \"Character \" + c + \" has ASCII code \" + (float)c;\n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }

    /**
     * Should warn, char is not necessary to convert to double,
     * float will also generate the same String representation
     */
    public void testCharToCodeConversionDouble() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "        char c = 'a';\n" +
            "        String s = \"Character \" + c + \" has ASCII code \" + (double)c;\n" +
            "    }\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:59-4:68:verifier:Type cast to double is too strong. float should be used instead");
    }
    
    public void testMemberSelectOnPrimitive() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "       final int i = 1234;\n" +
            "       System.out.println(((Integer) i).byteValue());\n" +
            "    }\n" +
            "    void e(Number foo) {}\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }
    
    /**
     * Checks that unnecessry cast is still reported
     * @throws Exception 
     */
    public void testUnnecessaryCastingPrimitiveToWrapper() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "    void bu() {\n" +
            "        int a = 20;\n" +
            "        e((Integer)a);\n" +
            "    }\n" +
            "    void e(Number foo) {}\n" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("4:11-4:18:verifier:Unnecessary cast to Integer");
    }
    
    public void testCastLoosingPrecision() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "   public double foo(double x) {\n" +
            "        return (int) x;\n" +
            "   }" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings();
    }
    
    public void testCastWideningPrimitive() throws Exception {
        HintTest.create().
            input("package test;\n" +
            "class Test {\n" +
            "   public float foo(int x) {\n" +
            "        return (long) x;\n" +
            "   }" +
            "}\n" +
            "").
            run(TooStrongCast.class).
            assertWarnings("3:16-3:20:verifier:Unnecessary cast to long");
    }
}
