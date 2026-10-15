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


package org.netbeans.modules.progress.spi;

import java.awt.EventQueue;
import java.util.LinkedHashSet;
import java.util.concurrent.Executor;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.progress.module.DefaultHandleFactory;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 * @since org.netbeans.api.progress/1 1.18
 */
public final class TaskModel {
    private DefaultListSelectionModel selectionModel;
    private final DefaultListModel<InternalHandle> model;
    private InternalHandle explicit;
    private final LinkedHashSet<ListDataListener> dataListeners;
    private final LinkedHashSet<ListSelectionListener> selectionListeners;
    private final Executor eventExecutor;
    
    TaskModel(Executor eventExecutor) {
        selectionModel = new DefaultListSelectionModel();
        model = new DefaultListModel<>();
        dataListeners = new LinkedHashSet<ListDataListener>();
        selectionListeners = new LinkedHashSet<ListSelectionListener>();
        TaskListener list = new TaskListener();
        model.addListDataListener(list);
        selectionModel.addListSelectionListener(list);
        this.eventExecutor = eventExecutor;
    }
    
    /** Creates a new instance of TaskModel */
    public TaskModel() {
        this(Controller.getDefault().getEventExecutor());
    }
    
    
    
    public void addHandle(InternalHandle handle) {
        synchronized (model) {
            model.addElement(handle);
        }
        updateSelection();
    }
    
    public void removeHandle(InternalHandle handle) {
        if (explicit == handle) {
            explicit = null;
        }
        synchronized (model) {
            model.removeElement(handle);
        }
        updateSelection();
    }
    
    /** Impl of selection policy. Tasks which are not in sleep mode and
     * user initiated are preferred for selection.
     */
    void updateSelection () {
        // don't touch selection if explicit
        if (explicit != null) {
            return;
        }
        InternalHandle oldSelected = getSelectedHandle();
        // keep selection if possible
        if (oldSelected != null && !oldSelected.isInSleepMode()) {
            return;
        }

        // select last added that is not in sleep mode and preferrably userInitiated
        InternalHandle toSelect = null;
        synchronized (model) {
            for (int i = 0; i < model.size(); i++) {
                InternalHandle curHandle = (InternalHandle) model.getElementAt(i);
                if (getSelectionRating(curHandle) >= getSelectionRating(toSelect)) {
                    toSelect = curHandle;
                }
            }
            if (toSelect != null) {
                selectionModel.setSelectionInterval(model.indexOf(toSelect), model.indexOf(toSelect));
            } else {
                selectionModel.clearSelection();
            }
        }
    }

    private int getSelectionRating (InternalHandle handle) {
        int result = 0;
        if (handle != null) {
            if (!handle.isInSleepMode()) {
                result += 4;
            }
            if (handle.isUserInitialized()) {
                result += 2;
            }
            result += 1;
        }
        return result;
    }
    
    public void explicitlySelect(InternalHandle handle) {
        explicit = handle;
        synchronized (model) {
            int index = model.indexOf(explicit);
            if (index == -1) {
                return;
            }
            selectionModel.setSelectionInterval(index, index);
        }
    }
    
    public InternalHandle getExplicitSelection() {
        return explicit;
    }
    
    public int getSize() {
        synchronized (model) {
            return model.size();
        }
    }
           
    
    public InternalHandle[] getHandles() {
        InternalHandle[] handles;
        synchronized (model) {
            handles = new InternalHandle[model.size()];
            model.copyInto(handles);
        }
        return handles;
    }
    
    public InternalHandle getSelectedHandle() {
        synchronized (model) {
            int select = selectionModel.getMinSelectionIndex();
            if (select != -1) {
                if (select >= 0 && select < model.size()) {
                    return (InternalHandle) model.getElementAt(select);
                }
            }
        }
        return null;
    }
    
    public void addListSelectionListener(ListSelectionListener listener) {        
        synchronized (selectionListeners) {
            selectionListeners.add(listener);
        }
    }
    
    public void removeListSelectionListener(ListSelectionListener listener) {
        synchronized (selectionListeners) {
            selectionListeners.remove(listener);
        }
    }
    
    public void addListDataListener(ListDataListener listener) {
        synchronized (dataListeners) {
            dataListeners.add(listener);
        }
    }
    
    public void removeListDataListener(ListDataListener listener) {        
        synchronized (dataListeners) {
            dataListeners.remove(listener);
        }
    }
    
    private ListDataListener[] getDataListeners() {
        synchronized (dataListeners) {
            return dataListeners.toArray(new ListDataListener[0]);
        }
    } 
    
    private ListSelectionListener[] getSelectionListeners() {
        synchronized (selectionListeners) {
            return selectionListeners.toArray(new ListSelectionListener[0]);
        }
    }
     
     
    private class TaskListener implements ListDataListener, ListSelectionListener {

        @Override
        public void intervalAdded(final ListDataEvent e) {
            final ListDataListener[] lists = getDataListeners();
            eventExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for (ListDataListener list : lists) {
                        list.intervalAdded(e);
                    }
                }
            });
        }

        @Override
        public void intervalRemoved(final ListDataEvent e) {
            final ListDataListener[] lists = getDataListeners();
            eventExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for (ListDataListener list : lists) {
                        list.intervalRemoved(e);
                    }
                }
            });
        }

        @Override
        public void contentsChanged(final ListDataEvent e) {
            final ListDataListener[] lists = getDataListeners();
            eventExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for (ListDataListener list : lists) {
                        list.contentsChanged(e);
                    }
                }
            });
        }

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            final ListSelectionListener[] lists = getSelectionListeners();
            eventExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for (ListSelectionListener list : lists) {
                        list.valueChanged(e);
                    }
                }
            });
        }
        
    }
}
