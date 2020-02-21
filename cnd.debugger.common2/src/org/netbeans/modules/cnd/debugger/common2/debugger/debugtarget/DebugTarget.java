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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget;

import java.util.Collection;

import java.io.File;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;

import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;

import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;


import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.Record;
import org.netbeans.modules.cnd.debugger.common2.capture.CaptureInfo;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.EngineProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ProjectSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettingsBridge;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.util.Lookup;

/**
 * Debug Target.
 *
 * A debugtarget is associated with an executable. Its properties
 * include program arguments, run directory, environment variable etc.
 * <p>
 * Multiple debug sessions of the same program _don't_ share RunConfig
 * objects in memory. findConfig() will always create a new one from
 * the on-disk copy.
 * <p> 
 * The Debug Target will keep information about:

 <ul>
 <li> rundir
 <li> runargs
 <li> envvars
 <li> signals (caught ignored)
 Signal information is kept in two parts. 
 There is a the 'signals' array that is initialized with the default list
 of what's caught and ignored as told us by dbx. When the default
 changes 'signals' gets updated but so does 'signalOverrides' which
 only encodes the delta information that is saved-restored and sent to
 dbx.

 <li> Exceptions (intercepted, not intercepted)
 <li> pathmap
 <li> preloaded LO's
 <li> excluded LO's
 <li> collector settings
 <li> rtc settings
 <li> various session-specific settings for debugging options: stop in main,
      step over allowed, etc.
  </ul>

 */
   

public final class DebugTarget implements Record {

    // Session: anything transient like pids, etc.
    // RunConfig: anything persistent like runargs, etc.

    /** User name of run config (null: 'Default') */
    private String name;

    /** Data Object representing this executable */
    
    // Properties we're not getting from the configuration:
    private String hostName = null;

    private String corefile = null;
    private long pid = -1;
    private CaptureInfo captureInfo = null;

    private EngineType engine;
    private String redirection = null;
    private String argsUnparsed = null;

    private MakeConfiguration configuration = null;
    
    /**
     * Describes how a new debug session should be related to a project.
     */
    public static enum ProjectMode {
        NO_PROJECT,	// Debug session not associated with any project.
                    // In fact a temporary, in-memory project is created
                    // but is not user-visible/manipulable.

        NEW_PROJECT,// Create a new project just for this executable.

        OLD_PROJECT	// Reuse an existing project
    };
    private ProjectMode projectMode = ProjectMode.OLD_PROJECT;

    /** Has the configuration changed such that we need to save */
    private boolean needSave = true; // Save empty ones too!

    /** Property name: runargs (args, cd, etc.) have changed */
    private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public static final String PROP_RUNARGS_CHANGED = "runargs-ch"; // NOI18N
    public static final String PROP_RUNDIR_CHANGED = "rundir-ch"; // NOI18N
    public static final String PROP_ENVVARS_CHANGED = "envvars-ch"; // NOI18N
    public static final String PROP_PATHMAP_CHANGED = "pathmap-ch"; // NOI18N
    public static final String PROP_SIGNAL_CHANGED = "signal-ch"; // NOI18N
    public static final String PROP_SIGNALS_CHANGED = "signals-ch"; // NOI18N
    public static final String PROP_OPTION_CHANGED = "option-ch"; // NOI18N
    public static final String PROP_ENGINE_CHANGED = "engine"; // NOI18N
    public static final String PROP_INTERCEPTLIST_CHANGED = "intrcpt-ch"; // NOI18N
    public static final String PROP_INTERCEPTEXLIST_CHANGED = "exintrcpt-ch"; // NOI18N

    // In order to avoid assertion failures in RunProfile.setBaseDir():
    private static final String baseDir = "/";		// NOI18N

    public DebugTarget( String name) {
        this.engine = EngineTypeManager.getFallbackEnineType();
	this.name = name; 
	configuration = MakeConfiguration.createDefaultHostMakefileConfiguration(baseDir,
					       "Default"); // NOI18N
	configuration.getMakefileConfiguration().
			getOutput().setValue(name);
    }

    public DebugTarget() {
        this.engine = EngineTypeManager.getFallbackEnineType();
	configuration = MakeConfiguration.createDefaultHostMakefileConfiguration(baseDir,
					       "Default");//NOI18N
    }

