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
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.hints.unused.UsedDetector;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
public class UnusedDetector {

    public record UnusedDescription(Element unusedElement, TreePath unusedElementPath, boolean packagePrivate, UnusedReason reason) {}
    
    private static final Object RESULTS_KEY = new Object();
    private static final Object CLASS_INDEX_KEY = new Object();

    public enum UnusedReason {
        NOT_WRITTEN_READ("neither read or written to"),
        NOT_WRITTEN("never written to"), //makes sense?
        NOT_READ("never read"),
        NOT_USED("never used");
        private final String text;

        private UnusedReason(String text) {
            this.text = text;
        }

    }

    public static List<UnusedDescription> findUnused(CompilationInfo info, Callable<Boolean> cancel) {
        @SuppressWarnings("unchecked")
        List<UnusedDescription> cached = (List<UnusedDescription>) info.getCachedValue(RESULTS_KEY);
        if (cached != null) {
            return cached;
        }

        UnusedVisitor uv = new UnusedVisitor(info);
        uv.scan(info.getCompilationUnit(), null);
        AtomicReference<List<UsedDetector>> usedDetectors = new AtomicReference<>();
        BiFunction<Element, TreePath, Boolean> markedAsUsed = (el, path) -> {
            if (usedDetectors.get() == null) {
                usedDetectors.set(collectUsedDetectors(info));
            }
            for (UsedDetector detector : usedDetectors.get()) {
                if (detector.isUsed(el, path)) {
                    return true;
                }
            }
            return false;
        };
        List<UnusedDescription> result = new ArrayList<>();
        for (Entry<Element, TreePath> e : uv.element2Declaration.entrySet()) {
            Element el = e.getKey();
            TreePath declaration = e.getValue();
            Set<UseTypes> uses = uv.useTypes.getOrDefault(el, Collections.emptySet());
            boolean isPrivate = el.getModifiers().contains(Modifier.PRIVATE); //TODO: effectivelly private!
            boolean isPkgPrivate = !isPrivate && !el.getModifiers().contains(Modifier.PUBLIC) && !el.getModifiers().contains(Modifier.PROTECTED);
            if (isLocalVariableClosure(el)) {
                boolean isWritten = uses.contains(UseTypes.WRITTEN);
                boolean isRead = uses.contains(UseTypes.READ);
                if (!isWritten && !isRead && !markedAsUsed.apply(el, declaration)) {
                    result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_WRITTEN_READ));
                } else if (!isWritten && !markedAsUsed.apply(el, declaration)) {
                    result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_WRITTEN));
                } else if (!isRead && !markedAsUsed.apply(el, declaration)) {
                    result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_READ));
                }
            } else if (el.getKind().isField() && (isPrivate || isPkgPrivate)) {
                if (!isSerialSpecField(info, el) && !lookedUpElement(el, uv.type2LookedUpFields, uv.allStringLiterals)) {
                    boolean isWritten = uses.contains(UseTypes.WRITTEN);
                    boolean isRead = uses.contains(UseTypes.READ);
                    if (!isWritten && !isRead) {
                        if ((isPrivate || isUnusedInPkg(info, el, cancel)) && !markedAsUsed.apply(el, declaration)) {
                            result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_WRITTEN_READ));
                        }
                    } else if (!isWritten && !markedAsUsed.apply(el, declaration)) {
                        result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_WRITTEN));
                    } else if (!isRead) {
                        if ((isPrivate || isUnusedInPkg(info, el, cancel)) && !markedAsUsed.apply(el, declaration)) {
                            result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_READ));
                        }
                    }
                }
            } else if ((el.getKind() == ElementKind.CONSTRUCTOR || el.getKind() == ElementKind.METHOD) && (isPrivate || isPkgPrivate)) {
                ExecutableElement method = (ExecutableElement)el;
                if (!isSerializationMethod(info, method) && !uses.contains(UseTypes.USED)
                        && !info.getElementUtilities().overridesMethod(method) && !lookedUpElement(el, uv.type2LookedUpMethods, uv.allStringLiterals)
                        && !SourceUtils.isMainMethod(method)) {
                    if ((isPrivate || isUnusedInPkg(info, el, cancel)) && !markedAsUsed.apply(el, declaration)) {
                        result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_USED));
                    }
                }
            } else if ((el.getKind().isClass() || el.getKind().isInterface()) && (isPrivate || isPkgPrivate)) {
                if (!uses.contains(UseTypes.USED)) {
                    if ((isPrivate || isUnusedInPkg(info, el, cancel)) && !markedAsUsed.apply(el, declaration)) {
                        result.add(new UnusedDescription(el, declaration, isPkgPrivate, UnusedReason.NOT_USED));
                    }
                }
            }
        }

        info.putCachedValue(RESULTS_KEY, result, CompilationInfo.CacheClearPolicy.ON_CHANGE);

        return result;
    }

    /** Detects static final long SerialVersionUID
     * @return true if element is static final long serialVersionUID
     */
    private static boolean isSerialSpecField(CompilationInfo info, Element el) {
        if (el.getModifiers().contains(Modifier.FINAL)
                && el.getModifiers().contains(Modifier.STATIC)) {

            if (!isInSerializableOrExternalizable(info, el)) {
                return false;
            }
            if (info.getTypes().getPrimitiveType(TypeKind.LONG).equals(el.asType())
                && el.getSimpleName().toString().equals("serialVersionUID")) {
                return true;
            }
            if (el.getSimpleName().contentEquals("serialPersistentFields")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Also returns true on error / undecidable situation, so the filtering
     * will probably accept serial methods and will not mark them as unused, if
     * the class declaration is errneous.
     *
     * @param info the compilation context
     * @param e the class member (the enclosing element will be tested)
     * @return true, if in serializable/externalizable or unknown
     */
    private static boolean isInSerializableOrExternalizable(CompilationInfo info, Element e) {
        Element encl = e.getEnclosingElement();
        if (encl == null || !encl.getKind().isClass()) {
            return true;
        }
        TypeMirror m = encl.asType();
        if (m == null || m.getKind() != TypeKind.DECLARED) {
            return true;
        }
        Element serEl = info.getElements().getTypeElement("java.io.Serializable"); // NOI18N
        Element extEl = info.getElements().getTypeElement("java.io.Externalizable"); // NOI18N
        if (serEl == null || extEl == null) {
            return true;
        }
        if (info.getTypes().isSubtype(m, serEl.asType())) {
            return true;
        }
        if (info.getTypes().isSubtype(m, extEl.asType())) {
            return true;
        }
        return false;
    }

    private static Field signatureAccessField;

    /**
     * Hack to get signature out of ElementHandle - there's no API method for that
     */
    private static String _getSignatureHack(ElementHandle<ExecutableElement> eh) {
        try {
            if (signatureAccessField == null) {
                try {
                    Field f = ElementHandle.class.getDeclaredField("signatures"); // NOI18N
                    f.setAccessible(true);
                    signatureAccessField = f;
                } catch (NoSuchFieldException | SecurityException ex) {
                    // ignore
                    return ""; // NOI18N
                }
            }
            String[] signs = (String[])signatureAccessField.get(eh);
            if (signs == null || signs.length != 3) {
                return ""; // NOI18N
            } else {
                return signs[1] + signs[2];
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return ""; // NOI18N
        }
    }

    /**
     * Checks if the method is specified by Serialization API and the class
     * extends Serializable/Externalizable. Unused methods defined in API spec
     * should not be marked as unused.
     *
     * @param info compilation context
     * @param method the method
     * @return true, if the method is from serialization API and should not be reported
     */
    private static boolean isSerializationMethod(CompilationInfo info, ExecutableElement method) {
        if (!isInSerializableOrExternalizable(info, method)) {
            return false;
        }
        ElementHandle<ExecutableElement> eh = ElementHandle.create(method);
        String sign = _getSignatureHack(eh);
        return SERIALIZABLE_SIGNATURES.contains(sign);
    }

    /**
     * Signatures of Serializable methods.
     */
    private static final Set<String> SERIALIZABLE_SIGNATURES = new HashSet<>(Arrays.asList(new String[] {
        "writeObject(Ljava/io/ObjectOutputStream;)V",
        "readObject(Ljava/io/ObjectInputStream;)V",
        "readResolve()Ljava/lang/Object;",
        "writeReplace()Ljava/lang/Object;",
        "readObjectNoData()V",
    }));

    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(
            ElementKind.LOCAL_VARIABLE, ElementKind.RESOURCE_VARIABLE,
            ElementKind.EXCEPTION_PARAMETER, ElementKind.BINDING_VARIABLE);

    private static boolean isLocalVariableClosure(Element el) {
        return el.getKind() == ElementKind.PARAMETER ||
               LOCAL_VARIABLES.contains(el.getKind());
    }

    private static boolean lookedUpElement(Element element, Map<Element, Set<String>> type2LookedUp, Set<String> allStringLiterals) {
        String name = element.getKind() == ElementKind.CONSTRUCTOR ? "<init>" : element.getSimpleName().toString();
        return isLookedUp(element.getEnclosingElement(), name, type2LookedUp, allStringLiterals) ||
               isLookedUp(null, name, type2LookedUp, allStringLiterals);
    }

    private static boolean isLookedUp(Element owner, String name, Map<Element, Set<String>> type2LookedUp, Set<String> allStringLiterals) {
        Set<String> lookedUp = type2LookedUp.getOrDefault(owner, Collections.emptySet());
        return lookedUp.contains(name) || (allStringLiterals.contains(name) && lookedUp.contains(null));
    }

    private static boolean isUnusedInPkg(CompilationInfo info, Element el, Callable<Boolean> cancel) {
        TypeElement typeElement;
        Set<? extends String> packageSet = Collections.singleton(info.getElements().getPackageOf(el).getQualifiedName().toString());
        Set<ClassIndex.SearchKind> searchKinds;
        Set<ClassIndex.SearchScopeType> scope = Collections.singleton(new ClassIndex.SearchScopeType() {
            @Override
            public Set<? extends String> getPackages() {
                return packageSet;
            }

            @Override
            public boolean isSources() {
                return true;
            }

            @Override
            public boolean isDependencies() {
                return false;
            }
        });
        switch (el.getKind()) {
            case FIELD -> {
                typeElement = info.getElementUtilities().enclosingTypeElement(el);
                searchKinds = EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES);
            }
            case METHOD, CONSTRUCTOR -> {
                typeElement = info.getElementUtilities().enclosingTypeElement(el);
                searchKinds = EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES);
            }
            case ANNOTATION_TYPE, CLASS, ENUM, INTERFACE -> {
                List<? extends TypeElement> topLevelElements = info.getTopLevelElements();
                if (topLevelElements.size() == 1 && topLevelElements.get(0) == el) {
                    return false;
                }
                typeElement = (TypeElement) el;
                searchKinds = EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES);
            }
            default -> {
                return true;
            }
        }

        // check if previous runs already found a usage in package-local files
        FileObject fileObject = info.getFileObject();
        ElementHandle<Element> eh = ElementHandle.create(el);
        if (PkgScanResultCache.getCachedUsedInPkg(fileObject, eh)) {
            return false;
        }

        // scan package for usage
        ClassIndex classIndex = getCachedClassIndex(fileObject, info);
        Set<FileObject> res = classIndex.getResources(ElementHandle.create(typeElement), searchKinds, scope);

        if (res != null) {
            for (FileObject fo : res) {
                try {
                    if (Boolean.TRUE.equals(cancel.call())) {
                        return false;
                    }
                    if (fo != fileObject) {
                        JavaSource js = JavaSource.forFileObject(fo);
                        if (js == null) {
                            return false;
                        }
                        AtomicBoolean found = new AtomicBoolean();
                        js.runUserActionTask(cc -> {
                            cc.toPhase(JavaSource.Phase.RESOLVED);
                            new ErrorAwareTreePathScanner<Void, Element>() {
                                @Override
                                public Void scan(Tree tree, Element p) {
                                    if (!found.get() && tree != null) {
                                        Element element = cc.getTrees().getElement(new TreePath(getCurrentPath(), tree));
                                        if (element != null && eh.signatureEquals(element)) {
                                            found.set(true);
                                            return null;
                                        }
                                        super.scan(tree, p);
                                    }
                                    return null;
                                }
                            }.scan(new TreePath(cc.getCompilationUnit()), el);
                        }, true);
                        if (found.get()) {
                            PkgScanResultCache.markFoundInFile(fileObject, fo, ElementHandle.create(el));
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return true;
        }
        return false;
    }

    // obtaining the ClassIndex can be expensive, this is usually called multiple times per search
    private static ClassIndex getCachedClassIndex(FileObject fileObject, CompilationInfo info) {
        ClassIndex classIndex = (ClassIndex) info.getCachedValue(CLASS_INDEX_KEY);
        if (classIndex == null) {
            ClasspathInfo cpInfo;
            Project prj = FileOwnerQuery.getOwner(fileObject);
            if (prj != null) {
                SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                FileObject[] roots = new FileObject[sourceGroups.length];
                for (int i = 0; i < sourceGroups.length; i++) {
                    SourceGroup sourceGroup = sourceGroups[i];
                    roots[i] = sourceGroup.getRootFolder();
                }
                cpInfo = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPathSupport.createClassPath(roots));
            } else {
                cpInfo = info.getClasspathInfo();
            }
            classIndex = cpInfo.getClassIndex();
            info.putCachedValue(CLASS_INDEX_KEY, classIndex, CompilationInfo.CacheClearPolicy.ON_CHANGE);
        }
        return classIndex;
    }

    /*
     * Remembers if an Element is used in a package-local source file to avoid having to
     * repeat the search.
     *
     * Invalidates the whole cache if the source file of the current CompilationInfo becomes a different file.
     * Invalidates a single cached value if the file was (externally) modified in the meantime.
     * The not-used case is not stored to keep the logic simple.
     */
    private static class PkgScanResultCache {
        
        private static FileObject lastSource;
        private static Map<ElementHandle<Element>, Usage> cache = new HashMap<>();
        
        private record Usage(FileObject inFile, Instant markedTime) {}

        private static void markFoundInFile(FileObject source, FileObject pkgLocalFile, ElementHandle<Element> signature) {
            if (source == pkgLocalFile) {
                throw new IllegalArgumentException();
            }
            lastSource = source;
            cache.put(signature, new Usage(pkgLocalFile, Instant.now()));
        }

        private static boolean getCachedUsedInPkg(FileObject root, ElementHandle<Element> signature) {
            if (lastSource == null) {
                return false;
            }
            if (lastSource != root) {
                lastSource = null;
                cache = new HashMap<>();
                return false;
            }
            Usage usage = cache.get(signature);
            if (usage == null) {
                return false;
            }
            if (usage.inFile().lastModified().toInstant().isAfter(usage.markedTime())) {
                cache.remove(signature);
                return false;
            }
            return true;
        }
    }
    
    private static List<UsedDetector> collectUsedDetectors(CompilationInfo info) {
        List<UsedDetector> detectors = new ArrayList<>();
        for (UsedDetector.Factory factory : Lookup.getDefault().lookupAll(UsedDetector.Factory.class)) {
            UsedDetector detector = factory.create(info);
            if (detector != null) {
                detectors.add(detector);
            }
        }
        return detectors;
    }

    private enum UseTypes {
        READ, WRITTEN, USED;
    }

    private static final class UnusedVisitor extends ErrorAwareTreePathScanner<Void, Void> {

        private final Map<Element, Set<UseTypes>> useTypes = new HashMap<>();
        private final Map<Element, TreePath> element2Declaration = new HashMap<>();
        private final Map<Element, Set<String>> type2LookedUpMethods = new HashMap<>();
        private final Map<Element, Set<String>> type2LookedUpFields = new HashMap<>();
        private final Set<String> allStringLiterals = new HashSet<>();
        private final TypeElement methodHandlesLookup;
        private final CompilationInfo info;
        private ExecutableElement recursionDetector;

        public UnusedVisitor(CompilationInfo info) {
            this.info = info;
            this.methodHandlesLookup = info.getElements().getTypeElement(MethodHandles.Lookup.class.getCanonicalName());
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            handleUse();
            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void p) {
            handleUse();
            return super.visitMemberSelect(node, p);
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Void p) {
            handleUse();
            return super.visitMemberReference(node, p);
        }

        @Override
        public Void visitNewClass(NewClassTree node, Void p) {
            handleUse();
            return super.visitNewClass(node, p);
        }

        private void handleUse() {
            Element el = info.getTrees().getElement(getCurrentPath());

            if (el == null) {
                return ;
            }

            boolean isPrivate = el.getModifiers().contains(Modifier.PRIVATE); //TODO: effectivelly private!
            boolean isPkgPrivate = !isPrivate && !el.getModifiers().contains(Modifier.PUBLIC) && !el.getModifiers().contains(Modifier.PROTECTED);

            if (isLocalVariableClosure(el) || (el.getKind().isField() && (isPrivate | isPkgPrivate))) {
                TreePath effectiveUse = getCurrentPath();
                boolean isWrite = false;
                boolean isRead = false;

                OUTER: while (true) {
                    TreePath parent = effectiveUse.getParentPath();

                    switch (parent.getLeaf().getKind()) {
                        case ASSIGNMENT:
                            AssignmentTree at = (AssignmentTree) parent.getLeaf();
                            if (at.getVariable() == effectiveUse.getLeaf()) {
                                isWrite = true;
                            } else if (at.getExpression() == effectiveUse.getLeaf()) {
                                isRead = true;
                            }
                            break OUTER;
                        case AND_ASSIGNMENT: case DIVIDE_ASSIGNMENT:
                        case LEFT_SHIFT_ASSIGNMENT: case MINUS_ASSIGNMENT:
                        case MULTIPLY_ASSIGNMENT: case OR_ASSIGNMENT:
                        case PLUS_ASSIGNMENT: case REMAINDER_ASSIGNMENT:
                        case RIGHT_SHIFT_ASSIGNMENT: case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                        case XOR_ASSIGNMENT:
                            CompoundAssignmentTree cat = (CompoundAssignmentTree) parent.getLeaf();
                            if (cat.getVariable() == effectiveUse.getLeaf()) {
                                //check if the results of compound assignment is used:
                                effectiveUse = parent;
                                break;
                            }
                            //use on the right hand side of the compound assignment - consider as read
                            isRead = true;
                            break OUTER;
                        case EXPRESSION_STATEMENT:
                            break OUTER;
                        default:
                            isRead = true;
                            break OUTER;
                    }
                }

                if (isWrite) {
                    addUse(el, UseTypes.WRITTEN);
                }

                if (isRead) {
                    addUse(el, UseTypes.READ);
                }
            } else if (isPrivate | isPkgPrivate) {
                if (el.getKind() != ElementKind.METHOD || recursionDetector != el)
                addUse(el, UseTypes.USED);
            }
        }

        private void addUse(Element el, UseTypes type) {
            useTypes.computeIfAbsent(el, x -> EnumSet.noneOf(UseTypes.class)).add(type);
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            ExecutableElement prevRecursionDetector = recursionDetector;

            try {
                recursionDetector = null;

                handleDeclaration(getCurrentPath());
                return super.visitClass(node, p);
            } finally {
                recursionDetector = prevRecursionDetector;
            }
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            handleDeclaration(getCurrentPath());
            return super.visitVariable(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {
            ExecutableElement prevRecursionDetector = recursionDetector;

            try {
                Element el = info.getTrees().getElement(getCurrentPath());
                recursionDetector = (el != null && el.getKind() == ElementKind.METHOD) ? (ExecutableElement) el : null;
                handleDeclaration(getCurrentPath());
                return super.visitMethod(node, p);
            } finally {
                recursionDetector = prevRecursionDetector;
            }
        }

        private void handleDeclaration(TreePath path) {
            Element el = info.getTrees().getElement(path);

            if (el == null) {
                return ;
            }

            element2Declaration.put(el, path);

            if (el.getKind() == ElementKind.PARAMETER) {
                addUse(el, UseTypes.WRITTEN);
                boolean read = true;
                Tree parent = path.getParentPath().getLeaf();
                if (parent.getKind() == Kind.METHOD) {
                    MethodTree method = (MethodTree) parent;
                    Set<Modifier> mods = method.getModifiers().getFlags();
                    if (method.getParameters().contains(path.getLeaf()) &&
                        mods.contains(Modifier.PRIVATE) && !mods.contains(Modifier.ABSTRACT) &&
                        !mods.contains(Modifier.NATIVE)) {
                        read = false;
                    }
                }
                if (read) {
                    addUse(el, UseTypes.READ);
                }
            } else if (el.getKind() == ElementKind.EXCEPTION_PARAMETER) {
                //Ignore unread caught exceptions. There are valid reasons why it
                //could be unused; and there should be a separate hint checking if
                //it makes sense to no use it:
                addUse(el, UseTypes.READ);
                addUse(el, UseTypes.WRITTEN);
            } else if (el.getKind() == ElementKind.BINDING_VARIABLE) {
                addUse(el, UseTypes.WRITTEN);
            } else if (el.getKind() == ElementKind.LOCAL_VARIABLE) {
                Tree parent = path.getParentPath().getLeaf();
                if (parent.getKind() == Kind.ENHANCED_FOR_LOOP &&
                    ((EnhancedForLoopTree) parent).getVariable() == path.getLeaf()) {
                    addUse(el, UseTypes.WRITTEN);
                }
            } else if (Utilities.toRecordComponent(el).getKind() == ElementKind.RECORD_COMPONENT) {
                addUse(el, UseTypes.READ);
                addUse(el, UseTypes.WRITTEN);
            } else if (el.getKind().isField()) {
                addUse(el, UseTypes.WRITTEN);
            } else if (el.getKind() == ElementKind.CONSTRUCTOR &&
                       el.getModifiers().contains(Modifier.PRIVATE) &&
                       ((ExecutableElement) el).getParameters().isEmpty()) {
                //check if this constructor prevent initalization of "utility" class,
                //in which case, it is not "unused":
                TypeElement encl = (TypeElement) el.getEnclosingElement();
                TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
                boolean utility = !encl.getModifiers().contains(Modifier.ABSTRACT) &&
                                  encl.getInterfaces().isEmpty() &&
                                  (jlObject == null || info.getTypes().isSameType(encl.getSuperclass(), jlObject.asType()));
                for (Element sibling : el.getEnclosingElement().getEnclosedElements()) {
                    if (sibling.getKind() == ElementKind.CONSTRUCTOR && !sibling.equals(el)) {
                        utility = false;
                        break;
                    } else if ((sibling.getKind().isField() || sibling.getKind() == ElementKind.METHOD) &&
                                !sibling.getModifiers().contains(Modifier.STATIC)) {
                        utility = false;
                        break;
                    }
                }
                if (utility) {
                    addUse(el, UseTypes.USED);
                }
            }

            if (path.getLeaf().getKind() == Kind.VARIABLE) {
                VariableTree vt = (VariableTree) path.getLeaf();
                if (vt.getInitializer() != null) {
                    addUse(el, UseTypes.WRITTEN);
                }
            }
        }

        @Override
        public Void visitLiteral(LiteralTree node, Void p) {
            if (node.getKind() == Kind.STRING_LITERAL) {
                allStringLiterals.add((String)node.getValue());
            }
            return super.visitLiteral(node, p);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
            Element invoked = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getMethodSelect()));
            if (invoked != null && invoked.getEnclosingElement() == methodHandlesLookup && !node.getArguments().isEmpty()) {
                ExpressionTree clazz = node.getArguments().get(0);
                Element lookupType = null;
                if (clazz.getKind() == Kind.MEMBER_SELECT) {
                    MemberSelectTree mst = (MemberSelectTree) clazz;
                    if (mst.getIdentifier().contentEquals("class")) {
                        lookupType = info.getTrees().getElement(new TreePath(new TreePath(getCurrentPath(), clazz), mst.getExpression()));
                    }
                }
                String lookupName = null;
                if (node.getArguments().size() > 1) {
                    ExpressionTree name  = node.getArguments().get(1);
                    if (name.getKind() == Kind.STRING_LITERAL) {
                        lookupName = (String) ((LiteralTree) name).getValue();
                    }
                }
                switch (invoked.getSimpleName().toString()) {
                    case "findStatic", "findVirtual", "findSpecial" ->
                        type2LookedUpMethods.computeIfAbsent(lookupType, t -> new HashSet<>()).add(lookupName);
                    case "findConstructor" ->
                        type2LookedUpMethods.computeIfAbsent(lookupType, t -> new HashSet<>()).add("<init>");
                    case "findGetter", "findSetter", "findStaticGetter", "findStaticSetter", "findStaticVarHandle", "findVarHandle" ->
                        type2LookedUpFields.computeIfAbsent(lookupType, t -> new HashSet<>()).add(lookupName);
                }
            }
            return super.visitMethodInvocation(node, p);
        }

    }
}
