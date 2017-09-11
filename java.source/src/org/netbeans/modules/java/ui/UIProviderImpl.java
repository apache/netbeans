/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JButton;

import org.netbeans.api.annotations.common.StaticResource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = UIProvider.class)
public class UIProviderImpl implements UIProvider {

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
}
