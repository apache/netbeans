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

package org.netbeans.modules.apisupport.project.ui.branding;

import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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
final class BasicBrandingPanel extends AbstractBrandingPanel  {
    
    private URL iconSource48;
    private URL iconSource32;
    private URL iconSource16;

    private boolean titleValueModified;

    public BasicBrandingPanel(BrandingModel model) {
        super(NbBundle.getMessage(BasicBrandingPanel.class, "LBL_BasicTab"), model); //NOI18N
        initComponents();        
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
        iconSource48 = model.getIconSource(48);
        if (iconSource48 != null) {
            ((ImagePreview)iconPreview48).setImage(new ImageIcon(iconSource48));
        }
        iconSource32 = model.getIconSource(32);
        if (iconSource32 != null) {
            ((ImagePreview)iconPreview32).setImage(new ImageIcon(iconSource32));
        }
        iconSource16 = model.getIconSource(16);
        if (iconSource16 != null) {
            ((ImagePreview)iconPreview16).setImage(new ImageIcon(iconSource16));
        }
        browse16.setEnabled(null != iconSource16 && model.isBrandingEnabled());
        browse32.setEnabled(null != iconSource32 && model.isBrandingEnabled());
        browse48.setEnabled(null != iconSource48 && model.isBrandingEnabled());
        titleValue.setEnabled(model.isBrandingEnabled());
    }
    
    public @Override void store() {
        if (titleValueModified)
            getBranding().setTitle(titleValue.getText());
        getBranding().setIconSource(48, iconSource48);
        getBranding().setIconSource(32, iconSource32);
        getBranding().setIconSource(16, iconSource16);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        title = new javax.swing.JLabel();
        titleValue = new javax.swing.JTextField();
        iconPreview48 = new ImagePreview(48,48);
        browse48 = new javax.swing.JButton();
        icon48 = new javax.swing.JLabel();
        icon16 = new javax.swing.JLabel();
        iconPreview16 = new ImagePreview(16,16);
        browse16 = new javax.swing.JButton();
        icon32 = new javax.swing.JLabel();
        iconPreview32 = new ImagePreview(32,32);
        browse32 = new javax.swing.JButton();
        lblSpacer = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        title.setLabelFor(titleValue);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/branding/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(title, bundle.getString("LBL_AppTitle")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(title, gridBagConstraints);
        title.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_Title")); // NOI18N

        titleValue.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(titleValue, gridBagConstraints);

        iconPreview48.setLabelFor(iconPreview48);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(iconPreview48, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse48, bundle.getString("CTL_Browse")); // NOI18N
        browse48.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse48ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(browse48, gridBagConstraints);
        browse48.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_Browse")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(icon48, bundle.getString("LBL_AppIcon48")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 12);
        add(icon48, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(icon16, bundle.getString("LBL_AppIcon16")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 10, 0, 12);
        add(icon16, gridBagConstraints);

        iconPreview16.setLabelFor(iconPreview48);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 12);
        add(iconPreview16, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse16, bundle.getString("CTL_Browse")); // NOI18N
        browse16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse16ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 0);
        add(browse16, gridBagConstraints);
        browse16.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicBrandingPanel.class, "ACS_Browse")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(icon32, bundle.getString("LBL_AppIcon32")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 12);
        add(icon32, gridBagConstraints);

        iconPreview32.setLabelFor(iconPreview48);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(iconPreview32, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse32, bundle.getString("CTL_Browse")); // NOI18N
        browse32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse32ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(browse32, gridBagConstraints);
        browse32.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicBrandingPanel.class, "ACS_Browse")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        add(lblSpacer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void browse48ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse48ActionPerformed
        iconSource48 = browseIcon( (ImagePreview) iconPreview48);
    }//GEN-LAST:event_browse48ActionPerformed

    private void browse16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse16ActionPerformed
        iconSource16 = browseIcon( (ImagePreview) iconPreview16);
    }//GEN-LAST:event_browse16ActionPerformed

    private void browse32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse32ActionPerformed
        iconSource32 = browseIcon( (ImagePreview) iconPreview32);
    }//GEN-LAST:event_browse32ActionPerformed
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse16;
    private javax.swing.JButton browse32;
    private javax.swing.JButton browse48;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel icon16;
    private javax.swing.JLabel icon32;
    private javax.swing.JLabel icon48;
    private javax.swing.JLabel iconPreview16;
    private javax.swing.JLabel iconPreview32;
    private javax.swing.JLabel iconPreview48;
    private javax.swing.JLabel lblSpacer;
    private javax.swing.JLabel title;
    private javax.swing.JTextField titleValue;
    // End of variables declaration//GEN-END:variables

    private URL browseIcon( ImagePreview preview ) {
        URL res = null;
        JFileChooser chooser = UIUtil.getIconFileChooser();
        int ret = chooser.showDialog(this, NbBundle.getMessage(getClass(), "LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            try {
                res = Utilities.toURI(file).toURL();
                preview.setImage(new ImageIcon(res));
                setModified();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return res;
    }

    static class ImagePreview extends JLabel {
        private ImageIcon image = null;
        private int width;
        private int height;
        ImagePreview(int width, int height){
            this.width = width;
            this.height = height;            
        }
        
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D)g;
            
            if (!isEnabled()) {
                g2d.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
            }
            
            if ((getWidth() >= width) && (getHeight() >= height) && image != null) {
                int x = 0;//(getWidth()/2)-(width/2);
                int y = 0;//(getHeight()/2)-(height/2);
                g.drawImage(image.getImage(),x, y, width, height, this.getBackground(),null);
            }
        }
        
        private void setImage(ImageIcon image) {
            this.image = image;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width, height);
        }
    }
}
