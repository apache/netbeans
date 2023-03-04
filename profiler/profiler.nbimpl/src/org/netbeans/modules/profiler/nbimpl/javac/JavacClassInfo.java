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
package org.netbeans.modules.profiler.nbimpl.javac;

import com.sun.source.tree.ClassTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavacClassInfo extends SourceClassInfo {
    private static final Logger LOG = Logger.getLogger(JavacClassInfo.class.getName());
    
    private ElementHandle<TypeElement> handle;
    private FileObject src;
    private ClasspathInfo cpInfo;
    private Reference<JavaSource> sourceRef;

    private JavacClassInfo(ElementHandle<TypeElement> eh) {
        super(getSimpleName(eh.getBinaryName()), eh.getBinaryName(), eh.getBinaryName().replace('.', '/')); // NOI18N
        handle = eh;
    }
    
    public JavacClassInfo(ElementHandle<TypeElement> eh, ClasspathInfo cpInfo) {
        this(eh);
        
        this.cpInfo = cpInfo;
    }
    
    public JavacClassInfo(ElementHandle<TypeElement> eh, CompilationController cc) {
        this(eh);
        
        this.cpInfo = cc.getClasspathInfo();
        sourceRef = new SoftReference<JavaSource>(cc.getJavaSource());
    }

    @Override
    public Set<SourceMethodInfo> getMethods(final boolean all) {
        final Set<SourceMethodInfo>[] rslt = new Set[1];
        if (handle != null) {
            try {
                getSource(false).runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED) == JavaSource.Phase.ELEMENTS_RESOLVED) {
                            rslt[0] = getMethods(cc, all);
                        }
                    }
                }, true);
            } catch (IllegalArgumentException e) {
                LOG.log(Level.WARNING, null, e);
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        return rslt[0] != null ? rslt[0] : Collections.EMPTY_SET;
    }

    @Override
    public Set<SourceClassInfo> getSubclasses() {
        final Set<SourceClassInfo> rslt = new HashSet<SourceClassInfo>();
        if (handle != null) {
            try {
                JavaSource s = getSource(true);
                if (s != null) {
                    for(ElementHandle<TypeElement> eh : ElementUtilitiesEx.findImplementors(s.getClasspathInfo(), handle)) {
                        rslt.add(new JavacClassInfo(eh));
                    }
                }
            } catch (IllegalArgumentException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        return rslt;
    }
    
    @Override
    public FileObject getFile() {
        ElementHandle<TypeElement> eh = handle;
        ClasspathInfo ci = cpInfo;
        synchronized(this) {
            if (src == null) {
                src = SourceUtils.getFile(eh, ci);
                if (src == null) {
                    String resName = eh.getBinaryName().replace('.', '/').concat(".class"); // NOI18N
                    src = ci.getClassPath(ClasspathInfo.PathKind.BOOT).findResource(resName);
                    if (src == null) {
                        src = ci.getClassPath(ClasspathInfo.PathKind.COMPILE).findResource(resName);
                        if (src == null) {
                            src = ci.getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(resName);
                        }
                    }
                }
            }
            return src;
        }
    }

    @Override
    public Set<SourceMethodInfo> getConstructors() {
        final Set<SourceMethodInfo> infos = new HashSet<SourceMethodInfo>();
        if (handle != null) {
            try {
                JavaSource s = getSource(false);
                if (s != null) {
                    s.runUserActionTask(new Task<CompilationController>() {

                        @Override
                        public void run(CompilationController cc) throws Exception {
                            if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED) == JavaSource.Phase.ELEMENTS_RESOLVED) {
                                TypeElement type = handle.resolve(cc);
                                if (type != null) {
                                    for (ExecutableElement method : ElementFilter.constructorsIn(type.getEnclosedElements())) {
                                        infos.add(new JavacMethodInfo(method, cc));
                                    }
                                }
                            }
                        }
                    }, true);
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        return infos;
    }

    @Override
    public Set<SourceClassInfo> getInnerClases() {
        final Set<SourceClassInfo> innerClasses = new HashSet<SourceClassInfo>();

        if (handle != null) {
            try {
                JavaSource s = getSource(false);
                if (s != null) {
                    s.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController cc)
                                throws Exception {
                            if (cc.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                                return;
                            }

                            TypeElement type = handle.resolve(cc);
                            if (type != null) {
                                List<TypeElement> elements = ElementFilter.typesIn(type.getEnclosedElements());

                                for (TypeElement element : elements) {
                                    innerClasses.add(new JavacClassInfo(ElementHandle.create(element), cc));
                                }

                                addAnonymousInnerClasses(cc, innerClasses);
                            }
                        }
                    }, true);
                }
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return innerClasses;
    }

    @Override
    public Set<SourceClassInfo> getInterfaces() {
        final Set<SourceClassInfo> ifcs = new HashSet<SourceClassInfo>();
        if (handle != null) {
            try {
                JavaSource s = getSource(false);
                if (s != null) {
                    s.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            TypeElement te = handle.resolve(cc);

                            Types t = cc.getTypes();
                            if (te != null) {
                                for(TypeMirror ifc : te.getInterfaces()) {
                                    TypeElement ife = (TypeElement)t.asElement(ifc);
                                    ifcs.add(new JavacClassInfo(ElementHandle.create(ife), cc));
                                }
                            }
                        }
                    }, true);
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
            }    
        }
        return ifcs;
    }

    @Override
    public SourceClassInfo getSuperType() {
        final SourceClassInfo[] rslt = new SourceClassInfo[1];
        
        if (handle != null) {
            try {
                JavaSource s = getSource(false);
                if (s != null) {
                    s.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController cc) throws Exception {
                            if (cc.toPhase(JavaSource.Phase.RESOLVED) == JavaSource.Phase.RESOLVED) {
                                TypeElement te = handle.resolve(cc);

                                if (te != null) {
                                    TypeMirror superTm = te.getSuperclass();
                                    if (superTm != null) {
                                        TypeElement superType = (TypeElement)cc.getTypes().asElement(superTm);
                                        
                                        if (superType != null) {
                                            rslt[0] = new JavacClassInfo(ElementHandle.create(superType), cc);
                                        }
                                    }
                                }
                            }
                        }
                    }, true);
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
            }    
        }
        return rslt[0];
    }
    
    private Set<SourceMethodInfo> getMethods(final CompilationController cc, final boolean all) {
        final Set<SourceMethodInfo> mis = new HashSet<SourceMethodInfo>();
        TypeElement te = handle.resolve(cc);
        if (te != null) {
            Set<ExecutableElement> methods = new HashSet<ExecutableElement>(ElementFilter.methodsIn(te.getEnclosedElements()));
            for (ExecutableElement method : ElementFilter.methodsIn(cc.getElements().getAllMembers(te))) {
                String parent = ElementUtilities.getBinaryName((TypeElement) method.getEnclosingElement());
                if (parent.equals(getQualifiedName()) || 
                    (all && 
                     !containsAny(method.getModifiers(), EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)) &&
                     !parent.equals(Object.class.getName()))) {
                    methods.add(method);
                }
            }
            for(ExecutableElement method : methods) {
                mis.add(new JavacMethodInfo(method, cc));
            }
        }
        return mis;
    }
    
    private static String getSimpleName(String qualName) {
        String name = qualName;
        int lastDot = name.lastIndexOf('.');
        if (lastDot > -1) {
            name = name.substring(lastDot + 1);
        }
        return name;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JavacClassInfo other = (JavacClassInfo) obj;
        if (this.handle != other.handle && (this.handle == null || !this.handle.equals(other.handle))) {
            return false;
        }
        synchronized(this) {
            if (this.src != null) {
                if (this.src != other.src && !this.src.equals(other.src)){
                    return false;
                }
            } 
        }
        if (this.cpInfo != null) {
            if (this.cpInfo != other.cpInfo && !this.cpInfo.toString().equals(other.cpInfo.toString())) { // ClassPath does not implement "equals()" method and as such ClasspathInfo does effectivly neither
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.handle != null ? this.handle.hashCode() : 0);
        hash = 89 * hash + (this.src != null ? this.src.hashCode() : 0);
        hash = 89 * hash + (this.cpInfo != null ? this.cpInfo.hashCode() : 0);
        return hash;
    }   

    private void addAnonymousInnerClasses(final CompilationController cc, final Set<SourceClassInfo> innerClasses)
            throws IOException {
        final int parentClassNameLength = getQualifiedName().length();

        cc.toPhase(JavaSource.Phase.RESOLVED);

        ErrorAwareTreePathScanner<Void, Void> scanner = new ErrorAwareTreePathScanner<Void, Void>() {

            @Override
            public Void visitClass(ClassTree node, Void v) {
                Element classElement = cc.getTrees().getElement(getCurrentPath());

                if ((classElement != null) && (classElement.getKind() == ElementKind.CLASS)) {
                    TypeElement innerClassElement = (TypeElement) classElement;
                    String className = ElementUtilities.getBinaryName(innerClassElement);

                    if (className.length() <= parentClassNameLength) {
                        className = "";
                    } else {
                        className = className.substring(parentClassNameLength);
                    }

                    if (isAnonymous(className)) {
                        innerClasses.add(new JavacClassInfo(ElementHandle.create(innerClassElement), cc));
                    }
                }

                super.visitClass(node, v);

                return null;
            }  
        };

        scanner.scan(cc.getCompilationUnit(), null);
    }
    
    private static <T> boolean containsAny(Set<T> superSet, Set<T> subSet) {
        Set<T> set = new HashSet<T>(superSet);
        
        return set.removeAll(subSet);
    }
    
    private synchronized JavaSource getSource(boolean allowSourceLess) {
        JavaSource jSrc = sourceRef != null ? sourceRef.get() : null;
        if (jSrc == null || (!allowSourceLess && jSrc.getFileObjects().isEmpty())) {
            FileObject f = getFile();
            if (f.getExt().equalsIgnoreCase("java") || f.getExt().equalsIgnoreCase("class")) { // NOI18N
                jSrc = cpInfo != null ? JavaSource.create(cpInfo, f) : JavaSource.forFileObject(f);
            } else if (cpInfo != null) {
                jSrc = JavaSource.create(cpInfo);
            }
            sourceRef = new SoftReference(jSrc);
        }
        return jSrc;
    }
}
