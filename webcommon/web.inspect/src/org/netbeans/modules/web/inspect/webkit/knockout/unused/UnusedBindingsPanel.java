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

package org.netbeans.modules.web.inspect.webkit.knockout.unused;

import java.awt.EventQueue;
import java.awt.dnd.DnDConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.inspect.files.Files;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.page.Page;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel showing information about unused bindings.
 *
 * @author Jan Stola
 */
public class UnusedBindingsPanel extends javax.swing.JPanel implements ExplorerManager.Provider  {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(UnusedBindingsPanel.class);
    /** Page model for this panel. */
    private WebKitPageModel pageModel;
    /** Explorer manager provided by this panel. */
    private final ExplorerManager manager = new ExplorerManager();
    /** Tree with unused bindings. */
    private BeanTreeView treeView;
    /** Root node of the tree with unused bindings. */
    private final UnusedRootNode rootNode = new UnusedRootNode(Collections.<String, Map<Integer, UnusedBinding>>emptyMap());

    /**
     * Creates a new {@code UnusedBindingsPanel}.
     */
    public UnusedBindingsPanel() {
        initTreeView();
        initComponents();
        add(findPanel);
        manager.setRootContext(rootNode);
        dataPanel.add(treeView);
    }

    /**
     * Initializes the three with unused bindings.
     */
    private void initTreeView() {
        treeView = new BeanTreeView();
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
    }

