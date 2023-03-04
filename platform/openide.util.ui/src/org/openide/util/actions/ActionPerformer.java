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

package org.openide.util.actions;

/** Specifies how an action should be performed.
* Should be implemented by classes which are able to perform an action's work
* on its behalf, e.g. for {@link CallbackSystemAction}s.
 * @deprecated No longer recommended. See {@link CallbackSystemAction#setActionPerformer} for details.
*/
@Deprecated
public interface ActionPerformer {
    /** Called when the action is to be performed.
    * @param action the action to be performed by this performer
    */
    public void performAction(SystemAction action);
}
