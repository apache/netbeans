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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.action.MoveControlPointProvider;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex
 */
public final class FreeMoveControlPointProvider implements MoveControlPointProvider {

    public List<Point> locationSuggested(ConnectionWidget connectionWidget, int index, Point suggestedLocation) {
        List<Point> controlPoints = connectionWidget.getControlPoints();
        int cpSize=controlPoints.size()-1;
        ArrayList<Point> list = new ArrayList<Point> (controlPoints);
        if (index <= 0 || index >= cpSize)return null;
        if(index==1)list.set(0,connectionWidget.getSourceAnchor().compute(connectionWidget.getSourceAnchorEntry()).getAnchorSceneLocation());
        if(index==cpSize - 1)
            list.set(cpSize,connectionWidget.getTargetAnchor().compute(connectionWidget.getTargetAnchorEntry()).getAnchorSceneLocation());
        list.set(index, suggestedLocation);
        return list;
    }
    
}
