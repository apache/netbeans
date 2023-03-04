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

package org.netbeans.modules.csl.api;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseAction;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Code selection according to syntax tree.
 *
 * TODO: javadoc selection
 *
 * @author Miloslav Metelka, Jan Pokorsky
 * @deprecated use {@link CslActions#createSelectCodeElementAction(java.lang.String, boolean)  } instead.
*/
@Deprecated
public final class SelectCodeElementAction extends BaseAction {
    public static final String selectNextElementAction = "select-element-next"; //NOI18N
    public static final String selectPreviousElementAction = "select-element-previous"; //NOI18N

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
            shortDesc = NbBundle.getBundle(SelectCodeElementAction.class).getString(name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }
    
    public @Override void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            int selectionStartOffset = target.getSelectionStart();
            int selectionEndOffset = target.getSelectionEnd();
            if (selectionEndOffset > selectionStartOffset || selectNext) {
                SelectionHandler handler = (SelectionHandler)target.getClientProperty(SelectionHandler.class);
                if (handler == null) {
                    handler = new SelectionHandler(target);
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

    private static final class SelectionHandler extends UserTask implements CaretListener, Runnable {
        
        private JTextComponent target;
        private SelectionInfo[] selectionInfos;
        private int selIndex = -1;
        private boolean ignoreNextCaretUpdate;

        SelectionHandler(JTextComponent target) {
            this.target = target;
        }

        public void selectNext() {
            if (selectionInfos == null) {
                Source source = Source.create (target.getDocument());
                try {
                    ParserManager.parse (Collections.<Source> singleton (source), this);
                } catch (ParseException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }

            if (selectionInfos != null) {
                run();
            }
        }

        public synchronized void selectPrevious() {
            if (selIndex == -1) {
                // Try to figure out the selected AST index based on the editor selection
                selIndex = computeSelIndex(false);
            }
            if (selIndex > 0) {
                select(selectionInfos[--selIndex]);
            }
        }

        private void select(SelectionInfo selectionInfo) {
            Caret caret = target.getCaret();
            markIgnoreNextCaretUpdate();
            caret.setDot(selectionInfo.getStartOffset());
            markIgnoreNextCaretUpdate();
            caret.moveDot(selectionInfo.getEndOffset());
        }
        
        private void markIgnoreNextCaretUpdate() {
            ignoreNextCaretUpdate = true;
        }
        
        public @Override void caretUpdate(CaretEvent e) {
            if (!ignoreNextCaretUpdate) {
                synchronized (this) {
                    selectionInfos = null;
                    selIndex = -1;
                }
            }
            ignoreNextCaretUpdate = false;
        }

        public void cancel() {
        }

        public @Override void run (ResultIterator resultIterator) throws ParseException {
            Parser.Result parserResult = resultIterator.getParserResult (target.getCaretPosition ());
            if(!(parserResult instanceof ParserResult)) {
                return ;
            }
            selectionInfos = initSelectionPath(target, (ParserResult)parserResult);
        }
        
        private KeystrokeHandler getBracketCompletion(Document doc, int offset) {
            List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(doc, offset);
            for (Language l : list) {
                if (l.getBracketCompletion() != null) {
                    return l.getBracketCompletion();
                }
            }

            return null;
        }
        
        private SelectionInfo[] initSelectionPath(JTextComponent target, ParserResult parserResult) {
            KeystrokeHandler bc = getBracketCompletion(target.getDocument(), target.getCaretPosition());
            if (bc != null) {
                List<OffsetRange> ranges = bc.findLogicalRanges(parserResult, target.getCaretPosition());
                SelectionInfo[] result = new SelectionInfo[ranges.size()];
                for (int i = 0; i < ranges.size(); i++) {
                    OffsetRange range = ranges.get(i);
                    result[i] = new SelectionInfo(range.getStart(), range.getEnd());
                }
                return result;
            } else {
                return new SelectionInfo[0];
            }
        }
        
        private int computeSelIndex(boolean inner) {
            Caret caret = target.getCaret();
            if (selectionInfos != null && caret != null && caret.getDot() != caret.getMark()) {
                int dot = caret.getDot();
                int mark = caret.getMark();
                int start = Math.min(dot,mark);
                //int end = Math.max(dot,mark);
                for (int i = 0; i < selectionInfos.length; i++) {
                    if (selectionInfos[i].getStartOffset() == start) {
                        // TODO - check end offset too
                        return i;
                    }
                }
                // No exact match - look at the editor selection and find the range
                // that most closely surround the selection (if inner is true, go
                // for the inner one, otherwise the outer)
                for (int i = selectionInfos.length-2; i >= 0; i--) {
                    if (selectionInfos[i].getStartOffset() > start &&
                            selectionInfos[i+1].getStartOffset() < start) {
                        return inner ? i : i-1;
                    }
                }
            }
            
            return selIndex;
        }

        public @Override void run() {
            if (selIndex == -1) {
                // Try to figure out the selected AST index based on the editor selection
                selIndex = computeSelIndex(true);
            }
            if (selIndex < selectionInfos.length - 1) {
                select(selectionInfos[++selIndex]);
            }
        }

    }
    
    // This looks a lot like an OffsetRange! Just reuse my own OffsetRange class?
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
        
    }
}
