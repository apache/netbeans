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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.ErrorManager;

import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionLayers;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.GlobalOptionSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.ProfileOptionSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Pathmap;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointBag;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;


/**
 * Bridges between configuration settings and engine.
 *
 * Methods beginning with 'note' are notifications from engine
 * They mostly follow the pattern of forwarding the notification to 
 * the actual profile while having disabled change notification via
 * 'ignoreSettingsChange'.
 */
public abstract class DebuggerSettingsBridge implements PropertyChangeListener {

    protected NativeDebugger debugger;
    
    // A tentative setting is the one communicated to us directly
    // via the gui through a DebuggerInfo
    private DebuggerSettings tentativeSettings;
    private String tentativeTarget;// ... assoc'd with 'tentativeSettings'

    // A default setting holds information passed to us from enigne from the
    // time of connection to the time of setting commitment
    private final DebuggerSettings defaultSettings;

    // The tentative setting can be committed to and it becomes the main
    // setting, usually when we get a prog_loaded() message from enigne.
    private DebuggerSettings mainSettings;

    // Used for eliminating redundant applications to enigne
    private DebuggerSettings lastAppliedSettings;

    // Create option sets preset to "default" values.
    //
    // Option sets are initialized with (our) notion of what
    // the default values are and are used to control change notifications
    // back to enigne.
    //
    // SHOULD absorb dbx prop_decl's to seed the default values instead
    // of relying on our own assumptions
    // SHOULD then absorb any prop_changed notifications that come from
    // .dbxrc (or not depending on whether we want .dbxrc to trump the gui)
    // SHOULD then accomodate the values that the gui wants to set a certain
    // way.
    // SHOULD collect these two into an option layer that is in effect 
    // before we commit to a profile. Then prop_changed doesn't
    // need to decide which set to target. The tricky part is that the
    // different parts of the layer get committed at different times.

    private final OptionSet defaultGlobalOptionSet = new GlobalOptionSet();
    private final OptionSet defaultProfileOptionSet = new ProfileOptionSet();



    /**
     * When true, we don't forward property notifications about the setting.
     */
    protected boolean ignoreSettingsChange = false;

    protected boolean ignoreClassPath = false;
    protected boolean ignoreJavaSrcPath = false;

    /**
     * A bit mask denoting what parts of the session data have changed
     * since the last application (via initialApply())
     */

    protected static final int DIRTY_ARGS		= 1;
    protected static final int DIRTY_DIR		= 1<<1;
    protected static final int DIRTY_ENVVARS	= 1<<2;
    protected static final int DIRTY_PRELOAD	= 1<<3;
    protected static final int DIRTY_PATHMAP	= 1<<4;
    protected static final int DIRTY_BREAKPOINTS	= 1<<5;
    protected static final int DIRTY_SIGNALS	= 1<<6;
    protected static final int DIRTY_EXCEPTIONS	= 1<<7;
    protected static final int DIRTY_CLASSPATH	= 1<<8;
    protected static final int DIRTY_WATCHES	= 1<<9;

    // Flags to apply when we've loaded a regular program
    private static final int DIRTY_PROG_APPLY = 0xffffffff;

    // Flags to apply when we've loaded a corefile and have found profile
    private static final int DIRTY_COREFILE_APPLY = 0xffffffff;
	// 6740160
	// DIRTY_DIR|DIRTY_PATHMAP|DIRTY_ENVVARS;

    // Flags to apply when we've attached to a process and have found profile
    private static final int DIRTY_ATTACH_APPLY = 0xffffffff;
	// 6740160
	// DIRTY_DIR|DIRTY_PATHMAP|DIRTY_BREAKPOINTS|DIRTY_WATCHES|DIRTY_SIGNALS|DIRTY_EXCEPTIONS|DIRTY_PRELOAD|DIRTY_ENVVARS;

    protected DebuggerSettingsBridge(NativeDebugger debugger, DebuggerSettings defaultSettings) {
	this.debugger = debugger;

        this.defaultSettings = defaultSettings;
    } 

    public final DebuggerSettings getMainSettings() {
        return mainSettings;
    }

    protected final DebuggerSettings getTentativeSettings() {
        return tentativeSettings;
    }

    protected final DebuggerSettings getDefaultSettings() {
        return defaultSettings;
    }

    public final DebuggerSettings getCurrentSettings() {
        if (mainSettings != null) {
            return mainSettings;
        } else if (tentativeSettings != null) {
            return tentativeSettings;
        } else {
            return defaultSettings;
        }
    }

    private RunProfile currentRunProfile() {
        return getCurrentSettings().runProfile();
    } 