    public DebugTarget(MakeConfiguration f) {
        this.engine = EngineTypeManager.getFallbackEnineType();
	configuration = f;
    }

    public String name() {
	return name;
    }

    public DbgProfile getDbgProfile() {
	return getDbgProfile(engine);
    }

    public DbgProfile getDbgProfile(EngineType e) {
        String id = EngineTypeManager.engine2DebugProfileID(e);
	return (DbgProfile) configuration.getAuxObject(id);
    }
    
    public RunProfile getRunProfile() {
	return configuration.getProfile();
    }
    
    // engine profile
    public EngineProfile getEngineProfile() {
	return (EngineProfile) configuration.getAuxObject(EngineProfile.PROFILE_ID); 
    }

    public ConfigurationAuxObject getAuxProfile(String id) {
        return configuration.getAuxObject(id);
    }

    public StringConfiguration getOutput() {
	return configuration.getMakefileConfiguration().getOutput();
    }

    public static String makeKey(String exec,
				 String flattennedArgs,
				 String hostName) {
	return exec + " " + flattennedArgs + " " + "[" + hostName + "]" ; // NOI18N
    }

    // interface Record
    @Override
    public String getKey() {
	return displayName();
    /*
	return makeKey(getOutput().getValue(),
		       getRunProfile().getArgsFlat(),
		       hostName);
   */
    }

    // interface Record
    @Override
    public void setKey(String newKey) {
	// Cannot for now
	return;
    }

    // interface record
    @Override
    public boolean matches(String key) {
	return IpeUtils.sameString(getKey(), key, 120);
    }


    // interface Record
    @Override
    public boolean isArchetype() {
	return false;
    }

    /**
     * Make this absorb the information from 'project'.
     */
    public boolean assignProject(Project project) {
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
        if (conf == null) {
            return false;
        }
	configuration = conf.clone();
        setProjectMode(ProjectMode.OLD_PROJECT);

	return true;
    }

    /**
     * Create a Project based on the information in this.
     */
    public Project createProject() throws java.io.IOException {
	ProjectSupport.ProjectSeed seed = new ProjectSupport.ProjectSeed(
	    /* Project */ null, getEngine(),
	    /* noproject */ false,	// create a project for us
	    getExecutable(),
	    ProjectSupport.Model.DONTCARE,
	    /*corefile*/ null,
	    /* pid */ 0,
	    /*workingdir*/ null,
	    /*args*/ null,
	    /*envs*/ null,
	    getHostName());
	seed.setConfiguration(this.configuration);

	ProjectSupport.getProject(seed);

	return seed.project();
    }

    // interface Record
    @Override
    public DebugTarget cloneRecord() {

	// IZ 134190 is fixed in NB6.1 patch 3
	// we don't need work around and making call in copy
	// would work.

	MakeConfiguration configClone = (MakeConfiguration) configuration.copy();

	// work around 134190
	// with the fixes in NB65, we don't need this work around anymore
	// we still need this work around if we use clone() instead of copy() 
	/*
	MakeConfiguration configClone = (MakeConfiguration) configuration.clone();
	fixClonesPCS(configClone);
	*/


	DebugTarget clone = new DebugTarget(configClone);

	clone.setHostName(this.getHostName());
	clone.setCorefile(this.getCorefile());
	clone.setEngine(this.getEngine());
        clone.setUnparsedArgs(this.getUnparsedArgs());

	return clone;
    }

    public void assign(DebugTarget that) {
	this.configuration.assign(that.configuration);
	this.setExecutable(that.getExecutable());
	this.setHostName(that.getHostName());
	this.setEngine(that.getEngine());
	this.setCorefile(that.getCorefile());
	this.setProjectMode(that.getProjectMode());
    }
    
    // interface Record
    @Override
    public String displayName() {
	String exec = configuration.getMakefileConfiguration().
			getOutput().getValue() ;
	String args =  getRunProfile().getArgsFlat();
	String corename = corefile == null ? "" : corefile;
	String display_name = exec + " " + args + " " + "[" + hostName + "]"; // NOI18N
	String engine = System.getProperty("debug.engine");

	if (engine != null && engine.equals("on")) // NOI18N
	    display_name += " " + "[" + getEngine().getDisplayName() + "]"; // NOI18N

	if (corefile != null) {
	    display_name = exec + " " + corename + " " + args + " " + "[" + hostName + "]"; // NOI18N
	    if (engine != null && engine.equals("on")) // NOI18N
		display_name +=  " " + "[" + getEngine().getDisplayName() + "]"; // NOI18N
	}

	if (display_name.length() > 120) {
	    display_name = display_name.substring(0, 120);
	}
	return display_name;

    }

