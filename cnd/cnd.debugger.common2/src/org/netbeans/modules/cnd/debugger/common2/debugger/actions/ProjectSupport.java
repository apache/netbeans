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


package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.io.File;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;

import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;
import org.netbeans.modules.cnd.makeproject.api.wizards.DefaultMakeProjectLocationProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;

/**
 * Help manage project and configuration creation for debug/attach/core.
 *
 * An attempt to factor common code from
 * 	AttachPanel
 * 	DebugExecutableNodeAction
 * 	DebugCoreAction
 */

public final class ProjectSupport {

    public static enum Model {
	DONTCARE, IS32, IS64
    };

    private ProjectSupport() {
    }

    public static final class ProjectSeed {

	/**
	 * Desribes how a new debug session should be related to a project.
	 */
	private static enum ProjectMode {
	    NO_PROJECT,	// Debug session not associated with any project.
			// In fact a temporary, in-memory project is created
			// but is not user-visible/manipulable.

	    NEW_PROJECT,// Create a new project just for this executable.

	    OLD_PROJECT	// Reuse an existing project
	};

	//
	// input
	//
	private Project project;

					// caller where field is not null
	private final EngineType engineType;
	private String executable;
	private final Model model;
	private File file;		// DebugExecutableNodeAction
	private final String corefile;	// DebugCoreAction
	private final long pid;		// AttachPanel
	private final String hostName;

	private String workingdir;	// DebugExecutableNodeAction
	private String args;		// DebugExecutableNodeAction
	private String envs;		// DebugExecutableNodeAction
	private Host host;


	//
	// input || derived/output
	//
        private MakeConfiguration conf;

	//
	// derived/output
	//

	// A "base" directory to govern where project may be created
	// and set workingdirectory of one isn't set.
	private String directory;

	private final ProjectMode projectMode;
	private String projectName;


	public ProjectSeed(Project project, EngineType engineType,
			   boolean noproject,
			   String executable,
			   Model model,
			   String corefile,
			   long pid,
			   String workingdir, 
			   String args, 
			   String envs,
			   String hostName) {

	    this.project = project;
	    this.engineType = engineType;
	    this.executable = executable;
	    this.hostName = hostName;
	    this.model = model;
	    this.corefile = corefile;
	    this.pid = pid;
	    this.workingdir = workingdir;
	    this.args = args;
	    this.envs = envs;


	    //
	    // map 'project' X 'noproject' into 'projectMode'.
	    //
	    if (project == null) {
		if (noproject)
		    projectMode = ProjectMode.NO_PROJECT;
		else
		    projectMode = ProjectMode.NEW_PROJECT;
	    } else {
		assert noproject == false;
		projectMode = ProjectMode.OLD_PROJECT;
	    }

	    // do this work elsewhere so we don't get param/field shadowing.
	    prepare();
	}

	private void prepare() {

	    //
	    // Pick a project name
	    //

	    /* OLD

	    Don't do this ... caller depends on 'executable' keeping
	    it's sentinel value for dialog chooser persistence

	    if (Catalog.get("FromProcess").equals(executable) ||// NOI18N
		Catalog.get("AutoCoreExe").equals(executable)) {// NOI18N

		executable = "-"; // so we don't break anything
	    }
	    */

	    if (IpeUtils.isEmpty(executable) || isAuto(executable) ) {
		projectName = "Project"; // NOI18N
	    } else {
		projectName = CndPathUtilities.getBaseName(executable);
	    }


	    //
	    // Make sure executable pathname is absolute
	    //
	    if (executable != null) {
		file = new File(executable);
		if (! IpeUtils.isEmpty(executable) &&
		      executable.charAt(0) != '/') {
		    if (file.exists()) {
			executable = file.getAbsolutePath();
		    }
		}
	    }


	    //
	    // Pick a directory
	    // directory = workingdir? workingdir: dirname(executable||corefile)
	    // 

	    directory = workingdir;

	    if (directory == null && projectMode == ProjectMode.OLD_PROJECT) {
		MakeConfiguration conf2 = ConfigurationSupport.getProjectActiveConfiguration(project);
		if (conf2 != null)
		    directory = conf2.getProfile().getRunDirectory();
	    }
	    if (directory == null && CndPathUtilities.isPathAbsolute(executable)) {
		directory = CndPathUtilities.getDirName(executable);
	    }
	    if (directory == null && CndPathUtilities.isPathAbsolute(corefile)) {
		directory = CndPathUtilities.getDirName(corefile);
	    }
	}

	public void setConfiguration(MakeConfiguration conf) {
	    this.conf = conf;
	}

        public void setHost(Host host) {
            this.host = host;
        }
	public Host getHost() {
	    return host;
	}

	public String getHostName() {
	    return hostName;
	}

