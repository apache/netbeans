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

import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Parameters;

/**
 * Provides support for extending a PHP module with a PHP framework, that is,
 * it allows to modify the PHP module to make use of the framework.
 *
 * @author Tomas Mysik
 */
public abstract class PhpModuleExtender {

    /**
     * Attaches a change listener that is to be notified of changes
     * in the extender (e.g., the result of the {@link #isValid} method
     * has changed.
     *
     * @param  listener a listener.
     */
    public abstract void addChangeListener(ChangeListener listener);

    /**
     * Removes a change listener.
     *
     * @param  listener a listener.
     */
    public abstract void removeChangeListener(ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to customize this extender.
     *
     * @return a component or <code>null</code> if this extender does not provide a configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    public abstract JComponent getComponent();

    /**
     * Returns a help context for {@link #getComponent}.
     *
     * @return a help context; can be <code>null</code>.
     */
    public abstract HelpCtx getHelp();

    /**
     * Checks if this extender is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     * <p>
     * If it returns <code>false</code>, check {@link #getErrorMessage() error message}, it
     * should not be <code>null</code>.
     *
     * @return <code>true</code> if the configuration is valid, <code>false</code> otherwise.
     * @see #getErrorMessage()
     * @see #getWarningMessage()
     */
    public abstract boolean isValid();

    /**
     * Get error message or <code>null</code> if the {@link #getComponent component} is {@link #isValid() valid}.
     * @return error message or <code>null</code> if the {@link #getComponent component} is {@link #isValid() valid}
     * @see #isValid()
     * @see #getWarningMessage()
     */
    public abstract String getErrorMessage();

    /**
     * Get warning message that can be not <code>null</code> even for {@link #isValid() valid} extender.
     * In other words, it is safe to extend PHP module even if this method returns a message.
     * @return warning message or <code>null</code>
     * @see #isValid()
     * @see #getErrorMessage()
     */
    public abstract String getWarningMessage();

    /**
     * Called to extend the given PHP module with the PHP framework
     * corresponding to this extender. Can fail if {@link #isValid()} is <code>false</code>.
     * <p>
     * After extending, {@link PhpFrameworkProvider#isInPhpModule(PhpModule)} is expected to be <code>true</code>.
     *
     *
     * @param  phpModule the PHP module to be extended; never <code>null</code>
     * @return the set of newly created files in the web module, can be empty but never <code>null</code>
     * @throws ExtendingException if extending fails
     * @see #isValid()
     */
    public abstract Set<FileObject> extend(PhpModule phpModule) throws ExtendingException;

    /**
     * Exception that is thrown if the {@link PhpModuleExtender#extend(PhpModule) extending operation} fails.
     */
    public static final class ExtendingException extends Exception {
        private static final long serialVersionUID = 160207942147917846L;

        /**
         * Constructs a new exception with the specified detail failure message.
         * @param failureMessage the detail failure message.
         */
        public ExtendingException(String failureMessage) {
            this(failureMessage, null);
        }

        /**
         * Constructs a new exception with the specified detail failure message and cause.
         * @param failureMessage the detail failure message.
         * @param cause the cause (which is saved for later retrieval by the
         * {@link #getCause()} method).  (A <tt>null</tt> value is permitted,
         * and indicates that the cause is nonexistent or unknown.)
         */
        public ExtendingException(String failureMessage, Throwable cause) {
            super(failureMessage, cause);
            Parameters.notEmpty("failureMessage", failureMessage);
        }

        /**
         * Get the localized message why the {@link PhpModuleExtender#extend(PhpModule) extending operation} failed.
         * @return the localized message why the {@link PhpModuleExtender#extend(PhpModule) extending operation} failed.
         */
        public String getFailureMessage() {
            return getMessage();
        }
    }
}
