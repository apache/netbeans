/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