	public Project project() {
	    return project;
	}
	public String projectName() {
	    return projectName;
	}
        public MakeConfiguration conf() {
	    return conf;
	}

	public String executable() {
	    return executable;
	}

	/**
	 * Converts an executable name which is a sentinel to "-".
	 */
	public String executableNoSentinel() {
	    if (isAuto(executable))
		return "-";		// NOI18N
	    else
		return executable;
	}

	public File file() {
	    return file;
	}
	public String corefile() {
	    return corefile;
	}
	public long pid() {
	    return pid;
	}
	public String workingdir() {
	    return workingdir;
	}
	public String args() {
	    return args;
	}
	public String envs() {
	    return envs;
	}
    }

    /**
     * Return true if 'executable' is one of the sentinels which implies
     * figure executable name automatically.
     */

    private static boolean isAuto(String executable) {
	if (Catalog.get("FromProcess").equals(executable) ||	// NOI18N
	    Catalog.get("AutoCoreExe").equals(executable) ||	// NOI18N
	                           "-".equals(executable) ) {	// NOI18N
	    return true;
	} else {
	    return false;
	}
    }


    /**
     * Copy information from seed to the configuration.
     */

    private static void populateConfiguration(ProjectSeed seed) {
        
	// we may not always have an executable, especially under core|attach!
	if (!isAuto(seed.executable) && CndPathUtilities.isPathAbsolute(seed.executable)) {
	    seed.conf.getMakefileConfiguration().getOutput().
		setValue(org.netbeans.modules.cnd.utils.CndPathUtilities.normalizeSlashes(seed.executable));
	}

        String currentDebuggerProfileID = EngineTypeManager.engine2DebugProfileID(seed.engineType);
	DbgProfile dbgProfile = (DbgProfile) seed.conf.getAuxObject(currentDebuggerProfileID);
	OptionSet options = dbgProfile.getOptions();
	OptionValue optionExec32 =
	    options.byName(DebuggerOption.OPTION_EXEC32.getName());
	switch (seed.model) {
	    case DONTCARE:
		/* DEBUG
		System.out.printf("ProjectSupport: model -> DONTCARE\n");
		*/
		break;
	    case IS32:
		/* DEBUG
		System.out.printf("ProjectSupport: model -> IS32\n");
		*/
		optionExec32.setEnabled(true);
		break;
	    case IS64:
		/* DEBUG
		System.out.printf("ProjectSupport: model -> IS64\n");
		*/
		optionExec32.setEnabled(false);
		break;
	}
	options.save();

	/* OLD
	// Only set these if we're debugging an executable
	if (seed.corefile != null || seed.pid != 0)
	    return;
	*/
	// in a capture scenario we have a pid and we have some of 
	// the below stuff, so let's just do it like this:
        
	if (seed.workingdir != null)
	    seed.conf.getProfile().setRunDirectory(seed.workingdir);
	if (seed.args != null)
	    seed.conf.getProfile().setArgs(seed.args);
	if (seed.envs != null)
	    seed.conf.getProfile().getEnvironment().putenv(seed.envs);
	// LATER seed.conf.getProfile().setEnv(seed.envs);

	// OLD adjustDefaults(seed);

        final ProjectSeed static_seed = seed;
	if ( ! NativeDebuggerManager.isStandalone()) {
            fillCndConfiguration(static_seed);
	}
    }

    private static void fillCndConfiguration (final ProjectSeed seed) {
        /*
	 Host host = CndRemote.hostFromName(null, seed.getHostName());
        seed.setHost(host);
	CndRemote.fillConfiguratioFromHost(seed.conf, host);
	 * 
	 */

	final String hostName = seed.getHostName();
	CndRemote.validate(hostName, new Runnable() {
                @Override
		public void run() {
		    Host host = Host.byName(hostName);
		    seed.setHost(host);
                    // see IZ 208582, for remote attach we need to set correct remote FSPath
                    if (host.isRemote()) {
                        seed.conf.setBaseFSPath(new FSPath(FileSystemProvider.getFileSystem(host.executionEnvironment()), seed.conf.getBaseDir()));
                    }
		    CndRemote.fillConfigurationFromHost(seed.conf, seed.engineType, host);
		}
	});
    }

// OLD fix
//    /**
//     * Adjust defaults which may have been reset as a side-effect of a
//     * project getting created with an existing configuration.
//     */
//
//    private static void adjustDefaults(ProjectSeed seed) {
//
//	// Turn off compiler requirements, since this project is only
//	// going to be [re]used for debugging.
//	// We often run into errors claiming host/cset is invalid because
//	// it doesn't have the required compilers.
//
//	seed.conf.getCRequired().setValue(false);
//	seed.conf.getCppRequired().setValue(false);
//	seed.conf.getFortranRequired().setValue(false);
//    }

