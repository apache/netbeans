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

package org.netbeans.modules.git.ui.repository;

import java.awt.Dialog;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevisionPicker implements PropertyChangeListener {
    private final RevisionPickerDialog panel;
    private final JButton okButton;
    private final RevisionInfoPanelController infoPanelController;
    private Revision revision;
    private DialogDescriptor dd;
    private final RepositoryBrowserPanel browserPanel;
    private static final String PROP_PANEL_SLIDER_POSITION = "RevisionPicker.slider.pos"; //NOI18N
    private static final String PROP_BROWSER_SLIDER_POSITION = "RevisionPicker.browser.slider.pos"; //NOI18N

    public RevisionPicker (File repository, File[] roots) {
        infoPanelController = new RevisionInfoPanelController(repository);
        browserPanel = new RepositoryBrowserPanel(RepositoryBrowserPanel.OPTIONS_INSIDE_PANEL, repository, roots, null);
        panel = new RevisionPickerDialog(infoPanelController.getPanel(), browserPanel);
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(RevisionPicker.class, "LBL_RevisionPickerDialog.okButton.title")); //NOI18N
    }

    public boolean open () {
        dd = new DialogDescriptor(panel, NbBundle.getMessage(RevisionPicker.class, "LBL_RevisionPickerDialog.title"), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("org.netbeans.modules.git.ui.repository.RevisionPickerDialog"), null); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        updateDialogState();
        browserPanel.addPropertyChangeListener(this);
        Preferences prefs = GitModuleConfig.getDefault().getPreferences();
        WindowListener windowListener = new DialogBoundsPreserver(prefs, this.getClass().getName());
        dialog.addWindowListener(windowListener);
        windowListener.windowOpened(new WindowEvent(dialog, WindowEvent.WINDOW_OPENED));
        dialog.pack();
        updateSliders(prefs);
        dialog.setVisible(true);
        persistSliders(prefs);
        browserPanel.removePropertyChangeListener(this);
        return dd.getValue() == okButton;
    }

    public Revision getRevision () {
        return revision;
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RepositoryBrowserPanel.PROP_REVISION_CHANGED) {
            revision = (Revision) evt.getNewValue();
            updateDialogState();
        } else if (evt.getPropertyName() == RepositoryBrowserPanel.PROP_REVISION_ACCEPTED) {
            if (revision != null && revision.equals(evt.getNewValue())) {
                okButton.doClick();
            }
        }
    }

    private void updateSliders (Preferences prefs) {
        int pos = prefs.getInt(PROP_PANEL_SLIDER_POSITION, 0);
        if (pos > 0) {
            panel.setSliderPosition(pos);
        }
        pos = prefs.getInt(PROP_BROWSER_SLIDER_POSITION, 0);
        if (pos > 0) {
            browserPanel.setSliderPosition(pos);
        }
    }

    private void persistSliders (Preferences prefs) {
        prefs.putInt(PROP_PANEL_SLIDER_POSITION, panel.getSliderPosition());
        prefs.putInt(PROP_BROWSER_SLIDER_POSITION, browserPanel.getSliderPosition());
    }

    void displayMergedStatus (String revision) {
        browserPanel.displayBrancheMergedStatus(revision);
        infoPanelController.displayMergedStatus(revision);
    }

    private void updateDialogState () {
        boolean enabled = revision != null;
        dd.setValid(enabled);
        okButton.setEnabled(enabled);
        infoPanelController.loadInfo(revision == null ? null : revision.getRevision());
    }
}
