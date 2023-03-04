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
package org.netbeans.modules.visual.bugs;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.GraphLayoutListener;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.framework.VisualTestCase;

/**
 *
 * @author Ernest Lotter
 */
public class GraphLayoutListenerRemoval197502Test extends VisualTestCase {

    public GraphLayoutListenerRemoval197502Test(String testName) {
        super(testName);
    }

    /**
     * Bug #197502 caused GraphLayout.remove() to not really remove listener,
     * but in fact add it again.
     *
     * This test will involve counting the number of times that the graph layout
     * listener is invoked, and failing when the count does not match the expected
     * number.
     */
    public void testGraphLayoutListenerRemoval() {
        // the number of times that the listener was invoked (graphLayoutStarted invocations)
        final AtomicInteger numStarts = new AtomicInteger(0);
        GraphLayoutListener<String,String> listener = new GraphLayoutListener<String, String>() {
            @Override
            public void graphLayoutStarted(UniversalGraph<String, String> graph) {
                numStarts.incrementAndGet();
            }

            @Override
            public void graphLayoutFinished(UniversalGraph<String, String> graph) {
            }

            @Override
            public void nodeLocationChanged(UniversalGraph<String, String> graph, String node, Point previousPreferredLocation, Point newPreferredLocation) {
            }
        };
       
        // make a basic scene
        StringGraphScene sgs = new StringGraphScene();
        GridGraphLayout<String, String> graphLayout = new GridGraphLayout<String, String>();
        SceneLayout sceneGraphLayout = LayoutFactory.createSceneGraphLayout(sgs, graphLayout);
        sceneGraphLayout.invokeLayout();
       
        // add some nodes
        sgs.addNode("Node1");
        sgs.addNode("Node2");
       
        // (a) as listener not added yet, numStarts should be zero after first run
        graphLayout.layoutGraph(sgs);
        assertEquals(0, numStarts.get());
       
        // add the listener
        graphLayout.addGraphLayoutListener(listener);
       
        // (b) listener added, we expect numStarts == 1 after single layout invocation
        graphLayout.layoutGraph(sgs);
        assertEquals(1, numStarts.get());
       
        // remove the listener (#197502 would cause this to not really remove)
        graphLayout.removeGraphLayoutListener(listener);
       
        // (c) listener removed, we expect numStarts == 1 (still) after single layout invocation
        graphLayout.layoutGraph(sgs);
        assertEquals("GraphLayoutListener erroneously invoked after being removed from GraphLayout", 1, numStarts.get());
    }

    /**
     * A simple string graph scene with which to perform the test
     */
    private class StringGraphScene extends GraphScene.StringGraph {
        private LayerWidget mainLayer;
        private LayerWidget connectionLayer;

        public StringGraphScene() {
            mainLayer = new LayerWidget(this);
            connectionLayer = new LayerWidget(this);
            addChild(mainLayer);
            addChild(connectionLayer);
        }

        protected Widget attachNodeWidget(String node) {
            Widget widget = new LabelWidget(this, node);
            mainLayer.addChild (widget);
            return widget;
        }

        protected Widget attachEdgeWidget(String edge) {
            ConnectionWidget connectionWidget = new ConnectionWidget(this);
            connectionLayer.addChild(connectionWidget);
            return connectionWidget;
        }

        protected void attachEdgeSourceAnchor(String edge, String oldSourceNode, String sourceNode) {
            ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(sourceNode)));
        }

        protected void attachEdgeTargetAnchor(String edge, String oldTargetNode, String targetNode) {
            ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(findWidget(targetNode)));
        }

        public LayerWidget getMainLayer() {
            return mainLayer;
        }

        public LayerWidget getConnectionLayer() {
            return connectionLayer;
        }
    }
}
