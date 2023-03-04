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


package org.netbeans.modules.palette;

import java.beans.PropertyChangeListener;
import java.util.EventListener;

/**
 * Listens for changes in palette model.
 *
 * @author S. Aubrecht
 */
public interface ModelListener extends PropertyChangeListener {

    public static final String PROP_SELECTED_ITEM = "selectedItem";

    public void categoriesAdded( Category[] addedCategories );

    public void categoriesRemoved( Category[] removedCategories );

    public void categoriesReordered();
}
