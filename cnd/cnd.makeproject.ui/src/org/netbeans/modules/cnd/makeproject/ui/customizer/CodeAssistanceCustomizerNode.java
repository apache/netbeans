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

import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.makeproject.api.configurations.CodeAssistanceConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringListNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.utils.TokenizerFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class CodeAssistanceCustomizerNode extends CustomizerNode {

    public CodeAssistanceCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        switch (getContext().getKind()) {
            case Project:
                Sheet generalSheet = getGeneralSheet((MakeConfiguration) configuration, ((MakeConfiguration) configuration).getCodeAssistanceConfiguration());
                return new Sheet[]{generalSheet};
        }
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsParser"); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getMessage(CodeAssistanceCustomizerNode.class, s);
    }

    private Sheet getGeneralSheet(MakeConfiguration conf, CodeAssistanceConfiguration ca) {
        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("CodeAssistance"); // NOI18N
        set.setDisplayName(getString("CodeAssistanceTxt"));
        set.setShortDescription(getString("CodeAssistanceHint"));
        set.put(new BooleanNodeProp(ca.getBuildAnalyzer(), true, "BuildAnalyzer", getString("BuildAnalyzerTxt"), getString("BuildAnalyzerHint"))); // NOI18N
        if (System.getProperty("cnd.buildtrace.tools") != null) {
            // hide node by default
            set.put(new StringNodeProp(ca.getTools(), CodeAssistanceConfiguration.DEFAULT_TOOLS, "Tools", getString("ToolsTxt2"), getString("ToolsHint2"))); // NOI18N
        }
        set.put(new StringListNodeProp(ca.getTransientMacros(), null,
                new String[]{"transient-macros", // NOI18N
                             getString("TransientMacrosTxt"), // NOI18N
                             getString("TransientMacrosHint"), // NOI18N
                             getString("TransientMacrosLbl"), // NOI18N
                             null}, true, new HelpCtx("transient-macros")){ // NOI18N
            @Override
            protected List<String> convertToList(String text) {
                return TokenizerFactory.DEFAULT_CONVERTER.convertToList(text);
            }

            @Override
            protected String convertToString(List<String> list) {
                return TokenizerFactory.DEFAULT_CONVERTER.convertToString(list);
            }
        });
        set.put(new StringListNodeProp(ca.getEnvironmentVariables(), null,
                new String[]{"environment-variables", // NOI18N
                             getString("EnvironmentVariablesTxt"), // NOI18N
                             getString("EnvironmentVariablesHint"), // NOI18N
                             getString("EnvironmentVariablesLbl"), // NOI18N
                             null}, true, new HelpCtx("environment-variables")){ // NOI18N
            @Override
            protected List<String> convertToList(String text) {
                return TokenizerFactory.DEFAULT_CONVERTER.convertToList(text);
            }

            @Override
            protected String convertToString(List<String> list) {
                return TokenizerFactory.DEFAULT_CONVERTER.convertToString(list);
            }
        });
        sheet.put(set);
        
        set = new Sheet.Set();
        set.setName("IncludeInCodeAssistance"); // NOI18N
        set.setDisplayName(getString("IncludeInCodeAssistanceTxt")); // NOI18N
        set.setShortDescription(getString("IncludeInCodeAssistanceHint")); // NOI18N
        set.put(new BooleanNodeProp(ca.getIncludeInCA(), true, "IncludeFlag", getString("IncludeFlagTxt"), getString("IncludeFlagHint"))); // NOI18N
        set.put(new PatternNodeProp(ca.getExcludeInCA(), "", "ExcludePattern", getString("ExcludePatternTxt"), getString("ExcludePatternHint"))); // NOI18N
        set.put(new BooleanNodeProp(ca.getResolveSymbolicLinks(), false, "ResolveSymbolicLinks", getString("ResolveSymbolicLinksTxt"), getString("ResolveSymbolicLinksHint"))); // NOI18N
        sheet.put(set);
        
        return sheet;
    }

    private static class PatternNodeProp extends StringNodeProp {

        public PatternNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }

        @Override
        public void setValue(String v) {
            try {
                Pattern.compile(v);
            } catch (Throwable ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("InvalidPattern"), NotifyDescriptor.ERROR_MESSAGE));
            }
            super.setValue(v);
        }
    }

}
