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
