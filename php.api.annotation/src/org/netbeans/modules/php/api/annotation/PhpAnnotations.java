/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.api.annotation;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to:
 *
 * <ol>
 * <li>
 * <p>the list of registered PHP annotations providers
 * that are <b>globally</b> available (it means that their annotations are available
 * in every PHP file). For <b>framework specific</b> annotations, use
 * {@link org.netbeans.modules.php.spi.framework.PhpFrameworkProvider#getAnnotationsCompletionTagProviders(org.netbeans.modules.php.api.phpmodule.PhpModule)}.</p>
 *
 * <p>The path is {@value #ANNOTATIONS_COMPLETION_TAG_PROVIDERS_PATH} on SFS.</p>
 * </li>
 *
 * <li>
 * <p>the list of registered PHP annotation line parsers that are <b>globally</b>
 * available.</p>
 *
 * <p>The path is {@value #ANNOTATIONS_LINE_PARSERS_PATH} on SFS.</p>
 * </li>
 * </ol>
 */
public final class PhpAnnotations {

    public static final String ANNOTATIONS_COMPLETION_TAG_PROVIDERS_PATH = "PHP/Annotations"; // NOI18N

    public static final String ANNOTATIONS_LINE_PARSERS_PATH = "PHP/Annotations/Line/Parsers"; // NOI18N

    private static final Lookup.Result<AnnotationCompletionTagProvider> COMPLETION_TAG_PROVIDERS = Lookups.forPath(ANNOTATIONS_COMPLETION_TAG_PROVIDERS_PATH).lookupResult(AnnotationCompletionTagProvider.class);

    private static final Lookup.Result<AnnotationLineParser> LINE_PARSERS = Lookups.forPath(ANNOTATIONS_LINE_PARSERS_PATH).lookupResult(AnnotationLineParser.class);

    private PhpAnnotations() {
    }

    /**
     * Get all registered {@link AnnotationCompletionTagProvider}s
     * that are <b>globally</b> available (it means that their annotations are available
     * in every PHP file). For <b>framework specific</b> annotations, use
     * {@link org.netbeans.modules.php.spi.framework.PhpFrameworkProvider#getAnnotationsCompletionTagProviders(org.netbeans.modules.php.api.phpmodule.PhpModule)}.
     * @return a list of all registered {@link AnnotationCompletionTagProvider}s; never {@code null}
     */
    public static List<AnnotationCompletionTagProvider> getCompletionTagProviders() {
        return new ArrayList<AnnotationCompletionTagProvider>(COMPLETION_TAG_PROVIDERS.allInstances());
    }

    /**
     * Add {@link LookupListener listener} to be notified when annotations providers change
     * (new provider added, existing removed).
     * <p>
     * To avoid memory leaks, do not forget to {@link #removeCompletionTagProvidersListener(LookupListener) remove} the listener.
     * @param listener {@link LookupListener listener} to be added
     * @see #removeCompletionTagProvidersListener(LookupListener)
     */
    public static void addCompletionTagProvidersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        COMPLETION_TAG_PROVIDERS.addLookupListener(listener);
    }

    /**
     * Remove {@link LookupListener listener}.
     * @param listener {@link LookupListener listener} to be removed
     * @see #addCompletionTagProvidersListener(LookupListener)
     */
    public static void removeCompletionTagProvidersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        COMPLETION_TAG_PROVIDERS.removeLookupListener(listener);
    }

    /**
     * Get all registered {@link AnnotationLineParser}s that are
     * <b>globally</b> available.
     *
     * @return a list of all registered {@link AnnotationLineParser}s; never {@code null}
     */
    public static List<AnnotationLineParser> getLineParsers() {
        return new ArrayList<AnnotationLineParser>(LINE_PARSERS.allInstances());
    }

    /**
     * Add {@link LookupListener listener} to be notified when line parsers change
     * (new parser added, existing removed).
     * <p>
     * To avoid memory leaks, do not forget to {@link #removeLineParsersListener(LookupListener) remove} the listener.
     *
     * @param listener {@link LookupListener listener} to be added
     * @see #removeLineParsersListener(LookupListener)
     */
    public static void addLineParsersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        LINE_PARSERS.addLookupListener(listener);
    }

    /**
     * Remove {@link LookupListener listener}.
     *
     * @param listener {@link LookupListener listener} to be removed
     * @see #addLineParsersListener(LookupListener)
     */
    public static void removeLineParsersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        LINE_PARSERS.removeLookupListener(listener);
    }

}
