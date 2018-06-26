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

import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;

/**
 * Represent context which contains varaibles ( VariableNodes ). Could be Local,
 * Superglobal,...
 *
 * @author ads
 *
 */
public abstract class ContextNode extends AbstractModelNode implements ModelNode {
    private static final String SUPER_GLOBAL = "Superglobals"; // NOI18N
    private static final String SUPER_ICON = "org/netbeans/modules/debugger/resources/watchesView/SuperVariable"; // NOI18N
    private final String myName;
    private final int myIndex;

    protected ContextNode(Context ctx, List<Property> properties) {
        super(null, properties);
        myName = ctx.getContext();
        myIndex = ctx.getId();
    }

    @Override
    public String getName() {
        return myName;
    }

    public int getIndex() {
        return myIndex;
    }

    public int getVaraibleSize() {
        return getVariables().size();
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
        if (isGlobal()) {
            return SUPER_ICON;
        }
        return null;
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return getChildrenSize() == 0;
    }

    public boolean equalsTo(ContextNode node) {
        String name = node.myName;
        if (name == null) {
            return myName == null;
        } else {
            return name.equals(myName);
        }
    }

    public boolean isGlobal() {
        return SUPER_GLOBAL.equals(getDbgpName());
    }

    @Override
    protected boolean isTypeApplied(Set<FilterType> set) {
        if (!set.contains(FilterType.SUPERGLOBALS)) {
            return !isGlobal();
        }
        return true;
    }

    private String getDbgpName() {
        return myName;
    }

}