    public String programName() {
        String fullpath = configuration.getMakefileConfiguration().
			    getOutput().getValue();
	int idx = fullpath.lastIndexOf(File.separatorChar);
	if (idx >= 0) {
	    return fullpath.substring(idx + 1);
	} else {
	    return fullpath;
	}
    }

    public String getExecutable() {
	if (configuration.getMakefileConfiguration().getOutput() == null)
	    return " "; // NOI18N
	else
	    return configuration.getMakefileConfiguration().
		    getOutput().getValue();
    }

    public void setBuildFirst(boolean b) {
	getRunProfile().setBuildFirst(b);
    }

    public long getPid() {
	return pid;
    }

    public void setPid(long pid) {
	this.pid = pid;
    }

    public CaptureInfo getCaptureInfo() {
	return captureInfo;
    }
    
    public void setCaptureInfo(CaptureInfo captureInfo) {
	this.captureInfo = captureInfo;
    }
    
    public String getCorefile() {
	return corefile;
    }
    
    public void setCorefile(String corefile) {
	this.corefile = corefile;
    }
    
    public void setExecutable(String exec) {
	configuration.getMakefileConfiguration().
			getOutput().setValue(exec);
    }

    public Env getEnvs() {
	return getRunProfile().getEnvironment();
    }

    public void setEnvs(String e) {
	getRunProfile().getEnvironment().putenv(e);
    }


    /*
     * Don't use this ... use getRunProfile(), getOutput(), etc.
     *
     * In general a DebugTarget should fully encapsulate it's MakeConfiguration
     * but we still need to get at the config in the following places:
     * 1) DebuggerManager.debug()
     */

    public void setConfig(MakeConfiguration conf) {
	configuration = conf;
    }
    public MakeConfiguration getConfig() {
	return configuration;
    }

    /**
     * Return the full path to the run directory for this program.
     * @return The directory, or null if no run directory has been set
     */
    public String getRunDir() {
        return DebuggerSettingsBridge.getRunDir(getDbgProfile(), getRunProfile());
    }
    
    /**
     * Set the run directory for this program
     * @param newdir Full path to the run directory
     */
    public void setRunDir(String newdir) {
     // System.out.println(" DebugTarget setRunDir new dir  " + newdir);
	
	String rundir = getRunDir();

	if (newdir == null) {
	    // Set directory to "empty" - means set it to the initial
	    // directory. This is the location of the program.

	    String executable = getExecutable();
	    if (executable != null) 
		newdir = CndPathUtilities.getDirName(executable);

	}
	/* OLD
	if (rundir == null) {
	    if (rundir == newdir) {
		return;
	    }
	} else if (rundir.equals(newdir)) {
	    return;
	}
	*/
	if (IpeUtils.sameString(rundir, newdir)) {
	    return;
        }
	getDbgProfile().setDebugDir(newdir);
	pcs.firePropertyChange(PROP_RUNDIR_CHANGED, null, this);
	needSave = true;
    }

    /**
     * Set the flat argument list for this program.
     * unused
    public void setArgs(String newargs) {
	getRunProfile().setArgs(newargs);
    }
    public String getArgsFlat() {
	return getRunProfile().getArgsFlat();
    }
     */

    public String [] getArgs() {
	return getRunProfile().getArgsArray();
    }

    /**
     * Set the argument list for this program. Doesn't affect redirection.
     * @param newargs List of arguments to replace current list
     */
    public void setArgsOnly(String [] newargs) {
	getRunProfile().setArgs(newargs);
	updateFlattenedArgs();
	needSave = true;
    }

    public void setHostName(String h) {

	// Under the IDE a hostname is actually in the form username@hostname
	// (unless its' localhost).
	// Under dbxtool it's a pure hostname.
	// 
	// We need to use the right form consistently throughout dbxgui code.
	// This is probably the most central point where this information
	// gets chanelled through hence these checks:

	if (NativeDebuggerManager.isStandalone()) {
	    assert IpeUtils.isEmpty(h) ||
		   h.indexOf('@') == -1;	// no embedded @
	} else {
	    assert IpeUtils.isEmpty(h) ||
		   h.equals("localhost") ||	// NOI18N
		   h.equals("127.0.0.1") ||	// NOI18N
		   h.indexOf('@') != -1;	// has to have embedded @
	}

	if ("127.0.0.1".equals(h))		// NOI18N
	    hostName = "localhost";		// NOI18N
	else
	    hostName = h;
    }

