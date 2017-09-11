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
import java.awt.Image;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.BrandingSupport.BrandedFile;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents <em>Splash branding parameters</em> panel in branding editor.
 *
 * @author Radek Matous, S. Aubrecht
 */
public final class SplashBrandingPanel extends AbstractBrandingPanel {
    
    private final @NonNull SplashComponentPreview splashImage;
    private final @NonNull JFormattedTextField fontSize;
    private final @NonNull JFormattedTextField runningTextBounds;
    private final @NonNull JFormattedTextField progressBarBounds;
    private final @NonNull SplashUISupport.ColorComboBox textColor;
    private final @NonNull SplashUISupport.ColorComboBox barColor;
    private final @NonNull SplashUISupport.ColorComboBox edgeColor;
    private final @NonNull SplashUISupport.ColorComboBox cornerColor;
    
    private URL splashSource;

    public SplashBrandingPanel(BrandingModel model) {
        super(NbBundle.getMessage(BasicBrandingPanel.class, "LBL_SplashTab"), model); //NOI18N
        
        splashImage =new SplashComponentPreview();
        fontSize = SplashUISupport.getIntegerField();
        runningTextBounds = SplashUISupport.getBoundsField();
        progressBarBounds = SplashUISupport.getBoundsField();
        textColor = SplashUISupport.getColorComboBox();
        barColor = SplashUISupport.getColorComboBox();
        edgeColor = SplashUISupport.getColorComboBox();
        cornerColor = SplashUISupport.getColorComboBox();
        splashImage.setDropHandletForProgress(new DragManager.DropHandler(){
            @Override
            public void dragAccepted(Rectangle original, Rectangle afterDrag) {
                progressBarBounds.setValue(afterDrag);
                setModified();
            }            
        });
        
        splashImage.setDropHandletForText(new DragManager.DropHandler(){
            @Override
            public void dragAccepted(Rectangle original, Rectangle afterDrag) {
                runningTextBounds.setValue(afterDrag);
                double ratio = ((double)afterDrag.height)/original.height;
                int size = (int)((((Number)fontSize.getValue()).intValue()*ratio));
                size = (size > 0) ? size : 3;
                fontSize.setValue(size);
                setModified();
            }
        });
        
        initComponents();
        refresh();
        enableDisableComponents();
        
        PropertyChangeListener pL = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() != SplashUISupport.ColorComboBox.PROP_COLOR) {
                    return;
                }
                setModified();
                resetSplashPreview();
            }
        };
        textColor.addPropertyChangeListener(pL);
        barColor.addPropertyChangeListener(pL);
        edgeColor.addPropertyChangeListener(pL);
        cornerColor.addPropertyChangeListener(pL);
        
        fontSize.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (e != null || fontSize.isFocusOwner()) {
                    try {
                        fontSize.commitEdit();
                        ((Number) fontSize.getValue()).intValue();
                        setErrorMessage(null);
                        setValid(true);
                        resetSplashPreview();
                    } catch (ParseException ex) {
                        //user's invalide input
                        setErrorMessage(NbBundle.getMessage(SplashBrandingPanel.class, "ERR_InvalidFontSize")); //NOI18N
                        setValid(false);
                    }
                    setModified();
                }
            }
        });
        
        runningTextBounds.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (e != null || runningTextBounds.isFocusOwner()) {
                    try {
                        runningTextBounds.commitEdit();
                        setErrorMessage(null);
                        setValid(true);
                        resetSplashPreview();
                    } catch (ParseException ex) {
                        //user's invalide input
                        setErrorMessage(NbBundle.getMessage(SplashBrandingPanel.class, "ERR_InvalidTextBounds")); //NOI18N
                        setValid(false);
                    }
                    setModified();
                }
            }
        });
        
        progressBarBounds.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (e != null || progressBarBounds.isFocusOwner()) {
                    try {
                        progressBarBounds.commitEdit();
                        setErrorMessage(null);
                        setValid(true);
                        resetSplashPreview();
                    } catch (ParseException ex) {
                        //user's invalide input
                        setErrorMessage(NbBundle.getMessage(SplashBrandingPanel.class, "ERR_InvalidProgressBarBounds")); //NOI18N
                        setValid(false);
                    }
                    setModified();
                }
            }
        });
        
    }
    
    
    @Override
    public void store() {
        BrandingModel branding = getBranding();
        
        SplashUISupport.setValue(branding.getSplashRunningTextFontSize(), SplashUISupport.numberToString((Number) fontSize.getValue()));
        SplashUISupport.setValue(branding.getSplashRunningTextBounds(), SplashUISupport.boundsToString((Rectangle)runningTextBounds.getValue()));
        SplashUISupport.setValue(branding.getSplashProgressBarBounds(), SplashUISupport.boundsToString((Rectangle)progressBarBounds.getValue()));
        if (textColor.getColor() != null) {
            SplashUISupport.setValue(branding.getSplashRunningTextColor(), SplashUISupport.colorToString(textColor.getColor()));
        }
        if (barColor.getColor() != null) {
            SplashUISupport.setValue(branding.getSplashProgressBarColor(), SplashUISupport.colorToString(barColor.getColor()));
        }
        //these colors below has a little effect on resulting branded splash
        //then user can't adjust it from UI
        //edgeColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarEdgeColor().getValue()));
        //cornerColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarCornerColor().getValue()));
        
        SplashUISupport.setValue(branding.getSplashShowProgressBar(), Boolean.toString(progressBarEnabled.isSelected()));
        BrandedFile splash = branding.getSplash();
        if (splash != null) {
            splash.setBrandingSource(splashSource);
        }
        
        Image image = splashImage.image;
        if (image != null) {
            SplashUISupport.setValue(branding.getSplashWidth(), Integer.toString(image.getWidth(null),10));
            SplashUISupport.setValue(branding.getSplashHeight(), Integer.toString(image.getHeight(null),10));
        }
    }
    
    
    void refresh() {
        BrandingModel branding = getBranding();
        
        fontSize.setValue(SplashUISupport.bundleKeyToInteger(branding.getSplashRunningTextFontSize()));
        runningTextBounds.setValue(SplashUISupport.bundleKeyToBounds(branding.getSplashRunningTextBounds()));
        progressBarBounds.setValue(SplashUISupport.bundleKeyToBounds(branding.getSplashProgressBarBounds()));
        textColor.setColor(SplashUISupport.bundleKeyToColor(branding.getSplashRunningTextColor()));
        barColor.setColor(SplashUISupport.bundleKeyToColor(branding.getSplashProgressBarColor()));
        edgeColor.setColor(SplashUISupport.bundleKeyToColor(branding.getSplashProgressBarEdgeColor()));
        cornerColor.setColor(SplashUISupport.bundleKeyToColor(branding.getSplashProgressBarCornerColor()));
        progressBarEnabled.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getSplashShowProgressBar()));
        BrandedFile splash = branding.getSplash();
        
        splashSource = splash != null ? splash.getBrandingSource() : null;
        resetSplashPreview();
        
        splashImage.setMaxSteps(10);
        //splashImage.increment(10);
        splashImage.resetSteps();
        splashImage.setText(NbBundle.getMessage(getClass(),"TEXT_SplashSample")); //NOI18N
        
        enableDisableComponents();
        
    }
    
    private void enableDisableComponents() {
        final BrandingModel branding = getBranding();
        jLabel1.setEnabled(branding.isBrandingEnabled());
        jLabel2.setEnabled(branding.isBrandingEnabled());
        fontSize.setEnabled(branding.isBrandingEnabled());
        runningTextBounds.setEnabled(branding.isBrandingEnabled());
        progressBarBounds.setEnabled(branding.isBrandingEnabled());
        textColor.setEnabled(branding.isBrandingEnabled());
        barColor.setEnabled(branding.isBrandingEnabled());
        edgeColor.setEnabled(branding.isBrandingEnabled());
        cornerColor.setEnabled(branding.isBrandingEnabled());
        progressBarEnabled.setEnabled(branding.isBrandingEnabled());
        splashImage.setEnabled(branding.isBrandingEnabled());
        barBoundsLabel.setEnabled(branding.isBrandingEnabled());
        barColorLabel.setEnabled(branding.isBrandingEnabled());
        browse.setEnabled(branding.isBrandingEnabled());
        splashLabel.setEnabled(branding.isBrandingEnabled());
        splashPreview.setEnabled(branding.isBrandingEnabled());
        textBoundsLabel.setEnabled(branding.isBrandingEnabled());
        textColorLabel.setEnabled(branding.isBrandingEnabled());
        textFontSizeLabel.setEnabled(branding.isBrandingEnabled());
        splashImage.setEnabled(branding.isBrandingEnabled());
    }
    
    private void resetSplashPreview() throws NumberFormatException {
        Image oldImage = splashImage.image;
        if (null != oldImage)
            oldImage.flush();
        splashImage.setSplashImageIcon(splashSource);
        Rectangle tRectangle = (Rectangle)runningTextBounds.getValue();
        Rectangle pRectangle = (Rectangle)progressBarBounds.getValue();
        splashImage.setTextColor(textColor.getColor());
        splashImage.setColorBar(barColor.getColor());
        splashImage.setColorEdge(edgeColor.getColor());
        splashImage.setColorEdge(cornerColor.getColor());
        splashImage.setFontSize(((Number)fontSize.getValue()).intValue());
        splashImage.setRunningTextBounds(tRectangle);
        splashImage.setProgressBarBounds(pRectangle);
        splashImage.setProgressBarEnabled(progressBarEnabled.isSelected());
        splashImage.resetSteps();
        splashImage.setText(NbBundle.getMessage(getClass(),"TEXT_SplashSample")); //NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JComboBox barColor = this.barColor;
        javax.swing.JTextField jTextField1 = this.progressBarBounds;
        barColorLabel = new javax.swing.JLabel();
        barBoundsLabel = new javax.swing.JLabel();
        textColorLabel = new javax.swing.JLabel();
        javax.swing.JComboBox textColor = this.textColor;
        javax.swing.JTextField jTextField4 = this.runningTextBounds;
        javax.swing.JTextField fontSize = this.fontSize;
        progressBarEnabled = new javax.swing.JCheckBox();
        textFontSizeLabel = new javax.swing.JLabel();
        textBoundsLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        splashLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        splashPreview = splashImage;
        browse = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(barColor, gridBagConstraints);

        jTextField1.setInputVerifier(jTextField1.getInputVerifier());
        jTextField1.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jTextField1, gridBagConstraints);

        barColorLabel.setLabelFor(barColor);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/branding/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(barColorLabel, bundle.getString("LBL_BarColor")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(barColorLabel, gridBagConstraints);
        barColorLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_BarColor")); // NOI18N

        barBoundsLabel.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(barBoundsLabel, bundle.getString("LBL_BarBounds")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(barBoundsLabel, gridBagConstraints);
        barBoundsLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_BarBounds")); // NOI18N

        textColorLabel.setLabelFor(textColor);
        org.openide.awt.Mnemonics.setLocalizedText(textColorLabel, bundle.getString("LBL_TextColor")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textColorLabel, gridBagConstraints);
        textColorLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TextColor")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textColor, gridBagConstraints);

        jTextField4.setInputVerifier(jTextField1.getInputVerifier());
        jTextField4.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jTextField4, gridBagConstraints);

        fontSize.setInputVerifier(jTextField1.getInputVerifier());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(fontSize, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(progressBarEnabled, bundle.getString("LBL_ProgressBarEnabled")); // NOI18N
        progressBarEnabled.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        progressBarEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        progressBarEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progressBarEnabledActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 1, 0, 12);
        add(progressBarEnabled, gridBagConstraints);
        progressBarEnabled.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ProgressEnabled")); // NOI18N

        textFontSizeLabel.setLabelFor(fontSize);
        org.openide.awt.Mnemonics.setLocalizedText(textFontSizeLabel, bundle.getString("LBL_TextFontSize")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textFontSizeLabel, gridBagConstraints);
        textFontSizeLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_FontSize")); // NOI18N

        textBoundsLabel.setLabelFor(jTextField4);
        org.openide.awt.Mnemonics.setLocalizedText(textBoundsLabel, bundle.getString("LBL_TextBounds")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textBoundsLabel, gridBagConstraints);
        textBoundsLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TextBounds")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_ProgressBar")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("LBL_RunningText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(splashLabel, bundle.getString("LBL_Splash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        jPanel1.add(splashLabel, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportView(splashPreview);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, bundle.getString("LBL_Browse")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(browse, gridBagConstraints);
        browse.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_SplashBrowse")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser();
        int ret = chooser.showDialog(this, NbBundle.getMessage(getClass(), "LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            try {
                splashSource = Utilities.toURI(file).toURL();
                Image oldImage = splashImage.image;
                splashImage.setSplashImageIcon(splashSource);
                Image newImage = splashImage.image;
                assert newImage != null; // since splashSource != null
                int newWidth = newImage.getWidth(null);
                int newHeight = newImage.getHeight(null);
                if (oldImage != null) {
                oldImage.flush();
                int oldWidth = oldImage.getWidth(null);
                int oldHeight = oldImage.getHeight(null);
                if (newWidth != oldWidth || newHeight != oldHeight) {
                    double xRatio = newWidth / ((double) oldWidth);
                    double yRatio = newHeight / ((double) oldHeight);
                    Rectangle tRectangle = (Rectangle)runningTextBounds.getValue();
                    Rectangle pRectangle = (Rectangle)progressBarBounds.getValue();
                    
                    int x = ((int)(tRectangle.x*xRatio));
                    int y = ((int)(tRectangle.y*yRatio));
                    int width = ((int)(tRectangle.width*xRatio));
                    int height = ((int)(tRectangle.height*xRatio));
                    width = (width <= 0) ? 2 : width;
                    height = (height <= 0) ? 2 : height;
                    tRectangle.setBounds(x,y,width,height);
                    
                    x = ((int)(pRectangle.x*xRatio));
                    y = ((int)(pRectangle.y*yRatio));
                    width = ((int)(pRectangle.width*xRatio));
                    height = ((int)(pRectangle.height*xRatio));
                    width = (width <= 6) ? 6 : width;
                    height = (height <= 6) ? 6 : height;                    
                    pRectangle.setBounds(x,y,width,height);

                    runningTextBounds.setValue(tRectangle);
                    progressBarBounds.setValue(pRectangle);
                    int size = (int)((((Number)fontSize.getValue()).intValue()*yRatio));
                    size = (size <= 6) ? 6 : size;                    
                    fontSize.setValue(size);                    
                } else {
                    resetSplashPreview();
                }
                } else {
                    resetSplashPreview();
                }
                setModified();
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            
        }
        
    }//GEN-LAST:event_browseActionPerformed
    
    private void progressBarEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progressBarEnabledActionPerformed
        resetSplashPreview();
    }//GEN-LAST:event_progressBarEnabledActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel barBoundsLabel;
    private javax.swing.JLabel barColorLabel;
    private javax.swing.JButton browse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox progressBarEnabled;
    private javax.swing.JLabel splashLabel;
    private javax.swing.JLabel splashPreview;
    private javax.swing.JLabel textBoundsLabel;
    private javax.swing.JLabel textColorLabel;
    private javax.swing.JLabel textFontSizeLabel;
    // End of variables declaration//GEN-END:variables
}
