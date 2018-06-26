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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
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
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Chris Webster
 */
class WebContainerImpl implements EnterpriseReferenceContainer {
    
    private WebProject webProject;
    private AntProjectHelper antHelper;
    private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
    private WebApp webApp;
    
    public WebContainerImpl(WebProject p, ReferenceHelper helper, AntProjectHelper antHelper) {
        webProject = p;
        this.antHelper = antHelper;
    }
    
    @Override
    public String addEjbLocalReference(EjbReference localRef, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(localRef, refType, ejbRefName, true, referencingFile, referencingClass);
    }
    
    @Override
    public String addEjbReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        return addReference(ref, refType, ejbRefName, false, referencingFile, referencingClass);
    }
    
    private String addReference(final EjbReference ejbReference, EjbReference.EjbRefIType refType, String ejbRefName, boolean local, FileObject referencingFile, String referencingClass) throws IOException {
        String refName;
        
        MetadataModel<EjbJarMetadata> ejbReferenceMetadataModel = ejbReference.getEjbModule().getMetadataModel();
        String ejbName = ejbReferenceMetadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            @Override
            public String run(EjbJarMetadata metadata) throws Exception {
                return metadata.findByEjbClass(ejbReference.getEjbClass()).getEjbName();
            }
        });

        FileObject ejbReferenceEjbClassFO = SourceUtils.getFileObject(ejbReference.getComponentName(refType), ejbReference.getClasspathInfo());
        assert ejbReferenceEjbClassFO != null : "Reference FileObject not found: " + ejbReference.getComponentName(refType);
        Project project = FileOwnerQuery.getOwner(ejbReferenceEjbClassFO);
        AntArtifact[] antArtifacts = AntArtifactQuery.findArtifactsByType(project, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        boolean hasArtifact = (antArtifacts != null && antArtifacts.length > 0);
        final AntArtifact moduleJarTarget = hasArtifact ? antArtifacts[0] : null;
        // only first URI is taken, if more of them are defined, just first one is taken
        String[] names = new String[] { "" };
        if (moduleJarTarget != null) {
            names = moduleJarTarget.getArtifactLocations()[0].getPath().split("/");  //NOI18N
            try {
                ProjectClassPathModifier.addAntArtifacts(new AntArtifact[]{moduleJarTarget},
                        new URI[]{moduleJarTarget.getArtifactLocations()[0]}, webProject.getSourceRoots().getRoots()[0], ClassPath.COMPILE);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        WebApp webAppl = getWebApp();
        if (webAppl == null){
            return null;
        }

        String jarName = names[names.length - 1] + "#";
        final String ejbLink = jarName + ejbName;

        if (local) {
            refName = getUniqueName(getWebApp(), "EjbLocalRef", "EjbRefName", ejbRefName); //NOI18N
            // EjbLocalRef can come from Ejb project
            try {
                EjbLocalRef newRef = (EjbLocalRef)webAppl.createBean("EjbLocalRef"); //NOI18N
                newRef.setEjbLink(ejbLink);
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
                EjbRef newRef = (EjbRef)webAppl.createBean("EjbRef"); //NOI18N
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
        EditableProperties ep =
                antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return ep.getProperty(SERVICE_LOCATOR_PROPERTY);
    }
    
    @Override
    public void setServiceLocatorName(String serviceLocator) throws IOException {
        EditableProperties ep =
                antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(SERVICE_LOCATOR_PROPERTY, serviceLocator);
        antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(webProject);
    }
    
    private WebApp getWebApp() throws IOException {
        if (webApp==null) {
            ProjectWebModule jp = webProject.getLookup().lookup(ProjectWebModule.class);
            FileObject fo = jp.getDeploymentDescriptor();
            if (fo != null){
                webApp = DDProvider.getDefault().getDDRoot(fo);
            }
        }
        return webApp;
    }
    
    private void writeDD(FileObject referencingFile, final String referencingClass) throws IOException {
        ProjectWebModule jp = webProject.getLookup().lookup(ProjectWebModule.class);
        
        // test if referencing class is injection target
        final boolean[] isInjectionTarget = {false};
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    Elements elements = controller.getElements();
                    TypeElement thisElement = elements.getTypeElement(referencingClass);
                    if (thisElement!=null) {
                        isInjectionTarget[0] = InjectionTargetQuery.isInjectionTarget(controller, thisElement);
                    }
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
        WebApp wa = getWebApp();
        if (wa == null) {
            ProjectWebModule jp = webProject.getLookup().lookup(ProjectWebModule.class);
            // if web.xml is optional then create a blank one so that reference can be added to it;
            // if this results into unnecessary creation of web.xml then the caller of this
            // method should be fixed to not call it
            if (!isDescriptorMandatory(jp.getJ2eeProfile())) {
                DDHelper.createWebXml(jp.getJ2eeProfile(), jp.getWebInf());
                wa = getWebApp();
            }
        }
        if (wa == null) {
            return null;
        }
        String resourceRefName = ref.getResRefName();
        // see if jdbc resource has already been used in the app
        // this change requested by Ludo
        if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
            ResourceRef[] refs = wa.getResourceRef();
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
        if (!isResourceRefUsed(wa, ref)) {
            resourceRefName = getUniqueName(wa, "ResourceRef", "ResRefName", ref.getResRefName()); //NOI18N
            ResourceRef resourceRef = createResourceRef();
            EnterpriseReferenceSupport.populate(ref, resourceRefName, resourceRef);
            wa.addResourceRef(resourceRef);
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
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
        return refName;
    }
    
    public ResourceRef createResourceRef() throws IOException {
        try {
            return (ResourceRef) getWebApp().createBean("ResourceRef");
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
 
    public MessageDestinationRef createDestinationRef() throws IOException {
        try {
            return (org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) getWebApp().createBean("MessageDestinationRef");
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
        if (Profile.J2EE_13.equals(j2eeVersion) || Profile.J2EE_14.equals(j2eeVersion)) {
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
