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
