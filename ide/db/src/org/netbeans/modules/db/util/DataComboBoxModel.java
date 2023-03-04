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

package org.netbeans.modules.db.util;

import javax.swing.ComboBoxModel;

/**
 * Serves as a model for {@link DataComboBoxSupport}.
 *
 * @author Andrei Badea
 */
public interface DataComboBoxModel {

    /**
     * Returns the combo box model; cannot be null.
     */
    ComboBoxModel getListModel();

    /**
     * Returns the display name for the given item. The given item
     * is one of the items in the model returned by {@link #getListModel}.
     */
    String getItemDisplayName(Object item);

    /**
     * Returns the tooltip text for the given item. The given item
     * is one of the items in the model returned by {@link #getListModel}.
     */
    String getItemTooltipText(Object item);

    /**
     * Returns the text for the "Add item" item (used to add new items 
     * to the combo box).
     */
    String getNewItemDisplayName();

    /**
     * Invoked when the "Add item" is selected. This method should do 
     * whatever is necessary to retrieve the new item to be added (e.g.
     * by prompting the user) and add the new item to {@link #getListModel},
     * firing a contentsChanged event.
     */
    void newItemActionPerformed();
}
