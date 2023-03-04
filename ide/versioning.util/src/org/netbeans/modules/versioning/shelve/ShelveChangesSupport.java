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
package org.netbeans.modules.versioning.shelve;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.versioning.shelve.impl.PatchStorage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public abstract class ShelveChangesSupport {

    private String patchName;
    private static final Pattern PATCH_NAME_PATTERN = Pattern.compile("^(.*)-(\\d+)$"); //NOI18N

    protected abstract void exportPatch (File toFile, File commonParent) throws IOException;
    
    protected abstract void postExportCleanup ();
    
    protected abstract boolean isCanceled ();
    
    public final boolean prepare (String helpCtx) {
        return prepare(null, helpCtx);
    }

    public final boolean prepare (final JPanel additionalOptions, String helpCtx) {
        return openDialog(additionalOptions, helpCtx) && patchName != null;
    }
    
    public final void shelveChanges (File[] roots) {
        assert !EventQueue.isDispatchThread();
        if (patchName == null) {
            throw new IllegalStateException("Patch name not initialized, you probably forgot to run prepare()");
        }
        PatchStorage storage = PatchStorage.getInstance();
        File commonParent = getCommonParent(roots);
        File patchFile = storage.reservePatchFile(patchName);
        boolean patchCreated = false;
        try {
            if (isCanceled()) {
                return;
            }
            exportPatch(patchFile, commonParent);
            if (!patchFile.exists() || isCanceled()) {
                return;
            }
            patchCreated = true;
            storage.savePatchInfo(patchName, patchFile, commonParent);
            postExportCleanup();
        } catch (IOException ex) {
            Logger.getLogger(ShelveChangesSupport.class.getName()).log(Level.FINE, null, ex);
        } finally {
            if (!patchCreated) {
                storage.dismissPatchFile(patchFile);
            }
        }
    }
    
    private static File getCommonParent (File [] files) {
        File root = files[0];
        if (!root.exists() || root.isFile()) root = root.getParentFile();
        for (int i = 1; i < files.length; i++) {
            root = Utils.getCommonParent(root, files[i]);
            if (root == null) return null;
        }
        return root;
    }

    private boolean openDialog (JPanel additionalOptions, String helpCtx) {
        ShelveChangesPanel panel = new ShelveChangesPanel(additionalOptions);
        initializePatchName(panel.txtPatchName);
        panel.lblError.setVisible(false);
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(ShelveChangesSupport.class, "CTL_ShelveChangesPanel.okButton.text")); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ShelveChangesSupport.class, "LBL_ShelveChangesPanel.title"), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(helpCtx), null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        panel.txtPatchName.getDocument().addDocumentListener(new PatchNameListener(panel, okButton, dialog));
        dialog.setVisible(true);
        if (dd.getValue() == okButton) {
            patchName = panel.txtPatchName.getText().trim();
            return !patchName.isEmpty();
        } else {
            return false;
        }
    }

    private void initializePatchName (final JTextField tf) {
        Utils.postParallel(new Runnable() {
            @Override
            public void run () {
                List<String> patchNames = PatchStorage.getInstance().getPatchNames();
                String offeredPatchName = patchNames.isEmpty() ? "unfinishedChanges" : patchNames.get(0); //NOI18N
                String originalPatchName = offeredPatchName;
                int i = 0;
                Matcher m = PATCH_NAME_PATTERN.matcher(offeredPatchName);
                if (m.matches()) {
                    try {
                        i = Integer.parseInt(m.group(2));
                    } catch (NumberFormatException ex) {
                    }
                    originalPatchName = m.group(1);
                    offeredPatchName = originalPatchName + "-" + ++i; //NOI18N
                }
                while (patchNames.contains(offeredPatchName)) {
                    offeredPatchName = originalPatchName + "-" + ++i; //NOI18N
                }
                final String patchName = offeredPatchName;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        if (tf.getText().isEmpty()) {
                            tf.setText(patchName);
                            tf.selectAll();
                        }
                    }
                });
            }
        }, 0);
    }

    private static class PatchNameListener implements DocumentListener, ActionListener {
        private final JButton button;
        private final Timer timer;
        private final ShelveChangesPanel panel;
        private final Dialog dialog;
        private static final Pattern p = Pattern.compile("([\\\\/:\\*\\?\"\\<\\>\\|\\s])"); //NOI18N

        private PatchNameListener (ShelveChangesPanel panel, JButton okButton, Dialog dialog) {
            this.panel = panel;
            this.button = okButton;
            this.dialog = dialog;
            timer = new Timer(300, this);
            timer.stop();
        }

        @Override
        public void insertUpdate (DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate (DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate (DocumentEvent e) {
        }

        private void update () {
            button.setEnabled(false);
            panel.lblError.setVisible(false);
            timer.restart();
        }

        @Override
        @NbBundle.Messages({
            "# {0} - invalid character", "ShelveChangesPanel.lblError.invalidCharacters=Patch name must not contain \"{0}\"."
        })
        public void actionPerformed (ActionEvent e) {
            String patchName = panel.txtPatchName.getText().trim();
            if (!patchName.isEmpty()) {
               Matcher m = p.matcher(patchName);
               if (m.find()) {
                    setError(Bundle.ShelveChangesPanel_lblError_invalidCharacters(m.group(1)));
                } else if (!PatchStorage.getInstance().containsPatch(patchName)) {
                    button.setEnabled(true);
                } else {
                    setError(org.openide.util.NbBundle.getMessage(ShelveChangesPanel.class, "ShelveChangesPanel.lblError.text")); //NOI18N
                    if (dialog.getHeight() < dialog.getPreferredSize().height || dialog.getWidth() < dialog.getPreferredSize().width) {
                        dialog.pack();
                    }
                }
            }
        }

        private void setError (String msg) {
            panel.lblError.setText(msg);
            panel.lblError.setVisible(true);
        }
    }
}
