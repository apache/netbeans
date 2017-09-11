/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
