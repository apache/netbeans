/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.actions;

/** A context interface representing ability of an object to be edited.
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 7.10
 */
public interface Editable {
    /** Instructs an editor to be opened. The operation can
     * return immediately and the editor be opened later.
     * There can be more than one editor open, so one of them is
     * arbitrarily chosen and opened.
     */
    public void edit();
}
