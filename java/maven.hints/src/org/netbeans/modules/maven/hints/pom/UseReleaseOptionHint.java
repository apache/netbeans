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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.xml.namespace.QName;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginContainer;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.Line;
import org.openide.util.NbBundle;

import static org.netbeans.modules.maven.hints.pom.Bundle.*;

/**
 * Converts source/target javac options to the release option where possible (JEP 247).
 * @author mbien
 */
@NbBundle.Messages({
    "TIT_UseReleaseVersionHint=Convert matching source/target (or testSource/testTarget) javac options to the release (or testRelease) option.",
    "DESC_UseReleaseVersionHint=Matching source/target (or testSource/testTarget) options can be replaced with a single release (or testRelease= option (JEP 247). This enforces API compatibility with the chosen Java release.",
    "# {0} - option name (e.g. release or testRelease)",
    "FIX_UseReleaseVersionHint=Convert to {0} option for strict compatibility checks."})
public class UseReleaseOptionHint implements POMErrorFixProvider {

    // min compiler plugin version for release option support
    private static final ComparableVersion COMPILER_PLUGIN_VERSION = new ComparableVersion("3.6.0");

    // maven version which added the required compiler plugin implicitly
    private static final ComparableVersion MAVEN_VERSION = new ComparableVersion("3.9.0");

