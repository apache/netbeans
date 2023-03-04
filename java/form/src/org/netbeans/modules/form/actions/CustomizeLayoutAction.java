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

package org.netbeans.modules.form.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.netbeans.modules.form.*;

/** CustomizeLayout action - enabled on RADContainerNodes and RADLayoutNodes.
 *
 * @author   Ian Formanek
 */

public class CustomizeLayoutAction extends CookieAction {

    private static String name;

    /** @return the mode of action. Possible values are disjunctions of MODE_XXX
     * constants. */
    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /** Creates new set of classes that are tested by the cookie.
     *
     * @return list of classes the that the cookie tests
     */
    @Override
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    @Override
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(CustomizeLayoutAction.class)
                     .getString("ACT_CustomizeLayout"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualContainer) {
                Node layoutNode = ((RADVisualContainer)metacomp)
                                      .getLayoutNodeReference();
                if (layoutNode != null && layoutNode.hasCustomizer())
                    NodeOperation.getDefault().customize(layoutNode);
            }
        }
    }

    /*
     * In this method the enable / disable action logic can be defined.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if (metacomp instanceof RADVisualContainer) {
                    Node layoutNode = ((RADVisualContainer)metacomp)
                                      .getLayoutNodeReference();
                    return layoutNode != null && layoutNode.hasCustomizer();
                }
            }
        }
        return false;
    }
}
