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
