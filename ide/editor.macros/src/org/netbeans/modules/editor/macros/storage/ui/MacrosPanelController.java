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
package org.netbeans.modules.editor.macros.storage.ui;

import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Jan Jancura
 */
@OptionsPanelController.SubRegistration(
    location=OptionsDisplayer.EDITOR,
    id="Macros",
    displayName="#CTL_Macros_DisplayName",
    keywords="#KW_Macros",
    keywordsCategory="Editor/Macros",
    position=700
//    toolTip="#CTL_Macros_ToolTip"
)
public final class MacrosPanelController extends OptionsPanelController {

    public void update() {
        MacrosModel model = lastPanel.getModel();
        if (!model.isLoaded()) {
            model.load();
        }
    }

    public void applyChanges() {
        lastPanel.save();
    }

    public void cancel() {
        lastPanel.getModel().load();
    }

    public boolean isValid() {
        return true;
    }

    public boolean isChanged() {
        if (lastPanel == null) {
            return false;
        }
        return lastPanel.getModel().isChanged();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.editor.macros"); //NOI18N
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getMacrosPanel(masterLookup);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        lastPanel.getModel().addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        lastPanel.getModel().removePropertyChangeListener(l);
    }
    private static final Map<Lookup, Reference<MacrosPanel>> PANELS = new WeakHashMap<Lookup, Reference<MacrosPanel>>();
    private MacrosPanel lastPanel = null;

    private MacrosPanel getMacrosPanel(Lookup masterLookup) {
        Reference<MacrosPanel> ref = PANELS.get(masterLookup);
        MacrosPanel panel = ref == null ? null : ref.get();

        if (panel == null) {
            panel = new MacrosPanel(masterLookup);
            PANELS.put(masterLookup, new WeakReference<MacrosPanel>(panel));
        }

        this.lastPanel = panel;
        return panel;
    }
}
