/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.api.customizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import static org.netbeans.modules.maven.api.customizer.Bundle.*;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
public class ModelHandle2 {
    
    public static final String PANEL_RUN = "RUN"; //NOI18N
    public static final String PANEL_BASIC = "BASIC"; //NOI18N
    public static final String PANEL_CONFIGURATION = "CONFIGURATION"; //NOI18N
    public static final String PANEL_MAPPING = "MAPPING"; //NOI18N
    public static final String PANEL_LIBRARIES = "LIBRARIES"; //NOI18N
    public static final String PANEL_SOURCES = "SOURCES"; //NOI18N
    public static final String PANEL_COMPILE = "COMPILE"; //NOI18N
    public static final String PANEL_HEADERS = "LICENSE_HEADERS"; //NOI18N

    
    private final MavenProjectPropsImpl auxiliaryProps;
    private final MavenProject project;
    private final POMModel model;
    private final List<ModelOperation<POMModel>> pomOperations = new ArrayList<ModelOperation<POMModel>>();
    private final Map<String, ActionToGoalMapping> mappings;
    private final List<String> allActions;
    
    private List<Configuration> configurations;
    private boolean modConfig = false;
    private Configuration active;
    TreeMap<String, String> transPropsShared = new TreeMap<String, String>();
    TreeMap<String, String> transPropsPrivate = new TreeMap<String, String>();
    Set<ActionToGoalMapping> modifiedMappings = new HashSet<ActionToGoalMapping>();
    
    
    static {
        ModelHandle2.AccessorImpl impl = new ModelHandle2.AccessorImpl();
        impl.assign();
    }

    static class AccessorImpl extends CustomizerProviderImpl.ModelAccessor2 {
        
         public @Override ModelHandle2 createHandle(
                                        POMModel model,
                                        MavenProject proj, 
                                        Map<String, ActionToGoalMapping> mapp, 
                                        List<Configuration> configs,
                                        Configuration active,
                                        List<String> allActions,
                                        MavenProjectPropsImpl auxProps) {
            return new ModelHandle2(model, proj, mapp, configs, active, allActions, auxProps);
        }
        
         public void assign() {
             if (CustomizerProviderImpl.ACCESSOR2 == null) {
                 CustomizerProviderImpl.ACCESSOR2 = this;
             }
         }

        @Override
        public List<String> getAllActions(ModelHandle2 handle) {
            return handle.getAllActions();
        }

        @Override
        public TreeMap<String, String> getModifiedAuxProps(ModelHandle2 handle, boolean shared) {
            return handle.getModifiedAuxProps(shared);
        }

        @Override
        public boolean isConfigurationModified(ModelHandle2 handle) {
            return handle.modConfig;
        }

        @Override
        public boolean isModified(ModelHandle2 handle, ActionToGoalMapping mapp) {
            return handle.modifiedMappings.contains(mapp);
        }
    }    

    private ModelHandle2(POMModel model, MavenProject proj, Map<String, ActionToGoalMapping> mapp, List<Configuration> configs, Configuration active, List<String> allActions, MavenProjectPropsImpl auxProps) {
        project = proj;
        this.mappings = mapp;
        configurations = configs;
        this.active = active;
        auxiliaryProps = auxProps;
        this.model = model;
        this.allActions = allActions;
    }
    
    /**
     * pom.xml model, READ-ONLY
     * @return
     */
    public POMModel getPOMModel() {
        return model;
    }
    
    /**
     * the non changed (not-to-be-changed) instance of the complete project. 
     * NOT TO BE CHANGED.
     * @return 
     */
    public MavenProject getProject() {
        return project;
    }


    /**
     * get the value of Auxiliary property defined in the project,
     * however take only the content in nb-configurations.xml file into account, never
     * consider values from pom.xml here.
     * @param propertyName
     * @param shared
     * @return
     */
    public String getRawAuxiliaryProperty(String propertyName, boolean shared) {
        if (shared && transPropsShared.containsKey(propertyName)) {
            return transPropsShared.get(propertyName);
        }
        if (!shared && transPropsPrivate.containsKey(propertyName)) {
            return transPropsPrivate.get(propertyName);
        }
        return auxiliaryProps.get(propertyName, shared, false);
    }

