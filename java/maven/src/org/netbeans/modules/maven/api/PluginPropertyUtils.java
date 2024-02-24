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

package org.netbeans.modules.maven.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Examines a POM for configuration of plugins.
 */
public class PluginPropertyUtils {
    private static final String CONTEXT_EXPRESSION_EVALUATOR = "NB_EVALUATOR";

    private PluginPropertyUtils() {
    }

    private static final List<String> LIFECYCLE_PLUGINS = Arrays.asList(
                Constants.PLUGIN_COMPILER,
                Constants.PLUGIN_SUREFIRE,
                Constants.PLUGIN_EAR,
                Constants.PLUGIN_JAR,
                Constants.PLUGIN_WAR,
                Constants.PLUGIN_RESOURCES
            );
    
    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * @deprecated use the variant with expressionProperty value
     */
    @Deprecated
    public static @CheckForNull String getPluginProperty(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal) {
       return getPluginProperty(prj, groupId, artifactId, parameter, goal, null);
    }
    
    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * @param parameter the name of the plugin parameter to look for
     * @param expressionProperty expression property that once defined (and plugin configuration is omited) is used. only value, no ${}
     */
    public static @CheckForNull String getPluginProperty(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal, @NullAllowed String expressionProperty) {
        NbMavenProjectImpl project = prj instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)prj : prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyImpl(project.getOriginalMavenProject(), groupId, artifactId, simpleProperty(parameter), goal, simpleDefaultProperty(expressionProperty));
    }    

    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @deprecated use the variant with expressionProperty value
     */
    @Deprecated
    public static @CheckForNull String getPluginProperty(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal) {
        return getPluginProperty(prj, groupId, artifactId, parameter, goal, null);
    }
    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * @param parameter the name of the plugin parameter to look for
     * @param expressionProperty expression property that once defined (and plugin configuration is omited) is used. only value, no ${}
     */
    public static @CheckForNull String getPluginProperty(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal, @NullAllowed String expressionProperty) {
        return getPluginPropertyImpl(prj, groupId, artifactId, simpleProperty(parameter), goal, simpleDefaultProperty(expressionProperty));
    }

    private static @CheckForNull <T> T getPluginPropertyImpl(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull ConfigurationBuilder<T> builder, @NullAllowed String goal, @NullAllowed ExternalDefaultBuilder<T> external) {
        T toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Plugin plug : prj.getBuildPlugins()) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                for (PluginExecution exe : getPluginExecutions(plug, goal)) {
                    toRet = builder.build((Xpp3Dom)exe.getConfiguration(), DUMMY_EVALUATOR);
                    if (toRet != null) {
                        break;
                    }
                }
                if (toRet == null) {
                    toRet = builder.build((Xpp3Dom)plug.getConfiguration(), DUMMY_EVALUATOR);
                }
            }
        }
        if (toRet == null && 
                //TODO - the plugin configuration probably applies to 
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                LIFECYCLE_PLUGINS.contains(artifactId)) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Plugin plug : prj.getPluginManagement().getPlugins()) {
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = builder.build((Xpp3Dom)plug.getConfiguration(), DUMMY_EVALUATOR);
                        break;
                    }
                }
            }
        }
        if (toRet == null && external != null) {
            toRet = external.externalValue(prj);
        }
        return toRet;
    }
    
    /**
     * tries to figure out if the property of the given plugin is customized in
     * the current project and returns it's value if so, otherwise null
     *
     * @since 2.70
     */
    public static <T> T getPluginPropertyBuildable(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, 
            @NullAllowed String goal, @NonNull ConfigurationBuilder<T> builder) {
        NbMavenProjectImpl project = prj instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)prj : prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyImpl(project.getOriginalMavenProject(), groupId, artifactId, builder, goal, null);
    }
    
    /**
     * tries to figure out if the property of the given plugin is customized in
     * the current project and returns it's value if so, otherwise null
     *
     * @since 2.102
     */
    public static <T> T getPluginPropertyBuildable(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, 
            @NullAllowed String goal, @NonNull ConfigurationBuilder<T> builder) {
        return getPluginPropertyImpl(prj, groupId, artifactId, builder, goal, null);
    }

    /**
     * builder responsible for converting Xpp3Dom from Maven to custom object trees.
     * Since 2.105 ExpressionEvaluator parameter is no longer to be used. The dummy evaluator passed in will just return the original value. Consider it deprecated.
     * @param <T> 
     * @since 2.70
     */
    public static interface ConfigurationBuilder<T> {
        T build(Xpp3Dom configRoot, ExpressionEvaluator eval);
    }
    
    private static ExpressionEvaluator DUMMY_EVALUATOR = new ExpressionEvaluator() {

        @Override
        public Object evaluate(String string) throws ExpressionEvaluationException {
            return string;
        }

        @Override
        public File alignToBaseDirectory(File file) {
            return file;
        }
    };

    
    private static interface ExternalDefaultBuilder<T> {
        T externalValue(MavenProject prj);
    }

    /**
     * tries to figure out if the property of the given report plugin is customized in the
     * current project and returns it's value if so, otherwise null
     */
    public static @CheckForNull String getReportPluginProperty(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String property, @NullAllowed String report) {
        NbMavenProjectImpl project = prj instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)prj : prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getReportPluginPropertyImpl(project.getOriginalMavenProject(), groupId, artifactId, simpleProperty(property), report);
    }

    /**
     * tries to figure out if the property of the given report plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     */
    public static @CheckForNull String getReportPluginProperty(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String property, @NullAllowed String report) {
        return getReportPluginPropertyImpl(prj, groupId, artifactId, simpleProperty(property), report);
    }

    private static @CheckForNull <T> T  getReportPluginPropertyImpl(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull ConfigurationBuilder<T> builder, @NullAllowed String report) {
        T toRet = null;
        for (ReportPlugin plug : getEffectiveReportPlugins(prj)) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                if (plug.getReportSets() != null) {
                    for (ReportSet exe : plug.getReportSets()) {
                        if (exe.getReports().contains(report)) {
                            toRet = builder.build((Xpp3Dom)exe.getConfiguration(), DUMMY_EVALUATOR);
                            if (toRet != null) {
                                break;
                            }
                        }
                    }
                }
                if (toRet == null) {
                    toRet = builder.build((Xpp3Dom)plug.getConfiguration(), DUMMY_EVALUATOR);
                }
            }
        }
        return toRet;
    }


    /**
     * tries to figure out if the a plugin is defined in the project
     * and return the version declared.
     * @return version string or null
     */
    public static @CheckForNull String getPluginVersion(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId) {
        String toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Plugin plug : prj.getBuildPlugins()) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                toRet = plug.getVersion();
            }
        }
        if (toRet == null &&
                //TODO - the plugin configuration probably applies to
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                LIFECYCLE_PLUGINS.contains(artifactId)) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Plugin plug : prj.getPluginManagement().getPlugins()) {
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = plug.getVersion();
                        break;
                    }
                }
            }
        }
        return toRet;
    }

    /**
     * Like {@link #getPluginVersion} but for report plugins.
     * @since 2.32
     */
    public static @CheckForNull String getReportPluginVersion(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId) {
        for (ReportPlugin plug : getEffectiveReportPlugins(prj)) {
            if (groupId.equals(plug.getGroupId()) && artifactId.equals(plug.getArtifactId())) {
                return plug.getVersion();
            }
        }
        return null;
    }
    
    static @NonNull ConfigurationBuilder<String> simpleProperty(final @NonNull String property) {
        return new ConfigurationBuilder<String>() {

            @Override
            public String build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
                if (configRoot != null) {
                    Xpp3Dom source = configRoot.getChild(property);
                    if (source != null) {
                        String value = source.getValue();
                        if (value == null) {
                            return null;
                        }
                        return value.trim();
                    }
                }
                return null;
            }
        };
    }
    
    static @NonNull ExternalDefaultBuilder<String> simpleDefaultProperty(final @NullAllowed String expressionProperty) {
        return new ExternalDefaultBuilder<String>() {
            @Override
            public String externalValue(MavenProject prj) {
                if (expressionProperty == null) {
                    return null;
                }
                Properties prop = prj.getProperties();
                if (prop != null) {
                    return prop.getProperty(expressionProperty);
                }
                return null;
            }
        };
    }
    
    static @NonNull ConfigurationBuilder<String[]> listProperty(final @NonNull String multiProperty, final @NonNull String singleProperty) {
        return new ConfigurationBuilder<String[]>() {
            @Override
            public String[] build(Xpp3Dom conf, ExpressionEvaluator eval) {
                if (conf != null) {
                    Xpp3Dom dom = conf; // MNG-4862
                    Xpp3Dom source = dom.getChild(multiProperty);
                    if (source != null) {
                        List<String> toRet = new ArrayList<String>();
                        Xpp3Dom[] childs = source.getChildren(singleProperty);
                        for (Xpp3Dom ch : childs) {
                            String chvalue = ch.getValue() == null ? "" : ch.getValue().trim();  //NOI18N
                            toRet.add(chvalue);  //NOI18N
                        }
                        return toRet.toArray(new String[0]);
                    }
                }
                return null;
            }
        };
    } 
    
    static @NonNull ConfigurationBuilder<Properties> propertiesBuilder(final @NonNull String propertyParameter) {
        return new ConfigurationBuilder<Properties>() {
            @Override
            public Properties build(Xpp3Dom conf, ExpressionEvaluator eval) {
                if (conf != null) {

                    Xpp3Dom source = conf.getChild(propertyParameter);
                    if (source != null) {
                        Properties toRet = new Properties();
                        Xpp3Dom[] childs = source.getChildren();
                        for (Xpp3Dom ch : childs) {
                                String val = ch.getValue();
                                if (val == null) {
                                    //#168036
                                    //we have the "property" named element now.
                                    if (ch.getChildCount() == 2) {
                                        Xpp3Dom nameDom = ch.getChild("name"); //NOI18N
                                        Xpp3Dom valueDom = ch.getChild("value"); //NOI18N
                                        if (nameDom != null && valueDom != null) {
                                            String name = nameDom.getValue();
                                            String value = valueDom.getValue();
                                            if (name != null && value != null) {
                                                toRet.put(name, value);  //NOI18N
                                            }
                                        }
                                    }
                                    // #153063, #187648
                                    toRet.put(ch.getName(), "");
                                    continue;
                                }
                                toRet.put(ch.getName(), val.trim());  //NOI18N
                        }
                        return toRet;
                    }
                }
                return null;
            }
        };
    }

    /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getPluginPropertyList(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        NbMavenProjectImpl project = prj instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)prj : prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyImpl(project.getOriginalMavenProject(), groupId, artifactId, listProperty(multiproperty, singleproperty), goal, null);
    }

    /**
     * gets the list of values for the given property, if configured in the current project.
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getPluginPropertyList(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        return getPluginPropertyImpl(prj, groupId, artifactId, listProperty(multiproperty, singleproperty), goal, null);
    }

    /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getReportPluginPropertyList(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        NbMavenProjectImpl project = prj instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)prj : prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getReportPluginPropertyImpl(project.getOriginalMavenProject(), groupId, artifactId, listProperty(multiproperty, singleproperty), goal);
    }

    /**
     * gets the list of values for the given property, if configured in the current project.
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getReportPluginPropertyList(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        return getReportPluginPropertyImpl(prj, groupId, artifactId, listProperty(multiproperty, singleproperty), goal);
    }

    /**
     * Loads name/value pairs from plugin configuration.
     * Two syntaxes are supported:
     * {@code <params><key>value</key><flag/></params>} produces {@code {key=value, flag=}}
     * (last value is {@code ""} not {@code null} due to {@link Properties} limitation);
     * {@code <params><param><name>key</name><value>value</value></param></params>} produces {@code {key=value}}.
     * @return properties
     */
    public static @CheckForNull Properties getPluginPropertyParameter(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String propertyParameter, @NullAllowed String goal) {
        NbMavenProjectImpl project = prj instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)prj : prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyImpl(project.getOriginalMavenProject(), groupId, artifactId, propertiesBuilder(propertyParameter), goal, null);
    }
    /**
     * Loads name/value pairs from plugin configuration. 
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.
     * Two syntaxes are supported:
     * {@code <params><key>value</key><flag/></params>} produces {@code {key=value, flag=}}
     * (last value is {@code ""} not {@code null} due to {@link Properties} limitation);
     * {@code <params><param><name>key</name><value>value</value></param></params>} produces {@code {key=value}}.
     * @return properties
     */
    public static @CheckForNull Properties getPluginPropertyParameter(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String propertyParameter, @NullAllowed String goal) {
        return getPluginPropertyImpl(prj, groupId, artifactId, propertiesBuilder(propertyParameter), goal, null);
    }

    /**
     * Should handle both deprecated 2.x-style report section, and 3.x-style Site Plugin config.
     * https://jira.codehaus.org/browse/MSITE-484 and https://jira.codehaus.org/browse/MSITE-443 if and when implemented may require updates.
     */
    private static @NonNull Iterable<ReportPlugin> getEffectiveReportPlugins(@NonNull MavenProject prj) {
        List<ReportPlugin> plugins = new ArrayList<ReportPlugin>();
        for (Plugin plug : prj.getBuildPlugins()) {
            if (Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId()) && Constants.PLUGIN_SITE.equals(plug.getArtifactId())) {
                Xpp3Dom cfg = (Xpp3Dom) plug.getConfiguration(); // MNG-4862
                if (cfg == null) {
                    continue;
                }
                Xpp3Dom reportPlugins = cfg.getChild("reportPlugins");
                if (reportPlugins == null) {
                    continue;
                }
                for (Xpp3Dom plugin : reportPlugins.getChildren("plugin")) {
                    ReportPlugin p = new ReportPlugin();
                    Xpp3Dom groupId = plugin.getChild("groupId");
                    if (groupId != null) {
                        p.setGroupId(groupId.getValue());
                    }
                    Xpp3Dom artifactId = plugin.getChild("artifactId");
                    if (artifactId != null) {
                        p.setArtifactId(artifactId.getValue());
                    }
                    Xpp3Dom version = plugin.getChild("version");
                    if (version != null) {
                        p.setVersion(version.getValue());
                    }
                    p.setConfiguration(plugin.getChild("configuration"));
                    // XXX reportSets
                    // maven-site-plugin does not appear to apply defaults from plugin.xml (unlike 2.x?)
                    plugins.add(p);
                }
            }
        }
        @SuppressWarnings("deprecation") List<ReportPlugin> m2Plugins = prj.getReportPlugins();
        plugins.addAll(m2Plugins);
        return plugins;
    }
    
    /**
     * Evaluator usable for interpolating variables in non resolved (interpolated) models. 
     * Should not be necessary when dealing with the project's <code>MavenProject</code> instance accessed via<code>NbMavenProject.getMavenProject()</code>
     * @since 2.57
     */
    public static @NonNull ExpressionEvaluator createEvaluator(@NonNull Project project) {
        NbMavenProjectImpl prj = project instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)project : project.getLookup().lookup(NbMavenProjectImpl.class);
        assert prj != null;
        MavenProject mvnprj = prj.getOriginalMavenProject();
        //the idea here is to tie the lifecycle of the evaluator to the lifecycle of the MavenProject, both
        //get changed when settings.xml is changed or pom is change or when a profile gets updated..
        ExpressionEvaluator eval = (ExpressionEvaluator) mvnprj.getContextValue(CONTEXT_EXPRESSION_EVALUATOR);
        if (eval == null) {
            Settings ss = EmbedderFactory.getProjectEmbedder().getSettings();
            ss.setLocalRepository(EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir());

            eval =  new NBPluginParameterExpressionEvaluator(
                mvnprj,
                ss,
                prj.createSystemPropsForPropertyExpressions(),
                prj.createUserPropsForPropertyExpressions());
            mvnprj.setContextValue(CONTEXT_EXPRESSION_EVALUATOR, eval);
        }
        return eval;
    }
    
    /**
     * Evaluator usable for interpolating variables in non resolved (interpolated) models.
     * Should not be necessary when dealing with the project's <code>MavenProject</code> instance accessed via<code>NbMavenProject.getMavenProject()</code>
     * Please NOTE that if you have access to <code>Project</code> instance, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @since 2.32
     */
    public static @NonNull ExpressionEvaluator createEvaluator(@NonNull MavenProject prj) {
        ExpressionEvaluator eval = (ExpressionEvaluator) prj.getContextValue(CONTEXT_EXPRESSION_EVALUATOR);
        if (eval != null) {
            return eval;
        }
        Map<? extends String,? extends String> sysprops = Collections.emptyMap();
        Map<? extends String,? extends String> userprops = Collections.emptyMap();
        File basedir = prj.getBasedir();
        if (basedir != null) {
        FileObject bsd = FileUtil.toFileObject(basedir);
        if (bsd != null) {
            Project p = FileOwnerQuery.getOwner(bsd);
            if (p != null) {
                NbMavenProjectImpl project = p.getLookup().lookup(NbMavenProjectImpl.class);
                if (project != null) {
                    sysprops = project.createSystemPropsForPropertyExpressions();
                    userprops = project.createUserPropsForPropertyExpressions();
                }
            }
        }
        }
        //ugly
        Settings ss = EmbedderFactory.getProjectEmbedder().getSettings();
        ss.setLocalRepository(EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir());

        eval = new NBPluginParameterExpressionEvaluator(
                prj,
                ss,
                sysprops,
                userprops);
        prj.setContextValue(CONTEXT_EXPRESSION_EVALUATOR, eval);
        return eval;
    }

    /** @see org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator */
    private static @NonNull List<PluginExecution> getPluginExecutions(@NonNull Plugin plug, @NullAllowed String goal) {
        if (goal == null) {
            return Collections.emptyList();
        }
        List<PluginExecution> exes = new ArrayList<PluginExecution>();
        for (PluginExecution exe : plug.getExecutions()) {
            if (exe.getGoals().contains(goal) || /* #179328: Maven 2.2.0+ */ ("default-" + goal).equals(exe.getId())) {
                exes.add(exe);
            }
        }
        exes.sort(new Comparator<PluginExecution>() {
            @Override public int compare(PluginExecution e1, PluginExecution e2) {
                return e2.getPriority() - e1.getPriority();
            }
        });
        return exes;
    }

    /**
     * Reads dependency list from the XML
     */
    static class DependencyListBuilder implements ConfigurationBuilder<List<Dependency>> {
        private static final String PROP_ARTIFACT_ID = "artifactId"; // NOI18N
        private static final String PROP_GROUP_ID = "groupId"; // NOI18N
        private static final String PROP_CLASSIFIER = "classifier"; // NOI18N
        private static final String PROP_TYPE = "type"; // NOI18N
        private static final String PROP_VERSION = "version"; // NOI18N
        private static final String PROP_SCOPE = "scope"; // NOI18N
        
        private final MavenProject mvnProject;
        private final String multiPropertyName;
        private final String propertyItemName;
        private final String filterType;
        
        public DependencyListBuilder(MavenProject mvnProject, String multiPropertyName, String propertyItemName, String filterType) {
            this.mvnProject = mvnProject;
            this.multiPropertyName = multiPropertyName;
            this.propertyItemName = propertyItemName;
            this.filterType = filterType;
        }
        
        @Override
        public List<Dependency> build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
            if (configRoot == null) {
                return null;
            }
            List<Dependency> coords = new ArrayList<>();
            Xpp3Dom source = configRoot.getChild(multiPropertyName);
            if (source == null) {
                return null;
            }
            for (Xpp3Dom ch : source.getChildren(propertyItemName)) {
                Xpp3Dom a = ch.getChild(PROP_ARTIFACT_ID);
                Xpp3Dom g = ch.getChild(PROP_GROUP_ID);
                Xpp3Dom v = ch.getChild(PROP_VERSION);
                Xpp3Dom t = ch.getChild(PROP_TYPE);
                Xpp3Dom c = ch.getChild(PROP_CLASSIFIER);
                Xpp3Dom s = ch.getChild(PROP_SCOPE);
                
                // XXX todo: transfer locations from Xpp3Dom to Dependency
                
                if (t != null && filterType != null && !filterType.equals(t)) {
                    continue;
                }
                
                Dependency item = new Dependency();
                if (a != null) {
                    item.setArtifactId(a.getValue());
                    item.setLocation(PROP_ARTIFACT_ID, (InputLocation)a.getInputLocation()); 
                }
                if (g != null) {
                    item.setGroupId(g.getValue());
                    item.setLocation(PROP_GROUP_ID, (InputLocation)g.getInputLocation());
                }
                if (v != null) {
                    item.setVersion(v.getValue());
                    item.setLocation(PROP_VERSION, (InputLocation)v.getInputLocation());
                }
                if (c != null) {
                    item.setClassifier(c.getValue());
                    item.setLocation(PROP_CLASSIFIER, (InputLocation)c.getInputLocation());
                }
                if (t != null) {
                    item.setType(t.getValue());
                    item.setLocation(PROP_TYPE, (InputLocation)t.getInputLocation());
                }
                if (s != null) {
                    item.setScope(s.getValue());
                    item.setLocation(PROP_SCOPE, (InputLocation)s.getInputLocation());
                }
                coords.add(item);
            }
            return coords;
        }
    }

    /**
     * Query parameters to convert property containing a dependency list into artifact list.
     * @since 2.151
     */
    public static final class PluginConfigPathParams {
        private final String pluginGroupId;
        private final String pluginArtifactId;
        private final String pathProperty;
        private final String pathItemName;

        private String goal;
        private String artifactType;
        private String defaultScope;

        /**
         * Creates a query instance with mandatory parameters
         * @param pluginGroupId plugin's group ID
         * @param pluginArtifactId plugin's artifact ID
         * @param pathProperty name of the property (the property should contain a list of items)
         * @param pathItemName name of the single item's element
         */
        public PluginConfigPathParams(String pluginGroupId, String pluginArtifactId, String pathProperty, String pathItemName) {
            this.pluginGroupId = pluginGroupId;
            this.pluginArtifactId = pluginArtifactId;
            this.pathProperty = pathProperty;
            this.pathItemName = pathItemName;
        }

        /**
         * Optional. Specifies the goal whose configuration should be inspected.
         * @param goal goal ID
         */
        public void setGoal(String goal) {
            this.goal = goal;
        }

        /**
         * Optional. Filters artifact types that are accepted. If unspecified, the type defaults to "jar".
         * @param artifactType accepted artifact type.
         */
        public void setArtifactType(String artifactType) {
            this.artifactType = artifactType;
        }

        public void setDefaultScope(String defaultScope) {
            this.defaultScope = defaultScope;
        }

        public String getPluginGroupId() {
            return pluginGroupId;
        }

        public String getPluginArtifactId() {
            return pluginArtifactId;
        }

        public String getGoal() {
            return goal;
        }

        public String getPathProperty() {
            return pathProperty;
        }

        public String getPathItemName() {
            return pathItemName;
        }

        public String getArtifactType() {
            return artifactType;
        }

        public String getDefaultScope() {
            return defaultScope;
        }
    }

    /**
     * Converts a list of dependency declarations in the plugin configuration into list of Artifacts. Can add (transitive) dependencies. Useful to convert
     * list of some dependencies into artifacts and subsequently into classpaths or (artifact) file lists. The {@link PluginConfigPathParams} contains a property
     * selector and other data to process the dependency list. If `errorsOpt` is not {@code null}, it will receive errors from artifact resolution, if any. Otherwise,
     * errors are just ignored. The method returns {@code null} if the plugin/execution property is not defined at all.
     * 
     * @param project the maven project
     * @param query specifies the query for artifacts.
     * @param transitiveDependencies if true, returns also transitive dependencies.
     * @param errorsOpt if not {@code null}, will receive list of errors encountered.
     * @return list of artifacts for the property, or {@code null} if the property does not exist
     * @since 2.151
     */
    public static @CheckForNull List<Artifact> getPluginPathProperty(
            @NonNull Project project, @NonNull PluginConfigPathParams query, boolean transitiveDependencies, 
            @NullAllowed List<ArtifactResolutionException> errorsOpt) {

        NbMavenProjectImpl projectImpl = project instanceof NbMavenProjectImpl ? (NbMavenProjectImpl)project : project.getLookup().lookup(NbMavenProjectImpl.class);
        if (projectImpl == null) {
            return null;
        }
        
        MavenProject mavenProject = projectImpl.getOriginalMavenProject();
        DependencyListBuilder bld = new DependencyListBuilder(mavenProject, query.getPathProperty(), query.getPathItemName(), query.getArtifactType());
        List<Dependency> coordinates = PluginPropertyUtils.getPluginPropertyBuildable(mavenProject, query.getPluginGroupId(), query.getPluginArtifactId(), query.getGoal(), bld);
        if (coordinates == null) {
            return null;
        }
        // maintain somewhat the order
        Set<Artifact> requiredArtifacts = new LinkedHashSet<>(coordinates.size());
        projectImpl.getEmbedder().setUpLegacySupport();
        ArtifactHandlerManager ahm = projectImpl.getEmbedder().lookupComponent(ArtifactHandlerManager.class);
        RepositorySystem repos = projectImpl.getEmbedder().lookupComponent(RepositorySystem.class);
        
        if (ahm == null || repos == null) {
            // cannot resolve artifacts or dependencies, sorry.
            return null;
        }
        String scope = query.getDefaultScope();
        if (scope == null) {
            scope = Artifact.SCOPE_RUNTIME;
        }
        for (Dependency coord : coordinates) {
            ArtifactHandler handler = ahm.getArtifactHandler(coord.getType());

            // BEGIN:copied from maven-compiler-plugin + adapted
            Artifact artifact;
            try {
                artifact = new DefaultArtifact(
                     coord.getGroupId(),
                     coord.getArtifactId(),
                     VersionRange.createFromVersionSpec( coord.getVersion() ),
                     coord.getScope() == null ? query.getDefaultScope() : coord.getScope(),
                     coord.getType(),
                     coord.getClassifier(),
                     handler,
                     false );
            } catch (InvalidVersionSpecificationException ex) {
                errorsOpt.add(new ArtifactResolutionException(ex.getMessage(), 
                        coord.getGroupId(), coord.getArtifactId(), coord.getVersion(), 
                        coord.getType(), coord.getClassifier(), ex));
                continue;
            }

            requiredArtifacts.add( artifact );

            ArtifactResolutionRequest request = new ArtifactResolutionRequest()
                            .setArtifact( requiredArtifacts.iterator().next() )
                            .setResolveRoot(true)
                            .setResolveTransitively(true)
                            .setArtifactDependencies(requiredArtifacts)
                            .setLocalRepository(projectImpl.getEmbedder().getLocalRepository())
                            .setRemoteRepositories( mavenProject.getRemoteArtifactRepositories() );

            ArtifactResolutionResult resolutionResult = repos.resolve(request);
            // END:copied from maven-compiler-plugin + adapted
            if (errorsOpt != null) {
                errorsOpt.addAll(resolutionResult.getMetadataResolutionExceptions());
                errorsOpt.addAll(resolutionResult.getErrorArtifactExceptions());
            }
            requiredArtifacts.addAll(resolutionResult.getArtifacts());
        }
        return new ArrayList<>(requiredArtifacts);
    }
    
}
