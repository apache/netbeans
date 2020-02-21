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

import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.debug.DebugUtils;
import static org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration.UNSUPPORTED_INT_CONFIGURATION;
import static org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration.UNSUPPORTED_STRING_CONFIGURATION;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem.OptionItem;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.util.NbBundle;

public abstract class CCCCompilerConfiguration extends BasicCompilerConfiguration {

    public static final boolean STANDARDS_SUPPORT = DebugUtils.getBoolean("cnd.support.standards", true); //NOI18N

    public static final int LIBRARY_LEVEL_NONE = 0;
    public static final int LIBRARY_LEVEL_RUNTIME = 1;
    public static final int LIBRARY_LEVEL_CLASSIC = 2;
    public static final int LIBRARY_LEVEL_BINARY = 3;
    public static final int LIBRARY_LEVEL_CONFORMING = 4;
    private static final String[] LIBRARY_LEVEL_NAMES = {
        getString("NoneTxt"),
        getString("RuntimeOnlyTxt"),
        getString("ClassicIostreamsTxt"),
        getString("BinaryStandardTxt"),
        getString("ConformingStandardTxt"),};
    public static final int STANDARDS_OLD = 0;
    public static final int STANDARDS_LEGACY = 1;
    public static final int STANDARDS_DEFAULT = 2;
    public static final int STANDARDS_MODERN = 3;
    private static final String[] STANDARDS_NAMES = {
        getString("OldTxt"),
        getString("LegacyTxt"),
        getString("DefaultTxt"),
        getString("ModernTxt"),};
    public static final int LANGUAGE_EXT_NONE = 0;
    public static final int LANGUAGE_EXT_DEFAULT = 1;
    public static final int LANGUAGE_EXT_ALL = 2;
    private static final String[] LANGUAGE_EXT_NAMES = {
        getString("NoneTxt"),
        getString("DefaultTxt"),
        getString("AllTxt"),};
    private VectorConfiguration<String> includeDirectories;
    private static final int inheritIncludes = 0;
    private VectorConfiguration<String> includeFiles;
    private static final int inheritFiles = 1;
    private VectorConfiguration<String> preprocessorConfiguration;
    private VectorConfiguration<String> preprocessorUndefinedConfiguration;
    private static final int inheritPreprocessor = 3;
    private static final int inheritUndefinedPreprocessor = 4;
    private static final int useLinkerPkgConfigLibraries = 5;
    private int booleanFlags = 0;
    private final CppConfigurationContainer cppContainer;
    
    // Constructors
    protected CCCCompilerConfiguration(String baseDir, CCCCompilerConfiguration master, MakeConfiguration owner) {
        super(baseDir, master, owner);
        if (!owner.isMakefileConfiguration()) {
            cppContainer = new ManagedCppConfigurationContainer(master);
        } else {
            cppContainer = new UnmanagedCppConfigurationContainer();
        }
        includeDirectories = new VectorConfiguration<>(master != null ? master.getIncludeDirectories() : null);
        set(inheritIncludes, new BooleanConfiguration(true));
        includeFiles = new VectorConfiguration<>(master != null ? master.getIncludeFiles() : null);
        set(inheritFiles, new BooleanConfiguration(true));
        preprocessorConfiguration = new VectorConfiguration<>(master != null ? master.getPreprocessorConfiguration() : null);
        set(inheritPreprocessor, new BooleanConfiguration(true));
        preprocessorUndefinedConfiguration = new VectorConfiguration<>(master != null ? master.getUndefinedPreprocessorConfiguration() : null);
        set(inheritUndefinedPreprocessor, new BooleanConfiguration(true));
        set(useLinkerPkgConfigLibraries, new BooleanConfiguration(true));
    }

    public void fixupMasterLinks(CCCCompilerConfiguration compilerConfiguration) {
        super.fixupMasterLinks(compilerConfiguration);
        getMTLevel().setMaster(compilerConfiguration.getMTLevel());
        getLibraryLevel().setMaster(compilerConfiguration.getLibraryLevel());
        getStandardsEvolution().setMaster(compilerConfiguration.getStandardsEvolution());
        getLanguageExt().setMaster(compilerConfiguration.getLanguageExt());
    }

