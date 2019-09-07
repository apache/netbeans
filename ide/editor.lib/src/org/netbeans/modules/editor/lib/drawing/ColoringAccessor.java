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

package org.netbeans.modules.editor.lib.drawing;

import org.netbeans.editor.Coloring;

/**
 *
 * @author vita
 */
public abstract class ColoringAccessor {

    private static ColoringAccessor ACCESSOR = null;

    public static synchronized void register(ColoringAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }

    public static synchronized ColoringAccessor get() {
        if (ACCESSOR == null) {
            // Trying to wake up EditorUI ...
            try {
                Class<?> clazz = Class.forName(Coloring.class.getName());
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }

        assert ACCESSOR != null : "There is no package accessor available!"; //NOI18N
        return ACCESSOR;
    }

    protected ColoringAccessor() {
    }

    public abstract void apply(Coloring c, DrawContext ctx);
}
