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

package org.netbeans.modules.maven.configurations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.MavenConfiguration;
import static org.netbeans.modules.maven.configurations.Bundle.*;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionProfile;
import org.netbeans.modules.maven.execute.model.NetbeansActionReader;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Writer;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Represents a configuration, Properties defined by {@link MavenConfiguration} plus list of action descriptions
 * for that configuration. This class is used to represent:
 * <ul>
 * <li>default configurations, provided by Maven project.
 * <li>profile-based configurations
 * <li>user-customized configurations, that loads action configs from the project directory.
 * </ul>
 *
 * @author mkleint
 */
public class M2Configuration extends AbstractMavenActionsProvider implements MavenConfiguration, Comparable<M2Configuration> {
    private static final Logger LOG = Logger.getLogger(M2Configuration.class.getName());

    public static final String DEFAULT = "%%DEFAULT%%"; //NOI18N
    public static final String FILENAME = "nbactions.xml"; //NOI18N
    public static final String FILENAME_PREFIX = "nbactions-"; //NOI18N
    public static final String FILENAME_SUFFIX = ".xml"; //NOI18N
    
    public static M2Configuration createDefault(FileObject projectDirectory) {
        return new M2Configuration(DEFAULT, projectDirectory);
    }

    /**
     * True, if the M2Configuration comes from a customized project storage.
     */
    private boolean customized;
    private @NonNull final String id;
    private List<String> profiles;
    private final Map<String,String> properties = new HashMap<String,String>();
    private final FileObject projectDirectory;
    
    private final AtomicBoolean resetCache = new AtomicBoolean(false);
    private FileChangeListener listener = null;
    private String displayName;
    
    public M2Configuration(String id, FileObject projectDirectory) {
        assert id != null;
        this.id = id;
        this.projectDirectory = projectDirectory;
        profiles = Collections.<String>emptyList();
    }
    
     @Override       
     @Messages("TXT_DefaultConfig=<default config>")
     public String getDisplayName() {
        if (DEFAULT.equals(id)) {
            return TXT_DefaultConfig();
        }
        return displayName != null ? displayName : id;
    }
     
    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public @NonNull String getId() {
        return id;
    }
    
    public void setActivatedProfiles(List<String> profs) {
        profiles = profs;
    }
    
    @Override
    public List<String> getActivatedProfiles() {
        return profiles;
    }

