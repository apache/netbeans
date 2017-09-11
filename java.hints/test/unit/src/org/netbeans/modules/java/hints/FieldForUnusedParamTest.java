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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.FieldForUnusedParam.FixImpl;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class FieldForUnusedParamTest extends TreeRuleTestBase {
    
    public FieldForUnusedParamTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     public Test(int |a) {\n" +
                       "     }\n" +
                       "}\n",
                       "2:21-2:21:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private final int a; public Test(int a) { this.a = a; } } ");
    }
    
    public void testSimple2() throws Exception {
        performAnalysisTest("test/Test.java",
                           "package test;\n" +
                           "public class Test {\n" +
                           "     private String a;\n"+
                           "     public Test(int |a) {\n" +
                           "     }\n" +
                           "}\n");
    }
    
    public void testSimple3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     public Test(int |a) {\n" +
                       "     }\n" +
                       "}\n",
                       "3:21-3:21:hint:Unused Parameter",
                       "FixImpl:true",
                       "package test; public class Test { private int a; public Test(int a) { this.a = a; } } ");
    }
    
    public void test125691() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private int a;\n" +
                            "     public Test(int |a)\n" +
                            "}\n");
    }
    
    public void testOrdering1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     private int c;\n" +
                       "     public Test(int a, int |b, int c) {\n" +
                       "          this.a = a;\n" +
                       "          this.c = c;\n" +
                       "     }\n" +
                       "}\n",
                       "4:28-4:28:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private int a; private final int b; private int c; public Test(int a, int b, int c) { this.a = a; this.b = b; this.c = c; } } ");
    }
    
    public void test206367() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     public Test(String|) {}\n" +
                            "}\n");
    }

    public void testMultipleCtors() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     private int c;\n" +
                       "     public Test() {}\n" +
                       "     public Test(int a, int |b, int c) {\n" +
                       "          this.a = a;\n" +
                       "          this.c = c;\n" +
                       "     }\n" +
                       "}\n",
                       "5:28-5:28:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private int a; private int b; private int c; public Test() {} public Test(int a, int b, int c) { this.a = a; this.b = b; this.c = c; } } ");
    }
    
    public void testMultipleCtorsChained() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int a;\n" +
                       "     private int c;\n" +
                       "     public Test() { this(0, 0, 0); }\n" +
                       "     public Test(int a, int |b, int c) {\n" +
                       "          this.a = a;\n" +
                       "          this.c = c;\n" +
                       "     }\n" +
                       "}\n",
                       "5:28-5:28:hint:Unused Parameter",
                       "FixImpl:false",
                       "package test; public class Test { private int a; private final int b; private int c; public Test() { this(0, 0, 0); } public Test(int a, int b, int c) { this.a = a; this.b = b; this.c = c; } } ");
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path, int offset) {
        FieldForUnusedParam h = new FieldForUnusedParam();

        if ("test206367".equals(getName())) {
            while (path != null && !h.getTreeKinds().contains(path.getLeaf().getKind())) {
                path = path.getParentPath();
            }

            assertNotNull(path);
        }

        return h.run(info, path, offset);
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof FixImpl) {
            return "FixImpl:" + ((FixImpl) f).existing;
        }
        return super.toDebugString(info, f);
    }

    static {
        NbBundle.setBranding("test");
    }
    
}
