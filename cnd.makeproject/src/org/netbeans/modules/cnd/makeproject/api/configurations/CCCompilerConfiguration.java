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

public class CCCompilerConfiguration extends CCCCompilerConfiguration implements Cloneable {

    public static final int STANDARD_DEFAULT = 0;
    public static final int STANDARD_CPP98 = 1;
    public static final int STANDARD_CPP11 = 2;
    public static final int STANDARD_CPP14 = 3;
    public static final int STANDARD_CPP17 = 4;
    public static final int STANDARD_INHERITED = 5;
    private static final String[] STANDARD_NAMES = {
        getString("STANDARD_DEFAULT"),
        getString("STANDARD_CPP98"),
        getString("STANDARD_CPP11"),
        getString("STANDARD_CPP14"),
        getString("STANDARD_CPP17"),
        getString("STANDARD_INHERITED"),
    };
    private static final String[] STANDARD_NAMES_ROOT = {
        getString("STANDARD_DEFAULT"),
        getString("STANDARD_CPP98"),
        getString("STANDARD_CPP11"),
        getString("STANDARD_CPP14"),
        getString("STANDARD_CPP17"),
    };    
    private IntConfiguration cppStandard;    
    
    // Constructors
    public CCCompilerConfiguration(String baseDir, CCCompilerConfiguration master, MakeConfiguration owner) {
        super(baseDir, master, owner);
        if (master != null) {
            cppStandard = new IntConfiguration(null, STANDARD_INHERITED, STANDARD_NAMES, null);
        } else {
            cppStandard = new IntConfiguration(null, STANDARD_DEFAULT, STANDARD_NAMES_ROOT, null);
        }
    }
    
    public void fixupMasterLinks(CCCompilerConfiguration compilerConfiguration) {
        super.fixupMasterLinks(compilerConfiguration);
        getCppStandard().setMaster(compilerConfiguration.getCppStandard());
    }    
    
    public IntConfiguration getCppStandard() {
        return cppStandard;
    }    

    public int getCppStandardExternal() {
        switch(getCppStandard().getValue()) {
            case STANDARD_DEFAULT: return LanguageFlavor.DEFAULT.toExternal();
            case STANDARD_CPP98: return LanguageFlavor.CPP98.toExternal();
            case STANDARD_CPP11: return LanguageFlavor.CPP11.toExternal();
            case STANDARD_CPP14: return LanguageFlavor.CPP14.toExternal();
            case STANDARD_CPP17: return LanguageFlavor.CPP17.toExternal();
            case STANDARD_INHERITED:  return LanguageFlavor.UNKNOWN.toExternal();
            default: return LanguageFlavor.UNKNOWN.toExternal();
        }
    }    
    
    public void setCppStandard(IntConfiguration cppStandard) {
        this.cppStandard = cppStandard;
    }

    public void setCppStandardExternal(int cppStandard) {
        if (cppStandard == LanguageFlavor.DEFAULT.toExternal()) {
            this.cppStandard.setValue(STANDARD_DEFAULT);
        } else if (cppStandard == LanguageFlavor.CPP98.toExternal()) {
            this.cppStandard.setValue(STANDARD_CPP98);
        } else if (cppStandard == LanguageFlavor.CPP11.toExternal()) {
            this.cppStandard.setValue(STANDARD_CPP11);
        } else if (cppStandard == LanguageFlavor.CPP14.toExternal()) {
            this.cppStandard.setValue(STANDARD_CPP14);
        } else if (cppStandard == LanguageFlavor.CPP17.toExternal()) {
            this.cppStandard.setValue(STANDARD_CPP17);
        } else if (cppStandard == LanguageFlavor.UNKNOWN.toExternal()) {
            this.cppStandard.setValue(STANDARD_INHERITED);
        }
    }
    
    // Clone and assign
    public void assign(CCCompilerConfiguration conf) {
        // From XCompiler
        super.assign(conf);
        getCppStandard().assign(conf.getCppStandard());
    }
    
    @Override
    public boolean getModified() {
        return super.getModified() || getCppStandard().getModified();
    }    
    
    public boolean isCppStandardChanged() {
        return getCppStandard().getDirty() && getCppStandard().getPreviousValue() != getInheritedCppStandard();
    }
    
    // Cloning
    @Override
    public CCCompilerConfiguration clone() {
        CCCompilerConfiguration clone = new CCCompilerConfiguration(getBaseDir(), (CCCompilerConfiguration)getMaster(),  getOwner());
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
        // From CCCompiler
        clone.setCppStandard(getCppStandard().clone());
        return clone;
    }
    
    // Interface OptionsProvider
    @Override
    public String getOptions(AbstractCompiler compiler) {
        StringBuilder options = new StringBuilder("$(COMPILE.cc) "); // NOI18N
        options.append(getAllOptions2(compiler)).append(' '); // NOI18N
        options.append(getCommandLineOptions(true));
        return MakeProjectOptionsFormat.reformatWhitespaces(options.toString());
    }

    public String getCCFlagsBasic(AbstractCompiler compiler) {
        String options = ""; // NOI18N
        options += compiler.getMTLevelOptions(getMTLevel().getValue()) + " "; // NOI18N
        options += compiler.getLibraryLevelOptions(getLibraryLevel().getValue()) + " "; // NOI18N
        options += compiler.getStandardEvaluationOptions(getStandardsEvolution().getValue()) + " "; // NOI18N
        options += compiler.getLanguageExtOptions(getLanguageExt().getValue()) + " "; // NOI18N
        //options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " "; // NOI18N
        if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST) {
            options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
        }
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }
    
    public String getCCFlags(AbstractCompiler compiler) {
        String options = getCCFlagsBasic(compiler) + " "; // NOI18N
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
        options.append(getCCFlagsBasic(compiler)).append(" "); // NOI18N
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
        options += compiler.getCppStandardOptions(getInheritedCppStandard());
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    public int getInheritedCppStandard() {
        for(BasicCompilerConfiguration master : getMasters(true)) {
            if (((CCCompilerConfiguration)master).getCppStandard().getValue() != STANDARD_INHERITED) {
                return ((CCCompilerConfiguration)master).getCppStandard().getValue();
            }
        }
        return STANDARDS_DEFAULT;
    }
    
    public String getPreprocessorOptions(CompilerSet cs) {
        List<CCCCompilerConfiguration> list = new ArrayList<>();
        for(BasicCompilerConfiguration master : getMasters(true)) {
            list.add((CCCompilerConfiguration)master);
            if (!((CCCompilerConfiguration)master).getInheritPreprocessor().getValue()) {
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
        List<CCCCompilerConfiguration> list = new ArrayList<>();
        for(BasicCompilerConfiguration master : getMasters(true)) {
            list.add((CCCCompilerConfiguration)master);
            if (!((CCCCompilerConfiguration)master).getInheritIncludes().getValue()) {
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
        List<CCCCompilerConfiguration> list = new ArrayList<>();
        for(BasicCompilerConfiguration master : getMasters(true)) {
            list.add((CCCCompilerConfiguration)master);
            if (!((CCCCompilerConfiguration)master).getInheritFiles().getValue()) {
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
        return cs.getCompilerFlavor().getToolchainDescriptor().getCpp();
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCCompilerConfiguration.class, s);
    }
}
