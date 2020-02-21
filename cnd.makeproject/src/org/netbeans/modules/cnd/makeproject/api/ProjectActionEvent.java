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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class ProjectActionEvent {

    public interface Type {
        int ordinal();
        String name();
        String getLocalizedName();
        void setLocalizedName(String name);
    }

    public static enum PredefinedType implements Type {
        PRE_BUILD("PreBuild"), // NOI18N
        BUILD("Build"), // NOI18N
        COMPILE_SINGLE("CompileSingle"), // NOI18N
        CLEAN("Clean"), // NOI18N
        RUN("Run"), // NOI18N
        DEBUG("Debug"), // NOI18N
        DEBUG_STEPINTO("Debug"), // NOI18N
        DEBUG_TEST("Debug"), // NOI18N
        DEBUG_STEPINTO_TEST("Debug"), // NOI18N
        ATTACH("Attach"),  // NOI18N
        CHECK_EXECUTABLE("CheckExecutable"), // NOI18N
        CUSTOM_ACTION("Custom"), // NOI18N
        BUILD_TESTS("BuildTests"), // NOI18N
        TEST("Test"); // NOI18N

        private final String localizedName;

        private PredefinedType(String resourceNamePrefix) {
            localizedName = getString(resourceNamePrefix + "ActionName"); // NOI18N
        }

        @Override
        public String getLocalizedName() {
            return localizedName;
        }

        @Override
        public void setLocalizedName(String name) {
            // predefined events already have localized name
            throw new UnsupportedOperationException();
        }
    }

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private final Project project;
    private final Type type;
    private String executable;
    /** guarded by configurationName */
    private volatile MakeConfiguration configuration;
    private final String configurationName;
    private final RunProfile profile;
    private final boolean wait;
    private final Lookup context;
    private boolean isFinalExecutable;
    private String[] runCommandCache = null;

    public ProjectActionEvent(Project project, Type type, String executable, MakeConfiguration configuration, RunProfile profile, boolean wait) {
        this(project, type, executable, configuration, profile, wait, Lookup.EMPTY);
    }

    public ProjectActionEvent(Project project, Type type, String executable, MakeConfiguration configuration, RunProfile profile, boolean wait, Lookup context) {
        this.project = project;
        this.type = type;
	this.executable = executable;
	this.configuration = configuration;
        configurationName = configuration.getName();
	this.profile = profile;
	this.wait = wait;
        this.context = context;
        if (type == PredefinedType.PRE_BUILD ||
            type == PredefinedType.BUILD ||
            type == PredefinedType.COMPILE_SINGLE ||
            type == PredefinedType.CLEAN || 
            type == PredefinedType.BUILD_TESTS || 
            type == PredefinedType.TEST) {
            if (profile != null && profile.getConsoleType().getValue() != RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                assert false : type + " must not be run in " + profile.getConsoleType().getName() + " use OutputWindow instead";
            }
        }
    }
    
    public Project getProject() {
        return project;
    }

    public final Lookup getContext(){
        return context;
    }
    
    public Type getType() {
        return type;
    }

    // TODO: move method to ProjectActionHandlerFactory or ProjectActionHandler
    public String getActionName() {
        Type myType = getContext().lookup(Type.class);
        if (myType != null) {
            return myType.getLocalizedName();
        } else {
            return type.getLocalizedName();
        }
    }

    private String getExecutableFromRunCommand() {
        String[] runCommand = getRunCommand();
        if (runCommand.length == 0) {
            return "";
        }
        String command = runCommand[0];

        // Use absolute path for shell commands. FIXUP: always a shell command here?
        if (!FileSystemProvider.isAbsolute(command) && !command.contains("/")) { // NOI18N
            ExecutionEnvironment execEnv = getConfiguration().getDevelopmentHost().getExecutionEnvironment();
            PlatformInfo pi = PlatformInfo.getDefault(execEnv);
            String qualifiedCommand = pi.findCommand(command);
            if (qualifiedCommand != null) {
                command = qualifiedCommand;
            }
        }

        return command;
    }

    public String getExecutable() {
        String result;
	if (type == PredefinedType.RUN || type == PredefinedType.DEBUG || type == PredefinedType.DEBUG_STEPINTO) {
            result = getExecutableFromRunCommand();
            if (result != null && result.length() > 0) {
                ExecutionEnvironment execEnv = getConfiguration().getDevelopmentHost().getExecutionEnvironment();
                if (!CndPathUtilities.isPathAbsolute(result)) {
                    CndUtils.assertTrueInConsole(false, "getExecutableFromRunCommand() returned non-absolute path", result); //NOI18N
                    String baseDir = getConfiguration().getProfile().getRunDirectory();
                    if (execEnv.isRemote()) {
                        PathMap mapper = RemoteSyncSupport.getPathMap(getProject());
                        if (mapper != null) {
                            baseDir = mapper.getRemotePath(baseDir, true);
                            if (baseDir == null) {
                                baseDir = getConfiguration().getProfile().getRunDirectory();
                            }
                        } else {
                            LOGGER.log(Level.SEVERE, "Path Mapper not found for project {0} - using local path {1}", new Object[]{getProject(), baseDir}); //NOI18N
                        }
                    }
                    result = baseDir + FileSystemProvider.getFileSeparatorChar(execEnv) + result;
                }
                result = FileSystemProvider.normalizeAbsolutePath(result, execEnv);
            }
        } else {
            result = executable;
        }
	return result;
    }

    public String getRunCommandAsString() {
        PathMap mapper = RemoteSyncSupport.getPathMap(getProject());
        return getRunCommandAsString(getProfile().getRunCommand().getValue(), getConfiguration(), mapper);
        
    }
    
    public static String getRunCommandAsString(String command, MakeConfiguration configuration, PathMap mapper) {
        String outputValue = ""; // NOI18N

        if (!configuration.isLibraryConfiguration()) {
            if (configuration.getOutputValue().length() > 0) {
                outputValue = configuration.getAbsoluteOutputValue();
            }
        }

        if (configuration.getPlatformInfo().isLocalhost()) {
            command = CndPathUtilities.expandAllMacroses(command, MakeConfiguration.CND_OUTPUT_PATH_MACRO, outputValue); // NOI18N
        } else { //            if (!configuration.getDevelopmentHost().isLocalhost()) {
            if (!outputValue.isEmpty()) {
                if (mapper != null) {
                    String aValue = mapper.getRemotePath(outputValue, true);
                    if (aValue != null) {
                        outputValue = aValue;
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Path Mapper not found for the project - using local path {0}", new Object[]{outputValue}); //NOI18N
                }
            }

            command = CndPathUtilities.expandAllMacroses(command, "${OUTPUT_PATH}", outputValue); // NOI18N
        }

        return configuration.expandMacros(command);
    }

    private String[] getRunCommand() {
        if (runCommandCache == null || runCommandCache.length == 0) {
            // not clear what is the difference between getPlatformInfo
            // and getDevelopmentHost.
            // TODO: get rid off one of ifs below
            assert(getConfiguration().getPlatformInfo().isLocalhost() == getConfiguration().getDevelopmentHost().isLocalhost());

            runCommandCache = Utilities.parseParameters(getRunCommandAsString());
        }
        return runCommandCache;
    }

    public ArrayList<String> getArguments() {
        ArrayList<String> result = new ArrayList<>();
        if (type == PredefinedType.RUN) {
            String[] params = getRunCommand();
            if (params.length > 1) {
                result.addAll(Arrays.asList(Arrays.copyOfRange(params, 1, params.length)));
            }
        }
        else if (type == PredefinedType.DEBUG || type == PredefinedType.DEBUG_STEPINTO) {
            result.addAll(Arrays.asList(getProfile().getArgsArray()));
            //???????? <===== Egor, need to do something here for debugging ????
        }
        else {
            result.addAll(Arrays.asList(getProfile().getArgsArray()));
        }
        return result;
    }

    public MakeConfiguration getConfiguration() {
        synchronized (configurationName) {
            if (!configuration.isValid()) {
                ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                if (pdp != null) {
                    MakeConfigurationDescriptor cd = pdp.getConfigurationDescriptor();
                    if (cd != null) {
                        Configuration conf = cd.getConfs().getConf(configurationName);
                        if (conf != null && conf.isValid()) {
                            configuration = (MakeConfiguration) conf;
                        }
                    }
                }
            }
            return configuration;
        }
    }

    public RunProfile getProfile() {
        if (profile != null) {
            return profile;
        } else {
            return getConfiguration().getProfile();
        }
    }

    public boolean getWait() {
        return wait;
    }

    void setExecutable(String executable) {
        this.executable = executable;
    }

    void setFinalExecutable(){
        isFinalExecutable = true;
    }

    boolean isFinalExecutable(){
        return isFinalExecutable || type == PredefinedType.RUN;
    }

    @Override
    public String toString() {
        return "PAE " + type + " " + getActionName() + " exec: " + getExecutable(); // NOI18N
    }

     /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ProjectActionEvent.class);
        }
        return bundle.getString(s);
    }
}
