/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
