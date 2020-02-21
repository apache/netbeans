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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import java.awt.BorderLayout;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;

import  org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

public final class DisassemblerWindow extends TopComponent {

    private DisView view ;

    private final String name =
	Catalog.get("TITLE_DisassemblerWindow");    //NOI18N
    private NativeDebugger debugger = null;

    static final String preferredID = "DisassemblerWindow"; // NOI18N
    static DisassemblerWindow DEFAULT;


    public synchronized static DisassemblerWindow getDefault() {
	if (DEFAULT == null) {
	    DisassemblerWindow tc = (DisassemblerWindow) WindowManager.getDefault().
					findTopComponent(preferredID);
	    if (tc == null)
		new DisassemblerWindow();
	}
	return DEFAULT;
    }

    public DisassemblerWindow() {
	super.setName(name);
	setIcon(org.openide.util.ImageUtilities.loadImage
	    (Catalog.get("ICON_DisassemblerView"))); // NOI18N
	DEFAULT = this;
	initView();
    }

    public void initView () {
	if (view == null) {
	    setLayout (new BorderLayout ());
	    view = new DisView();
	}
        add (view.getComponent(), "Center");  // NOI18N
    }

    // interface TopComponent
    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }

    // interface TopComponent
    @Override
    public void componentHidden () {
//	if (debugger != null)
//	    debugger.registerDisassemblerWindow(null);
        super.componentHidden();
    }
    
    // interface TopComponent
    @Override
    public void componentShowing () {
	if (debugger == null) return;
//	debugger.registerDisassemblerWindow(this);
        super.componentShowing ();
	view.getController().requestDis(true);
    }

    // interface TopComponent
    @Override
    protected void componentClosed () {
//	if (debugger != null) {
//	    debugger.registerDisassemblerWindow(null);
//	}
        super.componentClosed();
    }

    // interface TopComponent
    @Override
    public void componentActivated() {
	super.componentActivated();
	view.componentActivated();
    }

    // interface TopComponent
    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public String getName () {
        return name;
    }
    
    @Override
    public String getToolTipText () {
	return Catalog.get("TIP_DisWindow");	// NOI18N
    }

    public DisView getView() {
	return view;
    }

    public void setDebugger(NativeDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("DisassemblerWindow");
    }

}
