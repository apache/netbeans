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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author ads
 */
@ActionsProvider.Registration(actions = {"toggleBreakpoint"}, activateForMIMETypes = {Utils.MIME_TYPE})
public class BreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {

    public BreakpointActionProvider() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
        EditorContextDispatcher.getDefault().addPropertyChangeListener(Utils.MIME_TYPE, WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
    }

    @Override
    public void doAction(Object action) {
        if (SwingUtilities.isEventDispatchThread()) {
            addBreakpoints();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addBreakpoints();
                }
            });
        }
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    private void addBreakpoints() {
        Line line = Utils.getCurrentLine();
        if (line == null) {
            return;
        }
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        boolean add = true;
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpoint instanceof LineBreakpoint && ((LineBreakpoint) breakpoint).getLine().equals(line)) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(breakpoint);
                add = false;
                break;
            }
        }
        LineBreakpoint lineBreakpoint = new LineBreakpoint(line);
        lineBreakpoint.refreshValidity();
        if (add) {
            DebuggerManager.getDebuggerManager().addBreakpoint(lineBreakpoint);
        }
    }

    @Override
    public boolean isEnabled(Object action) {
        boolean enabled = super.isEnabled(action);
        if (ActionsManager.ACTION_TOGGLE_BREAKPOINT.equals(action)) {
            if (enabled) {
                // Check if the current line is also PHP:
                Line line = Utils.getCurrentLine();
                return line != null && Utils.isInPhpScript(line);
            }
        }
        return enabled;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // We need to push the state there :-(( instead of wait for someone to be interested in...
        boolean enabled = Utils.getCurrentLine() != null;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }

}
