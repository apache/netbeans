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
package org.netbeans.modules.css.prep.ui.options;

import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.options.CssPrepOptionsValidator;
import org.netbeans.modules.css.prep.sass.SassCli;
import org.netbeans.modules.css.prep.sass.SassCssPreprocessor;
import org.netbeans.modules.css.prep.util.Warnings;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.openide.util.NbBundle;

public class SassOptions implements CssPreprocessorUIImplementation.Options {

    private final SassCssPreprocessor sassCssPreprocessor;

    // @GuardedBy("EDT")
    private SassOptionsPanel panel = null;


    public SassOptions(SassCssPreprocessor sassCssPreprocessor) {
        assert sassCssPreprocessor != null;
        this.sassCssPreprocessor = sassCssPreprocessor;
    }

    @NbBundle.Messages("SassOptions.displayName=Sass")
    @Override
    public String getDisplayName() {
        return Bundle.SassOptions_displayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public SassOptionsPanel getComponent() {
        assert EventQueue.isDispatchThread();
        if (panel == null) {
            panel = new SassOptionsPanel();
        }
        return panel;
    }

    @Override
    public void update() {
        getComponent().setSassPath(getOptions().getSassPath());
        getComponent().setSassOutputOnError(getOptions().getSassOutputOnError());
        getComponent().setSassDebug(getOptions().getSassDebug());
    }

    @Override
    public boolean isValid() {
        return !getValidationResult().hasErrors();
    }

    @Override
    public String getErrorMessage() {
        ValidationResult validationResult = getValidationResult();
        if (validationResult.hasErrors()) {
            return validationResult.getErrors().get(0).getMessage();
        }
        return null;
    }

    @Override
    public String getWarningMessage() {
        ValidationResult validationResult = getValidationResult();
        if (validationResult.hasWarnings()) {
            return validationResult.getWarnings().get(0).getMessage();
        }
        return null;
    }

    @Override
    public boolean changed() {
        String sassPath = getOptions().getSassPath();
        return !getComponent().getSassPath().equals(sassPath == null ? "" : sassPath)
                || getComponent().getSassOutputOnError() != getOptions().getSassOutputOnError()
                || getComponent().getSassDebug()!= getOptions().getSassDebug();
    }

    @Override
    public void save() throws IOException {
        Warnings.resetWarning(CssPreprocessorType.SASS);
        SassCli.resetVersion();
        boolean fire = false;
        // path
        String originalPath = getOptions().getSassPath();
        String path = getComponent().getSassPath();
        getOptions().setSassPath(path);
        if (!path.equals(originalPath)) {
            fire = true;
        }
        // output on error
        getOptions().setSassOutpuOnError(getComponent().getSassOutputOnError());
        // debug
        boolean originalDebug = getOptions().getSassDebug();
        boolean debug = getComponent().getSassDebug();
        getOptions().setSassDebug(debug);
        if (debug != originalDebug) {
            fire = true;
        }
        // changes
        if (fire) {
            sassCssPreprocessor.fireOptionsChanged();
        }
    }

    private ValidationResult getValidationResult() {
        return new CssPrepOptionsValidator()
                .validateSassPath(getComponent().getSassPath(), true)
                .getResult();
    }

    private CssPrepOptions getOptions() {
        return CssPrepOptions.getInstance();
    }

}
