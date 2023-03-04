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

package org.netbeans.lib.profiler.ui.cpu;

import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;


/**
 * This interface declares actions that the user may initiate when browsing profiling results.
 * For example, the user may move the cursor to some method and request the tool to show its source
 * code, etc.
 *
 * @author Ian Formanek
 * @author Misha Dmitriev
 */
public interface CPUResUserActionsHandler {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class Adapter implements CPUResUserActionsHandler {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void addMethodToRoots(String className, String methodName, String methodSig) {
            throw new UnsupportedOperationException();
        }

        public void find(Object source, String findString) {
            throw new UnsupportedOperationException();
        }

        public void showReverseCallGraph(CPUResultsSnapshot snapshot, int threadId, int methodId, int view, int sortingColumn,
                                         boolean sortingOrder) {
            throw new UnsupportedOperationException();
        }

        public void showSourceForMethod(String className, String methodName, String methodSig) {
            throw new UnsupportedOperationException();
        }

        public void showSubtreeCallGraph(CPUResultsSnapshot snapshot, CCTNode node, int view, int sortingColumn,
                                         boolean sortingOrder) {
            throw new UnsupportedOperationException();
        }

        public void viewChanged(int viewType) {
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void addMethodToRoots(String className, String methodName, String methodSig);

    public void find(Object source, String findString);

    public void showReverseCallGraph(CPUResultsSnapshot snapshot, int threadId, int methodId, int view, int sortingColumn,
                                     boolean sortingOrder);

    /**
     * Display the source for the given method. className should never be null, but methodName and methodSig
     * may be null (for example, if we are viewing results at class level). In that case, just the class
     * source code should be displayed.
     * @param className  The fully qualified class name in VM format ("org/profiler/Main");
     * @param methodName The method name
     * @param methodSig  The method signature in VM format
     */
    public void showSourceForMethod(String className, String methodName, String methodSig);

    public void showSubtreeCallGraph(CPUResultsSnapshot snapshot, CCTNode node, int view, int sortingColumn, boolean sortingOrder);

    /**
     * Called when a view type change has been initiated from within a results component and the change should perhaps
     * be reflected in other views / ui as well.
     *
     * @param viewType the new view type
     */
    public void viewChanged(int viewType);
}
