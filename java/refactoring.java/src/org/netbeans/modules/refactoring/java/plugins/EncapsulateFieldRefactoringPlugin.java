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
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldPanel.Javadoc;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldPanel.SortBy;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Hurka
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldRefactoringPlugin extends JavaRefactoringPlugin {

    private static final Logger LOG = Logger.getLogger(EncapsulateFieldRefactoringPlugin.class.getName());
    private ElementHandle<TypeElement> fieldEncloserHandle;
    /**
     * most restrictive accessibility modifier on tree path
     */
    private Modifier fieldEncloserAccessibility;
    /**
     * present accessibility of field
     */
    private Set<Modifier> fieldAccessibility;
    private ElementHandle<ExecutableElement> currentGetter;
    private ElementHandle<ExecutableElement> currentSetter;
    private static Set<Modifier> accessModifiers = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
    private static List<Modifier> MODIFIERS = Arrays.asList(Modifier.PRIVATE, null, Modifier.PROTECTED, Modifier.PUBLIC);
    private final EncapsulateFieldRefactoring refactoring;

    /**
     * path in source with field declaration; refactoring.getSelectedObject()
     * may contain path to a reference
     */
    private TreePathHandle sourceType;

    /**
     * Creates a new instance of RenameRefactoring
     * @param refactoring 
     */
    public EncapsulateFieldRefactoringPlugin(EncapsulateFieldRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle handle = getSourceType();
        FileObject fo = handle.getFileObject();
        return JavaSource.forFileObject(fo);
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        try {
            javac.toPhase(JavaSource.Phase.RESOLVED);
            sourceType = this.refactoring.getSourceType();
            Problem result = isElementAvail(sourceType, javac);
            if (result != null) {
                return result;
            }

            Element field = sourceType.resolveElement(javac);
            fireProgressListenerStep();
            if (ElementKind.FIELD == field.getKind()) {
                TreePath tp = javac.getTrees().getPath(field);
                sourceType = TreePathHandle.create(tp, javac);
            } else {
                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongType"));
            }

            result = JavaPluginUtils.isSourceElement(field, javac);
            if (result != null) {
                return result;
            }

            TypeElement encloser = (TypeElement) field.getEnclosingElement();
            ElementKind classKind = encloser.getKind();
            if (classKind == ElementKind.INTERFACE || classKind == ElementKind.ANNOTATION_TYPE) {
                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateInIntf"));
            }

            fieldEncloserHandle = ElementHandle.create(encloser);
            fieldAccessibility = field.getModifiers();
            fieldEncloserAccessibility = resolveVisibility(encloser);

            return result;
        } finally {
            fireProgressListenerStop();
        }
    }

    @Override
    public Problem fastCheckParameters() {
        return fastCheckParameters(refactoring.getGetterName(), refactoring.getSetterName(), refactoring.getMethodModifiers(), refactoring.getFieldModifiers(), refactoring.isAlwaysUseAccessors());
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        Problem p = null;
        Element field = getSourceType().resolveElement(javac);
        TypeElement clazz = (TypeElement) field.getEnclosingElement();
        String getname = refactoring.getGetterName();
        String setname = refactoring.getSetterName();
        ExecutableElement getter = null;
        ExecutableElement setter = null;

        if (getname != null) {
            getter = findMethod(javac, clazz, getname, Collections.<VariableElement>emptyList(), true);
        }

        if (getter != null) {
            Types types = javac.getTypes();
            if (!types.isSameType(field.asType(), getter.getReturnType())) {
                String msg = NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateWrongGetter",
                        getname,
                        getter.getReturnType().toString());
                p = createProblem(p, false, msg);
            } else if(RefactoringUtils.isWeakerAccess(refactoring.getMethodModifiers(), getter.getModifiers())) {
                String msg = NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateAccessGetter",
                        getname,
                        getter.getEnclosingElement().getSimpleName());
                p = createProblem(p, false, msg);
            }
            if (getter.getEnclosingElement() == field.getEnclosingElement()) {
                currentGetter = ElementHandle.create(getter);
            }
        }
        p = overridingHasWeakerAccess(p, javac, clazz, getname, "ERR_EncapsulateAccessOverGetter", Collections.<VariableElement>emptyList());

        if (setname != null) {
            setter = findMethod(javac, clazz, setname, Collections.singletonList((VariableElement) field), true);
        }

        if (setter != null) {
            if (TypeKind.VOID != setter.getReturnType().getKind()) {
                p = createProblem(p, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongSetter", setname, setter.getReturnType()));
            } else if(RefactoringUtils.isWeakerAccess(refactoring.getMethodModifiers(), setter.getModifiers())) {
                String msg = NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateAccessSetter",
                        setname,
                        setter.getEnclosingElement().getSimpleName());
                p = createProblem(p, false, msg);
            }
            if (setter.getEnclosingElement() == field.getEnclosingElement()) {
                currentSetter = ElementHandle.create(setter);
            }
        }
        p = overridingHasWeakerAccess(p, javac, clazz, setname, "ERR_EncapsulateAccessOverSetter", Collections.<VariableElement>emptyList());
        return p;
    }

    private Problem fastCheckParameters(String getter, String setter,
            Set<Modifier> methodModifier, Set<Modifier> fieldModifier,
            boolean alwaysUseAccessors) {

        if ((getter != null && !Utilities.isJavaIdentifier(getter))
                || (setter != null && !Utilities.isJavaIdentifier(setter))
                || (getter == null && setter == null)) {
            // user doesn't use valid java identifier, it cannot be used
            // as getter/setter name
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethods"));
        } else {
            // we have no problem :-)
            return null;
        }
    }

    private Modifier resolveVisibility(TypeElement clazz) {
        NestingKind nestingKind = clazz.getNestingKind();

        if (nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL) {
            return Modifier.PRIVATE;
        }

        Set<Modifier> mods = clazz.getModifiers();
        if (nestingKind == NestingKind.TOP_LEVEL) {
            return mods.contains(Modifier.PUBLIC)
                    ? Modifier.PUBLIC
                    : null;
        }

        if (mods.contains(Modifier.PRIVATE)) {
            return Modifier.PRIVATE;

        }
        Modifier mod1 = resolveVisibility((TypeElement) clazz.getEnclosingElement());
        Modifier mod2 = null;
        if (mods.contains(Modifier.PUBLIC)) {
            mod2 = Modifier.PUBLIC;
        } else if (mods.contains(Modifier.PROTECTED)) {
            mod2 = Modifier.PROTECTED;
        }

        return max(mod1, mod2);
    }

    private Modifier max(Modifier a, Modifier b) {
        if (a == b) {
            return a;
        }
        int ai = MODIFIERS.indexOf(a);
        int bi = MODIFIERS.indexOf(b);
        return ai > bi ? a : b;
    }

    private static Modifier getAccessibility(Set<Modifier> mods) {
        if (mods.isEmpty()) {
            return null;
        }
        Set<Modifier> s = EnumSet.noneOf(Modifier.class);
        s.addAll(mods);
        s.retainAll(accessModifiers);
        return s.isEmpty() ? null : s.iterator().next();
    }

    private static Set<Modifier> replaceAccessibility(Modifier currentAccess, Modifier futureAccess, Element elm) {
        Set<Modifier> mods = EnumSet.noneOf(Modifier.class);
        mods.addAll(elm.getModifiers());

        if (currentAccess != null) {
            mods.remove(currentAccess);
        }
        if (futureAccess != null) {
            mods.add(futureAccess);
        }
        return mods;
    }

    public static ExecutableElement findMethod(CompilationInfo javac, TypeElement clazz, String name, List<? extends VariableElement> params, boolean includeSupertypes) {
        if (name == null || name.length() == 0) {
            return null;
        }

        TypeElement c = clazz;
        while (true) {
            for (Element elm : c.getEnclosedElements()) {
                if (ElementKind.METHOD == elm.getKind()) {
                    ExecutableElement m = (ExecutableElement) elm;
                    if (name.contentEquals(m.getSimpleName())
                            && compareParams(javac, params, m.getParameters())
                            && isAccessible(javac, clazz, m)) {
                        return m;
                    }
                }
            }

            TypeMirror superType = c.getSuperclass();
            if (!includeSupertypes || superType.getKind() == TypeKind.NONE) {
                return null;
            }
            c = (TypeElement) ((DeclaredType) superType).asElement();
        }
    }
    
    private Problem overridingHasWeakerAccess(Problem p, CompilationController javac, TypeElement clazz, String name, String msgKey, List<? extends VariableElement> params) {
        if (name == null || name.length() == 0) {
            return null;
        }

        final ClassIndex classIndex = javac.getClasspathInfo().getClassIndex();
        Set<ElementHandle<TypeElement>> elements = classIndex.getElements(ElementHandle.create(clazz), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
        final Set<Modifier> methodModifiers = refactoring.getMethodModifiers();
        
        for (ElementHandle<TypeElement> elementHandle : elements) {
            TypeElement c = elementHandle.resolve(javac);

            if(c != null) {
                for (Element elm : c.getEnclosedElements()) {
                    if (ElementKind.METHOD == elm.getKind()) {
                        ExecutableElement m = (ExecutableElement) elm;
                        if (name.contentEquals(m.getSimpleName())
                                && compareParams(javac, params, m.getParameters())
                                && RefactoringUtils.isWeakerAccess(elm.getModifiers(), methodModifiers)) {
                            String msg = NbBundle.getMessage(
                                    EncapsulateFieldRefactoringPlugin.class,
                                    msgKey,
                                    name,
                                    elm.getEnclosingElement().getSimpleName());
                            return createProblem(p, false, msg);
                        }
                    }
                }
            }
        }
        return p;
    }

    /**
     * returns true if elm is accessible from clazz. elm must be member of clazz
     * or its superclass
     */
    private static boolean isAccessible(CompilationInfo javac, TypeElement clazz, Element elm) {
        if (clazz == elm.getEnclosingElement()) {
            return true;
        }
        Set<Modifier> mods = elm.getModifiers();
        if (mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.PROTECTED)) {
            return true;
        } else if (mods.contains(Modifier.PRIVATE)) {
            return false;
        }
        Elements utils = javac.getElements();
        return utils.getPackageOf(elm) == utils.getPackageOf(clazz);
    }

    private static boolean compareParams(CompilationInfo javac, List<? extends VariableElement> params1, List<? extends VariableElement> params2) {
        Types types = javac.getTypes();
        if (params1.size() == params2.size()) {
            Iterator<? extends VariableElement> it1 = params1.iterator();
            for (VariableElement ve : params2) {
                TypeMirror veType = types.erasure(ve.asType());
                TypeMirror asType = types.erasure(it1.next().asType());
                if (veType != asType) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Problem prepare(RefactoringElementsBag bag) {

        fireProgressListenerStart(AbstractRefactoring.PREPARE, 9);
        try {
            fireProgressListenerStep();

            EncapsulateDesc desc = prepareEncapsulator(null);
            if (desc.p != null && desc.p.isFatal()) {
                return desc.p;
            }

            Encapsulator encapsulator = new Encapsulator(
                    Collections.singletonList(desc), desc.p,
                    refactoring.getContext().lookup(Integer.class),
                    refactoring.getContext().lookup(SortBy.class),
                    refactoring.getContext().lookup(Javadoc.class));

            Problem problem = createAndAddElements(
                    desc.refs,
                    new TransformTask(encapsulator, desc.fieldHandle),
                    bag, refactoring);

            return problem != null ? problem : encapsulator.getProblem();
        } finally {
            fireProgressListenerStop();
        }
    }

    EncapsulateDesc prepareEncapsulator(Problem previousProblem) {
        Set<FileObject> refs = getRelevantFiles();
        EncapsulateDesc etask = new EncapsulateDesc();

        if (refactoring.isAlwaysUseAccessors()
                && refactoring.getMethodModifiers().contains(Modifier.PRIVATE)
                // is reference fromother files?
                && refs.size() > 1) {
            // breaks code
            etask.p = createProblem(previousProblem, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsAccess"));
            return etask;
        }
        if (refactoring.isAlwaysUseAccessors()
                // is default accessibility?
                && getAccessibility(refactoring.getMethodModifiers()) == null
                // is reference fromother files?
                && refs.size() > 1) {
            // breaks code likely
            etask.p = createProblem(previousProblem, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsDefaultAccess"));
        }

        etask.fieldHandle = getSourceType();
        etask.refs = refs;
        etask.currentGetter = currentGetter;
        etask.currentSetter = currentSetter;
        etask.refactoring = refactoring;
        return etask;
    }

    private Set<FileObject> getRelevantFiles() {
        // search class index just in case Use accessors even when the field is accessible == true
        // or the field is accessible:
        // * private eclosers|private field -> CP: .java (project) => JavaSource.forFileObject
        // * default enclosers|default field -> CP: package (project)
        // * public|protected enclosers&public|protected field -> CP: project + dependencies
        Set<FileObject> refs;
        FileObject source = getSourceType().getFileObject();
        if (fieldAccessibility.contains(Modifier.PRIVATE) || fieldEncloserAccessibility == Modifier.PRIVATE) {
            // search file
            refs = Collections.singleton(source);
        } else { // visible field
            ClasspathInfo cpinfo;
            if (fieldEncloserAccessibility == Modifier.PUBLIC
                    && (fieldAccessibility.contains(Modifier.PUBLIC) || fieldAccessibility.contains(Modifier.PROTECTED))) {
                // search project and dependencies
                cpinfo = RefactoringUtils.getClasspathInfoFor(true, source);
            } else {
                // search project
                cpinfo = RefactoringUtils.getClasspathInfoFor(false, source);
            }
            ClassIndex index = cpinfo.getClassIndex();
            refs = index.getResources(fieldEncloserHandle, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            if (!refs.contains(source)) {
                refs = new LinkedHashSet<FileObject>(refs);
                refs.add(source);
            }
        }
        return refs;
    }

    private static boolean isSubclassOf(TypeElement subclass, TypeElement superclass) {
        TypeMirror superType = subclass.getSuperclass();
        while (superType.getKind() != TypeKind.NONE) {
            TypeElement superTypeElm = (TypeElement) ((DeclaredType) superType).asElement();
            if (superclass == superTypeElm) {
                return true;
            }
            superType = superTypeElm.getSuperclass();
        }
        return false;
    }

    private TreePathHandle getSourceType() {
        return sourceType != null ? sourceType : refactoring.getSourceType();
    }

    static final class Encapsulator extends RefactoringVisitor {

        private final FileObject sourceFile;
        private final Integer insertPoint;
        private final SortBy sortBy;
        private final Javadoc javadocType;
        private Problem problem;
        private List<EncapsulateDesc> descs;
        private Map<VariableElement, EncapsulateDesc> fields;
        private boolean setterUsed;

        public Encapsulator(List<EncapsulateDesc> descs, Problem problem, Integer ip, SortBy sortBy, Javadoc jd) {
            assert descs != null && descs.size() > 0;
            this.sourceFile = descs.get(0).fieldHandle.getFileObject();
            this.descs = descs;
            this.problem = problem;
            this.insertPoint = ip == null ? Integer.MIN_VALUE : ip;
            this.sortBy = sortBy == null ? SortBy.PAIRS : sortBy;
            this.javadocType = jd == null ? Javadoc.NONE : jd;
        }

        public Problem getProblem() {
            if (setterUsed && descs.get(0).refactoring.isGenerateVetoableChangeSupport()) {
                problem = createProblem(problem, 
                        false, 
                        NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulatePropertyVetoException"));
                setterUsed = false;
            }
            return problem;
        }

        @Override
        public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
            super.setWorkingCopy(workingCopy);

            // init caches
            fields = new HashMap<VariableElement, EncapsulateDesc>(descs.size());
            for (EncapsulateDesc desc : descs) {
                desc.field = (VariableElement) desc.fieldHandle.resolveElement(workingCopy);
                fields.put(desc.field, desc);
            }
        }

        @Override
        public Tree visitCompilationUnit(CompilationUnitTree node, Element field) {
            return scan(node.getTypeDecls(), field);
        }

        @Override
        public Tree visitClass(ClassTree node, Element field) {
            TypeElement clazz = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            boolean[] origValues = new boolean[descs.size()];
            int counter = 0;
            for (EncapsulateDesc desc : descs) {
                origValues[counter++] = desc.useAccessors;
                desc.useAccessors = resolveUseAccessor(clazz, desc);
            }

            if (sourceFile == workingCopy.getFileObject()) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                if (el == descs.get(0).field.getEnclosingElement()) {
                    // all fields come from the same class so testing the first field should be enough
                    ClassTree nct = node;
                    List<Tree> newMembers = new ArrayList<Tree>();
                    int getterIdx = 0;
                    VariableTree pcs = null;
                    if (descs.get(0).refactoring.isGeneratePropertyChangeSupport()) {
                        pcs = getPropertyChangeSupport(clazz, "java.beans.PropertyChangeSupport"); //NOI18N
                        if (pcs == null) {
                            pcs = createPropertyChangeSupport("java.beans.PropertyChangeSupport", "propertyChangeSupport");//NOI18N
                            newMembers.add(pcs);
                        }
                    }

                    VariableTree vcs = null;
                    if (descs.get(0).refactoring.isGenerateVetoableChangeSupport()) {
                        vcs = getPropertyChangeSupport(clazz, "java.beans.VetoableChangeSupport");//NOI18N
                        if (vcs == null) {
                            vcs = createPropertyChangeSupport("java.beans.VetoableChangeSupport", "vetoableChangeSupport");//NOI18N
                            newMembers.add(vcs);
                        }
                    }
                    CodeStyle cs = RefactoringUtils.getCodeStyle(workingCopy);
                    for (EncapsulateDesc desc : descs) {
                        VariableTree propName = createPropName(clazz, desc);
                        if (pcs!=null) {
                            newMembers.add(propName);
                        }
                        MethodTree[] ms = createGetterAndSetter(
                                desc.field,
                                desc.refactoring.getGetterName(),
                                desc.refactoring.getSetterName(),
                                desc.refactoring.getMethodModifiers(),
                                cs,
                                pcs,
                                vcs,
                                propName);
                        if (ms[0] != null) {
                            newMembers.add(getterIdx++, ms[0]);
                        }
                        if (ms[1] != null) {
                            int setterIdx = sortBy == SortBy.GETTERS_FIRST
                                    ? newMembers.size()
                                    : getterIdx++;
                            newMembers.add(setterIdx, ms[1]);
                        }
                    }

                    if (!newMembers.isEmpty()) {
                        if (sortBy == SortBy.ALPHABETICALLY) {
                            newMembers.sort(new SortMethodsByNameComparator());
                        }
                        if (insertPoint < 0) {
                            if(insertPoint > Integer.MIN_VALUE) {
                                nct = GeneratorUtilities.get(workingCopy).insertClassMembers(node, newMembers, Math.abs(insertPoint));
                            } else {
                                nct = GeneratorUtilities.get(workingCopy).insertClassMembers(node, newMembers);
                            }
                        } else {
                            List<? extends Tree> members = node.getMembers();
                            if (insertPoint >= members.size()) {
                                // last method
                                for (Tree mt : newMembers) {
                                    nct = make.addClassMember(nct, mt);
                                }
                            } else {
                                int idx = insertPoint;
                                for (Tree mt : newMembers) {
                                    nct = make.insertClassMember(nct, idx++, mt);
                                }
                            }
                        }
                        rewrite(node, nct);
                    }
                }
            }

            Tree result = scan(node.getMembers(), field);
            counter = 0;
            for (EncapsulateDesc desc : descs) {
                desc.useAccessors = origValues[counter++];
            }
            return result;
        }

        private VariableTree getPropertyChangeSupport(javax.lang.model.element.TypeElement node, String support) {
            TypeElement supportElement = workingCopy.getElements().getTypeElement(support);
            if(supportElement != null) {
                for (VariableElement el : ElementFilter.fieldsIn(node.getEnclosedElements())) {
                    if (el.asType().equals(supportElement.asType())) {
                        return (VariableTree) workingCopy.getTrees().getPath(el).getLeaf();
                    }
                }
            }
            return null;
        }

        private VariableTree createPropertyChangeSupport(String support, String supportName) {
            Set<Modifier> mods = EnumSet.of(Modifier.PRIVATE, Modifier.FINAL, Modifier.TRANSIENT);
            return make.Variable(
                    make.Modifiers(mods),
                    supportName,
                    make.QualIdent(support),
                    make.QualIdent("new " + support + "(this)")); //NOI18N
        }

        private String getPropertyName(EncapsulateDesc desc) {
            return "PROP_" + desc.field.getSimpleName().toString().toUpperCase();//NOI18N
        }

        private VariableTree createPropName(TypeElement node, EncapsulateDesc get) {
            
            String propertyName = getPropertyName(get);
            while (fieldExists(node, propertyName)) {
                propertyName += "_1";
            }
            
            return make.Variable(
                    make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)),
                    propertyName, 
                    make.Identifier("String"),//NOI18N
                    make.Literal(get.field.getSimpleName().toString()));
        }

        private boolean fieldExists(TypeElement clazz, String propertyName) {
            for (VariableElement el : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
                if (el.getSimpleName().contentEquals(propertyName)) {
                    return true;
                }
            }
            return false;
        }

        private static final class SortMethodsByNameComparator implements Comparator<Tree> {

            @Override
            public int compare(Tree o1, Tree o2) {
                if (o1.getKind() == Tree.Kind.VARIABLE) {
                    if (o2.getKind() == Tree.Kind.VARIABLE) {
                        return ((VariableTree) o1).getName().toString().compareTo(((VariableTree) o2).getName().toString());
                    }
                    return -11;
                }
                if (o2.getKind() == Tree.Kind.VARIABLE) {
                    return 1;
                }
                return ((MethodTree) o1).getName().toString().compareTo(((MethodTree) o2).getName().toString());
            }
        }

        @Override
        public Tree visitVariable(VariableTree node, Element field) {
            if (sourceFile == workingCopy.getFileObject()) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                EncapsulateDesc desc = el == null ? null : fields.get(el);
                if (desc != null) {
                    resolveFieldDeclaration(node, desc);
                    return node;
                }
            }
            return scan(node.getInitializer(), field);
        }

        @Override
        public Tree visitAssignment(AssignmentTree node, Element field) {
            ExpressionTree variable = node.getVariable();
            boolean isArray = false;
            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArray = true;
                // int[] a; a[a[0]][a[1]] = 0; // scan also array indices
                scan(((ArrayAccessTree) variable).getIndex(), field);
                variable = ((ArrayAccessTree) variable).getExpression();
            }

            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
            EncapsulateDesc desc = fields.get(el);
            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
                    // check (field = 3) == 3
                    && (isArray || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());

                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    ExpressionTree newExpTree;
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    TypeMirror expType = trees.getTypeMirror(expPath);
                    if (workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = expTree;
                    } else {
                        newExpTree = make.TypeCast(make.Type(varType), expTree);
                    }

                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));
                    
                    rewrite(node, invksetter);
                    
                    setterUsed = true;
                }
            }
            return scan(node.getExpression(), field);
        }

        @Override
        public Tree visitCompoundAssignment(CompoundAssignmentTree node, Element field) {
            ExpressionTree variable = node.getVariable();
            boolean isArray = false;
            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArray = true;
                variable = ((ArrayAccessTree) variable).getExpression();
            }

            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
            EncapsulateDesc desc = fields.get(el);
            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
                    // check (field += 3) == 3
                    && (isArray || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());

                    // translate compound op to binary op; ADD_ASSIGNMENT -> ADD
                    String s = node.getKind().name();
                    s = s.substring(0, s.length() - "_ASSIGNMENT".length()); // NOI18N
                    Tree.Kind operator = Tree.Kind.valueOf(s);

                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());

                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    ExpressionTree newExpTree;
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    // getter need not exist yet, use variable to resolve type of binary expression
                    ExpressionTree expTreeFake = make.Binary(operator, variable, expTree);
                    TypeMirror expType = workingCopy.getTreeUtilities().attributeTree(expTreeFake, trees.getScope(expPath));

                    newExpTree = make.Binary(operator, invkgetter, expTree);
                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
                    }

                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));
                    rewrite(node, invksetter);
                }
            }
            return scan(node.getExpression(), field);
        }

        @Override
        public Tree visitUnary(UnaryTree node, Element field) {
            ExpressionTree t = node.getExpression();
            Kind kind = node.getKind();
            boolean isArrayOrImmutable = kind != Kind.POSTFIX_DECREMENT
                    && kind != Kind.POSTFIX_INCREMENT
                    && kind != Kind.PREFIX_DECREMENT
                    && kind != Kind.PREFIX_INCREMENT;
            while (t.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArrayOrImmutable = true;
                t = ((ArrayAccessTree) t).getExpression();
            }
            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), t));
            EncapsulateDesc desc = el == null ? null : fields.get(el);
            if (desc != null && desc.useAccessors
                    && desc.refactoring.getGetterName() != null
                    && (isArrayOrImmutable || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                // check (++field + 3)
                ExpressionTree invkgetter = createGetterInvokation(t, desc.refactoring.getGetterName());
                if (isArrayOrImmutable) {
                    rewrite(t, invkgetter);
                } else if (desc.refactoring.getSetterName() != null) {
                    ExpressionTree setter = createMemberSelection(node.getExpression(), desc.refactoring.getSetterName());

                    Tree.Kind operator = kind == Tree.Kind.POSTFIX_INCREMENT || kind == Tree.Kind.PREFIX_INCREMENT
                            ? Tree.Kind.PLUS
                            : Tree.Kind.MINUS;

                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    TypeMirror expType = workingCopy.getTypes().getPrimitiveType(TypeKind.INT);
                    ExpressionTree newExpTree = make.Binary(operator, invkgetter, make.Literal(1));
                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
                    }

                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));
                    rewrite(node, invksetter);
                }
            }
            return null;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, Element field) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            EncapsulateDesc desc = el == null ? null : fields.get(el);
            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
                rewrite(node, nodeNew);
            }
            return super.visitMemberSelect(node, field);
        }

        @Override
        public Tree visitIdentifier(IdentifierTree node, Element field) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            EncapsulateDesc desc = el == null ? null : fields.get(el);
            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
                rewrite(node, nodeNew);
            }
            return null;
        }

        private boolean checkAssignmentInsideExpression() {
            Tree exp1 = getCurrentPath().getLeaf();
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
                // XXX would be useful if Problems support HTML
//                String code = parent.toString();
//                String replace = exp1.toString();
//                code = code.replace(replace, "&lt;b&gt;" + replace + "&lt;/b&gt;");
                problem = createProblem(
                        problem,
                        false,
                        NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateInsideAssignment", // NOI18N
                        exp1.toString(),
                        parent.toString(),
                        FileUtil.getFileDisplayName(workingCopy.getFileObject())));
                return false;
            }
            return true;
        }

        /**
         * replace current expresion with the proper one.<p> c.field ->
         * c.getField() field -> getField() or copy in case of
         * refactoring.getGetterName() == null
         */
        private ExpressionTree createGetterInvokation(ExpressionTree current, String getterName) {
            // check if exist refactoring.getGetterName() != null and visibility (subclases)
            if (getterName == null) {
                return current;
            }
            ExpressionTree getter = createMemberSelection(current, getterName);

            MethodInvocationTree invkgetter = make.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),
                    getter,
                    Collections.<ExpressionTree>emptyList());
            GeneratorUtilities.get(workingCopy).copyComments(current, invkgetter, false);
            GeneratorUtilities.get(workingCopy).copyComments(current, invkgetter, true);
            return invkgetter;
        }

        private ExpressionTree createMemberSelection(ExpressionTree node, String name) {
            ExpressionTree selector;
            ExpressionTree expr = node;
            boolean addParens = false;
            while (expr.getKind() == Tree.Kind.PARENTHESIZED) {
                ParenthesizedTree parens = (ParenthesizedTree) expr;
                expr = parens.getExpression();
                addParens = true;
            }
            if (expr.getKind() == Tree.Kind.MEMBER_SELECT) {
                ExpressionTree select = ((MemberSelectTree) expr).getExpression();
                if (addParens) {
                    select = make.Parenthesized(select);
                }
                selector = make.MemberSelect(select, name);
            } else {
                selector = make.Identifier(name);
            }
            return selector;
        }

        private MethodTree[] createGetterAndSetter(
                VariableElement field, String getterName, String setterName,
                Set<Modifier> useModifiers, CodeStyle cs,
                VariableTree propertyChange, VariableTree vetoableChange, VariableTree propName) {
            boolean staticMod = field.getModifiers().contains(Modifier.STATIC);
            String fieldName = CodeStyleUtils.removePrefixSuffix(field.getSimpleName(),
                    staticMod ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                    staticMod ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
            
            String longName = (staticMod ? "" : "this.") + field.getSimpleName();//NOI18N
            String oldName = "old" + CodeStyleUtils.getCapitalizedName(fieldName);//NOI18N
            String parName = staticMod ? "a" + CodeStyleUtils.getCapitalizedName(fieldName) : fieldName; //NOI18N
            String getterBody = "{return " + field.getSimpleName() + ";}"; //NOI18N
            StringBuilder setterBody = new StringBuilder();
            setterBody.append("{");//NOI18N

            if (propertyChange != null || vetoableChange != null) {
                setterBody.append(field.asType().toString()).append(" ").append(oldName).append(" = ").append(longName).append(";");//NOI18N
            }
            if (vetoableChange != null) {
                setterBody.append(vetoableChange.getName()).append(".fireVetoableChange(").append(propName.getName()).append(", ").append(oldName).append(", ").append(fieldName).append(");");//NOI18N
            }

            setterBody.append(longName).append(" = ").append(parName).append(";"); //NOI18N

            if (propertyChange != null) {
                setterBody.append(propertyChange.getName()).append(".firePropertyChange(").append(propName.getName()).append(", ").append(oldName).append(", ").append(fieldName).append(");");//NOI18N
            }


            setterBody.append("}");//NOI18N

            Set<Modifier> mods = EnumSet.noneOf(Modifier.class);
            mods.addAll(useModifiers);

            if (staticMod) {
                mods.add(Modifier.STATIC);
            }

            VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
            MethodTree[] result = new MethodTree[2];

            ExecutableElement getterElm = null;
            if (getterName != null) {
                getterElm = findMethod(
                        workingCopy,
                        (TypeElement) field.getEnclosingElement(),
                        getterName,
                        Collections.<VariableElement>emptyList(), false);
            }
            if (getterElm == null && getterName != null) {
                MethodTree getter = make.Method(
                        make.Modifiers(mods),
                        getterName,
                        fieldTree.getType(),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        getterBody,
                        null);
                result[0] = getter;
                String jdText = null;
                if (javadocType == Javadoc.COPY) {
                    jdText = workingCopy.getElements().getDocComment(field);
                    jdText = trimNewLines(jdText);
                }
                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
                    Comment comment = Comment.create(
                            Comment.Style.JAVADOC, -2, -2, -2,
                            prefix + "@return the " + field.getSimpleName()); // NOI18N
                    make.addComment(getter, comment, true);
                }
            }

            ExecutableElement setterElm = null;
            if (setterName != null) {
                setterElm = findMethod(
                        workingCopy,
                        (TypeElement) field.getEnclosingElement(),
                        setterName,
                        Collections.<VariableElement>singletonList(field), false);
            }
            if (setterElm == null && setterName != null) {
                VariableTree paramTree = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()), parName, fieldTree.getType(), null);
                MethodTree setter = make.Method(
                        make.Modifiers(mods),
                        setterName,
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.singletonList(paramTree),
                        vetoableChange==null?Collections.<ExpressionTree>emptyList():Collections.singletonList(make.QualIdent("java.beans.PropertyVetoException")),
                        setterBody.toString(),
                        null);
                result[1] = setter;

                String jdText = null;
                if (javadocType == Javadoc.COPY) {
                    jdText = workingCopy.getElements().getDocComment(field);
                    jdText = trimNewLines(jdText);
                }
                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
                    Comment comment = Comment.create(
                            Comment.Style.JAVADOC, -2, -2, -2,
                            prefix + String.format("@param %s the %s to set", parName, fieldName)); // NOI18N
                    make.addComment(setter, comment, true);
                }
            }

            return result;
        }

        private String trimNewLines(String javadoc) {
            if (javadoc == null) {
                return null;
            }

            int len = javadoc.length();
            int st = 0;
            int off = 0;      /*
             * avoid getfield opcode
             */
            char[] val = javadoc.toCharArray();    /*
             * avoid getfield opcode
             */

            while ((st < len) && Character.isWhitespace(val[off + st])/*
                     * && (val[off + st] <= '\n')
                     */) {
                st++;
            }
            while ((st < len) && Character.isWhitespace(val[off + len - 1])/*
                     * val[off + len - 1] <= '\n')
                     */) {
                len--;
            }
            return ((st > 0) || (len < val.length)) ? javadoc.substring(st, len) : javadoc;
        }

        private void resolveFieldDeclaration(VariableTree node, EncapsulateDesc desc) {
            Modifier currentAccess = getAccessibility(desc.field.getModifiers());
            Modifier futureAccess = getAccessibility(desc.refactoring.getFieldModifiers());
            ModifiersTree newModTree = null;
            if (currentAccess != futureAccess) {
                newModTree = make.Modifiers(
                        replaceAccessibility(currentAccess, futureAccess, desc.field),
                        node.getModifiers().getAnnotations());
            }

            if (node.getModifiers().getFlags().contains(Modifier.FINAL)
                    && desc.refactoring.getSetterName() != null) {
                // remove final flag in case user wants to create setter
                ModifiersTree mot = newModTree == null ? node.getModifiers() : newModTree;
                Set<Modifier> flags = EnumSet.noneOf(Modifier.class);
                flags.addAll(mot.getFlags());
                flags.remove(Modifier.FINAL);
                newModTree = make.Modifiers(flags, mot.getAnnotations());
            }

            if (newModTree != null) {
                VariableTree newNode = make.Variable(
                        newModTree, node.getName(), node.getType(), node.getInitializer());
                rewrite(node, newNode);
            }
        }

        private boolean resolveUseAccessor(TypeElement where, EncapsulateDesc desc) {
            if (desc.refactoring.isAlwaysUseAccessors()) {
                return true;
            }

            // target field accessibility
            Set<Modifier> mods = desc.refactoring.getFieldModifiers();
            if (mods.contains(Modifier.PRIVATE)) {
                // check enclosing top level class
                // return SourceUtils.getOutermostEnclosingTypeElement(where) != SourceUtils.getOutermostEnclosingTypeElement(desc.field);
                return where != desc.field.getEnclosingElement();
            }

            if (mods.contains(Modifier.PROTECTED)) {
                // check inheritance
                if (isSubclassOf(where, (TypeElement) desc.field.getEnclosingElement())) {
                    return false;
                }
                // check same package
                return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
            }

            if (mods.contains(Modifier.PUBLIC)) {
                return false;
            }

            // default access
            // check same package
            return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
        }

        private boolean isInConstructorOfFieldClass(TreePath path, Element field) {
            Tree leaf = path.getLeaf();
            Kind kind = leaf.getKind();
            while (true) {
                switch (kind) {
                    case METHOD:
                        if (workingCopy.getTreeUtilities().isSynthetic(path)) {
                            return false;
                        }
                        Element m = workingCopy.getTrees().getElement(path);
                        if (m == null) {
                            return false;
                        }
                        boolean result = m.getKind() == ElementKind.CONSTRUCTOR && (m.getEnclosingElement() == field.getEnclosingElement() || isSubclassOf((TypeElement) m.getEnclosingElement(), (TypeElement) field.getEnclosingElement()));
                        if (m.getKind() == ElementKind.CONSTRUCTOR
                                && m.getEnclosingElement() != field.getEnclosingElement()
                                && isSubclassOf((TypeElement) m.getEnclosingElement(), (TypeElement) field.getEnclosingElement())
                                && fields.get(field).refactoring.getFieldModifiers().contains(Modifier.PRIVATE)) {

                            problem = createProblem(
                                    problem,
                                    false,
                                    NbBundle.getMessage(
                                    EncapsulateFieldRefactoringPlugin.class,
                                    "ERR_EncapsulateInsideConstructor", // NOI18N
                                    field.getSimpleName(),
                                    m.getEnclosingElement().getSimpleName()));
                        }
                        return result;

                    case COMPILATION_UNIT:
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case NEW_CLASS:
                        return false;
                }
                path = path.getParentPath();
                leaf = path.getLeaf();
                kind = leaf.getKind();
            }
        }

        private boolean isInGetterSetter(
                TreePath path,
                ElementHandle<ExecutableElement> currentGetter,
                ElementHandle<ExecutableElement> currentSetter) {

            if (sourceFile != workingCopy.getFileObject()) {
                return false;
            }

            Tree leaf = path.getLeaf();
            Kind kind = leaf.getKind();
            while (true) {
                switch (kind) {
                    case METHOD:
                        if (workingCopy.getTreeUtilities().isSynthetic(path)) {
                            return false;
                        }
                        Element m = workingCopy.getTrees().getElement(path);
                        return currentGetter != null && m == currentGetter.resolve(workingCopy)
                                || currentSetter != null && m == currentSetter.resolve(workingCopy);
                    case COMPILATION_UNIT:
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case NEW_CLASS:
                        return false;
                }
                path = path.getParentPath();
                leaf = path.getLeaf();
                kind = leaf.getKind();
            }
        }
    }

    /**
     * A descriptor of the encapsulated field for Encapsulator.
     */
    static final class EncapsulateDesc {

        Problem p;
        Set<FileObject> refs;
        TreePathHandle fieldHandle;
        // following fields are used solely by Encapsulator
        VariableElement field;
        private ElementHandle<ExecutableElement> currentGetter;
        private ElementHandle<ExecutableElement> currentSetter;
        private EncapsulateFieldRefactoring refactoring;
        private boolean useAccessors;
    }
}
