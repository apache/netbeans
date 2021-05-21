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
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.lang.reflect.Field;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

/**
 *
 * @author lahvac
 */
public class UnusedDetector {

    public static class UnusedDescription {
        public final Element unusedElement;
        public final TreePath unusedElementPath;
        public final UnusedReason reason;

        public UnusedDescription(Element unusedElement, TreePath unusedElementPath, UnusedReason reason) {
            this.unusedElement = unusedElement;
            this.unusedElementPath = unusedElementPath;
            this.reason = reason;
        }

    }

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

    public static List<UnusedDescription> findUnused(CompilationInfo info) {
        List<UnusedDescription> cached = (List<UnusedDescription>) info.getCachedValue(UnusedDetector.class);
        if (cached != null) {
            return cached;
        }

        UnusedVisitor uv = new UnusedVisitor(info);
        uv.scan(info.getCompilationUnit(), null);
        List<UnusedDescription> result = new ArrayList<>();
        for (Entry<Element, TreePath> e : uv.element2Declaration.entrySet()) {
            Element el = e.getKey();
            TreePath declaration = e.getValue();
            Set<UseTypes> uses = uv.useTypes.getOrDefault(el, Collections.emptySet());
            boolean isPrivate = el.getModifiers().contains(Modifier.PRIVATE); //TODO: effectivelly private!
            if (isLocalVariableClosure(el) || (el.getKind().isField() && isPrivate)) {
                if (!isSerialSpecField(info, el)) {
                    boolean isWritten = uses.contains(UseTypes.WRITTEN);
                    boolean isRead = uses.contains(UseTypes.READ);
                    if (!isWritten && !isRead) {
                        result.add(new UnusedDescription(el, declaration, UnusedReason.NOT_WRITTEN_READ));
                    } else if (!isWritten) {
                        result.add(new UnusedDescription(el, declaration, UnusedReason.NOT_WRITTEN));
                    } else if (!isRead) {
                        result.add(new UnusedDescription(el, declaration, UnusedReason.NOT_READ));
                    }
                }
            } else if ((el.getKind() == ElementKind.CONSTRUCTOR || el.getKind() == ElementKind.METHOD) && isPrivate) {
                if (!isSerializationMethod(info, (ExecutableElement)el) && !uses.contains(UseTypes.USED)) {
                    result.add(new UnusedDescription(el, declaration, UnusedReason.NOT_USED));
                }
            } else if ((el.getKind().isClass() || el.getKind().isInterface()) && isPrivate) {
                if (!uses.contains(UseTypes.USED)) {
                    result.add(new UnusedDescription(el, declaration, UnusedReason.NOT_USED));
                }
            }
        }

        info.putCachedValue(UnusedDetector.class, result, CompilationInfo.CacheClearPolicy.ON_CHANGE);

        return result;
    }

    /** Detects static final long SerialVersionUID
     * @return true if element is final static long serialVersionUID
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
            ElementKind.EXCEPTION_PARAMETER);
    private static final ElementKind BINDING_VARIABLE;

    static {
        ElementKind bindingVariable;
        try {
            LOCAL_VARIABLES.add(bindingVariable = ElementKind.valueOf(TreeShims.BINDING_VARIABLE));
        } catch (IllegalArgumentException ex) {
            bindingVariable = null;
        }
        BINDING_VARIABLE = bindingVariable;
    }

    private static boolean isLocalVariableClosure(Element el) {
        return el.getKind() == ElementKind.PARAMETER ||
               LOCAL_VARIABLES.contains(el.getKind());
    }

    private enum UseTypes {
        READ, WRITTEN, USED;
    }

    private static final class UnusedVisitor extends ErrorAwareTreePathScanner<Void, Void> {

        private final Map<Element, Set<UseTypes>> useTypes = new HashMap<>();
        private final Map<Element, TreePath> element2Declaration = new HashMap<>();
        private final CompilationInfo info;
        private ExecutableElement recursionDetector;

        public UnusedVisitor(CompilationInfo info) {
            this.info = info;
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

            if (isLocalVariableClosure(el) || (el.getKind().isField() && isPrivate)) {
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
            } else if (isPrivate) {
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

        @Override
        public Void scan(Tree tree, Void p) {
            if (tree != null && TreeShims.BINDING_PATTERN.equals(tree.getKind().name())) {
                handleDeclaration(new TreePath(getCurrentPath(), tree));
            }
            return super.scan(tree, p);
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
            } else if (el.getKind() == BINDING_VARIABLE) {
                addUse(el, UseTypes.WRITTEN);
            } else if (el.getKind() == ElementKind.LOCAL_VARIABLE) {
                Tree parent = path.getParentPath().getLeaf();
                if (parent.getKind() == Kind.ENHANCED_FOR_LOOP &&
                    ((EnhancedForLoopTree) parent).getVariable() == path.getLeaf()) {
                    addUse(el, UseTypes.WRITTEN);
                }
            } else if (TreeShims.isRecordComponent(Utilities.toRecordComponent(el).getKind())) {
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
    }
}
