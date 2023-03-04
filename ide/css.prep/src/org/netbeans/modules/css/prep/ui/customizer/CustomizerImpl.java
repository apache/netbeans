/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.prep.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.preferences.CssPreprocessorPreferences;
import org.netbeans.modules.css.prep.util.BaseCssPreprocessor;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.modules.css.prep.util.Warnings;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Pair;

public final class CustomizerImpl implements CssPreprocessorUIImplementation.Customizer, PropertyChangeListener {

    private final BaseCssPreprocessor cssPreprocessor;
    private final Project project;
    private final CssPreprocessorType type;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private CustomizerOptionsPanel customizerPanel = null;
    private volatile OptionsPanel optionsPanel = null;


    public CustomizerImpl(BaseCssPreprocessor cssPreprocessor, Project project, CssPreprocessorType type) {
        assert cssPreprocessor != null;
        assert project != null;
        assert type != null;

        this.cssPreprocessor = cssPreprocessor;
        this.project = project;
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        return type.getDisplayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getOptionsPanel().addChangeListener(listener);
        changeSupport.addChangeListener(listener);
        CssPrepOptions.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        CssPrepOptions.getInstance().removePropertyChangeListener(this);
        changeSupport.removeChangeListener(listener);
        getOptionsPanel().removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        if (customizerPanel == null) {
            customizerPanel = new CustomizerOptionsPanel(getOptionsPanel());
        }
        return customizerPanel;
    }

    public synchronized OptionsPanel getOptionsPanel() {
        if (optionsPanel == null) {
            CssPreprocessorPreferences preferences = type.getPreferences();
            optionsPanel = new OptionsPanel(type, project, preferences.isEnabled(project), preferences.getMappings(project),
                    preferences.getCompilerOptions(project));
        }
        assert optionsPanel != null;
        return optionsPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.css.prep.ui.customizer.CustomizerImpl." + type.name()); // NOI18N
    }

    @Override
    public boolean isValid() {
        return !getValidationResult().hasErrors();
    }

    @Override
    public String getErrorMessage() {
        return getValidationResult().getFirstErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getValidationResult().getFirstWarningMessage();
    }

    @Override
    public void save() throws IOException {
        Warnings.resetWarning(type);
        boolean fire = false;
        CssPreprocessorPreferences preferences = type.getPreferences();
        // configured
        if (getOptionsPanel().isConfigured()) {
            // only if true, otherwise do not change!
            preferences.setConfigured(project, true);
        }
        // enabled
        boolean originalEnabled = preferences.isEnabled(project);
        boolean enabled = getOptionsPanel().isCompilationEnabled();
        preferences.setEnabled(project, enabled);
        if (enabled != originalEnabled) {
            fire = true;
        }
        // mappings
        List<Pair<String, String>> originalMappings = preferences.getMappings(project);
        List<Pair<String, String>> mappings = new ArrayList<>(getOptionsPanel().getMappings());
        preferences.setMappings(project, mappings);
        // #230945
        mappings.removeAll(originalMappings);
        if (!fire
                && !mappings.isEmpty()) {
            fire = true;
        }
        // compiler options
        String originalCompilerOptions = preferences.getCompilerOptions(project);
        String compilerOptions = getOptionsPanel().getCompilerOptions();
        preferences.setCompilerOptions(project, compilerOptions);
        if (!fire
                && !originalCompilerOptions.equals(compilerOptions)) {
            fire = true;
        }
        // change?
        if (fire) {
            cssPreprocessor.fireCustomizerChanged(project);
        }
    }

    private ValidationResult getValidationResult() {
        boolean compilationEnabled = getOptionsPanel().isCompilationEnabled();
        return type.getPreferencesValidator()
                .validateMappings(CssPreprocessorUtils.getWebRoot(project), compilationEnabled, getOptionsPanel().getMappings())
                .validateExecutable(compilationEnabled)
                .getResult();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (type.getExecutablePathPropertyName().equals(evt.getPropertyName())) {
            changeSupport.fireChange();
        }
    }

}
