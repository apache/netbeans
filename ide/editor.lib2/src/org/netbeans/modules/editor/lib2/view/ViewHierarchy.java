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

import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;

/**
 * View hierarchy associated with a particular text component (for its whole lifetime).
 * <br>
 * View hierarchy needs to be locked before doing most operations - see {@link #lock() }.
 * <br>
 * If editor view hierarchy is currently not installed into particular text component
 * (text component's root view is not an instance of DocumentView)
 * the methods (in LockedViewHierarchy) return default values as described in their documentation.
 * 
 * @author Miloslav Metelka
 */
public final class ViewHierarchy {
    
    static {
        ViewApiPackageAccessor.register(new PackageAccessor());
    }
    
    /**
     * Get view hierarchy for an existing text component.
     *
     * @param component non-null text component.
     * @return non-null view hierarchy instance. The method will always return the same result
     *  for the given text component.
     */
    public static @NonNull ViewHierarchy get(@NonNull JTextComponent component) {
        return ViewHierarchyImpl.get(component).viewHierarchy();
    }
    
    private final ViewHierarchyImpl impl;
    
    ViewHierarchy(ViewHierarchyImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Get text component that this view hierarchy is associated with.
     * <br>
     * 
     * @return non-null text component.
     */
    public @NonNull JTextComponent getTextComponent() {
        return impl.textComponent();
    }
    
    /**
     * Lock view hierarchy in order to perform operations described in {@link LockedViewHierarchy }.
     * <br>
     * Underlying document of the view hierarchy's text component must be read-locked
     * to guarantee stability of offsets passed to methods of LockedViewHierarchy.
     * <br>
     * Code example:<code>
     * // Possible textComponent.getDocument() read-locking
     * LockedViewHierarchy lvh = ViewHierarchy.get(textComponent).lock();
     * try {
     *     ...
     * } finally {
     *     lvh.unlock();
     * }
     * </code>
     *
     * @return locked view hierarchy.
     * @throws Exception 
     */
    public LockedViewHierarchy lock() {
        return impl.lock();
    }
    
    /**
     * Add listener for view hierarchy changes.
     * <br>
     * Listener will be notified on a locked view hierarchy.
     *
     * @param l non-null listener.
     */
    public void addViewHierarchyListener(@NonNull ViewHierarchyListener l) {
        impl.addViewHierarchyListener(l);
    }

    /**
     * Remove listener for view hierarchy changes.
     *
     * @param l non-null listener.
     */
    public void removeViewHierarchyListener(@NonNull ViewHierarchyListener l) {
        impl.removeViewHierarchyListener(l);
    }

    @Override
    public String toString() {
        return impl.toString();
    }

    private static final class PackageAccessor extends ViewApiPackageAccessor {

        @Override
        public ViewHierarchy createViewHierarchy(ViewHierarchyImpl impl) {
            return new ViewHierarchy(impl);
        }

        @Override
        public LockedViewHierarchy createLockedViewHierarchy(ViewHierarchyImpl impl) {
            return new LockedViewHierarchy(impl);
        }
        
        @Override
        public ViewHierarchyEvent createEvent(ViewHierarchy viewHierarchy, ViewHierarchyChange change) {
            return new ViewHierarchyEvent(viewHierarchy, change);
        }

    }
}
