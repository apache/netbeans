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

package org.netbeans.modules.refactoring.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 * Support class for progress notifications
 * @author Martin Matula, Jan Becicka
 */
public final class ProgressSupport {
    /** Utility field holding list of ProgressListeners. */
    private final List<ProgressListener> progressListenerList = new ArrayList<ProgressListener>();
    private int counter;
    private boolean deterministic;

    public boolean isEmpty() {
        return progressListenerList.isEmpty();
    }
    
    public synchronized void addProgressListener(ProgressListener listener) {
        progressListenerList.add(listener);
    }
    
    /** Removes ProgressListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
        progressListenerList.remove(listener);
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    public void fireProgressListenerStart(Object source, int type, int count) {
        counter = -1;
        deterministic = count > 0;
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.START, type, count);
        ProgressListener[] listeners = getListenersCopy();
        for (ProgressListener listener : listeners) {
            try {
                listener.start(event);
            } catch (RuntimeException e) {
                log(e);
            }
        }
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    public void fireProgressListenerStart(int type, int count) {
        fireProgressListenerStart(this, type, count);
    }
    
    
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStep(Object source, int count) {
        if (deterministic) {
            if (count < 0) {
                deterministic = false;
            }
            counter = count;
        } else {
            if (count > 0) {
                deterministic = true;
                counter = -1;
            } else {
                counter = count;
            }
        }
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.STEP, 0, count);
        ProgressListener[] listeners = getListenersCopy();
        for (ProgressListener listener : listeners) {
            try {
                listener.step(event);
            } catch (RuntimeException e) {
                log(e);
            }
        }
    }
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStep(Object source) {
        if (deterministic) {
            ++counter;
        }
        fireProgressListenerStep(source, counter);
    }
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStop(Object source) {
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.STOP);
        ProgressListener[] listeners = getListenersCopy();
        for (ProgressListener listener : listeners) {
            try {
                listener.stop(event);
            } catch (RuntimeException e) {
                log(e);
            }
        }
    }
    
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStop() {
        fireProgressListenerStop(this);
    }

    private synchronized ProgressListener[] getListenersCopy() {
        return progressListenerList.toArray(new ProgressListener[0]);
    }

    private void log(Exception e) {
        Logger.getLogger(ProgressSupport.class.getName()).log(Level.INFO, e.getMessage(), e);
    }

}
