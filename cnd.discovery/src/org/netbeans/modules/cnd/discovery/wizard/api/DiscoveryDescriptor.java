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

package org.netbeans.modules.cnd.discovery.wizard.api;

import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface DiscoveryDescriptor {
    
    // Common properties
    public static final WizardConstants.WizardConstant<String> ROOT_FOLDER =  WizardConstants.DISCOVERY_ROOT_FOLDER;
    public static final WizardConstants.WizardConstant<String> BUILD_RESULT = WizardConstants.DISCOVERY_BUILD_RESULT;
    public static final WizardConstants.WizardConstant<FileSystem> FILE_SYSTEM = WizardConstants.DISCOVERY_BINARY_FILESYSTEM;
    public static final WizardConstants.WizardConstant<String> ADDITIONAL_LIBRARIES = WizardConstants.DISCOVERY_LIBRARIES;
    public static final WizardConstants.WizardConstant<String> COMPILER_NAME = WizardConstants.DISCOVERY_COMPILER;
    public static final WizardConstants.WizardConstant<List<String>> DEPENDENCIES = WizardConstants.DISCOVERY_BINARY_DEPENDENCIES;
    public static final WizardConstants.WizardConstant<List<String>> SEARCH_PATHS = WizardConstants.DISCOVERY_BINARY_SEARCH_PATH;
    public static final WizardConstants.WizardConstant<List<String>> ERRORS = WizardConstants.DISCOVERY_ERRORS;
    public static final WizardConstants.WizardConstant<Boolean> RESOLVE_SYMBOLIC_LINKS = WizardConstants.DISCOVERY_RESOLVE_LINKS;

    public static final WizardConstants.WizardConstant<Project> PROJECT = new WizardConstants.WizardConstant<>("DW:project"); // NOI18N
    public static final WizardConstants.WizardConstant<DiscoveryProvider> PROVIDER = new WizardConstants.WizardConstant<>("DW:provider"); // NOI18N
    public static final WizardConstants.WizardConstant<String> BUILD_FOLDER = new WizardConstants.WizardConstant<>("DW:buildFolder"); // NOI18N
    public static final WizardConstants.WizardConstant<String> LOG_FILE = new WizardConstants.WizardConstant<>("DW:logFile"); // NOI18N
    public static final WizardConstants.WizardConstant<String> EXEC_LOG_FILE = new WizardConstants.WizardConstant<>("DW:execLogFile"); // NOI18N
    public static final WizardConstants.WizardConstant<List<ProjectConfiguration>> CONFIGURATIONS = new WizardConstants.WizardConstant<>("DW:configurations"); // NOI18N
    public static final WizardConstants.WizardConstant<List<String>> INCLUDED = new WizardConstants.WizardConstant<>("DW:included"); // NOI18N
    public static final WizardConstants.WizardConstant<Boolean> INVOKE_PROVIDER = new WizardConstants.WizardConstant<>("DW:invokeProvider"); // NOI18N
    public static final WizardConstants.WizardConstant<List<String>> BUILD_ARTIFACTS = new WizardConstants.WizardConstant<>("DW:buildArtifacts"); // NOI18N
    public static final WizardConstants.WizardConstant<Map<ItemProperties.LanguageKind, Map<String, Integer>>> BUILD_TOOLS = new WizardConstants.WizardConstant<>("DW:buildTools"); // NOI18N
    public static final WizardConstants.WizardConstant<Boolean> INCREMENTAL = new WizardConstants.WizardConstant<>("DW:incremental"); // NOI18N

    Project getProject();
    void setProject(Project project);
    
    DiscoveryProvider getProvider();
    String getProviderID();
    void setProvider(DiscoveryProvider provider);

    String getRootFolder();
    void setRootFolder(String root);

    List<String> getErrors();
    void setErrors(List<String> errors);

    String getBuildResult();
    void setBuildResult(String binaryPath);

    String getBuildFolder();
    void setBuildFolder(String buildPath);

    FileSystem getFileSystem();
    void setFileSystem(FileSystem fs);

    String getAditionalLibraries();
    void setAditionalLibraries(String binaryPath);

    String getBuildLog();
    void setBuildLog(String logFile);

    String getExecLog();
    void setExecLog(String logFile);

    List<ProjectConfiguration> getConfigurations();
    void setConfigurations(List<ProjectConfiguration> configuration);

    List<String> getIncludedFiles();
    void setIncludedFiles(List<String> includedFiles);

    boolean isInvokeProvider();
    void setInvokeProvider(boolean invoke);
    
    boolean isIncrementalMode();
    void setIncrementalMode(boolean incremental);

    boolean isResolveSymbolicLinks();
    void setResolveSymbolicLinks(boolean resolveSymbolicLinks);

    String getCompilerName();
    void setCompilerName(String compiler);

    List<String> getDependencies();
    void setDependencies(List<String> dependencies);

    List<String> getBuildArtifacts();
    void setBuildArtifacts(List<String> buildArtifacts);

    Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools();
    void setBuildTools(Map<ItemProperties.LanguageKind, Map<String, Integer>> buildTools);

    List<String> getSearchPaths();
    void setSearchPaths(List<String> searchPaths);

    void setMessage(String message);

    void clean();
}
