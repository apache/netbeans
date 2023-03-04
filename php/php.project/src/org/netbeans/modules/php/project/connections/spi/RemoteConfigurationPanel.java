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

package org.netbeans.modules.php.project.connections.spi;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.connections.ConfigManager;

/**
 * @author Tomas Mysik
 */
public interface RemoteConfigurationPanel {

    /**
     * Attach a {@link ChangeListener change listener} that is to be notified of changes
     * in the configration panel (e.g., the result of the {@link #isValidConfiguration} method
     * has changed).
     *
     * @param listener a listener.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Remove a {@link ChangeListener change listener}.
     *
     * @param listener a listener.
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Return a UI component used to allow the user to customize this {@link RemoteConfiguration remote configuration}.
     *
     * @return a component which provides a configuration UI, never <code>null</code>.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    JComponent getComponent();

    /**
     *
     * @param configuration {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration} to read data from.
     */
    void read(ConfigManager.Configuration configuration);

    /**
     *
     * @param configuration {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration} to store data to.
     */
    void store(ConfigManager.Configuration configuration);

    /**
     * Check whether this {@link RemoteConfiguration configuration} is valid, it means it contains no errors.
     * @return <code>true</code> if this {@link RemoteConfiguration remote configuration} contains no errors, <code>false</code> otherwise.
     */
    boolean isValidConfiguration();

    /**
     * Get the error messsage if this {@link RemoteConfiguration remote configuration} is not valid.
     * @return error messsage if this {@link RemoteConfiguration remote configuration} is not valid.
     * @see #isValidConfiguration()
     * @see #getWarning()
     */
    String getError();

    /**
     * Get the warning messsage. Please notice that this warning message is not related
     * to panel {@link #isValidConfiguration() validity}.
     * @return warning messsage.
     * @see #isValidConfiguration()
     * @see #getError()
     */
    String getWarning();
}
