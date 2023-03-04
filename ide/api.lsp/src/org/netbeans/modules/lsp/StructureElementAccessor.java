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
package org.netbeans.modules.lsp;

import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lsp.StructureElement;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 */
public abstract class StructureElementAccessor {
    
    private static volatile StructureElementAccessor DEFAULT;
    
    public static synchronized StructureElementAccessor getDefault () {
        StructureElementAccessor instance = DEFAULT;
        if (instance == null) {
            Class<?> c = StructureElement.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
                instance = DEFAULT;
                assert instance != null;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return instance;
    }
    
    public static void setDefault(@NonNull final StructureElementAccessor accessor) {
        Parameters.notNull("accessor", accessor);
        DEFAULT = accessor;
    }
    
    public abstract StructureElement createStructureElement(FileObject file, @NonNull String name, String detail, int selectionStartOffset, int selectionEndOffset, int expandedStartOffset, int expandedEndOffset, @NonNull StructureElement.Kind kind, Set<StructureElement.Tag> tags, List<StructureElement> children);
    
}
