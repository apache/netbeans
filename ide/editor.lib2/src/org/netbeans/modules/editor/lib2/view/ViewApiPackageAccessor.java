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

package org.netbeans.modules.editor.lib2.view;

import org.openide.util.Exceptions;

/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.view.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class ViewApiPackageAccessor {
    
    private static ViewApiPackageAccessor INSTANCE;
    
    public static ViewApiPackageAccessor get() {
        if (INSTANCE == null) {
            // Cause api accessor impl to get initialized
            try {
                Class.forName(ViewHierarchy.class.getName(), true, ViewApiPackageAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return INSTANCE;
    }

    public static void register(ViewApiPackageAccessor accessor) {
        INSTANCE = accessor;
    }
    
    public abstract ViewHierarchy createViewHierarchy(ViewHierarchyImpl impl);
    
    public abstract LockedViewHierarchy createLockedViewHierarchy(ViewHierarchyImpl impl);    

    public abstract ViewHierarchyEvent createEvent(ViewHierarchy viewHierarchy, ViewHierarchyChange change);

}
