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

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;

/**
 * This interface controls an in-place editor of an in-place editor action.
 *
 * @author David Kaspar
 */
public interface InplaceEditorProvider<C extends JComponent> {

    /**
     * Represents possible directions for expansion of an editor component.
     */
    public enum ExpansionDirection {

        /**
         * Allow expansion to the left.
         */
        LEFT,
        
        /**
         * Allow expansion to the right.
         */
        RIGHT,
        
        /**
         * Allow expansion to the top.
         */
        TOP,
        
        /**
         * Allow expansion to the bottom.
         */
        BOTTOM

    }

    /**
     * This is an interface of editor action supplied to the methods in the provider.
     */
    interface EditorController {

        /**
         * Returns whether an in-place editor is visible.
         * @return true, if visible; false, if not visible
         */
        boolean isEditorVisible ();

        /**
         * Opens an in-place editor on a specified widget.
         * @param widget the widget
         * @return true, if the editor is really opened
         */
        boolean openEditor (Widget widget);

        /**
         * Closes the current in-place editor.
         * @param commit whether the current value in the in-place editor is approved by an user
         */
        void closeEditor (boolean commit);
        
        /**
         * Notify the boundary of an editor component is changed and auto-expansion should be recalculated.
         */
        void notifyEditorComponentBoundsChanged ();

    }

    /**
     * This is an interface that extends EditorController for ability to query for invocation type.
     * @since 2.16
     */
    interface TypedEditorController extends EditorController {

        /**
         * Returns a type of editor invocation
         * @return invocation type
         * @since 2.16
         */
        EditorInvocationType getEditorInvocationType ();

    }

    /**
     * Represents a type of in-place editor action invocation.
     * @since 2.16
     */
    enum EditorInvocationType {

        /**
         * Invoked by mouse.
         * @since 2.16
         */
        MOUSE,

        /**
         * Invoked by keyboard.
         * @since 2.16
         */
        KEY,

        /**
         * Invoked by <code>ActionFactory.getInplaceEditorController (inplaceEditorAction).openEditor(widget)</code> method.
         * @since 2.16
         */
        CODE,

    }

    /**
     * Called to notify about opening an in-place editor.
     * @param controller the editor controller
     * @param widget the widget where the editor is opened
     * @param editor the editor component
     */
    void notifyOpened (EditorController controller, Widget widget, C editor);

    /**
     * Called to notify about closing an in-place editor.
     * @param controller the editor controller
     * @param widget the widget where the editor is opened
     * @param editor the editor component
     * @param commit true, if the current value is approved by user and
     *     should be used; false if the current value is discarded by an user
     */
    void notifyClosing (EditorController controller, Widget widget, C editor, boolean commit);

    /**
     * Creates an in-place editor component for a specified widget. Called to acquire the component which should be added into the scene.
     * @param controller the editor controller
     * @param widget the widget where the editor is going to be opened
     * @return the editor component
     */
    C createEditorComponent (EditorController controller, Widget widget);
    
    /**
     * Called to obtain the initial boundary editor component in view coordination system.
     * @param controller the editor controller
     * @param widget the widget where the editor is going to be opened
     * @param editor the editor component
     * @param viewBounds the precalculated boundary of the editor component
     * @return the boundary of editor component in view coordination system;
     *     if null, then the viewBounds are automatically used
     */
    public Rectangle getInitialEditorComponentBounds(EditorController controller, Widget widget, C editor, Rectangle viewBounds);

    /**
     * Called to obtain directions where an editor component can expand to.
     * @param controller the editor controller
     * @param widget the widget where the editor is going to be opened
     * @param editor the editor component
     * @return the set of directions where the editor component can expand to;
     *     if null, then the editor component is not expanded to any direction
     */
    public EnumSet<ExpansionDirection> getExpansionDirections (EditorController controller, Widget widget, C editor);
    
}
