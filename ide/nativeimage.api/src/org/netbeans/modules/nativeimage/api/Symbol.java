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
package org.netbeans.modules.nativeimage.api;

/**
 * A symbol in the debuggee program.
 *
 * @since 0.2
 */
public final class Symbol {

    private final String name;
    private final String type;
    private final String description;

    private Symbol(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    /**
     * Get the symbol name. Never <code>null<code>.
     *
     * @since 0.2
     */
    public String getName() {
        return name;
    }

    /**
     * Get the symbol type.
     *
     * @since 0.2
     */
    public String getType() {
        return type;
    }

    /**
     * Get the symbol description.
     *
     * @since 0.2
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates a builder to build a new {@link Symbol}.
     *
     * @since 0.2
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Symbol{" + "name=" + name + ", type=" + type + ", description=" + description + '}';
    }

    /**
     * Symbol's builder.
     *
     * @since 0.2
     */
    public static final class Builder {

        private String name;
        private String type;
        private String description;

        Builder() {}

        /**
         * Set the symbol name. The name must be defined.
         *
         * @since 0.2
         */
        public void name(String name) {
            this.name = name;
        }

        /**
         * Set the symbol type.
         *
         * @since 0.2
         */
        public void type(String type) {
            this.type = type;
        }

        /**
         * Set the symbol description.
         *
         * @since 0.2
         */
        public void description(String description) {
            this.description = description;
        }

        /**
         * Build the {@link Symbol} object.
         *
         * @since 0.2
         */
        public Symbol build() {
            if (name == null) {
                throw new IllegalArgumentException("Name must be defined.");
            }
            return new Symbol(name, type, description);
        }
    }
}
