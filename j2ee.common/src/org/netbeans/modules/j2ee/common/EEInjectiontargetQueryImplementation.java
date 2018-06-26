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

package org.netbeans.modules.j2ee.common;

import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Realization for all common ee injectable targets
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation.class)
public class EEInjectiontargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    /** Creates a new instance of EEInjectiontargetQueryImplementation */
    public EEInjectiontargetQueryImplementation() {
    }
    
    @Override
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (controller == null || typeElement==null) {
            throw new NullPointerException("Passed null to EEInjectiontargetQueryImplementation.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        FileObject fo = controller.getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        J2eeProjectCapabilities j2eeProjectCapabilities = J2eeProjectCapabilities.forProject(project);
        if (j2eeProjectCapabilities == null) {
            return false;
        }
        boolean ejb31 = j2eeProjectCapabilities.isEjb31Supported() || j2eeProjectCapabilities.isEjb31LiteSupported();//it's foe ee6 only or common annotations 1.1

        if (ejb31 && !(ElementKind.INTERFACE==typeElement.getKind())) {
            
            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
            boolean found = false;

            for (AnnotationMirror m : annotations) {
                Name qualifiedName = ((TypeElement)m.getAnnotationType().asElement()).getQualifiedName();
                if (qualifiedName.contentEquals("javax.annotation.ManagedBean")) { //NOI18N
                    found = true;
                    break;
                }
            }
            if (found) return true;
        }
        return false;
    }
    
    @Override
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
}
