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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.Locale;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.LinkerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration.OptionToString;
import org.netbeans.modules.cnd.makeproject.api.wizards.CommonUtilities;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.util.NbBundle;

public class LinkerConfiguration implements AllOptionsProvider, Cloneable {

    private MakeConfiguration makeConfiguration;
    private StringConfiguration output;
    private VectorConfiguration<String> additionalLibs;
    private VectorConfiguration<String> dynamicSearch;
    private BooleanConfiguration stripOption;
    private BooleanConfiguration picOption;
    private BooleanConfiguration norunpathOption;
    private BooleanConfiguration nameassignOption;
    private OptionsConfiguration commandLineConfiguration;
    private OptionsConfiguration additionalDependencies;
    private IntConfiguration runTimeSearchPath;
    private LibrariesConfiguration librariesConfiguration;
    private BooleanConfiguration copyLibrariesConfiguration;
    private StringConfiguration tool;

    public static final int SEARCH_PATH_NONE = 0;
    public static final int SEARCH_PATH_RELATIVE_TO_WORKING_DIR = 1;
    public static final int SEARCH_PATH_RELATIVE_TO_BINARY = 2;
    public static final int SEARCH_PATH_ABSOLUTE = 3;
    private static final String[] SEARCH_PATH_NAMES = {
        getString("SEARCH_PATH_NONE"),
        getString("SEARCH_PATH_RELATIVE_TO_WORKING_DIR"),
        getString("SEARCH_PATH_RELATIVE_TO_BINARY"),
        getString("SEARCH_PATH_ABSOLUTE"),};
    

    // Constructors
    public LinkerConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        output = new StringConfiguration(null, ""); // NOI18N
        additionalLibs = new VectorConfiguration<>(null);
        dynamicSearch = new VectorConfiguration<>(null);

        stripOption = new BooleanConfiguration(false); // NOI18N
        picOption = new BooleanConfiguration(true); // NOI18N
        norunpathOption = new BooleanConfiguration(true); // NOI18N
        nameassignOption = new BooleanConfiguration(true);
        commandLineConfiguration = new OptionsConfiguration();
        additionalDependencies = new OptionsConfiguration();
        additionalDependencies.setPreDefined(getAdditionalDependenciesPredefined());
        librariesConfiguration = new LibrariesConfiguration();
        
        boolean isMac = makeConfiguration.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX;
        copyLibrariesConfiguration = new BooleanConfiguration(isMac);
        
