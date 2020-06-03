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

import java.awt.event.ActionEvent;

import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.utils.FlyweightAction;

/**
 * Suppress the last access error in the current RtcView.
 *
 * SHOULD be sensitive to RtcOption.RTC_AUTO_SUPPRESS ... tooltip SHOULD
 * say "automatically suppressed".
 */

final class SuppressAction extends FlyweightAction {

    private final static class SharedSuppressAction extends Shared {
	// like SystemAction
	protected String iconResource() {
	    return "org/netbeans/modules/cnd/debugger/dbx/resources/icons/rtc/suppress_16.png";  // NOI18N
	}

	// like SystemAction
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("Debugging_RTC"); // NOI18N
	}

	// interface SystemAction
	protected void initialize() {
	    super.initialize();

	    // SHOULD pick one:
	    putValue(NAME, Catalog.get("SuppressLast"));
	    putValue(SHORT_DESCRIPTION, Catalog.get("Rtc_Suppress"));

	    setMnemonic(Catalog.getMnemonic("MNEM_SuppressLast"));
	    // LATER setAccelerator("control U");
	    // LATER:
	    // Catalog.setAccessibleDescription(bSuppress, "ACSD_SuppressLast");
	}
    }

    private final RtcView rtcView;

    public SuppressAction(RtcView rtcView) {
	super(SharedSuppressAction.class);
	this.rtcView = rtcView;
    }

    // interface Action
    public void actionPerformed(ActionEvent e) {
	if (rtcView == null)
	    return;
	if (rtcView.getController() == null)
	    return;
	rtcView.getController().suppressLastError();
    }
}
