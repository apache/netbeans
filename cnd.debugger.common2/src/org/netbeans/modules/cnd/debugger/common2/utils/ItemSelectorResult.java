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
