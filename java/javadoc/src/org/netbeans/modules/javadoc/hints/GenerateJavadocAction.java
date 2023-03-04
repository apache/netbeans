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

package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.TextAction;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * The action supposed to fix an empty javadoc block.
 * The present use case is to complete javadoc generated on <code>/** + &lt;ENTER&gt;</code>.
 * See org.netbeans.modules.editor.java.JavaKit.JavaInsertBreakAction
 * 
 * <p><b>Note:</b> The text action is a temporary solution until the editor
 * introduce some SPI to plug.</p>
 * 
 * @author Jan Pokorsky
 */
public final class GenerateJavadocAction extends TextAction {

    GenerateJavadocAction() {
        super("fix-javadoc-action"); // NOI18N
    }
    
    public static GenerateJavadocAction create() {
        return new GenerateJavadocAction();
    }

    public void actionPerformed(ActionEvent e) {
        final JTextComponent jtc = getTextComponent(e);
        final Document doc = jtc.getDocument();

        if (!(doc instanceof StyledDocument)) {
            // unsupported document
            return;
        }
        
        try {
            final Descriptor desc = new Descriptor(doc.createPosition(jtc.getCaretPosition()));

            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (prepareGenerating(doc, desc)) {
                            // add javadoc content
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        generate(doc, desc, jtc);
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            });
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private boolean prepareGenerating(final Document doc, final Descriptor desc) throws IOException {
        JavaSource js = JavaSource.forDocument(doc);
        if (js == null) {
            return false;
        }

        FileObject file = js.getFileObjects().iterator().next();
        SourceVersion sv = JavadocUtilities.resolveSourceVersion(file);
        final JavadocGenerator gen = new JavadocGenerator(sv);
        gen.updateSettings(file);
        
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TokenHierarchy tokens = javac.getTokenHierarchy();
                TokenSequence ts = tokens.tokenSequence();
                ts.move(desc.caret.getOffset());
                if (!ts.moveNext() || ts.token().id() != JavaTokenId.JAVADOC_COMMENT) {
                    return;
                }
                
                desc.caret = doc.createPosition(desc.caret.getOffset());
                final int jdBeginOffset = ts.offset();
                int offsetBehindJavadoc = ts.offset() + ts.token().length();
                
                while (ts.moveNext()) {
                    TokenId tid = ts.token().id();
                    if (tid != JavaTokenId.WHITESPACE && tid != JavaTokenId.LINE_COMMENT && tid != JavaTokenId.BLOCK_COMMENT) {
                        offsetBehindJavadoc = ts.offset();
                        // it is magic for TreeUtilities.pathFor
                        ++offsetBehindJavadoc;
                        break;
                    }
                }
                
                TreePath tp = javac.getTreeUtilities().pathFor(offsetBehindJavadoc);
                Tree leaf = tp.getLeaf();
                Kind kind = leaf.getKind();
                SourcePositions positions = javac.getTrees().getSourcePositions();
                
                while (!TreeUtilities.CLASS_TREE_KINDS.contains(kind) && kind != Kind.METHOD && kind != Kind.VARIABLE && kind != Kind.COMPILATION_UNIT) {
                    tp = tp.getParentPath();
                    if (tp == null) {
                        leaf = null;
                        kind = null;
                        break;
                    }
                    leaf = tp.getLeaf();
                    kind = leaf.getKind();
                }
                
                if (leaf == null || positions.getStartPosition(javac.getCompilationUnit(), leaf) < jdBeginOffset) {
                    // not a class member javadoc -> ignore
                    return;
                }
                
                if (kind != Kind.COMPILATION_UNIT && !JavadocUtilities.hasErrors(leaf) /*&& Access.PRIVATE.isAccessible(javac, tp, true)*/) {
                    Element el = javac.getTrees().getElement(tp);
                    if (el != null) {
                        String javadoc = gen.generateComment(el, javac);
                        if(javadoc == null) {
                            return;
                        }
                        desc.javadoc = javadoc;
                    }
                }
            }
            
        }, true);
        
        return desc.javadoc != null;
    }

    private void generate(final Document doc, final Descriptor desc, final JTextComponent jtc) throws BadLocationException {
        final Indent ie = Indent.get(doc);
        try {
            ie.lock();
            NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {

                public void run() {
                    try {
                        int caretPos = jtc.getCaretPosition();
                        generateJavadoc(doc, desc, ie);
                        // move caret
                        jtc.setCaretPosition(caretPos);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            });
        } finally {
            ie.unlock();
        }
    }

    private void generateJavadoc(Document doc, Descriptor desc, Indent ie) throws BadLocationException {
        // skip javadoc header /** and tail */ since they were already generated
        String content = desc.javadoc;
        
        if(content.endsWith("\n")) {
            content = content.substring(0, content.length() - "\n".length());
        }
        
        if (content.length() == 0) {
            return;
        }
        
        Position pos = desc.caret;
        int startOffset = pos.getOffset();
        doc.insertString(startOffset, content, null);
        
        if (startOffset != pos.getOffset()) {
            ie.reindent(startOffset + 1, pos.getOffset());
        }

    }
    
    private static final class Descriptor {
        String javadoc;
        /** position inside javadoc where to write */
        Position caret;

        public Descriptor(Position caret) {
            this.caret = caret;
        }
    }

}
