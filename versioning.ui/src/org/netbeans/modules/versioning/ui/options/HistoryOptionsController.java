/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
