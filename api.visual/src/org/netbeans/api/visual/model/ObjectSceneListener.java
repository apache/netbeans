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