    private static final Configuration config = new Configuration(UseReleaseOptionHint.class.getName(),
                TIT_UseReleaseVersionHint(), DESC_UseReleaseVersionHint(), true, Configuration.HintSeverity.WARNING);

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {

        if (prj == null) {
            return List.of();
        }

        // no hints if plugin was downgraded
        NbMavenProject nbproject = prj.getLookup().lookup(NbMavenProject.class);
        if (nbproject != null) {
            // note: this is the embedded plugin version, only useful for downgrade checks
            String pluginVersion = PluginPropertyUtils.getPluginVersion(nbproject.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
            if (pluginVersion != null && new ComparableVersion(pluginVersion).compareTo(COMPILER_PLUGIN_VERSION) <= 0) {
                return List.of();
            }
        }

        Build build = model.getProject().getBuild();

        List<ErrorDescription> hints = new ArrayList<>();

        boolean releaseSupportedByDeclaredPlugin = false;

        if (build != null) {

            for (PluginContainer pc : Arrays.asList(build, build.getPluginManagement())) {
                if (pc == null || pc.getPlugins() == null) {
                    continue;
                }
                Optional<Plugin> compilerPlugin = pc.getPlugins().stream()
                        .filter(p -> Constants.PLUGIN_COMPILER.equals(p.getArtifactId()))
                        .filter(this::isPluginCompatible)
                        .findFirst();

                if (compilerPlugin.isPresent()) {
                    releaseSupportedByDeclaredPlugin |= true;
                    hints.addAll(createHintsForParent("", compilerPlugin.get().getConfiguration()));
                    if (compilerPlugin.get().getExecutions() != null) {
                        for (PluginExecution exec : compilerPlugin.get().getExecutions()) {
                            hints.addAll(createHintsForParent("", exec.getConfiguration()));
                        }
                    }
                }
            }
        }

        // no hints if required version not declared and also not provided by maven
        if (!releaseSupportedByDeclaredPlugin) {
            ComparableVersion mavenVersion = PomModelUtils.getActiveMavenVersion();
            if (mavenVersion == null || mavenVersion.compareTo(MAVEN_VERSION) <= 0) {
                return List.of();
            }
        }

        Properties properties = model.getProject().getProperties();
        if (properties != null) {
            hints.addAll(createHintsForParent("maven.compiler.", properties));
        }

        return hints;
    }

    private List<ErrorDescription> createHintsForParent(String prefix, POMComponent parent) {
        if (parent == null) {
            return List.of();
        }
        List<ErrorDescription> hints = new ArrayList<>();
        for (TagGroup tagGroup : TagGroup.values()) {
            hints.addAll(createHintsForParent(prefix, parent, tagGroup));
        }
        return hints;
    }

    private List<ErrorDescription> createHintsForParent(String prefix, POMComponent parent, TagGroup tagGroup) {
        int source;
        int target;

        // property name or an int value
        String release = null;

        try {
            String sourceText = parent.getChildElementText(POMQName.createQName(prefix+tagGroup.sourceTag, true));
            if (PomModelUtils.isPropertyExpression(sourceText)) {
                release = sourceText;
                sourceText = PomModelUtils.getProperty(parent.getModel(), sourceText);
            }

            String targetText = parent.getChildElementText(POMQName.createQName(prefix+tagGroup.targetTag, true));
            if (PomModelUtils.isPropertyExpression(targetText)) {
                release = targetText;
                targetText = PomModelUtils.getProperty(parent.getModel(), targetText);
            }

            source = Integer.parseInt(sourceText);
            target = Integer.parseInt(targetText);
            if (release == null) {
                release = String.valueOf(target);
            }
        } catch (NumberFormatException ignored) {
            // if source or target is invalid or missing
            return List.of();
        }

        if (source == target && source >= 8) { // ok since 3.13.0+ maps release to source/target on 8
            List<ErrorDescription> hints = new ArrayList<>();
            for (POMComponent prop : parent.getChildren()) {
                String name = prop.getPeer().getNodeName();
                if (name.equals(prefix+tagGroup.sourceTag) || name.equals(prefix+tagGroup.targetTag)) {
                    hints.add(createHintForComponent(prefix, prop, parent.getModel(), release, tagGroup));
                }
            }
            return hints;
        }
        return List.of();
    }

    private ErrorDescription createHintForComponent(String prefix, POMComponent component, POMModel model, String release, TagGroup tagGroup) {
        Line line = NbEditorUtilities.getLine(model.getBaseDocument(), component.findPosition(), false);
        List<Fix> fix = List.of(new ConvertToReleaseOptionFix(prefix, release, component, tagGroup));
        return ErrorDescriptionFactory.createErrorDescription(Severity.HINT, FIX_UseReleaseVersionHint(tagGroup.releaseTag), fix, model.getBaseDocument(), line.getLineNumber()+1);
    }

    /**
     * maven-compiler-plugin version must be >= 3.6
     */
    private boolean isPluginCompatible(Plugin plugin) {
        String version = plugin.getVersion();
        if (version == null || version.isEmpty()) {
            return true; // plugin management, lets assume that it is compatible
        }
        return new ComparableVersion(version).compareTo(COMPILER_PLUGIN_VERSION) >= 0;
    }

    private static class ConvertToReleaseOptionFix implements Fix {

        private final String prefix;
        private final String release;
        private final POMComponent component;
        private final TagGroup tagGroup;

        private ConvertToReleaseOptionFix(String prefix, String release, POMComponent component, TagGroup tagGroup) {
            this.prefix = prefix;
            this.component = component;
            this.release = release;
            this.tagGroup = tagGroup;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ChangeInfo info = new ChangeInfo();
            POMModel model = component.getModel();
            PomModelUtils.implementInTransaction(model, () -> {
                POMComponent parent = component.getParent();
                for (POMComponent child : parent.getChildren()) {
                    String name = child.getPeer().getNodeName();
                    if (name.equals(prefix+tagGroup.sourceTag) || name.equals(prefix+tagGroup.targetTag) || name.equals(prefix+tagGroup.releaseTag)) {
                        parent.removeExtensibilityElement((POMExtensibilityElement) child);
                    }
                }
                POMExtensibilityElement element = model.getFactory().createPOMExtensibilityElement(QName.valueOf(prefix+tagGroup.releaseTag));
                element.setElementText(release);
                parent.addExtensibilityElement(element);
            });
            return info;
        }

        @Override
        public String getText() {
            return FIX_UseReleaseVersionHint(tagGroup.releaseTag);
        }

    }

    @Override
    public void cancel() {}

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public String getSavedValue(JComponent customizer, String key) {
        return null;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    private enum TagGroup {
        STANDARD("source", "target", "release"),
        TEST("testSource", "testTarget", "testRelease");

        private final String sourceTag;
        private final String targetTag;
        private final String releaseTag;

        private TagGroup(String sourceTag, String targetTag, String releaseTag) {
            this.sourceTag = sourceTag;
            this.targetTag = targetTag;
            this.releaseTag = releaseTag;
        }
    }

}
