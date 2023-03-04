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

import java.util.Set;

/**
 * This interface represents a listener that is notified about changes in a object scene
 * which is passed as the first parameter of every listener methods.
 * <p>
 * Implementers of this interface should try and make the implemented methods as brief as possible.
 * These methods are called in the same thread that is handing the corresponding action,
 * and taking too much time in these methods will slow the UI update down.
 *
 * @author David Kaspar, William Headrick
 */
public interface ObjectSceneListener {

    /**
     * Called to notify that an object was added to an object scene.
     * This is called when an object-widget mapping is registered in an ObjectScene only.
     * At the moment of the call, the object is still not reqistered in the Graph*Scene classes yet.
     * Therefore do not use the methods of Graph*Scene.
     * @param event
     * @param addedObject
     */
    void objectAdded (ObjectSceneEvent event, Object addedObject);

    /**
     * Called to notify that an object was removed from an object scene.
     * This is called when an object-widget mapping is unregistered in an ObjectScene and Graph*Scene classes.
     * At the moment of the call, a widget (visual representation of the object) is still in the scene.
     * Therefore do not rely on a tree of widgets of the scene.
     * @param event the object scene event
     * @param removedObject the removed object
     */
    void objectRemoved (ObjectSceneEvent event, Object removedObject);

    /**
     * Called to notify that the object state of an object is changed.
     * This method is always called before any other ObjectSceneListener method is called.
     * @param event the object scene event
     * @param changedObject the object with changed object state
     * @param previousState the previous object state
     * @param newState the new object state
     */
    void objectStateChanged (ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState);

    /**
     * Called to notify that the object-selection is changed.
     * @param event the object scene event
     * @param previousSelection the set of previously selected objects
     * @param newSelection the set of newly selected objects
     */
    void selectionChanged (ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection);

    /**
     * Called to notify that the object-highlighting is changed.
     * @param event the object scene event
     * @param previousHighlighting the set of previously highlighted objects
     * @param newHighlighting the set of newly highlighted objects
     */
    void highlightingChanged (ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting);

    /**
     * Called to notify that the object-hovering is changed.
     * @param event the object scene event
     * @param previousHoveredObject the previous hovered object; null if there was no hovered object
     * @param newHoveredObject the new hovered object; null if there is no hovered object
     */
    void hoverChanged (ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject);

    /**
     * Called to notify that the object-focus is changed.
     * @param event the object scene event
     * @param previousFocusedObject the previously focused object; null if there was no focused object
     * @param newFocusedObject the newly focused object; null if there is no focused object
     */
    void focusChanged (ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject);

}
