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

package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import java.io.IOException;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.util.Exceptions;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class SessionMethodController extends AbstractMethodController {

    private final MetadataModel<EjbJarMetadata> model;
    private final String sessionType;
    private final boolean allowsNoInterface;

    public SessionMethodController(final String ejbClass, MetadataModel<EjbJarMetadata> model) {
        this(ejbClass, model, false);
    }

    public SessionMethodController(final String ejbClass, MetadataModel<EjbJarMetadata> model, boolean allowsNoInterface) {
        super(ejbClass, model);
        this.model = model;
        this.allowsNoInterface = allowsNoInterface;
        String resultSessionType = null;
        try {
            resultSessionType = model.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws Exception {
                    Session session = (Session) metadata.findByEjbClass(ejbClass);
                    if (session != null) {
                        return session.getSessionType();
                    }
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        this.sessionType = resultSessionType;
    }

    @Override
    public boolean hasJavaImplementation(MethodModel intfView) {
        return true;
    }

    @Override
    public boolean hasJavaImplementation(MethodType methodType) {
        return true;
    }
    
    @Override
    public MethodType getMethodTypeFromImpl(MethodModel implView) {
        MethodType methodType = null;
        if (implView.getName().startsWith("ejbCreate")) {
            methodType = new MethodType.CreateMethodType(implView);
        } else if (!implView.getName().startsWith("ejb")) {
            methodType = new MethodType.BusinessMethodType(implView);
        }
        return methodType;
    }

    @Override
    public MethodType getMethodTypeFromInterface(MethodModel clientView) {
        // see if the interface is home or local home, otherwise assume business
        String localHome = getLocalHome();
        String home = getHome();
        if ((localHome != null && findInClass(localHome, clientView)) || (home != null && findInClass(home, clientView))) {
            return new MethodType.CreateMethodType(clientView);
        } else {
            return new MethodType.BusinessMethodType(clientView);
        }
    }

    public AbstractMethodController.GenerateFromImpl createGenerateFromImpl() {
        return new SessionGenerateFromImplVisitor();
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new SessionGenerateFromIntfVisitor();
    }

    @Override
    public boolean supportsMethodType(MethodType.Kind methodType) {
        boolean stateless = Session.SESSION_TYPE_STATELESS.equals(sessionType);
        return  methodType == MethodType.Kind.BUSINESS || (!isSimplified() && !stateless && (methodType == MethodType.Kind.CREATE));
    }

    @Override
    public boolean allowsNoInterface(){
        return allowsNoInterface;
    }
}
