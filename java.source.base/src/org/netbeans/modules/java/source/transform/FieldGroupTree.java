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
package org.netbeans.modules.java.source.transform;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.tree.TreeInfo;
import java.util.List;

/**
 * Fake tree. Represents fields separated by comma.
 *
 * @author pflaska
 */
public class FieldGroupTree extends JCTree implements Tree {

    private final List<JCVariableDecl> vars;
    private final boolean enumeration;
    private final boolean moreElementsFollowEnum;

    public FieldGroupTree(List<JCVariableDecl> vars) {
        this(vars, false, false);
    }

    public FieldGroupTree(List<JCVariableDecl> vars, boolean moreElementsFollowEnum) {
        this(vars, true, moreElementsFollowEnum);
    }

    private FieldGroupTree(List<JCVariableDecl> vars, boolean enumeration, boolean moreElementsFollowEnum) {
        this.vars = vars;
        pos = TreeInfo.getStartPos(vars.get(0));
        this.enumeration = enumeration;
        this.moreElementsFollowEnum = moreElementsFollowEnum;
    }

    public Kind getKind() {
        return Kind.OTHER;
    }

    public List<JCVariableDecl> getVariables() {
        return vars;
    }

    public boolean isEnum() {
        return enumeration;
    }

    public boolean moreElementsFollowEnum() {
        return moreElementsFollowEnum;
    }

    public int endPos() {
        return TreeInfo.endPos(vars.get(vars.size()-1));
    }

    public <R, D> R accept(TreeVisitor<R, D> arg0, D arg1) {
        R ret = null;
        for (JCVariableDecl v : vars) {
            ret = v.accept(arg0, arg1);
        }
        return ret;
    }

    public void accept(Visitor arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof FieldGroupTree) {
            return vars.equals(((FieldGroupTree) arg0).getVariables());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return vars.hashCode();
    }

    public Tag getTag() {
        return Tag.NO_TAG;
    }
}
