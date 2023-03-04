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
package org.netbeans.modules.javafx2.project.ui;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author psomol
 */
@ProjectServiceProvider(service=J2SECategoryExtensionProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class JFXApplicationCategoryProvider implements J2SECategoryExtensionProvider {

    public JFXApplicationCategoryProvider() {}
    
    @Override
    public ExtensibleCategory getCategory() {
        return ExtensibleCategory.APPLICATION;
    }

    @Override
    public JComponent createComponent(Project p, ConfigChangeListener listener) {
        boolean fxDisabled = false;
        if (p != null) {
            final J2SEPropertyEvaluator j2sepe = p.getLookup().lookup(J2SEPropertyEvaluator.class);
            fxDisabled = !JFXProjectProperties.isTrue(j2sepe.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED))
                    || JFXProjectProperties.isTrue(j2sepe.evaluator().getProperty(JFXProjectProperties.JAVAFX_SWING));
        }
        return fxDisabled ? null : JFXProjectProperties.getInstance(p.getLookup()).getApplicationPanel();
    }

    @Override
    public void configUpdated(Map<String, String> props) {
    }
    
}
