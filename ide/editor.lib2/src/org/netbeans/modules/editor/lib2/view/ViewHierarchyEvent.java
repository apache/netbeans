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
package org.netbeans.modules.editor.lib2.view;

import java.util.EventObject;
import javax.swing.event.DocumentEvent;

/**
 * View hierarchy event describing view rebuilding or view re-measurement change in view hierarchy.
 * <br>
 * Change may be related to several events:<ul>
 * <li> Document modification produces immediate updates to view hierarchy.
 * </li>
 * <li> Modification in view factories produces slightly delayed view rebuilding response.
 *    Currently either highlighting layers factory reports a change in one or more highlighting layers
 *    or a fold view factory reported a fold collapse or expansion.
 * </li>
 * <li> A client requested a view hierarchy operation (typically model-to-view or view-to-model translation)
 *    that caused certain view hierarchy area to be measured precisely. Due to very frequent changes
 *    (due to document re-parsing etc.) a most of the view hierarchy parts are just estimated
 *    and paragraph views sizes are either estimated (or based on their last known computation)
 *    and only computed precisely when needed.
 * </li>
 * 
 * <p>
 * Each change carries info of an offset area recomputed ({@link #changeStartOffset()} and
 * {@link #changeEndOffset()}) and whether the change produces any changes for y-coordinate related
 * components (such as error stripe and various side bars). Note that y-coordinate related clients
 * may completely ignore the change start/end offsets and only take care of y-related change information.
 * <br>
 * If change produces y-coordinate changes the changed visual area corresponds to
 * &lt;{@link #startY()},{@link #endY()}&gt;.
 * <br>
 * The change may cause rest of the document to move visually down/up which is reflected in {@link #deltaY()}
 * giving amount of pixels the area starting at {@link #endY()} moves down (negative value means moving up).
 * </p>
 * 
 * <p>
 * Note that when this event is notified the listeners must make no direct queries to view hierarchy.
 * They should only mark what has changed and needs to be recomputed and ask later.
 * <br>
 * View hierarchy events are fired rather frequently so the code in listeners should be efficient.
 * </p>
 * 
 * @author Miloslav Metelka
 */

public final class ViewHierarchyEvent extends EventObject {
    
    private final ViewHierarchyChange change;

    ViewHierarchyEvent(ViewHierarchy source, ViewHierarchyChange change) {
        super(source);
        this.change = change;
    }

    /**
     * View hierarchy in which the change occurred.
     */
    public ViewHierarchy viewHierarchy() {
        return (ViewHierarchy) getSource();
    }
    
    /**
     * Get document event that triggered view rebuild or null if there was no document modification
     * as a reason for view rebuilding.
     *
     * @return document event or null.
     */
    public DocumentEvent documentEvent() {
        return change.documentEvent();
    }
    
    /**
     * Start offset of a visual change in view hierarchy.
     * <br>
     * All model-to-view translations between {@link #changeStartOffset()} till {@link #changeEndOffset()}
     * might shift or change. They could change in x-coordinate and possibly also in y-coordinate in case
     * they lay between {@link #startY()} and {@link #endY()}. Those below {@link #endY()} have y-coordinate
     * shifted down by {@link #deltaY()} (may be negative for shifting up).
     * <br>
     * Offset corresponds to a state after possible document modification
     * (returned by {@link #documentEvent()}.
     *
     * @return start offset of visual change.
     */
    public int changeStartOffset() {
        return change.changeStartOffset();
    }
    
    /**
     * End offset of a visual change in view hierarchy.
     * <br>
     * All model-to-view translations between {@link #changeStartOffset()} till {@link #changeEndOffset()}
     * might shift or change. They could change in x-coordinate and possibly also in y-coordinate in case
     * they lay between {@link #startY()} and {@link #endY()}. Those below {@link #endY()} have y-coordinate
     * shifted down by {@link #deltaY()} (may be negative for shifting up).
     * <br>
     * Offset corresponds to a state after possible document modification
     * (returned by {@link #documentEvent()}.
     *
     * @return start offset of visual change.
     */
    public int changeEndOffset() {
        return change.changeEndOffset();
    }

    /**
     * Whether this change affects y-coordinate clients or not.
     * <br>
     * Return true if there was at least one paragraph view which changed its visual height.
     * Such views are be included inside {@link #startY()} and {@link #endY()} interval
     * and a corresponding {@link #deltaY()} is provided.
     * <br>
     * If the paragraph views retain their original offset boundaries upon rebuild
     * (and their precise span is not computed) they are not reported
     * as being y-changed.
     *
     * @return true if there was y-coordinate change or false otherwise.
     */
    public boolean isChangeY() {
        return change.isChangeY();
    }

    /**
     * Where the y-coordinate change begins.
     * <br>
     * Previously computed model-to-view values that have their y-coordinate
     * within {@link #startY()} and {@link #endY()} interval may be affected.
     * <br>
     * Measurements below {@link #endY()} should shift
     * its y-coordinate down by {@link #deltaY()} (may be negative for shifting up).
     *
     * @return y
     */
    public double startY() {
        return change.startY();
    }
    
    /**
     * Where the y-coordinate change ends.
     * <br>
     * Previously computed model-to-view values that have their y-coordinate
     * within {@link #startY()} and {@link #endY()} interval may be affected.
     * <br>
     * Measurements below {@link #endY()} should shift
     * its y-coordinate down by {@link #deltaY()} (may be negative for shifting up).
     *
     * @return y
     */
    public double endY() {
        return change.endY();
    }

    /**
     * Shift of area starting at {@link #endY()} down (or up if negative).
     * <br>
     * Measurements below {@link #endY()} should shift
     * its y-coordinate down by {@link #deltaY()} (may be negative for shifting up).
     *
     * @return delta
     */
    public double deltaY() {
        return change.deltaY();
    }
    
    @Override
    public String toString() {
        return viewHierarchy().toString() + " " + change.toString(); // NOI18N
    }
    
}
