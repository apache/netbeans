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
