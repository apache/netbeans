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
 * This interfaces provides a movement strategy.
 *
 * @author David Kaspar
 */
public interface MoveStrategy {

    /**
     * Called after an user suggests a new location and before the suggested location is stored to a specified widget.
     * This allows to manipulate with a suggested location to perform snap-to-grid, locked-axis on any other movement strategy.
     * @param widget the moved widget
     * @param originalLocation the original location specified by the MoveProvider.getOriginalLocation method
     * @param suggestedLocation the location suggested by an user (usually by a mouse cursor position)
     * @return the new (optional modified) location processed by the strategy
     */
    Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation);

}
