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
package org.netbeans.modules.java.source.queriesimpl;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.queries.api.QueryException;
import org.netbeans.modules.java.source.queries.spi.ModelOperations;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
class JavaOperationsImpl<T> implements ModelOperations {

    private final CompilationController control;

    JavaOperationsImpl(@NonNull final CompilationController control) {
        assert control != null;
        this.control = control;
    }

    @Override
    @NonNull
    public Collection<? extends String> getTopLevelClasses() throws QueryException {
        try {
            if (control.toPhase(Phase.ELEMENTS_RESOLVED) != Phase.ELEMENTS_RESOLVED) {
                throw new QueryException("Cannot resolve file: " +  //NOI18N
                        Optional.ofNullable(control.getFileObject())
                        .map((fo) -> FileUtil.getFileDisplayName(fo))
                        .orElse("<unkown>"));   //NOI18N
            }
        } catch (IOException ioe) {
            throw new QueryException(ioe);
        }
        final Collection<? extends Element> topLevels = control.getTopLevelElements();
        final List<String> result = new ArrayList<String>(topLevels.size());
        for (Element topLevel : topLevels) {
            result.add(((TypeElement)topLevel).getQualifiedName().toString());
        }
        return result;
    }

    @Override
    @CheckForNull
    public String getSuperClass(@NonNull final String cls) throws QueryException {
        final TypeElement te = findClass(cls);
        if (te == null) {
            return null;
        }
        final TypeMirror superType = te.getSuperclass();
        if (superType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        return ((TypeElement)((DeclaredType)superType).asElement()).getQualifiedName().toString();
    }

    @NonNull
    @Override
    public final Collection<? extends String> getInterfaces(@NonNull final String cls) throws QueryException {
        final TypeElement te = findClass(cls);
        if (te == null) {
            return null;
        }
        final List<? extends TypeMirror> interfaceTypes = te.getInterfaces();
        final List<String> result = new ArrayList<String>(interfaceTypes.size());
        for (TypeMirror tm : interfaceTypes) {
            if (tm.getKind() == TypeKind.DECLARED) {
                result.add(
                    ((TypeElement)((DeclaredType)tm).asElement()).getQualifiedName().toString());
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    @CheckForNull
    public String getClassBinaryName(@NonNull final String cls) throws QueryException {
        final TypeElement te = findClass(cls);
        return te == null ? null : ElementUtilities.getBinaryName(te);
    }

    @Override
    @NonNull
    public Collection<? extends String> getFieldNames(
            @NonNull final String clz,
            final boolean rt,
            @NullAllowed final String type) throws QueryException {
        final TypeElement te = findClass(clz);
        if (te == null) {
            return Collections.<String>emptyList();
        }
        final Types types = control.getTypes();
        TypeMirror tm = null;
        if (type != null) {
            final List<? extends TypeElement> topLevels = control.getTopLevelElements();
            tm = topLevels.isEmpty() ?
                null :
                control.getTreeUtilities().parseType(type, topLevels.get(0));
            if (tm == null) {
                return Collections.<String>emptyList();
            } else if (rt) {
                tm = types.erasure(tm);
            }
        }
        final Collection<String> result = new ArrayList<String>();
        for (VariableElement ve : ElementFilter.fieldsIn(te.getEnclosedElements())) {
            if (isSameType(types,tm,ve.asType(),rt)) {
                result.add(ve.getSimpleName().toString());
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    @NonNull
    public Collection<? extends String> getMethodNames(
            final @NonNull String clz,
            final boolean rt,
            final @NullAllowed String returnType,
            final @NullAllowed String... parameterTypes) throws QueryException {
        final List<? extends ExecutableElement> methods = getMethods(clz, null, rt, returnType, parameterTypes);
        final List<String> result = new ArrayList<String>(methods.size());
        for (ExecutableElement method : methods) {
            result.add(method.getSimpleName().toString());
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    @CheckForNull
    public int[] getMethodSpan(
            @NonNull final String clz,
            @NonNull final String methodName,
            final boolean rt,
            @NonNull final String returnType,
            @NonNull final String... parameterTypes) throws QueryException {
        final List<? extends ExecutableElement> methods = getMethods(clz, methodName, rt, returnType, parameterTypes);
        if (methods.isEmpty()) {
            return null;
        }
        //Todo: if size > 1 => 2 methods with same signature (invalid source) use the first one
        final ExecutableElement method = methods.get(0);
        final Trees trees = control.getTrees();
        final TreePath tp = trees.getPath(method);
        if (tp == null) {
            return null;
        }
        int start = (int) trees.getSourcePositions().getStartPosition(tp.getCompilationUnit(),tp.getLeaf());
        int end = (int) trees.getSourcePositions().getEndPosition(tp.getCompilationUnit(),tp.getLeaf());
        List<Comment> cmts = control.getTreeUtilities().getComments(tp.getLeaf(), true);
        for (Comment c : cmts) {
            final int cp = c.pos();
            if (cp >= 0) {
                start = Math.min(start,cp);
            }
        }
        cmts = control.getTreeUtilities().getComments(tp.getLeaf(), false);
        for (Comment c : cmts) {
            final int cp  = c.endPos();
            end = Math.max(end,cp);
        }
        return new int[] {start, end};
    }

    @Override
    public void modifyInterfaces(
        @NonNull final String clz,
        @NonNull final Collection<? extends String> toAdd,
        @NonNull final Collection<? extends String> toRemove) throws QueryException {
        if (!(control instanceof WorkingCopy)) {
            throw new IllegalStateException();
        }
        final WorkingCopy wcopy = (WorkingCopy) control;
        final TreePath mainClassTreePath = findClassInCompilationUnit(clz);
        if (mainClassTreePath == null) {
            throw new IllegalArgumentException("No class: " + clz + " in source: " +    //NOI18N
                    FileUtil.getFileDisplayName(control.getFileObject()));
        }
        final Element mainClassElm = wcopy.getTrees().getElement(mainClassTreePath);
        assert mainClassElm != null;
        ClassTree mainClassTree = (ClassTree) mainClassTreePath.getLeaf();
        final ClassTree origMainTree = mainClassTree;
        if (mainClassElm != null) {
            Set<String> actualInterfaces = new HashSet<String>();
            TreeMaker maker = wcopy.getTreeMaker();
            // first take the current interfaces and exclude the removed ones
            List<? extends TypeMirror> interfaces = ((TypeElement) mainClassElm).getInterfaces();
            for (int infIndex = interfaces.size() - 1; infIndex >= 0; infIndex--) {
                TypeMirror infMirror = interfaces.get(infIndex);
                TypeElement infElm = (TypeElement) wcopy.getTypes().asElement(infMirror);
                actualInterfaces.add(infElm.getQualifiedName().toString());
                if (toRemove.contains(infElm.getQualifiedName().toString())) {
                    mainClassTree = maker.removeClassImplementsClause(mainClassTree, infIndex);
                }
            }
            for (String name : toAdd) {
                if (!actualInterfaces.contains(name)) {
                    TypeElement inf2add = wcopy.getElements().getTypeElement(name);
                    ExpressionTree infTree2add = inf2add != null
                            ? maker.QualIdent(inf2add)
                            : maker.Identifier(name);
                    mainClassTree = maker.addClassImplementsClause(mainClassTree, infTree2add);
                }
            }
            if (origMainTree != mainClassTree) {
                wcopy.rewrite(origMainTree, mainClassTree);
            }
        }
    }

    @Override
    public void setSuperClass(
            @NonNull final String clz,
            @NonNull final String superClz) throws QueryException {
        if (!(control instanceof WorkingCopy)) {
            throw new IllegalStateException();
        }
        final WorkingCopy wcopy = (WorkingCopy) control;
        final TreePath mainClassTreePath = findClassInCompilationUnit(clz);
        if (mainClassTreePath == null) {
            throw new IllegalArgumentException("No class: " + clz + " in source: " +    //NOI18N
                    FileUtil.getFileDisplayName(control.getFileObject()));
        }
        final Element mainClassElm = wcopy.getTrees().getElement(mainClassTreePath);
        assert mainClassElm != null;
        ClassTree mainClassTree = (ClassTree) mainClassTreePath.getLeaf();
        final ClassTree origMainTree = mainClassTree;
        if (mainClassElm != null) {
            final TreeMaker maker = wcopy.getTreeMaker();
            ExpressionTree superClsTree = null;
            if (!Object.class.getName().equals(superClz)){
                final TypeElement inf2add = wcopy.getElements().getTypeElement(superClz);
                superClsTree = inf2add != null
                    ? maker.QualIdent(inf2add)
                    : maker.Identifier(superClz);
            }
            mainClassTree = maker.setExtends(mainClassTree,superClsTree);
            if (origMainTree != mainClassTree) {
                wcopy.rewrite(origMainTree, mainClassTree);
            }
        }
    }

    @Override
    public void renameField(
        @NonNull final String clz,
        @NonNull final String oldName,
        @NonNull final String newName)  throws QueryException {
        final WorkingCopy wcopy = (WorkingCopy) control;
        try {
            if (control.toPhase(Phase.RESOLVED) != Phase.RESOLVED) {
                throw new QueryException("Cannot resolve file: " +  //NOI18N
                        Optional.ofNullable(control.getFileObject())
                        .map((fo) -> FileUtil.getFileDisplayName(fo))
                        .orElse("<unkown>"));   //NOI18N
            }
        } catch (IOException ioe) {
            throw new QueryException(ioe);
        }
        final TypeElement te = findClass(clz);
        if (te == null) {
            throw new IllegalArgumentException("No class: " + clz + " in source: " +    //NOI18N
                FileUtil.getFileDisplayName(control.getFileObject()));
        }
        VariableElement field = null;
        for (VariableElement ve : ElementFilter.fieldsIn(te.getEnclosedElements())) {
            if (oldName.contentEquals(ve.getSimpleName())) {
                field = ve;
                break;
            }
        }
        if (field == null) {
            throw new IllegalArgumentException("No field: " + clz +"."+oldName + " in source: " +    //NOI18N
                FileUtil.getFileDisplayName(control.getFileObject()));
        }
        final Trees trees = wcopy.getTrees();
        final TreeMaker maker = wcopy.getTreeMaker();
        final CompilationUnitTree cu = wcopy.getCompilationUnit();
        final TreePath fieldPath = trees.getPath(field);
        if (fieldPath == null || !cu.equals(fieldPath.getCompilationUnit())) {
            throw new IllegalArgumentException("No field: " + clz +"."+oldName + " in source: " +    //NOI18N
                FileUtil.getFileDisplayName(control.getFileObject()));
        }
        final VariableTree oldVarTree = (VariableTree) fieldPath.getLeaf();
        final VariableTree newVarTree = wcopy.getTreeMaker().Variable(
                oldVarTree.getModifiers(),
                newName,
                oldVarTree.getType(),
                oldVarTree.getInitializer());
        wcopy.rewrite(oldVarTree, newVarTree);
        final VariableElement fieldF = field;
        final ErrorAwareTreePathScanner<Void,Void> scanner = new ErrorAwareTreePathScanner<Void, Void>(){
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                super.visitIdentifier(node, p);
                if (shouldRename()) {
                    wcopy.rewrite(
                        getCurrentPath().getLeaf(),
                        maker.Identifier(newName));
                }
                return null;
            }
            @Override
            public Void visitMemberSelect(MemberSelectTree node, Void p) {
                super.visitMemberSelect(node, p);
                if (shouldRename()) {
                    wcopy.rewrite(
                        getCurrentPath().getLeaf(),
                        maker.MemberSelect(node.getExpression(), newName));
                }
                return null;
            }

            private boolean shouldRename() {
                final Element e = trees.getElement(getCurrentPath());
                return fieldF.equals(e);
            }
        };
        scanner.scan(cu, null);
    }

    @Override
    public void fixImports(
            final int[][] ranges) throws QueryException {
        if (!(control instanceof WorkingCopy)) {
            throw new IllegalStateException();
        }
        Arrays.sort(ranges, new Comparator<int[]>(){
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });
        final WorkingCopy wcopy = (WorkingCopy) control;
        try {
            if (control.toPhase(Phase.RESOLVED) != Phase.RESOLVED) {
                throw new QueryException("Cannot resolve file: " +  //NOI18N
                        Optional.ofNullable(control.getFileObject())
                        .map((fo) -> FileUtil.getFileDisplayName(fo))
                        .orElse("<unkown>"));   //NOI18N
            }
        } catch (IOException ioe) {
            throw new QueryException(ioe);
        }
        final CompilationUnitTree cu = wcopy.getCompilationUnit();
        final Trees trees = wcopy.getTrees();
        final GeneratorUtilities utils = GeneratorUtilities.get(wcopy);
        final List<Tree> toImport = new ArrayList<Tree>();
        final ErrorAwareTreePathScanner<Void,Void> scanner = new ErrorAwareTreePathScanner<Void, Void>(){
            @Override
            public Void scan(Tree node, Void p) {
                final int start = (int) trees.getSourcePositions().getStartPosition(cu, node);
                final int end = (int) trees.getSourcePositions().getEndPosition(cu, node);
                final int status = contains(ranges,start,end);
                switch (status) {
                        case -1:
                            super.scan(node, p);
                            break;
                        case 0:
                            break;
                        case 1:
                            toImport.add(node);
                            break;
                }
                return null;
            }
        };
        scanner.scan(cu, null);
        for (Tree tree : toImport) {
            wcopy.rewrite(tree, utils.importFQNs(tree));
        }
    }

    private List<? extends ExecutableElement> getMethods(
            final @NonNull String clz,
            final @NullAllowed String methodName,
            final boolean rt,
            final @NullAllowed String returnType,
            final @NullAllowed String... parameterTypes) throws QueryException {
        final TypeElement te = findClass(clz);
        if (te == null) {
            return Collections.<ExecutableElement>emptyList();
        }
        final TreeUtilities treeUtils = control.getTreeUtilities();
        final Types types = control.getTypes();
        final List<? extends TypeElement> topLevels = control.getTopLevelElements();
        if (topLevels.isEmpty()) {
            return Collections.<ExecutableElement>emptyList();
        }
        TypeMirror rType = null;
        List<TypeMirror> pTypes = null;
        if (returnType != null) {
            rType = treeUtils.parseType(
                returnType,
                topLevels.get(0));
            if (rType == null) {
                return Collections.<ExecutableElement>emptyList();
            } else if (rt) {
                rType = types.erasure(rType);
            }
        }
        if (parameterTypes != null) {
            pTypes = new ArrayList<TypeMirror>(parameterTypes.length + 1);
            for (final String parameterType : parameterTypes) {
                TypeMirror tm = treeUtils.parseType(
                    parameterType,
                    topLevels.get(0));
                if (tm == null) {
                    return Collections.<ExecutableElement>emptyList();
                } else if (rt) {
                    tm = types.erasure(tm);
                }
                pTypes.add(tm);
            }
        }
        final List<ExecutableElement> result = new ArrayList<ExecutableElement>();
nextM:  for (ExecutableElement me : ElementFilter.methodsIn(te.getEnclosedElements())) {
            if (methodName != null && !methodName.contentEquals(me.getSimpleName())) {
                continue nextM;
            }
            if (pTypes != null) {
                final List<? extends VariableElement> params = me.getParameters();
                if (params.size() != pTypes.size()) {
                    continue nextM;
                }
                final Iterator<? extends VariableElement> paramsIt = params.iterator();
                final Iterator<? extends TypeMirror> pTypesIt = pTypes.iterator();
                for (;paramsIt.hasNext();) {
                    if (!isSameType(types, pTypesIt.next(), paramsIt.next().asType(), rt)) {
                        continue nextM;
                    }
                }
            }
            if (!isSameType(types, rType, me.getReturnType(), rt)) {
                continue nextM;
            }
            result.add(me);
        }
        return result;
    }

    private TypeElement findClass(
            @NonNull final String clz) throws QueryException {
        try {
            if (control.toPhase(Phase.ELEMENTS_RESOLVED) != Phase.ELEMENTS_RESOLVED) {
                throw new QueryException("Cannot resolve file: " +  //NOI18N
                        Optional.ofNullable(control.getFileObject())
                        .map((fo) -> FileUtil.getFileDisplayName(fo))
                        .orElse("<unkown>"));   //NOI18N
            }
        } catch (IOException ioe) {
            throw new QueryException(ioe);
        }
        return control.getElements().getTypeElement(clz);
    }

    private TreePath findClassInCompilationUnit(
            @NonNull final String clz) throws QueryException {
        try {
            if (control.toPhase(Phase.ELEMENTS_RESOLVED) != Phase.ELEMENTS_RESOLVED) {
                throw new QueryException("Cannot resolve file: " +  //NOI18N
                        Optional.ofNullable(control.getFileObject())
                        .map((fo) -> FileUtil.getFileDisplayName(fo))
                        .orElse("<unkown>"));   //NOI18N
            }
        } catch (IOException ioe) {
            throw new QueryException(ioe);
        }
        final Trees trees = control.getTrees();
        final ErrorAwareTreePathScanner<TreePath,Void> visitor = new ErrorAwareTreePathScanner<TreePath, Void>() {
            @Override
            public TreePath visitClass(ClassTree node, Void p) {
                final Element el = trees.getElement(getCurrentPath());
                if (el != null &&
                   (el.getKind().isClass() || el.getKind().isInterface()) &&
                   clz.contentEquals(((TypeElement)el).getQualifiedName())) {
                    return getCurrentPath();
                } else {
                    return super.visitClass(node, p);
                }
            }

            @Override
            public TreePath reduce(TreePath r1, TreePath r2) {
                return r1 != null ? r1 : r2;
            }

            @Override
            public TreePath visitMethod(MethodTree node, Void p) {
                return null;
            }
        };
        return visitor.scan(control.getCompilationUnit(), null);
    }

    private static boolean isSameType (
            @NonNull Types types,
            @NullAllowed final TypeMirror t1,
            @NonNull final TypeMirror t2,
            final boolean rawType) {
        return t1 == null ||
            types.isSameType(
                t1,
                rawType ? types.erasure(t2) : t2);
    }

    private static int contains(
            @NonNull final int[][] ranges,
            final int start,
            final int end) {
        for (int[] range : ranges) {
            if (start >= range[0] && end <= range[1]) {
                return 1;
            }
            if (start < range[0] && end > range[1]) {
                return -1;
            }
        }
        return 0;
    }
}
