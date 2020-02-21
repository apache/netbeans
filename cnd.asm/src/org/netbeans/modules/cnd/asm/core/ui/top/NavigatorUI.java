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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.asm.core.ui.top;

import java.awt.Image;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.explorer.view.ListView;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.asm.model.AsmState;
import org.netbeans.modules.cnd.asm.model.lang.syntax.FunctionBoundsResolver;
import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;
import org.netbeans.modules.cnd.asm.model.util.DefaultOffsetable;
import org.netbeans.modules.cnd.asm.model.util.IntervalSet;

public class NavigatorUI extends javax.swing.JPanel implements 
                                            ExplorerManager.Provider {
    
    private static final Logger LOGGER = 
            Logger.getLogger(NavigatorUI.class.getName());
    
    private final ListView navigatorPane;
    private final ExplorerManager explorerManager;
    /** Creates new form NavigatorUI */
    public NavigatorUI() {
        initComponents();
        
        explorerManager = new ExplorerManager();        
        navigatorPane = new ListView();        
       
        navigatorPane.setDropTarget(false);
        navigatorPane.setDragSource(false);
        add(navigatorPane, java.awt.BorderLayout.CENTER); 
                      
        setEmpty();
    }
      
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    public void updateCursor(int pos) {
        Node root = explorerManager.getRootContext();
        Node []nodes = root.getChildren().getNodes();
        
        for(Node node : nodes) {
            if (node instanceof AsmFunctionNode) {
                AsmFunctionNode funcNode = (AsmFunctionNode) node;
                if (funcNode.isActive(pos)) {
                    try {
                        explorerManager.setSelectedNodes(new Node[]{node});                        
                    } catch (PropertyVetoException ex) {
                        LOGGER.info("PropertyVetoException exception"); // NOI18N
                    }
                    return;
                }
            }
        }
    }
    
    public void update(DataObject dob, AsmState state) {              
        setFunctions(dob, state);       
    }        
    
    private void setEmpty() {
        Children ch = new Children.Array();      
        AbstractNode node = new AbstractNode(ch);
        explorerManager.setRootContext(node); 
    }
    
    private void setFunctions(DataObject dob, AsmState state) {        
        FunctionBoundsResolver resolver = 
                state.getServices().lookup(FunctionBoundsResolver.class);
        
        if (resolver == null) {
            setEmpty();
            return;
        }  

        IntervalSet<FunctionBoundsResolver.Entry> funcs = resolver.getFunctions();
        String fName = null;
        int startOffset = -1;
        int endOffset = -1;
        List<Node> nodeList = new ArrayList<Node>();
        for (FunctionBoundsResolver.Entry en : funcs) {
            List<AsmElement> comp = state.getElements().getCompounds();
            if (en.getName().equals(fName)) {
                endOffset = comp.get(en.getEndOffset()).getEndOffset();
            } else {
                if (fName != null) {
                    nodeList.add(new AsmFunctionNode(dob, fName, DefaultOffsetable.create(startOffset, endOffset)));
                    fName = null;
                }
                fName = en.getName();
                startOffset = comp.get(en.getStartOffset()).getStartOffset();
                endOffset = comp.get(en.getEndOffset()).getEndOffset();
            }
        }
        if (fName != null) {
            nodeList.add(new AsmFunctionNode(dob, fName, DefaultOffsetable.create(startOffset, endOffset)));
        }
                
        Node []nodes = nodeList.toArray(new Node[nodeList.size()]);
        Children ch = new Children.Array();
        ch.add(nodes);
        AbstractNode node = new AbstractNode(ch);
        explorerManager.setRootContext(node);        
    }
    
    
    
    
    private static class AsmFunctionNode extends AbstractNode {                
        
        private static final String FUNC_ICON = 
                "org/netbeans/modules/cnd/asm/core/resources/function.png";    // NOI18N
        
        private final AsmOffsetable off;     
        private final DataObject dob;
        
        public AsmFunctionNode(DataObject dob, String name, AsmOffsetable off) {
            super(Children.LEAF, null);
            this.dob = dob;
            
            setName(name);
            this.off = off;
        }
        
        public boolean isActive(int pos) {
            return pos >= off.getStartOffset() &&
                   pos < off.getEndOffset();
        }
        
        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(FUNC_ICON);
        }
        
        @Override
        public Action getPreferredAction() { 
            return new GoToFunctionAction();
        }
        
        class GoToFunctionAction extends AbstractAction {
            
            public GoToFunctionAction() {            
                 putValue(Action.NAME, NbBundle.getMessage(GoToFunctionAction.class, "LBL_GoToFunctionAction")); //NOI18N
            }   

            public void actionPerformed(ActionEvent e) {
                AsmObjectUtilities.goToSource(dob, off.getStartOffset());
            }
        } 
        
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
          
}
