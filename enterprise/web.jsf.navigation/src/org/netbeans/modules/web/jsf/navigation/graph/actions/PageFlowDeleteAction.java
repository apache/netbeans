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

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class PageFlowDeleteAction extends AbstractAction{
    private final PageFlowScene scene;
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowDeleteAction");
    //    private final static Logger LOG = Logger.getLogger("org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowDeleteAction");
    //    static {
    //        LOG.setLevel(Level.FINEST);
    //    }
    
    
    /** Creates a new instance of PageFlowDeleteAction
     * @param scene
     */
    public PageFlowDeleteAction(PageFlowScene scene) {
        super();
        this.scene = scene;
        putValue("ACCELERATOR_KEY", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }
    
    /* These are edges that do not exist in the local facesConfig. */
    private final Collection<NavigationCaseEdge> thoseEdges = new ArrayList<NavigationCaseEdge>();
    
    /* These are edges that exist in the local facesConfig. */
    private final Collection<NavigationCaseEdge> theseEdges = new ArrayList<NavigationCaseEdge>();
    
    @Override
    public boolean isEnabled() {
        //Workaround: Temporarily Wrapping Collection because of Issue: 100127
        Set<? extends Object> selectedObjs = scene.getSelectedObjects();
        if (selectedObjs.isEmpty() ){
            return false;
        }
        
        for( Object selectedObj : selectedObjs ){
            /* HACK until PinNode is made a Node */
            if(!( selectedObj instanceof PageFlowSceneElement )  ){
                return false;
            }
            PageFlowSceneElement element = (PageFlowSceneElement)selectedObj;
            /* Can usually assume the case is in the config file unless we are dealing with the SCOPE_ALL_FACESCONFIG. */
            if( !element.isModifiable()) {
                return false;
            }
            
            if( scene.getPageFlowView().getPageFlowController().isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG) &&
                    element instanceof Page ){
                /* These are edges in the local faces config */
                thoseEdges.clear();
                theseEdges.clear();
                
                Collection<NavigationCaseEdge> allEdges = new ArrayList<NavigationCaseEdge>();
                Collection<Pin> pins = scene.getNodePins((Page)element);
                for( Pin pin : pins ){
                    allEdges.addAll(scene.findPinEdges(pin, true, true));
                }
                for ( NavigationCaseEdge edge : allEdges ){
                    if ( edge.isModifiable() ) {
                        theseEdges.add(edge);
                    } else {
                        thoseEdges.add(edge);
                    }
                }
                if( theseEdges.isEmpty() ){
                    return false;
                }
            }
        }
        
        return super.isEnabled();
    }
    
    public void actionPerformed(ActionEvent event) {
        
        Queue<PageFlowSceneElement> deleteNodesList = new LinkedList<PageFlowSceneElement>();
        //Workaround: Temporarily Wrapping Collection because of Issue: 100127
        Set<Object> selectedObjects = new HashSet<Object>(scene.getSelectedObjects());
        LOG.fine("Selected Objects: " + selectedObjects);
        LOG.finest("Scene: \n" +
                "Nodes: " + scene.getNodes() + "\n" +
                "Edges: " + scene.getEdges()+ "\n" +
                "Pins: " + scene.getPins());
        
        /*When deleteing only one item. */
        if (selectedObjects.size() == 1){
            Object myObj = selectedObjects.toArray()[0];
            if( myObj instanceof PageFlowSceneElement ) {
                deleteNodesList.add((PageFlowSceneElement)myObj);
                deleteNodes(deleteNodesList);
                return;
            }
        }
        
        Set<NavigationCaseEdge> selectedEdges = new HashSet<NavigationCaseEdge>();
        Set<PageFlowSceneElement> selectedNonEdges = new HashSet<PageFlowSceneElement>();
        
        /* When deleting multiple objects, make sure delete all the links first. */
        Set<Object> nonEdgeSelectedObjects = new HashSet<Object>();
        for( Object selectedObj : selectedObjects ){
            if( selectedObj instanceof PageFlowSceneElement ){
                if( scene.isEdge(selectedObj) ){
                    assert !scene.isPin(selectedObj);
                    selectedEdges.add((NavigationCaseEdge)selectedObj);
                } else {
                    assert scene.isNode(selectedObj) || scene.isPin(selectedObj);
                    selectedNonEdges.add((PageFlowSceneElement)selectedObj);
                }
            }
        }
        
        /* I can not call deleteNodes on them separate because I need to guarentee that the edges are always deleted before anything else. */
        deleteNodesList.addAll(selectedEdges);
        deleteNodesList.addAll(selectedNonEdges);
        
        //        for( Object selectedObj : nonEdgeSelectedObjects ){
        //            deleteNodesList.add((PageFlowSceneElement)selectedObj);
        //        }
        deleteNodes(deleteNodesList);
        
    }
    
    //        public Queue<Node> myDeleteNodes;
    private void deleteNodes( Queue<PageFlowSceneElement> deleteNodes ){
        final Queue<PageFlowSceneElement> myDeleteNodes = deleteNodes;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //This should walk through in order.
                    for( PageFlowSceneElement deleteNode : myDeleteNodes ){
                        if( deleteNode.canDestroy() ){
                            
                            if( deleteNode instanceof NavigationCaseEdge ){
                                updateSourcePins((NavigationCaseEdge)deleteNode);
                            }
                            
                            if( scene.getPageFlowView().getPageFlowController().isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG)){
                                if( thoseEdges.isEmpty() ) {
                                    deleteNode.destroy();
                                } else {
                                    for( NavigationCaseEdge edge : theseEdges ){
                                        if ( scene.findWidget(edge) != null ){                                            
                                            updateSourcePins(edge);
                                            edge.destroy();
                                        }
                                    }
                                }
                                thoseEdges.clear();
                            } else {
                                deleteNode.destroy();
                            }
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    private void updateSourcePins(NavigationCaseEdge navCaseNode) {
        Pin source = scene.getEdgeSource(navCaseNode);
        if( source != null && !source.isDefault()) {
            source.setFromOutcome(null);
        }
        return;
    }
    
    
}
