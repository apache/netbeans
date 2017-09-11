/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
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
final class ReferenceTransformer extends TreePathScanner {
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
