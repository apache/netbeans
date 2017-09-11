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
package org.netbeans.modules.apisupport.project.ui.wizard.javahint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.spi.NbProjectProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@TemplateRegistration(folder=UIUtil.TEMPLATE_FOLDER, position=1500, displayName="#template_hint", iconBase="org/netbeans/modules/apisupport/project/ui/wizard/javahint/suggestion.png", description="javaHint.html", category=UIUtil.TEMPLATE_CATEGORY)
@Messages("template_hint=Java Hint")
public class NewJavaHintIterator extends BasicWizardIterator {

    private DataModel data;

    @Override
    protected Panel[] createPanels(WizardDescriptor wiz) {
        data = new DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new JavaHintDataPanel(wiz, data),
            new JavaHintLocationPanel(wiz, data)
        };
    }

    @Override
    public Set<?> instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }

    static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(model.getProject());

        //add module dependency
        cmf.add(cmf.addModuleDependency("org.netbeans.modules.java.source.base", "java")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.modules.java.source", "java")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.spi.java.hints", "java")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.spi.editor.hints", "ide")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.libs.javacapi", "java")); // NOI18N
        cmf.add(cmf.addTestModuleDependency("org.netbeans.modules.java.hints.test", "java"));
        cmf.add(cmf.addTestModuleDependency("org.netbeans.libs.junit4", "extra"));
        cmf.add(cmf.addTestModuleDependency("org.netbeans.modules.nbjunit", "harness")); // NOI18N
        cmf.add(cmf.addTestModuleDependency("org.netbeans.modules.parsing.nb", "ide")); // NOI18N
        cmf.add(cmf.addTestModuleDependency("org.netbeans.modules.projectapi.nb", "ide")); // NOI18N
        
        NbProjectProvider nbProjectProvider = getProjectProvider(model.getProject());

        if(nbProjectProvider!=null && nbProjectProvider.isSuiteComponent()) {
            //add module to target platform

            //java cluster
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.classfile", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.libs.javacimpl", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.spi.java.hints", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.java.hints.test", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.java.lexer", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.java.platform", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.java.project", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.api.java", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.lib.nbjavac", "java")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.java.preprocessorbridge", "java")); // NOI18N

            //extide cluster
            cmf.add(cmf.addModuleToTargetPlatform("org.apache.tools.ant.module", "extide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.options.java", "extide")); // NOI18N

            //harness cluster
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.insane", "harness")); // NOI18N

            //ide cluster
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.project.ant", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.apache.xml.resolver", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.api.java.classpath", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.diff", "ide")); // NOI18N ?
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.actions", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.document", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.fold", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.fold.nbui", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.errorstripe.api", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.errorstripe", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.guards", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.indent", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.indent.project", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.indent.support", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.lib", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.lib2", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.options.editor", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.settings", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.settings.lib", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.settings.storage", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.editor.util", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.project.libraries", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.libs.freemarker", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.lexer", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.libs.lucene", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.spi.navigator", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.parsing.api", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.parsing.indexing", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.parsing.lucene", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.parsing.nb", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.projectapi", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.project.indexingbridge", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.project.spi.intern", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.projectuiapi.base", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.projectuiapi", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.refactoring.api", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.code.analysis", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.spi.tasklist", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.xml.catalog", "ide")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.api.xml", "ide")); // NOI18N

            //platform cluster
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.autoupdate.services", "platform")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.netbeans.modules.autoupdate.ui", "platform")); // NOI18N
            cmf.add(cmf.addModuleToTargetPlatform("org.openide.execution", "platform")); // NOI18N
        }
        
        String className = model.getClassName();
        FileObject hintTemplate = CreatedModifiedFiles.getTemplate("javaHint.java"); // NOI18N
        assert hintTemplate != null;

        String hintPath = model.getDefaultPackagePath(className + ".java", false); // NOI18N

        Map<String,String> replaceTokens = new HashMap<String,String>();
        replaceTokens.put("CLASS_NAME", className); // NOI18N
        replaceTokens.put("PACKAGE_NAME", model.getPackageName()); // NOI18N
        replaceTokens.put("GENERATE_FIX", model.isDoFix() ? "true" : null); // NOI18N
        replaceTokens.put("DISPLAY_NAME", model.getDisplayName()); // NOI18N
        replaceTokens.put("DESCRIPTION", replaceLineBreaksWithHTMLLineBreaks(model.getDescription())); // NOI18N
        replaceTokens.put("WARNING_MESSAGE", model.getWarningMessage()); // NOI18N
        if (model.isDoFix()) {
            replaceTokens.put("FIX_MESSAGE", model.getFixText()); // NOI18N
        }

        cmf.add(cmf.createFileWithSubstitutions(hintPath, hintTemplate, replaceTokens));

        String testPath = model.getDefaultPackagePath(className + "Test.java", false, true); // NOI18N
        FileObject testTemplate = null;
        if(nbProjectProvider != null && nbProjectProvider.isNbPlatformApplication()) {
            testTemplate = CreatedModifiedFiles.getTemplate("javaHintTestApp.java"); // NOI18N
        } else {
            testTemplate = CreatedModifiedFiles.getTemplate("javaHintTest.java"); // NOI18N
        }
        assert testTemplate != null;

        cmf.add(cmf.createFileWithSubstitutions(testPath, testTemplate, replaceTokens));

        //at the end
        model.setCreatedModifiedFiles(cmf);
    }

    private static String replaceLineBreaksWithHTMLLineBreaks(String text) {
        String result = null != text ? text : ""; // NOI18N
        result = result.trim().replaceAll("(\r?\n|\r)(?=.)", "<br/>\" +\n\""); // NOI18N
        return result;
    }
    
    private static NbProjectProvider getProjectProvider(Project prj) {
            return prj.getLookup().lookup(NbProjectProvider.class);
    }

    static final class DataModel extends BasicWizardIterator.BasicDataModel {

        private CreatedModifiedFiles files;
        private String className;
        private String displayName;
        private String descriptor;
        private String warningMessage;
        private boolean doFix;
        private String fixText;
        private String codeNameBase;

        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }

        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return files;
        }

        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getDescription() {
            return descriptor;
        }

        public void setDescription(String descriptor) {
            this.descriptor = descriptor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public CreatedModifiedFiles getFiles() {
            return files;
        }

        public void setFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        public boolean isDoFix() {
            return doFix;
        }

        public void setDoFix(boolean doFix) {
            this.doFix = doFix;
        }

        public String getFixText() {
            return fixText;
        }

        public void setFixText(String fixText) {
            this.fixText = fixText;
        }

        public String getWarningMessage() {
            return warningMessage;
        }

        public void setWarningMessage(String warningMessage) {
            this.warningMessage = warningMessage;
        }

        public @Override String getPackageName() {
            String retValue;
            retValue = super.getPackageName();
            if (retValue == null) {
                retValue = getCodeNameBase();
                super.setPackageName(retValue);
            }
            return retValue;
        }

        private String getCodeNameBase() {
            if (codeNameBase == null) {
                NbModuleProvider mod = getProject().getLookup().lookup(NbModuleProvider.class);
                codeNameBase = mod.getCodeNameBase();
            }
            return codeNameBase;
        }
    }

}
