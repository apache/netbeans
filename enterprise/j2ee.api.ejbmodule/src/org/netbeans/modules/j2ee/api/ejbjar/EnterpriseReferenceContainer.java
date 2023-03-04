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

package org.netbeans.modules.j2ee.api.ejbjar;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Instances of this class should be supplied by projects to indicate that
 * enterprise resources (J2EE declarative resources such as DataSources,
 * Enterprise JavaBeans, and JMS queues and topics) can be used. This
 * class will be invoked to incorporate this resource into the J2EE project. 
 * This api is current experimental and subject to change.
 * @author Chris Webster
 */
public interface EnterpriseReferenceContainer {
    
    String addEjbReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException;
    
    String addEjbLocalReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException;

    /**
     * @return name of the service locator defined for this project or null
     * if service locator is not being used
     */
    String getServiceLocatorName();
    
    /**
     * set name of service locator fo this project. 
     * @param serviceLocator used in this project
     */
    void setServiceLocatorName(String serviceLocator) throws IOException;
    
    /**
     * Add given message destination reference into the deployment descriptor
     * @param ref to destination
     * @param referencingClass class using the destination
     * @return unique jndi name used in the deployment descriptor
     */
    String addDestinationRef(MessageDestinationReference ref, FileObject referencingFile, String referencingClass) throws IOException;

    /**
     * Add given resource reference into the deployment descriptor.
     * @param ref reference to resource used
     * @param referencingClass class which will use the resource
     * @return unique jndi name used in deployment descriptor
     */
    String addResourceRef(ResourceReference ref, FileObject referencingFile, String referencingClass) throws IOException;
    
}
