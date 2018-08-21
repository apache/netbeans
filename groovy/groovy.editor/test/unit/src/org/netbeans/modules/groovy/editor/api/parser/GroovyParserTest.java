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

import java.util.Collections;
import java.util.Scanner;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;

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
        super.setUp();
        Logger.getLogger(org.netbeans.modules.groovy.editor.api.parser.GroovyParser.class.getName())
                .setLevel(Level.FINEST);
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

//    public void testDuplicateDefinitions() throws Exception {
//        copyStringToFileObject(testFO,
//            "class DuplicateFieldExample {\n" +
//            "String name\n" +
//            "String name\n" +
//            "def method() {\n" +
//            "\tprintln 'Hello, world'\n" +
//            "\t}\n" +
//            "}\n");
//
//        Source source = Source.create(testFO);
//        ParserManager.parse(Collections.singleton(source), new UserTask() {
//            public @Override void run(ResultIterator resultIterator) throws Exception {
//                GroovyParserResult result = AstUtilities.getParseResult(resultIterator.getParserResult());
//                ASTNode root = AstUtilities.getRoot(result);
//                assertNotNull("AstUtilities.getRoot(info) failed", root);
//            }
//        });
//    }
    
}
