/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.queues;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class FinishPatch {

    private final File repository;
    private final PatchSeriesPanel panel;
    private HgProgressSupport support;
    private static final String GETTING_PATCHES = NbBundle.getMessage(FinishPatch.class, "LBL_FinishPatch.loadingPatches"); //NOI18N
    private static final String NO_PATCHES = NbBundle.getMessage(FinishPatch.class, "LBL_FinishPatch.noPatches"); //NOI18N
    private QPatch onTopPatch;

    public FinishPatch (File repository) {
        this.repository = repository;
        this.panel = new PatchSeriesPanel();
        panel.lstPatches.setCellRenderer(new PatchRenderer());
    }

    public boolean showDialog () {
        final JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(FinishPatch.class, "CTL_FinishPatch.ok.text")); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(FinishPatch.class, "LBL_FinishPatchPanel.title", repository.getName()), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(FinishPatch.class), null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        okButton.setEnabled(false);
        setInfo(null);
        panel.lstPatches.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged (ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Object selectedPatch = panel.lstPatches.getSelectedValue();
                    okButton.setEnabled(selectedPatch instanceof QPatch);
                    if (onTopPatch == null) {
                        setInfo(NbBundle.getMessage(FinishPatch.class, "PatchSeriesPanel.lblInfo.noAppliedPatches")); //NOI18N
                    } else if (selectedPatch instanceof QPatch) {
                        setInfo(NbBundle.getMessage(FinishPatch.class, "FinishPatchPanel.lblInfo.toFinishPatches", ((QPatch) selectedPatch).getId())); //NOI18N
                    } else {
                        setInfo(null);
                    }
                }
            }
        });
        panel.lstPatches.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (MouseUtils.isDoubleClick(e) && okButton.isEnabled()) {
                    okButton.doClick();
                }
            }
        });
        loadPatches();
        dialog.setVisible(true);
        HgProgressSupport supp = this.support;
        if (supp != null) {
            supp.cancel();
        }
        return dd.getValue() == okButton;
    }

    private void setInfo (String message) {
        if (message == null || message.isEmpty()) {
            panel.lblInfo.setVisible(false);
        } else {
            panel.lblInfo.setText(message);
            panel.lblInfo.setVisible(true);
        }
    }

    private void loadPatches () {
        panel.lstPatches.setListData(new String[] { GETTING_PATCHES });
        panel.lstPatches.setEnabled(false);
        support = new HgProgressSupport() {
            @Override
            protected void perform () {
                QPatch[] patches = null;
                try {
                    patches = HgCommand.qListSeries(repository);
                } catch (HgException ex) {
                
                } finally {
                    final QPatch[] qPatches = patches;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            displayPatches(qPatches);
                        }
                    });
                }
            }
        };
        support.start(Mercurial.getInstance().getRequestProcessor(repository), repository, GETTING_PATCHES);
    }

    private void displayPatches (QPatch[] patches) {
        List<QPatch> toAdd = patches == null ? Collections.<QPatch>emptyList() : new ArrayList<QPatch>(patches.length);
        if (patches != null) {
            for (int i = 0; i < patches.length; ++i) {
                QPatch p = patches[i];
                if (p.isApplied()) {
                    onTopPatch = p;
                    toAdd.add(p);
                } else {
                    break;
                }
            }
        }
        if (toAdd.isEmpty()) {
            panel.lstPatches.setListData(new String[] { NO_PATCHES });
        } else {
            panel.lstPatches.setListData(toAdd.toArray(new QPatch[toAdd.size()]));
            panel.lstPatches.setEnabled(true);
        }
    }

    String getSelectedPatch () {
        String retval = null;
        Object selected = panel.lstPatches.getSelectedValue();
        if (selected instanceof QPatch) {
            retval = ((QPatch) selected).getId();
        }
        return retval;
    }

    private static class PatchRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String tooltip = null;
            if (value instanceof QPatch) {
                QPatch patch = (QPatch) value;
                value = patch.getId();
                if (!patch.getMessage().trim().isEmpty()) {
                    tooltip = patch.getMessage();
                }
            }
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (comp instanceof JComponent) {
                ((JComponent) comp).setToolTipText(tooltip);
            }
            return comp;
        }
        
    }
    
}