    @Override
    public boolean getModified() {
        return super.getModified() ||
                cppContainer.getModified() ||
                includeDirectories.getModified() ||
                get(inheritIncludes).getModified() ||
                includeFiles.getModified() ||
                get(inheritFiles).getModified() ||
                preprocessorConfiguration.getModified() ||
                get(inheritPreprocessor).getModified() ||
                preprocessorUndefinedConfiguration.getModified() ||
                get(inheritUndefinedPreprocessor).getModified() ||
                get(useLinkerPkgConfigLibraries).getModified();
    }

    // Library Level
    public void setLibraryLevel(IntConfiguration libraryLevel) {
        cppContainer.setLibraryLevel(libraryLevel);
    }

    public IntConfiguration getLibraryLevel() {
        return cppContainer.getLibraryLevel();
    }

    // Standards Evolution
    public void setStandardsEvolution(IntConfiguration standardsEvolution) {
        cppContainer.setStandardsEvolution(standardsEvolution);
    }

    public IntConfiguration getStandardsEvolution() {
        return cppContainer.getStandardsEvolution();
    }

    // languageExt
    public void setLanguageExt(IntConfiguration languageExt) {
        cppContainer.setLanguageExt(languageExt);
    }

    public IntConfiguration getLanguageExt() {
        return cppContainer.getLanguageExt();
    }

    // Include Directories
    public VectorConfiguration<String> getIncludeDirectories() {
        return includeDirectories;
    }

    public void setIncludeDirectories(VectorConfiguration<String> includeDirectories) {
        this.includeDirectories = includeDirectories;
    }

    // Inherit Include Directories
    public BooleanConfiguration getInheritIncludes() {
        return get(inheritIncludes);
    }

    public void setInheritIncludes(BooleanConfiguration inheritIncludes) {
        set(CCCCompilerConfiguration.inheritIncludes, inheritIncludes);
    }

    // Include Files
    public VectorConfiguration<String> getIncludeFiles() {
        return includeFiles;
    }

    public void setIncludeFiles(VectorConfiguration<String> includeFiles) {
        this.includeFiles = includeFiles;
    }

    // Inherit Include Files
    public BooleanConfiguration getInheritFiles() {
        return get(inheritFiles);
    }

    public void setInheritFiles(BooleanConfiguration inheritFiles) {
        set(CCCCompilerConfiguration.inheritFiles, inheritFiles);
    }

    // Preprocessor
    public VectorConfiguration<String> getPreprocessorConfiguration() {
        return preprocessorConfiguration;
    }

    public void setPreprocessorConfiguration(VectorConfiguration<String> preprocessorConfiguration) {
        this.preprocessorConfiguration = preprocessorConfiguration;
    }
    
    // Preprocessor
    public VectorConfiguration<String> getUndefinedPreprocessorConfiguration() {
        return preprocessorUndefinedConfiguration;
    }

    public void setUndefinedPreprocessorConfiguration(VectorConfiguration<String> preprocessorUndefinedConfiguration) {
        this.preprocessorUndefinedConfiguration = preprocessorUndefinedConfiguration;
    }

    // Inherit Include Directories
    public BooleanConfiguration getInheritPreprocessor() {
        return get(inheritPreprocessor);
    }

    public void setInheritPreprocessor(BooleanConfiguration inheritPreprocessor) {
        set(CCCCompilerConfiguration.inheritPreprocessor, inheritPreprocessor);
    }

    public BooleanConfiguration getInheritUndefinedPreprocessor() {
        return get(inheritUndefinedPreprocessor);
    }

    public void setInheritUndefinedPreprocessor(BooleanConfiguration inheritUndefinedPreprocessor) {
        set(CCCCompilerConfiguration.inheritUndefinedPreprocessor, inheritUndefinedPreprocessor);
    }

    // Linker libraries
    public BooleanConfiguration getUseLinkerLibraries() {
        return get(useLinkerPkgConfigLibraries);
    }

