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
