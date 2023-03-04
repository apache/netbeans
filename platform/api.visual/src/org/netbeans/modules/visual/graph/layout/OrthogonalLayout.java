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
package org.netbeans.modules.visual.graph.layout;

import org.netbeans.api.visual.graph.layout.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.EmbeddedPlanarGraph;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.GTPlanarizer;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MinimumBendOrthogonalizer;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.OrthogonalRepresentation;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.RectangularCompactor;

/**
 * Takes the generic nodes and edges from a GraphScene and lays them out orthogonally.
 * Note that the OrthogonalRouter is used to route the edges instead of adding
 * extra control points during the orthogonalizing.
 */
public class OrthogonalLayout<N, E> extends GraphLayout<N, E> {

    private MGraph<N, E> mGraph = null;
    private GraphScene<N, E> scene = null;
    private final boolean animate;

    public static final int gutter = 55;
    public final int halfGutter = gutter/2 ;
    
    /**
     * Create an instance of an OrthogonalLayout. Note that this layout does not
     * work with the normal Scene class, but rather requires a GraphScene. This 
     * orthogonal layout uses the OrthogonalRouter to route the edges once it has 
     * completed laying out the nodes.
     * @param scene the scene containing the nodes and edges.
     */
    public OrthogonalLayout(GraphScene<N, E> scene, boolean animate) {
        this.scene = scene;
        this.animate = animate;
    }

    /**
     * Called from UniversalGraph.layoutGraph
     * @param graph the UniversalGraph created in UniversalGraph.layoutGraph
     */
    @Override
    protected void performGraphLayout(UniversalGraph<N, E> graph) {

        mGraph = MGraph.createGraph(graph, scene);

        GTPlanarizer<N, E> planarizer = new GTPlanarizer<>();
        Collection<EmbeddedPlanarGraph<N, E>> epgs = planarizer.planarize(mGraph);

        MinimumBendOrthogonalizer orthogonalizer = new MinimumBendOrthogonalizer();
        Collection<OrthogonalRepresentation<N, E>> ors = orthogonalizer.orthogonalize(epgs);

        RectangularCompactor<N, E> compactor = new RectangularCompactor<>();
        compactor.compact(ors);

        layoutNodes();
    }

    /**
     * 
     */
    private void layoutNodes() {

        //TODO: do we want to use this router or use the created dummy nodes as
        //control points?
        Collection<E> edges = scene.getEdges();
        for (E e : edges) {
            ConnectionWidget conn = (ConnectionWidget) scene.findWidget(e);
            conn.setRouter(RouterFactory.createOrthogonalSearchRouter());
        }

        List<Widget> singletons = new ArrayList<>() ;
        
        Collection<Vertex<N>> vertices = mGraph.getVertices();
        int maxX = -1 ;
        int maxY = -1 ;
        
        for (Vertex<N> v : vertices) {

            N node = v.getNodeDesignElement();
            if (node == null) {//if the vertex is a dummy, there is no 
                continue;      //node associated with it.
            }

            Widget w = scene.findWidget(node);

            int x = (int) v.getX() ;
            int y = (int) v.getY() ;
            
            Point p = new Point(x, y);
            
            if (p.x == 0 && p.y == 0) {
                singletons.add (w) ;
                continue;
            }
            
            if (animate) {
                scene.getSceneAnimator().animatePreferredLocation(w, p);
            }
            
            Rectangle bounds = w.getBounds() ;
            w.resolveBounds(p, bounds);

            if (x+bounds.width > maxX) maxX = x+bounds.width ;
            if (y+bounds.height > maxY) maxY = y+bounds.height ;
            
        }
        
        if (singletons == null || singletons.size() == 0) return ;
        
        Rectangle sceneBounds = scene.getBounds() ;
        
        if (sceneBounds.width > maxX) maxX = sceneBounds.width ;
        
        //layout singleton nodes in rows accross the bottom.
        int x = 0 ;
        int y = maxY + halfGutter ;
        
        int maxHeight = -1 ;
        
        Iterator<Widget> singletonIterator = singletons.iterator() ;
        while (singletonIterator.hasNext()) {
            Widget w = singletonIterator.next() ;
            Rectangle bounds = w.getBounds() ;
            
            if (x+bounds.width > maxX) {
                x = 0 ;
                y = y + maxHeight + halfGutter ;
                maxHeight = -1;
            }
            
            Point p = new Point(x, y);
            
            if (animate) {
                scene.getSceneAnimator().animatePreferredLocation(w, p);
            }
            
            w.resolveBounds(p, bounds);
            
            x = x + bounds.width + halfGutter ;
            if (bounds.height > maxHeight) maxHeight = bounds.height ;
            
        }

        
    }

    @Override
    protected void performNodesLayout(UniversalGraph<N, E> graph, Collection<N> nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
