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
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This class encapsulates functionality required for working with EJB methods.
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class EjbMethodController {
    
    private enum EjbType { SESSION, ENTITY, ERROR }
    
    public static EjbMethodController createFromClass(FileObject ejbClassFO, final String className) {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(ejbClassFO);
        if (ejbModule == null) {
            return null;
        }
        EjbMethodController controller = null;
        try {
            MetadataModel<EjbJarMetadata> model = ejbModule.getMetadataModel();
            EjbType ejbType = model.runReadAction(new MetadataModelAction<EjbJarMetadata, EjbType>() {
                public EjbType run(EjbJarMetadata metadata) throws Exception {
                    Ejb ejb = metadata.findByEjbClass(className);
                    if (ejb == null) {
                        return EjbType.ERROR;
                    } else if (ejb instanceof Session) {
                        return EjbType.SESSION;
                    } else if (ejb instanceof Entity) {
                        return EjbType.ENTITY;
                    } else {
                        return EjbType.ERROR;
                    }
                }
            });
            if (ejbType == EjbType.SESSION) {
                boolean allowsNoInterface = false;
                Project project = FileOwnerQuery.getOwner(ejbClassFO);
                if (project != null){
                    J2eeProjectCapabilities projectCap = J2eeProjectCapabilities.forProject(project);
                    allowsNoInterface = projectCap != null ? projectCap.isEjb31LiteSupported() : false;
                }

                controller = new SessionMethodController(className, model, allowsNoInterface);
                // TODO EJB3: on Java EE 5.0 this always sets controller to null
                if (!controller.allowsNoInterface() && !controller.hasLocal() && !controller.hasRemote()) {
                    // this is either an error or a web service 
                    controller = null;
                }
            } else if (ejbType == EjbType.ENTITY) {
                controller = new EntityMethodController(className, model);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return controller;
    }
    
    /**
     * Find the implementation methods
     * @return MethodElement representing the implementation method or null.
     */
    public abstract List getImplementation(MethodModel intfView);
    public abstract MethodModel getPrimaryImplementation(MethodModel intfView);
    /**
     * @return true if intfView has a java implementation.
     */
    public abstract boolean hasJavaImplementation(MethodModel intfView);
    public abstract boolean hasJavaImplementation(MethodType methodType);
    
    /**
     * return interface method in the requested interface. 
     * @param beanImpl implementation method
     * @param local true if local method should be returned false otherwise
     */
    public abstract ClassMethodPair getInterface(MethodModel beanImpl, boolean local);
    
    /** Return if the passed method is implementation of method defined 
     * in local or remote interface.
     * @param m Method from bean class.
     * @param methodType Type of method to define the search algorithm
     * @param local If <code>true</code> the local interface is searched,
     *              if <code>false</code> the remote interface is searched.
     */
    public abstract boolean hasMethodInInterface(MethodModel method, MethodType methodType, boolean local);
    
    /**
     * @param clientView of the method
     */
    public abstract MethodType getMethodTypeFromInterface(MethodModel clientView);
    public abstract MethodType getMethodTypeFromImpl(MethodModel implView);
    
    public abstract String getBeanClass();
    public abstract String getLocal();
    public abstract String getRemote();
    public abstract Collection<String> getLocalInterfaces();
    public abstract Collection<String> getRemoteInterfaces();
    public abstract boolean hasLocal();
    public abstract boolean hasRemote();
    public boolean allowsNoInterface(){
        return false;
    }
    public void addEjbQl(MethodModel clientView, String ejbql, FileObject ddFileObject) throws IOException {
        assert false: "ejbql not supported for this bean type";
    }
    
    public String createDefaultQL(MethodModel methodModel) {
        return null;
    }
    
    /**
     * create interface signature based on the given implementation
     */
    public abstract void createAndAddInterface(MethodModel beanImpl, boolean local);
    
    /**
     * create implementation methods based on the client method. 
     * @param clientView method which will be inserted into an interface
     * @param intf interface where element will be inserted. This can be the
     * use the business interface pattern.
     */
    public abstract void createAndAddImpl(MethodModel clientView);
    
    public abstract void delete(MethodModel classMethod);
    public abstract void delete(MethodModel interfaceMethod, boolean local);
    
    /** Checks if given method type is supported by controller.
     * @param type One of <code>METHOD_</code> constants in @link{MethodType}
     */
    public abstract boolean supportsMethodType(MethodType.Kind type);
    public abstract MethodModel createAndAdd(MethodModel clientView, boolean local, boolean component);
    
    
    /** Immutable type representing method and its enclosing class */
    protected static final class ClassMethodPair {
        
        private final String className;
        private final MethodModel methodModel;
        
        public ClassMethodPair(String className, MethodModel methodModel) {
            this.className = className;
            this.methodModel = methodModel;
        }
        
        public String getClassName() {
            return className;
        }
        
        public MethodModel getMethodModel() {
            return methodModel;
        }
        
    }
}
