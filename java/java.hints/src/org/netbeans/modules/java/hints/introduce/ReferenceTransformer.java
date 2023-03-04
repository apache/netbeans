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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author sdedic
 */
final class ReferenceTransformer extends ErrorAwareTreePathScanner {
    private final ElementKind kind;
    private final String name;
    private final WorkingCopy copy;
    private final Element target;
    private Element shadowed;
    private Element shadowedGate;
    
    public ReferenceTransformer(WorkingCopy info, ElementKind kind, 
            MemberSearchResult result, String name, Element target) {
        this.kind = kind;
        this.name = name;
        this.copy = info;
        this.target = target;
        if (result != null) {
            ElementHandle<? extends Element> s = result.getShadowed();
            if (s == null) {
                s = result.getOverriden();
            }
            if (s != null) {
                this.shadowed = s.resolve(info);
            }
            s = result.getShadowedGate();
            if (s != null) {
                this.shadowedGate = result.getShadowedGate().resolve(info);
            }
        }
    }

    @Override
    public Object scan(TreePath path, Object p) {
        if (shadowed == null || shadowedGate == null) {
            return null;
        }
        return super.scan(path, p);
    }

    @Override
    public Object visitMemberSelect(MemberSelectTree node, Object p) {
        Object o = scan(node.getExpression(), p);
        if (o == Boolean.TRUE) {
            handleIdentifier(node, node.getIdentifier());
        }
        return null;
    }
    
    private void handleIdentifier(Tree node, Name id) {
        TreeMaker mk = copy.getTreeMaker();
        String nn = id.toString();
        if (nn.equals(name)) {
            Element res = copy.getTrees().getElement(getCurrentPath());
            if (res != null && res == shadowed) {
                if (res.getModifiers().contains(Modifier.STATIC)) {
                    copy.rewrite(node, 
                            mk.MemberSelect(
                                mk.Identifier(shadowedGate), // NOI18N
                                res.getSimpleName().toString()
                            )
                    );
                } else if (shadowedGate == target) {
                    copy.rewrite(node, 
                            mk.MemberSelect(
                                mk.MemberSelect(mk.Identifier(target), "super"), // NOI18N
                                res.getSimpleName().toString()
                            )
                    );
                } else {
                    copy.rewrite(node, 
                            mk.MemberSelect(
                                mk.MemberSelect(mk.Identifier(shadowedGate), "this"), // NOI18N
                                res.getSimpleName().toString()
                            )
                    );
                }
            }
        }
    }

    @Override
    public Object visitIdentifier(IdentifierTree node, Object p) {
        String nn = node.getName().toString();
        if (nn.equals("this")) {
            return true;
        }
        handleIdentifier(node, node.getName());
        return null;
    }
}
