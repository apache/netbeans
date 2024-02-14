/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.php.dbgp.models;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.dbgp.ConversionUtils;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.packets.StatusCommand;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author ads
 */
public class ThreadsModel extends ViewModelSupport
        implements TreeModel, NodeModel, NodeActionsProvider, TableModel
{

    private static final String RUNNING_STATE
                                      = "LBL_Running";                        // NOI18N

    private static final String SUSPENDED_STATE
                                      = "LBL_Suspended";                      // NOI18N

    private static final String INACTIVE_THREAD_STATE
                                      = "LBL_InactiveThreadState";            // NOI18N

    private static final String ACTIVE_THREAD_STATE
                                       = "LBL_ActiveThreadState";             // NOI18N

    private static final String THREAD_NAME
                                       = "LBL_ThreadName";                    // NOI18N

    public static final String CURRENT =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThread";  // NOI18N
    public static final String RUNNING =
        "org/netbeans/modules/debugger/resources/threadsView/RunningThread";  // NOI18N
    public static final String SUSPENDED =
        "org/netbeans/modules/debugger/resources/threadsView/SuspendedThread";// NOI18N

    public ThreadsModel(ContextProvider contextProvider) {
        myProvider = contextProvider;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ViewModelSupport#clearModel()
     */
    @Override
    public void clearModel() {
        update();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getRoot()
     */
    @Override
    public Object getRoot() {
        return ROOT;
    }

    public void update( ){
        refresh();
    }

    public void updateSession( DebugSession session ){
        updateThreadState(session);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getChildren(java.lang.Object, int, int)
     */
    @Override
    public Object[] getChildren(Object parent, int from, int to)
        throws UnknownTypeException
    {
        if (parent == ROOT) {
            SessionId id = getSessionId();
            if ( id == null ){
                return new Object[0];
            }
            DebugSession debugSession = ConversionUtils.toDebugSession(id);
            int size = (debugSession != null) ? 1 : 0;

            if ( from >= size ){
                return new Object[0];
            }
            int end = Math.min( to, size);
            if ( from == 0 && to == size ){
                return debugSession != null ? new Object[] {debugSession} : new Object[ 0 ];
            }
            ArrayList<DebugSession> list = new ArrayList<>();
            if (debugSession != null) {list.add(debugSession);}
            List<DebugSession> result = list.subList( from , end );
            return result.toArray(new Object[0]);
        }

        throw new UnknownTypeException(parent);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        }
        else if (node instanceof DebugSession) {
            return true;
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getChildrenCount(java.lang.Object)
     */
    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            SessionId id = getSessionId();
            if ( id == null ){
                return 0;
            }
            SessionManager sessionManager = SessionManager.getInstance();
            return sessionManager.findSessionsById(id).size();
        }
        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getDisplayName(java.lang.Object)
     */
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof DebugSession ) {
            DebugSession session = (DebugSession) node;
            String scriptName = getScriptName( session );

            return NbBundle.getMessage(ThreadsModel.class, THREAD_NAME,
                    scriptName );
        }
        else if (node == ROOT) {
            return ROOT.toString();
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getIconBase(java.lang.Object)
     */
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof DebugSession) {
            DebugSession session = (DebugSession)node;
            if ( session.getBridge().isSuspended()){
                return SUSPENDED;
            }
            else {
                if ( isCurrent( session ) ) {
                    return CURRENT;
                }
                else {
                    return RUNNING;
                }
            }
        }
        else if (node == ROOT) {
            return null;
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getShortDescription(java.lang.Object)
     */
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return null;
        }
        else if (node instanceof DebugSession) {
            return ((DebugSession)node).getFileName();
        }
        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeActionsProvider#performDefaultAction(java.lang.Object)
     */
    @Override
    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node instanceof DebugSession) {
            DebugSession session = (DebugSession) node;
            SessionId id = getSessionId();
            if ( id == null ){
                return;
            }
            DebugSession current =
                SessionManager.getInstance().getSession(id);

            if (! session.equals( current)) {
                StatusCommand command = new StatusCommand(
                        session.getTransactionId() );
                session.sendCommandLater(command);
                updateThreadState(current);
                updateThreadState(session);
            }
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeActionsProvider#getActions(java.lang.Object)
     */
    @Override
    public Action[] getActions(Object node) throws UnknownTypeException {
        return new Action [] {};
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#getValueAt(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getValueAt(Object node, String columnID)
        throws UnknownTypeException
    {
        if (node == ROOT) {
            return null;
        }

        if (node instanceof DebugSession) {
            DebugSession session = (DebugSession)node;
            switch (columnID) {
                case Constants.THREAD_SUSPENDED_COLUMN_ID:
                    return session.getBridge().isSuspended();
                case Constants.THREAD_STATE_COLUMN_ID:
                    String key = isCurrent(session) ? ACTIVE_THREAD_STATE :
                            INACTIVE_THREAD_STATE;
                    String value = session.getBridge().isSuspended() ?
                            SUSPENDED_STATE : RUNNING_STATE;
                    String result = NbBundle.getMessage(ThreadsModel.class,
                            key,NbBundle.getMessage(ThreadsModel.class, value ));
                    return result;
            }
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#isReadOnly(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isReadOnly(Object node, String columnID)
        throws UnknownTypeException
    {
        if (node == ROOT || node instanceof DebugSession ) {
            return true;
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#setValueAt(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
    public void setValueAt(Object node, String columnID, Object value)
        throws UnknownTypeException
    {
        throw new UnknownTypeException(node);
    }


    private void updateThreadState(DebugSession session) {
        fireChangeEvent(new ModelEvent.NodeChanged(this, session));
    }

    private String getScriptName( DebugSession session ) {
        SessionId id = session.getSessionId();
        if ( id == null ){
            return "";
        }
        String fileName = session.getFileName();
        FileObject script = id.toSourceFile( fileName );
        if (script == null) {
            return ""; //NOI18N
        }
        Project project = FileOwnerQuery.getOwner( script );
        return FileUtil.getRelativePath( project.getProjectDirectory(), script );
    }

    private Session getSession(){
        return (Session)getContextProvider().lookupFirst( null , Session.class );
    }

    private SessionId getSessionId(){
        ContextProvider provider = getContextProvider();
        if ( provider == null ){
            return null;
        }
        return (SessionId)provider.lookupFirst( null , SessionId.class );
    }

    private ContextProvider getContextProvider() {
        return myProvider;
    }

    private boolean isCurrent( DebugSession session ){
        SessionId id = getSessionId();
        DebugSession current =
            SessionManager.getInstance().getSession(id);
        return session.equals( current );
    }

    private ContextProvider myProvider;

}
