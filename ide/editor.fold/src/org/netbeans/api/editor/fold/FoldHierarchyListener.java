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

package org.netbeans.api.editor.fold;

/**
 * Listener for changes in the fold hierarchy.
 * <br>
 * It can be attached to fold hierarhcy
 * by {@link FoldHierarchy#addFoldHierarchyListener(FoldHierarchyListener)}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface FoldHierarchyListener extends java.util.EventListener {

    /**
     * Notify that the code hierarchy of folds has changed.
     *
     * @param evt event describing the changes in the fold hierarchy.
     */
    void foldHierarchyChanged(FoldHierarchyEvent evt);

}
