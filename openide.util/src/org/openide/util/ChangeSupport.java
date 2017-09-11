/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple equivalent of {@link java.beans.PropertyChangeSupport} for
 * {@link ChangeListener}s. This class is not serializable.
 *
 * @since 7.8
 * @author Andrei Badea
 */
public final class ChangeSupport {

    private static final Logger LOG = Logger.getLogger(ChangeSupport.class.getName());

    // not private because used in unit tests
    final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private final Object source;

    /**
     * Creates a new <code>ChangeSupport</code>
     *
     * @param  source the instance to be given as the source for events.
     */
    public ChangeSupport(Object source) {
        this.source = source;
    }

    /**
     * Adds a <code>ChangeListener</code> to the listener list. The same
     * listener object may be added more than once, and will be called
     * as many times as it is added. If <code>listener</code> is null,
     * no exception is thrown and no action is taken.
     *
     * @param  listener the <code>ChangeListener</code> to be added.
     */
    public void addChangeListener(ChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINE) && listeners.contains(listener)) {
            LOG.log(Level.FINE, "diagnostics for #167491", new IllegalStateException("Added " + listener + " multiply"));
        }
        listeners.add(listener);
    }

    /**
     * Removes a <code>ChangeListener</code> from the listener list.
     * If <code>listener</code> was added more than once,
     * it will be notified one less time after being removed.
     * If <code>listener</code> is null, or was never added, no exception is
     * thrown and no action is taken.
     *
     * @param  listener the <code>ChangeListener</code> to be removed.
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    /**
     * Fires a change event to all registered listeners.
     */
    public void fireChange() {
        if (listeners.isEmpty()) {
            return;
        }
        fireChange(new ChangeEvent(source));
    }

    /**
     * Fires the specified <code>ChangeEvent</code> to all registered
     * listeners. If <code>event</code> is null, no exception is thrown
     * and no action is taken.
     *
     * @param  event the <code>ChangeEvent</code> to be fired.
     */
    private void fireChange(ChangeEvent event) {
        assert event != null;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(event);
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    /**
     * Checks if there are any listeners registered to this<code>ChangeSupport</code>.
     *
     * @return true if there are one or more listeners for the given property,
     *         false otherwise.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
