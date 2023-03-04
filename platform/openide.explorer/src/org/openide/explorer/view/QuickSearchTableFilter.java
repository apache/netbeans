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
package org.openide.explorer.view;

/**
 * Filter of quick search in a table view.
 * Implement this when it's necessary to provide a customized String value
 * for the purpose of quick search functionality in a table view.
 * See for instance {@link OutlineView#setQuickSearchTableFilter(org.openide.explorer.view.QuickSearchTableFilter, boolean)}
 * 
 * @author Martin Entlicher
 * @since 6.43
 */
public interface QuickSearchTableFilter {
    
    /**
     * Get the string value of a cell in a table, used by quick search.
     * @param row The cell row
     * @param col The cell column
     * @return The string value of the given cell.
     */
    String getStringValueAt(int row, int col);
    
}
