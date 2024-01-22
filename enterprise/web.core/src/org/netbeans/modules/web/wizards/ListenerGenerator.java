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
package org.netbeans.modules.web.wizards;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.openide.filesystems.FileObject;

import static org.netbeans.modules.web.wizards.ListenerPanel.HTTP_SESSION_ATTRIBUTE_LISTENER;
import static org.netbeans.modules.web.wizards.ListenerPanel.HTTP_SESSION_LISTENER;
import static org.netbeans.modules.web.wizards.ListenerPanel.SERVLET_CONTEXT_ATTRIBUTE_LISTENER;
import static org.netbeans.modules.web.wizards.ListenerPanel.SERVLET_CONTEXT_LISTENER;
import static org.netbeans.modules.web.wizards.ListenerPanel.SERVLET_REQUEST_ATTRIBUTE_LISTENER;
import static org.netbeans.modules.web.wizards.ListenerPanel.SERVLET_REQUEST_LISTENER;

/**
 * Generator for servlet listener class
 *
 * @author  milan.kuchtiak@sun.com
 * Created on March, 2004
 */
public class ListenerGenerator {

    private final boolean isContext;
    private final boolean isContextAttr;
    private final boolean isSession;
    private final boolean isSessionAttr;
    private final boolean isRequest;
    private final boolean isRequestAttr;

    private GenerationUtils gu;

    /**
     * Creates a new instance of ListenerGenerator
     *
     * @param isContext
     * @param isContextAttr
     * @param isSession
     * @param isSessionAttr
     * @param isRequest
     * @param isRequestAttr
     */
    public ListenerGenerator(boolean isContext, boolean isContextAttr, boolean isSession, boolean isSessionAttr, boolean isRequest, boolean isRequestAttr) {
        this.isContext = isContext;
        this.isContextAttr = isContextAttr;
        this.isSession = isSession;
        this.isSessionAttr = isSessionAttr;
        this.isRequest = isRequest;
        this.isRequestAttr = isRequestAttr;
    }

    public void generate(JavaSource clazz) throws IOException {
        Task<WorkingCopy> task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();

            gu = GenerationUtils.newInstance(workingCopy);
            for (Tree typeDecl : cut.getTypeDecls()) {
                if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                    Element e = workingCopy.getTrees().getElement(new TreePath(new TreePath(workingCopy.getCompilationUnit()), typeDecl));
                    if (e != null && e.getKind().isClass()) {
                        TypeElement te = (TypeElement) e;
                        ClassTree ct = (ClassTree) typeDecl;
                        workingCopy.rewrite(ct, generateInterfaces(workingCopy, te, ct, gu, clazz));
                    }
                }
            }
        };
        ModificationResult result = clazz.runModificationTask(task);
        result.commit();

//        if (isContext) addContextListenerMethods();
//        if (isContextAttr) addContextAttrListenerMethods();
//        if (isSession) addSessionListenerMethods();
//        if (isSessionAttr) addSessionAttrListenerMethods();
//        if (isRequest) addRequestListenerMethods();
//        if (isRequestAttr) addRequestAttrListenerMethods();
    }

    private ClassTree generateInterfaces(WorkingCopy wc, TypeElement te, ClassTree ct, GenerationUtils gu, JavaSource clazz) {
        ClassTree newClassTree = ct;

        List<String> ifList = new ArrayList<>();
        List<ExecutableElement> methods = new ArrayList<>();

        FileObject jakartaServletRequestListenerFo = clazz.getClasspathInfo()
                .getClassPath(ClasspathInfo.PathKind.COMPILE)
                .findResource("jakarta" + SERVLET_REQUEST_LISTENER.replace('.', '/') + ".class");

        String prefix = jakartaServletRequestListenerFo == null ? "javax" : "jakarta";

        if (isContext) {
            ifList.add(prefix + SERVLET_CONTEXT_LISTENER);
        }
        if (isContextAttr) {
            ifList.add(prefix + SERVLET_CONTEXT_ATTRIBUTE_LISTENER);
        }
        if (isSession) {
            ifList.add(prefix + HTTP_SESSION_LISTENER);
        }
        if (isSessionAttr) {
            ifList.add(prefix + HTTP_SESSION_ATTRIBUTE_LISTENER);
        }
        if (isRequest) {
            ifList.add(prefix + SERVLET_REQUEST_LISTENER);
        }
        if (isRequestAttr) {
            ifList.add(prefix + SERVLET_REQUEST_ATTRIBUTE_LISTENER);
        }
        for (String ifName : ifList) {
            newClassTree = gu.addImplementsClause(newClassTree, ifName);
            TypeElement typeElement = wc.getElements().getTypeElement(ifName);
            methods.addAll(ElementFilter.methodsIn(typeElement.getEnclosedElements()));
        }

        for (MethodTree t : GeneratorUtilities.get(wc).createAbstractMethodImplementations(te, methods)) {
            newClassTree = GeneratorUtilities.get(wc).insertClassMember(newClassTree, t);
        }

        return newClassTree;
    }
}
