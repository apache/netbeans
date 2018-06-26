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
package org.netbeans.modules.maven.j2ee.ejb;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceSupport;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/** 
 *
 * @author Milos Kleint
 */
@ProjectServiceProvider(service = EnterpriseReferenceContainer.class, projectType = {"org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB})
public final class EjbEntRefContainerImpl implements EnterpriseReferenceContainer {

    private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
    private final Project ejbProject;

    
    public EjbEntRefContainerImpl(Project prj) {
        ejbProject = prj;
    }

    @Override
    public String addEjbReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(ref, refType, ejbRefName, false, referencingFile, referencingClass);
    }

    @Override
    public String addEjbLocalReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(ref, refType, ejbRefName, true, referencingFile, referencingClass);
    }

    private String addReference(final EjbReference ejbReference, final EjbReference.EjbRefIType refType, final String ejbRefName, final boolean local, FileObject referencingFile,
            final String referencingClass) throws IOException {

        final org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = findEjbModule(referencingFile);
        MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();

        MetadataModel<EjbJarMetadata> ejbReferenceMetadataModel = ejbReference.getEjbModule().getMetadataModel();
        String ejbName = ejbReferenceMetadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {

            @Override
            public String run(EjbJarMetadata metadata) throws Exception {
                return metadata.findByEjbClass(ejbReference.getEjbClass()).getEjbName();
            }
        });

        FileObject ejbReferenceEjbClassFO = SourceUtils.getFileObject(ejbReference.getComponentName(refType), ejbReference.getClasspathInfo());
        assert ejbReferenceEjbClassFO != null : "Reference FileObject not found: " + ejbReference.getComponentName(refType);
        Project otherPrj = FileOwnerQuery.getOwner(ejbReferenceEjbClassFO);
        NbMavenProject oprj = otherPrj.getLookup().lookup(NbMavenProject.class);
        String jarName = "";
        if (oprj != null) {
            jarName = oprj.getMavenProject().getBuild().getFinalName();  //NOI18N
        }

        jarName = jarName +  "#";
        final String ejbLink = jarName + ejbName;

        final boolean fromSameProject = (oprj != null && ejbProject.equals(otherPrj)) || oprj == null /** ant? */;


        final boolean[] write = new boolean[]{false};
        String resourceName = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {

            @Override
            public String run(EjbJarMetadata metadata) {
                String refName = null;
                Ejb model = metadata.findByEjbClass(referencingClass);
                // try to write to deployment descriptor only if there is any
                // in case of metadata written in annotations, model should be updatet automaticaly
                if (model == null) {
                    return ejbRefName;
                }
                if (local) {
                    refName = getUniqueEjbLocalRefName(model, ejbRefName);
                    EjbLocalRef ejbRef = model.newEjbLocalRef();
                    ejbRef.setEjbRefName(refName);
                    ejbRef.setEjbRefType(ejbReference.getEjbRefType());
                    ejbRef.setLocal(ejbReference.getLocal());
                    ejbRef.setLocalHome(ejbReference.getLocalHome());
                    if (fromSameProject) {
                        ejbRef.setEjbLink(stripModuleName(ejbLink));
                    }
                    model.addEjbLocalRef(ejbRef);
                } else {
                    refName = getUniqueEjbRefName(model, ejbRefName);
                    EjbRef ejbRef = model.newEjbRef();
                    ejbRef.setEjbRefName(refName);
                    ejbRef.setEjbRefType(ejbReference.getEjbRefType());
                    ejbRef.setRemote(ejbReference.getRemote());
                    ejbRef.setHome(ejbReference.getRemoteHome());
                    if (fromSameProject) {
                        ejbRef.setEjbLink(stripModuleName(ejbLink));
                    }
                    model.addEjbRef(ejbRef);
                }
                write[0] = true;


                return refName;
            }
        });

        if (!fromSameProject && oprj != null) {
            final String grId = oprj.getMavenProject().getGroupId();
            final String artId = oprj.getMavenProject().getArtifactId();
            final String version = oprj.getMavenProject().getVersion();
            //TODO - also check the configuration of the ejb project and
            // depend on the client jar only (add configuration to generate one).
            //TODO - add dependency on j2ee jar (to have javax.ejb on classpath
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(
                    ejbProject.getProjectDirectory().getFileObject("pom.xml"),//NOI18N
                    Collections.<ModelOperation<POMModel>>singletonList(new ModelOperation<POMModel>() {

                @Override
                public void performOperation(POMModel model) {
                    //add as dependency
                    Dependency d = ModelUtils.checkModelDependency(model, grId, artId, true);
                    if (d != null) {
                        d.setVersion(version);
                    }
                }
            }));
        }
        if (write[0]) {
            writeDD(ejbModule);
        }
        ProjectManager.getDefault().saveProject(ejbProject);
        return resourceName;
    }

    @Override
    public String getServiceLocatorName() {
        AuxiliaryProperties props = ejbProject.getLookup().lookup(AuxiliaryProperties.class);
        return props.get(SERVICE_LOCATOR_PROPERTY, true);
    }

    @Override
    public void setServiceLocatorName(String serviceLocator) throws IOException {
        AuxiliaryProperties props = ejbProject.getLookup().lookup(AuxiliaryProperties.class);
        props.put(SERVICE_LOCATOR_PROPERTY, serviceLocator, true);
    }

    private String stripModuleName(String ejbLink) {
        int index = ejbLink.indexOf('#');
        return ejbLink.substring(index + 1);
    }

    private org.netbeans.modules.j2ee.api.ejbjar.EjbJar findEjbModule(FileObject fileObject) {
        return org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
    }

    private void writeDD(org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule) throws IOException {
        if (isDescriptorMandatory(ejbModule.getJ2eePlatformVersion())) {
            FileObject fo = ejbModule.getDeploymentDescriptor();
            if (fo != null) {
                DDProvider.getDefault().getDDRoot(fo).write(fo);
            }
        }
    }

    @Override
    public String addResourceRef(final ResourceReference ref, FileObject referencingFile, final String referencingClass) throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = findEjbModule(referencingFile);
        MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
        final boolean[] write = new boolean[]{false};
        String resourceName = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {

            @Override
            public String run(EjbJarMetadata metadata) throws IOException {
                Ejb ejb = metadata.findByEjbClass(referencingClass);
                if (ejb == null) {
                    return ref.getResRefName();
                }
                String resourceRefName = ref.getResRefName();
                if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
                    if (!isJdbcConnectionAlreadyUsed(ejb, ref)) {
                        resourceRefName = getUniqueResRefName(ejb, ref.getResRefName());
                        ResourceRef resourceRef = ejb.newResourceRef();
                        EnterpriseReferenceSupport.populate(ref, resourceRefName, resourceRef);
                        ejb.addResourceRef(resourceRef);
                        write[0] = true;
                    }
                } else {
                    if (!isResourceRefUsed(ejb, ref)) {
                        resourceRefName = getUniqueResRefName(ejb, ref.getResRefName());
                        ResourceRef resourceRef = ejb.newResourceRef();
                        EnterpriseReferenceSupport.populate(ref, resourceRefName, resourceRef);
                        ejb.addResourceRef(resourceRef);
                        write[0] = true;
                    }
                }
                return resourceRefName;
            }
        });
        if (write[0]) {
            writeDD(ejbModule);
        }
        return resourceName;
    }

    @Override
    public String addDestinationRef(final MessageDestinationReference ref, FileObject referencingFile, final String referencingClass) throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = findEjbModule(referencingFile);
        MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
        final boolean[] write = new boolean[]{false};
        String resourceName = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {

            @Override
            public String run(EjbJarMetadata metadata) {
                Ejb ejb = metadata.findByEjbClass(referencingClass);
                if (ejb == null) {
                    return ref.getMessageDestinationRefName();
                }
                try {
                    // do not add if there is already an existing destination ref (see #85673)
                    for (MessageDestinationRef mdRef : ejb.getMessageDestinationRef()) {
                        if (mdRef.getMessageDestinationRefName().equals(ref.getMessageDestinationRefName())) {
                            return mdRef.getMessageDestinationRefName();
                        }
                    }
                } catch (VersionNotSupportedException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }

                String destinationRefName = getUniqueMessageDestRefName(ejb, ref.getMessageDestinationRefName());
                try {
                    MessageDestinationRef messageDestinationRef = ejb.newMessageDestinationRef();
                    EnterpriseReferenceSupport.populate(ref, destinationRefName, messageDestinationRef);
                    ejb.addMessageDestinationRef(messageDestinationRef);
                } catch (VersionNotSupportedException vnse) {
                    // this exception should not be generated
                    }
                write[0] = true;
                return destinationRefName;
            }
        });
        if (write[0]) {
            writeDD(ejbModule);
        }
        return resourceName;
    }

    private boolean isJdbcConnectionAlreadyUsed(Ejb ejb, ResourceReference ref) throws IOException {
        if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
            for (ResourceRef existingRef : ejb.getResourceRef()) {
                String newDefaultDescription = ref.getDefaultDescription();
                String existingDefaultDescription = existingRef.getDefaultDescription();
                boolean canCompareDefDesc = (newDefaultDescription != null && existingDefaultDescription != null);
                if (javax.sql.DataSource.class.getName().equals(existingRef.getResType()) &&
                        (canCompareDefDesc ? newDefaultDescription.equals(existingDefaultDescription) : true) &&
                        ref.getResRefName().equals(existingRef.getResRefName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Searches for given resource reference in given EJB.
     * Two resource references are considered equal if their names and types are equal.
     *
     * @param ejb EJB where resource reference should be found
     * @param resRef resource reference to find
     * @return true id resource reference was found, false otherwise
     */
    private static boolean isResourceRefUsed(Ejb ejb, ResourceReference resRef) {
        String resRefName = resRef.getResRefName();
        String resRefType = resRef.getResType();
        for (ResourceRef existingRef : ejb.getResourceRef()) {
            if (resRefName.equals(existingRef.getResRefName()) && resRefType.equals(existingRef.getResType())) {
                return true;
            }
        }
        return false;
    }

    private String getUnigueName(Set<String> existingNames, String originalValue) {
        String proposedValue = originalValue;
        int index = 1;
        while (existingNames.contains(proposedValue)) {
            proposedValue = originalValue + Integer.toString(index++);
        }
        return proposedValue;
    }

    private String getUniqueResRefName(Ejb bean, String originalValue) {
        Set<String> resRefNames = new HashSet<String>();
        for (ResourceRef resourceRef : bean.getResourceRef()) {
            resRefNames.add(resourceRef.getResRefName());
        }
        return getUnigueName(resRefNames, originalValue);
    }

    private String getUniqueEjbRefName(Ejb bean, String originalValue) {
        Set<String> ejbRefNames = new HashSet<String>();
        for (EjbRef ejbRef : bean.getEjbRef()) {
            ejbRefNames.add(ejbRef.getEjbRefName());
        }
        return getUnigueName(ejbRefNames, originalValue);
    }

    private String getUniqueEjbLocalRefName(Ejb bean, String originalValue) {
        Set<String> ejbLocalRefNames = new HashSet<String>();
        for (EjbLocalRef ejbLocalRef : bean.getEjbLocalRef()) {
            ejbLocalRefNames.add(ejbLocalRef.getEjbRefName());
        }
        return getUnigueName(ejbLocalRefNames, originalValue);
    }

    private String getUniqueMessageDestRefName(Ejb bean, String originalValue) {
        Set<String> messageDestRefNames = new HashSet<String>();
        try {
            for (MessageDestinationRef messageDestRef : bean.getMessageDestinationRef()) {
                messageDestRefNames.add(messageDestRef.getMessageDestinationRefName());
            }
        } catch (VersionNotSupportedException vnse) {
            Exceptions.printStackTrace(vnse);
        }
        return getUnigueName(messageDestRefNames, originalValue);
    }

    private static boolean isDescriptorMandatory(String j2eeVersion) {
        if ("1.3".equals(j2eeVersion) || "1.4".equals(j2eeVersion)) {
            return true;
        }
        return false;
    }
}

