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

package org.netbeans.modules.cnd.makeproject.api;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

public class MakeArtifact implements Cloneable {
    public static final String MAKE_MACRO = "${MAKE}"; //NOI18N
    public static final String MAKEFLAGS_MACRO = "${MAKEFLAGS}"; //NOI18N
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    public static final int TYPE_QT_APPLICATION = 4;
    public static final int TYPE_QT_DYNAMIC_LIB = 5;
    public static final int TYPE_QT_STATIC_LIB = 6;
    public static final int TYPE_DB_APPLICATION = 7;
    public static final int TYPE_CUSTOM = 10;
    // Project
    private String projectLocation;
    // Configuration
    private final int configurationType;
    private final String configurationName;
    private final boolean active;
    // configuration of artifact, can be null
    // if configuration null it can be counted by configuration of enclosed project, path to atifact project and configuration name
    private MakeConfiguration makeConfiguration;
    // configuraten of enclosed project
    private MakeConfiguration parentMakeConfiguration;
    // Artifact
    private boolean build;
    private String workingDirectory;
    private String buildCommand;
    private String cleanCommand;
    private String output;

    public MakeArtifact(
	    String projectLocation,
	    int configurationType, 
	    String configurationName, 
	    boolean active, 
	    boolean build, 
	    String workingDirectory, 
	    String buildCommand, 
	    String cleanCommand, 
	    String output,
            MakeConfiguration parentMakeConfiguration) {
	this.projectLocation = projectLocation;
	this.configurationType = configurationType;
	this.configurationName = configurationName;
	this.active = active;
	this.build = build;
	this.workingDirectory = workingDirectory;
	this.buildCommand = buildCommand;
	this.cleanCommand = cleanCommand;
	this.output = output;
        this.parentMakeConfiguration = parentMakeConfiguration;
    }

    private MakeArtifact(
	    String projectLocation,
	    int configurationType, 
	    String configurationName, 
	    boolean active, 
	    boolean build, 
	    String workingDirectory, 
	    String buildCommand, 
	    String cleanCommand, 
	    String output,
            MakeConfiguration parentMakeConfiguration,
            MakeConfiguration makeConfiguration) {
        this(projectLocation, configurationType, configurationName, active, build, workingDirectory, buildCommand, cleanCommand, output, parentMakeConfiguration);
        this.makeConfiguration = makeConfiguration;
    }

    public MakeArtifact(MakeConfigurationDescriptor pd, MakeConfiguration makeConfiguration) {
        //PathMap pm = HostInfoProvider.default().getMapper(makeConfiguration.getDevelopmentHost().getName());
        projectLocation = makeConfiguration.getBaseDir();
        configurationName = makeConfiguration.getName();
        active = makeConfiguration.isDefault();
        build = true;
        this.makeConfiguration = makeConfiguration;
        if (makeConfiguration.isMakefileConfiguration()) {
            workingDirectory = makeConfiguration.getMakefileConfiguration().getAbsBuildCommandWorkingDir();
            buildCommand = makeConfiguration.getMakefileConfiguration().getBuildCommand().getValue();
            cleanCommand = makeConfiguration.getMakefileConfiguration().getCleanCommand().getValue();
        } else {
            workingDirectory = projectLocation;
            if (!pd.getProjectMakefileName().isEmpty()) {
                buildCommand = MakeArtifact.MAKE_MACRO+" " + MakeOptions.getInstance().getMakeOptions() + " -f " + pd.getProjectMakefileName() + " CONF=" + configurationName; // NOI18N
                cleanCommand = MakeArtifact.MAKE_MACRO+" " + MakeOptions.getInstance().getMakeOptions() + " -f " + pd.getProjectMakefileName() + " CONF=" + configurationName + " clean"; // NOI18N
            } else {
                buildCommand = MakeArtifact.MAKE_MACRO+" " + MakeOptions.getInstance().getMakeOptions() + " CONF=" + configurationName; // NOI18N
                cleanCommand = MakeArtifact.MAKE_MACRO+" " + MakeOptions.getInstance().getMakeOptions() + " CONF=" + configurationName + " clean"; // NOI18N
            }
        }

        switch (makeConfiguration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_MAKEFILE:
                configurationType = MakeArtifact.TYPE_UNKNOWN;
                break;
            case MakeConfiguration.TYPE_APPLICATION:
                configurationType = MakeArtifact.TYPE_APPLICATION;
                break;
            case MakeConfiguration.TYPE_DYNAMIC_LIB:
                configurationType = MakeArtifact.TYPE_DYNAMIC_LIB;
                break;
            case MakeConfiguration.TYPE_STATIC_LIB:
                configurationType = MakeArtifact.TYPE_STATIC_LIB;
                break;
            case MakeConfiguration.TYPE_QT_APPLICATION:
                configurationType = MakeArtifact.TYPE_QT_APPLICATION;
                break;
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                configurationType = MakeArtifact.TYPE_QT_DYNAMIC_LIB;
                break;
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                configurationType = MakeArtifact.TYPE_QT_STATIC_LIB;
                break;
            case MakeConfiguration.TYPE_DB_APPLICATION:
                configurationType = MakeArtifact.TYPE_DB_APPLICATION;
                break;
            case MakeConfiguration.TYPE_CUSTOM:
                configurationType = MakeArtifact.TYPE_CUSTOM;
                break;
            default:
                assert false; // FIXUP: error
                configurationType = -1;
        }
        output = makeConfiguration.getOutputValue();
    }