    @Override
    public Map<String,String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> props) {
        if (props == null) {
            props = Collections.emptyMap();
        }
        properties.clear();
        properties.putAll(props);
    }
    
    public static String getFileNameExt(String id) {
        if (DEFAULT.equals(id)) {
            return FILENAME;
        }
        return FILENAME_PREFIX + id + FILENAME_SUFFIX;
    }

    public @Override boolean equals(Object obj) {
        return obj instanceof M2Configuration && id.equals(((M2Configuration) obj).id);
    }

    public @Override int hashCode() {
        return id.hashCode();
    }

    @Override public String toString() {
        return id;
    }

    public @Override int compareTo(M2Configuration o) {
        return id.compareTo(o.id);
    }

    public @Override InputStream getActionDefinitionStream() {
        return getActionDefinitionStream(id);
    }

    public boolean isCustomized() {
        return customized;
    }

    final InputStream getActionDefinitionStream(String forId) {
        checkListener();
        FileObject fo = projectDirectory.getFileObject(getFileNameExt(forId));
        customized = fo != null;
        resetCache.set(false);
        if (fo != null) {
            try {
                return fo.getInputStream();
            } catch (FileNotFoundException ex) {
                LOG.log(Level.WARNING, "Cannot read " + fo, ex); // NOI18N
            }
        } 
        return null;
    }
    
    private synchronized void checkListener() {
        final String fid = getFileNameExt(id);
        if (listener == null) {
            listener = new FileChangeAdapter() {

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    if (fid.equals(fe.getName() + "." + fe.getExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    if (fid.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    if (fid.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    if (fid.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }
                
            };
            projectDirectory.addFileChangeListener(FileUtil.weakFileChangeListener(listener, projectDirectory));
        }
    }
    
    private void resetCache() {
        resetCache.compareAndSet(false, true);
    }
    
   /**
     * get custom action maven mapping configuration
     * No replacements happen.
     * The instances returned is always a new copy, can be modified or reused.
     * Same method in NbGlobalActionGolaProvider 
     */
    public NetbeansActionMapping[] getCustomMappings() {
        NetbeansActionMapping[] fallbackActions = new NetbeansActionMapping[0];
        
        try {
            List<NetbeansActionMapping> toRet = new ArrayList<NetbeansActionMapping>();
            // just a converter for the To-Object reader..
            Reader read = performDynamicSubstitutions(Collections.<String,String>emptyMap(), getRawMappingsAsString());
            // basically doing a copy here..
            ActionToGoalMapping mapping = reader.read(read);    
            for (NetbeansActionMapping mapp : mapping.getActions()) {
                if (mapp.getActionName().startsWith("CUSTOM-")) { //NOI18N
                    toRet.add(mapp);
                }
            }
            return toRet.toArray(new NetbeansActionMapping[0]);
        } catch (XmlPullParserException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return fallbackActions;
    }
    
    @Override
    protected boolean reloadStream() {
        return resetCache.get();
    }

    @Override
    public ActionToGoalMapping getRawMappings() {
        if (originalMappings == null || reloadStream()) {
            Reader rdr = null;
            InputStream in = getActionDefinitionStream();
            try {
                if (in == null) {
                    in = getActionDefinitionStream(DEFAULT);
                    if (in != null) {
                        rdr = new InputStreamReader(in);
                        ActionToGoalMapping def = reader.read(rdr);
                        for (NetbeansActionProfile p : def.getProfiles()) {
                            if (id.equals(p.getId()) && p.getActions() != null) {
                                Map<String, Integer> posMap = new HashMap<>();
                                for (int i = 0; i < def.getActions().size(); i++) {
                                    posMap.put(def.getActions().get(i).getActionName(), i);
                                }
                                // merge in or override global actions:
                                for (NetbeansActionMapping am : p.getActions()) {
                                    String n = am.getActionName();
                                    Integer i = posMap.get(n);
                                    if (null == i) {
                                        def.getActions().add(am);
                                    } else {
                                        def.getActions().set(i, am);
                                    }
                                }
                                break;
                            }
                        }
                        originalMappings = def;
                    } else {
                        originalMappings = new ActionToGoalMapping();
                    }
                } else {
                    rdr = new InputStreamReader(in);
                    originalMappings = reader.read(rdr);
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Loading raw mappings", ex);
            } catch (XmlPullParserException ex) {
                LOG.log(Level.INFO, "Loading raw mappings", ex);
            } finally {
                if (rdr != null) {
                    try {
                        rdr.close();
                    } catch (IOException ex) {
                    }
                }
            }
            if (originalMappings == null) {
                originalMappings = new ActionToGoalMapping();
            }
        }
        return originalMappings;
    }

    public NetbeansActionMapping getProfileMappingForAction(
        String action, Project project, 
        Map<String,String> replaceMap, boolean[] fallback
    ) {
        NetbeansActionReader parsed = new NetbeansActionReader() {
            @Override
            protected String getRawMappingsAsString() {
                NetbeansBuildActionXpp3Writer writer = new NetbeansBuildActionXpp3Writer();
                StringWriter str = new StringWriter();
                InputStreamReader rdr = null;
                try {
                    // relay the action load to the possibly customized getRawMappings
                    ActionToGoalMapping map = getRawMappings();
                    writer.write(str, map);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Loading raw mappings", ex);
                } finally {
                    if(rdr != null) {
                        try { 
                            rdr.close(); 
                        } catch (IOException ex) { 
                        }
                    }
                }
                return str.toString();
            }

            @Override
            protected Reader performDynamicSubstitutions(Map<String, String> replaceMap, String in) throws IOException {
                return M2Configuration.this.performDynamicSubstitutions(replaceMap, in);
            }
        };
        NetbeansActionMapping ret = parsed.getMappingForAction(reader, LOG, action, null, project, id, replaceMap);
        if (ret == null) {
            boolean[] hasProfiles = { false };
            ret = parsed.getMappingForAction(reader, LOG, action, hasProfiles, project, null, replaceMap);
            if (fallback != null && ret != null && hasProfiles[0]) {
                fallback[0] = true;
            }
        }
        return ret;
    }

    public NetbeansActionMapping findMappingFor(
        Map<String, String> replaceMap, Project project, String actionName,
        boolean[] fallback
    ) {
        NetbeansActionMapping action = getProfileMappingForAction(
            actionName, project, replaceMap, fallback
        );
        if (action != null) {
            return action;
        }
        return new NetbeansActionReader() {
            @Override
            protected String getRawMappingsAsString() {
                return M2Configuration.this.getRawMappingsAsString();
            }

            @Override
            protected Reader performDynamicSubstitutions(Map<String, String> replaceMap, String in) throws IOException {
                return M2Configuration.this.performDynamicSubstitutions(replaceMap, in);
            }
        }.getMappingForAction(reader, LOG, actionName, null, project, null, replaceMap);
    }
}
