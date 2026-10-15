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


package org.netbeans.modules.i18n;


import javax.swing.GroupLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.netbeans.api.project.Project;
import org.openide.awt.Mnemonics;
import org.openide.util.Enumerations;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;


/**
 * Panel which provides GUI for i18n action.
 * Customizes {@code I18nString} object and is used by {@code I18nSupport} for i18n-izing 
 * one source.
 *
 * @author  Peter Zavadsky
 */
public class I18nPanel extends JPanel {

    private static final String CONTENT_FORM = "form visible";          //NOI18N
    private static final String CONTENT_MESG = "message visible";       //NOI18N

    private final boolean withButtons;

    /** layout of the {@link #contentsPanelPlaceholder} */
    private final CardLayout cardLayout = new CardLayout();

    /** {@code I18nString} cusomized in this panel. */
    private I18nString i18nString;
    
    /** Helper bundle used for i18n-zing strings in this source.  */
    private ResourceBundle bundle;

    /** Helper property change support. */
    private PropertyChangeListener propListener;
    
    /** Generated serial version ID. */
    static final long serialVersionUID =-6982482491017390786L;

    private Project project;

    private FileObject file;
    
    private static final long ALL_BUTTONS    = ~0;
    private static final long NO_BUTTONS     = 0;
    private static final long REPLACE_BUTTON = 1 << 0;
    private static final long SKIP_BUTTON    = 1 << 1;
    private static final long IGNORE_BUTTON  = 1 << 2;
    private static final long INFO_BUTTON    = 1 << 3;
    private static final long CANCEL_BUTTON  = 1 << 4;
    private static final long HELP_BUTTON    = 1 << 5;


    
    /** 
     * Creates new I18nPanel.  In order to correctly localize
     * classpath for property bundle chooser, the dialog must know the
     * project and a file to choose the bundle for.
     *
     * @param  propertyPanel  panel for customizing i18n strings 
     * @param  project  the Project to choose bundles from
     * @param  file  the FileObject to choose bundles for
     */
    public I18nPanel(PropertyPanel propertyPanel, Project project, FileObject file) {
        this(propertyPanel, true, project, file);
    }

    /**
     * Creates i18n panel.
     *
     * @param  propertyPanel  panel for customizing i18n strings 
     * @param  withButtons  if panel with replace, skip ect. buttons should be added 
     * @param  project  the Project to choose bundles from
     * @param  file  the FileObject to choose bundles for
     */
    public I18nPanel(PropertyPanel propertyPanel, boolean withButtons, Project project, FileObject file) {
        this.project = project;
        this.file = file;
        this.propertyPanel = propertyPanel;
        this.propertyPanel.setFile(file);
        this.propertyPanel.setEnabled(project != null);
        this.emptyPanel = new EmptyPropertyPanel();

        // Init bundle.
        bundle = I18nUtil.getBundle();
        
        this.withButtons = withButtons;
        initComponents();
        myInitComponents();
        initAccessibility();        
        
        showBundleMessage("TXT_SearchingForStrings");                   //NOI18N
    }


    private boolean contentsShown;

    public void showBundleMessage(String bundleKey) {
        emptyPanel.setBundleText(bundleKey);
        cardLayout.show(contentsPanelPlaceholder, CONTENT_MESG);
        contentsShown = false;
        buttonsEnableDisable();
    }

    public void showPropertyPanel() {
        cardLayout.show(contentsPanelPlaceholder, CONTENT_FORM);
        contentsShown = true;
        buttonsEnableDisable();        
    }


    
    /**
     * Reset associated project to a new value
     */
//    public void setProject(Project project) {
// //        ((ResourcePanel)resourcePanel).setProject(project);
//        propertyPanel.setEnabled(project != null);
//
//    }
//
//    public Project getProject() { 
//        return ((ResourcePanel)resourcePanel).getProject();
//    }
    
    /**
     * Sets the file associated with this panel -- the one, which
     * is localized
     */ 
    public void setFile(FileObject file) {
//        ((ResourcePanel)resourcePanel).setFile(file);
        propertyPanel.setFile(file);
    }
    
    /**
     * Gets the file associated with this panel -- the one, which
     * is localized
     */ 
    public FileObject getFile() {
//        return ((ResourcePanel)resourcePanel).getFile();
        return propertyPanel.getFile();
    }

    
    /** Overrides superclass method to set default button. */
    @Override
    public void addNotify() {
        super.addNotify();
        
        if (withButtons) {
            if (SwingUtilities.isDescendingFrom(replaceButton, this)) {
                getRootPane().setDefaultButton(replaceButton);
            }
        }
    }
    
    /** Getter for <code>i18nString</code>. */
    public I18nString getI18nString() {
        return i18nString;
    }
    
