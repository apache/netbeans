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
package org.netbeans.modules.javascript2.debug.spi;

import java.util.Collection;
import org.netbeans.modules.parsing.api.Source;

/**
 * A debugger-specific query for JavaScript source elements.
 *
 * @author Martin
 */
public interface SourceElementsQuery {

    /**
     * Get a collection of variables visible at the given offset.
     * @param source The JavaScript source
     * @param offset The offset
     * @return Collection of variables visible at the given offset.
     */
    Collection<Var> getVarsAt(Source source, int offset);

    /**
     * Get an offset of declaration of an object (variable) present on the given offset.
     * @param source The JavaScript source
     * @param offset The offset
     * @return An offset of declaration of the object, or <code>-1</code> when not found.
     */
    int getObjectOffsetAt(Source source, int offset);

    /**
     * A representation of variable.
     */
    public static final class Var {

        private final String name;
        private final int offset;

        /**
         * Create a variable of the given name at the given offset.
         * @param name Variable name
         * @param offset Variable offset
         */
        public Var(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }

        /**
         * Get the variable name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the variable offset.
         *
         * @return the offset
         */
        public int getOffset() {
            return offset;
        }

    }
}
