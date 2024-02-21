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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Code selection according to syntax tree. It also supports JavaDoc.
 *
 * @author Miloslav Metelka, Jan Pokorsky
 */
final class SelectCodeElementAction extends BaseAction {

    private boolean selectNext;

    /**
     * Construct new action that selects next/previous code elements
     * according to the language model.
     * <br>
     *
     * @param name name of the action (should be one of
     *  <br>
     *  <code>JavaKit.selectNextElementAction</code>
     *  <code>JavaKit.selectPreviousElementAction</code>
     * @param selectNext <code>true</code> if the next element should be selected.
     *  <code>False</code> if the previous element should be selected.
     */
    public SelectCodeElementAction(String name, boolean selectNext) {
        super(name);
        this.selectNext = selectNext;
        String desc = getShortDescription();
        if (desc != null) {
            putValue(SHORT_DESCRIPTION, desc);
        }
    }

    public String getShortDescription(){
        String name = (String)getValue(Action.NAME);
        if (name == null) return null;
        String shortDesc;
        try {
            shortDesc = NbBundle.getBundle(JavaKit.class).getString(name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            int selectionStartOffset = target.getSelectionStart();
            int selectionEndOffset = target.getSelectionEnd();
            if (selectionEndOffset > selectionStartOffset || selectNext) {
                SelectionHandler handler = (SelectionHandler)target.getClientProperty(SelectionHandler.class);
                if (handler == null) {
                    handler = new SelectionHandler(target, getShortDescription());
                    target.addCaretListener(handler);
                    // No need to remove the listener above as the handler
                    // is stored is the client-property of the component itself
                    target.putClientProperty(SelectionHandler.class, handler);
                }

                if (selectNext) { // select next element
                    handler.selectNext();
                } else { // select previous
                    handler.selectPrevious();
                }
            }
        }
    }

    private static final class SelectionHandler implements CaretListener, Runnable {

        private final JTextComponent target;
        private final String name;
        //@GuardedBy("this")
        private SelectionInfo[] selectionInfos;
        //@GuardedBy("this")
        private int selIndex = -1;
        //Threading: Confinement within EDT
        private boolean ignoreNextCaretUpdate;

        SelectionHandler(JTextComponent target, String name) {
            this.target = target;
            this.name = name;
        }

        public void selectNext() {
            SelectionInfo[] si;
            synchronized (this) {
                si = selectionInfos;
            }
            if (si == null) {
                final JavaSource js = JavaSource.forDocument(target.getDocument());
                if (js != null) {
                    final AtomicBoolean cancel = new AtomicBoolean();
                    ProgressUtils.runOffEventDispatchThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                js.runUserActionTask(
                                    new Task<CompilationController>(){
                                        @Override
                                        public void run(CompilationController cc) throws Exception {
                                            try {
                                                if (cancel.get()) {
                                                    return;
                                                }
                                                cc.toPhase(Phase.RESOLVED);
                                                if (cancel.get()) {
                                                    return;
                                                }
                                                synchronized (SelectionHandler.this) {
                                                    selectionInfos = initSelectionPath(target, cc);
                                                    if (selectionInfos != null && selectionInfos.length > 0) {
                                                        selIndex = 0;
                                                    }
                                                }
                                            } catch (IOException ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        }
                                    },
                                    true);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }, name, cancel, false);
                }
            }
            run();
        }

        public synchronized void selectPrevious() {
            if (selIndex > 0) {
                select(selectionInfos[--selIndex]);
            }
        }

        private void select(SelectionInfo selectionInfo) {
            assert SwingUtilities.isEventDispatchThread();
            Caret caret = target.getCaret();
            ignoreNextCaretUpdate = true;
            try {
                caret.setDot(selectionInfo.getEndOffset());
                caret.moveDot(selectionInfo.getStartOffset());
            } finally {
                ignoreNextCaretUpdate = false;
            }
        }

        public void caretUpdate(CaretEvent e) {
            assert SwingUtilities.isEventDispatchThread();
            if (!ignoreNextCaretUpdate) {
                synchronized (this) {
                    selectionInfos = null;
                    selIndex = -1;
                }
            }
        }

        private SelectionInfo[] initSelectionPath(JTextComponent target, CompilationController ci) {
            List<SelectionInfo> positions = new ArrayList<SelectionInfo>();
            int caretPos = target.getCaretPosition();
            positions.add(new SelectionInfo(caretPos, caretPos));
            if (target.getDocument() instanceof BaseDocument) {
                try {
                    //as a nice side effect it also supports word selection within string literals
                    //"foo b|ar" -> bar
                    int block[] = org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) target.getDocument(), caretPos);
                    if (block != null) {
                        positions.add(new SelectionInfo(block[0], block[1]));
                    }
                } catch (BadLocationException ble) {}
            }
            SourcePositions sp = ci.getTrees().getSourcePositions();
	    final TreeUtilities treeUtilities = ci.getTreeUtilities();
            TreePath tp = treeUtilities.pathFor(caretPos);
            for (Tree tree: tp) {
                int startPos = (int)sp.getStartPosition(tp.getCompilationUnit(), tree);
                int endPos = (int)sp.getEndPosition(tp.getCompilationUnit(), tree);
                positions.add(new SelectionInfo(startPos, endPos));

                //support content selection within the string literal too
                //"A|BC" -> ABC
                if (tree.getKind() == Tree.Kind.STRING_LITERAL) {
                    positions.add(new SelectionInfo(startPos + 1, endPos - 1));
                }
                //support content selection within the {}-block too
                //{A|BC} -> ABC
                if (tree.getKind() == Tree.Kind.BLOCK) {
                    positions.add(new SelectionInfo(startPos + 1, endPos - 1));
                }

		//Support selection of JavaDoc
		int docBegin = Integer.MAX_VALUE;
                for (Comment comment : treeUtilities.getComments(tree, true)) {
                    docBegin = Math.min(comment.pos(), docBegin);
                }
		int docEnd = Integer.MIN_VALUE;
                for (Comment comment : treeUtilities.getComments(tree, false)) {
                    docEnd = Math.max(comment.endPos(), docEnd);
                }
		if (docBegin != Integer.MAX_VALUE && docEnd != Integer.MIN_VALUE) {
		    positions.add(new SelectionInfo(docBegin, docEnd));
		} else if (docBegin == Integer.MAX_VALUE && docEnd != Integer.MIN_VALUE) {
		    positions.add(new SelectionInfo(startPos, docEnd));
		} else if (docBegin != Integer.MAX_VALUE && docEnd == Integer.MIN_VALUE) {
		    positions.add(new SelectionInfo(docBegin, endPos));
		}
            }
	    //sort selectioninfo by their start
	    SortedSet<SelectionInfo> orderedPositions = new TreeSet<SelectionInfo>(new Comparator<SelectionInfo>() {
		@Override
		public int compare(SelectionInfo o1, SelectionInfo o2) {
                    //to support selections, which start at the same offset also compare the end offsets
                    int offsetStartDiff = o2.getStartOffset() - o1.getStartOffset();
                    if (0 == offsetStartDiff) {
                        return (o1.getEndOffset() - o2.getEndOffset());
                    }
                    return offsetStartDiff;
		}
	    });
	    orderedPositions.addAll(positions);
	    //for each selectioninfo add its line selection
	    if (target.getDocument() instanceof StyledDocument) {
		StyledDocument doc = (StyledDocument) target.getDocument();

                Iterator<SelectionInfo> it = positions.iterator();
                SelectionInfo selectionInfo = it.hasNext() ? it.next() : null;
		while (selectionInfo != null) {
		    int startOffset = NbDocument.findLineOffset(doc, NbDocument.findLineNumber(doc, selectionInfo.getStartOffset()));
		    int endOffset = doc.getLength();
                    try {
                        endOffset = NbDocument.findLineOffset(doc, NbDocument.findLineNumber(doc, selectionInfo.getEndOffset()) + 1);
                    } catch (IndexOutOfBoundsException ioobe) {}
                    SelectionInfo next = it.hasNext() ? it.next() : null;
                    final boolean isEmptySelection = selectionInfo.startOffset == selectionInfo.endOffset;
                    //don't create line selection for empty selections
                    if (!isEmptySelection) {
                        if (next == null || startOffset >= next.startOffset && endOffset <= next.endOffset) {
                            orderedPositions.add(new SelectionInfo(startOffset, endOffset));
                        }
                    }
                    selectionInfo = next;
		}
	    }

	    return orderedPositions.toArray(new SelectionInfo[0]);
        }

        public synchronized void run() {
            if (selectionInfos != null && selIndex < selectionInfos.length - 1) {
                select(selectionInfos[++selIndex]);
            }
        }

    }

    private static final class SelectionInfo {

        private int startOffset;
        private int endOffset;

        SelectionInfo(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public String toString() {
            return "<" + startOffset + ":" + endOffset + ">"; //NOi18N
        }
    }
}
