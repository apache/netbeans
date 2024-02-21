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
package org.netbeans.modules.php.analysis.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
    location=UiUtils.OPTIONS_PATH,
    id=AnalysisOptionsPanelController.OPTIONS_SUB_PATH,
    displayName="#AnalysisOptionsPanel.name",
//    toolTip="#LBL_OptionsTooltip"
    position=157
)
public class AnalysisOptionsPanelController extends OptionsPanelController implements ChangeListener {

    public static final String OPTIONS_SUB_PATH = "CodeAnalysis"; // NOI18N
    public static final String OPTIONS_PATH = UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUB_PATH; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("EDT")
    private AnalysisOptionsPanel analysisOptionsPanel = null;
    // @GuardedBy("EDT")
    private List<AnalysisCategoryPanel> categoryPanels = null;
    private volatile boolean changed = false;


    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        for (AnalysisCategoryPanel panel : getCategoryPanels()) {
            panel.update();
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (AnalysisCategoryPanel panel : getCategoryPanels()) {
                    panel.applyChanges();
                }

                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        AnalysisOptionsPanel panel = getAnalysisOptionsPanel();
        AnalysisCategoryPanel selectedPanel = panel.getSelectedPanel();
        if (selectedPanel != null) {
            ValidationResult result = selectedPanel.getValidationResult();
            // errors
            if (result.hasErrors()) {
                panel.setError(result.getErrors().get(0).getMessage());
                return false;
            }
            // warnings
            if (result.hasWarnings()) {
                panel.setWarning(result.getWarnings().get(0).getMessage());
                return true;
            }
        }
        // everything ok
        panel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }
    
    private void fireChanged() {
        boolean isChanged = false;
        for (AnalysisCategoryPanel panel : getCategoryPanels()) {
            isChanged |= panel.isChanged();
        }
        changed = isChanged;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getAnalysisOptionsPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!changed) {
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        fireChanged();
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private AnalysisOptionsPanel getAnalysisOptionsPanel() {
        assert EventQueue.isDispatchThread();
        if (analysisOptionsPanel == null) {
            analysisOptionsPanel = new AnalysisOptionsPanel();
            analysisOptionsPanel.addChangeListener(this);
            // inner panels
            String firstPanelName = null;
            for (AnalysisCategoryPanel panel : getCategoryPanels()) {
                if (firstPanelName == null) {
                    firstPanelName = panel.getCategoryName();
                }
                panel.addChangeListener(this);
                analysisOptionsPanel.addCategoryPanel(panel);
            }
            if (firstPanelName != null) {
                // set selected panel
                analysisOptionsPanel.selectCategoryPanel(firstPanelName);
            }
        }
        return analysisOptionsPanel;
    }

    private Collection<AnalysisCategoryPanel> getCategoryPanels() {
        assert EventQueue.isDispatchThread();
        if (categoryPanels == null) {
            categoryPanels = new ArrayList<>(AnalysisCategoryPanels.getCategoryPanels());
            // sort them by name
            final Collator collator = Collator.getInstance();
            categoryPanels.sort(new Comparator<AnalysisCategoryPanel>() {
                @Override
                public int compare(AnalysisCategoryPanel o1, AnalysisCategoryPanel o2) {
                    return collator.compare(o1.getCategoryName(), o2.getCategoryName());
                }
            });
        }
        return categoryPanels;
    }

}
