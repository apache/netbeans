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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
            scene.getPageFlowView().setActivatedNodes(selected.toArray(new Node[selected.size()]));
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
