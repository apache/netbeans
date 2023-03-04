/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.ModuleInfoUtils;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class CompilerPluginVersionError implements POMErrorFixProvider {
    private final Configuration configuration;
    
    @NbBundle.Messages({
        "TIT_WrongCompilerVersion=Wrong maven-compiler-plugin version.",
        "DESC_ModulesNotSupported=Modules are not supported with maven-compiler-plugin < 3.6."})
    public CompilerPluginVersionError() {
        configuration = new Configuration("CompilerPluginVersionError", //NOI18N
                Bundle.TIT_WrongCompilerVersion(),
                Bundle.DESC_ModulesNotSupported(),
                true, 
                Configuration.HintSeverity.WARNING);
    }

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        
        if(prj == null) {
            return toRet;
        }
        
        if(ModuleInfoUtils.checkModuleInfoAndCompilerFit(prj)) {
            return toRet;
        }
                
        int pos = -1;
        org.netbeans.modules.maven.model.pom.Project p = model.getProject();
        Build bld = p.getBuild();
        if (bld != null) {
            Plugin plg = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
            if (plg != null) {
                pos = plg.findPosition();
            }
        }    
        
        if(pos == -1) {
            pos = p.findPosition();
        }
        
        if(pos == -1) {
            return toRet;
        }        
               
        Document baseDocument = model.getBaseDocument();        
        Line line = NbEditorUtilities.getLine(baseDocument, pos, false);
        toRet.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, Bundle.DESC_ModulesNotSupported(), Collections.<Fix>singletonList(new UpdatePluginVersion(model)), baseDocument, line.getLineNumber() + 1));
        
        return toRet;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    @Override
    public String getSavedValue(JComponent customCustomizer, String key) {
        return null;
    }

    @Override
    public void cancel() { }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @NbBundle.Messages({"TXT_UpdateCompiler=Update maven-compiler-plugin version."})       
    private static class UpdatePluginVersion implements Fix {
        private final POMModel mdl;

        UpdatePluginVersion(POMModel model) {
            mdl = model;            
        }

        @Override
        public String getText() {
            return Bundle.TXT_UpdateCompiler();
        }

        @Override
        public ChangeInfo implement() throws Exception {

            ChangeInfo info = new ChangeInfo();
            if (!mdl.getState().equals(Model.State.VALID)) {
                return info;
            }
            
            PomModelUtils.implementInTransaction(mdl, new Runnable() {
                @Override
                public void run() {
                    org.netbeans.modules.maven.model.pom.Project prj = mdl.getProject();
                    ModelUtils.updatePluginVersion(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "3.6.1", prj);
                    ModelUtils.openAtPlugin(mdl, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
                }
            });
            
            return info;
        }
    }
}
