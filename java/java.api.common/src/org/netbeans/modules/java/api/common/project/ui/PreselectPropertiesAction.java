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
package org.netbeans.modules.java.api.common.project.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.util.NbBundle;

/**
 * Action to preselect a category in the project's properties.
 * @author Tomas Zezula
 */
final class PreselectPropertiesAction extends AbstractAction {
    private final Project project;
    private final String nodeName;
    private final String panelName;

    PreselectPropertiesAction(Project project, String nodeName, String panelName) {
        super(NbBundle.getMessage(PreselectPropertiesAction.class, "LBL_Properties_Action"));
        this.project = project;
        this.nodeName = nodeName;
        this.panelName = panelName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CustomizerProvider2 cp2 = project.getLookup().lookup(CustomizerProvider2.class);
        if (cp2 != null) {
            cp2.showCustomizer(nodeName, panelName);
        } else {
            CustomizerProvider cp = project.getLookup().lookup(CustomizerProvider.class);
            if (cp != null) {
                cp.showCustomizer();
            }
        }
    }
}
