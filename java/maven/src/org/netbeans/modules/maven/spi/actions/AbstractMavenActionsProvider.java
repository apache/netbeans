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
package org.netbeans.modules.maven.spi.actions;


import java.io.File;
import org.netbeans.modules.maven.api.execute.RunConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.ActionNameProvider;
import org.netbeans.modules.maven.execute.DefaultActionGoalProvider;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionProfile;
import org.netbeans.modules.maven.execute.model.NetbeansActionReader;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Writer;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * a default implementation of MavenActionsProvider, a fallback when nothing is
 * user configured or overridden by a more specialized provider.
 * @author mkleint
 */
public abstract class AbstractMavenActionsProvider implements MavenActionsProvider {
    private static final Logger LOG = Logger.getLogger(AbstractMavenActionsProvider.class.getName());

    protected ActionToGoalMapping originalMappings;
    protected NetbeansBuildActionXpp3Reader reader = new NetbeansBuildActionXpp3Reader();
    private final NetbeansBuildActionXpp3Writer writer = new NetbeansBuildActionXpp3Writer();

    /** Creates a new instance of DefaultActionProvider */
    public AbstractMavenActionsProvider() {
    }

    /**
     * just gets the array of FOs from lookup.
     * @param lookup
     * @return 
     */
    protected static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<FileObject>();
        Iterator<? extends DataObject> it = lookup.lookupAll(DataObject.class).iterator();
        while (it.hasNext()) {
            DataObject d = it.next();
            FileObject f = d.getPrimaryFile();
            files.add(f);
        }
        Collection<? extends SingleMethod> methods = lookup.lookupAll(SingleMethod.class);
        if (methods.size() == 1) {
            SingleMethod method = methods.iterator().next();
            files.add(method.getFile());
        }

        if (files.isEmpty()) {
            files.addAll(lookup.lookupAll(FileObject.class));
        }
        
