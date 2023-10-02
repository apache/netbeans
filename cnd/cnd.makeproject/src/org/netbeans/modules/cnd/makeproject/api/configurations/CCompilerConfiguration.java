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
import java.util.List;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;
import org.openide.util.NbBundle;

public class CCompilerConfiguration extends CCCCompilerConfiguration implements Cloneable {
    
    public static final int STANDARD_DEFAULT = 0;
    public static final int STANDARD_C89 = 1;
    public static final int STANDARD_C99 = 2;
    public static final int STANDARD_C11 = 3;
    public static final int STANDARD_C17 = 4;
    public static final int STANDARD_C23 = 5;
    public static final int STANDARD_INHERITED = 6;
    private static final String[] STANDARD_NAMES = {
        getString("STANDARD_DEFAULT"),
        getString("STANDARD_C89"),
        getString("STANDARD_C99"),
        getString("STANDARD_C11"),
        getString("STANDARD_C17"),
        getString("STANDARD_C23"),
        getString("STANDARD_INHERITED"),};
    private static final String[] STANDARD_NAMES_ROOT = {
        getString("STANDARD_DEFAULT"),
        getString("STANDARD_C89"),
        getString("STANDARD_C99"),
        getString("STANDARD_C11"),
        getString("STANDARD_C17"),
        getString("STANDARD_C23"),};
    private IntConfiguration cStandard;        
    
    // Constructors
    public CCompilerConfiguration(String baseDir, CCompilerConfiguration master, MakeConfiguration owner) {
        super(baseDir, master, owner);
        if (master != null) {
            cStandard = new IntConfiguration(null, STANDARD_INHERITED, STANDARD_NAMES, null);
        } else {
            cStandard = new IntConfiguration(null, STANDARD_DEFAULT, STANDARD_NAMES_ROOT, null);
        }
    }

    public void fixupMasterLinks(CCompilerConfiguration compilerConfiguration) {
        super.fixupMasterLinks(compilerConfiguration);
        getCStandard().setMaster(compilerConfiguration.getCStandard());
    }
       
    public IntConfiguration getCStandard() {
        return cStandard;
    }    
    
    public int getCStandardExternal() {
        switch(getCStandard().getValue()) {
            case STANDARD_DEFAULT: return LanguageFlavor.DEFAULT.toExternal();
            case STANDARD_C89: return LanguageFlavor.C89.toExternal();
            case STANDARD_C99: return LanguageFlavor.C99.toExternal();
            case STANDARD_C11: return LanguageFlavor.C11.toExternal();
            case STANDARD_C17: return LanguageFlavor.C17.toExternal();
            case STANDARD_C23: return LanguageFlavor.C23.toExternal();
            case STANDARD_INHERITED: return LanguageFlavor.UNKNOWN.toExternal();
            default: return LanguageFlavor.UNKNOWN.toExternal();
        }
    }    

    public void setCStandard(IntConfiguration cStandard) {
        this.cStandard = cStandard;
    }
    
    public void setCStandardExternal(int cStandard) {
        if (cStandard == LanguageFlavor.DEFAULT.toExternal()) {
            this.cStandard.setValue(STANDARD_DEFAULT);
        } else if (cStandard == LanguageFlavor.C89.toExternal()) {
            this.cStandard.setValue(STANDARD_C89);
        } else if (cStandard == LanguageFlavor.C99.toExternal()) {
            this.cStandard.setValue(STANDARD_C99);
        } else if (cStandard == LanguageFlavor.C11.toExternal()) {
            this.cStandard.setValue(STANDARD_C11);
        } else if (cStandard == LanguageFlavor.C17.toExternal()) {
            this.cStandard.setValue(STANDARD_C17);
        } else if (cStandard == LanguageFlavor.C23.toExternal()) {
            this.cStandard.setValue(STANDARD_C23);
        } else if (cStandard == LanguageFlavor.UNKNOWN.toExternal()) {
            this.cStandard.setValue(STANDARD_INHERITED);
        }
    }

    @Override
    public boolean getModified() {
        return super.getModified() || getCStandard().getModified();
    }
     
    public boolean isCStandardChanged() {
        return getCStandard().getDirty() && getCStandard().getPreviousValue() != getInheritedCStandard();
    }    
    
    // Clone and assign
    public void assign(CCompilerConfiguration conf) {
        // From XCompiler
        super.assign(conf);
        getCStandard().assign(conf.getCStandard());
    }
    
    @Override
    public CCompilerConfiguration clone() {
        CCompilerConfiguration clone = new CCompilerConfiguration(getBaseDir(), (CCompilerConfiguration)getMaster(), getOwner());
        // BasicCompilerConfiguration
        clone.setDevelopmentMode(getDevelopmentMode().clone());
        clone.setWarningLevel(getWarningLevel().clone());
        clone.setMTLevel(getMTLevel().clone());
        clone.setSixtyfourBits(getSixtyfourBits().clone());
        clone.setStrip(getStrip().clone());
        clone.setAdditionalDependencies(getAdditionalDependencies().clone());
        clone.setTool(getTool().clone());
        clone.setCommandLineConfiguration(getCommandLineConfiguration().clone());
        // From CCCCompiler
        clone.setMTLevel(getMTLevel().clone());
        clone.setLibraryLevel(getLibraryLevel().clone());
        clone.setStandardsEvolution(getStandardsEvolution().clone());
        clone.setLanguageExt(getLanguageExt().clone());
        clone.setIncludeDirectories(getIncludeDirectories().clone());
        clone.setInheritIncludes(getInheritIncludes().clone());
        clone.setIncludeFiles(getIncludeFiles().clone());
        clone.setInheritFiles(getInheritFiles().clone());
        clone.setPreprocessorConfiguration(getPreprocessorConfiguration().clone());
        clone.setInheritPreprocessor(getInheritPreprocessor().clone());
        clone.setUndefinedPreprocessorConfiguration(getUndefinedPreprocessorConfiguration().clone());
        clone.setInheritUndefinedPreprocessor(getInheritUndefinedPreprocessor().clone());
        clone.setUseLinkerLibraries(getUseLinkerLibraries().clone());
        clone.setImportantFlags(getImportantFlags().clone());
        // From CCompiler
        clone.setCStandard(getCStandard().clone());
        return clone;
    }
    
