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
package org.netbeans.modules.visual.router;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.visual.router.OrthogonalSearchRouter.Solution;

/**
 * @author David Kaspar
 */
final class OrthogonalSearchRouterCore {

    private static final int MAXIMAL_DEPTH = 5; // 6 is too slow for 10+ nodes and 10+ links
    private static final int CORNER_LENGTH = 200;
    private static final boolean UPDATE_COLLISION_LISTS = true; // set true for faster computation but not optimal results
    private static final boolean UPDATE_COLLISION_LISTS_REMOVE = true;
    static final boolean IGNORE_LINKS_WITH_ACTUAL_PORTS = false; // cause links to try to merge as a bus, multilinks are merged too
    private static final boolean OPTIMALIZE_REGIONS = false;

    private Scene scene;
    private ArrayList<Rectangle> verticalCollisions;
    private ArrayList<Rectangle> horizontalCollisions;
    private Point sourcePoint;
    private Anchor.Direction sourceDirection;
    private Point targetPoint;
    private Anchor.Direction targetDirection;

    private Point sourceBoundaryPoint;
    private Point targetBoundaryPoint;
    private final OrthogonalSearchRouterRegion[] regions = new OrthogonalSearchRouterRegion[MAXIMAL_DEPTH  + 1];

    private Point[] bestControlPoints;
    private int bestControlPointsPrice;

    public OrthogonalSearchRouterCore (Scene scene, ArrayList<Rectangle> verticalCollisions, ArrayList<Rectangle> horizontalCollisions, Point sourcePoint, Anchor.Direction sourceDirection, Point targetPoint, Anchor.Direction targetDirection) {
        this.scene = scene;
        this.verticalCollisions = verticalCollisions;
        this.horizontalCollisions = horizontalCollisions;
        this.sourcePoint = sourcePoint;
        this.sourceDirection = sourceDirection;
        this.targetPoint = targetPoint;
        this.targetDirection = targetDirection;
    }

    public OrthogonalSearchRouter.Solution route () {
        sourceBoundaryPoint = findBoundaryPoint(sourcePoint, sourceDirection);
        targetBoundaryPoint = findBoundaryPoint(targetPoint, targetDirection);

        //fixing a routing bug. If the x or y coordinates are on top of each other,
        // it causes a division by zero later on. So i force them to be slightly
        // offset at all times.
        if (sourceBoundaryPoint.x == targetBoundaryPoint.x) {
            targetBoundaryPoint.x += 1;
        }
        if (sourceBoundaryPoint.y == targetBoundaryPoint.y) {
            targetBoundaryPoint.y += 1;
        }

        OrthogonalSearchRouterRegion region =
                new OrthogonalSearchRouterRegion(sourceBoundaryPoint.x, sourceBoundaryPoint.y, 
                0, 0, sourceDirection, 0);

        search(region);

        //something went wrong in the search
        if (bestControlPoints == null) return null ;
        
        Solution solution = 
                new OrthogonalSearchRouter.Solution(bestControlPointsPrice, Arrays.asList(bestControlPoints)) ;
        
        return solution ;
    }

    private Point findBoundaryPoint (Point point, Anchor.Direction direction) {
        point = new Point (point);
        ArrayList<Rectangle> collisions;
        switch (direction) {
            case LEFT:
            case RIGHT:
                collisions = horizontalCollisions;
                break;
            case BOTTOM:
            case TOP:
                collisions = verticalCollisions;
                break;
            default:
                return point;
        }

        boolean changed = true;
        while (changed) {
            changed = false;

            switch (direction) {
                case LEFT:
                    for (Rectangle rectangle : collisions) {
                        if (rectangle.contains (point)) {
                            point.x = rectangle.x - 1;
                            changed = true;
                        }
                    }
                    break;
                case RIGHT:
                    for (Rectangle rectangle : collisions) {
                        if (rectangle.contains (point)) {
                            point.x = rectangle.x + rectangle.width;
                            changed = true;
                        }
                    }
                    break;
                case BOTTOM:
                    for (Rectangle rectangle : collisions) {
                        if (rectangle.contains (point)) {
                            point.y = rectangle.y + rectangle.height;
                            changed = true;
                        }
                    }
                    break;
                case TOP:
                    for (Rectangle rectangle : collisions) {
                        if (rectangle.contains (point)) {
                            point.y = rectangle.y - 1;
                            changed = true;
                        }
                    }
                    break;
            }
        }
        return point;
    }

