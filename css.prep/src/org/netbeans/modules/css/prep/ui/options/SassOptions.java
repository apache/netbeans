/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
