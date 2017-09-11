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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.Customizer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.project.libraries.LibraryCustomizerContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.DialogDescriptor;
import org.openide.util.Utilities;

/**
 *
 * @author  tom
 */
public class J2SEVolumeCustomizer extends javax.swing.JPanel implements Customizer {

    private String volumeType;
    private LibraryImplementation impl;
    private LibraryStorageArea area;
    private VolumeContentModel model;
    private Boolean allowRelativePaths = null;

    J2SEVolumeCustomizer (String volumeType) {
        this.volumeType = volumeType;
        initComponents();
        postInitComponents ();
        this.setName (NbBundle.getMessage(J2SEVolumeCustomizer.class,"TXT_"+volumeType));
    }


    public void addNotify() {
        super.addNotify();
        this.addButton.requestFocus();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.addButton.setEnabled(enabled);
        addURLButton.setEnabled(volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC));
        int[] indices = content.getSelectedIndices();
        this.removeButton.setEnabled(enabled && indices.length > 0);
        this.downButton.setEnabled(enabled && indices.length > 0 && indices[indices.length-1]<model.getSize()-1);
        this.upButton.setEnabled(enabled && indices.length>0 && indices[0]>0);
    }

    @CheckForNull
    static String[] select(
            @NonNull final String volumeType,
            @NonNull final String libName,
            @NonNull final File[] lastFolder,
            @NullAllowed final Component owner,
            @NullAllowed final File baseFolder) {
        assert volumeType != null;
        assert lastFolder != null;
        assert lastFolder.length == 1;
        final File libFolder = baseFolder == null ?
            null:
            FileUtil.normalizeFile(new File(baseFolder, libName));
        FileChooser chooser = new FileChooser(baseFolder, libFolder);
        // for now variable based paths are disabled in library definition
        // can be revisit if it is needed
        chooser.setFileHidingEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        if (volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
            chooser.setMultiSelectionEnabled (true);
            chooser.setDialogTitle(NbBundle.getMessage(J2SEVolumeCustomizer.class,"TXT_OpenClasses"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new ArchiveFileFilter(NbBundle.getMessage(
                    J2SEVolumeCustomizer.class,"TXT_Classpath"),new String[] {"ZIP","JAR"}));   //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_SelectCP"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_SelectCP").charAt(0));
        } else if (volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC)) {
            chooser.setMultiSelectionEnabled (true);
            chooser.setDialogTitle(NbBundle.getMessage(J2SEVolumeCustomizer.class,"TXT_OpenJavadoc"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new ArchiveFileFilter(NbBundle.getMessage(
                    J2SEVolumeCustomizer.class,"TXT_Javadoc"),new String[] {"ZIP","JAR"}));     //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_SelectJD"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_SelectJD").charAt(0));
        } else if (volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_SRC)) {
            chooser.setMultiSelectionEnabled (true);
            chooser.setDialogTitle(NbBundle.getMessage(J2SEVolumeCustomizer.class,"TXT_OpenSources"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new ArchiveFileFilter(NbBundle.getMessage(
                    J2SEVolumeCustomizer.class,"TXT_Sources"),new String[] {"ZIP","JAR"}));     //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_SelectSRC"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_SelectSRC").charAt(0));
        }
        if (lastFolder[0] != null) {
            chooser.setCurrentDirectory (lastFolder[0]);
        } else if (baseFolder != null) {
            chooser.setCurrentDirectory (baseFolder);
        }
        if (chooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
            try {
                lastFolder[0] = chooser.getCurrentDirectory();
                return chooser.getSelectedPaths();
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    static URI pathToURI(
            final File baseFolder,
            final String fileName,
            final String volume) throws MalformedURLException, URISyntaxException {
        File f = new File(fileName);
        URI uri = LibrariesSupport.convertFilePathToURI(fileName);
        if (baseFolder != null) {
            File realFile = f;
            if (!f.isAbsolute()) {
                    realFile = FileUtil.normalizeFile(new File(
                        baseFolder, f.getPath()));
            }
            String jarPath = checkFile(realFile, volume);
            if (FileUtil.isArchiveFile(Utilities.toURI(realFile).toURL())) {
                uri = LibrariesSupport.getArchiveRoot(uri);
                if (jarPath != null) {
                    assert uri.toString().endsWith("!/") : uri.toString(); //NOI18N
                    uri = URI.create(uri.toString() + encodePath(jarPath));
                }
            } else {
                if (!realFile.isDirectory()) {
                    return null;
                }
                if (!uri.toString().endsWith("/")){ //NOI18N
                    try {
                        uri = new URI(uri.toString()+"/"); //NOI18N
                    } catch (URISyntaxException ex) {
                        throw new AssertionError(ex);
                    }
                }
            }
            return uri;
        } else {
            assert f.isAbsolute() : f.getPath();
            f = FileUtil.normalizeFile (f);
            String jarPath = checkFile(f, volume);
            uri = Utilities.toURI(f);
            if (FileUtil.isArchiveFile(uri.toURL())) {
                uri = LibrariesSupport.getArchiveRoot(uri);
                if (jarPath != null) {
                    assert uri.toString().endsWith("!/") : uri.toString(); //NOI18N
                    uri = URI.create(uri.toString() + encodePath(jarPath));
                }
            } else {
                if (!f.isDirectory()) {
                    return null;
                }
                if (!uri.toString().endsWith("/")){ //NOI18N
                    uri = URI.create(uri.toString()+"/"); //NOI18N
                }
            }
            return uri;
        }
    }


    private void postInitComponents () {
        this.content.setCellRenderer(new ContentRenderer());
        this.upButton.setEnabled (false);
        this.downButton.setEnabled (false);
        this.removeButton.setEnabled (false);
        if (this.volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
            this.addButton.setText (NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_AddClassPath"));
            this.addButton.setMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_AddClassPath").charAt(0));
            this.message.setText(NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_ContentClassPath"));
            this.message.setDisplayedMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_ContentClassPath").charAt(0));
            this.addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEVolumeCustomizer.class,"AD_AddClassPath"));
            this.message.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEVolumeCustomizer.class,"AD_ContentClassPath"));
        }
        else if (this.volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC)) {
            this.addButton.setText(NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_AddJavadoc"));
            this.addButton.setMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_AddJavadoc").charAt(0));
            this.message.setText(NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_ContentJavadoc"));
            this.message.setDisplayedMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_ContentJavadoc").charAt(0));
            this.addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEVolumeCustomizer.class,"AD_AddJavadoc"));
            this.message.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEVolumeCustomizer.class,"AD_ContentJavadoc"));
//            this.addURLButton = new JButton ();
//            this.addURLButton.setText(NbBundle.getMessage (J2SEVolumeCustomizer.class,"CTL_AddJavadocURL"));
//            this.addURLButton.setMnemonic(NbBundle.getMessage (J2SEVolumeCustomizer.class,"MNE_AddJavadocURL").charAt(0));
//            this.addURLButton.addActionListener (new ActionListener () {
//                public void actionPerformed(ActionEvent e) {
//                    addURLResource ();
//                }
//            });
//            GridBagConstraints c = new GridBagConstraints();
//            c.gridx = 1;
//            c.gridy = 2;
//            c.gridwidth = GridBagConstraints.REMAINDER;
//            c.gridheight = 1;
//            c.fill = GridBagConstraints.HORIZONTAL;
//            c.anchor = GridBagConstraints.NORTHWEST;
//            c.insets = new Insets (0,6,5,6);
//            ((GridBagLayout)this.getLayout()).setConstraints(this.addURLButton,c);
//            this.add (this.addURLButton);
        }
        else if (this.volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_SRC)) {
            this.addButton.setText (NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_AddSources"));
            this.addButton.setMnemonic (NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_AddSources").charAt(0));
            this.message.setText(NbBundle.getMessage(J2SEVolumeCustomizer.class,"CTL_ContentSources"));
            this.message.setDisplayedMnemonic(NbBundle.getMessage(J2SEVolumeCustomizer.class,"MNE_ContentSources").charAt(0));
            this.addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEVolumeCustomizer.class,"AD_AddSources"));
            this.message.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2SEVolumeCustomizer.class,"AD_ContentSources"));
        }
        this.content.addListSelectionListener(new ListSelectionListener () {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                int[] indices = content.getSelectedIndices();
                removeButton.setEnabled(indices.length > 0);
                downButton.setEnabled(indices.length > 0 && indices[indices.length-1]<model.getSize()-1);
                upButton.setEnabled(indices.length>0 && indices[0]>0);
            }
        });
        //#143481
        content.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        content.getActionMap().put("delete", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeResource(null);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        message = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        content = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        addURLButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        message.setLabelFor(content);
        org.openide.awt.Mnemonics.setLocalizedText(message, org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("CTL_ContentMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 2, 6);
        add(message, gridBagConstraints);

        jScrollPane1.setViewportView(content);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("CTL_AddContent")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addResource(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("AD_addButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addURLButton, NbBundle.getMessage(J2SEVolumeCustomizer.class, "J2SEVolumeCustomizer.addURLButton.text")); // NOI18N
        addURLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addURLButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 6);
        add(addURLButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("CTL_RemoveContent")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeResource(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 6);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("AD_RemoveContent")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("CTL_UpContent")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upResource(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 6);
        add(upButton, gridBagConstraints);
        upButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("AD_UpContent")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("CTL_DownContent")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downResource(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 6);
        add(downButton, gridBagConstraints);
        downButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("AD_DownContent")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2SEVolumeCustomizer.class).getString("AD_J2SEVolumeCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void downResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downResource
        int[] indices = this.content.getSelectedIndices();
        if (indices.length == 0 || indices[0] < 0 || indices[indices.length-1]>=model.getSize()-1) {
            return;
        }
        this.model.moveDown(indices);
        for (int i=0; i< indices.length; i++) {
            indices[i] = indices[i] + 1;
        }
        this.content.setSelectedIndices (indices);
    }//GEN-LAST:event_downResource

    private void upResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upResource
        int[] indices = this.content.getSelectedIndices();
        if (indices.length == 0 || indices[0] <= 0) {
            return;
        }
        this.model.moveUp(indices);
        for (int i=0; i< indices.length; i++) {
            indices[i] = indices[i] - 1;
        }
        this.content.setSelectedIndices(indices);
    }//GEN-LAST:event_upResource

    private void removeResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeResource
        int[] indices =this.content.getSelectedIndices();
        if (indices.length == 0) {
            return;
        }
        this.model.removeResources(indices);
        if (indices[indices.length-1]-indices.length+1 < this.model.getSize()) {
            this.content.setSelectedIndex(indices[indices.length-1]-indices.length+1);
        }
        else if (indices[0]  >= 1) {
            this.content.setSelectedIndex (indices[0]-1);
        }
        if (this.volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
            impl.setContent(J2SELibraryTypeProvider.VOLUME_TYPE_MAVEN_POM, Collections.<URL>emptyList());
        }
    }//GEN-LAST:event_removeResource

    private void addResource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addResource
        final boolean arp = allowRelativePaths != null && allowRelativePaths.booleanValue();
        final File baseFolder = arp ?
            FileUtil.normalizeFile(Utilities.toFile(URI.create(area.getLocation().toExternalForm())).getParentFile()):
            null;
        final File[] cwd = new File[]{lastFolder};
        final String[] paths = select(volumeType, impl.getName(), cwd, this, baseFolder);
        if (paths != null) {
            try {
                lastFolder = cwd[0];
                addFiles (
                    pathsToURIs (
                        paths,
                        volumeType,
                        baseFolder),
                    arp);
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            } catch (URISyntaxException ue) {
                Exceptions.printStackTrace(ue);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_addResource

    private void addURLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addURLButtonActionPerformed
        DialogDescriptor.InputLine input = new DialogDescriptor.InputLine(
                NbBundle.getMessage(J2SEVolumeCustomizer.class, "CTL_AddJavadocURLMessage"),
                NbBundle.getMessage(J2SEVolumeCustomizer.class, "CTL_AddJavadocURLTitle"));
        if (DialogDisplayer.getDefault().notify(input) == DialogDescriptor.OK_OPTION) {

            final String value = input.getInputText();
            if (allowRelativePaths != null && allowRelativePaths.booleanValue()) {
                try {
                    final URI uri = new URI(value);
                    model.addResource(uri);
                    content.setSelectedIndex(model.getSize() - 1);
                } catch (URISyntaxException use) {
                    DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                    NbBundle.getMessage(J2SEVolumeCustomizer.class, "CTL_InvalidURLFormat"),
                    DialogDescriptor.ERROR_MESSAGE));
                }
            } else {
                try {
                    final URL url = new URL(value);
                    model.addResource(url);
                    content.setSelectedIndex(model.getSize() - 1);
                } catch (MalformedURLException mue) {
                    DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                    NbBundle.getMessage(J2SEVolumeCustomizer.class, "CTL_InvalidURLFormat"),
                    DialogDescriptor.ERROR_MESSAGE));
                }
            }
        }
    }//GEN-LAST:event_addURLButtonActionPerformed

    private void addFiles (URI[] toAdd, boolean  allowRelativePaths) throws MalformedURLException, URISyntaxException {
        int firstIndex = this.model.getSize();
        for (URI uri : toAdd) {
            if (allowRelativePaths) {
                model.addResource(uri);
            } else {
                model.addResource(uri.toURL()); //Has to be added as URL, model asserts it
            }
        }
        int lastIndex = this.model.getSize()-1;
        if (firstIndex<=lastIndex) {
            int[] toSelect = new int[lastIndex-firstIndex+1];
            for (int i = 0; i < toSelect.length; i++) {
                toSelect[i] = firstIndex+i;
            }
            this.content.setSelectedIndices(toSelect);
        }
        if (this.volumeType.equals(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
            if (impl != null) {
                impl.setContent(J2SELibraryTypeProvider.VOLUME_TYPE_MAVEN_POM, Collections.<URL>emptyList());
            }
        }
    }

    static String encodePath(String path) throws URISyntaxException {
        return new URI(null, null, path, null).getRawPath();
    }

    @NonNull
    private static URI[] pathsToURIs(
            @NonNull final String[] fileNames,
            @NonNull final String volume,
            @NullAllowed final File baseFolder) throws MalformedURLException, URISyntaxException {
        final List<URI> result = new ArrayList<URI>(fileNames.length);
        for (String fileName : fileNames) {
            final URI uri = pathToURI(baseFolder,fileName,volume);
            if (uri != null) {
                result.add(uri);
            }
        }
        return result.toArray(new URI[result.size()]);
    }

    private static String checkFile(File f, String volume) {
        FileObject fo = FileUtil.toFileObject(f);
        if (volume.equals(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC)) {
            if (fo != null) {
                if (fo.isData()) {
                    fo = FileUtil.getArchiveRoot(fo);
                }
                FileObject root = fo == null ? null : JavadocAndSourceRootDetection.findJavadocRoot(fo);
                if (root == null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(J2SEVolumeCustomizer.class,"TXT_InvalidJavadocRoot", f.getPath()), //NOI18N
                        NotifyDescriptor.ERROR_MESSAGE));
                    return null;
                } else {
                    if (root.getFileObject("index-files") == null && root.getFileObject("index-all.html") == null) {    //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(J2SEVolumeCustomizer.class,"TXT_InvalidJavadocRoot2", f.getPath()), //NOI18N
                        NotifyDescriptor.ERROR_MESSAGE));
                    }
                    return FileUtil.getRelativePath(fo, root)+"/"; // NOI18N
                }
            }
        } else if (volume.equals(J2SELibraryTypeProvider.VOLUME_TYPE_SRC)) {
            if (fo != null) {
                if (fo.isData()) {
                    fo = FileUtil.getArchiveRoot(fo);
                }
                FileObject root = fo == null ? null : JavadocAndSourceRootDetection.findSourceRoot(fo);
                if (root == null) {
                    // TODO: warn user that no source root was found
                    return null;
                }
                if (FileUtil.isParentOf(root,fo)) {
                    // TODO: warn user that selected folder is under source root
                    return null;
                }
                else {
                    assert fo.equals(root) || FileUtil.isParentOf(fo, root) : fo.toString()+" is not parent of "+root; // NOI18N
                    return FileUtil.getRelativePath(fo, root)+"/"; // NOI18N
                }
            }
        }
        return null;
    }

    public void setObject(Object bean) {
        assert bean instanceof LibraryCustomizerContext : bean.getClass();
        LibraryCustomizerContext context = (LibraryCustomizerContext)bean;
        area = context.getLibraryStorageArea();
        impl = context.getLibraryImplementation();
        allowRelativePaths = Boolean.valueOf(context.getLibraryImplementation2() != null);
        model = new VolumeContentModel(impl, area, volumeType);
        content.setModel(model);
        if (model.getSize()>0) {
            content.setSelectedIndex(0);
        }
    }


    private static class ArchiveFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public ArchiveFileFilter(String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring(index+1).toUpperCase();
            if (!this.extensions.contains(extension)) {
                return false;
            }
            try {
                return FileUtil.isArchiveFile (Utilities.toURI(f).toURL());
            } catch (MalformedURLException e) {
                Exceptions.printStackTrace(e);
                return false;
            }
        }

        public String getDescription() {
            return this.description;
        }
    }


    private static File lastFolder = null;


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addURLButton;
    private javax.swing.JList content;
    private javax.swing.JButton downButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel message;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    private static class ContentRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName = null;
            Color color = null;
            String toolTip = null;

            URI uri = null;
            if (value instanceof URI) {
                uri = (URI)value;
            } else if (value instanceof URL) {
                try {
                    uri = ((URL) value).toURI();
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (uri != null && uri.toString().startsWith("http")) {
                displayName = uri.toString();
            } else if (uri != null) {
                if (uri.toString().contains("!/")) {   //NOI18N
                    uri = LibrariesSupport.getArchiveFile(uri);
                }
                boolean broken = false;
                VolumeContentModel model = (VolumeContentModel)list.getModel();
                LibraryStorageArea area = model.getArea();
                FileObject fo = LibrariesSupport.resolveLibraryEntryFileObject(area != null ? area.getLocation() : null, uri);
                if (fo == null) {
                    broken = true;
                    if ("file".equals(uri.getScheme())) { //NOI18N
                        displayName = LibrariesSupport.convertURIToFilePath(uri);
                        if (displayName.startsWith("${")) { // NOI18N
                            // if URL starts with an Ant property name assume it is OK.
                            // url cannot be resolved because customizer does not have necessary context.
                            // for example in case of hand written library entry ${MAVEN_REPO}/struts/struts.jar
                            broken = false;
                        }
                    } else {
                        displayName = uri.toString();
                    }
                } else {
                    if (uri.isAbsolute()) {
                        displayName = FileUtil.getFileDisplayName(fo);
                    } else {
                        displayName = LibrariesSupport.convertURIToFilePath(uri);
                        toolTip = FileUtil.getFileDisplayName(fo);
                    }
                }
                if (broken) {
                    color = new Color (164,0,0);
                    toolTip = NbBundle.getMessage (J2SEVolumeCustomizer.class,"TXT_BrokenFile");
                }
            }
            Component c = super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            if (c instanceof JComponent) {
                if (color != null) {
                    ((JComponent)c).setForeground (color);
                }
                if (toolTip != null) {
                    ((JComponent)c).setToolTipText(toolTip);
                } else {
                    ((JComponent)c).setToolTipText(null);
                }
            }
            return c;
        }

    }

}
