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

package org.netbeans.modules.payara.common.nodes.actions;

import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Michal Mocnak
 */
public interface RefreshModulesCookie extends Node.Cookie {
    
    /**
     * Requests the refresh of the server state.
     * <p/>
     * @return Task handler when refresh is executed as asynchronous thread
     *         or <code>null</code> otherwise.
     */
    public RequestProcessor.Task refresh();

    /**
     * Requests the refresh of the server state.
     * <p/>
     * @return Task handler when refresh is executed as asynchronous thread
     *         or <code>null</code> otherwise.
     */
    public RequestProcessor.Task refresh(String expectedChild, String unexpectedChild);
    
}
