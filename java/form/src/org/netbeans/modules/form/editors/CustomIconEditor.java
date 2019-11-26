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

package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.form.editors.IconEditor.NbImageIcon;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * UI for choosing an icon. Custom editor for icon property editor (IconEditor).
 * 
 * @author Tomas Pavek
 */
public class CustomIconEditor extends javax.swing.JPanel {

    private IconEditor propertyEditor;

    private FileObject packageRoot;
    private FileObject selectedPackage;
    private FileObject selectedCPFile;

    private File selectedExternalFile;
    private String selectedURL;
    private static String lastDirectoryUsed; // for file chooser

    private boolean ignoreSetValue;
    private boolean ignoreNull;
    private boolean ignoreCombo;

    private Icon packageIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/package.gif", false); // NOI18N

    public CustomIconEditor(IconEditor prEd) {
        propertyEditor = prEd;

        initComponents();

        scrollPane.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        setupBrowseButton(browseFileButton);
        setupBrowseButton(browseExternalButton);
        if (!prEd.isExternalIconsAllowed()) {
            externalRadio.setEnabled(false);
            urlField.setEnabled(false);
            browseExternalButton.setEnabled(false);
        }

        packageCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setIcon(packageIcon);
                return this;
            }
        });
        fileCombo.setPrototypeDisplayValue(NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor_FileCombo_Select")); // NOI18N
        fileCombo.setRenderer(new IconComboRenderer());

        setTransferHandler(new FileDropHandler());
    }

    private static void setupBrowseButton(JButton button) {
        Insets margin = button.getMargin();
        if (margin.left > 4) {
            margin.left = 4;
            margin.right = 4;
            button.setMargin(margin);
        }
    }

    /**
     * Receives the property value (icon) from the property editor. Sets up the
     * GUI accordingly.
     */
    void setValue(NbImageIcon nbIcon) {
        if (ignoreSetValue || (nbIcon == null && ignoreNull))
            return;

        selectedCPFile = null;
        selectedExternalFile = null;
        selectedURL = null;
        ignoreCombo = true;
        if (fileCombo.getItemCount() > 0)
            fileCombo.setSelectedIndex(0);
        ignoreCombo = false;
        urlField.setText(""); // NOI18N
        
        if ((nbIcon == null) || (nbIcon.getType() != IconEditor.TYPE_CLASSPATH)) {
            FileObject sourceFile = propertyEditor.getSourceFile();
            ClassPath cp = ClassPath.getClassPath(sourceFile, ClassPath.SOURCE);
            if (cp != null) {
                FileObject defaultFolder = propertyEditor.getDefaultResourceFolder();
                setPackageRoot(cp.findOwnerRoot(defaultFolder));
                setPackage(defaultFolder);
            } else {
                externalRadio.setSelected(true);
                classPathRadio.setEnabled(false);
                packageCombo.setEnabled(false);
                fileCombo.setEnabled(false);
                browseFileButton.setEnabled(false);
                importButton.setEnabled(false);
                return;
            }
        }
        
        if (nbIcon == null) {
            classPathRadio.setSelected(true);
            previewLabel.setIcon(null);
            return;
        }

        switch (nbIcon.getType()) {
            case IconEditor.TYPE_CLASSPATH:
                setFromResourceName(nbIcon.getName(), true);
                classPathRadio.setSelected(true);
                break;
            case IconEditor.TYPE_FILE:
                setFromFileName(nbIcon.getName());
                externalRadio.setSelected(true);
                break;
            case IconEditor.TYPE_URL:
                setFromURL(nbIcon.getName());
                externalRadio.setSelected(true);
                break;
        }

        previewLabel.setIcon(nbIcon.getIcon());
    }

    private void setFromResourceName(String resName, boolean setDefaultIfInvalid) {
        if (resName.startsWith("/")) // NOI18N
            resName = resName.substring(1);

        FileObject sourceFile = propertyEditor.getSourceFile();
        ClassPath sourceCP = ClassPath.getClassPath(sourceFile, ClassPath.SOURCE);
        ClassPath execCP = null;
        ClassPath cp = sourceCP;
        FileObject fo = cp.findResource(resName);
        if (fo == null) {
            execCP = ClassPath.getClassPath(sourceFile, ClassPath.EXECUTE);
            cp = execCP;
            fo = cp.findResource(resName);
            if (fo != null) {
                FileObject[] roots = findSourceRoots(fo);
                if ((roots == null) || (roots.length == 0)) {
                    // icon is on execution path only
                    // e.g. in some jar that is not built by the IDE
                } else {
                    // icon comes from some source path
                    FileObject fob = findSourceRootOf(roots, resName);
                    if (fob == null) {
                        // the icon is on execution path, but no longer
                        // on source path - it was probably deleted recently
                        // but the corresponding artifact wasn't rebuilt
                        fo = null;
                        cp = sourceCP;
                    } else {
                        // the icon was found on source path of its corresponding project
                        fo = fob;
                        cp = ClassPath.getClassPath(fob, ClassPath.SOURCE);
                    }
                }
            }
        }
        if (fo != null) {
            setPackageRoot(cp.findOwnerRoot(fo));
            setPackage(fo.getParent());
            setPackageFile(fo);
        }
        else if (setDefaultIfInvalid) {
            FileObject folder = null;
            String pkgName;
            int i = resName.lastIndexOf('/');
            if (i < 0) {
                pkgName = null; // NOI18N
            }
            else {
                pkgName = resName.substring(0, i);
                cp = sourceCP;
                folder = cp.findResource(pkgName);
                if (folder == null) {
                    cp = execCP;
                    folder = cp.findResource(pkgName);
                    if (folder != null) {
                        FileObject[] roots = findSourceRoots(folder);
                        if ((roots != null) && (roots.length > 0)) {
                            FileObject fob = findSourceRootOf(roots, pkgName);
                            if (fob == null) {
                                folder = null;
                            } else {
                                folder = fob;
                                cp = ClassPath.getClassPath(fob, ClassPath.SOURCE);
                            }
                        }
                    }
                } else if (cp.findAllResources(pkgName).size() > 1) {
                    folder = null; // if the package exists under more roots, e.g. in maven project, we don't know which one to pick
                }
            }
            if (folder == null) {
                folder = propertyEditor.getDefaultResourceFolder();
                cp = sourceCP;
            }
            setPackageRoot(cp.findOwnerRoot(folder));
            setPackage(folder);
        }
    }

    private FileObject[] findSourceRoots(FileObject fo) {
        try {
            ClassPath cp = ClassPath.getClassPath(propertyEditor.getSourceFile(), ClassPath.EXECUTE);
            return SourceForBinaryQuery.findSourceRoots(cp.findOwnerRoot(fo).getURL()).getRoots();
        } catch (FileStateInvalidException fsiex) {
            Logger.getLogger(CustomIconEditor.class.getName()).log(Level.INFO, null, fsiex);
        }
        return null;
    }

    private FileObject findSourceRootOf(FileObject[] roots, String resName) {
        for (FileObject root : roots) {
            ClassPath resCP = ClassPath.getClassPath(root, ClassPath.SOURCE);
            if (resCP != null) {
                FileObject res = resCP.findResource(resName);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private void setFromFileName(String fileName) {
        selectedExternalFile = new File(fileName);
        try {
            urlField.setText(selectedExternalFile.toURI().toURL().toExternalForm());
        }
        catch (MalformedURLException ex) {
            urlField.setText("file:/" + fileName); // NOI18N
        }
    }

    private void setFromURL(String urlString) {
        selectedURL = urlString;
        urlField.setText(selectedURL);
    }

    private void setPackageRoot(FileObject root) {
        if (root != packageRoot) {
            packageCombo.setModel(createPackageComboModel(root));
            packageRoot = root;
        }
    }

    private void setPackage(FileObject folder) {
        if (folder != selectedPackage) {
            selectedPackage = folder;
            ignoreCombo = true;
            packageCombo.setSelectedItem(getPackageName(folder, packageRoot));
            ignoreCombo = false;
            fileCombo.setModel(createFileComboModel(folder));
        }
    }

    private void setPackageFile(FileObject fo) {
        selectedCPFile = null;
        for (int i=1, n=fileCombo.getModel().getSize(); i < n; i++) {
            IconFileItem item = (IconFileItem) fileCombo.getModel().getElementAt(i);
            if (item.file.equals(fo)) {
                selectedCPFile = fo;
                ignoreCombo = true;
                fileCombo.setSelectedIndex(i);
                ignoreCombo = false;
                break;
            }
        }
    }

    private void setExternalFile(File file) {
        setExternalAsCPFile(file); // also check if the file isn't actually on classpath (in sources)
        selectedExternalFile = file;
        selectedURL = null;
    }

    /**
     * Tries to find given file on classpath and set it as selected classpath file.
     * @return true if the file is on classpath
     */
    private boolean setExternalAsCPFile(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            ClassPath cp = ClassPath.getClassPath(propertyEditor.getSourceFile(), ClassPath.SOURCE);
            if (cp.contains(fo)) {
                setPackageRoot(cp.findOwnerRoot(fo));
                setPackage(fo.getParent());
                setPackageFile(fo);
                return true;
            }
        }
        return false;
    }

    private void switchFromCPToExternal() {
        if (isClassPathSelected()) {
            if (selectedCPFile != null && selectedExternalFile == null && selectedURL == null) {
                selectedExternalFile = FileUtil.toFile(selectedCPFile);
                try {
                    urlField.setText(selectedExternalFile.toURI().toURL().toExternalForm());
                }
                catch (MalformedURLException ex) { // should not happen for existing file
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    /**
     * Constructs the value (NbImageIcon) according to the current state of the
     * GUI and sets it to the property editor.
     */
    private void updateValue() {
        int type = -1;
        String name = null;
        Icon icon = null;
        if (isClassPathSelected()) {
            if (selectedCPFile != null) {
                name = FileUtil.getRelativePath(packageRoot, selectedCPFile);
                try {
                    try {
                        Image image = ImageIO.read(selectedCPFile.toURL());
                        if (image != null) {
                            icon = new ImageIcon(image);
                            type = IconEditor.TYPE_CLASSPATH;
                        } // no NbImageIcon will be created for invalid file
                    } catch (IllegalArgumentException iaex) { // Issue 178906
                        Logger.getLogger(CustomIconEditor.class.getName()).log(Level.INFO, null, iaex);
                        icon = new ImageIcon(selectedCPFile.toURL());
                        type = IconEditor.TYPE_CLASSPATH;
                    }
                } catch (IOException ex) { // should not happen
                    Logger.getLogger(CustomIconEditor.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
        else if (isExternalSelected()) {
            if (selectedExternalFile != null) {
                name = selectedExternalFile.getAbsolutePath();
                try {
                    try {
                        Image image = ImageIO.read(new File(name));
                        if (image != null) {
                            icon = new ImageIcon(image);
                            type = IconEditor.TYPE_FILE;
                        } // no NbImageIcon will be created for invalid file
                    } catch (IllegalArgumentException iaex) { // Issue 178906
                        Logger.getLogger(CustomIconEditor.class.getName()).log(Level.INFO, null, iaex);
                        icon = new ImageIcon(name);
                        type = IconEditor.TYPE_FILE;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CustomIconEditor.class.getName()).log(Level.WARNING, null, ex);
                }
            }
            else if (selectedURL != null && !"".equals(selectedURL)) { // NOI18N
                type = IconEditor.TYPE_URL;
                name = selectedURL;
                try {
                    try {
                        Image image = ImageIO.read(new URL(selectedURL));
                        if (image != null) {
                            icon = new ImageIcon(image);
                        }
                    } catch (IllegalArgumentException iaex) { // Issue 178906
                        Logger.getLogger(CustomIconEditor.class.getName()).log(Level.INFO, null, iaex);
                        icon = new ImageIcon(new URL(selectedURL));
                    }
                    // for URL-based icon create NbImageIcon even if no icon can be loaded from the URL
                } catch (IOException ex) {
                    Logger.getLogger(CustomIconEditor.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }

        ignoreSetValue = true;
        try {
            propertyEditor.setValue(type != -1 ? new NbImageIcon(type, name, icon) : null);
        }
        finally {
            ignoreSetValue = false;
        }

        previewLabel.setIcon(icon);
    }

    private boolean isClassPathSelected() {
        return classPathRadio.isSelected();
    }

    private boolean isExternalSelected() {
        return externalRadio.isSelected();
    }

    // -----

    private static ComboBoxModel createPackageComboModel(FileObject root) {
        // can't use PackageView from java project support - it only works with sources
        TreeSet<String> data = new TreeSet<String>();
        collectPackages(root, root, data);
        return new DefaultComboBoxModel(new Vector<String>(data));
    }

    private static void collectPackages(FileObject folder, FileObject root, Collection<String> col) {
        assert folder.isFolder();
        boolean hasSubfolders = false;
        boolean hasFiles = false;
        for (FileObject fo : folder.getChildren()) {
            if (fo.isFolder() && VisibilityQuery.getDefault().isVisible(fo)) {
                collectPackages(fo, root, col);
                hasSubfolders = true;
            }
            else {
                hasFiles = true;
            }
        }
        if (hasFiles || !hasSubfolders) {
            col.add(getPackageName(folder, root));
        }
    }

    private static String getPackageName(FileObject folder, FileObject root) {
        String path = FileUtil.getRelativePath(root, folder);
        return "".equals(path) ? // NOI18N
                "<default>" : path.replace('/', '.'); // NOI18N
    }

    private static ComboBoxModel createFileComboModel(FileObject folder) {
//        if (folder == null)
//            return new DefaultComboBoxModel();

        TreeSet<IconFileItem> data = new TreeSet<IconFileItem>();
        int maxIconW = 0;
        int maxIconH = 0;
        for (FileObject fo : folder.getChildren()) {
            if (IconEditor.isImageFile(fo)) {
                IconFileItem ifi = new IconFileItem(fo);
                data.add(ifi);

                Dimension iconSize = ifi.getScaledSize();
                if (iconSize.width > maxIconW) {
                    maxIconW = iconSize.width;
                }
                if (iconSize.height > maxIconH) {
                    maxIconH = iconSize.height;
                }
            }
        }
        for (IconFileItem ifi : data) {
            ifi.setEffectiveSize(maxIconW, maxIconH);
        }

        Vector<Object> v = new Vector<Object>(data.size()+1);
        v.add(NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor_FileCombo_Select")); // NOI18N
        v.addAll(data);
        return new DefaultComboBoxModel(v);
    }

    private static class IconFileItem implements Comparable, Icon {
        private FileObject file;
        private ImageIcon icon;
        private boolean scaled;
        private int maxW;
        private int maxH;

        private static final int MAX_W = 32;
        private static final int MAX_H = 32;

        private static final long SIZE_LIMIT = 50000;

        IconFileItem(FileObject file) {
            this.file = file;
            try {
                try {
                    Image image = (file.getSize() < SIZE_LIMIT) ? ImageIO.read(file.toURL()) : null;
                    icon = (image != null) ? new ImageIcon(image) : null;
                } catch (IllegalArgumentException iaex) { // Issue 178906
                    Logger.getLogger(CustomIconEditor.class.getName()).log(Level.INFO, null, iaex);
                    icon = new ImageIcon(file.toURL());
                }
            } catch (IOException ex) {
                Logger.getLogger(CustomIconEditor.class.getName()).log(Level.WARNING, null, ex);
            }
        }

        @Override
        public String toString() {
            return file.getNameExt();
        }

        @Override
        public int compareTo(Object obj) {
            return toString().compareTo(obj.toString());
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (icon == null) {
                return;
            }
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            if (!scaled) {
                if (w > maxW || h > maxH) {
                    Dimension size = getScaledSize();
                    w = size.width;
                    h = size.height;
                    icon.setImage(icon.getImage().getScaledInstance(w, h, Image.SCALE_FAST));
                }
                scaled = true;
            }
            icon.paintIcon(c, g, x + ((maxW - w) / 2), y + ((maxH - h) / 2));
        }

        @Override
        public int getIconWidth() {
            return maxW;
        }

        @Override
        public int getIconHeight() {
            return maxH;
        }

        Dimension getScaledSize() {
            if (icon == null) {
                return new Dimension(0, 0);
            }
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            if (w > MAX_W || h > MAX_H) {
                float ratio = ((float)w) / ((float)h);
                if (w > h) {
                    w = MAX_W;
                    h = Math.max(1, Math.round(((float)MAX_W) / ratio));
                } else {
                    h = MAX_H;
                    w = Math.max(1, Math.round(((float)MAX_H) * ratio));
                }
            }
            return new Dimension(w, h);
        }

        void setEffectiveSize(int w, int h) {
            maxW = w > 0 ? w : MAX_W;
            maxH = h > 0 ? h : MAX_H;
        }
    }

    private class IconComboRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list,
                    value != null ? value.toString() : value,
                    index, isSelected, cellHasFocus);
            setIcon(value instanceof IconFileItem ? (IconFileItem) value : null);
            return this;
        }

        @Override
        public void paintComponent(Graphics g) {
            Icon icon = getIcon();
            if (icon != null && !fileCombo.isPopupVisible()) {
                // try not to paint the icon in the combo box itself (only in popup list)
                setIcon(null);
            } else {
                icon = null;
            }
            super.paintComponent(g);
            if (icon != null) {
                setIcon(icon);
            }
        }
    }

    // -----

    private class FileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            for (DataFlavor f : flavors) {
                if (DataFlavor.javaFileListFlavor.equals(f)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            }
            List files = null;
            try {
                files = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return false;
            }

            if (files.size() > 0) {
                File file = (File) files.get(0);
                if (file != null && IconEditor.isImageFileName(file.getName())) {
                    if (setExternalAsCPFile(file)) { // the file is on classpath
                        classPathRadio.setSelected(true);
                    } else {
                        selectedExternalFile = file;
                        selectedURL = null;
                        externalRadio.setSelected(true);
                        try {
                            urlField.setText(file.toURI().toURL().toExternalForm());
                        } catch (MalformedURLException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                    updateValue();
                    return true;
                }
            }
            return false;
        }
    }

    // -----

    private void selectClassPathFile() {
        ClassPathFileChooser chooser = new ClassPathFileChooser(
                    propertyEditor.getSourceFile(),
                    new ClassPathFileChooser.Filter() {
                        @Override
                        public boolean accept(FileObject fo) {
                            return fo.isFolder() || IconEditor.isImageFileName(fo.getNameExt());
                        }
                    },
                    false, true);
        try {
            if (selectedCPFile != null) {
                chooser.setSelectedFile(selectedCPFile);
            } else {
                chooser.setSelectedFile(selectedPackage);
                chooser.setSelectedFile(null);
            }
        } catch (IllegalArgumentException iaex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iaex);
        }
        chooser.getDialog(NbBundle.getMessage(CustomIconEditor.class, "CTL_OpenDialogName"), null)// NOI18N
            .setVisible(true);

        if (chooser.isConfirmed()) {
            setPackageRoot(chooser.getSelectedPackageRoot());
            FileObject fo = chooser.getSelectedFile();
            setPackage(fo.getParent());
            setPackageFile(fo);
            classPathRadio.setSelected(true);
            updateValue();
        }
    }

    private String getFileChooserDir() {
        if (lastDirectoryUsed == null && selectedPackage != null) {
            lastDirectoryUsed = FileUtil.toFile(selectedPackage).getPath();
        }
        return lastDirectoryUsed;
    }

    private void selectExternalFile() {
        JFileChooser fileChooser = new JFileChooser(getFileChooserDir());
        fileChooser.setDialogTitle(NbBundle.getMessage(CustomIconEditor.class, "CTL_OpenDialogName")); // NOI18N
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || IconEditor.isImageFileName(f.getName());
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(CustomIconEditor.class, "CTL_ImagesExtensionName"); // NOI18N
            }
        });
        if (fileChooser.showOpenDialog(getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                try {
                    urlField.setText(file.toURI().toURL().toExternalForm());
                }
                catch (MalformedURLException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                externalRadio.setSelected(true);
            }
            lastDirectoryUsed = file.getParent();
            setExternalFile(file);
            updateValue();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        scrollPane = new javax.swing.JScrollPane();
        previewLabel = new javax.swing.JLabel();
        classPathRadio = new javax.swing.JRadioButton();
        externalRadio = new javax.swing.JRadioButton();
        fileLabel = new javax.swing.JLabel();
        packageLabel = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        browseExternalButton = new javax.swing.JButton();
        fileCombo = new javax.swing.JComboBox();
        packageCombo = new javax.swing.JComboBox();
        importButton = new javax.swing.JButton();
        noIconRadio = new javax.swing.JRadioButton();
        urlField = new javax.swing.JTextField();
        browseFileButton = new javax.swing.JButton();

        previewLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        scrollPane.setViewportView(previewLabel);

        buttonGroup1.add(classPathRadio);
        classPathRadio.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.classPathRadio.text")); // NOI18N
        classPathRadio.setToolTipText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.classPathRadio.toolTipText")); // NOI18N
        classPathRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classPathRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        classPathRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classPathRadioActionPerformed(evt);
            }
        });

        buttonGroup1.add(externalRadio);
        externalRadio.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.externalRadio.text")); // NOI18N
        externalRadio.setToolTipText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.externalRadio.toolTipText")); // NOI18N
        externalRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        externalRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        externalRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalRadioActionPerformed(evt);
            }
        });

        fileLabel.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.fileLabel.text")); // NOI18N

        packageLabel.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.packageLabel.text")); // NOI18N

        urlLabel.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.urlLabel.text")); // NOI18N

        browseExternalButton.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.browseExternalButton.text")); // NOI18N
        browseExternalButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.browseExternalButton.toolTipText")); // NOI18N
        browseExternalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseExternalButtonActionPerformed(evt);
            }
        });

        fileCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileComboActionPerformed(evt);
            }
        });

        packageCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageComboActionPerformed(evt);
            }
        });

        importButton.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.importButton.text")); // NOI18N
        importButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.importButton.toolTipText")); // NOI18N
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(noIconRadio);
        noIconRadio.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.noIconRadio.text")); // NOI18N
        noIconRadio.setToolTipText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.noIconRadio.toolTipText")); // NOI18N
        noIconRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noIconRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noIconRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noIconRadioActionPerformed(evt);
            }
        });

        urlField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlFieldActionPerformed(evt);
            }
        });

        browseFileButton.setText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.browseFileButton.text")); // NOI18N
        browseFileButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomIconEditor.class, "CustomIconEditor.browseFileButton.toolTipText")); // NOI18N
        browseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .addComponent(classPathRadio, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(externalRadio, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileLabel)
                            .addComponent(packageLabel)
                            .addComponent(urlLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(urlField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseExternalButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fileCombo, 0, 293, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseFileButton))
                            .addComponent(importButton)
                            .addComponent(packageCombo, 0, 345, Short.MAX_VALUE)))
                    .addComponent(noIconRadio, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(classPathRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageLabel)
                    .addComponent(packageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(fileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseFileButton))
                .addGap(11, 11, 11)
                .addComponent(externalRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browseExternalButton)
                    .addComponent(urlLabel)
                    .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importButton)
                .addGap(11, 11, 11)
                .addComponent(noIconRadio)
                .addGap(11, 11, 11)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        File[] files = selectedExternalFile != null ?
            files = new File[] { selectedExternalFile } : null;
        FileObject srcFile = propertyEditor.getSourceFile();
        ImportImageWizard.lastDirectoryUsed = getFileChooserDir();
        FileObject[] imported = new ImportImageWizard(files, selectedPackage, srcFile).show();
        lastDirectoryUsed = ImportImageWizard.lastDirectoryUsed;
        FileObject fo = imported != null && imported.length > 0 ?  imported[0] : null;
        if (fo != null) {
            ClassPath cp = ClassPath.getClassPath(srcFile, ClassPath.SOURCE);
            if (cp.contains(fo)) {
                setPackageRoot(cp.findOwnerRoot(fo));
                selectedPackage = null; // to be sure it is refreshed
                setPackage(fo.getParent());
                setPackageFile(fo);
                classPathRadio.setSelected(true);
                updateValue();
            }
        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void urlFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlFieldActionPerformed
        String text = urlField.getText();
        if (selectedPackage != null) { // use current package to resolve short file names
            propertyEditor.setCurrentFolder(selectedPackage);
        }
        ignoreNull = true; // do not set no icon from text field
        try {
            propertyEditor.setAsText(text);
        }
        finally {
            ignoreNull = false;
        }
        if (propertyEditor.getValue() instanceof NbImageIcon) {
            setValue((NbImageIcon)propertyEditor.getValue());
            switchFromCPToExternal();
        }
        else if (!"".equals(text.trim())) { // not a valid text // NOI18N
            urlField.setText(text);
            urlField.setSelectionStart(0);
            urlField.setSelectionEnd(text.length());
            Toolkit.getDefaultToolkit().beep();
        }
        externalRadio.setSelected(true);
    }//GEN-LAST:event_urlFieldActionPerformed

    private void classPathRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classPathRadioActionPerformed
        updateValue();
    }//GEN-LAST:event_classPathRadioActionPerformed

    private void externalRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalRadioActionPerformed
        updateValue();
    }//GEN-LAST:event_externalRadioActionPerformed

    private void noIconRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noIconRadioActionPerformed
        updateValue();
    }//GEN-LAST:event_noIconRadioActionPerformed

    private void fileComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileComboActionPerformed
        if (!ignoreCombo) { // only if triggered directly by user
            Object item = fileCombo.getSelectedItem();
            if (item instanceof IconFileItem) {
                selectedCPFile = ((IconFileItem) item).file;
                classPathRadio.setSelected(true);
            }
            else selectedCPFile = null;
            updateValue();
        }
    }//GEN-LAST:event_fileComboActionPerformed

    private void browseExternalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseExternalButtonActionPerformed
        selectExternalFile(); // will also set the CP file if the file is actually on project's CP
    }//GEN-LAST:event_browseExternalButtonActionPerformed

    private void browseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFileButtonActionPerformed
        selectClassPathFile();
    }//GEN-LAST:event_browseFileButtonActionPerformed

    private void packageComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageComboActionPerformed
        if (!ignoreCombo) {// only if triggered directly by user
            String pkgName = packageCombo.getSelectedItem().toString();
            selectedPackage = packageRoot.getFileObject(pkgName.replace('.', '/'));
            if (selectedPackage == null) {
                selectedPackage = packageRoot;
            }
            fileCombo.setModel(createFileComboModel(selectedPackage));
        }
    }//GEN-LAST:event_packageComboActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseExternalButton;
    private javax.swing.JButton browseFileButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton classPathRadio;
    private javax.swing.JRadioButton externalRadio;
    private javax.swing.JComboBox fileCombo;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JButton importButton;
    private javax.swing.JRadioButton noIconRadio;
    private javax.swing.JComboBox packageCombo;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextField urlField;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables
    
}
