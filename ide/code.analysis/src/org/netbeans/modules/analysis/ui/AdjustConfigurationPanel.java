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
package org.netbeans.modules.analysis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.netbeans.modules.analysis.Configuration;
import org.netbeans.modules.analysis.ConfigurationsManager;
import org.netbeans.modules.analysis.RunAnalysisPanel.ConfigurationRenderer;
import org.netbeans.modules.analysis.SPIAccessor;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerContext;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;

/**
 *
 * @author lahvac
 */
public class AdjustConfigurationPanel extends javax.swing.JPanel implements PropertyChangeListener {
    private static final Logger LOG = Logger.getLogger(AdjustConfigurationPanel.class.getName());

    private static final String LBL_NEW = "New...";
    private static final String LBL_DUPLICATE = "Duplicate...";
    private static final String LBL_RENAME = "Rename...";
    private static final String LBL_DELETE = "Delete";
    
    private final Iterable<? extends AnalyzerFactory> analyzers;
    private CustomizerContext<Object, JComponent> currentContext;
    private final Map<AnalyzerFactory, CustomizerProvider> customizers = new IdentityHashMap<AnalyzerFactory, CustomizerProvider>();
    private final Map<AnalyzerFactory, String> errors = new IdentityHashMap<AnalyzerFactory, String>();
    private final Map<CustomizerProvider, Object> customizerData = new IdentityHashMap<CustomizerProvider, Object>();
    private Preferences currentPreferences;
    private ModifiedPreferences currentPreferencesOverlay;
    private final String preselected;
    private final ErrorListener errorListener;