    /**
     * set the value of Auxiliary property, will be written to nb-configurations.xml file
     * @param propertyName
     * @param shared
     * @param value
     */
    public void setRawAuxiliaryProperty(String propertyName, String value, boolean shared) {
        if (shared) {
            transPropsShared.put(propertyName, value);
        } else {
            transPropsPrivate.put(propertyName, value);
        }
    } 
    
    private TreeMap<String, String> getModifiedAuxProps(boolean shared) {
        if (shared) return transPropsShared;
        else return transPropsPrivate;
    }
    
    
    /**
     * action mapping model
     * @return 
     */
    public ActionToGoalMapping getActionMappings() {
        return mappings.get(M2Configuration.DEFAULT);
    }
    
    /**
     * action mapping model
     * @param config
     * @return 
     */
    public ActionToGoalMapping getActionMappings(Configuration config) {
        ActionToGoalMapping mapp = mappings.get(config.getId());
        if (mapp == null) {
            mapp = new ActionToGoalMapping();
            markAsModified(mapp);
            mappings.put(config.getId(), mapp);
        }
        return mapp;
    }
    
    /**
     * always after modifying the models, mark them as modified.
     * without the marking, the particular file will not be saved.
     */ 
    
    public void markAsModified(ActionToGoalMapping mapp) {
        modifiedMappings.add(mapp);
    }
    
    /**
     * always after modifying the models, mark them as modified.
     * without the marking, the particular file will not be saved.
     */ 
    public void markConfigurationsAsModified() {
        modConfig = true;
    }
    
    
    public List<Configuration> getConfigurations() {
        return configurations;
    }
    
    public void addConfiguration(Configuration config) {
        assert config != null;
        configurations.add(config);
        modConfig = true;
    }
    
    public void removeConfiguration(Configuration config) {
        assert config != null;
        configurations.remove(config);
        if (active == config) {
            active = configurations.size() > 0 ? configurations.get(0) : null;
        }
        modConfig = true;
    }
    
    public Configuration getActiveConfiguration() {
        return active;
    }
    public void setActiveConfiguration(Configuration conf) {
        assert conf != null;
        assert configurations.contains(conf);
        active = conf;
    }
    
    
    public void addPOMModification(ModelOperation<POMModel> operation) {
        if (!pomOperations.contains(operation)) {
            pomOperations.add(operation);
        }
    }

    public void removePOMModification(ModelOperation<POMModel> operation) {
        pomOperations.remove(operation);
    }

    public List<ModelOperation<POMModel>> getPOMOperations() {
        return new ArrayList<ModelOperation<POMModel>>(pomOperations);
    }
    
    public static Configuration createProfileConfiguration(String id) {
        return ModelHandle.createProfileConfiguration(id);
    }

    public static Configuration createProvidedConfiguration(String id) {
        ModelHandle.Configuration conf = new ModelHandle.Configuration();
        conf.setId(id);
        conf.setDefault(true);
        return conf;
    }
    
    public static Configuration createDefaultConfiguration() {
        return ModelHandle.createDefaultConfiguration();
    }
    
    public static Configuration createCustomConfiguration(String id) {
        return ModelHandle.createCustomConfiguration(id);
    }   
    
    /**
     * inserts the action definition in the right place based on matching action name.
     * replaces old defintion or appends at the end.
     * 
     * @param action
     * @param mapp
     */
    public static void setUserActionMapping(NetbeansActionMapping action, ActionToGoalMapping mapp) {
        List<NetbeansActionMapping> lst = mapp.getActions() != null ? mapp.getActions() : new ArrayList<NetbeansActionMapping>();
        Iterator<NetbeansActionMapping> it = lst.iterator();
        while (it.hasNext()) {
            NetbeansActionMapping act = it.next();
            if (act.getActionName().equals(action.getActionName())) {
                int index = lst.indexOf(act);
                it.remove();
                lst.add(index, action);
                return;
            }

        }
        //if not found, add to the end.
        lst.add(action);
    }

    /**
     * @since 2.19
     */
    public static @CheckForNull NetbeansActionMapping getDefaultMapping(String action, Project project) {
        return ActionToGoalUtils.getDefaultMapping(action, project);
    }

