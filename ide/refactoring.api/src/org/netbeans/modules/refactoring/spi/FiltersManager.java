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
package org.netbeans.modules.refactoring.spi;

import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;

/**
 * Handles creation and manipulation with boolean state filters.
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 * @see FiltersDescription
 * @since 1.29
 */
public abstract class FiltersManager {

    /**
     * Indicates if a filter is selected.
     *
     * @param filterName the name of the filter to check
     * @return Returns true when given filter is selected, false otherwise.
     */
    public abstract boolean isSelected(String filterName);

    /**
     * {@code RefactoringElement}s should implement this interface if they
     * should be filterable in the results.
     *
     * @see RefactoringElement#include
     */
    public static interface Filterable {

        /**
         * Indicates if this element should be included in the results.
         *
         * @param manager the FiltersManager to use
         * @return true if this element should be included
         */
        boolean filter(FiltersManager manager);
    }
}
