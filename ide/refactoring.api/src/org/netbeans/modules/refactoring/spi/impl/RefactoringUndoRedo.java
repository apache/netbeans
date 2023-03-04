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
package org.netbeans.modules.refactoring.spi.impl;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=UndoRedo.class, path="org/netbeans/modules/refactoring")
public class RefactoringUndoRedo implements UndoRedo {
    
    private UndoManager manager = UndoManager.getDefault();
    
    private ChangeSupport pcs = new ChangeSupport(this);
    
    public RefactoringUndoRedo() {
        manager.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                pcs.fireChange();
            }

        });
    }

    @Override
    public boolean canUndo() {
        return manager.isUndoAvailable();
    }

    @Override
    public boolean canRedo() {
        return manager.isRedoAvailable();
    }

    @Override
    public void undo() throws CannotUndoException {
        manager.undo(null);
    }

    @Override
    public void redo() throws CannotRedoException {
        manager.redo(null);
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        pcs.addChangeListener(l);
        
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        pcs.removeChangeListener(l);
    }

    @Override
    public String getUndoPresentationName() {
        return manager.getUndoDescription(null);
    }

    @Override
    public String getRedoPresentationName() {
        return manager.getRedoDescription(null);
    }
    
}
