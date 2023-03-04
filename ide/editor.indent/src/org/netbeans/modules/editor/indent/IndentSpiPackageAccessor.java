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

package org.netbeans.modules.editor.indent;

import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.editor.indent.spi.Context;

/**
 * Accessor for the package-private functionality of bookmarks API.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class IndentSpiPackageAccessor {
    
    private static IndentSpiPackageAccessor INSTANCE;
    
    public static IndentSpiPackageAccessor get() {
        if (INSTANCE == null) {
            // Enforce the static initializer in Context class to be run
            try {
                Class.forName(Context.class.getName(), true, Context.class.getClassLoader());
            } catch (ClassNotFoundException e) { }
        }
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(IndentSpiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    public abstract Context createContext(TaskHandler.MimeItem mimeItem);
    
    public abstract Context.Region createContextRegion(MutablePositionRegion region);
    
    public abstract MutablePositionRegion positionRegion(Context.Region region);
    
}
