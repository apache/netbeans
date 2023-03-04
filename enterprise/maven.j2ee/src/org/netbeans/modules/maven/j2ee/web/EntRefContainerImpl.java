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

package org.netbeans.modules.maven.j2ee.web;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceSupport;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
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
@org.netbeans.api.annotations.common.SuppressWarnings("DLS_DEAD_LOCAL_STORE")
@ProjectServiceProvider(
    service =
        EnterpriseReferenceContainer.class,
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
    }
)
public class EntRefContainerImpl implements EnterpriseReferenceContainer {
    
    private Project project;
    private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
    private WebApp webApp;
    
    public EntRefContainerImpl(Project p) {
        project = p;
    }
    
    @Override
    public String addEjbLocalReference(EjbReference localRef, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(localRef, refType, ejbRefName, true, referencingFile, referencingClass);
    }
    
    @Override
    public String addEjbReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(ref, refType, ejbRefName, false, referencingFile, referencingClass);
    }
    
    private String addReference(final EjbReference ejbReference, final EjbReference.EjbRefIType refType, String ejbRefName, boolean local, FileObject referencingFile, String referencingClass) throws IOException {
        String refName = null;

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
        final NbMavenProject oprj = otherPrj.getLookup().lookup(NbMavenProject.class);
        String jarName = "";
        
        final boolean fromSameProject = (oprj != null && project.equals(otherPrj)) || oprj == null /** ant? */;
        
        if (!fromSameProject && oprj != null) {
            jarName = oprj.getMavenProject().getBuild().getFinalName();  //NOI18N

            final String grId = oprj.getMavenProject().getGroupId();
            final String artId = oprj.getMavenProject().getArtifactId();
            final String version = oprj.getMavenProject().getVersion();
            //TODO - also check the configuration of the ejb project and
            // depend on the client jar only (add configuration to generate one).
            //TODO - add dependency on j2ee jar (to have javax.ejb on classpath
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(
                    project.getProjectDirectory().getFileObject("pom.xml"),//NOI18N
                    Collections.<ModelOperation<POMModel>>singletonList(new ModelOperation<POMModel>() {

                @Override
                public void performOperation(POMModel model) {
                    //add as dependency
                    if (!ModelUtils.hasModelDependency(model, grId, artId)) {
                        Dependency d = ModelUtils.checkModelDependency(model, grId, artId, true);
                        if (d != null) {
                            d.setVersion(version);
                            if (NbMavenProject.TYPE_EJB.equals(oprj.getPackagingType()) ||
                                NbMavenProject.TYPE_WAR.equals(oprj.getPackagingType())) {
                                d.setScope(Artifact.SCOPE_PROVIDED);
                            }
                        }
                    }
                }
            }));
        }

        WebApp wApp = getWebApp();
        if (webApp == null){
            return null;
        }

        jarName = jarName +  "#";
        final String ejbLink = jarName + ejbName;
        
        if (local) {
            refName = getUniqueName(getWebApp(), "EjbLocalRef", "EjbRefName", ejbRefName); //NOI18N
            // EjbLocalRef can come from Ejb project
            try {
                EjbLocalRef newRef = (EjbLocalRef)wApp.createBean("EjbLocalRef"); //NOI18N
                if (fromSameProject) {
                    newRef.setEjbLink(ejbLink);
                }
                newRef.setEjbRefName(refName);
                newRef.setEjbRefType(ejbReference.getEjbRefType());
                newRef.setLocal(ejbReference.getLocal());
                newRef.setLocalHome(ejbReference.getLocalHome());
                getWebApp().addEjbLocalRef(newRef);
            } catch (ClassNotFoundException ex){}
        } else {
            refName = getUniqueName(getWebApp(), "EjbRef", "EjbRefName", ejbRefName); //NOI18N
            // EjbRef can come from Ejb project
            try {
                EjbRef newRef = (EjbRef)wApp.createBean("EjbRef"); //NOI18N
                newRef.setEjbRefName(refName);
                newRef.setEjbRefType(ejbReference.getEjbRefType());
                newRef.setHome(ejbReference.getRemoteHome());
                newRef.setRemote(ejbReference.getRemote());
                getWebApp().addEjbRef(newRef);
            } catch (ClassNotFoundException ex){}
        }

        writeDD(referencingFile, referencingClass);
        return refName;
    }
    
    @Override
    public String getServiceLocatorName() {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        return props.get(SERVICE_LOCATOR_PROPERTY, true);
    }
    
    @Override
    public void setServiceLocatorName(String serviceLocator) throws IOException {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        props.put(SERVICE_LOCATOR_PROPERTY, serviceLocator, true);
    }
    
    private WebApp getWebApp() throws IOException {
        if (webApp==null) {
            WebModuleImpl jp = project.getLookup().lookup(WebModuleProviderImpl.class).getModuleImpl();
            FileObject fo = jp.getDeploymentDescriptor();
            if (fo != null) {
                webApp = DDProvider.getDefault().getDDRoot(fo);
            }
        }
        return webApp;
    }
    
    private void writeDD(FileObject referencingFile, final String referencingClass) throws IOException {
        WebModuleImpl jp = project.getLookup().lookup(WebModuleProviderImpl.class).getModuleImpl();
        
        // test if referencing class is injection target
        final boolean[] isInjectionTarget = {false};
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            @Override
                public void run(CompilationController controller) throws IOException {
                    Elements elements = controller.getElements();
                    TypeElement thisElement = elements.getTypeElement(referencingClass);
                    if (thisElement!=null)
                        isInjectionTarget[0] = InjectionTargetQuery.isInjectionTarget(controller, thisElement);
                }
            @Override
                public void cancel() {}
        };
        JavaSource refFile = JavaSource.forFileObject(referencingFile);
        if (refFile!=null) {
            refFile.runUserActionTask(task, true);
        }
        
        boolean shouldWrite = isDescriptorMandatory(jp.getJ2eeProfile()) || !isInjectionTarget[0];
        if (shouldWrite) {
            FileObject fo = jp.getDeploymentDescriptor();
            getWebApp().write(fo);
        }
    }
    
    @Override
    public String addResourceRef(ResourceReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        WebApp webApp = getWebApp();
        if (webApp == null) {
            WebModuleImpl jp = project.getLookup().lookup(WebModuleProviderImpl.class).getModuleImpl();
            // if web.xml is optional then create a blank one so that reference can be added to it;
            // if this results into unnecessary creation of web.xml then the caller of this
            // method should be fixed to not call it
            if (!isDescriptorMandatory(jp.getJ2eeProfile())) {
                // in cases of missing WEB-INF directory create that folder
                if (jp.getWebInf() == null) {
                    jp.createWebInf();
                }
                DDHelper.createWebXml(jp.getJ2eeProfile(), jp.getWebInf());
                webApp = getWebApp();
            }
        }
        String resourceRefName = ref.getResRefName();
        // see if jdbc resource has already been used in the app
        // this change requested by Ludo
        if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
            ResourceRef[] refs = webApp.getResourceRef();
            for (int i=0; i < refs.length; i++) {
                String newDefaultDescription = ref.getDefaultDescription();
                String existingDefaultDescription = refs[i].getDefaultDescription();
                boolean canCompareDefDesc = (newDefaultDescription != null && existingDefaultDescription != null);
                if (javax.sql.DataSource.class.getName().equals(refs[i].getResType()) &&
                        (canCompareDefDesc ? newDefaultDescription.equals(existingDefaultDescription) : true) &&
                        ref.getResRefName().equals(refs[i].getResRefName())) {
                    return refs[i].getResRefName();
                }
            }
        }
        if (!isResourceRefUsed(webApp, ref)) {
            resourceRefName = getUniqueName(webApp, "ResourceRef", "ResRefName", ref.getResRefName()); //NOI18N
            ResourceRef resourceRef = createResourceRef();
            EnterpriseReferenceSupport.populate(ref, resourceRefName, resourceRef);
            webApp.addResourceRef(resourceRef);
            writeDD(referencingFile, referencingClass);
        }
        return resourceRefName;
    }
    
    @Override
    public String addDestinationRef(MessageDestinationReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        try {
            // do not add if there is already an existing destination ref (see #85673)
            for (MessageDestinationRef mdRef : getWebApp().getMessageDestinationRef()){
                if (mdRef.getMessageDestinationRefName().equals(ref.getMessageDestinationRefName())){
                    return mdRef.getMessageDestinationRefName();
                }
            }
        } catch (VersionNotSupportedException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
        
        String refName = getUniqueName(getWebApp(), "MessageDestinationRef", "MessageDestinationRefName", //NOI18N
                ref.getMessageDestinationRefName());

        MessageDestinationRef messageDestinationRef = createDestinationRef();
        EnterpriseReferenceSupport.populate(ref, refName, messageDestinationRef);
        try {
            getWebApp().addMessageDestinationRef(messageDestinationRef);
            writeDD(referencingFile, referencingClass);
        } catch (VersionNotSupportedException ex){
            Logger.getLogger("global").log(Level.INFO, null, ex);//NOI18N
        }
        return refName;
    }
    
    public ResourceRef createResourceRef() throws IOException {
        try {
            return (ResourceRef) getWebApp().createBean("ResourceRef");//NOI18N
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
 
    public MessageDestinationRef createDestinationRef() throws IOException {
        try {
            return (org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) getWebApp().createBean("MessageDestinationRef");//NOI18N
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private String getUniqueName(WebApp wa, String beanName,
            String property, String originalValue) {
        String proposedValue = originalValue;
        int index = 1;
        while (wa.findBeanByName(beanName, property, proposedValue) != null) {
            proposedValue = originalValue+Integer.toString(index++);
        }
        return proposedValue;
    }
    
    private static boolean isDescriptorMandatory(Profile j2eeVersion) {
        if (Profile.J2EE_14.equals(j2eeVersion)) { //NOI18N
            return true;
        }
        return false;
    }
    
    /**
     * Searches for given resource reference in given web module.
     * Two resource references are considered equal if their names and types are equal.
     *
     * @param webApp web module where resource reference should be found
     * @param resRef resource reference to find
     * @return true id resource reference was found, false otherwise
     */
    private static boolean isResourceRefUsed(WebApp webApp, ResourceReference resRef) {
        String resRefName = resRef.getResRefName();
        String resRefType = resRef.getResType();
        for (ResourceRef existingRef : webApp.getResourceRef()) {
            if (resRefName.equals(existingRef.getResRefName()) && resRefType.equals(existingRef.getResType())) {
                return true;
            }
        }
        return false;
    }
}
