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
package org.netbeans.modules.cnd.refactoring.support;

import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring;
import org.netbeans.modules.cnd.refactoring.codegen.ConstructorGenerator.Inited;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.Utilities;
import org.netbeans.modules.cnd.refactoring.ui.InsertPoint;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 */
public class GeneratorUtils {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(GeneratorUtils.class.getName());
    private static final String ERROR = "<error>"; //NOI18N

    private GeneratorUtils() {
    }
    public enum Kind {
        GETTERS_ONLY,
        SETTERS_ONLY,
        GETTERS_SETTERS,
    }

    public static String getGetterSetterDisplayName(Kind type) {
        if (type == Kind.GETTERS_ONLY) {
            return org.openide.util.NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_getter"); //NOI18N
        }
        if (type == Kind.SETTERS_ONLY) {
            return org.openide.util.NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_setter"); //NOI18N
        }
        return org.openide.util.NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_getter_and_setter"); //NOI18N
    }

    public static Collection<CsmMember> getAllMembers(CsmClass typeElement) {
        // for now returns only current class elements, but in fact needs full hierarchy
        return typeElement.getMembers();
    }

    public static Collection<CsmFunction> getAllOutOfClassMethodDefinitions(CsmClass clazz) {
        // get all method declarations
        Iterator<CsmMember> methods = CsmSelect.getClassMembers(clazz, CsmSelect.FUNCTION_KIND_FILTER);
        List<CsmFunction> result = new ArrayList<>();
        // find definitions of that declarations
        while (methods.hasNext()) {
            CsmMethod method = (CsmMethod) methods.next();
            CsmFunction definition = ((CsmFunction) method).getDefinition();
            if (definition != null && definition != method) {
                result.add(definition);
            }
        }
        return result;
    }

    public static boolean isConstant(CsmVariable var) {
        return var.getType() != null && var.getType().isConst();
    }
//    public static ClassTree insertClassMember(WorkingCopy copy, TreePath path, Tree member) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TreeUtilities tu = copy.getTreeUtilities();
//        int idx = 0;
//        for (Tree tree : ((ClassTree)path.getLeaf()).getMembers()) {
//            if (!tu.isSynthetic(new TreePath(path, tree)) && ClassMemberComparator.compare(member, tree) < 0)
//                break;
//            idx++;
//        }
//        return copy.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), idx, member);
//    }
//
//    public static List<? extends ExecutableElement> findUndefs(CompilationInfo info, TypeElement impl) {
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
//            ERR.log(ErrorManager.INFORMATIONAL, "findUndefs(" + info + ", " + impl + ")");
//        List<? extends ExecutableElement> undef = info.getElementUtilities().findUnimplementedMethods(impl);
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
//            ERR.log(ErrorManager.INFORMATIONAL, "undef=" + undef);
//        return undef;
//    }
//
//    public static List<? extends ExecutableElement> findOverridable(CompilationInfo info, TypeElement impl) {
//        List<ExecutableElement> overridable = new ArrayList<ExecutableElement>();
//        List<TypeElement> classes = getAllClasses(impl);
//
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
//            ERR.log(ErrorManager.INFORMATIONAL, "classes=" + classes);
//
//        for (TypeElement te : classes.subList(1, classes.size())) {
//            for (ExecutableElement ee : ElementFilter.methodsIn(te.getEnclosedElements())) {
//                Set<Modifier> set = EnumSet.copyOf(NOT_OVERRIDABLE);
//
//                set.removeAll(ee.getModifiers());
//
//                if (set.size() != NOT_OVERRIDABLE.size())
//                    continue;
//
//                if(ee.getModifiers().contains(Modifier.PRIVATE)) //do not offer overriding of private methods
//                    continue;
//
//                if(overridesPackagePrivateOutsidePackage(ee, impl)) //do not offer package private methods in case they're from different package
//                    continue;
//
//                int thisElement = classes.indexOf(te);
//
//                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                    ERR.log(ErrorManager.INFORMATIONAL, "ee=" + ee);
//                    ERR.log(ErrorManager.INFORMATIONAL, "thisElement = " + thisElement);
//                    ERR.log(ErrorManager.INFORMATIONAL, "classes.subList(0, thisElement + 1)=" + classes.subList(0, thisElement + 1));
//                    ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(info, ee, classes.subList(0, thisElement + 1))=" + isOverriden(info, ee, classes.subList(0, thisElement + 1)));
//                }
//
//                if (!isOverriden(info, ee, classes.subList(0, thisElement + 1))) {
//                    overridable.add(ee);
//                }
//            }
//        }
//
//        return overridable;
//    }
//
//    public static Map<? extends TypeElement, ? extends List<? extends VariableElement>> findAllAccessibleFields(CompilationInfo info, TypeElement clazz) {
//        Map<TypeElement, List<? extends VariableElement>> result = new HashMap<TypeElement, List<? extends VariableElement>>();
//
//        result.put(clazz, findAllAccessibleFields(info, clazz, clazz));
//
//        for (TypeElement te : getAllParents(clazz)) {
//            result.put(te, findAllAccessibleFields(info, clazz, te));
//        }
//
//        return result;
//    }
//
    public static void scanForFieldsAndConstructors(final CsmClass clsPath, List<Pair<CsmField, Inited>> fields, final List<CsmConstructor> constructors) {
        for (CsmMember member : clsPath.getMembers()) {
            if (CsmKindUtilities.isField(member)) {
                CsmField field = (CsmField) member;
                if (field.isStatic()) {
                    continue;
                }
                CsmType type = field.getType();
                if (type.getArrayDepth() > 0) {
                    fields.add(Pair.of(field, Inited.cannot));
                    continue;
                }
                if (type.isConst() || type.isReference()) {
                    fields.add(Pair.of(field, Inited.must));
                    continue;
                }
                fields.add(Pair.of(field, Inited.may));
            } else if (CsmKindUtilities.isConstructor(member)) {
                constructors.add((CsmConstructor)member);
            }
        }
    }
//
//    public static void generateAllAbstractMethodImplementations(WorkingCopy wc, TreePath path) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            ClassTree clazz = (ClassTree)path.getLeaf();
//            List<Tree> members = new ArrayList<Tree>();
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            ElementUtilities elemUtils = wc.getElementUtilities();
//            for(ExecutableElement element : elemUtils.findUnimplementedMethods(te))
//                members.add(gu.createAbstractMethodImplementation(te, element));
//            ClassTree nue = gu.insertClassMembers(clazz, members);
//            wc.rewrite(clazz, nue);
//        }
//    }
//
//    public static void generateAbstractMethodImplementations(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            ClassTree clazz = (ClassTree)path.getLeaf();
//            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            members.addAll(index, gu.createAbstractMethodImplementations(te, elements));
//            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
//            wc.rewrite(clazz, nue);
//        }
//    }
//
//    public static void generateAbstractMethodImplementation(WorkingCopy wc, TreePath path, ExecutableElement element, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            ClassTree decl = wc.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), index, gu.createAbstractMethodImplementation(te, element));
//            wc.rewrite(path.getLeaf(), decl);
//        }
//    }
//
//    public static void generateMethodOverrides(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            ClassTree clazz = (ClassTree)path.getLeaf();
//            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            members.addAll(index, gu.createOverridingMethods(te, elements));
//            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
//            wc.rewrite(clazz, nue);
//        }
//    }
//
//    public static void generateMethodOverride(WorkingCopy wc, TreePath path, ExecutableElement element, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            ClassTree decl = wc.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), index, gu.createOverridingMethod(te, element));
//            wc.rewrite(path.getLeaf(), decl);
//        }
//    }

