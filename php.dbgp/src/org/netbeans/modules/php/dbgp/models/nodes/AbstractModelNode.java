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
            case ScalarTypeVariableNode.BOOLEAN:
            case ScalarTypeVariableNode.BOOL:
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
