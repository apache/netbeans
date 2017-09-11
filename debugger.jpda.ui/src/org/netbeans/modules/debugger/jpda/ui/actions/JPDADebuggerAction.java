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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.util.RequestProcessor;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerAction extends ActionsProviderSupport implements
PropertyChangeListener {

    private JPDADebugger debugger;

    JPDADebuggerAction (JPDADebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        try {
            checkEnabled (debugger.getState ());
        } catch (com.sun.jdi.VMDisconnectedException e) {
            // Causes kill action when something is being evaluated
        }
    }
    
    protected abstract void checkEnabled (int debuggerState);

    private boolean canApplyLazyEnabled = false;

    /**
     * Call this from {@link #checkEnabled(int)} when the code needs to be run outside of AWT.
     * Override {@link #checkEnabledLazyImpl(int)} method, which will be called
     * in the provided RequestProcessor. The returned enabled status is set through
     * {@link #setEnabledSingleAction(boolean)}.
     * <p>
     * Do not call {@link #setEnabled(java.lang.Object, boolean)} method! When
     * you also need to set the status directly, use {@link #setEnabledSingleAction(boolean)},
     * which correctly cooperates with the lazy code.
     * 
     * @param debuggerState
     * @param rp
     */
    protected final void checkEnabledLazySingleAction(final int debuggerState, RequestProcessor rp) {
        canApplyLazyEnabled = true;
        rp.post(new Runnable() {
            @Override
            public void run() {
                final boolean enabled = checkEnabledLazyImpl(debuggerState);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (canApplyLazyEnabled) {
                            setEnabledSingleAction(enabled);
                            canApplyLazyEnabled = true; // In case there were several lazy invocations
                        }
                    }
                });
            }
        });
    }

    /** Do not call setEnabled(), return the enabled state instead. */
    protected boolean checkEnabledLazyImpl (int debuggerState) {
        return false;
    }

    protected final void setEnabledSingleAction(boolean enabled) {
        canApplyLazyEnabled = false;
        setEnabled(getActions().iterator().next(), enabled);
    }

    JPDADebugger getDebuggerImpl () {
        return debugger;
    }
}
