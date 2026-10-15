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

package org.netbeans.modules.nbform;

import java.awt.event.ActionEvent;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.options.OptionsDisplayer;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.openide.DialogDisplayer;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport.LibraryDefiner;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Defines Swing Application Framework Library by downloading swingapplib module.
 * The code is based on {@code JUnitLibraryDownloader} almost literally.
 * 
 * @author Jan Stola
 */
@ServiceProvider(service=LibraryDefiner.class)
public class SwingAppLibDownloader implements LibraryDefiner {
    private static RequestProcessor RP = new RequestProcessor(SwingAppLibDownloader.class.getName(), 1);
    private static final Logger LOG = Logger.getLogger(SwingAppLibDownloader.class.getName());

    @Override
    public Callable<Library> missingLibrary(final String name) {
        if (!name.matches("swing-app-framework")) { // NOI18N
            return null;
        }
        return new Callable<Library>() {
            @Override
            public Library call() throws Exception {
                return download(name);
            }
        };
    }
    
    private JButton libraryManager;
    private JButton tryAgain;
    private JButton proxySettings;

    @SuppressWarnings("SleepWhileInLoop") // NOI18N
    private Library download(String name) throws Exception {
        UpdateUnit unit = findSwingAppLibModule();
        if (unit == null) {
            String searchingHandleTxt = FormUtils.getBundleString("swingapp.searching_handle"); // NOI18N
            final ProgressHandle handle = ProgressHandle.createHandle(searchingHandleTxt);
            initButtons();
            final DialogDescriptor searching = new DialogDescriptor(
                    searchingPanel(new JLabel(searchingHandleTxt), ProgressHandleFactory.createProgressComponent(handle)),
                    FormUtils.getBundleString("swingapp.resolve_title"), true, null); // NOI18N
            handle.setInitialDelay(0);
            handle.start();
            searching.setOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
            searching.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
            final Dialog dlg = DialogDisplayer.getDefault().createDialog(searching);
            RP.post(new Runnable() {

                @Override
                public void run() {
                    // May be first start, when no update lists have yet been downloaded.
                    try {
                        for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
                            p.refresh(handle, true);
                        }
                        // close searching
                        dlg.dispose();
                    } catch (IOException ex) {
                        Logger.getLogger(SwingAppLibDownloader.class.getName()).log(Level.FINE, ex.getMessage(), ex);
                        if (!dlg.isVisible()) {
                            LOG.fine("dialog not visible => do nothing"); // NOI18N
                            return ;
                        }
                        DialogDescriptor networkProblem = new DialogDescriptor(
                                problemPanel(FormUtils.getBundleString("swingapp.resolve_title"), FormUtils.getBundleString("swingapp.networkproblem_message")), // NOI18N
                                FormUtils.getBundleString("swingapp.networkproblem_header"), // NOI18N
                                true, // modal
                                null);
                        networkProblem.setOptions(new Object[] {tryAgain, proxySettings, NotifyDescriptor.CANCEL_OPTION});
                        networkProblem.setAdditionalOptions(new Object[] {libraryManager});
                        networkProblem.setClosingOptions(new Object[] {libraryManager, tryAgain, NotifyDescriptor.CANCEL_OPTION});
                        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
                        networkProblemDialog.setVisible(true);
                        Object answer = networkProblem.getValue();
                        if (NotifyDescriptor.CANCEL_OPTION.equals(answer) || answer.equals(-1) /* escape */ ) {
                            LOG.fine("cancel network problem dialog"); // NOI18N
                            searching.setValue(answer);
                            dlg.dispose();
                        } else if (tryAgain.equals(answer)) {
                            LOG.fine("try again searching"); // NOI18N
                            RP.post(this);
                        } else if (libraryManager.equals(answer)) {
                            LOG.fine("open library manager"); // NOI18N
                            searching.setValue(answer);
                            dlg.dispose();
                        } else {
                            assert false : "Unknown " + answer; // NOI18N
                        }
                    }
                }
            });
            dlg.setVisible(true);
            handle.finish();
            if (NotifyDescriptor.CANCEL_OPTION.equals(searching.getValue()) || searching.getValue().equals(-1) /* escape */) {
                LOG.fine("user canceled searching swingapplib"); // NOI18N
                return showNoDownloadDialog(name);
            } else if (libraryManager.equals(searching.getValue())) {
                throw new Exception("user canceled searching"); // NOI18N
            }
            unit = findSwingAppLibModule();
            if (unit == null) {
                LOG.fine("could not find swingapplib on any update site"); // NOI18N
                return showNoDownloadDialog(name);
            }
        }
        // check if swingapplib is installed
        if (unit.getInstalled() != null) {
            LOG.log(Level.FINE, "{0} already installed. Is active? {1}", new Object[]{unit.getInstalled(), unit.getInstalled().isEnabled()}); //NOI18N
            if (unit.getInstalled().isEnabled()) {
                throw new Exception(unit.getInstalled() + " already installed and active"); // NOI18N
            } else {
                // activate it
                OperationContainer<OperationSupport> oc = OperationContainer.createForEnable();
                if (!oc.canBeAdded(unit, unit.getInstalled())) {
                    throw new Exception("could not add " + unit.getInstalled() + " for activation"); // NOI18N
                }
                for (UpdateElement req : oc.add(unit.getInstalled()).getRequiredElements()) {
                    oc.add(req);
                }
                ProgressHandle activeHandle = ProgressHandle.createHandle (FormUtils.getBundleString("swingapp.active_handle")); // NOI18N
                Restarter restarter = oc.getSupport().doOperation(activeHandle);
                assert restarter == null : "No Restater need to make " + unit.getInstalled() + " active"; // NOI18N
                // XXX new library & build.properties apparently do not show up immediately... how to listen properly?
                for (int i = 0; i < 10; i++) {
                    Library lib = LibraryManager.getDefault().getLibrary(name);
                    if (lib != null) {
                        return lib;
                    }
                    Thread.sleep(1000);
                }
                LOG.info("swingapplib failed to make active properly"); // NOI18N
                return showNoDownloadDialog(name);
            }
        }
        List<UpdateElement> updates = unit.getAvailableUpdates();
        if (updates.isEmpty()) {
            throw new Exception("no updates for " + unit); // NOI18N
        }
        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
        UpdateElement element = updates.get(0);
        if (!oc.canBeAdded(unit, element)) {
            throw new Exception("could not add " + element + " to updates"); // NOI18N
        }
        for (UpdateElement req : oc.add(element).getRequiredElements()) {
            oc.add(req);
        }
        if (!PluginManager.openInstallWizard(oc)) {
            LOG.fine("user canceled PM"); // NOI18N
            return showNoDownloadDialog(name);
        }
        // XXX new library & build.properties apparently do not show up immediately... how to listen properly?
        for (int i = 0; i < 10; i++) {
            Library lib = LibraryManager.getDefault().getLibrary(name);
            if (lib != null) {
                return lib;
            }
            Thread.sleep(1000);
        }
        LOG.info("swingapplib failed to install properly"); // NOI18N
        return showNoDownloadDialog(name);
    }

    private UpdateUnit findSwingAppLibModule() throws IOException {
        for (UpdateUnit unit : UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE)) {
            if (unit.getCodeName().equals("org.netbeans.modules.swingapplib")) { // NOI18N
                return unit;
            }
        }
        return null;
    }
    
    private void initButtons() {
        if (libraryManager != null) {
            return ;
        }
        libraryManager = new JButton();
        tryAgain = new JButton();
        proxySettings = new JButton();
        Mnemonics.setLocalizedText(tryAgain, FormUtils.getBundleString("swingapp.tryagain_button")); // NOI18N
        Mnemonics.setLocalizedText(libraryManager, FormUtils.getBundleString("swingapp.library_button")); // NOI18N
        Mnemonics.setLocalizedText(proxySettings, FormUtils.getBundleString("swingapp.proxy_button")); // NOI18N
        proxySettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOG.fine("show proxy options"); // NOI18N
                OptionsDisplayer.getDefault().open("General"); // NOI18N
            }
        });
    }
    
    private Library showNoDownloadDialog(String name) throws Exception {
        DialogDescriptor networkProblem = new DialogDescriptor(
                problemPanel(FormUtils.getBundleString("swingapp.nodownload_header"), FormUtils.getBundleString("swingapp.nodownload_message")), // NOI18N
                FormUtils.getBundleString("swingapp.resolve_title"), // NOI18N
                true, // modal
                null);
        initButtons();
        networkProblem.setOptions(new Object[] {tryAgain, NotifyDescriptor.CANCEL_OPTION});
        networkProblem.setAdditionalOptions(new Object[] {libraryManager});
        networkProblem.setClosingOptions(new Object[] {libraryManager, tryAgain, NotifyDescriptor.CANCEL_OPTION});
        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
        networkProblemDialog.setVisible(true);
        Object answer = networkProblem.getValue();
        if (NotifyDescriptor.CANCEL_OPTION.equals(answer) || answer.equals(-1) /* escape */ ) {
            LOG.fine("cancel no download dialog"); // NOI18N
            return null;
        } else if (tryAgain.equals(answer)) {
            LOG.fine("try again download()"); // NOI18N
            return download(name);
        } else if (libraryManager.equals(answer)) {
            LOG.fine("open library manager");
            throw new Exception("swingapplib failed/canceled to install properly, open library manager instaed");            
        } else {
            assert false : "Unknown " + answer; // NOI18N
        }
        return null;
    }
    
    private static JPanel searchingPanel(JLabel progressLabel, JComponent progressComponent) {
        JPanel panel = new JPanel();
        progressLabel.setLabelFor(progressComponent);
        javax.swing.GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(progressLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressComponent, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(progressLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressComponent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(109, Short.MAX_VALUE))
        );
        return panel;
    }
    
    private static JPanel problemPanel(String header, String message) {
        JPanel panel = new JPanel();
        JLabel jLabel1 = new javax.swing.JLabel();
        JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        JTextArea jTextArea1 = new javax.swing.JTextArea();

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel1.setText(header);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(message);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(107, 107, 107)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addGap(82, 82, 82))
        );
        return panel;
    }

}
