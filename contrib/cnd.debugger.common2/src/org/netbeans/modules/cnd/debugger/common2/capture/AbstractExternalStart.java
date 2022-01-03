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

package org.netbeans.modules.cnd.debugger.common2.capture;

import org.openide.awt.StatusDisplayer;

/**
 *
 */
public class AbstractExternalStart {

    private boolean running = false;

    //
    // partial implementation of interface ExtrnalStart
    //
    public final void fail() {
	ExternalStartManager.fail("LBL_dbxFailure"); // NOI18N
    }

    public final boolean isRunning() {
	return running;
    }

    public final void debuggerStarted() {
	// ensure that the test of 'debuggerStarted' at the end of
	// runJob() hasn't happenned yet because of a race condition caused
	// by asynch startup of debugger
	ExternalStartManager.debuggerStarted();
    }

    protected AbstractExternalStart() {
    }

    private void status(String msg) {
	StatusDisplayer.getDefault().setStatusText(msg);
    }

    protected final void setRunning(boolean running) {
	this.running = running;
	if (running) {
	    status(Catalog.get("LBL_enable_ss_attach"));	// NOI18N
	} else {
	    status(Catalog.get("LBL_disable_ss_attach"));	// NOI18N
	}
    }
}
