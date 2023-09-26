/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.model.spi;

import java.util.List;
import org.netbeans.modules.javascript2.model.FunctionArgumentAccessor;
import org.netbeans.modules.javascript2.model.api.JsObject;

/**
 *
 * @author Petr Hejl
 */
public final class FunctionArgument {

    static {
        FunctionArgumentAccessor.setDefault(new FunctionArgumentAccessor() {

            @Override
            public FunctionArgument createForAnonymousObject(int order, int offset, JsObject value) {
                return new FunctionArgument(Kind.ANONYMOUS_OBJECT, order, offset, value);
            }

            @Override
            public FunctionArgument createForArray(int order, int offset, JsObject value) {
                return new FunctionArgument(Kind.ARRAY, order, offset, value);
            }

            @Override
            public FunctionArgument createForString(int order, int offset, String value) {
                return new FunctionArgument(Kind.STRING, order, offset, value);
            }

            @Override
            public FunctionArgument createForReference(int order, int offset, List<String> value) {
                return new FunctionArgument(Kind.REFERENCE, order, offset, value);
            }

            @Override
            public FunctionArgument createForUnknown(int order) {
                return new FunctionArgument(Kind.UNKNOWN, order, -1, null);
            }
        });
    }
    private final Kind kind;

    private final int order;

    private final int offset;

    private final Object value;

    private FunctionArgument(Kind kind, int order, int offset, Object value) {
        this.kind = kind;
        this.order = order;
        this.offset = offset;
        this.value = value;
    }

    public Kind getKind() {
        return this.kind;
    }

    public int getOrder() {
        return this.order;
    }

    public int getOffset() {
        return this.offset;
    }

    public Object getValue() {
        return this.value;
    }

    public static enum Kind {
        STRING,
        REFERENCE,
        ANONYMOUS_OBJECT,
        ARRAY,
        UNKNOWN
    };
}
