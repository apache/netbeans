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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.util.LinkedList;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;


/**
 * @author Peter Williams
 */
public class DDSectionNodeView extends SectionNodeView {
    
    protected RootInterface rootDD;
    protected ASDDVersion version;
    
    public DDSectionNodeView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        rootDD = dataObject.getDDRoot();
        version = dataObject.getASDDVersion();
    }
    
    /** API to set the child nodes (subpanels) of this view node without creating
     *  an extra top level root node.
     */
    public void setChildren(SectionNode [] children) {
        int size = children.length;
        if(size > 0) {
            setRootNode(children[0]);
            
            if(--size > 0) {
                SectionNode [] remainingNodes = new SectionNode[size];
                System.arraycopy(children, 1, remainingNodes, 0, size);
                
                Node rootNode = getRoot();
                rootNode.getChildren().add(remainingNodes);
                for(int i = 0; i < size; i++) {
                    addSection(remainingNodes[i].getSectionNodePanel());
                }
            }
        }
    }
    
    public void setChildren(LinkedList<SectionNode> children) {
        if(children.peek() != null) {
            SectionNode firstNode = children.removeFirst();
            setRootNode(firstNode);

            if(children.peek() != null) {
                SectionNode [] remainingNodes = children.toArray(new SectionNode[0]);
                
                Node rootNode = getRoot();
                rootNode.getChildren().add(remainingNodes);
                for(int i = 0; i < remainingNodes.length; i++) {
                    addSection(remainingNodes[i].getSectionNodePanel());
                }
            }
        }
    }
    
    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return ((SunDescriptorDataObject) getDataObject()).getModelSynchronizer();
    }
    
    // ------------------------------------------------------------------------
    // Overrides required to properly support multiple rootNodes
    //   Taken from SectionNodeView and enhanced for to handle rootNode[]
    // ------------------------------------------------------------------------
    private final RequestProcessor.Task ddRefreshTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            refreshView();
        }
    });

    private static final int DD_REFRESH_DELAY = 20;
    
    @Override
    public void refreshView() {
        Node [] rootNodes = getRoot().getChildren().getNodes();
        if(rootNodes != null) {
            for(Node n: rootNodes) {
                if(n instanceof SectionNode) {
                    ((SectionNode) n).refreshSubtree();
                }
            }
        }
    }
    
    @Override
    public void scheduleRefreshView() {
        ddRefreshTask.schedule(DD_REFRESH_DELAY);
    }
    
    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
//        System.out.println("DDSectionNodeView [" + this.getClass().getSimpleName() + "] .dataModelPropertyChange: " + 
//                source + ", " + propertyName + ", " + oldValue + ", " + newValue);
        Node [] rootNodes = getRoot().getChildren().getNodes();
        if(rootNodes != null) {
            for(Node n: rootNodes) {
                if(n instanceof SectionNode) {
                    ((SectionNode) n).dataModelPropertyChange(source, propertyName, oldValue, newValue);
                }
            }
        }
    }

//    /** Override this if required by derived classes.  Called before refreshView()
//     *  to ensure child nodes are up to date.
//     */
//    protected void checkChildren() {
//        // As long as NamedGroups have setExpanded = true, this is required to
//        // ensure initialization of the child nodes in the group.
//        final Children children = getRoot().getChildren();
//        final Node[] nodes = children.getNodes();
//        for(Node node: nodes) {
//            if(node instanceof NamedBeanGroupNode) {
//                System.out.println(node.getClass().getSimpleName() + ".checkChildren() called by " + this.getClass().getSimpleName() + ".checkChildren()");
//                ((NamedBeanGroupNode) node).checkChildren(null);
//            }
//        }
//    }
    
}
