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
package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
final class JavaMoveCodeElementAction extends BaseAction {

    private boolean downward;

    public JavaMoveCodeElementAction(String name, boolean downward) {
        super(name);
        this.downward = downward;
    }

    @Override
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        if (target == null || !target.isEditable() || !target.isEnabled()) {
            target.getToolkit().beep();
            return;
        }
        final BaseDocument doc = (BaseDocument) target.getDocument();
        final JavaSource js = JavaSource.forDocument(doc);
        if (js != null) {
            final AtomicBoolean cancel = new AtomicBoolean();
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        js.runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(final CompilationController controller) throws Exception {
                                try {
                                    if (cancel.get()) {
                                        return;
                                    }
                                    controller.toPhase(JavaSource.Phase.PARSED);

                                    if (cancel.get()) {
                                        return;
                                    }
                                    final Indent indent = Indent.get(doc);
                                    indent.lock();
                                    try {
                                        doc.runAtomicAsUser(new Runnable() {
                                            @Override
                                            public void run() {
                                                DocumentUtilities.setTypingModification(doc, true);
                                                try {
                                                    int[] currentBounds = {target.getSelectionStart(), target.getSelectionEnd()};
                                                    TreePath tp = widenToElementBounds(controller, doc, currentBounds);
                                                    if (tp == null) {
                                                        target.getToolkit().beep();
                                                        return;
                                                    }
                                                    boolean insideBlock = tp.getLeaf().getKind() == Tree.Kind.BLOCK;
                                                    if (downward) {
                                                        int destinationOffset = findDestinationOffset(controller, doc, currentBounds[1], insideBlock);
                                                        if (destinationOffset < 0) {
                                                            target.getToolkit().beep();
                                                            return;
                                                        }
                                                        String text = doc.getText(currentBounds[1], destinationOffset - currentBounds[1]);
                                                        doc.remove(currentBounds[1], text.length());
                                                        doc.insertString(currentBounds[0], text, null);
                                                        indent.reindent(currentBounds[0] + text.length(), currentBounds[1] + text.length());
                                                    } else {
                                                        int destinationOffset = findDestinationOffset(controller, doc, currentBounds[0] - 1, insideBlock);
                                                        if (destinationOffset < 0) {
                                                            target.getToolkit().beep();
                                                            return;
                                                        }
                                                        String text = doc.getText(destinationOffset, currentBounds[0] - destinationOffset);
                                                        doc.insertString(currentBounds[1], text, null);
                                                        doc.remove(destinationOffset, text.length());
                                                        indent.reindent(destinationOffset, currentBounds[1] - text.length());
                                                    }
                                                } catch (BadLocationException ble) {
                                                    target.getToolkit().beep();
                                                } finally {
                                                    DocumentUtilities.setTypingModification(doc, false);
                                                }
                                            }
                                        });
                                    } finally {
                                        indent.unlock();
                                    }
                                } catch (IOException ioe) {
                                    target.getToolkit().beep();
                                }
                            }
                        }, true);
                    } catch (IOException ioe) {
                        target.getToolkit().beep();
                    }
                }
            }, getShortDescription(), cancel, false);
        }
    }

    private TreePath widenToElementBounds(CompilationInfo cInfo, BaseDocument doc, int[] bounds) {
        SourcePositions sp = cInfo.getTrees().getSourcePositions();
        TreeUtilities tu = cInfo.getTreeUtilities();
        int startOffset = getLineStart(doc, bounds[0]);
        int endOffset = getLineEnd(doc, bounds[1]);
        while (true) {
            TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(cInfo.getTokenHierarchy(), startOffset);
            if (ts != null && (ts.moveNext() || ts.movePrevious())) {
                if (ts.offset() < startOffset && ts.token().id() != JavaTokenId.WHITESPACE) {
                    startOffset = getLineStart(doc, ts.offset());
                    continue;
                }
            }
            ts = SourceUtils.getJavaTokenSequence(cInfo.getTokenHierarchy(), endOffset);
            if (ts != null && (ts.moveNext() || ts.movePrevious())) {
                if (ts.offset() < endOffset && ts.token().id() != JavaTokenId.WHITESPACE) {
                    endOffset = getLineEnd(doc, ts.offset() + ts.token().length());
                    continue;
                }
            }
            boolean finish = true;
            TreePath tp = tu.pathFor(startOffset);
            while (tp != null) {
                Tree leaf = tp.getLeaf();
                List<? extends Tree> children = null;
                switch (leaf.getKind()) {
                    case BLOCK:
                        if (endOffset < sp.getEndPosition(tp.getCompilationUnit(), leaf)) {
                            children = ((BlockTree) leaf).getStatements();
                        }
                        break;
                    case CLASS:
                    case INTERFACE:
                    case ANNOTATION_TYPE:
                    case ENUM:
                        if (endOffset < sp.getEndPosition(tp.getCompilationUnit(), leaf)) {
                            children = ((ClassTree) leaf).getMembers();
                        }
                        break;
                }
                if (children != null) {
                    for (Tree tree : children) {
                        int startPos = (int) sp.getStartPosition(tp.getCompilationUnit(), tree);
                        int endPos = (int) sp.getEndPosition(tp.getCompilationUnit(), tree);
                        if (endPos > startOffset) {
                            if (startPos < startOffset) {
                                startOffset = getLineStart(doc, startPos);
                                finish = false;
                            }
                            if (startPos < endOffset && endOffset < endPos) {
                                endOffset = getLineEnd(doc, endPos);
                                finish = false;
                            }
                        }
                    }
                    break;
                }
                tp = tp.getParentPath();
            }
            if (finish) {
                bounds[0] = startOffset;
                bounds[1] = endOffset;
                return tp;
            }
        }
    }

    private int findDestinationOffset(CompilationInfo cInfo, BaseDocument doc, int offset, boolean insideBlock) {
        TreeUtilities tu = cInfo.getTreeUtilities();
        SourcePositions sp = cInfo.getTrees().getSourcePositions();
        while (true) {
            if (offset < 0 || offset > doc.getLength()) {
                return -1;
            }
            int destinationOffset = downward ? getLineEnd(doc, offset) : getLineStart(doc, offset);
            if (doc instanceof GuardedDocument && ((GuardedDocument)doc).isPosGuarded(destinationOffset)) {
                return -1;
            }
            if (destinationOffset < doc.getLength()) {
                TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(cInfo.getTokenHierarchy(), destinationOffset);
                if (ts != null && (ts.moveNext() || ts.movePrevious())) {
                    if (ts.offset() < destinationOffset && ts.token().id() != JavaTokenId.WHITESPACE) {
                        offset = downward ? ts.offset() + ts.token().length() : ts.offset();
                        continue;
                    }
                }
            }
            TreePath destinationPath = tu.pathFor(destinationOffset);
            Tree leaf = destinationPath.getLeaf();
            if (insideBlock) {
                switch (leaf.getKind()) {
                    case COMPILATION_UNIT:
                        return -1;
                    case BLOCK:
                        return destinationOffset;
                    case IF:
                    case FOR_LOOP:
                    case ENHANCED_FOR_LOOP:
                    case WHILE_LOOP:
                    case DO_WHILE_LOOP:
                    case SWITCH:
                    case CASE:
                    case SYNCHRONIZED:
                    case TRY:
                    case CATCH:
                        offset = destinationOffset + (downward ? 1 : -1);
                        break;
                    default:
                        offset = downward
                                ? (int) sp.getEndPosition(destinationPath.getCompilationUnit(), leaf)
                                : (int) sp.getStartPosition(destinationPath.getCompilationUnit(), leaf);
                }
            } else {
                switch (leaf.getKind()) {
                    case COMPILATION_UNIT:
                        return -1;
                    case CLASS:
                    case INTERFACE:
                    case ANNOTATION_TYPE:
                    case ENUM:
                        return destinationOffset;
                    default:
                        offset = downward
                                ? (int) sp.getEndPosition(destinationPath.getCompilationUnit(), leaf)
                                : (int) sp.getStartPosition(destinationPath.getCompilationUnit(), leaf);
                }
            }
        }
    }

    private int getLineStart(BaseDocument doc, int offset) {
        Element rootElement = doc.getDefaultRootElement();
        int lineNumber = rootElement.getElementIndex(offset);
        return lineNumber < 0 ? lineNumber : rootElement.getElement(lineNumber).getStartOffset();
    }

    private int getLineEnd(BaseDocument doc, int offset) {
        Element rootElement = doc.getDefaultRootElement();
        int lineNumber = rootElement.getElementIndex(offset);
        return lineNumber < 0 ? lineNumber : rootElement.getElement(lineNumber).getEndOffset();
    }

    private String getShortDescription() {
        String name = (String) getValue(Action.NAME);
        if (name != null) {
            try {
                return NbBundle.getMessage(JavaMoveCodeElementAction.class, name);
            } catch (MissingResourceException mre) {
            }
        }
        return name;
    }
}
