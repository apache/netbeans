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

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import java.awt.Component;

import javax.swing.JDialog;

import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.LastLocation;

import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordList;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.MasterDetailView;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.MasterView;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.DetailView;

public class HostListEditor extends MasterDetailView {

    private final CustomizableHostList original;
    private final RecordList<CustomizableHost> editable;	// working copy

    
    private JDialog dialog;
    private static LastLocation lastLocation = new LastLocation(700, 0);

    public void showDialog(Component parent) {
	dialog = IpeUtils.createDialog(parent, getTitleText(), this);
	installInDialog(dialog);
	Catalog.setAccessibleDescription(dialog,
	    "ACSD_Hosts");	// NOI18N
	// LATER lastLocation.applyto(dialog);

	// will block until closeDialog is called
	dialog.setVisible(true);
    }

    public HostListEditor() {
	super(null, MasterDetailView.WITHLIST, null);

	original = CustomizableHostList.getInstance();
	editable = original.cloneList();

        CustomizableHost localhost = editable.getRecordAt(0);
	DetailView<CustomizableHost> detailView = new HostPanel(localhost);

	MasterView<CustomizableHost> masterView = new MasterView<CustomizableHost>(editable, detailView);

	addMasterView(masterView);
	addDetailView(detailView);
    }

    protected String getTitleText() {
	return Catalog.get("REMOTE_DIALOG_TITLE"); // NOI18N
    }

    protected String getGuidanceText() {
	return Catalog.get("REMOTE_DIALOG_GUIDANCE_TXT"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("RemoteHost"); // FIXUP
    }


    // implement MasterDetailView.Actions
    @Override
    public void handleCommit() {
	Host lastone = editable.getRecordAt(editable.getCurrentRecordIndex());
	original.copyList(editable, lastone.getHostName() );
    }

    // implement MasterView.Actions
    @Override
    public void handleClose() {
	closeDialog();
    }

    private void closeDialog() {
	lastLocation.rememberFrom(dialog);
        dialog.setVisible(false);
        dialog.dispose();
	dialog = null;
    }
}