    public AdjustConfigurationPanel(Iterable<? extends AnalyzerFactory> analyzers, AnalyzerFactory preselectedAnalyzer, String preselected, Configuration configurationToSelect, ErrorListener errorListener) {
        this.preselected = preselected;
        initComponents();

        if (preselected == null) {
            final ConfigurationsComboModel model = new ConfigurationsComboModel(true);
            configurationCombo.setModel(model);
            configurationCombo.setRenderer(new ConfigurationRenderer(false));
            configurationCombo.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (configurationCombo.getSelectedItem() instanceof ActionListener) {
                        ((ActionListener) configurationCombo.getSelectedItem()).actionPerformed(e);
                    } else if (configurationCombo.getSelectedItem() instanceof String) {
                        assert model.currentActiveItem != null;
                        model.currentActiveItem.confirm(new EventObject(configurationCombo.getEditor().getEditorComponent()));
                    } else {
                        updateConfiguration();
                    }
                }
            });
        } else {
            configurationLabel.setVisible(false);
            configurationCombo.setVisible(false);
        }

        this.analyzers = analyzers;
        DefaultComboBoxModel<AnalyzerFactory> analyzerModel = new DefaultComboBoxModel<>();

        for (AnalyzerFactory a : analyzers) {
            CustomizerProvider<Object, JComponent> cp = a.getCustomizerProvider();

            if (cp == null) continue;
            
            customizers.put(a, cp);
            analyzerModel.addElement(a);
        }

        analyzerCombo.setModel(analyzerModel);
        if (preselectedAnalyzer != null) {
            analyzerCombo.setSelectedItem(preselectedAnalyzer);
        }
        analyzerCombo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateAnalyzer();
            }
        });
        analyzerCombo.setRenderer(new AnalyzerRenderer());

        updateConfiguration();
        
        if (configurationToSelect != null)
            configurationCombo.setSelectedItem(configurationToSelect);
        this.errorListener = errorListener;
    }

    private void updateConfiguration() {
        if (currentPreferencesOverlay != null && currentPreferences != null) {
            currentPreferencesOverlay.store(currentPreferences);
        }
        if (preselected == null) {
            currentPreferences = ((Configuration) configurationCombo.getSelectedItem()).getPreferences();
        } else {
            currentPreferences = ConfigurationsManager.getDefault().getTemporaryConfiguration().getPreferences();
            try {
                for (String c : currentPreferences.childrenNames()) {
                    currentPreferences.node(c).removeNode();
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        currentPreferencesOverlay = new ModifiedPreferences(null, "", currentPreferences);
        updateAnalyzer();
    }
    
    private JComponent currentPanel;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == currentPanel) {
            if ("contentLoaded".equals(evt.getPropertyName())) { // NOI18N
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w != null) {
                        w.pack();
                    }
            }
        }
    }

    private void updateAnalyzer() {
        if (currentPanel != null) {
            currentPanel.removePropertyChangeListener(this);
        }
        analyzerPanel.removeAll();
        
        final AnalyzerFactory selected = (AnalyzerFactory) analyzerCombo.getSelectedItem();
        CustomizerProvider customizer = customizers.get(selected);

        if (customizer == null) return ;
        
        if (!customizerData.containsKey(customizer)) {
            customizerData.put(customizer, customizer.initialize());
        }

        Object data = customizerData.get(customizer);
        Preferences settings = currentPreferencesOverlay.node(SPIAccessor.ACCESSOR.getAnalyzerId(selected));

        currentContext = new CustomizerContext<Object, JComponent>(settings, preselected, null, data, new ErrorListener() {
            @Override public void setError(String error) {
                synchronized (errors) {
                    if (error != null) {
                        errors.put(selected, error);
                    } else {
                        errors.remove(selected);
                    }
                }
                
                updateErrors();
            }
        });
        currentContext.setSelectedId(preselected);
        JComponent c = customizer.createComponent(currentContext);
        currentPanel = c;
        currentPanel.addPropertyChangeListener(this);
        analyzerPanel.add(c, BorderLayout.CENTER);
        analyzerPanel.revalidate();
        analyzerPanel.repaint();
    }
    
    @Messages("ERR_AnalyzerError={0}: {1}")
    private void updateErrors() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    updateErrors();
                }
            });
        }
        
        String currentAnalyzerError;
        
        synchronized (errors) {
            currentAnalyzerError = errors.get(analyzerCombo.getSelectedItem());

            if (currentAnalyzerError == null) {
                for (Entry<AnalyzerFactory, String> e : errors.entrySet()) {
                    if (e.getValue() != null) {
                        currentAnalyzerError = Bundle.ERR_AnalyzerError(SPIAccessor.ACCESSOR.getAnalyzerDisplayName(e.getKey()), e.getValue());
                    }
                }
            }
        }
        
        errorListener.setError(currentAnalyzerError);
        
        analyzerCombo.repaint();
    }

    public String getIdToRun() {
        return SPIAccessor.ACCESSOR.getSelectedId(currentContext);
    }

    public void save() {
        currentPreferencesOverlay.store(currentPreferences);
    }

    public Configuration getSelectedConfiguration() {
        return (Configuration) configurationCombo.getSelectedItem();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configurationLabel = new javax.swing.JLabel();
        configurationCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        analyzerCombo = new javax.swing.JComboBox();
        analyzerPanel = new javax.swing.JPanel();

        configurationLabel.setText(org.openide.util.NbBundle.getMessage(AdjustConfigurationPanel.class, "AdjustConfigurationPanel.configurationLabel.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AdjustConfigurationPanel.class, "AdjustConfigurationPanel.jLabel2.text")); // NOI18N

        analyzerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(analyzerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(configurationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configurationCombo, 0, 220, Short.MAX_VALUE)
                            .addComponent(analyzerCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configurationLabel)
                    .addComponent(configurationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(analyzerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(analyzerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox analyzerCombo;
    private javax.swing.JPanel analyzerPanel;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    private class AnalyzerRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            AnalyzerFactory a = (AnalyzerFactory) value;
            String text = SPIAccessor.ACCESSOR.getAnalyzerDisplayName(a);
            boolean isErroneous;
            synchronized (errors) {
                isErroneous = errors.containsKey(a);
            }
            if (isErroneous) {
                try {
                    text = "<html><font color='ref'>" + XMLUtil.toElementContent(text);
                } catch (CharConversionException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
            }
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }

    //XXX: need tests for the ModifiedPreferences
    //XXX: should move MP to some generic API, copied on several places (java.hints, findbugs, etc.)
    private static class ModifiedPreferences extends AbstractPreferences {

        private final Map<String,Object> properties = new HashMap<String, Object>();
        private final Map<String,ModifiedPreferences> subNodes = new HashMap<String, ModifiedPreferences>();

        public ModifiedPreferences(ModifiedPreferences parent, String name) {
            super(parent, name);
        }

        public ModifiedPreferences(ModifiedPreferences parent, String name, Preferences node) {
            this(parent, name); // NOI18N
            try {
                for (java.lang.String key : node.keys()) {
                    put(key, node.get(key, null));
                }
                for (String child : node.childrenNames()) {
                    subNodes.put(child, new ModifiedPreferences(this, node.name(), node.node(child)));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }


        public void store( Preferences target ) {

            try {
                for (String key : keys()) {
                    target.put(key, get(key, null));
                }
                for (String child : childrenNames()) {
                    ((ModifiedPreferences) node(child)).store(target.node(child));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        protected void putSpi(String key, String value) {
            properties.put(key, value);
        }

        protected String getSpi(String key) {
            return (String)properties.get(key);
        }

        protected void removeSpi(String key) {
            properties.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            ((ModifiedPreferences) parent()).subNodes.put(name(), new ModifiedPreferences(this, name()));
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[properties.keySet().size()];
            return properties.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            return subNodes.keySet().toArray(new String[0]);
        }

        protected AbstractPreferences childSpi(String name) {
            ModifiedPreferences result = subNodes.get(name);

            if (result == null) {
                subNodes.put(name, result = new ModifiedPreferences(this, name));
            }

            return result;
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

	boolean isEmpty() {
	    return properties.isEmpty();
	}
    }
    
    public interface ErrorListener {
        public void setError(String error);
    }
}
