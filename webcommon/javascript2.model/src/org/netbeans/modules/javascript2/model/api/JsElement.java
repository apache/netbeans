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
package org.netbeans.modules.javascript2.model.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Petr Pisl
 */
public interface JsElement extends ElementHandle {

    public enum Kind {

        FUNCTION(1),
        METHOD(2),
        CONSTRUCTOR(3),
        OBJECT(4),
        PROPERTY(5),
        VARIABLE(6),
        FIELD(7),
        FILE(8),
        PARAMETER(9),
        ANONYMOUS_OBJECT(10),
        PROPERTY_GETTER(11),
        PROPERTY_SETTER(12),
        OBJECT_LITERAL(13),
        CATCH_BLOCK(14),
        WITH_OBJECT(15),
        CALLBACK(16),
        CLASS(17),
        GENERATOR(18),
        CONSTANT(19),
        BLOCK(20);

        private final int id;
        private static final Map<Integer, Kind> LOOKUP = new HashMap<>();

        static {
            for (Kind kind : EnumSet.allOf(Kind.class)) {
                LOOKUP.put(kind.getId(), kind);
            }
        }

        private Kind(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static  Kind fromId(int id) {
            return LOOKUP.get(id);
        }

        public boolean isFunction() {
            return this == FUNCTION || this == METHOD || this == CONSTRUCTOR
                    || this == PROPERTY_GETTER || this == PROPERTY_SETTER
                    || this == CALLBACK || this == GENERATOR;
        }

        public boolean isPropertyGetterSetter() {
            return this == PROPERTY_GETTER || this == PROPERTY_SETTER;
        }

    }

    int getOffset();

    OffsetRange getOffsetRange();

    Kind getJSKind();

    boolean isDeclared();

    boolean isPlatform();

    String getSourceLabel();
}
