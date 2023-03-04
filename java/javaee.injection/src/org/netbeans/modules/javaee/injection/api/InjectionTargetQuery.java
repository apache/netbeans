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

package org.netbeans.modules.javaee.injection.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Ask whether it is possible to use dependency injection in some class
 * @author Martin Adamek, Milan Kuchtiak
 */
public class InjectionTargetQuery {
    
    private static Lookup.Result<InjectionTargetQueryImplementation> implementations;
    /** Cache of all available InjectionTargetQueryImplementation instances. */
    private static List<InjectionTargetQueryImplementation> cache;

    private InjectionTargetQuery() {
    }
    
    /**
     * Decide if dependency injection can be used in given class
     * @param controller CompilationController related to JavaSource
     * @param typeElement class where annotated field or method should be inserted,
     * if null is provided, main public class from file is taken
     * @return true if any container or environment is able to inject resources in given class, false otherwise
     */
    public static boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (typeElement == null || controller==null) {
            throw new NullPointerException("Passed null to InjectionTargetQuery.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        for (InjectionTargetQueryImplementation elem : getInstances()) {
            if (elem.isInjectionTarget(controller, typeElement)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isInjectionTarget(FileObject fileObject, final String className) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] result = new boolean[] { false };
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                if (typeElement != null) {
                    result[0] = isInjectionTarget(controller, typeElement);
                }
            }
        }, true);
        return result[0];
    }
    
    /**
     * Decide if injected reference must be static in given class. 
     * For example, in application client injection can be used only in class with main method and all
     * injected fields must be static<br>
     * Implementation 
     * @param controller CompilationController related to JavaSource
     * @param typeElement class where annotated field or method should be inserted,rted,
     * if null is provided, main public class from file is taken
     * @return true if static reference is required in given class, false otherwise
     */
    public static boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        if (typeElement == null || controller==null) {
            throw new NullPointerException("Passed null to InjectionTargetQuery.isStaticReferenceRequired(CompilationController, TypeElement)"); // NOI18N
        }
        for (InjectionTargetQueryImplementation elem : getInstances()) {
            if (elem.isStaticReferenceRequired(controller, typeElement)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isStaticReferenceRequired(FileObject fileObject, final String className) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] result = new boolean[] { false };
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                if (typeElement != null) {
                    result[0] = isStaticReferenceRequired(controller, typeElement);
                }
            }
        }, true);
        return result[0];
    }
    
    private static synchronized List<InjectionTargetQueryImplementation> getInstances() {
        if (implementations == null) {
            implementations = Lookup.getDefault().lookup(new Lookup.Template<InjectionTargetQueryImplementation>(InjectionTargetQueryImplementation.class));
            implementations.addLookupListener(new LookupListener() {
                public void resultChanged (LookupEvent ev) {
                    synchronized (InjectionTargetQuery.class) {
                        cache = null;
                    }
                }});
        }
        if (cache == null) {
            cache = new ArrayList<InjectionTargetQueryImplementation>(implementations.allInstances());
        }
        return cache;
    }

}
