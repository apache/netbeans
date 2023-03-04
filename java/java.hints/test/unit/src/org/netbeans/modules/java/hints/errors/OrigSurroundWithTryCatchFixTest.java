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
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.ErrorFixesFakeHint.FixKind;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
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
        if (f instanceof JavaFixImpl && ((JavaFixImpl) f).jf instanceof OrigSurroundWithTryCatchFix) {
            return "FixImpl";
        }
        
        return super.toDebugString(info, f);
    }

}