    /**
     * Sets the page model for this panel.
     * 
     * @param pageModel page model for this panel
     */
    public void setPageModel(WebKitPageModel pageModel) {
        this.pageModel = pageModel;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    /**
     * Prepares the page for the collection of unused binding information.
     * It reloads the page with a special JavaScript preprocessor.
     */
    void preparePage() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Page page = pageModel.getWebKit().getPage();
                String prefix = Files.getScript("knockout-pre"); // NOI18N
                prefix = prefix.replace("\"", "\\\""); // NOI18N
                prefix = toOneLiner(prefix);
                String suffix = Files.getScript("knockout-post"); // NOI18N
                suffix = suffix.replace("\"", "\\\""); // NOI18N
                suffix = suffix.replace("\n", "\\n"); // NOI18N
                String preprocessor =
                        "(function (script) {\n" + // NOI18N
                        "  var prefix = \"" + prefix + "\";\n" +  // NOI18N
                        "  var suffix = \"" + suffix + "\";\n" +  // NOI18N
                        "  var newScript;\n" + // NOI18N
                        "  if (script.indexOf('getBindingAccessors') != -1 && script.indexOf('bindingProvider') != -1) {\n" + // NOI18N
                        "    newScript = prefix + script + suffix;\n" + // NOI18N
                        "  } else {\n" + // NOI18N
                        "    newScript = script;\n" + // NOI18N
                        "  }\n" + // NOI18N
                        "  return newScript;\n" + // NOI18N
                        "})";
                page.reload(false, null, preprocessor);
            }
        });
    }

    /**
     * Removes all new-line characters (and line comments) from
     * the given JavaScript source code. In other words, puts all the JavaScript
     * source code on one line.
     * 
     * @param jsCode JavaScript source code.
     * @return one-liner equivalent to the specified JavaScript code. 
     */
    private String toOneLiner(String jsCode) {
        StringBuilder result = new StringBuilder(jsCode.length());
        for (String line : jsCode.split("\n")) { // NOI18N
            int index = line.indexOf("//"); // NOI18N
            String lineWithoutComment;
            if (index == -1) {
                lineWithoutComment = line;
            } else {
                lineWithoutComment = line.substring(0,index);
            }
            result.append(lineWithoutComment);
        }
        return result.toString();
    }

    /**
     * This method is invoked to notify the panel about usage of Knockout
     * in the inspected page.
     * 
     * @param knockoutVersion version of Knockout used by the inspected page
     * or {@code null} when the page is not using Knockout
     */
    @NbBundle.Messages({
        "# {0} - Knockout version",
        "UnusedBindingsPanel.unsupportedVersion=<html><center>Detection of unused bindings<br>is not supported for<br>Knockout version {0}!",
        "UnusedBindingsPanel.unsupportedBrowser=<html><center>Detection of unused bindings<br>is not supported for this browser!<br>Use Chrome or Chromium instead."
    })
    public void setKnockoutVersion(String knockoutVersion) {
        if (knockoutVersion == null) {
            showComponent(findPanel);
        } else {
            BrowserFamilyId browser = pageModel.getPageContext().lookup(BrowserFamilyId.class);
            if (isSupportedBrowser(browser)) {
                if (isSupportedKnockoutVersion(knockoutVersion)) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            RemoteObject remoteObject = pageModel.getWebKit().getRuntime().evaluate("window.NetBeans && NetBeans.unusedBindingsAvailable()"); // NOI18N
                            final boolean found = (remoteObject != null) && "true".equals(remoteObject.getValueAsString()); // NOI18N
                            if (found) {
                                updateData();
                            }
                        }
                    });
                } else {
                    messageLabel.setText(Bundle.UnusedBindingsPanel_unsupportedVersion(knockoutVersion));
                    showComponent(messageLabel);
                }
            } else {
                messageLabel.setText(Bundle.UnusedBindingsPanel_unsupportedBrowser());
                showComponent(messageLabel);
            }
        }
    }

    /**
     * Determines whether the specified browser supports the detection
     * of unused bindings or not.
     * 
     * @param browser family ID of the browser.
     * @return {@code true} when the specified browser supports the detection
     * on unused bindings, returns {@code false} otherwise.
     */
    private boolean isSupportedBrowser(BrowserFamilyId browser) {
        return (browser != null &&
                (browser == BrowserFamilyId.CHROME ||
                browser == BrowserFamilyId.CHROMIUM ||
                browser == BrowserFamilyId.ANDROID));
    }

    /**
     * Determines whether the specified version of Knockout supports
     * the detection of unused bindings or not.
     * 
     * @param version version of Knockout.
     * @return {@code true} when the specified version of Knockout supports
     * the detection of unused bindings, returns {@code false} otherwise.
     */
    private boolean isSupportedKnockoutVersion(String version) {
        return (version != null) && !version.startsWith("2.") && !version.startsWith("1."); // NOI18N
    }

    /**
     * Shows the specified component in this panel.
     * 
     * @param component component to show in the panel.
     */
    void showComponent(JComponent component) {
        if (component.getParent() != this) {
            removeAll();
            add(component);
            revalidate();
            repaint();
        }
    }

    /**
     * Updates the unused binding information shown in the panel.
     */
    @NbBundle.Messages({
        "UnusedBindingsPanel.noUnusedBindings=<No Unused Bindings>"
    })
    void updateData() {
        RemoteObject remoteObject = pageModel.getWebKit().getRuntime().evaluate("NetBeans.unusedBindings()"); // NOI18N
        String json = remoteObject.getValueAsString();
        Map<String,Map<Integer,UnusedBinding>> unusedBindings = parse(json);
        rootNode.update(unusedBindings);
        final JComponent componentToShow;
        if (unusedBindings.isEmpty()) {
            messageLabel.setText(Bundle.UnusedBindingsPanel_noUnusedBindings());
            componentToShow = messageLabel;
        } else {
            componentToShow = treeView;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                dataPanel.remove(componentToShow == messageLabel ? treeView : messageLabel);
                dataPanel.add(componentToShow);
                showComponent(dataPanel);
                dataPanel.revalidate();
                dataPanel.repaint();
            }
        });
    }

    /**
     * Parses the information about unused bindings.
     * 
     * @param json information about unused bindings (in JSON string).
     * @return information about unused bindings {@code (name -> (id -> binding))} map.
     */
    private Map<String,Map<Integer,UnusedBinding>> parse(String json) {
        Map<String,Map<Integer,UnusedBinding>> map = new HashMap<String,Map<Integer,UnusedBinding>>();
        try {
            JSONArray array = (JSONArray)new JSONParser().parse(json);
            for (Object o : array) {
                JSONObject jsonBinding = (JSONObject)o;
                String name = (String)jsonBinding.get("name"); // NOI18N
                if (ignoreUnusedBinding(name)) {
                    continue;
                }
                int id = ((Number)jsonBinding.get("id")).intValue();
                UnusedBinding binding = new UnusedBinding(id, name,
                    (String)jsonBinding.get("nodeTagName"), // NOI18N
                    (String)jsonBinding.get("nodeId"), // NOI18N
                    (String)jsonBinding.get("nodeClasses"), // NOI18N
                    (Boolean)jsonBinding.get("nodeRemoved"), // NOI18N
                    pageModel
                );
                Map<Integer,UnusedBinding> innerMap = map.get(name);
                if (innerMap == null) {
                    innerMap = new HashMap<Integer,UnusedBinding>();
                    map.put(name, innerMap);
                }
                innerMap.put(id, binding);
            }
        } catch (ParseException pex) {
            Logger.getLogger(UnusedBindingsPanel.class.getName()).log(Level.INFO, null, pex);
        }
        return map;
    }

    /**
     * Determines whether the unused binding with the specified name should
     * be ignored.
     * 
     * @param name name of the unused binding.
     * @return {@code true} if the unused binding with the specified name
     * should be ignored, returns {@code false} otherwise.
     */
    private boolean ignoreUnusedBinding(String name) {
        // Ignore Knockout's implementation details like _ko_property_writers
        return name.startsWith("_ko_"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        findPanel = new javax.swing.JPanel();
        findButton = new javax.swing.JButton();
        findLabel = new javax.swing.JLabel();
        dataPanel = new javax.swing.JPanel();
        refreshPanel = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

        findPanel.setBackground(UIManager.getColor("Tree.background"));

        org.openide.awt.Mnemonics.setLocalizedText(findButton, org.openide.util.NbBundle.getMessage(UnusedBindingsPanel.class, "UnusedBindingsPanel.findButton.text")); // NOI18N
        findButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(findLabel, org.openide.util.NbBundle.getMessage(UnusedBindingsPanel.class, "UnusedBindingsPanel.findLabel.text")); // NOI18N

        javax.swing.GroupLayout findPanelLayout = new javax.swing.GroupLayout(findPanel);
        findPanel.setLayout(findPanelLayout);
        findPanelLayout.setHorizontalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(findPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(findButton)
                    .addComponent(findLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        findPanelLayout.setVerticalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(findPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(findButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dataPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(UnusedBindingsPanel.class, "UnusedBindingsPanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout refreshPanelLayout = new javax.swing.GroupLayout(refreshPanel);
        refreshPanel.setLayout(refreshPanelLayout);
        refreshPanelLayout.setHorizontalGroup(
            refreshPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(refreshPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(refreshButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        refreshPanelLayout.setVerticalGroup(
            refreshPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(refreshPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(refreshButton)
                .addContainerGap())
        );

        dataPanel.add(refreshPanel, java.awt.BorderLayout.PAGE_END);

        messageLabel.setBackground(UIManager.getColor("Tree.background"));
        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        messageLabel.setEnabled(false);
        messageLabel.setOpaque(true);

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        preparePage();
    }//GEN-LAST:event_findButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        RP.post(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        });
    }//GEN-LAST:event_refreshButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dataPanel;
    private javax.swing.JButton findButton;
    private javax.swing.JLabel findLabel;
    private javax.swing.JPanel findPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel refreshPanel;
    // End of variables declaration//GEN-END:variables
}
