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
package org.netbeans.modules.php.dbgp.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.actions.AbstractActionProvider;
import org.netbeans.modules.php.dbgp.annotations.CurrentLineAnnotation;
import org.netbeans.modules.php.dbgp.annotations.DebuggerAnnotation;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.breakpoints.BreakpointModel;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.text.Line;


/**
 * @author ads
 *
 */
public abstract class AbstractIDEBridge {

    public static final String LOCALS_VIEW_NAME     = "LocalsView";     // NOI18N

    public static final String CALLSTACK_VIEW_NAME  = "CallStackView";  // NOI18N

    public static final String WATCHES_VIEW_NAME    = "WatchesView";    // NOI18N

    public static final String BREAKPOINTS_VIEW_NAME
                                                    = "BreakpointsView";// NOI18N

    public static final String THREADS_VIEW_NAME    = "ThreadsView";    // NOI18N

    protected AbstractIDEBridge() {
        myAnnotations = new HashMap<>();
        isSuspended = new AtomicBoolean( false );
    }

    public void hideAnnotations(){
        Collection<List<DebuggerAnnotation>> annotations;
        synchronized ( myAnnotations ) {
            annotations = new ArrayList<>(
                    myAnnotations.values());
            myAnnotations.clear();
        }

        for( List<DebuggerAnnotation> list : annotations ){
            for (DebuggerAnnotation annotation : list ) {
                annotation.detach();
            }
        }
    }

    public void showCurrentDebuggerLine( final Line line ) {
        if ( line != null) {
            annotate( new CurrentLineAnnotation( line ) );

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
                }
            });
        }
    }

    public void annotate( DebuggerAnnotation annotation ) {
        String type = annotation.getAnnotationType();
        synchronized ( myAnnotations ) {
            List<DebuggerAnnotation> list = myAnnotations.get( type );
            if ( list == null ) {
                list = new LinkedList<>();
                myAnnotations.put( type , list );
            }
            list.add( annotation );
        }
    }

    public VariablesModel getVariablesModel() {
        DebuggerEngine engine = getEngine();
        if ( engine == null ) {
            return null;
        }
        return (VariablesModel)engine.lookupFirst(LOCALS_VIEW_NAME,
                TreeModel.class);
    }

    public CallStackModel getCallStackModel() {
        DebuggerEngine engine = getEngine();
        if ( engine == null ) {
            return null;
        }
        return (CallStackModel)engine.lookupFirst(CALLSTACK_VIEW_NAME,
                TreeModel.class);
    }

    public WatchesModel getWatchesModel() {
        DebuggerEngine engine = getEngine();
        if ( engine == null ) {
            return null;
        }
        return (WatchesModel)engine.lookupFirst(WATCHES_VIEW_NAME,
                TreeModel.class);
    }

    public BreakpointModel getBreakpointModel() {
        DebuggerManager debuggerManager = DebuggerManager.getDebuggerManager();
        Iterator it = debuggerManager != null ? debuggerManager.lookup(
                BREAKPOINTS_VIEW_NAME, NodeModel.class).iterator() : null;

        while(it != null && it.hasNext()) {
            NodeModel model = (NodeModel)it.next();
            if (model instanceof BreakpointModel) {
                return (BreakpointModel) model;
            }
        }

        return null;
    }

    public ThreadsModel getThreadsModel() {
        DebuggerEngine engine = getEngine();
        if ( engine == null ) {
            return null;
        }
        return (ThreadsModel)engine.lookupFirst(THREADS_VIEW_NAME,
                TreeModel.class);
    }

    public void setSuspended( boolean flag ) {
        isSuspended.set( flag );
        synchronized (SessionManager.getInstance()) {
            SessionId id = getDebugSession().getSessionId();
            if ( id == null ){
                return;
            }
            DebugSession current =
                SessionManager.getInstance().getSession(id);
            if ( current != null && !current.equals( getDebugSession() ) ){
                return;
            }
        }
        DebuggerEngine engine = getEngine();
        List list = engine != null ? engine.lookup( null , ActionsProvider.class ) : Collections.emptyList();
        for (Object object : list) {
            assert object instanceof AbstractActionProvider;
            AbstractActionProvider provider = (AbstractActionProvider) object;
            Set set = provider.getActions();
            for (Object obj: set) {
                if ( obj == ActionsManager.ACTION_CONTINUE ||
                        obj == ActionsManager.ACTION_STEP_INTO ||
                            obj ==  ActionsManager.ACTION_STEP_OVER ||
                                obj == ActionsManager.ACTION_STEP_OUT ||
                                    obj == ActionsManager.ACTION_RUN_TO_CURSOR)
                {
                    provider.setEnabled(flag);
                }
            }
        }
    }

    public boolean isSuspended(){
        return isSuspended.get();
    }

    protected abstract DebuggerEngine getEngine();

    protected abstract DebugSession getDebugSession();

    private Map<String, List<DebuggerAnnotation>> myAnnotations;

    private AtomicBoolean isSuspended;

}
