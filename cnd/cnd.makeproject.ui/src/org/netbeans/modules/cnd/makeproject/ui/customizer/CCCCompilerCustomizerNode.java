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
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringListNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.VectorNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.utils.TokenizerFactory;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class CCCCompilerCustomizerNode {
    // Sheet
    protected static Sheet.Set getSet(final Project project, final Folder folder, final Item item, CCCCompilerConfiguration cconf) {
        CCCCompilerConfiguration.OptionToString visitor = new CCCCompilerConfiguration.OptionToString(null, null);

        Sheet.Set set1 = new Sheet.Set();
        set1.setName("General"); // NOI18N
        set1.setDisplayName(getString("GeneralTxt"));
        set1.setShortDescription(getString("GeneralHint"));
        StringBuilder inheritedValues;
        {
            // Include Dirctories
            inheritedValues = new StringBuilder();
            List<CCCCompilerConfiguration> list = new ArrayList<>();
            for(BasicCompilerConfiguration master : cconf.getMasters(false)) {
                list.add((CCCCompilerConfiguration)master);
                if (!((CCCCompilerConfiguration)master).getInheritIncludes().getValue()) {
                    break;
                }
            }
            for(int i = list.size() - 1; i >= 0; i--) {
                inheritedValues.append(list.get(i).getIncludeDirectories().toString(visitor, "\n")); //NOI18N
            }
            set1.put(new VectorNodeProp(cconf.getIncludeDirectories(), cconf.getMaster() != null ? cconf.getInheritIncludes() : null, cconf.getOwner().getBaseFSPath(), 
                    new String[]{"IncludeDirectories", getString("IncludeDirectoriesTxt"), getString("IncludeDirectoriesHint"), inheritedValues.toString()},
                    true, JFileChooser.DIRECTORIES_ONLY, new HelpCtx("AddtlIncludeDirectories")){// NOI18N
                private final TokenizerFactory.Converter converter = TokenizerFactory.getPathConverter(project, folder, item, "-I"); //NOI18N
                @Override
                protected List<String> convertToList(String text) {
                    return converter.convertToList(text);
                }
                @Override
                protected String convertToString(List<String> list) {
                    return converter.convertToString(list);
                }
            });
        } 
        {
            // Include Dirctories
            inheritedValues = new StringBuilder();
            List<CCCCompilerConfiguration> list = new ArrayList<>();
            for(BasicCompilerConfiguration master : cconf.getMasters(false)) {
                list.add((CCCCompilerConfiguration)master);
                if (!((CCCCompilerConfiguration)master).getInheritFiles().getValue()) {
                    break;
                }
            }
            for(int i = list.size() - 1; i >= 0; i--) {
                inheritedValues.append(list.get(i).getIncludeFiles().toString(visitor, "\n")); //NOI18N
            }
            set1.put(new VectorNodeProp(cconf.getIncludeFiles(), cconf.getMaster() != null ? cconf.getInheritFiles() : null, cconf.getOwner().getBaseFSPath(), 
                    new String[]{"IncludeFiles", getString("IncludeFilesTxt"), getString("IncludeFilesHint"), inheritedValues.toString()},
                    true, JFileChooser.FILES_ONLY, new HelpCtx("AddtlIncludeFiles")){// NOI18N
                private final TokenizerFactory.Converter converter = TokenizerFactory.getPathConverter(project, folder, item, "-include"); //NOI18N
                @Override
                protected List<String> convertToList(String text) {
                    return converter.convertToList(text);
                }
                @Override
                protected String convertToString(List<String> list) {
                    return converter.convertToString(list);
                }
            });
        } 
        {
            // Preprocessor Macros
            inheritedValues = new StringBuilder();
            for(BasicCompilerConfiguration master : cconf.getMasters(false)) {
                inheritedValues.append(((CCCCompilerConfiguration)master).getPreprocessorConfiguration().toString(visitor, "\n")); //NOI18N
                if (!((CCCCompilerConfiguration)master).getInheritPreprocessor().getValue()) {
                    break;
                }
            }
            set1.put(new StringListNodeProp(cconf.getPreprocessorConfiguration(), cconf.getMaster() != null ? cconf.getInheritPreprocessor() : null, new String[]{"preprocessor-definitions", getString("PreprocessorDefinitionsTxt"), getString("PreprocessorDefinitionsHint"), getString("PreprocessorDefinitionsLbl"), inheritedValues.toString()}, true, new HelpCtx("preprocessor-definitions")){  // NOI18N
                @Override
                protected List<String> convertToList(String text) {
                    return TokenizerFactory.MACRO_CONVERTER.convertToList(text);
                }

                @Override
                protected String convertToString(List<String> list) {
                    return TokenizerFactory.MACRO_CONVERTER.convertToString(list);
                }
            });
        }
        if (cconf.getOwner().getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
            // Undefined Macros
            inheritedValues = new StringBuilder();
            for(BasicCompilerConfiguration master : cconf.getMasters(false)) {
                inheritedValues.append(((CCCCompilerConfiguration)master).getUndefinedPreprocessorConfiguration().toString(visitor));
                if (!((CCCCompilerConfiguration)master).getInheritUndefinedPreprocessor().getValue()) {
                    break;
                }
            }
            set1.put(new StringListNodeProp(cconf.getUndefinedPreprocessorConfiguration(),
                    cconf.getMaster() != null ? cconf.getInheritUndefinedPreprocessor() : null,
                    new String[]{"preprocessor-undefined", getString("PreprocessorUndefinedTxt"), getString("PreprocessorUndefinedHint"), getString("PreprocessorUndefinedLbl"), inheritedValues.toString()}, // NOI18N
                    true, new HelpCtx("preprocessor-undefined")) { // NOI18N

                        @Override
                        protected List<String> convertToList(String text) {
                            return TokenizerFactory.UNDEF_CONVERTER.convertToList(text);
                        }

                        @Override
                        protected String convertToString(List<String> list) {
                            return TokenizerFactory.UNDEF_CONVERTER.convertToString(list);
                        }
                    });
        }
        if (cconf.getMaster() == null) {            
            final IntConfiguration configurationType = cconf.getOwner() == null ? null : cconf.getOwner().getConfigurationType();
            if (configurationType == null || (configurationType.getValue() != MakeConfiguration.TYPE_MAKEFILE)) {
                set1.put(new BooleanNodeProp(cconf.getUseLinkerLibraries(), true,
                        "use-linker-libraries", getString("UseLinkerLibrariesTxt"), getString("UseLinkerLibrariesHint"))); // NOI18N
            }
        }
        return set1;
    }

    // Sheet
    protected static Sheet getSheet(Project project, CCCCompilerConfiguration cconf) {
        Sheet sheet = new Sheet();
        sheet.put(getSet(project, null, null, cconf));
        return sheet;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCCCompilerCustomizerNode.class, s);
    }
}
