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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent;


/**
 * Propagate tree change notifications to listener (if one is registered)
 *
 * Models (and their role as ModelListeners) come and go, while the
 * rest of our code relies on more permanent ModelListener's to send
 * change notifications to so we pre-instantiate ModelChangeDelegator's
 * which everyone can call at all times.
 *
 * Models come and go at two levels.
 * 1) It's not entirely clear to me when they get instantiated, so it's
 *    feasible that a, say, NativeSession is created before it's Model has been.
 * 2) Once a Model has been instantiated it might or might not have any
 *    listeners. The listeners correspond to the number of observing views.
 *    The listeners are tracked by ModelListenerSupport and when the refcount
 *    moves up or down past 1 the Model gets registered or deregistered ...
 *    ultimately with us.
 */

public class ModelChangeDelegator implements ModelListener {
    private final List<ModelListener> listeners = new ArrayList();
    private static final class Lock {
    }
    private final Object listenersLock = new Lock();

    private boolean	firechange;
    private boolean	batchmode = false;

    public void addListener(ModelListener listener) {
        if (listener  == null) {
            return;
        }
        synchronized( listenersLock ) {
            if (listeners.contains(listener)) {
                return;
            }
            listeners.add(listener);
        }
	// Listener may have been registered after treeChanged() was called.
	// LATER? treeChanged();
    }
    public void removeListener(ModelListener listener) {
            synchronized( listenersLock ) {
		listeners.remove(listener);
	    }
	// Listener may have been registered after treeChanged() was called.
	// LATER? treeChanged();
    }    

    public boolean hasListener() {
	return !listeners.isEmpty();
    }

    // interface ModelListener
    @Override
    public void modelChanged(ModelEvent e) {
	firechange = true;
        List<ModelListener> listenersCopy;
        synchronized( listenersLock ) {
            listenersCopy = new ArrayList<>(listeners);
        }          
	if (!batchmode) {
	    //System.out.println("\tmodelChanged");
            for (ModelListener listener : listenersCopy) {
                listener.modelChanged(e);
            }	    
        }
    }

    // interface ex-TreeModelListener
    public void treeChanged() {
	firechange = true;
        List<ModelListener> listenersCopy;
        synchronized( listenersLock ) {
            listenersCopy = new ArrayList<>(listeners);
        }          
	if (!batchmode) {
	    //System.out.println("\tmodelChanged");
            for (ModelListener listener : listenersCopy) {
                listener.modelChanged(new ModelEvent.TreeChanged(this));
            }	    
        }       
    }

    // interface ex-TreeModelListener
    public void treeNodeChanged(Object node) {
	firechange = true;
        List<ModelListener> listenersCopy;
        synchronized( listenersLock ) {
            listenersCopy = new ArrayList<>(listeners);
        }          
	if (!batchmode) {
	    //System.out.println("\tmodelChanged");
            for (ModelListener listener : listenersCopy) {
                listener.modelChanged(new ModelEvent.NodeChanged(this, node));
            }	    
        }         
    }
    
    // interface ex-TreeModelListener
    public void treeNodeChanged(Object node, int changed) {
	firechange = true;
        List<ModelListener> listenersCopy;
        synchronized( listenersLock ) {
            listenersCopy = new ArrayList<>(listeners);
        }          
	if (!batchmode) {
	    //System.out.println("\tmodelChanged");
            for (ModelListener listener : listenersCopy) {
                listener.modelChanged(new ModelEvent.NodeChanged(this, node, changed));
            }	    
        }         
    }    

    public boolean batchMode() {
	return batchmode;
    }

    public void batchOn() {
	batchmode = true;
	firechange = false;
    }

    public void batchOff() {
	batchmode = false;
        List<ModelListener> listenersCopy;
        synchronized( listenersLock ) {
            listenersCopy = new ArrayList<>(listeners);
        }          
	if (firechange) {
	    //System.out.println("\tmodelChanged");
            for (ModelListener listener : listenersCopy) {
                listener.modelChanged(new ModelEvent.TreeChanged(this));
            }	    
        }        
    }

    /**
     * Turn batching off and force a refresh even if no updates were made.
     */
    public void batchOffForce() {
	batchmode = false;
        List<ModelListener> listenersCopy;
        synchronized( listenersLock ) {
            listenersCopy = new ArrayList<>(listeners);
        }          
        for (ModelListener listener : listenersCopy) {
            listener.modelChanged(new ModelEvent.TreeChanged(this));
        }	    
    }
}
