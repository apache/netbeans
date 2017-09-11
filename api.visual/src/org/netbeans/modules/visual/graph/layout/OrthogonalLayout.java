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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visual.graph.layout;

import org.netbeans.api.visual.graph.layout.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
public class OrthogonalLayout<N, E> extends GraphLayout {

    private MGraph mGraph = null;
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
    protected void performGraphLayout(UniversalGraph graph) {

        mGraph = MGraph.createGraph(graph, scene);

        GTPlanarizer planarizer = new GTPlanarizer();
        Collection<EmbeddedPlanarGraph> epgs = planarizer.planarize(mGraph);

        MinimumBendOrthogonalizer orthogonalizer = new MinimumBendOrthogonalizer();
        Collection<OrthogonalRepresentation> ors = orthogonalizer.orthogonalize(epgs);

        RectangularCompactor compactor = new RectangularCompactor();
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

        ArrayList <Widget> singletons = new ArrayList<Widget>() ;
        
        Collection<Vertex> vertices = mGraph.getVertices();
        int maxX = -1 ;
        int maxY = -1 ;
        
        for (Vertex v : vertices) {

            N node = (N) v.getNodeDesignElement();
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

    /**
     * 
     * @param graph
     * @param nodes
     */
    @Override
    protected void performNodesLayout(UniversalGraph graph, Collection nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
