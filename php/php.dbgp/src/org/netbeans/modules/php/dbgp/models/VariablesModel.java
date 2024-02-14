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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;

import java.util.logging.Logger;
import javax.swing.JToolTip;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.models.nodes.AbstractModelNode;
import org.netbeans.modules.php.dbgp.models.nodes.VariableNode;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.PropertyGetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertySetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertyValueCommand;
import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

public class VariablesModel extends ViewModelSupport implements TreeModel, TableModel, NodeModel {

    private static final Logger LOGGER = Logger.getLogger(VariablesModel.class.getName());

    // #239743
    private static final boolean SHOW_FULL_VALUES = Boolean.getBoolean("nb.php.debugger.full.values"); // NOI18N
    private static final String FULL_VALUE_LENGTH = "nb.php.debugger.full.value.length"; // NOI18N
    private static final int MAX_VALUE_LENGTH;
    private static final int DEFAULT_MAX_VALUE_LENGTH = 2000;

    private static final String EVALUATING = "TXT_Evaluating"; // NOI18N
    static final String GET_SHORT_DESCRIPTION = "getShortDescription"; // NOI18N
    static final String NULL = "null"; // NOI18N
    private final ContextProvider myContextProvider;
    //@GuardedBy("this")
    private DebugSession debugSession;
    private List<ModelNode> myNodes;
    private ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();
    private ReadLock myReadlock = myLock.readLock();
    private WriteLock myWritelock = myLock.writeLock();