    public void setUseLinkerLibraries(BooleanConfiguration useLinkerPkgConfigLibraries) {
        set(CCCCompilerConfiguration.useLinkerPkgConfigLibraries, useLinkerPkgConfigLibraries);
    }

    public StringConfiguration getImportantFlags() {
        return cppContainer.getImportantFlags();
    }
    
    public void setImportantFlags(StringConfiguration importantFlags) {
        cppContainer.setImportantFlags(importantFlags);
    }
    
    // Clone and assign
    protected void assign(CCCCompilerConfiguration conf) {
        // BasicCompilerConfiguration
        super.assign(conf);
        // XCompilerConfiguration
        getLibraryLevel().assign(conf.getLibraryLevel());
        getStandardsEvolution().assign(conf.getStandardsEvolution());
        getLanguageExt().assign(conf.getLanguageExt());
        getIncludeDirectories().assign(conf.getIncludeDirectories());
        getInheritIncludes().assign(conf.getInheritIncludes());
        getIncludeFiles().assign(conf.getIncludeFiles());
        getInheritFiles().assign(conf.getInheritFiles());
        getPreprocessorConfiguration().assign(conf.getPreprocessorConfiguration());
        getInheritPreprocessor().assign(conf.getInheritPreprocessor());
        getUndefinedPreprocessorConfiguration().assign(conf.getUndefinedPreprocessorConfiguration());
        getInheritUndefinedPreprocessor().assign(conf.getInheritUndefinedPreprocessor());
        getUseLinkerLibraries().assign(conf.getUseLinkerLibraries());
        getImportantFlags().assign(conf.getImportantFlags());
    }

