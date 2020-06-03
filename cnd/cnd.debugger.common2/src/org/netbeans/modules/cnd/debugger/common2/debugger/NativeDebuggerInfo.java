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

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.debugger.common2.DbgActionHandler;
import org.netbeans.modules.cnd.makeproject.api.configurations.*;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;

import org.netbeans.modules.cnd.debugger.common2.capture.CaptureInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;


/**
 * DebuggerInfo is used to communicate session startup parameters through
 * debuggercore.
 * 
 * getID() also governs what type of debugger we start.
 *
 * This information is used in DebuggerManager.debugNoAsk() to create
 * a generic DebuggerInfo which passes through debugger core and is
 * retrieved using lookup in StartAction.
 */

public abstract class NativeDebuggerInfo {

    public interface Factory {
        public NativeDebuggerInfo create(EngineType debuggerTypeId);
    }

    private final EngineType debuggerType;
    private DbgProfile dbgProfile = null;

    protected NativeDebuggerInfo(EngineType debuggerType) {
	this.debuggerType = debuggerType;
    } 

    public abstract String getID();

    private String target;

    public final String getTarget() {
	if (debugtarget != null) {
	    return debugtarget.getExecutable();
        }
	return target;
    } 

    public final void setTarget(String target) {
	if (debugtarget != null) {
	    debugtarget.setExecutable(target);
        }
	this.target = target;
    } 

    private String corefile;
    public final String getCorefile() {
	return corefile;
    }
    public final void setCorefile(String corefile) {
	this.corefile = corefile;
    }

    private String hostName;
    public final String getHostName() {
	return hostName;
    } 

    public final void setHostName(String hostName) {
	this.hostName = hostName;
    } 

    private long pid = -1;
    public final long getPid() {
	return pid;
    } 
    public final void setPid(long pid) {
	this.pid = pid;
    } 
    
    public String getRunDir() {
        return DebuggerSettingsBridge.getRunDir(getDbgProfile(), profile);
    }

    public final String[] getArguments() {
	if (profile != null)
	    return profile.getArgsArray();
	else
	    return null;
    } 

    private String argsFlat = null;
    public final String getArgsFlat() {
	if (profile != null)
	    return profile.getArgsFlat();
	else
	    return null;
    } 

    private Configuration configuration;

    public final Configuration getConfiguration() {
	return configuration;
    }

    public final void setConfiguration(Configuration configuration) {
	this.configuration = configuration;
    }
    
    private final Object lock = new Object();
    private volatile Project project;
    
    /*package*/ final Project getProject() {
        synchronized (lock) {
            if (project == null) {
                FileObject projectDir = getConfiguration().getBaseFSPath().getFileObject();
                for (Project openProject : OpenProjects.getDefault().getOpenProjects()) {
                    if (openProject.getProjectDirectory().equals(projectDir)) {
                        project = openProject;
                    }
                }
            }
        }
        
        return project;
    }

    private DebugTarget debugtarget;
    public final void setDebugTarget(DebugTarget dt) {
	this.debugtarget = dt;
    }

    public final DebugTarget getDebugTarget() {
	return debugtarget;
    }

    private boolean loadsucceed;
    public final void setLoadSuccess(boolean s) {
	loadsucceed = s;
    }

    public final boolean loadSucceed() {
	return loadsucceed;
    }

    private RunProfile profile = null;
    public final RunProfile getProfile() {
	if (profile == null)
	    profile = (RunProfile) configuration.getAuxObject(RunProfile.PROFILE_ID);
	return profile;
    }
    
    public int getConsoleType(boolean remote) {
        //TODO: we can determine remoteness from the configuration
        RunProfile _profile = getProfile();
        if (_profile != null) {
            int value = _profile.getConsoleType().getValue();
            // no external console remotely for now
            if (remote && value == RunProfile.CONSOLE_TYPE_EXTERNAL) {
                value = RunProfile.CONSOLE_TYPE_INTERNAL;
            }
            return value;
        }
        return RunProfile.CONSOLE_TYPE_INTERNAL;
    }

