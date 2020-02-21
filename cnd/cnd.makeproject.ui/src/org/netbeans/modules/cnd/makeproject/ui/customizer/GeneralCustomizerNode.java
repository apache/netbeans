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

import javax.swing.JPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.ui.configurations.ProjectPropPanel;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

class GeneralCustomizerNode extends CustomizerNode {

    private ProjectPropPanel projectPropPanel;

    public GeneralCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public JPanel getPanel(Configuration configuration) {
        if (projectPropPanel == null) {
            projectPropPanel = new ProjectPropPanel(getContext().getProject(), getContext().getConfigurationDescriptor());
            getContext().registerSavable(projectPropPanel);
        }
        return projectPropPanel;
    }

    @Override
    public CustomizerStyle customizerStyle() {
        return CustomizerStyle.PANEL;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectProperties"); // NOI18N
    }
}