    private void search (OrthogonalSearchRouterRegion region) {
        Rectangle inter = region.intersection (scene.getMaximumBounds ());
        region = new OrthogonalSearchRouterRegion (inter.x, inter.y, inter.width, inter.height, region.getDirection (), region.getDepth ());

        if (region.width < 0 || region.height < 0)
            return;
        assert region.x >= OrthogonalSearchRouterRegion.MIN_INT_REGION;
        assert region.y >= OrthogonalSearchRouterRegion.MIN_INT_REGION;
        assert region.x + region.width <= OrthogonalSearchRouterRegion.MAX_INT_REGION;
        assert region.y + region.height <= OrthogonalSearchRouterRegion.MAX_INT_REGION;
//        System.out.println ("REG: " + region);

        int depth = region.getDepth ();
        if (depth >= MAXIMAL_DEPTH) // too deeply
            return;

        region.extendToInfinity ();
        regions[depth] = region;

        List<Rectangle> collisions = region.isHorizontal () ? horizontalCollisions : verticalCollisions;

        ArrayList<OrthogonalSearchRouterRegion> subRegions = region.parseSubRegions (collisions);

        boolean updatedCollissions = false;
        if (! region.isEmpty ()) {
//            addRectangle (link, region);
            if (UPDATE_COLLISION_LISTS) {
                Rectangle updateCollisionsRect = new Rectangle (region);
                horizontalCollisions.add (updateCollisionsRect);
                verticalCollisions.add (updateCollisionsRect);
                updatedCollissions = true;
            }
        }

        if (region.containsInsideEdges (targetBoundaryPoint)) {
            // TODO - not true - point could lay on right or bottom edge ! - this is not checked yet
//            System.out.println ("FOUND");
            constructControlPoints (depth);
            // TODO
            // if not correct direction then
            //   reuse the same pathRegion rectangle and correct the direction
            // return the path as the best
        } else {

            //        try left and/or right
            if (region.getLength () > 0) {
                search (region.cloneWithCounterClockwiseEdge ());
                search (region.cloneWithClockwiseEdge ());
            }

            //        tryNearestSubRegion and maybeOthersSubRegions
            if (! subRegions.isEmpty ()) {
                for (OrthogonalSearchRouterRegion subRegion : subRegions) {
                    search (subRegion);
                }
            }
        }

        if (updatedCollissions  &&  UPDATE_COLLISION_LISTS_REMOVE) {
            horizontalCollisions.remove (horizontalCollisions.size () - 1);
            verticalCollisions.remove (verticalCollisions.size () - 1);
        }
    }

