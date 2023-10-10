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

package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCaseLabel;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 *
 * @author lahvac
 */
public class JackpotTrees {


    public static class VariableDeclWildcard extends JCVariableDecl implements IdentifierTree {

        private final Name ident;
        private final JCIdent jcIdent;

        public VariableDeclWildcard(Name ident, JCIdent jcIdent, JCModifiers mods, Name name, JCExpression vartype, JCExpression init, VarSymbol sym) {
            super(mods, name, vartype, init, sym);  // JDK 12-17+
            this.ident = ident;
            this.jcIdent = jcIdent;
        }

        @Override
        public Name getName() {
            return ident;
        }

        @Override
        public Kind getKind() {
            return Kind.IDENTIFIER;
        }

        @Override
        public void accept(Visitor v) {
            v.visitIdent(jcIdent);
        }

        @Override
        public <R, D> R accept(TreeVisitor<R, D> v, D d) {
            return v.visitIdentifier(this, d);
        }

        @Override
        public String toString() {
            return ident.toString();
        }

    }

    public static class CaseWildcard extends JCCase implements IdentifierTree {

        private final Name ident;
        private final JCIdent jcIdent;

        public CaseWildcard(Name ident, JCIdent jcIdent, CaseKind caseKind, List<JCCaseLabel> labels, JCExpression guard, List<JCStatement> stats, JCTree body) {
            super(caseKind, labels, guard, stats, body);
            this.ident = ident;
            this.jcIdent = jcIdent;
        }

        @Override
        public Name getName() {
            return ident;
        }

        @Override
        public Kind getKind() {
            return Kind.IDENTIFIER;
        }

        @Override
        public void accept(Visitor v) {
            v.visitIdent(jcIdent);
        }

        @Override
        public <R, D> R accept(TreeVisitor<R, D> v, D d) {
            return v.visitIdentifier(this, d);
        }

        @Override
        public String toString() {
            return ident.toString();
        }

    }

    public static class AnnotationWildcard extends JCAnnotation implements IdentifierTree {

        private final Name ident;
        private final JCIdent jcIdent;

        public AnnotationWildcard(Name ident, JCIdent jcIdent) {
            super(Tag.ANNOTATION, jcIdent, List.<JCExpression>nil());
            this.ident = ident;
            this.jcIdent = jcIdent;
        }

        @Override
        public Name getName() {
            return ident;
        }

        @Override
        public Kind getKind() {
            return Kind.IDENTIFIER;
        }

        @Override
        public void accept(Visitor v) {
            v.visitIdent(jcIdent);
        }

        @Override
        public <R, D> R accept(TreeVisitor<R, D> v, D d) {
            return v.visitIdentifier(this, d);
        }

        @Override
        public String toString() {
            return ident.toString();
        }

    }
    
    public static class CatchWildcard extends JCCatch implements IdentifierTree {

        private final Name ident;
        private final JCIdent jcIdent;

        public CatchWildcard(Context ctx, Name ident, JCIdent jcIdent) {
            super(createVariableWildcard(ctx, ident), TreeMaker.instance(ctx).Block(0, List.<JCStatement>nil()));
            this.ident = ident;
            this.jcIdent = jcIdent;
        }

        @Override
        public Name getName() {
            return ident;
        }

        @Override
        public Kind getKind() {
            return Kind.IDENTIFIER;
        }

        @Override
        public void accept(Visitor v) {
            v.visitIdent(jcIdent);
        }

        @Override
        public <R, D> R accept(TreeVisitor<R, D> v, D d) {
            return v.visitIdentifier(this, d);
        }

        @Override
        public String toString() {
            return "catch " + ident.toString();
        }

    }
    
    public static JCVariableDecl createVariableWildcard(Context ctx, Name name) {
        TreeMaker make = TreeMaker.instance(ctx);
        JCIdent jcIdent = make.Ident(name);

        JCErroneous err = new JCErroneous(List.<JCTree>nil()) {};

        err.type = Symtab.instance(ctx).errType;

        JCVariableDecl var = new VariableDeclWildcard(name, jcIdent, new FakeModifiers(), name, err, null, null);

        var.sym = new VarSymbol(0, name, var.vartype.type, Symtab.instance(ctx).errSymbol);
        var.type = var.vartype.type;
        return var;
    }

    private static class FakeModifiers extends JCModifiers {
        public FakeModifiers() {
            super(0, List.<JCAnnotation>nil());
        }
    }

    public static class FakeBlock extends JCBlock {

        public FakeBlock(long flags, List<JCStatement> stats) {
            super(flags, stats);
        }
        
    }
}
