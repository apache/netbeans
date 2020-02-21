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
package org.netbeans.modules.cnd.makeproject.ui.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.utils.ui.CndUIConstants;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
    id=CndUIConstants.TOOLS_OPTIONS_CND_PROJECTS_ID,
    location=CndUIConstants.TOOLS_OPTIONS_CND_CATEGORY_ID,
    displayName="#TAB_ProjectsTab", // NOI18N
    position=200
)
public final class ProjectOptionsPanelController extends OptionsPanelController {

    private final ProjectOptionsPanel panel = new ProjectOptionsPanel();

    @Override
    public void update() {
        panel.update();
    }

    @Override
    public void applyChanges() {
        panel.applyChanges();
    }

    @Override
    public void cancel() {
        panel.cancel();
    }

    @Override
    public boolean isValid() {
        return panel.dataValid();
    }

    @Override
    public boolean isChanged() {
        return panel.isChanged();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("cnd.optionsDialog"); // NOI18N
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        panel.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        panel.removePropertyChangeListener(l);
    }
}
