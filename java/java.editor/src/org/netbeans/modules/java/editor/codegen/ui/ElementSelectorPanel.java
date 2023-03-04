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

package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk, Dusan Balek
 */
public class ElementSelectorPanel extends JPanel implements ExplorerManager.Provider {
    
    private ExplorerManager manager = new ExplorerManager();
    private CheckTreeView elementView;
    
    @NbBundle.Messages({
        "BTN_SelectAll=Select &All",
        "BTN_SelectNone=Select &None",
    })
    /** Creates new form ElementSelectorPanel */
    public ElementSelectorPanel(ElementNode.Description elementDescription, boolean singleSelection ) {        
        this(elementDescription, singleSelection, false);
    }
    
    public ElementSelectorPanel(ElementNode.Description elementDescription, boolean singleSelection, boolean enableButtons ) {      
        setLayout(new BorderLayout());
        elementView = new CheckTreeView();
        elementView.setRootVisible(false);
        add(elementView, BorderLayout.CENTER);
        setRootElement(elementDescription, singleSelection);
        //make sure that the first element is pre-selected
        Node root = manager.getRootContext();
        Node[] children = root.getChildren().getNodes();
        if( null != children && children.length > 0 && !elementDescription.hasSelection(true)) {
            try {
                manager.setSelectedNodes(new org.openide.nodes.Node[]{children[0]});
            } catch (PropertyVetoException ex) {
                //ignore
            }
        }
        if (!singleSelection && hasMultipleSelectables(elementDescription)) {
            final JButton selectAll = new JButton();
            final JButton selectNone = new JButton();
            Mnemonics.setLocalizedText(selectAll, Bundle.BTN_SelectAll());
            Mnemonics.setLocalizedText(selectNone, Bundle.BTN_SelectNone());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.add(selectAll);
            buttonPanel.add(selectNone);

            add(buttonPanel, BorderLayout.SOUTH);
            
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectAllNodes(e.getSource() == selectAll);
                }
            };
            selectAll.addActionListener(al);
            selectNone.addActionListener(al);
        }
    }
    
    private boolean hasMultipleSelectables(ElementNode.Description elementDescription) {
        Deque<ElementNode.Description> toProcess = new ArrayDeque<>();
        if (elementDescription != null) {
            toProcess.add(elementDescription);
        }
        boolean selectableFound = false;
        while (!toProcess.isEmpty()) {
            ElementNode.Description d = toProcess.poll();
            List<ElementNode.Description> subs = d.getSubs();
            if (subs == null) {
                if (d.isSelectable()) {
                    if (selectableFound) {
                        return true;
                    }
                    selectableFound = true;
                }
            } else {            
                toProcess.addAll(subs);
            }
        }
        return false;
    }
    
    private void selectAllNodes(boolean select) {
        boolean oldScroll = elementView.getScrollsOnExpand();
        elementView.setScrollsOnExpand( false );
        Node root = getExplorerManager().getRootContext();
        Deque<Node> toProcess = new ArrayDeque<>(Arrays.asList(root.getChildren().getNodes(true)));
        while (!toProcess.isEmpty()) {
            Node n = toProcess.poll();
            ElementNode.Description desc = getDescription(n);
            if (desc == null) {
                continue;
            }
            if (desc.hasSelectableSubs()) {
                elementView.expandNode(n);
                toProcess.addAll(Arrays.asList(n.getChildren().getNodes(true)));
            } else if (desc.isSelectable()) {
                desc.setSelected(select);
            }
        }
        elementView.setScrollsOnExpand( oldScroll );
    }
    
    public List<ElementHandle<? extends Element>> getTreeSelectedElements() {
        ArrayList<ElementHandle<? extends Element>> handles = new ArrayList<ElementHandle<? extends Element>>();
                
        for (Node node : manager.getSelectedNodes()) {
            if (node instanceof ElementNode) {
                ElementNode.Description description = node.getLookup().lookup(ElementNode.Description.class);
                handles.add(description.getElementHandle());
            }
        }
     
        return handles;
    }    
        
    public List<ElementHandle<? extends Element>> getSelectedElements() {
        ArrayList<ElementHandle<? extends Element>> handles = new ArrayList<ElementHandle<? extends Element>>();
            
        Node n = manager.getRootContext();        
        ElementNode.Description description = n.getLookup().lookup(ElementNode.Description.class);
        getSelectedHandles( description, handles );
       
        return handles;
    }
    
    public void setRootElement(ElementNode.Description elementDescription, boolean singleSelection) {  
        
        Node n;
        if ( elementDescription != null ) {
            ElementNode en = new ElementNode(elementDescription);
            en.setSingleSelection(singleSelection);
            n = en;        
        }
        else {
            n = Node.EMPTY;
        }
        manager.setRootContext(n);
        
    }
    
    private static ElementNode.Description getDescription(Node n) {
        return  n.getLookup().lookup(ElementNode.Description.class);
    }
    
    public void doInitialExpansion( int howMuch ) {
        
        Node root = getExplorerManager().getRootContext();
        Node[] subNodes = root.getChildren().getNodes(true);
        
        if ( subNodes == null ) {
            return;
        }
        Node toSelect = null;
        
        int row = 0;

        boolean oldScroll = elementView.getScrollsOnExpand();
        elementView.setScrollsOnExpand( false );
        
        for( int i = 0; subNodes != null && i < (howMuch == - 1 || howMuch > subNodes.length ? subNodes.length : howMuch ) ; i++ ) {                    
             elementView.expandNode(subNodes[i]);
            Node[] ssn = subNodes[i].getChildren().getNodes( true );
            row += ssn.length;
            if ( toSelect == null ) {                
                if ( ssn.length > 0 ) {
                    toSelect = ssn[0];
                }                    
            }
        }
        Node toSelect2 = null;
        // extend the expansion to all initially selected nodes
        Deque<Node> toProcess = new ArrayDeque<>(Arrays.asList(subNodes));
        while (!toProcess.isEmpty()) {
            Node n = toProcess.poll();
            ElementNode.Description desc = getDescription(n);
            if (desc == null) {
                continue;
            }
            if (desc.isSelected() && toSelect2 == null) {
                // respect initial selection, if some is done
                toSelect2 = n;
            }
            if (desc.hasSelection(false)) {
                elementView.expandNode(n);
                toProcess.addAll(Arrays.asList(n.getChildren().getNodes(true)));
            }
        }
        if (toSelect2 != null) {
            toSelect = toSelect2;
        }
        
        elementView.setScrollsOnExpand( oldScroll );
        
        try  {
            if (toSelect != null ) {
                getExplorerManager().setSelectedNodes(new org.openide.nodes.Node[]{toSelect});
            }
        }
        catch (PropertyVetoException ex) {
            // Ignore
        }
    }
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void getSelectedHandles( ElementNode.Description description,
                                     ArrayList<ElementHandle<? extends Element>> target) {

        //#143049
        if (description == null)
            return;
        
        List<ElementNode.Description> subs = description.getSubs();
        
        if ( subs == null ) {
            return;
        }
        
        for( ElementNode.Description d : subs ) {
            if ( d.isSelectable() && d.isSelected() ) {
                target.add(d.getElementHandle() );
            }
            else {
                getSelectedHandles( d, target );
            }
        }
    }
       
}
