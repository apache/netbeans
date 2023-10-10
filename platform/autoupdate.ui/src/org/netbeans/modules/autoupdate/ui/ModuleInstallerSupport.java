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
package org.netbeans.modules.autoupdate.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.autoupdate.ui.Bundle.*;

/**
 *
 * @author Jesse Glick
 * @author Jan Lahoda
 * @author Petr Hejl
 */
public class ModuleInstallerSupport  {
    private static RequestProcessor RP = new RequestProcessor(ModuleInstallerSupport.class.getName(), 1);
    private static final Logger LOG = Logger.getLogger(ModuleInstallerSupport.class.getName());

    @Messages({
        "searching_handle_single=Searching for \"{0}\" module on NetBeans plugin portal...",
        "resolve_title_single=Resolve \"{0}\" Reference Problem",
        "nodownload_header_single=\"{0}\" module has not been downloaded",
        "nodownload_message_single=You can try to download \"{0}\" module again",
        "active_handle_single=Activating {0}",

        "searching_handle=Searching for modules on NetBeans plugin portal...",
        "resolve_title=Resolve Reference Problem",
        "nodownload_header=Modules have not been downloaded",
        "nodownload_message=You can try to download modules again",
        "active_handle=Activating modules",

        "networkproblem_header=Unable to connect  to the NetBeans plugin portal",
        "networkproblem_message=Check your proxy settings or try again later. "
            + "The server may be unavailable at the moment. "
            + "You may also want to make sure that your firewall is not blocking network traffic.",
        "proxy_button=&Proxy Settings...",
        "tryagain_button=Try &Again"
    })

    private final Object[] closingOptions;

    private Object[] fullClosingOptions;
    private JButton tryAgain;
    private JButton proxySettings;

    public ModuleInstallerSupport(Object... alternativeOptions) {
        this.closingOptions = alternativeOptions.clone();
    }

