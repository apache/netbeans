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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.util.NbBundle;

public abstract class BasicCompilerConfiguration implements AllOptionsProvider, ConfigurationBase {

    private String baseDir;
    private BasicCompilerConfiguration master;
    public static final int DEVELOPMENT_MODE_FAST = 0;
    public static final int DEVELOPMENT_MODE_DEBUG = 1;
    public static final int DEVELOPMENT_MODE_DEBUG_PERF = 2;
    public static final int DEVELOPMENT_MODE_TEST = 3;
    public static final int DEVELOPMENT_MODE_RELEASE_DIAG = 4;
    public static final int DEVELOPMENT_MODE_RELEASE = 5;
    public static final int DEVELOPMENT_MODE_RELEASE_PERF = 6;
    private static final String[] DEVELOPMENT_MODE_NAMES = {
        getString("FastBuildTxt"),
        getString("DebugTxt"),
        getString("PerformanceDebugTxt"),
        getString("TestCoverageTxt"),
        getString("DiagnosableReleaseTxt"),
        getString("ReleaseTxt"),
        getString("PerformanceReleaseTxt"),};
    public static final int WARNING_LEVEL_NO = 0;
    public static final int WARNING_LEVEL_DEFAULT = 1;
    public static final int WARNING_LEVEL_MORE = 2;
    public static final int WARNING_LEVEL_TAGS = 3;
    public static final int WARNING_LEVEL_CONVERT = 4;
    public static final int WARNING_LEVEL_32_64 = 5;
    private static final String[] WARNING_LEVEL_NAMES = {
        getString("NoWarningsTxt"),
        getString("SomeWarningsTxt"),
        getString("MoreWarningsTxt"),
        getString("ConvertWarningsTxt"),};
    public static final int BITS_DEFAULT = 0;
    public static final int BITS_32 = 1;
    public static final int BITS_64 = 2;
    private static final String[] BITS_NAMES = {
        getString("BITS_DEFAULT"),
        getString("BITS_32"),
        getString("BITS_64"),};
    public static final int MT_LEVEL_NONE = 0;
    public static final int MT_LEVEL_SAFE = 1;
    public static final int MT_LEVEL_AUTOMATIC = 2;
    public static final int MT_LEVEL_OPENMP = 3;
    private static final String[] MT_LEVEL_NAMES = {
        getString("NoneTxt"),
        getString("SafeTxt"),
        getString("AutomaticTxt"),
        getString("OpenMPTxt"),};
    private MakeConfiguration owner;
    private final ConfigurationContainer container;

    // Constructors
    protected BasicCompilerConfiguration(String baseDir, BasicCompilerConfiguration master, MakeConfiguration owner) {
        assert owner != null;
        this.owner = owner;
        this.baseDir = baseDir;
        this.master = master;
        if (!owner.isMakefileConfiguration()) {
            // managed configuration propertyes only
            container = new ManagedConfigurationContainer(master);
        } else {
            container = UNMANAGED_CONTAINER;
        }
    }

    /**
     * @return the owner
     */
    public MakeConfiguration getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(MakeConfiguration owner) {
        this.owner = owner;
    }

    public void fixupMasterLinks(BasicCompilerConfiguration compilerConfiguration) {
        getDevelopmentMode().setMaster(compilerConfiguration.getDevelopmentMode());
        getWarningLevel().setMaster(compilerConfiguration.getWarningLevel());
        getSixtyfourBits().setMaster(compilerConfiguration.getSixtyfourBits());
        getStrip().setMaster(compilerConfiguration.getStrip());
        getAdditionalDependencies().setMaster(compilerConfiguration.getAdditionalDependencies());
        getTool().setMaster(compilerConfiguration.getTool());
    }

    @Override
    public boolean getModified() {
        return container.getModified();
    }

    // baseDir
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    // To be overridden
    public abstract String getOptions(AbstractCompiler compiler);

    // Master
    public void setMaster(BasicCompilerConfiguration master) {
        this.master = master;
    }

