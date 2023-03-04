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
package org.netbeans.modules.editor.lib2.view;

import java.util.EventListener;
import org.netbeans.api.annotations.common.NonNull;

/**
 * View hierarchy listener notifies about view rebuilds and visual changes in view hierarchy.
 * 
 * @author Miloslav Metelka
 */

public interface ViewHierarchyListener extends EventListener {

    /**
     * Notification about visual change that occurred in view hierarchy.
     * <br>
     * Notification may come in response to document modification but also in response
     * to a model&lt;-&gt;view query to view hierarchy (due to fact that view hierarchy is computed lazily).
     * <br>
     * Notification may come from any thread.
     * <br>
     * When this event is notified the listeners must make no
     * queries to view hierarchy synchronously (they should only mark what has changed and
     * ask later).
     * 
     * @param evt non-null description of visual change.
     */
    void viewHierarchyChanged(@NonNull ViewHierarchyEvent evt);

}
