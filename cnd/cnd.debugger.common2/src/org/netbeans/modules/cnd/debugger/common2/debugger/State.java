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
