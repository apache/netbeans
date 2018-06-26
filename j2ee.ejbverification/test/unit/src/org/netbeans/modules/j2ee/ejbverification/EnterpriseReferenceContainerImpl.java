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

package org.netbeans.modules.j2ee.ejbverification;

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
