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

package org.netbeans.modules.j2ee.ddloaders.web;

import java.awt.Component;
import java.util.List;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.modules.j2ee.ddloaders.web.event.DDChangeEvent;

/** Panel that contains list of changes for deployment descriptor
 * to accomodate recent servlet modification.
 *
 * @author  Radim Kubacki
 */
public class DDChangesPanel extends JPanel {
    
    private JPanel changesPanel;
    private JScrollPane jScrollPane1;
    /* pkg private */ JList changesList;
    
    /* pkg private */ DefaultListModel listModel;
    
    /** Initializes the Form */
    public DDChangesPanel (String caption, final JButton processButton) {
        setLayout (new java.awt.BorderLayout (0, 12));
        setBorder (new EmptyBorder (12, 12, 11, 0));
        
        JTextArea text = new JTextArea ();
        text.setEnabled (false);
        text.setEditable (false);
        text.setDisabledTextColor (UIManager.getColor ("Label.foreground")); // NOI18N
        text.setBackground (UIManager.getColor ("Label.background")); // NOI18N
        text.setLineWrap (true);
        text.setWrapStyleWord (true);
        text.setText (caption);
        add (text, "North"); // NOI18N
        
        changesPanel = new JPanel ();
        changesPanel.setLayout (new java.awt.BorderLayout (5, 5));
        
        JLabel changesLabel = new JLabel ();
        changesLabel.setText (NbBundle.getMessage (DDChangesPanel.class, "LAB_ChangesList"));
        changesLabel.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (DDChangesPanel.class, "ACS_ChangesListA11yDesc"));  // NOI18N
        changesPanel.add (changesLabel, "North"); // NOI18N
        
        jScrollPane1 = new JScrollPane ();
        
        listModel = new DefaultListModel ();
        
        changesList = new JList (listModel);
        changesList.setToolTipText (NbBundle.getMessage (DDChangesPanel.class, "HINT_ChangesList"));
        changesList.setCellRenderer (new ChangesListCellRenderer ());
        changesList.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                processButton.setEnabled (!changesList.isSelectionEmpty ());
            }
        });
        changesLabel.setLabelFor (changesList);
        changesLabel.setDisplayedMnemonic (NbBundle.getMessage (DDChangesPanel.class, "LAB_ChangesList_Mnemonic").charAt (0));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DDChangesPanel.class, "ACS_ChangesListA11yPanelDesc"));
        
        jScrollPane1.setViewportView (changesList);
        
        changesPanel.add (jScrollPane1, "Center"); // NOI18N
        
        add (changesPanel, "Center"); // NOI18N
    }
    
    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension(600, 400);
    }
    
    synchronized void setChanges (List<DefaultListModel> changes) {
        listModel.clear ();
        if (changes != null) {
            Iterator<DefaultListModel> it = changes.iterator();
            while (it.hasNext ())
                listModel.addElement (it.next ());
        }
    }
    
    static class ChangesListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            try {
                if ((comp instanceof JLabel) && (value instanceof DDChangeEvent)) {
                    DDChangeEvent evt = (DDChangeEvent)value;
                    String label = "";  // NOI18N
                    String clz = evt.getNewValue ();
                    if (evt.getType () == DDChangeEvent.SERVLET_ADDED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_addServletElement", clz);
                    }
                    else if (evt.getType () == DDChangeEvent.SERVLET_CHANGED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_changeServletElement", evt.getOldValue (), evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.SERVLET_DELETED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_deleteServletElement", evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.SERVLET_MOVED) {
                        String fsname;
                        try {
                            fsname = evt.getOldDD ().getPrimaryFile ().getFileSystem ().getDisplayName ();
                        }
                        catch (FileStateInvalidException e) {
                            fsname = ""; // NOI18N
                        }
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_moveServletElement", clz, fsname);
                    }
                    else if (evt.getType () == DDChangeEvent.FILTER_CHANGED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_changeFilterElement", evt.getOldValue (), evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.FILTER_DELETED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_deleteFilterElement", evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.LISTENER_CHANGED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_changeListenerElement", evt.getOldValue (), evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.LISTENER_DELETED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_deleteListenerElement", evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.JSP_CHANGED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_changeJspElement", evt.getOldValue (), evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.JSP_DELETED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_deleteJspElement", evt.getNewValue ());
                    }                    
                    ((JLabel)comp).setText (label);
                }
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
            return comp;
        }
    }
}
