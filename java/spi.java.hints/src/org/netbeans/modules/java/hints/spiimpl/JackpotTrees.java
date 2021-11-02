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
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    // JDK 8-11
    public static JCCase createJCCase(Name ident, JCIdent jcIdent, List<?> stats) {
        return createInstance(JCCase.class, ident, jcIdent,
                new Class<?>[] {JCExpression.class, List.class},
                new Object[] {jcIdent, stats});
    }

    // JDK 12-17+
    public static JCCase createJCCase(Name ident, JCIdent jcIdent, String caseKind, List<?> labels, List<?> stats, JCTree body) throws ReflectiveOperationException {
        
        @SuppressWarnings("rawtypes")
        Class kindClass = Class.forName("com.sun.source.tree.CaseTree$CaseKind", false, JCCase.class.getClassLoader());
        @SuppressWarnings("unchecked")
        Object caseKindValue = Enum.valueOf(kindClass, caseKind);

        return createInstance(JCCase.class, ident, jcIdent,
                    new Class<?>[] {kindClass, List.class, List.class, JCTree.class},
                    new Object[] {caseKindValue, labels, stats, body});
    }

    // JDK 8-17+
    public static JCVariableDecl createJCVariableDecl(Name ident, JCIdent jcIdent, JCModifiers mods, Name param2, JCExpression vartype, JCExpression init, VarSymbol sym) {
        return createInstance(JCVariableDecl.class, ident, jcIdent,
                new Class<?>[] {JCModifiers.class, Name.class, JCExpression.class, JCExpression.class, VarSymbol.class},
                new Object[] {mods, param2, vartype, init, sym});
    }

    private static <T> T createInstance(Class<T> clazz, Name ident, JCIdent jcIdent, Class<?>[] requiredConstructor, Object[] params) {
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
            
            Constructor<?> compatible = null;
            for (Constructor<?> constructor : fake.getDeclaredConstructors()) {
                if (Arrays.equals(constructor.getParameterTypes(), requiredConstructor)) {
                    compatible = constructor;
                    break;
                }
            }
            
            if (compatible != null) {
                
                JCTree tree = (JCTree) compatible.newInstance(params);

                Field identField = fake.getDeclaredField("ident");
                identField.set(tree, ident);

                Field jcIdentField = fake.getDeclaredField("jcIdent");
                jcIdentField.set(tree, jcIdent);

                return clazz.cast(tree);
            } else {
                throw new IllegalStateException("no compatible constructors found in: "+Arrays.asList(fake.getDeclaredConstructors()).toString());
            }
        } catch (ReflectiveOperationException | IllegalArgumentException | IllegalStateException | SecurityException ex) {
            throw new IllegalStateException("can't instantiate "+Arrays.asList(requiredConstructor).toString()+" of "+clazz, ex);
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

        JCVariableDecl var = createJCVariableDecl(name, jcIdent, new FakeModifiers(), name, err, null, null);

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
