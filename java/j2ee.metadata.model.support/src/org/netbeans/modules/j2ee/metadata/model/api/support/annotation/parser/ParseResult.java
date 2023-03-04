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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser;

import java.util.Map;
import org.openide.util.Parameters;

/**
 * Encapsulates a parsed annotation.
 *
 * @see AnnotationParser
 *
 * @author Andrei Badea
 */
public final class ParseResult {

    private final Map<String, Object> resultMap;

    ParseResult(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

    /**
     * Returns the value for the <code>name</code> key if that
     * value is of the type specified by <code>asType</code>.
     */
    public <T> T get(String name, Class<T> asType) {
        Parameters.notNull("name", name); //NOI18N
        Parameters.notNull("asType", asType); //NOI18N
        Object value = resultMap.get(name);
        if (asType.isInstance(value)) {
            @SuppressWarnings("unchecked") //NOI18N
            T typedValue = (T)value;
            return typedValue;
        } else {
            if (value != null) {
                throw new IllegalStateException("Incorrect class token specified for the " + name + " element: was " + asType + ", should have been " + value.getClass()); // NOI18N
            }
        }
        return null;
    }
}
