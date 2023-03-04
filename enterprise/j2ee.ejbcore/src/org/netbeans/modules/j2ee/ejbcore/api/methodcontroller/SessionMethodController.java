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
