/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
