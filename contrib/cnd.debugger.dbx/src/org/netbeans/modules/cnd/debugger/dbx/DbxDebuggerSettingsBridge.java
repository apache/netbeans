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

package org.netbeans.modules.cnd.debugger.dbx;

import java.util.Vector;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import com.sun.tools.swdev.glue.dbx.GPDbxJN_mode;
import com.sun.tools.swdev.glue.dbx.GPDbxPathMap;
import com.sun.tools.swdev.glue.dbx.GPDbxSignalInfo;
import com.sun.tools.swdev.glue.dbx.GPDbxSignalInfoInit;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionClient;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettingsBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettings;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcOption;
import org.netbeans.modules.cnd.debugger.dbx.rtc.Loadobjs;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcProfile;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcController;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcOptionSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.Pathmap;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;

public final class DbxDebuggerSettingsBridge extends DebuggerSettingsBridge {
    private final OptionSet defaultRtcOptionSet = new RtcOptionSet();

    public DbxDebuggerSettingsBridge(NativeDebugger debugger) {
	super(debugger, new DbxDebuggerSettings());
    }

    private DbxDebuggerImpl dbxDebugger() {
	return (DbxDebuggerImpl) debugger;
    }

    private Dbx dbx() {
	return dbxDebugger().dbx();
    }

    @Override
    protected DebuggerSettings createSettingsFromTarget(DebugTarget dt) {
        RunProfile newRunProfile = dt.getRunProfile();
        DbgProfile newDbgProfile = dt.getDbgProfile();
	RtcProfile newRtcProfile = (RtcProfile) dt.getAuxProfile(RtcProfile.ID);
        return DbxDebuggerSettings.create(newRunProfile, newDbgProfile, newRtcProfile);
    }

    /**
     * Set tentative profile as the current profile.
     * See CR 6668134.
     */
    @Override
    protected void setTentativeSettings(NativeDebuggerInfo info) {
        DbxDebuggerInfo dbxInfo = (DbxDebuggerInfo) info;
	RunProfile newRunProfile = dbxInfo.getProfile();
	DbgProfile newDbgProfile = dbxInfo.getDbgProfile();
	RtcProfile newRtcProfile = dbxInfo.getRtcProfile();
        assert newRunProfile != null;

        String exename = dbxInfo.getTarget();
        assignTentativeSettings(DbxDebuggerSettings.create(newRunProfile, newDbgProfile, newRtcProfile), exename);
    }

    @Override
    protected void setupExtra(NativeDebuggerInfo info) {
        super.setupExtra(info);
        RtcProfile newRtcProfile = ((DbxDebuggerInfo)info).getRtcProfile();
        DbxDebuggerSettings dbxSettings = (DbxDebuggerSettings) getDefaultSettings();
        dbxSettings.rtcProfile().getOptions().
                assignNonClient(newRtcProfile.getOptions());
    }

    @Override
    protected void applyConfigurationOptionsExtra(boolean firstTime, DebuggerSettings lastSettings) {
        super.applyConfigurationOptionsExtra(firstTime, lastSettings);
        DbxDebuggerSettings dbxSettings = (DbxDebuggerSettings) lastSettings;
        if (dbxSettings != null && dbxSettings.rtcProfile() != null) {
            applyRtcOptions(firstTime, dbxSettings.rtcProfile());
        } else {
            applyRtcOptions(firstTime, null);
	}
    }

    @Override
    protected boolean propertyChangeExtra(PropertyChangeEvent event) {
        boolean handled = false;
        String name = event.getPropertyName();

        // RTC portion
        if (RtcProfile.PROP_RTC_OPTIONS.equals(name)) {
            applyConfigurationOptions(false, null);
            handled = true;
        }
        // LATER
//        } else if (Dbx.PROP_ACCESS_STATE.equals(name)) {
//            invalidateSessionData();
//            handled = true;
//        } else if (Dbx.PROP_MEMUSE_STATE.equals(name)) {
//            invalidateSessionData();
//            handled = true;
//        }
        return handled;
    }

    protected void applyPathmap(Pathmap o, Pathmap n) {
	if (o == null) {
	    applyPathmap(shadowPathmap, n.getPathmap());
	} else {
	    applyPathmap(o.getPathmap(), n.getPathmap());
	}
    }


