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
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan lahoda
 */
public class DeclarationForInstanceOfTest extends TreeRuleTestBase {
    
    public DeclarationForInstanceOfTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "2:20-2:20:hint:Declaration for instanceof",
                       "FixImpl",
                       "package test;public class Test { private void test(Object o) { if (o instanceof String) { String string = (String) o; } } } ");
    }
    
    public void DISABLEDtestBrokenSource() throws Exception {
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String)\n" +
                       "    }\n" +
                       "}\n",
                       "2:20-2:20:hint:Declaration for instanceof",
                       "FixImpl",
                       "package test;public class Test { private void test(Object o) { if (o instanceof String) { String string = (String) o; } } } ");
    }
    
    public void DISABLEtest132746() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String) {\n" +
                       "            String s = (String) o;" +
                       "        }\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void test132747() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Integer o) {\n" +
                       "        if (o instan|ceof String) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n");
    }
    
    public void test132755() throws Exception {
        performFixTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Object o) {\n" +
                       "        if (o instan|ceof String)\n" +
                       "            System.err.println(o);\n" +
                       "    }\n" +
                       "}\n",
                       "2:20-2:20:hint:Declaration for instanceof",
                       "FixImpl",
                       "package test;public class Test { private void test(Object o) { if (o instanceof String) { String string = (String) o; System.err.println(o); } } } ");
    }
    
    public void test132757() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "public class Test {\n" +
                       "    private void test(Integer o) {\n" +
                       "        if (o instan|ceof ) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n");
    }
    
    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path, int offset) {
        return new DeclarationForInstanceOf().run(info, path, offset);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof DeclarationForInstanceOf.FixImpl) {
            return "FixImpl";
        }
        return super.toDebugString(info, f);
    }

}
