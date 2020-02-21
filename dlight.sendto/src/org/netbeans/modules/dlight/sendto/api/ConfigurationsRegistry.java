/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.sendto.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 */
public final class ConfigurationsRegistry {

    private final static ConfigurationsRegistry instance;
    private final ConfigurationsModel model = ConfigurationsModel.getDefault();

    static {
        instance = new ConfigurationsRegistry();
        restore();
    }

    private ConfigurationsRegistry() {
    }

    public static ConfigurationsRegistry getDefault() {
        return instance;
    }

    public static void update(ConfigurationsModel newModel) {
        instance.model.setDataFrom(newModel);
    }

    public static void store() {
        try {
            Preferences registry = NbPreferences.forModule(ConfigurationsRegistry.class);
            registry.clear();
            int h_idx = 1;

            for (Configuration cfg : instance.model.getConfigurations()) {
                String prefix = Integer.toString(h_idx++) + '_';
                Map<String, String> properties = cfg.getProperties();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    registry.put(prefix + entry.getKey(), entry.getValue());
                }
            }

            registry.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void restore() {
        try {
            Preferences registry = NbPreferences.forModule(ConfigurationsRegistry.class);

            if (registry.keys().length == 0) {
                // First start... copy some template...
                registry.absolutePath();
                InputStream is = ConfigurationsRegistry.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/sendto/resources/initialConfiguration"); // NOI18N

                if (is == null) {
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String s;
                while ((s = br.readLine()) != null) {
                    int idx = s.indexOf('=');
                    if (idx > 0) {
                        registry.put(s.substring(0, idx), s.substring(idx + 1).replace("\\n", "\n")); // NOI18N
                    }
                }
            }

            HashMap<String, Configuration> configs = new HashMap<String, Configuration>();

            for (String key : registry.keys()) {
                int idx = key.indexOf('_');

                if (idx < 0) {
                    // Should not happen
                    continue;
                }
                
                String prefix = key.substring(0, idx);

                Configuration cfg;

                if (!configs.containsKey(prefix)) {
                    configs.put(prefix, new Configuration());
                }

                cfg = configs.get(prefix);
                String property = key.substring(idx + 1);
                String val = registry.get(key, "<error>"); // NOI18N
                cfg.set(property, val);
            }

            for (Configuration cfg : configs.values()) {
                instance.model.add(cfg);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static ConfigurationsModel getModelCopy() {
        return (ConfigurationsModel) instance.model.clone();
    }

    public static List<Configuration> getConfigurations() {
        return instance.model.getConfigurations();
    }
}
