/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.classfile.Record_attribute.ComponentInfo;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSourcePath;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;

public class EmbeddingTest extends GeneratorTestMDRCompat {

    public EmbeddingTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(EmbeddingTest.class);
//        suite.addTest(new ArraysTest("testConstantRename"));
//        suite.addTest(new ArraysTest("testDuplicateMethodWithArrReturn1"));
//        suite.addTest(new ArraysTest("testDuplicateMethodWithArrReturn2"));
//        suite.addTest(new ArraysTest("test120768"));
//        suite.addTest(new ArraysTest("testRewriteType"));
//        suite.addTest(new ArraysTest("test162485a"));
//        suite.addTest(new ArraysTest("test162485b"));
        return suite;
    }

    public void testInsertImport1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code = """
                      package hierbas.del.litoral;

                      public class Test {
                          public void get() {
                              String java =
                                  ""\"
                                  package test;

                                  public class Test {
                                  }
                                  ""\";
                          }
                      }
                      """;

        TestUtilities.copyStringToFile(testFile, code);

        String golden = """
                        package hierbas.del.litoral;

                        public class Test {
                            public void get() {
                                String java =
                                    ""\"
                                    package test;

                                    import java.util.List;

                                    public class Test {
                                    }
                                    ""\";
                            }
                        }
                        """;
        Source src = getSource(testFile);
        AtomicReference<JavaSourcePath> path = new AtomicReference<>();
        AtomicReference<Document> doc = new AtomicReference<>();
        ParserManager.parse(List.of(src), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult(code.indexOf("package test;"));
                CompilationController cc = CompilationController.get(result);
                path.set(cc.getJavaSourcePath());
                doc.set(cc.getSnapshot().getSource().getDocument(true));
            }
        });
        ModificationResult mr = ModificationResult.runModificationTask(path.get(), wc -> {
            wc.toPhase(Phase.RESOLVED);
            TreeMaker make = wc.getTreeMaker();
            wc.rewrite(wc.getCompilationUnit(), make.addCompUnitImport(wc.getCompilationUnit(), make.Import(make.QualIdent("java.util.List"), false)));
        });
        mr.commit();
        String res = doc.get().getText(0, doc.get().getLength());
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testInsertImport2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code = """
                      package hierbas.del.litoral;

                      public class Test {
                          public void get() {
                              String java =
                                  ""\"
                                  package test;
                                  public class Test {
                                  }
                                  ""\";
                          }
                      }
                      """;

        TestUtilities.copyStringToFile(testFile, code);

        //TODO: the whitespace:
        String golden = """
                        package hierbas.del.litoral;

                        public class Test {
                            public void get() {
                                String java =
                                    ""\"
                                    package test;
                                   \s
                                    import java.util.List;

                                    public class Test {
                                    }
                                    ""\";
                            }
                        }
                        """;
        Source src = getSource(testFile);
        AtomicReference<JavaSourcePath> path = new AtomicReference<>();
        AtomicReference<Document> doc = new AtomicReference<>();
        ParserManager.parse(List.of(src), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult(code.indexOf("package test;"));
                CompilationController cc = CompilationController.get(result);
                path.set(cc.getJavaSourcePath());
                doc.set(cc.getSnapshot().getSource().getDocument(true));
            }
        });
        ModificationResult mr = ModificationResult.runModificationTask(path.get(), wc -> {
            wc.toPhase(Phase.RESOLVED);
            TreeMaker make = wc.getTreeMaker();
            wc.rewrite(wc.getCompilationUnit(), make.addCompUnitImport(wc.getCompilationUnit(), make.Import(make.QualIdent("java.util.List"), false)));
        });
        mr.commit();
        String res = doc.get().getText(0, doc.get().getLength());
        System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
