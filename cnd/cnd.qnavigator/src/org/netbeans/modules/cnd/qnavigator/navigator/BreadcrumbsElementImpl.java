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