    /**
     * Load a particular action mapping.
     * @param action an action name
     * @param project a Maven project
     * @param config a configuration of that project
     * @return an action mapping model, or null
     * @since 2.19
     */
    public static @CheckForNull NetbeansActionMapping getMapping(String action, Project project, ProjectConfiguration config) {
        return ActionToGoalUtils.getActiveMapping(action, project, (M2Configuration) config);
    }

    /**
     * Store a particular action mapping.
     * @param mapp an action mapping model
     * @param project a Maven project
     * @param config a configuration of that project
     * @throws IOException in case of trouble
     * @since 2.19
     */
    public static void putMapping(NetbeansActionMapping mapp, Project project, ProjectConfiguration config) throws IOException {
        M2Configuration cfg = (M2Configuration) config;
        ActionToGoalMapping mapping;
        try {
            mapping = new NetbeansBuildActionXpp3Reader().read(new StringReader(cfg.getRawMappingsAsString()));
        } catch (XmlPullParserException x) {
            throw new IOException(x);
        }
        NetbeansActionMapping existing = null;
        for (NetbeansActionMapping m : mapping.getActions()) {
            if (m.getActionName().equals(mapp.getActionName())) {
                existing = m;
                break;
            }
        }
        if (existing != null) {
            mapping.getActions().set(mapping.getActions().indexOf(existing), mapp);
        } else {
            mapping.addAction(mapp);
        }
        CustomizerProviderImpl.writeNbActionsModel(project, mapping, M2Configuration.getFileNameExt(cfg.getId()));
    }    
    
    List<String> getAllActions() {
        return allActions;
    }
    
    
/**
     * a javabean wrapper for configurations within the project customizer
     * 
     */
    public static class Configuration {
        private String id;
        private String displayName;
        private boolean profileBased = false;
        private boolean defaul = false;

        private List<String> activatedProfiles;
        private Map<String, String> properties;

        private boolean shared = false;
        
        Configuration() {}

        public String getFileNameExt() {
            return M2Configuration.getFileNameExt(id);
        }

        /**
         * Identifies the default configuration.
         * @return true for just the default configuration
         * @since 2.148
         */
        public boolean isDefault() {
            return defaul && M2Configuration.DEFAULT.equals(id);
        }

        /**
         * True, if the configuration was provided but not default or profile.
         * @return true for provided configuration
         * @since 2.148
         */
        public boolean isProvided() {
            return defaul;
        }
        
        /**
         * Sets the display name of this configuration.
         * @param dn 
         * @since 2.148
         */
        public void setDisplayName(String dn) {
            this.displayName = dn;
        }

        public void setDefault(boolean def) {
            this.defaul = def;
        }
        
        public List<String> getActivatedProfiles() {
            return activatedProfiles;
        }

        public void setActivatedProfiles(List<String> activatedProfiles) {
            this.activatedProfiles = activatedProfiles;
        }
        

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }        

        @Messages({
            "DefaultConfig=<default config>",
            "# {0} - config ID", "ProfileConfig={0} (Profile)",
            "# {0} - config ID", "ProvidedConfig={0} (provided)",
            "# {0} - config ID", "# {1} - list of profiles", "CustomConfig1={0} (Profiles: {1})",
            "# {0} - config ID", "CustomConfig2={0}"
        })
        public String getDisplayName() {
            String n = displayName == null ? id : displayName;
            if (isDefault()) {
                return DefaultConfig();
            }
            if (isProfileBased()) {
                return ProfileConfig(n);
            }
            if (isProvided()) {
                return ProvidedConfig(n);
            }
            if (getActivatedProfiles() != null && getActivatedProfiles().size() > 0) {
                return CustomConfig1(n, Arrays.toString(getActivatedProfiles().toArray()));
            }
            return CustomConfig2(n);
        }

        public String getId() {
            return id;
        }

        public void setShared(boolean shared) {
            this.shared = shared;
        }
        
        public boolean isShared() {
            return shared;
        }

        void setId(String id) {
            this.id = id;
        }

        public boolean isProfileBased() {
            return profileBased;
        }

        void setProfileBased(boolean profileBased) {
            this.profileBased = profileBased;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
        
    }    
    
    
}
