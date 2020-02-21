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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettings;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettingsBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Pathmap;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;

public final class GdbDebuggerSettingsBridge extends DebuggerSettingsBridge {
    
    private final GdbDebuggerImpl gdbDebugger;

    private Pathmap.Item[] shadowPathmap = null;

    public GdbDebuggerSettingsBridge(NativeDebugger debugger) {
	super(debugger, new GdbDebuggerSettings());
	gdbDebugger = (GdbDebuggerImpl) debugger;
    }

    @Override
    protected DebuggerSettings createSettingsFromTarget(DebugTarget dt) {
        RunProfile newRunProfile = dt.getRunProfile();
        DbgProfile newDbgProfile = dt.getDbgProfile();
        return GdbDebuggerSettings.create(newRunProfile, newDbgProfile);
    }
    
    /**
     * Set tentative profile as the current profile.
     */
    @Override
    protected void setTentativeSettings(NativeDebuggerInfo info) {
        RunProfile newRunProfile = info.getProfile();
        DbgProfile newDbgProfile = info.getDbgProfile();
        assert newRunProfile != null;

        String exename = info.getTarget();
        assignTentativeSettings(GdbDebuggerSettings.create(newRunProfile, newDbgProfile), exename);
    }

    @Override
    protected void applyPathmap(Pathmap o, Pathmap n) {
	if (o == null) {
	    shadowPathmap = new Pathmap.Item[0];
	    applyPathmap(shadowPathmap, n.getPathmap());
	} else {
	    applyPathmap(o.getPathmap(), n.getPathmap());
	}
    }

    private void applyPathmap(Pathmap.Item[] oldMap, Pathmap.Item[] newMap) {
        // Wipe out elements in the old map that aren't in the new map,
        // and then set the new map

        // Clear old elements
        if (oldMap != null) {
            for (int i = 0; i < oldMap.length; i++) {
                // If this is in the new map, we don't have to do anything...
                if (newMap != null) {
                    int j = 0;
                    for (; j < newMap.length; j++) {
                        if (newMap[j].from().equals(oldMap[i].from() ) &&
                            IpeUtils.sameString(newMap[j].to(), oldMap[i].to())) {
                            break;
                        }
                    }
                    if (j < newMap.length) { // Found: no need to delete
                        continue;
                    }
                }
                gdbDebugger.pathmap("unset substitute-path " + oldMap[i].from()); // NOI18N
            }
        }

	if (newMap != null) {
            for (int i = 0; i < newMap.length; i++) {
                // If this is in the old map, we don't have to do anything...
                if (oldMap != null) {
                    int j = 0;
                    for (; j < oldMap.length; j++) {
                        if (newMap[i].from().equals(oldMap[j].from()) &&
                            IpeUtils.sameString(newMap[i].to(), oldMap[j].to()) ) {
                            break;
                        }
                    }
                    if (j < oldMap.length) { // Found: no need to add
                        continue;
                    }
                }

		String pathmap = null;
		pathmap = "set substitute-path " + newMap[i].from(); // NOI18N
                if (newMap[i].to() != null)
		    pathmap =  pathmap + " " + newMap[i].to(); // NOI18N
		gdbDebugger.pathmap(pathmap);
            }
        }
    }

    @Override
    protected int getProgLoadedDirty() {
        // on attach we should set breakpoints and watches later, see IZ 197786
        if ( (debugger.getNDI().getAction() & NativeDebuggerManager.ATTACH) != 0) {
            return 0xffffffff & ~DIRTY_BREAKPOINTS & ~DIRTY_WATCHES;
        } else if (debugger.getNDI().getCorefile() != null) {
            return super.getProgLoadedDirty();
        } else {
            // do not create watches, this will be done on the first stop, see IZ 210468
            return super.getProgLoadedDirty() & ~DIRTY_WATCHES;
        }
    }
    
    @Override
    public void noteAttached() {
        super.noteAttached();
        initialApply(DIRTY_BREAKPOINTS | DIRTY_WATCHES);
    }
    
    void noteFistStop() {
        initialApply(DIRTY_WATCHES);
    }
    
    void noteSignalList(String source) {
	ignoreSettingsChange = true;
        String[] signals = source.split("\\\\n"); // NOI18N
        //fix for bz#268306 - NegativeArraySizeException at org.netbeans.modules.cnd.debugger.gdb2.GdbDebuggerSettingsBridge.noteSignalLis
        if (signals.length < 4) {
            currentDbgProfile().signals().setDefaultSignals(new Signals.InitialSignalInfo[0]);
            ignoreSettingsChange = false;
            return;
        }
	Signals.InitialSignalInfo isi[] =
	    new Signals.InitialSignalInfo[signals.length - 4];
	for (int sx = 2; sx < signals.length -2 ; sx++) {
            String signal[] = signals[sx].split("\\\\t"); // NOI18N
	    isi[sx-2] = new Signals.InitialSignalInfo(0,
						    signal[0].split(" ")[0], // NOI18N
						    signal[4],
						    signal[1].equals("Yes"), // NOI18N
						    signal[1].equals("Yes")); // NOI18N
	}
	currentDbgProfile().signals().setDefaultSignals(isi);
	ignoreSettingsChange = false;
    }

    @Override
    protected void applyRunargs() {
        // Temp fix. Begin
        DebugTarget debugTarget = debugger.getNDI().getDebugTarget();
        String runargs;
        if ( (debugTarget != null) && (debugTarget.getUnparsedArgs() != null) ) {
            runargs = debugTarget.getUnparsedArgs();
        } else {
            // Temp fix. End
            runargs = getArgsFlatEx();
        }
        
	if (runargs == null) {
	    runargs = "";
        }
	gdbDebugger.runArgs(ioRedirect(runargs));
    }

