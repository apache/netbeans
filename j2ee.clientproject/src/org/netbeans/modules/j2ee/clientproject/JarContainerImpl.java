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

package org.netbeans.modules.j2ee.clientproject;


import java.io.IOException;
import java.net.URI;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceSupport;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author jungi
 */
public class JarContainerImpl implements EnterpriseReferenceContainer {
    
    private Project webProject;
    private AntProjectHelper antHelper;
    private static final String SERVICE_LOCATOR_PROPERTY = "project.serviceLocator.class"; //NOI18N
    private AppClient webApp;
    
    /** Creates a new instance of JarContainerImpl */
    public JarContainerImpl(Project p, ReferenceHelper helper, AntProjectHelper antHelper) {
        webProject = p;
        this.antHelper = antHelper;
    }
    
    /**
     * set name of service locator fo this project.
     *
     * @param serviceLocator used in this project
     */
    public void setServiceLocatorName(String serviceLocator) throws IOException {
        EditableProperties ep =
                antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(SERVICE_LOCATOR_PROPERTY, serviceLocator);
        antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(webProject);
    }
    
    /**
     * Add given resource reference into the deployment descriptor.
     *
     * @param ref reference to resource used
     * @param referencingClass class which will use the resource
     * @return unique jndi name used in deployment descriptor
     */
    public String addResourceRef(ResourceReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        String resourceRefName = ref.getResRefName();
        AppClient ac = getAppClient();
        // see if jdbc resource has already been used in the app
        // this change requested by Ludo
        if (javax.sql.DataSource.class.getName().equals(ref.getResType())) {
            ResourceRef[] refs = ac.getResourceRef();
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
        if (!isResourceRefUsed(ac, ref)) {
            resourceRefName = getUniqueName(ac, "ResourceRef", "ResRefName", ref.getResRefName()); //NOI18N
            ResourceRef resourceRef = ac.newResourceRef();
            EnterpriseReferenceSupport.populate(ref, resourceRefName, resourceRef);
            getAppClient().addResourceRef(resourceRef);
            writeDD(referencingFile, referencingClass);
        }
        return resourceRefName;
    }
    
    /**
     *
     *
     * @see #addEjbReference(EjbRef, String, AntArtifact)
     */
    public String addEjbLocalReference(EjbReference localRef, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencedClassName) throws IOException {
        throw new UnsupportedOperationException("Local references are not supported in App Client module.");
    }
    
    /**
     * Add given ejb reference into deployment descriptor. This method should
     * also ensure that the supplied target is added to the class path (as the
     * ejb interfaces will be referenced from this class) as well as the
     * deployed manifest. The deployed manifest is the generic J2EE compliant
     * strategy, application server specific behavior such as delegating to the
     * parent class loader could also be used. The main point is not to
     * include the target in the deployed archive but instead reference the
     * interface jar (or standard ejb module) included in the J2EE application.
     *
     * @param ref -- ejb reference this will include the ejb link which assumes
     * root packaging in the containing application. The name of this ref should
     * be considered a hint and made unique within the deployment descriptor.
     * @param referencedClassName -- name of referenced class, this can be used
     * to determine where to add the deployment descriptor entry. This class
     * will be modified with a method or other strategy to obtain the ejb.
     * @param target to include in the build
     * @return actual jndi name used in deployment descriptor
     */
    public String addEjbReference(EjbReference ref, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referenceClassName) throws IOException {
        return addReference(ref, refType, ejbRefName, referencingFile, referenceClassName);
    }
    
    /**
     * Add given message destination reference into the deployment descriptor
     *
     * @param ref to destination
     * @param referencingClass class using the destination
     * @return unique jndi name used in the deployment descriptor
     */
    public String addDestinationRef(MessageDestinationReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        AppClient ac = getAppClient();
        String refName = getUniqueName(ac, "MessageDestinationRef", "MessageDestinationRefName", ref.getMessageDestinationRefName()); //NOI18N
        try {
            MessageDestinationRef messageDestinationRef = ac.newMessageDestinationRef();
            EnterpriseReferenceSupport.populate(ref, refName, messageDestinationRef);
            ac.addMessageDestinationRef(messageDestinationRef);
            writeDD(referencingFile, referencingClass);
        } catch (VersionNotSupportedException ex){}
        return refName;
    }
    
    /**
     *
     *
     * @return name of the service locator defined for this project or null
     * if service locator is not being used
     */
    public String getServiceLocatorName() {
        EditableProperties ep =
                antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return ep.getProperty(SERVICE_LOCATOR_PROPERTY);
    }
    
    private AppClient getAppClient() throws IOException {
        if (webApp==null) {
            AppClientProvider jp = webProject.getLookup().lookup(AppClientProvider.class);
            FileObject fo = jp.getDeploymentDescriptor();
            webApp = DDProvider.getDefault().getDDRoot(fo);
        }
        return webApp;
    }
    
    private String getUniqueName(AppClient wa, String beanName,
            String property, String originalValue) {
        String proposedValue = originalValue;
        int index = 1;
        while (wa.findBeanByName(beanName, property, proposedValue) != null) {
            proposedValue = originalValue+Integer.toString(index++);
        }
        return proposedValue;
    }
    
    private void writeDD(FileObject referencingFile, final String referencingClassName) throws IOException {
        final AppClientProvider jp = webProject.getLookup().lookup(AppClientProvider.class);
        JavaSource javaSource = JavaSource.forFileObject(referencingFile);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = controller.getElements().getTypeElement(referencingClassName);
                if (isDescriptorMandatory(jp.getJ2eeProfile()) ||
                        !InjectionTargetQuery.isInjectionTarget(controller, typeElement)) {
                    FileObject fo = jp.getDeploymentDescriptor();
                    getAppClient().write(fo);
                }
            }
        }, true);
    }
    
    private String addReference(EjbReference ejbReference, EjbReference.EjbRefIType refType, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        String refName = ejbRefName;
        AppClient webApp = getAppClient();

        // don't duplicate ejbRefs with same name - issue #193576
        CommonDDBean bean = getAppClient().findBeanByName("EjbRef", "EjbRefName", ejbRefName); //NOI18N
        if (bean != null) {
            // when the name leads to the same bean
            if (bean.getValue(EjbRef.REMOTE).equals(ejbReference.getRemote())) {
                return ejbRefName;
            } else {
                refName = getUniqueName(getAppClient(), "EjbRef", "EjbRefName", ejbRefName); //NOI18N
            }
        }

        // EjbRef can come from Ejb project
        try {
            EjbRef newRef = (EjbRef)webApp.createBean("EjbRef"); //NOI18N
            newRef.setEjbRefName(refName);
            newRef.setEjbRefType(ejbReference.getEjbRefType());
            newRef.setHome(ejbReference.getRemoteHome());
            newRef.setRemote(ejbReference.getRemote());
            getAppClient().addEjbRef(newRef);
        } catch (ClassNotFoundException ex){}
        
        try {
            AntArtifact target = getAntArtifact(ejbReference, refType);
            ProjectClassPathModifier.addAntArtifacts(new AntArtifact[] {target}, new URI[] {target.getArtifactLocations()[0].normalize()}, referencingFile, ClassPath.COMPILE);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        
        writeDD(referencingFile, referencingClass);
        return refName;
    }

    private static AntArtifact getAntArtifact(final EjbReference ejbReference, EjbReference.EjbRefIType refType) throws IOException {
        FileObject ejbReferenceEjbClassFO = SourceUtils.getFileObject(ejbReference.getComponentName(refType), ejbReference.getClasspathInfo());
        assert ejbReferenceEjbClassFO != null : "Reference FileObject not found: " + ejbReference.getComponentName(refType);
        Project otherPrj = FileOwnerQuery.getOwner(ejbReferenceEjbClassFO);

        Project project = FileOwnerQuery.getOwner(ejbReferenceEjbClassFO);
        AntArtifact[] antArtifacts = AntArtifactQuery.findArtifactsByType(project, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        boolean hasArtifact = (antArtifacts != null && antArtifacts.length > 0);
        
        return hasArtifact ? antArtifacts[0] : null;
        
    }
    
    private static boolean isDescriptorMandatory(Profile j2eeVersion) {
        if (Profile.J2EE_13.equals(j2eeVersion) || Profile.J2EE_14.equals(j2eeVersion)) {
            return true;
        }
        return false;
    }
    
    /**
     * Searches for given resource reference in given client module.
     * Two resource references are considered equal if their names and types are equal.
     * 
     * @param ac client module where resource reference should be found
     * @param resRef resource reference to find
     * @return true id resource reference was found, false otherwise
     */
    private static boolean isResourceRefUsed(AppClient ac, ResourceReference resRef) {
        String resRefName = resRef.getResRefName();
        String resRefType = resRef.getResType();
        for (ResourceRef existingRef : ac.getResourceRef()) {
            if (resRefName.equals(existingRef.getResRefName()) && resRefType.equals(existingRef.getResType())) {
                return true;
            }
        }
        return false;
    }

}
