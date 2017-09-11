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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProvider.Registration;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Honza
 */
@Registration(actions={"toggleBreakpoint"}, activateForMIMETypes={"text/x-ant+xml"})
public class AntBreakpointActionProvider extends ActionsProviderSupport
                                         implements PropertyChangeListener {
    
    private static final String ANT_MIME_TYPE = "text/x-ant+xml"; // NOI18N
    
    private static final Set actions = Collections.singleton (
        ActionsManager.ACTION_TOGGLE_BREAKPOINT
    );
    
    EditorContextDispatcher context = EditorContextDispatcher.getDefault();
    
    public AntBreakpointActionProvider () {
        context.addPropertyChangeListener(ANT_MIME_TYPE,
                WeakListeners.propertyChange(this, context));
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
    }
    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    @Override
    public void doAction (Object action) {
        Line line = getCurrentLine ();
        if (line == null) return ;
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        int i, k = breakpoints.length;
        for (i = 0; i < k; i++)
            if ( breakpoints [i] instanceof AntBreakpoint &&
                 ((AntBreakpoint) breakpoints [i]).getLine ().equals (line)
            ) {
                DebuggerManager.getDebuggerManager ().removeBreakpoint
                    (breakpoints [i]);
                break;
            }
        if (i == k)
            DebuggerManager.getDebuggerManager ().addBreakpoint (
                new AntBreakpoint (line)
            );
        //S ystem.out.println("toggle");
    }
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    @Override
    public Set getActions () {
        return actions;
    }
    
    
    private static Line getCurrentLine () {
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        //System.out.println("n = "+n+", FO = "+fo+" => is ANT = "+isAntFile(fo));
        if (!isAntFile(fo)) {
            return null;
        }
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }
    
    
    private static boolean isAntFile(FileObject fo) {
        if (fo == null) {
            return false;
        } else {
            return fo.getMIMEType().equals("text/x-ant+xml");
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // We need to push the state there :-(( instead of wait for someone to be interested in...
        boolean enabled = getCurrentLine() != null;
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }
    
}
