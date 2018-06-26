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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            Iterator it = changes.iterator ();
            while (it.hasNext ())
                listModel.addElement (it.next ());
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
