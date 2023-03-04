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
package org.netbeans.modules.websvc.manager.model;

import java.util.EventListener;

/**
 * This listener notifies when the Webservice is
 * added/removed to/from the WebServiceGroup
 * Listeners - WebServicesRootNodeChildren
 * @author Winston Prakash
 */

public interface WebServiceGroupListener extends EventListener {
    
    public void webServiceAdded(WebServiceGroupEvent groupEvent);
    public void webServiceRemoved(WebServiceGroupEvent groupEvent);
    
}
