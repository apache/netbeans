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

package org.openide.awt;

/** Possible types of selections in case of {@link ContextAction.Performer}.
 * These values can be passed to {@link Factory#context} method to specify 
 * the selection mode that will influence the selection in the action.
 *
 * @author Jaroslav Tulach
 */
enum ContextSelection {
    /** the action is enabled when exactly one instance of 
     * desired object is present
     */
    EXACTLY_ONE,
    /** the action is enabled if at least one, but potentially
     * many instances of a desired object are present
     */
    ANY,
    /** Each of the selected items (like <a href="@org-openide-nodes@/org/openide/nodes/Node.html">Node</a>s)
     * has to provide exactly one desired object. Moreover there is at least
     * one selected item.
     */
    EACH,
    /** Each of the selected items (like <a href="@org-openide-nodes@/org/openide/nodes/Node.html">Node</a>s)
     * has to provide at least one of the desired objects, but it can provide more.
     */
    ALL
}
