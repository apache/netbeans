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
