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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;

/**
 * Utility methods mainly for getting annotations.
 * This is a helper class; all methods are static.
 * @author Tomas Mysik, Martin Adamek
 */
public class CommonAnnotationHelper {
    
    // see JSR250
    private static final Set<String> RESOURCE_REF_TYPES = new HashSet<String>(Arrays.<String>asList(
            "javax.sql.DataSource",
            "javax.jms.ConnectionFactory",
            "javax.jms.QueueConnectionFactory",
            "javax.jms.TopicConnectionFactory",
            "javax.mail.Session",
            "java.net.URL",
            "javax.resource.cci.ConnectionFactory",
            "org.omg.CORBA_2_3.ORB"
            // any other connection factory defined by a resource adapter
            ));
    private static final Set<String> ENV_ENTRY_TYPES = new HashSet<String>(Arrays.<String>asList(
            "java.lang.String",
            "java.lang.Character",
            "java.lang.Integer",
            "java.lang.Boolean",
            "java.lang.Double",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Long",
            "java.lang.Float"));
    private static final Set<String> SERVICE_REF_TYPES = new HashSet<String>(Arrays.<String>asList(
            "javax.xml.rpc.Service",
            "javax.xml.ws.Service",
            "javax.jws.WebService"));
    private static final Set<String> MESSAGE_DESTINATION_TYPES = new HashSet<String>(Arrays.<String>asList(
            "javax.jms.Queue",
            "javax.jms.Topic"));

    private CommonAnnotationHelper() {
    }
    
