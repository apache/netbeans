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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.MavenConfiguration;
import static org.netbeans.modules.maven.configurations.Bundle.*;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionProfile;
import org.netbeans.modules.maven.execute.model.NetbeansActionReader;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Writer;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
public class M2Configuration extends AbstractMavenActionsProvider implements MavenConfiguration, Comparable<M2Configuration> {
    private static final Logger LOG = Logger.getLogger(M2Configuration.class.getName());

    public static final String DEFAULT = "%%DEFAULT%%"; //NOI18N
    
    public static M2Configuration createDefault(FileObject projectDirectory) {
        return new M2Configuration(DEFAULT, projectDirectory);
    }
    
    private @NonNull final String id;
    private List<String> profiles;
    public static final String FILENAME = "nbactions.xml"; //NOI18N
    public static final String FILENAME_PREFIX = "nbactions-"; //NOI18N
    public static final String FILENAME_SUFFIX = ".xml"; //NOI18N
    private final Map<String,String> properties = new HashMap<String,String>();
    private final FileObject projectDirectory;
    
    private final AtomicBoolean resetCache = new AtomicBoolean(false);
    private FileChangeListener listener = null;
    
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
        return id;
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
    
    final InputStream getActionDefinitionStream(String forId) {
        checkListener();
        FileObject fo = projectDirectory.getFileObject(getFileNameExt(forId));
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
            return toRet.toArray(new NetbeansActionMapping[toRet.size()]);
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
                            if (id.equals(p.getId())) {
                                ActionToGoalMapping m = new ActionToGoalMapping();
                                m.setActions(m.getActions());
                                m.setModelEncoding(m.getModelEncoding());
                                originalMappings = m;
                                break;
                            }
                        }

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
                    InputStream in = getActionDefinitionStream();
                    if (in == null) {
                      in = getActionDefinitionStream(DEFAULT);
                    }
                    if (in == null) {
                        return null;
                    }                    
                    rdr = new InputStreamReader(in);
                    ActionToGoalMapping map = reader.read(rdr);
                    writer.write(str, map);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Loading raw mappings", ex);
                } catch (XmlPullParserException ex) {
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
