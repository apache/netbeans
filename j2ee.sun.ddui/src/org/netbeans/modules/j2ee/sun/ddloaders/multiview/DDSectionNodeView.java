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
