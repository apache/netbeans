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
                       "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
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
                       "         throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
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
                        "             throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
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
                        "             throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
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
                        "            throw new UnsupportedOperationException(\"Not supported yet.\"); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody\n" +
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
