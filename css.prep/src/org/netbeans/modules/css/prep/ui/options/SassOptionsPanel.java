/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.ui.options;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.css.prep.sass.SassCli;
import org.netbeans.modules.css.prep.util.FileUtils;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

@OptionsPanelController.Keywords(keywords={"css", "preprocessors", "sass", "#CssPrepOptionsPanel.keywords.preprocessing"},
        location=CssPreprocessorsUI.OPTIONS_CATEGORY, tabTitle="#CssPrepOptionsPanel.name")
public final class SassOptionsPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(SassOptionsPanel.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public SassOptionsPanel() {
        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - long script name",
        "# {1} - short script name",
        "SassOptionsPanel.path.hint=Full path of Sass executable (typically {0} or {1}).",
    })
    private void init() {
        String[] executableNames = SassCli.getExecutableNames();
        sassPathHintLabel.setText(Bundle.SassOptionsPanel_path_hint(executableNames[0], executableNames[1]));

        // listeners
        sassPathTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
        DefaultItemListener defaultItemListener = new DefaultItemListener();
        sassOutputOnErrorCheckBox.addItemListener(defaultItemListener);
        sassDebugCheckBox.addItemListener(defaultItemListener);
    }

    public String getSassPath() {
        return sassPathTextField.getText();
    }

    public void setSassPath(String path) {
        sassPathTextField.setText(path);
    }

    public boolean getSassOutputOnError() {
        return sassOutputOnErrorCheckBox.isSelected();
    }

    public void setSassOutputOnError(boolean outputOnError) {
        sassOutputOnErrorCheckBox.setSelected(outputOnError);
    }

    public boolean getSassDebug() {
        return sassDebugCheckBox.isSelected();
    }

    public void setSassDebug(boolean debug) {
        sassDebugCheckBox.setSelected(debug);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sassPathLabel = new JLabel();
        sassPathTextField = new JTextField();
        sassPathBrowseButton = new JButton();
        sassPathSearchButton = new JButton();
        sassPathHintLabel = new JLabel();
        installSassLabel = new JLabel();
        sassOutputOnErrorCheckBox = new JCheckBox();
        sassDebugCheckBox = new JCheckBox();

        sassPathLabel.setLabelFor(sassPathTextField);
        Mnemonics.setLocalizedText(sassPathLabel, NbBundle.getMessage(SassOptionsPanel.class, "SassOptionsPanel.sassPathLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(sassPathBrowseButton, NbBundle.getMessage(SassOptionsPanel.class, "SassOptionsPanel.sassPathBrowseButton.text")); // NOI18N
        sassPathBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sassPathBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(sassPathSearchButton, NbBundle.getMessage(SassOptionsPanel.class, "SassOptionsPanel.sassPathSearchButton.text")); // NOI18N
        sassPathSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sassPathSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(sassPathHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(installSassLabel, NbBundle.getMessage(SassOptionsPanel.class, "SassOptionsPanel.installSassLabel.text")); // NOI18N
        installSassLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                installSassLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                installSassLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(sassOutputOnErrorCheckBox, NbBundle.getMessage(SassOptionsPanel.class, "SassOptionsPanel.sassOutputOnErrorCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(sassDebugCheckBox, NbBundle.getMessage(SassOptionsPanel.class, "SassOptionsPanel.sassDebugCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sassPathLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sassPathHintLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(installSassLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sassPathTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sassPathBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sassPathSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sassOutputOnErrorCheckBox)
                    .addComponent(sassDebugCheckBox))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {sassPathBrowseButton, sassPathSearchButton});

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sassPathLabel)
                    .addComponent(sassPathBrowseButton)
                    .addComponent(sassPathSearchButton)
                    .addComponent(sassPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sassPathHintLabel)
                    .addComponent(installSassLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sassOutputOnErrorCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sassDebugCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("SassOptionsPanel.browse.title=Select Sass")
    private void sassPathBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sassPathBrowseButtonActionPerformed
        File file = new FileChooserBuilder(SassOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.SassOptionsPanel_browse_title())
                .showOpenDialog();
        if (file != null) {
            sassPathTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_sassPathBrowseButtonActionPerformed

    @NbBundle.Messages("SassOptionsPanel.executable.notFound=No Sass executable found.")
    private void sassPathSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sassPathSearchButtonActionPerformed
        List<String> sassPaths = FileUtils.findFileOnUsersPath(SassCli.getExecutableNames());
        if (sassPaths.isEmpty()) {
            StatusDisplayer.getDefault().setStatusText(Bundle.SassOptionsPanel_executable_notFound());
        } else {
            sassPathTextField.setText(sassPaths.get(0));
        }
    }//GEN-LAST:event_sassPathSearchButtonActionPerformed

    private void installSassLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_installSassLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_installSassLabelMouseEntered

    private void installSassLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_installSassLabelMousePressed
        try {
            URL url = new URL("http://sass-lang.com/"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_installSassLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel installSassLabel;
    private JCheckBox sassDebugCheckBox;
    private JCheckBox sassOutputOnErrorCheckBox;
    private JButton sassPathBrowseButton;
    private JLabel sassPathHintLabel;
    private JLabel sassPathLabel;
    private JButton sassPathSearchButton;
    private JTextField sassPathTextField;
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
