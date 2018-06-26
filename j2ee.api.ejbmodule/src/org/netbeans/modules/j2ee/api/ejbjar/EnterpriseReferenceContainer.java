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
