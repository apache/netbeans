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

package org.netbeans.modules.java.graph;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JScrollPane;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;


/**
 * Layout instance implementing the FruchtermanReingold algorithm 
 * http://mtc.epfl.ch/~beyer/CCVisu/manual/main005.html
 * 
 * Inspired by implementations at JUNG and Prefuse.
 */
class FruchtermanReingoldLayout<I extends GraphNodeImplementation> extends SceneLayout {

    private double forceConstant;
    private double temp;
    private int iterations = 700;
    private final int magicSizeMultiplier = 10;
    private final int magicSizeConstant = 200;
    private Rectangle bounds;
    protected int m_fidx;
    
    private static final double MIN = 0.000001D;
    private static final double ALPHA = 0.1;
    private DependencyGraphScene<I> scene;
    private JScrollPane panel;
    
    FruchtermanReingoldLayout(DependencyGraphScene<I> scene, JScrollPane panel) {
        super(scene);
        iterations = 700;
        this.scene = scene;
        init();
        this.panel = panel;

    }
    
    public @Override void performLayout() {
        performLayout(true);
//        scene.validate();
//        Rectangle rectangle = new Rectangle (0, 0, 1, 1);
//        for (Widget widget : scene.getChildren()) {
//            Rectangle childBounds = widget.getBounds();
//            if (childBounds == null) {
//                continue;
//            }
//            rectangle = rectangle.union(widget.convertLocalToScene(childBounds));
//        }
//        Dimension dim = rectangle.getSize ();
//        Dimension viewDim = panel.getViewportBorderBounds ().getSize ();
//        double zoom = Math.min ((float) viewDim.width / dim.width, (float) viewDim.height / dim.height);
//        scene.setZoomFactor (Math.min(zoom, 1));
//        scene.validate();
    }
    
    private void performLayout(boolean finish) {
        for (int i=0; i < iterations; i++ ) {
            int repeats = 0;
            while (true) {
                
                for (GraphNode n : scene.getNodes()) {
                    NodeWidget w = getWidget(n);
                    if (w.isFixed()) {
                        continue;
                    }
    //                if (i < iterations / 5) {
    //                    if (scene.findNodeEdges(n, false, true).size() == 1 
    //                       && scene.findNodeEdges(n, true, false).size() == 0) {
    //                        //for leaves, ignore repulsion first, to cause closer location to parent...
    //                        System.out.println("continue.." + n.getArtifact().getId());
    //                        continue;
    //                    }
    //                }
                    calcRepulsion(w);
                }
                for (GraphEdge e : scene.getEdges()) {
                    calcAttraction(e);
                }
                for (GraphNode n : scene.getNodes()) {
                    NodeWidget w = getWidget(n);
                    if (w.isFixed()) {
                        continue;
                    }
                    calcPositions(w);
                }
                if (areAllFixed() || repeats > 2) {
                    doRelayoutNonFixed();
                    resetFixed();
                    cool(i);
                    break;
                }
                repeats = repeats + 1;
            }
        }
        if (finish) {
            finish();
        }
    }
    
    public void rePerformLayout(int iters) {
        int nds = scene.getNodes().size();
        iterations = iters;
        bounds = scene.getBounds();
        if (bounds == null) {
            return;
        }
//        System.out.println("scene bounds are =" + bounds);
        temp = bounds.getWidth() / 1000;
//        forceConstant = 0.75 * Math.sqrt(bounds.getHeight() * bounds.getWidth() / nds);
        forceConstant = 0.25 * Math.sqrt(bounds.getHeight() * bounds.getWidth() / nds);
//        System.out.println("force constant2=" + forceConstant);
        performLayout(false);
    }
    
    
    
    
    private void init() {
        int nds = scene.getNodes().size();
        bounds = new Rectangle(magicSizeConstant  + (magicSizeMultiplier * nds), 
                               magicSizeConstant  + (magicSizeMultiplier * nds)); //g.getMaximumBounds();
        temp = bounds.getWidth() / 10;
        forceConstant = 0.75 * Math.sqrt(bounds.getHeight() * bounds.getWidth() / nds);
        
        GraphNode<I> rn = scene.getRootGraphNode();
        NodeWidget rw = getWidget(rn);
        rw.locX = bounds.getCenterX();
        rw.locY = bounds.getCenterY();
        rw.setFixed(true);
        layoutCirculary(scene.getNodes(), rn);
    }
    
