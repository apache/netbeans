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
