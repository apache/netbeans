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
package org.netbeans.swing.laf.flatlaf;

import com.formdev.flatlaf.FlatLaf;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author Karl Tauber
 */
@OptionsPanelController.SubRegistration(
    displayName="#FlatLaf_DisplayName",
    keywords="#KW_FlatLafOptions",
    keywordsCategory="Appearance/FlatLaf",
    location = "Appearance"
)
public class FlatLafOptionsPanelController extends OptionsPanelController {
    
    private FlatLafOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        if (!changed) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            boolean oldUseWindowDecorations = FlatLafPrefs.isUseWindowDecorations();
            boolean oldUnifiedTitleBar = FlatLafPrefs.isUnifiedTitleBar();
            boolean oldMenuBarEmbedded = FlatLafPrefs.isMenuBarEmbedded();
            boolean oldUnderlineMenuSelection = FlatLafPrefs.isUnderlineMenuSelection();
            boolean oldAlwaysShowMnemonics = FlatLafPrefs.isAlwaysShowMnemonics();

            getPanel().store();
            changed = false;

            UIDefaults defaults = UIManager.getDefaults();
            defaults.put("TitlePane.unifiedBackground", FlatLafPrefs.isUnifiedTitleBar());
            defaults.put("TitlePane.menuBarEmbedded", FlatLafPrefs.isMenuBarEmbedded());
            defaults.put("MenuItem.selectionType", FlatLafPrefs.isUnderlineMenuSelection() ? "underline" : null);
            defaults.put("Component.hideMnemonics", !FlatLafPrefs.isAlwaysShowMnemonics());
            defaults.put(FlatLFCustoms.FILECHOOSER_FAVORITES_ENABLED, FlatLafPrefs.isShowFileChooserFavorites());

            FlatLFCustoms.updateUnifiedBackground();

            if (oldUseWindowDecorations != FlatLafPrefs.isUseWindowDecorations()) {
                FlatLaf.setUseNativeWindowDecorations(FlatLafPrefs.isUseWindowDecorations());
            } 

            if (oldMenuBarEmbedded != FlatLafPrefs.isMenuBarEmbedded()) {
                FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
            } else if (oldUnifiedTitleBar != FlatLafPrefs.isUnifiedTitleBar()
                    || oldUnderlineMenuSelection != FlatLafPrefs.isUnderlineMenuSelection()
                    || oldAlwaysShowMnemonics != FlatLafPrefs.isAlwaysShowMnemonics()) {
                FlatLaf.repaintAllFramesAndDialogs();
            }
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
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

    protected FlatLafOptionsPanel getPanel() {
        if (panel == null) {
            panel = new FlatLafOptionsPanel(this);
        }
        return panel;
    }

    protected void changed(boolean isChanged) {
        if (!changed) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        changed = isChanged;
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
