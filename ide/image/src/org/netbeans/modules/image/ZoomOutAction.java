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


package org.netbeans.modules.image;


import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;


/**
 * Action which zooms out of an image.
 *
 * @author  Lukas Tadial
 */
@ActionID(id = "org.netbeans.modules.image.ZoomOutAction", category = "View")
@ActionRegistration(lazy = false, displayName = "#LBL_ZoomOut")
public class ZoomOutAction extends CallableSystemAction {

    /** Generated serial version UID. */
    static final long serialVersionUID = 1859897546585041051L;


    /** Peforms action. */
    @Override
    public void performAction() {
        TopComponent curComponent = TopComponent.getRegistry().getActivated();
        if(curComponent instanceof ImageViewer)
            ((ImageViewer) curComponent).zoomOut();

    }

    /** Gets name of action. Implements superclass abstract method. */
    @Override
    public String getName() {
        return NbBundle.getMessage(ZoomOutAction.class, "LBL_ZoomOut");
    }

    /** Gets help context for action. Implements superclass abstract method. */
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Overrides superclass method. */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /** Gets icon resource. Overrides superclass method. */
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/image/zoomOut.gif"; // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
