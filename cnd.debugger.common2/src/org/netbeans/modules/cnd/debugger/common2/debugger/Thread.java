/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
