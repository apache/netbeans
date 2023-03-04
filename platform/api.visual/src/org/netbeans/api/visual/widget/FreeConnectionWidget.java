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
package org.netbeans.api.visual.widget;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an extension of the ConnectionWidget. Primarily it is used with FreeRouter and optionally Free*Anchor.
 * It has a method for adding and removing control points on specific locations.
 *
 * @author Alex
 */
// TODO - later, logic of this class can be moved to the ConnectionWidget class directly
// TODO - control points can be modified by accessing: getControlPoints ().get (0).x or y
public class FreeConnectionWidget extends ConnectionWidget {

    private double createSensitivity=1.00, deleteSensitivity=5.00;

    /**
     * Creates a free connection widget.
     * @param scene the scene
     */
    public FreeConnectionWidget (Scene scene) {
        super (scene);
    }

    /**
     * Creates a free connection widget with a specified create/delete sensitivity.
     * @param scene the scene
     * @param createSensitivity the sensitivity for adding a control point
     * @param deleteSensitivity the sensitivity for removing a control point
     */
    public FreeConnectionWidget (Scene scene, double createSensitivity, double deleteSensitivity) {
        super (scene);
        this.createSensitivity=createSensitivity; 
        this.deleteSensitivity=deleteSensitivity;
    }

    /**
     * Adds or removes a control point on a specified location
     * @param localLocation the local location
     */
    public void addRemoveControlPoint (Point localLocation) {
        ArrayList<Point> list = new ArrayList<Point> (getControlPoints());
            if(!removeControlPoint(localLocation,list,deleteSensitivity)){
                Point exPoint=null;int index=0;
                for (Point elem : list) {
                    if(exPoint!=null){
                        Line2D l2d=new Line2D.Double(exPoint,elem);
                        if(l2d.ptLineDist(localLocation)<createSensitivity){
                            list.add(index,localLocation);
                            break;
                        }
                    }
                    exPoint=elem;index++;
                }
            }
            setControlPoints(list,false);
    }
    
    private boolean removeControlPoint(Point point, ArrayList<Point> list, double deleteSensitivity){
        for (Point elem : list) {
            if(elem.distance(point)<deleteSensitivity){
                list.remove(elem);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a sensitivity.
     * @param createSensitivity the sensitivity for adding a control point
     * @param deleteSensitivity the sensitivity for removing a control point
     */
    public void setSensitivity(double createSensitivity, double deleteSensitivity){
        this.createSensitivity=createSensitivity; 
        this.deleteSensitivity=deleteSensitivity;
    } 
   
}
