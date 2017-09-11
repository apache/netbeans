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
package org.netbeans.modules.css.prep.preferences;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.openide.util.Pair;

/**
 * Sass preferences specific for project.
 */
public final class SassPreferences extends BasePreferences implements CssPreprocessorPreferences {

    private static final String CONFIGURED = "sass.configured"; // NOI18N
    private static final String ENABLED = "sass.enabled"; // NOI18N
    private static final String MAPPINGS = "sass.mappings"; // NOI18N
    private static final String COMPILER_OPTIONS = "sass.compiler.options"; // NOI18N

    private static final SassPreferences INSTANCE = new SassPreferences();


    private SassPreferences() {
    }

    public static SassPreferences getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isConfigured(Project project) {
        return isConfigured(project, CONFIGURED);
    }

    @Override
    public void setConfigured(Project project, boolean configured) {
        setConfigured(project, CONFIGURED, configured);
    }

    @Override
    public boolean isEnabled(Project project) {
        return isEnabled(project, ENABLED);
    }

    @Override
    public void setEnabled(Project project, boolean enabled) {
        setEnabled(project, ENABLED, enabled);
    }

    @Override
    public List<Pair<String, String>> getMappings(Project project) {
        return getMappings(project, MAPPINGS, CssPreprocessorType.SASS);
    }

    @Override
    public void setMappings(Project project, List<Pair<String, String>> mappings) {
        setMappings(project, MAPPINGS, mappings);
    }

    @Override
    public String getCompilerOptions(Project project) {
        return getCompilerOptions(project, COMPILER_OPTIONS);
    }

    @Override
    public void setCompilerOptions(Project project, String compilerOptions) {
        setCompilerOptions(project, COMPILER_OPTIONS, compilerOptions);
    }

}
