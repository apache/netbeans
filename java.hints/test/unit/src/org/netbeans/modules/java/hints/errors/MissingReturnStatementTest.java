/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class MissingReturnStatementTest extends ErrorHintsTestBase {

    public MissingReturnStatementTest(String name) {
        super(name);
    }

    public void testToVoid1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private String m() { |}\n" +
                       "}\n",
                       "FIX_ChangeMethodReturnType void",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private void m() { }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testAddReturn1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private String m() {\n" +
                       "    |}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private String m() {\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void test205020a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                       "        List<String> result \n" +
                       "    |}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       ("package test;\n" +
                        "import java.util.Collection;\n" +
                        "import java.util.List;\n" +
                        "public class Test {\n" +
                        "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                        "        List<String> result \n" +
                        "        return null;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test205020b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                       "        List<String> result;|}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       "package test; import java.util.Collection; import java.util.List; public class Test { public static Collection<String> join(String[] arr1, String[] arr2) { List<String> result;return null; } } ");
    }

    public void test205020c() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public static Collection<String> join(String[] arr1, String[] arr2) {\n" +
                       "        List<String> result; |}\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       "package test; import java.util.Collection; import java.util.List; public class Test { public static Collection<String> join(String[] arr1, String[] arr2) { List<String> result; return null; } } ");
    }
    
    public void testMissingLambdaReturn() throws Exception {
        diagKey = "compiler.err.prob.found.req/compiler.misc.incompatible.ret.type.in.lambda/compiler.misc.missing.ret.val"; // NOI18N
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Collection;\n" +
                       "import java.util.concurrent.Callable;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        Callable<String> c = |() -> {" +
                       "        };\n" +
                       "    }\n" +
                       "}\n",
                       "FIX_AddReturnStatement",
                       "package test; import java.util.Collection; import java.util.concurrent.Callable; public class Test { public static void test() { Callable<String> c = () -> {return null; }; } } ");
    }
    
    private String diagKey = "compiler.err.missing.ret.stmt"; // NOI18N

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new MissingReturnStatement().run(info, diagKey, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }

}
