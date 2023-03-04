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
package org.netbeans.swing.tabcontrol.customtabs;

import org.netbeans.swing.tabcontrol.TabbedContainer;

/**
 * Lists all possible types of tabbed container.
 *
 * @see TabbedContainer
 *
 * @since 1.33
 * @author S. Aubrecht
 */
public enum TabbedType {
    /**
     * Tabbed container showing non-document windows.
     */
    VIEW {
        @Override
        public int toInt() {
            return TabbedContainer.TYPE_VIEW;
        }
    },
    /**
     * Tabbed container showing document windows.
     */
    EDITOR {
        @Override
        public int toInt() {
            return TabbedContainer.TYPE_EDITOR;
        }
    },
    /**
     * Tabbed container showing minimized windows.
     */
    SLIDING {
        @Override
        public int toInt() {
            return TabbedContainer.TYPE_SLIDING;
        }
    },
    /**
     * Tabbed container which uses toolbar-like component to switch active window.
     */
    TOOLBAR {
        @Override
        public int toInt() {
            return TabbedContainer.TYPE_TOOLBAR;
        }
    };

    public abstract int toInt();
}
