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
package org.netbeans.modules.websvc.api.support;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Provides a facility for obtaining the addOperation feature
 * for both JAX-WS and JAX-RPC web service.
 */
public interface AddOperationCookie extends Node.Cookie {

    /**
     * Adds a method definition to the the implementation class, possibly to SEI.
     */
    void addOperation();

    /**
     * Determines if the Add Operation pop up menu is enabled in source editor.
     * @param nodeLookup lookup of node for which AddOperationCookie action should be enabled or disabled
     * @return true if enabled false if not
     */
    boolean isEnabledInEditor(Lookup nodeLookup);

}
