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
package org.netbeans.modules.mercurial.ui.queues;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class GoToPatch {

    private final File repository;
    private final PatchSeriesPanel panel;
    private HgProgressSupport support;
    private static final String GETTING_PATCHES = NbBundle.getMessage(GoToPatch.class, "LBL_GoToPatch.loadingPatches"); //NOI18N
    private static final String NO_PATCHES = NbBundle.getMessage(GoToPatch.class, "LBL_GoToPatch.noPatches"); //NOI18N
    private QPatch onTopPatch;
    private static final String SEP = "--------------------------------------------"; //NOI18N
    private static final String ICON_QUEUE_PATH = "org/netbeans/modules/mercurial/resources/icons/queue.png"; //NOI18N
    private static final Icon icon = ImageUtilities.loadImageIcon(ICON_QUEUE_PATH, true);

    public GoToPatch (File repository) {
        this.repository = repository;
        this.panel = new PatchSeriesPanel();
        panel.lstPatches.setCellRenderer(new PatchRenderer());
    }

    public boolean showDialog () {
        final JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(GoToPatch.class, "CTL_GoToPatch.ok.text")); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(GoToPatch.class, "LBL_GoToPatchPanel.title", repository.getName()), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(GoToPatch.class), null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        okButton.setEnabled(false);
        setInfo(null);
        panel.lstPatches.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged (ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Object selectedObject = panel.lstPatches.getSelectedValue();
                    Queue selectedQueue = selectedObject instanceof Queue ? ((Queue) selectedObject) : null;
                    okButton.setEnabled(true);
                    if (selectedQueue != null && selectedQueue.isActive() && onTopPatch == null) {
                        setInfo(NbBundle.getMessage(GoToPatch.class, "PatchSeriesPanel.lblInfo.noAppliedPatches")); //NOI18N
                        okButton.setEnabled(false);
                    } else if (selectedObject instanceof QPatch && onTopPatch == selectedObject) {
                        setInfo(NbBundle.getMessage(GoToPatch.class, "PatchSeriesPanel.lblInfo.alreadyOnTop")); //NOI18N
                        okButton.setEnabled(false);
                    } else if (selectedObject == null || selectedObject == SEP) {
                        okButton.setEnabled(false);
                        setInfo(null);
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
                Map<Queue, QPatch[]> patches = null;
                try {
                    patches = HgCommand.qListAvailablePatches(repository);
                } catch (HgException ex) {
                
                } finally {
                    final Map<Queue, QPatch[]> qPatches = patches;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            displayResults(qPatches);
                        }
                    });
                }
            }
        };
        support.start(Mercurial.getInstance().getRequestProcessor(repository), repository, GETTING_PATCHES);
    }

    private void displayResults (Map<Queue, QPatch[]> patches) {
        if (patches == null || patches.isEmpty()) {
            panel.lstPatches.setListData(new String[] { NO_PATCHES });
        } else {
            List<Object> toAdd = new LinkedList<Object>();
            for (Map.Entry<Queue, QPatch[]> e : patches.entrySet()) {
                if (!toAdd.isEmpty()) {
                    toAdd.add(SEP);
                }
                Queue q = e.getKey();
                toAdd.add(q);
                for (QPatch p : e.getValue()) {
                    toAdd.add(p);
                    if (p.isApplied()) {
                        onTopPatch = p;
                    }
                }
            }
            panel.lstPatches.setListData(toAdd.toArray(new Object[0]));
            panel.lstPatches.setEnabled(true);
            if (onTopPatch == null) {
                panel.lstPatches.setSelectedIndex(1);
            } else {
                panel.lstPatches.setSelectedValue(onTopPatch, true);
            }
            panel.lstPatches.requestFocusInWindow();
        }
    }

    boolean isPopAllSelected () {
        Object selectedObject = panel.lstPatches.getSelectedValue();
        Queue selectedQueue = selectedObject instanceof Queue ? ((Queue) selectedObject) : null;
        return selectedQueue != null && selectedQueue.isActive();
    }
    
    QPatch getSelectedPatch () {
        QPatch retval = null;
        Object selected = panel.lstPatches.getSelectedValue();
        if (selected instanceof QPatch) {
            retval = (QPatch) selected;
        }
        return retval;
    }
    
    Queue getSelectedQueue () {
        Queue retval = null;
        Object selected = panel.lstPatches.getSelectedValue();
        if (selected instanceof Queue) {
            retval = (Queue) selected;
        }
        return retval;
    }

    @NbBundle.Messages({
        "# {0} - queue name", "Queue_name_active=<strong>{0}</strong> (active queue)",
        "# {0} - queue name", "Queue_name_inactive={0} (inactive queue)",
        "# {0} - queue name", "MSG_QPatch_differentQueue=<html>Patch from a different queue - {0}.<br>"
            + "Will switch the active queue and go to this patch.</html>",
        "MSG_Queue_active_TT=<html>Queue is active.<br>Will pop all applied patches.</html>",
        "MSG_Queue_inactive_TT=<html>Queue is inactive.<br>Will become the active queue.</html>"
    })
    private static class PatchRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String tooltip = null;
            boolean isPatch = false;
            boolean isQueue = false;
            if (value instanceof QPatch) {
                isPatch = true;
                QPatch patch = (QPatch) value;
                StringBuilder sb = new StringBuilder(100);
                if (patch.isApplied()) {
                    sb.append("<html><strong>").append(patch.getId()).append("</strong>"); //NOI18N
                } else {
                    sb.append(patch.getId());
                }
                sb.append(" (").append(NbBundle.getMessage(GoToPatch.class, //NOI18N
                        patch.isApplied() ? "LBL_PatchSeriesPanel.applied" : "LBL_PatchSeriesPanel.unapplied")).append(')'); //NOI18N
                if (patch.isApplied()) {
                    sb.append("</html>"); //NOI18N
                }
                value = sb.toString();
                if (!patch.getQueue().isActive()) {
                    tooltip = Bundle.MSG_QPatch_differentQueue(patch.getQueue().getName());
                } else if (!patch.getMessage().trim().isEmpty()) {
                    tooltip = patch.getMessage();
                }
            } else if (value instanceof Queue) {
                Queue q = (Queue) value;
                StringBuilder sb = new StringBuilder(50).append("<html>"); //NOI18N
                sb.append(q.isActive() ? Bundle.Queue_name_active(q.getName()) : Bundle.Queue_name_inactive(q.getName()));
                sb.append("</html>"); //NOI18N
                tooltip = q.isActive() ? Bundle.MSG_Queue_active_TT() : Bundle.MSG_Queue_inactive_TT();
                value = sb.toString();
                isQueue = true;
            }
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent) comp;
                jcomp.setToolTipText(tooltip);
                if (isPatch) {
                    jcomp.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 0));
                } else if (isQueue) {
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setIcon(icon);
                    }
                } else {
                    if (comp instanceof JLabel && !isSelected) {
                        ((JLabel) comp).setForeground(UIManager.getColor("Label.disabledForeground"));
                    }
                }
            }
            return comp;
        }
        
    }
    
}
