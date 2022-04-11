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

import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent;


/**
 * Factoring of modelview Model implementation listener mgmt.
 *
 * SHOULD be called ModelListenerSupport.
 */

public class ModelListenerSupport implements ModelListener {

    private final CopyOnWriteArrayList<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();

    private String owner;

    protected ModelListenerSupport(String owner) {
	this.owner = owner;
    } 

    /**
     * Delegate ModelListener behaviour to all our registered listeners
     * We skip calling redundant listeners.
     * See issue 48887 which is still there even though Hanz went through
     * the TreeModelListener -> ModelListener transition.
     */

    // interface ModelListener
    @Override
    public void modelChanged(ModelEvent event) {
	ModelListener lastListener = null;

	for (ModelListener l : listeners) {
	    if (l != lastListener) {
		l.modelChanged(event);
		lastListener = l;
	    }
	}
    }


    /**
     * Delegate ModelListener behaviour to all our registered listeners
     * We skip calling redundant listeners.
     * See issue 48887
     */

    // interface ex-TreeModelListener
    public void treeChanged() {
	ModelListener lastListener = null;

	for (ModelListener l : listeners) {
	    if (l != lastListener) {
		// OLD l.treeChanged();
		l.modelChanged(new ModelEvent.TreeChanged(this));
		lastListener = l;
	    }
	}
    }

    // interface ex-TreeModelListener
    public void treeNodeChanged(Object node) {
	//System.out.println
	//   (owner + " ModelListenerSupport.treeNodeChanged() " +
	//   listeners.size() + " listeners");

	ModelListener lastListener = null;

	for (ModelListener l : listeners) {
	    if (l != lastListener) {
		// OLD l.treeNodeChanged(node);
		l.modelChanged(new ModelEvent.NodeChanged(this, node));
		lastListener = l;
	    }
	}
    }

    /*
     * Helpers for registering listeners.
     *
     * These return true if the reference count of listeners goes
     * above or back to 0.
     */

    // helper for interface TreeModel etc
    protected boolean addModelListenerHelp(ModelListener l) {
	listeners.add(l);
	return listeners.size() == 1;
    }

    // helper for interface TreeModel etc
    protected boolean removeModelListenerHelp(ModelListener l) {
	listeners.remove(l);
	return listeners.size() == 0;
    }
}
