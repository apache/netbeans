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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.ConnectionWidget;

import java.util.List;
import java.awt.*;

/**
 * This interface controls a move control point action.
 *
 * @author David Kaspar
 */
public interface MoveControlPointProvider {

    /**
     * Called to resolve control points of a connection widget for specified suggested change of a location of a control point specified by its index.
     * Usually used for supplying the move strategy of control points.
     * @param connectionWidget the connection widget
     * @param index the index of the control point which new location was suggested by an user
     * @param suggestedLocation the suggested location (by an user) of a control point specified by its index
     * @return the list of new control points of the connection widget
     */
    List<Point> locationSuggested (ConnectionWidget connectionWidget, int index, Point suggestedLocation);

}
