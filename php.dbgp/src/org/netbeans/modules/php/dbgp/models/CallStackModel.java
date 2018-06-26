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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.ContextNamesCommand;
import org.netbeans.modules.php.dbgp.packets.Stack;
import org.netbeans.modules.php.dbgp.packets.StackGetResponse;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Mutex;

/**
 *
 * @author   ads
 */
public class CallStackModel extends ViewModelSupport
        implements TreeModel, NodeModel, NodeActionsProvider, TableModel {

    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/" +
        "callStackView/NonCurrentFrame";                    // NOI18N
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/" +
        "callStackView/CurrentFrame";                       // NOI18N

    public CallStackModel(final ContextProvider contextProvider) {
        myContextProvider = contextProvider;
        myStack = new AtomicReference<>();
        myCurrentStack = new AtomicReference<>();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ViewModelSupport#clearModel()
     */
    @Override
    public void clearModel() {
        setCallStack( new ArrayList<Stack>() );
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    public void setCallStack(List<Stack> stacks ){
        List<Stack> list = new ArrayList<>( stacks );
        myStack.set(list);
        if ( list.size() > 0 ) {
            myCurrentStack.set( list.get( 0 ));
        }
        refresh( );
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getChildren(java.lang.Object, int, int)
     */
    @Override
    public Object[] getChildren(Object parent, int from, int to)
        throws UnknownTypeException
    {
        if (parent == ROOT) {
            List<Stack> list = myStack.get();
            if ( list == null ){
                return new Object[0];
            }
            else {
                if ( from >= list.size() ) {
                    return new Object[0];
                }
                int end = Math.min( list.size(), to);
                List<Stack> stack = list.subList( from , end );
                return stack.toArray();
            }
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
        else if (node instanceof Stack) {
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
            List<Stack> list = myStack.get();
            if ( list == null ){
                return 0;
            }
            else {
                return list.size();
            }
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getDisplayName(java.lang.Object)
     */
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof Stack) {
            Stack stack = (Stack)node;
            String commandName = stack.getCurrentCommandName();
            if ( commandName == null ) {
                return getFile(stack) + ":" + stack.getLine();
            } else {
                return getFile(stack) + "." +commandName +":" + stack.getLine();
            }
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
        if (node instanceof Stack) {
            Stack curStack = myCurrentStack.get();
            if( curStack == node) {
                return CURRENT_CALL_STACK;
            }
            else {
                return CALL_STACK;
            }
        }
        else if (node == ROOT) {
            return null;
        }

        throw new UnknownTypeException (node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getShortDescription(java.lang.Object)
     */
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if(node == ROOT) {
            return null;
        }
        else if (node instanceof Stack) {
            return null;
        }

        throw new UnknownTypeException (node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeActionsProvider#performDefaultAction(java.lang.Object)
     */
    @Override
    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node instanceof Stack) {
            Stack stack = (Stack)node;

            updateDependentViews( stack );

            // Focus current file/line of selected stack frame.
            final Line line = Utils.getLine( stack.getLine(), stack.getFileName() ,
                    getSessionId());

            if (line != null) {
                Mutex.EVENT.readAccess(new Runnable () {
                    @Override
                    public void run() {
                        line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
                    }
                });
            }
        }
        throw new UnknownTypeException (node);
    }

    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {
        return new Action[] {};
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#getValueAt(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getValueAt(Object node, String columnID)
        throws UnknownTypeException
    {
        if(node == ROOT) {
            return null;
        }
        else if (node instanceof Stack) {
            if (Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID.equals(columnID)) {
                Stack stack = (Stack)node;

                return getFile(stack) ;
            }
            else {
                return "?! unknown column";     // NOI18N
            }
        }

        throw new UnknownTypeException (node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#isReadOnly(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isReadOnly(Object node, String columnID)
        throws UnknownTypeException
    {
        if(node == ROOT) {
            return true;
        }
        else if (node instanceof Stack) {
            return true;
        }

        throw new UnknownTypeException (node);
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

    private ContextProvider getContextProvider() {
        return myContextProvider;
    }

    private SessionId getSessionId(){
        return (SessionId)getContextProvider().lookupFirst( null,
                SessionId.class );
    }

    private String getFile( Stack stack ){
        String fileName = stack.getFileName();
        FileObject fileObject = getSessionId().toSourceFile( fileName );
        if ( fileObject == null ){
            // TODO inform somehow about local file is not found , output remote file
            return fileName;
        }
        else {
            Project project = FileOwnerQuery.getOwner( fileObject );
            if (project != null) {
                String retval = FileUtil.getRelativePath(project.getProjectDirectory() ,fileObject );
                if (retval != null) {
                    return retval;
                }
            }
            File f = FileUtil.toFile(fileObject);
            return f != null ? f.getAbsolutePath() : fileName;
        }
    }

    private DebugSession getSession() {
        return SessionManager.getInstance().getSession(
                getSessionId() );
    }

    private void updateDependentViews( Stack stack ) {
        // Update stack dependent models to current frame.
        myCurrentStack.set(stack);
        DebugSession session = getSession();
        if ( session == null ) {
            return;
        }

        // Update local view.
        int depth = stack.getLevel();
        ContextNamesCommand command = new ContextNamesCommand(
                session.getTransactionId() );
        command.setDepth(depth);
        session.sendCommandLater( command );

        // Update watches view
        /*
         * Currently this has no effect.
         * "eval" command performs evaluation only against current stack depth.
         * So changing <code>stack</code> object doesn't lead to any change in
         * watches view.
         * I have asked authors of XDebug inforamtion about this behavior.
         *
         * Result of asking authors is bug number 0000316.
         * http://bugs.xdebug.org/bug_view_page.php?bug_id=0000316
         */
        StackGetResponse.updateWatchView( session );

        refresh();
    }

    private final ContextProvider myContextProvider;

    private AtomicReference<List<Stack>> myStack;

    private AtomicReference<Stack>  myCurrentStack;

}