    /** Setter for i18nString property. */
    public void setI18nString(I18nString i18nString) {
        this.i18nString = i18nString;

        propertyPanel.setI18nString(i18nString);
//        ((ResourcePanel)resourcePanel).setI18nString(i18nString);        
        
        showPropertyPanel();
    }

    /** Replace button accessor. */
    JButton getReplaceButton() {
        assert withButtons;
        return replaceButton;
    }
    
    /** Skip button accessor. */
    JButton getSkipButton() {
        assert withButtons;
        return skipButton;
    }

    /** Ignore button accessor. */
    JButton getIgnoreButton() {
        assert withButtons;
        return ignoreButton;
    }

    /** Info button accessor. */
    JButton getInfoButton() {
        assert withButtons;
        return infoButton;
    }
    
    /** Cancel/Close button accessor. */
    JButton getCancelButton() {
        assert withButtons;
        return cancelButton;
    }
    
    /** Enables/disables buttons based on the contents of the dialog. */
    private void buttonsEnableDisable() {
        if (!withButtons) {
            return;
        }

        if (contentsShown) {
            enableButtons(ALL_BUTTONS);
            boolean isBundle = (i18nString != null)
                               && (i18nString.getSupport().getResourceHolder().getResource() != null);
            boolean keyEmpty = (getI18nString() == null)
                               || (getI18nString().getKey() == null)
                               || (getI18nString().getKey().trim().length() == 0);
            replaceButton.setEnabled(isBundle && !keyEmpty);
        } else {
            enableButtons(CANCEL_BUTTON | HELP_BUTTON);
        }
    }

    public void setDefaultResource(DataObject dataObject) {
        if (dataObject != null) {
            // look for peer Bundle.properties
            FileObject fo = dataObject.getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);

            if (cp != null) {
                FileObject folder = cp.findResource(cp.getResourceName(fo.getParent()));

                while (folder != null && cp.contains(folder)) {
                
                    String rn = cp.getResourceName(folder) + "/Bundle.properties"; // NOI18N
                
                    FileObject peer = cp.findResource(rn);
                    if (peer == null) {
                        //Try to find any properties file
                        Enumeration<? extends FileObject> data = Enumerations.filter(folder.getData(false), new Enumerations.Processor(){
                            public Object process(Object obj, Collection alwaysNull) {
                                if (obj instanceof FileObject &&
                                        "properties".equals( ((FileObject)obj).getExt())){ //NOI18N
                                    return obj;
                                } else {
                                    return null;
                                }
                            }
                        });
                        if (data.hasMoreElements()) {
                            peer = data.nextElement();
                        }
                    }
                    if (peer != null) {
                        try {
                            DataObject peerDataObject = DataObject.find(peer);
//                          ((ResourcePanel)resourcePanel).setResource(peerDataObject);
                            propertyPanel.setResource(peerDataObject);
                            return;
                        } catch (IOException ex) {
                            // no default resource
                        }
                    }
                    folder = folder.getParent();
                }
            }
        }
    }        
    
    /** Creates <code>ResourcePanel</code>. */
