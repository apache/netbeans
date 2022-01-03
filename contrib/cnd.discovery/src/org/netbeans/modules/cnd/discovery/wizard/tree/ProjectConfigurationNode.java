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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.text.MessageFormat;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel;
import org.openide.util.NbBundle;

/**
 *
 */
public class ProjectConfigurationNode extends DefaultMutableTreeNode {
    private final ProjectConfigurationImpl project;
    private final int count;
    
    public ProjectConfigurationNode(ProjectConfigurationImpl project) {
        super(project);
        this.project = project;
        count = project.getFiles().size();
        add(new FolderConfigurationNode((FolderConfigurationImpl) project.getRoot()));
    }
    
    @Override
    public String toString() {
        if (getProject().getLanguageKind() == ItemProperties.LanguageKind.C){
            return getString("ConfigurationLanguageC",""+count);  // NOI18N
        } else if (getProject().getLanguageKind() == ItemProperties.LanguageKind.CPP){
            return getString("ConfigurationLanguageCPP",""+count);  // NOI18N
        } else if (getProject().getLanguageKind() == ItemProperties.LanguageKind.Fortran){
            return getString("ConfigurationLanguageFortran",""+count);  // NOI18N
        }
         return getString("ConfigurationLanguageUnknown",""+count);  // NOI18N
    }
    
    public ProjectConfigurationImpl getProject() {
        return project;
    }

    private String getString(String key, String files) {
        String message = NbBundle.getBundle(SelectConfigurationPanel.class).getString(key);
        return MessageFormat.format(message, new Object[]{files});
        
    }
}
