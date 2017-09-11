/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.pull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.docker.api.DockerRegistryImage;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerAction;
import org.openide.awt.HtmlRenderer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerHubSearchPanel extends javax.swing.JPanel {

    private static final int SEARCH_DELAY = 1000;

    private final DockerInstance instance;

    private final JButton pullButton;

    private final AtomicReference<String> searchTerm = new AtomicReference<>();

    private final RequestProcessor.Task searchTask = RequestProcessor.getDefault().create(new Runnable() {
        @Override
        public void run() {
            search(searchTerm.get());
        }
    });

    /**
     * Creates new form DockerPullPanel
     */
    public DockerHubSearchPanel(DockerInstance instance, JButton pullButton) {
        this.instance = instance;
        this.pullButton = pullButton;

        initComponents();

        this.pullButton.setEnabled(false);
        imageListScrollPane.setViewportView(null);

        HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
        renderer.setHtml(true);
        this.imageList.setCellRenderer(renderer);
        this.imageList.addListSelectionListener(new SelectionListener());
        this.searchTextField.getDocument().addDocumentListener(new SearchListener());
    }

    private void search(final String searchTerm) {
        assert !SwingUtilities.isEventDispatchThread();
        assert searchTerm != null && !searchTerm.isEmpty();

        DockerAction facade = new DockerAction(instance);
        final List<DockerRegistryImage> images = facade.search(searchTerm);

        final List<DockerHubImageItem> fresh = new ArrayList<>(images.size());
        for (DockerRegistryImage info : images) {
            fresh.add(new DockerHubImageItem(info));
        }
        Collections.sort(fresh);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!searchTerm.equals(DockerHubSearchPanel.this.searchTerm.get())) {
                    // there is another search already running
                    return;
                }
                DefaultListModel model = new DefaultListModel();
                for (DockerHubImageItem image : fresh) {
                    model.addElement(image);
                }

                if (model.isEmpty()) {
                    messageLabel.setText(NbBundle.getMessage(
                            DockerHubSearchPanel.class, "DockerHubSearchPanel.noImagesFound"));
                    imageListScrollPane.setViewportView(messagePanel);
                } else {
                    imageListScrollPane.setViewportView(imageList);
                    imageList.clearSelection();
                    imageList.setModel(model);
                }

                repaint();
                revalidate();
            }
        });
    }

    public String getImage() {
        String toPull;
        DockerHubImageItem selected = imageList.getSelectedValue();
        if (selected != null) {
            toPull = selected.getHubImage().getName();
        } else {
            toPull = searchTextField.getText().trim();
        }
        toPull = appendLatestTag(toPull);
        return toPull;
    }

    public static String appendLatestTag(String image) {
        String ret = image;
        if (!image.contains(":") && !image.contains("@")) { // NOI18N
            ret += ":latest"; // NOI18N
        }
        return ret;
    }

    private class SearchListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        private void update(DocumentEvent e) {
            String text = searchTextField.getText().trim();
            pullButton.setEnabled(!text.isEmpty());
            imageList.clearSelection();

            if (text.isEmpty()) {
                searchTask.cancel();
                imageListScrollPane.setViewportView(null);
                resultTextField.setText("");
            } else {
                messageLabel.setText(NbBundle.getMessage(
                        DockerHubSearchPanel.class, "DockerHubSearchPanel.searching"));
                imageListScrollPane.setViewportView(messagePanel);
                searchTerm.set(text);
                searchTask.schedule(SEARCH_DELAY);
                resultTextField.setText(getImage());
            }

            repaint();
            revalidate();
        }
    }

    private class SelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }

            resultTextField.setText(getImage());
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

        messagePanel = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        imageListScrollPane = new javax.swing.JScrollPane();
        imageList = new javax.swing.JList<>();
        searchTextLabel = new javax.swing.JLabel();
        imageListLabel = new javax.swing.JLabel();
        resultTextField = new javax.swing.JTextField();
        resultLabel = new javax.swing.JLabel();

        messagePanel.setLayout(new java.awt.GridBagLayout());

        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        messagePanel.add(messageLabel, new java.awt.GridBagConstraints());

        imageList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        imageListScrollPane.setViewportView(imageList);

        searchTextLabel.setLabelFor(searchTextField);
        org.openide.awt.Mnemonics.setLocalizedText(searchTextLabel, org.openide.util.NbBundle.getMessage(DockerHubSearchPanel.class, "DockerHubSearchPanel.searchTextLabel.text")); // NOI18N

        imageListLabel.setLabelFor(imageList);
        org.openide.awt.Mnemonics.setLocalizedText(imageListLabel, org.openide.util.NbBundle.getMessage(DockerHubSearchPanel.class, "DockerHubSearchPanel.imageListLabel.text")); // NOI18N

        resultTextField.setEditable(false);
        resultTextField.setFocusable(false);

        resultLabel.setLabelFor(resultTextField);
        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(DockerHubSearchPanel.class, "DockerHubSearchPanel.resultLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE)
                    .addComponent(searchTextField)
                    .addComponent(resultTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchTextLabel)
                            .addComponent(imageListLabel)
                            .addComponent(resultLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<org.netbeans.modules.docker.ui.pull.DockerHubImageItem> imageList;
    private javax.swing.JLabel imageListLabel;
    private javax.swing.JScrollPane imageListScrollPane;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JTextField resultTextField;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JLabel searchTextLabel;
    // End of variables declaration//GEN-END:variables
}
