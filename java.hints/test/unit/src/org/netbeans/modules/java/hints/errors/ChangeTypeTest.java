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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;


/**
 *
 * @author Sandip Chitale
 */
public class ChangeTypeTest extends ErrorHintsTestBase {
    
    /** Creates a new instance of ChangeTypeTest */
    public ChangeTypeTest(String name) {
        super(name);
    }
    
    public void testIntToString() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {int i = \"s\";}", 41, "Change type of i to String");
    }

    public void testIntToStringFix() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { int i = \"s\";}",
                       41,
                       "Change type of i to String",
                       "package test; public class Test { String i = \"s\";}");
    }

    public void testStringToInt() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {String s = 5;}", 44, "Change type of s to int");
    }

    public void testStringToIntFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test { String s = 5;}",
                44,
                "Change type of s to int",
                "package test; public class Test { int s = 5;}");
    }

    public void testStringToObject() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {String s = new Object();}", 44, "Change type of s to Object");
    }

    public void testStringToObjectFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {String s = new Object();}",
                44,
                "Change type of s to Object",
                "package test; public class Test {Object s = new Object();}");
    }

    public void testLocalVariableIntToString() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {private void test() {int i = \"s\";}}", 62, "Change type of i to String");
    }

    public void testLocalVariableIntToStringFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {private void test() {int i = \"s\";}}",
                62,
                "Change type of i to String",
                "package test; public class Test {private void test() {String i = \"s\";}}"
                );
    }

    public void testLocalVariableStringToInt() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {private void test() {String s = 5;}}", 65, "Change type of s to int");
    }

    public void testLocalVariableStringToIntFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {private void test() {String s = 5;}}",
                65,
                "Change type of s to int",
                "package test; public class Test {private void test() {int s = 5;}}");
    }

    public void testLocalVariableStringToObject() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {private void test() {String s = new Object();}}", 65, "Change type of s to Object");
    }

    public void testLocalVariableStringToObjectFix() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {private void test() {String s = new Object();}}",
                65,
                "Change type of s to Object",
                "package test; public class Test {private void test() {Object s = new Object();}}");
    }

    public void testCapturedWildcard1() throws Exception {
        performFixTest("test/Test.java",
                "package test; import java.util.List; public class Test {private void test() {String o = |test1();} private List<? extends CharSequence> test1() {return null;}}",
                "Change type of o to List&lt;? extends CharSequence>",
                "package test; import java.util.List; public class Test {private void test() {List<? extends CharSequence> o = test1();} private List<? extends CharSequence> test1() {return null;}}");
    }

    public void testCapturedWildcard2() throws Exception {
        performFixTest("test/Test.java",
                "package test; import java.util.List; public class Test {private void test() {List<? extends CharSequence> l = null; Number o = |l.get(0);}}",
                "Change type of o to CharSequence",
                "package test; import java.util.List; public class Test {private void test() {List<? extends CharSequence> l = null; CharSequence o = l.get(0);}}");
    }

    public void testToAnonymousType120619() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void foo() {Strin|g d = new Runnable() {public void run() {}};}}",
                       "Change type of d to Runnable",
                       "package test; public class Test {public void foo() {Runnable d = new Runnable() {public void run() {}};}}");
    }

    /**
     * change to &lt;nulltype&gt; should not be offered
     */
    public void test141664() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {private void test() {char x = |null;}}");
    }

    public void testForEach1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void foo(Iterable<Object> it) { for (String o : it) { } } }",
                       -1,
                       "Change type of o to Object",
                       "package test; public class Test {public void foo(Iterable<Object> it) { for (Object o : it) { } } }");
    }

    public void testForEach2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void foo(java.util.List<? extends Object> it) { for (String o : it) { } } }",
                       -1,
                       "Change type of o to Object",
                       "package test; public class Test {public void foo(java.util.List<? extends Object> it) { for (Object o : it) { } } }");
    }

    public void testForEach3() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void foo(Object[] it) { for (String o : it) { } } }",
                       -1,
                       "Change type of o to Object",
                       "package test; public class Test {public void foo(Object[] it) { for (Object o : it) { } } }");
    }
    
    public void testForEachCaptured224232() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void foo() { for (String o : m()) { } } private Object[] m() {return null;} }",
                       -1,
                       "Change type of o to Object",
                       "package test; public class Test {public void foo() { for (Object o : m()) { } } private Object[] m() {return null;} }");
    }

    public void testGenericsEscaped() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.*; public class Test {public void foo() { List<Number> l = |new ArrayList<String>(); } }",
                       "Change type of l to ArrayList&lt;String>",
                       "package test; import java.util.*; public class Test {public void foo() { ArrayList<String> l = new ArrayList<String>(); } }");
    }

    public void test235716FixType() throws Exception {
        performFixTest("test/Test.java",
            "package test;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "class Test {\n" +
            "    static void f() {\n" +
            "       List<Class<?>> asList = Arrays.asList(Integer.class,String.class);\n" +
            "    }\n" +
            "}", -1,
            "Change type of asList to List",
            ("package test;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
             "class Test {\n" +
             "    static void f() {\n" +
             "       List asList = Arrays.asList(Integer.class,String.class);\n" +  
             "    }\n" +
             "}").replaceAll("\\s+", " "));
    }

    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) {
        List<Fix> fixes = new ChangeType().run(info, null, pos, path, null);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof ChangeTypeFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((ChangeTypeFix) f).getText();
    }
    
    static {
        NbBundle.setBranding("test");
    }

}
