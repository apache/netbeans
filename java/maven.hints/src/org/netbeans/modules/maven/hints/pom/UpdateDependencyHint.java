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
package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.Document;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.ReportPlugin;
import org.netbeans.modules.maven.model.pom.Reporting;
import org.netbeans.modules.maven.model.pom.VersionablePOMComponent;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

import static org.netbeans.modules.maven.hints.pom.Bundle.*;

/**
 * Marks the artifact if there is a newer version available and provides a fix
 * which updates the version.
 * @author mbien
 */
@NbBundle.Messages({
    "TIT_UpdateDependencyHint=Mark artifact upgrade opportunities.",
    "DESC_UpdateDependencyHint=Marks the artifact if there is a new version available and provides a version upgrde fix."
                            + "<p>Upgrades of major versions can be optionally omitted.</p>",
    "HINT_UpdateDependencyHint=New version available: ",
    "FIX_UpdateDependencyHint=upgrade to: "})
public class UpdateDependencyHint implements POMErrorFixProvider {

    private static final Configuration config = new Configuration(UpdateDependencyHint.class.getSimpleName(),
                TIT_UpdateDependencyHint(), DESC_UpdateDependencyHint(), true, Configuration.HintSeverity.WARNING);

    static final String KEY_NO_MAJOR_UPGRADE = "no_major_upgrade";

    private boolean noMajorUpgrde;
    private UpdateDependencyHintCustomizer customizer;

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {

        noMajorUpgrde = getNoMajorUpgradeOption();

        Map<POMComponent, ErrorDescription> hints = new HashMap<>();
        org.netbeans.modules.maven.model.pom.Project project = model.getProject();

        addHintsToDependencies(project.getDependencies(), project.getDependencyManagement(), hints);

        Build build = project.getBuild();
        if (build != null) {
            addHintsToPlugins(build.getPlugins(), build.getPluginManagement(), hints);
        }

        Reporting reporting = project.getReporting();
        if (reporting != null) {
            if (reporting.getReportPlugins() != null) {
                addHintsTo(reporting.getReportPlugins(), hints);
            }
        }

        List<Profile> profiles = project.getProfiles();
        if (profiles != null) {
            for (Profile profile : profiles) {
                addHintsToDependencies(profile.getDependencies(), profile.getDependencyManagement(), hints);
                BuildBase base = profile.getBuildBase();
                if (base != null) {
                    addHintsToPlugins(base.getPlugins(), base.getPluginManagement(), hints);
                }
            }
        }

        return new ArrayList<>(hints.values());
    }

    private void addHintsToDependencies(List<Dependency> deps, DependencyManagement depman, Map<POMComponent, ErrorDescription> hints) {
        if (deps != null) {
            addHintsTo(deps, hints);
        }
        if (depman != null && depman.getDependencies() != null) {
            addHintsTo(depman.getDependencies(), hints);
        }
    }

    private void addHintsToPlugins(List<Plugin> plugins, PluginManagement plugman, Map<POMComponent, ErrorDescription> hints) {
        if (plugins != null) {
            addHintsTo(plugins, hints);
        }
        if (plugman != null && plugman.getPlugins() != null) {
            addHintsTo(plugman.getPlugins(), hints);
        }
    }

