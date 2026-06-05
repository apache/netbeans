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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;

/**
 * Utility class, which determines whether the given set of TreePaths use 'this' - must be located
 * in an instance method. Also checks references to OuterType.this, OuterType.super. Currently 
 * only the function to find whether an instance is or is not used is tested, but the finder can
 * be extended to also either track or invoke a callback on such references.
 *
 * @author sdedic
 */
public class InstanceRefFinder extends ErrorAwareTreePathScanner {
    /**
     * The initial path for analysis
     */
    private TreePath          initPath;
    
    /**
     * Compilation context
     */
    private final CompilationInfo   ci;
    
    /**
     * Holds TypeElements of required instances, which must be passed as parameters
     */
    private Set<TypeElement>        instancesRequired = Collections.emptySet();
    
    /**
     * Immediate enclosing element
     */
    private Element                 enclosingElement;
    private TypeElement             enclosingType;
    
    private Set<TypeElement>        superReferences = Collections.<TypeElement>emptySet();
    
    private Set<Element>            usedMembers = new HashSet<Element>();
    
    /**
     * Local class references.
     */
    private Set<TypeElement>        localReferences = Collections.<TypeElement>emptySet();
    
    public InstanceRefFinder(CompilationInfo ci, TreePath path) {
        this.ci = ci;
        this.initPath = path;
    }

    /**
     * Returns TypeElements, whose instances are required for proper function of the tree.
     * The tree contents reference those instances in one way or other.
     * 
     * @return 
     */
    public Set<TypeElement> getRequiredInstances() {
        return instancesRequired;
    }

    /**
     * Determines whether the tree contains references to local classes. If the tree
     * is to be moved, those local classes must be either copied or externalized.
     * 
     * @return 
     */
    public boolean containsLocalReferences() {
        return !localReferences.isEmpty();
    }
    
    public boolean containsInstanceReferences() {
        return !instancesRequired.isEmpty();
    }
    
    /**
     * Determines whether the tree accesses superclass' members bypassing possible overrides. If
     * the tree moves out of its current class, accessors must be generated to reach the super
     * implementations.
     * 
     * @return 
     */
    public boolean containsReferencesToSuper() {
        return !superReferences.isEmpty();
    }
    
    public Set<TypeElement> getSuperReferences() {
        return superReferences;
    }
    
    private void findEnclosingElement() {
        TreePath path = initPath;
        do {
            Tree t = path.getLeaf();
            switch (t.getKind()) {
                case METHOD:
                case VARIABLE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                case NEW_CLASS:
                    enclosingElement = ci.getTrees().getElement(path);
                    if (enclosingElement == null) {
                        return;
                    }
                    if (enclosingElement instanceof TypeElement) {
                        this.enclosingType = (TypeElement)enclosingElement;
                    } else {
                        enclosingType = ci.getElementUtilities().enclosingTypeElement(enclosingElement);
                    }
                    return;
            }
            path = path.getParentPath();
        } while (path != null);
    }
    
    private void addLocalReference(TypeElement el) {
        if (localReferences.isEmpty()) {
            localReferences = new HashSet<TypeElement>();
        }
        localReferences.add(el);
    }
    
    /**
     * Registers an instance for a type member, if necessary
     * 
     * @param el the referenced element (method, variable, constructor)
     */
    private void addInstanceForMemberOf(Element el) {
        if (el.getModifiers().contains(Modifier.STATIC)) {
            return;
        }
        TypeElement owner = findOwnerType(el);
        addRequiredInstance(owner);
        addUsedMember(el, owner);
    }
    
    protected TypeElement findOwnerType(Element el) {
        Element t;
        if (el instanceof TypeElement) {
            t = ((TypeElement)el);
        } else {
            t = ci.getElementUtilities().enclosingTypeElement(el);
            if (t == null) {
                return null;
            }
        }
        ElementKind k = t.getKind();
        TypeMirror declType = ci.getTypes().erasure(t.asType());
        
        for (TypeElement enclType = enclosingType; enclType != null; enclType = ci.getElementUtilities().enclosingTypeElement(enclType)) {
            if (ci.getTypes().isSubtype(ci.getTypes().erasure(enclType.asType()), declType)) {
                if (k.isClass()) {
                    return enclType;
                } else if (k == ElementKind.INTERFACE) {
                    if (t.getModifiers().contains(Modifier.DEFAULT)) {
                        return enclType;
                    }
                }
                break;
            }
        }
        // PENDING - this is strange, report an error ??
        return null;
    }
    
