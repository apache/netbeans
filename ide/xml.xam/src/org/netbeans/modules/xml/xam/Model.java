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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeListener;
import javax.swing.event.UndoableEditListener;
import org.netbeans.api.annotations.common.CheckReturnValue;

/**
 * Interface describing an abstract model. The model is based on a
 * document representation that represents the persistent form.
 *
 * @author Chris Webster
 * @author Nam Nguyen
 * @author Rico Cruz
 */
public interface Model<C extends Component<C>> extends Referenceable {
    
    public static final String STATE_PROPERTY = "state";
    
    /**
     * Adds coarse-grained change listener for events on model components.
     */
    public void removeComponentListener(ComponentListener cl);

    /**
     * Removes component event listener.
     */
    public void addComponentListener(ComponentListener cl);

    /**
     * Adds fine-grained property change listener for events on model components.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl);

    /**
     * Remove property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl);

    /**
     * Removes undoable edit listener.
     */
    void removeUndoableEditListener(UndoableEditListener uel);

    /**
     * Adds undoable edit listener.
     */
    void addUndoableEditListener(UndoableEditListener uel);

    /**
     * Removes undoable refactoring edit listener.  This will also restored
     * the existing undoable edit listeners to the set before the start of
     * refactoring.  Note, if these listeners are UndoManager instances
     * their queues are cleared of existing edits.
     */
    void removeUndoableRefactorListener(UndoableEditListener uel);

    /**
     * Adds undoable refactoring edit listener.  This is typically called by a
     * refactoring manager before start refactoring changes.  This
     * will also save existing undoable edit listeners.  Note, if these listeners
     * are UndoManager instances, their queues will be cleared of existing edits.
     */
    void addUndoableRefactorListener(UndoableEditListener uel);

    /**
     * Makes the current memory model consistent with the underlying
     * representation, typically a swing document. 
     */
    void sync() throws java.io.IOException;
    
    /**
     * Returns true if sync is being performed.
     */
    boolean inSync();
    
    /**
     * State of the model.
     * VALID - Source is well-formed and model is in-sync.
     * NOT_WELL_FORMED - Source is not well-formed, model is not synced.
     * NOT_SYNCED - Source is well-formed, but there was error from last sync.
     */
    enum State {
        VALID, 
        NOT_WELL_FORMED,
        NOT_SYNCED
    }
    /**
     * @return the last known state of the document. This method is affected
     * by invocations of #sync().
     */
    State getState();
    
    /**
     * Be very careful while using this method. It returns only current state
     * and doesn't inform if the transaction has been started by current thread.
     * Only the thread, which owns the transaction can use it and do changes to
     * the model.  
     *
     * @return true if model is in middle of transformation tranasction.
     */
    boolean isIntransaction();
    
    /** 
     * This method will block until a transaction can be started. A transaction
     * in this context will fire events (such as property change) when 
     * {@link #endTransaction} has been invoked. A transaction must be
     * be acquired during a mutation, reading can be performed without
     * a transaction. Only a single transaction at a time is supported. Mutations
     * which occur based on events will not be reflected until the transaction
     * has completed.
     * @return true if transaction is acquired successfully, else false, for example
     * if model has transitioned into invalid state.
     */
    @CheckReturnValue boolean startTransaction();
    
    /**
     * This method stops the transaction and causes all events to be fired. 
     * After all events have been fired, the document representation will be 
     * modified to reflect the current value of the model (flush).
     *
     * Be aware that the method does nothing if the transaction hasn't been started or
     * started by another thread.
     */
    void endTransaction();
    
    /**
     * Adds child component at specified index.
     * @param target the parent component.
     * @param child the child component to be added.
     * @param index position among same type of child components, or -1 if not relevant.
     */
    void addChildComponent(Component target, Component child, int index);
    
    /**
     * Removes specified component from model.
     */
    void removeChildComponent(Component child);

    /**
     * @return the source of this model or null if this model does associate
     * with any model source.
     */
    ModelSource getModelSource();

}
