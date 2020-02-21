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
package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.ui.configurations.LibrariesNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.VectorNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.utils.TokenizerFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class LinkerGeneralCustomizerNode extends CustomizerNode {

    public LinkerGeneralCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        switch (getContext().getKind()) {
            case Folder:
                // folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()
                Folder[] folders = getContext().getFolders();
                List<Sheet> folderSheets = new ArrayList<>();
                for (Folder folder : folders) {
                    if (folder.isTest()) {
                        folderSheets.add(
                                getGeneralSheet(
                                        getContext().getProject(), (MakeConfigurationDescriptor) getContext().getConfigurationDescriptor(),
                                        (MakeConfiguration) configuration, getContext().isQtMode(), false, folder.getFolderConfiguration(configuration).getLinkerConfiguration())
                        );
                    } else {
                        folderSheets.add(
                                getGeneralSheet(
                                        getContext().getProject(), (MakeConfigurationDescriptor) getContext().getConfigurationDescriptor(),
                                        (MakeConfiguration) configuration, getContext().isQtMode(), true, folder.getFolderConfiguration(configuration).getLinkerConfiguration())
                        );
                    }
                }
            return folderSheets.toArray(new Sheet[folderSheets.size()]);
            case Project:
                return new Sheet[]{
                    getGeneralSheet(
                    getContext().getProject(), (MakeConfigurationDescriptor) getContext().getConfigurationDescriptor(),
                    (MakeConfiguration) configuration, getContext().isQtMode(), false, ((MakeConfiguration) configuration).getLinkerConfiguration())
                };
        }
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsLinking"); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getMessage(LinkerGeneralCustomizerNode.class, s);
    }
    
    private Sheet getGeneralSheet(Project project, MakeConfigurationDescriptor configurationDescriptor, MakeConfiguration conf, boolean isQtMode, boolean inheritablePropertiesOnly, LinkerConfiguration lc) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        ToolchainManager.LinkerDescriptor linker = null;
        String linkDriver = null;
        String[] texts;
        if (compilerSet != null) {
            linker = compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker();
            if (conf.hasCPPFiles(configurationDescriptor)) {
                AbstractCompiler ccCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
                linkDriver = ccCompiler.getName();
            } else {
                AbstractCompiler cCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
                linkDriver = cCompiler.getName();
            }
        }

        Sheet.Set set1 = new Sheet.Set();
        set1.setName("General"); // NOI18N
        set1.setDisplayName(getString("GeneralTxt"));
        set1.setShortDescription(getString("GeneralHint"));
        if (!isQtMode) {
            if (!inheritablePropertiesOnly) {
                set1.put(new OutputNodeProp(lc.getOutput(), lc.getOutputDefault(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
            }
            set1.put(new VectorNodeProp(lc.getAdditionalLibs(), null, lc.getMakeConfiguration().getBaseFSPath(), new String[]{"AdditionalLibraryDirectories", getString("AdditionalLibraryDirectoriesTxt"), getString("AdditionalLibraryDirectoriesHint")},
                    true, JFileChooser.DIRECTORIES_ONLY, new HelpCtx("AddtlLibraryDirectories")){ // NOI18N
                @Override
                protected List<String> convertToList(String text) {
                    return TokenizerFactory.DEFAULT_CONVERTER.convertToList(text);
                }
                @Override
                protected String convertToString(List<String> list) {
                    return TokenizerFactory.DEFAULT_CONVERTER.convertToString(list);
                }
            });
        }
        if (linker != null && linker.getDynamicLibrarySearchFlag() != null && linker.getDynamicLibrarySearchFlag().length() > 0) {
            set1.put(new VectorNodeProp(lc.getDynamicSearch(), null, lc.getMakeConfiguration().getBaseFSPath(), new String[]{"RuntimeSearchDirectories", getString("RuntimeSearchDirectoriesTxt"), getString("RuntimeSearchDirectoriesHint")},
                    false, JFileChooser.DIRECTORIES_ONLY, new HelpCtx("RuntimeSearchDirectories")){ // NOI18N
                @Override
                protected List<String> convertToList(String text) {
                    return TokenizerFactory.DEFAULT_CONVERTER.convertToList(text);
                }
                @Override
                protected String convertToString(List<String> list) {
                    return TokenizerFactory.DEFAULT_CONVERTER.convertToString(list);
                }
            });
        }
        sheet.put(set1);
        if (!isQtMode) {
            Sheet.Set set2 = new Sheet.Set();
            if (!inheritablePropertiesOnly) {
                set2.setName("Options"); // NOI18N
                set2.setDisplayName(getString("OptionsTxt"));
                set2.setShortDescription(getString("OptionsHint"));
                set2.put(new BooleanNodeProp(lc.getStripOption(), true, "StripSymbols", getString("StripSymbolsTxt"), getString("StripSymbolsHint"))); // NOI18N
                if (conf.isDynamicLibraryConfiguration()) {
                    set2.put(new BooleanNodeProp(lc.getPICOption(), true, "PositionIndependantCode", getString("PositionIndependantCodeTxt"), getString("PositionIndependantCodeHint"))); // NOI18N
                    if (compilerSet != null && compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                        set2.put(new BooleanNodeProp(lc.getNorunpathOption(), true, "NoRunPath", getString("NoRunPathTxt"), getString("NoRunPathHint"))); // NOI18N
                        set2.put(new BooleanNodeProp(lc.getNameassignOption(), true, "AssignName", getString("AssignNameTxt"), getString("AssignNameHint"))); // NOI18N
                    }
                }
                sheet.put(set2);
            }
            Sheet.Set set3 = new Sheet.Set();
            texts = new String[]{getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"), getString("AdditionalDependenciesTxt2"), getString("InheritedValuesTxt")};
            set3.setName("Input"); // NOI18N
            set3.setDisplayName(getString("InputTxt"));
            set3.setShortDescription(getString("InputHint"));
            set3.put(new OptionsNodeProp(lc.getAdditionalDependencies(), null, new AdditionalDependenciesOptions(lc), null, ",", texts)); // NOI18N
            sheet.put(set3);
            Sheet.Set set4 = new Sheet.Set();
            if (!inheritablePropertiesOnly) {
                set4.setName("Tool"); // NOI18N
                set4.setDisplayName(getString("ToolTxt1"));
                set4.setShortDescription(getString("ToolHint1"));
                if (linkDriver != null) {
                    set4.put(new StringNodeProp(lc.getTool(), linkDriver, "Tool", getString("ToolTxt1"), getString("ToolHint1"))); // NOI18N
                }
                sheet.put(set4);
            }
        }

        texts = new String[]{getString("LibrariesTxt1"), getString("LibrariesHint"), getString("LibrariesTxt2"), getString("AllOptionsTxt2")};
        Sheet.Set set5 = new Sheet.Set();
        set5.setName("Libraries"); // NOI18N
        set5.setDisplayName(getString("LibrariesTxt1"));
        set5.setShortDescription(getString("LibrariesHint"));
        set5.put(new LibrariesNodeProp(lc.getLibrariesConfiguration(), project, conf, lc.getMakeConfiguration().getBaseFSPath(), texts));
        set5.put(new IntNodeProp(lc.getLibrariesRunTimeSearchPathKind(), true, "RunTimeSerchPath", getString("RunTimeSerchPathTxt"), getString("RunTimeSerchPathHint"))); // NOI18N
        set5.put(new BooleanNodeProp(lc.getCopyLibrariesConfiguration(), true, "CopyLibraries", getString("CopyLibrariesTxt"), getString("CopyLibrariesHint")));
        sheet.put(set5);

        if (!isQtMode) {
            texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")}; // NOI18N
            Sheet.Set set6 = new Sheet.Set();
            set6.setName("CommandLine"); // NOI18N
            set6.setDisplayName(getString("CommandLineTxt"));
            set6.setShortDescription(getString("CommandLineHint"));
            set6.put(new OptionsNodeProp(lc.getCommandLineConfiguration(), null, lc, null, null, texts));
            sheet.put(set6);
        }

        return sheet;
    }

    private static final class AdditionalDependenciesOptions implements AllOptionsProvider {
        private final LinkerConfiguration lc;
        public AdditionalDependenciesOptions(LinkerConfiguration lc) {
            this.lc = lc;
        }

        @Override
        public String getAllOptions(Tool tool) {
            String options = ""; // NOI18N
            options += lc.getAdditionalDependencies().getPreDefined();
            return MakeProjectOptionsFormat.reformatWhitespaces(options);
        }
    }

    private static class OutputNodeProp extends StringNodeProp {

        public OutputNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }

        @Override
        public void setValue(String v) {
            if (CndPathUtilities.hasMakeSpecialCharacters(v)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("SPECIAL_CHARATERS_ERROR"), NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            super.setValue(v);
        }
    }
}
