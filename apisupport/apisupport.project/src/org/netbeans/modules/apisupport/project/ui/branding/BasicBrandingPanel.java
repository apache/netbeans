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
package org.netbeans.modules.apisupport.project.ui.branding;

import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import static java.util.stream.Collectors.toMap;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents <em>Application</em> panel in branding editor.
 *
 * @author Radek Matous, S. Aubrecht
 */
final class BasicBrandingPanel extends AbstractBrandingPanel {

    private final SortedMap<Integer, URL> iconSources;
    private boolean titleValueModified;

    public BasicBrandingPanel(BrandingModel model) {
        super(NbBundle.getMessage(BasicBrandingPanel.class, "LBL_BasicTab"), model); //NOI18N
        this.iconSources = IntStream.of(16, 32, 48, 256, 512, 1024).boxed()
                .collect(toMap(size -> size, model::getIconSource,
                        (a, b) -> a, TreeMap::new));

        initComponents();
        final DefaultListModel<Map.Entry<Integer, URL>> listModel = new DefaultListModel<>();
        iconSources.entrySet().forEach(listModel::addElement);
        sizeList.setModel(listModel);
        sizeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                final JLabel ret = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                final Integer size = ((Map.Entry<Integer, URL>) value).getKey();
                ret.setText(String.format("Application Icon (%dx%d)", size, size));
                return ret;
            }
        });
        sizeList.setSelectedIndex(0);

        refresh();
        checkValidity();
        DocumentListener textFieldChangeListener = new UIUtil.DocumentAdapter() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
                setModified();
                titleValueModified = true;
            }
        };
        titleValue.getDocument().addDocumentListener(textFieldChangeListener);
        titleValueModified = false;
    }

    protected void checkValidity() {
        boolean panelValid = true;

        if (panelValid && titleValue.getText().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(BasicBrandingPanel.class, "ERR_EmptyTitle"));//NOI18N
            panelValid = false;
        }

        if (panelValid) {
            setErrorMessage(null);
        }
        setValid(panelValid);
    }

    void refresh() {
        BrandingModel model = getBranding();
        model.brandingEnabledRefresh();
        model.initTitle(true);
        titleValue.setText(model.getTitle());
        browse.setEnabled(model.isBrandingEnabled());
        titleValue.setEnabled(model.isBrandingEnabled());
    }

    public @Override
    void store() {
        if (titleValueModified) {
            getBranding().setTitle(titleValue.getText());
        }

        iconSources.entrySet()
                .forEach(e -> getBranding().setIconSource(e.getKey(), e.getValue()));
    }

    private void setPreview(final URL res) {
        try {
            final BufferedImage img = ImageIO.read(res);
            ((ImagePreview) preview).setImage(img);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        title = new javax.swing.JLabel();
        titleValue = new javax.swing.JTextField();
        javax.swing.JScrollPane listScrollPane = new javax.swing.JScrollPane();
        sizeList = new javax.swing.JList<>();
        browse = new javax.swing.JButton();
        javax.swing.JScrollPane previewScrollPane = new javax.swing.JScrollPane();
        preview = new ImagePreview();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/branding/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(title, bundle.getString("LBL_AppTitle")); // NOI18N

        titleValue.setColumns(20);

        sizeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                sizeListValueChanged(evt);
            }
        });
        listScrollPane.setViewportView(sizeList);

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(BasicBrandingPanel.class, "CTL_Browse")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout previewLayout = new javax.swing.GroupLayout(preview);
        preview.setLayout(previewLayout);
        previewLayout.setHorizontalGroup(
            previewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1058, Short.MAX_VALUE)
        );
        previewLayout.setVerticalGroup(
            previewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1898, Short.MAX_VALUE)
        );

        previewScrollPane.setViewportView(preview);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleValue))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(browse)
                            .addComponent(previewScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(title)
                    .addComponent(titleValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(browse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE))
                    .addComponent(listScrollPane))
                .addContainerGap())
        );

        title.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicBrandingPanel.class, "ACS_Title")); // NOI18N
        browse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicBrandingPanel.class, "ACS_Browse")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void sizeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_sizeListValueChanged
        setPreview(sizeList.getSelectedValue().getValue());
    }//GEN-LAST:event_sizeListValueChanged

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        final URL res = browseIcon();
        setPreview(res);
        setModified();
        sizeList.getSelectedValue().setValue(res);
    }//GEN-LAST:event_browseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse;
    private javax.swing.JPanel preview;
    private javax.swing.JList<Map.Entry<Integer, URL>> sizeList;
    private javax.swing.JLabel title;
    private javax.swing.JTextField titleValue;
    // End of variables declaration//GEN-END:variables

    private URL browseIcon() {
        URL res = null;
        JFileChooser chooser = UIUtil.getIconFileChooser();
        int ret = chooser.showDialog(this, NbBundle.getMessage(getClass(), "LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                res = Utilities.toURI(file).toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return res;
    }

    private static class ImagePreview extends JPanel {

        private BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.clearRect(0, 0, getWidth(), getHeight());
            g.drawImage(this.image, 0, 0, image.getWidth(), image.getHeight(), this);
        }

        public void setImage(BufferedImage image) {
            this.image = image;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(), image.getHeight());
        }
    }
}
