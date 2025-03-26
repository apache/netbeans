/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef;
import org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.EjbRefHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;

public class MessageDrivenImpl extends PersistentObject implements MessageDriven {

    // persistent
    private String name;
    private String ejbClass;
    private String mappedName;
    private ActivationConfig activationConfig;
    
    private ResourceRef[] resourceRefs = null;
    private ResourceEnvRef[] resourceEnvRefs = null;
    private EnvEntry[] envEntries = null;
    private MessageDestinationRef[] messageDestinationRefs = null;
    private ServiceRef[] serviceRefs = null;
    private EjbRef[] ejbRefs;
    private EjbLocalRef[] ejbLocalRefs;
    
    public MessageDrivenImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }
    
    boolean refresh(TypeElement typeElement) {
        Map<String, ? extends AnnotationMirror> annByType = getHelper().getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror annotationMirror = annByType.get("jakarta.ejb.MessageDriven"); // NOI18N
        String activationConfigClass = "jakarta.ejb.ActivationConfigProperty";
        if (annotationMirror == null) {
            annotationMirror = annByType.get("javax.ejb.MessageDriven"); // NOI18N
            activationConfigClass = "javax.ejb.ActivationConfigProperty";
        }
        if (annotationMirror == null) {
            return false;
        }
        
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectString("name", AnnotationParser.defaultValue(typeElement.getSimpleName().toString())); // NOI18N
        parser.expectString("mappedName", null); // NOI18N

        activationConfig = new ActivationConfigImpl();
        TypeMirror acpType = getHelper().resolveType(activationConfigClass);
        ActivationConfigPropertyHandler handler = new ActivationConfigPropertyHandler(getHelper(), activationConfig);
        if (acpType != null) {
            parser.expectAnnotationArray("activationConfig", acpType, handler, null); //NOI18N
        }

        ParseResult parseResult = parser.parse(annotationMirror);
        name = parseResult.get("name", String.class);               //NOI18N
        mappedName = parseResult.get("mappedName", String.class);   //NOI18N
        ejbClass = typeElement.getQualifiedName().toString();
        return true;
    }

    private void initResourceRefs() {
        if (resourceRefs != null) {
            return;
        }
        resourceRefs = CommonAnnotationHelper.getResourceRefs(getHelper(), getTypeElement());
    }
    
    private void initResourceEnvRefs() {
        if (resourceEnvRefs != null) {
            return;
        }
        resourceEnvRefs = CommonAnnotationHelper.getResourceEnvRefs(getHelper(), getTypeElement());
    }
    
    private void initEnvEntries() {
        if (envEntries != null) {
            return;
        }
        envEntries = CommonAnnotationHelper.getEnvEntries(getHelper(), getTypeElement());
    }
    
    private void initMessageDestinationRefs() {
        if (messageDestinationRefs != null) {
            return;
        }
        messageDestinationRefs = CommonAnnotationHelper.getMessageDestinationRefs(getHelper(), getTypeElement());
    }
    
    private void initServiceRefs() {
        if (serviceRefs != null) {
            return;
        }
        serviceRefs = CommonAnnotationHelper.getServiceRefs(getHelper(), getTypeElement());
    }

    private void initLocalAndRemoteEjbRefs() {
        if (ejbRefs != null && ejbLocalRefs != null) {
            return;
        }

        final List<EjbRef> resultEjbRefs = new ArrayList<EjbRef>();
        final List<EjbLocalRef> resultEjbLocalRefs = new ArrayList<EjbLocalRef>();

        EjbRefHelper.setEjbRefsForClass(getHelper(), getTypeElement(), resultEjbRefs, resultEjbLocalRefs);

        ejbRefs = resultEjbRefs.toArray(new EjbRef[0]);
        ejbLocalRefs = resultEjbLocalRefs.toArray(new EjbLocalRef[0]);
    }

    // <editor-fold desc="Model implementation">
    public String getEjbName() {
        return name;
    }

    public String getEjbClass() {
        return ejbClass;
    }

    public ResourceRef[] getResourceRef() {
        initResourceRefs();
        return resourceRefs;
    }
    
    public ResourceRef getResourceRef(int index) {
        initResourceRefs();
        return resourceRefs[index];
    }
    
    public int sizeResourceRef() {
        initResourceRefs();
        return resourceRefs.length;
    }

    
    public ResourceEnvRef[] getResourceEnvRef() {
        initResourceEnvRefs();
        return resourceEnvRefs;
    }

    public ResourceEnvRef getResourceEnvRef(int index) {
        initResourceEnvRefs();
        return resourceEnvRefs[index];
    }

    public int sizeResourceEnvRef() {
        initResourceEnvRefs();
        return resourceEnvRefs.length;
    }

    public EnvEntry[] getEnvEntry() {
        initEnvEntries();
        return envEntries;
    }

    public EnvEntry getEnvEntry(int index) {
        initEnvEntries();
        return envEntries[index];
    }

    public int sizeEnvEntry() {
        initEnvEntries();
        return envEntries.length;
    }
    
    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs;
    }

    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs[index];
    }

    public int sizeMessageDestinationRef() throws VersionNotSupportedException {
        initMessageDestinationRefs();
        return messageDestinationRefs.length;
    }
    
    public ServiceRef[] getServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs;
    }

    public ServiceRef getServiceRef(int index) throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs[index];
    }

    public int sizeServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs.length;
    }

    public String getDefaultDisplayName() {
        return getEjbName();
    }

    /**
     * Mapped name mustn't be available for MDB 2.0, there is used destinationLookup activationConfigProperty.
     *
     * @return mappedName for MDBs 1.1 or {@code null}
     * @throws VersionNotSupportedException never thrown
     */
    @Override
    public String getMappedName() throws VersionNotSupportedException {
        return mappedName;
    }

    @Override
    public ActivationConfig getActivationConfig() throws VersionNotSupportedException {
        return activationConfig;
    }

    @Override
    public String getMessageDestinationType() throws VersionNotSupportedException {
        if (activationConfig == null) {
            return null;
        }

        for (ActivationConfigProperty activationConfigProperty : activationConfig.getActivationConfigProperty()) {
            if ("destinationType".equals(activationConfigProperty.getActivationConfigPropertyName())) {
                return activationConfigProperty.getActivationConfigPropertyValue();
            }
        }
        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setTransactionType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTransactionType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessagingType(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMessagingType() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationType(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationLink(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMessageDestinationLink() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setActivationConfig(ActivationConfig value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ActivationConfig newActivationConfig() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMappedName(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTimeoutMethod(NamedMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedMethod getTimeoutMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAroundInvoke(int index, AroundInvoke valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AroundInvoke getAroundInvoke(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeAroundInvoke() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAroundInvoke(AroundInvoke[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AroundInvoke[] getAroundInvoke() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeAroundInvoke(AroundInvoke valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceContextRef(int index,
                                         PersistenceContextRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceContextRef getPersistenceContextRef(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePersistenceContextRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceContextRef(PersistenceContextRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceContextRef[] getPersistenceContextRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePersistenceContextRef(PersistenceContextRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceUnitRef(int index,
                                      PersistenceUnitRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceUnitRef getPersistenceUnitRef(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePersistenceUnitRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPersistenceUnitRef(PersistenceUnitRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceUnitRef[] getPersistenceUnitRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePersistenceUnitRef(PersistenceUnitRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostConstruct(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback getPostConstruct(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePostConstruct() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostConstruct(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback[] getPostConstruct() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePostConstruct(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPreDestroy(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback getPreDestroy(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePreDestroy() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPreDestroy(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback[] getPreDestroy() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePreDestroy(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedMethod newNamedMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AroundInvoke newAroundInvoke() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceContextRef newPersistenceContextRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PersistenceUnitRef newPersistenceUnitRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LifecycleCallback newLifecycleCallback() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbJar getRoot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbClass(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(int index, EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnvEntry(EnvEntry[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEnvEntry(EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEnvEntry(EnvEntry value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EnvEntry newEnvEntry() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(int index, EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef getEjbRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbRef(EjbRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef[] getEjbRef() {
        initLocalAndRemoteEjbRefs();
        return ejbRefs;
    }

    public int removeEjbRef(EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEjbRef(EjbRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbRef newEjbRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbLocalRef(int index, EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef getEjbLocalRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEjbLocalRef(EjbLocalRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef[] getEjbLocalRef() {
        initLocalAndRemoteEjbRefs();
        return ejbLocalRefs;
    }

    public int addEjbLocalRef(EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEjbLocalRef(EjbLocalRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EjbLocalRef newEjbLocalRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityIdentity getSecurityIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityIdentity(SecurityIdentity value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityIdentity newSecurityIdentity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(int index, ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceRef(ResourceRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceRef(ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceRef(ResourceRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceRef newResourceRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(int index, ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResourceEnvRef(ResourceEnvRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addResourceEnvRef(ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeResourceEnvRef(ResourceEnvRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResourceEnvRef newResourceEnvRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(int index, ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeServiceRef(ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addServiceRef(ServiceRef value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceRef newServiceRef() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDefaultDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDisplayName(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean addBean(String beanName, String[] propertyNames,
                                Object[] propertyValues, String keyProperty) throws ClassNotFoundException,
                                                                                    NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean findBeanByName(String beanName, String propertyName,
                                       String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllIcons(String[] locales, String[] smallIcons,
                            String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon getDefaultIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // </editor-fold>

    private static class ActivationConfigPropertyHandler implements ArrayValueHandler {

        private final AnnotationModelHelper helper;
        private final ActivationConfig config;

        public ActivationConfigPropertyHandler(AnnotationModelHelper helper, ActivationConfig config) {
            this.helper = helper;
            this.config = config;
        }

        @Override
        public Object handleArray(List<AnnotationValue> arrayMembers) {
            for (AnnotationValue arrayMember : arrayMembers) {
                Object arrayMemberValue = arrayMember.getValue();
                if (arrayMemberValue instanceof AnnotationMirror) {
                    AnnotationParser parser = AnnotationParser.create(helper);
                    parser.expectString("propertyName", null);      //NOI18N
                    parser.expectString("propertyValue", null);     //NOI18N
                    ParseResult pr = parser.parse((AnnotationMirror) arrayMemberValue);

                    // fill up the ActivationConfig
                    ActivationConfigPropertyImpl property = new ActivationConfigPropertyImpl(
                            pr.get("propertyName", String.class),   //NOI18N
                            pr.get("propertyValue", String.class)); //NOI18N
                    config.addActivationConfigProperty(property);
                }
            }
            return null;
        }

    }

}
 
