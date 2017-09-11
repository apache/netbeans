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
