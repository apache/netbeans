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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;


/**
 *
 * @author Jan Lahoda
 */
public class CreateMethodTest extends ErrorHintsTestBase {
    
    /** Creates a new instance of CreateElementTest */
    public CreateMethodTest(String name) {
        super(name, CreateElement.class);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    public void testMoreMethods() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {test(1);}}", 103 - 48, "CreateMethodFix:test(int i)void:test.Test");
    }
    
    public void testConstructor() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new Test(1);}}", 114 - 48, "CreateConstructorFix:(int i):test.Test");
    }
    
    public void testNoCreateConstructorForNonExistingClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new NonExisting(1);}}", 114 - 48);
    }
    
    public void testFieldLike() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {Collections.emptyList();}}", 107 - 48);
    }

    public void testMemberSelect1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {emptyList().doSomething();}}", 107 - 48, "CreateMethodFix:emptyList()java.lang.Object:test.Test");
    }
    
    public void testMemberSelect2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public Test test() {test().doSomething();}}", 112 - 48, "CreateMethodFix:doSomething()void:test.Test");
    }
    
    public void testAssignment() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {int i = fff();}}", 110 - 48, "CreateMethodFix:fff()int:test.Test");
    }
    
    public void testNewInAnnonymousInnerclass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public Test(){} public void test() {new Runnable() {public void run() {new Test(1);}}}}", 158 - 48, "CreateConstructorFix:(int i):test.Test");
    }
    
    public void testCreateMethodInInterface() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {Int i = null; i.test(1);} public static interface Int{}}", 96 - 24,
                       "CreateMethodFix:test(int i)void:test.Test.Int",
                       "package test; public class Test {public void test() {Int i = null; i.test(1);} public static interface Int{ public void test(int i); }}");
    }
    
    public void testCreateMethod106255() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {test2(null);}}", 82 - 25,
                       "CreateMethodFix:test2(java.lang.Object object)void:test.Test",
                       "package test; public class Test {public void test() {test2(null);} private void test2(Object object) { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } }");
    }
    
    public void testCreateMethod77038() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {b(test2() ? true : false);} void t(boolean b){}}", 82 - 25,
                       "CreateMethodFix:test2()boolean:test.Test",
                       "package test; public class Test {public void test() {b(test2() ? true : false);} void t(boolean b){} private boolean test2() { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } }");
    }
    
    public void testCreateMethod82923() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void test() {int i = 0; switch (i) {case 1: fff(); break;}}}", 134 - 48, "CreateMethodFix:fff()void:test.Test");
    }
    
    public void testCreateMethod82931() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.Collection; public class Test {public static void test() {fff(getStrings());} private static Collection<String> getStrings() {return null;}}",
                       116 - 25,
                       "CreateMethodFix:fff(java.util.Collection<java.lang.String> strings)void:test.Test",
                       "package test; import java.util.Collection; public class Test {public static void test() {fff(getStrings());} private static Collection<String> getStrings() {return null;} private static void fff(Collection<String> strings) { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } }");
    }
    
    public void testCreateMethod74129() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {TopLevel.f|ff();}} class TopLevel {}",
                       "CreateMethodFix:fff()void:test.TopLevel",
                       "package test; public class Test {public void test() {TopLevel.fff();}} class TopLevel { static void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } }");
    }
    
    public void testCreateMethod76498() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static class T extends Test {public void test() {super.fff();}}}",
                       122 - 25,
                       "CreateMethodFix:fff()void:test.Test",
                       "package test; public class Test { private void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } public static class T extends Test {public void test() {super.fff();}}}");
    }
    
    public void testCreateMethod75069() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test<T> {public void test() {this.fff();}}",
                       88 - 25,
                       "CreateMethodFix:fff()void:test.Test",
                       "package test; public class Test<T> {public void test() {this.fff();} private void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } }");
    }
    
    public void testCreateMethod119037() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {static {f|ff();}}",
                       "CreateMethodFix:fff()void:test.Test",
                       "package test; public class Test {static {fff();} private static void fff() { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } }");
    }

    public void testCreateMethodWithAnonymousParameter104820() throws Exception {
        performFixTest("test/Test.java",
                       "package test;public class Test {public static void method() {final Test ac = new Test();new Runnable() {public void run() {ac.a|ction(this);}};}}",
                       "CreateMethodFix:action(java.lang.Runnable aThis)void:test.Test",
                       "package test;public class Test {public static void method() {final Test ac = new Test();new Runnable() {public void run() {ac.action(this);}};} private void action(Runnable aThis) { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } }");
    }

    public void testCreateMethodWithEnumParam() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { enum Paddle{UP, DOWN} public void foo() {f|ff(Paddle.UP);}}",
                       "CreateMethodFix:fff(test.Test.Paddle paddle)void:test.Test",
                       "package test; public class Test { private void fff(Paddle paddle) { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } enum Paddle{UP, DOWN} public void foo() {fff(Paddle.UP);}}");
    }

    public void testCreateMethodWithParamOfEnumType199793() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { enum Paddle{UP, DOWN} public void foo(Paddle test) {f|ff(test);}}",
                       "CreateMethodFix:fff(test.Test.Paddle test)void:test.Test",
                       "package test; public class Test { private void fff(Paddle test) { throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. } enum Paddle{UP, DOWN} public void foo(Paddle test) {fff(test);}}");
    }
    
    public void test220582() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void foo() {\n" +
                       "        String name = null, description = null;\n" +
                       "        if(!is|New(name) && isNew(description)){ // 1\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:isNew(java.lang.String name)boolean:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void foo() {\n" +
                        "        String name = null, description = null;\n" +
                        "        if(!isNew(name) && isNew(description)){ // 1\n" +
                        "        }\n" +
                        "    }\n" +
                        "    private boolean isNew(String name) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }
    
    public void test223011a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.reflect.*;\n" +
                       "public class Test {\n" +
                       "    public void foo(Class<?> c) throws Exception {\n" +
                       "        Field f = m|ethod(c);\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:method(java.lang.Class<?> c)java.lang.reflect.Field:test.Test",
                       ("package test;\n" +
                        "import java.lang.reflect.*;\n" +
                        "public class Test {\n" +
                        "    public void foo(Class<?> c) throws Exception {\n" +
                        "        Field f = method(c);\n" +
                        "    }\n" +
                        "    private Field method(Class<?> c) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }
    
    public void test223011b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.reflect.*;\n" +
                       "public class Test {\n" +
                       "    public <T extends Number&CharSequence, E extends Integer> void foo(Class<E> c1, Class<T> c2) throws Exception {\n" +
                       "        Field f = m|ethod(c1, c2);\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:method(java.lang.Class<E> c1,java.lang.Class<T> c2)java.lang.reflect.Field:test.Test",
                       ("package test;\n" +
                        "import java.lang.reflect.*;\n" +
                        "public class Test {\n" +
                        "    public <T extends Number&CharSequence, E extends Integer> void foo(Class<E> c1, Class<T> c2) throws Exception {\n" +
                        "        Field f = method(c1, c2);\n" +
                        "    }\n" +
                        "    private <T extends Number & CharSequence, E extends Integer> Field method(Class<E> c1, Class<T> c2) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }
    
    public void test223011c() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.reflect.*;\n" +
                       "public class Test {\n" +
                       "    public <T extends Number&CharSequence, E> void foo(Class<T> c) throws Exception {\n" +
                       "        Class<E> cr = m|ethod(c);\n" +
                       "    }\n" +
                       "}\n",
                       "CreateMethodFix:method(java.lang.Class<T> c)java.lang.Class<E>:test.Test",
                       ("package test;\n" +
                        "import java.lang.reflect.*;\n" +
                        "public class Test {\n" +
                        "    public <T extends Number&CharSequence, E> void foo(Class<T> c) throws Exception {\n" +
                        "        Class<E> cr = method(c);\n" +
                        "    }\n" +
                        "    private <T extends Number & CharSequence, E> Class<E> method(Class<T> c) {\n" +
                        "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }
    
    public void test203476() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import static test.Aux.getName;\n" +
                       "public class Test {\n" +
                       "    public void foo() {\n" +
                       "        getName(undefined());\n" +
                       "    }\n" +
                       "}\n" +
                       "class Aux {\n" +
                       "    public static void getName(String param) { }\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:undefined()java.lang.String:test.Test",
                       ("package test;\n" +
                        "import static test.Aux.getName;\n" +
                        "public class Test {\n" +
                        "    public void foo() {\n" +
                        "        getName(undefined());\n" +
                        "    }\n" +
                        "    private String undefined() {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. \n" +
                        "    }\n" +
                        "}\n" +
                        "class Aux {\n" +
                        "    public static void getName(String param) { }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    public void test233502() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public static void foo(Number str) {\n" +
                       "        m(str);\n" +
                       "    }\n" +
                       "    public static void m(String str) {}\n" +
                       "}\n",
                       -1,
                       "CreateMethodFix:m(java.lang.Number str)void:test.Test",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void foo(Number str) {\n" +
                        "        m(str);\n" +
                        "    }\n" +
                        "    public static void m(String str) {}\n" +
                        "    private static void m(Number str) {\n" +
                        "         throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates. \n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \n\t\r]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, String diagnosticCode, int pos, TreePath path) throws Exception {
        List<Fix> fixes = new CreateElement().analyze(info, diagnosticCode, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateMethodFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateMethodFix) f).toDebugString(info);
    }
    
}
