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

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;

/**
 * Provides the known server instances. Any registered listener must be
 * notified when instance is added or removed during the life of the provider.
 * <p>
 * In order to register your provider you have to register it in filesystem
 * in folder <code>Servers</code>.
 * <p>
 * Note you can use {@link org.openide.util.ChangeSupport} for implementation
 * of listener logic.
 *
 * @author Petr Hejl
 */
public interface ServerInstanceProvider {

    /**
     * Returns the list of known server instances.
     *
     * @return the list of known server instances
     */
    List<ServerInstance> getInstances();

    /**
     * Adds a change listener to the provider. The listener must be notified
     * any time instance is added or removed.
     *
     * @param listener listener to add, <code>null</code> is allowed (but it si noop then)
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes the previously added listener. No more events will be fired on
     * the listener.
     *
     * @param listener listener to remove, <code>null</code> is allowed (but it si noop then)
     */
    void removeChangeListener(ChangeListener listener);

}
