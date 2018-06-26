/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.spi.components;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.openide.filesystems.FileObject;

/**
 * This interface allows providing support for JSF component libraries. It can be used
 * to extend a web module and JSF framework with a JSF suite, to find out whether a web
 * module is already extended by a JSF component library, or to retrieve a JSF suite's
 * specific configuration thru the {@link JsfComponentCustomizer}.
 *
 * <p>Instances of this class are registered in the {@value
 * org.netbeans.modules.web.jsfapi.spi.components.JsfComponentProvider#COMPONENTS_PATH}
 * in the module layer, see {@link Registration}.</p>
 *
 * @author Martin Fousek <marfous@netbeans.org>
 * @since 1.27
 */
public interface JsfComponentImplementation {

    /**
     * Returns the name of this JSF component library.
     * <p>
     * <b>This name mustn't be localized since it's used for logging and could appear in configuration files (like in
     * the pom.xml files of the user project). For localized name used {@link #getDisplayName()} instead.</b>
     *
     * @return the name; never {@code null}.
     * @see #getDisplayName()
     */
    @NonNull
    String getName();

    /**
     * Gets display name of the JSF suite.
     * <p>
     * Display name can be localized and it will appear in the wizards and project customizers.
     *
     * @return display name of the JSF component library, never {@code null}
     * @see #getName()
     * @since 1.44
     */
    @NonNull
    String getDisplayName();

    /**
     * Returns the description of this JSF component library.
     *
     * @return the description.
     */
    @NonNull
    String getDescription();

    /**
     * Called to extend the given web module and JSF framework with the JSF
     * component library corresponding to this implementation.
     *
     * @param  webModule the web module to be extended; never null.
     * @param  jsfComponentCustomizer customizer with JSF component library
     * settings for given project (web module)
     * @return the set of newly created files in the web module which should be opened.
     */
    @NonNull
    Set<FileObject> extend(@NonNull WebModule webModule, @NullAllowed JsfComponentCustomizer jsfComponentCustomizer);

    /**
     * Returns for which versions is the JSF component library designed.
     * <p>
     * By creating new project and choosing JSF framework are JSF suites filtered
     * out according to their {@link JSFVersion}s.
     *
     * @return set of {@link JSFVersion} suitable for this JSF component library
     */
    @NonNull
    Set<JSFVersion> getJsfVersion();

    /**
     * Finds out if a given web module has already been extended with this JSF component library.
     * <p>
     * <b>This method should be as fast as possible.</b>
     *
     * @param webmodule the web module; never {@code null}.
     * @return {@code true} if the web module has already been extended with this JSF suite,
     * {@code false} otherwise.
     */
    boolean isInWebModule(@NonNull WebModule webModule);

    /**
     * Returns a new {@link JsfComponentCustomizer} for this JSF component
     * library.
     * <p>
     * For new project is called with {@code null} parameter. In project customizer is called for
     * given {@link WebModule}.
     *
     * @param webmodule the web module for which should be customizer adapted; null if the
     * project doesn't exist yet
     * @return a new customizer; can be {@code null} if the JSF suite doesn't
     * support that and no extending panel should be offered.
     */
    JsfComponentCustomizer createJsfComponentCustomizer(@NullAllowed WebModule webModule);

    /**
     * Performs actions needed for removal JSF component library form the web module.
     *
     * @param webModule the web module from which should be JSF component library removed; never null.
     */
    void remove(@NonNull WebModule webModule);

}
