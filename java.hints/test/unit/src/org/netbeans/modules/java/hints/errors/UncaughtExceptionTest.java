/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class UncaughtExceptionTest extends ErrorHintsTestBase {
    private Set<String> supportedCodes = Collections.singleton(UncaughtException.ERR_UNREPORTED);
    
    public UncaughtExceptionTest(String name) {
        super(name, UncaughtException.class);
    }

    /**
     * test228693 and others report 2 exceptions, one for the operation itself and
     * another diag code for uncaught IOException from the implcitly generated close().
     */
    @Override
    protected Set<String> getSupportedErrorKeys() throws Exception {
        return supportedCodes != null ? supportedCodes : super.getSupportedErrorKeys();
    }

    public void test204029() throws Exception {
        String code =  "package test;\n" +
                       "import java.io.IOException;\n" +
                       "import javax.swing.text.BadLocationException;\n" +
                       "public class Test {\n" +
                       "    void t2() {\n" +
                       "        try {\n" +
                       "            g();\n" +
                       "        } catch (BadLocationException | IOException e) {\n" +
                       "            throw e;\n" +
                       "        }\n" +
                       "    }\n" +
                       "    void g() throws BadLocationException | IOException { }\n" +
                       "}\n";
        performAnalysisTest("test/Test.java",
                            code,
                            code.indexOf("throw e") + 1,
                            "Add throws clause for javax.swing.text.BadLocationException",
                            "Add throws clause for java.io.IOException",
                            "Surround Statement with try-catch");
    }
    
    public void test228693() throws Exception {
        sourceLevel = "1.7";
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "import java.io.*;\n" +
                            "public class Test {\n" +
                            "    void t2() {\n" +
                            "        try (InputStream in = new FileInputStream(\"\")) {\n" +
                            "        }\n" +
                            "    }\n" +
                            "}\n",
                            -1,
                            "Add throws clause for java.io.FileNotFoundException",
                            "LBL_AddCatchClauses");
    }

    public void test216085() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.net.*;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                       "   public static void main(String[] args) {\n" +
                       "      a(0);\n" +
                       "   }\n" +
                       "   private static int a(int i) throws FileNotFoundException, IOException, MalformedURLException {\n" +
                       "      return i;\n" +
                       "   }\n" +
                       "}\n",
                       -1,
                       "Surround Statement with try-catch",
                       ("package test;\n" +
                        "import java.net.*;\n" +
                        "import java.io.*;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                        "   public static void main(String[] args) {\n" +
                        "      try {\n" +
                        "          a(0);\n" +
                        "      } catch (IOException ex) {\n" +
                        "          Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);\n" +
                        "      }\n" +
                        "   }\n" +
                        "   private static int a(int i) throws FileNotFoundException, IOException, MalformedURLException {\n" +
                        "      return i;\n" +
                        "   }\n" +
                        "}\n").replaceAll("\\s+", " "));
    }
    
    public void test229157a() throws Exception {
        sourceLevel = "1.8";
        String code =  "package test;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                       "    void t2(Runnable r, File f) {\n" +
                       "        t2( () -> {\n" +
                       "             try (InputStream in = new FileInputStream(f)) {\n" +
                       "                 in.read();\n" +
                       "             }\n" +
                       "        });\n" +
                       "    }\n" +
                       "}\n";
        performAnalysisTest("test/Test.java",
                            code,
                            code.indexOf("in.read();") + 1,
                            "LBL_AddCatchClauses",
                            "Surround Statement with try-catch");
    }

    public void test229157b() throws Exception {
        sourceLevel = "1.8";
        String code =  "package test;\n" +
                       "import java.io.*;\n" +
                       "public class Test {\n" +
                       "    void t2(Runnable r, File f) {\n" +
                       "        try (InputStream in = new FileInputStream(f)) {\n" +
                       "            t2( () -> {\n" +
                       "                 in.read();\n" +
                       "            });\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n";
        performAnalysisTest("test/Test.java",
                            code,
                            code.indexOf("in.read();") + 1,
                            "Surround Statement with try-catch");
    }

    public void test243098() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "class SurroundLambdaExpressionWithTryCatch {\n" +
                "    void bug() {\n" +
                "        Runnable r = () -> throwing(); // \"unreported exception Exception; must be caught or declared to be thrown\"\n" +
                "    }\n" +
                "    void throwing() throws Exception {}\n" +
                "}",
                -1,
                "Surround Statement with try-catch",
                ("package test;\n" +
                 "import java.util.logging.Level;\n" +
                 "import java.util.logging.Logger;\n" +
                 "\n" +
                 "class SurroundLambdaExpressionWithTryCatch {\n" +
                 "    void bug() {\n" +
                 "        Runnable r = () -> {\n" +
                 "            try {\n" +
                 "                throwing();\n" +
                 "            } catch (Exception ex) {\n" +
                 "                Logger.getLogger(SurroundLambdaExpressionWithTryCatch.class.getName()).log(Level.SEVERE, null, ex);\n" +
                 "            }\n" +
                 "        }; // \"unreported exception Exception; must be caught or declared to be thrown\"\n" +
                 "    }\n" +
                 "    void throwing() throws Exception {}\n" +
                 "}").replaceAll("\\s+", " "));
    }
    
    public void testJDK8ResourceImplicitClose() throws Exception {
        supportedCodes = null;
        sourceLevel = "1.7";
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.io.InputStreamReader;\n" +
                "import java.io.Reader;\n" +
                "public class Test {\n" +
                "    public static void main(String[] args) {   \n" +
                "        try (Reader in = new InputStreamReader(Test.class.getResourceAsStream(\"nashorn.js\"))) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
                -1,
                "Add throws clause for java.io.IOException",
                ("package test;\n" +
                "import java.io.IOException;\n" +
                "import java.io.InputStreamReader;\n" +
                "import java.io.Reader;\n" +
                "public class Test {\n" +
                "    public static void main(String[] args) throws IOException {   \n" +
                "        try (Reader in = new InputStreamReader(Test.class.getResourceAsStream(\"nashorn.js\"))) {\n" +
                "        }\n" +
                "    }\n" +
                "}").replaceAll("\\s+", " "));
    }
    

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    static {
        NbBundle.setBranding("test");
    }
}