    private void addHintsTo(List<? extends VersionablePOMComponent> components, Map<POMComponent, ErrorDescription> hints) {

        for (VersionablePOMComponent comp : components) {

            String groupId = comp.getGroupId() != null && !comp.getGroupId().isBlank() ? comp.getGroupId() : null;
            String artifactId = comp.getArtifactId() != null && !comp.getArtifactId().isBlank() ? comp.getArtifactId() : null;

            // no group ID could indicate it is a default maven plugin
            if (groupId == null && (comp instanceof Plugin || comp instanceof ReportPlugin)) {
                groupId = Constants.GROUP_APACHE_PLUGINS;
            }

            if (artifactId != null && groupId != null && comp.getVersion() != null) {

                class HintCandidate { // can be record
                    final String version;
                    final POMExtensibilityElement component;
                    HintCandidate(String version, POMExtensibilityElement component) {
                       this.version = version;
                       this.component = component;
                    }
                }

                List<HintCandidate> candidates = List.of();

                if (PomModelUtils.isPropertyExpression(comp.getVersion())) {
                    // properties can be set in profiles and the properties section
                    // this collects all candidates which might need an annotation, versions are checked later
                    candidates = new ArrayList<>();
                    String propName = PomModelUtils.getPropertyName(comp.getVersion());
                    Properties props = comp.getModel().getProject().getProperties();
                    if (props != null) {
                        POMComponent c = PomModelUtils.getFirstChild(props, propName);
                        if (c instanceof POMExtensibilityElement) {
                            candidates.add(new HintCandidate(props.getProperty(propName), (POMExtensibilityElement) c));
                        }
                    }
                    // check profile properties for candidates
                    List<Profile> profiles = comp.getModel().getProject().getProfiles();
                    if (profiles != null) {
                        for (Profile profile : profiles) {
                            Properties profProps = profile.getProperties();
                            if (profProps != null) {
                                POMComponent c = PomModelUtils.getFirstChild(profProps, propName);
                                if (c instanceof POMExtensibilityElement) {
                                    candidates.add(new HintCandidate(profProps.getProperty(propName), (POMExtensibilityElement) c));
                                }
                            }
                        }
                    }
                } else {
                    // simple case, were the version is directly set where the artifact is declared
                    POMComponent c = PomModelUtils.getFirstChild(comp, "version");
                    if (c instanceof POMExtensibilityElement) {
                        candidates = List.of(new HintCandidate(comp.getVersion(), (POMExtensibilityElement) c));
                    }
                }

                if (candidates.isEmpty()) {
                    continue;
                }

                List<NBVersionInfo> versions = RepositoryQueries.getVersionsResult(groupId, artifactId, null).getResults();

                for (HintCandidate candidate : candidates) {

                    // don't upgrade clean numerical versions to timestamps or non-numerical versions (other way around is allowed)
                    boolean allow_qualifier = !isNumerical(candidate.version);
                    boolean allow_timestamp = !noTimestamp(candidate.version);
                    String requiredPrefix = noMajorUpgrde ? getMajorComponentPrefix(candidate.version) : "";

                    Optional<ComparableVersion> latest = versions.stream()
                            .map(NBVersionInfo::getVersion)
                            .filter((v) -> !v.isEmpty() && v.startsWith(requiredPrefix))
                            .filter((v) -> allow_qualifier || !Character.isDigit(v.charAt(0)) || isNumerical(v))
                            .filter((v) -> allow_timestamp || !Character.isDigit(v.charAt(0)) || noTimestamp(v))
                            .map(ComparableVersion::new)
                            .max(ComparableVersion::compareTo);

                    if (latest.isPresent() && latest.get().compareTo(new ComparableVersion(candidate.version)) > 0) {
                        ErrorDescription previous = hints.get(candidate.component);
                        if (previous == null || compare(((UpdateVersionFix) previous.getFixes().getFixes().get(0)).version, candidate.version) > 0) {
                            hints.put(candidate.component, createHintForComponent(candidate.component, latest.get().toString()));
                        }
                    }
                }
            }
        }
    }

    private static boolean isNumerical(String v) {
        for (char c : v.toCharArray()) {
            if (!(Character.isDigit(c) || c == '.')) {
                return false;
            }
        }
        return true;
    }

    private boolean noTimestamp(String v) {
        char[] chars = v.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!(Character.isDigit(chars[i]))) {
                return i == 0 || Integer.parseInt(v.substring(0, i)) < 10_000;
            }
        }
        return v.isEmpty() || Integer.parseInt(v) < 10_000;
    }

    // example: in '3.14' -> out '3.'
    private String getMajorComponentPrefix(String v) {
        int dot = v.indexOf('.');
        if (dot > 0) {
            String major = v.substring(0, dot+1);
            if (isNumerical(major)) {
                return major;
            }
        }
        return "";
    }

    private static int compare(String version1, String version2) {
        return new ComparableVersion(version1).compareTo(new ComparableVersion(version2));
    }

    private ErrorDescription createHintForComponent(POMExtensibilityElement comp, String version) {
        Document doc = comp.getModel().getBaseDocument();
        int line = NbEditorUtilities.getLine(doc, comp.findPosition(), false).getLineNumber() + 1;
        List<Fix> fix = List.of(new UpdateVersionFix(comp, version));
        return ErrorDescriptionFactory.createErrorDescription(Severity.HINT, HINT_UpdateDependencyHint() + version, fix, doc, line);
    }

    private static class UpdateVersionFix implements Fix {

        private final POMExtensibilityElement version_comp;
        private final String version;

        private UpdateVersionFix(POMExtensibilityElement component, String toVersion) {
            this.version_comp = component;
            this.version = toVersion;
        }

        @Override
        public String getText() {
            return FIX_UpdateDependencyHint() + version;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            PomModelUtils.implementInTransaction(version_comp.getModel(), () -> {
                version_comp.setElementText(version);
            });
            return new ChangeInfo();
        }

    }

    @Override
    public void cancel() {
        customizer = null;
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    private static boolean getNoMajorUpgradeOption() {
        return config.getPreferences().getBoolean(KEY_NO_MAJOR_UPGRADE, false);
    }

    @Override
    public String getSavedValue(JComponent customizer, String key) {
        if (KEY_NO_MAJOR_UPGRADE.equals(key) && customizer instanceof UpdateDependencyHintCustomizer) {
            return Boolean.toString(((UpdateDependencyHintCustomizer)customizer).getSavedNoMajorUpgradeOption());
        }
        return null;
    }

    @Override
    public JComponent getCustomizer(Preferences prefsCopy) {
        if (customizer == null) {
            customizer = new UpdateDependencyHintCustomizer(prefsCopy, getNoMajorUpgradeOption());
        }
        return customizer;
    }

}
