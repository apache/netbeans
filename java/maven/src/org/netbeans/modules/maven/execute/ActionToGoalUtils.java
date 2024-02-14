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
package org.netbeans.modules.maven.execute;

import java.io.File;
import org.netbeans.modules.maven.api.execute.RunConfig;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Build;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Writer;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 *
 * @author mkleint
 */
@SuppressWarnings("StaticNonFinalUsedInInitialization")
public final class ActionToGoalUtils {

    private static final String FO_ATTR_CUSTOM_MAPP = "customActionMappings"; //NOI18N

    public static ContextAccessor ACCESSOR = null;
    static {
        // invokes static initializer of ExecutionResult.class
        // that will assign value to the ACCESSOR field above
        Class c = ExecutionContext.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            assert false : ex;
        }
        assert ACCESSOR != null;
    }


    public abstract static class ContextAccessor {

        public abstract ExecutionContext createContext(InputOutput inputoutput, ProgressHandle handle);
    }
    

    
    /** Creates a new instance of ActionToGoalUtils */
    private ActionToGoalUtils() {
    }

    /**
     * Finds all action providers for a project (usually differentiated by packaging type).
     * @param project a Maven project
     * @return a list of action providers, type-specific first, then general from global lookup
     * @since 2.50
     */
    public static @NonNull List<? extends MavenActionsProvider> actionProviders(@NonNull Project project) {
        List<MavenActionsProvider> providers = new ArrayList<MavenActionsProvider>();
        providers.addAll(project.getLookup().lookupAll(MavenActionsProvider.class));        
        providers.addAll(Lookup.getDefault().lookupAll(MavenActionsProvider.class));
        return providers;
    }

    public static RunConfig createRunConfig(String action, NbMavenProjectImpl project, Lookup lookup) {
        return createRunConfig(action, project, null, lookup);
    }
    
    public static RunConfig createRunConfig(String action, NbMavenProjectImpl project, ProjectConfiguration c, Lookup lookup) {
        M2ConfigProvider configs = project.getLookup().lookup(M2ConfigProvider.class);
        M2Configuration requested = (c instanceof M2Configuration) ? (M2Configuration)c : configs.getActiveConfiguration();
        RunConfig rc = requested.createConfigForDefaultAction(action, project, lookup);

// #241340 let's comment this out and see if it's actually used, it's a bit unsystemic (something gets executed that was never visible in UI)
//        
//        if (rc == null) {
//            // for build and rebuild check the pom for default goal and run that one..
//            if (ActionProvider.COMMAND_BUILD.equals(action) || ActionProvider.COMMAND_REBUILD.equals(action)) {
//                Build bld = project.getOriginalMavenProject().getBuild();
//                if (bld != null) {
//                    String goal = bld.getDefaultGoal();
//                    if (goal != null && goal.trim().length() > 0) {
//                        BeanRunConfig brc = new BeanRunConfig();
//                        brc.setExecutionDirectory(FileUtil.toFile(project.getProjectDirectory()));
//                        brc.setProject(project);
//                        StringTokenizer tok = new StringTokenizer(goal, " ", false); //NOI18N
//                        List<String> toRet = new ArrayList<String>();
//                        while (tok.hasMoreTokens()) {
//                            toRet.add(tok.nextToken());
//                        }
//                        if (ActionProvider.COMMAND_REBUILD.equals(action)) {
//                            toRet.add(0, "clean"); //NOI18N
//                            }
//                        brc.setGoals(toRet);
//                        brc.setExecutionName(ProjectUtils.getInformation(project).getDisplayName());
//                        brc.setActivatedProfiles(Collections.<String>emptyList());
//                        rc = brc;
//                    }
//                }
//            }
//        }
        if(rc==null){
            M2Configuration save = configs.setLocalConfiguration(requested);
            try {
                for (MavenActionsProvider add : actionProviders(project)) {
                            if (add.isActionEnable(action, project, lookup)) {
                                rc = add.createConfigForDefaultAction(action, project, lookup);
                                if (rc != null) {
                                    break;
                                }
                            }
                }
            } finally {
                configs.setLocalConfiguration(save);
            }
        }
        if (rc != null ) {
            if (rc instanceof ModelRunConfig && ((ModelRunConfig)rc).isFallback()) {
                return rc;
            }
            List<String> acts = new ArrayList<String>(); 
            acts.addAll(rc.getActivatedProfiles());
            acts.addAll(requested.getActivatedProfiles());
            rc.setActivatedProfiles(acts);
            Map<String, String> props = new HashMap<String, String>(rc.getProperties());
            props.putAll(requested.getProperties());
            rc.addProperties(props);
        }
        return rc;
    }
 
    private static class PackagingProvider {
        private String packaging;
        private final NbMavenProjectImpl project;

        public PackagingProvider(NbMavenProjectImpl project) {
            this.project = project;
        }
        
        String getPackaging() {
            if(packaging == null) {
                packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
            }
            return packaging;
        }
    }
    
    public static boolean isActionEnable(String action, NbMavenProjectImpl project, Lookup lookup) {
        return isActionEnable(action, project, null, lookup);
    }
    
    
    /**
     * Determines if the action mapping actually disables the action. Mapping that disables an action
     * has no goal - cannot be executed by Maven anyway.
     * 
     * @param am the checked action mapping
     * @return {@code true}, if the action is disabled.
     * @since 2.149
     */
    public static boolean isDisabledMapping(NetbeansActionMapping am) {
        return am == null || am.getGoals().isEmpty();
    }

    public static boolean isActionEnable(String action, NbMavenProjectImpl project, ProjectConfiguration c, Lookup lookup) {
        PackagingProvider packProv = new PackagingProvider(project);
        M2ConfigProvider configs = project.getLookup().lookup(M2ConfigProvider.class);
        M2Configuration active = configs.getActiveConfiguration();
        M2Configuration useConfiguration = (c instanceof M2Configuration) ? (M2Configuration)c : active;        
        NetbeansActionMapping m = findEnabledAction(useConfiguration, action, project, packProv, lookup);
        if(m != null) {
            return !isDisabledMapping(m);
        }
        //check fallback default config as well..
        m = findEnabledAction(configs.getDefaultConfig(), action, project, packProv, lookup);
        if(m != null) {
            return !isDisabledMapping(m);
        }
        if (ActionProvider.COMMAND_BUILD.equals(action) ||
                ActionProvider.COMMAND_REBUILD.equals(action)) {
            Build bld = project.getOriginalMavenProject().getBuild();
            if (bld != null) {
                String goal = bld.getDefaultGoal();
                if (goal != null && goal.trim().length() > 0) {
                    return true;
                }
            }
        }
        
        // MavenActionsProvider can query back for the active configuration.
        M2Configuration save = configs.setLocalConfiguration(useConfiguration);
        try {
            for (MavenActionsProvider add : actionProviders(project)) {
                m = findEnabledAction(add, action, project, packProv, lookup);
                if(m != null) {
                    return !isDisabledMapping(m);
                }
            }
        } finally {
            configs.setLocalConfiguration(save);
        }
        return false;
    }

    private static NetbeansActionMapping findEnabledAction(MavenActionsProvider activeConfiguration, String action, NbMavenProjectImpl project, PackagingProvider packProv, Lookup lookup) {
        NetbeansActionMapping mapping = activeConfiguration.getMappingForAction(action, project);
        if (activeConfiguration instanceof AbstractMavenActionsProvider) {
            boolean enabled = ((AbstractMavenActionsProvider)activeConfiguration).isActionEnable(action, packProv.getPackaging());
            if (enabled) {
                return mapping;
            }
        } else {
            if (activeConfiguration.isActionEnable(action, project, lookup)) {
                return mapping;
            }
        }
        return null;
    }

    public static NetbeansActionMapping getActiveMapping(String action, Project project, M2Configuration configuration) {
        NetbeansActionMapping na = null;
        if (configuration != null) {
            na = configuration.getMappingForAction(action, project);
            if (na == null) {
                na = configuration.getProfileMappingForAction(action, project, Collections.<String,String>emptyMap(), null);
            }
        }
        if (na == null) {
            na = getDefaultMapping(action, project);
        }
        return na;
    }

    public static NetbeansActionMapping[] getActiveCustomMappings(NbMavenProjectImpl project) {
        return getActiveCustomMappingsImpl(project, false);
    }
    
    public static NetbeansActionMapping[] getActiveCustomMappingsForFile(NbMavenProjectImpl project) {
        return getActiveCustomMappingsImpl(project, true);
    }
    
    private static NetbeansActionMapping[] getActiveCustomMappingsImpl(NbMavenProjectImpl project, boolean forFiles) {
        M2ConfigProvider configs = project.getLookup().lookup(M2ConfigProvider.class);
        List<NetbeansActionMapping> toRet = new ArrayList<NetbeansActionMapping>();
        List<String> names = new ArrayList<String>();
        // first add all project specific custom actions.
        for (NetbeansActionMapping map : configs.getActiveConfiguration().getCustomMappings()) {
            toRet.add(map);
            names.add(map.getActionName());
        }
        for (NetbeansActionMapping map : configs.getDefaultConfig().getCustomMappings()) {
            if (!names.contains(map.getActionName())) {
                toRet.add(map);
                names.add(map.getActionName());
            }
        }
        String prjPack = project.getProjectWatcher().getPackagingType();
        // check the global actions defined, include only if not the same name as project-specific one.
        for (NetbeansActionMapping map : Lookup.getDefault().lookup(NbGlobalActionGoalProvider.class).getCustomMappings()) {
            if (!names.contains(map.getActionName())
                    && (map.getPackagings().isEmpty()
                    || map.getPackagings().contains(prjPack.trim())
                    || map.getPackagings().contains("*") /* back compat only - all packagings is empty */)) { 
                toRet.add(map);
            }
        }
        Iterator<NetbeansActionMapping> it = toRet.iterator();
        while (it.hasNext()) {
            NetbeansActionMapping map = it.next();
            boolean hasFiles = false;
            LBL : for (Map.Entry<String, String> ent : map.getProperties().entrySet()) {
                for (String s : DefaultReplaceTokenProvider.fileBasedProperties) {
                    hasFiles = ent.getValue().contains(s);
                    if (hasFiles) {
                        break LBL;
                    }
                }
            }
            if (forFiles != hasFiles) {
                it.remove();
            }
            
        }
        return toRet.toArray(new NetbeansActionMapping[0]);
    }
        

    public static NetbeansActionMapping getDefaultMapping(String action, Project project) {
        NetbeansActionMapping na = null;
        for (MavenActionsProvider add : actionProviders(project)) {
            na = add.getMappingForAction(action, project);
            if (na != null) {
                break;
            }
        }
        return na;
    }

    /**
     * read the action mappings from the fileobject attribute "customActionMappings"
     * @param fo should be the project's root directory fileobject
     *
     */
    public static ActionToGoalMapping readMappingsFromFileAttributes(FileObject fo) {
        String string = (String) fo.getAttribute(FO_ATTR_CUSTOM_MAPP);
        ActionToGoalMapping mapp = null;
        if (string != null) {
            NetbeansBuildActionXpp3Reader reader = new NetbeansBuildActionXpp3Reader();
            try {
                mapp = reader.read(new StringReader(string));
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (XmlPullParserException ex) {
                ex.printStackTrace();
            }
        }
        if (mapp == null) {
            mapp = new ActionToGoalMapping();
        }
        return mapp;
    }

    /**
     * writes the action mappings to the fileobject attribute "customActionMappings"
     * @param fo should be the project's root directory fileobject
     *
     */
    public static void writeMappingsToFileAttributes(FileObject fo, ActionToGoalMapping mapp) {
        NetbeansBuildActionXpp3Writer writer = new NetbeansBuildActionXpp3Writer();
        StringWriter string = new StringWriter();
        boolean error = false;
        try {
            writer.write(string, mapp);
        } catch (IOException ex) {
            ex.printStackTrace();
            error = true;
        }
        if (!error) {
            try {
                fo.setAttribute(FO_ATTR_CUSTOM_MAPP, string.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Here for compatibility with old action mappings setting basedir, but unnecessary when reactor mode defined.
     */
    public static File resolveProjectExecutionBasedir(NetbeansActionMapping mapp, Project prj) {
        File base = FileUtil.toFile(prj.getProjectDirectory());
        if (mapp.getBasedir() != null) {
            base = FileUtilities.resolveFilePath(base, mapp.getBasedir());
        }
        return base;
    }
}

