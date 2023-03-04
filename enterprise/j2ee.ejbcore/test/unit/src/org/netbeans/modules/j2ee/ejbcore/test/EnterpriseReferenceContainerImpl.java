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

package org.netbeans.modules.j2ee.ejbcore.test;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class EnterpriseReferenceContainerImpl implements EnterpriseReferenceContainer {

    private EjbReference remoteEjbReference;
    private String remoteEjbRefName;
    private FileObject remoteReferencingFile;
    private String remoteReferencingClass;
    private EjbReference localEjbReference;
    private String localEjbRefName;
    private FileObject localReferencingFile;
    private String localReferencingClass;

    public EnterpriseReferenceContainerImpl() {}

    public String addEjbReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        this.remoteEjbReference = ref;
        this.remoteEjbRefName = ejbRefName;
        this.remoteReferencingFile = referencingFile;
        this.remoteReferencingClass = referencingClass;
        return null;
    }

    public String addEjbLocalReference(EjbReference localRef, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        this.localEjbReference = localRef;
        this.localEjbRefName = ejbRefName;
        this.localReferencingFile = referencingFile;
        this.localReferencingClass = referencingClass;
        return null;
    }

    public String getServiceLocatorName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceLocatorName(String serviceLocator) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String addDestinationRef(MessageDestinationReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String addResourceRef(ResourceReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        return "testJndiName";
    }

    public String getLocalEjbRefName() {
        return localEjbRefName;
    }

    public EjbReference getLocalEjbReference() {
        return localEjbReference;
    }

    public String getLocalReferencingClass() {
        return localReferencingClass;
    }

    public FileObject getLocalReferencingFile() {
        return localReferencingFile;
    }

    public String getRemoteEjbRefName() {
        return remoteEjbRefName;
    }

    public EjbReference getRemoteEjbReference() {
        return remoteEjbReference;
    }

    public String getRemoteReferencingClass() {
        return remoteReferencingClass;
    }

    public FileObject getRemoteReferencingFile() {
        return remoteReferencingFile;
    }
    
}
