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

package org.netbeans.modules.cnd.analysis.api.options;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.cnd.analysis.api.options.HintsPanel.CodeAuditProviderProxy;
import org.netbeans.modules.cnd.analysis.api.options.HintsPanel.CodeAuditProxy;
import org.netbeans.modules.cnd.analysis.api.options.HintsPanel.ExtendedModel;
import org.netbeans.modules.cnd.analysis.api.options.HintsPanel.NamedOptionProxy;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/** Contains all important listeners and logic of the Hints Panel.
 *
 */
class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener {

    private static final Map<Severity,Integer> severity2index;
    
    private static final String DESCRIPTION_HEADER = 
        "<html><head>" + // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbdocs://org.netbeans.modules.usersguide/org/netbeans/modules/usersguide/ide.css\" type=\"text/css\">" // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbresloc:/org/netbeans/modules/java/hints/resources/ide.css\" type=\"text/css\">" + // NOI18N
        "</head><body>"; // NOI18N

    private static final String DESCRIPTION_FOOTER = "</body></html>"; // NOI18N
    
    
    static {
        severity2index = new EnumMap<Severity, Integer>(Severity.class);
        severity2index.put( Severity.ERROR, 0  );
        severity2index.put( Severity.WARNING, 1  );
        severity2index.put( Severity.HINT, 2  );
    }
    
    private JTree errorTree;
    private ExtendedModel extendedModel;
    private JLabel severityLabel;
    private JComboBox severityComboBox;
    private JPanel customizerPanel;
    private JEditorPane descriptionTextArea;
    private final DefaultComboBoxModel defModel = new DefaultComboBoxModel();
    private final String defLabel = NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label"); //NOI18N
    
