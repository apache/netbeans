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

package org.netbeans.modules.cnd.debugger.common2.debugger;

/**
 * A random collection of information shadowing the state of the engine.
 * Used _strictly_ for the following:
 * - ActionEnabler and other actions enabledness decision making.
 * - Echoing in Session view.
 */

public final class State {
    public boolean isRunning;	// tracks glue proc_go/stopped
    public boolean isBusy;	
    public boolean isCore;
    public boolean isAttach;
    public boolean isProcess;	// tracks glue proc_new*/gone etc.
    public boolean isLoading;	// tracks glue prog_loading/loaded
    public boolean isLoaded;	// tracks glue prog_loaded
    public boolean is64bit;	// tracks glue prog_datamodel

    // the following typically track Locations
    public boolean isDownAllowed;
    public boolean isUpAllowed;
    public boolean isDebuggerCall;

    public boolean accessOn;
    public boolean memuseOn;
    public boolean leaksOn;

    public boolean multi_threading;

    // the following represent capabilities
    public boolean capabAccess;		// access checking is available
    public boolean capabMprof;		// leaks and memuse is available
    public boolean capabCollector;	// collector is available
    public boolean capabAutoRun;	// step/next/etc initiate run
    public String capabilities;		// dbx engine capabilities

    // interface Object
    @Override
    public String toString() {
	String state = "";	// NOI18N;
	if (isLoading) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "Loading"; // NOI18N
	}
	if (isLoaded) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "Loaded"; // NOI18N
	}
	if (isProcess) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "Process"; // NOI18N
	}
	if (isRunning) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "Running"; // NOI18N
	}
	if (isBusy) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "Busy"; // NOI18N
	}
	if (isCore) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "Core"; // NOI18N
	}
	if (isDownAllowed) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "DownAllowed"; // NOI18N
	}
	if (isUpAllowed) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "UpAllowed"; // NOI18N
	}
	if (isDebuggerCall) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "DebuggerCall"; // NOI18N
	}

	if (accessOn) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "accessOn"; // NOI18N
	}
	if (memuseOn) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "memuseOn"; // NOI18N
	}
	if (leaksOn) {
	    if (state.length() > 0)
		state += " "; // NOI18N
	    state += "leaksOn"; // NOI18N
	}
	return state;
    } 

    public boolean isListening() {
	return !(isRunning || isBusy);
    }
} 
