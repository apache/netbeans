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
package org.netbeans.modules.groovy.editor.java;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Handle that represent an {@link IndexedElement}. But as the {@link IndexedElement} is stateful
 * ant may keep the Document instance, this just uses the qualified name to identify the
 * real element.
 * <p>
 * Some items are created from Groovy indexes, so there's no real Element to create ElementHandle from,
 * so if an ElementHandle is available, the JavaElementHandle should be created on top of the real
 * java source APIs ElementHandle. Otherwise, the coordinates are computed from URL, qualified parent, simple
 * name kind and signature types.
 * 
 * @author sdedic
 */
public final class JavaElementHandle implements ElementHandle {
    /**
     * URL of the file where the handle was created; used to construct classpaths etc.
     */
    private final URL   anchorURL;
    
    /**
     * Name of the element
     */
    private final String name;

    /**
     * FQN of the outer/owner element.
     */
    private final String ownerFQN;
    
    /**
     * Kind of the element
     */
    private final ElementKind kind;
    
    /**
     * Signature info, for methods and ctors only
     */
    private final List<String> signatureInfo;
    
    private final Set<Modifier> modifiers;
    
    private final org.netbeans.api.java.source.ElementHandle elementHandle;
    
    public JavaElementHandle(String name, String ownerFQN, org.netbeans.api.java.source.ElementHandle h, List<String> signatureInfo, Set<Modifier> modifiers) {
        assert name != null : "simple name is needed";
        this.anchorURL = null;
        this.name = name;
        this.ownerFQN = ownerFQN;
        this.elementHandle = h;
        this.kind = fromJavaKind(h.getKind());
        this.signatureInfo = signatureInfo == null ? Collections.emptyList() : signatureInfo;
        this.modifiers = modifiers == null ? Collections.emptySet() : modifiers;
    }
    
    private static ElementKind fromJavaKind(javax.lang.model.element.ElementKind k) {
        switch (k) {
            case CLASS:         return ElementKind.CLASS;
            case INTERFACE:     return ElementKind.INTERFACE;
            case METHOD:        return ElementKind.METHOD;
            case FIELD:         return ElementKind.FIELD;
            case CONSTRUCTOR:   return ElementKind.CONSTRUCTOR;
            case ENUM:          return ElementKind.CONSTANT;
            case ENUM_CONSTANT: return ElementKind.CONSTANT;
            case ANNOTATION_TYPE: return ElementKind.INTERFACE;
                    
            default:
                throw new IllegalArgumentException("Unsupported: " + k);
        }
    }
    
    public  JavaElementHandle(URL anchorURL, String name, String ownerFQN, ElementKind kind, List<String> signatureInfo, Set<Modifier> modifiers) {
        assert anchorURL != null : "Need an anchor to resolve the handle in the future";
        assert name != null : "simple name is needed";
        assert kind != null;
        this.anchorURL = anchorURL;
        this.name = name;
        this.ownerFQN = ownerFQN;
        this.kind = kind;
        this.signatureInfo = signatureInfo == null ? Collections.emptyList() : signatureInfo;
        this.modifiers = modifiers == null ? Collections.emptySet() : modifiers;
        this.elementHandle = null;
    }
    
    @Override
    public FileObject getFileObject() {
        return URLMapper.findFileObject(anchorURL);
    }