    protected final DbgProfile currentDbgProfile() {
        return getCurrentSettings().dbgProfile();
    } 

    /**
     * Set tentative settings as the current settings.
     */
    protected abstract void setTentativeSettings(NativeDebuggerInfo info);
    
    protected final void assignTentativeSettings(DebuggerSettings tentative, String tentativeTarget) {
	this.tentativeSettings = tentative;
        this.tentativeTarget = tentativeTarget;
    }

    private void commitToTentativeSettings() {
	setSettingsImpl(tentativeSettings, tentativeTarget);
    }

    private void commitToAltSettings(DebuggerSettings altSettings, String altTarget) {
        if (altSettings != null) {
            tentativeSettings = altSettings;
            tentativeTarget = altTarget;
        }
        setSettingsImpl(altSettings, altTarget);
    }    

    /**
     * switch to a new DebuggerSettings (possibly null)
     */
    private void setSettingsImpl(DebuggerSettings newSettings, String target) {

	if (mainSettings == newSettings) {
	    return;
        }

        if (mainSettings != null) {
            mainSettings.detachBridge(this);
        }
        mainSettings = newSettings;
        if (mainSettings != null) {
            mainSettings.attachBridge(this);
        }
        if (mainSettings != null) {
            DbgProfile mainDbgProfile = mainSettings.dbgProfile();
            RunProfile mainRunProfile = mainSettings.runProfile();
            if (mainDbgProfile != null && mainRunProfile != null) {
                // See DbxDebuggerImpl.rebuildOnNextDebug
                if (mainDbgProfile.isBuildFirstOverriden()) {
                    // restore
                    boolean oldBuildFirst = mainDbgProfile.isSavedBuildFirst();
                    mainRunProfile.setBuildFirst(oldBuildFirst);

                    // reset out own flag
                    mainDbgProfile.setBuildFirstOverriden(false);
                }
            }
        }
    }

    /**
     * Extended version of DbgProfile.getArgsFlat() which appends redirection.
     */
    protected final String getArgsFlatEx() {
        DebuggerSettings currentSettings = getCurrentSettings();
        String debugExecutable = currentSettings.dbgProfile().getExecutable();
        // If debug command is specified - use arguments from there
        if (!debugExecutable.isEmpty()) {
            return currentSettings.dbgProfile().getArgsFlat();
        } else {
            return ProjectActionEvent.getRunCommandAsString(
                    currentSettings.runProfile().getArgsFlat(), 
                    (MakeConfiguration) currentDbgProfile().getConfiguration(), 
                    NativeDebuggerImpl.getPathMapFromConfig(currentDbgProfile().getConfiguration()));
        }
    }
    
    // Hold between setup() and noteInitializationDone()
    private NativeDebuggerInfo info;
    /*
     * Setup a new tentative settings for us
     *
     * was: portion of startDebugger()
     */
    public void setup(NativeDebuggerInfo info) {
	// Rememebr info until noteInitializationDone()
	this.info = info;

	// default setting is the current setting

	//
	// Copy non-client properties from info to default settings
	// This is really only needed in the scenario where settings
	// trump .dbxrc, but doesn't do harm otherwise.
	//

	DbgProfile newDbgProfile = info.getDbgProfile();

	// 6815699
	if (newDbgProfile.signals().count() == 0) {
            newDbgProfile.signals().assign(currentDbgProfile().signals());
        }

	getDefaultSettings().dbgProfile().getOptions().
	    assignNonClient(newDbgProfile.getOptions());

        setupExtra(info);
        
	// If we set tentative settings here then debugger properties
        // read from user's initialization file will later trump them
	setTentativeSettings(info);
    }

    protected void setupExtra(NativeDebuggerInfo info) {
    }
    /**
     * Engine is done initializing (which typically means processing
     * commands in the user's debugger initialization file like '.dbxrc')
     *
     */
    public final void noteInitializationDone() {
	OptionSet globalOptions = NativeDebuggerManager.get().globalOptions();
	globalOptions.deltaWithRespectTo(defaultGlobalOptionSet);
	globalOptions.applyTo(debugger.getOptionClient());
	globalOptions.doneApplying();

	// If we set tentative settings here, they will trump debugger properties
        // read from user's initialization file
	// setTentativeProfile(info);
    }

    public void noteRunArgs(String argv[]) {
	ignoreSettingsChange = true;
	currentRunProfile().setArgs(argv);
	ignoreSettingsChange = false;
        
        String args = currentRunProfile().getArgsFlat();
	String redirection = currentDbgProfile().getRedirection();
        if (redirection != null) {
	    args += " " + redirection;//NOI18N
	}

        informListener(RunProfile.PROP_RUNARGS_CHANGED, args );
    }

