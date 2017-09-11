/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;


/**
 *
 * @author lahvac
 */
public class JackpotTrees {
    public static class AnnotationWildcard extends JCAnnotation implements IdentifierTree {

        private final Name ident;
        private final JCIdent jcIdent;

        public AnnotationWildcard(Name ident, JCIdent jcIdent) {
            super(Tag.ANNOTATION, jcIdent, List.<JCExpression>nil());
            this.ident = ident;
            this.jcIdent = jcIdent;
        }

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
            super(new FakeVariable(ctx, ident, jcIdent), TreeMaker.instance(ctx).Block(0, List.<JCStatement>nil()));
            this.ident = ident;
            this.jcIdent = jcIdent;
        }

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
    
    public static class VariableWildcard extends FakeVariable implements IdentifierTree {

        private final Name ident;

        public VariableWildcard(Context ctx, Name ident, JCIdent jcIdent) {
            super(ctx, ident, jcIdent);
            this.ident = ident;
        }

        public Name getName() {
            return ident;
        }

        @Override
        public Kind getKind() {
            return Kind.IDENTIFIER;
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

    private static class FakeVariable extends JCVariableDecl {
        
        private final JCIdent jcIdent;

        public FakeVariable(Context ctx, Name ident, JCIdent jcIdent) {
            super(new FakeModifiers(), ident, createType(ctx), null, null);
            this.sym = new VarSymbol(0, name, type, Symtab.instance(ctx).errSymbol);
            this.type = vartype.type;
            this.jcIdent = jcIdent;
        }

        private static JCErroneous createType(Context ctx) {
            JCErroneous err = new JCErroneous(List.<JCTree>nil()) {};

            err.type = Symtab.instance(ctx).errType;

            return err;
        }

        @Override
        public void accept(Visitor v) {
            v.visitIdent(jcIdent);
        }

    }

    private static class FakeModifiers extends JCModifiers {
        public FakeModifiers() {
            super(0, List.<JCAnnotation>nil());
        }
    }

    public static class CaseWildcard extends JCCase implements IdentifierTree {

        private final Name ident;
        private final JCIdent jcIdent;

        public CaseWildcard(Context ctx, Name ident, JCIdent jcIdent) {
            super(jcIdent, List.<JCStatement>nil());
            this.ident = ident;
            this.jcIdent = jcIdent;
        }

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
            return "case " + ident.toString();
        }

    }
    
    public static class FakeBlock extends JCBlock {

        public FakeBlock(long flags, List<JCStatement> stats) {
            super(flags, stats);
        }
        
    }
}
