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

package org.netbeans.api.debugger;

import java.util.EventListener;


/**
 * This listener notifies about changes in the
 * {@link org.netbeans.api.debugger.DebuggerEngine}.
 *
 * @author   Jan Jancura
 */
public interface ActionsManagerListener extends EventListener {

    /**
     * Property name constant for
     * {@link #actionPerformed(Object)} event.
     * It should be use as a propertyName argument in
     * {@link ActionsManager#addActionsManagerListener(String,ActionsManagerListener)}
     * call, if you would like to receive this event notification.
     */
    public static final String              PROP_ACTION_PERFORMED = "actionPerformed"; // NOI18N
    /** 
     * Property name constant for 
     * {@link #actionPerformed(Object)} event.
     * It should be use as a propertyName argument in 
     * {@link ActionsManager#addActionsManagerListener(String,ActionsManagerListener)}
     * call, if you would like to receive this event notification.
     */
    public static final String              PROP_ACTION_STATE_CHANGED = "actionStateChanged"; // NOI18N
    
    /**
     * Called when some action is performed.
     *
     * @param action action constant
     */
    public void actionPerformed (
        Object action
    );
    
    /**
     * Called when a state of some action has been changed.
     *
     * @param action action constant
     * @param enabled a new state of action
     */
    public void actionStateChanged (
        Object action, boolean enabled
    );
}

