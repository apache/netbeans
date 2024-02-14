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

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.models.VariablesModel;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.PropertyCommand;
import org.netbeans.modules.php.dbgp.packets.PropertyGetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertySetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertyValueCommand;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.openide.text.Line;

/**
 * @author ads
 *
 */
public abstract class AbstractVariableNode extends AbstractModelNode implements VariableNode {
    protected static final String FIELD_ICON = "org/netbeans/modules/debugger/resources/watchesView/Field"; // NOI18N
    private Property myProperty;

    /**
     * <code>property</code> is not authority for this class. It is used as
     * information. This class is initialized based on
     * <code>property</code>. And it could be updated later based on other
     * property . F.e. one can set new property via
     * {@link #setProperty(Property)} but this doesn't mean that all
     * AbstractVariableNode should be reinitialized due property change.
     * AbstractVariableNode provides its own information that updates by (
     * basically children ) adding/removing children. So children in current
     * <code>property</code> and AbstractVariableNode class could be different.
     */
    protected AbstractVariableNode(Property property, AbstractModelNode parent) {
        super(parent, property.getChildren());
        myProperty = property;
    }

    @Override
    public String getFullName() {
        return getProperty().getFullName();
    }

    @Override
    public String getName() {
        Property property = getProperty();
        String propertyName = property != null ? property.getName() : null;
        if (getParent() instanceof ArrayVariableNode) {
            StringBuilder builder = new StringBuilder("[");
            builder.append(propertyName);
            builder.append("]");
            return builder.toString();
        }
        return propertyName;
    }

    @Override
    public ModelNode[] getChildren(int from, int to) {
        List<AbstractVariableNode> subList = getVariables().subList(from, to);
        return subList.toArray(new ModelNode[0]);
    }

    @Override
    public int getChildrenSize() {
        return getVariables().size();
    }

    @Override
    public String getIconBase() {
        AbstractModelNode node = getParent();
        if (node instanceof ObjectVariableNode) {
            return FIELD_ICON;
        }
        return LOCAL_VARIABLE_ICON;
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public String getType() {
        return getProperty().getType();
    }

    @Override
    public String getValue() throws UnsufficientValueException {
        return getProperty().getStringValue();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Line findDeclarationLine() {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return !getProperty().hasChildren();
    }

    public void setupCommand(PropertyValueCommand valueCommand) {
        setupCommand((PropertyGetCommand) valueCommand);
        valueCommand.setMaxDataSize(getProperty().getSize());
    }

    public void setupCommand(PropertyGetCommand getCommand) {
        setupCommand((PropertyCommand) getCommand);
        String key = getProperty().getKey();
        if (key != null) {
            getCommand.setKey(key);
        }
    }

    public void setupCommand(PropertySetCommand command) {
        setupCommand((PropertyCommand) command);
    }

    public void setupFillChildrenCommand(PropertyGetCommand getCommand) {
        setupCommand(getCommand);
        int page = getProperty().getPage() + 1;
        getCommand.setDataPage(page);
    }

    public boolean isChildrenFilled() {
        int pageSize = getProperty().getPageSize();
        if (pageSize == 0) {
            return true;
        }
        int childrenSize = getProperty().getChildrenSize();
        int page = getProperty().getPage();
        return childrenSize <= (page + 1) * pageSize;
    }

    public int getContext() {
        return getRootContext().getIndex();
    }

    protected void setProperty(Property property) {
        Property old = getProperty();
        property.setName(old.getName());
        myProperty = property;
    }

    protected abstract void collectUpdates(VariablesModel variablesModel, VariableNode node, Collection<ModelEvent> events);

    protected Property getProperty() {
        return myProperty;
    }

    private ContextNode getRootContext() {
        AbstractModelNode retval = this;
        while (retval != null && !(retval instanceof ContextNode)) {
            retval = retval.getParent();
        }
        return (ContextNode) retval;
    }

    private void setupCommand(PropertyCommand command) {
        command.setName(getFullName());
        command.setContext(getContext());
    }

}
