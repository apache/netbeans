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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import static org.openide.DialogDescriptor.DEFAULT_ALIGN;
import static org.openide.NotifyDescriptor.CANCEL_OPTION;
import static org.openide.NotifyDescriptor.CLOSED_OPTION;

public class ClonePathsWizardPanel implements WizardDescriptor.Panel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ClonePathsPanel component;
    private HgURL repositoryOrig;
    private Listener listener;
    private HgURL pullUrl, pushUrl;
    private HgURL defaultUrl;
    private String defaultUrlString;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new ClonePathsPanel();
            initInteraction();
        }
        return component;
    }

    private void initInteraction() {
        listener = new Listener();

        component.defaultValuesButton.addActionListener(listener);
        component.changePullPathButton.addActionListener(listener);
        component.changePushPathButton.addActionListener(listener);
    }

    final class Listener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            HgURL changedUrl;

            Object source = e.getSource();
            if (source == component.defaultValuesButton) {
                setDefaultValues();
            } else if (source == component.changePullPathButton) {
                changedUrl = changeUrl("changePullPath.Title");         //NOI18N
                if (changedUrl != null) {
                    component.defaultPullPathField.setText(
                            changedUrl.toHgCommandStringWithMaskedPassword());
                    pullUrl = (changedUrl != HgURL.NO_URL) ? changedUrl : null;
                }
            } else if (source == component.changePushPathButton) {
                changedUrl = changeUrl("changePushPath.Title");         //NOI18N
                if (changedUrl != null) {
                    component.defaultPushPathField.setText(
                            changedUrl.toHgCommandStringWithMaskedPassword());
                    pushUrl = (changedUrl != HgURL.NO_URL) ? changedUrl : null;
                }
            } else {
                assert false;
            }
        }

    }

    /**
     * Invoked when the second page of wizard <em>Clone External Repository</em>
     * (aka <em>Clone Other...</em>) is displayed and one of the
     * <em>Change...</em> buttons is pressed. It displays a repository chooser
     * dialog.
     * 
     * @param  titleMsgKey  resource bundle key for the title of the repository
     *                      chooser dialog
     * @return  {@code HgURL} of the selected repository if one was selected,
     *          {@code HgURL.NO_URL} if the <em>Clear Path</em> button was
     *          selected, {@code null} otherwise (button <em>Cancel</em> pressed
     *          or the dialog closed without pressing any of the above buttons)
     */
    private HgURL changeUrl(String titleMsgKey) {
        int repoModeMask = Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS;
        String title = getMessage(titleMsgKey);

        final JButton set   = new JButton();
        final JButton clear = new JButton();
        Mnemonics.setLocalizedText(set,   getMessage("changePullPushPath.Set"));   //NOI18N
        Mnemonics.setLocalizedText(clear, getMessage("changePullPushPath.Clear")); //NOI18N

        final Repository repository = new Repository(repoModeMask, title, true);
        set.setEnabled(repository.isValid());
        clear.setDefaultCapable(false);

        final DialogDescriptor dialogDescriptor
                = new DialogDescriptor(
                        HgUtils.addContainerBorder(repository.getPanel()),
                        title,                          //title
                        true,                           //modal
                        new Object[] {set,
                                      clear,
                                      CANCEL_OPTION},
                        set,                            //default option
                        DEFAULT_ALIGN,                  //alignment
                        new HelpCtx(ClonePathsWizardPanel.class.getName()
                                    + ".change"),                       //NOI18N
                        null);                          //action listener
        dialogDescriptor.setClosingOptions(new Object[] {clear, CANCEL_OPTION});

        final NotificationLineSupport notificationLineSupport
                = dialogDescriptor.createNotificationLineSupport();

        class RepositoryChangeListener implements ChangeListener, ActionListener {
            private Dialog dialog;
            public void setDialog(Dialog dialog) {
                this.dialog = dialog;
            }
            public void stateChanged(ChangeEvent e) {
                assert e.getSource() == repository;
                boolean isValid = repository.isValid();
                dialogDescriptor.setValid(isValid);
                set.setEnabled(isValid);
                if (isValid) {
                    notificationLineSupport.clearMessages();
                } else {
                    String errMsg = repository.getMessage();
                    if ((errMsg != null) && (errMsg.length() != 0)) {
                        notificationLineSupport.setErrorMessage(errMsg);
                    } else {
                        notificationLineSupport.clearMessages();
                    }
                }
            }
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() != set) {
                    return;
                }

                try {
                    //remember the selected URL:
                    dialogDescriptor.setValue(repository.getUrl());

                    /*
                     * option "set" is not closing so we must handle closing
                     * of the dialog explictly here:
                     */
                    dialog.setVisible(false);
                    dialog.dispose();
                } catch (URISyntaxException ex) {
                    repository.setInvalid();
                    notificationLineSupport.setErrorMessage(ex.getMessage());
                }
            }
        }

        RepositoryChangeListener optionListener = new RepositoryChangeListener();
        repository.addChangeListener(optionListener);

        dialogDescriptor.setButtonListener(optionListener);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        optionListener.setDialog(dialog);

        dialog.pack();
        dialog.setVisible(true);
        
        Object selectedValue = dialogDescriptor.getValue();
        assert (selectedValue instanceof HgURL)
               || (selectedValue == clear)
               || (selectedValue == CANCEL_OPTION)
               || (selectedValue == CLOSED_OPTION);

        if (selectedValue instanceof HgURL) {
            return (HgURL) selectedValue;
        } else if (selectedValue == clear) {
            return HgURL.NO_URL;
        } else {
            return null;        //CANCEL_OPTION, CLOSED_OPTION
        }
    }

    public boolean isValid() {
        return true;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ClonePathsWizardPanel.class);
    }
    
    public final void addChangeListener(ChangeListener l) {
        //always valid - no changes - no change listeners
    }
    public final void removeChangeListener(ChangeListener l) {
        //always valid - no changes - no change listeners
    }

    private void setDefaultValues() {
        setDefaultValues(true, true);
    }

    private void setDefaultValues(boolean pullPath, boolean pushPath) {
        if (pullPath) {
            component.defaultPullPathField.setText(getDefaultPath());
            pullUrl = repositoryOrig;
        }
        if (pushPath) {
            component.defaultPushPathField.setText(getDefaultPath());
            pushUrl = repositoryOrig;
        }
    }

    private String getDefaultPath() {
        if (defaultUrlString == null) {
            defaultUrlString = repositoryOrig.toHgCommandUrlStringWithoutUserInfo();
        }
        return defaultUrlString;
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        assert (settings instanceof WizardDescriptor);

        defaultUrl = (HgURL) ((WizardDescriptor) settings).getProperty("repository"); // NOI18N
        HgURL repository = defaultUrl;
        boolean repoistoryChanged = !repository.equals(repositoryOrig);
        repositoryOrig = repository;
        defaultUrlString = null;

        boolean resetPullPath = repoistoryChanged || (pullUrl == null);
        boolean resetPushPath = repoistoryChanged || (pushUrl == null);

        setDefaultValues(resetPullPath,
                         resetPushPath);
    }
    public void storeSettings(Object settings) {

        if (settings instanceof WizardDescriptor) {
            ((WizardDescriptor) settings).putProperty("defaultPullPath", pullUrl); // NOI18N
            ((WizardDescriptor) settings).putProperty("defaultPushPath", pushUrl); // NOI18N
        }
    }

    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(ClonePathsWizardPanel.class, msgKey);
    }

}
