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

import java.awt.event.ActionEvent;
import java.util.List;

/** Callback handler of invocations on some data type.
 *
 * @author Jaroslav Tulach
 */
interface ContextActionPerformer<T> {
    /** Performs action initiated by the event <code>ev</code> on
     * given data. 
     * 
     * @param ev the event that initiated this action
     * @param data the unmodifiable list of elements to operate on
     */
    public void actionPerformed(ActionEvent ev, List<? extends T> data);
}
