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

package org.netbeans.modules.java.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JButton;

import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = UIProvider.class)
public class UIProviderImpl implements UIProvider {

    private static final Logger LOG = Logger.getLogger(UIProviderImpl.class.getName());
    @StaticResource
    private static final String WARNING_ICON = "org/netbeans/modules/java/source/resources/icons/warning.png"; //NOI18N
    private static final NotificationDisplayer.Priority DEFAULT_PRIORITY = NotificationDisplayer.Priority.SILENT;
    private static final NotificationDisplayer.Priority PRIORITY;
    static {
        final String val = System.getProperty("UIProviderImpl.mem.priority");   //NOI18N
        if ("high".equals(val)) {   //NOI18N
            PRIORITY = NotificationDisplayer.Priority.HIGH;
        } else if ("silent".equals(val)) {  //NOI18N
            PRIORITY = NotificationDisplayer.Priority.SILENT;
        } else if ("hidden".equals(val)) {  //NOI18N
            PRIORITY = null;
        } else {
            PRIORITY = DEFAULT_PRIORITY;
        }
    }

    @Override
    public boolean warnContainsErrors(Preferences pref) {
        JButton btnRunAnyway = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnRunAnyway, NbBundle.getMessage(UIProviderImpl.class, "BTN_RunAnyway"));
        btnRunAnyway.getAccessibleContext().setAccessibleName(NbBundle.getMessage(UIProviderImpl.class, "ACSN_BTN_RunAnyway"));
        btnRunAnyway.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UIProviderImpl.class, "ACSD_BTN_RunAnyway"));

        JButton btnCancel = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, NbBundle.getMessage(UIProviderImpl.class, "BTN_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(UIProviderImpl.class, "ACSN_BTN_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UIProviderImpl.class, "ACSD_BTN_Cancel"));

        ContainsErrorsWarning panel = new ContainsErrorsWarning();
        DialogDescriptor dd = new DialogDescriptor(panel,
                NbBundle.getMessage(UIProviderImpl.class, "TITLE_ContainsErrorsWarning"),
                true,
                new Object[]{btnRunAnyway, btnCancel},
                btnRunAnyway,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);

        Object option = DialogDisplayer.getDefault().notify(dd);

        if (option == btnRunAnyway) {
            pref.putBoolean(ASK_BEFORE_RUN_WITH_ERRORS, panel.getAskBeforeRunning());
            return true;
        }
        return false;
    }

    @Override
    public void notifyLowMemory(String rootName) {
        if (PRIORITY != null) {
            NotificationDisplayer.getDefault().notify(
                    NbBundle.getMessage(UIProviderImpl.class, "TITLE_LowMemory"),
                    ImageUtilities.loadImageIcon(WARNING_ICON, false),
                    NbBundle.getMessage(UIProviderImpl.class, "MSG_LowMemory", rootName),
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                final URL url = new URL(NbBundle.getMessage(UIProviderImpl.class, "URL_LowMemory"));
                                HtmlBrowser.URLDisplayer.getDefault().showURLExternal(url);
                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    },
                    PRIORITY, NotificationDisplayer.Category.ERROR);
        }

    }

    @Override
    @Messages({
        "TITLE_CompileOnSaveNotSupported=Compile on Save Not Supported",
        "DESC_CompileOnSaveNotSupported=Compile on Save not supported without nb-javac library." +
                                       "Please either install nb-javac, or open project properties " +
                                       "and disable compile on save and re-run the project.",
        "BTN_InstallNbJavac=Install &nb-javac library",
        "ACSN_BTN_InstallNbJavac=Install nb-javac library",
        "ACSD_BTN_InstallNbJavac=Install nb-javac library",
        "BTN_ProjectProperties=&Open Project Properties",
        "ACSN_BTN_ProjectProperties=Open Project Properties",
        "ACSD_BTN_ProjectProperties=Open Project Properties",
        "DN_nbjavac=nb-javac library",
    })
    public void warnNoNbJavac() {
        JButton btnInstallNbJavac = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnInstallNbJavac, Bundle.BTN_InstallNbJavac());
        btnInstallNbJavac.getAccessibleContext().setAccessibleName(Bundle.ACSN_BTN_InstallNbJavac());
        btnInstallNbJavac.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_BTN_InstallNbJavac());

        JButton btnProjectProperties = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnProjectProperties, Bundle.BTN_ProjectProperties());
        btnProjectProperties.getAccessibleContext().setAccessibleName(Bundle.ACSN_BTN_ProjectProperties());
        btnProjectProperties.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_BTN_ProjectProperties());

        JButton btnCancel = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, NbBundle.getMessage(UIProviderImpl.class, "BTN_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(UIProviderImpl.class, "ACSN_BTN_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UIProviderImpl.class, "ACSD_BTN_Cancel"));

        Project prj = Utilities.actionsGlobalContext().lookup(Project.class);

        if (prj == null) {
            FileObject file = Utilities.actionsGlobalContext().lookup(FileObject.class);
            if (file != null) {
                prj = FileOwnerQuery.getOwner(file);
            }
        }

        Object customizer;

        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            //to avoid dependency on project.uiapi:
            Class<?> customizerProviderClass = Class.forName("org.netbeans.spi.project.ui.CustomizerProvider", false, cl);
            customizer = prj != null ? prj.getLookup().lookup(customizerProviderClass) : null;
        } catch(ClassNotFoundException e) {
            customizer = null;
        }

        DialogDescriptor dd = new DialogDescriptor(Bundle.DESC_CompileOnSaveNotSupported(),
                Bundle.TITLE_CompileOnSaveNotSupported(),
                true,
                customizer != null ? new Object[]{btnInstallNbJavac, btnProjectProperties, btnCancel} : new Object[]{btnInstallNbJavac, btnCancel},
                btnInstallNbJavac,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

        dd.setMessageType(DialogDescriptor.ERROR_MESSAGE);

        Object option = DialogDisplayer.getDefault().notify(dd);

        if (option == btnProjectProperties) {
            try {
                customizer.getClass().getMethod("showCustomizer", String.class, String.class).invoke(customizer, "Build", null);
                //
            } catch (NoSuchMethodException ex) {
                try {
                    customizer.getClass().getMethod("showCustomizer").invoke(customizer);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex1) {
                    LOG.log(Level.FINE, "CustomizerProvider#showCustomizer cannot be called", ex1);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOG.log(Level.FINE, "CustomizerProvider#showCustomizer(String, String) cannot be called", ex);
            }
        } else if (option == btnInstallNbJavac) {
            PluginManager.installSingle("org.netbeans.modules.nbjavac", Bundle.DN_nbjavac());
        }
    }
}
