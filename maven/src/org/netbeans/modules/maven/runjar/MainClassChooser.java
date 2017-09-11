/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.runjar;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.maven.runjar.Bundle.*;
import org.openide.util.NbBundle.Messages;

/** Browses and allows to choose a project's main class.
 *
 * @author  Jiri Rechtacek
 */
class MainClassChooser extends JPanel {

    private ChangeListener changeListener;
    private Collection<ElementHandle<TypeElement>> possibleMainClasses;
            
    /** Creates new form MainClassChooser */
    MainClassChooser (FileObject... sourcesRoots) {
        initComponents();
        jMainClassList.setCellRenderer(new MainClassRenderer());
        initClassesView (sourcesRoots);
    }
    
    @Messages("LBL_ChooseMainClass_NO_CLASSES_NODE=<No main classes found>")
    private void initClassesView (final FileObject... sourcesRoots) {
        possibleMainClasses = null;
        jMainClassList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        jMainClassList.setListData (getWarmupList ());
        jMainClassList.addListSelectionListener (new ListSelectionListener () {
            @Override
            public void valueChanged (ListSelectionEvent evt) {
                if (changeListener != null) {
                    changeListener.stateChanged (new ChangeEvent (evt));
                }
            }
        });
        // support for double click to finish dialog with selected class
        jMainClassList.addMouseListener (new MouseListener () {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (MouseUtils.isDoubleClick (e)) {
                    if (getSelectedMainClass () != null) {
                        if (changeListener != null) {
                            changeListener.stateChanged (new ChangeEvent (e));
                        }
                    }
                }
            }
            @Override
            public void mousePressed (MouseEvent e) {}
            @Override
            public void mouseReleased (MouseEvent e) {}
            @Override
            public void mouseEntered (MouseEvent e) {}
            @Override
            public void mouseExited (MouseEvent e) {}
        });
        
        RequestProcessor.getDefault ().post (new Runnable () {
            @Override
            public void run () {
                
                possibleMainClasses = SourceUtils.getMainClasses(sourcesRoots);
                if (possibleMainClasses.isEmpty ()) {                    
                    SwingUtilities.invokeLater( new Runnable () {
                        @Override
                        public void run () {
                            jMainClassList.setListData (new String[] { LBL_ChooseMainClass_NO_CLASSES_NODE () } ); // NOI18N
                        }
                    });                    
                } else {
                    final ElementHandle<TypeElement>[] arr = possibleMainClasses.toArray(new ElementHandle[possibleMainClasses.size()]);
                    // #46861, sort name of classes
                    Arrays.sort (arr, new MainClassComparator());
                    SwingUtilities.invokeLater(new Runnable () {
                        @Override
                        public void run () {
                            jMainClassList.setListData (arr);
                            jMainClassList.setSelectedIndex (0);
                        }
                    });                    
                }
            }
        });
        
    }
    
    @Messages("LBL_ChooseMainClass_WARMUP_MESSAGE=Initializing view, please wait ...")
    private Object[] getWarmupList () {        
          return new Object[] {LBL_ChooseMainClass_WARMUP_MESSAGE ()}; //NOI18N
    }
    
    private boolean isValidMainClassName (Object value) {
        return (possibleMainClasses != null) && (possibleMainClasses.contains (value));
    }


    /** Returns the selected main class.
     *
     * @return name of class or null if no class with the main method is selected
     */    
    String getSelectedMainClass () {
        if (isValidMainClassName (jMainClassList.getSelectedValue ())) {
            return ((ElementHandle)jMainClassList.getSelectedValue()).getQualifiedName();
        } else {
            return null;
        }
    }
    
    
    
    public void addChangeListener (ChangeListener l) {
        changeListener = l;
    }
    
    public void removeChangeListener (ChangeListener l) {
        changeListener = null;
    }
    
    // Used only from unit tests to suppress check of main method. If value
    // is different from null it will be returned instead.
    public static Boolean unitTestingSupport_hasMainMethodResult = null;
    
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java 
     * @return false if parameter is null or doesn't contain SourceCookie
     * or SourceCookie doesn't contain the main method
     */    
    public static boolean hasMainMethod (FileObject fo) {
      // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        return !SourceUtils.getMainClasses(fo).isEmpty();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblMainClass = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMainClassList = new javax.swing.JList();
        rbSession = new javax.swing.JRadioButton();
        rbPermanent = new javax.swing.JRadioButton();

        setPreferredSize(new java.awt.Dimension(380, 300));

        lblMainClass.setLabelFor(jMainClassList);
        org.openide.awt.Mnemonics.setLocalizedText(lblMainClass, org.openide.util.NbBundle.getMessage(MainClassChooser.class, "CTL_AvaialableMainClasses")); // NOI18N

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 200));
        jScrollPane1.setViewportView(jMainClassList);
        jMainClassList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooser.class, "AD_jMainClassList")); // NOI18N

        buttonGroup1.add(rbSession);
        rbSession.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbSession, org.openide.util.NbBundle.getMessage(MainClassChooser.class, "MainClassChooser.rbSession.text")); // NOI18N

        buttonGroup1.add(rbPermanent);
        org.openide.awt.Mnemonics.setLocalizedText(rbPermanent, org.openide.util.NbBundle.getMessage(MainClassChooser.class, "MainClassChooser.rbPermanent.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbPermanent)
                    .addComponent(rbSession)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                    .addComponent(lblMainClass))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMainClass, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbSession)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbPermanent)
                .addContainerGap())
        );

        lblMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooser.class, "AD_jMainClassList")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JList jMainClassList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblMainClass;
    javax.swing.JRadioButton rbPermanent;
    javax.swing.JRadioButton rbSession;
    // End of variables declaration//GEN-END:variables


    private static final class MainClassRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName;
            if (value instanceof ElementHandle) {
                displayName = ((ElementHandle)value).getQualifiedName();
            } else {
                displayName = value.toString ();
            }
            return super.getListCellRendererComponent (list, displayName, index, isSelected, cellHasFocus);
        }
    }
    
    private static class MainClassComparator implements Comparator<ElementHandle> {
            
        @Override
        public int compare(ElementHandle arg0, ElementHandle arg1) {
            return arg0.getQualifiedName().compareTo(arg1.getQualifiedName());
        }
    }

}