    HintsPanelLogic() {
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsError")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsWarning")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_WarningOnCurrentLine")); //NOI18N
   }
    
    void connect( JTree errorTree, ExtendedModel model, JLabel severityLabel, JComboBox severityComboBox,
                  JPanel customizerPanel, JEditorPane descriptionTextArea) {
        this.errorTree = errorTree;
        this.extendedModel = model;
        this.severityLabel = severityLabel;
        this.severityComboBox = severityComboBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        
        
        valueChanged( null );
        
        errorTree.addKeyListener(this);
        errorTree.addMouseListener(this);
        errorTree.getSelectionModel().addTreeSelectionListener(this);
            
        severityComboBox.addActionListener(this);
    }
    
    void disconnect() {
        
        errorTree.removeKeyListener(this);
        errorTree.removeMouseListener(this);
        errorTree.getSelectionModel().removeTreeSelectionListener(this);
            
        severityComboBox.removeActionListener(this);
                
        componentsSetEnabled( false, ROOT );
    }
    
    synchronized void applyChanges() {
        extendedModel.store();
    }
    
    synchronized void cancel() {
        extendedModel.cancel();
    }

    /** Were there any changes in the settings
     */
    boolean isChanged() {
        return extendedModel.isChanged();
    }
    
    static Object getUserObject( TreePath path ) {
        if( path == null ) {
            return null;
        }
        Object node = path.getLastPathComponent();
        if (node instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode)node;
            return tn.getUserObject();
        }
        return null;
    }
    
    static Object getUserObject( DefaultMutableTreeNode node ) {
        return node.getUserObject();
    }
    
    boolean isSelected( DefaultMutableTreeNode node ) {
        for( int i = 0; i < node.getChildCount(); i++ ) {
            DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);
            Object o = ch.getUserObject();
            if ( o instanceof CodeAuditProxy ) {
                CodeAuditProxy hint = (CodeAuditProxy)o;
                if ( hint.isEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // MouseListener implementation --------------------------------------------
    
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        TreePath path = errorTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if ( path != null ) {
            Rectangle r = errorTree.getPathBounds(path);
            if (r != null) {
                if ( r.contains(p)) {
                    int shift = p.x - r.x;
                    if (shift < r.height) {
                        toggle( path );
                    }
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
    
    // KeyListener implementation ----------------------------------------------

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ) {

            if ( e.getSource() instanceof JTree ) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();

                if ( toggle( path )) {
                    e.consume();
                }
            }
        }
    }
    
    // TreeSelectionListener implementation ------------------------------------
    
    @Override
    public void valueChanged(TreeSelectionEvent ex) {            
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof CodeAuditProxy ) {
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }

            CodeAuditProxy hint = (CodeAuditProxy) o;
            
            // Enable components
            componentsSetEnabled(true, AUDIT);
            
            // Set proper values to the componetnts

            if ("action".equals(hint.getKind())) {// NOI18N
                severityComboBox.setSelectedIndex(severity2index.get(Severity.HINT));
                severityComboBox.setEnabled(false);
            } else {
                if ("error".equals(hint.minimalSeverity())) { // NOI18N
                    severityComboBox.setSelectedIndex(severity2index.get(Severity.ERROR));
                } else if ("warning".equals(hint.minimalSeverity())) { // NOI18N
                    severityComboBox.setSelectedIndex(severity2index.get(Severity.WARNING));
                }  else {
                    severityComboBox.setSelectedIndex(severity2index.get(Severity.HINT));
                }
            }
            
            String description = hint.getDescription();
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
                                    
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = null; //hint.getCustomizer()
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
        } else if ( o instanceof CodeAuditProviderProxy ) {
            CodeAuditProviderProxy hint = (CodeAuditProviderProxy) o;
            String description = hint.getDescription();
            componentsSetEnabled(true, PROVIDER);
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = hint.createComponent();
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
       } else if (o instanceof NamedOptionProxy) {
            NamedOptionProxy option = (NamedOptionProxy)o;
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }
            componentsSetEnabled(true, OPTION);
            String description = option.getDescription();
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = option.createComponent();
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
        } else {
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }
            componentsSetEnabled(false, ROOT);
        }
    }
    
    // ActionListener implementation -------------------------------------------
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if( errorTree.getSelectionPath() == null){
            return;
        }
        if (severityComboBox.equals(e.getSource())) {
            Object o = getUserObject(errorTree.getSelectionPath());
            if ( o instanceof CodeAuditProxy ) {
                CodeAuditProxy hint = (CodeAuditProxy) o;
                if (index2severity(severityComboBox.getSelectedIndex()) == Severity.ERROR) {
                    hint.setSeverity(Severity.ERROR);
                } else if (index2severity(severityComboBox.getSelectedIndex()) == Severity.WARNING) {
                    hint.setSeverity(Severity.WARNING);
                } else {
                    hint.setSeverity(Severity.HINT);
                }
            }
        }
    }

   
    // ChangeListener implementation -------------------------------------------
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------

    private String wrapDescription( String description ) {
        return new StringBuffer( DESCRIPTION_HEADER ).append(description).append(DESCRIPTION_FOOTER).toString();        
    }
    
    private Severity index2severity( int index ) {
        for( Map.Entry<Severity,Integer> e : severity2index.entrySet()) {
            if ( e.getValue() == index ) {
                return e.getKey();
            }
        }
        throw new IllegalStateException( "Unknown severity"); //NOI18N
    }
       

    private boolean toggle(TreePath treePath) {
        if (treePath == null) {
            return false;
        }
        Object o = getUserObject(treePath);
        ExtendedModel model = extendedModel;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        if (o instanceof CodeAuditProxy) {
            CodeAuditProxy hint = (CodeAuditProxy) o;
            hint.setEnabled(!hint.isEnabled()); //NOI18N
            model.nodeChanged(node);
            errorTree.repaint();
        } else if (o instanceof CodeAuditProviderProxy) {
            CodeAuditProviderProxy provider = (CodeAuditProviderProxy) o;
            boolean hasEnabled = false;
            boolean hasDisabled = false;
            for(int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                CodeAuditProxy audit = (CodeAuditProxy) childAt.getUserObject();
                if (audit.isEnabled()) {
                    hasEnabled = true;
                } else {
                    hasDisabled = true;
                }
            }
            if (hasEnabled) {
                if (hasDisabled) {
                    for(int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                        CodeAuditProxy audit = (CodeAuditProxy) childAt.getUserObject();
                        audit.setEnabled(true);
                    }
                } else {
                    for(int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                        CodeAuditProxy audit = (CodeAuditProxy) childAt.getUserObject();
                        audit.setEnabled(false);
                    }
                }
            } else {
                for(int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(i);
                    CodeAuditProxy audit = (CodeAuditProxy) childAt.getUserObject();
                    audit.setEnabled(true);
                }
            }
            model.nodeChanged(node);
            errorTree.repaint();
       } else if (o instanceof NamedOptionProxy) {
            NamedOptionProxy option = (NamedOptionProxy)o;
            if (option.getBoolean()) {
                option.setBoolean(false);
            } else {
                option.setBoolean(true);
            }
            model.nodeChanged(node);
            errorTree.repaint();
       }
            
        return false;
    }
    
    private static final int ROOT = 0;
    private static final int PROVIDER = 1;
    private static final int AUDIT = 2;
    private static final int OPTION = 3;
    
    
    private void componentsSetEnabled( boolean enabled, int component ) {
        if ( !enabled ) {
            customizerPanel.removeAll();
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
            severityComboBox.setSelectedIndex(0);
            descriptionTextArea.setText(""); // NOI18N
        }
        if (component == ROOT) {
            severityComboBox.setEnabled(false);
        } else if (component == AUDIT) {
            severityComboBox.setEnabled(enabled);
        } else  if (component == PROVIDER) {
            severityComboBox.setEnabled(false);
        } else  if (component == OPTION) {
            severityComboBox.setEnabled(false);
        }
        descriptionTextArea.setEnabled(enabled);
    }
    
}
