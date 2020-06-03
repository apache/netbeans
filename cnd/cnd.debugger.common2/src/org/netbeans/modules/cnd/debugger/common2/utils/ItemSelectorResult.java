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


package org.netbeans.modules.cnd.debugger.common2.utils;

/**
 * Hold the result of ItemSelectorDialog
 */
public final class ItemSelectorResult {

    private final int[] selected_indices;
    private final boolean cancelled;

    private int routingToken;

    /**
     * Factory for a result indicating that all chocies were made.
     * Used when we automatically want to choose all w/o burdening user
     * w/ a popup.
     */
    public static ItemSelectorResult selectAll(int nitems) {
	int[] selected = new int[nitems];
	for (int i = 0; i < nitems; i++)
	    selected[i] = i;
	return new ItemSelectorResult(false, selected);
    }

    /**
     * Factory for a set of choices
     */
    static ItemSelectorResult select(int[] selected) {
	return new ItemSelectorResult(false, selected);
    }

    /**
     * Factory for a cancelled result.
     */
    static ItemSelectorResult cancelled() {
	return new ItemSelectorResult(true, null);
    }

    public boolean isCancelled() {
	return cancelled;
    }

    /**
     * Return number of selected items; 0 if cancelled.
     */
    public int nSelected() {
	if (cancelled)
	    return 0;
	else
	    return selected_indices.length;
    }

    /**
     * Return selected items.
     */
    public int[] selections() {
	return selected_indices;
    }

    public void setRoutingToken(int routingToken) {
	this.routingToken = routingToken;
    }

    public int getRoutingToken() {
	return routingToken;
    }

    private ItemSelectorResult(boolean cancelled, int[] selected_indices) {
	
	this.cancelled = cancelled;
	this.selected_indices = selected_indices;

	if (cancelled) {
	    assert selected_indices == null;
	} else {
	    assert selected_indices != null;
	}
    }
} 
