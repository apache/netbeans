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

package org.netbeans.spi.viewmodel;

/**
 * The extension of {@link NodeModel} that can display check-boxes next to the
 * node display name.
 * Instead of a special column with check-boxes, this extension can be used.
 * It will make the UI cleaner and more usable.
 *
 * @author Martin Entlicher
 * @since 1.17
 */
public interface CheckNodeModel extends NodeModel {

    /**
     * Tell the renderer to display the check-box.
     *
     * @param node the tree node object
     * @return <code>true</code> if the check-box should be displayed, <code>false</code> otherwise.
     */
    boolean isCheckable(Object node) throws UnknownTypeException;

    /**
     * Provide the enabled state of the check-box.
     *
     * @param node the tree node object
     * @return <code>true</code> if the check-box should be enabled, <code>false</code> otherwise.
     */
    boolean isCheckEnabled(Object node) throws UnknownTypeException;

    /**
     * Provide the selected state of the check-box.
     *
     * @param node the tree node object
     * @return <code>true</code> if the check-box should be selected,
     *         <code>false</code> if it should be unselected and
     *         <code>null</code> if the state is unknown.
     */
    Boolean isSelected(Object node) throws UnknownTypeException;

    /**
     * Called by the renderer when the check-box gets selected/unselected
     *
     * @param node the tree node object
     * @param selected <code>true</code> if the check-box was selected,
     *                 <code>false</code> if the check-box was unselected.
     */
    void setSelected(Object node, Boolean selected) throws UnknownTypeException;

}