    private void finish() {
        for (GraphNode n : scene.getNodes()) {
            NodeWidget w = getWidget(n);
            Widget wid = scene.findWidget(n);
            Point point = new Point();
            point.setLocation(w.locX, w.locY);
            if (scene.isAnimated()) {
                scene.getSceneAnimator().animatePreferredLocation(wid, point);
            } else {
                wid.setPreferredLocation(point);
            }
        }
    }
    
    private void calcPositions(NodeWidget w) {
        double deltaLength = Math.max(MIN,
                Math.sqrt(w.dispX * w.dispX + w.dispY * w.dispY));
        
        double xDisp = w.dispX/deltaLength * Math.min(deltaLength, temp);

        double yDisp = w.dispY/deltaLength * Math.min(deltaLength, temp);
        
        w.locX += xDisp;
        w.locY += yDisp;
        if (isThereFreeSpaceNonFixedSpace(w)) {
            w.setFixed(true);
        }
//        double x = n.locX;
//        double y = n.locY;
//        // don't let nodes leave the display
//        double borderWidth = bounds.getWidth() / 50.0;
//        if (x < bounds.getMinX() + borderWidth) {
//            x = bounds.getMinX() + borderWidth + Math.random() * borderWidth * 2.0;
//        } else if (x > (bounds.getMaxX() - borderWidth)) {
//            x = bounds.getMaxX() - borderWidth - Math.random() * borderWidth * 2.0;
//        }
//
//        if (y < bounds.getMinY() + borderWidth) {
//            y = bounds.getMinY() + borderWidth + Math.random() * borderWidth * 2.0;
//        } else if (y > (bounds.getMaxY() - borderWidth)) {
//            y = bounds.getMaxY() - borderWidth - Math.random() * borderWidth * 2.0;
//        }

//        n.locX = x;
//        n.locY = y;
    }

    public void calcAttraction(GraphEdge e) {
        NodeWidget w1 = getWidget(scene.getEdgeSource(e));
        NodeWidget w2 = getWidget(scene.getEdgeTarget(e));
        assert (w1 != null && w2 != null) : "wrong edge=" + e;
//        Widget wid1 = scene.findWidget(n1);
//        Rectangle rect1 = wid1.getBounds();
//        Widget wid2 = scene.findWidget(n2);
//        Rectangle rect2 = wid2.getBounds();
        
        double xDelta = w1.locX - w2.locX;
        double yDelta = w1.locX - w2.locY;

        double deltaLength = Math.max(MIN, Math.sqrt(xDelta*xDelta + yDelta*yDelta));
        double force =  (deltaLength * deltaLength) / forceConstant;

        double xDisp = (xDelta / deltaLength) * force;
        double yDisp = (yDelta / deltaLength) * force;
        
        w1.dispX -= xDisp; 
        w1.dispY -= yDisp;
        w2.dispX += xDisp; 
        w2.dispY += yDisp;
    }

    public void calcRepulsion(NodeWidget w1) {
        w1.dispX = 0.0; 
        w1.dispY = 0.0;
//        Widget wid1 = scene.findWidget(n1);
//        Rectangle rect1 = wid1.getBounds();

        for (GraphNode n2 : scene.getNodes()) {
            NodeWidget w2 = getWidget(n2);
//            Widget wid2 = scene.findWidget(n2);
//            Rectangle rect2 = wid1.getBounds();
            //TODO..
//            if (n2.isFixed()) continue;
            if (w1 != w2) {
                double xDelta = w1.locX - w2.locX;
                double yDelta = w1.locY - w2.locY;
                double deltaLength = Math.max(MIN, Math.sqrt(xDelta*xDelta + yDelta*yDelta));
                double force = (forceConstant * forceConstant) / deltaLength;
                w1.dispX += (xDelta / deltaLength) * force;
                w1.dispY += (yDelta / deltaLength) * force;
            }
        }
    }
    
