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

package org.openide.loaders;

import java.awt.event.KeyEvent;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.swing.event.*;
import org.openide.WizardDescriptor;
import org.openide.explorer.propertysheet.*;
import org.openide.filesystems.*;
import org.openide.util.*;

/** Dialog that can be used in create from template.
 *
 * @author  Jaroslav Tulach, Jiri Rechtacek
 */
final class TemplateWizard2 extends javax.swing.JPanel implements DocumentListener {

    /** listener to changes in the wizard */
    private ChangeListener listener;
    
    private static final String PROP_LOCATION_FOLDER = "locationFolder"; // NOI18N
    private File locationFolder;
    private DefaultPropertyModel locationFolderModel;

    /** File extension of the template and of the created file -
     * it is used to test whether file already exists.
     */
    private String extension;

    /** Creates new form TemplateWizard2 */
    public TemplateWizard2() {
        initLocationFolder ();
        initComponents ();
        setName (DataObject.getString("LAB_TargetLocationPanelName"));  // NOI18N
        // registers itself to listen to changes in the content of document
        java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(TemplateWizard2.class);
        newObjectName.getDocument().addDocumentListener(this);
        newObjectName.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        newObjectName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NewObjectName")); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the location folder and make it accessible.
     * The getter/setter methods must be accessible for purposes introspection.
     * Because this class is not public then these methods are made accessible explicitly.
     */
    private void initLocationFolder () {
        PropertyDescriptor pd = null;
        try {
            Method getterMethod = this.getClass ().getDeclaredMethod("getLocationFolder", new Class[] {}); // NOI18N
            getterMethod.setAccessible (true);
            Method setterMethod = this.getClass ().getDeclaredMethod("setLocationFolder", new Class[] {File.class}); // NOI18N
            setterMethod.setAccessible (true);
            pd = new PropertyDescriptor (PROP_LOCATION_FOLDER, getterMethod, setterMethod);
            pd.setValue("directories", true); // NOI18N
            pd.setValue("files", false); // NOI18N
        } catch (java.beans.IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
        } catch (NoSuchMethodException nsme) {
            Exceptions.printStackTrace(nsme);
        } 
        locationFolderModel = new DefaultPropertyModel (this, pd);
    }

    /** Getter for default name of a new object.
    * @return the default name.
    */
    private static String defaultNewObjectName () {
        return DataObject.getString ("FMT_DefaultNewObjectName"); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        namePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        newObjectName = new javax.swing.JTextField();
        dataFolderPanel = dataFolderPanel = new PropertyPanel (locationFolderModel, PropertyPanel.PREF_CUSTOM_EDITOR);

        FormListener formListener = new FormListener();

        setLayout(new java.awt.BorderLayout());

        namePanel.setLayout(new java.awt.BorderLayout(12, 0));

        jLabel1.setLabelFor(newObjectName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/openide/loaders/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_NewObjectName")); // NOI18N
        namePanel.add(jLabel1, java.awt.BorderLayout.WEST);

        newObjectName.addFocusListener(formListener);
        namePanel.add(newObjectName, java.awt.BorderLayout.CENTER);

        add(namePanel, java.awt.BorderLayout.NORTH);
        add(dataFolderPanel, java.awt.BorderLayout.CENTER);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.FocusListener {
        FormListener() {}
        public void focusGained(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == newObjectName) {
                TemplateWizard2.this.newObjectNameFocusGained(evt);
            }
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void newObjectNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_newObjectNameFocusGained
        newObjectName.selectAll ();
    }//GEN-LAST:event_newObjectNameFocusGained

    /** Add a listener to changes of the panel's validity.
     * @param l the listener to add
     * @see #isValid
     *
     */
    void addChangeListener(ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();

        listener = l;
    }    
    
    public void addNotify () {
        super.addNotify();
        //Fix for issue 31086, initial focus on Back button 
        newObjectName.requestFocus();
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getBundle(TemplateWizard2.class).getString ("ACSD_TemplateWizard2") // NOI18N
        );
    }
    
    /** Helper implementation of WizardDescription.Panel for TemplateWizard.Panel2.
     * Provides the wizard panel with the current data--either
     * the default data or already-modified settings, if the user used the previous and/or next buttons.
     * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
     * <p>The settings object is originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}.
     * In the case of a <code>TemplateWizard.Iterator</code> panel, the object is
     * in fact the <code>TemplateWizard</code>.
     * @param settings the object representing wizard panel state
     * @exception IllegalStateException if the the data provided
     * by the wizard are not valid.
     *
     */
    void implReadSettings(Object settings) {
        TemplateWizard wizard = (TemplateWizard)settings;

        DataObject template = wizard.getTemplate ();
        if (template != null) {
            extension = template.getPrimaryFile().getExt();
        }
        
        setNewObjectName (wizard.getTargetName ());

        try {
            setLocationDataFolder(wizard.getTargetFolder());
        } catch (IOException ioe) {
            setLocationFolder (null);
        }
    }
    
    /** Remove a listener to changes of the panel's validity.
     * @param l the listener to remove
     *
     */
    public void removeChangeListener(ChangeListener l) {
        listener = null;
    }
    
    /** Helper implementation of WizardDescription.Panel for TemplateWizard.Panel2.
     * Provides the wizard panel with the opportunity to update the
     * settings with its current customized state.
     * Rather than updating its settings with every change in the GUI, it should collect them,
     * and then only save them when requested to by this method.
     * Also, the original settings passed to {@link #readSettings} should not be modified (mutated);
     * rather, the object passed in here should be mutated according to the collected changes,
     * in case it is a copy.
     * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
     * <p>The settings object is originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}.
     * In the case of a <code>TemplateWizard.Iterator</code> panel, the object is
     * in fact the <code>TemplateWizard</code>.
     * @param settings the object representing wizard panel state
     *
     */
    void implStoreSettings(Object settings) {
        TemplateWizard wizard = (TemplateWizard)settings;

        wizard.setTargetFolder(getLocationDataFolder());

        String name = newObjectName.getText ();
        if (name.equals (defaultNewObjectName ())) {
            name = null;
        }
        wizard.setTargetName (name);
    }

    /** Helper implementation of WizardDescription.Panel for TemplateWizard.Panel2.
    * Test whether the panel is finished and it is safe to proceed to the next one.
    * If the panel is valid, the "Next" (or "Finish") button will be enabled.
    * @return <code>null</code> if the user has entered satisfactory information
    * or localized string describing the error.
    */
    String implIsValid () {
        // test whether the selected folder on selected filesystem already exists
        DataFolder lF = getLocationDataFolder();
        if (lF == null)
            return NbBundle.getMessage(TemplateWizard2.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        
        // target filesystem should be writable
        if (!lF.getPrimaryFile().canWrite())
            return NbBundle.getMessage(TemplateWizard2.class, "MSG_fs_is_readonly"); // NOI18N
        
        if (locationFolder.exists()) {
            return NbBundle.getMessage(TemplateWizard2.class, "MSG_file_already_exist", locationFolder.getAbsolutePath()); // NOI18N
        }

        if ((Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2))) {
            if (TemplateWizard.checkCaseInsensitiveName(lF.getPrimaryFile(), newObjectName.getText(), extension)) {
                return NbBundle.getMessage(TemplateWizard2.class, "MSG_file_already_exist", newObjectName.getText ()); // NOI18N
            }
        }

        // all ok
        return null;
    }
    
    /** Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     *
     */
    public void changedUpdate(DocumentEvent e) {
        if (e.getDocument () == newObjectName.getDocument ()) {
            SwingUtilities.invokeLater (new Updater ());
        }
    }
    
    /** Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     *
     */
    public void insertUpdate(DocumentEvent e) {
        changedUpdate (e);
    }
    
    /** Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     *
     */
    public void removeUpdate(DocumentEvent e) {
        // so just check the name
        if (e.getDocument () == newObjectName.getDocument ()) {
            SwingUtilities.invokeLater (new Updater ());
        }
    }
    
    /** Does the update called from changedUpdate, insertUpdate and
     *  removeUpdate methods.
     */
    private class Updater implements Runnable {
        Updater() {}
        public void run () {
            if (newObjectName.getText().equals ("")) { // NOI18N
                setNewObjectName (""); // NOI18N
            }

            fireStateChanged ();
        }
    }

    /** Request focus.
    */
    public void requestFocus () {
        newObjectName.requestFocus();
        newObjectName.selectAll ();
    }

    /** Sets the class name to some reasonable value.
    * @param name the name to set the name to
    */
    private void setNewObjectName (String name) {
        String n = name;
        if (name == null || name.length () == 0) {
            n = defaultNewObjectName ();
        }

        newObjectName.getDocument().removeDocumentListener(this);
        newObjectName.setText (n);
        newObjectName.getDocument().addDocumentListener(this);

        if (name == null || name.length () == 0) {
            newObjectName.selectAll ();
        }
    }

    /** Fires info to listener.
    */
    private void fireStateChanged () {
        if (listener != null) {
            listener.stateChanged (new ChangeEvent (this));
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.propertysheet.PropertyPanel dataFolderPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField newObjectName;
    // End of variables declaration//GEN-END:variables

    public void setLocationFolder(File fd) {
        if (locationFolder == fd)
            return ;
        if (locationFolder != null && locationFolder.equals (fd))
            return ;
        File oldLocation = locationFolder;
        locationFolder = fd;
        firePropertyChange (PROP_LOCATION_FOLDER, oldLocation, locationFolder);
        fireStateChanged ();
    }
    private void setLocationDataFolder(DataFolder fd) {
        setLocationFolder(fd != null ? FileUtil.toFile(fd.getPrimaryFile()) : null);
    }
    
    public File getLocationFolder() {
        return locationFolder;
    }
    private DataFolder getLocationDataFolder() {
        if (locationFolder != null) {
            FileObject f = FileUtil.toFileObject(FileUtil.normalizeFile(locationFolder));
            if (f != null && f.isFolder()) {
                return DataFolder.findFolder(f);
            }
        }
        return null;
    }
    
}
