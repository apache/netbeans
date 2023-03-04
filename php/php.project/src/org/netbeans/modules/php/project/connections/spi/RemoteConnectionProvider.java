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

import java.util.Set;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.openide.windows.InputOutput;

/**
 * Provider for remote connection, e.g. FTP, SFTP etc.
 * @author Tomas Mysik
 */
public interface RemoteConnectionProvider {

    /**
     * Get the display name of the remote connection, e.g. <i>FTP</i>, <i>SFTP</i> etc.
     * @return the display name.
     */
    String getDisplayName();

    /**
     * Get the list of property names which will be used by this connection type.
     * @return the list of property names.
     */
    Set<String> getPropertyNames();

    /**
     * Create {@link RemoteConfiguration remote configuration} for this connection type
     * for the given {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}.
     * This {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     * should be somehow "marked" (it is up to each connection provider implementation) by this connection mark so it can be later recognized as
     * a {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     * for this connection type. It is suitable to put all the default values in it as well.
     * @param configuration
     * @return {@link RemoteConfiguration remote configuration}, never <code>null</code>.
     */
    RemoteConfiguration createRemoteConfiguration(ConfigManager.Configuration configuration);

    /**
     * Get a {@link RemoteConfiguration remote configuration} if
     * {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     * is configuration of this connection type, <code>null</code> otherwise.
     * <p>
     * <b>This method can throw an exception</b> if
     * {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     * contains invalid values.
     * @param configuration {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     *                      from which {@link RemoteConfiguration remote configuration} can be created.
     * @return {@link RemoteConfiguration remote configuration} if this connection type is interested
     *         in {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     *         or <code>null</code>.
     * @see #createRemoteConfiguration(org.netbeans.modules.php.project.connections.ConfigManager.Configuration)
     */
    RemoteConfiguration getRemoteConfiguration(ConfigManager.Configuration configuration);

    /**
     * Similar to {@link #getRemoteConfiguration(org.netbeans.modules.php.project.connections.ConfigManager.Configuration)};
     * get the {@link RemoteClient remote client} if the {@link RemoteConfiguration remote configuration} belongs
     * to this connection type, <code>null</code> otherwise.
     * @param remoteConfiguration {@link RemoteConfiguration remote configuration} with information remote connection.
     * @param io {@link InputOutput}, the displayer of protocol commands, can be <code>null</code>.
     * @return {@link RemoteClient remote client} or <code>null</code>.
     * @see #getRemoteConfiguration(org.netbeans.modules.php.project.connections.ConfigManager.Configuration)
     */
    RemoteClient getRemoteClient(RemoteConfiguration remoteConfiguration, InputOutput io);

    /**
     * Get a {@link RemoteConfigurationPanel remote configuration panel} if
     * {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     * is configuration of this connection type, <code>null</code> otherwise.
     * @param configuration {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     *                      from which {@link RemoteConfigurationPanel remote configuration panel} can be created.
     * @return {@link RemoteConfigurationPanel remote configuration panel} if this connection type is interested
     *         in {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration}
     *         or <code>null</code>.
     * @see #getRemoteConfiguration(org.netbeans.modules.php.project.connections.ConfigManager.Configuration)
     */
    RemoteConfigurationPanel getRemoteConfigurationPanel(ConfigManager.Configuration configuration);

    /**
     * Validate the given configuration and return possible errors/warnings. Return {@code null} if this provider
     * cannot validate the given configuration.
     * @param remoteConfiguration remote configuration to be validated
     * @return errors/warnings or {@code null} if this provider cannot validate the given configuration
     */
    ValidationResult validate(RemoteConfiguration remoteConfiguration);

}
