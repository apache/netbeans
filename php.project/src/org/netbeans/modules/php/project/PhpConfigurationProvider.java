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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Manages configurations for a PHP project (copy/pasted from Java SE project).
 * @author Jesse Glick, Radek Matous
 */
public final class PhpConfigurationProvider implements ProjectConfigurationProvider<PhpConfigurationProvider.Config> {

    private static final Logger LOGGER = Logger.getLogger(PhpConfigurationProvider.class.getName());
    /**
     * Ant property name for active config.
     */
    public static final String PROP_CONFIG = "config"; // NOI18N

    /**
     * Ant property file which specified active config.
     */
    public static final String CONFIG_PROPS_PATH = "nbproject/private/config.properties"; // NOI18N


    public static final class Config implements ProjectConfiguration {

        /** The file basename, or <code>null</code> for default config. */
        public final String name;
        private final String displayName;

        public Config(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Config) && Utilities.compareObjects(name, ((Config) o).name);
        }

        @Override
        public String toString() {
            return "PhpConfigurationProvider.Config[" + name + "," + displayName + "]"; // NOI18N

        }
    }
    private static final Config DEFAULT = new Config(null,
            NbBundle.getMessage(PhpConfigurationProvider.class, "LBL_DefaultConfiguration"));
    private final PhpProject project;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final FileChangeListener fcl = new FileChangeAdapter() {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            update(fe);
        }

        private void update(FileEvent ev) {
            LOGGER.log(Level.FINEST, "Received {0}", ev);
            Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
            configDir = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N

            if (configDir != null) {
                configDir.removeFileChangeListener(fclWeak);
                configDir.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "(Re-)added listener to {0}", configDir);
            } else {
                LOGGER.log(Level.FINEST, "No nbproject/configs exists");
            }
            calculateConfigs();
            Set<String> newConfigs = configs.keySet();
            if (!oldConfigs.equals(newConfigs)) {
                LOGGER.log(Level.FINER, "Firing " + ProjectConfigurationProvider.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[]{oldConfigs, newConfigs});
                pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
            // XXX also fire PROP_ACTIVE_CONFIGURATION?
            }
        }
    };
    private final FileChangeListener fclWeak;
    private FileObject configDir;
    private Map<String, Config> configs;
    private final FileObject nbp;

    public PhpConfigurationProvider(PhpProject project) {
        this.project = project;
        fclWeak = FileUtil.weakFileChangeListener(fcl, null);
        nbp = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N

        if (nbp != null) {
            nbp.addFileChangeListener(fclWeak);
            LOGGER.log(Level.FINEST, "Added listener to {0}", nbp);
            configDir = nbp.getFileObject("configs"); // NOI18N

            if (configDir != null) {
                configDir.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "Added listener to {0}", configDir);
            }
        }
        project.getEvaluator().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PROP_CONFIG.equals(evt.getPropertyName())) {
                    LOGGER.log(Level.FINER, "Refiring " + PROP_CONFIG + " -> " + ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE);
                    pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
                }
            }
        });
    }

    private void calculateConfigs() {
        configs = new HashMap<>();
        if (configDir != null) {
            for (FileObject kid : configDir.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                try {
                    try (InputStream is = kid.getInputStream()) {
                        Properties p = new Properties();
                        p.load(is);
                        String name = kid.getName();
                        String label = p.getProperty(ConfigManager.PROP_DISPLAY_NAME);

                        configs.put(name, new Config(name, label != null ? label : name));
                    }
                } catch (IOException x) {
                    LOGGER.log(Level.INFO, null, x);
                }
            }
        }
        LOGGER.log(Level.FINEST, "Calculated configurations: {0}", configs);
    }

    @Override
    public Collection<Config> getConfigurations() {
        calculateConfigs();
        List<Config> l = new ArrayList<>();
        l.addAll(configs.values());
        Collections.sort(l, new Comparator<Config>() {

            Collator c = Collator.getInstance();

            @Override
            public int compare(Config c1, Config c2) {
                return c.compare(c1.getDisplayName(), c2.getDisplayName());
            }
        });
        l.add(0, DEFAULT);
        return l;
    }

    @Override
    public Config getActiveConfiguration() {
        calculateConfigs();
        String config = project.getEvaluator().getProperty(PROP_CONFIG);
        if (config != null && configs.containsKey(config)) {
            return configs.get(config);
        } else {
            return DEFAULT;
        }
    }

    @Override
    public void setActiveConfiguration(Config c) throws IOException {
        if (c != DEFAULT && !configs.values().contains(c)) {
            throw new IllegalArgumentException();
        }
        final String n = c.name;
        EditableProperties ep = project.getHelper().getProperties(CONFIG_PROPS_PATH);
        if (Utilities.compareObjects(n, ep.getProperty(PROP_CONFIG))) {
            return;
        }
        if (n != null) {
            ep.setProperty(PROP_CONFIG, n);
        } else {
            ep.remove(PROP_CONFIG);
        }
        project.getHelper().putProperties(CONFIG_PROPS_PATH, ep);
        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        ProjectManager.getDefault().saveProject(project);
        assert project.getProjectDirectory().getFileObject(CONFIG_PROPS_PATH) != null;
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        PhpProjectUtils.openCustomizerRun(project);
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return command.equals(ActionProvider.COMMAND_RUN)
                || command.equals(ActionProvider.COMMAND_DEBUG);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }
}
