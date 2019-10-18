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

package org.netbeans.api.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.ModuleFinder;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.ElementUtils;
import org.netbeans.modules.java.source.usages.ClassFileUtil;
import org.openide.util.Parameters;
import org.openide.util.WeakSet;

/**
 * Represents a handle for {@link Element} which can be kept and later resolved
 * by another javac. The javac {@link Element}s are valid only in a single
 * {@link javax.tools.CompilationTask} or a single run of a
 * {@link CancellableTask}. A client needing to
 * keep a reference to an {@link Element} and use it in another {@link CancellableTask}
 * must serialize it into an {@link ElementHandle}.
 * Currently not all {@link Element}s can be serialized. See {@link #create} for details.
 * <div class="nonnormative">
 * <p>
 * Typical usage of {@link ElementHandle} is as follows:
 * </p>
 * <pre>
 * final ElementHandle[] elementHandle = new ElementHandle[1];
 * javaSource.runUserActionTask(new Task&lt;CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         compilationController.toPhase(Phase.RESOLVED);
 *         CompilationUnitTree cu = compilationController.getTree();
 *         List&lt;? extends Tree> types = getTypeDecls(cu);
 *         Tree tree = getInterestingElementTree(types);
 *         Element element = compilationController.getElement(tree);
 *         elementHandle[0] = ElementHandle.create(element);
 *    }
 * }, true);
 *
 * otherJavaSource.runUserActionTask(new Task&lt;CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         compilationController.toPhase(Phase.RESOLVED);
 *         Element element = elementHandle[0].resolve(compilationController);
 *         // ....
 *    }
 * }, true);
 * </pre>
 * </div>
 * @author Tomas Zezula
 */
public final class ElementHandle<T extends Element> {
    private static final Logger log = Logger.getLogger(ElementHandle.class.getName());
    static {
        ElementHandleAccessor.setInstance(new ElementHandleAccessorImpl ());
    }
    
