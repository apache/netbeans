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
            return result.toArray( new Object[ result.size() ] );
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
