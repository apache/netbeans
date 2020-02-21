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
package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.cnd.spi.editor.CsmCodeBlockProvider;
import org.openide.util.NbBundle;

/**
 *
 */
public class SelectCodeElementAction extends BaseAction {

    private final boolean selectNext;

    /**
     * Construct new action that selects next/previous code elements
     * according to the language model.
     * <br>
     *
     * @param name name of the action (should be one of
     *  <br>
     *  <code>CCKit.selectNextElementAction</code>
     *  <code>CCKit.selectPreviousElementAction</code>
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
        
    private String getShortDescription(){
        String name = (String)getValue(Action.NAME);
        if (name == null) return null;
        String shortDesc;
        try {
            shortDesc = NbBundle.getMessage(CCKit.class, name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }
    
    @Override
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
        private SelectionInfo[] selectionInfos;
        private int selIndex = -1;
        private boolean ignoreNextCaretUpdate;
        private AtomicBoolean cancel;

        SelectionHandler(JTextComponent target, String name) {
            this.target = target;
            this.name = name;
        }

        public void selectNext() {
            if (selectionInfos == null) {
                cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        CsmCodeBlockProvider.Scope scope = CsmCodeBlockProvider.getDefault().getScope(target.getDocument(), target.getCaretPosition());
                        selectionInfos = initSelectionPath(target, scope);
                    }
                }, name, cancel, false);
            }            
            run();
        }

        public synchronized void selectPrevious() {
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
        
        @Override
        public void caretUpdate(CaretEvent e) {
            if (!ignoreNextCaretUpdate) {
                synchronized (this) {
                    selectionInfos = null;
                    selIndex = -1;
                }
            }
            ignoreNextCaretUpdate = false;
        }


        private SelectionInfo[] initSelectionPath(JTextComponent target, CsmCodeBlockProvider.Scope scope) {
            List<SelectionInfo> positions = new ArrayList<SelectionInfo>();
            while(scope != null) {
                positions.add(new SelectionInfo(scope.getStartOffset(), scope.getEndOffset()));
                scope = scope.getParentScope();
            }
            return positions.toArray(new SelectionInfo[positions.size()]);
        }

        @Override
        public synchronized void run() {
            if (selectionInfos != null && selIndex < selectionInfos.length - 1) {
                select(selectionInfos[++selIndex]);
            }
        }

    }
    
    private static final class SelectionInfo {
        
        private final int startOffset;
        private final int endOffset;
        
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
