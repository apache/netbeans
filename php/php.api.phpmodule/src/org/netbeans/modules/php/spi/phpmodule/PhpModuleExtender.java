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
package org.netbeans.modules.php.spi.phpmodule;

import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Parameters;

/**
 * Provides support for extending existing PHP module.
 * <p>
 * New instances are created using their {@link Factory factories}.
 * <p>
 * Implementations must be thread safe.
 * @see Factory
 * @since 2.28
 */
public interface PhpModuleExtender {

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this PHP module extender.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    String getIdentifier();

    /**
     * Returns the display name of this PHP module extender. The display name is used
     * in the UI.
     * @return the display name; never {@code null}.
     */
    String getDisplayName();

    /**
     * Attaches a change listener that is to be notified of changes
     * in the extender (e.g., the result of the {@link #isValid} method
     * has changed.
     * @param listener a listener.
     */
    void addChangeListener(@NonNull ChangeListener listener);

    /**
     * Removes a change listener.
     * @param listener a listener.
     */
    void removeChangeListener(@NonNull ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to customize this extender.
     * <p>
     * This method is always called in the UI thread.
     * @return a component or {@code null} if this extender does not provide a configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    @CheckForNull
    JComponent getComponent();

    /**
     * Returns a help context for {@link #getComponent}.
     * @return a help context; can be {@code null}.
     */
    @CheckForNull
    HelpCtx getHelp();

    /**
     * Checks if this extender is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     * <p>
     * If it returns {@code false}, check {@link #getErrorMessage() error message}, it
     * should not be {@code null}.
     * @return {@code true} if the configuration is valid, {@code false} otherwise.
     * @see #getErrorMessage()
     * @see #getWarningMessage()
     */
    boolean isValid();

    /**
     * Get error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}.
     * @return error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}
     * @see #isValid()
     * @see #getWarningMessage()
     */
    @CheckForNull
    String getErrorMessage();

    /**
     * Get warning message that can be not {@code null} even for {@link #isValid() valid} extender.
     * In other words, it is safe to extend PHP module even if this method returns a message.
     * @return warning message or {@code null}
     * @see #isValid()
     * @see #getErrorMessage()
     */
    @CheckForNull
    String getWarningMessage();

    /**
     * Called to extend the given PHP module with this extender. Never called if {@link #isValid()} is {@code false}.
     * @param  phpModule the PHP module to be extended; never {@code null}
     * @return the set of created/modified/important files in the PHP module, can be empty but never {@code null}
     * @throws ExtendingException if extending fails
     * @see #isValid()
     */
    Set<FileObject> extend(PhpModule phpModule) throws ExtendingException;

    //~ Inner classes

    /**
     * Factory for creating {@link PhpModuleExtender}.
     * <p>
     * Implementations are searched on SFS, folder {@value Factory#EXTENDERS_PATH}.
     */
    interface Factory {

        /**
         * Path on SFS.
         */
        String EXTENDERS_PATH = "PHP/Extenders"; // NOI18N

        /**
         * Create new PHP module extender.
         * @return new PHP module extender.
         */
        PhpModuleExtender create();

    }

    /**
     * Exception that is thrown if the {@link PhpModuleExtender#extend(PhpModule) extending operation} fails.
     */
    final class ExtendingException extends Exception {

        private static final long serialVersionUID = 78931657632435457L;


        /**
         * Constructs a new exception with the specified detail failure message.
         * @param failureMessage the detail failure message.
         */
        public ExtendingException(@NonNull String failureMessage) {
            this(failureMessage, null);
        }

        /**
         * Constructs a new exception with the specified detail failure message and cause.
         * @param failureMessage the detail failure message.
         * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
         *        (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
         */
        public ExtendingException(@NonNull String failureMessage, @NullAllowed Throwable cause) {
            super(failureMessage, cause);
            Parameters.notEmpty("failureMessage", failureMessage);
        }

        /**
         * Get the localized message why the {@link PhpModuleExtender#extend(PhpModule) extending operation} failed.
         * @return the localized message why the {@link PhpModuleExtender#extend(PhpModule) extending operation} failed.
         */
        @NonNull
        public String getFailureMessage() {
            return getMessage();
        }

    }

}
