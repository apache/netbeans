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
package org.netbeans.modules.php.dbgp.models.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.EvalCommand;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;

public class ScriptWatchEvaluating extends AbstractModelNode implements ModelNode {
    private static final String WATCH_ICON = "org/netbeans/modules/debugger/resources/watchesView/Watch"; // NOI18N
    private Watch myWatch;
    private ContextProvider myProvider;
    private Property myValue;

    protected ScriptWatchEvaluating(ContextProvider provider, Watch watch) {
        super(null, null);
        myWatch = watch;
        myProvider = provider;
        requestValue();
    }

    public synchronized String getExpression() {
        return myWatch.getExpression();
    }

    public synchronized void remove() {
        myWatch.remove();
    }

    public synchronized void setExpression(String expression) {
        myWatch.setExpression(expression);
        requestValue();
    }

    @Override
    public String getName() {
        return myWatch.getExpression();
    }

    public String getDisplayName() {
        return myWatch.getExpression();
    }

    @Override
    public String getShortDescription() {
        return myWatch.getExpression();
    }

    @Override
    public String getIconBase() {
        return WATCH_ICON;
    }

    @Override
    public synchronized String getType() {
        if (myValue == null) {
            return null;
        }
        if (myValue.getType().equals(NbBundle.getMessage(ArrayVariableNode.class, ArrayVariableNode.TYPE_ARRAY))) {
            StringBuilder type = new StringBuilder(myValue.getType());
            return type.append("[").append(myValue.getChildrenSize()).append("]").toString(); // NOI18N
        }
        String className = myValue.getClassName();
        return (className != null && !className.isEmpty()) ? className : myValue.getType();
    }

    @Override
    public synchronized String getValue() throws UnsufficientValueException {
        if (!PhpOptions.getInstance().isDebuggerWatchesAndEval()) {
            return NbBundle.getMessage(ScriptWatchEvaluating.class, "WatchesAndEvalDisabled");
        }
        if (myValue == null) {
            return null;
        }
        return myValue.getStringValue();
    }

    @Override
    public VariableNode[] getChildren(int from, int to) {
        List<AbstractModelNode> list;
        synchronized (this) {
            if (getVariables() == null) {
                list = new ArrayList<>();
            } else {
                list = new ArrayList<AbstractModelNode>(getVariables());
            }
        }
        if (from >= list.size()) {
            return new VariableNode[0];
        }
        int end = Math.min(to, list.size());
        list = list.subList(from, end);
        return list.toArray(new VariableNode[0]);
    }

    @Override
    public boolean isLeaf() {
        return getChildrenSize() == 0;
    }

    @Override
    public boolean isReadOnly() {
        /*
         * Theoretically one could perfrom in Eval command response send
         * PropertyGet command with expression as name.
         * In case of appropraite answer one can allow edit such node
         * ( via PropertySet command ) by its full name.
         * But there are complexity in
         * 1) each time one need to each watch request property via PropertyGet
         * 2) for each child node one needs to build full name manually.
         *
         * For this time I disallow to edit values in watches view.
         */
        return true;
    }

    @Override
    public synchronized int getChildrenSize() {
        return getVariables() == null ? 0 : getVariables().size();
    }

    /**
     * Method intended for call only by WatchesModel. WatchesModel is
     * responsible for update this node value when debugger response with value.
     * This is done in async way.
     */
    protected synchronized void setEvaluated(Property value) {
        myValue = value;
        if (value != null) {
            initVariables(value.getChildren());
        }
    }

    protected void requestValue() {
        setEvaluated(null);
        DebugSession session = getSession();
        if (session == null) {
            return;
        }
        final String toEvaluation = getExpression();
        /* TODO : uncommented but it may cause following problems:
         * I found a bug in XDEbug with eval command:
         * after response to eval request it performs two actions:
         * 1) Stops script execution ( and debugging ) unexpectedly
         * 2) Response with unexpected "response" packet that don't contain
         * "command" attribute with "status" attribute equals to "stopped"
         * and "reason" equals "ok".
         * Need to investigate this more deeply and file a bug on XDebug.
         *
         * XDebug bug submitted:
         * http://bugs.xdebug.org/bug_view_page.php?bug_id=0000313
         *
         */
        if (PhpOptions.getInstance().isDebuggerWatchesAndEval()) {
            EvalCommand command = new EvalCommand(session.getTransactionId());
            command.setData(toEvaluation);
            session.sendCommandLater(command);
        }


    }

    @Override
    protected boolean isTypeApplied(Set<FilterType> set) {
        return true;
    }

    private SessionId getSessionId() {
        if (myProvider == null) {
            return null;
        }
        SessionId id = (SessionId) myProvider.lookupFirst(null,
                SessionId.class);
        if (id == null) {
            return null;
        }
        return id;
    }

    private DebugSession getSession() {
        return SessionManager.getInstance().getSession(
                getSessionId());
    }

}
