/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 */
public class NavigatorChildren extends Children.SortedArray {

    private CsmOffsetableDeclaration element;
    private CsmCompoundClassifier container;
    private CsmFileModel model;
    private List<IndexOffsetNode> lineNumberIndex;
    private final AtomicBoolean canceled;

    public NavigatorChildren(CsmOffsetableDeclaration element, CsmFileModel model, List<IndexOffsetNode> lineNumberIndex, AtomicBoolean canceled) {
        this(element, model, null, lineNumberIndex, canceled);
    }

    public NavigatorChildren(CsmOffsetableDeclaration element, CsmFileModel model, CsmCompoundClassifier container, List<IndexOffsetNode> lineNumberIndex, AtomicBoolean canceled) {
        this.element = element;
        this.container = container;
        this.model = model;
        this.lineNumberIndex = lineNumberIndex;
        this.canceled = canceled;
        this.getNodes();
    }

    @Override
    protected Collection<Node> initCollection() {
        List<CppDeclarationNode> retValue = new ArrayList<CppDeclarationNode>();
        if (container != null) {
            if (CsmKindUtilities.isClass(container)) {
                initClassifier((CsmClass) container, retValue);
            } else {
                initEnum((CsmEnum) container, retValue);
            }
        } else if (CsmKindUtilities.isClass(element)) {
            initClassifier((CsmClass) element, retValue);
        } else if (CsmKindUtilities.isEnum(element)) {
            initEnum((CsmEnum) element, retValue);
        } else if (CsmKindUtilities.isNamespaceDefinition(element)) {
            CsmNamespaceDefinition ns = (CsmNamespaceDefinition) element;
            if (!canceled.get()) {
                for (CsmDeclaration decl : ns.getDeclarations()) {
                    if (canceled.get()) {
                        break;
                    }
                    CppDeclarationNode node = CppDeclarationNode.nodeFactory(decl, model, false, lineNumberIndex, canceled);
                    if (node != null) {
                        retValue.add(node);
                    }
                }
            }
        }
        if (!canceled.get()) {
            Collections.sort(retValue);
        }
        // CppDeclarationNode is Node
        @SuppressWarnings("unchecked")
        List<Node> ret = ((List)retValue);
        return ret;
    }

    private void initClassifier(CsmClass cls, List<CppDeclarationNode> retValue) {
        if (canceled.get()) {
            return;
        }
        for (CsmMember member : cls.getMembers()) {
            if (canceled.get()) {
                return;
            }
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(member, model, false, lineNumberIndex, canceled);
            if (node != null) {
                retValue.add(node);
            }
        }
        if (canceled.get()) {
            return;
        }
        for (CsmFriend friend : cls.getFriends()) {
            if (canceled.get()) {
                return;
            }
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(friend, model, true, lineNumberIndex, canceled);
            if (node != null) {
                retValue.add(node);
            }
        }
    }

    private void initEnum(CsmEnum cls, List<CppDeclarationNode> retValue) {
        if (canceled.get()) {
            return;
        }
        for (CsmEnumerator en : cls.getEnumerators()) {
            if (canceled.get()) {
                return;
            }
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(en, model, false, lineNumberIndex, canceled);
            if (node != null) {
                retValue.add(node);
            }
        }
    }
}
