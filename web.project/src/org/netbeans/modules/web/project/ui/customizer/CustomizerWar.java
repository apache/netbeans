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
package org.netbeans.modules.web.project.ui.customizer;

import java.text.MessageFormat;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils;
import org.netbeans.modules.javaee.project.api.ui.utils.UIUtil;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Customizer for WAR packaging.
 */
public class CustomizerWar extends JPanel implements HelpCtx.Provider, TableModelListener {

    WebProjectProperties uiProperties;
    ProjectCustomizer.Category category;

    /** Creates new form CustomizerCompile */
    public CustomizerWar(ProjectCustomizer.Category category, WebProjectProperties uiProperties) {
        this.category=category;
        this.uiProperties = uiProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerWar.class, "ACS_CustomizeWAR_A11YDesc")); //NOI18N

        jTextFieldFileName.setDocument( uiProperties.WAR_NAME_MODEL );
        jTextFieldFileName.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(checkValidWarFilename()){
                    showWarningOnWARnameChange();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if(checkValidWarFilename()){
                    showWarningOnWARnameChange();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if(checkValidWarFilename()){
                    showWarningOnWARnameChange();
                }
            }
            
        });
        jTextFieldExContent.setDocument( uiProperties.BUILD_CLASSES_EXCLUDES_MODEL );
        uiProperties.WAR_COMPRESS_MODEL.setMnemonic( jCheckBoxCompress.getMnemonic() );
        jCheckBoxCompress.setModel( uiProperties.WAR_COMPRESS_MODEL );
        ClassPathUiSupport.Callback callback = new ClassPathUiSupport.Callback() {

            public void initItem(Item item) {
                if (item.getType() != ClassPathSupport.Item.TYPE_LIBRARY || !item.getLibrary().getType().equals(J2eePlatform.LIBRARY_TYPE)) {
                    item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, "/"); //NOI18N
                }
            }
            
        };

        jTableAddContent.setModel( uiProperties.WAR_CONTENT_ADDITIONAL_MODEL );
        jTableAddContent.setDefaultRenderer(ClassPathSupport.Item.class, uiProperties.CLASS_PATH_TABLE_ITEM_RENDERER);
        UIUtil.initTwoColumnTableVisualProperties(this, jTableAddContent);
        jTableAddContent.setRowHeight(jTableAddContent.getRowHeight() + 4);

        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jTableAddContent, uiProperties.WAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()) ,
                jButtonAddJar.getModel(),
                jButtonAddLib.getModel(),
                jButtonAddProject.getModel(),
                jButtonRemove.getModel(),
                (new JButton()).getModel(), // no button in UI
                (new JButton()).getModel(), // no button in UI
                (new JButton()).getModel(), // no button in UI
                uiProperties.SHARED_LIBRARIES_MODEL,
                callback,
                new String[]{EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE, JavaProjectConstants.ARTIFACT_TYPE_JAR},
                null, JFileChooser.FILES_AND_DIRECTORIES);
        uiProperties.WAR_CONTENT_ADDITIONAL_MODEL.addTableModelListener(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabelFileName = new javax.swing.JLabel();
        jTextFieldFileName = new javax.swing.JTextField();
        jLabelExContent = new javax.swing.JLabel();
        jTextFieldExContent = new javax.swing.JTextField();
        excludeMessage = new javax.swing.JLabel();
        jCheckBoxCompress = new javax.swing.JCheckBox();
        jLabelAddContent = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableAddContent = new javax.swing.JTable();
        jButtonAddProject = new javax.swing.JButton();
        jButtonAddLib = new javax.swing.JButton();
        jButtonAddJar = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabelFileName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_FileName_LabelMnemonic").charAt(0));
        jLabelFileName.setLabelFor(jTextFieldFileName);
        jLabelFileName.setText(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_FileName_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(jLabelFileName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanel1.add(jTextFieldFileName, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle"); // NOI18N
        jTextFieldFileName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_FileName_A11YDesc")); // NOI18N

        jLabelExContent.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_Content_LabelMnemonic").charAt(0));
        jLabelExContent.setLabelFor(jTextFieldExContent);
        jLabelExContent.setText(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Content_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel1.add(jLabelExContent, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jTextFieldExContent, gridBagConstraints);
        jTextFieldExContent.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "ACS_CustomizeWAR_Content_A11YDesc")); // NOI18N

        excludeMessage.setLabelFor(jTextFieldExContent);
        excludeMessage.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizerWAR_ExcludeMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(excludeMessage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jPanel1, gridBagConstraints);

        jCheckBoxCompress.setMnemonic(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Commpres_LabelMnemonic").charAt(0));
        jCheckBoxCompress.setText(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Commpres_JCheckBox")); // NOI18N
        jCheckBoxCompress.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jCheckBoxCompress, gridBagConstraints);
        jCheckBoxCompress.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_Commpres_A11YDesc")); // NOI18N

        jLabelAddContent.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddContent_LabelMnemonic").charAt(0));
        jLabelAddContent.setLabelFor(jTableAddContent);
        jLabelAddContent.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddContent_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 2, 0);
        add(jLabelAddContent, gridBagConstraints);

        jTableAddContent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(jTableAddContent);
        jTableAddContent.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddContent_A11YDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        add(jScrollPane2, gridBagConstraints);

        jButtonAddProject.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddProject_LabelMnemonic").charAt(0));
        jButtonAddProject.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddProject, gridBagConstraints);
        jButtonAddProject.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddProject_A11YDesc")); // NOI18N

        jButtonAddLib.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddLib_LabelMnemonic").charAt(0));
        jButtonAddLib.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddLib_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddLib, gridBagConstraints);
        jButtonAddLib.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddLib_A11YDesc")); // NOI18N

        jButtonAddJar.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddJar_LabelMnemonic").charAt(0));
        jButtonAddJar.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonAddJar, gridBagConstraints);
        jButtonAddJar.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddJar_A11YDesc")); // NOI18N

        jButtonRemove.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AdditionalRemove_LabelMnemonic").charAt(0));
        jButtonRemove.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonRemove, gridBagConstraints);
        jButtonRemove.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AdditionalRemove_A11YDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        add(errorLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel excludeMessage;
    private javax.swing.JButton jButtonAddJar;
    private javax.swing.JButton jButtonAddLib;
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JCheckBox jCheckBoxCompress;
    private javax.swing.JLabel jLabelAddContent;
    private javax.swing.JLabel jLabelExContent;
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableAddContent;
    private javax.swing.JTextField jTextFieldExContent;
    private javax.swing.JTextField jTextFieldFileName;
    // End of variables declaration//GEN-END:variables

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerWar.class);
    }
    
    private boolean checkValidWarFilename(){
        MessageUtils.clear(errorLabel);
        category.setValid(true);
        
        String filename=jTextFieldFileName.getText();
        String pattern;
        String forbiddenChars;
        if (Utilities.isWindows()) {
            pattern = ".*[\\\\/:\\*\\?\"<>\\|].*";    // NOI18N
            forbiddenChars = "\\ / : * ? \" < > |";    // NOI18N
        } else {
            pattern = ".*[\\\\/].*";    // NOI18N
            forbiddenChars = "\\ /";    // NOI18N
        }
        String message=null;
        if (filename.trim().length() == 0) {
            message = NbBundle.getMessage(CustomizerWar.class, "MSG_EmptyWarName");
        }else if (Pattern.matches(pattern, filename)) {
            message = NbBundle.getMessage(CustomizerWar.class, "MSG_ForbiddenCharactersInWarName");
            message=MessageFormat.format(message, forbiddenChars);
        }
        if(message != null){
            category.setValid(false);
            MessageUtils.setMessage(errorLabel, MessageUtils.MessageType.ERROR, "<html>"+message+"</html>"); // NOI18N
        }
        return message == null;
    }
    
    private void showWarningOnWARnameChange(){
        String message=NbBundle.getMessage(CustomizerWar.class, "MSG_WarningOnWARnameChange");
        MessageUtils.setMessage(errorLabel, MessageUtils.MessageType.WARNING, "<html>"+message+"</html>"); // NOI18N
    }

    public void tableChanged(TableModelEvent e) {
        if (e.getColumn() != 1) {
            return;
        }
        TableModel listModel = uiProperties.WAR_CONTENT_ADDITIONAL_MODEL;
        ClassPathSupport.Item cpItem = (ClassPathSupport.Item) listModel.getValueAt(e.getFirstRow(), 0);
        String newPathInWar = (String) listModel.getValueAt(e.getFirstRow(), 1);
        String message = null;
        if (cpItem.getType() == ClassPathSupport.Item.TYPE_JAR && newPathInWar.startsWith("WEB-INF")) { //NOI18N
            if (newPathInWar.equals("WEB-INF\\lib") || newPathInWar.equals("WEB-INF/lib")) { //NOI18N
                if (cpItem.getResolvedFile().isDirectory()) {
                    message = NbBundle.getMessage(CustomizerWar.class,
                        "MSG_NO_FOLDER_IN_WEBINF_LIB", newPathInWar); // NOI18N
                } else {
                    message = NbBundle.getMessage(CustomizerWar.class,
                        "MSG_NO_FILE_IN_WEBINF_LIB", newPathInWar); // NOI18N
                }
            } else if (newPathInWar.equals("WEB-INF\\classes") || newPathInWar.equals("WEB-INF/classes")) { //NOI18N
                    message = NbBundle.getMessage(CustomizerWar.class,
                        "MSG_NO_FOLDER_IN_WEBINF_CLASSES", newPathInWar); // NOI18N
            }
        }
        if (message != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (message, NotifyDescriptor.WARNING_MESSAGE));
        }
    }
}
