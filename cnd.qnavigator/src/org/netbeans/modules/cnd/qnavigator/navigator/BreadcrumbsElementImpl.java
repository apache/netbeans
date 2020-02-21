/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 */
class BreadcrumbsElementImpl implements BreadcrumbsElement {
    private final Node node;
    private final Lookup lookup;
    private final DataObject cdo;
    private BreadcrumbsElement parent;
    private List<BreadcrumbsElement> children;
    private final AtomicBoolean canceled;
    private final CharSequence text;

    public BreadcrumbsElementImpl(BreadcrumbsElement parent, Node node, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        lookup = Lookups.fixed(new OpenableImpl(node));
        this.parent = parent;
        this.node = node;
        this.cdo = cdo;
        this.canceled = canceled;
        this.text = text;
    }

    @Override
    public String getHtmlDisplayName() {
        String name = node.getName();
        if (name.indexOf('(') > 0) {
            name = name.substring(0, name.indexOf('('));
            if (node instanceof CppDeclarationNode) {
                CharSequence methodDefinitionScopeName = ((CppDeclarationNode) node).getMethodDefinitionScopeName();
                if (methodDefinitionScopeName != null && methodDefinitionScopeName.length() > 0) {
                    name = methodDefinitionScopeName+"::"+name; //NOI18N
                }
            }
        }
        name = CsmDisplayUtilities.htmlize(name);
        return name;
    }

    @Override
    public Image getIcon(int type) {
        return node.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return node.getOpenedIcon(type);
    }

    @Override
    public List<BreadcrumbsElement> getChildren() {
        if (children == null) {
            children = new ArrayList<BreadcrumbsElement>();
            if (node instanceof CppDeclarationNode) {
                CsmObject csmObject = ((CppDeclarationNode) node).getCsmObject();
                if (CsmKindUtilities.isOffsetable(csmObject)) {
                    if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                        CsmFunctionDefinition funDef = (CsmFunctionDefinition) csmObject;
                        CsmCompoundStatement body = funDef.getBody();
                        if (body != null) {
                            if (canceled != null && canceled.get()) {
                                return children;
                            }
                            for (CsmStatement st : body.getStatements()) {
                                if (canceled != null && canceled.get()) {
                                    break;
                                }
                                StatementNode statementNode = StatementNode.createStatementNode(st, null, this, cdo, canceled, text);
                                if (statementNode != null) {
                                    children.add(statementNode);
                                }
                            }
                        }
                        return children;
                    }
                }
            }
            for (Node n : node.getChildren().getNodes()) {
                children.add(new BreadcrumbsElementImpl(this, n, cdo, canceled, text));
            }
        }
        return children;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public BreadcrumbsElement getParent() {
        return parent;
    }

    Node getNode() {
        return node;
    }

    void setParent(BreadcrumbsRoot root) {
        parent = root;
    }

    private static final class OpenableImpl implements Openable, OpenCookie {

        private final Node node;

        public OpenableImpl(Node node) {
            this.node = node;
        }

        @Override
        public void open() {
            if (node instanceof CppDeclarationNode) {
                Action preferredAction = ((CppDeclarationNode) node).getPreferredAction();
                if (preferredAction instanceof AbstractAction) {
                    ((AbstractAction) preferredAction).actionPerformed(null);
                }
            }
        }
    }
    
    static final class BreadcrumbsRoot implements BreadcrumbsElement {
        private final DataObject cdo;
        private final BreadcrumbsElementImpl root;
        
        BreadcrumbsRoot(DataObject cdo, BreadcrumbsElementImpl root) {
            this.cdo = cdo;
            this.root = root;
        }

        @Override
        public String getHtmlDisplayName() {
            return "";
        }

        @Override
        public Image getIcon(int type) {
            return cdo.getNodeDelegate().getIcon(type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return cdo.getNodeDelegate().getOpenedIcon(type);
        }

        @Override
        public List<BreadcrumbsElement> getChildren() {
            return Collections.<BreadcrumbsElement>singletonList(root);
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public BreadcrumbsElement getParent() {
            return null;
        }
    
    }
}