    // Interface OptionsProvider
    @Override
    public String getOptions(AbstractCompiler compiler) {
        StringBuilder options = new StringBuilder("$(COMPILE.c) "); // NOI18N
        options.append(getAllOptions2(compiler)).append(' '); // NOI18N
        options.append(getCommandLineOptions(true));
        return MakeProjectOptionsFormat.reformatWhitespaces(options.toString());
    }

    public String getCFlagsBasic(AbstractCompiler compiler) {
        String options = ""; // NOI18N
        options += compiler.getMTLevelOptions(getMTLevel().getValue()) + " "; // NOI18N
        options += compiler.getStandardEvaluationOptions(getStandardsEvolution().getValue()) + " "; // NOI18N
        options += compiler.getLanguageExtOptions(getLanguageExt().getValue()) + " "; // NOI18N
        //options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " "; // NOI18N
        if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST) {
            options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
        }
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }
    
    public String getCFlags(AbstractCompiler compiler) {
        String options = getCFlagsBasic(compiler) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }
    
    @Override
    public String getAllOptions(Tool tool) {
        if (!(tool instanceof AbstractCompiler)) {
            return "";
        }
        AbstractCompiler compiler = (AbstractCompiler) tool;
        
        StringBuilder options = new StringBuilder();
        options.append(getCFlagsBasic(compiler)).append(" "); // NOI18N
        getMasters(true).forEach((master) -> {
            options.append(master.getCommandLineConfiguration().getValue()).append(" "); // NOI18N
        });
        options.append(getAllOptions2(compiler)).append(" "); // NOI18N
        return MakeProjectOptionsFormat.reformatWhitespaces(options.toString());
    }
    
    public String getAllOptions2(AbstractCompiler compiler) {
        String options = ""; // NOI18N
        if (getDevelopmentMode().getValue() != DEVELOPMENT_MODE_TEST) {
            options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " "; // NOI18N
        }
        options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " "; // NOI18N
        options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += getPreprocessorOptions(compiler.getCompilerSet());
        options += getIncludeDirectoriesOptions(compiler.getCompilerSet());
        options += getLibrariesFlags();
        options += compiler.getCStandardOptions(getInheritedCStandard());
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }
    
    public int getInheritedCStandard() {
        for(BasicCompilerConfiguration master : getMasters(true)) {
            if (((CCompilerConfiguration)master).getCStandard().getValue() != STANDARD_INHERITED) {
                return ((CCompilerConfiguration)master).getCStandard().getValue();
            }
        }
        return STANDARDS_DEFAULT;
    }
    
    public String getPreprocessorOptions(CompilerSet cs) {
        List<CCompilerConfiguration> list = new ArrayList<>();
        for(BasicCompilerConfiguration master : getMasters(true)) {
            list.add((CCompilerConfiguration)master);
            if (!((CCompilerConfiguration)master).getInheritPreprocessor().getValue()) {
                break;
            }
        }
        OptionToString visitor = new OptionToString(null, getUserMacroFlag(cs));
        StringBuilder options = new StringBuilder();
        for(int i = list.size() - 1; i >= 0; i--) {
            options.append(list.get(i).getPreprocessorConfiguration().toString(visitor)).append(' '); // NOI18N
        }
        return options.toString();
    }
    
    public String getIncludeDirectoriesOptions(CompilerSet cs) {
        List<CCompilerConfiguration> list = new ArrayList<>();
        for(BasicCompilerConfiguration master : getMasters(true)) {
            list.add((CCompilerConfiguration)master);
            if (!((CCompilerConfiguration)master).getInheritIncludes().getValue()) {
                break;
            }
        }
        OptionToString visitor = new OptionToString(cs, getUserIncludeFlag(cs));
        StringBuilder options = new StringBuilder(getIncludeDirectories().toString(visitor)).append(' '); // NOI18N
        for(int i = list.size() - 1; i > 0; i--) {
            options.append(list.get(i).getIncludeDirectories().toString(visitor)).append(' '); // NOI18N
        }
        String includeFilesOptions = getIncludeFilesOptions(cs);
        if (includeFilesOptions.isEmpty()) {
            return options.toString();
        }
        return options.toString()+" "+includeFilesOptions; //NOI18N
    } 

    private String getIncludeFilesOptions(CompilerSet cs) {
        List<CCompilerConfiguration> list = new ArrayList<>();
        for(BasicCompilerConfiguration master : getMasters(true)) {
            list.add((CCompilerConfiguration)master);
            if (!((CCompilerConfiguration)master).getInheritFiles().getValue()) {
                break;
            }
        }
        OptionToString visitor = new OptionToString(cs, getUserFileFlag(cs));
        StringBuilder options = new StringBuilder(getIncludeFiles().toString(visitor)).append(' '); // NOI18N
        for(int i = list.size() - 1; i > 0; i--) {
            options.append(list.get(i).getIncludeFiles().toString(visitor)).append(' '); // NOI18N
        }
        return options.toString();
    } 

    @Override
    protected ToolchainManager.CompilerDescriptor getCompilerDescriptor(CompilerSet cs) {
        return cs.getCompilerFlavor().getToolchainDescriptor().getC();
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCompilerConfiguration.class, s);
    }
}
