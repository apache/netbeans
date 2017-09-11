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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class CreateClassTest extends ErrorHintsTestBase {
    
    public CreateClassTest(String name) {
        super(name);
    }
    
    public void testCreateClassForNewExpression() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new NonExisting(1);}}", 114 - 48, "CreateClass:test.NonExisting:[public]:CLASS", "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }

    public void testCreateClassVariable() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {NonExisting n;}}", 112 - 48, "CreateClass:test.NonExisting:[]:CLASS", "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }

    public void testCreateMemberSelect() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {NonExisting.call();}}", 112 - 48, "CreateClass:test.NonExisting:[]:CLASS", "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }

    public void test116853() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public interface Test {public Non|Existing test();}", "CreateClass:test.NonExisting:[]:CLASS", "CreateInnerClass:test.Test.NonExisting:[public, static]:CLASS");
    }

    public void testPerformCreateClass() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test {public static void test() {NonExisting f;}}", 112 - 48, "CreateClass:test.NonExisting:[]:CLASS", "test/NonExisting.java", "package test; class NonExisting { public NonExisting() { } } ");
    }

    public void testPerformCreateClassParams() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test {public static void test() {new NonExisting(1, \"\");}}", 112 - 48, "CreateClass:test.NonExisting:[public]:CLASS", "test/NonExisting.java", "package test; public class NonExisting { public NonExisting(int i, String string) { } } ");
    }

    public void disabledtestPerformCreateClassInPackage() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test {public static void test() {new test2.NonExisting(1, \"\");}}", 112 - 48, "CreateClass:test2.NonExisting:[]:CLASS", "test2/NonExisting.java", "package test2; class NonExisting { public NonExisting(int i,String string) { } } ");
    }

    public void testCreateInnerClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {Test.NonExisting x;}}", 119- 48, "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }

    public void testCreateInnerClassPerform() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {Test.NonExisting x;}}",
                       94 - 25,
                       "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS",
                       "package test; public class Test {public static void test() {Test.NonExisting x;} private static class NonExisting { public NonExisting() { } } }");
    }

    public void testCreateInnerClassForNewClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public static void test() {new Test.NonExisting(1);}}", 121- 48, "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }

    public void testCreateInnerClassForNewClassPerform() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Test.NonExisting(1);}}",
                       100 - 25,
                       "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS",
                       "package test; public class Test {public static void test() {new Test.NonExisting(1);} private static class NonExisting { public NonExisting(int i) { } } }");
    }

    public void testCreateClassTypeParameter() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test<T extends Number&CharSequence> {public static void test() {Test<NonExisting> t;}}", 150 - 48, "CreateClass:test.NonExisting:[]:CLASS", "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }

    public void testPerformCreateClassTypeParameter() throws Exception {
        performFixTest("test/Test.java", "package test; public class Test<T extends Number&CharSequence> {public static void test() {Test<NonExisting> t;}}", 150 - 48, "CreateClass:test.NonExisting:[]:CLASS", "test/NonExisting.java", "package test; class NonExisting extends Number implements CharSequence { public NonExisting() { } } ");
    }

    public void testPerformCreateInterface() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static class X implements Test.NonExisting {}}",
                       119 - 43,
                       "CreateInnerClass:test.Test.NonExisting:[private, static]:INTERFACE",
                       "package test; public class Test {public static class X implements Test.NonExisting {} private static interface NonExisting { } }");
    }

    public void testPerformCreateForExtends() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test {public static class X extends NonExisting {}}",
                            92 - 25,
                            "CreateClass:test.NonExisting:[]:CLASS",
			    "CreateInnerClass:test.Test.NonExisting:[private, static]:CLASS");
    }

    public void testPerformCreateInterfaceGeneric() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static class X implements java.util.concurrent.Future<NonExisting> {}}",
                       123 - 25,
                       "CreateClass:test.NonExisting:[]:CLASS",
                       "test/NonExisting.java",
                       "package test; class NonExisting { public NonExisting() { } } ");
    }

    public void testGenericNC1() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Unknown1<Unknown2>();}}",
                       93 - 25,
                       "CreateClass:test.Unknown1:[public]:CLASS",
		       "CreateInnerClass:test.Test.Unknown1:[private, static]:CLASS"
                       );
    }

    public void testGenericNC2() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Unknown1<Unknown2>();}}",
                       101 - 25,
                       "CreateClass:test.Unknown2:[public]:CLASS",
		       "CreateInnerClass:test.Test.Unknown2:[private, static]:CLASS"
                       );
    }

    public void testDeclWithTypeArguments() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {Unknown1<String> s;}}",
                       88 - 25,
                       "CreateClass:test.Unknown1:[]:CLASS",
                       "test/Unknown1.java",
                       "package test; class Unknown1<T> { public Unknown1() { } } "
                       );
    }

    public void testGenericCreateWithTypeArguments() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public static void test() {new Unknown1<String>(1);}}",
                       92 - 25,
                       "CreateClass:test.Unknown1:[public]:CLASS",
                       "test/Unknown1.java",
                       "package test; public class Unknown1<T> { public Unknown1(int i) { } } "
                       );
    }

    public void testCreateFromTopLevelClass() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test implements UU {}",
                       69 - 25,
                       "CreateClass:test.UU:[]:INTERFACE");
    }

    public void testCreate106773() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public UU g() {return null;}}",
                       66 - 25,
                       "CreateClass:test.UU:[]:CLASS",
		       "CreateInnerClass:test.Test.UU:[private, static]:CLASS");
    }

    public void testCreateFromNewArray() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public Object g() {return new Unknown[0];}}",
                       91 - 25,
                       "CreateClass:test.Unknown:[]:CLASS",
		       "CreateInnerClass:test.Test.Unknown:[private, static]:CLASS");
    }

    public void testCreate108016() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test; public class Test {public void g() {new Runnable() {public void run() {new Runnable() {public void run() {DDD.ddd();}};}};}}",
                       147 - 25,
                       "CreateClass:test.DDD:[]:CLASS",
		       "CreateInnerClass:test.Test.DDD:[private, static]:CLASS");
    }

    /**
     * Test offering creation of a new Exception type
     */
    public void testCreate113905() throws Exception {
	performAnalysisTest("test/Test.java",
                       "package test; public class Test {public void g() throws FooException{}}",
		       84 - 25,
		       "CreateClass:test.FooException:[]:CLASS",
		       "CreateInnerClass:test.Test.FooException:[private, static]:CLASS");
    }

    /**
     * Test if more than one exception is offered
     */
    public void testCreateException130810() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void g() throws FooException0, FooException1, FooExc|eption2 {}}",
                "CreateClass:test.FooException2:[]:CLASS", "CreateInnerClass:test.Test.FooException2:[private, static]:CLASS");
    }

    public void testCreateClassForArray() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { private St|ate[] arr;}",
                       "CreateClass:test.State:[]:CLASS",
                       "test/State.java",
                       "package test; class State { public State() { } } "
                       );
    }

    public void testCreateClassForThrow() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { private void test() { throw new Unde|fined(); } }",
                       "CreateClass:test.Undefined:[public]:CLASS",
                       "test/Undefined.java",
                       "package test; public class Undefined extends Exception { public Undefined() { } } ");
    }

    public void testNPE195488() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import test.Test.t|t; public class Test { }",
                       "CreateInnerClass:test.Test.tt:[static]:CLASS",
                       "package test; import test.Test.tt; public class Test { static class tt { public tt() { } } }");
    }
    
    public void test159844a() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { public Class t() { return T|T.class; } }",
                       "CreateClass:test.TT:[]:CLASS",
                       "test/TT.java",
                       "package test; class TT { public TT() { } } ");
    }
    
    public void test159844b() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { public Class<CharSequence> t() { return T|T.class; } }",
                       "CreateClass:test.TT:[]:CLASS",
                       "test/TT.java",
                       "package test; class TT implements CharSequence { public TT() { } } ");
    }
    
    public void test231160() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { public Test() { return T|T.class; } }",
                       "CreateClass:test.TT:[]:CLASS",
                       "test/TT.java",
                       "package test; class TT { public TT() { } } ");
    }

    public void testNPE206374() throws Exception {
        FileObject workFO = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workFO);

        FileObject aux = FileUtil.createData(workFO, "src/test/Aux.java");

        TestUtilities.copyStringToFile(aux, "package test; public class Aux {}");
        
        performAnalysisTest("Test.java",
                            "import test.Aux.t|t; public class Test { }",
                            "CreateInnerClass:test.Aux.tt:[public, static]:CLASS");
    }

    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws IOException {
        List<Fix> fixes = new CreateElement().analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateClassFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateClassFix) f).toDebugString(info);
    }

    @Override
    protected String[] getAdditionalLayers() {
        return new String[] {"org/netbeans/modules/java/hints/errors/empty-class-template.xml"};
    }
    
}
