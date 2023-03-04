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

import java.beans.PropertyEditor;

/**
 * Use this to provide different property editors for different table cells.
 * Implement this filter to override the behavior of any registered {@link TablePropertyEditorsModel}s.
 *
 * @author Martin Entlicher
 * @since 1.42
 * @see TablePropertyEditorsModel
 */
public interface TablePropertyEditorsModelFilter extends Model {
    
    /**
     * Get the property editor for the given table cell.
     * 
     * @param original The original {@link TablePropertyEditorsModel}
     * @param node an object returned from {@link TreeModel#getChildren(java.lang.Object, int, int) }
     *             for this row
     * @param columnID an id of column defined by {@link ColumnModel#getID()}
     * @return The property editor or <code>null</code> to use the column default one.
     * @throws UnknownTypeException if there is nothing to be provided for the given
     *         parameter type
     */
    PropertyEditor getPropertyEditor(TablePropertyEditorsModel original,
                                     Object node, String columnID) throws UnknownTypeException;
    
}
