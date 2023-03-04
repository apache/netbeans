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
package org.netbeans.modules.javascript.nodejs.ui.options;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript.nodejs.exec.ExpressExecutable;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.ui.NodeJsPathPanel;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

@OptionsPanelController.Keywords(keywords = {"#KW.NodeJsOptionsPanel"}, location = "Html5", tabTitle = "Node.js")
public final class NodeJsOptionsPanel extends JPanel implements ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(NodeJsOptionsPanel.class.getName());

    final NodeJsPathPanel nodePanel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private NodeJsOptionsPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();

        nodePanel = new NodeJsPathPanel();

        init();
    }

    public static NodeJsOptionsPanel create() {
        NodeJsOptionsPanel panel = new NodeJsOptionsPanel();
        panel.nodePanel.addChangeListener(panel);
        return panel;
    }

    @NbBundle.Messages({
        "# {0} - npm file name",
        "NodeJsOptionsPanel.npm.hint=Full path of npm file (typically {0}).",
        "# {0} - express file name",
        "NodeJsOptionsPanel.express.hint=Full path of express file (typically {0}).",
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        npmHintLabel.setText(Bundle.NodeJsOptionsPanel_npm_hint(NpmExecutable.NPM_NAME));
        expressHintLabel.setText(Bundle.NodeJsOptionsPanel_express_hint(ExpressExecutable.EXPRESS_NAME));
        nodePanelHolder.add(nodePanel, BorderLayout.CENTER);
        DefaultItemListener defaultItemListener = new DefaultItemListener();
        stopAtFirstLineCheckBox.addItemListener(defaultItemListener);
        liveEditCheckBox.addItemListener(defaultItemListener);
        npmIgnoreNodeModulesCheckBox.addItemListener(defaultItemListener);
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        npmTextField.getDocument().addDocumentListener(defaultDocumentListener);
        expressTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void setError(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public String getNode() {
        return nodePanel.getNode();
    }

    public void setNode(String node) {
        nodePanel.setNode(node);
    }

    @CheckForNull
    public String getNodeSources() {
        return nodePanel.getNodeSources();
    }

    public void setNodeSources(String nodeSources) {
        nodePanel.setNodeSources(nodeSources);
    }

    public boolean isStopAtFirstLine() {
        return stopAtFirstLineCheckBox.isSelected();
    }

    public void setStopAtFirstLine(boolean stopAtFirstLine) {
        stopAtFirstLineCheckBox.setSelected(stopAtFirstLine);
    }

    public boolean isLiveEdit() {
        return liveEditCheckBox.isSelected();
    }

    public void setLiveEdit(boolean liveEdit) {
        liveEditCheckBox.setSelected(liveEdit);
    }

    public String getNpm() {
        return npmTextField.getText();
    }

    public void setNpm(String npm) {
        npmTextField.setText(npm);
    }

    public boolean isNpmIgnoreNodeModules() {
        return npmIgnoreNodeModulesCheckBox.isSelected();
    }

    public void setNpmIgnoreNodeModules(boolean npmIgnoreNodeModules) {
        npmIgnoreNodeModulesCheckBox.setSelected(npmIgnoreNodeModules);
    }

    public String getExpress() {
        return expressTextField.getText();
    }

    public void setExpress(String express) {
        expressTextField.setText(express);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodePanelHolder = new JPanel();
        debuggingLabel = new JLabel();
        debuggingSeparator = new JSeparator();
        stopAtFirstLineCheckBox = new JCheckBox();
        liveEditCheckBox = new JCheckBox();
        liveEditInfo1Label = new JLabel();
        liveEditInfo2Label = new JLabel();
        npmHeaderLabel = new JLabel();
        npmSeparator = new JSeparator();
        npmLabel = new JLabel();
        npmTextField = new JTextField();
        npmBrowseButton = new JButton();
        npmSearchButton = new JButton();
        npmHintLabel = new JLabel();
        npmIgnoreNodeModulesCheckBox = new JCheckBox();
        expressHeaderLabel = new JLabel();
        expressSeparator = new JSeparator();
        expressLabel = new JLabel();
        expressTextField = new JTextField();
        expressBrowseButton = new JButton();
        expressSearchButton = new JButton();
        expressHintLabel = new JLabel();
        expressInstallLabel = new JLabel();
        errorLabel = new JLabel();

        nodePanelHolder.setLayout(new BorderLayout());

        Mnemonics.setLocalizedText(debuggingLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.debuggingLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(stopAtFirstLineCheckBox, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.stopAtFirstLineCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(liveEditCheckBox, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.liveEditCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(liveEditInfo1Label, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.liveEditInfo1Label.text")); // NOI18N

        Mnemonics.setLocalizedText(liveEditInfo2Label, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.liveEditInfo2Label.text")); // NOI18N

        Mnemonics.setLocalizedText(npmHeaderLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmHeaderLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(npmLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(npmBrowseButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmBrowseButton.text")); // NOI18N
        npmBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                npmBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(npmSearchButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmSearchButton.text")); // NOI18N
        npmSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                npmSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(npmHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(npmIgnoreNodeModulesCheckBox, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmIgnoreNodeModulesCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(expressHeaderLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.expressHeaderLabel.text")); // NOI18N

        expressLabel.setLabelFor(expressTextField);
        Mnemonics.setLocalizedText(expressLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.expressLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(expressBrowseButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.expressBrowseButton.text")); // NOI18N
        expressBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                expressBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(expressSearchButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.expressSearchButton.text")); // NOI18N
        expressSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                expressSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(expressHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(expressInstallLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.expressInstallLabel.text")); // NOI18N
        expressInstallLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                expressInstallLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                expressInstallLabelMouseEntered(evt);
            }
        });

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(npmLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(npmTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(npmBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(npmSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(npmHintLabel)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(expressHeaderLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(expressSeparator))
            .addGroup(layout.createSequentialGroup()
                .addComponent(expressLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(expressHintLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(expressInstallLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(expressTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(expressBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(expressSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(npmHeaderLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(npmSeparator))
            .addComponent(nodePanelHolder, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(debuggingLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(debuggingSeparator))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(liveEditCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(liveEditInfo2Label)
                            .addComponent(liveEditInfo1Label)))
                    .addComponent(stopAtFirstLineCheckBox)
                    .addComponent(npmIgnoreNodeModulesCheckBox))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {npmBrowseButton, npmSearchButton});

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(nodePanelHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(debuggingLabel)
                    .addComponent(debuggingSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopAtFirstLineCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(liveEditCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(liveEditInfo1Label)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(liveEditInfo2Label)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(npmHeaderLabel)
                    .addComponent(npmSeparator, GroupLayout.PREFERRED_SIZE, 8, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(npmLabel)
                    .addComponent(npmTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(npmSearchButton)
                    .addComponent(npmBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(npmHintLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(npmIgnoreNodeModulesCheckBox)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(expressHeaderLabel)
                    .addComponent(expressSeparator, GroupLayout.PREFERRED_SIZE, 8, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(expressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(expressBrowseButton)
                    .addComponent(expressSearchButton)
                    .addComponent(expressLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(expressHintLabel)
                    .addComponent(expressInstallLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("NodeJsOptionsPanel.npm.browse.title=Select npm")
    private void npmBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_npmBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(NodeJsOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.NodeJsOptionsPanel_npm_browse_title())
                .showOpenDialog();
        if (file != null) {
            npmTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_npmBrowseButtonActionPerformed

    @NbBundle.Messages("NodeJsOptionsPanel.npm.none=No npm executable was found.")
    private void npmSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_npmSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        for (String npm : FileUtils.findFileOnUsersPath(NpmExecutable.NPM_NAME)) {
            npmTextField.setText(new File(npm).getAbsolutePath());
            return;
        }
        // no npm found
        StatusDisplayer.getDefault().setStatusText(Bundle.NodeJsOptionsPanel_npm_none());
    }//GEN-LAST:event_npmSearchButtonActionPerformed

    @NbBundle.Messages("NodeJsOptionsPanel.express.browse.title=Select Express")
    private void expressBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_expressBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(NodeJsOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.NodeJsOptionsPanel_express_browse_title())
                .showOpenDialog();
        if (file != null) {
            expressTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_expressBrowseButtonActionPerformed

    @NbBundle.Messages("NodeJsOptionsPanel.express.none=No Express executable was found.")
    private void expressSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_expressSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        for (String express : FileUtils.findFileOnUsersPath(ExpressExecutable.EXPRESS_NAME)) {
            expressTextField.setText(new File(express).getAbsolutePath());
            return;
        }
        // no express found
        StatusDisplayer.getDefault().setStatusText(Bundle.NodeJsOptionsPanel_express_none());
    }//GEN-LAST:event_expressSearchButtonActionPerformed

    private void expressInstallLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_expressInstallLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_expressInstallLabelMouseEntered

    private void expressInstallLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_expressInstallLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://expressjs.com/starter/generator.html")); // NOI18N
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_expressInstallLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel debuggingLabel;
    private JSeparator debuggingSeparator;
    private JLabel errorLabel;
    private JButton expressBrowseButton;
    private JLabel expressHeaderLabel;
    private JLabel expressHintLabel;
    private JLabel expressInstallLabel;
    private JLabel expressLabel;
    private JButton expressSearchButton;
    private JSeparator expressSeparator;
    private JTextField expressTextField;
    private JCheckBox liveEditCheckBox;
    private JLabel liveEditInfo1Label;
    private JLabel liveEditInfo2Label;
    private JPanel nodePanelHolder;
    private JButton npmBrowseButton;
    private JLabel npmHeaderLabel;
    private JLabel npmHintLabel;
    private JCheckBox npmIgnoreNodeModulesCheckBox;
    private JLabel npmLabel;
    private JButton npmSearchButton;
    private JSeparator npmSeparator;
    private JTextField npmTextField;
    private JCheckBox stopAtFirstLineCheckBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }

    }

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }

    }

}
