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
