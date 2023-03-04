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
 * A model filter for {@link CheckNodeModel}.
 * Can be also used to add the check-boxes to an ordinary {@link NodeModel}.
 * 
 * @author Martin Entlicher
 * @since 1.17
 */
public interface CheckNodeModelFilter extends NodeModelFilter {

    /**
     * Tell the renderer to display the check-box.
     *
     * @param original the original node model
     * @param node the tree node object
     * @return <code>true</code> if the check-box should be displayed, <code>false</code> otherwise.
     */
    boolean isCheckable(NodeModel original, Object node) throws UnknownTypeException;

    /**
     * Provide the enabled state of the check-box.
     *
     * @param original the original node model
     * @param node the tree node object
     * @return <code>true</code> if the check-box should be enabled, <code>false</code> otherwise.
     */
    boolean isCheckEnabled(NodeModel original, Object node) throws UnknownTypeException;

    /**
     * Provide the selected state of the check-box.
     *
     * @param original the original node model
     * @param node the tree node object
     * @return <code>true</code> if the check-box should be selected,
     *         <code>false</code> if it should be unselected and
     *         <code>null</code> if the state is unknown.
     */
    Boolean isSelected(NodeModel original, Object node) throws UnknownTypeException;

    /**
     * Called by the renderer when the check-box gets selected/unselected
     *
     * @param original the original node model
     * @param node the tree node object
     * @param selected <code>true</code> if the check-box was selected,
     *                 <code>false</code> if the check-box was unselected.
     */
    void setSelected(NodeModel original, Object node, Boolean selected) throws UnknownTypeException;

}