    public BasicCompilerConfiguration getMaster() {
        return master;
    }

    public List<BasicCompilerConfiguration> getMasters(boolean addThis) {
        List<BasicCompilerConfiguration> res = new ArrayList<>();
        if (addThis) {
            res.add(this);
        }
        BasicCompilerConfiguration current = master;
        while (current != null) {
            if (res.contains(current)) {
                assert false:"Infinite loop in configurations"; //NOI18N
                break;
            }
            res.add(current);
            current = current.getMaster();
        }
        return res;
    }
    
    // Development Mode
    public void setDevelopmentMode(IntConfiguration developmentMode) {
        container.setDevelopmentMode(developmentMode);
    }

    public IntConfiguration getDevelopmentMode() {
        return container.getDevelopmentMode();
    }

    // Warning Level
    public void setWarningLevel(IntConfiguration warningLevel) {
        container.setWarningLevel(warningLevel);
    }

    public IntConfiguration getWarningLevel() {
        return container.getWarningLevel();
    }

    // SixtyfourBits
    public void setSixtyfourBits(IntConfiguration sixtyfourBits) {
        container.setSixtyfourBits(sixtyfourBits);
    }

    public IntConfiguration getSixtyfourBits() {
        return container.getSixtyfourBits();
    }

    // MT Level
    public void setMTLevel(IntConfiguration mpLevel) {
        container.setMTLevel(mpLevel);
    }

    public IntConfiguration getMTLevel() {
        return container.getMTLevel();
    }
    
    // Strip
    public void setStrip(InheritedBooleanConfiguration strip) {
        container.setStrip(strip);
    }

    public InheritedBooleanConfiguration getStrip() {
        return container.getStrip();
    }

    public void setAdditionalDependencies(StringConfiguration additionalDependencies) {
        container.setAdditionalDependencies(additionalDependencies);
    }

    public StringConfiguration getAdditionalDependencies() {
        return container.getAdditionalDependencies();
    }

    // Tool
    public void setTool(StringConfiguration tool) {
        container.setTool(tool);
    }

    public StringConfiguration getTool() {
        return container.getTool();
    }

