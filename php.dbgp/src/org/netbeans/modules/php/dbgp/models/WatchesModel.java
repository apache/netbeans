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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JToolTip;
import org.netbeans.api.debugger.*;
import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.models.nodes.VariableNode;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.*;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/**
 * @author ads
 */
public class WatchesModel extends ViewModelSupport
    implements TreeModel, NodeModel, TableModel
{
    public WatchesModel(ContextProvider lookupProvider) {
        myLookupProvider = lookupProvider;
        myWatcheNodes = new AtomicReference<>();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ViewModelSupport#clearModel()
     */
    @Override
    public void clearModel() {
        fireTreeChanged();
    }

    public void updateExpressionValue( String expr, Property value ){
        ScriptWatchEvaluating[] nodes = myWatcheNodes.get();
        if ( nodes == null ) {
            return;
        }
        for (ScriptWatchEvaluating node : nodes) {
            String expression = node.getExpression();
            if ( expr.equals( expression )){
                node.setEvaluated( value );
                fireTableValueChangedComputed( node , null);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getRoot()
     */
    @Override
    public Object getRoot() {
        return ROOT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getChildren(java.lang.Object, int, int)
     */
    @Override
    public Object[] getChildren(Object parent, int from, int to)
        throws UnknownTypeException
    {
        if(parent == ROOT) {
            // get watches
            Watch [] allWatches = DebuggerManager.getDebuggerManager().getWatches();
            to = Math.min(allWatches.length, to);
            from = Math.min(allWatches.length, from);
            Watch [] watches = new Watch [to - from];
            System.arraycopy(allWatches, from, watches, 0, to - from);

            ScriptWatchEvaluating[] evaluatedWatches =
                getEvaluatingWatches(watches);

            if(myListener == null) {
                myListener = new Listener();
            }
            myWatcheNodes.set( evaluatedWatches );
            return evaluatedWatches;
        }
        else if(parent instanceof ModelNode) {
            return ((ModelNode)parent).getChildren(from, to);
        }
        throw new UnknownTypeException(parent + " " + parent.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getChildrenCount(java.lang.Object)
     */
    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if(node == ROOT) {
            if(myListener == null) {
                myListener = new Listener();
            }
            // Performance, see issue #59058.
            return Integer.MAX_VALUE; // DebuggerManager.getDebuggerManager().getWatches().length;
        }
        else if(node instanceof ModelNode) {
            return ((ModelNode)node).getChildrenSize();
        }

        throw new UnknownTypeException(node + " " + node.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if(node == ROOT) {
            return false;
        }
        else if(node instanceof ModelNode) {
            return ((ModelNode) node).getChildrenSize() ==0;
        }

        throw new UnknownTypeException(node + " " + node.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#getValueAt(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getValueAt(Object node, String columnID)
        throws UnknownTypeException
    {
        if(node instanceof JToolTip) {
            return getTooltip( ( (JToolTip) node), columnID);
        }
        switch (columnID) {
            case Constants.WATCH_TYPE_COLUMN_ID:
                if(node instanceof ModelNode) {
                    return ((ModelNode)node).getType();
                }
                break;
            case Constants.WATCH_VALUE_COLUMN_ID:
                if(node instanceof ModelNode) {
                    Object value;
                    try {
                        value = ((ModelNode)node).getValue();
                    }
                    catch (UnsufficientValueException e) {
                        /*
                         *  This should not happened for property in eval command
                         *  becuase we are not able to send command property_value.
                         */

                        return VariablesModel.NULL;
                    }
                    return value == null ? VariablesModel.NULL : value;
                }
                break;
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#isReadOnly(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isReadOnly(Object node, String string)
        throws UnknownTypeException
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#setValueAt(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
    public void setValueAt(Object node, String string, Object value)
        throws UnknownTypeException
    {
        /*
         * See comments in ScriptWatchEvaluating#isReadOnly() method.
         */
        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getDisplayName(java.lang.Object)
     */
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == null) {
            return VariablesModel.NULL;
        }
        else if (node == ROOT) {
            return ROOT;
        }
        else if (node instanceof ModelNode) {
            return ((ModelNode)node).getName();
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getIconBase(java.lang.Object)
     */
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node == null || node == ROOT) {
            return VariableNode.LOCAL_VARIABLE_ICON;
        }
        else if (node instanceof ModelNode) {
            return ((ModelNode)node).getIconBase();
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getShortDescription(java.lang.Object)
     */
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node == null || node == ROOT) {
            return null;
        }
        else if (node instanceof ModelNode) {
            return ((ModelNode)node).getShortDescription();
        }

        throw new UnknownTypeException(node);
    }

    /*
     * This is how tooltips are implemented in the debugger views.
     */
    private String getTooltip( JToolTip tooltip, String columnId )
            throws UnknownTypeException
    {
        Object row = tooltip.getClientProperty(
                VariablesModel.GET_SHORT_DESCRIPTION);

        if (Constants.WATCH_TYPE_COLUMN_ID.equals(columnId)) {
            if (row instanceof ModelNode) {
                return ((ModelNode) row).getType();
            }
        }
        throw new UnknownTypeException( tooltip );
    }

    private void fireTreeChanged() {
        synchronized(getWatchesMap()) {
            for (ScriptWatchEvaluating watch :  getWatchesMap().values()) {
                watch.requestValue();
            }
        }
        fireChangeEvent(new ModelEvent.TreeChanged(this));
    }

    private void fireWatchesChanged() {
        fireChangeEvent(
                new ModelEvent.NodeChanged(this, ROOT,
                        ModelEvent.NodeChanged.CHILDREN_MASK));
    }

    void fireTableValueChanged(Object node, String propertyName) {
        ((ScriptWatchEvaluating) node).requestValue();
        fireTableValueChangedComputed(node, propertyName);
    }

    private void fireTableValueChangedComputed(Object node, String propertyName) {
        fireChangeEvent(new ModelEvent.TableValueChanged(this, node, propertyName));
    }

    private ScriptWatchEvaluating[] getEvaluatingWatches( Watch[] watches ) {
        // create ScriptWatchEvaluating for Watches
        ScriptWatchEvaluating[] evaluatingWatches =
            new ScriptWatchEvaluating[watches.length];
        int i =0;
        for(Watch watch : watches) {
            ScriptWatchEvaluating evaluatingWatch = getWatchesMap().get(watch);
            if(evaluatingWatch == null) {
                evaluatingWatch = new ScriptWatchEvaluating( myLookupProvider ,
                        watch);
                getWatchesMap().put(watches[i], evaluatingWatch);
            }
            evaluatingWatches[i++] = evaluatingWatch;
        }
        return evaluatingWatches;
    }

    private static class ScriptWatchEvaluating extends
        org.netbeans.modules.php.dbgp.models.nodes.ScriptWatchEvaluating
    {

        protected ScriptWatchEvaluating( ContextProvider provider, Watch watch )
        {
            super(provider, watch);
        }

        @Override
        protected synchronized void setEvaluated( Property value ){
            super.setEvaluated( value );
        }

        @Override
        protected void requestValue() {
            super.requestValue();
        }
    }


    private Map<Watch, ScriptWatchEvaluating> getWatchesMap(){
        return myWatches;
    }

    private Listener myListener;

    private ContextProvider myLookupProvider;

    private Map<Watch, ScriptWatchEvaluating> myWatches=
            new WeakHashMap<>();

    private AtomicReference<ScriptWatchEvaluating[]> myWatcheNodes;

    private static final ClearingThread<Listener> CLERAING_THREAD
        = new ClearingThread<>();

    static {
        CLERAING_THREAD.start();
    }

    class Listener extends DebuggerManagerAdapter
            implements PropertyChangeListener
    {

        private Listener() {

            myListener =
                new WeakProxyListener<>( this , CLERAING_THREAD.getQueue() );
            DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_WATCHES,
                myListener
            );
            Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
            for( Watch watch : watches ) {
                watch.addPropertyChangeListener( myListener );
            }
        }

        @Override
        public void watchAdded(Watch watch) {
            watch.addPropertyChangeListener(this);
            fireWatchesChanged();
        }

        @Override
        public void watchRemoved(Watch watch) {
            watch.removePropertyChangeListener(this);
            fireWatchesChanged();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            // We already have watchAdded & watchRemoved. Ignore PROP_WATCHES:
            if(DebuggerManager.PROP_WATCHES.equals(propName)) {
                return;
            }

            if(evt.getSource() instanceof Watch) {
                Object node;
                synchronized(getWatchesMap()) {
                    node = getWatchesMap().get((Watch) evt.getSource());
                }
                if(node != null) {
                    fireTableValueChanged(node, null);
                    return ;
                }
            }

            myListener.setupTask();
        }

        void fireTreeChanged() {
            WatchesModel.this.fireTreeChanged();
        }

        private WeakProxyListener<Listener> myListener;

    }

}

class WeakProxyListener<T extends
    org.netbeans.modules.php.dbgp.models.WatchesModel.Listener>
        extends WeakReference<T>
            implements PropertyChangeListener, DebuggerManagerListener
{

    private static final RequestProcessor RP = new RequestProcessor("Watches Model - Weak Proxy Listener"); //NOI18N

    WeakProxyListener( T t , ReferenceQueue<T> queue) {
        super(t, queue);
    }

    void setupTask() {
        if( getTask() == null) {
            myTask = RP.create(
                new Runnable() {
                    @Override
                    public void run() {
                        org.netbeans.modules.php.dbgp.models.WatchesModel.Listener
                            listener = get();
                        if ( listener == null ){
                            return;
                        }
                        listener.fireTreeChanged();
                    }
                });
        }
        getTask().schedule(100);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange( PropertyChangeEvent arg0 ) {
        T t = get();
        if (t != null){
            ((PropertyChangeListener) t).propertyChange(arg0);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#breakpointAdded(org.netbeans.api.debugger.Breakpoint)
     */
    @Override
    public void breakpointAdded( Breakpoint breakpoint ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
     */
    @Override
    public void breakpointRemoved( Breakpoint breakpoint ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#engineAdded(org.netbeans.api.debugger.DebuggerEngine)
     */
    @Override
    public void engineAdded( DebuggerEngine engine ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
     */
    @Override
    public void engineRemoved( DebuggerEngine engine ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#initBreakpoints()
     */
    @Override
    public Breakpoint[] initBreakpoints() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#initWatches()
     */
    @Override
    public void initWatches() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#sessionAdded(org.netbeans.api.debugger.Session)
     */
    @Override
    public void sessionAdded( Session session ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#sessionRemoved(org.netbeans.api.debugger.Session)
     */
    @Override
    public void sessionRemoved( Session session ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#watchAdded(org.netbeans.api.debugger.Watch)
     */
    @Override
    public void watchAdded( Watch watch ) {
        T t = get();
        if (t != null){
            ((DebuggerManagerListener) t).watchAdded(watch);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.debugger.DebuggerManagerListener#watchRemoved(org.netbeans.api.debugger.Watch)
     */
    @Override
    public void watchRemoved( Watch watch ) {
        T t = get();
        if (t != null){
            ((DebuggerManagerListener) t).watchRemoved(watch);
        }
    }

    Task getTask(){
        return myTask;
    }

    // currently waiting / running refresh task
    // there is at most one
    private Task myTask;

}

class ClearingThread<T extends
    org.netbeans.modules.php.dbgp.models.WatchesModel.Listener> extends Thread
{

    ClearingThread(){
        myQueue = new ReferenceQueue<>();
    }

    @Override
    public void run()
    {
        while (!isInterrupted()) {
            try {
                java.lang.ref.Reference<? extends T> ref = getQueue().remove(
                        1000);
                if ( ref instanceof DebuggerManagerListener ){
                    DebuggerManagerListener listener =
                        (DebuggerManagerListener) ref;
                    DebuggerManager.getDebuggerManager().removeDebuggerListener(
                            listener );
                }
                if ( ref instanceof PropertyChangeListener ){
                    PropertyChangeListener listener =
                        (PropertyChangeListener) ref;
                    Watch[] watches =
                        DebuggerManager.getDebuggerManager().getWatches();
                    for( Watch watch : watches ) {
                        watch.removePropertyChangeListener( listener );
                    }
                }
            }
            catch (IllegalArgumentException e) {
                assert false;
            }
            catch (InterruptedException e) {
                return;
            }
        }
    }

    ReferenceQueue<T> getQueue(){
        return myQueue;
    }

    private final ReferenceQueue<T> myQueue;
}
