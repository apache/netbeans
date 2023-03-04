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

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;


/**
 *
 * @author Jan Lahoda
 */
public class CreateFieldTest extends ErrorHintsTestBase {
    
    /** Creates a new instance of CreateElementTest */
    public CreateFieldTest(String name) {
        super(name);
    }
    
    public void test96590() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import java.lang.annotation.Retention; @Retention(value = RetentionPolicy) public @interface Test {}", 125 - 48);
    }
    
    //Creating a field from an annonymous class expression
    public void test118701() throws Exception {
        performFixTest("test/Test.java",
                       "package test;import java.io.Writer;public class Test {public static void main(String[] args) {ww = new Writer() {public void write(char[] cbuf, int off, int len) {}public void close() {}public void flush() {}};}}",
                       120 - 25,
                       "CreateFieldFix:ww:test.Test:java.io.Writer:[private, static]",
                       "package test;import java.io.Writer;public class Test { private static Writer ww; public static void main(String[] args) {ww = new Writer() {public void write(char[] cbuf, int off, int len) {}public void close() {}public void flush() {}};}}");
    }

    public void testFinalFromCtor() throws Exception {
        performFixTest("test/Test.java",
                "package test;public class Test {public Test() { i|i = 1; }}",
                "CreateFieldFix:ii:test.Test:int:[private, final]",
                "package test;public class Test { private final int ii; public Test() { ii = 1; }}");
    }

    //Creating a field in Enum
    public void test155581() throws Exception {
        performFixTest("test/Test.java",
                "package test;public enum Test {A(0), B(1); private Test(int value) { i|i = value; }}",
                "CreateFieldFix:ii:test.Test:int:[private]",
                "package test;public enum Test {A(0), B(1); private int ii; private Test(int value) { ii = value; }}");
    }

    //Creating a field in Enum
    public void test155581_2() throws Exception {
        performFixTest("test/Test.java",
                "package test;public enum Test {A(0), B(1); private Test(int value) { this.i|i = value; }}",
                "CreateFieldFix:ii:test.Test:int:[private]",
                "package test;public enum Test {A(0), B(1); private int ii; private Test(int value) { this.ii = value; }}");
    }

    public void test177201() throws Exception {
        doRunIndexing = true;
        performFixTest("test/Test.java",
                "package test;import javax.swing.JFrame;import javax.swing.JOptionPane;public class Test {public boolean isOk() {JFrame frame = null;JOptionPane.showMessageDialog(frame,X.whan|tAutoGenerateThisVariableInXClass,\"Error\",JOptionPane.ERROR_MESSAGE );return true;}} class X {}",
                "CreateFieldFix:whantAutoGenerateThisVariableInXClass:test.X:java.lang.Object:[static]",
                "package test;import javax.swing.JFrame;import javax.swing.JOptionPane;public class Test {public boolean isOk() {JFrame frame = null;JOptionPane.showMessageDialog(frame,X.whantAutoGenerateThisVariableInXClass,\"Error\",JOptionPane.ERROR_MESSAGE );return true;}} class X { static Object whantAutoGenerateThisVariableInXClass; }");
    }

    public void test192219() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test<T> { private T getA() { return a|a;} }",
                "CreateFieldFix:aa:test.Test:T:[private]",
                "package test; public class Test<T> { private T aa; private T getA() { return aa;} }");
    }
    
    public void test224626Temp() throws Exception {
        performAnalysisTest("test/package-info.java",
                            "@B(\"\" + T.A)\npackage test;\nenum T {}\n@interface B { String value();}\n",
                            -1);
    }
    
    public void test213699() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test { public Test(boolean b) { if (b) { fie|ld = 0; } } }",
                "CreateFieldFix:field:test.Test:int:[private]",
                "package test; public class Test { private int field; public Test(boolean b) { if (b) { field = 0; } } }");
    }
    
    public void test219626a() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test { public Test() {} public Test(boolean b) { fie|ld = 0; } }",
                "CreateFieldFix:field:test.Test:int:[private]",
                "package test; public class Test { private int field; public Test() {} public Test(boolean b) { field = 0; } }");
    }
    
    public void test219626b() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test { public Test() { this(true); } public Test(boolean b) { fie|ld = 0; } }",
                "CreateFieldFix:field:test.Test:int:[private, final]",
                "package test; public class Test { private final int field; public Test() { this(true); } public Test(boolean b) { field = 0; } }");
    }
    
    public void test233502() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test; public class Test { public Test() { long l = 0; int j = l; }",
                -1);
    }

    public void test234968() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test; public class Test { public Test() { l = 0; } public Test(int i); }",
                -1,
                "CreateFieldFix:l:test.Test:int:[private]");
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, String diagnosticCode, int pos, TreePath path) throws Exception {
        List<Fix> fixes = CreateElement.analyze(info, diagnosticCode, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateFieldFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateFieldFix) f).toDebugString(info);
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
        return new CreateElement().getCodes();
    }
    
}