    /**
     * this "cools" down the forces causing smaller movements..
     */
    private void cool(int iter) {
        temp *= (1.0 - iter / (double) iterations);
    }
    
    
    private void layoutCirculary(Collection<GraphNode<I>> nodes, GraphNode<I> masterNode) {
        Point masterPoint = new Point();
        NodeWidget master = getWidget(masterNode);
        masterPoint.setLocation(master.locX, master.locY);
        double r;
        double theta;
        double thetaStep = Math.PI / 5;
        r = 150;
        theta = 0;
        Iterator<GraphNode<I>> it = nodes.iterator();
        NodeWidget nw = getWidget(it.next());
        while (true) {
            AffineTransform tr = AffineTransform.getRotateInstance(theta);
            Point2D d2point = tr.transform(new Point2D.Double(0, r), null);
            Point point = new Point((int)d2point.getX() + masterPoint.x, (int)d2point.getY() + masterPoint.y);
            if (isThereFreeSpace(point, nw)) {
                nw.locX = point.getX();
                nw.locY = point.getY();
                nw.dispX = 0;
                nw.dispY = 0;
                if (it.hasNext()) {
                    nw = getWidget(it.next());
                } else {
                    return;
                }
            }
            theta = theta + thetaStep;
            if (theta > (Math.PI * 2 - Math.PI / 10)) {
                r = r + 90;
                theta = theta - Math.PI * 2;
                thetaStep = thetaStep * 3 / 4; 
            }
        }
        
    }
    
    private boolean isThereFreeSpace(Point pnt, NodeWidget widget) {
        if(scene != null) {            
            if(widget != null) {
                Rectangle bnds = widget.getBounds();
                if (bnds == null) {
                    return true;
                }
                bnds = new Rectangle(pnt.x, pnt.y, bnds.width, bnds.height);
                for (GraphNode nd : scene.getNodes()) {
                    NodeWidget nw = getWidget(nd);
                    Rectangle bnds2 = nw.getBounds();
                    if (bnds2 == null) {
                        return true;
                    }
                    Point point = new Point();
                    point.setLocation(nw.locX, nw.locY);
                    bnds2 = new Rectangle(point, bnds2.getSize());
                    if (bnds.intersects((bnds2))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean areAllFixed() {
        for (GraphNode nd : scene.getNodes()) {
            if (!getWidget(nd).isFixed()) {
                return false;
            }
        }
        return true;
    }
    
    private void resetFixed() {
        for (GraphNode nd : scene.getNodes()) {
            getWidget(nd).setFixed(false); // XXX suboptimal, the same loop & findWidget here and at many other places 
        }
        getWidget(scene.getRootGraphNode()).setFixed(true);
    }
    
    private boolean isThereFreeSpaceNonFixedSpace(NodeWidget w) {
        Rectangle bnds = w.getBounds();
        if (bnds == null) {
            return true;
        }
        Point pnt = new Point();
        pnt.setLocation(w.locX, w.locY);
        bnds = new Rectangle(pnt, bnds.getSize());
        for (GraphNode nd : scene.getNodes()) {
            NodeWidget nw = getWidget(nd);
            Rectangle bnds2 = scene.findWidget(nd).getBounds();
            if (bnds2 == null) {
                return true;
            }
            Point point = new Point();
            point.setLocation(nw.locX, nw.locY);
            bnds2 = new Rectangle(point, bnds2.getSize());
            if (nw.isFixed() && bnds.intersects((bnds2))) {
                return false;
            }
        }
        return true;
    }
    
    private void doRelayoutNonFixed() {
        for (GraphNode node : scene.getNodes()) {
            NodeWidget w = getWidget(node);
            if (!w.isFixed()) {
                relayoutNonFixed(w);
            }
        }
    }
    
    private void relayoutNonFixed(NodeWidget w) {
        Point masterPoint = new Point();
        masterPoint.setLocation(w.locX, w.locY);
        double r;
        double theta;
        double thetaStep = Math.PI / 5;
        r = 30;
        theta = 0;
        w.setFixed(false);
        while (true) {
            AffineTransform tr = AffineTransform.getRotateInstance(theta);
            Point2D d2point = tr.transform(new Point2D.Double(0, r), null);
            Point point = new Point((int)d2point.getX() + masterPoint.x, (int)d2point.getY() + masterPoint.y);
            w.locX = point.getX();
            w.locY = point.getY();
            if (isThereFreeSpaceNonFixedSpace(w)) {
                w.setFixed(true);
                return;
            }
            theta = theta + thetaStep;
            if (theta > (Math.PI * 2 - Math.PI / 10)) {
                r = r + 30;
                theta = theta - Math.PI * 2;
                thetaStep = thetaStep * 3 / 4; 
            }
        }
        
    }

    private NodeWidget getWidget(GraphNode n) {
        return (NodeWidget) scene.findWidget(n);
    }
    
    private EdgeWidget getWidget(GraphEdge e) {
        return (EdgeWidget) scene.findWidget(e);
    }
   
} 
