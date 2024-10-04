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
package org.netbeans.modules.php.blade.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@UiUtils.PhpOptionsPanelRegistration(
        id = BladeOptionsPanelController.ID,
        displayName = "Blade", // NOI18N
        position = 2150
)
public class BladeOptionsPanelController extends OptionsPanelController {

    static final String ID = "Blade"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH + "/" + ID; // NOI18N

    private BladeOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        getPanel().load();
    }

    @Override
    public void applyChanges() {
        getPanel().store();
    }

    @Override
    public void cancel() {
        //no action
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return getPanel().changed();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.blade.editor.Options"); //NOI18N
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private BladeOptionsPanel getPanel() {
        if (panel == null) {
            panel = new BladeOptionsPanel();
        }
        return panel;
    }

    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }
}
