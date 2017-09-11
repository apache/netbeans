/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
