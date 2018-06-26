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
package org.netbeans.modules.php.api.validation;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 * Validation result. This class can used by miscellaneous validators
 * for collecting errors and warnings.
 * <p>
 * This class is not thread safe.
 * @since 2.9
 */
public final class ValidationResult {

    private final List<Message> errors = new ArrayList<>();
    private final List<Message> warnings = new ArrayList<>();


    /**
     * Create new validation result.
     */
    public ValidationResult() {
    }

    /**
     * Copy constructor.
     */
    public ValidationResult(ValidationResult anotherResult) {
        merge(anotherResult);
    }

    /**
     * Check whether there are no errors and no warnings present.
     * @return {@code true} if the validation result contains no errors and no warnings
     * @see #hasErrors()
     * @see #hasWarnings()
     * @since 2.16
     */
    public boolean isFaultless() {
        return !hasErrors()
                && !hasWarnings();
    }

    /**
     * Check whether there are some errors present.
     * @return {@code true} if the validation result contains any error
     * @see #isFaultless()
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Get errors.
     * @return list of errors, can be empty but never {@code null}
     */
    public List<Message> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Check whether there are some warnings present.
     * @return {@code true} if the validation result contains any warning
     * @see #isFaultless()
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Get warnings.
     * @return list of warnings, can be empty but never {@code null}
     */
    public List<Message> getWarnings() {
        return new ArrayList<>(warnings);

    }

    /**
     * Add error.
     * @param error error to be added
     */
    public void addError(Message error) {
        Parameters.notNull("error", error); // NOI18N
        errors.add(error);
    }

    /**
     * Add warning.
     * @param warning  warning to be added
     */
    public void addWarning(Message warning) {
        Parameters.notNull("warning", warning); // NOI18N
        warnings.add(warning);
    }

    /**
     * Merge with some other validation result.
     * <p>
     * This method simply merges all errros and warnings from {@code otherResult} to this one.
     * It can be useful if one validator uses another one for validation.
     * @param otherResult validation result to be merged to this one
     */
    public void merge(ValidationResult otherResult) {
        Parameters.notNull("otherResult", otherResult); // NOI18N
        errors.addAll(otherResult.errors);
        warnings.addAll(otherResult.warnings);
    }

    //~ Inner classes

    /**
     * Validation message.
     */
    public static final class Message {

        private final Object source;
        private final String message;
        private final String type;


        /**
         * Create new validation message.
         * @param source source of the message, e.g. "siteRootFolder"
         * @param message message itself, e.g. "Invalid directory specified."
         */
        public Message(Object source, String message) {
            this(source, message, null);
        }

        /**
         * Create new validation message.
         * @param source source of the message, e.g. "siteRootFolder"
         * @param message message itself, e.g. "Invalid directory specified."
         * @param type message type, can be {@code null}
         * @since 2.26
         */
        public Message(Object source, String message, @NullAllowed String type) {
            Parameters.notNull("source", source); // NOI18N
            Parameters.notNull("message", message); // NOI18N
            this.source = source;
            this.message = message;
            this.type = type;
        }

        /**
         * Get source of the message, e.g. "siteRootFolder".
         * @return source of the message, e.g. "siteRootFolder"
         */
        public Object getSource() {
            return source;
        }

        /**
         * Get message itself, e.g. "Invalid directory specified."
         * @return message itself, e.g. "Invalid directory specified.".
         */
        public String getMessage() {
            return message;
        }

        /**
         * Get message type, can be {@code null}.
         * @return message type, can be {@code null}
         * @since 2.26
         */
        @CheckForNull
        public String getType() {
            return type;
        }

        /**
         * {@code True} if this message has the given type, {@code false} otherwise.
         * @param type type to be checked
         * @return {@code true} if this message has the given type, {@code false} otherwise.
         * @since 2.26
         */
        public boolean isType(@NonNull String type) {
            Parameters.notNull("type", type); // NOI18N
            return type.equals(this.type);
        }

        @Override
        public String toString() {
            return "Message{" + "source=" + source + ", message=" + message + ", type=" + type + '}'; // NOI18N
        }

    }

}
