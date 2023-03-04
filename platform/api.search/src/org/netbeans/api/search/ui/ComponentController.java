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
package org.netbeans.api.search.ui;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.ChangeSupport;

/**
 * Base class for component controllers.
 *
 * @author jhavlin
 */
abstract class ComponentController<T extends JComponent> {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    protected T component;

    protected ComponentController(T component) {
        this.component = component;
    }

    public final T getComponent() {
        return component;
    }

    /**
     * Adds a
     * <code>ChangeListener</code> to the listener list. The same listener
     * object may be added more than once, and will be called as many times as
     * it is added. If
     * <code>listener</code> is null, no exception is thrown and no action is
     * taken.
     *
     * @param listener the
     * <code>ChangeListener</code> to be added.
     */
    public final void addChangeListener(@NonNull ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes a
     * <code>ChangeListener</code> from the listener list. If
     * <code>listener</code> was added more than once, it will be notified one
     * less time after being removed. If
     * <code>listener</code> is null, or was never added, no exception is thrown
     * and no action is taken.
     *
     * @param listener the
     * <code>ChangeListener</code> to be removed.
     */
    public final void removeChangeListener(@NonNull ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Fires a change event to all registered listeners.
     */
    protected final void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * Checks if there are any listeners registered to this
     * <code>ChangeSupport</code>.
     *
     * @return true if there are one or more listeners for the given property,
     * false otherwise.
     */
    public final boolean hasListeners() {
        return changeSupport.hasListeners();
    }
}
