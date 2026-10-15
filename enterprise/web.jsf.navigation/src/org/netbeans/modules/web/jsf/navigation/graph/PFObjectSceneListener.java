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
package org.netbeans.modules.web.jsf.navigation.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.openide.nodes.Node;


/**
 *
 * @author joelle
 */
public class PFObjectSceneListener implements ObjectSceneListener {

    private static final UnsupportedOperationException uoe = new UnsupportedOperationException("Not supported yet.");


    public PFObjectSceneListener() {
    }

    public void objectAdded(ObjectSceneEvent event, Object addedObject) {
        throw uoe;
    }

    public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
        throw uoe;
    }

    public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState prevState, ObjectState newState) {
        throw uoe;
    }

    public void selectionChanged(ObjectSceneEvent event, Set<Object> prevSelection, Set<Object> newSelection) {
        PageFlowScene scene = (PageFlowScene) event.getObjectScene();
        Set<NavigationCaseEdge> releventEdges = new HashSet<NavigationCaseEdge>();
        Set<Node> selected = new HashSet<Node>();
        for (Object obj : newSelection) {
            if (obj instanceof PageFlowSceneElement) {
                PageFlowSceneElement element = (PageFlowSceneElement) obj;
                selected.add(element.getNode());
                releventEdges.addAll(getRelevantEdges(element, scene));
            }
        }
        scene.setHighlightedObjects(releventEdges);
        if (selected.isEmpty()) {
            scene.getPageFlowView().setDefaultActivatedNode();
        } else {
            scene.getPageFlowView().setActivatedNodes(selected.toArray(new Node[0]));
        }
    }

    public void highlightingChanged(ObjectSceneEvent event, Set<Object> prevHighlighting, Set<Object> newHighlighting) {
        throw uoe;
    }

    public void hoverChanged(ObjectSceneEvent event, Object prevHoveredObject, Object newHoveredObject) {
        throw uoe;
    }

    public void focusChanged(ObjectSceneEvent event, Object prevFocusedObject, Object newFocusedObject) {
        throw uoe;
    }

    private Set<NavigationCaseEdge> getRelevantEdges(PageFlowSceneElement element, PageFlowScene scene) {

        Set<NavigationCaseEdge> edgeSet = new HashSet<NavigationCaseEdge>();
        if (element instanceof Page) {
            Page page = (Page) element;
            Collection<Pin> pins = scene.getNodePins(page);
            for (Pin pin : pins) {
                Collection<NavigationCaseEdge> edges = scene.findPinEdges(pin, true, false);
                edgeSet.addAll(edges);
            }
        } else if ( element instanceof Pin){
            Collection<NavigationCaseEdge> edges = scene.findPinEdges((Pin)element, true, false);
            edgeSet.addAll(edges);
        }
        return edgeSet;
    }
}
