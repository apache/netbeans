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

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.PrependToolCollectionPathNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.CompilerSetNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.DevelopmentHostNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.PlatformSpecificProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.RemoteSyncFactoryNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class BuildCustomizerNode extends CustomizerNode {

    public BuildCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        // Doesn't depend on items. Depends on a project.
        Sheet buildSheet = getBuildSheet(getContext().getProject(), (MakeConfiguration) configuration);
        return new Sheet[]{buildSheet};
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsBuild"); // NOI18N
    }
 
    private static String getString(String s) {
        return NbBundle.getMessage(BuildCustomizerNode.class, s);
    }
    
    private static Sheet getBuildSheet(Project project, MakeConfiguration conf) {
        Sheet sheet = new Sheet();

        Sheet.Set set = new Sheet.Set();
        set.setName("ProjectDefaults"); // NOI18N
        set.setDisplayName(getString("ProjectDefaultsTxt"));
        set.setShortDescription(getString("ProjectDefaultsHint"));
        boolean canEditHost = MakeProjectUtils.canChangeHost(project, conf);
        set.put(new DevelopmentHostNodeProp(conf.getDevelopmentHost(), canEditHost, "DevelopmentHost", getString("DevelopmentHostTxt"), getString("DevelopmentHostHint"))); // NOI18N
        RemoteSyncFactoryNodeProp rsfNodeProp = new RemoteSyncFactoryNodeProp(conf);
        set.put(rsfNodeProp);
//        set.put(new BuildPlatformNodeProp(getDevelopmentHost().getBuildPlatformConfiguration(), developmentHost, makeCustomizer, getDevelopmentHost().isLocalhost(), "builtPlatform", getString("PlatformTxt"), getString("PlatformHint"))); // NOI18N
        set.put(new CompilerSetNodeProp(conf.getCompilerSet(), conf.getDevelopmentHost(), true, "CompilerSetCollection", getString("CompilerCollectionTxt"), getString("CompilerCollectionHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getCRequired(), true, "cRequired", getString("CRequiredTxt"), getString("CRequiredHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getCppRequired(), true, "cppRequired", getString("CppRequiredTxt"), getString("CppRequiredHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getFortranRequired(), true, "fortranRequired", getString("FortranRequiredTxt"), getString("FortranRequiredHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getAssemblerRequired(), true, "assemblerRequired", getString("AssemblerRequiredTxt"), getString("AssemblerRequiredHint"))); // NOI18N
        set.put(new IntNodeProp(conf.getConfigurationType(), true, "ConfigurationType", getString("ConfigurationTypeTxt"), getString("ConfigurationTypeHint"))); // NOI18N
        sheet.put(set);

        set = Sheet.createExpertSet();
        if (conf.isCompileConfiguration()) {
            set.put(new BooleanNodeProp(conf.getDependencyChecking(), true, "DependencyChecking", getString("DependencyCheckingTxt"), getString("DependencyCheckingHint"))); // NOI18N
            set.put(new BooleanNodeProp(conf.getRebuildPropChanged(), true, "RebuildPropChanged", getString("RebuildPropChangedTxt"), getString("RebuildPropChangedHint"))); // NOI18N
        }
        set.put(new PlatformSpecificProp(conf, conf.getPlatformSpecific(), true, "PlatformSpecific", getString("PlatformSpecificTxt"), getString("PlatformSpecificHint"))); // NOI18N
        set.put(new PrependToolCollectionPathNodeProp(conf.getPrependToolCollectionPath(), conf.getCompilerSet(), conf.getDevelopmentHost(), true, "PrependToolCollectionPath", getString("PrependToolCollectionPathTxt"), getString("PrependToolCollectionPathHint"))); //NOI18N
        sheet.put(set);

        return sheet;
    }
}
