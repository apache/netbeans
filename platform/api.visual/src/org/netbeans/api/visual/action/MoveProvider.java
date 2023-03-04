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

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface controls move action.
 *
 * @author David Kaspar
 */
public interface MoveProvider {

    /**
     * Called to nofity about the start of movement of a specified widget.
     * @param widget the moving widget
     */
    void movementStarted (Widget widget);

    /**
     * Called to notify about the end of movement of a specified widget.
     * @param widget the moved widget
     */
    void movementFinished (Widget widget);

    /**
     * Called to acquire a origin location against which the movement will be calculated.
     * Usually it is a value of the Widget.getLocation method.
     * @param widget the moving widget
     * @return the origin location
     */
    Point getOriginalLocation (Widget widget);

    /**
     * Called to set a new location of a moved widget. The new location is based on the location returned by getOriginalLocation method.
     * Usually it is implemented as the Widget.setPreferredLocation method call.
     * @param widget the moved widget
     * @param location the new location
     */
    void setNewLocation (Widget widget, Point location);

}
