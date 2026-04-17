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

package org.openide.util;

import javax.swing.Action;

/**
 * Interface to be implemented by an action whose behavior
 * is dependent on some context.
 * The action created by {@link #createContextAwareInstance}
 * is bound to the provided context: {@link Action#isEnabled},
 * {@link Action#actionPerformed}, etc. may be specific to that context.
 * <p class="nonnormative">For example, the action representing a context menu item will usually implement
 * this interface. When the actual context menu is created, rather than making a
 * presenter for the generic action, the menu will contain a presenter for the
 * context-aware instance. The context will then be taken from the GUI
 * environment where the context menu was shown; for example it may be a
 * <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent's context</a>,
 * often taken from an activated node selection. The context action might be
 * enabled only if a certain "cookie" is present in that selection. When invoked,
 * the action need not search for an object to act on, since it can use the context.
 *
 * @author Jaroslav Tulach, Peter Zavadsky
 *
 * @see <a href="https://netbeans.apache.org/wiki/DevFaqActionContextSensitive">NetBeans FAQ</a>
 * @see org.openide.util.Utilities#actionsToPopup
 * @see org.openide.util.Utilities#actionsGlobalContext
 * @since 3.29
 */
public interface ContextAwareAction extends Action {

    /**
     * Creates action instance for provided context.
     * @param actionContext an arbitrary context (e.g. "cookies" from a node selection)
     * @return a transient action whose behavior applies only to that context
     */
    public Action createContextAwareInstance(Lookup actionContext);

}
