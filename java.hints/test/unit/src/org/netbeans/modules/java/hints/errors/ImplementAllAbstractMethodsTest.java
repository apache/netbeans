/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.ImplementAllAbstractMethods.DebugFix;
import org.netbeans.modules.java.hints.errors.ImplementAllAbstractMethods.ImplementOnEnumValues2;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.Fix;

/**Legacy tests are in ErrorHintsTest.
 *
 * @author Jan Lahoda
 */
public class ImplementAllAbstractMethodsTest extends ErrorHintsTestBase {

    public ImplementAllAbstractMethodsTest(String name) {
        super(name);
    }

    public void testImplementAllMethodsForEnums() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public en|um Test implements Runnable {\n" +
                       "     A;\n" +
                       "}\n",
                       "IAAM",
                       ("package test;\n" +
                       "public enum Test implements Runnable {\n" +
                       "     A;\n" +
                       "     public void run() {\n" +
                       "         throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testNoMakeAbstractForEnums() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;\n" +
                       "public en|um Test implements Runnable {\n" +
                       "     A;\n" +
                       "}\n",
                       "IAAM");
    }

    public void test204252a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public cl|ass Test implements Runnable {\n" +
                       "}\n",
                       "MA:Test",
                       ("package test;\n" +
                       "public abstract class Test implements Runnable {\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test204252b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public cl|ass Test implements Runnable {\n" +
                       "}\n",
                       "IAAM",
                       ("package test;\n" +
                       "public class Test implements Runnable {\n" +
                       "     public void run() {\n" +
                       "         throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test204252c() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "final cl|ass Test implements Runnable {\n" +
                       "}\n",
                       "MA:Test",
                       ("package test;\n" +
                       "abstract class Test implements Runnable {\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test209164() throws Exception {
        performAnalysisTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void run() {\n" +
                       "          new Runnable() |{};\n" +
                       "     }\n" +
                       "}\n",
                       "IAAM");
    }
    
    public void test178153a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public enum Test implements Runnable {\n" +
                       "     A {\n" +
                       "     };\n" +
                       "}\n",
                       -1,
                       "IAAM",
                       ("package test;\n" +
                        "public enum Test implements Runnable {\n" +
                        "     A {\n" +
                        "         public void run() {\n" +
                        "             throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                        "         }\n" +
                        "     };\n" +
                        "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void test178153b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public enum Test implements Runnable {\n" +
                       "     A { public void run() {} },\n" +
                       "     B;\n" +
                       "}\n",
                       -1,
                       "IAAM",
                       ("package test;\n" +
                        "public enum Test implements Runnable {\n" +
                        "     A { public void run() {} },\n" +
                        "     B {\n" +
                        "         public void run() {\n" +
                        "             throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                        "         }\n" +
                        "     };\n" +
                        "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testEnumWithAbstractMethods() throws Exception {
        performFixTest("test/Test.java",
                        "package test;\n" +
                        "public enum Test {\n" +
                        "    A;\n" +
                        "    public abstract int boo();\n" +
                        "}",
                       -1,
                       "IOEV",
                       ("package test;\n" +
                        "public enum Test {\n" +
                        "    A {\n" +
                        "\n" +
                        "        @Override\n" +
                        "        public int boo() {\n" +
                        "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                        "        }\n" +
                        "    };\n" +
                        "    public abstract int boo();\n" +
                        "}").replaceAll("[ \t\n]+", " "));
    }
    
    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new ImplementAllAbstractMethods().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        Object o = f;
        if (o instanceof JavaFixImpl) {
            o = ((JavaFixImpl)f).jf;
        }
        if (o instanceof DebugFix) {
            return ((DebugFix) o).toDebugString();
        } else if (o instanceof ImplementOnEnumValues2) {
            return "IOEV";
        } else 
        return super.toDebugString(info, f);
    }

}
