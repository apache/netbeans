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

package org.netbeans.modules.dlight.sendto.config;

import org.netbeans.modules.dlight.sendto.api.ConfigurationsRegistry;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 */
@OptionsPanelController.SubRegistration(id = OptionsController.ID, displayName = "#LBL_Options")
public final class OptionsController extends OptionsPanelController {

    public static final String ID = "SendTo"; // NOI18N
    private OptionsPanel panel;

    @Override
    public void update() {
        getPanel().update();
    }

    @Override
    public void applyChanges() {
        getPanel().applyChanges();
        ConfigurationsRegistry.store();
    }

    @Override
    public void cancel() {
        getPanel().cancel();
    }

    @Override
    public boolean isValid() {
        return getPanel().isDataValid();
    }

    @Override
    public boolean isChanged() {
        return getPanel().isModified();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("sendto-about"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPanel().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPanel().addPropertyChangeListener(l);
    }

    private OptionsPanel getPanel() {
        if (panel == null) {
            panel = new OptionsPanel();
        }

        return panel;
    }
}
