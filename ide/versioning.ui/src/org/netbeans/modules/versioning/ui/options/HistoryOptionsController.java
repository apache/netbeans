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
package org.netbeans.modules.versioning.ui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.versioning.ui.history.HistorySettings;
import org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public final class HistoryOptionsController extends OptionsPanelController implements DocumentListener, ActionListener, VCSOptionsKeywordsProvider {
    
    private final HistoryOptionsPanel panel;
    private boolean noLabelValue;
    private String daysValue;
    private String increments;
    
    public HistoryOptionsController() {
        panel = new HistoryOptionsPanel();
        panel.warningLabel.setVisible(false);
        panel.olderThanDaysTextField.getDocument().addDocumentListener(this);
        panel.daysIncrementTextField.getDocument().addDocumentListener(this);
        panel.keepForeverRadioButton.addActionListener(this);
        panel.removeOlderRadioButton.addActionListener(this);
        panel.loadAllRadioButton.addActionListener(this);
        panel.loadIncrementsRadioButton.addActionListener(this);
    }   
        
    @Override
    public void update() {        
        panel.olderThanDaysTextField.setText(daysValue = String.valueOf(HistorySettings.getInstance().getTTL()));
        panel.daysIncrementTextField.setText(increments = String.valueOf(HistorySettings.getInstance().getIncrements()));
        panel.noLabelCleanupCheckBox.setSelected(noLabelValue = !HistorySettings.getInstance().getCleanUpLabeled());
        if(HistorySettings.getInstance().getKeepForever()) {
            panel.keepForeverRadioButton.setSelected(true);
        } else {
            panel.removeOlderRadioButton.setSelected(true);
        }
        if(HistorySettings.getInstance().getLoadAll()) {
            panel.loadAllRadioButton.setSelected(true);
        } else {
            panel.loadIncrementsRadioButton.setSelected(true);
        }
        updateForeverState();
        updateLoadAllState();
    }

    @Override
    public void applyChanges() {
        if(!isValid()) return;
        if(panel.keepForeverRadioButton.isSelected()) {
            HistorySettings.getInstance().setKeepForever(true);
            HistorySettings.getInstance().setTTL(Integer.parseInt(daysValue));
            HistorySettings.getInstance().setCleanUpLabeled(!noLabelValue);
        } else {
            HistorySettings.getInstance().setKeepForever(false);
            HistorySettings.getInstance().setTTL(Integer.parseInt(panel.olderThanDaysTextField.getText()));
            HistorySettings.getInstance().setCleanUpLabeled(!panel.noLabelCleanupCheckBox.isSelected());
        }
        if(panel.loadAllRadioButton.isSelected()) {
            HistorySettings.getInstance().setLoadAll(true);
            HistorySettings.getInstance().setIncrements(Integer.parseInt(increments));
        } else {
            HistorySettings.getInstance().setLoadAll(false);
            HistorySettings.getInstance().setIncrements(Integer.parseInt(panel.daysIncrementTextField.getText()));
        }
    }

    @Override
    public void cancel() {
        // do nothing
    }

    @Override
    public boolean isValid() {
        boolean valid = true;
        try {       
            if(!panel.keepForeverRadioButton.isSelected()) {
                Integer.parseInt(panel.olderThanDaysTextField.getText());
            } 
            if(panel.loadIncrementsRadioButton.isSelected()) {
                Integer.parseInt(panel.daysIncrementTextField.getText());
            } 
        } catch (NumberFormatException e) {
            valid = false;
        }
        panel.warningLabel.setVisible(!valid); 
        return valid;
    }

    @Override
    public boolean isChanged() {       
        String ttl = Long.toString(HistorySettings.getInstance().getTTL());        
        String increments = Long.toString(HistorySettings.getInstance().getIncrements());        
        return !ttl.equals(panel.olderThanDaysTextField.getText())
                || HistorySettings.getInstance().getKeepForever() && panel.removeOlderRadioButton.isSelected()
                || HistorySettings.getInstance().getLoadAll() && panel.loadIncrementsRadioButton.isSelected()
                || panel.daysIncrementTextField.isEnabled() && !increments.equals(panel.daysIncrementTextField.getText())
                || (panel.noLabelCleanupCheckBox.isSelected() != !HistorySettings.getInstance().getCleanUpLabeled())
                || (panel.keepForeverRadioButton.isSelected() != HistorySettings.getInstance().getKeepForever())
                || (panel.loadAllRadioButton.isSelected() != HistorySettings.getInstance().getLoadAll());
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.localhistory.options.LocalHistoryOptionsController"); // NOi18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // do nothing
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // do nothing
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
       isValid();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
       isValid();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
       isValid();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.keepForeverRadioButton || e.getSource() == panel.removeOlderRadioButton) {
            updateForeverState();
        } else if(e.getSource() == panel.loadAllRadioButton || e.getSource() == panel.loadIncrementsRadioButton) {
            updateLoadAllState();
        }
    }

    @Override
    public boolean acceptKeywords (List<String> keywords) {
        Set<String> allKeywords = new HashSet<String>(panel.getKeywords());
        allKeywords.retainAll(keywords);
        return !allKeywords.isEmpty();
    }
    
    private void updateForeverState() {
        if(panel.keepForeverRadioButton.isSelected()) {
            panel.olderThanDaysTextField.setEnabled(false);
            panel.noLabelCleanupCheckBox.setEnabled(false);
            panel.noLabelCleanupCheckBox.setEnabled(false);
            noLabelValue = panel.noLabelCleanupCheckBox.isSelected();
            daysValue = panel.olderThanDaysTextField.getText();

            panel.noLabelCleanupCheckBox.setSelected(false);
            panel.olderThanDaysTextField.setText(""); // NOI18N
        } else {
            panel.olderThanDaysTextField.setEnabled(true);
            panel.noLabelCleanupCheckBox.setEnabled(true);

            panel.noLabelCleanupCheckBox.setSelected(noLabelValue);
            panel.olderThanDaysTextField.setText(daysValue);
        }
    }
    
    private void updateLoadAllState() {
        if(panel.loadAllRadioButton.isSelected()) {
            panel.loadAllRadioButton.setSelected(true);
            panel.loadIncrementsRadioButton.setSelected(false);
            panel.daysIncrementTextField.setEnabled(false);
        
            increments = panel.daysIncrementTextField.getText();
            panel.daysIncrementTextField.setText(""); // NOI18N
        } else {
            panel.daysIncrementTextField.setText(increments);
            
            panel.loadAllRadioButton.setSelected(false);
            panel.loadIncrementsRadioButton.setSelected(true);
            panel.daysIncrementTextField.setEnabled(true);
        }
    }
}
