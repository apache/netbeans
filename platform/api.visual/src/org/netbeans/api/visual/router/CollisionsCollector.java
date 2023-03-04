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
package org.netbeans.api.visual.router;

import java.awt.*;
import java.util.List;

/**
 * This interface is used for collecting collision regions. There are two separate types of regions - vertical and horizontal.
 * The collector does not use any context. Use <code>ConnectionWidgetCollisionCollector</code> interface
 * if you want to receive context of currently routed connection widget.
 *
 * @author David Kaspar
 */
public interface CollisionsCollector {

    /**
     * Gathers collision collections and fills up the lists of vertical and horizontal collisions.
     * This method is similar to <code>ConnectionWidgetCollisionCollector.collectCollisions</code>
     * but does not take any paramter of a collector context.
     * @param verticalCollisions the list of vertical collisions
     * @param horizontalCollisions the list of horizontal collisions
     */
    public void collectCollisions (List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions);

}