    @Override
    protected void applyRunDirectory() {
        String runDirectory = getRunDirectory();
	if (runDirectory != null) {
            gdbDebugger.runDir(runDirectory);
        }
    }

    @Override
    protected void applyClasspath() {
	// System.out.println("GdbDebuggerSettingsBridge.applyClasspath(): NOT IMPLEMENTED");
    }

    @Override
    protected void applyEnvvars() {
        MacroMap macroMap = MacroMap.createEmpty(gdbDebugger.getExecutionEnvironment());
        RunProfile mainRunProfile = getMainSettings().runProfile();
        macroMap.putAll(mainRunProfile.getEnvironment().getenvAsMap());
        applyEnvvars(macroMap, null);
    }

    private void applyEnvvars(MacroMap setEnvs, MacroMap unSetEnvs) {
        // init unbuffer if needed
        gdbDebugger.getIOPack().updateEnv(setEnvs);
        
        // Iterate over the set environment variable list
	if (setEnvs != null)
	    for (Map.Entry<String, String> entry : setEnvs.entrySet()) {
		gdbDebugger.setEnv(entry.getKey() + '=' + entry.getValue());
	    }

        // Iterate over the unset environment variable list
	if (unSetEnvs != null)
	    for (Map.Entry<String, String> entry : unSetEnvs.entrySet()) {
		gdbDebugger.unSetEnv(entry.getKey() );
	    }
    }
    
    @Override
    protected void applyEnvvars(String[][] o, String[][] n) {
        MacroMap setEnvs = MacroMap.createEmpty(gdbDebugger.getExecutionEnvironment());
        for (int i = 0; i < n.length; i++) {
            setEnvs.put(n[i][0], n[i][1]);
        }

        MacroMap unSetEnvs = MacroMap.createEmpty(gdbDebugger.getExecutionEnvironment());
	// go through old env list and pick the ones that are not on new env list
	for (int ox = 0; ox < o.length; ox++) {
	    boolean found = false;
	    for (int nx = 0; nx < n.length; nx++) {
		if (o[ox][0].equals(n[nx][0])) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		// removed env
		unSetEnvs.put(o[ox][0], o[ox][1]);
	    }
	}

        applyEnvvars(setEnvs, unSetEnvs);
    }    

    @Override
    protected void applySignals(Signals o, Signals n) {
	// System.out.println("GdbDebuggerSettingsBridge.applySignals(): NOT IMPLEMENTED");
    }

    @Override
    protected void applyInterceptList() {
	// System.out.println("GdbDebuggerSettingsBridge.applyRunargs(): NOT IMPLEMENTED");
    }
    
    static String[] detectRedirect(String runargs, String type) {
        String[] res = {null, runargs};
        int argPos = runargs.indexOf(type);
        if (argPos != -1) {
            res[1] = runargs.substring(0, argPos);

            try {
                while (runargs.charAt(++argPos) == ' ');
            } catch (IndexOutOfBoundsException e) {
                argPos = -1;
            }
            
            if (argPos!=-1) {
                int endPos;
                if (runargs.charAt(argPos) == '\"') {
                    endPos = (runargs.indexOf('\"', argPos + 1) == -1 ? runargs.length() : runargs.indexOf('\"', argPos + 1) + 1); // NOI18N
                } else {
                    endPos = (runargs.indexOf(' ', argPos) == -1 ? runargs.length() : runargs.indexOf(' ', argPos));
                }

                res[0] = runargs.substring(argPos, endPos);
                res[1] += runargs.substring(Math.min(endPos+1, runargs.length()));
            }
        }
        return res;
    }
    
    private String ioRedirect(String runargs) {
        // not Standard Output
        String[] files = gdbDebugger.getIOPack().getIOFiles();
        if (files == null) {
            return runargs;
        }
            
        String[] res = detectRedirect(runargs, "<"); // NOI18N
        String inArg = res[0];
        runargs = res[1];
        
        res = detectRedirect(runargs, ">"); // NOI18N
        String outArg = res[0];
        runargs = res[1];
            
        OSFamily osFamily = OSFamily.UNKNOWN;
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(gdbDebugger.getExecutionEnvironment());
            osFamily = hostInfo.getOSFamily();
        } catch (CancellationException ex) {
        } catch (IOException ex) {
        }

        StringBuilder inRedir = new StringBuilder();
        inRedir.append(runargs);
        
        String inFile = (inArg == null ? files[0] : inArg);
        String outFile = (outArg == null ? files[1] : outArg);
        
        if (osFamily == OSFamily.WINDOWS) {
            inFile = gdbDebugger.fmap().worldToEngine(inFile);
            outFile = gdbDebugger.fmap().worldToEngine(outFile);
        }
        // fix for the issue 149736 (2>&1 redirection does not work in gdb MI on mac)
        if (osFamily == OSFamily.MACOSX) {
            inRedir.append(" < ").append(inFile).append(" > ").append(outFile).append(" 2> ").append(outFile); // NOI18N
        } else {
            // csh (tcsh also) does not support 2>&1 stream redirection, see issue 147872
            String shell = HostInfoProvider.getEnv(gdbDebugger.getExecutionEnvironment()).get("SHELL"); // NOI18N
            if (shell != null && shell.endsWith("csh")) { // NOI18N
                inRedir.append(" < ").append(inFile).append(" >& ").append(outFile); // NOI18N
            } else {
                inRedir.append(" < ").append(inFile).append(" > ").append(outFile).append(" 2>&1"); // NOI18N
            }
        }
        return inRedir.toString();
    }
}
