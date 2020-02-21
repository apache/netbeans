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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class FortranCompilerCustomizerNode extends CustomizerNode {

    public FortranCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        if (((MakeConfiguration)configuration).isCompileConfiguration()) {
            switch (getContext().getKind()) {
                case Item:
                    SharedItemConfiguration[] sharedConfigurations = getContext().getItems();
                    List<Sheet> out = new ArrayList<>();
                    for (SharedItemConfiguration cfg : sharedConfigurations) {
                        ItemConfiguration itemConfiguration = cfg.getItemConfiguration(configuration);
                        if (itemConfiguration != null) {
                            out.add(getGeneralSheet((MakeConfiguration) configuration, itemConfiguration.getFortranCompilerConfiguration()));
                        }
                    }
                    return out.isEmpty() ? null : out.toArray(new Sheet[out.size()]);
                case Project:
                    Sheet generalSheet = getGeneralSheet((MakeConfiguration) configuration, ((MakeConfiguration) configuration).getFortranCompilerConfiguration());
                    return new Sheet[]{generalSheet};
            }
        }
        return null;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsCompiling"); // NOI18N
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(FortranCompilerCustomizerNode.class, s);
    }

    private Sheet getGeneralSheet(MakeConfiguration conf, FortranCompilerConfiguration cconf) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        AbstractCompiler fortranCompiler = compilerSet == null ? null : (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.FortranCompiler);

        sheet.put(BasicCompilerCustomizerNode.getBasicSet(cconf));
        if (cconf.getMaster() != null) {
            sheet.put(BasicCompilerCustomizerNode.getInputSet(cconf));
        }
        if (compilerSet !=null && compilerSet.getCompilerFlavor().isSunStudioCompiler()) { // FIXUP: should be moved to SunCCompiler
            Sheet.Set set2 = new Sheet.Set();
            set2.setName("OtherOptions"); // NOI18N
            set2.setDisplayName(getString("OtherOptionsTxt"));
            set2.setShortDescription(getString("OtherOptionsHint"));
            set2.put(new IntNodeProp(cconf.getMTLevel(), (cconf.getMaster() == null), "MultithreadingLevel", getString("MultithreadingLevelTxt"), getString("MultithreadingLevelHint"))); // NOI18N
            sheet.put(set2);
        }
        Sheet.Set set4 = new Sheet.Set();
        set4.setName("Tool"); // NOI18N
        set4.setDisplayName(getString("ToolTxt1"));
        set4.setShortDescription(getString("ToolHint1"));
        if (fortranCompiler != null) {
            set4.put(new StringNodeProp(cconf.getTool(), fortranCompiler.getName(), false, "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
        }
        sheet.put(set4);

        String[] texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
        Sheet.Set set2 = new Sheet.Set();
        set2.setName("CommandLine"); // NOI18N
        set2.setDisplayName(getString("CommandLineTxt"));
        set2.setShortDescription(getString("CommandLineHint"));
        if (fortranCompiler != null) {
            set2.put(new OptionsNodeProp(cconf.getCommandLineConfiguration(), null, cconf, fortranCompiler, null, texts));
        }
        sheet.put(set2);

        return sheet;
    }
}
