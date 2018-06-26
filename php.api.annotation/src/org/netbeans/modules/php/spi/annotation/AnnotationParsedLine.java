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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.util.Parameters;

/**
 * Encapsulates parsed annotation line.
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface AnnotationParsedLine {

    /**
     * Returns a name of an annotation without the "at" sign.
     *
     * @return name
     */
    public String getName();

    /**
     * Returns a description of the parsed annotation.
     *
     * @return description
     */
    public String getDescription();

    /**
     * Returns an offset-ranges and their types.
     *
     * @return offset range of a parsed type and its textual representation
     */
    public Map<OffsetRange, String> getTypes();

    /**
     * Returns {@code true} if parsed line starts with an annotation.
     * <p>
     * This line must return {@code true}:
     * </p>
     * <pre>
     *  * @return FooBar
     * </pre>
     * <p>
     * This line must return {@code false}:
     * </p>
     * <pre>
     *  * some text @myInlineAnnotation some another text
     * </pre>
     *
     * @return {@code true} if this line starts with the annotations, {@code false} otherwise
     */
    public boolean startsWithAnnotation();

    /**
     * Dummy implementation of {@link AnnotationParsedLine}.
     */
    static final class ParsedLine implements AnnotationParsedLine {
        private final String name;
        private final String description;
        private final Map<OffsetRange, String> types;
        private final boolean startsWithAnnotation;

        /**
         * Creates new annotation parsed line.
         *
         * @param name name of the annotation; never {@code null}
         */
        public ParsedLine(@NonNull final String name) {
            this(name, null, null, false);
        }

        /**
         * Creates new annotation parsed line.
         *
         * @param name name of the annotation; never {@code null}
         * @param types types of the annotation; can be {@code null}
         */
        public ParsedLine(@NonNull final String name, @NullAllowed final Map<OffsetRange, String> types) {
            this(name, types, null, false);
        }

        /**
         * Creates new annotation parsed line.
         *
         * @param name name of the annotation; never {@code null}
         * @param description description of the annotation; can be {@code null}
         */
        public ParsedLine(@NonNull final String name, @NullAllowed final String description) {
            this(name, null, description, false);
        }

        /**
         * Creates new annotation parsed line.
         *
         * @param name name of the annotation; never {@code null}
         * @param types types of the annotation; can be {@code null}
         * @param description description of the annotation; can be {@code null}
         */
        public ParsedLine(@NonNull final String name, @NullAllowed final Map<OffsetRange, String> types, @NullAllowed final String description) {
            this(name, types, description, false);
        }

        /**
         * Creates new annotation parsed line.
         *
         * @param name name of the annotation; never {@code null}
         * @param types types of the annotation; can be {@code null}
         * @param description description of the annotation; can be {@code null}
         * @param startsWithAnnotation {@code true} if this line starts with the annotations, {@code false} otherwise
         */
        public ParsedLine(@NonNull final String name, @NullAllowed final Map<OffsetRange, String> types, @NullAllowed final String description, final boolean startsWithAnnotation) {
            Parameters.notNull("name", name); //NOI18N
            this.name = name;
            this.types = types;
            this.description = description;
            this.startsWithAnnotation = startsWithAnnotation;
        }

        /**
         * Returns a name of an annotation without the "at" sign.
         *
         * @return name; never {@code null}
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Returns an offset-ranges and their types.
         *
         * @return offset range of a parsed type and its textual representation; never {@code null}
         */
        @Override
        public Map<OffsetRange, String> getTypes() {
            Map<OffsetRange, String> result = Collections.emptyMap();
            if (types != null) {
                result = new HashMap<OffsetRange, String>(types);
            }
            return result;
        }

        /**
         * Returns a description of the parsed annotation.
         *
         * @return description; never {@code null}
         */
        @Override
        public String getDescription() {
            String result = "";
            if (description != null) {
                result = description;
            }
            return result;
        }

        /**
         * Returns {@code true} if parsed line starts with an annotation.
         * <p>
         * This line must return {@code true}:
         * </p>
         * <pre>
         *  * @return FooBar
         * </pre>
         * <p>
         * This line must return {@code false}:
         * </p>
         * <pre>
         *  * some text @myInlineAnnotation some another text
         * </pre>
         *
         * @return {@code true} if this line starts with the annotations, {@code false} otherwise
         */
        @Override
        public boolean startsWithAnnotation() {
            return startsWithAnnotation;
        }

        @Override
        public boolean equals(@NullAllowed final Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParsedLine other = (ParsedLine) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
                return false;
            }
            if (this.types != other.types && (this.types == null || !this.types.equals(other.types))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 59 * hash + (this.description != null ? this.description.hashCode() : 0);
            hash = 59 * hash + (this.types != null ? this.types.hashCode() : 0);
            return hash;
        }

    }

}
