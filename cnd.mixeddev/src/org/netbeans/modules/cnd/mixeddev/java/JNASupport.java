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

package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.COMMA;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.LPAREN;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.RPAREN;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.stringize;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.transform;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.createMethodInfo;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.isInterface;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.isMethod;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;

/**
 *
 */
public final class JNASupport {
    
    public static JavaEntityInfo getJNAEntity(Document doc, int offset) {
        return getJNAEntity(doc, offset, false);
    }
    
    public static JavaEntityInfo getJNAEntity(Document doc, int offset, boolean immediately) {
        return JavaContextSupport.resolveContext(doc, new ResolveJNAEntityTask(offset), immediately);
    }
    
    public static String getCppMethodSignature(JavaMethodInfo methodInfo) {
        if (methodInfo == null) {
            return null;
        }
        StringBuilder method = new StringBuilder();
        
        // Add method name
        method.append(methodInfo.getName());
        
        return method.toString();
    }
    
//<editor-fold defaultstate="collapsed" desc="Implementation">    
    private static class ResolveJNAEntityTask implements ResolveJavaContextTask<JavaEntityInfo> {
        
        private final int offset;
        
        private JavaEntityInfo result;
        
        public ResolveJNAEntityTask(int offset) {
            this.offset = offset;
        }
        
        @Override
        public boolean hasResult() {
            return result != null;
        }
        
        @Override
        public JavaEntityInfo getResult() {
            return result;
        }
        
        @Override
        public void cancel() {
            // Do nothing
        }
        
        @Override
        public void run(CompilationController controller) throws Exception {
            if (controller == null || controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                return;
            }
            // Looking for current element
            TreePath path = controller.getTreeUtilities().pathFor(offset);
            if (isMethod(path)) {
                TreePath parentPath = path.getParentPath();
                if (isInterface(parentPath)) {
                    if (findLibraryInterface(controller, parentPath)) {
                        result = createMethodInfo(controller, path);
                    }
                }
            }
        }
    }
    
    private static final String JNA_LIBRARY = "com.sun.jna.Library"; // NOI18N
    
    private static boolean findLibraryInterface(CompilationController controller, TreePath ifacePath) {
        TypeElement ifaceElement = (TypeElement) controller.getTrees().getElement(ifacePath);
        Set<String> handledEntities = new HashSet<String>();
        return findLibraryInterface(controller, ifaceElement, handledEntities);
    }
    
    private static boolean findLibraryInterface(CompilationController controller, TypeElement baseIfaceElement, Set<String> handled) {
        for (TypeMirror iface : baseIfaceElement.getInterfaces()) {
            if (iface.getKind() == TypeKind.DECLARED) {
                TypeElement ifaceElement = (TypeElement)((DeclaredType) iface).asElement();
                String qualifiedName = ifaceElement.getQualifiedName().toString();
                if (!handled.contains(qualifiedName)) {
                    handled.add(qualifiedName);
                    if (!JNA_LIBRARY.equals(qualifiedName)) {
                        return findLibraryInterface(controller, ifaceElement, handled);
                    } else {
                        return true;
                    }
                }
            }
        }
        return false; // this is not JNA Library        
    }
    
    private JNASupport() {
        throw new AssertionError("Not instantiable!"); // NOI18N
    }
//</editor-fold>
}
