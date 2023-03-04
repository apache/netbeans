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

package org.netbeans.modules.spring.java;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.beans.utils.ElementSeekerTask;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class JavaUtils {

    private JavaUtils() {
        
    }

    public static Collection<ExecutableElement> getMethodsFromHandles(CompilationInfo ci, Collection<ElementHandle<ExecutableElement>> methodHandles) {
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>(methodHandles.size());
        for(ElementHandle<ExecutableElement> handle : methodHandles) {
            ExecutableElement ee = handle.resolve(ci);
            if(ee != null) {
                methods.add(ee);
            }
        }
        
        return methods;
    }
    
    private static final String GET_PREFIX = "get"; // NOI18N
    private static final String SET_PREFIX = "set"; // NOI18N
    private static final String IS_PREFIX = "is"; // NOI18N
    
    public static boolean isGetter(ExecutableElement ee) {
        String methodName = ee.getSimpleName().toString();
        TypeMirror retType = ee.getReturnType();

        // discard private and static methods
        if (ee.getModifiers().contains(Modifier.PRIVATE) || ee.getModifiers().contains(Modifier.STATIC)) {
            return false;
        }
        
        
        boolean retVal = methodName.startsWith(GET_PREFIX) && methodName.length() > GET_PREFIX.length() && retType.getKind() != TypeKind.VOID;
        retVal = retVal || methodName.startsWith(IS_PREFIX) && methodName.length() > IS_PREFIX.length() && retType.getKind() == TypeKind.BOOLEAN;
        
        return retVal;
    }
    
    public static boolean isSetter(ExecutableElement ee) {
        String methodName = ee.getSimpleName().toString();
        TypeMirror retType = ee.getReturnType();
        
        // discard private and static methods
        if (ee.getModifiers().contains(Modifier.PRIVATE) || ee.getModifiers().contains(Modifier.STATIC)) {
            return false;
        }
        
        return methodName.startsWith(SET_PREFIX) && methodName.length() > SET_PREFIX.length() 
                && retType.getKind() == TypeKind.VOID && ee.getParameters().size() == 1;
    }
    
    public static String getPropertyName(String methodName) {
        if(methodName == null) {
            return null;
        }
        
        if(methodName.startsWith(GET_PREFIX) || methodName.startsWith(SET_PREFIX)) {
            String substring = methodName.substring(3);
            if (!"".equals(substring)) {
                return convertToPropertyName(substring);
            }
        } else if(methodName.startsWith(IS_PREFIX)) {
            String substring = methodName.substring(2);
            if (!"".equals(substring)) {
                return convertToPropertyName(substring);
            }
        }
        
        return null;
    }
    
    private static String convertToPropertyName(String name) {
        char[] vals = name.toCharArray();
        vals[0] = Character.toLowerCase(vals[0]);
        return String.valueOf(vals);
    }
    
    public static Collection<ElementHandle<ExecutableElement>> getOverridenMethodsAsHandles(ExecutableElement e, CompilationInfo info) {
        Collection<ExecutableElement> methods = getOverridenMethods(e, info);
        if(methods.isEmpty()) {
            return Collections.emptyList();
        }
        
        Collection<ElementHandle<ExecutableElement>> handles = new ArrayList<ElementHandle<ExecutableElement>>(methods.size());
        for(ExecutableElement ee : methods) {
            handles.add(ElementHandle.create(ee));
        }
        
        return handles;
    }
    
    public static Collection<ExecutableElement> getOverridenMethods(ExecutableElement e, CompilationInfo info) {
        return getOverridenMethods(e, SourceUtils.getEnclosingTypeElement(e), info);
    }

    private static Collection<ExecutableElement> getOverridenMethods(ExecutableElement e, TypeElement parent, CompilationInfo info) {
        ArrayList<ExecutableElement> result = new ArrayList<ExecutableElement>();
        
        TypeMirror sup = parent.getSuperclass();
        if (sup.getKind() == TypeKind.DECLARED) {
            TypeElement next = (TypeElement) ((DeclaredType)sup).asElement();
            ExecutableElement overriden = getMethod(e, next, info);
                result.addAll(getOverridenMethods(e,next, info));
            if (overriden!=null) {
                result.add(overriden);
            }
        }
        for (TypeMirror tm:parent.getInterfaces()) {
            TypeElement next = (TypeElement) ((DeclaredType)tm).asElement();
            ExecutableElement overriden2 = getMethod(e, next, info);
            result.addAll(getOverridenMethods(e,next, info));
            if (overriden2!=null) {
                result.add(overriden2);
            }
        }
        return result;
    }    
    
    private static ExecutableElement getMethod(ExecutableElement method, TypeElement type, CompilationInfo info) {
        for (ExecutableElement met: ElementFilter.methodsIn(type.getEnclosedElements())){
            if (info.getElements().overrides(method, met, type)) {
                return met;
            }
        }
        return null;
    }

    @NbBundle.Messages("JavaUtils.title.method.searching=Searching Method")
    public static ElementHandle<ExecutableElement> findMethod(FileObject fileObject, final String classBinName,
            final String methodName, int argCount, Public publicFlag, Static staticFlag) {
        JavaSource js = JavaUtils.getJavaSource(fileObject);
        if (js != null) {
            MethodFinder methodFinder = new MethodFinder(js, classBinName, methodName, argCount, publicFlag, staticFlag);
            methodFinder.runAsUserTask();
            if (methodFinder.getMethodHandle() == null && SourceUtils.isScanInProgress()) {
                if (!ScanDialog.runWhenScanFinished(methodFinder, Bundle.JavaUtils_title_method_searching())) {
                    return methodFinder.getMethodHandle();
                } else {
                    return null;
                }
            } else {
                return methodFinder.getMethodHandle();
            }
        }

        return null;
    }
    
    public static JavaSource getJavaSource(FileObject fileObject) {
        if (fileObject == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return null;
        }
        // XXX this only works correctly with projects with a single sourcepath,
        // but we don't plan to support another kind of projects anyway (what about Maven?).
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            return JavaSource.create(ClasspathInfo.create(sourceGroup.getRootFolder()));
        }
        return null;
    }
    
    public static JavaSource getJavaSource(Document doc) {
        return getJavaSource(NbEditorUtilities.getFileObject(doc));
    }
    
    public static TypeElement findClassElementByBinaryName(final String binaryName, CompilationController cc) {
        if (!binaryName.contains("$")) { // NOI18N
            // fast search based on fqn
            return cc.getElements().getTypeElement(binaryName);
        } else {
            // get containing package
            String packageName = ""; // NOI18N
            int dotIndex = binaryName.lastIndexOf("."); // NOI18N
            if (dotIndex != -1) {
                packageName = binaryName.substring(0, dotIndex);
            }
            PackageElement packElem = cc.getElements().getPackageElement(packageName);
            if (packElem == null) {
                return null;
            }

            // scan for element matching the binaryName
            return new BinaryNameTypeScanner().visit(packElem, binaryName);
        }
    }
    
    /**
     * Open the specified method of the specified class in the editor
     * 
     * @param doc The document on from which the java model context is to be created
     * @param classBinName binary name of the class whose method is to be opened in the editor
     * @param methodName name of the method
     * @param argCount number of arguments that the method has (-1 if caller doesn't care)
     * @param publicFlag YES if the method is public, NO if not, DONT_CARE if caller doesn't care
     * @param staticFlag YES if the method is static, NO if not, DONT_CARE if caller doesn't care
     */
    public static void openMethodInEditor(FileObject fileObject, final String classBinName,
            final String methodName, int argCount, Public publicFlag, Static staticFlag) {
        if (classBinName == null || methodName == null || fileObject == null) {
            return;
        }

        final JavaSource js = JavaUtils.getJavaSource(fileObject);
        if (js == null) {
            return;
        }

        final ElementHandle<ExecutableElement> eh = JavaUtils.findMethod(fileObject, classBinName, methodName, argCount, publicFlag, staticFlag);
        if (eh != null) {
            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        ExecutableElement ee = eh.resolve(cc);
                        ElementOpen.open(js.getClasspathInfo(), ee);
                    }
                }, true);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    @NbBundle.Messages("JavaUtils.title.class.searching=Searching Class...")
    public static void findAndOpenJavaClass(final String classBinaryName, FileObject fileObject) {
        if (classBinaryName == null || fileObject == null) {
            return;
        }

        final JavaSource js = JavaUtils.getJavaSource(fileObject);
        if (js != null) {
            final AtomicBoolean cancel = new AtomicBoolean(false);
            final ClassFinder classFinder = new ClassFinder(js, classBinaryName, cancel);
            if (SourceUtils.isScanInProgress()) {
                ScanDialog.runWhenScanFinished(new Runnable() {
                    @Override
                    public void run() {
                        ProgressUtils.runOffEventDispatchThread(classFinder, Bundle.JavaUtils_title_class_searching(), cancel, false);
                    }
                }, Bundle.JavaUtils_title_class_searching());
            } else {
                ProgressUtils.runOffEventDispatchThread(classFinder, Bundle.JavaUtils_title_class_searching(), cancel, false);
            }
        }
    }

    private static final class ClassFinder extends ElementSeekerTask {

        private final String classBinaryName;
        private final AtomicBoolean cancel;

        public ClassFinder(JavaSource javaSource, String classBinaryName, AtomicBoolean cancel) {
            super(javaSource);
            this.classBinaryName = classBinaryName;
            this.cancel = cancel;
        }

        @Override
        public void run(CompilationController controller) throws Exception {
            if (cancel.get()) { return; }
            boolean opened = false;
            if (classBinaryName == null) {
                return;
            }
            TypeElement element = JavaUtils.findClassElementByBinaryName(classBinaryName, controller);
            if (element != null) {
                elementFound.set(true);
                if (cancel.get()) { return; }
                opened = ElementOpen.open(javaSource.getClasspathInfo(), element);
            }
            if (!opened) {
                String msg = NbBundle.getMessage(JavaUtils.class, "LBL_SourceNotFound", classBinaryName); //NOI18N
                StatusDisplayer.getDefault().setStatusText(msg);
            }
        }
    }
    
    private static final class MethodFinder extends ElementSeekerTask {

        private String classBinName;
        private String methodName;
        private int argCount;
        private Public publicFlag;
        private Static staticFlag;
        private ElementHandle<ExecutableElement> methodHandle;

        public MethodFinder(JavaSource javaSource, String classBinName, String methodName, int argCount, Public publicFlag, Static staticFlag) {
            super(javaSource);
            this.classBinName = classBinName;
            this.methodName = methodName;
            this.argCount = argCount;
            this.publicFlag = publicFlag;
            this.staticFlag = staticFlag;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement element = findClassElementByBinaryName(classBinName, cc);
            while (element != null) {
                List<ExecutableElement> methods = ElementFilter.methodsIn(element.getEnclosedElements());
                for (ExecutableElement method : methods) {
                    // name match
                    String mName = method.getSimpleName().toString();
                    if (!mName.equals(methodName)) {
                        continue;
                    }

                    // argument match
                    if (this.argCount != -1) {
                        int actualArgCount = method.getParameters().size();
                        if (actualArgCount != argCount) {
                            continue;
                        }
                    }

                    // static match
                    if (staticFlag != Static.DONT_CARE) {
                        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                        if ((isStatic && staticFlag == Static.NO) || (!isStatic && staticFlag == Static.YES)) {
                            continue;
                        }
                    }

                    // public match
                    if (publicFlag != Public.DONT_CARE) {
                        boolean isPublic = method.getModifiers().contains(Modifier.PUBLIC);
                        if ((isPublic && publicFlag == Public.NO) || (!isPublic && publicFlag == Public.YES)) {
                            continue;
                        }
                    }

                    // found!
                    this.methodHandle = ElementHandle.create(method);
                    return;
                }

                TypeMirror superClassMirror = element.getSuperclass();
                if (superClassMirror instanceof DeclaredType) {
                    DeclaredType declaredType = (DeclaredType) superClassMirror;
                    Element elem = declaredType.asElement();
                    if (elem.getKind() == ElementKind.CLASS) {
                        element = (TypeElement) elem;
                    }
                } else {
                    element = null;
                }
            }
        }

        public ElementHandle<ExecutableElement> getMethodHandle() {
            return this.methodHandle;
        }
    }
    
    private static class BinaryNameTypeScanner extends SimpleElementVisitor6<TypeElement, String> {

        @Override
        public TypeElement visitPackage(PackageElement packElem, String binaryName) {
            for(Element e : packElem.getEnclosedElements()) {
                if(e.getKind().isClass()) {
                    TypeElement ret = e.accept(this, binaryName);
                    if(ret != null) {
                        return ret;
                    }
                }
            }
            
            return null;
        }

        @Override
        public TypeElement visitType(TypeElement typeElement, String binaryName) {
            String bName = ElementUtilities.getBinaryName(typeElement);
            if(binaryName.equals(bName)) {
                return typeElement;
            } else if(binaryName.startsWith(bName)) {
                for(Element child : typeElement.getEnclosedElements()) {
                    if(!child.getKind().isClass()) {
                        continue;
                    }
                    
                    TypeElement retVal = child.accept(this, binaryName);
                    if(retVal != null) {
                        return retVal;
                    }
                }
            }
            
            return null;
        }
    }
}
