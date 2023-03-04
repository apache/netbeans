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

/**
 * The enum represents allowed types of event processing the is used to process the Swing event coming from a view component
 * and that should be delegated to widgets on the scene.
 *
 * @author David Kaspar
 */
public enum EventProcessingType {

    /**
     * Means that an event is processed by all widgets in whole scene. The order follows the tree hierarchy of a scene.
     */
    ALL_WIDGETS,

    /**
     * Means that an event is processed by a focused widget of a scene and then by its parents only.
     */
    FOCUSED_WIDGET_AND_ITS_PARENTS,

    /**
     * Means that an event is processed by a focused widget and its children only.
     */
    FOCUSED_WIDGET_AND_ITS_CHILDREN,

    /**
     * Means that an event is processed by a focused widget and its children and its parents only. 
     */
    FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS

}
