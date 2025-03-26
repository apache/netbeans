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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;

import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.debugger.ui.PersistentController;
import org.openide.awt.Mnemonics;

import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public class ConnectorPanel extends JPanel implements ActionListener, HelpCtx.Provider {

    public static final String PROP_TYPE = "type";
    
    private static final String FIRST_ATTACH_TYPE = "org.netbeans.modules.debugger.jpda.ui.JPDAAttachType"; // NOI18N
    
    private static final String ERGONOMICS_CLASS = "org.netbeans.modules.ide.ergonomics";   // NOI18N

    private static final int TOTAL_SLOTS = 4; // TOTAL_SLOTS - 1 is the maximal number of items that can appear in the attach history

    /** Contains list of AttachType names.*/
    private JComboBox             cbAttachTypes;
    /** Switches off listening on cbAttachTypes.*/
    private boolean               doNotListen;
    /** Contains list of installed AttachTypes.*/
    private List<AttachType>      attachTypes;
    /** Currently displayed panel.*/
    private Controller            controller;
    /** Current attach type, which is stored into settings for the next invocation. */
    private AttachType            currentAttachType;
    /** Help for the dialog */
    private HelpCtx               help = HelpCtx.DEFAULT_HELP;


    public ConnectorPanel ()  {
        getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, "ACSD_ConnectorPanel")
        );
        cbAttachTypes = new JComboBox ();
        cbAttachTypes.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        List<? extends AttachType> types = DebuggerManager.getDebuggerManager ().lookup (
            null, AttachType.class
        );
        attachTypes = new ArrayList<AttachType>(types);
        for (AttachType att : types) {
            if (att.getTypeDisplayName() == null) {
                attachTypes.remove(att);
            }
        }
        String defaultAttachTypeName =
                Properties.getDefault ().getProperties ("debugger").getString ("last_attach_type", null);
        int defaultIndex = 0;
        int i, k = attachTypes.size ();
        attachTypes.sort(new Comparator<AttachType>() {
            @Override
            public int compare(AttachType at1, AttachType at2) {
                if (FIRST_ATTACH_TYPE.equals(at1.getClass().getName())) {
                    return -1;
                }
                if (FIRST_ATTACH_TYPE.equals(at2.getClass().getName())) {
                    return +1;
                }
                return at1.getTypeDisplayName().compareTo(at2.getTypeDisplayName());
            }
        });
        for (i = 0; i < k; i++) {
            AttachType at = attachTypes.get (i);
            String displayName = at.getTypeDisplayName();
            cbAttachTypes.addItem (displayName);
            if (displayName.equals(defaultAttachTypeName)) {
                defaultIndex = i;
            }
        }
        for (i = defaultIndex; i < k; ) {
            AttachType at = attachTypes.get (i);
            if (!at.getClass().getName().startsWith(ERGONOMICS_CLASS)) {
                // Find some real attach type
                defaultIndex = i;
                break;
            }
            i++;
            // Check all indexes, but the default
            if (i == k) {
                i = 0;
            }
            if (i == defaultIndex) {
                break;
            }
        }

        cbAttachTypes.setActionCommand ("SwitchMe!"); // NOI18N
        cbAttachTypes.addActionListener (this);

        setLayout (new GridBagLayout ());
        setBorder (new EmptyBorder (11, 11, 0, 10));
        refresh (defaultIndex);
    }
    
    private void refresh (int index) {
        JLabel cbLabel = new JLabel();
        Mnemonics.setLocalizedText(cbLabel,
                NbBundle.getMessage (ConnectorPanel.class, "CTL_Connect_through"));
        cbLabel.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        cbLabel.setLabelFor (cbAttachTypes);

        GridBagConstraints c = new GridBagConstraints ();
        c.insets = new Insets (0, 0, 6, 6);
        add (cbLabel, c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.gridwidth = 0;
        c.insets = new Insets (0, 3, 6, 0);
        doNotListen = true;
        if (cbAttachTypes.getItemCount() > 0) {
            cbAttachTypes.setSelectedIndex (index);
        }
        doNotListen = false;
        add (cbAttachTypes, c);
        c.insets = new Insets (0, 0, 6, 0);
        add (new JSeparator(), c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.gridwidth = 0;
        if (cbAttachTypes.getItemCount() == 0) {
            JLabel noAttachType = new JLabel(NbBundle.getMessage(ConnectorPanel.class, "CTL_Attach_Types_Not_Found"));
            add (noAttachType, c);
        } else {
            AttachType attachType = attachTypes.get (index);
            JComponent customizer = attachType.getCustomizer ();
            help = HelpCtx.findHelp (customizer);
            controller = attachType.getController();
            if (controller == null && (customizer instanceof Controller)) {
                Exceptions.printStackTrace(new IllegalStateException("FIXME: JComponent "+customizer+" must not implement Controller interface!"));
                controller = (Controller) customizer;
            }
            firePropertyChange(PROP_TYPE, null, customizer);
            this.currentAttachType = attachType;
            add (customizer, c);
        }
    }


    /**
     * Called when a user selects debugger type in a combo-box.
     */
    @Override
    public void actionPerformed (ActionEvent e) {
        if (doNotListen) return;
        if (e.getActionCommand ().equals ("SwitchMe!")); // NOI18N
        removeAll ();
        refresh (((JComboBox) e.getSource ()).getSelectedIndex ());
        Component w = getParent ();
        while (!(w instanceof Window))
            w = w.getParent ();
        if (w != null) ((Window) w).pack (); // ugly hack...
    }
    
    Controller getController() {
        return controller;
    }
    
    boolean cancel () {
        if (controller == null) return true;
        return controller.cancel ();
    }
    
    boolean ok () {
        String defaultAttachTypeName = currentAttachType.getTypeDisplayName();
        Properties props = Properties.getDefault().getProperties("debugger");
        props.setString("last_attach_type", defaultAttachTypeName);
        if (controller == null) return true;
        props = props.getProperties("last_attaches");
        Integer[] usedSlots = (Integer[]) props.getArray("used_slots", new Integer[0]);
        int freeSlot = -1;
        if (usedSlots.length >= TOTAL_SLOTS) {
            freeSlot = usedSlots[TOTAL_SLOTS - 1];
        } else {
            for (int x = 0; x < TOTAL_SLOTS; x++) {
                boolean found = true;
                for (int y = 0; y < usedSlots.length; y++) {
                    if (x == usedSlots[y]) {
                        found = false;
                        break;
                    }
                } // for
                if (found) {
                    freeSlot = x;
                    break;
                }
            } // for
        }
        String dispName = null;
        if (controller instanceof PersistentController) {
            PersistentController pController = (PersistentController) controller;
            Properties slot = props.getProperties("slot_" + freeSlot);
            dispName = pController.getDisplayName();
            if (dispName != null && dispName.trim().length() > 0) {
                slot.setString("display_name", dispName);
                pController.save(slot.getProperties("values"));
                slot.setString("attach_type", defaultAttachTypeName);
            }
        }

        boolean ok = controller.ok ();

        if (ok) {
            GestureSubmitter.logAttach(defaultAttachTypeName);
            if (dispName != null && dispName.trim().length() > 0) {
                int newLength = Math.min(TOTAL_SLOTS - 1, usedSlots.length + 1);
                int excludeIndex = -1;
                // remove duplicities having the same display name
                for (int x = 0; x < usedSlots.length; x++) {
                    String str = props.getProperties("slot_" + usedSlots[x]).getString("display_name", "");
                    if (dispName.equals(str) && x < TOTAL_SLOTS - 1) {
                        excludeIndex = x;
                        newLength--;
                        break;
                    }
                }
                Integer[] newUsedSlots = new Integer[newLength];
                int copyFrom = 0;
                for (int x = 1; x < newLength; x++) {
                    if (copyFrom == excludeIndex) {
                        copyFrom++;
                    }
                    newUsedSlots[x] = usedSlots[copyFrom];
                    copyFrom++;
                } // for
                newUsedSlots[0] = freeSlot;
                props.setArray("used_slots", newUsedSlots);
            } // if
            DebugMainProjectAction.attachHistoryChanged();
        } // if
        return ok;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return help;
    }
}



