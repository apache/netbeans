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

package org.netbeans.modules.cnd.debugger.common2.utils.props;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of Breakpoint Properties Owner
 */

public class PropertyOwnerSupport implements PropertyOwner {

    private List<Property> props = new LinkedList<Property>();

    private boolean dirty;

    public PropertyOwnerSupport() {
    }

    @Override
    public void register(Property p) {
        props.add(p);
    }


    /**
     * Find a property by it's name in this owner.
     * equals() is used for name comparison.
     */

    @Override
    public Property propertyByName(String name) {
	for (Property p : props) {
	    if (p.name().equals(name)) {
		return p;
            }
	}
	return null;
    }

    /**
     * Find a property by it's key in this owner.
     * == is used for key comparison.
     */

    @Override
    public Property propertyByKey(String key) {
        for (Property p : props) {
	    if (p.key() == key) {
		return p;
            }
	}
	return null;
    }


    /**
     * Number of properties in this owner.
     */
    @Override
    public int size() {
	return props.size();
    } 


    // interface Iterable
    @Override
    public Iterator<Property> iterator() {
	return props.iterator();
    }


    /**
     * Set the dirty bit.
     * To be called by Property.
     */
    void setDirty() {
	dirty = true;
	propagateDirty();
    }

    protected void propagateDirty() {
    }

    /**
     * Clear the dirty bit.
     */
    @Override
    public void clearDirty() {
	dirty = false;
    } 

    /**
     * Query the dirty bit.
     */
    @Override
    public boolean isDirty() {
	return dirty;
    } 

    @Override
    public boolean equals(PropertyOwner that, Comparator comparator) {
	Iterator<Property> thisIter = this.iterator();
	Iterator<Property> thatIter = that.iterator();

	while (true) {
	    if (thisIter.hasNext() && thatIter.hasNext()) {
		Property thisP = thisIter.next();
		Property thatP = thatIter.next();
		if (! comparator.equals(thisP, thatP)) {
		    return false;
		}
	    } else if (thisIter.hasNext() || thatIter.hasNext()) {
		// mismatched property lengths
		// 6791860
		return false;
	    } else {
		// we've reached the end
		break;
	    }
	}

	return true;
    }

    public void assign(PropertyOwner that) {
	for (Property thatProp : that) {
	    String thatName = thatProp.name();
	    Object thatObject = thatProp.getAsObject();
	    for (Property thisProp : this) {
	        String thisName = thisProp.name();
		if (thisName != null && thisName.equals(thatName)) {
		    if (thatProp.isDirty())
			thisProp.setFromObject(thatObject);
		}
	    }
	}
    }
} 
