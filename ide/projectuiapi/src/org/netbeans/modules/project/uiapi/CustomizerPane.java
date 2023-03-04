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

package org.netbeans.modules.project.uiapi;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  phrebejk
 */
public class CustomizerPane extends JPanel
        implements HelpCtx.Provider {
    
    public static final String HELP_CTX_PROPERTY = "helpCtxProperty";
    
    private Component currentCustomizer;
    private JPanel errorPanel;
    private JLabel errorIcon;
    private JTextArea errorMessageValue;
    private HelpCtx currentHelpCtx;
    
    private GridBagConstraints fillConstraints;
    private GridBagConstraints errMessConstraints = new GridBagConstraints();
    
    private ProjectCustomizer.CategoryComponentProvider componentProvider;
    
    private HashMap<ProjectCustomizer.Category, JComponent> panelCache = new HashMap<ProjectCustomizer.Category, JComponent>();
    
    // maximum dimension of the customizer is 3/4 of screen size
    private static final int MAX_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height * 3 / 4;
    private static final int MAX_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width * 3 / 4;

    public CustomizerPane(JPanel categoryView, CategoryModel categoryModel, ProjectCustomizer.CategoryComponentProvider componentProvider) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPane.class,"AD_CustomizerPane")); // NOI18N
        this.componentProvider = componentProvider;
        fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = 1;
        fillConstraints.fill = GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;
        categoryModel.addPropertyChangeListener( new CategoryChangeListener() );
        categoryPanel.add( categoryView, fillConstraints );

        errorIcon = new JLabel();
        errorPanel = new JPanel(new BorderLayout(errorIcon.getIconTextGap(), 0)); // cf. BasicLabelUI.layoutCL
        errorPanel.add(errorIcon, BorderLayout.LINE_START);
        errorIcon.setVerticalAlignment(SwingConstants.TOP);
        errorMessageValue = new JTextArea();
        errorMessageValue.setLineWrap(true);
        errorMessageValue.setWrapStyleWord(true);
        errorMessageValue.setBorder(BorderFactory.createEmptyBorder());
        errorMessageValue.setBackground(customizerPanel.getBackground());
        errorMessageValue.setEditable(false);
        errorPanel.add(errorMessageValue, BorderLayout.CENTER);
        
        // put it into under categoryView
        errMessConstraints = new GridBagConstraints();
        errMessConstraints.gridx = 0;
        errMessConstraints.gridy = 1;
        errMessConstraints.gridwidth = 1;
        errMessConstraints.gridheight = 1;
        errMessConstraints.insets = new Insets(12, 0, 0, 0);
        errMessConstraints.fill = GridBagConstraints.HORIZONTAL;
        customizerPanel.add(errorPanel, errMessConstraints);

        /*Preferences prefs = NbPreferences.forModule(org.netbeans.modules.project.uiapi.CustomizerPane.class);
        int paneWidth = prefs.getInt(CUSTOMIZER_DIALOG_WIDTH, 0);
        int paneHeight = prefs.getInt(CUSTOMIZER_DIALOG_HEIGHT, 0);
        if (paneWidth != 0 && paneHeight != 0) {
            previousDimension = new Dimension(paneWidth, paneHeight);
        }*/

        setCategory( categoryModel.getCurrentCategory() );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        categoryPanel = new javax.swing.JPanel();
        customizerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(categoryPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerPane.class, "LBL_Customizer_Categories")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 11, 0, 0);
        add(jLabel1, gridBagConstraints);

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setMinimumSize(new java.awt.Dimension(220, 4));
        categoryPanel.setPreferredSize(new java.awt.Dimension(220, 4));
        categoryPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 11, 8, 11);
        add(categoryPanel, gridBagConstraints);

        customizerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 8, 11);
        add(customizerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
    
    public void clearPanelComponentCache() {
        //should only happen when closign teh customizer..
        panelCache.clear();
    }
    
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        
        int height = Math.max(500, currentCustomizer.getPreferredSize().height + 50);
        int width = Math.max(800, currentCustomizer.getPreferredSize().width + 240);
        
        Dimension dim = super.getPreferredSize();
        if (dim == null) {
            return new Dimension(width, height);
        }
        if (dim.getWidth() < width || dim.getHeight() < height) {
            return new Dimension(width, height);
        }
        
        if (dim.getWidth() > MAX_WIDTH) {
            dim.width = MAX_WIDTH;
        }
        if (dim.getHeight() > MAX_HEIGHT) {
            dim.height = MAX_HEIGHT;
        }
        return dim;
    }
    
    // HelpCtx.Provider implementation -----------------------------------------
    
    public HelpCtx getHelpCtx() {        
        return currentHelpCtx;        
        /*
        System.out.println("Get Help Ctx");
        Thread.dumpStack();
        
        if ( currentCustomizer != null  ) {
            // System.out.println( "C " + HelpCtx.findHelp( currentCustomizer )  );
            return HelpCtx.findHelp( currentCustomizer );
        }
        /*
        else {
            // System.out.println( "P " + HelpCtx.findHelp( currentCustomizer )  );
            return HelpCtx.findHelp( customizerPanel );
        }
              
        // XXX
        return null;
        */
    }
    
    
    // Private methods ---------------------------------------------------------
    
    private void setCategory(final ProjectCustomizer.Category newCategory) {
        if ( newCategory == null ) {
            return;
        }
        
        if ( currentCustomizer != null ) {
            customizerPanel.remove( currentCustomizer );
        }

        JComponent newCustomizer = panelCache.get(newCategory);
        if (newCustomizer == null && !panelCache.containsKey(newCustomizer)) {
            newCustomizer = componentProvider.create( newCategory );
            panelCache.put(newCategory, newCustomizer);
        }

        if ( newCustomizer != null ) {
            Utilities.getCategoryChangeSupport(newCategory).addPropertyChangeListener(new PropertyChangeListener() {
                public @Override void propertyChange(PropertyChangeEvent evt) {
                    setErrorMessage(newCategory.getErrorMessage(), newCategory.isValid());
                }
            });
            currentCustomizer = newCustomizer;            
            currentHelpCtx = HelpCtx.findHelp( currentCustomizer );
            /*if (previousDimension == null) {
                previousDimension = currentCustomizer.getSize();
            }
            int newWidth = 0;
            int newHeight = 0;
            if (previousDimension != null) {
                newWidth = previousDimension.width;
                newHeight = previousDimension.height;
                if (currentCustomizer.getPreferredSize().width > previousDimension.width) {
                    newWidth = currentCustomizer.getPreferredSize().width;
                    int maxWidth = WindowManager.getDefault().getMainWindow().getGraphicsConfiguration().getBounds().width * 3 / 4;
                    if (newWidth > maxWidth) {
                        newWidth = maxWidth;
                    }
                }
                if (currentCustomizer.getPreferredSize().height > previousDimension.height) {
                    newHeight = currentCustomizer.getPreferredSize().height;
                    int maxHeght = WindowManager.getDefault().getMainWindow().getGraphicsConfiguration().getBounds().height * 3 / 4;
                    if (newHeight > maxHeght) {
                        newHeight = maxHeght;
                    }
                }
            }

            Dimension newDim = new Dimension(newWidth, newHeight);
            currentCustomizer.setPreferredSize(newDim);
            previousDimension = newDim;*/

            /*Preferences prefs = NbPreferences.forModule(org.netbeans.modules.project.uiapi.CustomizerPane.class);
            prefs.put(CUSTOMIZER_DIALOG_WIDTH, Integer.toString(newDim.width));
            prefs.put(CUSTOMIZER_DIALOG_HEIGHT, Integer.toString(newDim.height));*/

            customizerPanel.add( currentCustomizer, fillConstraints );
            customizerPanel.validate();
            customizerPanel.repaint();

            /*if (customizerPanel != null) {
                Window window = SwingUtilities.getWindowAncestor(customizerPanel);
                if (window != null) {
                    window.pack();
                    window.setBounds(org.openide.util.Utilities.findCenterBounds(window.getSize()));
                }
            }*/
            
            setErrorMessage(newCategory.getErrorMessage(), newCategory.isValid());
            firePropertyChange( HELP_CTX_PROPERTY, null, getHelpCtx() );
        } else {
            currentCustomizer = null;
        }
    }

    private void setErrorMessage(String errMessage, boolean valid) {
        customizerPanel.remove(errorPanel);
        if (errMessage != null && !errMessage.trim().isEmpty()) {
            errorIcon.setIcon(ImageUtilities.loadImageIcon(valid ? "org/netbeans/modules/dialogs/warning.gif" : "org/netbeans/modules/dialogs/error.gif", true));
            errorMessageValue.setText(errMessage);
            errorMessageValue.setForeground(UIManager.getColor(valid ? "nb.warningForeground" : "nb.errorForeground")); // NOI18N
            customizerPanel.add(errorPanel, errMessConstraints);
        }
        customizerPanel.revalidate();
        customizerPanel.repaint();
    }

    // Private innerclasses ----------------------------------------------------
                
    /** Listens to selection change and shows the customizers as
     *  panels
     */        
    private class CategoryChangeListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            
            if ( CategoryModel.PROP_CURRENT_CATEGORY.equals( evt.getPropertyName() ) ) {                                
                ProjectCustomizer.Category newCategory = (ProjectCustomizer.Category)evt.getNewValue();
                setCategory( newCategory );
            }
        }
    }

}