    /**
     * Given that dbx's pathmap state is reflected in 'oldMap' 
     * issue a set of commands to dbx to bring the state up so it looks like
     * 'newMap'.
     * See also comment for 'shadowPathmap'.
     */

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
                        if (newMap[j].from().equals(oldMap[i].from()) &&
                            IpeUtils.sameString(newMap[j].to(), oldMap[i].to())) {
                            break;
                        }
                    }
                    if (j < newMap.length) { // Found: no need to delete
                        continue;
                    }
                }

                dbx().sendCommand(0, 0, "pathmap -d " + oldMap[i].from()); // NOI18N
            }
        }

        // Set the new elements
        if (newMap != null) {
            for (int i = 0; i < newMap.length; i++) {
                // If this is in the old map, we don't have to do anything...
                if (oldMap != null) {
                    int j = 0;
                    for (; j < oldMap.length; j++) {
                        if (newMap[i].from().equals(oldMap[j].from()) &&
                            IpeUtils.sameString(newMap[i].to(), oldMap[j].to())) {
                            break;
                        }
                    }
                    if (j < oldMap.length) { // Found: no need to add
                        continue;
                    }
                }

                if (newMap[i].to() != null) {
                    dbx().sendCommand(0, 0, "pathmap " + newMap[i].from() + // NOI18N
                            " " + newMap[i].to()); //NOI18N
                } else {
                    dbx().sendCommand(0, 0, "pathmap " + newMap[i].from()); // NOI18N
                }
            }
        }
    }

    protected void applyRunargs() {
        String runargs;
        NativeDebuggerInfo ndi = debugger.getNDI();
        if (ndi.isCaptured()) {
            runargs = ndi.getCaptureInfo().quotedArgvString();
        } else {
            // Temp fix. Begin
            DebugTarget debugTarget = ndi.getDebugTarget();
            if ( (debugTarget != null) && (debugTarget.getUnparsedArgs() != null) ) {
                runargs = debugTarget.getUnparsedArgs();
            } else {
                // Temp fix. End
                runargs = getArgsFlatEx();
            }

            if (runargs == null) {
                runargs = "";
            }
        }
        String command = "runargs " + runargs; //NOI18N
	// maybe conflict with "Standard output" implementation
	boolean has_redir = runargs.contains("<") || runargs.contains(">");  //NOI18N
	String[] files = dbxDebugger().getIOPack().getIOFiles();
	if (has_redir) {
	    if (files != null && !files[0].contains("debuggerFifo")) { //NOI18N
		command += " < " + files[0] + " > " + files[1]; //NOI18N
	    }
	} else {
	    if (files != null) {
		command += " < " + files[0] + " > " + files[1]; //NOI18N
	    }
	}
	dbx().sendCommand(0, 0, command);
    }
    
    protected void applyRunDirectory() {
        String runDirectory;
        NativeDebuggerInfo ndi = debugger.getNDI();
        if (ndi.isCaptured()) {
            runDirectory = ndi.getCaptureInfo().workingDirectory;
        } else {
            runDirectory = getRunDirectory();
        }
	if (runDirectory != null) {
	    /* DEBUG
	    String baseDir = mainRunProfile.getBaseDir();
	    dbx().sendCommand(0, 0, "# baseDir " + baseDir); //NOI18N
	    */
	    runDirectory = dbxDebugger().localToRemote("applyRunDirectory", runDirectory); // NOI18N
	    // CR 6983742, 7009459, 7024153
	    boolean found = runDirectory.startsWith("//~") || runDirectory.startsWith("//."); // NOI18N
	    if (found)
		runDirectory = runDirectory.substring(2); // skip "//"
	    if (runDirectory != null) {
		// CR 7024148, 6767862
		// quote run dir
		runDirectory = "\"" + runDirectory; //NOI18N
		runDirectory += "\"";//NOI18N
		dbx().sendCommand(0, 0, "cd " + runDirectory); //NOI18N
	    }
	}
    }

    protected void applyClasspath() {
	// Set Java class path when in jdbx mode
	// 4662220: classpath not be set within dbx when debugging
	//          mixed Java and native code.
	if (dbxDebugger().getJavaMode() != GPDbxJN_mode.DBX_PLAIN) {
	    // Java debugger does this:
	    //    -cp "{filesystems}"
	    // So we should set the CLASSPATH environment variable here
	    // XXX Later worry about how to not persist this stuff!
	    StringBuffer s = new StringBuffer(1000);
	    // We don't want to see these updates frequently...
	    ignoreClassPath = true;
	    ignoreJavaSrcPath = true;
	    s.append("export "); // NOI18N  Same as string above, interned to same
	    s.append("CLASSPATH"); // NOI18N
	    s.append('=');
	    s.append('"');
	    s.append("<SHOULD figure how to get classpath from NB>"); // NOI18N
	    /* LATER
	    Deprecated s.append(NbClassPath.createRepositoryPath(FileSystemCapability.DEBUG).getClassPath());
	    */
	    s.append('"');
	    dbx().sendCommand(0, 0, s.toString());
	    dbx().sendCommand(0, 0, "export JAVASRCPATH=\"$CLASSPATH\""); // NOI18N
	}
    }
    
    // LATER: implement it in the base class as: initialApply(DIRTY_DIR | DIRTY_ENVVARS);
    public void noteReady() {
        debugger.postRestoring(true);
	try {
	    applyRunDirectory();
            applyEnvvars();
	} catch (Exception x) {
	}
	debugger.postRestoring(false);
    }

    protected void applyEnvvars() {
	// Iterate over the environment variable list
	dbx().postEnvvars(getCurrentSettings().runProfile().getEnvironment().getenv(), null);
    }

    // CR 4887794
    protected void applyEnvvars(String[][] o, String[][] n) {
	Vector<String> unset = new Vector<String>();
	// Find the removed ones, and send unsetcommand to dbx
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
		String env = o[ox][0];
		unset.add(env); // NOI18N
	    }
	}
	String[] unsetArray = null;
	if (unset != null) {
	    // convert to array
	    unsetArray = new String[unset.size()];
	    for (int i = 0; i < unset.size(); i++)
		unsetArray[i] = unset.elementAt((i));
	}
	dbx().postEnvvars(getMainSettings().runProfile().getEnvironment().getenv(),
		unsetArray);
    }
    protected void applySignals(Signals o, Signals n) {
	assert n != null : "applySignals: null new value";
	// OLD assert n.count() > 0 : "applySignals: empty new value";

	final StringBuilder toCatch = new StringBuilder();
	final StringBuilder toIgnore = new StringBuilder();

	if (o == null) {
	    // Initialization case. Delta wrt default values.
	    for (int sx = 0; sx < n.count(); sx++) {
		Signals.InitialSignalInfo si = n.getSignal(sx);
		if (si.isCaught() && ! si.isCaughtByDefault()) {
		    toCatch.append(" ").append(si.signo()); // NOI18N
		} else if (! si.isCaught() && si.isCaughtByDefault()) {
		    toIgnore.append(" ").append(si.signo()); // NOI18N
		}
	    }
	} else {
	    // In the middle of live session case. Delta wrt old values.
	    assert o.count() > 0 : "applySignals: empty old value";

	    for (int sx = 0; sx < o.count(); sx++) {
		Signals.InitialSignalInfo os = o.getSignal(sx);
		assert os != null : "applySignals: null old slot " + sx;

		Signals.InitialSignalInfo ns = n.getSignal(sx);
		assert ns != null : "applySignals: null new slot " + sx;

		if (os != null && ns != null && os.isCaught() != ns.isCaught()) {
		    if (ns.isCaught()) {
			toCatch.append(" ").append(ns.signo()); // NOI18N
		    } else {
			toIgnore.append(" ").append(ns.signo()); // NOI18N
		    }
		}
	    }
	}

	if (toCatch.length() > 0)
	    dbx().sendCommand(0, 0, "catch" + toCatch); // NOI18N
	if (toIgnore.length() > 0)
	    dbx().sendCommand(0, 0, "ignore" + toIgnore); // NOI18N
    }

    protected void applyInterceptList() {
        DbgProfile mainDbgProfile = getMainSettings().dbgProfile();
	String [] interceptList = mainDbgProfile.exceptions().getInterceptList();
	String [] interceptExceptList = mainDbgProfile.exceptions().getInterceptExceptList();
	boolean all = mainDbgProfile.exceptions().isAll();
	boolean interceptUnhandled = mainDbgProfile.exceptions().isInterceptUnhandled();
	boolean interceptUnexpected = mainDbgProfile.exceptions().isInterceptUnexpected();

	String cmd = "intercept -set ";	// NOI18N

	boolean need_comma = false;

	if (all) {
	    if (need_comma)
		cmd += ", ";		// NOI18N
	    cmd += "-all";		// NOI18N
	    need_comma = true;
	}
	if (interceptUnhandled) {
	    if (need_comma)
		cmd += ", ";		// NOI18N
	    cmd += "-unhandled";	// NOI18N
	    need_comma = true;
	}
	if (interceptUnexpected) {
	    if (need_comma)
		cmd += ", ";		// NOI18N
	    cmd += "-unexpected";	// NOI18N
	    need_comma = true;
	}

	for (int ix = 0; ix < interceptList.length; ix++) {
	    if (need_comma)
		cmd += ", ";			// NOI18N
	    cmd += interceptList[ix];
	    need_comma = true;
	}

	if (interceptExceptList.length > 0) {
	    cmd += " -x "; // NOI18N
	    need_comma = false;
	}

	for (int xx = 0; xx < interceptExceptList.length; xx++) {
	    if (need_comma)
		cmd += ", ";			// NOI18N
	    cmd += interceptExceptList[xx];
	    need_comma = true;
	}

	dbx().sendCommand(0, 0, cmd);
    }

    private void applyRtcOptions(boolean firstTime, RtcProfile lastRtcProfile) {
        DbxDebuggerSettings dbxSettings = (DbxDebuggerSettings) getMainSettings();
        RtcProfile mainRtcProfile = dbxSettings.rtcProfile();
	OptionSet rtcOptions = mainRtcProfile.getOptions();

	OptionValue rtc_enable_at_debug =
	    rtcOptions.byType(RtcOption.RTC_ENABLE_AT_DEBUG);
	OptionValue rtc_access_enable =
	    rtcOptions.byType(RtcOption.RTC_ACCESS_ENABLE);
	OptionValue rtc_leaks_memuse_enable =
	    rtcOptions.byType(RtcOption.RTC_LEAKS_MEMUSE_ENABLE);

	Loadobjs  loadobjs = mainRtcProfile.getLoadobjs();

	if (firstTime) {
	    OptionSet lastOptionSet = null;
	    if (lastRtcProfile != null) {
		lastOptionSet = lastRtcProfile.getOptions();
	    } else {
		lastOptionSet = defaultRtcOptionSet;
	    }
	    rtcOptions.deltaWithRespectTo(lastOptionSet);
	}
	OptionClient client = debugger.getOptionClient();
	rtcOptions.applyTo(client);	// only applies dirtied options
	rtcOptions.doneApplying();

	RtcController rtcController = dbxDebugger().rtcController();

	boolean rtcEnabled = rtc_enable_at_debug.isEnabled();

	if (rtcController != null && (rtcEnabled || NativeDebuggerManager.isStandalone())) {

	    // Checking for dirty only makes sense if deltaWithRespectTo
	    // considers these settings.
	    // However rtc state changes from dbx are not yet mirrored
	    // here so deltaWithRespectTo ends up not setting the dirty bits

	    rtcController.setChecking(rtc_access_enable.isEnabled(),
	                              rtc_leaks_memuse_enable.isEnabled());
	    // redundant:
	    // rtcController.setLeaksChecking(rtc_leaks_memuse_enable.isEnabled());

		
	    if (rtc_access_enable.isEnabled())
		rtcController.skipLoadobjs(loadobjs);

	} else {
	    if (rtcController != null) {
		rtcController.setChecking(false, false);
	    }
	}

	dbxDebugger().rtcListen();
    }


    /**
     * was: part of Dbx.signal_list
     */
    void noteSignalList(GPDbxSignalInfoInit initial_signal_list[]) {
	ignoreSettingsChange = true;
	Signals.InitialSignalInfo isi[] =
	    new Signals.InitialSignalInfo[initial_signal_list.length];
	for (int sx = 0; sx < initial_signal_list.length; sx++) {
	    isi[sx] = new Signals.InitialSignalInfo(initial_signal_list[sx].signo,
						    initial_signal_list[sx].name,
						    initial_signal_list[sx].description,
						    initial_signal_list[sx].caught_by_default,
						    initial_signal_list[sx].caught);
	}
	currentDbgProfile().signals().setDefaultSignals(isi);
	ignoreSettingsChange = false;
    }

    /**
     * was: part of Dbx.signal_list_state
     */
    void noteSignalState(GPDbxSignalInfo updated_signal) {
	ignoreSettingsChange = true;
	Signals.SignalInfo si = new Signals.SignalInfo(updated_signal.signo,
						       updated_signal.caught);
	currentDbgProfile().signals().setSignalState(si);
	ignoreSettingsChange = false;
    }

    /**
     * was: part of Dbx.intercept_list
     */

    void noteInterceptList(boolean unhandled,
			   boolean unexpected,
			   String typenames[]) {
	ignoreSettingsChange = true;
	currentDbgProfile().exceptions().
	    setInterceptList(typenames, unhandled, unexpected);
	ignoreSettingsChange = false;
    }

    /**
     * was: part of Dbx.intercept_except_list
     */
    void noteInterceptExceptList(String typenames[]) {
	ignoreSettingsChange = true;
	currentDbgProfile().exceptions().setInterceptExceptList(typenames);
	ignoreSettingsChange = false;
    }


    /*
     * Keeps track of the current pathmap in dbx.
     *
     * Necessary so that when we switch to a different program (with a
     * potentially new pathmap) I can do correct diff-analysis and
     * delete existing pathmap elements etc. (This was applicable 
     * in workshop and sunstudio <= 11 when a 'debug' in dbx switched
     * configurations. It will also be applicable in dbxtool).
     *
     * I can't just set a new pathmap because dbx complains about
     * duplicate etc. And I want to be able to delete the old/nonapplicable
     * pathmap elements.
     */

    // SHOULD eliminate our own copy
    private Pathmap.Item[] shadowPathmap = null;

    /**
     * was: parts of Dbx.pathmap_list (still called from there)
     */
    void notePathmap(GPDbxPathMap updated_pathmap[]) {
	int count = updated_pathmap.length;
	int source = 0;

	// Skip well known trouble maker

	// After fixing 6501961 dbx no longer sends it but let's skip it
	// for a while longer in case we talk to an older dbx.
	// Actually because we blindly reference [0] a new dbx not
	// sending this mapping will cause AIOOB's in older GUI's so
	// dbx will keep sending it for at least one more release.

	if (updated_pathmap.length > 0 &&
	    updated_pathmap[0].from.equals("/tmp_mnt")) { // NOI18N

	    source++;
	    count--;
	}

	// Keep track of what dbx thinks is the pathmap
	// shadowPathmap also doesn't have "/tmp_mnt" anymore so we use
	// it further below.
	shadowPathmap = new Pathmap.Item[count];
	for (int i = 0; i < count; i++) {
	    GPDbxPathMap gpItem = updated_pathmap[source++];
	    shadowPathmap[i] = new Pathmap.Item(gpItem.from,
		                                gpItem.to,
						gpItem.applies_to_cwd);
	}

	ignoreSettingsChange = true;
	if (dbx().dbxInitializing()) {
	    // create a union of what was in the profiles pathmap
	    // and what's in .dbxrc.
	    currentDbgProfile().pathmap().extendPathmap(shadowPathmap);

	} else {
	    currentDbgProfile().pathmap().setPathmap(shadowPathmap);
	}
	ignoreSettingsChange = false;
    }

    @Override
    public void noteRedir(String infile, String outfile, boolean append) {
        // CR 7088693: save redirection only if it is done by the user, not Standard Output
        if (dbxDebugger().getIOPack().getIOFiles() == null) {
            super.noteRedir(infile, outfile, append);
        }
    }

    /**
     * was: part of Dbx.env_changed
     */
    @Override
    public void noteEnvVar(String name, String new_value) {

        if (ignoreClassPath) {
            if (name.equals("CLASSPATH")) { // NOI18N
                // ignoring classpath set
                ignoreClassPath = false;
                return;
            }
        }
        if (ignoreJavaSrcPath) {
            if (name.equals("JAVASRCPATH")) { // NOI18N
                // ignoring javasrcpath set
                ignoreJavaSrcPath = false;
                return;
            }
        }

        if ("_".equals(name)) { // NOI18N
            return;
        } else if ("LD_LIBRARY_PATH".equals(name)) { // NOI18N
            // XXX We shouldn't suppress this one - this is merely a
            // workaround for the fact that jdbx seems to set this
            // envvar (to include VM) and tells us about it. It shouldn't.
            return;
        } else if ("LD_PRELOAD".equals(name)) { // NOI18N
            // XXX We shouldn't suppress this one - this is merely a
            // workaround for the fact that rtc seems to set this
            // envvar and tells us about it. It shouldn't.
            return;
        } else if ("SPRO_EXPAND_ERRORS".equals(name)) { // NOI18N
            return;
        } else if ("PWD".equals(name)) { // NOI18N
            return;
        } else if ("PS1".equals(name)) { // NOI18N
            return;
        } else if ("SP_COLLECTOR_EXPNAME".equals(name)) { // NOI18N
            return;
        } else if ("SP_COLLECTOR_PARAMS".equals(name)) { // NOI18N
            return;
        }

	super.noteEnvVar(name, new_value);
    }
}
