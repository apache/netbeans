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

import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.api.debugger.Session;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * Our prallel object to debuggercore Sessions.
 *
 * Stores information to be presented in sessions view which is accessed
 * in SessionFilter.
 *
 * Registered in
 *	META-INF/debugger/
 *	org.netbeans.modules.cnd.debugger.common2.debugger.NativeSession
 * This one is unusual in that it registers itself in itself.
 */

public final class NativeSession {

    private ContextProvider ctx;
    private Session coreSession;
    private NativeDebugger debugger;

    private static int nextSerialNumber = 0;
    private int serialNumber = 0;


    public NativeSession(ContextProvider ctx) {
	this.ctx = ctx;
	coreSession = ctx.lookupFirst(null, Session.class);

	serialNumber = nextSerialNumber++;

	// the following relies crucially on
	// DebuggerManager.registerSessionModel() having been called
	// already.
	setUpdater(NativeDebuggerManager.get().sessionUpdater());
    } 

    // point back to debugger
    public void setDebugger(NativeDebugger nd) {
	debugger = nd;
    }

    public NativeDebugger getDebugger() {
        return debugger;
    }
    
    /**
     * Get [debugger]core session corresponding to us.
     */
    public Session coreSession() {
	return coreSession;
    } 

    /**
     * Map a debuggercore Session to one of ours
     */

    public static NativeSession map(Session coreSession) {
	return coreSession.lookupFirst(null, NativeSession.class);
    }


    private ModelListener updater;

    private void setUpdater(ModelListener updater) {
	this.updater = updater;
    }

    public void update() {
	// OLD updater.treeNodeChanged(coreSession);
	updater.modelChanged(new ModelEvent.NodeChanged(this, coreSession));
    } 


    /**
     * Switch to this session
     */

    public void makeCurrent() {
	NativeDebuggerManager.get().setCurrentSession(coreSession);
    }

    public void kill() {
	coreSession.kill();
    }


    /**
     * Is this session target name unique between all sessions?
     */

    private boolean isUnique() {
	NativeSession sessions[] = NativeDebuggerManager.get().getSessions();
	String thisName = getShortName();
	boolean programUnique = true;
	for (int sx = 0; sx < sessions.length; sx++) {
	    NativeSession thatSession = sessions[sx];
	    if (thatSession != this) {
		if (IpeUtils.sameString(thisName, thatSession.getShortName())) {
		    programUnique = false;
		    break;
		}
	    }
	}
	return programUnique;
    }

    //
    // Attributes
    //

    /**
     * Return a uniquified short session name, suitable for the Name column.
     */

    // "interface" Session
    public String getName() {
	String name = getShortName();
	if (!isUnique())
	    name += " # " + serialNumber; // NOI18N
	return name;
    }

    @Override
    public String toString() {
	String s = "";				// NOI18N
	if (target != null)
	    s += target;
	else
	    s += "-";				// NOI18N

	if (corefile != null) {
	    s += " " + corefile + " ";		// NOI18N
	}
	if (pid != -1) {
	    s += " (" + pid + ")";		// NOI18N
	}
	return s;
    } 

    private EngineType engine = null;
    public final void setSessionEngine(EngineType s) {
	engine = s;
    }

    public final EngineType getSessionEngine() {
	return engine;
    }

    private State state = null;
    public void setSessionState(State s) {
	state = s;
    }

    public String getSessionState() {
	if (state == null)
            return Catalog.get("MSG_session_paused"); // NOI18N
        
        if (!state.isProcess) {
            return Catalog.get("MSG_session_exited"); // NOI18N
        } else if (state.isRunning) {
            return Catalog.get("MSG_session_running"); // NOI18N
        } else if (!state.isRunning && !state.isCore) {
            return Catalog.get("MSG_session_paused"); // NOI18N
        } else if (state.isCore) {
            return Catalog.get("MSG_session_core_file"); // NOI18N
        } else {
            return Catalog.get("MSG_session_paused"); // NOI18N
        }
    }

    // get core file location if this session is debugging core file
    public String getSessionCore() {
	NativeDebuggerInfo ddi = debugger.getNDI();

	if (ddi != null) 
	   return ddi.getCorefile();
	else 
	   return "";

    }

    public String getSessionLocation() {
	return getTarget();
    /*
	NativeDebuggerInfo ddi = debugger.getNDI();

	if (ddi != null) 
	   return ddi.getTarget();
	else 
	   return "";
   */
    }

    public String getSessionArgs() {
	NativeDebuggerInfo ddi = debugger.getNDI();

	if (ddi != null) 
	   return ddi.getArgsFlat();
	else 
	   return "";
    }
    
    public String getSessionMode() {
	StringBuffer mode = new StringBuffer();

	if (debugger.isMultiThreading())
	    mode.append(" MT"); // NOI18N
	/* LATER
	if (debugger.isAccessCheckingEnabled())
	    mode.append(" Access"); // NOI18N
	if (debugger.isMemuseEnabled())
	    mode.append(" MemUse"); // NOI18N
	*/

	return mode.toString().trim(); // NOI18N
    }


    private long pid = -1;
    public long getPid() {
	return pid;
    }
    public void setPid(long pid) {
	this.pid = pid;
	update();
    } 

    private String target;
    public String getTarget() {
	if (target == null)
	    return "-"; // NOI18N
	else 
	    return target;
    } 
    public void setTarget(String target) {
	this.target = target;

	shortName = null;	// reset cache

	// force a re-rendering of the _whole_ tree becauase unique names
	// might get affected by this.
	if (updater != null) {
	    // OLD updater.treeChanged();
	    updater.modelChanged(new ModelEvent.TreeChanged(this));
	}
    } 

    private String shortName = null;
    public String getShortName() {
	if (target == null)
	    return "";
	else {
	    if (shortName == null)
		shortName = CndPathUtilities.getBaseName(target);
	    return shortName;
	}
    }

    private String corefile;
    public String getCorefile() {
	return corefile;
    }
    public void setCorefile(String corefile) {
	this.corefile = corefile;
    }

    /* LATER
    private String currentLanguage;
    public String getCurrentLanguage() {
	return "unknown";
    } 

    public void setCurrentLanguage() {
	this.currentLanguage = currentLanguage;
    }
    */

    public void setSessionHost(String h) {
	hostName = h;
    }


    private String hostName;
    public String getSessionHost() {
	if (IpeUtils.isEmpty(hostName))
	    return "localhost"; // NOI18N
	else
	    return hostName;
    }
}
