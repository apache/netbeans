/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.javadoc.search;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/**
 * Search doc global action (working with whole solution).
 *
 * @author   Petr Hrebejk
 */
public final class SearchDocAction extends CallableSystemAction {

    public SearchDocAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName () {
        return NbBundle.getBundle (SearchDocAction.class).getString ("CTL_SEARCH_MenuItem");
    }

    /** The action's icon location.
     * @return the action's icon location
     */
    protected String iconResource () {
        return "org/netbeans/modules/javadoc/resources/searchDoc.gif"; // NOI18N
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SearchDocAction.class);
    }

    /** This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     * This default implementation calls the assigned actionPerformer if it
     * is not null otherwise the action is ignored.
     */
    public void performAction () {

        final IndexSearch indexSearch = IndexSearch.getDefault();

        String toFind = GetJavaWord.getCurrentJavaWord();

        if (toFind != null)
            indexSearch.setTextToFind( toFind );

        indexSearch.open();
        indexSearch.requestActive();
    }
	
	public boolean asynchronous() {
		return false;
	}
}
