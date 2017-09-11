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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.indexer.api;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.openide.util.BaseUtilities;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Provides information about available plugins, including their goals and parameters.
 */
public class PluginIndexManager {

    private static final Logger LOG = Logger.getLogger(PluginIndexManager.class.getName());

    /**
     * Gets available goals from known plugins.
     * @param groups e.g. {@code Collections.singleton("org.apache.maven.plugins")}
     * @return e.g. {@code [..., dependency:copy, ..., release:perform, ...]}
     */
    public static Set<String> getPluginGoalNames(Set<String> groups) throws Exception {
        Set<String> result = new TreeSet<String>();
        // XXX rather use ArtifactInfo.PLUGIN_GOALS
        for (String groupId : groups) {
            for (String artifactId : RepositoryQueries.filterPluginArtifactIdsResult(groupId, "", null).getResults()) {
                for (NBVersionInfo v : RepositoryQueries.getVersionsResult(groupId, artifactId, null).getResults()) {
                    if (v.getVersion().endsWith("-SNAPSHOT") && !v.getRepoId().equals(RepositorySystem.DEFAULT_LOCAL_REPO_ID)) {
                        continue;
                    }
                    //XXX mkleint: oh man this takes forever.. it's inpractical for any usecase unless one has already downloaded the internet.
                    // now that I think of it, the only scalable solution is make the info part of the index.
                    File jar = RepositoryUtil.downloadArtifact(v);
                    Document pluginXml = loadPluginXml(jar);
                    if (pluginXml == null) {
                        continue;
                    }
                    Element root = pluginXml.getDocumentElement();
                    Element goalPrefix = XMLUtil.findElement(root, "goalPrefix", null);
                    if (goalPrefix == null) {
                        LOG.log(Level.WARNING, "no goalPrefix in {0}", jar);
                        continue;
                    }
                    Element mojos = XMLUtil.findElement(root, "mojos", null);
                    if (mojos == null) {
                        LOG.log(Level.WARNING, "no mojos in {0}", jar);
                        continue;
                    }
                    for (Element mojo : XMLUtil.findSubElements(mojos)) {
                        if (!mojo.getTagName().equals("mojo")) {
                            continue;
                        }
                        Element goal = XMLUtil.findElement(mojo, "goal", null);
                        if (goal == null) {
                            LOG.log(Level.WARNING, "mojo missing goal in {0}", jar);
                            continue;
                        }
                        result.add(XMLUtil.findText(goalPrefix).trim() + ':' + XMLUtil.findText(goal).trim());
                    }
                    break;
                }
            }
        }
        LOG.log(Level.FINE, "found goal names: {0}", result);
        return result;
    }

    /**
     * Gets available goals from a particular plugin.
     * @param groupId e.g. {@code "org.apache.maven.plugins"}
     * @param artifactId e.g. {@code "maven-compiler-plugin"}
     * @param version e.g. {@code "2.0"}
     * @return e.g. {@code [compile, testCompile]}
     */
    public static Set<String> getPluginGoals(String groupId, String artifactId, String version) throws Exception {
        assert groupId != null && artifactId != null && version != null;
        for (NBVersionInfo v : RepositoryQueries.getVersionsResult(groupId, artifactId, null).getResults()) {
            if (!v.getVersion().equals(version)) {
                continue;
            }
            File jar = RepositoryUtil.downloadArtifact(v);
            Document pluginXml = loadPluginXml(jar);
            if (pluginXml == null) {
                continue;
            }
            Element root = pluginXml.getDocumentElement();
            Element mojos = XMLUtil.findElement(root, "mojos", null);
            if (mojos == null) {
                LOG.log(Level.WARNING, "no mojos in {0}", jar);
                continue;
            }
            Set<String> goals = new TreeSet<String>();
            for (Element mojo : XMLUtil.findSubElements(mojos)) {
                if (!mojo.getTagName().equals("mojo")) {
                    continue;
                }
                Element goal = XMLUtil.findElement(mojo, "goal", null);
                if (goal == null) {
                    LOG.log(Level.WARNING, "mojo missing goal in {0}", jar);
                    continue;
                }
                goals.add(XMLUtil.findText(goal).trim());
            }
            LOG.log(Level.FINE, "found goals: {0}", goals);
            return goals;
        }
        return Collections.emptySet();
    }

