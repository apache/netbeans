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

import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 */
public class BasicCompilerCustomizerNode {
    // Sheets
    protected static Sheet.Set getBasicSet(BasicCompilerConfiguration conf) {
        Sheet.Set set = new Sheet.Set();
        set.setName("BasicOptions"); // NOI18N
        set.setDisplayName(getString("BasicOptionsTxt"));
        set.setShortDescription(getString("BasicOptionsHint"));
        set.put(new IntNodeProp(conf.getDevelopmentMode(), true, "DevelopmentMode", getString("DevelopmentModeTxt"), getString("DevelopmentModeHint"))); // NOI18N
        set.put(new IntNodeProp(conf.getWarningLevel(), true, "WarningLevel", getString("WarningLevelTxt"), getString("WarningLevelHint"))); // NOI18N
        set.put(new IntNodeProp(conf.getSixtyfourBits(), true, "64BitArchitecture", getString("64BitArchitectureTxt"), getString("64BitArchitectureHint"))); // NOI18N
        set.put(new BooleanNodeProp(conf.getStrip(), true, "StripSymbols", getString("StripSymbolsTxt"), getString("StripSymbolsHint"))); // NOI18N
        return set;
    }

    protected static Sheet.Set getInputSet(BasicCompilerConfiguration conf) {
        Sheet.Set set = new Sheet.Set();
        set.setName("Input"); // NOI18N
        set.setDisplayName(getString("InputTxt"));
        set.setShortDescription(getString("InputHint"));
        set.put(new StringNodeProp(conf.getAdditionalDependencies(), "AdditionalDependencies", getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint")));  // NOI18N
        return set;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(BasicCompilerCustomizerNode.class, s);
    }
}
