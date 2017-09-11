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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeEvent;

/**
 * Empty implementation of
 * {@link DebuggerManagerListener}.
 *
 * @author   Jan Jancura
 */
public class DebuggerManagerAdapter implements LazyDebuggerManagerListener {

    /**
     * Called when set of breakpoints is initialized.
     *
     * @return initial set of breakpoints
     */
    public Breakpoint[] initBreakpoints () {
        return new Breakpoint [0];
    }

    /**
     * Called when some breakpoint is added.
     *
     * @param breakpoint a new breakpoint
     */
    public void breakpointAdded (Breakpoint breakpoint) {
    }

    /**
     * Called when some breakpoint is removed.
     *
     * @param breakpoint removed breakpoint
     */
    public void breakpointRemoved (Breakpoint breakpoint) {
    }

    /**
     * Called when set of watches is initialized.
     *
     * @return initial set of watches
     */
    public void initWatches () {
    }

    /**
     * Called when some watch is added.
     *
     * @param watch a new watch
     */
    public void watchAdded (Watch watch) {
    }

    /**
     * Called when some watch is removed.
     *
     * @param watch removed watch
     */
    public void watchRemoved (Watch watch) {
    }

    /**
     * Called when some session is added.
     *
     * @param session a new session
     */
    public void sessionAdded (Session session) {
    }

    /**
     * Called when some session is removed.
     *
     * @param session removed session
     */
    public void sessionRemoved (Session session) {
    }

    /**
     * Called when some engine is added.
     *
     * @param engine a new engine
     */
    public void engineAdded (DebuggerEngine engine) {
    }

    /**
     * Called when some engine is removed.
     *
     * @param engine removed engine
     */
    public void engineRemoved (DebuggerEngine engine) {
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
    }
    
    public String[] getProperties () {
        return new String [0];
    }
}
