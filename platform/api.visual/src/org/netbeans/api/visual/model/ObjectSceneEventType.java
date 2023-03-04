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
package org.netbeans.api.visual.model;

/**
 * This enum is used for specifying events in which an object scene listener is interested.
 *
 * @author David Kaspar
 */
public enum ObjectSceneEventType {

    /**
     * Related to ObjectSceneListener.objectAdded method.
     */
    OBJECT_ADDED,

    /**
     * Related to ObjectSceneListener.objectRemoved method.
     */
    OBJECT_REMOVED,

    /**
     * Related to ObjectSceneListener.objectStateChanged method.
     */
    OBJECT_STATE_CHANGED,

    /**
     * Related to ObjectSceneListener.selectionChanged method.
     */
    OBJECT_SELECTION_CHANGED,

    /**
     * Related to ObjectSceneListener.highlightingChanged method.
     */
    OBJECT_HIGHLIGHTING_CHANGED,

    /**
     * Related to ObjectSceneListener.hoverChanged method.
     */
    OBJECT_HOVER_CHANGED,

    /**
     * Related to ObjectSceneListener.focusChanged method.
     */
    OBJECT_FOCUS_CHANGED,

}
