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
package org.netbeans.modules.web.common.ui.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.web.common.api.CssPreprocessors.PREPROCESSORS_PATH;
import org.netbeans.modules.web.common.ui.cssprep.CssPrepOptionsPanelController;
import org.netbeans.modules.web.common.ui.cssprep.CssPreprocessorAccessor;
import org.netbeans.modules.web.common.ui.cssprep.CssPreprocessorsAccessor;
import org.netbeans.modules.web.common.ui.cssprep.CssPreprocessorsCustomizer;
import org.netbeans.modules.web.common.ui.cssprep.CssPreprocessorsProblemProvider;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered UI for CSS preprocessors.
 * The path for registration is "{@value #PREPROCESSORS_PATH}" on SFS.
 * <p>
 * This class is thread safe.
 * @since 1.0
 */
public final class CssPreprocessorsUI {

    /**
     * Category name in Project Customizer.
     * @see #createCustomizer()
     * @since 1.41
     */
    public static final String CUSTOMIZER_IDENT = "CssPreprocessors"; // NOI18N
    /**
     * Top level category name in IDE Options.
     * @since 1.43
     */
    public static final String OPTIONS_CATEGORY = "Html5"; // NOI18N
    /**
     * Subcategory name in IDE Options.
     * @since 1.43
     */
    public static final String OPTIONS_SUBCATEGORY = "CssPreprocessors"; // NOI18N
    /**
     * Full path in IDE Options.
     * @since 1.43
     */
    public static final String OPTIONS_PATH = OPTIONS_CATEGORY + "/" + OPTIONS_SUBCATEGORY; // NOI18N

    private static final Lookup.Result<CssPreprocessorUIImplementation> PREPROCESSORS = Lookups.forPath(PREPROCESSORS_PATH).lookupResult(CssPreprocessorUIImplementation.class);
    private static final CssPreprocessorsUI INSTANCE = new CssPreprocessorsUI();
    
    private final List<CssPreprocessorUI> preprocessors = new CopyOnWriteArrayList<>();
    
    static {
        PREPROCESSORS.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                INSTANCE.reinitProcessors();
            }
        });
        CssPreprocessorsAccessor.setDefault(new CssPreprocessorsAccessor() {
            @Override
            public List<CssPreprocessorUI> getPreprocessors() {
                return INSTANCE.getPreprocessors();
            }
        });
    }
    
    private CssPreprocessorsUI() {
        initProcessors();
    }
    
    /**
     * Get CssPreprocessorsUI instance.
     * @return CssPreprocessorsUI instance
     * @since 1.40
     */
    public static CssPreprocessorsUI getDefault() {
        return INSTANCE;
    }

    /**
     * Create project customizer for CSS preprocessors.
     * <p>
     * Category name is {@link #CUSTOMIZER_IDENT} ({@value #CUSTOMIZER_IDENT}).
     * <p>
     * Instance of this class can be registered for any project in its project customizer SFS folder.
     * @return project customizer for CSS preprocessors
     * @see ProjectCustomizer.CompositeCategoryProvider.Registration
     * @since 1.40
     */
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer() {
        return new CssPreprocessorsCustomizer();
    }

    /**
     * Create IDE Options for CSS preprocessors.
     * <p>
     * Options category is {@link #OPTIONS_CATEGORY} ({@value #OPTIONS_CATEGORY}) and
     * subcategory is {@link #OPTIONS_SUBCATEGORY} ({@value #OPTIONS_SUBCATEGORY}). The whole
     * path is {@link #OPTIONS_PATH} ({@value #OPTIONS_PATH}).
     * @return IDE Options for CSS preprocessors
     * @since 1.76
     */
    public OptionsPanelController createOptions() {
        return new CssPrepOptionsPanelController();
    }

    /**
     * Create provider of CSS preprocessors problems.
     * @param project project to be created provider for
     * @return provider of CSS preprocessors problems
     * @since 1.51
     */
    public ProjectProblemsProvider createProjectProblemsProvider(Project project) {
        return CssPreprocessorsProblemProvider.create(project);
    }

    List<CssPreprocessorUI> getPreprocessors() {
        return new ArrayList<>(preprocessors);
    }

    private void initProcessors() {
        assert preprocessors.isEmpty() : "Empty preprocessors expected but: " + preprocessors;
        preprocessors.addAll(map(PREPROCESSORS.allInstances()));
    }

    void reinitProcessors() {
        synchronized (preprocessors) {
            clearProcessors();
            initProcessors();
        }
    }

    private void clearProcessors() {
        preprocessors.clear();
    }
    
    private List<CssPreprocessorUI> map(Collection<? extends CssPreprocessorUIImplementation> preprocessors) {
        List<CssPreprocessorUI> result = new ArrayList<>();
        for (CssPreprocessorUIImplementation cssPreprocessor : preprocessors) {
            result.add(CssPreprocessorAccessor.getDefault().create(cssPreprocessor));
        }
        return result;
    }

}