    /**
     * Set up a project (or just a configuration) based on seed.
     *
     * The pathway space is covered by two dimensions with these bases:
     * Project: NoProject, NewProject, OldProject
     * Debuggee: FromExecutable, FromPid, FromCore
     */

    public static void getProject(ProjectSeed seed) {

	if (seed.workingdir == null)
	    seed.workingdir = seed.directory;
	if (seed.workingdir == null)
	    seed.workingdir = "";

	// baseDir is really a property of a project!
	// The fact that we need to pass it to a configuration is a historical
	// "design mistake".
	// baseDir points to the main project directory, the directory
	// in which we will find a 'nbproject' subdirectory.
	// It is always supposed to be an absolute pathname.
	// It follows that ...
	// - baseDir should be identical for all configurations of a project.

	String baseDir;

	String projectParentFolder;
	String projectName;

	switch (seed.projectMode) {
	    case NO_PROJECT:

		if (seed.directory != null) {
		    baseDir = seed.directory;
		    assert ! IpeUtils.isEmpty(baseDir);
		    assert CndPathUtilities.isPathAbsolute(baseDir);

		} else {
		    // TMP baseDir = projectFolder + File.separator + projectName;
		    // baseDir is not supposed to be empty.
		    // However, if we set it as above we'll get an error:
		    //	/home/ivan/SunStudioProjects/Project10: bad directory
		    // But only in the attach scenario, where we don't
		    // have a good directory. In the debug or corefile scenarios
		    // we have the executable or the corefile full pathnames
		    // to dirive a directory from.
		    // 
		    // Also, given that we're not really creating a project,
		    // it an empty baseDir should be OK.

		    // In order to avoid assertion failures in RunProfile.setBaseDir():
		    baseDir = "/"; // NOI18N
		}

		if (seed.conf != null) {
		    seed.conf.setBaseDir(baseDir);
		} else {
		    seed.conf = MakeConfiguration.createDefaultHostMakefileConfiguration(baseDir,
					      "Default");	// NOI18N
		}

		populateConfiguration(seed);
		break;

	    case NEW_PROJECT:
		projectParentFolder = DefaultMakeProjectLocationProvider.getDefault().getDefaultProjectFolder();
		projectName = ProjectGenerator.getDefault().getValidProjectName(projectParentFolder, seed.projectName);

		if (seed.directory != null) {
		    baseDir = seed.directory;
		} else {
		    baseDir = projectParentFolder + File.separator + projectName;
		}
		assert ! IpeUtils.isEmpty(baseDir);
		assert CndPathUtilities.isPathAbsolute(baseDir);

		if (seed.conf != null) {
		    seed.conf.setBaseDir(baseDir);
		} else {
		    seed.conf = MakeConfiguration.createDefaultHostMakefileConfiguration(baseDir,
					      "Default");	// NOI18N
		}

		// IZ 114302
		// need to pre-populate the conf
		populateConfiguration(seed);

		// creating Project
		try {
		    final boolean open = true;
                    ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, CndFileUtils.createLocalFile(projectParentFolder, projectName));
                    prjParams.setOpenFlag(open).setConfiguration(seed.conf);
		    seed.project =  ProjectGenerator.getDefault().createBlankProject(prjParams);
		} catch (Exception e) {
		    seed.project = null;
		}

		// IZ 114302
		// re-get the actual conf object
                seed.conf = ConfigurationSupport.getProjectActiveConfiguration(seed.project);
		// OLD adjustDefaults(seed);

		// IZ 114302
		// I guess we won't be able to factor populateConfiguration
		// to the end of this switch stmt.
		// OLD populateConfiguration(seed);
		break;

	    case OLD_PROJECT:
		assert seed.conf == null;
		/* CR 7000724 needs to make a clone, it won't override the orig one later on */
                MakeConfiguration mc = ConfigurationSupport.getProjectActiveConfiguration(seed.project);
                // bug 238853. Won't clone null value.
                if (mc != null) {
                    seed.conf = mc.clone();
                }

		/* CR 7000724 don't override configuration of existing project
		populateConfiguration(seed);
		 *
		 */
		break;
	}
	return;
    }

    public static Project matchProject(String projectFolder, String pName) {
	OpenProjects op = OpenProjects.getDefault();

	Project[] openedProjects = null;
	try {
	    openedProjects = op.openProjects().get();
	} catch (Exception e) {
	    return null;
	}

	for (int i = 0; i < openedProjects.length; i++) {
	    String projectName = ProjectUtils.getInformation(openedProjects[i]).getName();
	    if (pName.equals(projectName)) {
	        return openedProjects[i];
	    }
	}
	return null;
    }

}
