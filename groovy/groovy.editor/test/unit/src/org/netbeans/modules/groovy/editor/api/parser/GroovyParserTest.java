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

package org.netbeans.modules.groovy.editor.api.parser;

import groovy.transform.CompilationUnitAware;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import java.util.logging.Level;
import static junit.framework.TestCase.assertNotNull;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.test.GroovyTestTransformer;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Martin Adamek
 */
public class GroovyParserTest extends GroovyTestBase {

    public GroovyParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        Logger.getLogger(org.netbeans.modules.groovy.editor.api.parser.GroovyParser.class.getName())
                .setLevel(Level.FINEST);
    }

    @Override
    protected void tearDown() throws Exception {
        // remove the static stuff
        parserUnit = null;
        parserConfig = null;
        parserCompUnit = null;
        enabledForUnit = null;
        GroovyTestTransformer.parserCompUnit = null;
        
        // TODO cancel indexing?
        // don't clear since indexer might be still active; setUp() clears on reruns
//        clearWorkDir();

        URL u = URLMapper.findURL(FileUtil.getConfigRoot(), URLMapper.EXTERNAL);
        if (u != null) {
            Path p = Paths.get(u.toURI());
            FileObject fo = URLMapper.findFileObject(u);
            if (fo != null) {
                fo.delete();
            }
        }
        FileUtil.getConfigRoot().refresh();
        super.tearDown();
    }

    private void checkParseTree(final FileObject file, final String caretLine, final String nodeName) throws Exception {
        final Source source = Source.create(file);

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                String text = result.getSnapshot().getText().toString();

                int caretOffset = -1;
                int caretDelta = -1;
                String realCaretLine = null;

                if (caretLine != null) {
                    caretDelta = caretLine.indexOf("^");
                    assertTrue(caretDelta != -1);
                    realCaretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
                    int lineOffset = text.indexOf(realCaretLine);
                    assertTrue(lineOffset != -1);

                    caretOffset = lineOffset + caretDelta;
                    enforceCaretOffset(source, caretOffset);
                }

                ASTNode root = ASTUtils.getRoot(result);
                assertNotNull("Parsing broken input failed for " + file, root);

                // Ensure that we find the node we're looking for
                if (nodeName != null) {
        //            GroovyParserResult rpr = (GroovyParserResult)info.getParserResult();
                    OffsetRange range = OffsetRange.NONE; //rpr.getSanitizedRange();
                    if (range.containsInclusive(caretOffset)) {
                        caretOffset = range.getStart();
                    }

                    Scanner scanner = new Scanner(text);
                    int lineNumber = 1;
                    int column = -1;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        int indexOfCaretLine = line.indexOf(realCaretLine);
                        if (indexOfCaretLine != -1) {
                            column = indexOfCaretLine + caretDelta + 1;
                            break;
                        }
                        lineNumber++;
                    }

                    AstPath path = new AstPath(root, lineNumber, column);
                    ASTNode closest = path.leaf();
                    assertNotNull(closest);
                    String leafName = closest.getClass().getName();
                    leafName = leafName.substring(leafName.lastIndexOf('.')+1);
                    assertEquals(nodeName, leafName);
                }
            }
        });
    }

    public void test1() throws Exception {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tstatic void main(args) {\n" +
                "\t\tString s = 'aaa'\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");
        checkParseTree(testFO, "void ^main", "MethodNode");
    }

    public void test2() throws Exception {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");
        checkParseTree(testFO, "void ^main", "MethodNode");
    }

    public void testAstUtilitiesGetRoot() throws Exception {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");

        Source source = Source.create(testFO);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                ASTNode root = ASTUtils.getRoot(result);
                AstPath path = new AstPath(root ,1, (BaseDocument) result.getSnapshot().getSource().getDocument(true));
                assertNotNull("new AstPath() failed", path);
            }
        });
    }


    public void testSanatizerRemoveDotBeforeError() throws Exception {

        copyStringToFileObject(testFO,
                "def m() {\n" +
                "\tObject x = new Object()\n" +
                "\tx.\n" +
                "}\n");

        /*
            0000000   d   e   f       m   (   )       {  \n  \t   O   b   j   e   c
            0000016   t       x       =       n   e   w       O   b   j   e   c   t
            0000032   (   )  \n  \t   x   .  \n   }  \n
         */

        Source source = Source.create(testFO);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                ASTNode root = ASTUtils.getRoot(result);
                assertNotNull(root);
            }
        });
    }
    
    /**
     * Check the basic state, that the AST transformation is enabled.
     * @throws Exception 
     */
    public void testRegisteredGlobalASTProcessorUsed() throws Exception {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");
        // delete the instruction from annotation processor, to check the default behaviour
        FileUtil.getConfigFile("Editors/text/x-groovy/Parser/org.netbeans.modules.groovy.editor.test.DisableTransformersStub.instance").delete();
        FileUtil.getConfigFile("Editors/text/x-groovy/Parser/org.netbeans.modules.groovy.editor.api.parser.GroovyParserTest.DormantTransformation.instance").delete();
        Source source = Source.create(testFO);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                assertNotNull(result);
                // parser has run the gkobal transformer
                assertNotNull(parserUnit);
                // but the explicit transofmrer has not.
                assertNull(enabledForUnit);
                assertNotNull(GroovyTestTransformer.parserCompUnit);
            }
        });
    }
    
    /**
     * Checks that simple .disable file will disable the transformation
     * @throws Exception 
     */
    public void testGlobalASTProcessorDisabledByAttribute() throws Exception {
        // add an instruction to skip the transformation
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");

        Source source = Source.create(testFO);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                assertNotNull(result);
                assertNull(parserUnit);
                assertNotNull(GroovyTestTransformer.parserCompUnit);
            }
        });
    }
    
    public void testExplicitlyEnableASTTransformationByAttr() throws Exception {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");

        Source source = Source.create(testFO);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                assertNotNull(result);
                assertNotNull(enabledForUnit);
                assertNotNull(GroovyTestTransformer.parserCompUnit);
            }
        });
    }
    
    public void testSpockDisabledByDefault() throws Exception {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");

        Source source = Source.create(testFO);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                // the parser is lazy, must trigger parsing.
                ASTUtils.getParseResult(resultIterator.getParserResult());
            }
        });
        Set<String> disabledClasses = enabledForUnit.getConfiguration().getDisabledGlobalASTTransformations();
        assertTrue(disabledClasses.contains("org.spockframework.compiler.SpockTransform"));
    }
    
    static SourceUnit parserUnit;
    static CompilationUnit parserCompUnit;
    static CompilerConfiguration parserConfig;
    
    static CompilationUnit enabledForUnit;
    
    @GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
    @ApplyGroovyTransformation(enable = "parse")
    public static class DormantTransformation implements ASTTransformation, CompilationUnitAware {

        @Override
        public void setCompilationUnit(CompilationUnit unit) {
            enabledForUnit = unit;
        }

        @Override
        public void visit(ASTNode[] nodes, SourceUnit source) {
        }
    }

    @GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
    public static class TestingTransformation implements ASTTransformation, CompilationUnitAware {

        @Override
        public void setCompilationUnit(CompilationUnit unit) {
            parserCompUnit = unit;
        }
        
        @Override
        public void visit(ASTNode[] nodes, SourceUnit source) {
            parserUnit = source;
            parserConfig = source.getConfiguration();
        }
        
    }

}
