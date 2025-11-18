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

package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ImportClassPanel extends javax.swing.JPanel {

    private JavaSource javaSource;
    private DefaultListModel model;
    private final int position;
    private final JTextComponent target;
    private final String altKey = KeyEvent.getKeyText(org.openide.util.Utilities.isMac() ? KeyEvent.VK_META : KeyEvent.VK_ALT);
    
    /** Creates new form ImportClassPanel */
    public ImportClassPanel(List<TypeElement> priviledged, List<TypeElement> denied, Font font, JavaSource javaSource, int position, JTextComponent target ) {
        // System.err.println("priviledged=" + priviledged);
        // System.err.println("denied=" + denied);
        this.javaSource = javaSource;
        this.position = position;
        this.target = target;
        createModel(priviledged, denied);
        initComponents();
        setBackground(jList1.getBackground());
        
        if ( model.size() > 0) {
            jList1.setModel( model );
            setFocusable(false);        
            setNextFocusableComponent(jList1);
            jScrollPane1.setBackground( jList1.getBackground() );
            setBackground( jList1.getBackground() );
            if ( font != null ) {
                jList1.setFont(font);
            }
            int modelSize = jList1.getModel().getSize();
            if ( modelSize > 0 ) {
                jList1.setSelectedIndex(0);            
            }
            jList1.setVisibleRowCount( modelSize > 8 ? 8 : modelSize );
            jList1.setCellRenderer( new Renderer( jList1 ) );
            jList1.grabFocus();
        }
        else {            
            remove( jScrollPane1 );
            JLabel nothingFoundJL = new JLabel("<No Classes Found>");
            if ( font != null ) {
                nothingFoundJL.setFont(font);
            }
            nothingFoundJL.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));
            nothingFoundJL.setEnabled(false);
            nothingFoundJL.setBackground(jList1.getBackground());
            //nothingFoundJL.setOpaque(true);
            add( nothingFoundJL );
        }
	
	setA11Y();
    }
    
    private void setA11Y() {
	this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_ACN"));
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_ACSD"));
	jList1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_JList1_ACN"));
	jList1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_JList1_ACSD"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        ctrlLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(64, 64, 64)));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMouseReleased(evt);
            }
        });
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                listKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setLabelFor(jList1);
        jLabel1.setText("Type to import:");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jLabel1.setOpaque(true);
        add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jPanel1.setLayout(new java.awt.BorderLayout());

        ctrlLabel.setText(org.openide.util.NbBundle.getMessage(ImportClassPanel.class, "LBL_PackageImport", altKey)); // NOI18N
        jPanel1.add(ctrlLabel, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void listMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseReleased
        importClass( 
                getSelected(), 
                (evt.getModifiers() & (org.openide.util.Utilities.isMac() ? InputEvent.META_MASK : InputEvent.ALT_MASK)) > 0,
                (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0);
    }//GEN-LAST:event_listMouseReleased

    private void listKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listKeyReleased
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(evt);
        if ( ks.getKeyCode() == KeyEvent.VK_ENTER || 
             ks.getKeyCode() == KeyEvent.VK_SPACE ) {
            importClass( 
                    getSelected(),
                    (evt.getModifiers() & (org.openide.util.Utilities.isMac() ? InputEvent.META_MASK : InputEvent.ALT_MASK)) > 0,
                    (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0);
        }
    }//GEN-LAST:event_listKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel ctrlLabel;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JList jList1;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    public String getSelected() {
        TypeDescription typeDescription = ((TypeDescription)jList1.getSelectedValue());
        return typeDescription == null ? null : typeDescription.qualifiedName;
    }
    
    private void createModel( List<TypeElement> priviledged, List<TypeElement> denied ) {
                
        List<TypeDescription> l = new ArrayList<TypeDescription>( priviledged.size() );                
        for (TypeElement typeElement : priviledged) {
            l.add( new TypeDescription( typeElement, false ) );            
        }
        
        List<TypeDescription> ld = new ArrayList<TypeDescription>( priviledged.size() );                        
        for (TypeElement typeElement : denied ) {
            l.add( new TypeDescription( typeElement, true ) );
        }
        
        Collections.sort( l );
        
        model = new DefaultListModel();
        for( TypeDescription td : l ) {
            model.addElement( td );
        }
        
        
        
    }
    
    private void importClass( String name, final boolean packageImport, final boolean useFqn ) {
        PopupUtil.hidePopup();
        
        if ( packageImport && !useFqn ) {
            int index = name.lastIndexOf('.'); // NOI18N 
            if ( index != -1 ) {
                name = name.substring(0, index);        
            }
        }
        
        final String fqn = name;
        
        if (fqn != null) {
            final AtomicBoolean cancel = new AtomicBoolean();
            final Task<WorkingCopy> task = new Task<WorkingCopy>() {
                
                public void run(final WorkingCopy wc) throws IOException {
                    if (cancel != null && cancel.get())
                        return ;
                    wc.toPhase(Phase.RESOLVED);
                    if (cancel != null && cancel.get())
                        return ;
                    CompilationUnitTree cut = wc.getCompilationUnit();
                    
                    if ( useFqn ) {
                        if ( replaceSimpleName(fqn, wc) )
                            return;
                        Document doc = wc.getDocument();
                        if (doc instanceof BaseDocument) {
                            try {
                                int[] block = Utilities.getIdentifierBlock((BaseDocument)doc, position);
                                doc.remove(block[0], block[1] - block[0]);
                                doc.insertString(block[0], fqn, null);
                                return;
                            } catch (BadLocationException ex) {
                            }
                        }
                    }
                    
                    // Test whether already imported                    
                    if ( isImported(fqn, cut.getImports())) {
                        Utilities.setStatusText(EditorRegistry.lastFocusedComponent(), 
                        NbBundle.getMessage(
                                ImportClassPanel.class,
                                packageImport ? "MSG_PackageAlreadyImported" : "MSG_ClassAlreadyImported", 
                                fqn), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                        return;
                    }
                    
                    Element e = packageImport ? wc.getElements().getPackageElement(fqn) : wc.getElements().getTypeElement(fqn);
                    if (e == null) {
                        Utilities.setStatusText(EditorRegistry.lastFocusedComponent(), 
                        NbBundle.getMessage(
                                ImportClassPanel.class,
                                packageImport ? "MSG_CannotResolvePackage" : "MSG_CannotResolveClass", 
                                fqn), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                        return;
                    }
                    
                    CompilationUnitTree cutCopy = GeneratorUtilities.get(wc).addImports(cut, Collections.singleton(e));
                    wc.rewrite(cut, cutCopy);
                }
                
                private boolean replaceSimpleName(String fqn, WorkingCopy wc) {
                    
                    TreeUtilities tu = wc.getTreeUtilities();
                    TreePath tp = tu.pathFor(position);
                    TreePath tpPlusOne = tu.pathFor(position + 1); // on the beginning of desired import
                    TreeMaker tm = wc.getTreeMaker();

                    if ( tp.getLeaf().getKind() == Tree.Kind.IDENTIFIER) {
                        wc.rewrite(tp.getLeaf(), tm.Identifier(fqn));
                        return true;
                    }

                    if (tp.getLeaf().getKind() != Tree.Kind.IDENTIFIER && tpPlusOne.getLeaf().getKind() == Tree.Kind.IDENTIFIER) {
                        wc.rewrite(tpPlusOne.getLeaf(), tm.Identifier(fqn));
                        return true;
                    }

                    return false;
                }
                
                private boolean isImported(String fqn, List<? extends ImportTree> imports) {
                    for (ImportTree i : imports) {
                        if( fqn.equals( i.getQualifiedIdentifier().toString() )) {
                            return true;
                        }
                    }
                    return false;   
                    
                }
                
            };
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                public void run() {
                    try {
                        ModificationResult mr = javaSource.runModificationTask(task);
                        GeneratorUtils.guardedCommit(target, mr);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, NbBundle.getMessage(ImportClassPanel.class, "LBL_Fast_Import"), cancel, false); //NOI18N
        }
    }
            
    private static class Renderer extends DefaultListCellRenderer {
        
        private static int DARKER_COLOR_COMPONENT = 5;
        private static int LIGHTER_COLOR_COMPONENT = DARKER_COLOR_COMPONENT;
                
        
        private Color fgColor;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;
        
        public Renderer( JList list ) {
            setFont( list.getFont() );            
            fgColor = list.getForeground();
            bgColor = list.getBackground();
            bgColorDarker = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();        
        }
        
        public Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean hasFocus) {
                        
            if ( isSelected ) {
                setForeground(fgSelectionColor);
                setBackground(bgSelectionColor);
            }
            else {
                setForeground(fgColor);
                setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }
            
            if ( value instanceof TypeDescription ) {
                TypeDescription td = (TypeDescription)value;                
                 // setIcon(td.getIcon());
                if ( td.isDenied ) {
                    setText(JavaFixAllImports.NOT_VALID_IMPORT_HTML + td.qualifiedName);
                } else {
                    setText(td.qualifiedName);
                }
                setIcon( ElementIcons.getElementIcon( td.kind, null ) );
            }
            else {
                setText( value.toString() );
            }
                                    
            return this;
        }
        
     }
     
     private static class TypeDescription implements Comparable<TypeDescription> {
         private boolean isDenied;
         private final ElementKind kind;
         private final String qualifiedName;
                          
         public TypeDescription(TypeElement typeElement, boolean isDenied ) {
            this.isDenied = isDenied;
            this.kind = typeElement.getKind();
            this.qualifiedName = typeElement.getQualifiedName().toString();
         } 

        public int compareTo( TypeDescription o ) {
            
            if ( isDenied && !o.isDenied ) {
                return 1;
            }
            else if ( !isDenied && o.isDenied ) {
                return -1;
            }
            else {
                return qualifiedName.compareTo( o.qualifiedName );
            }        
        }
         
         
         
     }
            

}
