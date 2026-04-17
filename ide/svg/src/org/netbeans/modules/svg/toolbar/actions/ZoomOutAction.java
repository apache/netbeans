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
package org.netbeans.modules.svg.toolbar.actions;

import org.netbeans.modules.svg.SVGViewerElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Action which zooms out of an svg.
 *
 * @author Christian Lenz
 */
@ActionID(id = "org.netbeans.modules.svg.toolbar.ZoomOutAction", category = "View")
@ActionRegistration(lazy = false, displayName = "#LBL_ZoomOut")
public class ZoomOutAction extends CallableSystemAction {

    /**
     * Generated serial version UID.
     */
    static final long serialVersionUID = 1859897546585041051L;

    /**
     * Peforms action.
     */
    @Override
    public void performAction() {
        TopComponent currentComponent = TopComponent.getRegistry().getActivated();
        Lookup tcLookup = currentComponent != null ? currentComponent.getLookup() : null;
        SVGViewerElement svgViewerElement = tcLookup != null ? tcLookup.lookup(SVGViewerElement.class) : null;
        if (svgViewerElement != null) {
            svgViewerElement.zoomOut();
        }
    }

    /**
     * Gets name of action. Implements superclass abstract method.
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(ZoomOutAction.class, "LBL_ZoomOut");
    }

    /**
     * Gets help context for action. Implements superclass abstract method.
     */
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Overrides superclass method.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Gets icon resource. Overrides superclass method.
     */
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/image/zoomOut.gif"; // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