    // CommandLine
    public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
        container.setCommandLineConfiguration(commandLineConfiguration);
    }
    
    public OptionsConfiguration getCommandLineConfiguration() {
        return container.getCommandLineConfiguration();
    }
    
    protected String getCommandLineOptions(boolean inherit) {
        if (!inherit) {
            return getCommandLineConfiguration().getValue();
        }

        List<String> options = new LinkedList<>();
        //to fix bz#231603 - C/C++ Additional options passed to commandline twice
        //we have $(COMPILE.cc) in makefile which is extended to COMPILE.c=$(CC) $(CFLAGS) $(CPPFLAGS) -c        
        //so when user adds some additional options on the project level
        //they are recorded as CFLAGS in Makefile
        //and there is no any need to return them when compile target is written
        //that's why we check if parent.getMaster() != null
        List<BasicCompilerConfiguration> masters = getMasters(true);
        for(int i = 0; i < masters.size() -1; i++) {
            options.add(0, masters.get(i).getCommandLineConfiguration().getValue());
        }
        StringBuilder sb = new StringBuilder();
        options.forEach((opt) -> {
            sb.append(opt).append(' ');
        });
        return sb.toString().trim();
    }

    public String getOutputFile(Item item, MakeConfiguration conf, boolean expanded) {
        String filePath = item.getPath(true);
        // qmake generated Makefile expects to find all object files in one directory 
        // the project directory by default. So for Qt projects we need to get rid of any paths.
        // In other case in won't be possible to compile single file, which is not located 
        // in not a project directory.
        // See Bug 189542 - compile single file (QT)
        if (conf.isQmakeConfiguration()) {            
            filePath = CndPathUtilities.getBaseName(filePath);
        }
        String fileName = filePath;
        String suffix = ".o"; // NOI18N
        boolean append = false;
        if (item.hasHeaderOrSourceExtension(false, false)) {
            suffix = ".pch"; // NOI18N
            ItemConfiguration itemConf = item.getItemConfiguration(conf);
            if (conf.getCompilerSet().getCompilerSet() != null && itemConf != null) {
                AbstractCompiler compiler = (AbstractCompiler) conf.getCompilerSet().getCompilerSet().getTool(itemConf.getTool());
                if (compiler != null && compiler.getDescriptor() != null) {
                    suffix = compiler.getDescriptor().getPrecompiledHeaderSuffix();
                    append = compiler.getDescriptor().getPrecompiledHeaderSuffixAppend();
                }
            }
        }
        int i = fileName.lastIndexOf('.'); // NOI18N
        if (i >= 0 && !append) {
            fileName = fileName.substring(0, i) + suffix;
        } else {
            fileName = fileName + suffix;
        }

        String dirName;
        if (item.getFolder() != null && item.getFolder().isTest()) {
            if (expanded) {
                dirName = ConfigurationMakefileWriter.getTestObjectDir(conf);
            } else {
                // I think it's a good idea to get rid of hardcoded paths
                // (i.e. MakeActionProvider::getOutputFile, ConfigurationMakefileWriter::writeLinkTestTarget, ...)
                // target.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}");
                dirName = "${TESTDIR}"; // NOI18N
            }
        } else {
            if (expanded) {
                dirName = ConfigurationMakefileWriter.getObjectDir(conf);
            } else {
                dirName = MakeConfiguration.OBJECTDIR_MACRO;
            }
        }

        if (CndPathUtilities.isPathAbsolute(fileName) || filePath.startsWith("..")) { // NOI18N;
            String ofileName = CndPathUtilities.getBaseName(fileName);
            String odirName = CndPathUtilities.getDirName(fileName);
            if (odirName == null) {
                odirName = ""; // NOI18N
            }
            String absPath = dirName + '/' + MakeConfiguration.EXT_FOLDER + '/' + Integer.toHexString(odirName.hashCode()) + '/' + ofileName; // UNIX path
            absPath = CndPathUtilities.replaceOddCharacters(absPath, '_');
            return absPath;
        } else {
            fileName = CndPathUtilities.escapeOddCharacters(fileName);
            return dirName + '/' + fileName; // UNIX path
        }
    }

    // Assigning & Cloning
    protected void assign(BasicCompilerConfiguration conf) {
        setBaseDir(conf.getBaseDir());
        getDevelopmentMode().assign(conf.getDevelopmentMode());
        getWarningLevel().assign(conf.getWarningLevel());
        getSixtyfourBits().assign(conf.getSixtyfourBits());
        getStrip().assign(conf.getStrip());
        getMTLevel().assign(conf.getMTLevel());
        getAdditionalDependencies().assign(conf.getAdditionalDependencies());
        getTool().assign(conf.getTool());
        getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(BasicCompilerConfiguration.class, s);
    }
    
    private static interface ConfigurationContainer {
        void setDevelopmentMode(IntConfiguration developmentMode);
        IntConfiguration getDevelopmentMode();
        void setWarningLevel(IntConfiguration warningLevel);
        IntConfiguration getWarningLevel();
        void setSixtyfourBits(IntConfiguration sixtyfourBits);
        IntConfiguration getSixtyfourBits();
        void setMTLevel(IntConfiguration mpLevel);
        IntConfiguration getMTLevel();
        void setStrip(InheritedBooleanConfiguration strip);
        InheritedBooleanConfiguration getStrip();
        void setAdditionalDependencies(StringConfiguration additionalDependencies);
        StringConfiguration getAdditionalDependencies();
        void setTool(StringConfiguration tool);
        StringConfiguration getTool();
        void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration);
        OptionsConfiguration getCommandLineConfiguration();
        boolean getModified();
    }
    
    private static final class ManagedConfigurationContainer implements ConfigurationContainer {
        private IntConfiguration developmentMode;
        private IntConfiguration warningLevel;
        private IntConfiguration sixtyfourBits;
        private InheritedBooleanConfiguration strip;
        private IntConfiguration mpLevel;
        private StringConfiguration additionalDependencies;
        private StringConfiguration tool;
        private OptionsConfiguration commandLineConfiguration;
        private ManagedConfigurationContainer(BasicCompilerConfiguration master) {
            developmentMode = new IntConfiguration(master != null ? master.getDevelopmentMode() : null, DEVELOPMENT_MODE_DEBUG, DEVELOPMENT_MODE_NAMES, null);
            warningLevel = new IntConfiguration(master != null ? master.getWarningLevel() : null, WARNING_LEVEL_DEFAULT, WARNING_LEVEL_NAMES, null);
            sixtyfourBits = new IntConfiguration(master != null ? master.getSixtyfourBits() : null, BITS_DEFAULT, BITS_NAMES, null);
            strip = new InheritedBooleanConfiguration(master != null ? master.getStrip() : null, false);
            mpLevel = new IntConfiguration(master != null ? master.getMTLevel() : null, MT_LEVEL_NONE, MT_LEVEL_NAMES, null);
            additionalDependencies = new StringConfiguration(master != null ? master.getAdditionalDependencies() : null, ""); // NOI18N
            tool = new StringConfiguration(master != null ? master.getTool() : null, ""); // NOI18N
            commandLineConfiguration = new OptionsConfiguration();
        }
        
        @Override
        public void setDevelopmentMode(IntConfiguration developmentMode) {
            this.developmentMode = developmentMode;
        }

        @Override
        public IntConfiguration getDevelopmentMode() {
            return developmentMode;
        }

        @Override
        public void setWarningLevel(IntConfiguration warningLevel) {
            this.warningLevel = warningLevel;
        }

        @Override
        public IntConfiguration getWarningLevel() {
            return warningLevel;
        }

        @Override
        public void setSixtyfourBits(IntConfiguration sixtyfourBits) {
            this.sixtyfourBits = sixtyfourBits;
        }

        @Override
        public IntConfiguration getSixtyfourBits() {
            return sixtyfourBits;
        }

        @Override
        public void setMTLevel(IntConfiguration mpLevel) {
            this.mpLevel = mpLevel;
        }

        @Override
        public IntConfiguration getMTLevel() {
            return mpLevel;
        }

        @Override
        public void setStrip(InheritedBooleanConfiguration strip) {
            this.strip = strip;
        }

        @Override
        public InheritedBooleanConfiguration getStrip() {
            return strip;
        }

        @Override
        public void setAdditionalDependencies(StringConfiguration additionalDependencies) {
            this.additionalDependencies = additionalDependencies;
        }

        @Override
        public StringConfiguration getAdditionalDependencies() {
            return additionalDependencies;
        }

        @Override
        public void setTool(StringConfiguration tool) {
            this.tool = tool;
        }

        @Override
        public StringConfiguration getTool() {
            return tool;
        }

        @Override
        public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
            this.commandLineConfiguration = commandLineConfiguration;
        }
    
        @Override
        public OptionsConfiguration getCommandLineConfiguration() {
            return commandLineConfiguration;
        }
        
        @Override
        public boolean getModified() {
            return developmentMode.getModified() ||
                   mpLevel.getModified() ||
                   warningLevel.getModified() ||
                   sixtyfourBits.getModified() ||
                   strip.getModified() ||
                   additionalDependencies.getModified() ||
                   tool.getModified() ||
                   commandLineConfiguration.getModified();
        }
    }
    
    private static final ConfigurationContainer UNMANAGED_CONTAINER = new UnmanagedConfigurationContainer();
    
    private static final class UnmanagedConfigurationContainer implements ConfigurationContainer {

        @Override
        public void setDevelopmentMode(IntConfiguration developmentMode) {
        }

        @Override
        public IntConfiguration getDevelopmentMode() {
            return UNSUPPORTED_INT_CONFIGURATION;
        }

        @Override
        public void setWarningLevel(IntConfiguration warningLevel) {
        }

        @Override
        public IntConfiguration getWarningLevel() {
            return UNSUPPORTED_INT_CONFIGURATION;
        }

        @Override
        public void setSixtyfourBits(IntConfiguration sixtyfourBits) {
        }

        @Override
        public IntConfiguration getSixtyfourBits() {
            return UNSUPPORTED_INT_CONFIGURATION;
        }

        @Override
        public void setMTLevel(IntConfiguration mpLevel) {
        }

        @Override
        public IntConfiguration getMTLevel() {
            return UNSUPPORTED_INT_CONFIGURATION;
        }

        @Override
        public void setStrip(InheritedBooleanConfiguration strip) {
        }

        @Override
        public InheritedBooleanConfiguration getStrip() {
            return UNSUPPORTED_INHERITED_BOOLEAN_CONFIGURATION;
        }

        @Override
        public void setAdditionalDependencies(StringConfiguration additionalDependencies) {
        }

        @Override
        public StringConfiguration getAdditionalDependencies() {
            return UNSUPPORTED_STRING_CONFIGURATION;
        }

        @Override
        public void setTool(StringConfiguration tool) {
        }

        @Override
        public StringConfiguration getTool() {
            return UNSUPPORTED_STRING_CONFIGURATION;
        }

        @Override
        public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
        }

        @Override
        public OptionsConfiguration getCommandLineConfiguration() {
            return UNSUPPORTED_OPTIONS_CONFIGURATION;
        }

        @Override
        public boolean getModified() {
            return false;
        }
    }
    
    private static final String UNSUPPORTED = "Unsupported"; // NOI18N
    protected static final IntConfiguration UNSUPPORTED_INT_CONFIGURATION = new UnsupportedIntConfiguration();
    protected static final StringConfiguration UNSUPPORTED_STRING_CONFIGURATION = new UnsupportedStringConfiguration();
    protected static final BooleanConfiguration UNSUPPORTED_BOOLEAN_CONFIGURATION= new UnsupportedBooleanConfiguration();
    protected static final InheritedBooleanConfiguration UNSUPPORTED_INHERITED_BOOLEAN_CONFIGURATION= new UnsupportedInheritedBooleanConfiguration();
    protected static final OptionsConfiguration UNSUPPORTED_OPTIONS_CONFIGURATION = new UnsupportedOptionsConfiguration();
    
    private static final class UnsupportedIntConfiguration extends IntConfiguration {

        protected UnsupportedIntConfiguration() {
        }

        @Override
        public void setMaster(IntConfiguration master) {
        }

        @Override
        public void setValue(int value) {
        }

        @Override
        public void setValue(String s) {
        }

        @Override
        public int getValue() {
            return 0;
        }

        @Override
        public final void setModified(boolean b) {
        }

        @Override
        public boolean getModified() {
            return false;
        }

        @Override
        public void setDirty(boolean dirty) {
        }

        @Override
        public boolean getDirty() {
            return false;
        }

        @Override
        public int getDefault() {
            return 0;
        }

        @Override
        public void setDefault(int def) {
        }

        @Override
        public void reset() {
        }

        @Override
        public String getName() {
            return UNSUPPORTED;
        }

        @Override
        public String[] getNames() {
            return new String[]{UNSUPPORTED};
        }

        @Override
        public String getOption() {
            return UNSUPPORTED;
        }

        // Clone and Assign
        @Override
        public void assign(IntConfiguration conf) {
        }

        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
        public IntConfiguration clone() {
            return this;
        }

        @Override
        public byte getPreviousValue() {
            return 0;
        }

        @Override
        public String toString() {
            return "(" + getValue() + ")" + getName(); // NOI18N
        }

    }
    
    private static final class UnsupportedStringConfiguration extends StringConfiguration {
        protected UnsupportedStringConfiguration() {
        }

        @Override
        public void setMaster(StringConfiguration master) {
        }

        @Override
        public void setValue(String b) {
        }

        @Override
        public String getValue() {
            return "";
        }

        @Override
        public String getValueDef(String def) {
            return "";
        }

        @Override
        public String getValue(String delim) {
            return "";
        }

        @Override
        public void setModified(boolean b) {
        }

        @Override
        public boolean getModified() {
            return false;
        }

        @Override
        public String getDefault() {
            return "";
        }

        @Override
        public void reset() {
        }

        @Override
        public void setDefaultValue(String def) {
        }

        // Clone and Assign
        @Override
        public void assign(StringConfiguration conf) {
        }

        @Override
        public StringConfiguration clone() {
            return this;
        }

        @Override
        public String toString() {
            return "{value=" + getValue() + " modified=" + getModified() + '}'; // NOI18N
        }
    }
    
    private static final class UnsupportedBooleanConfiguration extends BooleanConfiguration {
        protected UnsupportedBooleanConfiguration() {
        }

        @Override
        public void setValue(boolean b) {
        }

        @Override
        public boolean getValue() {
            return false;
        }

        @Override
        public void setModified(boolean b) {
        }

        @Override
        public boolean getModified() {
            return false;
        }

        @Override
        public void setDirty(boolean dirty) {
        }

        @Override
        public boolean getDirty() {
            return false;
        }

        @Override
        public boolean getDefault() {
            return false;
        }

        @Override
        public void setDefault(boolean b) {
        }

        @Override
        public void reset() {
        }

        // Clone and Assign
        @Override
        public void assign(BooleanConfiguration conf) {
        }

        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
        public BooleanConfiguration clone() {
            return this;
        }

        @Override
        public String toString() {
            return "{value=" + getValue() + " modified=" + getModified() + " dirty=" + getDirty() +  '}'; // NOI18N
        }
    }

    private static final class UnsupportedInheritedBooleanConfiguration extends InheritedBooleanConfiguration {
        protected UnsupportedInheritedBooleanConfiguration() {
        }

        @Override
        protected BooleanConfiguration getMaster() {
            return null;
        }

        @Override
        public void setMaster(InheritedBooleanConfiguration master) {
        }


        @Override
        public void setValue(boolean b) {
        }

        @Override
        public boolean getValue() {
            return false;
        }

        @Override
        public void setModified(boolean b) {
        }

        @Override
        public boolean getModified() {
            return false;
        }

        @Override
        public void setDirty(boolean dirty) {
        }

        @Override
        public boolean getDirty() {
            return false;
        }

        @Override
        public boolean getDefault() {
            return false;
        }

        @Override
        public void setDefault(boolean b) {
        }

        @Override
        public void reset() {
        }

        // Clone and Assign
        @Override
        public void assign(InheritedBooleanConfiguration conf) {
        }

        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
        public InheritedBooleanConfiguration clone() {
            return this;
        }

        @Override
        public String toString() {
            return "{value=" + getValue() + " modified=" + getModified() + " dirty=" + getDirty() +  '}'; // NOI18N
        }
    }
    
    private static final class UnsupportedOptionsConfiguration extends OptionsConfiguration {
        protected UnsupportedOptionsConfiguration() {
        }

        @Override
        public void setDirty(boolean dirty) {
        }

        @Override
        public boolean getDirty() {
            return false;
        }

        @Override
        public void setValue(String commandLine) {
        }
        
        @Override
        public String getValue() {
            return "";
        }
        
        @Override
        public void setModified(boolean b) {
        }

        @Override
        public boolean getModified() {
            return false;
        }
        
        @Override
        public String getDefault() {
            return "";
        }

        @Override
        public void optionsReset() {
        }

        // Predefined
        @Override
        public void setPreDefined(String preDefined) {
        }

        @Override
        public String getPreDefined() {
            return "";
        }

        // Clone and assign
        @Override
        public void assign(OptionsConfiguration conf) {
        }

        @Override
        public OptionsConfiguration clone() {
            return this;
        }

        @Override
        public String toString() {
            return "{commandLine=" + getValue() + "] dirty=" + getDirty() + // NOI18N
                    " commandLineModified=" + getModified() + '}'; // NOI18N
        }
    }
}
