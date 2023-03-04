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
package org.netbeans.modules.javascript2.model;

import java.util.EnumSet;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;

/**
 *
 * @author Petr Pisl
 */
public class AnonymousObject extends JsObjectImpl {

    public AnonymousObject(JsObject parent, String name, OffsetRange offsetRange, String mimeType, String sourceLabel) {
        super(parent, name, true, offsetRange, EnumSet.of(Modifier.PRIVATE), mimeType, sourceLabel);
    }

    @Override
    public Kind getJSKind() {
        return JsElement.Kind.ANONYMOUS_OBJECT;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public int getOffset() {
        return getOffsetRange().getStart();
    }

    @Override
    public boolean hasExactName() {
        return false;
    }

    public static class AnonymousArray extends JsArrayImpl {

        public AnonymousArray(JsObject parent, String name, OffsetRange offsetRange, String mimeType, String sourceLabel) {
            super(parent, name, true, offsetRange, EnumSet.of(Modifier.PRIVATE), mimeType, sourceLabel);
        }

        @Override
        public Kind getJSKind() {
            return JsElement.Kind.ANONYMOUS_OBJECT;
        }

        @Override
        public boolean isAnonymous() {
            return true;
        }

        @Override
        public int getOffset() {
            return getOffsetRange().getStart();
        }

        @Override
        public boolean hasExactName() {
            return false;
        }
    }
}
