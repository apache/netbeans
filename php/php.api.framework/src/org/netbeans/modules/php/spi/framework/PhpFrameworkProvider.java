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

package org.netbeans.modules.php.spi.framework;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.openide.util.Parameters;

/**
 * Encapsulates a PHP framework.
 *
 * <p>This class allows providing support for PHP frameworks. It can be used
 * to extend a PHP module with a PHP framework, to find out whether a PHP
 * module is already extended by a PHP framework, or to retrieve a PHP framework's
 * specific configuration files.</p>
 *
 * <p>Instances of this class are registered in the <code>{@value org.netbeans.modules.php.api.phpmodule.PhpFrameworks#FRAMEWORK_PATH}</code>
 * in the module layer, see {@link Registration}.</p>
 *
 * @author Tomas Mysik
 */
public abstract class PhpFrameworkProvider {

    private final String identifier;
    private final String name;
    private final String description;

    /**
     * Creates a new PHP framework with a name and description.
     *
     * @param  identifier the <b>non-localized (usually english)</b> identifier of this PHP framework (e.g., "Symfony Framework"); never <code>null</code>.
     * @param  name the <b>localized</b> name of this PHP framework (e.g., "Symfony PHP Web Framework"); never <code>null</code>.
     * @param  description the description of this PHP framework (e.g., "An open source framework based on the MVC pattern"); can be <code>null</code>.
     * @throws NullPointerException if the <code>identifier</code> or <code>name</code> parameter is <code>null</code>.
     */
    public PhpFrameworkProvider(String identifier, String name, String description) {
        Parameters.notNull("identifier", identifier); // NOI18N
        Parameters.notNull("name", name); // NOI18N

        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this PHP framework.
     *
     * @return the <b>non-localized (usually english)</b> identifier; never <code>null</code>.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the <b>localized</b> name of this PHP framework.
     *
     * @return the name; never <code>null</code>.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the <b>localized</b> name of this PHP framework, specific for the given PHP module.
     * <p>
     * The default implementation simply returns the {@link #getName() name}.
     *
     * @param phpModule the PHP module; never <code>null</code>.
     * @return the name; never <code>null</code>.
     * @since 0.27
     */
    @NonNull
    public String getName(PhpModule phpModule) {
        return name;
    }

    /**
     * Returns the description of this PHP framework. Defaults to the name
     * if a <code>null</code> <code>description</code> parameter was passed to the constructor.
     *
     * @return the description; never <code>null</code>.
     */
    public final String getDescription() {
        if (description != null) {
            return description;
        }
        return getName();
    }

    /**
     * Returns the {@link BadgeIcon badge icon} of this PHP framework.
     * <p>
     * The default implementation returns {@code null}.
     * @return the {@link BadgeIcon badge icon} for this PHP framework or {@code null}
     */
    public BadgeIcon getBadgeIcon() {
        return null;
    }

    /**
     * Finds out if a given PHP module has already been extended with this PHP framework.
     * <p>
     * <b>This method should be as fast as possible.</b>
     *
     * @param  phpModule the PHP module; never <code>null</code>.
     * @return <code>true</code> if the PHP module has already been extended with this framework, <code>false</code> otherwise.
     */
    public abstract boolean isInPhpModule(PhpModule phpModule);

    /**
     * Deprecated, use {@link #getConfigurationFiles2(PhpModule)} instead.
     * <p>
     * Returns the configuration files (no directories allowed!) belonging to this framework. The files
     * do not need to exist, however only existing files are taken into account.
     * <p>
     * These files are displayed under <tt>Configuration Files</tt> node in <tt>Projects</tt> view.
     * <p>
     * <b>This method should be as fast as possible.</b>
     *
     * @param  phpModule the PHP module for which the configuration files are returned; never <code>null</code>.
     * @return an array containing the configuration files; can be empty but never <code>null</code>.
     */
    @Deprecated
    public File[] getConfigurationFiles(PhpModule phpModule) {
        return new File[0];
    }

    /**
     * Returns the configuration files (no directories allowed!) belonging to this framework.
     * These files are displayed under <tt>Configuration Files</tt> node in <tt>Projects</tt> view.
     * <p>
     * Please note that {@link ImportantFilesImplementation#getFiles() files} are not sorted so <b>sorted collection should be returned</b>.
     * Also, {@link ImportantFilesImplementation.FileInfo#getDescription()} is always ignored.
     *
     * @param  phpModule the PHP module for which the configuration files are returned; never <code>null</code>.
     * @return configuration files, can be <code>null</code> if no configuration files exist
     * @since 0.23
     */
    @CheckForNull
    public ImportantFilesImplementation getConfigurationFiles2(PhpModule phpModule) {
        return null;
    }

    /**
     * Creates a {@link PhpModuleExtender PHP module extender} for this framework
     * and the given PHP module.
     * <p>
     * <b>WARNING:</b> This method is called for all PHP modules (with or without this framework present).
     *
     * @param  phpModule the PHP module to be extended; can be <code>null</code>, e.g., if the
     *         method is called while creating a new PHP application, in which
     *         case the module doesn't exist yet.
     * @return a new PHP module extender; can be <code>null</code> if the framework doesn't support
     *         extending (either PHP modules in general or the particular PHP module
     *         passed in the <code>phpModule</code> parameter).
     */
    public abstract PhpModuleExtender createPhpModuleExtender(PhpModule phpModule);

    /**
     * Creates a {@link PhpModuleCustomizerExtender PHP module customizer extender} for this framework
     * and the given PHP module.
     * <p>
     * <p>
     * <b>WARNING:</b> This method is called for all PHP modules (with or without this framework present,
     * this is usually useful for adding framework to such {@link PhpModule PHP module}).
     * <p>
     * The default implementation returns {@code null}.
     *
     * @param  phpModule the PHP module which properties are to be extended
     * @return a new PHP module customizer extender; can be {@code null} if the framework doesn't need
     *         to store/read any PHP module specific properties (or does not need to be added to PHP module)
     */
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return null;
    }

    /**
     * Get {@link PhpModuleProperties PHP module properties} the given PHP module. PHP framework
     * can provide default values for any property (e.g. web root).
     * <p>
     * This method is called only for PHP modules with this framework present.
     *
     * @param  phpModule the PHP module which properties are going to be changed
     * @return new PHP module properties
     */
    public abstract PhpModuleProperties getPhpModuleProperties(PhpModule phpModule);

    /**
     * Get a {@link PhpModuleActionsExtender PHP module actions extender} for this framework
     * and the given PHP module.
     * <p>
     * This method is called only for PHP modules with this framework present.
     *
     * @param  phpModule the PHP module which actions are going to be extended
     * @return a new PHP module actions extender, can be <code>null</code> if the framework doesn't support
     *         extending of actions
     */
    public abstract PhpModuleActionsExtender getActionsExtender(PhpModule phpModule);

    /**
     * Get a {@link PhpModuleIgnoredFilesExtender PHP module ignored files extender} for this framework
     * and the given PHP module.
     * <p>
     * This method is called only for PHP modules with this framework present.
     *
     * @param  phpModule the PHP module which ignored files are going to be extended
     * @return PHP module ignored files extender, can be <code>null</code> if the framework doesn't need
     *         to recommend to hide any files or folders
     */
    public abstract PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule);

