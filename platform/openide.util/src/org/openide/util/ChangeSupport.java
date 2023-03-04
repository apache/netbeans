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

package org.openide.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple equivalent of {@link java.beans.PropertyChangeSupport} for
 * {@link ChangeListener}s. This class is not serializable.
 *
 * @since 7.8
 * @author Andrei Badea
 */
public final class ChangeSupport {

    private static final Logger LOG = Logger.getLogger(ChangeSupport.class.getName());

    // not private because used in unit tests
    final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private final Object source;

    /**
     * Creates a new <code>ChangeSupport</code>
     *
     * @param  source the instance to be given as the source for events.
     */
    public ChangeSupport(Object source) {
        this.source = source;
    }

    /**
     * Adds a <code>ChangeListener</code> to the listener list. The same
     * listener object may be added more than once, and will be called
     * as many times as it is added. If <code>listener</code> is null,
     * no exception is thrown and no action is taken.
     *
     * @param  listener the <code>ChangeListener</code> to be added.
     */
    public void addChangeListener(ChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINE) && listeners.contains(listener)) {
            LOG.log(Level.FINE, "diagnostics for #167491", new IllegalStateException("Added " + listener + " multiply"));
        }
        listeners.add(listener);
    }

    /**
     * Removes a <code>ChangeListener</code> from the listener list.
     * If <code>listener</code> was added more than once,
     * it will be notified one less time after being removed.
     * If <code>listener</code> is null, or was never added, no exception is
     * thrown and no action is taken.
     *
     * @param  listener the <code>ChangeListener</code> to be removed.
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    /**
     * Fires a change event to all registered listeners.
     */
    public void fireChange() {
        if (listeners.isEmpty()) {
            return;
        }
        fireChange(new ChangeEvent(source));
    }

    /**
     * Fires the specified <code>ChangeEvent</code> to all registered
     * listeners. If <code>event</code> is null, no exception is thrown
     * and no action is taken.
     *
     * @param  event the <code>ChangeEvent</code> to be fired.
     */
    private void fireChange(ChangeEvent event) {
        assert event != null;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(event);
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    /**
     * Checks if there are any listeners registered to this<code>ChangeSupport</code>.
     *
     * @return true if there are one or more listeners for the given property,
     *         false otherwise.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
