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

package org.netbeans.spi.debugger.ui;

import org.netbeans.api.debugger.Properties;

/**
 * Controller, that is able to persist it's customized state.
 * 
 * @author Martin Entlicher
 * @since 2.46
 */
public interface PersistentController extends Controller {
    
    /**
     * Display name, that characterizes the current customized state of this controller.
     * @return The display name
     */
    String getDisplayName();
    
    /**
     * Load customized state from properties.
     * @param props The properties to load the state from.
     * @return <code>true</code> if the properties were successfully loaded, false otherwise.
     */
    boolean load(Properties props);
    
    /**
     * Save customized state into properties.
     * @param props The properties to save the state to.
     */
    void save(Properties props);
}