    public void noteRedir(String infile, String outfile, boolean append) {
	ignoreSettingsChange = true;
	currentDbgProfile().setRedirection(infile, outfile, append);
	String args = currentRunProfile().getArgsFlat();
	String redirection = currentDbgProfile().getRedirection();
        if (redirection != null) {
	    args += " ";//NOI18N
	    args += currentDbgProfile().getRedirection();
	}
	if (args != null)
	    currentRunProfile().setArgsRaw(args);
	ignoreSettingsChange = false;
        
        informListener(RunProfile.PROP_RUNARGS_CHANGED, args );
    } 

    public void noteRunDir(String dir) {

	dir = debugger.remoteToLocal("noteRunDir", dir); // NOI18N

	// Note sure why we're not setting ignoreSettingsChange. That's
	// how it was in vulcan and before

	// OLD ignoreSettingsChange = true;
	currentRunProfile().setRunDirectory(dir);
	// OLD ignoreSettingsChange = false;
        
        informListener(RunProfile.PROP_RUNDIR_CHANGED, dir);
    } 

    public void noteEnvVar(String name, String new_value) {
	Env env = currentRunProfile().getEnvironment();

	if (new_value == null) {
	    env.removeByName(name);
	} else {
	    env.putenv(name, new_value);
	}

	ignoreSettingsChange = true;
	currentRunProfile().setEnvironment(env);
	ignoreSettingsChange = false;
        
        informListener(RunProfile.PROP_ENVVARS_CHANGED, env.getenvAsMap());
    }


    /**
     * Return true if some other session is using this profile.
     */
    private boolean isInUse(RunProfile profile, NativeDebugger myself) {
	NativeDebuggerManager manager = NativeDebuggerManager.get();
	for (NativeDebugger aDebugger : manager.nativeDebuggers()) {
	    if (aDebugger == myself)
		continue;
	    if (aDebugger.profileBridge().currentRunProfile() == profile) {
		// DEBUG System.out.println("DebuggerSettingsBridge.isInUse(): YES");
		return true;
	    }
	}
	// DEBUG System.out.println("DebuggerSettingsBridge.isInUse(): NO");
	return false;
    }

    protected abstract DebuggerSettings createSettingsFromTarget(DebugTarget dt);
    
    protected int getProgLoadedDirty() {
        if (debugger.session().getCorefile() != null) {
            return DIRTY_COREFILE_APPLY;
        } else if (debugger.session().getPid() != -1) {
            return DIRTY_ATTACH_APPLY;
        } else {
            return DIRTY_PROG_APPLY;
        }
    }
    
    /**
     * Note that engine has switched to a new program.
     *
     * This may be the program that was loaded through a GUI action, in
     * which case we should already have settings.
     *
     * Or it may be that the user typed "debug" at the engine command
     * line and they are debugging something totally different in
     * which case we need to switch settings.
     *
     */
    public void noteProgLoaded(String progname) {
        
	if (IpeUtils.sameString("-", tentativeTarget) || // NOI18N
	    IpeUtils.sameString(progname, IpeUtils.normalizePath(tentativeTarget, EditorBridge.getSourceFileSystem(debugger))) &&
	    ! isInUse(tentativeSettings.runProfile(), debugger)) {

	    // Nothing to do
	    commitToTentativeSettings();

	} else {
	    DebugTarget original = info.getDebugTarget();
	    // debug session start from IDE could have no debug target
	    if (original == null) {
		original = new DebugTarget( (MakeConfiguration) info.getConfiguration());
		original.setHostName(info.getHostName());
                original.setEngine(info.getEngineDescriptor().getType());
	    }
	    DebugTarget dt = original.cloneRecord();
            DebuggerSettings newSettings = createSettingsFromTarget(dt);
            // OLD:
//            if (Log.Startup.debug) {
//                if (currentRunProfile() == newRunProfile) {
//                    System.out.println("ProfileBridge.noteProgLoaded(): YES");
//                } else {
//                    System.out.println("ProfileBridge.noteProgLoaded(): NO");
//                }
//            }
            // NEW:
	    if (Log.Startup.debug) {
                if (currentRunProfile() == newSettings.runProfile()) {
                    System.out.println("ProfileBridge.noteProgLoaded(): YES"); // NOI18N
                } else {
                    System.out.println("ProfileBridge.noteProgLoaded(): NO"); // NOI18N
                }
	    } 
	    commitToAltSettings(newSettings, progname);
	    info.setConfiguration(dt.getConfig());
	    info.setDebugTarget(dt);
	}
	
	debugger.optionLayersReset();

	resolveDefaults();

	// notify engine of the commited-to settings

	if (mainSettings != lastAppliedSettings) {
	    initialApply(getProgLoadedDirty());

	    applyConfigurationOptions(true, lastAppliedSettings);
            lastAppliedSettings = mainSettings;
	} else {
	    // engine retains a lot of stuff across debuggables, so only
	    // apply what gets blown away between 'debug' commands.

	    int dirty = 0;
	    dirty |= DIRTY_BREAKPOINTS;
	    dirty |= DIRTY_WATCHES;
	    initialApply(dirty);
	}
        
        informListener(RunProfile.PROP_RUNCOMMAND_CHANGED, progname);
    }

