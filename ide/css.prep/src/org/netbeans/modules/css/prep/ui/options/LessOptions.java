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
import org.netbeans.modules.css.prep.less.LessCssPreprocessor;
import org.netbeans.modules.css.prep.less.LessExecutable;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.options.CssPrepOptionsValidator;
import org.netbeans.modules.css.prep.util.Warnings;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.openide.util.NbBundle;

public class LessOptions implements CssPreprocessorUIImplementation.Options {

    private final LessCssPreprocessor lessCssPreprocessor;

    // @GuardedBy("EDT")
    private LessOptionsPanel panel = null;


    public LessOptions(LessCssPreprocessor lessCssPreprocessor) {
        assert lessCssPreprocessor != null;
        this.lessCssPreprocessor = lessCssPreprocessor;
    }

    @NbBundle.Messages("LessOptions.displayName=LESS")
    @Override
    public String getDisplayName() {
        return Bundle.LessOptions_displayName();
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
    public LessOptionsPanel getComponent() {
        assert EventQueue.isDispatchThread();
        if (panel == null) {
            panel = new LessOptionsPanel();
        }
        return panel;
    }

    @Override
    public void update() {
        getComponent().setLessPath(getOptions().getLessPath());
        getComponent().setLessOutputOnError(getOptions().getLessOutputOnError());
        getComponent().setLessDebug(getOptions().getLessDebug());
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
        String lessPath = getOptions().getLessPath();
        return !getComponent().getLessPath().equals(lessPath == null ? "" : lessPath)
                || getComponent().getLessOutputOnError() != getOptions().getLessOutputOnError()
                || getComponent().getLessDebug()!= getOptions().getLessDebug();
    }

    @Override
    public void save() throws IOException {
        Warnings.resetWarning(CssPreprocessorType.LESS);
        LessExecutable.resetVersion();
        boolean fire = false;
        // path
        String originalPath = getOptions().getLessPath();
        String path = getComponent().getLessPath();
        getOptions().setLessPath(path);
        if (!path.equals(originalPath)) {
            fire = true;
        }
        // output on error
        getOptions().setLessOutpuOnError(getComponent().getLessOutputOnError());
        // debug
        boolean originalDebug = getOptions().getLessDebug();
        boolean debug = getComponent().getLessDebug();
        getOptions().setLessDebug(debug);
        if (debug != originalDebug) {
            fire = true;
        }
        // changes
        if (fire) {
            lessCssPreprocessor.fireOptionsChanged();
        }
    }

    private ValidationResult getValidationResult() {
        return new CssPrepOptionsValidator()
                .validateLessPath(getComponent().getLessPath(), true)
                .getResult();
    }

    private CssPrepOptions getOptions() {
        return CssPrepOptions.getInstance();
    }

}
