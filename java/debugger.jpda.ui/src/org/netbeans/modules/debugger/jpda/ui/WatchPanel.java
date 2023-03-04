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
package org.netbeans.modules.debugger.jpda.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.tools.Diagnostic.Kind;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import org.netbeans.api.editor.DialogBinding;

import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.swing.border.Border;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import com.sun.source.tree.*;
import java.awt.Point;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.preprocessorbridge.spi.WrapperFactory;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 * A GUI panel for customizing a Watch.

 * @author Maros Sandor
 */
public class WatchPanel {

    private static final Logger logger = Logger.getLogger(WatchPanel.class.getName());

    private JPanel panel;
    private JEditorPane editorPane;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }
    
    public static void setupContext(final JEditorPane editorPane, final Runnable contextSetUp) {
        //EditorKit kit = CloneableEditorSupport.getEditorKit("text/x-java");
        //editorPane.setEditorKit(kit); - Do not set it, setupContext() will do the job.
        DebuggerEngine en = DebuggerManager.getDebuggerManager ().getCurrentEngine();
        if (EventQueue.isDispatchThread() && en != null) {
            RequestProcessor contextRetrievalRP = en.lookupFirst(null, RequestProcessor.class);
            if (contextRetrievalRP != null) {
                final DebuggerEngine den = en;
                contextRetrievalRP.post(new Runnable() {
                    @Override
                    public void run() {
                        final Context c = retrieveContext(den);
                        if (c != null) {
                            setupContext(editorPane, c.url, c.line, c.column, c.debugger);
                            if (contextSetUp != null) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        contextSetUp.run();
                                    }
                                });
                            }
                        }
                    }
                });
                Context c = retrieveContext(null);
                if (c != null) {
                    setupContext(editorPane, c.url, c.line, c.column, c.debugger);
                } else {
                    setupUI(editorPane);
                }
                return ;
            } else {
                en = null;
            }
        }
        Context c = retrieveContext(en);
        if (c != null) {
            setupContext(editorPane, c.url, c.line, c.column, c.debugger);
        } else {
            setupUI(editorPane);
        }
        if (contextSetUp != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    contextSetUp.run();
                }
            });
        }
    }

    private static Context retrieveContext(DebuggerEngine en) {
        CallStackFrame csf = null;
        JPDADebugger d = null;
        if (en != null) {
            d = en.lookupFirst(null, JPDADebugger.class);
            if (d != null) {
                csf = d.getCurrentCallStackFrame();
            }
        }
        boolean adjustContext = true;
        Context c;
        if (csf != null) {
            Session session = en.lookupFirst(null, Session.class);
            String language = session.getCurrentLanguage();
            SourcePath sp = en.lookupFirst(null, SourcePath.class);
            c = new Context();
            c.url = sp.getURL(csf, language);
            c.line = csf.getLineNumber(language);
            c.debugger = d;
            if (c.line > 0) {
                adjustContext = false;
            }
        } else {
            EditorContext context = EditorContextBridge.getContext();
            String url = context.getCurrentURL();
            if (url != null && url.length() > 0) {
                c = new Context();
                c.url = url;
                c.line = context.getCurrentLineNumber();
                c.debugger = d;
            } else {
                url = EditorContextDispatcher.getDefault().getMostRecentURLAsString();
                if (url != null && url.length() > 0) {
                    c = new Context();
                    c.url = url;
                    c.line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();
                    c.debugger = d;
                } else {
                    return null;
                }
            }
            c.column = getRecentColumn();
        }
        if (adjustContext && !EventQueue.isDispatchThread()) {
            // Do the adjustment only outside of AWT.
            // When in AWT, the context update in RP is spawned.
            adjustLine(c);
        }
        return c;
    }
    
    private static int getRecentColumn() {
        JEditorPane mostRecentEditor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        if (mostRecentEditor != null) {
            Caret caret = mostRecentEditor.getCaret();
            if (caret != null) {
                int offset = caret.getDot();
                try {
                    int rs = javax.swing.text.Utilities.getRowStart(mostRecentEditor, offset);
                    return offset - rs;
                } catch (BadLocationException blex) {}
            }
        }
        return 0;
    }

    private static void adjustLine(Context c) {
        if (c.line == -1) {
            c.line = 1;
        }
        URL url;
        try {
            url = new URL(c.url);
        } catch (MalformedURLException ex) {
            return ;
        }
        FileObject fo = URLMapper.findFileObject(url);
        if (fo == null) {
            return ;
        }
        if (!"java".equalsIgnoreCase(fo.getExt())) {
            // we do not understand other languages
            return ;
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
        } catch (FileNotFoundException ex) {
            return ;
        }
        try {
            int line = findClassLine(br);
            Point lc = findMethodLineColumn(line, c.column, br);
            if (c.line < lc.x) {
                c.line = lc.x;
                c.column = lc.y;
            }
        } catch (IOException ioex) {
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
            }
        }
    }

    private static int findClassLine(BufferedReader br) throws IOException {
        int l = 1;
        String line;
        boolean comment = false;
        boolean classDecl = false;
        for (; (line = br.readLine()) != null; l++) {
            if (classDecl) {
                if (line.indexOf('{') >= 0) {
                    return l + 1;
                } else {
                    continue;
                }
            }
            boolean slash = false;
            boolean asterix = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (comment) {
                    if (asterix && c == '/') {
                        comment = false;
                        asterix = false;
                        continue;
                    }
                    asterix = c == '*';
                    continue;
                }
                if (slash && c == '*') {
                    comment = true;
                    slash = false;
                    continue;
                }
                if (c == '/') {
                    if (slash) {
                        // comment, ignore the rest of the line
                        break;
                    }
                    slash = true;
                }
                if (c == 'c' && line.length() >= (i+"class".length()) && "lass".equals(line.substring(i+1, i+5))) {
                    // class declaration
                    classDecl = true;
                    if (line.indexOf('{', i+5) > 0) {
                        return l + 1;
                    }
                }
            }
        }
        return 1; // Did not find anything interesting
    }
    
    private static Point findMethodLineColumn(int l, int col, BufferedReader br) throws IOException {
        int origLine = l;
        String line;
        boolean isParenthesis = false;
        boolean isThrows = false;
        for (; (line = br.readLine()) != null; l++) {
            int i = 0;
            if (!isParenthesis && (i = line.indexOf(')')) >= 0 || isParenthesis) {
                isParenthesis = true;
                if (!isThrows) {
                    for (i++; i < line.length() && Character.isWhitespace(line.charAt(i)); i++) ;
                    if ((i+"throws".length()) < line.length() && "throws".equals(line.substring(i, i+"throws".length()))) {
                        isThrows = true;
                    }
                }
                if (isThrows) {
                    i = line.indexOf("{", i);
                    if (i < 0) {
                        i = line.length();
                    }
                }
                if (i < line.length()) {
                    if (line.charAt(i) == '{') {
                        return new Point(l, i+1);
                    } else {
                        isParenthesis = false;
                    }
                }
            }
        }
        return new Point(origLine, col);
    }

    public static void setupContext(final JEditorPane editorPane, String url, int line, int column) {
        setupContext(editorPane, url, line, column, null);
    }

    public static void setupContext(final JEditorPane editorPane, String url, final int line, final int column, final JPDADebugger debugger) {
        final FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
            if (file == null) {
                return;
            }
        } catch (MalformedURLException e) {
            // null dobj
            return;
        }
        //System.err.println("WatchPanel.setupContext("+file+", "+line+", "+offset+")");
        // Do the binding for Java files only:
        if ("text/x-java".equals(file.getMIMEType())) { // NOI18N
            Runnable bindComponentToDocument = new Runnable() {
                @Override
                public void run() {
                    String origText = editorPane.getText();
                    DialogBinding.bindComponentToFile(file, (line > 0) ? (line - 1) : 0, column, 0, editorPane);
                    Document editPaneDoc = editorPane.getDocument();
                    editPaneDoc.putProperty("org.netbeans.modules.editor.java.JavaCompletionProvider.skipAccessibilityCheck", "true");
                    editPaneDoc.putProperty(WrapperFactory.class,
                            debugger != null ? new MyWrapperFactory(debugger, file) : null);
                    editorPane.setText(origText);
                }
            };
            if (EventQueue.isDispatchThread()) {
                bindComponentToDocument.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(bindComponentToDocument);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        setupUI(editorPane);
    }
    
    private static void setupUI(final JEditorPane editorPane) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
                if (eui == null) {
                    return ;
                }
                editorPane.putClientProperty(
                    "HighlightsLayerExcludes", //NOI18N
                    "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
                );
                // Do not draw text limit line
                try {
                    java.lang.reflect.Field textLimitLineField = EditorUI.class.getDeclaredField("textLimitLineVisible"); // NOI18N
                    textLimitLineField.setAccessible(true);
                    textLimitLineField.set(eui, false);
                } catch (Exception ex) {}
                editorPane.repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public JComponent getPanel() {
        if (panel != null) {
            return panel;
        }

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString ("CTL_Watch_Name")); // NOI18N
        if (expression != null && expression.trim().length() == 0) {
            JEditorPane editor = EditorContextDispatcher.getDefault().getMostRecentEditor();
            if (editor != null && editor.getDocument() instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument) editor.getDocument();
                String selectedExpression = getSelectedIdentifier(doc, editor, editor.getCaret ().getDot ());
                if (selectedExpression != null) {
                    expression = selectedExpression;
                }
            }
        }
        JComponent [] editorComponents = Utilities.createSingleLineEditor("text/plain");
        JScrollPane sp = (JScrollPane) editorComponents[0];
        editorPane = (JEditorPane) editorComponents[1];

        int h = sp.getPreferredSize().height;
        int w = Math.min(70*editorPane.getFontMetrics(editorPane.getFont()).charWidth('a'),
                         org.openide.windows.WindowManager.getDefault().getMainWindow().getSize().width);
        sp.setPreferredSize(new Dimension(w, h));
        /*
        FontMetrics fm = editorPane.getFontMetrics(editorPane.getFont());
        int size = 2*fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent();
        Insets eInsets = editorPane.getInsets();
        Insets spInsets = sp.getInsets();
        sp.setPreferredSize(new Dimension(30*size,
                size +
                eInsets.bottom + eInsets.top +
                spInsets.bottom + spInsets.top));
        */
        textLabel.setBorder (new EmptyBorder (0, 0, 5, 0));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add (BorderLayout.NORTH, textLabel);
        panel.add (BorderLayout.CENTER, sp);
        
        editorPane.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        editorPane.setText (expression);
        editorPane.selectAll ();

        Runnable editorPaneUpdated = new Runnable() {
            @Override
            public void run() {
                editorPane.setText (expression);
                editorPane.selectAll ();
            }
        };
        setupContext(editorPane, editorPaneUpdated);

        textLabel.setLabelFor (editorPane);
        HelpCtx.setHelpIDString(editorPane, "debug.customize.watch");
        editorPane.requestFocus ();
        
        return panel;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }

    
    private static String getSelectedIdentifier (
        StyledDocument doc,
        JEditorPane ep,
        int offset
    ) {
        String t = null;
        if (ep.getSelectionStart () <= offset && offset <= ep.getSelectionEnd ()) {
            t = ep.getSelectedText ();
        }
        if (t != null) {
            return t;
        }

        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            javax.swing.text.Element lineElem =
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 &&
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen &&
                   Character.isJavaIdentifierPart(t.charAt(identEnd))
            ) {
                identEnd++;
            }

            if (identStart == identEnd) {
                return null;
            }
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException e) {
            return null;
        }
    }


    private static final class Context {
        public String url;
        public int line;
        public int column;
        public JPDADebugger debugger;
    }

    private static class MyWrapperFactory implements WrapperFactory {

        private WeakReference<JPDADebugger> debuggerRef;
        private FileObject fileObject;

        public MyWrapperFactory(JPDADebugger debugger, FileObject file) {
            debuggerRef = new WeakReference(debugger);
            this.fileObject = file;
        }

        private CompilationController findController(FileObject fileObj) {
            JavaSource javaSource = JavaSource.forFileObject(fileObj);
            if (javaSource == null) {
                return null;
            }
            final CompilationController[] result = new CompilationController[1];
            result[0] = null;
            try {
                javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                    }
                    @Override
                    public void run(CompilationController ci) throws Exception {
                        if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                            ErrorManager.getDefault().log(ErrorManager.WARNING,
                                    "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                    "\nDiagnostics = "+ci.getDiagnostics()+
                                    "\nFree memory = "+Runtime.getRuntime().freeMemory());
                            return;
                        }
                        result[0] = ci;
                    }
                }, true);
            } catch (IOException ioex) {
                ErrorManager.getDefault().notify(ioex);
                return null;
            }
            return result[0];
        }

        @Override
        public Trees wrapTrees(Trees trees) {
            JPDADebugger debugger = debuggerRef.get();
            if (debugger == null) {
                return trees;
            }
            return new MyTrees(trees, findController(fileObject), debugger);
        }

    }

    private static class MyTrees extends Trees {

        Trees trees;
        private CompilationController controller;
        private JPDADebugger debugger;

        MyTrees(Trees trees, CompilationController controller, JPDADebugger debugger) {
            this.trees = trees;
            this.controller = controller;
            this.debugger = debugger;
        }

        @Override
        public SourcePositions getSourcePositions() {
            return trees.getSourcePositions();
        }

        @Override
        public Tree getTree(Element arg0) {
            return trees.getTree(arg0);
        }

        @Override
        public ClassTree getTree(TypeElement arg0) {
            return trees.getTree(arg0);
        }

        @Override
        public MethodTree getTree(ExecutableElement arg0) {
            return trees.getTree(arg0);
        }

        @Override
        public Tree getTree(Element arg0, AnnotationMirror arg1) {
            return trees.getTree(arg0, arg1);
        }

        @Override
        public Tree getTree(Element arg0, AnnotationMirror arg1, AnnotationValue arg2) {
            return trees.getTree(arg0, arg1, arg2);
        }

        @Override
        public TreePath getPath(CompilationUnitTree arg0, Tree arg1) {
            return trees.getPath(arg0, arg1);
        }

        @Override
        public TreePath getPath(Element arg0) {
            return trees.getPath(arg0);
        }

        @Override
        public TreePath getPath(Element arg0, AnnotationMirror arg1) {
            return trees.getPath(arg0, arg1);
        }

        @Override
        public TreePath getPath(Element arg0, AnnotationMirror arg1, AnnotationValue arg2) {
            return trees.getPath(arg0, arg1, arg2);
        }

        @Override
        public Element getElement(TreePath arg0) {
            return trees.getElement(arg0);
        }

        @Override
        public TypeMirror getTypeMirror(TreePath arg0) {
            Tree tree = arg0.getLeaf();
            if (tree.getKind() == Tree.Kind.IDENTIFIER) {
                Map<String, ObjectVariable> map = null;
                try {
                    // [TODO] add JPDADebuggerImpl.getAllLabels() to API
                    Method method = debugger.getClass().getMethod("getAllLabels"); // NOI18N
                    map = (Map<String, ObjectVariable>) method.invoke(debugger);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (map != null) {
                    String name = ((IdentifierTree)tree).getName().toString();
                    ObjectVariable var = map.get(name);
                    if (var != null) {
                        Elements elements = controller.getElements();
                        TypeElement typeElem = elements.getTypeElement(var.getClassType().getName());
                        if (typeElem != null) {
                            return typeElem.asType();
                        }
                    }
                }
            }
            return trees.getTypeMirror(arg0);
        }

        @Override
        public Scope getScope(TreePath arg0) {
            return trees.getScope(arg0);
        }

        @Override
        public boolean isAccessible(Scope arg0, TypeElement arg1) {
            return trees.isAccessible(arg0, arg1);
        }

        @Override
        public boolean isAccessible(Scope arg0, Element arg1, DeclaredType arg2) {
            return trees.isAccessible(arg0, arg1, arg2);
        }

        @Override
        public TypeMirror getOriginalType(ErrorType arg0) {
            return trees.getOriginalType(arg0);
        }

        @Override
        public void printMessage(Kind arg0, CharSequence arg1, Tree arg2, CompilationUnitTree arg3) {
            trees.printMessage(arg0, arg1, arg2, arg3);
        }

        @Override
        public String getDocComment(TreePath path) {
            return trees.getDocComment(path);
        }

        @Override
        public TypeMirror getLub(CatchTree tree) {
            return trees.getLub(tree);
        }
    }

    public static final class DelegatingBorder implements Border {

        private Border delegate;
        private Insets insets;// = new Insets(1, 1, 1, 1);

        public DelegatingBorder(Border delegate, Insets insets) {
            this.delegate = delegate;
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            //logger.fine("Delegate paintBorder("+c+", "+g+", "+x+", "+y+", "+width+", "+height+")");
            delegate.paintBorder(c, g, x, y, width, height);
        }

        public Insets getInsets() {
            return insets;
        }

        public void setInsets(Insets insets) {
            this.insets = insets;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            //logger.fine("Delegate getBorderInsets() = "+delegate.getBorderInsets(c));
            //Insets insets = delegate.getBorderInsets(c);
            //insets.top = 0;
            //insets.bottom = 0;
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return delegate.isBorderOpaque();
        }

    }

}
