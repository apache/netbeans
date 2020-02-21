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

import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.ArchiverConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.ui.configurations.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class ArchiverGeneralCustomizerNode extends CustomizerNode {

    public ArchiverGeneralCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        Sheet generalSheet = getGeneralSheet(((MakeConfiguration) configuration).getArchiverConfiguration());
        return new Sheet[]{generalSheet};
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsArchiverGeneral"); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getMessage(ArchiverGeneralCustomizerNode.class, s);
    }
    
    private Sheet getGeneralSheet(ArchiverConfiguration conf) {
        Sheet sheet = new Sheet();
        Sheet.Set set1 = new Sheet.Set();
        set1.setName("General"); // NOI18N
        set1.setDisplayName(getString("GeneralTxt"));
        set1.setShortDescription(getString("GeneralHint"));
        set1.put(new OutputNodeProp(conf.getOutput(), conf.getOutputValue(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
        set1.put(new BooleanNodeProp(conf.getRunRanlib(), true, "RunRanlib", getString("RunRanlibTxt"), getString("RunRanlibHint"))); // NOI18N
        sheet.put(set1);
        Sheet.Set set2 = new Sheet.Set();
        set2.setName("Options"); // NOI18N
        set2.setDisplayName(getString("OptionsTxt"));
        set2.setShortDescription(getString("OptionsHint"));
        //set2.put(new BooleanNodeProp(getReplaceOption(), "Add", "Add", "Add (-r)"));
        set2.put(new BooleanNodeProp(conf.getVerboseOption(), true, "Verbose", getString("VerboseTxt"), getString("VerboseHint"))); // NOI18N
        set2.put(new BooleanNodeProp(conf.getSupressOption(), true, "SupressDiagnostics", getString("SupressDiagnosticsTxt"), getString("SupressDiagnosticsHint"))); // NOI18N
        sheet.put(set2);
        Sheet.Set set3 = new Sheet.Set();
        String[] texts = new String[]{getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"), getString("AdditionalDependenciesTxt2"), getString("InheritedValuesTxt")}; // NOI18N
        set3.setName("Input"); // NOI18N
        set3.setDisplayName(getString("InputTxt"));
        set3.setShortDescription(getString("InputHint"));
        set3.put(new OptionsNodeProp(conf.getAdditionalDependencies(), null, new AdditionalDependenciesOptions(conf), null, ",", texts)); // NOI18N
        sheet.put(set3);
        Sheet.Set set4 = new Sheet.Set();
        set4.setName("Tool"); // NOI18N
        set4.setDisplayName(getString("ToolTxt1"));
        set4.setShortDescription(getString("ToolHint1"));
        set4.put(new StringNodeProp(conf.getTool(), "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
        set4.put(new StringNodeProp(conf.getRanlibTool(), "ranlibTool", getString("RanlibToolTxt"), getString("RanlibToolHint"))); // NOI18N
        sheet.put(set4);

        texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
        set2 = new Sheet.Set();
        set2.setName("CommandLine"); // NOI18N
        set2.setDisplayName(getString("CommandLineTxt"));
        set2.setShortDescription(getString("CommandLineHint"));
        set2.put(new OptionsNodeProp(conf.getCommandLineConfiguration(), null, conf, null, null, texts));
        sheet.put(set2);

        return sheet;
    }

    private static class AdditionalDependenciesOptions implements AllOptionsProvider {
        private final ArchiverConfiguration conf;
        
        AdditionalDependenciesOptions(ArchiverConfiguration conf) {
            this.conf = conf;
        }

        public String getOptions() {
            return null; // Not used
        }

        @Override
        public String getAllOptions(Tool tool) {
            String options = ""; // NOI18N
            options += conf.getAdditionalDependencies().getPreDefined();
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