    private final ElementKind kind;
    private final String[] signatures;
        
       
    private ElementHandle(final ElementKind kind, String... signatures) {
        assert kind != null;
        assert signatures != null;
        this.kind = kind;
        this.signatures = signatures;
    }
    
    
    /**
     * Resolves an {@link Element} from the {@link ElementHandle}.
     * @param compilationInfo representing the {@link javax.tools.CompilationTask}
     * in which the {@link Element} should be resolved.
     * @return resolved subclass of {@link Element} or null if the elment does not exist on
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.
     */
    @SuppressWarnings ("unchecked")     // NOI18N
    public @CheckForNull T resolve (@NonNull final CompilationInfo compilationInfo) {
        Parameters.notNull("compilationInfo", compilationInfo); // NOI18N
        ModuleElement module;

        if (compilationInfo.getFileObject() != null) {
            JCTree.JCCompilationUnit cut = (JCTree.JCCompilationUnit)compilationInfo.getCompilationUnit();
            if (cut != null) {
                module = cut.modle;
            } else if (compilationInfo.getTopLevelElements().iterator().hasNext()) {
                module = ((Symbol) compilationInfo.getTopLevelElements().iterator().next()).packge().modle;
            } else {
                module = null;
            }
        } else {
            module = null;
        }
        T result = resolveImpl (module, compilationInfo.impl.getJavacTask());
        if (result == null) {
            if (log.isLoggable(Level.INFO))
                log.log(Level.INFO, "Cannot resolve: {0}", toString()); //NOI18N                
        } else {
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "Resolved element = {0}", result);
        }
        return result;
    }
        
    
    private T resolveImpl (final ModuleElement module, final JavacTaskImpl jt) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Resolving element kind: {0}", this.kind); // NOI18N       
        switch (this.kind) {
            case PACKAGE:
                assert signatures.length == 1;
                return (T) jt.getElements().getPackageElement(signatures[0]);
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE: {
                assert signatures.length == 1;
                final Element type = getTypeElementByBinaryName (module, signatures[0], jt);
                if (type instanceof TypeElement) {
                    return (T) type;
                } else  {
                    log.log(Level.INFO, "Resolved type is null for kind = {0}", this.kind);  // NOI18N
                }
                break;
            }
            case OTHER:
                assert signatures.length == 1;
                return (T) getTypeElementByBinaryName (module, signatures[0], jt);
            case METHOD:
            case CONSTRUCTOR:            
            {
                assert signatures.length == 3;
                final Element type = getTypeElementByBinaryName (module, signatures[0], jt);
                if (type instanceof TypeElement) {
                   final List<? extends Element> members = type.getEnclosedElements();
                   for (Element member : members) {
                       if (this.kind == member.getKind()) {
                           String[] desc = ClassFileUtil.createExecutableDescriptor((ExecutableElement)member);
                           assert desc.length == 3;
                           if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                               return (T) member;
                           }
                       }
                   }
                } else if (type != null) {
                    return (T) new Symbol.MethodSymbol(0, (Name) jt.getElements().getName(this.signatures[1]), Symtab.instance(jt.getContext()).unknownType, (Symbol)type);
                } else 
                    log.log(Level.INFO, "Resolved type is null for kind = {0}", this.kind);  // NOI18N
                break;
            }
            case INSTANCE_INIT:
            case STATIC_INIT:
            {
                assert signatures.length == 2;
                final Element type = getTypeElementByBinaryName (module, signatures[0], jt);
                if (type instanceof TypeElement) {
                   final List<? extends Element> members = type.getEnclosedElements();
                   for (Element member : members) {
                       if (this.kind == member.getKind()) {
                           String[] desc = ClassFileUtil.createExecutableDescriptor((ExecutableElement)member);
                           assert desc.length == 2;
                           if (this.signatures[1].equals(desc[1])) {
                               return (T) member;
                           }
                       }
                   }
                } else
                    log.log(Level.INFO, "Resolved type is null for kind = {0}", this.kind); // NOI18N
                break;
            }
            case FIELD:
            case ENUM_CONSTANT:
            {
                assert signatures.length == 3;
                final Element type = getTypeElementByBinaryName (module, signatures[0], jt);
                if (type instanceof TypeElement) {
                    final List<? extends Element> members = type.getEnclosedElements();
                    for (Element member : members) {
                        if (this.kind == member.getKind()) {
                            String[] desc = ClassFileUtil.createFieldDescriptor((VariableElement)member);
                            assert desc.length == 3;
                            if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                                return (T) member;
                            }
                        }
                    }
                } else if (type != null) {
                    return (T) new Symbol.VarSymbol(0, (Name) jt.getElements().getName(this.signatures[1]), Symtab.instance(jt.getContext()).unknownType, (Symbol)type);
                } else 
                    log.log(Level.INFO, "Resolved type is null for kind = {0}", this.kind); // NOI18N
                break;
            }
            case TYPE_PARAMETER:
            {
                if (signatures.length == 2) {
                     Element type = getTypeElementByBinaryName (module, signatures[0], jt);
                     if (type instanceof TypeElement) {
                         List<? extends TypeParameterElement> tpes = ((TypeElement)type).getTypeParameters();
                         for (TypeParameterElement tpe : tpes) {
                             if (tpe.getSimpleName().contentEquals(signatures[1])) {
                                 return (T)tpe;
                             }
                         }
                     } else 
                        log.log(Level.INFO, "Resolved type is null for kind = {0} signatures.length = {1}", new Object[] {this.kind, signatures.length});   // NOI18N
                }
                else if (signatures.length == 4) {
                    final Element type = getTypeElementByBinaryName (module, signatures[0], jt);
                    if (type instanceof TypeElement) {
                        final List<? extends Element> members = type.getEnclosedElements();
                        for (Element member : members) {
                            if (member.getKind() == ElementKind.METHOD || member.getKind() == ElementKind.CONSTRUCTOR) {
                                String[] desc = ClassFileUtil.createExecutableDescriptor((ExecutableElement)member);
                                assert desc.length == 3;
                                if (this.signatures[1].equals(desc[1]) && this.signatures[2].equals(desc[2])) {
                                    assert member instanceof ExecutableElement;
                                    List<? extends TypeParameterElement> tpes =((ExecutableElement)member).getTypeParameters();
                                    for (TypeParameterElement tpe : tpes) {
                                        if (tpe.getSimpleName().contentEquals(signatures[3])) {
                                            return (T) tpe;
                                        }
                                    }
                                }
                            }
                        }
                    } else 
                        log.log(Level.INFO, "Resolved type is null for kind = {0} signatures.length = {1}", new Object[] {this.kind, signatures.length}); // NOI18N
                }
                else {
                    throw new IllegalStateException ();
                }
                break;
            }
            case MODULE:
                assert signatures.length == 1;
                final ModuleFinder cml = ModuleFinder.instance(jt.getContext());
                final Element me = cml.findModule((Name)jt.getElements().getName(this.signatures[0]));
                if (me != null) {
                    return (T) me;
                } else {
                    log.log(Level.INFO, "Cannot resolve module: {0}", this.signatures[0]);  // NOI18N
                }
                break;
            default:
                throw new IllegalStateException ();
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "All resolvings failed. Returning null.");  // NOI18N
        return null;
    }
    
    
    /**
     * Tests if the handle has the same signature as the parameter.
     * The handles with the same signatures are resolved into the same
     * element in the same {@link javax.tools.JavaCompiler} task, but may be resolved into
     * the different {@link Element}s in the different {@link javax.tools.JavaCompiler} tasks.
     * @param handle to be checked
     * @return true if the handles resolve into the same {@link Element}s
     * in the same {@link javax.tools.JavaCompiler} task.
     */
    public boolean signatureEquals (@NonNull final ElementHandle<? extends Element> handle) {
         if (!isSameKind (this.kind, handle.kind) || this.signatures.length != handle.signatures.length) {
             return false;
         }
         for (int i=0; i<signatures.length; i++) {
             if (!signatures[i].equals(handle.signatures[i])) {
                 return false;
             }
         }
         return true;
    }
    
    
    private static boolean isSameKind (ElementKind k1, ElementKind k2) {
        if ((k1 == k2) ||
           (k1 == ElementKind.OTHER && (k2.isClass() || k2.isInterface())) ||     
           (k2 == ElementKind.OTHER && (k1.isClass() || k1.isInterface()))) {
            return true;
        }
        return false;
    }
    
    
    /**
     * Returns a binary name of the {@link TypeElement} represented by this
     * {@link ElementHandle}. When the {@link ElementHandle} doesn't represent
     * a {@link TypeElement} it throws a {@link IllegalStateException}
     * @return the qualified name
     * @throws an {@link IllegalStateException} when this {@link ElementHandle} 
     * isn't created for the {@link TypeElement}.
     */
    public @NonNull String getBinaryName () throws IllegalStateException {
        if ((this.kind.isClass() && !isArray(signatures[0])) ||
                this.kind.isInterface() ||
                this.kind == ElementKind.MODULE ||
                this.kind == ElementKind.OTHER) {
            return this.signatures[0];
        }
        else {
            throw new IllegalStateException ();
        }
    }
    
    
    /**
     * Returns a qualified name of the {@link TypeElement} represented by this
     * {@link ElementHandle}. When the {@link ElementHandle} doesn't represent
     * a {@link TypeElement} it throws a {@link IllegalStateException}
     * @return the qualified name
     * @throws an {@link IllegalStateException} when this {@link ElementHandle} 
     * isn't creatred for the {@link TypeElement}.
     */
    public @NonNull String getQualifiedName () throws IllegalStateException {
        if ((this.kind.isClass() && !isArray(signatures[0])) ||
                this.kind.isInterface() ||
                this.kind == ElementKind.MODULE ||
                this.kind == ElementKind.OTHER) {
            return this.signatures[0].replace (Target.DEFAULT.syntheticNameChar(),'.');    //NOI18N
        }
        else {
            throw new IllegalStateException ();
        }
    }
    
    
    /**
     * Tests if the handle has this same signature as the parameter.
     * The handles has the same signatures if it is resolved into the same
     * element in the same {@link javax.tools.JavaCompiler} task, but may be resolved into
     * the different {@link Element} in the different {@link javax.tools.JavaCompiler} task.
     * @param element to be checked
     * @return true if this handle resolves into the same {@link Element}
     * in the same {@link javax.tools.JavaCompiler} task.
     */
    public boolean signatureEquals (@NonNull final T element) {
        final ElementKind ek = element.getKind();
        final ElementKind thisKind = getKind();
        if ((ek != thisKind) && !(thisKind == ElementKind.OTHER && (ek.isClass() || ek.isInterface()))) {
            return false;
        }
        final ElementHandle<T> handle = create (element);
        return signatureEquals (handle);
    }
    
    /**
     * Returns the {@link ElementKind} of this element handle,
     * it is the kind of the {@link Element} from which the handle
     * was created.
     * @return {@link ElementKind}
     *
     */
    public @NonNull ElementKind getKind () {
        return this.kind;
    }
    
    private static final WeakSet<ElementHandle<?>> NORMALIZATION_CACHE = new WeakSet<ElementHandle<?>>();

    /**
     * Factory method for creating {@link ElementHandle}.
     * @param element for which the {@link ElementHandle} should be created. Permitted
     * {@link ElementKind}s
     * are: {@link ElementKind#PACKAGE}, {@link ElementKind#CLASS},
     * {@link ElementKind#INTERFACE}, {@link ElementKind#ENUM}, {@link ElementKind#ANNOTATION_TYPE}, {@link ElementKind#METHOD},
     * {@link ElementKind#CONSTRUCTOR}, {@link ElementKind#INSTANCE_INIT}, {@link ElementKind#STATIC_INIT},
     * {@link ElementKind#FIELD}, and {@link ElementKind#ENUM_CONSTANT}.
     * @return a new {@link ElementHandle}
     * @throws IllegalArgumentException if the element is of an unsupported {@link ElementKind}
     */
    public static @NonNull <T extends Element> ElementHandle<T> create (@NonNull final T element) throws IllegalArgumentException {
        ElementHandle<T> eh = createImpl(element);

        return (ElementHandle<T>) NORMALIZATION_CACHE.putIfAbsent(eh);
    }
    
    /**
     * Creates an {@link ElementHandle} representing a {@link PackageElement}.
     * @param packageName the name of the package
     * @return the created {@link ElementHandle}
     * @since 0.98
     */
    @NonNull
    public static ElementHandle<PackageElement> createPackageElementHandle (
        @NonNull final String packageName) {
        Parameters.notNull("packageName", packageName); //NOI18N
        return new ElementHandle<PackageElement>(ElementKind.PACKAGE, packageName);
    }
    
    /**
     * Creates an {@link ElementHandle} representing a {@link TypeElement}.
     * @param kind the {@link ElementKind} of the {@link TypeElement},
     * allowed values are {@link ElementKind#CLASS}, {@link ElementKind#INTERFACE},
     * {@link ElementKind#ENUM} and {@link ElementKind#ANNOTATION_TYPE}.
     * @param binaryName the class binary name as specified by JLS §13.1
     * @return the created {@link ElementHandle}
     * @throws IllegalArgumentException if kind is neither class nor interface
     * @since 0.98
     */
    @NonNull
    public static ElementHandle<TypeElement> createTypeElementHandle(
        @NonNull final ElementKind kind,
        @NonNull final String binaryName) throws IllegalArgumentException {
        Parameters.notNull("kind", kind);   //NOI18N
        Parameters.notNull("binaryName", binaryName);   //NOI18N
        if (!kind.isClass() && !kind.isInterface()) {
            throw new IllegalArgumentException(kind.toString());
        }
        return new ElementHandle<TypeElement>(kind, binaryName);
    }

    /**
     * Creates an {@link ElementHandle} representing a {@link ModuleElement}.
     * @param moduleName the name of the module
     * @return the created {@link ElementHandle}
     * @since 2.26
     */
    @NonNull
    public static ElementHandle<ModuleElement> createModuleElementHandle(
            @NonNull final String moduleName) {
        Parameters.notNull("moduleName", moduleName); //NOI18N
        return new ElementHandle<>(ElementKind.MODULE, moduleName);
    }

    private static @NonNull <T extends Element> ElementHandle<T> createImpl (@NonNull final T element) throws IllegalArgumentException {
        Parameters.notNull("element", element);
        ElementKind kind = element.getKind();
        String[] signatures;
        switch (kind) {
            case PACKAGE:
                assert element instanceof PackageElement;
                signatures = new String[]{((PackageElement)element).getQualifiedName().toString()};
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
                assert element instanceof TypeElement;
                signatures = new String[] {ClassFileUtil.encodeClassNameOrArray((TypeElement)element)};
                break;
            case METHOD:
            case CONSTRUCTOR:                
            case INSTANCE_INIT:
            case STATIC_INIT:
                assert element instanceof ExecutableElement;
                signatures = ClassFileUtil.createExecutableDescriptor((ExecutableElement)element);
                break;
            case FIELD:
            case ENUM_CONSTANT:
                assert element instanceof VariableElement;
                signatures = ClassFileUtil.createFieldDescriptor((VariableElement)element);
                break;
            case TYPE_PARAMETER:
                assert element instanceof TypeParameterElement;
                TypeParameterElement tpe = (TypeParameterElement) element;
                Element ge = tpe.getGenericElement();
                ElementKind gek = ge.getKind();
                if (gek.isClass() || gek.isInterface()) {
                    assert ge instanceof TypeElement;
                    signatures = new String[2];
                    signatures[0] = ClassFileUtil.encodeClassNameOrArray((TypeElement)ge);
                    signatures[1] = tpe.getSimpleName().toString();
                }
                else if (gek == ElementKind.METHOD || gek == ElementKind.CONSTRUCTOR) {
                    assert ge instanceof ExecutableElement;
                    String[] _sigs = ClassFileUtil.createExecutableDescriptor((ExecutableElement)ge);
                    signatures = new String[_sigs.length + 1];
                    System.arraycopy(_sigs, 0, signatures, 0, _sigs.length);
                    signatures[_sigs.length] = tpe.getSimpleName().toString();
                }
                else {
                    throw new IllegalArgumentException(gek.toString());
                }
                break;
            case MODULE:
                signatures = new String[]{((ModuleElement)element).getQualifiedName().toString()};
                break;
            default:
                throw new IllegalArgumentException(kind.toString());
        }
        return new ElementHandle<T> (kind, signatures);
    }
    
    /**
     * Gets {@link ElementHandle} from {@link TypeMirrorHandle} representing {@link DeclaredType}.
     * @param typeMirrorHandle from which the {@link ElementHandle} should be retrieved. Permitted
     * {@link TypeKind} is {@link TypeKind#DECLARED}.
     * @return an {@link ElementHandle}
     * @since 0.29.0
     */
    public static @NonNull ElementHandle<? extends TypeElement> from (@NonNull final TypeMirrorHandle<? extends DeclaredType> typeMirrorHandle) {
        Parameters.notNull("typeMirrorHandle", typeMirrorHandle);
        if (typeMirrorHandle.getKind() != TypeKind.DECLARED) {
            throw new IllegalStateException("Incorrect kind: " + typeMirrorHandle.getKind());
        }
        return (ElementHandle<TypeElement>)typeMirrorHandle.getElementHandle();
    }
    
    public @Override String toString () {
        final StringBuilder result = new StringBuilder ();
        result.append (this.getClass().getSimpleName());
        result.append ('[');                                // NOI18N
        result.append ("kind=").append (this.kind.toString());      // NOI18N
        result.append ("; sigs=");                          // NOI18N
        for (String sig : this.signatures) {
            result.append (sig);
            result.append (' ');                            // NOI18N
        }
        result.append (']');                                // NOI18N
        return result.toString();
    }
    
    
    /**@inheritDoc*/
    @Override
    public int hashCode () {
        int hashCode = 0;
        
        for (String sig : signatures) {
            hashCode = hashCode ^ (sig != null ? sig.hashCode() : 0);
        }
        
        return hashCode;
    }
    
    /**@inheritDoc*/
    @Override
    public boolean equals (Object other) {
        if (other instanceof ElementHandle) {
            return signatureEquals((ElementHandle)other);
        }
        return false;
    }
    
    
    /**
     * Returns the element signature.
     * Package private, used by ClassIndex.
     */
    String[] getSignature () {
        return this.signatures;
    }
        
    
    private static class ElementHandleAccessorImpl extends ElementHandleAccessor {
        
        @Override
        public ElementHandle create(ElementKind kind, String... descriptors) {
            assert kind != null;
            assert descriptors != null;
            switch (kind) {
                case PACKAGE:
                    if (descriptors.length != 1) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<PackageElement> (kind, descriptors);
                case MODULE:
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                case OTHER:
                    if (descriptors.length != 1) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<TypeElement> (kind, descriptors);
                case METHOD:
                case CONSTRUCTOR:                
                    if (descriptors.length != 3) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<ExecutableElement> (kind, descriptors);
                case INSTANCE_INIT:
                case STATIC_INIT:
                    if (descriptors.length != 2) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<ExecutableElement> (kind, descriptors);
                case FIELD:
                case ENUM_CONSTANT:
                    if (descriptors.length != 3) {
                        throw new IllegalArgumentException ();
                    }
                    return new ElementHandle<VariableElement> (kind, descriptors);
                default:
                    throw new IllegalArgumentException ();
            }            
        }

        @Override
        public <T extends Element> T resolve(ElementHandle<T> handle, JavacTaskImpl jti) {
            return handle.resolveImpl (null, jti);
        }

        @Override
        @NonNull
        public String[] getJVMSignature(@NonNull final ElementHandle<?> handle) {
            return Arrays.copyOf(handle.signatures, handle.signatures.length);
        }

    }
    
    private static Element getTypeElementByBinaryName (final ModuleElement module, final String signature, final JavacTaskImpl jt) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Calling getTypeElementByBinaryName: signature = {0}", signature);
        if (isNone(signature)) {
            return Symtab.instance(jt.getContext()).noSymbol;
        }
        else if (isArray(signature)) {
            return Symtab.instance(jt.getContext()).arrayClass;
        }
        else {
            return (TypeElement) (module != null
                    ? ElementUtils.getTypeElementByBinaryName(jt, module, signature)
                    : ElementUtils.getTypeElementByBinaryName(jt, signature));
        }
    }
    
    private static boolean isNone (String signature) {
        return signature.length() == 0;
    }

    private static boolean isArray (String signature) {
        return signature.length() == 1 && signature.charAt(0) == '[';
    }
}