    private CCCCompilerConfiguration getTopMaster() {
        List<BasicCompilerConfiguration> masters = getMasters(true);
        return (CCCCompilerConfiguration)masters.get(masters.size()-1);
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCCCompilerConfiguration.class, s);
    }

    private static final String LIB_FLAG = " --libs "; // NOI18N
    private static final String FLAGS_FLAG = " --cflags "; // NOI18N

    protected String getLibrariesFlags() {
        CCCCompilerConfiguration topMaster = getTopMaster();
        if (!topMaster.getUseLinkerLibraries().getValue() || topMaster.getOwner() == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for (LibraryItem lib : topMaster.getOwner().getLinkerConfiguration().getLibrariesConfiguration().getValue()) {
            if (lib.getType() == LibraryItem.OPTION_ITEM) {
                OptionItem option = (OptionItem) lib;
                String task = option.getLibraryOption();
                if (task.length() > 2  && task.charAt(0) == '`' && task.charAt(task.length()-1) == '`') { // NOI18N
                    int i = task.indexOf(LIB_FLAG);
                    if (i > 0) {
                        if (buf.length() > 0) {
                            buf.append(' ');
                        }
                        buf.append(task.substring(0, i));
                        buf.append(FLAGS_FLAG);
                        buf.append( task.substring(i+LIB_FLAG.length()));
                    }
                }
            }
        }
        if (buf.length() > 0) {
            buf.append(' ');
        }
        return buf.toString();
    }

    protected abstract ToolchainManager.CompilerDescriptor getCompilerDescriptor(CompilerSet cs);
    
    protected String getUserIncludeFlag(CompilerSet cs){
        ToolchainManager.CompilerDescriptor compilerDescriptor = getCompilerDescriptor(cs);
        if (compilerDescriptor != null) {
            return compilerDescriptor.getUserIncludeFlag();
        }
        return ""; //broken tool collection
    }

    protected String getUserFileFlag(CompilerSet cs){
        ToolchainManager.CompilerDescriptor compilerDescriptor = getCompilerDescriptor(cs);
        if (compilerDescriptor != null) {
            return getCompilerDescriptor(cs).getUserFileFlag();
        }
        return ""; //broken tool collection
    }

    protected String getUserMacroFlag(CompilerSet cs){
        ToolchainManager.CompilerDescriptor compilerDescriptor = getCompilerDescriptor(cs);
        if (compilerDescriptor != null) {
            return getCompilerDescriptor(cs).getUserMacroFlag();
        }
        return ""; //broken tool collection
    }

    private static final int DEFAULT_INDEX = 0;
    private static final int DIRTY_INDEX = 1;
    private static final int MODIFIED_INDEX = 2;
    private static final int VALUE_INDEX = 3;
    private static final int BOOLEAN_SIZE = 4;
    private void set(int i, BooleanConfiguration b) {
        if (b.getDefault()) {
            booleanFlags |= 1<<(i*BOOLEAN_SIZE+DEFAULT_INDEX);
        } else {
            booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+DEFAULT_INDEX));
        }
        if (b.getDirty()) {
            booleanFlags |= 1<<(i*BOOLEAN_SIZE+DIRTY_INDEX);
        } else {
            booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+DIRTY_INDEX));
        }
        if (b.getModified()) {
            booleanFlags |= 1<<(i*BOOLEAN_SIZE+MODIFIED_INDEX);
        } else {
            booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+MODIFIED_INDEX));
        }
        if (b.getValue()) {
            booleanFlags |= 1<<(i*BOOLEAN_SIZE+VALUE_INDEX);
        } else {
            booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+VALUE_INDEX));
        }
    }
    
    private BooleanConfiguration get(final int i) {
        return new BooleanConfiguration() {
            @Override
            public void setValue(boolean b) {
                if (b) {
                    booleanFlags |= 1<<(i*BOOLEAN_SIZE+VALUE_INDEX);
                } else {
                    booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+VALUE_INDEX));
                }
                setModified(b != getDefault());
            }

            @Override
            public boolean getValue() {
                return (booleanFlags & 1<<(i*BOOLEAN_SIZE+VALUE_INDEX)) != 0;
            }

            @Override
            public void setModified(boolean b) {
                if (b) {
                    booleanFlags |= 1<<(i*BOOLEAN_SIZE+MODIFIED_INDEX);
                } else {
                    booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+MODIFIED_INDEX));
                }                
            }

            @Override
            public boolean getModified() {
                return (booleanFlags & 1<<(i*BOOLEAN_SIZE+MODIFIED_INDEX)) != 0;
            }

            @Override
            public void setDirty(boolean dirty) {
                if (dirty) {
                    booleanFlags |= 1<<(i*BOOLEAN_SIZE+DIRTY_INDEX);
                } else {
                    booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+DIRTY_INDEX));
                }
            }

            @Override
            public boolean getDirty() {
                return (booleanFlags & 1<<(i*BOOLEAN_SIZE+DIRTY_INDEX)) != 0;
            }

            @Override
            public boolean getDefault() {
                return (booleanFlags & 1<<(i*BOOLEAN_SIZE+DEFAULT_INDEX)) != 0;
            }

            @Override
            public void setDefault(boolean b) {
                if (b) {
                    booleanFlags |= 1<<(i*BOOLEAN_SIZE+DEFAULT_INDEX);
                } else {
                    booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+DEFAULT_INDEX));
                }
                setModified( getValue() != b);
            }

            @Override
            public final void reset() {
                if (getDefault()) {
                    booleanFlags |= 1<<(i*BOOLEAN_SIZE+VALUE_INDEX);
                } else {
                    booleanFlags &= ~(1<<(i*BOOLEAN_SIZE+VALUE_INDEX));
                }
                setModified(false);
            }

            // Clone and Assign
            @Override
            public void assign(BooleanConfiguration conf) {
                boolean dirty = getDirty();
                dirty |= conf.getValue() ^ getValue();
                setDirty(dirty);
                setValue(conf.getValue());
                setModified(conf.getModified());
            }

            @Override
            @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
            public BooleanConfiguration clone() {
                BooleanConfiguration clone = new BooleanConfiguration(getDefault());
                clone.setValue(getValue());
                clone.setModified(getModified());
                return clone;
            }

            @Override
            public String toString() {
                return "{value=" + getValue() + " modified=" + getModified() + " dirty=" + getDirty() +  '}'; // NOI18N
            }
        };
    }

    public static class OptionToString implements VectorConfiguration.ToString<String> {

        private final CompilerSet compilerSet;
        private final String prepend;

        public OptionToString(CompilerSet compilerSet, String prepend) {
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
                return prepend == null ? item : prepend + item;
            } else {
                return ""; // NOI18N
            }
        }
    }
    
    private static interface CppConfigurationContainer {
        void setLibraryLevel(IntConfiguration libraryLevel);
        IntConfiguration getLibraryLevel();
        void setStandardsEvolution(IntConfiguration standardsEvolution);
        IntConfiguration getStandardsEvolution();
        void setLanguageExt(IntConfiguration languageExt);
        IntConfiguration getLanguageExt();
        void setImportantFlags(StringConfiguration importantFlags);
        StringConfiguration getImportantFlags();
        boolean getModified();        
    }
    
    private static final class ManagedCppConfigurationContainer implements CppConfigurationContainer {
        private IntConfiguration libraryLevel;
        private IntConfiguration standardsEvolution;
        private IntConfiguration languageExt;
        
        private ManagedCppConfigurationContainer(CCCCompilerConfiguration master) {
            libraryLevel = new IntConfiguration(master != null ? master.getLibraryLevel() : null, LIBRARY_LEVEL_BINARY, LIBRARY_LEVEL_NAMES, null);
            standardsEvolution = new IntConfiguration(master != null ? master.getStandardsEvolution() : null, STANDARDS_DEFAULT, STANDARDS_NAMES, null);
            languageExt = new IntConfiguration(master != null ? master.getLanguageExt() : null, LANGUAGE_EXT_DEFAULT, LANGUAGE_EXT_NAMES, null);
        }
        
        @Override
        public void setLibraryLevel(IntConfiguration libraryLevel) {
            this.libraryLevel = libraryLevel;
        }

        @Override
        public IntConfiguration getLibraryLevel() {
            return libraryLevel;
        }

        @Override
        public void setStandardsEvolution(IntConfiguration standardsEvolution) {
            this.standardsEvolution = standardsEvolution;
        }

        @Override
        public IntConfiguration getStandardsEvolution() {
            return standardsEvolution;
        }

        @Override
        public void setLanguageExt(IntConfiguration languageExt) {
            this.languageExt = languageExt;
        }

        @Override
        public IntConfiguration getLanguageExt() {
            return languageExt;
        }
        
        @Override
        public void setImportantFlags(StringConfiguration importantFlags) {
        }

        @Override
        public StringConfiguration getImportantFlags() {
            return UNSUPPORTED_STRING_CONFIGURATION;
        }

        @Override
        public boolean getModified() {
            return libraryLevel.getModified() ||
                    standardsEvolution.getModified() ||
                    languageExt.getModified();
        }

    }
    private static final class UnmanagedCppConfigurationContainer implements CppConfigurationContainer {
        private StringConfiguration importantFlags;

        private UnmanagedCppConfigurationContainer() {
            importantFlags = new StringConfiguration(null, "");
        }
        
        @Override
        public void setLibraryLevel(IntConfiguration libraryLevel) {
        }

        @Override
        public IntConfiguration getLibraryLevel() {
            return UNSUPPORTED_INT_CONFIGURATION;
        }

        @Override
        public void setStandardsEvolution(IntConfiguration standardsEvolution) {
        }

        @Override
        public IntConfiguration getStandardsEvolution() {
            return UNSUPPORTED_INT_CONFIGURATION;
        }

        @Override
        public void setLanguageExt(IntConfiguration languageExt) {
        }

        @Override
        public IntConfiguration getLanguageExt() {
            return UNSUPPORTED_INT_CONFIGURATION;
        }
        
        @Override
        public void setImportantFlags(StringConfiguration importantFlags) {
            this.importantFlags = importantFlags;
        }

        @Override
        public StringConfiguration getImportantFlags() {
            return importantFlags;
        }

        @Override
        public boolean getModified() {
            return importantFlags.getValue() != null && !importantFlags.getValue().isEmpty();
        }
    }
}
