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

package org.openide.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.util.logging.Level.*;
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
    final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final Object source;

    /**
     * Creates a new <code>ChangeSupport</code>
     *
     * @param  source the instance to be given as the source for events.
     */
    public ChangeSupport(final Object source) {
        
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
    public void addChangeListener(final ChangeListener listener) {

        if (listener != null) {
            if (LOG.isLoggable(FINE) && this.listeners.contains(listener)) {
                LOG.log(FINE, "diagnostics for #167491", 
                        new IllegalStateException("Added " + listener + " multiply"));
            }
            this.listeners.add(listener);
        }
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
    public void removeChangeListener(final ChangeListener listener) {

        this.listeners.remove(listener);
    }

    /**
     * Fires a change event to all registered listeners.
     */
    public void fireChange() {

        if (!listeners.isEmpty()) {
            final ChangeEvent event = new ChangeEvent(this.source);
            for (final ChangeListener listener : this.listeners) {
                try {
                    listener.stateChanged(event);
                } catch (final RuntimeException x) {
                    Exceptions.printStackTrace(x);
                }
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
        
        return !this.listeners.isEmpty();
    }
}
