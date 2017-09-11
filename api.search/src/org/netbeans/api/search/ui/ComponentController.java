/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.search.ui;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.ChangeSupport;

/**
 * Base class for component controllers.
 *
 * @author jhavlin
 */
abstract class ComponentController<T extends JComponent> {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    protected T component;

    protected ComponentController(T component) {
        this.component = component;
    }

    public final T getComponent() {
        return component;
    }

    /**
     * Adds a
     * <code>ChangeListener</code> to the listener list. The same listener
     * object may be added more than once, and will be called as many times as
     * it is added. If
     * <code>listener</code> is null, no exception is thrown and no action is
     * taken.
     *
     * @param listener the
     * <code>ChangeListener</code> to be added.
     */
    public final void addChangeListener(@NonNull ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes a
     * <code>ChangeListener</code> from the listener list. If
     * <code>listener</code> was added more than once, it will be notified one
     * less time after being removed. If
     * <code>listener</code> is null, or was never added, no exception is thrown
     * and no action is taken.
     *
     * @param listener the
     * <code>ChangeListener</code> to be removed.
     */
    public final void removeChangeListener(@NonNull ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Fires a change event to all registered listeners.
     */
    protected final void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * Checks if there are any listeners registered to this
     * <code>ChangeSupport</code>.
     *
     * @return true if there are one or more listeners for the given property,
     * false otherwise.
     */
    public final boolean hasListeners() {
        return changeSupport.hasListeners();
    }
}
