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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AbstractGridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridBoundsChange;
import org.openide.awt.UndoRedo;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Stola
 */
public class UndoRedoSupport {
    private static Map<FormModel,UndoRedoSupport> supportMap = new WeakHashMap<FormModel,UndoRedoSupport>();
    GridActionPerformer performer;
    UndoAction undoAction;
    RedoAction redoAction;
    int undoableEdits;
    int redoableEdits;
    FormModel.UndoRedoManager manager;
    private Lookup undoRedoLookup;
    
    private UndoRedoSupport(FormModel model) {
        manager = (FormModel.UndoRedoManager)model.getUndoRedoManager();
        manager.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (manager.isUndoInProgress()) {
                    undoableEdits--;
                    redoableEdits++;
                } else if (manager.isRedoInProgress()) {
                    undoableEdits++;
                    redoableEdits--;
                } else {
                    undoableEdits++;
                    redoableEdits=0;
                }
                undoAction.updateEnabled();
                redoAction.updateEnabled();
            }
        });
        undoAction = new UndoAction(SystemAction.get(org.openide.actions.UndoAction.class));
        redoAction = new RedoAction(SystemAction.get(org.openide.actions.RedoAction.class));
        undoRedoLookup = Lookups.singleton(new UndoRedo.Provider() {
            @Override
            public UndoRedo getUndoRedo() {
                return manager;
            }
        });
    }

    static UndoRedoSupport getSupport(FormModel model) {
        UndoRedoSupport support = supportMap.get(model);
        if (support == null) {
            support = createUndoRedoSupport(model);
            supportMap.put(model, support);
        }
        return support;
    }
    
    private static UndoRedoSupport createUndoRedoSupport(FormModel model) {
        return new UndoRedoSupport(model);
    }
    
    public void reset(GridActionPerformer performer) {
        this.performer = performer;
        undoableEdits = 0;
        redoableEdits = 0;
        undoAction.updateEnabled();
        redoAction.updateEnabled();
    }
    
    public Action getUndoAction() {
        return undoAction;
    }
    
    public Action getRedoAction() {
        return redoAction;
    }
    
    abstract static class DelegateAction extends AbstractAction {
        protected Action delegate;
        
        protected DelegateAction(Action delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            delegate.putValue(key, value);
        }
    }
    
    class UndoAction extends DelegateAction {
        
        UndoAction(Action delegate) {
            super(delegate);
            updateEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Action perfAct = delegate instanceof ContextAwareAction ? ((ContextAwareAction)delegate).createContextAwareInstance(undoRedoLookup) : delegate;
            performer.performAction(new DelegateGridAction(perfAct));
        }
        
        final void updateEnabled() {
            setEnabled(undoableEdits>0 && manager.canUndo());
        }
        
    }
    
    class RedoAction extends DelegateAction {
        
        RedoAction(Action delegate) {
            super(delegate);
            updateEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Action perfAct = delegate instanceof ContextAwareAction ? ((ContextAwareAction)delegate).createContextAwareInstance(undoRedoLookup) : delegate;
            performer.performAction(new DelegateGridAction(perfAct));
        }
        
        final void updateEnabled() {
            setEnabled(redoableEdits>0 && manager.canRedo());
        }
        
    }
    
    static class DelegateGridAction extends AbstractGridAction {
        protected Action delegate;
        
        protected DelegateGridAction(Action delegate) {
            this.delegate = delegate;
        }

        @Override
        public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
            GridInfoProvider info = gridManager.getGridInfo();
            int oldColumns = info.getColumnCount();
            int oldRows = info.getRowCount();
            GridUtils.removePaddingComponents(gridManager);

            // Undo/redo itself
            delegate.actionPerformed(null);
            gridManager.updateLayout(true);
            
            // Remove deleted components from selection
            Set<Component> newSelection = new HashSet<Component>();
            for (Component comp : context.getSelectedComponents()) {
                if (comp.getParent() != null) {
                    newSelection.add(comp);
                }
            }
            context.setSelectedComponents(newSelection);
                    
            GridUtils.revalidateGrid(gridManager);
            gridManager.updateGaps(false);
            int newColumns = info.getColumnCount();
            int newRows = info.getRowCount();
            int columns = Math.max(oldColumns, newColumns);
            int rows = Math.max(oldRows, newRows);
            GridUtils.addPaddingComponents(gridManager, columns, rows);
            GridUtils.revalidateGrid(gridManager);
            return null;
        }
        
    }
    
}