    static {
        if (SHOW_FULL_VALUES) {
            LOGGER.log(Level.INFO, "Max value length unlimited");
            MAX_VALUE_LENGTH = -1;
        } else {
            int maxValueLength;
            try {
                maxValueLength = Integer.parseInt(System.getProperty(FULL_VALUE_LENGTH, String.valueOf(DEFAULT_MAX_VALUE_LENGTH)));
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.INFO, "Invalid max value length given", ex);
                maxValueLength = DEFAULT_MAX_VALUE_LENGTH;
            }
            if (maxValueLength <= 10) {
                LOGGER.log(Level.INFO, "Invalid max value length given, must be >= 10 (was {0})", maxValueLength);
                maxValueLength = DEFAULT_MAX_VALUE_LENGTH;
            }
            LOGGER.log(Level.INFO, "Max value length set to {0}", maxValueLength);
            MAX_VALUE_LENGTH = maxValueLength;
        }
    }


    public VariablesModel(final ContextProvider contextProvider) {
        myContextProvider = contextProvider;
        myNodes = new LinkedList<>();
    }

    @Override
    public void clearModel() {
        myWritelock.lock();
        try {
            myNodes.clear();
        } finally {
            myWritelock.unlock();
        }
        fireTreeChanged();
    }

    @Override
    public Object getRoot() {
        return ROOT; // ROOT is defined by TreeModel
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        myReadlock.lock();
        try {
            // Should be only two cases -- ROOT or a node from our tree
            ModelNode usedParent = null;
            if (parent == ROOT) {
                List<ModelNode> list = getTopLevelElements();
                if (from >= list.size()) {
                    return new Object[0];
                }
                int end = Math.min(list.size(), to);
                List<ModelNode> contexts = list.subList(from, end);
                return contexts.toArray(new Object[0]);
            } else if (parent instanceof ModelNode) {
                usedParent = (ModelNode) parent;
            }
            if (usedParent != null) {
                int size = ((ModelNode) parent).getChildrenSize();
                if (from >= size) {
                    return new Object[0];
                }
                int end = Math.min(size, to);
                return ((ModelNode) parent).getChildren(from, end);
            }
        } finally {
            myReadlock.unlock();
        }

        throw new UnknownTypeException(parent + " " + parent.getClass().getName());
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == null) {
            return true;
        } else if (node == ROOT) {
            return myNodes.isEmpty();
        } else if (node instanceof ModelNode) {
            ModelNode modelNode = (ModelNode) node;
            DebugSession session = getSession();
            if (session != null) {
                childrenRequest(modelNode, session);
                fillChildrenList(modelNode, session);
            }
            return modelNode.isLeaf();
        }

        throw new UnknownTypeException(node);
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        myReadlock.lock();
        try {
            if (node == ROOT) {
                //return myNodes.size();
                return getTopLevelElements().size();
            } else if (node instanceof ModelNode) {
                return ((ModelNode) node).getChildrenSize();
            }

            throw new UnknownTypeException(node);
        } finally {
            myReadlock.unlock();
        }
    }

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node instanceof JToolTip) {
            return getTooltip(((JToolTip) node), columnID);
        }
        String result = ""; // default is blank
        switch (columnID) {
            case Constants.LOCALS_TYPE_COLUMN_ID:
                if (node instanceof ModelNode) {
                    String type = ((ModelNode) node).getType();
                    assert type != null;
                    result = type;
                } else {
                    result = (node != null) ? node.getClass().getName() : "";
                }
                break;
            case Constants.LOCALS_VALUE_COLUMN_ID:
                if (node instanceof ModelNode) {
                    ModelNode modelNode = (ModelNode) node;
                    try {
                        result = shortenValue(modelNode.getValue());
                    } catch (UnsufficientValueException e) {
                        sendValueCommand(modelNode);
                        return NbBundle.getMessage(VariablesModel.class, EVALUATING);
                    }
                } else if (node == null) {
                    result = "";
                }
                break;
            default:
                //no-op
        }

        return result;
    }

    @Override
    public boolean isReadOnly(Object node, String string) throws UnknownTypeException {
        return false;
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        assert value instanceof String;
        if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
            if (!(node instanceof VariableNode)) {
                throw new UnknownTypeException(node);
            }
            ModelNode modelNode = (ModelNode) node;
            if (modelNode.isReadOnly()) {
                throw new UnknownTypeException(node);
            }
            DebugSession session = getSession();
            if (session == null) {
                // TODO : need signal to user about inability to set value
                return;
            }
            PropertySetCommand command = new PropertySetCommand(session.getTransactionId());
            command.setData((String) value);
            assert node instanceof AbstractVariableNode;
            ((AbstractVariableNode) node).setupCommand(command);
            session.sendCommandLater(command);
        }
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        String retval = null;
        if (node == ROOT) {
            retval = ROOT;
        } else if (node instanceof ModelNode) {
            retval = ((ModelNode) node).getName();
        } else if (node != null) {
            throw new UnknownTypeException(node);
        }
        if (retval == null && node != null) {
            Logger.getLogger(VariablesModel.class.getName()).log(Level.WARNING, "display name isn''t expected to be null: {0}", node.getClass().getName());
        }
        return (retval != null) ? retval : NULL;
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node == null || node == ROOT) {
            return VariableNode.LOCAL_VARIABLE_ICON;
        } else if (node instanceof ModelNode) {
            return ((ModelNode) node).getIconBase();
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node == null || node == ROOT) {
            return null;
        } else if (node instanceof ModelNode) {
            return ((ModelNode) node).getShortDescription();
        }
        throw new UnknownTypeException(node);
    }

    public void updateContext(ContextNode node) {
        myWritelock.lock();
        try {
            if (myNodes.isEmpty()) {
                myNodes.add(node);
            } else {
                boolean found = false;
                for (ModelNode child : myNodes) {
                    if (!(child instanceof ContextNode)) {
                        continue;
                    }
                    if (node.equalsTo((ContextNode) child)) {
                        updateContext((ContextNode) child, node);
                        found = true;
                    }
                }
                if (!found) {
                    myNodes.add(node);
                }
            }
            fireTreeChanged();
        } finally {
            myWritelock.unlock();
        }
    }

    public void updateProperty(Property property) {
        myWritelock.lock();
        try {
            for (ModelNode node : myNodes) {
                if (updateVariable(node, property)) {
                    return;
                }
            }
        } finally {
            myWritelock.unlock();
        }
    }

    private boolean updateVariable(ModelNode node, Property property) {
        if (node instanceof AbstractVariableNode) {
            AbstractVariableNode var = (AbstractVariableNode) node;
            String name = var.getFullName();
            final String propertyFullName = property.getFullName();
            String propertyName = property.getName();
            if ((propertyFullName != null && propertyFullName.equals(name)) || propertyName.equals(name)) {
                Collection<ModelEvent> events = new ArrayList<>();
                var.collectUpdates(this, AbstractModelNode.createVariable(property, var.getParent()), events);
                fireTableUpdate(events);
                return true;
            }
        }
        for (ModelNode child : node.getChildren(0, node.getChildrenSize())) {
            if (updateVariable(child, property)) {
                return true;
            }
        }
        return false;
    }

    private List<ModelNode> getTopLevelElements() {
        List<ModelNode> result = new LinkedList<>();
        for (ModelNode node : myNodes) {
            if (node instanceof ContextNode && !((ContextNode) node).isGlobal()) {
                result.addAll(Arrays.asList(node.getChildren(0, node.getChildrenSize())));
            } else {
                result.add(0, node);
            }
        }
        return result;
    }

    /*
     * This is how tooltips are implemented in the debugger views.
     */
    private String getTooltip(JToolTip tooltip, String columnId) throws UnknownTypeException {
        Object row = tooltip.getClientProperty(VariablesModel.GET_SHORT_DESCRIPTION);
        // TODO
        if (row instanceof ModelNode) {
            return getValueAt(row, columnId).toString();
        }
        throw new UnknownTypeException(tooltip);
    }

    private void sendValueCommand(ModelNode modelNode) {
        if (modelNode instanceof AbstractVariableNode) {
            AbstractVariableNode node = (AbstractVariableNode) modelNode;
            DebugSession session = getSession();
            PropertyValueCommand command = new PropertyValueCommand(session.getTransactionId());
            node.setupCommand(command);
            session.sendCommandLater(command);
        }
    }

    private void updateContext(ContextNode old, ContextNode node) {
        Collection<ModelEvent> events = new LinkedList<>();
        old.collectUpdates(this, node, events);
        fireTableUpdate(events);
    }

    private void fireTreeChanged() {
        refresh();
    }

    private void fireTableUpdate(Collection<ModelEvent> events) {
        fireChangeEvents(events);
    }

    private synchronized DebugSession getSession() {
        if (debugSession == null) {
            ContextProvider provider = getContextProvider();
            if (provider != null) {
                SessionId id = (SessionId) provider.lookupFirst(null, SessionId.class);
                if (id != null) {
                    debugSession = SessionManager.getInstance().getSession(id);
                }
            }
        }
        return debugSession;
    }

    private ContextProvider getContextProvider() {
        return myContextProvider;
    }

    /**
     * This method should check children availability and request them is they
     * absent originally ( it relates to max_depth option in debugger engine ).
     */
    private void childrenRequest(ModelNode modelNode, DebugSession session) {
        int size = modelNode.getChildrenSize();
        if (!modelNode.isLeaf() && size == 0) {
            assert modelNode instanceof AbstractVariableNode;
            PropertyGetCommand getCommand = new PropertyGetCommand(session.getTransactionId());
            ((AbstractVariableNode) modelNode).setupCommand(getCommand);
            session.sendCommandLater(getCommand);
        }
    }

    /**
     * This method should check quantity of retrieved children. It retrieve next
     * page of children if necessarily. This relates to max_children option in
     * debugger engine.
     */
    private void fillChildrenList(ModelNode modelNode, DebugSession session) {
        if (!(modelNode instanceof AbstractVariableNode)) {
            return;
        }
        AbstractVariableNode var = (AbstractVariableNode) modelNode;
        if (session != null && !var.isChildrenFilled()) {
            PropertyGetCommand command = new PropertyGetCommand(session.getTransactionId());
            var.setupFillChildrenCommand(command);
            session.sendCommandLater(command);
        }
    }

    // #239743
    @NbBundle.Messages({
        "# {0} - shortened value",
        "VariablesModel.value.shortened={0}... [shortened]",
    })
    private String shortenValue(String value) {
        if (SHOW_FULL_VALUES) {
            return value;
        }
        int length = value.length();
        if (length <= MAX_VALUE_LENGTH) {
            return value;
        }
        LOGGER.log(Level.INFO, "Shortening value from {0} to {1}", new Object[] {length, MAX_VALUE_LENGTH});
        return Bundle.VariablesModel_value_shortened(value.substring(0, MAX_VALUE_LENGTH));
    }

    public static class ContextNode extends org.netbeans.modules.php.dbgp.models.nodes.ContextNode {

        public ContextNode(Context ctx, List<Property> properties) {
            super(ctx, properties);
        }

        void collectUpdates(VariablesModel variablesModel, ContextNode node, Collection<ModelEvent> events) {
            boolean hasChanged = false;
            if ((getVariables() == null || getVariables().isEmpty()) && node.getVariables() != null) {
                setVars(node.getVariables());
                hasChanged = true;
            } else if (getVariables() != null) {
                hasChanged = updateExistedChildren(variablesModel, node, events);
                hasChanged = addAbsentChildren(node) || hasChanged;
            }
            if (hasChanged) {
                events.add(new ModelEvent.NodeChanged(variablesModel, this));
            }
        }

    }

    public abstract static class AbstractVariableNode extends org.netbeans.modules.php.dbgp.models.nodes.AbstractVariableNode {

        protected AbstractVariableNode(Property property, AbstractModelNode parent) {
            super(property, parent);
        }

        @Override
        protected void collectUpdates(VariablesModel variablesModel, VariableNode node, Collection<ModelEvent> events) {
            AbstractVariableNode newNode = (AbstractVariableNode) node;
            boolean hasChanged = false;
            /*
             * Always update property.
             */
            setProperty(newNode.getProperty());
            if (updatePage(newNode)) {
                if (newNode.getChildrenSize() > 0) {
                    events.add(new ModelEvent.NodeChanged(variablesModel, this));
                }
                return;
            }
            if (!Property.equals(getProperty(), newNode.getProperty())) {
                hasChanged = true;
            }
            if ((getVariables() == null || getVariables().isEmpty()) && newNode.getVariables() != null) {
                initVariables(newNode.getProperty().getChildren());
                hasChanged = true;
            } else if (getVariables() != null) {
                hasChanged = updateExistedChildren(variablesModel, newNode, events) || hasChanged;
                hasChanged = addAbsentChildren(newNode) || hasChanged;
            }
            if (hasChanged) {
                events.add(new ModelEvent.NodeChanged(variablesModel, this));
            }
        }

    }

}