    private TypeElement findEnclosingType(Element el) {
        return ci.getElementUtilities().enclosingTypeElement(el);
    }
    
    /**
     * Registers an instance for the resolved constructor. If a 
     * 
     * @param el 
     */
    private void addInstanceForConstructor(Element el) {
        TypeElement pt = findEnclosingType(el);
        if (pt == null) {
            return;
        }
        switch (pt.getNestingKind()) {
            case ANONYMOUS:
                // the anonymous class itself may not need an enclosing instance, if its
                // contents do not reference anything from the instance.
                break;
                
            case LOCAL:
                // implicit reference to the enclosing instance, but since a type with no qualname is referenced,
                // the expression / statement can't be probably moved anywhere.
                addLocalReference(pt);
                break;
            case MEMBER:
                // add enclosing type's reference
                if (!pt.getModifiers().contains(Modifier.STATIC)) {
                    addRequiredInstance((TypeElement)pt.getEnclosingElement());
                }
                break;
        }
    }
    
    public void process() {
        process(initPath);
    }
    
    public void process(TreePath path) {
        this.initPath = path;
        findEnclosingElement();
        if (enclosingElement == null || enclosingType == null) {
            return;
        }
        scan(initPath, null);
    }
    
    protected boolean isEnclosingType(TypeElement el) {
        if (el == null) {
            return false;
        }
        Element e = enclosingElement;
        while (e != null && e != el) {
            e = e.getEnclosingElement();
        }
        return e != null;
    }
    
    /**
     * Adds a super reference. Super reference is treated differently, as it forces a call
     * to a super method (possibly overriden in the subclass), it is not possible to call that
     * method from outside - the override would be called. For calls to super.method, an accessor
     * must be generated.
     * 
     * @param el 
     */
    private void addSuperInstance(TypeElement el) {
        if (isEnclosingType(el)) {
            if (superReferences.isEmpty()) {
                superReferences = new HashSet<TypeElement>();
            }
            superReferences.add(el);
        }
        addRequiredInstance(el);
    }
    
    private void addRequiredInstance(TypeElement el) {
        if (isEnclosingType(el)) {
            if (instancesRequired.isEmpty()) {
                instancesRequired = new HashSet<TypeElement>();
            }
            instancesRequired.add(el);
            return;
        }
    }
    
    /**
     * If the variable is typed as a local class, record the local class; it may need to go along
     * with the factored expression, unless the expression only uses some specific interface
     * implemented by the local class.
     * 
     * @param el 
     */
    private void addLocalClassVariable(Element el) {
        TypeMirror tm = el.asType();
        if (tm.getKind() != TypeKind.DECLARED)  {
            return;
        }
        Element e = ((DeclaredType)tm).asElement();
        if (!(e instanceof TypeElement)) {
            return;
        }
        TypeElement t = (TypeElement)e;
        if (t.getNestingKind() == NestingKind.LOCAL) {
            addLocalReference(t);
        }
    }
    
    private void addInstanceForType(TypeElement t) {
        addRequiredInstance(t);
    }
    
    private void addInstanceOfTypeParameter(Element el) {
        Element parent = el.getEnclosingElement();
        if (parent.getKind().isClass() || parent.getKind().isInterface()) {
            addRequiredInstance(((TypeElement)parent));
        }
    }
    
    protected void addUsedMember(Element el, TypeElement owner) {
        if (!isEnclosingType(owner)) {
            return;
        }
        usedMembers.add(el);
    }
    
    public Set<Element> getUsedMembers() {
        return usedMembers;
    }
    
    private void addInstanceOfParameterOwner(Element el) {
        while (el != null && el.getKind() != ElementKind.CONSTRUCTOR && el.getKind() != ElementKind.METHOD && !el.getKind().isClass() && !el.getKind().isInterface()) {
            el = el.getEnclosingElement();
        }
        if (el == null || el instanceof TypeElement || el == enclosingElement) {
            return;
        }
        addInstanceForMemberOf(el);
    }
    
