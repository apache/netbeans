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
/*
 * ClassMemberPanelUi.java
 *
 * Created on November 8, 2006, 4:03 PM
 */

package org.netbeans.modules.beans;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.Action;
import javax.lang.model.element.Element;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author  phrebejk
 */
public class BeanPanelUI extends javax.swing.JPanel
        implements ExplorerManager.Provider/* ,FiltersManager.FilterChangeListener */ {
    
    private static WaitNode WAIT_NODE;
    
    private ExplorerManager manager = new ExplorerManager();
    private MyBeanTreeView elementView;
    // private TapPanel filtersPanel;
    private JLabel filtersLbl;
    private Lookup lookup = null; // XXX may need better lookup
    // private ClassMemberFilters filters;
    
    private Action[] actions; // General actions for the panel
    
    private static final Rectangle ZERO = new Rectangle(0,0,1,1);

    
    /** Creates new form ClassMemberPanelUi */
    public BeanPanelUI() {
                      
        initComponents();
        
        // Tree view of the elements
        elementView = createBeanTreeView();        
        add(elementView, BorderLayout.CENTER);
               
        // filters
//        filtersPanel = new TapPanel();
//        filtersLbl = new JLabel(NbBundle.getMessage(ClassMemberPanelUI.class, "LBL_Filter")); //NOI18N
//        filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
//        filtersPanel.add(filtersLbl);
//        filtersPanel.setOrientation(TapPanel.DOWN);
//        // tooltip
//        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
//                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
//        String keyText = Utilities.keyToString(toggleKey);
//        filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText));
//        
//        filters = new ClassMemberFilters( this );
//        filters.getInstance().hookChangeListener(this);
//        filtersPanel.add(filters.getComponent());
//        
//        actions = new Action[] {            
//            new SortByNameAction( filters ),
//            new SortBySourceAction( filters ),
//            null,
//            new FilterSubmenuAction(filters.getInstance())            
//        };
//        
//        add(filtersPanel, BorderLayout.SOUTH);
//        
        manager.setRootContext(getWaitNode());
        
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        elementView.requestFocusInWindow();
        return result;
    }
    
    public org.openide.util.Lookup getLookup() {
        // XXX Check for chenge of FileObject
        return lookup;
    }
    
    static synchronized Node getWaitNode() {
        if ( WAIT_NODE == null ) {
            WAIT_NODE = new WaitNode();
        }
        return WAIT_NODE;
    }
    
    public BeanScanningTask getTask() {        
        return new BeanScanningTask(this);        
    }
    
    
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               elementView.setRootVisible(true);
               manager.setRootContext(getWaitNode());
            } 
        });
    }
    
    public void selectElementNode( ElementHandle<Element> eh ) {
//        PatternNode root = getRootNode();
//        if ( root == null ) {
//            return;
//        }
//        PatternNode node = root.getNodeForElement(eh);
//        try {
//            manager.setSelectedNodes(new Node[]{ node == null ? getRootNode() : node });
//        } catch (PropertyVetoException propertyVetoException) {
//            Exceptions.printStackTrace(propertyVetoException);
//        }
    }

    public void refresh( final ClassPattern pa ) {
        
        final PatternNode rootNode = getRootNode();
        
        if ( rootNode != null && rootNode.getPattern().getPatternAnalyser().getFileObject().equals( pa.getPatternAnalyser().getFileObject() ) ) {
            // update
            //System.out.println("UPDATE ======" + description.fileObject.getName() );
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    rootNode.updateRecursively( pa );
                }
            } );            
        } 
        else {
            //System.out.println("REFRES =====" + description.fileObject.getName() );
            // New fileobject => refresh completely
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    elementView.setRootVisible(false);        
                    manager.setRootContext(new PatternNode( pa, true ) );
                    boolean scrollOnExpand = elementView.getScrollOnExpand();
                    elementView.setScrollOnExpand( false );
                    elementView.expandAll();
                    elementView.setScrollOnExpand( scrollOnExpand );
                }
            } );
            
        }
    }
    
    public void sort() {
//        getRootNode().refreshRecursively();
        throw new UnsupportedOperationException();
    }
    
//    public ClassMemberFilters getFilters() {
//        return filters;
//    }
    
    public void expandNode( Node n ) {
        elementView.expandNode(n);
    }
    
    public Action[] getActions() {
        return actions;
    }
    
    public FileObject getFileObject() {
        return getRootNode().getPattern().getPatternAnalyser().getFileObject();
    }
    
    // FilterChangeListener ----------------------------------------------------
    
    public void filterStateChanged(ChangeEvent e) {
//        ElementNode root = getRootNode();
//        
//        if ( root != null ) {
//            root.refreshRecursively();
//        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    // Private methods ---------------------------------------------------------
    
    private PatternNode getRootNode() {
        
        Node n = manager.getRootContext();
        if ( n instanceof PatternNode ) {
            return (PatternNode)n;
        }
        else {
            return null;
        }
    }
    
    private MyBeanTreeView createBeanTreeView() {
//        ActionMap map = getActionMap();
//        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
//        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
//        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
//        map.put("delete", new DelegatingAction(ActionProvider.COMMAND_DELETE, ExplorerUtils.actionDelete(manager, true)));
//        
        
        MyBeanTreeView btv = new MyBeanTreeView();    // Add the BeanTreeView        
//      btv.setDragSource (true);        
//      btv.setRootVisible(false);        
//      associateLookup( ExplorerUtils.createLookup(manager, map) );        
        return btv;
        
    }
    
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    
    private static class MyBeanTreeView extends BeanTreeView {
        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
}
        
        public void setScrollOnExpand( boolean scroll ) {
            this.tree.setScrollsOnExpand( scroll );
        }
    }
    
    private static class WaitNode extends AbstractNode {
        
        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/beans/resources/wait.gif"); // NOI18N
        
        WaitNode( ) {
            super( Children.LEAF );
        }
        
        @Override
        public Image getIcon(int type) {
             return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return NbBundle.getMessage(BeanPanel.class, "LBL_WaitNode"); // NOI18N
        }
        
    }
}
