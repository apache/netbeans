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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.modules.web.clientproject.sites.SiteZip;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class SiteTemplateWizard extends JPanel {

    private static final long serialVersionUID = 154768576465454L;

    static final Logger LOGGER = Logger.getLogger(SiteTemplateWizard.class.getName());
    private static final SiteTemplateImplementation NO_SITE_TEMPLATE = new DummySiteTemplateImplementation(null);
    private static final RequestProcessor RP = new RequestProcessor(SiteTemplateWizard.class.getName(), 2);

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    // @GuardedBy("EDT")
    private final SiteZip archiveSiteTemplate = new SiteZip();
    // @GuardedBy("EDT")
    private final SiteZip.Customizer archiveSiteCustomizer;
    // @GuardedBy("EDT")
    final DefaultListModel onlineTemplatesListModel = new DefaultListModel();
    final Object siteTemplateLock = new Object();

    // @GuardedBy("siteTemplateLock")
    SiteTemplateImplementation siteTemplate = NO_SITE_TEMPLATE;

    public SiteTemplateWizard() {
        assert EventQueue.isDispatchThread();

        archiveSiteCustomizer = archiveSiteTemplate.getCustomizer();
        assert archiveSiteCustomizer != null : "Archive template must have a customizer"; //NOI18N

        initComponents();
        // archive
        initArchiveTemplate();
        // other templates
        initOnlineTemplates();
        // listeners
        initListeners();
        // fire first change
        updateSiteTemplate();
    }

    private void initArchiveTemplate() {
        archiveTemplatePanel.add(archiveSiteCustomizer.getComponent(), BorderLayout.CENTER);
    }

    @NbBundle.Messages("SiteTemplateWizard.loading=Loading...")
    private void initOnlineTemplates() {
        assert EventQueue.isDispatchThread();
        // clear cache label
        updateClearCacheLabel(false);
        // renderer
        onlineTemplateList.setCellRenderer(new TemplateListCellRenderer(onlineTemplateList.getCellRenderer()));
        // data
        onlineTemplateList.setModel(onlineTemplatesListModel);
        onlineTemplatesListModel.addElement(new DummySiteTemplateImplementation(Bundle.SiteTemplateWizard_loading()));
        RP.post(new Runnable() {
            @Override
            public void run() {
                Collection<? extends SiteTemplateImplementation> templates = Lookup.getDefault().lookupAll(SiteTemplateImplementation.class);
                onlineTemplatesListModel.removeAllElements();
                for (SiteTemplateImplementation template : templates) {
                    onlineTemplatesListModel.addElement(template);
                }
            }
        });
    }

    @NbBundle.Messages({
        "SiteTemplateWizard.clearCache.ready=<html><a href=\"#\">Clear local cache</a></html>",
        "SiteTemplateWizard.clearCache.running=Clearing...",
    })
    void updateClearCacheLabel(boolean clearing) {
        assert EventQueue.isDispatchThread();
        String label;
        if (clearing) {
            label = Bundle.SiteTemplateWizard_clearCache_running();
        } else {
            label = Bundle.SiteTemplateWizard_clearCache_ready();
        }
        clearCacheLabel.setText(label);
        // fix ui
        clearCacheLabel.setMaximumSize(clearCacheLabel.getPreferredSize());
    }

    private void initListeners() {
        // radios
        ItemListener defaultItemListener = new DefaultItemListener();
        noTemplateRadioButton.addItemListener(defaultItemListener);
        archiveTemplateRadioButton.addItemListener(defaultItemListener);
        onlineTemplateRadioButton.addItemListener(defaultItemListener);
        // online templates
        onlineTemplateList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setSiteTemplate(getSelectedOnlineTemplate());
                fireChange();
                updateOnlineTemplateDescription();
            }
        });
    }

    final void updateSiteTemplate() {
        enableTemplates();
        fireChange();
    }

    private void enableTemplates() {
        if (noTemplateRadioButton.isSelected()) {
            setSiteTemplate(NO_SITE_TEMPLATE);
            setArchiveTemplateEnabled(false);
            setOnlineTemplateEnabled(false);
        } else if (archiveTemplateRadioButton.isSelected()) {
            setSiteTemplate(archiveSiteTemplate);
            setArchiveTemplateEnabled(true);
            setOnlineTemplateEnabled(false);
        } else if (onlineTemplateRadioButton.isSelected()) {
            setSiteTemplate(getSelectedOnlineTemplate());
            setArchiveTemplateEnabled(false);
            setOnlineTemplateEnabled(true);
        } else {
            throw new IllegalStateException("No template radio button selected?!"); // NOI18N
        }
    }

    void updateOnlineTemplateDescription() {
        String desc;
        synchronized (siteTemplateLock) {
            desc = siteTemplate != null ? siteTemplate.getDescription() : ""; // NOI18N
        }
        onlineTemplateDescriptionTextPane.setText(desc);
    }

    SiteTemplateImplementation getSelectedOnlineTemplate() {
        return (SiteTemplateImplementation) onlineTemplateList.getSelectedValue();
    }

    private void setArchiveTemplateEnabled(boolean enabled) {
        for (Component component : archiveSiteCustomizer.getComponent().getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void setOnlineTemplateEnabled(boolean enabled) {
        onlineTemplateDescriptionLabel.setEnabled(enabled);
        onlineTemplateList.setEnabled(enabled);
        onlineTemplateDescriptionTextPane.setEnabled(enabled);
    }

    public void addChangeListener(ChangeListener listener) {
        assert EventQueue.isDispatchThread();
        changeSupport.addChangeListener(listener);
        archiveSiteCustomizer.addChangeListener(listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
        // #216283 - can be called form non-EDT thread
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                archiveSiteCustomizer.removeChangeListener(listener);
            }
        });
    }

    @NbBundle.Messages({"SiteTemplateWizard.error.noTemplateSelected=No online template selected."})
    public String getErrorMessage() {
        boolean isArchiveSiteTemplate;
        synchronized (siteTemplateLock) {
            if (siteTemplate == null) {
                return Bundle.SiteTemplateWizard_error_noTemplateSelected();
            }
            isArchiveSiteTemplate = siteTemplate == archiveSiteTemplate;
        }
        if (isArchiveSiteTemplate) {
            // archive
            archiveSiteCustomizer.isValid();
            return archiveSiteCustomizer.getErrorMessage();
        }
        return null;
    }

    public String getWarningMessage() {
        boolean isArchiveSiteTemplate;
        synchronized (siteTemplateLock) {
            isArchiveSiteTemplate = siteTemplate == archiveSiteTemplate;
        }
        if (isArchiveSiteTemplate) {
            // archive
            archiveSiteCustomizer.isValid();
            return archiveSiteCustomizer.getWarningMessage();
        }
        return null;
    }

    public void lockPanel() {
        enablePanel(false);
        setArchiveTemplateEnabled(false);
        setOnlineTemplateEnabled(false);
    }

    public void unlockPanel() {
        enablePanel(true);
        enableTemplates();
    }

    private void enablePanel(boolean enable) {
        noTemplateRadioButton.setEnabled(enable);
        archiveTemplateRadioButton.setEnabled(enable);
        onlineTemplateRadioButton.setEnabled(enable);
    }

    void preSelectSiteTemplate(SiteTemplateImplementation impl) {
        if (!"NONE".equals(impl.getId()) && !"ARCHIVE".equals(impl.getId()))  { // NOI18N
            onlineTemplateRadioButton.setSelected(true);
            this.onlineTemplateList.setSelectedValue(impl, true);
        }
    }

    @NbBundle.Messages({
        "# {0} - template name",
        "SiteTemplateWizard.template.preparing=Preparing template \"{0}\" for first usage...",
        "# {0} - template name",
        "SiteTemplateWizard.error.preparing=Cannot prepare template \"{0}\" (see IDE log for more details).",
        "# {0} - template name",
        "# {1} - custom error",
        "SiteTemplateWizard.error.preparing.custom=Cannot prepare template \"{0}\". {1}"
    })
    public String prepareTemplate() {
        assert !EventQueue.isDispatchThread();
        final String templateName;
        // #242666
        final SiteTemplateImplementation siteTemplateRef;
        synchronized (siteTemplateLock) {
            if (siteTemplate.isPrepared()) {
                // already prepared
                return null;
            }
            templateName = siteTemplate.getName();
            siteTemplateRef = siteTemplate;
        }
        // prepare
        ProgressHandle progressHandle = ProgressHandle.createHandle(Bundle.SiteTemplateWizard_template_preparing(templateName));
        progressHandle.start();
        try {
            for (;;) {
                try {
                    siteTemplateRef.prepare();
                    break;
                } catch (NetworkException ex) {
                    LOGGER.log(Level.INFO, null, ex.getCause());
                    if (!NetworkSupport.showNetworkErrorDialog(ex.getFailedRequests())) {
                        return Bundle.SiteTemplateWizard_error_preparing(templateName);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return Bundle.SiteTemplateWizard_error_preparing(templateName);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return Bundle.SiteTemplateWizard_error_preparing_custom(templateName, ex.getMessage());
        } finally {
            progressHandle.finish();
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    public SiteTemplateImplementation getSiteTemplate() {
        synchronized (siteTemplateLock) {
            return siteTemplate;
        }
    }

    void setSiteTemplate(SiteTemplateImplementation siteTemplate) {
        synchronized (siteTemplateLock) {
            this.siteTemplate = siteTemplate;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        templateButtonGroup = new javax.swing.ButtonGroup();
        infoLabel = new javax.swing.JLabel();
        noTemplateRadioButton = new javax.swing.JRadioButton();
        archiveTemplateRadioButton = new javax.swing.JRadioButton();
        archiveTemplatePanel = new javax.swing.JPanel();
        onlineTemplateRadioButton = new javax.swing.JRadioButton();
        onlineTemplateScrollPane = new javax.swing.JScrollPane();
        onlineTemplateList = new javax.swing.JList();
        onlineTemplateDescriptionLabel = new javax.swing.JLabel();
        clearCacheLabel = new javax.swing.JLabel();
        onlineTemplateDescriptionScrollPane = new javax.swing.JScrollPane();
        onlineTemplateDescriptionTextPane = new javax.swing.JTextPane();

        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.infoLabel.text")); // NOI18N

        templateButtonGroup.add(noTemplateRadioButton);
        noTemplateRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(noTemplateRadioButton, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.noTemplateRadioButton.text")); // NOI18N

        templateButtonGroup.add(archiveTemplateRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(archiveTemplateRadioButton, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.archiveTemplateRadioButton.text")); // NOI18N

        archiveTemplatePanel.setLayout(new java.awt.BorderLayout());

        templateButtonGroup.add(onlineTemplateRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(onlineTemplateRadioButton, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.onlineTemplateRadioButton.text")); // NOI18N

        onlineTemplateList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        onlineTemplateScrollPane.setViewportView(onlineTemplateList);

        onlineTemplateDescriptionLabel.setLabelFor(onlineTemplateDescriptionTextPane);
        org.openide.awt.Mnemonics.setLocalizedText(onlineTemplateDescriptionLabel, org.openide.util.NbBundle.getMessage(SiteTemplateWizard.class, "SiteTemplateWizard.onlineTemplateDescriptionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(clearCacheLabel, "CLEAR CACHE"); // NOI18N
        clearCacheLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearCacheLabelMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                clearCacheLabelMousePressed(evt);
            }
        });

        onlineTemplateDescriptionTextPane.setEditable(false);
        onlineTemplateDescriptionScrollPane.setViewportView(onlineTemplateDescriptionTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(infoLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(onlineTemplateDescriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearCacheLabel))
                    .addComponent(archiveTemplatePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(onlineTemplateScrollPane)
                    .addComponent(onlineTemplateDescriptionScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noTemplateRadioButton)
                    .addComponent(archiveTemplateRadioButton)
                    .addComponent(onlineTemplateRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(infoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(noTemplateRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiveTemplateRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiveTemplatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onlineTemplateRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlineTemplateScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(onlineTemplateDescriptionLabel)
                    .addComponent(clearCacheLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlineTemplateDescriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearCacheLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearCacheLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_clearCacheLabelMouseEntered

    private void clearCacheLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearCacheLabelMousePressed
        updateClearCacheLabel(true);
        RP.post(new Runnable() {
            @Override
            public void run() {
                Enumeration<SiteTemplateImplementation> templates = onlineTemplatesListModel.elements();
                while (templates.hasMoreElements()) {
                    SiteTemplateImplementation template = templates.nextElement();
                    try {
                        template.cleanup();
                    } catch (IOException exc) {
                        LOGGER.log(Level.INFO, "Cannot cleanuop site template: " + template.getId(), exc);
                    }
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateClearCacheLabel(false);
                    }
                });
            }
        });
    }//GEN-LAST:event_clearCacheLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel archiveTemplatePanel;
    private javax.swing.JRadioButton archiveTemplateRadioButton;
    private javax.swing.JLabel clearCacheLabel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JRadioButton noTemplateRadioButton;
    private javax.swing.JLabel onlineTemplateDescriptionLabel;
    private javax.swing.JScrollPane onlineTemplateDescriptionScrollPane;
    private javax.swing.JTextPane onlineTemplateDescriptionTextPane;
    private javax.swing.JList onlineTemplateList;
    private javax.swing.JRadioButton onlineTemplateRadioButton;
    private javax.swing.JScrollPane onlineTemplateScrollPane;
    private javax.swing.ButtonGroup templateButtonGroup;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class DummySiteTemplateImplementation implements SiteTemplateImplementation {

        private final String name;


        public DummySiteTemplateImplementation(String name) {
            this.name = name;
        }

        @Override
        public String getId() {
            return "NONE"; // NOI18N
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public boolean isPrepared() {
            return true;
        }

        @Override
        public void prepare() {
            // noop
        }

        @Override
        public void configure(ProjectProperties properties) {
            // noop
        }

        @Override
        public void apply(FileObject projectDir, ProjectProperties projectProperties, ProgressHandle handle) throws IOException {
            // noop
        }

        @Override
        public void cleanup() {
            // noop
        }

    }

    private static final class TemplateListCellRenderer implements ListCellRenderer {

        private final ListCellRenderer cellRenderer;


        public TemplateListCellRenderer(ListCellRenderer cellRenderer) {
            this.cellRenderer = cellRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            SiteTemplateImplementation site = (SiteTemplateImplementation) value;
            return cellRenderer.getListCellRendererComponent(list, site.getName(), index, isSelected, cellHasFocus);
        }

    }

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                updateSiteTemplate();
            }
        }

    }

}
