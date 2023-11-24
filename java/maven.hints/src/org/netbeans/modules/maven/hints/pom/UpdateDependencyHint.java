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
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
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

        List<Dependency> deps = model.getProject().getDependencies();
        if (deps != null) {
            addHintsTo(deps, hints);
        }

        DependencyManagement depman = model.getProject().getDependencyManagement();
        if (depman != null && depman.getDependencies() != null) {
            addHintsTo(depman.getDependencies(), hints);
        }

        Build build = model.getProject().getBuild();
        if (build != null) {
            if (build.getPlugins() != null) {
                addHintsTo(build.getPlugins(), hints);
            }

            PluginManagement plugman = build.getPluginManagement();
            if (plugman != null && plugman.getPlugins() != null) {
                addHintsTo(plugman.getPlugins(), hints);
            }
        }

        Reporting reporting = model.getProject().getReporting();
        if (reporting != null) {
            if (reporting.getReportPlugins() != null) {
                addHintsTo(reporting.getReportPlugins(), hints);
            }
        }

        return new ArrayList<>(hints.values());
    }

    private void addHintsTo(List<? extends VersionablePOMComponent> components, Map<POMComponent, ErrorDescription> hints) {

        for (VersionablePOMComponent comp : components) {

            String groupId = comp.getGroupId() != null && !comp.getGroupId().isBlank() ? comp.getGroupId() : null;
            String artifactId = comp.getArtifactId() != null && !comp.getArtifactId().isBlank() ? comp.getArtifactId() : null;

            // no group ID could indicate it is a default maven plugin
            if (groupId == null && (comp instanceof Plugin || comp instanceof ReportPlugin)) {
                groupId = Constants.GROUP_APACHE_PLUGINS;
            }

            if (artifactId != null && groupId != null) {

                boolean property = false;
                String version = comp.getVersion();
                if (PomModelUtils.isPropertyExpression(version)) {
                    version = PomModelUtils.getProperty(comp.getModel(), version);
                    property = true;
                }

                if (version != null) {

                    // don't upgrade clean numerical versions to timestamps or non-numerical versions (other way around is allowed)
                    boolean allow_qualifier = !isNumerical(version);
                    boolean allow_timestamp = !noTimestamp(version);
                    String requiredPrefix = noMajorUpgrde ? getMajorComponentPrefix(version) : "";

                    Optional<ComparableVersion> latest = RepositoryQueries.getVersionsResult(groupId, artifactId, null)
                            .getResults().stream()
                            .map(NBVersionInfo::getVersion)
                            .filter((v) -> !v.isEmpty() && v.startsWith(requiredPrefix))
                            .filter((v) -> allow_qualifier || !Character.isDigit(v.charAt(0)) || isNumerical(v))
                            .filter((v) -> allow_timestamp || !Character.isDigit(v.charAt(0)) || noTimestamp(v))
                            .map(ComparableVersion::new)
                            .max(ComparableVersion::compareTo);

                    if (latest.isPresent() && latest.get().compareTo(new ComparableVersion(version)) > 0) {
                        POMComponent version_comp = null;
                        if (property) {
                            Properties props = comp.getModel().getProject().getProperties();
                            if (props != null) {
                                version_comp = PomModelUtils.getFirstChild(props, PomModelUtils.getPropertyName(comp.getVersion()));
                            }
                        } else {
                            version_comp = PomModelUtils.getFirstChild(comp, "version");
                        }
                        if (version_comp instanceof POMExtensibilityElement) {
                            ErrorDescription previous = hints.get(version_comp);
                            if (previous == null || compare(((UpdateVersionFix) previous.getFixes().getFixes().get(0)).version, version) > 0) {
                                hints.put(version_comp, createHintForComponent((POMExtensibilityElement) version_comp, latest.get().toString()));
                            }
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
