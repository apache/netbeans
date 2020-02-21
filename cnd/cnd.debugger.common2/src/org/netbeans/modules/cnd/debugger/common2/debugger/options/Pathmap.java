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

import java.util.ArrayList;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import java.util.List;

/**
 * Pathmap lists
 *
 * was: ... inlined in RunConfig
 */

public final class Pathmap extends ProfileCategory {

    public static final class Item {
	private final String from;	    // may be null
	private final String to;
	private final boolean appliesToCwd;

	public Item(String from, String to, boolean appliesToCwd) {
	    this.from = from;
	    this.to = to;
	    this.appliesToCwd = appliesToCwd;
	}

	public String from() {
	    return from;
	}

	public String to() {
	    return to;
	}

	public boolean appliesToCwd() {
	    return appliesToCwd;
	}
    }

    private List<Item> pathmap = new ArrayList<Item>();

    public Pathmap(DbgProfile owner) {
	super(owner, DbgProfile.PROP_PATHMAP);
    }

    @Override
    public boolean equals(Object thatObject) {
	// canonical part
	if (this == thatObject)
	    return true;
	if (! (thatObject instanceof Pathmap))
	    return false;
	Pathmap that = (Pathmap) thatObject;

	// per-field part
	if (this.pathmap == that.pathmap)
	    return true;
	if (this.pathmap == null || that.pathmap == null)
	    return false;
	if (this.pathmap.size() != that.pathmap.size())
	    return false;

	for (int px = 0; px < this.pathmap.size(); px++) {
	    Item a = this.pathmap.get(px);
	    Item b = that.pathmap.get(px);

	    if (! (IpeUtils.sameString(a.from, b.from) &&
		   IpeUtils.sameString(a.to, b.to) &&
		   a.appliesToCwd == b.appliesToCwd) ) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public Object clone() {
	Pathmap clone = new Pathmap(owner);

	if (pathmap != null) {
	    clone.pathmap = new ArrayList<Item>();
	    for (Item map : this.pathmap)
		clone.pathmap.add(map);
	}

	return clone;
    }

    @Override
    public void assign(Object thatObject) {
	// canonical part
	if (this.equals(thatObject))
	    return;

	if (! (thatObject instanceof Pathmap))
	    return;
	Pathmap that = (Pathmap) thatObject;

	// per-field part
	Pathmap old = (Pathmap) this.clone();

	// fixe NPE problem when remove all entries in pathmap
	if (that.pathmap != null) {
	    this.pathmap = new ArrayList<Item>();
	    for (Item map : that.pathmap)
		this.pathmap.add(map);
	} else {
	    this.pathmap = null;
	}

	delta(old, this);
    }


    public Item[] getPathmap() {
	if (pathmap == null)
	    return null;
	else
	    return pathmap.toArray(new Item[pathmap.size()]);
    }

    public boolean isDefaultValue() {
	if (count() == 0)
	    return true;
	else
	    return false;
    }

    public void restoreDefaultValue() {
	Pathmap old = (Pathmap) this.clone();
	pathmap = new ArrayList<Item>();

	delta(old, this);
    }

    public int count() {
	if (pathmap == null)
	    return 0;
	else
	    return pathmap.size();
    }

    public void setPathmap(Item [] newmap) {
	db_print("Before setPathmap"); // NOI18N
	this.pathmap = new ArrayList<Item>();
	if (newmap != null) {
	    for (Item map : newmap)
		this.pathmap.add(map);
	}
	db_print("After setPathmap"); // NOI18N
    }


    /**
     * Locate a map entry in 'this' matching 'map'.
     *
     * We match only based on the 'from' field.
     * If we don't then we might end up with a pathmap which has duplicate
     * froms and that is not allowed.
     *
     * @returns index of matching entry.
     */

    private int find(Item map) {
	for (int x = 0; x < pathmap.size(); x++) {
	    Item candidate = pathmap.get(x);
	    if (IpeUtils.sameString(candidate.from, map.from))
		return x;
	}
	return -1;
    }


    /**
     * Extend this pathmap with new maps in 'newmap'.
     * Entries in 'newmap' which "match" (see find()) replace existing
     * entries. Other entries get appended.
     */

    public void extendPathmap(Item [] newmap) {
	db_print("Before extendPathmap"); // NOI18N
	// setPathmap(newmap);
	for (Item nm : newmap) {
	    int match = find(nm);
	    if (match != -1) {
		pathmap.set(match, nm);		// replace
	    } else {
		pathmap.add(nm);
	    }
	}
	db_print("After extendPathmap"); // NOI18N
    }

    private void db_print(String msg) {
	/* DEBUG
	System.out.printf("%s -----------------------\n", msg);
	for (Pathmap.Item map : pathmap) {
	    System.out.printf("\t%s -> %s\n", map.from, map.to);
	}
	*/
    }

    @Override
    public String toString() {
	boolean addSep = false;
	String ret = "";
	if (pathmap != null && pathmap.size() > 0) {
	    for (Item map : pathmap) {
		if (addSep)
		    ret += ","; // NOI18N
		ret += map.from + "->" + map.to; // NOI18N
		addSep = true;
	    }
	}
	return ret;
    }
}

