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
