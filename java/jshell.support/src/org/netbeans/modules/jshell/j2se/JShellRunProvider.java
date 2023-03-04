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
package org.netbeans.modules.jshell.j2se;

import org.netbeans.modules.jshell.project.JShellOptions2;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(
        service = J2SECategoryExtensionProvider.class,
         projectType = {
            "org-netbeans-modules-java-j2seproject",
            "org-netbeans-modules-java-j2semodule"
        }
)
public class JShellRunProvider implements J2SECategoryExtensionProvider{
    private JShellOptions2  component;
    
    @Override
    public ExtensibleCategory getCategory() {
        return ExtensibleCategory.RUN;
    }

    @Override
    public JComponent createComponent(Project proj, ConfigChangeListener listener) {
        if (component == null || component.getProject() != proj) {
            component = new JShellOptions2(proj);
        }
        component.setConfigChangeListener(listener);
        return component;
    }

    @Override
    public void configUpdated(Map<String, String> props) {
        if (component != null) {
            component.readOptions(props);
        }
    }
}
