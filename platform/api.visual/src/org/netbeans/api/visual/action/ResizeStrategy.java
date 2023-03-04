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
 * This interface provides a resizing strategy.
 *
 * @author David Kaspar
 */
public interface ResizeStrategy {

    /**
     * Called after an user suggests a new boundary and before the suggested boundary is stored to a specified widget.
     * This allows to manipulate with a suggested boundary to perform snap-to-grid, locked-axis on any other resizing strategy.
     * @param widget the resized widget
     * @param originalBounds the original bounds of the resizing widget
     * @param suggestedBounds the bounds of the resizing widget suggested by an user (usually by a mouse cursor position)
     * @param controlPoint the control point that is used by an user for resizing
     * @return the new (optionally modified) boundary processed by the strategy
     */
    public Rectangle boundsSuggested (Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ResizeProvider.ControlPoint controlPoint);

}
