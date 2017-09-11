/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.shelve.impl;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import org.netbeans.modules.diff.PatchAction;
import org.netbeans.modules.versioning.shelve.impl.PatchStorage.Patch;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.versioning.shelve.impl.UnshelveChangesAction", category = "Versioning/UnshelveChanges")
@ActionRegistration(displayName = "#CTL_MenuItem_UnshelveChanges", iconInMenu=false)
public final class UnshelveChangesAction extends AbstractAction {
    private final String defaultPatchName;

    public UnshelveChangesAction () {
        defaultPatchName = null;
    }

    public UnshelveChangesAction (String patchName) {
        super(NbBundle.getMessage(UnshelveChangesAction.class, "CTL_MenuItem_UnshelveChanges.patch", patchName)); //NOI18N
        this.defaultPatchName = patchName;
    }
    
    @Override
    public void actionPerformed (ActionEvent ev) {
        final String patchName;
        final boolean removePatchFile;
        if (defaultPatchName == null) {
            Unshelve unshelve = new Unshelve();
            if (unshelve.selectPatchName()) {
                patchName = unshelve.getPatchName();
                removePatchFile = unshelve.getRemovePatchFile();
            } else {
                return;
            }
        } else {
            patchName = defaultPatchName;
            removePatchFile = true;
        }
        final PatchStorage storage = PatchStorage.getInstance();
        Utils.post(new Runnable () {
            @Override
            public void run () {
                Patch patch = storage.getPatch(patchName);
                if (patch != null) {
                    if (PatchAction.performPatch(patch.getPatchFile(), patch.getPatchContext())) {
                        storage.removePatch(patchName, removePatchFile);
                    }
                }
            }
        });
    }

    private static class Unshelve implements ActionListener {
        private final JButton unshelveButton;
        private final JButton removeButton;
        private final UnshelveChangesPanel panel;
        private String patchName;
        private boolean removePatchFile;
        private static final String LOADING_PATCHES = NbBundle.getMessage(UnshelveChangesAction.class, "LBL_UnshelveChangesPanel.loading"); //NOI18N

        public Unshelve () {
            panel = new UnshelveChangesPanel();
            panel.cmbPatches.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof Patch) {
                        value = ((Patch) value).getPatchName();
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            unshelveButton = new JButton();
            removeButton = new JButton();
            panel.cmbPatches.addActionListener(this);
            initializePatches();
        }

        public boolean selectPatchName () {
            Mnemonics.setLocalizedText(unshelveButton, NbBundle.getMessage(UnshelveChangesAction.class, "CTL_UnshelveChangesPanel.unshelveButton.text")); //NOI18N
            Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(UnshelveChangesAction.class, "CTL_UnshelveChangesPanel.removeButton.text")); //NOI18N
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(UnshelveChangesAction.class, "LBL_UnshelveChangesPanel.title"), //NOI18N
                    true, new Object[] { unshelveButton, removeButton, DialogDescriptor.CANCEL_OPTION }, unshelveButton, DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx("org.netbeans.modules.versioning.shelve.impl.UnshelveChangesAction"), null); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
            patchName = panel.cmbPatches.getSelectedItem() instanceof Patch ? ((Patch) panel.cmbPatches.getSelectedItem()).getPatchName().trim() : null;
            removePatchFile = !panel.cbKeepPatchFile.isSelected();
            if (patchName == null) {
                return false;
            } else if (dd.getValue() == unshelveButton) {
                return true;
            } else if (dd.getValue() == removeButton) {
                Utils.post(new Runnable() {
                    @Override
                    public void run () {
                        PatchStorage.getInstance().removePatch(patchName, removePatchFile);
                    }
                });
            }
            return false;
        }

        private String getPatchName () {
            return patchName;
        }

        private boolean getRemovePatchFile () {
            return removePatchFile;
        }

        private void initializePatches () {
            panel.cmbPatches.setModel(new DefaultComboBoxModel(new String[] { LOADING_PATCHES }));
            validate();
            Utils.postParallel(new Runnable() {
                @Override
                public void run () {
                    final List<Patch> patches = PatchStorage.getInstance().getPatches();
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            panel.cmbPatches.setModel(new DefaultComboBoxModel(patches.toArray(new Patch[patches.size()])));
                            if (!patches.isEmpty()) {
                                panel.cmbPatches.setSelectedIndex(0);
                            }
                        }
                    });
                }
            }, 0);
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            validate();
        }

        private void validate () {
            if (panel.cmbPatches.getSelectedItem() instanceof Patch) {
                removeButton.setEnabled(true);
                unshelveButton.setEnabled(true);
            } else {
                removeButton.setEnabled(false);
                unshelveButton.setEnabled(false);
            }
        }
    }
}