    public Object installPlugins(final String displayName, Set<String> cnbs) throws OperationException {
        Collection<UpdateUnit> units = findModules(cnbs);
        if (units == null) {
            final String searchMessage = displayName != null
                    ? searching_handle_single(displayName) : searching_handle();
            final String resolveTitle = displayName != null
                    ? resolve_title_single(displayName) : resolve_title();

            final ProgressHandle handle = ProgressHandle.createHandle(searchMessage);
            initButtons();
            final DialogDescriptor searching = new DialogDescriptor(searchingPanel(new JLabel(searchMessage),
                    ProgressHandleFactory.createProgressComponent(handle)), resolveTitle, true, null);
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
                        LOG.log(Level.FINE, ex.getMessage(), ex);
                        if (!dlg.isVisible()) {
                            LOG.fine("dialog not visible => do nothing");
                            return;
                        }
                        DialogDescriptor networkProblem = new DialogDescriptor(
                                problemPanel(resolveTitle, networkproblem_message()), // message
                                networkproblem_header(), // title
                                true, // modal
                                null);
                        networkProblem.setOptions(new Object[] {tryAgain, proxySettings, NotifyDescriptor.CANCEL_OPTION});
                        networkProblem.setAdditionalOptions(closingOptions);
                        networkProblem.setClosingOptions(fullClosingOptions);
                        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
                        networkProblemDialog.setVisible(true);
                        Object answer = networkProblem.getValue();
                        if (NotifyDescriptor.CANCEL_OPTION.equals(answer)
                                || Arrays.asList(closingOptions).contains(answer)
                                || answer.equals(NotifyDescriptor.DEFAULT_OPTION) /* escape */ ) {
                            LOG.fine("cancel network problem dialog");
                            searching.setValue(answer);
                            dlg.dispose();
                        } else if (tryAgain.equals(answer)) {
                            LOG.fine("try again searching");
                            RP.post(this);
                        } else {
                            assert false : "Unknown " + answer;
                        }
                    }
                }
            });
            dlg.setVisible(true);
            handle.finish();
            if (NotifyDescriptor.CANCEL_OPTION.equals(searching.getValue())
                    || searching.getValue().equals(NotifyDescriptor.DEFAULT_OPTION) /* escape */) {
                LOG.log(Level.FINE, "user canceled searching for {0}", cnbs);
                return showNoDownloadDialog(displayName, cnbs);
            } else if (Arrays.asList(closingOptions).contains(searching.getValue())){
                return searching.getValue();
            }
            units = findModules(cnbs);
            if (units == null) {
                LOG.log(Level.FINE, "could not find {0} on any update site", cnbs);
                return showNoDownloadDialog(displayName, cnbs);
            }
        }

        List<UpdateUnit> toHandle = new ArrayList<UpdateUnit>(units);
        OperationContainer<OperationSupport> oc = null;

        for (Iterator<UpdateUnit> it = toHandle.iterator(); it.hasNext(); ) {
            UpdateUnit unit = it.next();

            // check if module installed
            if (unit.getInstalled() != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(unit.getInstalled() + " already installed. Is active? " + unit.getInstalled().isEnabled());
                }
                if (unit.getInstalled().isEnabled()) {
                    it.remove();
                    continue;
                } else {
                    if (oc == null) {
                        oc = OperationContainer.createForEnable();
                    }
                    if (!oc.canBeAdded(unit, unit.getInstalled())) {
                        throw new OperationException(OperationException.ERROR_TYPE.ENABLE,
                                "could not add " + unit.getInstalled() + " for activation");
                    }
                    for (UpdateElement req : oc.add(unit.getInstalled()).getRequiredElements()) {
                        oc.add(req);
                    }
                    it.remove();
                    continue;
                }
            }
        }

        if (oc != null) {
            ProgressHandle activeHandle = ProgressHandleFactory.createHandle(
                    displayName != null ? active_handle_single(displayName) : active_handle());
            Restarter restarter = oc.getSupport().doOperation(activeHandle);
            assert restarter == null : "No Restater need to make units active";
        }

        if (toHandle.isEmpty()) {
            return null;
        }

        OperationContainer<InstallSupport> ocInstall = OperationContainer.createForInstall();
        for (Iterator<UpdateUnit> it = toHandle.iterator(); it.hasNext(); ) {
            UpdateUnit unit = it.next();

            List<UpdateElement> updates = unit.getAvailableUpdates();
            if (updates.isEmpty()) {
                throw new OperationException(OperationException.ERROR_TYPE.INSTALL, "no updates for " + unit);
            }

            UpdateElement element = updates.get(0);
            if (!ocInstall.canBeAdded(unit, element)) {
                throw new OperationException(OperationException.ERROR_TYPE.INSTALL, "could not add " + element + " to updates");
            }
            for (UpdateElement req : ocInstall.add(element).getRequiredElements()) {
                ocInstall.add(req);
            }
            it.remove();
        }
        assert toHandle.isEmpty() : "These unit were not handled " + toHandle;

        if (!PluginManager.openInstallWizard(ocInstall)) {
            LOG.fine("user canceled PM");
            return showNoDownloadDialog(displayName, cnbs);
        }
        return null;
    }

    private Collection<UpdateUnit> findModules(Set<String> cnbs) {
        List<UpdateUnit> ret = new ArrayList<UpdateUnit>(cnbs.size());
        for (UpdateUnit unit : UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE)) {
            if (cnbs.contains(unit.getCodeName())) {
                ret.add(unit);
                if (ret.size() == cnbs.size()) {
                    break;
                }
            }
        }
        if (ret.size() == cnbs.size()) {
            return ret;
        }
        return null;
    }

    private void initButtons() {
        if (tryAgain != null) {
            return ;
        }
        tryAgain = new JButton();
        proxySettings = new JButton();
        Mnemonics.setLocalizedText(tryAgain, tryagain_button());
        Mnemonics.setLocalizedText(proxySettings, proxy_button());
        proxySettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOG.fine("show proxy options");
                OptionsDisplayer.getDefault().open("General"); // NOI18N
            }
        });
        fullClosingOptions = new Object[closingOptions.length + 2];
        System.arraycopy(closingOptions, 0, fullClosingOptions, 0, closingOptions.length);
        fullClosingOptions[fullClosingOptions.length - 2] = tryAgain;
        fullClosingOptions[fullClosingOptions.length - 1] = NotifyDescriptor.CANCEL_OPTION;
    }

    private Object showNoDownloadDialog(String displayName, Set<String> cnbs) throws OperationException {
        DialogDescriptor networkProblem;
        if (displayName != null) {
            networkProblem = new DialogDescriptor(
                    problemPanel(nodownload_header_single(displayName), nodownload_message_single(displayName)), // message
                    resolve_title_single(displayName), // title
                    true, // modal
                    null);
        } else {
            networkProblem = new DialogDescriptor(
                    problemPanel(nodownload_header(), nodownload_message()), // message
                    resolve_title(), // title
                    true, // modal
                    null);
        }
        initButtons();
        networkProblem.setOptions(new Object[] {tryAgain, NotifyDescriptor.CANCEL_OPTION});
        networkProblem.setAdditionalOptions(closingOptions);
        networkProblem.setClosingOptions(fullClosingOptions);
        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
        networkProblemDialog.setVisible(true);
        Object answer = networkProblem.getValue();
        if (NotifyDescriptor.CANCEL_OPTION.equals(answer)
                || Arrays.asList(closingOptions).contains(answer)
                || answer.equals(NotifyDescriptor.DEFAULT_OPTION) /* escape */ ) {
            LOG.fine("cancel no download dialog");
            //throw new InterruptedException("user canceled download & install JUnit");
            return answer;
        } else if (tryAgain.equals(answer)) {
            LOG.fine("try again download()");
            return installPlugins(displayName, cnbs);
        } else {
            assert false : "Unknown " + answer;
        }
        assert false : "Unknown " + answer;
        return NotifyDescriptor.DEFAULT_OPTION;
    }

    private static JPanel searchingPanel(JLabel progressLabel, JComponent progressComponent) {
        JPanel panel = new JPanel();
        progressLabel.setLabelFor(progressComponent);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressComponent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(progressLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(107, 107, 107)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addGap(82, 82, 82))
        );
        return panel;
    }

}

