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

import java.awt.*;

/**
 * Represents an event for ContiguousSelectEvent passed to ContiguousSelectProvider. Contains information about selection-type, previously and currently choosen objects spots.
 *
 * @author David Kaspar
 * @since 2.17
 */
public final class ContiguousSelectEvent {

    private final Widget previouslyChoosenWidget;
    private final Point previouslyChoosenLocalLocation;

    private final Widget choosenWidget;
    private final Point choosenLocalLocation;

    private final SelectionType selectionType;

    private ContiguousSelectEvent (Widget previouslyChoosenWidget, Point previouslyChoosenLocalLocation, Widget choosenWidget, Point choosenLocalLocation, SelectionType selectionType) {
        this.previouslyChoosenWidget = previouslyChoosenWidget;
        this.previouslyChoosenLocalLocation = previouslyChoosenLocalLocation;
        this.choosenWidget = choosenWidget;
        this.choosenLocalLocation = choosenLocalLocation;
        this.selectionType = selectionType;
    }

    /**
     * Returns a previously choosen widget.
     * @return the previously choosen widget
     */
    public Widget getPreviouslyChoosenWidget () {
        return previouslyChoosenWidget;
    }

    /**
     * Returns a local location of a previously choosen widget.
     * @return the local location of the previously choosen widget
     */
    public Point getPreviouslyChoosenLocalLocation () {
        return previouslyChoosenLocalLocation != null ? new Point (previouslyChoosenLocalLocation) : null;
    }

    /**
     * Returns a choosen widget.
     * @return the choosen widget
     */
    public Widget getChoosenWidget () {
        return choosenWidget;
    }

    /**
     * Returns a local location of a choosen widget.
     * @return the local location of the choosen widget
     */
    public Point getChoosenLocalLocation () {
        return choosenLocalLocation != null ?  new Point (choosenLocalLocation) : null;
    }

    /**
     * Represents a selection type.
     * @return the selection type
     */
    public SelectionType getSelectionType () {
        return selectionType;
    }

    /**
     * Creates an event. Meant to be used by the library only.
     * @param previousWidget the previously choosen widget
     * @param previousLocalLocation the local location of the previously choosen widget
     * @param choosenWidget the choosen widget
     * @param choosenLocalLocation the local location of the currently choosen widget
     * @param selectionType the selection type invoked by an user
     * @return the contiguous select event
     */
    public static ContiguousSelectEvent create (Widget previousWidget, Point previousLocalLocation, Widget choosenWidget, Point choosenLocalLocation, SelectionType selectionType) {
        assert selectionType != null;
        return new ContiguousSelectEvent (previousWidget, previousLocalLocation, choosenWidget, choosenLocalLocation, selectionType);
    }

    /**
     * Defines a type of a selection.
     */
    public enum SelectionType {

        /**
         * Represents a normal selection that replace the previous selection.
         * Usually invokes without any key-modifier.
         */
        REPLACE_NON_CONTIGUOUS,

        /**
         * Represents a normal selection that replace the previous selection.
         * All objects that are between previously and current choosen spots defines the current selection.
         * Usually invokes with Shift key-modifier.
         */
        REPLACE_CONTIGUOUS,

        /**
         * Represents an additive selection where the new selection should be added to the current selection.
         * Usually invokes with Ctrl key-modifier.
         */
        ADDITIVE_NON_CONTIGUOUS,

        /**
         * Represents an additive selection where the new selection should be added to the current selection.
         * All objects that are between previously and current choosen spots defines the current selection.
         * Usually invokes with Ctrl and Shift key-modifiers.
         */
        ADDITIVE_CONTIGUOUS,

    }

}
