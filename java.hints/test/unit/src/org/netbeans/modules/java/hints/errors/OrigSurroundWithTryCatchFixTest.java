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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.ErrorFixesFakeHint.FixKind;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class OrigSurroundWithTryCatchFixTest extends ErrorHintsTestBase {
    
    public OrigSurroundWithTryCatchFixTest(String testName) {
        super(testName);
    }

    private boolean origUseLogger;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.SURROUND_WITH_TRY_CATCH);
        origUseLogger = ErrorFixesFakeHint.isUseLogger(prefs);

        ErrorFixesFakeHint.setUseLogger(prefs, false);
    }

    @Override
    protected void tearDown() throws Exception {
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.SURROUND_WITH_TRY_CATCH);
        ErrorFixesFakeHint.setUseLogger(prefs, origUseLogger);
        
        super.tearDown();
    }
    
    public void testTryWrapper1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        try {\n" +
                       "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTryWrapper2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        {\n" +
                       "            FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "        }\n" +
                       "        {\n" +
                       "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                       "            fis.read();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                        "        {\n" +
                        "            try {\n" +
                        "                FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                        "            } catch (FileNotFoundException ex) {\n" +
                        "                ex.printStackTrace();\n" +
                        "            }\n" +
                        "        }\n" +
                        "        {\n" +
                        "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                        "            fis.read();\n" +
                        "        }\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper4() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis = |new FileInputStream(new File(\"\"));\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper5() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis = |new FileInputStream(new File(\"\")),d,e,f;\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream a,b,c,fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        FileInputStream d,e,f;\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper6() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\")),a,b,c;\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        FileInputStream a,b,c;\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void XtestTryWrapper7() throws Exception {} //TODO: see the original test
    
    public void testTryWrapper8() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis;\n" +
                       "        fis = |new FileInputStream(new File(\"\"));\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        FileInputStream fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapper9() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.IOException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        |throw new IOException();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.IOException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        try {\n" +
                       "            throw new IOException();\n" +
                       "        } catch (IOException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void testTryWrapperComments171262a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        //pribytek\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        try {\n" +
                       "            //pribytek\n" +
                       "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTryWrapperComments171262b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        //nabytek\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\"));//NOI18N\n" +
                       "        //foo\n" +
                       "        \n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        //nabytek\n" +
                       "        FileInputStream fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\")); //NOI18N\n" +
                       "            //foo\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTryWrapper171124() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private int test() throws Exception {\n" + 
                       "        return 0;\n" +
                       "    }\n" +
                       "    private void testm() {\n" +
                       "        switch(10) {\n" +
                       "            case 1:\n" +
                       "                int i = |test();\n" +
                       "                return i;\n" +
                       "            case 2:\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() throws Exception {\n" + 
                       "        return 0;\n" +
                       "    }\n" +
                       "    private void testm() {\n" +
                       "        switch(10) {\n" +
                       "            case 1:\n" +
                       "                int i;\n" +
                       "                try {\n" +
                       "                    i = test();\n" +
                       "                } catch (Exception ex) {\n" +
                       "                    ex.printStackTrace();\n" +
                       "                }\n" +
                       "                return i;\n" +
                       "            case 2:\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTryWrapper189271() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private int test() throws Exception {\n" + 
                       "        return 0;\n" +
                       "    }\n" +
                       "    private void testm() {\n" +
                       "        for(int i = |test(); i < 10; i++) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() throws Exception {\n" + 
                       "        return 0;\n" +
                       "    }\n" +
                       "    private void testm() {\n" +
                       "        try {\n" +
                       "            for(int i = test(); i < 10; i++) {\n" +
                       "            }\n" +
                       "        } catch (Exception ex) {\n" +
                       "                    ex.printStackTrace();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test220064() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "public abstract class Test {\n" +
                       "    static {\n" +
                       "        FileInputStream fis = |new FileInputStream(new File(\"\"));\n" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.io.File;\n" +
                       "import java.io.FileInputStream;\n" +
                       "import java.io.FileNotFoundException;\n" +
                       "public abstract class Test {\n" +
                       "    static {\n" +
                       "        FileInputStream fis;\n" +
                       "        try {\n" +
                       "            fis = new FileInputStream(new File(\"\"));\n" +
                       "        } catch (FileNotFoundException ex) {\n" +
                       "            ex.printStackTrace();\n" +
                       "        }" +
                       "        fis.read();\n" +
                       "    }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new UncaughtException().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof OrigSurroundWithTryCatchFix) {
            return "FixImpl";
        }
        
        return super.toDebugString(info, f);
    }

}
