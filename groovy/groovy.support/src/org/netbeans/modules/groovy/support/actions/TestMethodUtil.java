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
package org.netbeans.modules.groovy.support.actions;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Janicek
 */
public final class TestMethodUtil {

    private static final String GROOVY_PARSER_RESULT_CLASS_NAME = "org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult";

    private static final String GPR_GET_ROOT_ELEMENT = "getRootElement";

    private static final String AST_GET_MODULE_NODE = "getModuleNode";

    static boolean isTestClass(Node activatedNode) {
        FileObject fo = getFileObjectFromNode(activatedNode);
        if (fo != null) {
            //TODO add more checks here when action gets enabled?
            return isGroovyFile(fo);
        }
        return false;
    }

    /**
     * Given the text from a Document, will read through the lines, adding 1 to
     * a counter for each line, plus the length of the given line until the
     * value is greater than or equal to the cursor, and this gives us the line.
     * Next, the offset in the line which would make the counter equal to the
     * cursor value is taken to represent the column. This is done to map to the
     * Groovy AST nodes line and columns to match to classes and methods. The
     * return from this method can be used to find the class and method the
     * cursor is in. Remember, the Groovy lines and columns are index 1 with
     * relation to the AST node classes.
     *
     * @param srcText the Groovy source as represented in the edited Groovy
     * document; it must be this text as the expectation is a line will always
     * end in a \n
     * @param cursor the cursor offset in the given text.
     * @return the line and column of the cursor offset in the text as an
     * integer array such that index 0 is the line, and index 1 is the column
     */
    public static int[] getLineAndColumn(final String srcText, final int cursor) {
        Parameters.notNull("srcText", srcText);
        Parameters.notEmpty("srcText", srcText);
        int[] ret = new int[]{-1, -1};
        if (cursor > 0) {
            try {
                final StringReader sr = new StringReader(srcText);
                final LineNumberReader lr = new LineNumberReader(sr);
                int counter = 0;
                String line;
                while ((line = lr.readLine()) != null) {
                    //remember, we went over a line, and it has a \n
                    counter += line.length();
                    if (counter >= cursor) {
                        //lr incs line number at every line it reads,
                        //and started at 0
                        ret[0] = lr.getLineNumber();
                        //if we take away what we just added, then take the whole
                        //away from the cursor, then we are left with the number
                        //it takes to equal the cursor, and then since the chars
                        //and positions in the line are 0 indexed, we need to add
                        //1; if 0 or 0 length, then that should also work out
                        //correctly as if the cursor is on the line, it is in
                        //column 1
                        ret[1] = (cursor - (counter - line.length())) + 1;
                        break;
                    }
                    //account for the newline now as we don't care about it
                    //until we are done with the "current" line. The extra value
                    //will throw off the above calculation if added too soon, but
                    //if not added at all will also throw it off. So, add it here.
                    counter++;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (cursor == 0) {
            //first line and start of first line
            ret[0] = 1;
            ret[1] = 1;
        }
        return ret;
    }

    /**
     * Given a start and end line and a start and end column plus a given line
     * and column, tests if the given line and column falls between the start
     * and end values in relation to Groovy code and the Groovy AST.
     *
     * @param startLine the start line
     * @param startCol the start column
     * @param endLine the end line
     * @param endCol the end column
     * @param line the line to test
     * @param col the column to test
     * @return whether the line and column fall between the given start and end
     * values
     */
    public static boolean isBetweenLinesAndColumns(final int startLine,
            final int startCol,
            final int endLine,
            final int endCol,
            final int line,
            final int col) {
        boolean ret = false;
        if (line >= startLine && line <= endLine) {
            //at the momment we are approximately
            //in the method bounds, so we'll assume
            //true, then adjust accordingly
            //as this is simpler than embedded ifs
            //and logic as we only care about the col
            //if we are on one of the bounding lines
            ret = true;

            //even though above we say true, we may take it back
            //if on the same line as the start line and not in
            //the method bounds
            if (line == startLine && !(col >= startCol)) {
                ret = false;
            }

            //even though above we may say true, we may take it back
            //if on the same line as the end line and not in
            //the method bounds            
            if (line == endLine && !(col <= endCol)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Gets the Groovy AST ModuleNode associated with the given Source result.
     * This is currently done with reflection due to the dependency graph of
     * "Groovy Support" and "Groovy Editor". This is hopefully just temporary
     * until the modules can be refactored into a better DAG.
     *
     * @param r the Result to extract from
     * @return the ModuleNode if the Result is in fact a "GroovyParserResult",
     * the sources have been parsed, and the {@link ModuleNode ModuleNode} has
     * been set. If not set or the wrong "Result" time, then null will be
     * returned.
     */
    public static ModuleNode extractModuleNode(final Result r) {
        //below line long to show up properly in enumerations
        //TODO TestMethodUtil.extractModuleNode refactor "Groovy Support" and "Groovy Editor" to have a better DAG to remove need to use reflection to extract Groovy ModuleNode
        ModuleNode ret = null;
        try {
            //no need to test result type as it will have the method or it won't
            //and this makes it easier to test
            final Method getRE = r.getClass().getMethod(GPR_GET_ROOT_ELEMENT);
            final Object astRoot = getRE.invoke(r);
            if (astRoot != null) {
                final Method getMN = astRoot.getClass().getMethod(AST_GET_MODULE_NODE);
                final ModuleNode lmn = ModuleNode.class.cast(getMN.invoke(astRoot));
                ret = lmn;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            //push it up to the NB caller
            throw new RuntimeException("The given result doesn't appear to come from parsing a Groovy file.", e);
        }
        return ret;
    }

    /**
     * Given a root {@link ModuleNode ModuleNode}, finds and returns the
     * {@link ClassNode ClassNode} for the given line and column. It is possible
     * in some situations for this to be null.
     *
     * @param root the root ModuleNode
     * @param line the line
     * @param col the column
     * @return the ClassNode for the line and column (cursor)
     */
    public static ClassNode getClassNodeForLineAndColumn(final ModuleNode root,
            final int line,
            final int col) {
        ClassNode ret = null;
        if (root != null) {
            final List<ClassNode> classes = root.getClasses();
            for (ClassNode cn : classes) {
                if (isBetweenLinesAndColumns(cn.getLineNumber(), cn.getColumnNumber(),
                        cn.getLastLineNumber(), cn.getLastColumnNumber(),
                        line, col)) {
                    return cn;
                }
            }
        }
        return ret;
    }

    /**
     * Given a {@link ClassNode ClassNode}, finds and returns the
     * {@link MethodNode MethodNode} for the given line and column. It is
     * possible in some situations for this to be null.
     *
     * @param cn the ClassNode
     * @param line the line
     * @param col the column
     * @return the MethodNode for the line and column (cursor)
     */
    public static MethodNode getMethodNodeForLineAndColumn(final ClassNode cn,
            final int line,
            final int col) {
        MethodNode ret = null;
        if (cn != null) {
            final List<MethodNode> methods = cn.getMethods();
            for (MethodNode mn : methods) {
                if (isBetweenLinesAndColumns(mn.getLineNumber(), mn.getColumnNumber(),
                        mn.getLastLineNumber(), mn.getLastColumnNumber(),
                        line, col)) {
                    return mn;
                }
            }
        }
        return ret;
    }

    public static SingleMethod getTestMethod(final Document doc, final int cursor) {
        final AtomicReference<SingleMethod> sm = new AtomicReference<>();
        if (doc != null) {

            Source s = Source.create(doc);
            try {
                ParserManager.parseWhenScanFinished(Collections.<Source>singleton(s), new UserTask() {
                    @Override
                    public void run(ResultIterator rit) throws Exception {
                        Result r = rit.getParserResult();
                        //0:line, 1:column
                        final int[] lc = getLineAndColumn(doc.getText(0, doc.getLength()), cursor);
                        final int line = lc[0];
                        final int col = lc[1];
                        final ModuleNode root = extractModuleNode(r);
                        final ClassNode cn = getClassNodeForLineAndColumn(root, line, col);
                        final MethodNode mn = getMethodNodeForLineAndColumn(cn, line, col);
                        if (mn != null) {
                            final SingleMethod lsm = new SingleMethod(s.getFileObject(), mn.getName());
                            sm.set(lsm);
                        }
                    }

                });
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return sm.get();
    }

    static boolean canHandle(Node activatedNode) {
        FileObject fo = getFileObjectFromNode(activatedNode);
        if (fo != null) {
            if (!isGroovyFile(fo)) {
                return false;
            }

            final EditorCookie ec = activatedNode.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                final AtomicReference<Document> doc = new AtomicReference<>();
                final AtomicInteger dot = new AtomicInteger();
                try {
                    EventQueue.invokeAndWait(() -> {
                        final JEditorPane pane = NbDocument.findRecentEditorPane(ec);
                        if (pane != null) {
                            doc.set(pane.getDocument());
                            dot.set(pane.getCaret().getDot());
                        }
                    });
                } catch (InterruptedException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                if (doc.get() != null) {
                    SingleMethod sm = getTestMethod(doc.get(), dot.get());
                    if (sm != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static FileObject getFileObjectFromNode(Node node) {
        DataObject dO;
        DataFolder df;

        dO = node.getLookup().lookup(DataObject.class);
        if (null != dO) {
            return dO.getPrimaryFile();
        }

        df = node.getLookup().lookup(DataFolder.class);
        if (null != df) {
            return df.getPrimaryFile();
        }
        return null;
    }

    private static boolean isGroovyFile(FileObject fileObj) {
        final String ext = fileObj.getExt();
        final String mtype = FileUtil.getMIMEType(fileObj);
        return "groovy".equals(ext) || "text/x-groovy".equals(mtype); //NOI18N
    }
}
