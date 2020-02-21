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


package org.netbeans.modules.cnd.debugger.dbx.rtc;

import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.util.ImageUtilities;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Mode;


public class RtcViewer extends TopComponent {

    // should match tc-id tags in
    //	groups/.../*.wstcgrp
    //	modes/.../*.wstcref
    static final String preferredID = "RtcViewer";

    // LATER private static final long serialVersionUID = 0;

    private static final String ICON_RESOURCE =
	"org/netbeans/modules/cnd/debugger/common2/icons/Refresh.png";

    public RtcViewer() {
	setIcon(ImageUtilities.loadImage(ICON_RESOURCE));

	Mode mode = WindowManager.getDefault().findMode("atactionsmode");
	if (mode != null)
	    mode.dockInto(this);
    }

    // interface TopComponent
    public int getPersistenceType() {
	return TopComponent.PERSISTENCE_NEVER;
    }

    private java.awt.BorderLayout layout;
    private RtcView view;

    public void setView(RtcView view) {
	if (layout == null) {
	    layout = new java.awt.BorderLayout();
	    setLayout(layout);
	}

	// out with the old ...
	if (this.view != null)
	    remove(this.view);

	// in with the new
	this.view = view;
	add(view, java.awt.BorderLayout.CENTER);

	setName(CndPathUtilities.getBaseName(view.getName()));
	setToolTipText(view.getName());
    }

    // interface TopComponent
    protected String preferredID() {
	return preferredID;
    }

    // interface TopComponent
    protected void componentClosed() {
	if (Log.Rtc.debug)
	    System.out.printf("RtcViewer.componentClosed()\n");
	if (view != null)
	    view.componentClosed();
    }

    // interface TopComponent
    protected void componentActivated() {
	// if (Log.Rtc.debug)
	    System.out.printf("RtcViewer.componentActivated()\n");
	super.componentActivated();
	if (view != null)
	    view.focusToFirstTab();
    }

}
