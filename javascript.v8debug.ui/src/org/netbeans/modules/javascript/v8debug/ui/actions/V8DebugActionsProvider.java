/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import static org.netbeans.api.debugger.ActionsManager.*;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerSessionProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 *
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path=V8DebuggerSessionProvider.SESSION_NAME)
public class V8DebugActionsProvider extends ActionsProviderSupport implements V8Debugger.Listener {
    
    private static final Logger LOG = Logger.getLogger(V8DebugActionsProvider.class.getName());

    private static final Set<Object> ACTIONS =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(new Object[] {
                        ACTION_RUN_TO_CURSOR,
                        ACTION_EVALUATE,
                    })));
    
    private final V8Debugger dbg;
    private final PropertyChangeListener jsFileContextListener = new JSFileContextListener();
    
    public V8DebugActionsProvider(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, V8Debugger.class);
        dbg.addListener(this);
        notifySuspended(false);
    }

    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        if (action == ACTION_EVALUATE) {
            CodeEvaluator.getDefault().open();
            actionPerformedNotifier.run();
        } else {
            super.postAction(action, actionPerformedNotifier);
        }
    }
    
    @Override
    public void doAction(Object action) {
        LOG.fine("doAction("+action+")");
        if (action == ACTION_RUN_TO_CURSOR) {
            Line currentLine = JSUtils.getCurrentLine();
            if (currentLine != null) {
                FileObject fo = currentLine.getLookup().lookup(FileObject.class);
                long line = (long) currentLine.getLineNumber();
                dbg.runTo(fo, line);
            }
        }
    }

    @Override
    public Set getActions() {
        return ACTIONS;
    }

    @Override
    public void notifySuspended(boolean suspended) {
        setEnabled(ACTION_EVALUATE, suspended);
        if (suspended) {
            EditorContextDispatcher.getDefault().addPropertyChangeListener(JSUtils.JS_MIME_TYPE, jsFileContextListener);
            setEnabled(ACTION_RUN_TO_CURSOR, JSUtils.getCurrentLine() != null);
        } else {
            EditorContextDispatcher.getDefault().removePropertyChangeListener(jsFileContextListener);
            setEnabled(ACTION_RUN_TO_CURSOR, false);
        }
    }

    @Override
    public void notifyCurrentFrame(CallFrame cf) {
    }
    
    @Override
    public void notifyFinished() {
        EditorContextDispatcher.getDefault().removePropertyChangeListener(jsFileContextListener);
    }
    
    private class JSFileContextListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setEnabled(ACTION_RUN_TO_CURSOR, JSUtils.getCurrentLine() != null);
        }
        
    }
    
}
