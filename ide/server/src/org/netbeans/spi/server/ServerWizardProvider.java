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

package org.netbeans.spi.server;

import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 * Provides the iterator and display name for addition of the new instance of
 * the server. Implementation of this class must be registered in
 * <code>Servers</code> folder in the system filesystem.
 *
 * @author Petr Hejl
 */
public interface ServerWizardProvider {

    /**
     * Returns the display name of the wizard. Usually same as the server name.
     *
     * @return the display name of the wizard
     */
    String getDisplayName();

    /**
     * Returns the iterator for adding the instance. {@link InstantiatingIterator#instantiate()}
     * should return the {@link org.netbeans.api.server.ServerInstance} created by the wizard.
     * <p>
     * Note that if the instance created by the wizard should be mentioned by
     * the infrastructure (as you usually want this to happen),
     * {@link ServerInstanceProvider} must fire change events on all registered
     * listeners.
     * <p>
     * The {@link java.util.Set} returned by {@link InstantiatingIterator#instantiate()}
     * should return the created {@link org.netbeans.api.server.ServerInstance}.
     *
     * @return iterator for adding the server instance
     */
    InstantiatingIterator getInstantiatingIterator();

}
