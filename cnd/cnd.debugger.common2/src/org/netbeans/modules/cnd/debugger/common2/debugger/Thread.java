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


package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent;

public abstract class Thread implements DebuggingView.DVThread {

    private final NativeDebugger debugger;
    private final ModelListener updater;
    
    protected String current_function;
    protected String address;
    protected boolean current;
    private Frame[] stack;

    /**
     * Create a new Thread
     * <p>
     * 'thread' may be 'null, making this be a dummy placeholder thread.
     */

    protected Thread(NativeDebugger debugger, ModelListener updater) {
	this.debugger = debugger;
	this.updater = updater;
    }

    public abstract boolean hasEvent();

    /**
     * Mark this thread as the current one.
     */
    public void setCurrent(boolean current) {
	if (this.current != current) {
	    this.current = current;
	    update();
	}
    }

    /**
     * Is the thread the current one?
     */
    public boolean isCurrent() {
	return current;
    }

    private void update() {
	// trigger a pull from the view
	// OLD updater.treeNodeChanged(this);
	updater.modelChanged(new ModelEvent.NodeChanged(this, this));
    }
    
    public String getCurrentFunction() {
	if (current_function == null) {
	    return "<unknown>"; // NOI18N
	} else  {
	    return current_function;
	} 
    }
    
   @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof Thread) ) {
            return false;
        }
        return getName().equals(((Thread)obj).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public String getAddress() {
	// Got 0x-10f2288 for some reason!
	// toHexString will prepend a - if the high-order bit is on.
	// Really? The javadoc says 'unsigned' all over.
	// SHOULD perhaps 0 pad according to arch?
	/* LATER
	*/
	// String address = "0x" + Address.toHexString(thread.address); // NOI18N
	return address;
    }

    // interface DVThread
    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        // TODO
    }

    // interface DVThread
    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        // TODO
    }

    // interface DVThread
    @Override
    public List<DebuggingView.DVThread> getLockerThreads() {
        return null;    // TODO
    }

    // interface DVThread
    @Override
    public Breakpoint getCurrentBreakpoint() {
        return null; // TODO
    }

    // interface DVThread
    @Override
    public boolean isInStep() {
        return false;   // TODO
    }

    // interface DVThread
    @Override
    public void makeCurrent() {
        // TODO check that non-ui operations are carried out
        setCurrent(true);
    }

    // interface DVThread
    @Override
    public void resume() {
        debugger.resumeThread(this);    // TODO better way to implement this functionality
    }

    // interface DVThread
    @Override
    public void suspend() {
        debugger.pause();   // TODO better way to implement this functionality | implement pausing a single thread
    }

    // interface DVThread
    @Override
    public void resumeBlockingThreads() {
        // TODO
    }
    
    // interface DVThread
    @Override
    public DebuggingView.DVSupport getDVSupport() {
        return debugger.session().coreSession().lookupFirst(null, DebuggingView.DVSupport.class);
    }

    public abstract String getFile();

    public abstract String getLine();

    public abstract String getLWP();

    public abstract Integer getPriority();

    public Integer getStackSize() {
        return stack.length;
    }
    
    public Frame[] getStack() {
        return stack;
    }
    
    public void setStack(Frame[] stack) {
        this.stack = stack;
    }

    public abstract String getStartFunction();

    public abstract String getStartupFlags();

    public abstract String getState();

}
