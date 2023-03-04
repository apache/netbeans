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

/**
 * @author David Kaspar
 */
public interface ResizeProvider {

    /**
     * This enum represents a control point of a resize action.
     */
    // TODO - could be moved to ResizeStrategy interface, where it is used
    public enum ControlPoint {

        TOP_CENTER, BOTTOM_CENTER, CENTER_LEFT, CENTER_RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT

    }

    /**
     * Called to notify about the start of resizing.
     * @param widget the resizing widget
     */
    void resizingStarted (Widget widget);

    /**
     * Called to notify about the finish of resizing.
     * @param widget the resized widget
     */
    void resizingFinished (Widget widget);

}
