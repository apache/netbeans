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
package org.netbeans.modules.cnd.makeproject.ui.utils;

import java.util.ArrayList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ui.BuildToolsAction;
import org.netbeans.modules.cnd.api.toolchain.ui.LocalToolsPanelModel;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelModel;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.BrokenReferencesSupport;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ResolveBuildToolsFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=ResolveBuildToolsFactory.class)
public class ResolveBuildToolsFactoryImpl implements ResolveBuildToolsFactory {

    @Override
    public boolean resolveTools(String title, MakeConfigurationDescriptor pd, MakeConfiguration conf, ExecutionEnvironment env, String csname, CompilerSet cs, boolean cRequired, boolean cppRequired, boolean fRequired, boolean asRequired, ArrayList<String> errs) {
        //if (conf.getDevelopmentHost().isLocalhost()) {
        BuildToolsAction bt = SystemAction.get(BuildToolsAction.class);
        bt.setTitle(NbBundle.getMessage(BuildToolsAction.class, "LBL_ResolveMissingTools_Title")); // NOI18N
        ToolsPanelModel model = new LocalToolsPanelModel();
        model.setSelectedDevelopmentHost(env); // only localhost until BTA becomes more functional for remote sets
        model.setEnableDevelopmentHostChange(false);
        model.setCompilerSetName(null); // means don't change
        model.setSelectedCompilerSetName(csname);
        model.setMakeRequired(true);
        model.setDebuggerRequired(false);
        model.setCRequired(cRequired);
        model.setCppRequired(cppRequired);
        model.setFortranRequired(fRequired);
        model.setQMakeRequired(conf.isQmakeConfiguration());
        model.setAsRequired(asRequired);
        model.setShowRequiredBuildTools(true);
        model.setShowRequiredDebugTools(false);
        model.setEnableRequiredCompilerCB(conf.isMakefileConfiguration());
        if (bt.initBuildTools(model, errs, cs) && pd.okToChange()) {
            String name = model.getSelectedCompilerSetName();
            ToolsPanelModel.resetCompilerSetName(name);
            conf.getCRequired().setValue(model.isCRequired());
            conf.getCppRequired().setValue(model.isCppRequired());
            conf.getFortranRequired().setValue(model.isFortranRequired());
            conf.getAssemblerRequired().setValue(model.isAsRequired());
            conf.getCompilerSet().setValue(name);
            cs = conf.getCompilerSet().getCompilerSet();
            CndUtils.assertNotNull(cs, "Null compiler set for " + name); //NOI18N
            pd.setModified();
            pd.save();
            BrokenReferencesSupport.updateProblems(pd.getProject());
            return true;
        } else {
            return false;
        }
    }
    
}
