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

package org.netbeans.modules.j2ee.ejbcore;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation.class)
public class EjbInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public EjbInjectionTargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(controller.getFileObject());
        final String fqn = typeElement.getQualifiedName().toString();
        if (ejbModule != null && 
                !Profile.J2EE_13.equals(ejbModule.getJ2eeProfile()) &&
                !Profile.J2EE_14.equals(ejbModule.getJ2eeProfile())) {
            boolean isEjb = false;
            try {
                 isEjb = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Boolean>() {
                    public Boolean run(EjbJarMetadata metadata) throws Exception {
                        return metadata.findByEjbClass(fqn) != null;
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return isEjb;
        }
        return false;
    }

    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
    
}
