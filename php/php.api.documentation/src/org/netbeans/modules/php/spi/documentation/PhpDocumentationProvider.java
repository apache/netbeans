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

package org.netbeans.modules.php.spi.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizer;
import org.openide.util.Parameters;

/**
 * Encapsulates a PHP documentation provider. Provider might be interested in storing
 * and/or reading provider specific values (for example, a directory for generating documentation)
 * from {@link PhpModule#getPreferences(Class, boolean) preferences).
 *
 * <p>This class allows providing support for generating PHP documentation.</p>
 *
 * <p>Instances of this class are registered in the <code>{@value org.netbeans.modules.php.api.documentation.PhpDocs#DOCS_PATH}</code>
 * in the module layer.</p>
 *
 * @author Tomas Mysik
 */
public abstract class PhpDocumentationProvider {

    private final String name;
    private final String displayName;

    /**
     * Creates a new PHP documentation provider with a name and display name.
     *
     * @param  name the short name of this PHP documentation provider (e.g., "PhpDoc"), should not be localized; never <code>null</code>
     * @param  displayName the display name of the provider, should be localized; never <code>null</code>
     */
    public PhpDocumentationProvider(@NonNull String name, @NonNull String displayName) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("displayName", displayName); // NOI18N

        this.name = name;
        this.displayName = displayName;
    }

    /**
     * Returns the name of this PHP documentation provider.
     *
     * @return the name; never <code>null</code>
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the display name of this PHP documentation provider. The display name is used
     * in the UI, e.g. in context menu action.
     *
     * @return the display name; never <code>null</code>
     */
    public final String getDisplayName() {
        return displayName;
    }

    /**
     * Finds out if a given PHP module contains documentation for this PHP documentation provider.
     * <p>
     * <b>This method should be as fast as possible.</b>
     * <p>
     * The default implementation simply returns {@code true} as it is acceptable in most of the cases.
     *
     * @param  phpModule the PHP module; never <code>null</code>
     * @return <code>true</code> if the PHP module already contains documentation for this PHP documentation provider, <code>false</code> otherwise.
     * @see #notifyEnabled(PhpModule, boolean)
     */
    public boolean isInPhpModule(@NonNull PhpModule phpModule) {
        return true;
    }

    /**
     * Creates customizer for given PHP module.
     * @param phpModule PHP module, never {@code null}
     * @return customizer for given PHP module or {@code null} if no customization is needed/available.
     */
    @CheckForNull
    public PhpModuleCustomizer createPhpModuleCustomizer(PhpModule phpModule) {
        return null;
    }

    /**
     * Generate PHP documentation for the given PHP module. Called only if {@link #isInPhpModule(PhpModule)} returns {@code true}.
     * <p>
     * This method runs in a background thread.
     * @param  phpModule the PHP module; never <code>null</code>
     * @see #isInPhpModule(PhpModule)
     * @see PhpModule#getPreferences(Class, boolean)
     */
    public abstract void generateDocumentation(@NonNull PhpModule phpModule);

    /**
     * Notify when this provider is enabled or disabled. Typically, provider
     * stores the state in its properties and returns it when {@link #isInPhpModule(PhpModule)}
     * is called.
     * @param phpModule PHP module, never {@code null}
     * @param enabled {@code true} if enabled, {@code false} otherwise
     * @see #isInPhpModule(PhpModule)
     * @since 0.12
     */
    public void notifyEnabled(@NonNull PhpModule phpModule, boolean enabled) {
        // noop
    }

    /**
     * Declarative registration of a singleton PHP documentation provider provider.
     * By marking an implementation class or a factory method with this annotation,
     * you automatically register that implementation, normally in {@link org.netbeans.modules.php.api.documentation.PhpDocs#DOCS_PATH}.
     * The class must be public and have:
     * <ul>
     *  <li>a public no-argument constructor, or</li>
     *  <li>a public static factory method.</li>
     * </ul>
     *
     * <p>Example of usage:
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
     * &#64;PhpDocumentationProvider.Registration(position=100)
     * public class MyDoc extends PhpDocumentationProvider {...}
     * </pre>
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
     * public class MyDoc extends PhpDocumentationProvider {
     *     &#64;PhpDocumentationProvider.Registration(position=100)
     *     public static PhpDocumentationProvider getInstance() {...}
     * }
     * </pre>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {
        /**
         * An optional position in which to register this documentation provider relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Providers with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;
    }
}
