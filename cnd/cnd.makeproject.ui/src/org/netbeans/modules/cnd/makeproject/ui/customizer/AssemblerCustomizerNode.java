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
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.AssemblerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.ui.configurations.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class AssemblerCustomizerNode extends CustomizerNode {

    public AssemblerCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        if (((MakeConfiguration)configuration).isCompileConfiguration()) {
            SharedItemConfiguration[] itemConfigurations = getContext().getItems();
            List<Sheet> out = new ArrayList<>();
            if (itemConfigurations != null) {
                for (SharedItemConfiguration cfg : itemConfigurations) {
                    if (cfg != null) {
                        ItemConfiguration itemConfiguration = cfg.getItemConfiguration(configuration);
                        if (itemConfiguration != null) {
                            out.add(getGeneralSheet((MakeConfiguration) configuration, itemConfiguration.getAssemblerConfiguration()));
                        }
                    }
                }
            } else {
                out.add(getGeneralSheet((MakeConfiguration) configuration, ((MakeConfiguration) configuration).getAssemblerConfiguration()));
            }
            return out.isEmpty() ? null : out.toArray(new Sheet[out.size()]);
        }
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsCompiling"); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getMessage(AssemblerCustomizerNode.class, s);
    }
    
    private static Sheet getGeneralSheet(MakeConfiguration conf, AssemblerConfiguration ac) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        AbstractCompiler assemblerCompiler = compilerSet == null ? null : (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.Assembler);

        Sheet.Set basicSet = BasicCompilerCustomizerNode.getBasicSet(ac);
        basicSet.remove("StripSymbols"); // NOI18N
        sheet.put(basicSet);
        if (ac.getMaster() != null) {
            sheet.put(BasicCompilerCustomizerNode.getInputSet(ac));
        }
        Sheet.Set set4 = new Sheet.Set();
        set4.setName("Tool"); // NOI18N
        set4.setDisplayName(getString("ToolTxt1"));
        set4.setShortDescription(getString("ToolHint1"));
        if (assemblerCompiler != null) {
            set4.put(new StringNodeProp(ac.getTool(), assemblerCompiler.getName(), false, "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
        }
        sheet.put(set4);

        String[] texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
        Sheet.Set set2 = new Sheet.Set();
        set2.setName("CommandLine"); // NOI18N
        set2.setDisplayName(getString("CommandLineTxt"));
        set2.setShortDescription(getString("CommandLineHint"));
        if (assemblerCompiler != null) {
            set2.put(new OptionsNodeProp(ac.getCommandLineConfiguration(), null, ac, assemblerCompiler, null, texts));
        }
        sheet.put(set2);

        return sheet;
    }
}
