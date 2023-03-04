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
package org.netbeans.modules.php.spi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.util.Parameters;

/**
 * Encapsulates a PHP annotations completion provider.
 *
 * <p>This class allows providing support for completion of PHP annotations.</p>
 *
 * <p>Globally available (annotations available in all PHP files) instances of this class are registered
 * in the <code>{@value org.netbeans.modules.php.api.annotations.PhpAnnotations#ANNOTATIONS_PATH}</code>
 * in the module layer, see {@link Registration}. For <b>framework specific</b> annotations, use
 * {@link org.netbeans.modules.php.spi.framework.PhpFrameworkProvider#getAnnotationsProvider(org.netbeans.modules.php.api.phpmodule.PhpModule)}.</p>

 * @see org.netbeans.modules.php.spi.framework.PhpFrameworkProvider#getAnnotationsProvider(PhpModule)
 */
public abstract class AnnotationCompletionTagProvider {

    private final String identifier;
    private final String name;
    private final String description;


    /**
     * Create a new PHP annotations provider with a name and description.
     *
     * @param  identifier the <b>non-localized (usually english)</b> identifier of this PHP annotations
     *         provider (e.g., "Symfony Annotations"); never {@code null}
     * @param  name <b>short, localized</b> name of this PHP annotations provider (e.g., "Symfony");
     *         never {@code null}
     * @param  description the description of this PHP annotations provider (e.g., "PHP annotations
     *        for an open source framework based on the MVC pattern."); can be {@code null}
     * @throws NullPointerException if the {@code identifier} or {@code name} parameter is {@code null}
     */
    public AnnotationCompletionTagProvider(@NonNull String identifier, @NonNull String name, @NullAllowed String description) {
        Parameters.notNull("identifier", identifier); // NOI18N
        Parameters.notNull("name", name); // NOI18N

        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    /**
     * Get <b>non-localized (usually english)</b> identifier of this PHP annotations provider.
     *
     * @return <b>non-localized (usually english)</b> identifier; never {@code null}
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Get the <b>short, localized</b> name of this PHP annotations provider.
     *
     * @return name; never {@code null}
     */
    public final String getName() {
        return name;
    }

    /**
     * Get the description of this PHP annotations provider. Defaults to the name
     * if a {@code null} {@code description} parameter was passed to the constructor.
     *
     * @return the description; never {@code null}
     */
    public final String getDescription() {
        if (description != null) {
            return description;
        }
        return getName();
    }

    /**
     * Get all possible annotations.
     * <p>
     * Default implementation simply return all the possible annotations.
     * @return all possible annotations
     */
    public List<AnnotationCompletionTag> getAnnotations() {
        Set<AnnotationCompletionTag> annotations = new LinkedHashSet<AnnotationCompletionTag>();
        annotations.addAll(getFunctionAnnotations());
        annotations.addAll(getTypeAnnotations());
        annotations.addAll(getFieldAnnotations());
        annotations.addAll(getMethodAnnotations());
        return new ArrayList<AnnotationCompletionTag>(annotations);
    }

    /**
     * Get annotations that are available for global functions.
     * @return annotations that are available for global functions
     */
    public abstract List<AnnotationCompletionTag> getFunctionAnnotations();

    /**
     * Get annotations that are available for types (classes, interfaces).
     * @return annotations that are available for types (classes, interfaces)
     */
    public abstract List<AnnotationCompletionTag> getTypeAnnotations();

    /**
     * Get annotations that are available for type fields.
     * @return annotations that are available for type fields
     */
    public abstract List<AnnotationCompletionTag> getFieldAnnotations();

    /**
     * Get annotations that are available for type methods.
     * @return annotations that are available for type methods
     */
    public abstract List<AnnotationCompletionTag> getMethodAnnotations();

    //~ Inner classes

    /**
     * Declarative registration of a singleton PHP annotations provider.
     * that is <b>globally</b> available (it means that its annotations are available
     * in every PHP file). For <b>framework specific</b> annotations, use
     * {@link org.netbeans.modules.php.spi.framework.PhpFrameworkProvider#getAnnotationsProvider(org.netbeans.modules.php.api.phpmodule.PhpModule)}.
     * <p>
     * By marking an implementation class or a factory method with this annotation,
     * you automatically register that implementation, normally in {@link org.netbeans.modules.php.api.annotations.PhpAnnotations#ANNOTATIONS_PATH}.
     * The class must be public and have:
     * <ul>
     *  <li>a public no-argument constructor, or</li>
     *  <li>a public static factory method.</li>
     * </ul>
     *
     * <p>Example of usage:
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
     * &#64;AnnotationCompletionTagProvider.Registration(position=100)
     * public class MyAnnotations extends AnnotationCompletionTagProvider {...}
     * </pre>
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.phpmodule.AnnotationCompletionTagProvider;
     * public class MyAnnotations extends AnnotationCompletionTagProvider {
     *     &#64;AnnotationCompletionTagProvider.Registration(position=100)
     *     public static AnnotationCompletionTagProvider getInstance() {...}
     * }
     * </pre>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {

        /**
         * An optional position in which to register this annotations provider relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Providers with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;

    }

}
