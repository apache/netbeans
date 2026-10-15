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
/*
 * LinkCreateProvider.java
 *
 * Created on January 29, 2007, 12:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.lang.ref.WeakReference;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.navigation.PageFlowController;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;

/**
 *
 * @author joelle
 */
public class LinkCreateProvider implements ConnectProvider {

    private WeakReference<PageFlowScene> refGraphScene;
    Page source = null;
    Page target = null;
    Pin pinNode = null;

    /**
     * Creates a new instance of LinkCreateProvider
     * @param graphScene
     *
     */
    public LinkCreateProvider(PageFlowScene graphScene) {
        setGraphScene(graphScene);
    }

    public PageFlowScene getGraphScene() {
        PageFlowScene scene = null;
        if (refGraphScene != null) {
            scene = refGraphScene.get();
        }
        return scene;
    }
    
    public void setGraphScene( PageFlowScene scene ) {
        refGraphScene = new WeakReference<PageFlowScene>(scene);
    }

    public boolean isSourceWidget(Widget sourceWidget) {
        PageFlowScene scene = getGraphScene();
        Object object = scene.findObject(sourceWidget);
        source = null;
        pinNode = null;
        if (scene.isPin(object)) {
            pinNode = (Pin) object;
            source = pinNode.getPage();
        } else if (scene.isNode(object)) {
            source = (Page) object;
        }

        return source != null;
    }

    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        target = null;
        PageFlowScene scene = getGraphScene();
        Object object = scene.findObject(targetWidget);
        target = scene.isNode(object) ? (Page) object : null;
        if (target != null) {
            return ConnectorState.ACCEPT;
        }
        return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
        //
        //        if (targetWidget instanceof VMDNodeWidget ) {
        //            return ConnectorState.ACCEPT;
        //        }
        //
        //        // Only allow it to be attached to the default pin.
        // //        if (graphScene.isPin(targetWidget) &&
        // //                ((Pin)graphScene.findObject(targetWidget)).getNavComp() == null ) {
        // //            return ConnectorState.ACCEPT;
        // //        }
        //        return ConnectorState.REJECT_AND_STOP;
    }

    public boolean hasCustomTargetWidgetResolver(Scene scene) {
        return false;
    }

    public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
        return null;
    }

    public void createConnection(Widget sourceWidget, Widget targetWidget) {
        PageFlowScene scene = getGraphScene();
        PageFlowController pfc = scene.getPageFlowView().getPageFlowController();
        if (pfc != null && sourceWidget != null && targetWidget != null) {

            NavigationCase caseNode = pfc.createLink(source, target, pinNode);
//            assert caseNode != null;
//            assert caseNode.getToViewId() != null;
//            assert caseNode.getFromOutcome() != null;
            scene.validate();
        }
        //            addEdge (edge);
        //            setEdgeSource (edge, source);
        //            setEdgeTarget (edge, target);
    }
}