    public String getProjectLocation() {
	return projectLocation;
    }

    public void setProjectLocation(String projectLocation) {
	this.projectLocation = projectLocation;
    }


    public int getConfigurationType() {
	return configurationType;
    }

    public String getConfigurationName() {
	return configurationName;
    }

    public boolean getActive() {
	return active;
    }

    public boolean getBuild() {
	return build;
    }

    public void setBuild(boolean build) {
	this.build = build;
    }

    public String getWorkingDirectory() {
	return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
	this.workingDirectory = workingDirectory;
    }

    public String getBuildCommand() {
	return buildCommand;
    }

    public String getBuildCommand(String makeCommand, String makeFlags) {
        return subsituteMake(getBuildCommand(), makeCommand, makeFlags);
    }

    public String getBuildCommand(String buildCommandFromProjectProperties, String makeCommand, String makeFlags) {
        return subsituteMake(buildCommandFromProjectProperties, makeCommand, makeFlags);
    }

    public String getCleanCommand() {
	return cleanCommand;
    }

    public String getCleanCommand(String makeCommand, String makeFlags) {
        return subsituteMake(getCleanCommand(), makeCommand, makeFlags);
    }

    private String subsituteMake(String template, String makeCommand, String makeFlags) {
        if (makeCommand.indexOf(' ') > 0 && !(makeCommand.indexOf('"')==0 || makeCommand.indexOf('\'')==0)) { // NOI18N
            makeCommand = "\""+makeCommand+"\""; // NOI18N
        }
        int startCommand = template.indexOf(MakeArtifact.MAKE_MACRO);
        int startFlags = template.indexOf(MakeArtifact.MAKEFLAGS_MACRO);
        if (startCommand >= 0) {
            if (makeFlags.length() > 0 && startFlags < 0) {
                makeCommand = makeCommand + " "+makeFlags; // NOI18N
            }
            template = template.substring(0, startCommand) + makeCommand + template.substring(startCommand + 7);
        }

        if (startFlags >= 0) {
            startFlags = template.indexOf(MakeArtifact.MAKEFLAGS_MACRO);
            template = template.substring(0, startFlags) + makeFlags + template.substring(startFlags + 12);
        } else {
            if (startCommand < 0) {
                template = template + " " + makeFlags; // NOI18N
            }
        }

        return template;
    }

    public String getOutput() {
        initConfiguration();
        if (makeConfiguration != null) {
            return makeConfiguration.expandMacros(output);
        } else {
            String val = CndPathUtilities.expandMacro(output, MakeConfiguration.CND_CONF_MACRO, configurationName); // NOI18N
            return parentMakeConfiguration.expandMacros(val);
        }
    }

    private void initConfiguration() {
        if (makeConfiguration == null) {
            try {
                String projectPath = projectLocation;
                FileSystem fs = parentMakeConfiguration.getBaseFSPath().getFileSystem();
                if (!CndPathUtilities.isPathAbsolute(projectLocation)) {
                    projectPath = parentMakeConfiguration.getBaseFSPath().getPath() + FileSystemProvider.getFileSeparatorChar(fs) + projectPath;
                }
                projectPath = FileSystemProvider.normalizeAbsolutePath(projectPath, fs);
                FileObject toFileObject = fs.findResource(projectPath);
                if (toFileObject != null) {
                    Project findProject = ProjectManager.getDefault().findProject(toFileObject);
                    if (findProject != null) {
                        ProjectConfigurationProvider<Configuration> lookup = findProject.getLookup().lookup(ProjectConfigurationProvider.class);
                        if (lookup != null) {
                            for(Configuration c : lookup.getConfigurations()) {
                                if (configurationName.equals(c.getName())) {
                                    makeConfiguration = (MakeConfiguration) c;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getStoredOutput() {
        return output;
    }

    @Override
    public String toString() {
        String ret = getConfigurationName();
        if (getOutput() != null && getOutput().length() > 0) {
            ret = ret + " (" + getOutput() + ")"; // NOI18N
        }
        return ret;
    }

    public static MakeArtifact[] getMakeArtifacts(Project project) {
        MakeArtifactProvider map = project.getLookup().lookup(MakeArtifactProvider.class);
        if (map != null) {
            return map.getBuildArtifacts();
        } else {
            return null;
        }
    }

    @Override
    public MakeArtifact clone() {
        return new MakeArtifact(
                projectLocation,
                configurationType,
                configurationName,
                active,
                build,
                workingDirectory,
                buildCommand,
                cleanCommand,
                output,
                parentMakeConfiguration,
                makeConfiguration);
    }
}
