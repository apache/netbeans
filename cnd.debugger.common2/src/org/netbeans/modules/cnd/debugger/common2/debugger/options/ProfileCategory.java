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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import java.beans.PropertyChangeSupport;


public abstract class ProfileCategory {
    protected DbgProfile owner;
    protected String propertyName;

    ProfileCategory(DbgProfile owner, String propertyName) {
	this.owner = owner;
	this.propertyName = propertyName;
    }

    /**
     * We've been mutated
     */
    protected void delta(Object o, Object n) {
	// clones don't have an owner
	if (owner == null)
	    return;

	PropertyChangeSupport pcs = owner.pcs;
	// clones don't have a pcs
	if (pcs != null) {
	    // SHOULD do some kind of comparison here
	    // e.g. if pathmap == newvars we know there is nothing to do...
	    pcs.firePropertyChange(propertyName, o, n);
	}
	owner.needSave = true;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public int hashCode() {
        assert false : "hashCode not designed"; // NOI18N
        return 5;
    }

    @Override
    public abstract Object clone();
    public abstract void assign(Object that);
}