//    private JPanel createResourcePanel() {
//        return new ResourcePanel(project, file);
//    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_I18nPanel"));                     //NOI18N
        if (withButtons) {
            skipButton.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACS_CTL_SkipButton"));            //NOI18N
            ignoreButton.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACS_CTL_IgnoreButton"));            //NOI18N
            ignoreButton.setToolTipText(bundle.getString("TT_IgnoreButton"));            //NOI18N
            cancelButton.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACS_CTL_CancelButton"));          //NOI18N
            replaceButton.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACS_CTL_ReplaceButton"));         //NOI18N
            infoButton.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACS_CTL_InfoButton"));            //NOI18N
            helpButton.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACS_CTL_HelpButton"));            //NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="UI Initialization Code">
    private void initComponents() {

        contentsPanelPlaceholder = new JPanel(cardLayout);

        if (withButtons) {
            replaceButton = new JButton();
            skipButton = new JButton();
            ignoreButton = new JButton();
            infoButton = new JButton();
            cancelButton = new JButton();
            helpButton = new JButton();

            Mnemonics.setLocalizedText(replaceButton, bundle.getString("CTL_ReplaceButton")); // NOI18N
            Mnemonics.setLocalizedText(skipButton, bundle.getString("CTL_SkipButton")); // NOI18N
            Mnemonics.setLocalizedText(ignoreButton, bundle.getString("CTL_IgnoreButton")); // NOI18N
            Mnemonics.setLocalizedText(infoButton, bundle.getString("CTL_InfoButton")); // NOI18N
            Mnemonics.setLocalizedText(cancelButton, bundle.getString("CTL_CloseButton")); // NOI18N
            Mnemonics.setLocalizedText(helpButton, bundle.getString("CTL_HelpButton")); // NOI18N

            helpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    helpButtonActionPerformed(evt);
                }
            });
        }

        GroupLayout layout;
        setLayout (layout = new GroupLayout(this));

        GroupLayout.SequentialGroup horizGroup = layout.createSequentialGroup();
        horizGroup.addContainerGap();
        if (withButtons) {
            horizGroup.addGroup(
                    layout.createParallelGroup(TRAILING)
                    .addComponent(contentsPanelPlaceholder)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(replaceButton)
                        .addPreferredGap(RELATED)
                        .addComponent(skipButton)
                        .addPreferredGap(RELATED)
                        .addComponent(ignoreButton)
                        .addPreferredGap(RELATED)
                        .addComponent(infoButton)
                        .addPreferredGap(RELATED)
                        .addComponent(cancelButton)
                        .addPreferredGap(RELATED)
                        .addComponent(helpButton)));
            layout.linkSize(SwingConstants.HORIZONTAL, cancelButton,
                                             helpButton,
                                             infoButton,
                                             replaceButton,
                                             ignoreButton,
                                             skipButton);
        } else {
            horizGroup.addComponent(contentsPanelPlaceholder);
        }
        horizGroup.addContainerGap();
        layout.setHorizontalGroup(horizGroup);

        GroupLayout.SequentialGroup vertGroup = layout.createSequentialGroup();
        vertGroup.addContainerGap();
        vertGroup.addComponent(contentsPanelPlaceholder);
        if (withButtons) {
            vertGroup
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(BASELINE)
                .addComponent(helpButton)
                .addComponent(cancelButton)
                .addComponent(infoButton)
                .addComponent(skipButton)
                .addComponent(ignoreButton)
                .addComponent(replaceButton));
        }
        vertGroup.addContainerGap();
        layout.setVerticalGroup(vertGroup);
    }// </editor-fold>

    private void myInitComponents() {
//        resourcePanel = createResourcePanel();
//        contentsPanel = new JPanel();
//        contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));

//        contentsPanel.add(resourcePanel);
//        contentsPanel.add(propertyPanel);

        contentsPanelPlaceholder.add(propertyPanel, CONTENT_FORM);
        contentsPanelPlaceholder.add(emptyPanel, CONTENT_MESG);

        cardLayout.show(contentsPanelPlaceholder, CONTENT_FORM);
        contentsShown = true;
        
        if (withButtons) {
            propertyPanel.addPropertyChangeListener(
                    PropertyPanel.PROP_STRING, 
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            buttonsEnableDisable();
                        }
                    });
            
            propertyPanel.addPropertyChangeListener(
                    PropertyPanel.PROP_RESOURCE,
    //                WeakListeners.propertyChange(
                        new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent evt) {
                                if(PropertyPanel.PROP_RESOURCE.equals(evt.getPropertyName())) {
                                    buttonsEnableDisable();
        //                            ((PropertyPanel)propertyPanel).updateAllValues();
                                }
                            }
                        }
    //                },resourcePanel
    //               )
            );
        }
        
    }

  private void helpButtonActionPerformed(ActionEvent evt) {
      assert withButtons;
      HelpCtx help = new HelpCtx(I18nUtil.HELP_ID_AUTOINSERT);
      
      String sysprop = System.getProperty("org.openide.actions.HelpAction.DEBUG"); // NOI18N
      
      if ("true".equals(sysprop) || "full".equals(sysprop)) { // NOI18N
          System.err.println ("I18n module: Help button showing: " + help); // NOI18N, please do not comment out
      }
      help.display();
  }

    private void enableButtons(long buttonMask) {
        assert withButtons;
        replaceButton.setEnabled((buttonMask & REPLACE_BUTTON) != 0);
        skipButton.setEnabled((buttonMask & SKIP_BUTTON) != 0);
        ignoreButton.setEnabled((buttonMask & IGNORE_BUTTON) != 0);
        infoButton.setEnabled((buttonMask & INFO_BUTTON) != 0);
        cancelButton.setEnabled((buttonMask & CANCEL_BUTTON) != 0);
        helpButton.setEnabled((buttonMask & HELP_BUTTON) != 0);               
    }

        


    // Variables declaration
    private JButton cancelButton;
    private JPanel contentsPanelPlaceholder;
    private JButton helpButton;
    private JButton infoButton;
    private JButton replaceButton;
    private JButton skipButton;
    private JButton ignoreButton;
    // End of variables declaration

    private EmptyPropertyPanel emptyPanel;
//    private JPanel resourcePanel;
    private PropertyPanel propertyPanel;
//    private JPanel contentsPanel;

}
