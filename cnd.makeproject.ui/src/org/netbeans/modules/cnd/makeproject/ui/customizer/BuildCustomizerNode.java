/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
