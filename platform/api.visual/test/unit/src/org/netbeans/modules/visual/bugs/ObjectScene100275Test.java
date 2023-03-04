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
package org.netbeans.modules.visual.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.api.visual.model.*;
import org.netbeans.api.visual.widget.Widget;

import java.util.Collections;
import java.util.Set;

/**
 * @author David Kaspar
 */
public class ObjectScene100275Test extends NbTestCase {

    private static final String OBJECT = "theObject"; // NOI18N

    public ObjectScene100275Test (String name) {
        super (name);
    }

    public void testRemoveObjectNotifyListeners () {
        ObjectScene scene = new ObjectScene ();
        scene.addObjectSceneListener (new LoggingObjectSceneListener (), ObjectSceneEventType.values ());
        Widget widget = new LoggingWidget (scene);
        scene.addObject (OBJECT, widget);
        scene.setSelectedObjects (Collections.singleton (OBJECT));
        scene.setHighlightedObjects (Collections.singleton (OBJECT));
        scene.setHoveredObject (OBJECT);
        scene.setFocusedObject (OBJECT);
        scene.removeObject (OBJECT);
        compareReferenceFiles ();
    }

    private class LoggingObjectSceneListener implements ObjectSceneListener {

        public void objectAdded (ObjectSceneEvent event, Object addedObject) {
            getRef ().println ("Added: " + addedObject);
        }

        public void objectRemoved (ObjectSceneEvent event, Object removedObject) {
            getRef ().println ("Removed: " + removedObject);
        }

        public void objectStateChanged (ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
// TODO - not working since the ObjectState.toString has to produce loggable/comparable output
//            getRef ().println ("objectStateChanged: " + changedObject + " : " + previousState + " -> " + newState);
        }

        public void selectionChanged (ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
            getRef ().println ("selectionChanged: " + previousSelection + " -> " + newSelection);
        }

        public void highlightingChanged (ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
            getRef ().println ("highlightingChanged: " + previousHighlighting + " -> " + newHighlighting);
        }

        public void hoverChanged (ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
            getRef ().println ("hoverChanged: " + previousHoveredObject + " -> " + newHoveredObject);
        }

        public void focusChanged (ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
            getRef ().println ("focusChanged: " + previousFocusedObject + " -> " + newFocusedObject);
        }

    }

    private class LoggingWidget extends Widget {

        public LoggingWidget (ObjectScene scene) {
            super (scene);
        }

        protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
// TODO - not working since the ObjectState.toString has to produce loggable/comparable output
//            getRef ().println ("notifyStateChanged: " + ((ObjectScene) getScene ()).findObject (this) + " : " + previousState + " -> " + state);
            super.notifyStateChanged (previousState, state);
        }

    }

}
