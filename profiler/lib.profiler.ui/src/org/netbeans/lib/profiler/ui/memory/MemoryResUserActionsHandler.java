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

package org.netbeans.lib.profiler.ui.memory;

import java.util.ResourceBundle;


/**
 * This interface declares actions that the user may initiate when browsing memory profiling results.
 * For example, the user may move the cursor to some class and request the tool to show stack
 * traces for allocations of instances of this class.
 *
 * @author Misha Dmitriev
 */
public interface MemoryResUserActionsHandler {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    public static final String CANNOT_SHOW_PRIMITIVE_SRC_MSG = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle") // NOI18N
                                                                             .getString("MemoryResUserActionsHandler_CannotShowPrimitiveSrcMsg"); // NOI18N
    public static final String CANNOT_SHOW_REFLECTION_SRC_MSG = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle") // NOI18N
                                                                             .getString("MemoryResUserActionsHandler_CannotShowReflectionSrcMsg"); // NOI18N
    // -----
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void showSourceForMethod(String className, String methodName, String methodSig);

    // if sorting is not defined, use showStacksForClass(selectedClassId, CommonConstants.SORTING_COLUMN_DEFAULT, false);
    public void showStacksForClass(int selectedClassId, int sortingColumn, boolean sortingOrder);
}