        tool = new StringConfiguration(null, ""); // NOI18N
        runTimeSearchPath = new IntConfiguration(null, SEARCH_PATH_RELATIVE_TO_WORKING_DIR, SEARCH_PATH_NAMES, null);
    }

    public boolean getModified() {
        return getOutput().getModified()
                || getAdditionalLibs().getModified()
                || getDynamicSearch().getModified()
                || getStripOption().getModified()
                || getPICOption().getModified()
                || getNorunpathOption().getModified()
                || getLibrariesRunTimeSearchPathKind().getModified()
                || getNameassignOption().getModified()
                || getAdditionalDependencies().getModified()
                || getTool().getModified()
                || getLibrariesConfiguration().getModified()
                || getCopyLibrariesConfiguration().getModified()
                || getCommandLineConfiguration().getModified();
    }

    private String getAdditionalDependenciesPredefined() {
        String pd = "${BUILD_SUBPROJECTS} ${OBJECTFILES}"; // NOI18N
        return pd;
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    // Output
    public void setOutput(StringConfiguration output) {
        this.output = output;
    }

    public StringConfiguration getOutput() {
        return output;
    }

    // Additional Libraries
    public VectorConfiguration<String> getAdditionalLibs() {
        return additionalLibs;
    }

    public void setAdditionalLibs(VectorConfiguration<String> additionalLibs) {
        this.additionalLibs = additionalLibs;
    }

    // Dynamic Search
    public VectorConfiguration<String> getDynamicSearch() {
        return dynamicSearch;
    }

    public void setDynamicSearch(VectorConfiguration<String> dynamicSearch) {
        this.dynamicSearch = dynamicSearch;
    }

    // Strip
    public void setStripOption(BooleanConfiguration stripOption) {
        this.stripOption = stripOption;
    }

    public BooleanConfiguration getStripOption() {
        return stripOption;
    }

    // Kpic
    public void setPICOption(BooleanConfiguration picOption) {
        this.picOption = picOption;
    }

    public BooleanConfiguration getPICOption() {
        return picOption;
    }

    // Norunpath
    public void setNorunpathOption(BooleanConfiguration norunpathOption) {
        this.norunpathOption = norunpathOption;
    }

    public BooleanConfiguration getNorunpathOption() {
        return norunpathOption;
    }

    // Name Assign
    public void setNameassignOption(BooleanConfiguration nameassignOption) {
        this.nameassignOption = nameassignOption;
    }

    public BooleanConfiguration getNameassignOption() {
        return nameassignOption;
    }

    // CommandLine
    public OptionsConfiguration getCommandLineConfiguration() {
        return commandLineConfiguration;
    }

    public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
        this.commandLineConfiguration = commandLineConfiguration;
    }

    // Additional Dependencies
    public OptionsConfiguration getAdditionalDependencies() {
        return additionalDependencies;
    }

    public void setAdditionalDependencies(OptionsConfiguration additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    // LibrariesConfiguration
    public LibrariesConfiguration getLibrariesConfiguration() {
        return librariesConfiguration;
    }

    public void setLibrariesConfiguration(LibrariesConfiguration librariesConfiguration) {
        this.librariesConfiguration = librariesConfiguration;
    }

    // CopyLibrariesConfiguration
    public BooleanConfiguration getCopyLibrariesConfiguration() {
        return copyLibrariesConfiguration;
    }

    public void setCopyLibrariesConfiguration(BooleanConfiguration copyLibrariesConfiguration) {
        this.copyLibrariesConfiguration = copyLibrariesConfiguration;
    }

    // LibrariesConfiguration
    public IntConfiguration getLibrariesRunTimeSearchPathKind() {
        return runTimeSearchPath;
    }

    public void setLibrariesRunTimeSearchPathKind(IntConfiguration runTimeSearchPath) {
        this.runTimeSearchPath = runTimeSearchPath;
    }

    // Tool
    public void setTool(StringConfiguration tool) {
        this.tool = tool;
    }

    public StringConfiguration getTool() {
        return tool;
    }

    // Clone and assign
    public void assign(LinkerConfiguration conf) {
        // LinkerConfiguration
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getOutput().assign(conf.getOutput());
        getAdditionalLibs().assign(conf.getAdditionalLibs());
        getDynamicSearch().assign(conf.getDynamicSearch());
        getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
        getAdditionalDependencies().assign(conf.getAdditionalDependencies());
        getStripOption().assign(conf.getStripOption());
        getPICOption().assign(conf.getPICOption());
        getNorunpathOption().assign(conf.getNorunpathOption());
        getLibrariesRunTimeSearchPathKind().assign(conf.getLibrariesRunTimeSearchPathKind());
        getNameassignOption().assign(conf.getNameassignOption());
        getLibrariesConfiguration().assign(conf.getLibrariesConfiguration());
        getCopyLibrariesConfiguration().assign(conf.getCopyLibrariesConfiguration());
        getTool().assign(conf.getTool());
    }

    @Override
    public LinkerConfiguration clone() {
        LinkerConfiguration clone = new LinkerConfiguration(getMakeConfiguration());
        // LinkerConfiguration
        clone.setOutput(getOutput().clone());
        clone.setAdditionalLibs(getAdditionalLibs().clone());
        clone.setDynamicSearch(getDynamicSearch().clone());
        clone.setCommandLineConfiguration(getCommandLineConfiguration().clone());
        clone.setAdditionalDependencies(getAdditionalDependencies().clone());
        clone.setStripOption(getStripOption().clone());
        clone.setPICOption(getPICOption().clone());
        clone.setNorunpathOption(getNorunpathOption().clone());
        clone.setLibrariesRunTimeSearchPathKind(getLibrariesRunTimeSearchPathKind().clone());
        clone.setNameassignOption(getNameassignOption().clone());
        clone.setLibrariesConfiguration(getLibrariesConfiguration().clone());
        clone.setCopyLibrariesConfiguration(getCopyLibrariesConfiguration().clone());
        clone.setTool(getTool().clone());
        return clone;
    }

    public String getOutputOptions(){
        String options = ""; // NOI18N
        CompilerSet cs = getMakeConfiguration().getCompilerSet().getCompilerSet();
        if (cs != null) {
            options += cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getOutputFileFlag() + getOutputValue() + " "; // NOI18N
        }
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    public String getBasicOptions() {
        String options = ""; // NOI18N
        CompilerSet cs = getMakeConfiguration().getCompilerSet().getCompilerSet();
        if (getMakeConfiguration().isDynamicLibraryConfiguration()) {
            String libName = getOutputValue();
            int sep = libName.lastIndexOf('/');
            if (sep >= 0 && libName.length() > 1) {
                libName = libName.substring(sep + 1);
            }
            // FIXUP: should be move to Platform...
            if (cs != null) {
                options += cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getDynamicLibraryBasicFlag();
                if (cs.getCompilerFlavor().isGnuCompiler() && getMakeConfiguration().getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX) {
                    options += libName + " "; // NOI18N
                }
            }
        }
        if (cs != null && getStripOption().getValue()) {
            options += cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getStripFlag() + " "; // NOI18N
        }
        if (getMakeConfiguration().isDynamicLibraryConfiguration()) {
            // FIXUP: should move to Platform
            if (cs != null) {
                if (getPICOption().getValue()) {
                    options += getPICOption(cs);
                }
                if (cs.getCompilerFlavor().isSunStudioCompiler()) {
                    if (getNorunpathOption().getValue()) {
                        options += "-norunpath "; // NOI18N
                    }
                    options += getNameassignOption(getNameassignOption().getValue()) + " "; // NOI18N
                }
            }
        }
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    public String getPICOption(CompilerSet cs) {
        LinkerDescriptor linker = cs.getCompilerFlavor().getToolchainDescriptor().getLinker();
        if (linker != null) {
            return linker.getPICFlag();
        }
        return null;
    }

    public String getLibraryItems() {
        CompilerSet cs = getMakeConfiguration().getCompilerSet().getCompilerSet();
        LinkerDescriptor linker = cs == null ? null : cs.getCompilerFlavor().getToolchainDescriptor().getLinker();
        if (linker == null) {
            return ""; // NOI18N
        }
        String options = ""; // NOI18N
        OptionToString staticSearchVisitor = new OptionToString(cs, linker.getLibrarySearchFlag());
        options += getAdditionalLibs().toString(staticSearchVisitor) + " "; // NOI18N
        if (linker.getDynamicLibrarySearchFlag() != null && linker.getDynamicLibrarySearchFlag().length() > 0) {
            SearchOptionToString dynamicSearchVisitor = new SearchOptionToString(cs, linker.getDynamicLibrarySearchFlag());
            options += getDynamicSearch().toString(dynamicSearchVisitor) + " "; // NOI18N
        }
        LibraryToString libVisitor = new LibraryToString(getMakeConfiguration());
        options += getLibrariesConfiguration().toString(libVisitor) + " "; // NOI18N
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    // Interface OptionsProvider
    @Override
    public String getAllOptions(Tool tool) {
        String options = getOutputOptions() + " "; // NOI18N
        options += getLibraryItems() + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        options += getBasicOptions() + " "; // NOI18N
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    private String getNameassignOption(boolean val) {
        if (val) {
            return "-h " + CndPathUtilities.getBaseName(getOutputValue()); // NOI18N
        } else {
            return ""; // NOI18N
        }
    }

    public String getOutputValue() {
        if (getOutput().getModified()) {
            return getOutput().getValue();
        } else {
            return getOutputDefault();
        }
    }

    public String getOutputDefault() {
        String outputName = CndPathUtilities.getBaseName(getMakeConfiguration().getBaseDir());
        switch (getMakeConfiguration().getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_APPLICATION:
            case MakeConfiguration.TYPE_DB_APPLICATION:
                outputName = outputName.toLowerCase(Locale.getDefault());
                break;
            case MakeConfiguration.TYPE_DYNAMIC_LIB:
            case MakeConfiguration.TYPE_CUSTOM: // <=== FIXUP
                outputName = Platforms.getPlatform(getMakeConfiguration().getDevelopmentHost().getBuildPlatform()).getLibraryNameWithoutExtension(outputName);
                break;
        }
        outputName = ConfigurationSupport.makeNameLegal(outputName);
        if (getMakeConfiguration().isDynamicLibraryConfiguration()) {
            return MakeConfiguration.CND_DISTDIR_MACRO + "/" + MakeConfiguration.CND_CONF_MACRO + "/"+MakeConfiguration.CND_PLATFORM_MACRO+"/" + outputName + "." + MakeConfiguration.CND_DLIB_EXT_MACRO; // NOI18N
        } else {
            return MakeConfiguration.CND_DISTDIR_MACRO + "/" + MakeConfiguration.CND_CONF_MACRO + "/"+MakeConfiguration.CND_PLATFORM_MACRO+"/" + outputName; // NOI18N
        }
    }

    public String getOutputDefault27() {
        String outputName = CndPathUtilities.getBaseName(getMakeConfiguration().getBaseDir());
        if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION ||
            getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DB_APPLICATION) {
            outputName = outputName.toLowerCase(Locale.getDefault());
        } else if (getMakeConfiguration().isDynamicLibraryConfiguration()) {
            outputName = "lib" + outputName + ".so"; // NOI18N
        }
        return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + outputName; // NOI18N
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(LinkerConfiguration.class, s);
    }

    @Override
    public String toString() {
        return "{output=" + output + " additionalLibs=" + additionalLibs + " dynamicSearch=" + dynamicSearch + // NOI18N
                " stripOption=" + stripOption + " picOption=" + picOption + " norunpathOption=" + norunpathOption + // NOI18N
                " nameassignOption=" + nameassignOption + " commandLineConfiguration=" + commandLineConfiguration + // NOI18N
                " additionalDependencies=" + additionalDependencies + " librariesConfiguration=" + librariesConfiguration + // NOI18N
                " copyLibrariesConfiguration=" + copyLibrariesConfiguration + // NOI18N
                " tool=" + tool + '}'; // NOI18N
    }

    public static class LibraryToString implements VectorConfiguration.ToString<LibraryItem> {

        private final MakeConfiguration conf;

        public LibraryToString(MakeConfiguration conf) {
            this.conf = conf;
        }

        @Override
        public String toString(LibraryItem item) {
            return item.getOption(conf);
        }
    }
    
    public static class SearchOptionToString implements VectorConfiguration.ToString<String> {

        private final CompilerSet compilerSet;
        private final String prepend;

        public SearchOptionToString(CompilerSet compilerSet, String prepend) {
            this.compilerSet = compilerSet;
            this.prepend = prepend;
        }

        @Override
        public String toString(String item) {
            if (0 < item.length()) {
                if (compilerSet != null) {
                    item = CppUtils.normalizeDriveLetter(compilerSet, item);
                }
                item = CndPathUtilities.escapeOddCharacters(item);
                if (item.startsWith(CommonUtilities.ORIGIN)) {
                    //to prevent macro expansion in make file
                    item = "$"+item; // NOI18N
                }
                return prepend == null ? item : prepend + "'" + item + "'"; // NOI18N
            } else {
                return ""; // NOI18N
            }
        }
    }

}
