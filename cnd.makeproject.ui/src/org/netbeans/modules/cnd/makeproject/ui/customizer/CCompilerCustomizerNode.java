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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import static org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration.STANDARDS_SUPPORT;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import static org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration.STANDARD_INHERITED;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.CompileOptionsProvider;
import org.netbeans.modules.cnd.makeproject.ui.configurations.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringRONodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class CCompilerCustomizerNode extends CustomizerNode {

    public CCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        switch (getContext().getKind()) {
            case Item:
                SharedItemConfiguration[] configurations = getContext().getItems();
                List<Sheet> itemSheets = new ArrayList<>();
                for (SharedItemConfiguration cfg : configurations) {
                    ItemConfiguration itemConfiguration = cfg.getItemConfiguration(configuration);
                    if (itemConfiguration != null) {
                        Item item = cfg.getItem();
                        itemSheets.add(getGeneralSheet((MakeConfiguration) configuration, null, item, itemConfiguration.getCCompilerConfiguration()));
                    }
                }
                return itemSheets.isEmpty() ? null : itemSheets.toArray(new Sheet[itemSheets.size()]);
            case Folder:
                Folder[] folders = getContext().getFolders();
                List<Sheet> folderSheets = new ArrayList<>();
                for (Folder folder : folders) {
                    Sheet sheet = getGeneralSheet((MakeConfiguration) configuration, folder, null, folder.getFolderConfiguration(configuration).getCCompilerConfiguration());
                    folderSheets.add(sheet);
                }
                return folderSheets.toArray(new Sheet[folderSheets.size()]);
            case Project:
                return new Sheet[]{getGeneralSheet((MakeConfiguration) configuration, null, null, ((MakeConfiguration) configuration).getCCompilerConfiguration())};
        }
        return null;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getContext().isCompilerConfiguration() ? "ProjectPropsCompiling" : "ProjectPropsParser"); // NOI18N
    }
    
    private static String getString(String s) {
        return NbBundle.getMessage(CCompilerCustomizerNode.class, s);
    }

    private Sheet getGeneralSheet(MakeConfiguration conf, Folder folder, Item item, final CCompilerConfiguration cconf) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        AbstractCompiler cCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
        
        IntNodeProp standardProp = new IntNodeProp(cconf.getCStandard(), true, "CStandard", getString("CStandardTxt"), getString("CStandardHint")) {  // NOI18N

                @Override
                public PropertyEditor getPropertyEditor() {
                    if (intEditor == null) {
                        intEditor = new NewIntEditor();
                    }
                    return intEditor;
                }

                class NewIntEditor extends IntNodeProp.IntEditor {

                    @Override
                    public String getAsText() {
                        if (cconf.getCStandard().getValue() == STANDARD_INHERITED) {
                             return NbBundle.getMessage(CCompilerCustomizerNode.class, "STANDARD_INHERITED_WITH_VALUE", cconf.getCStandard().getNames()[cconf.getInheritedCStandard()]); //NOI18N
                        }
                        return super.getAsText();
                    }
                                       
                }
         
        };        
        Sheet.Set set0 = CCCCompilerCustomizerNode.getSet(null, folder, item, cconf);
        sheet.put(set0);
        if (conf.isCompileConfiguration()) {
            if (folder == null) {
                Sheet.Set bset = BasicCompilerCustomizerNode.getBasicSet(cconf);
                sheet.put(bset);
                if (STANDARDS_SUPPORT) {
                    bset.put(standardProp);
                }
                if (compilerSet != null && compilerSet.getCompilerFlavor().isSunStudioCompiler()) { // FIXUP: should be moved to SunCCompiler
                    Sheet.Set set2 = new Sheet.Set();
                    set2.setName("OtherOptions"); // NOI18N
                    set2.setDisplayName(getString("OtherOptionsTxt"));
                    set2.setShortDescription(getString("OtherOptionsHint"));
                    set2.put(new IntNodeProp(cconf.getMTLevel(), (cconf.getMaster() == null), "MultithreadingLevel", getString("MultithreadingLevelTxt"), getString("MultithreadingLevelHint"))); // NOI18N
                    set2.put(new IntNodeProp(cconf.getStandardsEvolution(), (cconf.getMaster() == null), "StandardsEvolution", getString("StandardsEvolutionTxt"), getString("StandardsEvolutionHint"))); // NOI18N
                    set2.put(new IntNodeProp(cconf.getLanguageExt(), (cconf.getMaster() == null), "LanguageExtensions", getString("LanguageExtensionsTxt"), getString("LanguageExtensionsHint"))); // NOI18N
                    sheet.put(set2);
                }
                if (cconf.getMaster() != null) {
                    sheet.put(BasicCompilerCustomizerNode.getInputSet(cconf));
                }
                Sheet.Set set4 = new Sheet.Set();
                set4.setName("Tool"); // NOI18N
                set4.setDisplayName(getString("ToolTxt1"));
                set4.setShortDescription(getString("ToolHint1"));
                if (cCompiler != null) {
                    set4.put(new StringNodeProp(cconf.getTool(), cCompiler.getName(), false, "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
                }
                sheet.put(set4);
            }
            
            String[] texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
            Sheet.Set set2 = new Sheet.Set();
            set2.setName("CommandLine"); // NOI18N
            set2.setDisplayName(getString("CommandLineTxt"));
            set2.setShortDescription(getString("CommandLineHint"));
            if (cCompiler != null) {
                set2.put(new OptionsNodeProp(cconf.getCommandLineConfiguration(), null, cconf, cCompiler, null, texts));
            }
            sheet.put(set2);
        } else if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE && item != null && cCompiler != null) {
            AllOptionsProvider options = CompileOptionsProvider.getDefault().getOptions(item);
            if (options != null) {
                String compileLine = options.getAllOptions(cCompiler);
                if (compileLine != null) {
                    int hasPath = compileLine.indexOf('#');
                    if (hasPath >= 0) {
                        set0.put(new StringRONodeProp(getString("CommandLineTxt"), getString("CommandLineHint"), compileLine.substring(hasPath+1)));
                        set0.put(new StringRONodeProp(getString("CompileFolderTxt"), getString("CompileFolderHint"), compileLine.substring(0, hasPath)));
                    } else {
                        set0.put(new StringRONodeProp(getString("CommandLineTxt"), getString("CommandLineHint"), compileLine));
                    }
                }
            }
        } 
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE && STANDARDS_SUPPORT) {
            set0.put(standardProp);
        }
        
        return sheet;
    }
}
