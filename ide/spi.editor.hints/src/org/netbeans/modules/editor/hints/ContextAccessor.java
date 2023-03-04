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
package org.netbeans.modules.editor.hints;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.spi.editor.hints.Context;

/**
 * Accessor for {@link Context}'s package private constructor
 * @author Max Sauer
 */
public abstract class ContextAccessor {

    public static ContextAccessor DEFAULT;

    public static ContextAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }

        Class c = Context.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }

        assert DEFAULT != null;
        return DEFAULT;
    }

    public abstract Context newContext(int position, AtomicBoolean cancel);
}
