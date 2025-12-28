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
package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.j2ee.weblogic9.VersionBridge;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.weblogic.common.api.Version;
import org.netbeans.modules.weblogic.common.api.WebLogicLayout;
import org.openide.WizardDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class ServerLocationVisual extends javax.swing.JPanel {

    private static final FilenameFilter SERVER_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("wlserver"); // NOI18N
        }
    };

    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private transient WLInstantiatingIterator instantiatingIterator;

    public ServerLocationVisual(WLInstantiatingIterator instantiatingIterator) {

        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;

        // register the supplied listener
        //addChangeListener(listener);
        // set the panel's name
        setName(NbBundle.getMessage(ServerLocationVisual.class,
                "SERVER_LOCATION_STEP"));        // NOI18N

        // init the GUI
        initComponents();

        locationField.addKeyListener(new ServerLocationVisual.LocationKeyListener());
        String loc = WLPluginProperties.getLastServerRoot();
        if (loc != null) { // NOI18N
            locationField.setText(loc);
        }

        // XXX
        localRadioButton.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                fireChangeEvent();
            }
        });
        remoteRadioButton.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                fireChangeEvent();
            }
        });
    }

    public boolean valid(WizardDescriptor wizardDescriptor) {
        // clear the error message
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);

        // check for the validity of the entered installation directory
        // if it's invalid, return false
        String location = getInstallLocation();

        if (location.trim().isEmpty()) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_EMPTY_SERVER_ROOT");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
            return false;
        }

        File serverRoot = FileUtil.normalizeFile(new File(location));

        serverRoot = findServerLocation(serverRoot, wizardDescriptor);
        if (serverRoot == null) {
            return false;
        }
        location = serverRoot.getPath();

        File weblogicJar = WebLogicLayout.getWeblogicJar(serverRoot);
        if (!weblogicJar.exists()) {
            File packed = new File(serverRoot, "server/lib/weblogic.jar.pack");
            if (packed.isFile()) {
                String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_INVALID_CONFIGURE", Utilities.isWindows() ? "cmd" : "sh");  // NOI18N
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
                return false;
            }
        }

        Version version = WebLogicLayout.getServerVersion(serverRoot);

        if (!WebLogicLayout.isSupportedVersion(version)) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_INVALID_SERVER_VERSION");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
            return false;
        }

        if (!WebLogicLayout.isSupportedLayout(serverRoot)) {
            String msg = NbBundle.getMessage(ServerLocationVisual.class, "ERR_INVALID_SERVER_ROOT");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, WLInstantiatingIterator.decorateMessage(msg));
            return false;
        }

        WLPluginProperties.setLastServerRoot(location);

        // set the server root in the parent instantiating iterator
        instantiatingIterator.setServerRoot(location);
        instantiatingIterator.setServerVersion(VersionBridge.getVersion(version));
        instantiatingIterator.setRemote(remoteRadioButton.isSelected());

        // everything seems ok
        return true;
    }

    public static File findServerLocation(File candidate, WizardDescriptor wizardDescriptor) {
        if (WebLogicLayout.isSupportedLayout(candidate)) {
            return candidate;
        } else {
            File[] files = candidate.listFiles(SERVER_FILTER);
            if (files != null) {
                if (files.length == 1) {
                    return files[0];
                } else {
                    for (File file : files) {
                        if (WebLogicLayout.isSupportedLayout(file)) {
                            String msg = NbBundle.getMessage(ServerLocationVisual.class,
                                    "WARN_CHILD_SERVER_ROOT", file.getPath());
                            wizardDescriptor.putProperty(
                                    WizardDescriptor.PROP_WARNING_MESSAGE,
                                    WLInstantiatingIterator.decorateMessage(msg));
                            return file;
                        }
                    }
                }
            }
            String msg = NbBundle.getMessage(ServerLocationVisual.class,
                    "ERR_INVALID_SERVER_ROOT");  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(msg));
            return null;
        }
    }

    private String getInstallLocation() {
        return locationField.getText();
    }

    /**
     * An instance of the fileschooser that is used for locating the server
     * installation directory
     */
    private JFileChooser fileChooser;

    /**
     * Shows the filechooser set to currently selected directory or to the
     * default system root if the directory is invalid
     */
    private void showFileChooser() {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        // set the chooser's properties
        fileChooser.setFileFilter(new DirectoryFileFilter());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // set the current directory
        File currentLocation = new File(locationField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            fileChooser.setCurrentDirectory(currentLocation.getParentFile());
            fileChooser.setSelectedFile(currentLocation);
        }

        // wait for the user to choose the directory and if he clicked the OK
        // button store the selected directory in the server location field
        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            locationField.setText(fileChooser.getSelectedFile().getPath());
            fireChangeEvent();
        }
    }

    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Fires a custom change event
     *
     * @param event the event
     */
    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    // Inner Classes
    /**
     * Simple key listener that delegates the event to its parent's listeners
     *
     * @author Kirill Sorokin
     */
    private class LocationKeyListener extends KeyAdapter {

        /**
         * This method is called when a user presses a key on the keyboard
         */
        public void keyTyped(KeyEvent event) {
            fireChangeEvent();
        }

        /**
         * This method is called when a user releases a key on the keyboard
         */
        public void keyReleased(KeyEvent event) {
            fireChangeEvent();
        }
    }

    /**
     * An extension of the FileFilter class that is setup to accept only
     * directories.
     *
     * @author Kirill Sorokin
     */
    private static class DirectoryFileFilter extends FileFilter {

        /**
         * This method is called when it is needed to decide whether a chosen
         * file meets the filter's requirements
         *
         * @return true if the file meets the requirements, false otherwise
         */
        @Override
        public boolean accept(File file) {
            // if the file exists and it's a directory - accept it
            return file.exists() && file.isDirectory();
        }

        /**
         * Returns the description of file group described by this filter
         *
         * @return group name
         */
        @Override
        public String getDescription() {
            return NbBundle.getMessage(ServerLocationVisual.class, "DIRECTORIES_FILTER_NAME"); // NOI18N
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

        domainButtonGroup = new javax.swing.ButtonGroup();
        locationLabel = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();
        locationBrowseButton = new javax.swing.JButton();
        downloadLabel = new javax.swing.JLabel();
        domainLabel = new javax.swing.JLabel();
        localRadioButton = new javax.swing.JRadioButton();
        remoteRadioButton = new javax.swing.JRadioButton();

        locationLabel.setLabelFor(locationField);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.locationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(locationBrowseButton, org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.locationBrowseButton.text")); // NOI18N
        locationBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downloadLabel, org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.downloadLabel.text")); // NOI18N
        downloadLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                downloadLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                downloadLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                downloadLabelMouseExited(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(domainLabel, org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.domainLabel.text")); // NOI18N

        domainButtonGroup.add(localRadioButton);
        localRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(localRadioButton, org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.localRadioButton.text")); // NOI18N

        domainButtonGroup.add(remoteRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(remoteRadioButton, org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.remoteRadioButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(locationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locationField, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locationBrowseButton))
            .addComponent(downloadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(domainLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(remoteRadioButton)
                            .addComponent(localRadioButton))))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(domainLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remoteRadioButton))
        );

        locationField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ACSD_ServerLocationPanel_locationField")); // NOI18N
        locationBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ACSD_ServerLocationPanel_locationBrowseButton")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void locationBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationBrowseButtonActionPerformed
        showFileChooser();
    }//GEN-LAST:event_locationBrowseButtonActionPerformed

    private void downloadLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadLabelMouseEntered
        downloadLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_downloadLabelMouseEntered

    private void downloadLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadLabelMouseExited
        downloadLabel.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_downloadLabelMouseExited

    private void downloadLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadLabelMouseClicked
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(
                    new URL("http://www.oracle.com/technetwork/middleware/weblogic/downloads/index.html")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_downloadLabelMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup domainButtonGroup;
    private javax.swing.JLabel domainLabel;
    private javax.swing.JLabel downloadLabel;
    private javax.swing.JRadioButton localRadioButton;
    private javax.swing.JButton locationBrowseButton;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JRadioButton remoteRadioButton;
    // End of variables declaration//GEN-END:variables
}