    @Override
    public String getMimeType() {
        return "text/x-java"; // NOI18N
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIn() {
        return ownerFQN;
    }

    @Override
    public ElementKind getKind() {
        return kind;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        if (handle instanceof JavaElementHandle) {
            JavaElementHandle jeh = (JavaElementHandle)handle;
            if (jeh.elementHandle != null && elementHandle != null) {
                return elementHandle.signatureEquals(jeh.elementHandle);
            } else {
                return Objects.equals(jeh.signatureInfo, signatureInfo);
            }
        } else {
            return false;
        }
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        // for now, to be replaced by lazy-loaded range from resolved Element.
        return OffsetRange.NONE;
    }
    
    /**
     * Resolves the handle for the passed CompilationInfo
     * @param <T> element type
     * @param info compilation info instance
     * @return resolved element, or {@code null} if none.
     */
    public <T extends Element> @CheckForNull T resolve(@NonNull CompilationInfo info) {
        return (T)toElement(info);
    }
    
    /**
     * Extracts data from the element.
     * @param <T>
     * @param resolver
     * @return
     * @throws IOException 
     */
    public <T> T extract(ParserResult groovyResult, ElementFunction<T> resolver) throws IOException {
        FileObject origin = groovyResult.getSnapshot().getSource().getFileObject();
        if (origin == null) {
            if (anchorURL == null) {
                return null;
            }
            origin = URLMapper.findFileObject(anchorURL);
        } 
        ClasspathInfo cpi = ClasspathInfo.create(origin);
        
        class Processor implements Task<CompilationController>, ClasspathInfo.Provider {
            T result;
            Exception thrown;
            
            @Override
            public void run(CompilationController parameter) throws Exception {
                try {
                    parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    result = resolver.apply(parameter, toElement(parameter));
                } catch (Exception ex) {
                    thrown = ex;
                }
            }

            @Override
            public ClasspathInfo getClasspathInfo() {
                return cpi;
            }
        }

        Processor inst = new Processor();

        JavaSource.create(cpi).runUserActionTask(inst, true);
        if (inst.thrown != null) {
            throw new IOException(inst.thrown);
        } else {
            return inst.result;
        }
    }
    
    private Element toElement(CompilationInfo info) {
        if (elementHandle != null) {
            return elementHandle.resolve(info);
        }
        switch (kind) {
            case CLASS:
                return info.getElements().getTypeElement(name);

            case FIELD:
            case CONSTANT: {
                Element owner = info.getElements().getTypeElement(ownerFQN);
                if (owner == null) {
                    return null;
                }
                
                return ElementFilter.fieldsIn(owner.getEnclosedElements()).stream().
                        filter(f -> f.getSimpleName().contentEquals(name)).
                        findAny().orElse(null);
            }
                
            case METHOD:
            case CONSTRUCTOR: {
                Element owner = info.getElements().getTypeElement(ownerFQN);
                if (owner == null || !(owner.getKind().isClass() || owner.getKind().isInterface())) {
                    return null;
                }
                for (ExecutableElement e : (kind == ElementKind.METHOD ? ElementFilter.methodsIn(owner.getEnclosedElements()) : ElementFilter.constructorsIn(owner.getEnclosedElements()))) {
                    if (kind == ElementKind.METHOD && !e.getSimpleName().contentEquals(name)) {
                        continue;
                    }
                    List<String> sigTypes = new ArrayList<>();
                    for (VariableElement v : e.getParameters()) {
                        TypeMirror t = v.asType();
                        sigTypes.add(info.getTypeUtilities().getTypeName(t, TypeUtilities.TypeNameOptions.PRINT_FQN).toString());
                    }
                    if (sigTypes.equals(signatureInfo)) {
                        return e;
                    }
                }
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Processes the ElementHandle into the desired result. The processor's callback
     * is called within parser's action task, so it can get a valid {@link CompilationInfo}
     * and the resolved {@link Element} instance.
     * <p>
     * The implementation must not leak the {@code CompliationInfo} or process the Element instance
     * outside of the {@link #apply} call.
     * 
     * @param <T> 
     */
    @FunctionalInterface
    public interface ElementFunction<T> {
        /**
         * Produces the desired result from the Element. 
         * @param info compilation info 
         * @param el resolved element; can be {@code null}, if resolution fails.
         * @return info extracted from the element.
         */
        public @CheckForNull T apply(@NonNull CompilationInfo info, @NullAllowed Element el);
    }
}