    public static void generateConstructor(CsmContext path, CsmClass enclosingClass, List<CsmConstructor> inheritedConstructors, List<CsmField> fields) {
        boolean inlineConstructor = true;
        if (inlineConstructor) {
            InsertInfo[] ins = getInsertPositons(path, enclosingClass, InsertPoint.DEFAULT);
            final InsertInfo def = ins[0];
            final StringBuilder result = new StringBuilder();
            final StringBuilder init = new StringBuilder();
            final StringBuilder superInit = new StringBuilder();
            final Document doc = path.getDocument();
            result.append('\n'); // NOI18N
            result.append(enclosingClass.getName());
            result.append('('); // NOI18N
            boolean first = true;
            for(CsmConstructor constructor : inheritedConstructors) {
                CsmFunctionParameterList parameterList = constructor.getParameterList();
                final StringBuilder args = new StringBuilder();
                for(CsmParameter parameter : parameterList.getParameters()) {
                    if (!first) {
                        result.append(", "); // NOI18N
                    }
                    result.append(parameter.getType().getCanonicalText());
                    result.append(' ');
                    result.append(parameter.getName());
                    if (args.length()>0) {
                        args.append(", "); // NOI18N
                    }
                    args.append(parameter.getName());
                    first = false;
                }
                if (superInit.length()>0) {
                    superInit.append(", "); // NOI18N
                }
                superInit.append(constructor.getContainingClass().getName());
                if (CsmKindUtilities.isTemplate(constructor.getContainingClass())) {
                    final CsmTemplate template = (CsmTemplate)constructor.getContainingClass();
                    List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                    if (templateParameters.size() > 0) {
                        superInit.append("<");//NOI18N
                        boolean afirst = true;
                        for(CsmTemplateParameter param : templateParameters) {
                            if (!afirst) {
                                superInit.append(", "); //NOI18N
                            }
                            afirst = false;
                            superInit.append(param.getName());
                        }
                        superInit.append(">");//NOI18N
                    }
                }
                superInit.append('('); // NOI18N
                superInit.append(args);
                superInit.append(')'); // NOI18N
            }
            for(CsmField field : fields) {
                if (!first) {
                    result.append(", "); // NOI18N
                }
                result.append(field.getType().getCanonicalText());
                result.append(' ');
                result.append(field.getName());
                if (init.length() > 0) {
                    init.append(", "); // NOI18N
                }
                init.append(field.getName());
                init.append('('); // NOI18N
                init.append(field.getName());
                init.append(')'); // NOI18N
                first = false;
            }
            result.append(')'); // NOI18N
            if (init.length()>0 || superInit.length()>0) {
                result.append(':');  // NOI18N
                result.append('\n');  // NOI18N
                result.append(superInit);
                if (superInit.length()>0 &&init.length()>0) {
                    result.append(", "); // NOI18N
                }
                result.append(init);
            }
            result.append("{}\n"); // NOI18N
            Runnable update = new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.insertString(def.dot, result.toString(), null);
                        Reformat format = Reformat.get(doc);
                        format.lock();
                        try {
                            int start = def.start.getOffset();
                            int end = def.end.getOffset();
                            format.reformat(start, end);
                        } finally {
                            format.unlock();
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).runAtomicAsUser(update);
            } else {
                update.run();
            }
        }
    }

    public static void generateCopyConstructor(CsmContext path, CsmClass enclosingClass, List<CsmConstructor> inheritedConstructors, List<CsmField> fields) {
        boolean inlineConstructor = true;
        if (inlineConstructor) {
            InsertInfo[] ins = getInsertPositons(path, enclosingClass, InsertPoint.DEFAULT);
            final InsertInfo def = ins[0];
            final StringBuilder result = new StringBuilder();
            final Document doc = path.getDocument();
            result.append('\n'); // NOI18N
            result.append(enclosingClass.getName());
            result.append("(const "); // NOI18N
            result.append(enclosingClass.getName());
            if (CsmKindUtilities.isTemplate(enclosingClass)) {
                final CsmTemplate template = (CsmTemplate)enclosingClass;
                List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                if (templateParameters.size() > 0) {
                    result.append("<");//NOI18N
                    boolean afirst = true;
                    for(CsmTemplateParameter param : templateParameters) {
                        if (!afirst) {
                            result.append(", "); //NOI18N
                        }
                        afirst = false;
                        result.append(param.getName());
                    }
                    result.append(">");//NOI18N
                }
            }
            result.append("& other) :"); // NOI18N
            result.append('\n'); // NOI18N
            boolean first = true;
            for(CsmConstructor constructor : inheritedConstructors) {
                if (!first) {
                    result.append(", "); // NOI18N
                }
                result.append(constructor.getContainingClass().getName());
                result.append("(other)"); // NOI18N
                first = false;
            }
            for(CsmField field : fields) {
                if (!first) {
                    result.append(", "); // NOI18N
                }
                result.append(field.getName());
                result.append("(other."); // NOI18N
                result.append(field.getName());
                result.append(')'); // NOI18N
                first = false;
            }
            result.append("{}\n"); // NOI18N
            Runnable update = new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.insertString(def.dot, result.toString(), null);
                        Reformat format = Reformat.get(doc);
                        format.lock();
                        try {
                            int start = def.start.getOffset();
                            int end = def.end.getOffset();
                            format.reformat(start, end);
                        } finally {
                            format.unlock();
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).runAtomicAsUser(update);
            } else {
                update.run();
            }
        }
    }

    
    
    public static void generateOperators(CsmContext path, CsmClass enclosingClass, List<CsmFunction> operators) {
        boolean inlineConstructor = true;
        if (inlineConstructor) {
            InsertInfo[] ins = getInsertPositons(path, enclosingClass, InsertPoint.DEFAULT);
            final InsertInfo def = ins[0];
            final StringBuilder result = new StringBuilder();
            final Document doc = path.getDocument();
            for(CsmFunction m : operators) {
                result.append('\n'); // NOI18N
                result.append(m.getText());
            }
            result.append('\n'); // NOI18N
            Runnable update = new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.insertString(def.dot, result.toString(), null);
                        Reformat format = Reformat.get(doc);
                        format.lock();
                        try {
                            int start = def.start.getOffset();
                            int end = def.end.getOffset();
                            format.reformat(start, end);
                        } finally {
                            format.unlock();
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).runAtomicAsUser(update);
            } else {
                update.run();
            }
        }
    }
    
    public static final class InsertInfo {
        public final CloneableEditorSupport ces;
        public final PositionRef start;
        public final PositionRef end;
        public final int dot;

        public InsertInfo(CloneableEditorSupport ces, int dot, PositionRef start, PositionRef end) {
            this.ces = ces;
            this.start = start;
            this.end = end;
            this.dot = dot;
        }

    }

    /**
     * 
     * @param clazz
     * @return { declaration file, definition file } definition file could be the same as declaration
     */
    public static CsmFile[] getDeclarationDefinitionFiles(CsmClass clazz) {
        CsmFile declFile =  CsmRefactoringUtils.getCsmFile(clazz);
        CsmFile defFile = declFile;
        if (declFile.isHeaderFile()) {
            Iterator<CsmFunction> extDefs = getAllOutOfClassMethodDefinitions(clazz).iterator();
            boolean found = false;
            while (extDefs.hasNext() && !found) {
                CsmFunction def = extDefs.next();
                defFile = CsmRefactoringUtils.getCsmFile(def);
                if (!declFile.equals(defFile)) {
                    found = true;
                }
            }
            if (!found) {
                CsmFile source = findSource(declFile);
                if (source != null) {
                    defFile = source;
                }
            }
        }
        return new CsmFile[] { declFile, defFile };
    }

    private static CsmFile findSource(CsmFile header) {
        String name = getFileName(header.getAbsolutePath());

        Collection<CsmFile> includers = CsmIncludeHierarchyResolver.getDefault().getFiles(header);

        for (CsmFile f : includers) {
            if (CndFileUtils.areFilenamesEqual(getFileName(f.getAbsolutePath()), name)) {
                // we found source file with the same name
                // as header and with dependency to it. Best shot.
                return f;
            }
        }

        // look for random namesake
        for (CsmFile f : header.getProject().getSourceFiles()) {
            if (CndFileUtils.areFilenamesEqual(getFileName(f.getAbsolutePath().toString()), name)) {
                return f;
            }
        }
        return null;
    }

    private static String getFileName(CharSequence file) {
        String name = new File(file.toString()).getName();
        return name;
    }
    /**
     * returns two elements array. The first contains information about declarations,
     * the second about definitions
     * @param path
     * @return [decl insert point; definition insert point]
     */
    public static InsertInfo[] getInsertPositons(CsmContext path, CsmClass enclClass, InsertPoint insPt) {
        int declPos = -1;
        int defPos = -1;
        CsmFile declFile = null;
        CsmFile defFile = null;
        InsertInfo[] out = new InsertInfo[2];
        if (insPt == InsertPoint.DEFAULT) {
            // calculate using editor context when possible
            CsmOffsetable decl = path == null ? null : path.getObjectUnderOffset();
            if (decl == null && path != null) {
                for (CsmObject csmObject : path.getPath()) {
                    if (CsmKindUtilities.isClass(csmObject)) {
                        enclClass = (CsmClass) csmObject;
                        declPos = path.getCaretOffset();
                        if (declPos <= enclClass.getLeftBracketOffset()) {
                            declPos = enclClass.getLeftBracketOffset() + 1;
                        }
                    } else {
                        if (CsmKindUtilities.isOffsetableDeclaration(csmObject)) {
                            decl = (CsmOffsetable)csmObject;
                        }
                    }
                }
            }
            if (decl != null && declPos == -1) {
                declPos = decl.getEndOffset();
            }
            if (CsmKindUtilities.isClassMember(decl)) {
                enclClass = ((CsmMember)decl).getContainingClass();
                if (CsmKindUtilities.isField(decl)) {
                    // we are on field
                    declPos = -1;
                }
            } else if (enclClass == null) {
                enclClass = path.getEnclosingClass();
            }
        } else {
            if (enclClass == null) {
                enclClass = insPt.getContainerClass();
            }
            if (insPt.getElementDeclaration() != null) {
                declPos = insPt.getElementDeclaration().getEndOffset();
            } else if (insPt.getIndex() == Integer.MIN_VALUE) {
                declPos = enclClass.getLeftBracketOffset() + 1;
            }
        }
        if (declPos < 0) {
            // let's try to find default place for insert point
            CsmMethod lastPublicMethod = null;
            CsmMethod firstPublicMethod = null;
            CsmMethod lastPublicConstructor = null;
            CsmMethod firstPublicConstructor = null;
            for (CsmMember member : enclClass.getMembers()) {
                if ((member.getVisibility() == CsmVisibility.PUBLIC) && CsmKindUtilities.isMethod(member)) {
                    CsmMethod method = (CsmMethod) member;
                    lastPublicMethod = method;
                    if (firstPublicMethod == null) {
                        firstPublicMethod = method;
                    }
                    if (CsmKindUtilities.isConstructor(method)) {
                        lastPublicConstructor = method;
                        if (firstPublicConstructor == null) {
                            firstPublicConstructor = method;
                        }
                    }
                }
            }
            // let's try to put after last public method
            if (lastPublicMethod != null) {
                declPos = lastPublicMethod.getEndOffset();
                if (declFile == null) {
                    declFile = CsmRefactoringUtils.getCsmFile(lastPublicMethod);
                }
                CsmFunctionDefinition def = lastPublicMethod.getDefinition();
                if (def != null && def != lastPublicMethod) {
                    defFile = CsmRefactoringUtils.getCsmFile(def);
                    defPos = def.getEndOffset();
                }
            } else {
                declPos = enclClass.getLeftBracketOffset() + 1;
            }
        }
        if (declFile == null) {
            declFile = CsmRefactoringUtils.getCsmFile(enclClass);
        }
        if (defFile == null) {
            Iterator<CsmFunction> extDefs;
            if (insPt.getElementDefinition() != null) {
                extDefs = Collections.singleton((CsmFunction)insPt.getElementDefinition()).iterator();
            } else {
                extDefs = getAllOutOfClassMethodDefinitions(enclClass).iterator();
            }
            while (extDefs.hasNext()) {
                CsmFunction def = extDefs.next();
                defFile = CsmRefactoringUtils.getCsmFile(def);
                defPos = def.getEndOffset();
                if (insPt.getIndex() != Integer.MAX_VALUE) {
                    break;
                }
            }

        }
        CloneableEditorSupport classDeclEditor = CsmUtilities.findCloneableEditorSupport(declFile);
        CloneableEditorSupport classDefEditor = CsmUtilities.findCloneableEditorSupport(defFile);
        PositionRef startDeclPos = classDeclEditor.createPositionRef(declPos, Bias.Backward);
        PositionRef endDeclPos = classDeclEditor.createPositionRef(declPos, Bias.Forward);
        out[0] = new InsertInfo(classDeclEditor, declPos, startDeclPos, endDeclPos);
        if (classDefEditor != null && defPos >= 0) {
            PositionRef startDefPos = classDefEditor.createPositionRef(defPos, Bias.Backward);
            PositionRef endDefPos = classDefEditor.createPositionRef(defPos, Bias.Forward);
            out[1] = new InsertInfo(classDefEditor, defPos, startDefPos, endDefPos);
        }
        return out;
    }

    public static Boolean checkStartWithUpperCase(CsmMethod method) {
        String name = ""; // NOI18N
        if (!CsmKindUtilities.isConstructor(method) && !CsmKindUtilities.isOperator(method) && !CsmKindUtilities.isDestructor(method)) {
            name = method.getName().toString();
        }
        return name.length() == 0 ? null : Boolean.valueOf(Character.isUpperCase(name.charAt(0)));
    }

    public static void generateGettersAndSetters(CsmContext path, Collection<? extends CsmField> fields, boolean inlineMethods, GeneratorUtils.Kind type, boolean isUpperCase) {
        CsmClass enclosingClass = Utilities.extractEnclosingClass(path);
        if (enclosingClass == null) {
            System.err.println("why enclosing class is null? " + path); // NOI18N
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (fields.isEmpty()) {
            System.err.println("nothing to encapsulate"); // NOI18N
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (inlineMethods) {
            InsertInfo[] ins = getInsertPositons(path, enclosingClass, InsertPoint.DEFAULT);
            final InsertInfo def = ins[0];
            final StringBuilder result = new StringBuilder();
            final Document doc = path.getDocument();
            CodeStyle codeStyle = CodeStyle.getDefault(doc);
            DeclarationGenerator.Kind declKind = DeclarationGenerator.Kind.INLINE_DEFINITION;
            if (codeStyle.getUseInlineKeyword()) {
                declKind = DeclarationGenerator.Kind.INLINE_DEFINITION_MAKRED_INLINE;
            }
            for (CsmField field : fields) {
                if (type != GeneratorUtils.Kind.SETTERS_ONLY) {
                    result.append("\n"); // NOI18N
                    result.append(DeclarationGenerator.createGetter(field, computeGetterName(field, isUpperCase), declKind));
                }
                if (type != GeneratorUtils.Kind.GETTERS_ONLY) {
                    result.append("\n"); // NOI18N
                    result.append(DeclarationGenerator.createSetter(field, computeSetterName(field, isUpperCase), declKind));
                }
            }
            result.append("\n"); // NOI18N
            Runnable update = new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.insertString(def.dot, result.toString(), null);
                        Reformat format = Reformat.get(doc);
                        format.lock();
                        try {
                            int start = def.start.getOffset();
                            int end = def.end.getOffset();
                            format.reformat(start, end);
                        } finally {
                            format.unlock();
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).runAtomicAsUser(update);
            } else {
                update.run();
            }
        } else {
            RefactoringSession session = RefactoringSession.create(getGetterSetterDisplayName(type));
            EncapsulateFieldsRefactoring refactoring = new EncapsulateFieldsRefactoring(null, path);

            Collection<EncapsulateFieldsRefactoring.EncapsulateFieldInfo> refFields = new ArrayList<>();
            for (CsmField field : fields) {
                String gName = (type != Kind.SETTERS_ONLY) ? computeGetterName(field, isUpperCase) : null;
                String sName = (type != Kind.GETTERS_ONLY) ? computeSetterName(field, isUpperCase) : null;
                refFields.add(new EncapsulateFieldsRefactoring.EncapsulateFieldInfo(field, gName, sName, null, null));
            }
            refactoring.setRefactorFields(refFields);
            refactoring.setFieldModifiers(Collections.<CsmVisibility>emptySet());
            refactoring.getContext().add(InsertPoint.DEFAULT);
            refactoring.setMethodInline(false);
            Problem problem = refactoring.preCheck();
            if (problem != null && problem.isFatal()) {
                // fatal problem
                System.err.println("preCheck failed: not possible to refactor " + problem); // NOI18N
                return;
            }
            problem = refactoring.prepare(session);
            if (problem != null && problem.isFatal()) {
                // fatal problem
                System.err.println("prepare failed: not possible to refactor " + problem); // NOI18N
                return;
            }
            session.doRefactoring(false);
        }
    }

    public static boolean hasGetter(CsmField field, Map<String, List<CsmMethod>> methods, boolean isUpperCase) {
        String getter = computeGetterName(field, isUpperCase);
        List<CsmMethod> candidates = methods.get(getter);
        if (candidates != null) {
            CsmType type = field.getType();
            for (CsmMethod candidate : candidates) {
                Collection<CsmParameter> parameters = candidate.getParameters();
                if (parameters.isEmpty() && isSameType(candidate.getReturnType(), type)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes the class field prefix from  the identifer of a field.
     * For example, if the class field prefix is "m_", the identifier "m_name"
     * is stripped to become "name". Or identifier is "pValue" is stipped to become "Value"
     * @param identifierString The identifer to strip.
     * @return The stripped identifier.
     */
    public static String stripFieldPrefix(String identifierString) {
        String stripped = identifierString;
        // remove usual C++ prefixes
        if (stripped.startsWith("m_")) { // NOI18N
            stripped = identifierString.substring(2);
        } 
        if (stripped.length() > 1) {
            if (stripped.charAt(0) == 'p' && Character.isUpperCase(stripped.charAt(1))) {
                // this is like pointer "pValue"
                stripped = stripped.substring(1);
            } else if (stripped.length() > 2 && stripped.startsWith("is") && Character.isUpperCase(stripped.charAt(2))) {// NOI18N
                // this is like isEnabled
                stripped = stripped.substring(2);
            }
        }
        return stripped;
    }

    private static StringBuilder getCapitalizedName(CsmField field) {
        StringBuilder name = new StringBuilder(stripFieldPrefix(field.getName().toString()));
        while (name.length() > 1 && name.charAt(0) == '_') //NOI18N
        {
            name.deleteCharAt(0);
        }
        if (name.length() > 0) {
            name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        }

        name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return name;
    }

    public static String computeSetterName(CsmField field, boolean isUpperCase) {
        StringBuilder name = getCapitalizedName(field);

        name.insert(0, toPrefix("set", isUpperCase)); //NOI18N
        return name.toString();
    }

    public static String computeGetterName(CsmField field, boolean isUpperCase) {
        StringBuilder name = getCapitalizedName(field);
        CsmType type = field.getType();
        name.insert(0, toPrefix(getTypeKind(type) == TypeKind.BOOLEAN ? "is" : "get", isUpperCase)); //NOI18N
        return name.toString();
    }

    private static String toPrefix(String str, boolean isUpperCase) {
        StringBuilder pref = new StringBuilder(str);
        char first = pref.charAt(0);
        if (isUpperCase) {
            first = Character.toUpperCase(first);
        } else {
            first = Character.toLowerCase(first);
        }
        pref.setCharAt(0, first);
        return pref.toString();
    }

    public static enum TypeKind {
        VOID,
        BOOLEAN,
        UNKNOWN
    };
    
    public static TypeKind getTypeKind(CsmType type) {
        CharSequence text = type.getClassifierText();
        if (CharSequences.comparator().compare("void", text) == 0) { // NOI18N
            return TypeKind.VOID;
        } else if (CharSequences.comparator().compare("bool", text) == 0 || // NOI18N
                CharSequences.comparator().compare("boolean", text) == 0) { // NOI18N
            return TypeKind.BOOLEAN;
        }
        return TypeKind.UNKNOWN;
    }

    public static boolean isSameType(CsmType type1, CsmType type2) {
        if (type1.equals(type2)) {
            return true;
        } else if (type2 != null) {
            return CharSequences.comparator().compare(type1.getCanonicalText(), type2.getCanonicalText()) == 0;
        } else {
            return false;
        }
    }

    public static boolean hasSetter(CsmField field, Map<String, List<CsmMethod>> methods, boolean isUpperCase) {
        String setter = computeSetterName(field, isUpperCase);
        List<CsmMethod> candidates = methods.get(setter);
        if (candidates != null) {
            CsmType type = field.getType();
            for (CsmMethod candidate : candidates) {
                Collection<CsmParameter> parameters = candidate.getParameters();
                if (getTypeKind(candidate.getReturnType()) == TypeKind.VOID && parameters.size() == 1 && isSameType(parameters.iterator().next().getType(), type)) {
                    return true;
                }
            }
        }
        return false;
    }
//
//    public static int findClassMemberIndex(WorkingCopy wc, ClassTree clazz, int offset) {
//        int index = 0;
//        SourcePositions sp = wc.getTrees().getSourcePositions();
//        GuardedDocument gdoc = null;
//        try {
//            Document doc = wc.getDocument();
//            if (doc != null && doc instanceof GuardedDocument)
//                gdoc = (GuardedDocument)doc;
//        } catch (IOException ioe) {}
//        Tree lastMember = null;
//        for (Tree tree : clazz.getMembers()) {
//            if (offset <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
//                if (gdoc == null)
//                    break;
//                int pos = (int)(lastMember != null ? sp.getEndPosition(wc.getCompilationUnit(), lastMember) : sp.getStartPosition(wc.getCompilationUnit(), clazz));
//                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
//                if (pos <= sp.getStartPosition(wc.getCompilationUnit(), tree))
//                    break;
//            }
//            index++;
//            lastMember = tree;
//        }
//        return index;
//    }
//
//    private static List<? extends VariableElement> findAllAccessibleFields(CompilationInfo info, TypeElement accessibleFrom, TypeElement toScan) {
//        List<VariableElement> result = new ArrayList<VariableElement>();
//
//        for (VariableElement ve : ElementFilter.fieldsIn(toScan.getEnclosedElements())) {
//            //check if ve is accessible from accessibleFrom:
//            if (ve.getModifiers().contains(Modifier.PUBLIC)) {
//                result.add(ve);
//                continue;
//            }
//            if (ve.getModifiers().contains(Modifier.PRIVATE)) {
//                if (accessibleFrom == toScan)
//                    result.add(ve);
//                continue;
//            }
//            if (ve.getModifiers().contains(Modifier.PROTECTED)) {
//                if (getAllParents(accessibleFrom).contains(toScan))
//                    result.add(ve);
//                continue;
//            }
//            //TODO:package private:
//        }
//
//        return result;
//    }
//
//    public static Collection<TypeElement> getAllParents(TypeElement of) {
//        Set<TypeElement> result = new HashSet<TypeElement>();
//
//        for (TypeMirror t : of.getInterfaces()) {
//            TypeElement te = (TypeElement) ((DeclaredType)t).asElement();
//
//            if (te != null) {
//                result.add(te);
//                result.addAll(getAllParents(te));
//            } else {
//                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                    ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + t);
//                }
//            }
//        }
//
//        TypeMirror sup = of.getSuperclass();
//        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
//
//        if (te != null) {
//            result.add(te);
//            result.addAll(getAllParents(te));
//        } else {
//            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
//            }
//        }
//
//        return result;
//    }
//
//    public static boolean supportsOverride(FileObject file) {
//        return SUPPORTS_OVERRIDE_SOURCE_LEVELS.contains(SourceLevelQuery.getSourceLevel(file));
//    }
//
//    private static final Set<String> SUPPORTS_OVERRIDE_SOURCE_LEVELS;
//
//    static {
//        SUPPORTS_OVERRIDE_SOURCE_LEVELS = new HashSet();
//
//        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.5");
//        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.6");
//    }
//
//    private static List<TypeElement> getAllClasses(TypeElement of) {
//        List<TypeElement> result = new ArrayList<TypeElement>();
//        TypeMirror sup = of.getSuperclass();
//        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
//
//        result.add(of);
//
//        if (te != null) {
//            result.addAll(getAllClasses(te));
//        } else {
//            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
//            }
//        }
//
//        return result;
//    }
//
//    private static boolean isOverriden(CompilationInfo info, ExecutableElement methodBase, List<TypeElement> classes) {
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//            ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(" + info + ", " + methodBase + ", " + classes + ")");
//        }
//
//        for (TypeElement impl : classes) {
//            for (ExecutableElement methodImpl : ElementFilter.methodsIn(impl.getEnclosedElements())) {
//                if (   ERR.isLoggable(ErrorManager.INFORMATIONAL)
//                && info.getElements().overrides(methodImpl, methodBase, impl)) {
//                    ERR.log(ErrorManager.INFORMATIONAL, "overrides:");
//                    ERR.log(ErrorManager.INFORMATIONAL, "impl=" + impl);
//                    ERR.log(ErrorManager.INFORMATIONAL, "methodImpl=" + methodImpl);
//                }
//
//                if (info.getElements().overrides(methodImpl, methodBase, impl))
//                    return true;
//            }
//        }
//
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//            ERR.log(ErrorManager.INFORMATIONAL, "no overriding methods overrides:");
//        }
//
//        return false;
//    }
//
//    private static final Set<Modifier> NOT_OVERRIDABLE = /*EnumSet.noneOf(Modifier.class);/*/EnumSet.of(Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL);
//
//    public static boolean isAccessible(TypeElement from, Element what) {
//        if (what.getModifiers().contains(Modifier.PUBLIC))
//            return true;
//
//        TypeElement fromTopLevel = SourceUtils.getOutermostEnclosingTypeElement(from);
//        TypeElement whatTopLevel = SourceUtils.getOutermostEnclosingTypeElement(what);
//
//        if (fromTopLevel.equals(whatTopLevel))
//            return true;
//
//        if (what.getModifiers().contains(Modifier.PRIVATE))
//            return false;
//
//        if (what.getModifiers().contains(Modifier.PROTECTED)) {
//            if (getAllClasses(fromTopLevel).contains(SourceUtils.getEnclosingTypeElement(what)))
//                return true;
//        }
//
//        //package private:
//        return ((PackageElement) fromTopLevel.getEnclosingElement()).getQualifiedName().toString().contentEquals(((PackageElement) whatTopLevel.getEnclosingElement()).getQualifiedName());
//    }
//

    public static DialogDescriptor createDialogDescriptor(JComponent content, String label) {
        JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_button"));
        buttons[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GeneratorUtils.class, "A11Y_Generate"));
        buttons[1] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_cancel_button"));
        return new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);

    }
//
//    /**
//     * Detects if this element overrides package private element from superclass
//     * outside package
//     * @param ee elememt to test
//     * @return true if it does
//     */
//    private static boolean overridesPackagePrivateOutsidePackage(ExecutableElement ee, TypeElement impl) {
//        String elemPackageName = ee.getEnclosingElement().getEnclosingElement().getSimpleName().toString();
//        String currentPackageName = impl.getEnclosingElement().getSimpleName().toString();
//        if(!ee.getModifiers().contains(Modifier.PRIVATE) && !ee.getModifiers().contains(Modifier.PUBLIC) && !ee.getModifiers().contains(Modifier.PROTECTED) && !currentPackageName.equals(elemPackageName))
//            return true;
//        else
//            return false;
//    }
//

//
//    private static class ClassMemberComparator {
//
//        public static int compare(Tree tree1, Tree tree2) {
//            if (tree1 == tree2)
//                return 0;
//            int importanceDiff = getSortPriority(tree1) - getSortPriority(tree2);
//            if (importanceDiff != 0)
//                return importanceDiff;
//            int alphabeticalDiff = getSortText(tree1).compareTo(getSortText(tree2));
//            if (alphabeticalDiff != 0)
//                return alphabeticalDiff;
//            return -1;
//        }
//
//        private static int getSortPriority(Tree tree) {
//            int ret = 0;
//            ModifiersTree modifiers = null;
//            switch (tree.getKind()) {
//            case CLASS:
//                ret = 400;
//                modifiers = ((ClassTree)tree).getModifiers();
//                break;
//            case METHOD:
//                MethodTree mt = (MethodTree)tree;
//                if (mt.getName().contentEquals("<init>"))
//                    ret = 200;
//                else
//                    ret = 300;
//                modifiers = mt.getModifiers();
//                break;
//            case VARIABLE:
//                ret = 100;
//                modifiers = ((VariableTree)tree).getModifiers();
//                break;
//            }
//            if (modifiers != null) {
//                if (!modifiers.getFlags().contains(Modifier.STATIC))
//                    ret += 1000;
//                if (modifiers.getFlags().contains(Modifier.PUBLIC))
//                    ret += 10;
//                else if (modifiers.getFlags().contains(Modifier.PROTECTED))
//                    ret += 20;
//                else if (modifiers.getFlags().contains(Modifier.PRIVATE))
//                    ret += 40;
//                else
//                    ret += 30;
//            }
//            return ret;
//        }
//
//        private static String getSortText(Tree tree) {
//            switch (tree.getKind()) {
//            case CLASS:
//                return ((ClassTree)tree).getSimpleName().toString();
//            case METHOD:
//                MethodTree mt = (MethodTree)tree;
//                StringBuilder sortParams = new StringBuilder();
//                sortParams.append('(');
//                int cnt = 0;
//                for(Iterator<? extends VariableTree> it = mt.getParameters().iterator(); it.hasNext();) {
//                    VariableTree param = it.next();
//                    if (param.getType().getKind() == Tree.Kind.IDENTIFIER)
//                        sortParams.append(((IdentifierTree)param.getType()).getName().toString());
//                    else if (param.getType().getKind() == Tree.Kind.MEMBER_SELECT)
//                        sortParams.append(((MemberSelectTree)param.getType()).getIdentifier().toString());
//                    if (it.hasNext()) {
//                        sortParams.append(',');
//                    }
//                    cnt++;
//                }
//                sortParams.append(')');
//                return mt.getName().toString() + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
//            case VARIABLE:
//                return ((VariableTree)tree).getName().toString();
//            }
//            return ""; //NOI18N
//        }
//    }
//
//    public static void guardedCommit(JTextComponent component, ModificationResult mr) throws IOException {
//        try {
//            mr.commit();
//        } catch (IOException e) {
//            if (e.getCause() instanceof GuardedException) {
//                String message = NbBundle.getMessage(GeneratorUtils.class, "ERR_CannotApplyGuarded");
//
//                Utilities.setStatusBoldText(component, message);
//                Logger.getLogger(GeneratorUtils.class.getName()).log(Level.FINE, null, e);
//            }
//        }
//    }
}
