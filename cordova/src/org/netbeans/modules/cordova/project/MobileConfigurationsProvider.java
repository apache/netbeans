/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * @author Jan Becicka
 */
public class MobileConfigurationsProvider implements ProjectConfigurationProvider<MobileConfigurationImpl>{

    private Project p;
    private Map<String, MobileConfigurationImpl> configs;
    private FileObject configDir;
    private FileObject nbProjectDir;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private static final Logger LOGGER = Logger.getLogger(MobileConfigurationsProvider.class.getName());
    private FileChangeListener fclWeakNB;
    private FileChangeListener fclWeakConfig;    
    
    private static final String PROP_CONFIG = "config"; //NOI18N
    
    
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
    };

    private void update(FileEvent ev) {
        LOGGER.log(Level.FINEST, "Received {0}", ev);
        Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
        configDir = p.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N
        if (configDir != null) {
            if (fclWeakConfig != null) {
                configDir.removeFileChangeListener(fclWeakConfig);
            }
            fclWeakConfig = FileUtil.weakFileChangeListener(fcl, configDir);
            configDir.addFileChangeListener(fclWeakConfig);
            LOGGER.log(Level.FINEST, "(Re-)added listener to {0}", configDir);
        } else {
            LOGGER.log(Level.FINEST, "No nbproject/configs exists");
        }
        calculateConfigs();
        Set<String> newConfigs = configs.keySet();
        if (!oldConfigs.equals(newConfigs)) {
            LOGGER.log(Level.FINER, "Firing " + ProjectConfigurationProvider.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[]{oldConfigs, newConfigs});
            support.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
        }
    }
    
    public MobileConfigurationsProvider(Project p) {
        this.p = p;
        this.nbProjectDir = p.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        if (nbProjectDir != null) {
            fclWeakNB = FileUtil.weakFileChangeListener(fcl, nbProjectDir);
            nbProjectDir.addFileChangeListener(fclWeakNB);
            LOGGER.log(Level.FINEST, "Added listener to {0}", nbProjectDir);
            configDir = nbProjectDir.getFileObject("configs"); // NOI18N
            if (configDir != null) {
                fclWeakConfig = FileUtil.weakFileChangeListener(fcl, configDir);
                configDir.addFileChangeListener(fclWeakConfig);
                LOGGER.log(Level.FINEST, "Added listener to {0}", configDir);
            }
        }
    }

    private void calculateConfigs() {
        configs = new HashMap<String, MobileConfigurationImpl>();
        if (configDir != null) {
            for (FileObject kid : configDir.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                MobileConfigurationImpl conf = MobileConfigurationImpl.create(p, kid);
                configs.put(conf.getId(), conf);
            }
        }
        LOGGER.log(Level.FINEST, "Calculated configurations: {0}", configs);
    }

    @Override
    public Collection<MobileConfigurationImpl> getConfigurations() {
        if (configs == null) {
            calculateConfigs();
        }
        Collection<MobileConfigurationImpl> l = new ArrayList<MobileConfigurationImpl>(configs.values());
        return l;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        support.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        support.removePropertyChangeListener(lst);
    }

//    @Override
    public List<String> getNewConfigurationTypes() {
        return Arrays.asList(new String[]{PlatformManager.ANDROID_TYPE, PlatformManager.IOS_TYPE});
    }

//    @Override
    public String createConfiguration(String configurationType, String configurationName) {
        EditableProperties props = new EditableProperties(true);
        props.put("type", configurationType); //NOI18N
        props.put("display.name", configurationName); //NOI18N
        FileObject conf;
        try {
            conf = ConfigUtils.createConfigFile(p.getProjectDirectory(), configurationType, props);
            MobileConfigurationImpl cfg = MobileConfigurationImpl.create(p, conf);
            return cfg.getId();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public MobileConfigurationImpl getActiveConfiguration() {
        if (configs == null) {
            calculateConfigs();
        }
        Preferences c = ProjectUtils.getPreferences(p, MobileConfigurationsProvider.class, false);
        String config = c.get(PROP_CONFIG, null);;
        if (config != null && configs.containsKey(config)) {
            return configs.get(config);
        }
        return getDefaultConfiguration();
    }

    @Override

    public void setActiveConfiguration(MobileConfigurationImpl c) throws IllegalArgumentException, IOException {
        if (configs == null) {
            calculateConfigs();
        }
        Preferences prefs = ProjectUtils.getPreferences(p, MobileConfigurationsProvider.class, false);
        prefs.put(PROP_CONFIG, c.getId());
    }

    private MobileConfigurationImpl getDefaultConfiguration() {
        if (configs.size() > 0) {
            return configs.values().iterator().next();
        } else {
            return null;
        }
    }

    
    @Override
    public boolean hasCustomizer() {
        return false;
    }

    @Override
    public void customize() {
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return false;
    }
}
