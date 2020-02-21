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

/*
 * "Exceptions.java"
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * C++ Exception intercept/unintercept lists
 *
 * was: ... inlined in RunConfig
 */

public final class Exceptions extends ProfileCategory {

    private String[] interceptList = null;
    private String[] interceptExceptList = null;
    private boolean interceptUnhandled = true;
    private boolean interceptUnexpected = true;
    private boolean all = false;

    public Exceptions(DbgProfile owner) {
	super(owner, DbgProfile.PROP_INTERCEPTLIST);
    } 

    @Override
    public boolean equals(Object thatObject) {
	// canonical part
	if (this == thatObject)
	    return true;
	if (! (thatObject instanceof Exceptions))
	    return false;
	Exceptions that = (Exceptions) thatObject;

	// per-field part
	if (this.interceptUnhandled != that.interceptUnhandled)
	    return false;
	if (this.interceptUnexpected != that.interceptUnexpected)
	    return false;
	if (this.all != that.all)
	    return false;
	if (!IpeUtils.sameStringArray(this.interceptList,
				      that.interceptList))
	    return false;
	if (!IpeUtils.sameStringArray(this.interceptExceptList,
				      that.interceptExceptList))
	    return false;

	return true;
    }

    @Override
    public Object clone() {
	Exceptions clone = new Exceptions(null);
	clone.interceptUnhandled = this.interceptUnhandled;
	clone.interceptUnexpected = this.interceptUnexpected;
	clone.all = this.all;
	clone.interceptList = IpeUtils.cloneStringArray(this.interceptList);
	clone.interceptExceptList = IpeUtils.cloneStringArray(this.interceptExceptList);
	return clone;
    }

    @Override
    public void assign(Object thatObject) {
	if (this.equals(thatObject))
	    return;
	if (! (thatObject instanceof Exceptions))
	    return;
	Exceptions that = (Exceptions) thatObject;

	Exceptions old = (Exceptions) this.clone();

	this.interceptUnhandled = that.interceptUnhandled;
	this.interceptUnexpected = that.interceptUnexpected;
	this.all = that.all;
	this.interceptList = IpeUtils.cloneStringArray(that.interceptList);
	this.interceptExceptList = IpeUtils.cloneStringArray(that.interceptExceptList);
	delta(old, this);
    }


    public String[] getInterceptList() {
	if (interceptList == null)
	    return new String[0];
	else
	    return interceptList;
    }

    public String[] getInterceptExceptList() {
	if (interceptExceptList == null)
	    return new String[0];
	else
	    return interceptExceptList;
    }

    public boolean isInterceptUnhandled() {
	return interceptUnhandled;
    }

    public boolean isInterceptUnexpected() {
	return interceptUnexpected;
    }

    public boolean isAll() {
	return all;
    }

    public boolean isDefaultValue() {
	/* DEBUG
	System.out.printf("Exceptions isDefault(): ======================\n");
	System.out.printf("\t                all: %s\n", all);
	System.out.printf("\t      interceptList: %s\n",
	    interceptList != null? interceptList.length: "null");
	System.out.printf("\tinterceptExceptList: %s\n",
	    interceptExceptList != null? interceptExceptList.length: "null");
	System.out.printf("\t interceptUnhandled: %s\n", interceptUnhandled);
	System.out.printf("\tinterceptUnexpected: %s\n", interceptUnexpected);
	*/

	return all == false &&
	       (interceptList == null ||
		interceptList.length == 0) &&
	       (interceptExceptList == null ||
		interceptExceptList.length == 0) &&
	       interceptUnhandled == true &&
	       interceptUnexpected == true;
    }

    public void restoreDefaultValue() {
	Exceptions old = (Exceptions) this.clone();

	all = false;
	interceptList = null;
	interceptExceptList = null;
	interceptUnhandled = true;
	interceptUnexpected = true;

	delta(old, this);
    }


    public void setInterceptList(String typenames[],
				 boolean unhandled, boolean unexpected) {

	// SHOULD do the right thing with 'all'.
	// For now 'checkForAll' has a side-effect.

	String[] newInterceptList = checkForAll(typenames);

	if (IpeUtils.sameStringArray(interceptList, newInterceptList) &&
	    interceptUnhandled == unhandled &&
	    interceptUnexpected == unexpected) {

	    return;
	}

	interceptList = newInterceptList;
	interceptUnhandled = unhandled;
	interceptUnexpected = unexpected;

	delta(null, null);
    }

    public void setInterceptExceptList(String typenames[]) {
	if (IpeUtils.sameStringArray(interceptExceptList, typenames))
	    return;
	interceptExceptList = typenames;
	delta(null, null);
    }

    public void setInterceptList(String typenames[], String xtypenames[],
				 boolean all,
				 boolean unhandled, boolean unexpected) {

	if (IpeUtils.sameStringArray(interceptList, typenames) &&
	    IpeUtils.sameStringArray(interceptExceptList, xtypenames) &&
	    this.all == all &&
	    interceptUnhandled == unhandled &&
	    interceptUnexpected == unexpected) {

	    return;
	}

	interceptList = typenames;
	interceptExceptList = xtypenames;
	this.all = all;
	interceptUnhandled = unhandled;
	interceptUnexpected = unexpected;
	delta(null, null);
    }

    /**
     * Convert an array of the form 
     *	{"A", "-all", "B"}
     * to
     *	{"A", "B"}
     * And remember, in 'all', if you saw a "-all".
     */
    private String[] checkForAll(String[] list) {
	for (int lx = 0; lx < list.length; lx++) {
	    if ("-all".equals(list[lx]) || "-a".equals(list[lx])) { // NOI18N
		all = true;

		// eliminate the "all" entry from 'list'
		String[] list2 = new String[list.length-1];
		// copy up to but not the all
		System.arraycopy(list, 0, list2, 0, lx);
		// copy from after all til the end
		System.arraycopy(list, lx+1, list2, lx, list.length-lx-1);
		return list2.length == 0? null: list2;
	    }
	}
	all = false;
	return list;
    }

    @Override
    public String toString() {
	String ret = "";
	boolean addSep = false;
	boolean needSpace = false;

	if (all) {
	    ret += Catalog.get("STR_InterceptAll"); // NOI18N
	    needSpace = true;
	} else {
	    addSep = false;
	    if (interceptList != null && interceptList.length > 0) {
		if (needSpace)
		    ret += " ";					// NOI18N
		ret += Catalog.get("STR_Intercept") + "=";	// NOI18N
		for (int i = 0; i < interceptList.length; i++) {
		    if (addSep)
			ret += ","; // NOI18N
		    ret += interceptList[i];
		    addSep = true;
		}
		needSpace = true;
	    }
	}

	addSep = false;
	if (interceptExceptList != null && interceptExceptList.length > 0) {
	    if (needSpace)
		ret += " ";					// NOI18N
	    ret += Catalog.get("STR_Ignore") + "=";	// NOI18N
	    for (int i = 0; i < interceptExceptList.length; i++) {
		if (addSep)
		    ret += ","; // NOI18N
		ret += interceptExceptList[i];
		addSep = true;
	    }
	    needSpace = true;
	}

	if (interceptUnhandled) {
	    if (needSpace)
		ret += " ";					// NOI18N
	    ret += Catalog.get("STR_Unhandled");		// NOI18N
	    needSpace = true;
	}

	if (interceptUnexpected) {
	    if (needSpace)
		ret += " ";					// NOI18N
	    ret += Catalog.get("STR_Unexpected");		// NOI18N
	    needSpace = true;
	}

	return ret;
    }
}