    @Override
    public Object visitIdentifier(IdentifierTree node, Object p) {
        Element el = ci.getTrees().getElement(getCurrentPath());
        if (el == null || el.asType() == null || el.asType().getKind() == TypeKind.ERROR) {
            return null;
        }
        switch (el.getKind()) {
            case LOCAL_VARIABLE:
                addLocalClassVariable(el);
                addUsedMember(el, enclosingType);
                break;
            case TYPE_PARAMETER:
                addInstanceOfTypeParameter(el);
                break;
            case FIELD:
            case METHOD:
                addInstanceForMemberOf(el);
                break;
            case CLASS:
            case ENUM:
            case INTERFACE:
            case RECORD:
                if (node.getName().contentEquals("this") || node.getName().contentEquals("super")) {
                    addInstanceForType(enclosingType);
                }
                break;
            case EXCEPTION_PARAMETER:
            case RESOURCE_VARIABLE:
                addLocalClassVariable(el);
                // fall through
            case PARAMETER:
                addInstanceOfParameterOwner(el);
                break;
            case PACKAGE:
                break;
            default:
                addUsedMember(el, enclosingType);
        }
        return super.visitIdentifier(node, p);
    }
    
    @Override
    public Object visitMemberReference(MemberReferenceTree node, Object p) {
        return super.visitMemberReference(node, p); 
    }
    
    private TypeElement findType(Tree selector) {
        TypeMirror tm = ci.getTrees().getTypeMirror(new TreePath(getCurrentPath(), selector));
        if (tm != null && tm.getKind() == TypeKind.DECLARED) {
            TypeElement t = (TypeElement)ci.getTypes().asElement(tm);
            ElementKind ek = t.getKind();
            if (!(ek.isClass() || ek.isInterface())) {
                // PENDING: an error, log
                return null;
            }
            // the referenced type must be in the same CU, cannot be a superclass.
            return t;
        }
        return null;
    }

    @Override
    public Object visitMemberSelect(MemberSelectTree node, Object p) {
        String exp = node.getExpression().toString();
        // ClassName.this
        if (exp.equals("this") || exp.endsWith(".this")) { // NOI18N
            addInstanceForType(findType(node.getExpression()));
        } else if (exp.equals("super")) { // NOI18N
            // reference to superclass of this type
            addSuperInstance(enclosingType);
        } else if (exp.endsWith(".super")) { // NOI18N
            // this is a reference to the superclass of some enclosing type.
            if (node.getExpression().getKind() == Tree.Kind.MEMBER_SELECT) {
                Tree t = ((MemberSelectTree)node.getExpression()).getExpression();
                addSuperInstance(findType(t));
            }
        } else if (node.getIdentifier().contentEquals("this")) { // NOI18N
            // reference to this
            addInstanceForType(findType(node.getExpression()));
        } else {
            // references to Clazz.super are invalid, references to Clazz.super.whatever() must be
            // a pert of a broader memberSelect, which will be caught one level up.
            return super.visitMemberSelect(node, p);
        }
        return null;
    }
    
    @Override
    public Object visitClass(ClassTree node, Object p) {
        TypeElement saveType = this.enclosingType;
        this.enclosingType = (TypeElement)ci.getTrees().getElement(getCurrentPath());
        Object o = super.visitClass(node, p);
        this.enclosingType = saveType;
        return o;
    }

    @Override
    public Object visitNewClass(NewClassTree node, Object p) {
        Element e = ci.getTrees().getElement(getCurrentPath());
        if (e != null && e.getKind() == ElementKind.CONSTRUCTOR) {
            addInstanceForConstructor(e);
        }
        Object r = scan(node.getEnclosingExpression(), p);
        r = scanAndReduce(node.getIdentifier(), p, r);
        r = scanAndReduce(node.getTypeArguments(), p, r);
        r = scanAndReduce(node.getArguments(), p, r);
        
        // switch context to the anonymous class
        if (e != null) {
            TypeElement saveType = enclosingType;
            enclosingType = ci.getElementUtilities().enclosingTypeElement(e);
            r = scanAndReduce(node.getClassBody(), p, r);
            this.enclosingType = saveType;
        }
        return r;
    }

    private Object scanAndReduce(Tree node, Object p, Object r) {
        return reduce(scan(node, p), r);
    }
    
    private Object scanAndReduce(Iterable<? extends Tree> nodes, Object p, Object r) {
        return reduce(scan(nodes, p), r);
    }
    
}
