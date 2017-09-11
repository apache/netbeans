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
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**XXX: declarative configurations?
 *
 * @author Jan Becicka
 */
public class ConfigurationsManager {
    private static final String RULE_PREFIX = "rule_config_";
    private static final String KEY_CONFIGURATIONS_VERSION = "configurations.version";
    private static final int CURRENT_CONFIGURATIONS_VERSION = 1;

    private ChangeSupport changeSupport = new ChangeSupport(this);

    private ConfigurationsManager() {
        configs = new ArrayList<Configuration>();
        init();
    }
    
    private static ConfigurationsManager instance;
    
    private ArrayList<Configuration> configs;
    
    public static synchronized ConfigurationsManager getDefault() {
        if (instance == null) {
            instance = new ConfigurationsManager();
        }
        return instance;
    }
    
    public Configuration getDefaultConfiguration() {
        return getConfiguration(0);
    }
    
    public List<Configuration> getConfigurations() {
        return Collections.unmodifiableList(configs);
    }
    
    public Configuration getConfiguration(int i) {
        return configs.get(i);
    }
    
    public int size() {
        return configs.size();
    }

    private void init() {
        Preferences prefs = NbPreferences.forModule(this.getClass());
        try {
            for (String kid:prefs.childrenNames()) {
                if (kid.startsWith(RULE_PREFIX)) {
                    Preferences p = NbPreferences.forModule(this.getClass()).node(kid);
                    String displayName = p.get("display.name", "unknown");
                    create(kid.substring(RULE_PREFIX.length()), displayName);
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        int configurationsVersion = prefs.getInt(KEY_CONFIGURATIONS_VERSION, 0);
        if (configs.isEmpty()) {
            create("default", NbBundle.getMessage(ConfigurationsManager.class, "DN_Default"));
            Configuration jdk7 = create("jdk7", NbBundle.getMessage(ConfigurationsManager.class, "DN_ConvertToJDK7"));
            jdk7.enable("Javac_canUseDiamond");
            jdk7.enable("org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch");
            jdk7.enable("org.netbeans.modules.java.hints.jdk.ConvertToARM");
            jdk7.enable("org.netbeans.modules.java.hints.jdk.JoinCatches");
            // #215546 - requires user inspection
            // jdk7.enable("org.netbeans.modules.java.hints.jdk.UseSpecificCatch");
            //jdk7.enable("java.util.Objects");
        }
        if (configurationsVersion < 1 && !configurationExists("organizeImports")) {
            Configuration organizeImports = create("organizeImports", NbBundle.getMessage(ConfigurationsManager.class, "DN_OrganizeImports"));
            organizeImports.enable("org.netbeans.modules.java.hints.OrganizeImports");
        }
        prefs.putInt(KEY_CONFIGURATIONS_VERSION, CURRENT_CONFIGURATIONS_VERSION);
    }

    private boolean configurationExists(String id) {
        for (Configuration c : configs) {
            if (id.equals(c.id())) return true;
        }

        return false;
    }
    
    public Configuration create(String id, String displayName) {
        assert !id.startsWith(RULE_PREFIX);
        Configuration config = new Configuration(RULE_PREFIX + id, displayName);
        configs.add(config);
        changeSupport.fireChange();
        return config;
    }
    
    //TODO: copied from HintsSettings - would be better to have it on one place:
    private static final String PREFERENCES_LOCATION = "org/netbeans/modules/java/hints";
    
    public Configuration duplicate(Configuration orig, String id, String displayName) {
        assert !id.startsWith(RULE_PREFIX);
        Configuration config = new Configuration(RULE_PREFIX + id, displayName);
        configs.add(config);
        
        Preferences oldOne = NbPreferences.root().node(PREFERENCES_LOCATION).node(orig.id());
        Preferences newOne = NbPreferences.root().node(PREFERENCES_LOCATION).node(config.id());
        try {
            for (String name:oldOne.childrenNames()) {
                Preferences node = oldOne.node(name);
                for (String key: node.keys()) {
                    String old = node.get(key, null);
                    newOne.node(name).put(key, old);
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        changeSupport.fireChange();
        return config;
    }
    
    public void remove(Configuration config) {
        configs.remove(config);
        Preferences prefs = NbPreferences.forModule(this.getClass()).node(config.id());
        try {
            prefs.removeNode();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        changeSupport.fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener( listener );
    }

    public void removeChangeListener(ChangeListener listener ) {
        changeSupport.removeChangeListener( listener );
    }
    
    
}
