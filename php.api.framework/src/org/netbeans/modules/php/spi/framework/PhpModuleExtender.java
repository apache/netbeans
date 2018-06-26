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
