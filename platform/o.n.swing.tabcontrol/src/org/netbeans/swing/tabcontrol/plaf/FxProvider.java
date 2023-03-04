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
/*
 * FxProvider.java
 *
 * Created on March 27, 2004, 9:04 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;

/** Class which can provide sliding or other eye-candy effects as a component
 * is displayed.  To use, subclass TabbedContainerUI and create an instance
 * in createFxProvider.  The abstract doFinish() method is expected to call
 * TabbedContainerUI.showComponent(), so this is best implemented as an inner
 * class of a TabbbedContainerUI implementation.
 *
 * @author  Tim Boudreau
 */
public abstract class FxProvider {
    protected JComponent comp;
    protected JRootPane root;
    private boolean running = false;
    protected Object orientation = null;
    /** Creates a new instance of FxProvider */
    public FxProvider() {
    }
    
    /** Start the effect running.  This method will set up the fields with 
     * the passed values, set the running flag, and then call <code>doStart()</code>.
     * If <code>isRunning()</code> is true, calls <code>abort()</code> before
     * initializing.
     */
    public final void start(JComponent comp, JRootPane root, Object orientation) {
        if (running) {
            if (comp == this.comp && root == this.root) {
                return;
            } else {
                abort();
            }
        }
        this.comp = comp;
        this.root = root;
        this.orientation = orientation;
        running = true;
        doStart();
    }
    
    /** 
     * Perform any cleanup necessary and complete the effect.
     * Sets the running flag to false, calls <code>doFinish()</code> (in which
     * the implementation should call showComponent() on the TabbedContainerUI
     * to actually show the component for which an effect has been being 
     * presented. <strong>After</strong> calling <code>finish()</code>, it 
     * calls <code>cleanup()</code>.  The common use case is for the effect
     * to be painted on the window's glass pane, so the idea is to leave that
     * onscreen while doing the work that will display the actual component,
     * and then hide the glass pane containing the effect's product once the
     * window is in its new state, with the component displayed.
     */
    public final void finish() {
        running = false;
        doFinish();
        cleanup();
    }
    
    /** Abort a running effect, so that finish will never be called.  Sets
     * the running flag to false and calls <code>cleanup()</code>. */
    public final void abort() {
        running = false;
        cleanup();
        comp = null;
        root = null;
    }
    
    /** Determine if an effect is currently running */
    public final boolean isRunning() {
        return running;
    }

    /** Clean up any artifacts of the effect, shut down timers, etc. */
    public abstract void cleanup();
    
    /** Implement whatever is needed to begin running the effect - starting a
     * timer, playing with the glass pane, creating offscreen images, etc.,
     * here */
    protected abstract void doStart();
    
    /** Finish the operation - this method should be implemented to actually
     * install the component and leave the displayer in its final state */
    protected abstract void doFinish();
    
}