    @Override
    public String getHostName() {
	return hostName;
    }

    public EngineDescriptor getEngineDescriptor() {
        assert engine != null;
        return new EngineDescriptor(engine);
    }

    public EngineType getEngine() {
        assert engine != null;
	//return getEngineProfile().getEngine();
	return engine;
    }

    public void setEngine(EngineType e) {
        if (e == null) {
            e = EngineTypeManager.getFallbackEnineType();
        }
	engine = e;
	getEngineProfile().setEngineType(engine);
    }
    
    public void setCompilerSet(CompilerSet cs) {
        configuration.getCompilerSet().assign(new CompilerSet2Configuration(new DevelopmentHostConfiguration(configuration.getFileSystemHost()), cs));
    }
    /**
     * for settings from decoder
     * @param id
     */
    public void setEngineByID(String id) {
	engine = getEngineProfile().setEngineByID(id);
    }

    /**
     * Create argsUnparsed from the args[] array and the redirection
     * expression
     */
    private void updateFlattenedArgs() {
	// Update argsUnparsed (we get args in array form from dbx,
	// yet they are manipulated as a command line string in the GUI,
	// hence this weirdness)
	String[] args = getArgs();
	StringBuilder sb = new StringBuilder();
	if (args != null && args.length > 0) {
	    for (int i = 0; i < args.length; i++) {
		if (args[i] == null) {
		    continue;
		}
		if (i > 0) {
		    sb.append(' ');
		}
		if (args[i].length() == 0) {
		    sb.append('"');
		    sb.append('"');
		} else {
		    sb.append(IpeUtils.quoteIfNecessary(args[i]));
		}
	    }
	    sb.append(' ');
	}
	if (redirection != null) {
	    sb.append(redirection);
	}
	setUnparsedArgs(sb.toString());
    }

    /**
     * Get runargs line. This is similar to getArgs(), but it returns
     * the args as a line instead of an array (in other words "flattened").
     * @return Array containing the arguments to the session, or null if none
     */
    public String getUnparsedArgs() {
	return argsUnparsed;
    }

    public void setProjectMode(ProjectMode b) {
	projectMode = b;
    }

    public ProjectMode getProjectMode() {
	return projectMode;
    }
    
    /**
     * Set the argument list for this program (as a single cmdline string,
     * not parsed).
     * @param newargs List of arguments to replace current list
     */
    public void setUnparsedArgs(String newargs) {
	/* OLD
	if (argsUnparsed == null) {
	    if (argsUnparsed == newargs) {
		return;
	    }
	} else if (argsUnparsed.equals(newargs)) {
	    return;
	}
	 */
	if (IpeUtils.sameString(argsUnparsed, newargs))
	    return;
	argsUnparsed = newargs.trim();
	pcs.firePropertyChange(PROP_RUNARGS_CHANGED, null, this);
	needSave = true;
    }

    public boolean isDirty() {
        boolean dirty = getRunProfile().hasChanged();
        for (ConfigurationAuxObject profile : getAuxProfiles()) {
            dirty |= profile.hasChanged();
        }
        return dirty;
    }

    public Collection<ConfigurationAuxObject> getAuxProfiles() {
        List<ConfigurationAuxObject> confObjs = new ArrayList<ConfigurationAuxObject>();
        Collection<? extends NativeDebuggerAuxObjectFactory> factories =
                Lookup.getDefault().lookupAll(NativeDebuggerAuxObjectFactory.class);
        for (NativeDebuggerAuxObjectFactory dbgFactory : factories) {
            final ConfigurationAuxObject auxProfile = getAuxProfile(dbgFactory.getAuxObjectID());
            if (auxProfile != null) {
                confObjs.add(auxProfile);
            }
        }
        return confObjs;
    }

    public void clearDirty() {
        getRunProfile().clearChanged();
        for (ConfigurationAuxObject profile : getAuxProfiles()) {
            profile.clearChanged();
        }
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
    }

}
