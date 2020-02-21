/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.trace;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CppParserActionImpl;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 */
public class TraceFactory {

    public static TraceWriter getTraceWriter(Object parser) {
        if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
            return new SimpleTraceWriter(parser);
        } else {
            if (TraceFlags.TRACE_CPP_PARSER_SHOW_AST) {
                return new TreeTraceWriter(parser);
            } else {
                return new OutputTraceWriter(parser);
            }
        }
    }

    public interface TraceWriter {
        void printIn(String message, Token... token);
        void printOut(String message, Token... token);
        void print(String message, Token... token);
    }

    private static class SimpleTraceWriter implements TraceWriter {

        private final LinkedList<String> stack = new LinkedList<>();
        private int level = 0;
        protected final CppParserActionImpl parser;

        private SimpleTraceWriter(Object parser) {
            if (parser instanceof CppParserActionImpl) {
                this.parser = (CppParserActionImpl) parser;
            } else {
                this.parser = null;
            }
        }

        protected void println(String s) {
            System.out.println(s);
        }

        @Override
        public void printIn(String message, Token... token) {
            stack.addLast(message);
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append('>'); //NOI18N
            buf.append(message);
            int backTracking = 0;
            CharSequence path = ""; //NOI18N
            if (parser != null) {
                backTracking = parser.getBacktrackingLevel();
                path = parser.getCurrentFile().getAbsolutePath();
            }
            if (backTracking != 0) {
                buf.append(" GUESSING LEVEL = "); //NOI18N
                buf.append(Integer.toString(backTracking));
            }
            if (token.length > 0) {
                buf.append(' '); //NOI18N
                buf.append(path);
                if (!APTUtils.isEOF(token[0])) {
                    buf.append('['); //NOI18N
                    buf.append(Integer.toString(token[0].getLine()));
                    buf.append(','); //NOI18N
                    buf.append(Integer.toString(token[0].getColumn()));
                    buf.append(']'); //NOI18N
                }
                for (int j = 0; j < token.length; j++) {
                    buf.append(' '); //NOI18N
                    buf.append(token[j]);
                }
            }
            println(buf.toString());
            level++;
        }

        @Override
        public void printOut(String message, Token... token) {
            String top = stack.removeLast();
            if (!message.equals(top)) {
                println("UNBALANCED exit. Actual " + message + " Expected " + top);//NOI18N
            }
            level--;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append('<'); //NOI18N
            buf.append(message);
            int backTracking = 0;
            CharSequence path = ""; //NOI18N
            if (parser != null) {
                backTracking = parser.getBacktrackingLevel();
                path = parser.getCurrentFile().getAbsolutePath();
            }
            if (backTracking != 0) {
                buf.append(" GUESSING LEVEL = "); //NOI18N
                buf.append(Integer.toString(backTracking));
            }
            if (token.length > 0) {
                buf.append(' '); //NOI18N
                buf.append(path);
                if (!APTUtils.isEOF(token[0])) {
                    buf.append('['); //NOI18N
                    buf.append(Integer.toString(token[0].getLine()));
                    buf.append(','); //NOI18N
                    buf.append(Integer.toString(token[0].getColumn()));
                    buf.append(']'); //NOI18N
                    buf.append(' '); //NOI18N
                }
                for (int j = 0; j < token.length; j++) {
                    buf.append(' '); //NOI18N
                    buf.append(token[j]);
                }
            }
            println(buf.toString());
        }

        @Override
        public void print(String message, Token... token) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append(' '); //NOI18N
            buf.append(message);
            int backTracking = 0;
            CharSequence path = ""; //NOI18N
            if (parser != null) {
                backTracking = parser.getBacktrackingLevel();
                path = parser.getCurrentFile().getAbsolutePath();
            }
            if (backTracking != 0) {
                buf.append(" GUESSING LEVEL = "); //NOI18N
                buf.append(Integer.toString(backTracking));
            }
            if (token.length > 0) {
                buf.append(' '); //NOI18N
                buf.append(path);
                if (!APTUtils.isEOF(token[0])) {
                    buf.append('['); //NOI18N
                    buf.append(Integer.toString(token[0].getLine()));
                    buf.append(','); //NOI18N
                    buf.append(Integer.toString(token[0].getColumn()));
                    buf.append(']'); //NOI18N
                    buf.append(' '); //NOI18N
                }
                for (int j = 0; j < token.length; j++) {
                    buf.append(' '); //NOI18N
                    buf.append(token[j]);
                }
            }
            println(buf.toString());
        }
    }

    private static class OutputTraceWriter extends SimpleTraceWriter {

        private final InputOutput io;
        private final OutputWriter out;

        private OutputTraceWriter(Object parser) {
            super(parser);
            final CsmFile mainFile;
            if (parser instanceof CppParserActionImpl) {
                mainFile = ((CppParserActionImpl) parser).getMainFile();
            } else {
                mainFile = null;
            }
            if (mainFile == null) {
                io = IOProvider.getDefault().getIO("Trace actions", false); // NOI18N
            } else {
                io = IOProvider.getDefault().getIO("Trace actions "+ CndPathUtilities.getBaseName(mainFile.getAbsolutePath().toString()), false); // NOI18N
            }
            io.select();
            out = io.getOut();
            //out.close();
        }

        @Override
        protected void println(String s) {
            out.println(s);
        }
    }

    private static final class TreeTraceWriter extends OutputTraceWriter {

        private final MyTreeNode root;
        private ASTFrame tree;
        private volatile MyTreeNode current;

        private TreeTraceWriter(Object parser) {
            super(parser);
            final CsmFile mainFile;
            if (parser instanceof CppParserActionImpl) {
                mainFile = ((CppParserActionImpl) parser).getMainFile();
            } else {
                mainFile = null;
            }
            root = new MyTreeNode("root", null); // NOI18N
            current = root;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (mainFile == null) {
                        tree = new ASTFrame("AST", root); // NOI18N
                    } else {
                        tree = new ASTFrame(CndPathUtilities.getBaseName(mainFile.getAbsolutePath().toString()), root); // NOI18N
                    }
                    tree.setVisible(true);
                }
            });
        }

        @Override
        public void printIn(String message, Token... token) {
            super.printIn(message, token);
            if (current != null) {
                CsmFile currentFile = null;
                if (parser != null) {
                    currentFile = parser.getCurrentFile();
                }
                MyTreeNode node = new MyTreeNode(message, currentFile, token);
                current.addNode(node);
                current = node;
            }
        }

        @Override
        public void printOut(String message, Token... token) {
            super.printOut(message, token);
            if (current != null) {
                current = (MyTreeNode) current.getParent();
                if (current != null) {
                    CsmFile currentFile = null;
                    if (parser != null) {
                        currentFile = parser.getCurrentFile();
                    }
                    MyTreeNode node = new MyTreeNode(message, currentFile, token);
                    current.addNode(node);
                }
            }
        }

        @Override
        public void print(String message, Token... token) {
            super.print(message, token);
            if (current != null) {
                CsmFile currentFile = null;
                if (parser != null) {
                    currentFile = parser.getCurrentFile();
                }
                MyTreeNode node = new MyTreeNode(message, currentFile, token);
                current.addNode(node);
            }
        }
    }

    private static final class MyTreeNode extends DefaultMutableTreeNode {

        private final String message;
        private final Token[] token;
        private final CsmFile currentFile;

        private MyTreeNode(String message, CsmFile currentFile, Token... token) {
            this.message = message;
            this.token = token;
            this.currentFile = currentFile;
        }

        private void addNode(MyTreeNode node) {
            insert(node, getChildCount());
        }

        @Override
        public String toString() {
            return getUserObject().toString();
        }

        @Override
        public Object getUserObject() {
            StringBuilder buf = new StringBuilder(message);
            if (currentFile != null) {
                buf.append(" ").append(PathUtilities.getBaseName(currentFile.getAbsolutePath().toString())); // NOI18N
                if (token.length > 0 && !APTUtils.isEOF(token[0])) {
                    buf.append("[").append(token[0].getLine()).append(':').append(token[0].getColumn()).append("]"); // NOI18N
                }
            }
            
            if (token.length > 0) {
                buf.append(' '); //NOI18N
                buf.append(token[0]);
            }
            return buf.toString();
        }
    }

    private static class ASTFrame extends JFrame {
        private final JTree tree;
        private final JTextArea text;

        private ASTFrame(String lab, MyTreeNode root) {
            super(lab);
            tree = new JTree(root, true);
            tree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
            TreeSelectionListener listener = new MyTreeSelectionListener();
            tree.addTreeSelectionListener(listener);
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            JScrollPane treeScroller = new JScrollPane(tree);
            Container content = getContentPane();
            content.setLayout(new BorderLayout());

            text = new JTextArea() {
                @Override
                public Insets getInsets() {
                    return new Insets(6, 6, 6, 6);
                }
            };
            text.setEditable(false);
            text.setTabSize(4);
            JScrollPane textScroller = new JScrollPane(text);

            JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitter.setLeftComponent(treeScroller);
            splitter.setRightComponent(textScroller);
            splitter.setDividerSize(2);
            splitter.setResizeWeight(0.6);

            content.add(splitter, BorderLayout.CENTER);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Frame f = (Frame) e.getSource();
                    f.setVisible(false);
                    f.dispose();
                }
            });
            setSize(320, 480);
        }
        
        private class MyTreeSelectionListener implements TreeSelectionListener {

            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TreePath path = event.getPath();
                MyTreeNode ast = (MyTreeNode) path.getLastPathComponent();
                StringBuilder buf = new StringBuilder();
                buf.append("name:\t").append(ast.message); // NOI18N
                if (ast.currentFile != null) {
                    buf.append("\nfile:\t").append(ast.currentFile.getAbsolutePath()); // NOI18N
                    if (!APTUtils.isEOF(ast.token[0])) {
                        buf.append("\npos:\t").append(ast.token[0].getLine()).append(':').append(ast.token[0].getColumn()); // NOI18N
                    }
                }
                for(int i = 0; i < ast.token.length; i++) {
                    buf.append("\ntoken:\t").append(ast.token[i]); // NOI18N
                }
                displayText(buf.toString());
            }
        }

        private void displayText(String s) {
            text.setText(s);
        }
    }
}