    public void noteProgUnloaded() {
	commitToAltSettings(null, null);
    }

    public void noteAttached() {
        NativeDebuggerManager.get().notifyAttached(debugger, debugger.getNDI().getPid());
    }



    /**
     * Process debugger-related (Running, Debugging, etc)
     *
     * Called when [OK] or [Apply] buttons clicked in Project Properties dialog.
     *
     */

    // word around IZ 197299
    Env savedEnv;

    // interface PropertyChangeListener
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	String name = evt.getPropertyName();

	if (RunProfile.PROP_RUNARGS_CHANGED.equals(name)) {
	    debugger.invalidateSessionData();
	}

	if (!ignoreSettingsChange) {

	    // Running portion
	    if (RunProfile.PROP_RUNARGS_CHANGED.equals(name)) {
		applyRunargs();

	    } else if (RunProfile.PROP_RUNDIR_CHANGED.equals(name)) {
		applyRunDirectory();

	    } else if (RunProfile.PROP_ENVVARS_CHANGED.equals(name)) {
		String o = (String) evt.getOldValue();
		Env n = (Env) evt.getNewValue();
		// CR 4887794
		if (savedEnv != null) {
		    String savedEnvString = savedEnv.toString();
		    if (savedEnvString.equals(o)) {
			applyEnvvars(savedEnv.getenvAsPairs(), n.getenvAsPairs());
		    } else
			applyEnvvars();
		}  else
		    // initial phase
		    applyEnvvars();
		savedEnv = n.clone();

	    // Debugging portion
	    } else if (DbgProfile.PROP_OPTIONS.equals(name)) {
		applyConfigurationOptions(false, null);
                
	    } else if (DbgProfile.PROP_SIGNALS.equals(name)) {
		Signals o = (Signals) evt.getOldValue();
		Signals n = (Signals) evt.getNewValue();
		assert o != null :
		    "ProfileBridge.propertyChange(): null old signals"; // NOI18N
		assert n != null :
		    "ProfileBridge.propertyChange(): null new signals"; // NOI18N
		applySignals(o, n);

	    } else if (DbgProfile.PROP_PATHMAP.equals(name)) {
		Pathmap o = (Pathmap) evt.getOldValue();
		Pathmap n = (Pathmap) evt.getNewValue();
		assert o != null :
		    "ProfileBridge.propertyChange(): null old pathmap"; // NOI18N
		assert n != null :
		    "ProfileBridge.propertyChange(): null new pathmap"; // NOI18N

		applyPathmap(o, n);

	    } else if (DbgProfile.PROP_INTERCEPTLIST.equals(name)) {
		applyInterceptList();

            } else if (!propertyChangeExtra(evt)) {
		ErrorManager.getDefault().log
		    ("Unhandled ProfileBridge.propertyChange: " + // NOI18N
		     name);
	    }
	}
    }

    protected boolean propertyChangeExtra(PropertyChangeEvent event) {
        return false;
    }

    /**
     * Apply default settings after debugging engine started.
     */

    private void resolveDefaults() {
        final DbgProfile mainDbgProfile = mainSettings.dbgProfile();
        final DbgProfile tentativeDbgProfile = tentativeSettings.dbgProfile();
        
        if (tentativeDbgProfile.signals().count() != 0) {
            mainDbgProfile.signals().assign(tentativeDbgProfile.signals());
        } else {
            mainDbgProfile.signals().assign(getDefaultSettings().dbgProfile().signals());
        }

        // OLD mainDbgProfile.signals().adjustSignals();

        if (tentativeDbgProfile.pathmap().count() != 0) {
            mainDbgProfile.pathmap().assign(tentativeDbgProfile.pathmap());
        } else {
            mainDbgProfile.pathmap().assign(getDefaultSettings().dbgProfile().pathmap());
        }

	// OLD mainDbgProfile.pathmap().adjustPathmap();

    }

    /**
     *  Send environment variable definitions etc. over to engine
     */
    private void initialApplyWork(int dirty) {
        /* LATER

	Need to either disable editing of options when we're running or
	postpone their application or use a transparent stop

        if (state.isRunning()) {
            return;
        }
        */

        // Apply session stuff if necessary (read: has changed)
        if ((dirty & DIRTY_DIR) != 0) {
	    applyRunDirectory();
        }
        if ((dirty & DIRTY_ARGS) != 0) {
	    applyRunargs();
        }
        if ((dirty & DIRTY_ENVVARS) != 0) {
	    applyEnvvars();
        }
        if ((dirty & DIRTY_CLASSPATH) != 0) {
	    applyClasspath();
        }

        if ((dirty & DIRTY_PRELOAD) != 0) {
            //System.out.println("Preload - not yet implemented");
        }
        if ((dirty & DIRTY_PATHMAP) != 0) {
            applyPathmap(null, mainSettings.dbgProfile().pathmap());
        }

        if ((dirty & DIRTY_EXCEPTIONS) != 0) {
            applyInterceptList();
        }

        if ((dirty & DIRTY_SIGNALS) != 0) {
	    applySignals(null, mainSettings.dbgProfile().signals());
        }

        if ((dirty & DIRTY_BREAKPOINTS) != 0) {
            // Restore breakpoints from the mainRunProfile
	    OptionLayers optionLayers = debugger.optionLayers();
            if (DebuggerOption.SAVE_BREAKPOINTS.isEnabled(optionLayers)) {
		BreakpointBag bb = NativeDebuggerManager.get().breakpointBag();
		debugger.bm().postRestoreBreakpoints(bb);
            }
        }

        if ((dirty & DIRTY_WATCHES) != 0) {
	    WatchBag wb = NativeDebuggerManager.get().watchBag();
	    debugger.restoreWatches(wb);
        }
    }

    protected final void initialApply(int dirty) {
	debugger.postRestoring(true);
	try {
	    initialApplyWork(dirty);
	} catch (Exception x) {
	}
	debugger.postRestoring(false);
    }
    
    protected final String getRunDirectory() {
        // NOTE: getRunDirectory() will attempt to cobble up something
        // based on config baseDir if no rundirectory was gven.
        DebuggerSettings currentSettings = getCurrentSettings();
        return getRunDir(currentSettings.dbgProfile(), currentSettings.runProfile());
    }
    
    public static String getRunDir(DbgProfile dbgProfile, RunProfile runProfile) {
        String debugDir = dbgProfile.getDebugDir();
        if (debugDir != null && !debugDir.isEmpty()) {
            return debugDir;
        } else {
            return runProfile.getRunDirectory();
        }
    }

    protected abstract void applyPathmap(Pathmap o, Pathmap n);

    protected abstract void applyRunargs();

    protected abstract void applyRunDirectory();

    protected abstract void applyClasspath();

    protected abstract void applyEnvvars();

    protected abstract void applyEnvvars(String[][] o, String[][] n);

    protected abstract void applySignals(Signals o, Signals n);

    protected abstract void applyInterceptList();

    /**
     * Send updates to debugger.
     * Delta against previous set to minimize how much we send.
     *
     */
    protected final void applyConfigurationOptions(boolean firstTime, DebuggerSettings lastSettings) {
	OptionSet dbxOptions = mainSettings.dbgProfile().getOptions();
	if (firstTime) {
	    OptionSet lastOptionSet = null;
	    if (lastSettings != null && lastSettings.dbgProfile() != null) {
		lastOptionSet = lastSettings.dbgProfile().getOptions();
	    } else {
		lastOptionSet = defaultProfileOptionSet;
	    }
	    dbxOptions.deltaWithRespectTo(lastOptionSet);
	}
	dbxOptions.applyTo(debugger.getOptionClient());	// only applies dirtied options
	dbxOptions.doneApplying();
        applyConfigurationOptionsExtra(firstTime, lastSettings);
    }

    protected void applyConfigurationOptionsExtra(boolean firstTime, DebuggerSettings lastSettings) {        
    }
    
    public final DebuggerSettings cloneMainSettings() {
        return (mainSettings == null) ? null : mainSettings.clone(info.getConfiguration());
    }
    
    protected PropertyChangeListener changeListener;

    public void assignChangeListener(PropertyChangeListener changeListener) {
        this.changeListener = changeListener;
    }
    
    private void informListener(String propName, Object value) {
        if (changeListener != null) {
            changeListener.propertyChange(new PropertyChangeEvent(debugger.session(), propName, null, value));
        }
    }
}
