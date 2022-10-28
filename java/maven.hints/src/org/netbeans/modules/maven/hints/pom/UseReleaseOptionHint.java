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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
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
    "TIT_UseReleaseVersionHint=Convert matching source/target javac options to the release option.",
    "DESC_UseReleaseVersionHint=Matching source/target options can be replaced with a single release option (JEP 247). This enforces API compatibility with the chosen Java release.",
    "FIX_UseReleaseVersionHint=Convert to release option for strict compatibility checks."})
public class UseReleaseOptionHint implements POMErrorFixProvider {

    private static final String TARGET_TAG = "target";
    private static final String SOURCE_TAG = "source";
    private static final String RELEASE_TAG = "release";

    private static final Configuration config = new Configuration(UseReleaseOptionHint.class.getName(),
                TIT_UseReleaseVersionHint(), DESC_UseReleaseVersionHint(), true, Configuration.HintSeverity.WARNING);

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {

        Build build = model.getProject().getBuild();

        if (build != null && build.getPlugins() != null) {

            List<ErrorDescription> hints = new ArrayList<>();
            Optional<Plugin> compilerPlugin = build.getPlugins().stream()
                    .filter((p) -> "maven-compiler-plugin".equals(p.getArtifactId()))
                    .filter(this::isPluginCompatible)
                    .findFirst();

            if (compilerPlugin.isPresent()) {
                hints.addAll(createHintsForParent("", compilerPlugin.get().getConfiguration()));
                if (compilerPlugin.get().getExecutions() != null) {
                    for (PluginExecution exec : compilerPlugin.get().getExecutions()) {
                        hints.addAll(createHintsForParent("", exec.getConfiguration()));
                    }
                }
            } else {
                return Collections.emptyList();
            }

            Properties properties = model.getProject().getProperties();
            if (properties != null) {
                hints.addAll(createHintsForParent("maven.compiler.", properties));
            }

            return hints;
        }

        return Collections.emptyList();
    }

    private List<ErrorDescription> createHintsForParent(String prefix, POMComponent parent) {

        if (parent == null) {
            return Collections.emptyList();
        }

        int source;
        int target;

        // property name or an int value
        String release = null;

        try {
            String sourceText = parent.getChildElementText(POMQName.createQName(prefix+SOURCE_TAG, true));
            if (isProperty(sourceText)) {
                release = sourceText;
                sourceText = getProperty(sourceText, parent.getModel());
            }

            String targetText = parent.getChildElementText(POMQName.createQName(prefix+TARGET_TAG, true));
            if (isProperty(targetText)) {
                release = targetText;
                targetText = getProperty(targetText, parent.getModel());
            }

            source = Integer.parseInt(sourceText);
            target = Integer.parseInt(targetText);
            if (release == null) {
                release = String.valueOf(target);
            }
        } catch (NumberFormatException ignored) {
            // if source or target is invalid or missing
            return Collections.emptyList();
        }

        if (source == target && source >= 9) {
            List<ErrorDescription> hints = new ArrayList<>();
            for (POMComponent prop : parent.getChildren()) {
                String name = prop.getPeer().getNodeName();
                if (name.equals(prefix+SOURCE_TAG) || name.equals(prefix+TARGET_TAG)) {
                    hints.add(createHintForComponent(prefix, prop, parent.getModel(), release));
                }
            }
            return hints;
        }
        return Collections.emptyList();
    }

    private ErrorDescription createHintForComponent(String prefix, POMComponent component, POMModel model, String release) {
        Line line = NbEditorUtilities.getLine(model.getBaseDocument(), component.findPosition(), false);
        List<Fix> fix = Collections.singletonList(new ConvertToReleaseOptionFix(prefix, release, component));
        return ErrorDescriptionFactory.createErrorDescription(Severity.HINT, FIX_UseReleaseVersionHint(), fix, model.getBaseDocument(), line.getLineNumber()+1);
    }

    /**
     * maven-compiler-plugin version must be >= 3.6
     */
    private boolean isPluginCompatible(Plugin plugin) {
        String string = plugin.getVersion();
        if (string == null) {
            return false;
        }
        String[] version = string.split("-")[0].split("\\.");
        try {
            int major = version.length > 0 ? Integer.parseInt(version[0]) : 0;
            int minor = version.length > 1 ? Integer.parseInt(version[1]) : 0;
            if (major < 3 || (major == 3 && minor < 6)) {
                return false;
            }
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }

    private static boolean isProperty(String property) {
        return property != null && property.startsWith("$");
    }

    private static String getProperty(String prop, POMModel model) {
        if (prop.length() > 3) {
            Properties properties = model.getProject().getProperties();
            if (properties != null) {
                return properties.getProperty(prop.substring(2, prop.length()-1));
            }
        }
        return null;
    }

    private static class ConvertToReleaseOptionFix implements Fix {

        private final String prefix;
        private final String release;
        private final POMComponent component;

        private ConvertToReleaseOptionFix(String prefix, String release, POMComponent component) {
            this.prefix = prefix;
            this.component = component;
            this.release = release;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ChangeInfo info = new ChangeInfo();
            POMModel model = component.getModel();
            PomModelUtils.implementInTransaction(model, () -> {
                POMComponent parent = component.getParent();
                for (POMComponent child : parent.getChildren()) {
                    String name = child.getPeer().getNodeName();
                    if (name.equals(prefix+SOURCE_TAG) || name.equals(prefix+TARGET_TAG) || name.equals(prefix+RELEASE_TAG)) {
                        parent.removeExtensibilityElement((POMExtensibilityElement) child);
                    }
                }
                POMExtensibilityElement element = model.getFactory().createPOMExtensibilityElement(QName.valueOf(prefix+RELEASE_TAG));
                element.setElementText(release);
                parent.addExtensibilityElement(element);
            });
            return info;
        }

        @Override
        public String getText() {
            return FIX_UseReleaseVersionHint();
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

}
