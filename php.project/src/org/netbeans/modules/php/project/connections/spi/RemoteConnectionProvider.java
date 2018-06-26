/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
