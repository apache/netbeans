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

package org.netbeans.modules.j2ee.ddloaders.client;

import java.awt.Component;
import java.util.List;
import java.util.Iterator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileStateInvalidException;

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
    
    @Override
    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension(600, 400);
    }
    
    synchronized void setChanges (List changes) {
        listModel.clear ();
        if (changes != null) {
            Iterator<?> it = changes.iterator ();
            while (it.hasNext ()) {
                listModel.addElement (it.next ());
            }
        }
    }
    
    static class ChangesListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            try {
                if ((comp instanceof JLabel) && (value instanceof DDChangeEvent)) {
                    DDChangeEvent evt = (DDChangeEvent)value;
                    String label = "";  // NOI18N
                    String clz = evt.getNewValue ();
                    if (evt.getType () == DDChangeEvent.EJB_ADDED) {
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_addServletElement", clz);
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_CLASS_CHANGED){
                        label = NbBundle.getMessage(DDChangesPanel.class, 
                            "LAB_changeEjbElement", evt.getOldValue (), //NOI18N
                                evt.getNewValue()); 
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_CLASS_DELETED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_deleteEjbElement", evt.getNewValue()); //NOI18N
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_HOME_CHANGED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_changeHomeElement", evt.getOldValue(), //NOI18N
                                evt.getNewValue());
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_REMOTE_CHANGED){
                        label = NbBundle.getMessage(DDChangesPanel.class, 
                            "LAB_changeRemoteElement",                  //NOI18N
                                evt.getOldValue(), evt.getNewValue());
                    }
                    else if (evt.getType() == 
                        DDChangeEvent.EJB_LOCAL_HOME_CHANGED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_changeLocalHomeElement",               //NOI18N
                                evt.getOldValue(), evt.getNewValue());
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_LOCAL_CHANGED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_changeLocalElement",                   //NOI18N 
                                evt.getOldValue(), evt.getNewValue());
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_HOME_DELETED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_deleteHomeElement",                    //NOI18N
                                evt.getNewValue ());
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_REMOTE_DELETED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_deleteRemoteElement",                  //NOI18N
                                evt.getNewValue ());
                    }
                    else if (evt.getType() == 
                        DDChangeEvent.EJB_LOCAL_HOME_DELETED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_deleteLocalHomeElement",               //NOI18N 
                                evt.getNewValue());                     
                    }
                    else if (evt.getType() == DDChangeEvent.EJB_LOCAL_DELETED){
                        label = NbBundle.getMessage(DDChangesPanel.class,
                            "LAB_deleteLocalElement",                   //NOI18N
                                evt.getNewValue ());
                    }
                    else if (evt.getType () == DDChangeEvent.EJB_MOVED) {
                        String fsname;
                        try {
                            fsname = evt.getOldDD ().getPrimaryFile ().getFileSystem ().getDisplayName ();
                        }
                        catch (FileStateInvalidException e) {
                            fsname = ""; // NOI18N
                        }
                        label = NbBundle.getMessage (DDChangesPanel.class, "LAB_moveServletElement", clz, fsname);
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