    /**
     * Get a {@link FrameworkCommandSupport framework command support} for this framework
     * and the given PHP module.
     * <p>
     * This method is called only for PHP modules with this framework present.
     *
     * @param  phpModule the PHP module for which framework command support is to be gotten
     * @return framework command support, can be <code>null</code> if the framework doesn't support
     *         running external commands
     */
    public abstract FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule);

    /**
     * Get a {@link EditorExtender editor extender} for this framework
     * and the given PHP module.
     * <p>
     * This method is called only for PHP modules with this framework present.
     *
     * @param  phpModule the PHP module for which editor extender is to be gotten
     * @return editor extender, can be <code>null</code> if the framework doesn't provide
     *         any additional fields/classes etx. to code completion etc.
     */
    public EditorExtender getEditorExtender(PhpModule phpModule) {
        return null;
    }

    /**
     * Get list of {@link AnnotationCompletionTagProvider annotations providers} for this framework
     * and the given PHP module.
     * <p>
     * This method is called only for PHP modules with this framework present.
     * <p>
     * The default implementation returns empty list.
     *
     * @param  phpModule the PHP module for which annotations provider is to be gotten
     * @return list of annotations providers, never <code>null</code>; empty list if the framework doesn't provide
     *         any PHP annotations
     */
    public List<AnnotationCompletionTagProvider> getAnnotationsCompletionTagProviders(PhpModule phpModule) {
        return Collections.emptyList();
    }

    /**
     * This method is called when the PHP module is opened in the IDE. It is suitable to make any detection/initialization
     * of this framework here but usually it is not needed to override this method.
     * <p>
     * <b>WARNING:</b> This method is called for all PHP modules (with or without this framework present,
     * this is usually useful for framework detection in such {@link PhpModule PHP module}).
     * <p>
     * @param phpModule the PHP module that is being opened in the IDE
     * @see #isInPhpModule(PhpModule)
     * @see #phpModuleClosed(PhpModule)
     * @see PhpModule#notifyPropertyChanged(java.beans.PropertyChangeEvent)
     */
    public void phpModuleOpened(PhpModule phpModule) {
    }

    /**
     * This method is called when the PHP module is closed in the IDE. It is suitable to make any clean up
     * of this framework here but usually it is not needed to override this method.
     * <p>
     * <b>WARNING:</b> This method is called for all PHP modules (with or without this framework present,
     * this is usually useful for framework clean up in such {@link PhpModule PHP module}).
     * <p>
     * @param phpModule the PHP module that is being closed in the IDE
     * @see #isInPhpModule(PhpModule)
     * @see #phpModuleOpened(PhpModule)
     * @see PhpModule#notifyPropertyChanged(java.beans.PropertyChangeEvent)
     */
    public void phpModuleClosed(PhpModule phpModule) {
    }

    /**
     * Declarative registration of a singleton PHP framework provider.
     * By marking an implementation class or a factory method with this annotation,
     * you automatically register that implementation, normally in {@link org.netbeans.modules.php.api.phpmodule.PhpFrameworks#FRAMEWORK_PATH}.
     * The class must be public and have:
     * <ul>
     *  <li>a public no-argument constructor, or</li>
     *  <li>a public static factory method.</li>
     * </ul>
     *
     * <p>Example of usage:
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
     * &#64;PhpFrameworkProvider.Registration(position=100)
     * public class MyFramework extends PhpFrameworkProvider {...}
     * </pre>
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
     * public class MyFramework extends PhpFrameworkProvider {
     *     &#64;PhpFrameworkProvider.Registration(position=100)
     *     public static PhpFrameworkProvider getInstance() {...}
     * }
     * </pre>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {
        /**
         * An optional position in which to register this framework provider relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Providers with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;
    }
}
