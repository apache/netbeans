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

package org.netbeans.modules.editor.lib2;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.StickyWindowSupport;

/**
 * Accessor for the package-private functionality of editor API.
 *
 * @author Miloslav Metelka
 */

public abstract class EditorApiPackageAccessor {
    
    private static EditorApiPackageAccessor INSTANCE;
    
    public static EditorApiPackageAccessor get() {
        if (INSTANCE == null) {
            // Force instance registration
            try {
                Class.forName(EditorRegistry.class.getName(), true, EditorRegistry.class.getClassLoader());
            } catch (ClassNotFoundException e) {
            }
        }
        return INSTANCE;
    }

    public static void register(EditorApiPackageAccessor accessor) {
        INSTANCE = accessor;
    }
    
    /** Register text component to registry. */
    public abstract void register(JTextComponent c);

    /**Forcibly release from the registry - useful for tests.*/
    public abstract void forceRelease(JTextComponent c);
    
    public abstract void setIgnoredAncestorClass(Class ignoredAncestorClass);
    
    public abstract void notifyClose(JComponent c);
    
    public abstract StickyWindowSupport createStickyWindowSupport(JTextComponent jtc);
}