    public final void setProfile(RunProfile profile) {
	this.profile = profile;
    }

    private InputOutput io = null;

    public InputOutput getInputOutput() {
        return io;
    }

    public void setInputOutput(InputOutput io) {
        this.io = io;
    }

    private DbgActionHandler dah = null;

    public DbgActionHandler getDah() {
        return dah;
    }

    public void setDah(DbgActionHandler dah) {
        this.dah = dah;
    }

    public final EngineType debuggerType() {
	return debuggerType;
    }

    public final EngineDescriptor getEngineDescriptor() {
        assert debuggerType != null;
        return new EngineDescriptor(debuggerType);
    }
    
    protected abstract String getDbgProfileId();
    
    public final DbgProfile getDbgProfile() {
	if (dbgProfile == null) {
	    dbgProfile = (DbgProfile) getConfiguration().getAuxObject(getDbgProfileId());
        }
	return dbgProfile;
    }
    
    public final void setDbgProfile(DbgProfile profile) {
	dbgProfile = profile;
    }

    private MakefileConfiguration makefileConfiguration = null;

    /**
     * Returns null if we're not an "existing makefile" configuration.
     */

    public final MakefileConfiguration getMakefileConfiguration() {
	if (makefileConfiguration == null) {
	    if (configuration instanceof MakeConfiguration) {
		MakeConfiguration makeConfiguration =
		    (MakeConfiguration) configuration;
		if (makeConfiguration.isMakefileConfiguration())
		    makefileConfiguration = makeConfiguration.getMakefileConfiguration();
	    }
	}
	return makefileConfiguration;
    }

    // For load & run / load & step / load only
    // one or more of: DebuggerManager.RUN | STEP | ATTACH | CORE
    // was: startupBehavior

    private int action = 0;

    public final void setAction(int i) {
        action |= i;
    }

    public final void removeAction(int i) {
        action &= ~i;
    }
     
    public final int getAction() {
        return action;
    }

    private boolean clone;

    public final void setClone() {
	clone = true;
    }

    public final boolean isClone() {
	return clone;
    }

    private boolean isAutoStart = true;
    public final void setAutoStart(boolean isAutoStart) {
        this.isAutoStart = isAutoStart;
    }

    public boolean isAutoStart() {
        return isAutoStart;
    }

    /**
     * captureInfo is to support the ss_attach/capture functionality
     */
    private CaptureInfo captureInfo;
    public final CaptureInfo getCaptureInfo() {
	return captureInfo;
    }
    public final void setCaptureInfo(CaptureInfo captureInfo) {
	this.captureInfo = captureInfo;
    }
    public final boolean isCaptured() {
	return captureInfo != null;
    }

    private boolean is32bitEngine = false; // user pre-choose 32/64 bit version of engine

    public final boolean is32bitEngine() {
        return is32bitEngine;
    }

    public final void set32bitEngine(boolean p) {
        is32bitEngine = p;
    }

    /*
     * Return true, if debuggee (corefile, <pid>, executable) is 64-bit,
     * only needed on linux where a 64-bit engine can't debug 32-bit app.
     * If there is no debuggee, then base on arch of host
     */
    public final boolean is64bitDebuggee(Host host) {
        int act = getAction();
        String debuggee = null;
        Executor executor = Executor.getDefault(Catalog.get("File"), host, 0); // NOI18N

        if ((act & NativeDebuggerManager.CORE) != 0) {
            debuggee = getCorefile();
        } else if ((act & NativeDebuggerManager.ATTACH) != 0) {
	    debuggee = executor.readlink(getPid());
	    if (debuggee == null)
		return false;

        } else {
            debuggee = getTarget();
        }

        // for starting empty engine session, base on host's machine type
	// for both local and remote
        if (debuggee == null || debuggee.length() == 0) {
	    // No debuggee provided
	    return host.isLinux64();

        } else {
	    return executor.is_64(debuggee);
	}
    }
    
    private String symbolFile;
    public String getSymbolFile() {
        return symbolFile;
    }

    public void setSymbolFile(String symbolFile) {
        this.symbolFile = symbolFile;
    }
    
}