        return files.toArray(new FileObject[0]);
    }

    @Override
    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        String prjPack = mp.getPackagingType();
        return isActionEnable(action, prjPack);
    }
    
    /**
     * Performance convenience to 
     * {@link  #isActionEnable(java.lang.String, org.netbeans.api.project.Project, org.openide.util.Lookup)}. 
     * Avoids redundant and potentially costly packaging retrieval. 
     * 
     * @param action
     * @param prjPack
     * @return 
     */
    public final boolean isActionEnable(String action, String prjPack) {
        ActionToGoalMapping rawMappings = getRawMappings();
        Iterator<NetbeansActionMapping> it = rawMappings.getActions().iterator();
        while (it.hasNext()) {
            NetbeansActionMapping elem = it.next();
            if (action.equals(elem.getActionName()) &&
                    (elem.getPackagings().isEmpty() ||
                    elem.getPackagings().contains(prjPack.trim()) ||
                    elem.getPackagings().contains("*"))) {//NOI18N
                return true;
            }
        }

        return false;
    }

    @Override
    public final RunConfig createConfigForDefaultAction(String actionName, Project project, Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        @SuppressWarnings("unchecked")
        Map<String, String> replaceMap = lookup.lookup(Map.class);
        if (replaceMap == null) { //#159698
            replaceMap = new HashMap<String, String>();
            Logger.getLogger(AbstractMavenActionsProvider.class.getName()).log(Level.FINE, "Missing replace tokens map when executing maven build. Could lead to problems with execution. See issue #159698 for details.", new Exception()); //NOI18N
        }
        FileObject fo = null;
        if (fos.length > 0) {
            fo = fos[0];
        }
//        if (group != null && MavenSourcesImpl.NAME_TESTSOURCE.equals(group.getName()) &&
//                ActionProvider.COMMAND_RUN_SINGLE.equals(actionName)) {
//            //TODO how to allow running main() in tests?
//            actionName = ActionProvider.COMMAND_TEST_SINGLE;
//        }
//        if (group != null && MavenSourcesImpl.NAME_TESTSOURCE.equals(group.getName()) &&
//                ActionProvider.COMMAND_DEBUG_SINGLE.equals(actionName)) {
//            //TODO how to allow running main() in tests?
//            actionName = ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
//        }
        return mapGoalsToAction(project, actionName, replaceMap, fo, lookup);
    }
  
    public ActionToGoalMapping getRawMappings() {
        if (originalMappings == null || reloadStream()) {
            InputStream in = getActionDefinitionStream();
            if (in == null) {
                originalMappings = new ActionToGoalMapping();
            } else {
                Reader rdr = null;
                try {
                    rdr = new InputStreamReader(in);
                    originalMappings = reader.read(rdr);
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Loading raw mappings", ex);
                    originalMappings = new ActionToGoalMapping();
                } catch (XmlPullParserException ex) {
                    LOG.log(Level.INFO, "Loading raw mappings", ex);
                    originalMappings = new ActionToGoalMapping();
                } finally {
                    if (rdr != null) {
                        try {
                            rdr.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }
        return originalMappings;
    }

    public String getRawMappingsAsString() {
        StringWriter str = new StringWriter();
        try {
            writer.write(str, getRawMappings());
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Loading raw mappings", ex);
        }
        return str.toString();
    }

    /**
     * default implementation will look in the content of the
     * @return
     */
    @Override
    public Set<String> getSupportedDefaultActions() {
        HashSet<String> toRet = new HashSet<String>();
        ActionToGoalMapping raw = getRawMappings();
        for (NetbeansActionMapping nb : raw.getActions()) {
            String name = nb.getActionName();
            if (name != null && !name.startsWith("CUSTOM-")) {
                toRet.add(name);
            }
        }
        return toRet;
    }
    

    /**
     * override in children that are listening on changes of model and need refreshing..
     * @return 
     */
    protected boolean reloadStream() {
        return false;
    }

    /**
     * get a action to maven mapping configuration for the given action.
     * No replacements happen.
     * The instance returned is always a new copy, can be modified or reused.
     * @param actionName
     * @param project
     */
    @Override
    public NetbeansActionMapping getMappingForAction(String actionName, Project project) {
        return new NetbeansActionReader() {
            @Override
            protected String getRawMappingsAsString() {
                return AbstractMavenActionsProvider.this.getRawMappingsAsString();
            }

            @Override
            protected Reader performDynamicSubstitutions(Map<String, String> replaceMap, String in) throws IOException {
                return AbstractMavenActionsProvider.this.performDynamicSubstitutions(replaceMap, in);
            }
        }.getMappingForAction(reader, LOG, actionName, null, project, null, Collections.<String, String>emptyMap());
    }

    /**
     * content of the input stream shall be the xml with action definitions
     * @return 
     */
    protected abstract InputStream getActionDefinitionStream();

    private RunConfig mapGoalsToAction(Project project, String actionName, Map<String, String> replaceMap, FileObject selectedFile, Lookup lookup) {
        try {
            boolean[] fallback = { false };
            NetbeansActionMapping action;
            if (this instanceof M2Configuration) {
                action = ((M2Configuration)this).findMappingFor(replaceMap, project, actionName, fallback);
            } else {
                action = findMapAction(replaceMap, project, actionName);
            }
            if (action != null) {
                ModelRunConfig mrc = new ModelRunConfig(project, action, actionName, selectedFile, lookup, fallback[0]);
                if (replaceMap.containsKey(DefaultReplaceTokenProvider.METHOD_NAME)) {
                    //sort of hack to push the method name through the current apis..
                    mrc.setProperty(DefaultReplaceTokenProvider.METHOD_NAME, replaceMap.get(DefaultReplaceTokenProvider.METHOD_NAME));
                }
                return mrc;
            }
        } catch (XmlPullParserException ex) {
            LOG.log(Level.INFO, "Parsing action mapping", ex);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Parsing action mapping", ex);
        }
        return null;
    }

    private NetbeansActionMapping findMapAction(Map<String, String> replaceMap, Project project, String actionName) throws XmlPullParserException, IOException {
        // TODO need some caching really badly here..
        Reader read = performDynamicSubstitutions(replaceMap, getRawMappingsAsString());
        ActionToGoalMapping mapping = reader.read(read);
        Iterator<NetbeansActionMapping> it = mapping.getActions().iterator();
        NetbeansActionMapping action = null;
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        String prjPack = mp.getPackagingType();
        while (it.hasNext()) {
            NetbeansActionMapping elem = it.next();
            if (actionName.equals(elem.getActionName()) &&
                    (elem.getPackagings().contains(prjPack.trim()) ||
                    elem.getPackagings().contains("*") || elem.getPackagings().isEmpty())) {//NOI18N
                action = elem;
                break;
            }
        }
        return action;
    }

    /**
     * takes the input stream and a map, and for each occurence of {@code ${<mapKey>} }, replaces it with map entry value..
     * @param replaceMap
     * @param in
     * @return 
     * @throws java.io.IOException
     */
    protected Reader performDynamicSubstitutions(Map<String,String> replaceMap, String in) throws IOException {
        return new StringReader(dynamicSubstitutions(replaceMap, in));
    }
    public static String dynamicSubstitutions(Map<String,String> replaceMap, String in) {
        StringBuilder buf = new StringBuilder(in);
        Iterator<Map.Entry<String, String>> it = replaceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> elem = it.next();
            String replaceItem = "${" + elem.getKey() + "}";//NOI18N
            int index = buf.indexOf(replaceItem);
            while (index > -1) {
                String newItem = elem.getValue();
                if (newItem == null) {
//                    System.out.println("no value for key=" + replaceItem);
                }
                newItem = newItem == null ? "" : newItem;//NOI18N
                buf.replace(index, index + replaceItem.length(), newItem);
                index = buf.indexOf(replaceItem);
            }
        }
        return buf.toString();
    }
    
    /**
     * Constructs a {@link MavenActionsProvider} from declarative action descriptions. The 
     * {@code resourceURL} parameter identifies a {@code nbactions.xml}-format resource which
     * can add/define actions in the default configuration. It can also <b>contribute</b>
     * configurations (profiles) using the nested {@code &lt;profile>} element.
     * <p>
     * Actions in the contributed configuration profiles are merged; multiple {@link MavenActionsProvider}s
     * created by {@link #fromNbActions(org.netbeans.api.project.Project, java.net.URL)} can supply their
     * actions. The first definition of a given {@code actionName} counts, subsequent definitions from 
     * {@link MavenActionsProvider} further in the project's Lookup are ignored.
     * <p>
     * Note: at the moment it is not possible to build the <b>default configuration</b> actions 
     * from multiple {@link MavenActionsProvider}s. 
     * <p>
     * <b>This is a part of Maven module's friend API.</b> If possible, use se  
     * <a href="@org-netbeans-api-maven@/org/netbeans/api/maven/MavenActions.html">MavenActions</a> to register
     * actions declaratively, using module layers.
     * 
     * @param mavenProject the maven project. 
     * @param resourceURL action definitions.
     * @return new {@link MavenActionsProvider} instance.
     * @since 2.148
     */
    public static MavenActionsProvider fromNbActions(Project mavenProject, URL resourceURL) {
        NbMavenProjectImpl mvn = mavenProject.getLookup().lookup(NbMavenProjectImpl.class);
        if (mvn == null) {
            throw new IllegalArgumentException("Not a maven proejct: " + mavenProject);
        }
        return new ResourceConfigAwareProvider(mvn, resourceURL);
    }
    
    /**
     * This provider wraps a custom resource, and provides actions from it.
     * It cooperates with {@link M2ConfigProvider#getActiveConfiguration()} and provides actions
     * for just the active one. For {@link M2ConfigProvider#DEFAULT} configuration, the returned
     * mapping contains list of contributed configurations (profiles) if defined.
     */
    static class ResourceConfigAwareProvider extends AbstractMavenActionsProvider implements ActionNameProvider {
        /**
         * The backing resource
         */
        private final URL resource;
        
        /**
         * The target project.
         */
        private final Project project;
        
        /**
         * Quick lookup for profiles.
         */
        private final Map<String, ActionToGoalMapping> profileMap = new HashMap<>();
        
        /**
         * Project's config provider, lazy initialized to avoid Lookup calls inside
         * constructor called from within beforeLookup.
         */
        private M2ConfigProvider cfg;
        
        private ResourceBundle resourceBundle;
        
        ResourceConfigAwareProvider(Project prj, URL resource) {
            this.resource = resource;
            this.project = prj;
            this.reader = DefaultActionGoalProvider.createI18nReader(getTranslations());
        }

        @Override
        public ResourceBundle getTranslations() {
            if (resourceBundle == null) {
                String p = null;
                
                if (resource.getProtocol().equals("nbres")) { // NOI18N
                    p = resource.getPath();
                } else {
                    // This branch is mainly active during tests, as tests and code is expanded on the classpath
                    String[] cp = System.getProperty("java.class.path", "").split(File.pathSeparator); // NOI18N
                    String rs = resource.toString();
                    for (String pref : cp) {
                        String s = new File(pref).toURI().toString();
                        if (rs.startsWith(s)) {
                            String frag = rs.substring(s.length());
                            if (frag.startsWith("!")) {
                                frag = frag.substring(1);
                            }
                            p = frag;
                            break;
                        }
                    }
                }
                if (p != null) {
                    int slash = p.lastIndexOf('/');
                    p = p.substring(0, slash + 1) + "Bundle"; // NOI18N
                    try {
                        resourceBundle = NbBundle.getBundle(p, Locale.getDefault(), Lookup.getDefault().lookup(ClassLoader.class));
                    } catch (MissingResourceException ex) {
                    }
                }
                if (resourceBundle == null) {
                    // fallback
                    resourceBundle = NbBundle.getBundle(M2Configuration.class);
                }
            }
            return resourceBundle;
        }
        
        private M2ConfigProvider cfg() {
            if (this.cfg != null) {
                return this.cfg;
            }
            return this.cfg = project.getLookup().lookup(M2ConfigProvider.class);
        }
        
        @Override
        public ActionToGoalMapping getRawMappings() {
            ActionToGoalMapping  allMappings = super.getRawMappings();
            String id = cfg().getActiveConfiguration().getId();
            if (M2Configuration.DEFAULT.equals(id) || allMappings.getProfiles() == null || allMappings.getProfiles().isEmpty()) {
                return allMappings;
            }
            synchronized (this) {
                ActionToGoalMapping pm = profileMap.get(id);
                if (pm != null) {
                    return pm;
                }
                if (!profileMap.isEmpty()) {
                    return allMappings;
                }
                
                for (NetbeansActionProfile p : allMappings.getProfiles()) {
                    Set<String> overridenIds = new HashSet<>();
                    ActionToGoalMapping m = new ActionToGoalMapping();
                    for (NetbeansActionMapping am : p.getActions()) {
                        overridenIds.add(am.getActionName());
                        m.addAction(am);
                    }
                    for (NetbeansActionMapping am : allMappings.getActions()) {
                        if (!overridenIds.contains(am.getActionName())) {
                            m.addAction(am);
                        }
                    }
                    profileMap.put(p.getId(), m);
                }
                
                pm = profileMap.get(id);
                return pm == null ? allMappings : pm;
            }
        }
        
        @Override
        protected InputStream getActionDefinitionStream() {
            try {
                return resource.openStream();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }
}