    public static SecurityRole[] getSecurityRoles(AnnotationModelHelper helper) {
        final List<SecurityRole> result = new ArrayList<SecurityRole>();
        final AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectStringArray("value", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    String value = (String) arrayMember.getValue();
                    result.add(new SecurityRoleImpl(value));
                }
                return null;
            }
        }, null);
        try {
            helper.getAnnotationScanner().findAnnotations("javax.annotation.security.DeclareRoles", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    parser.parse(annotation);
                }
            });
        } catch (InterruptedException e) {
            return new SecurityRole[0];
        }
        return result.toArray(new SecurityRole[result.size()]);
    }
    
    /**
     * Get all {@link ResourceRef}s for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link ResourceRef}s.
     */
    public static ResourceRef[] getResourceRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getResourceRefs(resources);
    }
    
    /**
     * Get all {@link ResourceRef}s
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link ResourceRef}s.
     */
    public static ResourceRef[] getResourceRefs(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getResourceRefs(resources);
    }
    
    /**
     * Get all {@link ResourceEnvRef}s for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link ResourceEnvRef}s.
     */
    public static ResourceEnvRef[] getResourceEnvRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getResourceEnvRefs(resources);
    }
    
    /**
     * Get all {@link ResourceEnvRef}s
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link ResourceEnvRef}s.
     */
    public static ResourceEnvRef[] getResourceEnvRefs(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getResourceEnvRefs(resources);
    }
    
    /**
     * Get all {@link EnvEntry EnvEntries} for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link EnvEntry}s.
     */
    public static EnvEntry[] getEnvEntries(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getEnvEntries(resources);
    }
    
    /**
     * Get all {@link EnvEntry EnvEntries}
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link EnvEntry}s.
     */
    public static EnvEntry[] getEnvEntries(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getEnvEntries(resources);
    }
    
    /**
     * Get all {@link MessageDestinationRef}s for given class.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link MessageDestinationRef}s.
     */
    public static MessageDestinationRef[] getMessageDestinationRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        List<ResourceImpl> resources = getResources(helper, typeElement);
        return getMessageDestinationRefs(resources);
    }
    
    /**
     * Get all {@link MessageDestinationRef}s
     * for given classpath (via given annotation model helper).
     * @param helper    annotation model helper.
     * @return          all found {@link MessageDestinationRef}s.
     */
    public static MessageDestinationRef[] getMessageDestinationRefs(final AnnotationModelHelper helper) {
        assert helper != null;
        
        List<ResourceImpl> resources = getResources(helper);
        return getMessageDestinationRefs(resources);
    }
    
    /**
     * Get all {@link ServiceRef}s for given class.
     * Fields annotated by {@link javax.xml.ws.WebServiceRef @WebServiceRef}
     * and {@link javax.annotation.Resource @Resource} are searched.
     * @param helper        annotation model helper.
     * @param typeElement   class that is searched.
     * @return              all found {@link ServiceRef}s.
     */
    public static ServiceRef[] getServiceRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        assert typeElement != null;
        
        // @WebServiceRef
        final List<ServiceRef> serviceRefs = getWebServiceRefs(helper, typeElement);
        // @Resource
        List<ResourceImpl> resources = getResources(helper, typeElement);
        serviceRefs.addAll(getServiceRefs(resources));
        
        return serviceRefs.toArray(new ServiceRef[serviceRefs.size()]);
    }
    
    /**
     * Get all {@link ServiceRef}s
     * for given classpath (via given annotation model helper).
     * Fields annotated by {@link javax.xml.ws.WebServiceRef @WebServiceRef}
     * and {@link javax.annotation.Resource @Resource} are searched.
     * @param helper    annotation model helper.
     * @return          all found {@link ServiceRef}s.
     */
    public static ServiceRef[] getServiceRefs(final AnnotationModelHelper helper) {
        assert helper != null;
        
        // @WebServiceRef
        final List<ServiceRef> serviceRefs = getWebServiceRefs(helper);
        // @Resource
        List<ResourceImpl> resources = getResources(helper);
        serviceRefs.addAll(getServiceRefs(resources));
        
        return serviceRefs.toArray(new ServiceRef[serviceRefs.size()]);
    }

    /**
     * Get service endpoint for given class.
     * Annotation {@link javax.jws.WebService @WebService} is searched.
     * @param helper        annotation model helper
     * @param typeElement   class that is searched
     * @return              service name
     */
    public static String getServiceEndpoint(final AnnotationModelHelper helper, final TypeElement typeElement) {
        assert helper != null;
        // type element mustn't exist for removed object yet - see issue #219882, PersistentObject#getElementType()
        if (typeElement == null) {
            return null;
        }

        Map<String, ? extends AnnotationMirror> ans = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror wsMirror = ans.get("javax.jws.WebService"); //NOI18N
        if (wsMirror != null) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : wsMirror.getElementValues().entrySet()) {
                ExecutableElement key = entry.getKey();
                if (key.getSimpleName().contentEquals("endpointInterface")) { //NOI18N
                    AnnotationValue value = entry.getValue();
                    return value.toString();
                }
            }
            return ElementUtilities.getBinaryName(typeElement);
        }

        return null;
    }


    private static List<ResourceImpl> getResources(final AnnotationModelHelper helper, final TypeElement typeElement) {
        
        final List<ResourceImpl> result = new ArrayList<ResourceImpl>();
        
        // fields
        for (VariableElement field : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(field.getAnnotationMirrors(), "javax.annotation.Resource")) { //NOI18N
                ResourceImpl resource = new ResourceImpl(field, typeElement, helper);
                result.add(resource);
            }
        }
        // methods
        for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(method.getAnnotationMirrors(), "javax.annotation.Resource")) { //NOI18N
                ResourceImpl resource = new ResourceImpl(method, typeElement, helper);
                result.add(resource);
            }
        }
        return result;
    }
    
    private static List<ResourceImpl> getResources(final AnnotationModelHelper helper) {
        
        final List<ResourceImpl> result = new ArrayList<ResourceImpl>();
        try {
            helper.getAnnotationScanner().findAnnotations(
                    "javax.annotation.Resource", // NOI18N
                    EnumSet.of(ElementKind.CLASS, ElementKind.METHOD, ElementKind.FIELD),new AnnotationHandler() {
                        public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotation) {
                            ResourceImpl resource = new ResourceImpl(element, typeElement, helper);
                            result.add(resource);
                        }
                    });
        } catch (InterruptedException e) {
            return Collections.emptyList();
        }
        return result;
    }
    
    private static List<ServiceRef> getWebServiceRefs(final AnnotationModelHelper helper, final TypeElement typeElement) {
        
        final List<ServiceRef> result = new ArrayList<ServiceRef>();
        
        // fields
        for (VariableElement field : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (helper.hasAnnotation(field.getAnnotationMirrors(), "javax.xml.ws.WebServiceRef")) { //NOI18N
                addServiceReference(result, field, typeElement, helper);
            }
        }
        return result;
    }
    
    private static List<ServiceRef> getWebServiceRefs(final AnnotationModelHelper helper) {
        
        final List<ServiceRef> result = new ArrayList<ServiceRef>();
        try {
            helper.getAnnotationScanner().findAnnotations(
                    "javax.xml.ws.WebServiceRef", // NOI18N
                    EnumSet.of(ElementKind.FIELD),new AnnotationHandler() {
                        public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotation) {
                            addServiceReference(result, element, typeElement, helper);
                        }
                    });
        } catch (InterruptedException e) {
            return Collections.emptyList();
        }
        return result;
    }
    
    private static ResourceRef[] getResourceRefs(final List<ResourceImpl> resources) {
        List<ResourceRef> elements = new ArrayList<ResourceRef>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (RESOURCE_REF_TYPES.contains(resource.getType())) {
                elements.add(new ResourceRefImpl(resource));
            }
        }
        return elements.toArray(new ResourceRef[elements.size()]);
    }
    
    private static EnvEntry[] getEnvEntries(final List<ResourceImpl> resources) {
        List<EnvEntry> elements = new ArrayList<EnvEntry>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (ENV_ENTRY_TYPES.contains(resource.getType())) {
                elements.add(new EnvEntryImpl(resource));
            }
        }
        return elements.toArray(new EnvEntry[elements.size()]);
    }
    
    private static MessageDestinationRef[] getMessageDestinationRefs(final List<ResourceImpl> resources) {
        List<MessageDestinationRef> elements = new ArrayList<MessageDestinationRef>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (MESSAGE_DESTINATION_TYPES.contains(resource.getType())) {
                elements.add(new MessageDestinationRefImpl(resource));
            }
        }
        return elements.toArray(new MessageDestinationRef[elements.size()]);
    }
    
    private static List<ServiceRef> getServiceRefs(final List<ResourceImpl> resources) {
        List<ServiceRef> elements = new ArrayList<ServiceRef>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (SERVICE_REF_TYPES.contains(resource.getType())) {
                elements.add(new ServiceRefImpl(resource));
            }
        }
        return elements;
    }
    
    private static ResourceEnvRef[] getResourceEnvRefs(final List<ResourceImpl> resources) {
        List<ResourceEnvRef> elements = new ArrayList<ResourceEnvRef>(resources.size());
        
        for (ResourceImpl resource : resources) {
            if (!RESOURCE_REF_TYPES.contains(resource.getType())
                    && !ENV_ENTRY_TYPES.contains(resource.getType())
                    && !MESSAGE_DESTINATION_TYPES.contains(resource.getType())
                    && !SERVICE_REF_TYPES.contains(resource.getType())) {
                elements.add(new ResourceEnvRefImpl(resource));
            }
        }
        return elements.toArray(new ResourceEnvRef[elements.size()]);
    }
    
    private static void addServiceReference(final List<ServiceRef> serviceRefs, final Element element, TypeElement parentElement, final AnnotationModelHelper helper) {
        TypeMirror fieldTypeMirror = element.asType();
        if (fieldTypeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType fieldDeclaredType = (DeclaredType) fieldTypeMirror;
            Element fieldTypeElement = fieldDeclaredType.asElement();
            
            if (ElementKind.INTERFACE == fieldTypeElement.getKind() || ElementKind.CLASS == fieldTypeElement.getKind() ) {
                TypeElement typeElement = (TypeElement) fieldTypeElement;
                ServiceRef newServiceRef = new ServiceRefImpl(element, typeElement, parentElement, helper);
                // test if already exists
                ServiceRef existingServiceRef = null;
                for (ServiceRef sr : serviceRefs) {
                    if (newServiceRef.getServiceRefName().equals(sr.getServiceRefName())) {
                        existingServiceRef = sr;
                    }
                }
                if (existingServiceRef != null) {
                    if (newServiceRef.sizePortComponentRef() > 0) {
                        PortComponentRef newPortComp = newServiceRef.getPortComponentRef(0);
                        // eventiually add new PortComponentRef
                        PortComponentRef[] portComps = existingServiceRef.getPortComponentRef();
                        boolean foundPortComponent = false;
                        for (PortComponentRef portComp : portComps) {
                            if (portComp.getServiceEndpointInterface().equals(newPortComp.getServiceEndpointInterface())) {
                                foundPortComponent = true;
                            }
                        }
                        if (!foundPortComponent) {
                            existingServiceRef.addPortComponentRef(newPortComp);
                        }
                    }
                } else {
                    serviceRefs.add(newServiceRef);
                }
            }
        }
    }
}