    private void constructControlPoints (int depth) {
        Anchor.Direction desiredDirection;
        switch (targetDirection) {
            case LEFT:
                desiredDirection = Anchor.Direction.RIGHT;
                break;
            case RIGHT:
                desiredDirection = Anchor.Direction.LEFT;
                break;
            case TOP:
                desiredDirection = Anchor.Direction.BOTTOM;
                break;
            case BOTTOM:
                desiredDirection = Anchor.Direction.TOP;
                break;
            default:
                throw new IllegalArgumentException ();
        }
        if (desiredDirection != regions[depth].getDirection ()) {
            final OrthogonalSearchRouterRegion region = regions[depth];
            depth++;
            regions[depth] = new OrthogonalSearchRouterRegion (region.x, region.y, region.width, region.height, desiredDirection, depth);
        }

        Point[] controlPoints = new Point[depth + 4];
        controlPoints[0] = new Point (sourcePoint);
        controlPoints[1] = new Point (sourceBoundaryPoint);
        for (int a = 2; a < depth + 2; a ++)
            controlPoints[a] = new Point ();
        controlPoints[depth + 2] = new Point (targetBoundaryPoint);
        controlPoints[depth + 3] = new Point (targetPoint);

        for (int a = 0; a < depth; a ++) {
            final OrthogonalSearchRouterRegion region = regions[a];
            Point previousPoint = controlPoints[a + 1];
            Point currentPoint = controlPoints[a + 2];
            if (region.isHorizontal ()) {
                int yy;
                if (a > 0) {
                    final int y1 = region.y;
                    final int y2 = region.y + region.height;
                    if (y1 <= OrthogonalSearchRouterRegion.MIN_INT_REGION && y2 < OrthogonalSearchRouterRegion.MAX_INT_REGION)
                        yy = y2 - OrthogonalSearchRouter.SPACING_EDGE;
                    else if (y1 > OrthogonalSearchRouterRegion.MIN_INT_REGION && y2 >= OrthogonalSearchRouterRegion.MAX_INT_REGION)
                        yy = y1 + OrthogonalSearchRouter.SPACING_EDGE;
                    else
                        yy = region.y + region.height / 2;
                } else
                    yy = previousPoint.y;
                previousPoint.y = currentPoint.y = yy;
            } else {
                int xx;
                if (a > 0) {
                    final int x1 = region.x;
                    final int x2 = region.x + region.width;
                    if (x1 <= OrthogonalSearchRouterRegion.MIN_INT_REGION && x2 < OrthogonalSearchRouterRegion.MAX_INT_REGION)
                        xx = x2 - OrthogonalSearchRouter.SPACING_EDGE;
                    else if (x1 > OrthogonalSearchRouterRegion.MIN_INT_REGION && x2 >= OrthogonalSearchRouterRegion.MAX_INT_REGION)
                        xx = x1 + OrthogonalSearchRouter.SPACING_EDGE;
                    else
                        xx = region.x + region.width / 2;
                } else
                    xx = previousPoint.x;
                previousPoint.x = currentPoint.x = xx;
            }
        }

        if (regions[depth].isHorizontal ())
            controlPoints[depth + 1].y = controlPoints[depth + 2].y;
        else
            controlPoints[depth + 1].x = controlPoints[depth + 2].x;

        //KRIS: this is never used since OPTIMALIZE_REGIONS is false always
//        if (OPTIMALIZE_REGIONS) {
//            for (int a = depth; a > 0; a --) {
////                if (infiniteY[a]) {
//                final int newY = controlPoints[a + 3].y;
//                final OrthogonalSearchRouterRegion regionY = regions[a];
//                final OrthogonalSearchRouterRegion regionY1 = regions[a - 1];
//                if (newY >= regionY.y && newY < regionY.y + regionY.height && newY >= regionY1.y && newY < regionY1.y + regionY1.height)
//                    controlPoints[a + 1].y = controlPoints[a + 2].y = newY;
////                }
////                if (infiniteX[a]) {
//                final int newX = controlPoints[a + 3].x;
//                final OrthogonalSearchRouterRegion regionX = regions[a];
//                final OrthogonalSearchRouterRegion regionX1 = regions[a - 1];
//                if (newX >= regionX.x && newX < regionX.x + regionX.width && newX >= regionX1.x && newX < regionX1.x + regionX1.width)
//                    controlPoints[a + 1].x = controlPoints[a + 2].x = newX;
////                }
//            }
//        }

        int controlPointsLength = removeDuplicateControlPoints (controlPoints);

        int price = calculatePrice (controlPointsLength, controlPoints);

        if (bestControlPoints == null || bestControlPointsPrice > price) {
            if (controlPointsLength < controlPoints.length) {
                bestControlPoints = new Point[controlPointsLength];
                System.arraycopy (controlPoints, 0, bestControlPoints, 0, controlPointsLength);
            } else
                bestControlPoints = controlPoints;
            bestControlPointsPrice = price;
//            System.out.println ("BEST");
            // DEBUG
//            if (SHOW_DEBUG_REGIONS) {
//                clearRectangle ();
//                for (int i = 0; i <= depth; i++)
//                    addRectangle (regions[i]);
//            }
        }
    }

    private int removeDuplicateControlPoints (Point[] controlPoints) {
        int newPointsLength = 0;
        for (int a = 1; a < controlPoints.length - 1; a++) {
            Point p0 = controlPoints[newPointsLength];
            Point p1 = controlPoints[a];
            Point p2 = controlPoints[a + 1];
            
            int epsilon = 2 ;
            if (Math.abs (p0.x - p1.x) < epsilon && Math.abs (p1.x - p2.x) < epsilon) {
                continue;
            }
            if (Math.abs (p0.y - p1.y) < epsilon && Math.abs (p1.y - p2.y) < epsilon) {
                continue;
            }
            
            newPointsLength++;
            if (newPointsLength != a) {
                controlPoints[newPointsLength] = p1;
            }
        }
        newPointsLength++;
        if (newPointsLength < controlPoints.length - 1) {
            controlPoints[newPointsLength++] = controlPoints[controlPoints.length - 1];
        }
        return newPointsLength;
    }

    private int calculatePrice (int controlPointsLength, Point[] controlPoints) {
        int price = 0;
        int numberOfLegs = controlPointsLength - 1 ;
        
        //add the lengths of the legs (cheap distance formula) and a corner value
        //for each control point (each turn).
        for (int a = 1; a < controlPointsLength; a++) {
            final Point p1 = controlPoints[a - 1];
            final Point p2 = controlPoints[a];
            price += Math.abs(p2.y - p1.y) + Math.abs(p2.x - p1.x) + CORNER_LENGTH;
        }
        
        //paths that have leg lengths out of proportion (non-square'ish), have a 
        //higher price.
        if (controlPointsLength > 0) {
            
            int average = price / numberOfLegs ;
            int diff = 0;
            for (int a = 1; a < controlPointsLength; a++) {
                final Point p1 = controlPoints[a - 1];
                final Point p2 = controlPoints[a];
                diff += Math.abs(Math.abs(p2.y - p1.y) + Math.abs(p2.x - p1.x) - average);
            }
            
            diff /= numberOfLegs;
            price += diff;
        }
        
        return price;
    }

}
