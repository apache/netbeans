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

import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringListNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.configurations.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.utils.TokenizerFactory;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class QtCustomizerNode extends CustomizerNode {

    public QtCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        Sheet generalSheet = getGeneralSheet(((MakeConfiguration) configuration).getQmakeConfiguration());
        return new Sheet[]{generalSheet};
    }

    private static String getString(String s) {
        return NbBundle.getMessage(QtCustomizerNode.class, s);
    }

    private Sheet getGeneralSheet(QmakeConfiguration conf) {
        Sheet sheet = new Sheet();

        Sheet.Set basic = new Sheet.Set();
        basic.setName("QtGeneral"); // NOI18N
        basic.setDisplayName(getString("QtGeneralTxt")); // NOI18N
        basic.setShortDescription(getString("QtGeneralHint")); // NOI18N
        basic.put(new StringNodeProp(conf.getDestdir(), conf.getDestdirDefault(), "QtDestdir", getString("QtDestdirTxt"), getString("QtDestdirHint"))); // NOI18N
        basic.put(new StringNodeProp(conf.getTarget(), conf.getTargetDefault(), "QtTarget", getString("QtTargetTxt"), getString("QtTargetHint"))); // NOI18N
        basic.put(new StringNodeProp(conf.getVersion(), "QtVersion", getString("QtVersionTxt"), getString("QtVersionHint"))); // NOI18N
        basic.put(new IntNodeProp(conf.getBuildMode(), true, "QtBuildMode", getString("QtBuildModeTxt"), getString("QtBuildModeHint"))); // NOI18N
        sheet.put(basic);

        Sheet.Set modules = new Sheet.Set();
        modules.setName("QtModules"); // NOI18N
        modules.setDisplayName(getString("QtModulesTxt")); // NOI18N
        modules.setShortDescription(getString("QtModulesHint")); // NOI18N
        modules.put(new BooleanNodeProp(conf.isCoreEnabled(), true, "QtCore", getString("QtCoreTxt"), getString("QtCoreHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isGuiEnabled(), true, "QtGui", getString("QtGuiTxt"), getString("QtGuiHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isWidgetsEnabled(), true, "QtWidgets", getString("QtWidgetsTxt"), getString("QtWidgetsHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isNetworkEnabled(), true, "QtNetwork", getString("QtNetworkTxt"), getString("QtNetworkHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isOpenglEnabled(), true, "QtOpengl", getString("QtOpenglTxt"), getString("QtOpenglHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isPhononEnabled(), true, "QtPhonon", getString("QtPhononTxt"), getString("QtPhononHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isQt3SupportEnabled(), true, "Qt3Support", getString("Qt3SupportTxt"), getString("Qt3SupportHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isPrintSupportEnabled(), true, "QtPrintSupport", getString("QtPrintSupportTxt"), getString("QtPrintSupportHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isSqlEnabled(), true, "QtSql", getString("QtSqlTxt"), getString("QtSqlHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isSvgEnabled(), true, "QtSvg", getString("QtSvgTxt"), getString("QtSvgHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isXmlEnabled(), true, "QtXml", getString("QtXmlTxt"), getString("QtXmlHint"))); // NOI18N
        modules.put(new BooleanNodeProp(conf.isWebkitEnabled(), true, "QtWebkit", getString("QtWebkitTxt"), getString("QtWebkitHint"))); // NOI18N
        sheet.put(modules);

        Sheet.Set generatedFiles = new Sheet.Set();
        generatedFiles.setName("QtIntermediateFiles"); // NOI18N
        generatedFiles.setDisplayName(getString("QtIntermediateFilesTxt")); // NOI18N
        generatedFiles.setShortDescription(getString("QtIntermediateFilesHint")); // NOI18N
        generatedFiles.put(new StringNodeProp(conf.getMocDir(), "QtMocDir", getString("QtMocDirTxt"), getString("QtMocDirHint"))); // NOI18N
        generatedFiles.put(new StringNodeProp(conf.getRccDir(), "QtRccDir", getString("QtRccDirTxt"), getString("QtRccDirHint"))); // NOI18N
        generatedFiles.put(new StringNodeProp(conf.getUiDir(), "QtUiDir", getString("QtUiDirTxt"), getString("QtUiDirHint"))); // NOI18N
        sheet.put(generatedFiles);

        Sheet.Set expert = new Sheet.Set();
        expert.setName("QtExpert"); // NOI18N
        expert.setDisplayName(getString("QtExpertTxt")); // NOI18N
        expert.setShortDescription(getString("QtExpertHint")); // NOI18N
        expert.put(new StringNodeProp(conf.getQmakeSpec(), "QtQmakeSpec", getString("QtQmakeSpecTxt"), getString("QtQmakeSpecHint"))); // NOI18N
        expert.put(new StringListNodeProp(conf.getCustomDefs(), null, new String[]{"QtCustomDefs", getString("QtCustomDefsTxt"), getString("QtCustomDefsHint"), getString("QtCustomDefsLbl")}, false, HelpCtx.DEFAULT_HELP) { // NOI18N
            @Override
            protected List<String> convertToList(String text) {
                return TokenizerFactory.MACRO_CONVERTER.convertToList(text);
            }
            @Override
            protected String convertToString(List<String> list) {
                return TokenizerFactory.MACRO_CONVERTER.convertToString(list);
            }
        });
        sheet.put(expert);

        return sheet;
    }
}
