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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.ProjectConfiguration;

/**
 * ModelHandle instance is passed down to customizer panel providers in the context lookup.
 * 
 * @author Milos Kleint (mkleint@codehaus.org)
 */
public final class ModelHandle {
    public static final String PANEL_RUN = ModelHandle2.PANEL_RUN;
    public static final String PANEL_BASIC = ModelHandle2.PANEL_BASIC;
    public static final String PANEL_CONFIGURATION = ModelHandle2.PANEL_CONFIGURATION;
    public static final String PANEL_MAPPING = ModelHandle2.PANEL_MAPPING;
    public static final String PANEL_LIBRARIES = ModelHandle2.PANEL_LIBRARIES;
    public static final String PANEL_SOURCES = ModelHandle2.PANEL_SOURCES;
    public static final String PANEL_COMPILE = ModelHandle2.PANEL_COMPILE;
    
    private final MavenProjectPropsImpl auxiliaryProps;
    private final POMModel model;
    private final MavenProject project;
    private final Map<String, ActionToGoalMapping> mappings;
    private final Map<ActionToGoalMapping, Boolean> modMappings;
    private List<Configuration> configurations;
    private boolean modModel = false;
    private boolean modConfig = false;
    private Configuration active;
    
    static {
        AccessorImpl impl = new AccessorImpl();
        impl.assign();
    }
    
    
    
    static class AccessorImpl extends CustomizerProviderImpl.ModelAccessor {

        @Override
        public void setConfigurationId(Configuration cfg, String id) {
            cfg.setId(id);
        }
        
         public @Override ModelHandle createHandle(POMModel model,
                                        MavenProject proj, 
                                        Map<String, ActionToGoalMapping> mapp, 
                                        List<ModelHandle.Configuration> configs,
                                        ModelHandle.Configuration active,
                                        MavenProjectPropsImpl auxProps) {
            return new ModelHandle(model, proj, mapp, configs, active, auxProps);
        }
        
         public void assign() {
             if (CustomizerProviderImpl.ACCESSOR == null) {
                 CustomizerProviderImpl.ACCESSOR = this;
             }
         }
    
    }
    
    private ModelHandle(POMModel mdl, MavenProject proj,
                        Map<String, ActionToGoalMapping> mappings,
                        List<Configuration> configs, Configuration active,
                        MavenProjectPropsImpl auxProps) {
        model = mdl;
        project = proj;
        this.mappings = mappings;
        this.modMappings = new HashMap<ActionToGoalMapping, Boolean>();
        for (ActionToGoalMapping map : mappings.values()) {
            modMappings.put(map, Boolean.FALSE);
        }
        configurations = configs;
        this.active = active;
        auxiliaryProps = auxProps;
    }

    /**
     * pom.xml model
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
        return auxiliaryProps.get(propertyName, shared, false);
    }

    /**
     * set the value of Auxiliary property, will be written to nb-configurations.xml file
     * @param propertyName
     * @param shared
     * @param value
     */
    public void setRawAuxiliaryProperty(String propertyName, String value, boolean shared) {
        auxiliaryProps.put(propertyName, value, shared);
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
            mappings.put(config.getId(), mapp);
            modMappings.put(mapp, Boolean.FALSE);
        }
        return mapp;
    }
    
    /**
     * inserts the action definition in the right place based on matching action name.
     * replaces old defintion or appends at the end.
     * 
     * @param action
     * @param mapp
     */
    public static void setUserActionMapping(NetbeansActionMapping action, ActionToGoalMapping mapp) {
        ModelHandle2.setUserActionMapping(action, mapp);
            }

    /**
     * @since 2.19
     */
    public static @CheckForNull NetbeansActionMapping getDefaultMapping(String action, Project project) {
        return ModelHandle2.getDefaultMapping(action, project);
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
        return ModelHandle2.getMapping(action, project, config);
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
        ModelHandle2.putMapping(mapp, project, config);
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
    
    public boolean isModified(Object obj) {
        if (modMappings.containsKey(obj)) {
            return modMappings.get(obj); 
        } else if (obj == model) {
            return modModel;
        } else if (obj == configurations || configurations.contains(obj)) {
            return modConfig;
        }
        return true;
    }
    
    /**
     * always after modifying the models, mark them as modified.
     * without the marking, the particular file will not be saved.
     * @param obj either getPOMModel() or getActionMappings()
     */ 
    public void markAsModified(Object obj) {
        if (modMappings.containsKey(obj)) {
            modMappings.put((ActionToGoalMapping)obj, Boolean.TRUE);
        } else if (obj == model) {
            modModel = true;
        } else if (obj == configurations || configurations.contains(obj)) {
            modConfig = true;
        } else {
            assert false : "Unexpected parameter type " + obj.getClass().getName(); //NOI18N
        }
    }

    
    public static Configuration createProfileConfiguration(String id) {
        Configuration conf = new Configuration();
        conf.setId(id);
        conf.setProfileBased(true);
        List<String> l = new ArrayList<>();
        if (!id.equals(M2Configuration.DEFAULT)) {
            l.add(id);
        }
        conf.setActivatedProfiles(Collections.singletonList(id));
        conf.setProperties(new HashMap<>());
        return conf;
    }
    
    public static Configuration createDefaultConfiguration() {
        Configuration conf = new Configuration();
        conf.setId(M2Configuration.DEFAULT);
        conf.setDefault(true);
        conf.setActivatedProfiles(new ArrayList<>());
        conf.setProperties(new HashMap<>());
        return conf;
    }
    
    public static Configuration createCustomConfiguration(String id) {
        Configuration conf = new Configuration();
        conf.setId(id);
        return conf;
    }
    
    /**
     * a javabean wrapper for configurations within the project customizer
     * 
     */
    public static class Configuration extends ModelHandle2.Configuration {
    }
}
