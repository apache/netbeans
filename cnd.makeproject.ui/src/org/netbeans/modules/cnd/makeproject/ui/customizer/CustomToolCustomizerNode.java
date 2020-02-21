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

import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 */
public class CustomToolCustomizerNode {
    
    public static Sheet getSheet(CustomToolConfiguration cc) {
        Sheet sheet = new Sheet();

        Sheet.Set set = new Sheet.Set();
        set.setName("CustomBuild"); // NOI18N
        set.setDisplayName(getString("CustomBuildTxt"));
        set.setShortDescription(getString("CustomBuildHint"));
        set.put(new StringNodeProp(cc.getCommandLine(), "Command Line", getString("CommandLineTxt2"), getString("CommandLineHint2"))); // NOI18N
        set.put(new StringNodeProp(cc.getDescription(), "Description", getString("DescriptionTxt"), getString("DescriptionHint"))); // NOI18N
        set.put(new StringNodeProp(cc.getOutputs(), "Outputs", getString("OutputsTxt"), getString("OutputsNint"))); // NOI18N
        set.put(new StringNodeProp(cc.getAdditionalDependencies(), "AdditionalDependencies", getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"))); // NOI18N
        sheet.put(set);

        return sheet;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(CustomToolCustomizerNode.class, key);
    }

}
