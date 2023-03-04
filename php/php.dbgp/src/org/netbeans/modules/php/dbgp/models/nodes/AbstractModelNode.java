/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.dbgp.models.VariablesModel;
import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.spi.viewmodel.ModelEvent;

/**
 * @author ads
 *
 */
public abstract class AbstractModelNode {
    private static final String RESOURCE = "resource"; // NOI18N
    private static final String OBJECT = "object"; // NOI18N
    private static final String ARRAY = "array"; // NOI18N
    private static final String STRING = "string"; // NOI18N
    private static final String UNDEF = "uninitialized"; // NOI18N
    private static final String NULL = "null"; // NOI18N
    private static final String BOOLEAN = "boolean"; // NOI18N
    private static final String BOOL = "bool"; // NOI18N
    private List<AbstractVariableNode> myVars;
    private AbstractModelNode myParent;

    AbstractModelNode(AbstractModelNode parent, List<Property> properties) {
        myParent = parent;
        initVariables(properties);
    }

    public AbstractModelNode getParent() {
        return myParent;
    }

    public boolean hasType(Set<FilterType> set) {
        return isTypeApplied(set);
    }

    public static org.netbeans.modules.php.dbgp.models.VariablesModel.AbstractVariableNode createVariable(Property property, AbstractModelNode parent) {
        String type = property.getType();
        switch (type) {
            case STRING:
                return new StringVariableNode(property, parent);
            case ARRAY:
                return new ArrayVariableNode(property, parent);
            case UNDEF:
                return new UndefinedVariableNode(property, parent);
            case NULL:
                return new NullVariableNode(property, parent);
            case OBJECT:
                return new ObjectVariableNode(property, parent);
            case RESOURCE:
                return new ResourceVariableNode(property, parent);
            case BOOLEAN:
            case BOOL:
                return new BooleanVariableNode(property, parent);
            case ScalarTypeVariableNode.INTEGER:
            case ScalarTypeVariableNode.INT:
            case ScalarTypeVariableNode.FLOAT:
                return new ScalarTypeVariableNode(property, parent);
            default:
                return new BaseVariableNode(property, parent);
        }
    }

    protected abstract boolean isTypeApplied(Set<FilterType> set);

    protected List<AbstractVariableNode> getVariables() {
        return myVars;
    }

    protected void initVariables(List<Property> properties) {
        if (properties == null) {
            return;
        }
        myVars = new ArrayList<>();
        for (Property property : properties) {
            org.netbeans.modules.php.dbgp.models.VariablesModel.AbstractVariableNode var = createVariable(property, this);
            myVars.add(var);
        }
    }

    protected void setVars(List<AbstractVariableNode> variables) {
        myVars = variables;
    }

    protected boolean addAbsentChildren(AbstractModelNode node) {
        boolean hasChanged = false;
        if (node.getVariables() != null && node.getVariables().size() > 0) {
            Iterator<AbstractVariableNode> iterator = node.getVariables().iterator();
            while (iterator.hasNext()) {
                AbstractVariableNode newChild = iterator.next();
                getVariables().add(newChild);
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    protected boolean updateExistedChildren(VariablesModel variablesModel, AbstractModelNode node, Collection<ModelEvent> events) {
        boolean hasChanged = false;
        List<AbstractVariableNode> list = new ArrayList<>(getVariables());
        int currentIndx = 0;
        for (AbstractVariableNode child : list) {
            Property property = child.getProperty();
            int newIndex = node.findChild(property);
            if (newIndex == -1) {
                getVariables().remove(currentIndx);
                hasChanged = true;
                continue;
            }
            AbstractVariableNode newChild = node.getVariables().get(newIndex);
            Property newProp = newChild.getProperty();
            if (property.getType().equals(newProp.getType())) {
                // properties are absolutely equal , need just update children and value
                node.getVariables().remove(newIndex);
                child.collectUpdates(variablesModel, newChild, events);
            } else {
                /*
                 * Properties have the same name only. But we need to change
                 * class for variable ( because they have different types ).
                 */
                getVariables().remove(currentIndx);
                getVariables().add(currentIndx, newChild);
                node.getVariables().remove(newIndex);
                hasChanged = true;
            }
            currentIndx++;
        }
        return hasChanged;
    }

    protected boolean updatePage(AbstractVariableNode node) {
        Property property = node.getProperty();
        if (property.getPageSize() > 0 && property.getPage() > 0) {
            addAbsentChildren(node);
            return true;
        } else {
            return false;
        }
    }

    protected int findChild(Property property) {
        int index = 0;
        for (AbstractVariableNode node : getVariables()) {
            Property prop = node.getProperty();
            String nodePropName = prop != null ? prop.getName() : null;
            if (nodePropName != null && nodePropName.equals(property.getName())) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
