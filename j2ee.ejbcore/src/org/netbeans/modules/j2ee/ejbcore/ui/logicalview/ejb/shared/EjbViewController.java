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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.io.IOException;
import java.util.*;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.spi.ejbjar.support.EjbReferenceSupport;
import org.openide.util.Exceptions;

/**
 * This class provides controller capabilities for ejb logical views. The nodes
 * should delegate non ui tasks to this class.
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EjbViewController {

    private final String ejbClass;
    private final org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule;
    private String displayName;
    private ClasspathInfo cpInfo;

    public EjbViewController(String ejbClass, org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule) {
        this.ejbClass = ejbClass;
        this.ejbModule = ejbModule;

        FileObject[] javaSources = ejbModule.getJavaSources();
        if (javaSources.length > 0) {
            cpInfo = ClasspathInfo.create(
                    ClassPath.getClassPath(javaSources[0], ClassPath.BOOT),
                    ClassPath.getClassPath(javaSources[0], ClassPath.COMPILE),
                    ClassPath.getClassPath(javaSources[0], ClassPath.SOURCE)
                    );
        }
    }

    public String getDisplayName() {
        try {
            displayName = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws IOException {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    if (ejb == null){
                        return null;
                    }
                    String name = ejb.getDefaultDisplayName();
                    if (name == null) {
                        name = ejb.getEjbName();
                    }
                    return name;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return displayName;
    }

    public void delete(boolean deleteClasses) throws IOException {

        Profile profile = ejbModule.getJ2eeProfile();
        boolean isEE5orEE6or7 = Profile.JAVA_EE_5.equals(profile) ||
                             Profile.JAVA_EE_6_FULL.equals(profile) ||
                             Profile.JAVA_EE_6_WEB.equals(profile) ||
                             Profile.JAVA_EE_7_FULL.equals(profile) ||
                             Profile.JAVA_EE_7_WEB.equals(profile);

        if (!isEE5orEE6or7) {
            ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) throws Exception {
                    EjbJar ejbJar = metadata.getRoot();
                    EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
                    // XXX get project (from EjbJar)
//                    J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
//                    j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
//                    Ejb ejb = metadata.findByEjbClass(ejbClass);
//                    deleteTraces(ejb, ejbJar);
//                    // for MDBs remove message destination from assembly descriptor
//                    if (ejb instanceof MessageDriven) {
//                        try {
//                            AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
//                            String mdLinkName = ((MessageDriven) ejb).getMessageDestinationLink();
//                            MessageDestination[] messageDestinations = assemblyDescriptor.getMessageDestination();
//                            for (MessageDestination messageDestination : messageDestinations) {
//                                if (messageDestination.getMessageDestinationName().equals(mdLinkName)) {
//                                    assemblyDescriptor.removeMessageDestination(messageDestination);
//                                    break;
//                                }
//                            }
//                        } catch (VersionNotSupportedException ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                    beans.removeEjb(ejb);
                    return null;
                }
            });
            writeDD();
            if (deleteClasses) {
                deleteClasses();
            }
        } else {
            deleteClasses();
        }
    }

    public EjbReference createEjbReference() throws IOException {
        return EjbReferenceSupport.createEjbReference(ejbModule, ejbClass);
    }

    public String getEjbClass(){
        return ejbClass;
    }

    public org.netbeans.modules.j2ee.api.ejbjar.EjbJar getEjbModule(){
        return ejbModule;
    }

    public ClasspathInfo getClasspathInfo(){
        return cpInfo;
    }

    public DataObject getBeanDo() {
        return getDataObject(ejbClass);
    }

    public FileObject getBeanFo() {
        return findFileObject(ejbClass);
    }

    public DataObject getDataObject(String className) {
        FileObject src = findFileObject(className);
        try {
            if (src != null) {
                return DataObject.find(src);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public ElementHandle<TypeElement> getBeanClass() {
        return getBeanClass(ejbClass);
    }

    public ElementHandle<TypeElement> getBeanClass(String qualifiedClassName) {
        Set<ElementHandle<TypeElement>> elementHandles = cpInfo.getClassIndex().getDeclaredTypes(
                getSimpleName(qualifiedClassName),
                NameKind.SIMPLE_NAME,
                new HashSet(Arrays.asList(SearchScope.SOURCE, SearchScope.DEPENDENCIES)));
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            if (qualifiedClassName.equals(elementHandle.getQualifiedName())) {
                return elementHandle;
            }
        }
        return null;
    }

    /**
     * gets an ejb reference representation
     * @return the xml code corresponding to this ejb
     */
    public String getRemoteStringRepresentation(final String ejbType) {
        String result = null;
        try {
            result = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws IOException {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    if (ejb == null){
                        return null;
                    }
                    assert ejb instanceof EntityAndSession;
                    EntityAndSession refModel = (EntityAndSession) ejb;
                    return "\t<ejb-ref>\n" +
                            "\t\t<ejb-ref-name>ejb/" + ejb.getEjbName() + "</ejb-ref-name>\n"+
                            "\t\t<ejb-ref-type>" + ejbType + "</ejb-ref-type>\n"+
                            "\t\t<home>" + refModel.getHome() + "</home>\n"+
                            "\t\t<remote>" + refModel.getRemote() + "</remote>\n"+
                            "\t</ejb-ref>\n";
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result;
    }

    public String getLocalStringRepresentation(final String ejbType) {
        String result = null;
        try {
            result = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws IOException {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    if (ejb == null){
                        return null;
                    }
                    assert ejb instanceof EntityAndSession;
                    EntityAndSession refModel = (EntityAndSession) ejb;
                    return "\t<ejb-local-ref>\n" +
                            "\t\t<ejb-ref-name>ejb/" + ejb.getEjbName() + "</ejb-ref-name>\n"+
                            "\t\t<ejb-ref-type>" + ejbType + "</ejb-ref-type>\n"+
                            "\t\t<local-home>" + refModel.getLocalHome() + "</local-home>\n"+
                            "\t\t<local>" + refModel.getLocal() + "</local>\n"+
                            "\t</ejb-local-ref>\n";
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result;
    }

    private String getSimpleName(String qualifiedClassName) {
        int beginIndex = qualifiedClassName.lastIndexOf('.') + 1; //NOI18N
        return qualifiedClassName.substring(beginIndex);
    }

    private void writeDD() throws IOException {
        FileObject ddFile = ejbModule.getDeploymentDescriptor();
        DDProvider.getDefault().getDDRoot(ddFile).write(ddFile); // EJB 2.1
    }

    private boolean isEjbUsed(EjbRelationshipRole role, String ejbName) {
        return role != null &&
                role.getRelationshipRoleSource() != null &&
                ejbName.equals(role.getRelationshipRoleSource().getEjbName());
    }

    private void deleteRelationships(String ejbName, EjbJar ejbJar) {
        Relationships relationships = ejbJar.getSingleRelationships();
        if (relationships != null) {
            EjbRelation[] relations = relationships.getEjbRelation();
            if (relations != null) {
                for (EjbRelation ejbRelation : relations) {
                    if (isEjbUsed(ejbRelation.getEjbRelationshipRole(), ejbName) || isEjbUsed(ejbRelation.getEjbRelationshipRole2(), ejbName)) {
                        relationships.removeEjbRelation(ejbRelation);
                    }
                }
                if (relationships.sizeEjbRelation() == 0) {
                    ejbJar.setRelationships(null);
                }
            }
        }
    }

    private void deleteTraces(Ejb ejb, EjbJar ejbJar) {
        String ejbName = ejb.getEjbName();
        String ejbNameCompare = ejbName + "";
        deleteRelationships(ejbName, ejbJar);
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor != null) {
            ContainerTransaction[] containerTransactions = assemblyDescriptor.getContainerTransaction();
            for (ContainerTransaction containerTransaction : containerTransactions) {
                Method[] methods = containerTransaction.getMethod();
                methods = methods == null ? new Method[0] : methods;
                for (Method method : methods) {
                    if (ejbNameCompare.equals(method.getEjbName())) {
                        containerTransaction.removeMethod(method);
                        if (containerTransaction.sizeMethod() == 0) {
                            assemblyDescriptor.removeContainerTransaction(containerTransaction);
                        }
                    }
                }
            }
            MethodPermission[] permissions = assemblyDescriptor.getMethodPermission();
            for (int i =0; i < permissions.length; i++) {
                Method[] methods = permissions[i].getMethod();
                methods = methods== null ? new Method[0]:methods;
                for (int method =0; method < methods.length; method++) {
                    if (ejbNameCompare.equals(methods[method].getEjbName())) {
                        permissions[i].removeMethod(methods[method]);
                        if (permissions[i].sizeMethod() == 0) {
                            assemblyDescriptor.removeMethodPermission(permissions[i]);
                        }
                    }
                }
            }
        }
    }

    private FileObject findFileObject(final String className) {
        ElementHandle<TypeElement> beanEH = getBeanClass(className);
        if (beanEH != null) {
            return SourceUtils.getFile(beanEH, cpInfo);
        }
        return null;
    }

    private void deleteClasses() {
        final ArrayList<FileObject> classFileObjects = new ArrayList<FileObject>();

        try {
            ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) throws Exception {
                    classFileObjects.add(metadata.findResource(ejbClass));
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    if (ejb instanceof EntityAndSession) {
                        EntityAndSession entityAndSessionfModel = (EntityAndSession) ejb;
                        if (entityAndSessionfModel.getLocalHome() != null) {
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getLocalHome()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getLocal()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getLocal() + "Business"));
                        }
                        if (entityAndSessionfModel.getHome() != null) {
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getHome()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getRemote()));
                            classFileObjects.add(metadata.findResource(entityAndSessionfModel.getRemote() + "Business"));
                        }
                    }
                    return null;
                }
            });

            for (FileObject fileObject : classFileObjects) {
                if (fileObject != null) {
                    DataObject dataObject = DataObject.find(fileObject);
                    assert dataObject != null: ("cannot find DataObject for " + fileObject.getPath());
                    if (dataObject != null) {
                        dataObject.delete();
                    }
                }
            }

        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

}
