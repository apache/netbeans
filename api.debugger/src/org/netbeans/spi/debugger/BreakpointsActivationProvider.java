/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.spi.debugger;

import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.ActiveBreakpoints;

/**
 * Provider of debugger engine-related breakpoints
 * activation/deactivation, which is independent on the enabled/disabled state.
 * Register an implementation of this class for an appropriate debugger engine
 * via {@link DebuggerServiceRegistration} annotation.
 * 
 * @author Martin Entlicher
 * @since 1.51
 */
public interface BreakpointsActivationProvider {
    
    /**
     * Test if the engine's breakpoints are currently active.
     * @return <code>true</code> when breakpoints are active,
     *         <code>false</code> otherwise.
     */
    boolean areBreakpointsActive();
    
    /**
     * Activate or deactivate breakpoints handled by this debugger engine.
     * The breakpoints activation/deactivation is independent on breakpoints enabled/disabled state.
     * 
     * @param active <code>true</code> to activate breakpoints,
     *               <code>false</code> to deactivate them.
     */
    void setBreakpointsActive(boolean active);
    
    /**
     * Add a property change listener to be notified about
     * {@link ActiveBreakpoints#PROP_BREAKPOINTS_ACTIVE}
     * @param l a property change listener
     */
    void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Remove a property change listener.
     * @param l  a property change listener
     */
    void removePropertyChangeListener(PropertyChangeListener l);
}
