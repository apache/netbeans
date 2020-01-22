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
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;


/**
 *
 * @author lahvac
 */
public class JackpotTrees {

    private static final Map<Class<?>, Class<?>> baseClass2Impl = new HashMap<>();
    public static <T> T createInstance(Context ctx, Class<T> clazz, Name ident, JCIdent jcIdent, Class<?>[] requiredConstructor, Object[] params) {
        try {
            Class<?> fake = baseClass2Impl.get(clazz);

            if (fake == null) {
                Method visitIdent = Visitor.class.getDeclaredMethod("visitIdent", JCIdent.class);
                Method visitIdentifier = TreeVisitor.class.getDeclaredMethod("visitIdentifier", IdentifierTree.class, Object.class);
                Method toString = Object.class.getDeclaredMethod("toString");
                fake = Utilities.load(new ByteBuddy()
                        .subclass(clazz)
                        .implement(IdentifierTree.class)
                        .defineField("ident", Name.class, Visibility.PUBLIC)
                        .defineField("jcIdent", JCIdent.class, Visibility.PUBLIC)
                        .method(ElementMatchers.named("getName")).intercept(FieldAccessor.ofField("ident"))
                        .method(ElementMatchers.named("getKind")).intercept(FixedValue.value(Kind.IDENTIFIER))
                        .method(ElementMatchers.named("accept").and(ElementMatchers.takesArguments(Visitor.class))).intercept(MethodCall.invoke(visitIdent).onArgument(0).withField("jcIdent"))
                        .method(ElementMatchers.named("accept").and(ElementMatchers.takesArgument(0, TreeVisitor.class))).intercept(MethodCall.invoke(visitIdentifier).onArgument(0).withThis().withArgument(1))
                        .method(ElementMatchers.named("toString")).intercept(MethodCall.invoke(toString).onField("ident"))
                        .name(JackpotTrees.class.getCanonicalName() + "$" + clazz.getCanonicalName().replace('.', '$'))
                        .make())
                        .getLoaded();
                baseClass2Impl.put(clazz, fake);
            }

            NEXT: for (Constructor c : fake.getDeclaredConstructors()) {
                if (c.getParameterCount() < requiredConstructor.length)
                    continue;
                for (int e = 0; e < requiredConstructor.length; e++) {
                    if (!c.getParameterTypes()[e].equals(requiredConstructor[e])) {
                        continue NEXT;
                    }
                }
                java.util.List<Object> instances = new ArrayList<>();
                instances.addAll(Arrays.asList(params));
                for (int i = instances.size(); i < c.getParameterCount(); i++) {
                    instances.add(null);
                }

                JCTree tree = (JCTree) c.newInstance(instances.toArray(new Object[0]));

                Field identField = fake.getDeclaredField("ident");

                identField.set(tree, ident);

                Field jcIdentField = fake.getDeclaredField("jcIdent");

                jcIdentField.set(tree, jcIdent);

                return clazz.cast(tree);
            }

            throw new IllegalStateException(Arrays.asList(fake.getDeclaredConstructors()).toString());
        } catch (IllegalAccessException | IllegalArgumentException | IllegalStateException | InstantiationException | NoSuchFieldException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
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

        JCVariableDecl var;
        
        try {
            var = createInstance(ctx,
                                 JCVariableDecl.class,
                                 name,
                                 jcIdent,
                                 new Class<?>[] {JCModifiers.class, Name.class, JCExpression.class, JCExpression.class, VarSymbol.class},
                                 new Object[] {new FakeModifiers(), name, err, null, null});
        } catch (IllegalStateException ex) {
            try {
                var = createInstance(ctx,
                                     JCVariableDecl.class,
                                     name,
                                     jcIdent,
                                     new Class<?>[] {JCModifiers.class, Name.class, JCExpression.class, JCExpression.class, VarSymbol.class, List.class},
                                     new Object[] {new FakeModifiers(), name, err, null, null, List.nil()});
            } catch (IllegalStateException ex2) {
                throw ex;
            }
        }

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
