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
        return subList.toArray(new ModelNode[subList.size()]);
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
