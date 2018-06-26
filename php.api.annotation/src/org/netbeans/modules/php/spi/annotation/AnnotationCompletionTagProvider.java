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
