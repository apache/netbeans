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
package org.netbeans.modules.payara.common.status;

import org.netbeans.modules.payara.tooling.PayaraStatusListener;

/**
 * Notification about server state check results containing common attribute
 * and methods related to listener registration.
 * <p/>
 * @author Tomas Kraus
 */
public abstract class BasicStateListener implements PayaraStatusListener {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Informs whether this listener is registered. */
    private boolean active;


    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of state check results notification.
     */
    public BasicStateListener() {
        active = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get an information whether this listener is registered.
     * <p/>
     * @return Value of <code>true</code> when this listener is registered
     *         or <code>false</code> otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Monitoring framework calls this method after listener was registered for
     * at least one event set.
     * <p/>
     * May be called multiple times for individual event sets during
     * registration phase.
     */
    @Override
    public void added() {
        active = true;
    }

    /**
     * Monitoring framework calls this method after listener was unregistered.
     * <p/>
     * Will be called once during listener removal phase when was found
     * registered for at least one event set.
     */
    @Override
    public void removed() {
        active = false;
    }

}
