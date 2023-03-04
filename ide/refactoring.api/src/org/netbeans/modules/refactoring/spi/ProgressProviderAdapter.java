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

package org.netbeans.modules.refactoring.spi;

import org.netbeans.modules.refactoring.api.impl.ProgressSupport;
import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 * Simple implementation of ProgressProvider
 * @see ProgressProvider
 * @author Jan Becicka
 */
public class ProgressProviderAdapter implements ProgressProvider {

    private ProgressSupport progressSupport;

    /**
     * Default constructor
     */
    protected ProgressProviderAdapter() {
    }
    
    /** Registers ProgressListener to receive events.
     * @param listener The listener to register.
     *
     */
    @Override
    public synchronized void addProgressListener(ProgressListener listener) {
        if (progressSupport == null ) {
            progressSupport = new ProgressSupport();
        }
        progressSupport.addProgressListener(listener);
    }
    
    /** Removes ProgressListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    @Override
    public synchronized void removeProgressListener(ProgressListener listener) {
        if (progressSupport != null ) {
            progressSupport.removeProgressListener(listener); 
        }
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    protected final void fireProgressListenerStart(int type, int count) {
        if (progressSupport != null) 
            progressSupport.fireProgressListenerStart(this, type, count);
    }
    
    /** Notifies all registered listeners about the event.
     */
    protected final void fireProgressListenerStep() {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStep(this);
    }
    
    /**
     * Notifies all registered listeners about the event.
     * @param count 
     */
    protected final void fireProgressListenerStep(int count) {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStep(this, count);
    }
    
    /** Notifies all registered listeners about the event.
     */
    protected final void fireProgressListenerStop() {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStop(this);
    }
}
