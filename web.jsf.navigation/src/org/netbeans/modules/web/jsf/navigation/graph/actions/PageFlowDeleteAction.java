/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
