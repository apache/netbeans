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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
