/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.api;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.project.PhpPreferences;
import org.openide.util.Parameters;

/**
 * Remote preferences, for the remote type and with import enabled or disabled.
 * @see #getPreferences()
 */
public final class RemotePreferences {

    private static final Logger LOGGER = Logger.getLogger(RemotePreferences.class.getName());
    // do not change arbitrary - consult with layer's folder OptionsExport
    private static final String REMOTE_CONNECTIONS = "RemoteConnections"; // NOI18N
    private static final String GENERAL_PREFERENCES = "general"; // NOI18N

    private final String type;
    private final boolean importEnabled;


    private RemotePreferences(String type, boolean importEnabled) {
        Parameters.notNull("type", type);

        this.type = type;
        this.importEnabled = importEnabled;
    }

    /**
     * Create remote preferences for the given type and with import enabled or disabled.
     * @param type connection type, e.g. FTP or SFTP
     * @param importEnabled {@code true} if the import is enabled
     * @return remote preferences for the given type and with import enabled or disabled
     */
    public static RemotePreferences forType(String type, boolean importEnabled) {
        return new RemotePreferences(type, importEnabled);
    }

    /**
     * Get all server configurations of all types.
     * @return all server configurations of all types
     */
    public static Map<String, Map<String, String>> getServerConfigs() {
        Map<String, Map<String, String>> serverConfigs = new HashMap<>();
        Preferences remoteConnections = getServerConfigsPreferences();
        try {
            for (String name : remoteConnections.childrenNames()) {
                if (name.equals(RemotePreferences.GENERAL_PREFERENCES)) {
                    // ignore "general" node
                    continue;
                }
                Preferences node = remoteConnections.node(name);
                Map<String, String> value = new TreeMap<>();
                for (String key : node.keys()) {
                    value.put(key, node.get(key, null));
                }
                serverConfigs.put(name, value);
            }
        } catch (BackingStoreException bse) {
            LOGGER.log(Level.INFO, "Error while reading existing remote connections", bse);
        }
        return serverConfigs;
    }

    /**
     * Get preferences themselves.
     * @return the preferences themselves
     */
    public Preferences getPreferences() {
        return PhpPreferences.getPreferences(importEnabled).node(REMOTE_CONNECTIONS).node(GENERAL_PREFERENCES).node(type);
    }

    // XXX could be moved into "servers" subnode
    /**
     * Get root node of server configurations.
     * @return root node of server configurations
     */
    public static Preferences getServerConfigsPreferences() {
        return PhpPreferences.getPreferences(true).node(REMOTE_CONNECTIONS);
    }

}