    /**
     * Gets a list of parameters expected by a plugin goal.
     * @param groupId e.g. {@code "org.apache.maven.plugins"}
     * @param artifactId e.g. {@code "maven-compiler-plugin"}
     * @param version e.g. {@code "2.0"}
     * @param mojo e.g. {@code "compile"}
     * @return null if not found, else e.g. {@code [..., <verbose>${maven.compiler.verbose}=false</>, ...]}
     */
    public static @CheckForNull Set<ParameterDetail> getPluginParameters(String groupId, String artifactId, String version, @NullAllowed String mojo) throws Exception {
        assert groupId != null && artifactId != null && version != null;
        for (NBVersionInfo v : RepositoryQueries.getVersionsResult(groupId, artifactId, null).getResults()) {
            if (!v.getVersion().equals(version)) {
                continue;
            }
            File jar = RepositoryUtil.downloadArtifact(v);
            Document pluginXml = loadPluginXml(jar);
            if (pluginXml == null) {
                continue;
            }
            Element root = pluginXml.getDocumentElement();
            Element mojos = XMLUtil.findElement(root, "mojos", null);
            if (mojos == null) {
                LOG.log(Level.WARNING, "no mojos in {0}", jar);
                continue;
            }
            Set<ParameterDetail> params = new TreeSet<ParameterDetail>(new Comparator<ParameterDetail>() {
                @Override public int compare(ParameterDetail o1, ParameterDetail o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            Map<String, ParameterDetail> details = new HashMap<String, ParameterDetail>();
            for (Element mojoEl : XMLUtil.findSubElements(mojos)) {
                if (!mojoEl.getTagName().equals("mojo")) {
                    continue;
                }
                Element goal = XMLUtil.findElement(mojoEl, "goal", null);
                if (goal == null) {
                    LOG.log(Level.WARNING, "mojo missing goal in {0}", jar);
                    continue;
                }
                if (mojo != null && !mojo.equals(XMLUtil.findText(goal).trim())) {
                    continue;
                }
                Element parameters = XMLUtil.findElement(mojoEl, "parameters", null);
                Element configuration = XMLUtil.findElement(mojoEl, "configuration", null);
                if (parameters != null) {
                    for (Element parameter : XMLUtil.findSubElements(parameters)) {
                        if (!parameter.getTagName().equals("parameter")) {
                            continue;
                        }
                        Element name = XMLUtil.findElement(parameter, "name", null);
                        if (name == null) {
                            LOG.log(Level.WARNING, "parameter missing name in {0}", jar);
                            continue;
                        }
                        Element description = XMLUtil.findElement(parameter, "description", null);
                        if (description == null) {
                            LOG.log(Level.WARNING, "parameter missing description in {0}", jar);
                            continue;
                        }
                        Element required = XMLUtil.findElement(parameter, "required", null);
                        if (required == null) {
                            LOG.log(Level.WARNING, "parameter missing required in {0}", jar);
                            continue;
                        }
                        String defaultValue = null;
                        String expression = null;
                        if (configuration != null) {
                            Element sample = XMLUtil.findElement(configuration, XMLUtil.findText(name), null);
                            if (sample != null) {
                                defaultValue = sample.getAttribute("default-value");
                                if (defaultValue.isEmpty()) {
                                    defaultValue = null;
                                }
                                String expressionWithSheBraces = XMLUtil.findText(sample);
                                if (expressionWithSheBraces != null && expressionWithSheBraces.matches("[$][{].+[}]")) {
                                    expression = expressionWithSheBraces.substring(2, expressionWithSheBraces.length() - 1);
                                }
                            }
                        }
                        String nameString = XMLUtil.findText(name);
                        ParameterDetail detail = new ParameterDetail(nameString, expression, defaultValue, Boolean.parseBoolean(XMLUtil.findText(required)), XMLUtil.findText(description));
                        if (mojo == null) {
                            //collect across multiple mojos
                            ParameterDetail det = details.get(nameString);
                            if (det != null) {
                                detail = det;
                            } else {
                                details.put(nameString, detail);
                                params.add(detail);
                            }
                            detail.addMojo(XMLUtil.findText(goal));
                        } else {
                            params.add(detail);
                        }
                    }
                }
            }
            LOG.log(Level.FINE, "for mojo {0} found params {1}", new Object[] {mojo, params});
            return params;
        }
        return null;
    }


    /**
     * find the plugins which are behind the given goal prefix.
     * @param prefix e.g. {@code "versions"}
     * @return groupId and artifactId and version separated by "|", e.g. {@code [..., org.codehaus.mojo|versions-maven-plugin|1.1, ...]}
     * @throws java.lang.Exception
     */
    public static Set<String> getPluginsForGoalPrefix(String prefix) throws Exception {
        assert prefix != null;
        Set<String> result = new TreeSet<String>();
        // Note that this will not work reliably for remote indices created prior to a fix for MINDEXER-34:
        QueryField qf = new QueryField();
        qf.setField(ArtifactInfo.PLUGIN_PREFIX);
        qf.setValue(prefix);
        qf.setOccur(QueryField.OCCUR_MUST);
        qf.setMatch(QueryField.MATCH_EXACT);
        for (NBVersionInfo v : RepositoryQueries.findResult(Collections.singletonList(qf), null).getResults()) {
            result.add(v.getGroupId() + '|' + v.getArtifactId() + '|' + v.getVersion());
        }
        // This is more complete but much too slow:
        /*
        for (String groupId : RepositoryQueries.filterPluginGroupIds("", infos)) {
            for (String artifactId : RepositoryQueries.filterPluginArtifactIds(groupId, "", infos)) {
                for (NBVersionInfo v : RepositoryQueries.getVersions(groupId, artifactId, infos)) {
                    if (v.getVersion().endsWith("-SNAPSHOT") && !v.getRepoId().equals(RepositorySystem.DEFAULT_LOCAL_REPO_ID)) {
                        continue;
                    }
                    File jar = RepositoryUtil.downloadArtifact(v);
                    Document pluginXml = loadPluginXml(jar);
                    if (pluginXml == null) {
                        continue;
                    }
                    Element root = pluginXml.getDocumentElement();
                    Element goalPrefix = XMLUtil.findElement(root, "goalPrefix", null);
                    if (goalPrefix == null) {
                        LOG.log(Level.WARNING, "no goalPrefix in {0}", jar);
                        continue;
                    }
                    if (!prefix.equals(XMLUtil.findText(goalPrefix))) {
                        continue;
                    }
                    result.add(v.getGroupId() + '|' + v.getArtifactId() + '|' + v.getVersion());
                }
            }
        }
        */
        LOG.log(Level.FINE, "found plugins {0}", result);
        return result;
    }

    /**
     * find the phase associations for the given packaging
     * @param packaging e.g. {@code "nbm"}
     * @param mvnVersion e.g. {@code "2.2.1"} (currently ignored)
     * @param extensionPlugins e.g. {@code ["org.codehaus.mojo:nbm-maven-plugin:3.5"]}
     * @return key= phase name, value - Set of Strings, where Strings are in format groupId:artifactId:mojo; e.g. {@code ..., package=[org.apache.maven.plugins:maven-jar-plugin:jar, org.codehaus.mojo:nbm-maven-plugin:nbm], ...}
     */
    public static Map<String,List<String>> getLifecyclePlugins(String packaging, @NullAllowed String mvnVersion, String[] extensionPlugins) throws Exception {
        assert packaging != null;
        URL standard = MavenEmbedder.class.getClassLoader().getResource("META-INF/plexus/artifact-handlers.xml");
        if (standard != null) {
            Map<String,List<String>> phases = parsePhases(standard.toString(), packaging);
            if (phases != null) {
                return phases;
            }
        }
        for (String extensionPlugin : extensionPlugins) {
            String[] gav = extensionPlugin.split(":", 3);
            MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
            Artifact art = online.createArtifact(gav[0], gav[1], gav[2], "maven-plugin");
            online.resolve(art, Collections.<ArtifactRepository>emptyList(), online.getLocalRepository());
            File jar = art.getFile();
            if (jar.isFile()) {
                Map<String, List<String>> phases = parsePhases("jar:" + BaseUtilities.toURI(jar) + "!/META-INF/plexus/components.xml", packaging);
                if (phases != null) {
                    return phases;
                }
            }
        }
        return Collections.emptyMap();
    }
    private static Map<String,List<String>> parsePhases(String u, String packaging) throws Exception {
        Document doc = XMLUtil.parse(new InputSource(u), false, false, XMLUtil.defaultErrorHandler(), null);
        for (Element componentsEl : XMLUtil.findSubElements(doc.getDocumentElement())) {
            for (Element componentEl : XMLUtil.findSubElements(componentsEl)) {
                if (XMLUtil.findText(XMLUtil.findElement(componentEl, "role", null)).trim().equals("org.apache.maven.lifecycle.mapping.LifecycleMapping")
                        && XMLUtil.findText(XMLUtil.findElement(componentEl, "implementation", null)).trim().equals("org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping")
                        && XMLUtil.findText(XMLUtil.findElement(componentEl, "role-hint", null)).trim().equals(packaging)) {
                    for (Element configurationEl : XMLUtil.findSubElements(componentEl)) {
                        if (!configurationEl.getTagName().equals("configuration")) {
                            continue;
                        }
                        Element phases = XMLUtil.findElement(configurationEl, "phases", null);
                        if (phases == null) {
                            for (Element lifecyclesEl : XMLUtil.findSubElements(configurationEl)) {
                                if (!lifecyclesEl.getTagName().equals("lifecycles")) {
                                    continue;
                                }
                                for (Element lifecycleEl : XMLUtil.findSubElements(lifecyclesEl)) {
                                    if (XMLUtil.findText(XMLUtil.findElement(lifecycleEl, "id", null)).trim().equals("default")) {
                                        phases = XMLUtil.findElement(lifecycleEl, "phases", null);
                                        break;
                                    }
                                }
                            }
                        }
                        if (phases != null) {
                            Map<String,List<String>> result = new LinkedHashMap<String,List<String>>();
                            for (Element phase : XMLUtil.findSubElements(phases)) {
                                List<String> plugins = new ArrayList<String>();
                                for (String plugin : XMLUtil.findText(phase).split(",")) {
                                    String[] gavMojo = plugin.trim().split(":", 4);
                                    plugins.add(gavMojo[0] + ':' + gavMojo[1] + ':' + (gavMojo.length == 4 ? gavMojo[3] : gavMojo[2])); // version is not used here
                                }
                                result.put(phase.getTagName(), plugins);
                            }
                            LOG.log(Level.FINE, "for {0} found in {1}: {2}", new Object[] {packaging, u, result});
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static @CheckForNull Document loadPluginXml(File jar) {
        if (!jar.isFile() || !jar.getName().endsWith(".jar")) {
            return null;
        }
        LOG.log(Level.FINER, "parsing plugin.xml from {0}", jar);
            try {
            return XMLUtil.parse(new InputSource("jar:" + BaseUtilities.toURI(jar) + "!/META-INF/maven/plugin.xml"), false, false, XMLUtil.defaultErrorHandler(), null);
        } catch (Exception x) {
            LOG.log(Level.FINE, "could not parse " + jar, x.toString());
            return null;
        }
    }

    /**
     * Detailed information about a given parameter
     */
    public static class ParameterDetail {
        private String name;
        private @NullAllowed String expression;
        private @NullAllowed String defaultValue;
        private boolean required;
        private String description;
        private SortedSet<String> mojos = new TreeSet<String>();

        private ParameterDetail(String name, @NullAllowed String expression, @NullAllowed String defaultValue, boolean required, String description) {
            this.name = name;
            this.expression = expression;
            this.defaultValue = defaultValue;
            this.required = required;
            this.description = description;
        }
        
        void addMojo(String mojo) {
            mojos.add(mojo);
        }

        /**
         * @return null, or e.g. {@code false}
         */
        @CheckForNull public String getDefaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }

        /**
         * @return null, or e.g. {@code maven.compiler.verbose}
         */
        @CheckForNull public String getExpression() {
            return expression;
        }

        /**
         * e.g. {@code verbose}
         */
        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        public String getHtmlDetails(boolean includeName) {
            String m = mojos.size() > 0 ? Arrays.toString(mojos.toArray()) : null;
            if (m != null) {
                m = m.substring(1, m.length() - 1);
            }
            return "<html><body>" + (includeName ? ("<h4>" + NbBundle.getMessage(PluginIndexManager.class, "TXT_LBL_PARAMETER") + getName() + "</h4>") : "") +
            "<b>" + NbBundle.getMessage(PluginIndexManager.class, "LBL_Expression") + "</b>" +  (getExpression() != null ? ("${" + getExpression() + "}") : NbBundle.getMessage(PluginIndexManager.class, "LBL_Undefined")) + "<br>" +
            "<b>" + NbBundle.getMessage(PluginIndexManager.class, "LBL_DefaultValue") + "</b>" + (getDefaultValue() != null ? getDefaultValue() : NbBundle.getMessage(PluginIndexManager.class, "LBL_Undefined"))  +
            (m != null ? "<br/><b>" + NbBundle.getMessage(PluginIndexManager.class, "LBL_Mojos") + "</b>" + m : "")  +
            "<br><b>" + NbBundle.getMessage(PluginIndexManager.class, "LBL_Description") + "</b><br>"+ getDescription() + "</body></html>";
        }

        public @Override String toString() {
            return "<" + name + ">" + (expression != null ? "${" + expression + "}" : "") + (defaultValue != null ? "=" + defaultValue : "") + "</>"; // NOI18N
        }

    }

    private PluginIndexManager() {}

}
