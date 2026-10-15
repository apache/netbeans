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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Utility class for java plugins.
 */
public final class JavaPluginUtils {
    private static final Logger LOG = Logger.getLogger(JavaPluginUtils.class.getName());

    public static Problem isSourceElement(Element el, CompilationInfo info) {
        if (el == null) {
            // XXX: shouldn't we create a problem ? How ?
            return null;
        }
        Problem preCheckProblem;
        Element typeElement;
        if(el.getKind() != ElementKind.PACKAGE) {
            typeElement = info.getElementUtilities().enclosingTypeElement(el);
            if(typeElement == null) {
                typeElement = el;
            }
        } else {
            typeElement = el;
        }
        ElementHandle<Element> handle = null;
        try {
            handle = ElementHandle.create(typeElement);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.WARNING, "Cannot create handle for source element", ex);
        }
        if (handle == null || JavaRefactoringUtils.isFromLibrary(handle, info.getClasspathInfo())) { //NOI18N
            preCheckProblem = new Problem(true, NbBundle.getMessage(
                    JavaPluginUtils.class, "ERR_CannotRefactorLibraryClass",
                    el.getKind()==ElementKind.PACKAGE?el:el.getEnclosingElement()
                    ));
            return preCheckProblem;
        }
        FileObject file = SourceUtils.getFile(handle, info.getClasspathInfo());
        // RefactoringUtils.isFromLibrary already checked file for null
        if (!RefactoringUtils.isFileInOpenProject(file)) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(
                    JavaPluginUtils.class,
                    "ERR_ProjectNotOpened",
                    FileUtil.getFileDisplayName(file)));
            return preCheckProblem;
        }
        return null;
    }
    
    public static Problem isSourceFile(FileObject fo, CompilationInfo info) {
        Problem preCheckProblem;
        if (fo == null || FileUtil.getArchiveFile(fo) != null) { //NOI18N
            preCheckProblem = new Problem(true, NbBundle.getMessage(
                    JavaPluginUtils.class, "ERR_CannotRefactorLibraryClass",
                    FileUtil.getFileDisplayName(fo)
                    ));
            return preCheckProblem;
        }
        // RefactoringUtils.isFromLibrary already checked file for null
        if (!RefactoringUtils.isFileInOpenProject(fo)) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(
                    JavaPluginUtils.class,
                    "ERR_ProjectNotOpened",
                    FileUtil.getFileDisplayName(fo)));
            return preCheckProblem;
        }
        return null;
    }
    
    public static TreePath findMethod(TreePath path) {
        while (path != null) {
            if (path.getLeaf().getKind() == Kind.METHOD) {
                return path;
            }

            if (path.getLeaf().getKind() == Kind.BLOCK
                    && path.getParentPath() != null
                    && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                //initializer:
                return path;
            }

            path = path.getParentPath();
        }

        return null;
    }
    
    public static TreePath findStatement(TreePath statementPath) {
        while (statementPath != null
                && (!StatementTree.class.isAssignableFrom(statementPath.getLeaf().getKind().asInterface())
                || (statementPath.getParentPath() != null
                && statementPath.getParentPath().getLeaf().getKind() != Kind.BLOCK))) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(statementPath.getLeaf().getKind())) {
                return null;
            }

            statementPath = statementPath.getParentPath();
        }

        return statementPath;
    }
    
    public static boolean isParentOf(TreePath parent, TreePath path) {
        Tree parentLeaf = parent.getLeaf();

        while (path != null && path.getLeaf() != parentLeaf) {
            path = path.getParentPath();
        }

        return path != null;
    }

    public static boolean isParentOf(TreePath parent, List<? extends TreePath> candidates) {
        for (TreePath tp : candidates) {
            if (!isParentOf(parent, tp)) {
                return false;
            }
        }

        return true;
    }
    
    public static Problem chainProblems(Problem result, Problem problem) {
        if (result == null) {
            return problem;
        }
        if (problem == null) {
            return result;
        }
        Problem value;
        if(problem.isFatal()) {
            problem.setNext(result);
            value = problem;
        } else {
            Problem next = value = result;
            while (next.getNext() != null) {
                next = next.getNext();
            }
            next.setNext(problem);
        }
        return value;
    }
    
    /**
     * Convert typemirror of an anonymous class to supertype/iface
     * 
     * @return typemirror of supertype/iface, initial tm if not anonymous
     */
    public static TypeMirror convertIfAnonymous(TypeMirror tm) {
        //anonymous class?
        Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
        if (tm instanceof DeclaredType) {
            Element el = ((DeclaredType) tm).asElement();
            //XXX: the null check is needed for lambda type, not covered by test:
            if (el != null && (el.getSimpleName().length() == 0 || fm.contains(el.getEnclosingElement().getKind()))) {
                List<? extends TypeMirror> interfaces = ((TypeElement) el).getInterfaces();
                if (interfaces.isEmpty()) {
                    tm = ((TypeElement) el).getSuperclass();
                } else {
                    tm = interfaces.get(0);
                }
            }
        }
        return tm;
    }
    
    public static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        TypeMirror type = resolveCapturedTypeInt(info, tm);
        
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            if (tmirr != null) {
                return tmirr;
            }
            else { //no extends, just '?'
                return info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            }
                
        }
        
        return type;
    }
    
    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            if (orig.getKind() == TypeKind.WILDCARD) {
                TypeMirror extendsBound = ((WildcardType) orig).getExtendsBound();
                TypeMirror rct = SourceUtils.resolveCapturedType(extendsBound != null ? extendsBound : ((WildcardType) orig).getSuperBound());
                if (rct != null) {
                    return rct;
                }
            }
            return orig;
        }
        
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
            
            for (TypeMirror t : dt.getTypeArguments()) {
                typeArguments.add(resolveCapturedTypeInt(info, t));
            }
            
            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, (TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            } else {
                return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;

            return info.getTypes().getArrayType(resolveCapturedTypeInt(info, at.getComponentType()));
        }
        
        return tm;
    }

    public static JavaSource createSource(final FileObject file, final ClasspathInfo cpInfo, final TreePathHandle tph) throws IllegalArgumentException {
        JavaSource source;
        if (file != null) {
            final ClassPath mergedPlatformPath = RefactoringUtils.merge(cpInfo.getClassPath(PathKind.BOOT), ClassPath.getClassPath(file, ClassPath.BOOT));
            final ClassPath mergedModulePlatformPath = RefactoringUtils.merge(cpInfo.getClassPath(PathKind.MODULE_BOOT), ClassPath.getClassPath(file, JavaClassPathConstants.MODULE_BOOT_PATH));
            final ClassPath mergedCompilePath = RefactoringUtils.merge(cpInfo.getClassPath(PathKind.COMPILE), ClassPath.getClassPath(file, ClassPath.COMPILE));
            final ClassPath mergedModuleCompilePath = RefactoringUtils.merge(cpInfo.getClassPath(PathKind.MODULE_COMPILE), ClassPath.getClassPath(file, JavaClassPathConstants.MODULE_COMPILE_PATH));
            final ClassPath mergedModuleClassPath = RefactoringUtils.merge(cpInfo.getClassPath(PathKind.MODULE_CLASS), ClassPath.getClassPath(file, JavaClassPathConstants.MODULE_CLASS_PATH));
            final ClassPath mergedSourcePath = RefactoringUtils.merge(cpInfo.getClassPath(PathKind.SOURCE), ClassPath.getClassPath(file, ClassPath.SOURCE));
            final ClasspathInfo mergedInfo = new ClasspathInfo.Builder(mergedPlatformPath)
                    .setModuleBootPath(mergedModulePlatformPath)
                    .setClassPath(mergedCompilePath)
                    .setModuleCompilePath(mergedModuleCompilePath)
                    .setModuleClassPath(mergedModuleClassPath)
                    .setSourcePath(mergedSourcePath)
                    .build();
            source = JavaSource.create(mergedInfo, new FileObject[]{tph.getFileObject()});
        } else {
            source = JavaSource.create(cpInfo);
        }
        return source;
    }
    
    public static boolean hasGetter(CompilationInfo info, TypeElement typeElement, VariableElement field, Map<String, List<ExecutableElement>> methods, CodeStyle cs) {
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = field.asType();
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        String getterName = CodeStyleUtils.computeGetterName(name, type.getKind() == TypeKind.BOOLEAN, isStatic, cs);
        Types types = info.getTypes();
        List<ExecutableElement> candidates = methods.get(getterName);
        if (candidates != null) {
            for (ExecutableElement candidate : candidates) {
                if ((!candidate.getModifiers().contains(Modifier.ABSTRACT) || candidate.getEnclosingElement() == typeElement)
                        && candidate.getParameters().isEmpty()
                        && types.isSameType(candidate.getReturnType(), type))
                    return true;
            }
        }
        return false;
    }
    
    public static boolean hasSetter(CompilationInfo info, TypeElement typeElement, VariableElement field, Map<String, List<ExecutableElement>> methods, CodeStyle cs) {
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = field.asType();
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        String setterName = CodeStyleUtils.computeSetterName(name, isStatic, cs);
        Types types = info.getTypes();
        List<ExecutableElement> candidates = methods.get(setterName);
        if (candidates != null) {
            for (ExecutableElement candidate : candidates) {
                if ((!candidate.getModifiers().contains(Modifier.ABSTRACT) || candidate.getEnclosingElement() == typeElement)
                        && candidate.getReturnType().getKind() == TypeKind.VOID
                        && candidate.getParameters().size() == 1
                        && types.isSameType(candidate.getParameters().get(0).asType(), type))
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isSyntheticPath(CompilationInfo ci, TreePath path) {
        TreeUtilities tu = ci.getTreeUtilities();
        return tu.isSynthetic(path);
    }
    
    //<editor-fold defaultstate="collapsed" desc="TODO: Copy from org.netbeans.modules.java.hints.errors.Utilities">
    public static final String DEFAULT_NAME = "par"; // NOI18N
    public static String makeNameUnique(CompilationInfo info, Scope s, String name) {
        return makeNameUnique(info, s, name, Collections.EMPTY_LIST, null, null);
    }
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, List<String> definedIds) {
        return makeNameUnique(info, s, name, definedIds, null, null);
    }
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, String prefix, String suffix) {
        return makeNameUnique(info, s, name, Collections.EMPTY_LIST, prefix, suffix);
    }
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, List<String> definedIds, String prefix, String suffix) {
        boolean cont;
        String proposedName;
        name = CodeStyleUtils.addPrefixSuffix(name, prefix, null);
        int counter = 0;
        do {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "") + safeString(suffix);
            
            cont = false;
            
            if (s != null) {
                for (String id : definedIds) {
                    if (proposedName.equals(id)) {
                        counter++;
                        cont = true;
                        break;
                    }
                }
                for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                if (proposedName.equals(e.getSimpleName().toString())) {
                    counter++;
                    cont = true;
                    break;
                    }
                }
            }
        } while(cont);
        
        return proposedName;
    }
    
    private static String safeString(String str) {
        return str == null ? "" : str;
    }
    
    public static String getName(TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return "" + Character.toLowerCase(tm.getKind().name().charAt(0));
        }

        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                return firstToLower(dt.asElement().getSimpleName().toString());
            case ARRAY:
                return getName(((ArrayType) tm).getComponentType());
            default:
                return DEFAULT_NAME;
        }
    }
    
    public static String getName(ExpressionTree et) {
        return getName((Tree) et);
    }
    
    public static String getName(Tree et) {
        return adjustName(getNameRaw(et));
    }
    
    private static String getNameRaw(Tree et) {
        if (et == null)
            return null;

        switch (et.getKind()) {
        case IDENTIFIER:
            return ((IdentifierTree) et).getName().toString();
        case METHOD_INVOCATION:
            return getNameRaw(((MethodInvocationTree) et).getMethodSelect());
        case MEMBER_SELECT:
            return ((MemberSelectTree) et).getIdentifier().toString();
        case NEW_CLASS:
            return firstToLower(getNameRaw(((NewClassTree) et).getIdentifier()));
        case PARAMETERIZED_TYPE:
            return firstToLower(getNameRaw(((ParameterizedTypeTree) et).getType()));
        case STRING_LITERAL:
            String name = guessLiteralName((String) ((LiteralTree) et).getValue());
            if(name == null) {
                return firstToLower(String.class.getSimpleName());
            } else {
                return firstToLower(name);
            }
        case VARIABLE:
            return ((VariableTree) et).getName().toString();
        default:
            return null;
        }
    }
    
    static String adjustName(String name) {
        if (name == null) {
            return null;
        }
        
        String shortName = null;
        
        if (name.startsWith("get") && name.length() > 3) {
            shortName = name.substring(3);
        }
        
        if (name.startsWith("is") && name.length() > 2) {
            shortName = name.substring(2);
        }
        
        if (shortName != null) {
            return firstToLower(shortName);
        }
        
        if (SourceVersion.isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name;
        }
    }
    
    private static String firstToLower(String name) {
        if (name.length() == 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        boolean toLower = true;
        char last = Character.toLowerCase(name.charAt(0));

        for (int i = 1; i < name.length(); i++) {
            if (toLower && (Character.isUpperCase(name.charAt(i)) || name.charAt(i) == '_')) {
                result.append(Character.toLowerCase(last));
            } else {
                result.append(last);
                toLower = false;
            }
            last = name.charAt(i);

        }

        result.append(toLower ? Character.toLowerCase(last) : last);
        
        if (SourceVersion.isKeyword(result)) {
            return "a" + name;
        } else {
            return result.toString();
        }
    }
    
    private static String guessLiteralName(String str) {
        if(str.isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch == ' ') {
                sb.append('_');
            } else if (sb.length() == 0 ? Character.isJavaIdentifierStart(ch) : Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            }
            if (sb.length() > 40) {
                break;
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        else {
            return sb.toString();
        }
    }

    public static CompilationUnitTree createCompilationUnit(FileObject sourceRoot, String relativePath, Tree typeDecl, WorkingCopy workingCopy, TreeMaker make) {
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
        CompilationUnitTree newCompilation;
        try {
            newCompilation = genUtils.createFromTemplate(sourceRoot, relativePath, ElementKind.CLASS);
            List<? extends Tree> typeDecls = newCompilation.getTypeDecls();
            if (typeDecls.isEmpty()) {
                newCompilation = make.addCompUnitTypeDecl(newCompilation, typeDecl);
            } else {
                List<Tree> typeDeclarations = new LinkedList<Tree>(newCompilation.getTypeDecls());
                Tree templateClazz = typeDeclarations.remove(0); // TODO: Check for class with correct name, template could start with another type.
                // mark the template CU as removed; any untransfered comments will be (?) lost.
                workingCopy.getTreeMaker().asRemoved(templateClazz);
                typeDecl = genUtils.importComments(typeDecl, newCompilation);
                if (workingCopy.getTreeUtilities().getComments(typeDecl, true).isEmpty()) {
                    genUtils.copyComments(templateClazz, typeDecl, true);
                } else if (workingCopy.getTreeUtilities().getComments(typeDecl, false).isEmpty()) {
                    genUtils.copyComments(templateClazz, typeDecl, false);
                }
                typeDeclarations.add(0, typeDecl);
                newCompilation = make.CompilationUnit(newCompilation.getPackageAnnotations(), sourceRoot, relativePath, newCompilation.getImports(), typeDeclarations);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            newCompilation = make.CompilationUnit(sourceRoot, relativePath, null, Collections.singletonList(typeDecl));
        }
        return newCompilation;
    }
    
    public static final class VariablesFilter implements ElementUtilities.ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }
    //</editor-fold>
}
