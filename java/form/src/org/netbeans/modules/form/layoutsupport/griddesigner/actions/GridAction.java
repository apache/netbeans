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

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

import javax.swing.JMenuItem;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;


/**
 * Action on the grid.
 *
 * @author Jan Stola
 */
public interface GridAction {
    /** Action's context. */
    public enum Context {COMPONENT, COLUMN, ROW, CELL}

    /**
     * Returns attribtue values of this action
     * (corresponds to {@code javax.swing.Action.getValue()}).
     *
     * @param key name of the attribute whose value should be returned.
     * @return value of the specified attribute.
     */
    Object getValue(String key);

    /**
     * Determines whether this action is enabled in the specified context.
     *
     * @param context designer context.
     * @return {@code true} if the action is enabled,
     * returns {@code false} otherwise.
     */
    boolean isEnabled(DesignerContext context);

    /**
     * Performs this action.
     *
     * @param gridManager manager to use to modify the grid.
     * @param context designer context.
     * @return column/row changes done by this action or {@code null}
     * if no columns/rows were inserted/deleted.
     */
    GridBoundsChange performAction(GridManager gridManager, DesignerContext context);

    /**
     * Returns (special) popup menu presenter for this action. Majority
     * of actions should return {@code null} from this method. You should
     * return your own presenter only if {@code JMenuItem} is not suitable
     * presenter for this action.
     * 
     * @param performer action performer that should be used by the popup
     * presenter to perform the actual action.
     * @return popup menu or {@code null} if {@code JMenuItem} should
     * be used as the popup menu presenter.
     */
    JMenuItem getPopupPresenter(GridActionPerformer performer);

}
