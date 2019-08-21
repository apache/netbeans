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

package org.netbeans.modules.editor.lib;

import org.netbeans.editor.EditorUI;
import org.netbeans.api.editor.StickyWindowSupport;
import org.netbeans.editor.ext.ToolTipSupport;


/**
 * Accessor for the package-private functionality in org.netbeans.editor.ext package.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class EditorExtPackageAccessor {

    private static EditorExtPackageAccessor ACCESSOR = null;

    public static synchronized void register(EditorExtPackageAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }

    public static synchronized EditorExtPackageAccessor get() {
        // Trying to wake up ToolTipSupport ...
        try {
            Class<?> clazz = Class.forName(ToolTipSupport.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }

        assert ACCESSOR != null : "There is no package accessor available!"; //NOI18N
        return ACCESSOR;
    }

    protected EditorExtPackageAccessor() {
    }

    public abstract ToolTipSupport createToolTipSupport(EditorUI eui);
}
