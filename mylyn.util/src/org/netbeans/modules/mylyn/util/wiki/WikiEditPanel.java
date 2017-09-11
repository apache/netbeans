/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mylyn.util.wiki;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.mylyn.util.WikiPanel;
import org.netbeans.modules.mylyn.util.WikiUtils;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class WikiEditPanel extends WikiPanel {

    private String wikiFormatText;
    private String wikiLanguage;
    private String htmlFormatText;
    private final boolean switchable;
    private boolean editing;
    private static final ImageIcon ICON_EDIT = ImageUtilities.loadImageIcon("org/netbeans/modules/mylyn/util/resources/edit.png", true); //NOI18N
    private static final ImageIcon ICON_PREVIEW = ImageUtilities.loadImageIcon("org/netbeans/modules/mylyn/util/resources/preview.png", true); //NOI18N
    private static final Logger LOG = Logger.getLogger("org.netbeans.mylyn.utils.WikiEditPanel"); //NOI18N
    private static final String CONTENT_HTML = "text/html"; //NOI18N
    private static final String CONTENT_PLAIN = "text/plain"; //NOI18N

    /**
     * Creates new form WikiEditPanel
     */
    public WikiEditPanel(String wikiLanguage, boolean editing, boolean switchable) {
        this.wikiLanguage = wikiLanguage;
        this.switchable = switchable;
        this.wikiFormatText = "";
        this.htmlFormatText = "";
        initComponents();
        pnlButtons.setVisible(switchable);
        textCode.getDocument().addDocumentListener(new RevalidatingListener());
        textPreview.getDocument().addDocumentListener(new RevalidatingListener());
        textCode.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                makeCaretVisible(textCode);
            }
        });
        textCode.getDocument().addDocumentListener(new EnablingListener());
        // A11Y - Issues 163597 and 163598
        UIUtils.fixFocusTraversalKeys(textCode);
        UIUtils.issue163946Hack(scrollCode);

        Spellchecker.register(textCode);
        textPreview.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        setEditing(editing);
    }

    @Override
    public String getWikiFormatText() {
        return textCode.getText();
    }

    @Override
    public void setWikiFormatText(String wikiFormatText) {
        this.wikiFormatText = wikiFormatText;
        String htmlText = 
            Boolean.getBoolean("bugtracking.noWikiStyle") 
                ? null 
                : WikiUtils.getHtmlFormatText(wikiFormatText, wikiLanguage);

        if (htmlText != null) {
            this.htmlFormatText = htmlText;
            textPreview.setContentType(CONTENT_HTML);
        } else {
            this.htmlFormatText = wikiFormatText;
            textPreview.setContentType(CONTENT_PLAIN);
        }
        textCode.setText(wikiFormatText);
        textPreview.setText(htmlFormatText);
        this.repaint();
    }

    @Override
    public JLabel getWarningLabel() {
        return lblWarning;
    }

    @Override
    public void appendCodeText(String codeToAppend) {
        setWikiFormatText(getWikiFormatText() + codeToAppend);
        setEditing(true);
    }

    @Override
    public void clear() {
        this.wikiFormatText = "";
        this.htmlFormatText = "";
        textCode.setText(wikiFormatText);
        textPreview.setText(htmlFormatText);
        this.repaint();
    }

    @Override
    public void registerHighlights(JTextPane wikiPreviewPane) { }

    @Override
    public JTextPane getPreviewPane() {
        return textPreview;
    }

    @Override
    public JTextPane getCodePane() {
        return textCode;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlButtons = new javax.swing.JPanel();
        btnEditPreview = new javax.swing.JButton();
        lblWarning = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        scrollPreview = new javax.swing.JScrollPane();
        textPreview = new javax.swing.JTextPane();
        scrollCode = new javax.swing.JScrollPane();
        textCode = new javax.swing.JTextPane();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        pnlButtons.setOpaque(false);

        btnEditPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mylyn/util/resources/edit.png"))); // NOI18N
        btnEditPreview.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        btnEditPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditPreviewActionPerformed(evt);
            }
        });

        lblWarning.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lblWarning, NbBundle.getMessage(WikiEditPanel.class, "WikiEditPanel.lblWarning.text")); // NOI18N

        javax.swing.GroupLayout pnlButtonsLayout = new javax.swing.GroupLayout(pnlButtons);
        pnlButtons.setLayout(pnlButtonsLayout);
        pnlButtonsLayout.setHorizontalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlButtonsLayout.createSequentialGroup()
                .addComponent(btnEditPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(lblWarning, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlButtonsLayout.setVerticalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlButtonsLayout.createSequentialGroup()
                .addComponent(btnEditPreview)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblWarning))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(pnlButtons, gridBagConstraints);

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        scrollPreview.setBorder(null);
        scrollPreview.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        textPreview.setEditable(false);
        textPreview.setBorder(javax.swing.BorderFactory.createEmptyBorder()
        );
        textPreview.setContentType("text/html"); // NOI18N
        textPreview.setText(htmlFormatText);
        textPreview.setMargin(new java.awt.Insets(0, 3, 3, 3));
        scrollPreview.setViewportView(textPreview);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 11, 0);
        jPanel1.add(scrollPreview, gridBagConstraints);

        scrollCode.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        textCode.setText(wikiFormatText);
        scrollCode.setViewportView(textCode);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(scrollCode, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditPreviewActionPerformed
        setEditing(!editing);
    }//GEN-LAST:event_btnEditPreviewActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditPreview;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblWarning;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JScrollPane scrollCode;
    private javax.swing.JScrollPane scrollPreview;
    private javax.swing.JTextPane textCode;
    private javax.swing.JTextPane textPreview;
    // End of variables declaration//GEN-END:variables

    private void setEditing(boolean editing) {

        if (editing) {
            btnEditPreview.setEnabled(textCode.getDocument().getLength() != 0);
            btnEditPreview.setIcon(ICON_PREVIEW);
            btnEditPreview.setToolTipText(NbBundle.getMessage(WikiEditPanel.class, "TOOL_Preview"));
            textCode.setVisible(true);
            scrollCode.setVisible(true);

            textPreview.setVisible(false);
            scrollPreview.setVisible(false);

            textCode.requestFocus();
        } else {
            btnEditPreview.setEnabled(true);
            btnEditPreview.setIcon(ICON_EDIT);
            btnEditPreview.setToolTipText(NbBundle.getMessage(WikiEditPanel.class, "TOOL_Edit"));
            textPreview.setVisible(true);
            scrollPreview.setVisible(true);

            textCode.setVisible(false);
            scrollCode.setVisible(false);
            if (this.editing != editing) {
                setWikiFormatText(textCode.getText());
            }
        }
        this.editing = editing;
        this.revalidate();
        this.repaint();
    }

    private void makeCaretVisible(JTextComponent textComponent) {
        int pos = textComponent.getCaretPosition();
        try {
            Rectangle rec = textComponent.getUI().modelToView(textComponent, pos);
            if (rec != null) {
                Point p = SwingUtilities.convertPoint(textComponent, rec.x, rec.y, this);
                scrollRectToVisible(new Rectangle(p.x, p.y, rec.width, rec.height));
            }
        } catch (BadLocationException blex) {
            LOG.log(Level.INFO, blex.getMessage(), blex);
        }
    }

    private class EnablingListener implements DocumentListener {

        public EnablingListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkButtonEnabled(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkButtonEnabled(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkButtonEnabled(e);
        }

        private void checkButtonEnabled(DocumentEvent e) {
            btnEditPreview.setEnabled(e.getDocument().getLength() != 0);
        }
    }

    private class RevalidatingListener implements DocumentListener, Runnable {

        private boolean ignoreUpdate;

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (ignoreUpdate) {
                return;
            }
            ignoreUpdate = true;
            EventQueue.invokeLater(this);
        }

        @Override
        public void run() {
            revalidate();
            repaint();
            ignoreUpdate = false;
        }
    }
}
