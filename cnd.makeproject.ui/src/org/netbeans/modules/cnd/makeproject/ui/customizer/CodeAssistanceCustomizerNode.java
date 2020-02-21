/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
