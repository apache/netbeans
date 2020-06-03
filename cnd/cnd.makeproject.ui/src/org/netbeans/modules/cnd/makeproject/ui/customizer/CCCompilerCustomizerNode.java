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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import static org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration.STANDARDS_SUPPORT;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import static org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration.STANDARD_INHERITED;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.CompileOptionsProvider;
import org.netbeans.modules.cnd.makeproject.ui.configurations.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringRONodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class CCCompilerCustomizerNode extends CustomizerNode {

    public CCCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        switch (getContext().getKind()) {
            case Item:
                SharedItemConfiguration[] configurations = getContext().getItems();
                List<Sheet> itemSheets = new ArrayList<>();
                for (SharedItemConfiguration cfg : configurations) {
                    ItemConfiguration itemConfiguration = cfg.getItemConfiguration(configuration);
                    if (itemConfiguration != null) {
                        Item item = cfg.getItem();
                        itemSheets.add(getSheet((MakeConfiguration) configuration, null, item, itemConfiguration.getCCCompilerConfiguration()));
                    }
                }
                return itemSheets.toArray(new Sheet[itemSheets.size()]);
            case Folder:
                Folder[] folders = getContext().getFolders();
                List<Sheet> folderSheets = new ArrayList<>();
                for (Folder folder: folders) {
                    Sheet sheet = getSheet((MakeConfiguration) configuration, folder, null, folder.getFolderConfiguration(configuration).getCCCompilerConfiguration());
                    folderSheets.add(sheet);
                }
                return folderSheets.toArray(new Sheet[folderSheets.size()]);
            case Project:
                return new Sheet[]{getSheet((MakeConfiguration) configuration, null, null, ((MakeConfiguration) configuration).getCCCompilerConfiguration())};
        }
        return null;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getContext().isCompilerConfiguration() ? "ProjectPropsCompiling" : "ProjectPropsParser"); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getMessage(CCCompilerCustomizerNode.class, s);
    }

    private Sheet getSheet(MakeConfiguration conf, Folder folder, Item item, final CCCompilerConfiguration cconf) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        AbstractCompiler ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);

        IntNodeProp standardProp = new IntNodeProp(cconf.getCppStandard(), true, "CPPStandard", getString("CPPStandardTxt"), getString("CPPStandardHint")) {  // NOI18N

                @Override
                public PropertyEditor getPropertyEditor() {
                    if (intEditor == null) {
                        intEditor = new NewIntEditor();
                    }
                    return intEditor;
                }

                class NewIntEditor extends IntNodeProp.IntEditor {

                    @Override
                    public String getAsText() {
                        if (cconf.getCppStandard().getValue() == STANDARD_INHERITED) {
                             return NbBundle.getMessage(CCCompilerCustomizerNode.class, "STANDARD_INHERITED_WITH_VALUE", cconf.getCppStandard().getNames()[cconf.getInheritedCppStandard()]); //NOI18N
                        }
                        return super.getAsText();
                    }
                                       
                }
         
        };
        Sheet.Set set0 = CCCCompilerCustomizerNode.getSet(null, folder, item, cconf);
        sheet.put(set0);
        if (conf.isCompileConfiguration()) {
            if (folder == null) {
                Sheet.Set bset = BasicCompilerCustomizerNode.getBasicSet(cconf);
                sheet.put(bset);
                if (compilerSet != null && compilerSet.getCompilerFlavor().isSunStudioCompiler()) { // FIXUP: should be moved to SunCCompiler                
                    Sheet.Set set2 = new Sheet.Set();
                    set2.setName("OtherOptions"); // NOI18N
                    set2.setDisplayName(getString("OtherOptionsTxt"));
                    set2.setShortDescription(getString("OtherOptionsHint"));
                    set2.put(new IntNodeProp(cconf.getMTLevel(), (cconf.getMaster() == null), "MultithreadingLevel", getString("MultithreadingLevelTxt"), getString("MultithreadingLevelHint"))); // NOI18N
                    set2.put(new IntNodeProp(cconf.getLibraryLevel(), (cconf.getMaster() == null), "LibraryLevel", getString("LibraryLevelTxt"), getString("LibraryLevelHint"))); // NOI18N
                    set2.put(new IntNodeProp(cconf.getStandardsEvolution(), (cconf.getMaster() == null), "StandardsEvolution", getString("StandardsEvolutionTxt"), getString("StandardsEvolutionHint"))); // NOI18N
                    set2.put(new IntNodeProp(cconf.getLanguageExt(), (cconf.getMaster() == null), "LanguageExtensions", getString("LanguageExtensionsTxt"), getString("LanguageExtensionsHint"))); // NOI18N
                    sheet.put(set2);
                } //else {
                    if (STANDARDS_SUPPORT) {
                        bset.put(standardProp);
                    }
                //}
                if (cconf.getMaster() != null) {
                    sheet.put(BasicCompilerCustomizerNode.getInputSet(cconf));
                }

                Sheet.Set set4 = new Sheet.Set();
                set4.setName("Tool"); // NOI18N
                set4.setDisplayName(getString("ToolTxt1"));
                set4.setShortDescription(getString("ToolHint1"));
                if (ccCompiler != null) {
                    set4.put(new StringNodeProp(cconf.getTool(), ccCompiler.getName(), false, "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
                }
                sheet.put(set4);
            }
            
            String[] texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
            Sheet.Set set2 = new Sheet.Set();
            set2.setName("CommandLine"); // NOI18N
            set2.setDisplayName(getString("CommandLineTxt"));
            set2.setShortDescription(getString("CommandLineHint"));
            if (ccCompiler != null) {
                set2.put(new OptionsNodeProp(cconf.getCommandLineConfiguration(), null, cconf, ccCompiler, null, texts));
            }
            sheet.put(set2);
        } else if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE && item != null && ccCompiler != null) {
            AllOptionsProvider options = CompileOptionsProvider.getDefault().getOptions(item);
            if (options != null) {
                String compileLine = options.getAllOptions(ccCompiler);
                if (compileLine != null) {
                    int hasPath = compileLine.indexOf('#');
                    if (hasPath >= 0) {
                        set0.put(new StringRONodeProp(getString("CommandLineTxt"), getString("CommandLineHint"), compileLine.substring(hasPath+1)));
                        set0.put(new StringRONodeProp(getString("CompileFolderTxt"), getString("CompileFolderHint"), compileLine.substring(0, hasPath)));
                    } else {
                        set0.put(new StringRONodeProp(getString("CommandLineTxt"), getString("CommandLineHint"), compileLine));
                    }
                }
            }
        }  
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
            //if (compilerSet == null || !compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                if (STANDARDS_SUPPORT) {
                    set0.put(standardProp);
                }
            //}
        }
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_APPLICATION || 
            conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_DYNAMIC_LIB || 
           conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_STATIC_LIB) {
            //if (compilerSet == null || !compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                if (STANDARDS_SUPPORT) {
                    set0.put(standardProp);
                }
            //}
        }
        
        return sheet;
    }
}
