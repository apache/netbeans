/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
