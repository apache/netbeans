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

package org.netbeans.modules.javawebstart;

import java.util.Map;

import javax.swing.JComponent;

import org.netbeans.api.project.Project;

import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;

import org.netbeans.modules.javawebstart.ui.customizer.JWSCustomizerPanel;

import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 *
 * @author Milan Kubec
 * @author Petr Somol
 * @since 1.14
 */
@ProjectServiceProvider(service=J2SECategoryExtensionProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class J2SERunConfigProviderImpl implements J2SECategoryExtensionProvider {
    
    public J2SERunConfigProviderImpl() {}
    
    @Override
    public ExtensibleCategory getCategory() {
        return ExtensibleCategory.RUN;
    }
    
    @Override
    public JComponent createComponent(Project p, J2SECategoryExtensionProvider.ConfigChangeListener listener) {
        J2SEPropertyEvaluator j2sePropEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        PropertyEvaluator evaluator = j2sePropEval.evaluator();
        String enabled = evaluator.getProperty("jnlp.enabled"); // NOI18N
        JWSCustomizerPanel.runComponent.addListener(listener);
        if ("true".equals(enabled)) { // NOI18N
            JWSCustomizerPanel.runComponent.setCheckboxEnabled(true);
            JWSCustomizerPanel.runComponent.setHintVisible(false);
        } else {
            JWSCustomizerPanel.runComponent.setCheckboxEnabled(false);
            JWSCustomizerPanel.runComponent.setHintVisible(true);
        }
        return JWSCustomizerPanel.runComponent;
    }
    
    @Override
    public void configUpdated(Map<String,String> m) {
        if ((m.get("$target.run") != null) && (m.get("$target.debug") != null)) { // NOI18N
            JWSCustomizerPanel.runComponent.setCheckboxSelected(true);
        } else {
            JWSCustomizerPanel.runComponent.setCheckboxSelected(false);
        }
    }
    
}
