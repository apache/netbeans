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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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
import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.InitMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback;
import org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef;
import org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef;
import org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.EjbRefHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.openide.util.Exceptions;

public class SessionImpl extends PersistentObject implements Session {
    
    protected enum Kind { STATELESS, STATEFUL, SINGLETON }
    
    private final Kind kind;
    
    // persistent
    private String ejbName;
    private String ejbClass;
    private String sessionType;
    
    // lazy initialization
    private Set<String> interfacesSet;
    private Set<String> businessLocal = new HashSet<String>();
    private Set<String> businessRemote = new HashSet<String>();
    private EjbRef[] ejbRefs;
    private EjbLocalRef[] ejbLocalRefs;
    private ServiceRef[] serviceRefs;
    private ResourceRef[] resourceRefs;
    private ResourceEnvRef[] resourceEnvRefs = null;
    private EnvEntry[] envEntries = null;
    private MessageDestinationRef[] messageDestinationRefs = null;
    private boolean localBean = false;
    private String serviceEndpoint = null;

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    public SessionImpl(Kind kind, AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        this.kind = kind;
        this.interfacesSet = new HashSet<String>();
        boolean valid = refresh(typeElement);
        assert valid;
    }
    
    boolean refresh(TypeElement typeElement) {
        Map<String, ? extends AnnotationMirror> annByType = getHelper().getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror annotationMirror = null;
        switch(kind){
            case STATELESS:
                annotationMirror = annByType.get("jakarta.ejb.Stateless"); //NOI18N
                if (annotationMirror == null) {
                    annotationMirror = annByType.get("javax.ejb.Stateless"); //NOI18N
                }
                sessionType = Session.SESSION_TYPE_STATELESS;
                break;
            case STATEFUL:
                annotationMirror = annByType.get("jakarta.ejb.Stateful"); //NOI18N
                if (annotationMirror == null) {
                    annotationMirror = annByType.get("javax.ejb.Stateful"); //NOI18N
                }
                sessionType = Session.SESSION_TYPE_STATEFUL;
                break;
            case SINGLETON: 
                annotationMirror = annByType.get("jakarta.ejb.Singleton"); //NOI18N
                if (annotationMirror == null) {
                    annotationMirror = annByType.get("javax.ejb.Singleton"); //NOI18N
                }
                sessionType = Session.SESSION_TYPE_SINGLETON;
                break;
        }
        if (annotationMirror == null) {
            return false;
        }

        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectString("name", parser.defaultValue(typeElement.getSimpleName().toString())); // NOI18N
        ParseResult parseResult = parser.parse(annotationMirror);
        String oldEjbName = ejbName;
        ejbName = parseResult.get("name", String.class); // NOI18N
        if (ejbName != null && !ejbName.equals(oldEjbName)){
            fireChange(new PropertyChangeEvent(this, EJB_NAME, oldEjbName, ejbName));
        }
        ejbClass = typeElement.getQualifiedName().toString();

        initBusinessInterfaces();

        localBean = annByType.get("jakarta.ejb.LocalBean") != null || annByType.get("javax.ejb.LocalBean") != null;

        return true;
    }
    
    // <editor-fold desc="Helpers">
    
    /**
     * Initializes businessLocal and businessRemote fields
     */
    private void initBusinessInterfaces() {
        Set<String> businessLocalOld = new HashSet<String>(businessLocal);
        Set<String> businessRemoteOld = new HashSet<String>(businessRemote);
        // try to remember set of interfaces from last initialization
        // and if it is changed, reinitialize again
        
        /*
        This optimization does not work properly
        (see http://www.netbeans.org/issues/show_bug.cgi?id=124295).
        If an interface is changed from Local to Remote (or vice versa)
        then interfacesSet remains the same and variables
        businessLocal and businessRemote are not refreshed which leads
        to problems reported in the issue above...
        
        boolean reinit = false;
        
        TypeElement typeElement = getTypeElement();
        List<? extends TypeMirror> interfacesTypes = typeElement.getInterfaces();
        if (interfacesTypes.size() != interfacesSet.size()) {
            reinit = true;
        } else {
            for (TypeMirror typeMirror : typeElement.getInterfaces()) {
                if (TypeKind.DECLARED == typeMirror.getKind()) {
                    TypeElement interfaceTypeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
                    if (!interfacesSet.contains(interfaceTypeElement.getQualifiedName().toString())) {
                        reinit = true;
                        break;
                    }
                }
            }
        }
        
        if (!reinit) {
            return;
        }
        */
        TypeElement typeElement = getTypeElement();

        interfacesSet = new HashSet<String>();
        businessLocal.clear();
        businessRemote.clear();
        
        if (typeElement == null) {
            return;
        }

        List<TypeElement> interfaces = new ArrayList<TypeElement>(); // all business interface candidates, EJB 3.0 Spec, Chapter 10.2
        for (TypeMirror typeMirror : typeElement.getInterfaces()) {
            if (TypeKind.DECLARED == typeMirror.getKind()) {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                Element element = declaredType.asElement();
                if (ElementKind.INTERFACE == element.getKind()) {
                    TypeElement interfaceTypeElement = (TypeElement) element;
                    String fqn = interfaceTypeElement.getQualifiedName().toString();
                    interfacesSet.add(fqn);
                    if (!"java.io.Serializable".equals(fqn) && !"java.io.Externalizable".equals(fqn) && !fqn.startsWith("javax.ejb") && !fqn.startsWith("jakarta.ejb")) {
                        interfaces.add(interfaceTypeElement);
                    }
                }
            }
        }
        
        Map<String, ? extends AnnotationMirror> annByType = getHelper().getAnnotationsByType(typeElement.getAnnotationMirrors());

        AnnotationMirror beanLocalAnnotation = annByType.get("jakarta.ejb.Local"); // @Local at bean class
        if (beanLocalAnnotation == null) {
            beanLocalAnnotation = annByType.get("javax.ejb.Local"); // @Local at bean class
        }
        boolean isEmptyBeanLocalAnnotation = beanLocalAnnotation != null && beanLocalAnnotation.getElementValues().isEmpty();
        AnnotationMirror beanRemoteAnnotation = annByType.get("jakarta.ejb.Remote"); // @Remote at beans class
        if (beanRemoteAnnotation == null) {
            beanRemoteAnnotation = annByType.get("javax.ejb.Remote"); // @Remote at beans class
        }
        boolean isEmptyBeanRemoteAnnotation = beanRemoteAnnotation != null && beanRemoteAnnotation.getElementValues().isEmpty();
        
        List<String> annotatedLocalInterfaces = new ArrayList<String>();
        List<String> annotatedRemoteInterfaces = new ArrayList<String>();
        List<String> allInterfaces = new ArrayList<String>();
        
        for (TypeElement interfaceTypeElement : interfaces) {
            Map<String, ? extends AnnotationMirror> ifaceAnnByType = getHelper().getAnnotationsByType(interfaceTypeElement.getAnnotationMirrors());
            if (ifaceAnnByType.get("jakarta.ejb.Local") != null || ifaceAnnByType.get("javax.ejb.Local") != null) {
                annotatedLocalInterfaces.add(interfaceTypeElement.getQualifiedName().toString());
            }
            if (ifaceAnnByType.get("jakarta.ejb.Remote") != null || ifaceAnnByType.get("javax.ejb.Remote") != null) {
                annotatedRemoteInterfaces.add(interfaceTypeElement.getQualifiedName().toString());
            }
            allInterfaces.add(interfaceTypeElement.getQualifiedName().toString());
        }

        boolean isNoIfaceView = annByType.get("javax.ejb.LocalBean") != null || annByType.get("jakarta.ejb.LocalBean") != null; //NOI18N
                                     // any interface is explicitly specified
        boolean isAnyIfaceExplicit = !annotatedRemoteInterfaces.isEmpty() || !annotatedLocalInterfaces.isEmpty()
                // annotated class with non-empty value
                || (beanLocalAnnotation != null && !isEmptyBeanLocalAnnotation)
                || (beanRemoteAnnotation != null && !isEmptyBeanRemoteAnnotation);

        if (interfaces.size() == 1) {
            if (!isNoIfaceView && !isAnyIfaceExplicit) {
                if (beanRemoteAnnotation != null) {
                    businessRemote.add(interfaces.get(0).getQualifiedName().toString());
                }
                businessLocal.add(interfaces.get(0).getQualifiedName().toString()) ;
            } else {
                if (beanRemoteAnnotation == null && annotatedRemoteInterfaces.isEmpty()) {
                    businessLocal.add(interfaces.get(0).getQualifiedName().toString()) ;
                } else if (beanLocalAnnotation == null && annotatedLocalInterfaces.isEmpty()) {
                    businessRemote.add(interfaces.get(0).getQualifiedName().toString());
                }
            }
        } else {
            if (!isNoIfaceView && !isAnyIfaceExplicit) {
                if (beanRemoteAnnotation != null) {
                    businessRemote.addAll(allInterfaces);
                }
                businessLocal.addAll(allInterfaces);
            } else {
                if (beanLocalAnnotation != null) {
                    List<String> annotationsValues = getClassesFromLocalOrRemote(beanLocalAnnotation);
                    businessLocal.addAll(annotationsValues);
                } else {
                    businessLocal.addAll(annotatedLocalInterfaces);
                }
                if (beanRemoteAnnotation != null) {
                    List<String> annotationsValues = getClassesFromLocalOrRemote(beanRemoteAnnotation);
                    businessRemote.addAll(annotationsValues);
                } else {
                    businessRemote.addAll(annotatedRemoteInterfaces);
                }
            }
        }

        if (!businessLocalOld.equals(businessLocal)){
            fireChange(new PropertyChangeEvent(this, BUSINESS_LOCAL, businessLocalOld, businessLocal));
        }
        if (!businessRemoteOld.equals(businessRemote)){
            fireChange(new PropertyChangeEvent(this, BUSINESS_REMOTE, businessRemoteOld, businessRemote));
        }
    }
    
    /**
     * Extracts Class[] from @Local and @Remote annotations
     */
    private List<String> getClassesFromLocalOrRemote(AnnotationMirror beanLocalAnnotation) {
        final List<String> result = new ArrayList<String>();
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectClassArray("value", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    TypeMirror typeMirror = (TypeMirror) arrayMember.getValue();
                    if (TypeKind.DECLARED == typeMirror.getKind()) {
                        DeclaredType declaredType = (DeclaredType) typeMirror;
                        Element element = declaredType.asElement();
                        if (ElementKind.INTERFACE == element.getKind()) {
                            TypeElement interfaceTypeElement = (TypeElement) element;
                            result.add(interfaceTypeElement.getQualifiedName().toString());
                        }
                    }
                }
                return null;
            }
        }, null);
        parser.parse(beanLocalAnnotation);
        return result;
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
    
    private void initServiceRefs() {
        
        if (serviceRefs != null) {
            return;
        }
        serviceRefs = CommonAnnotationHelper.getServiceRefs(getHelper(), getTypeElement());
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

    private void initServiceEndpoint() {
        if (serviceEndpoint != null) {
            return;
        }
        serviceEndpoint = CommonAnnotationHelper.getServiceEndpoint(getHelper(), getTypeElement());
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.removePropertyChangeListener(pcl);
    }

    private void fireChange(PropertyChangeEvent pce){
        changeSupport.firePropertyChange(pce);
    }
    // </editor-fold>

    // <editor-fold desc="Model implementation">
    
    public String getEjbName() {
        return ejbName;
    }
    
    public String getEjbClass() {
        return ejbClass;
    }
    
    public String getSessionType() {
        return sessionType;
    }
    
    public String[] getBusinessLocal() throws VersionNotSupportedException {
        initBusinessInterfaces();
        return businessLocal.toArray(new String[0]);
    }
    
    public String[] getBusinessRemote() throws VersionNotSupportedException {
        initBusinessInterfaces();
        return businessRemote.toArray(new String[0]);
    }
    
    public EjbRef[] getEjbRef() {
        initLocalAndRemoteEjbRefs();
        return ejbRefs;
    }
    
    public EjbLocalRef[] getEjbLocalRef() {
        initLocalAndRemoteEjbRefs();
        return ejbLocalRefs;
    }
    
    public ServiceRef getServiceRef(int index) throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs[index];
    }
    
    public ServiceRef[] getServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs;
    }
    
    public int sizeServiceRef() throws VersionNotSupportedException {
        initServiceRefs();
        return serviceRefs.length;
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
    
    public String getServiceEndpoint() throws VersionNotSupportedException {
        initServiceEndpoint();
        return serviceEndpoint;
    }
    
    public String getDefaultDisplayName() {
        // TODO
        return getEjbName();
    }
    
    public String getLocal() {
        // TODO
        try {
            String[] businessLocal = getBusinessLocal();
            if (businessLocal != null && businessLocal.length > 0) {
                return businessLocal[0];
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public String getRemote() {
        // TODO
        try {
            String[] businessRemote = getBusinessRemote();
            if (businessRemote != null && businessRemote.length > 0) {
                return businessRemote[0];
            }
        } catch (VersionNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public String getLocalHome() {
        // TODO
        return null;
    }
    
    public String getHome() {
        // TODO
        return null;
    }

    public boolean isLocalBean(){
        return localBean;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSessionType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getTransactionType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setTransactionType(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setServiceEndpoint(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setMappedName(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getMappedName() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessLocal(int index, String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getBusinessLocal(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeBusinessLocal() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessLocal(String[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addBusinessLocal(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeBusinessLocal(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessRemote(int index, String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getBusinessRemote(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeBusinessRemote() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setBusinessRemote(String[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addBusinessRemote(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeBusinessRemote(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setTimeoutMethod(NamedMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NamedMethod getTimeoutMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setInitMethod(int index, InitMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public InitMethod getInitMethod(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeInitMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setInitMethod(InitMethod[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public InitMethod[] getInitMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addInitMethod(InitMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeInitMethod(InitMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setRemoveMethod(int index, RemoveMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public RemoveMethod getRemoveMethod(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeRemoveMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setRemoveMethod(RemoveMethod[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public RemoveMethod[] getRemoveMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addRemoveMethod(RemoveMethod valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeRemoveMethod(RemoveMethod valueInterface) throws VersionNotSupportedException {
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
    
    public void setPostActivate(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback getPostActivate(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePostActivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPostActivate(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback[] getPostActivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPostActivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePostActivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPrePassivate(int index, LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback getPrePassivate(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizePrePassivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPrePassivate(LifecycleCallback[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public LifecycleCallback[] getPrePassivate() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addPrePassivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removePrePassivate(LifecycleCallback valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NamedMethod newNamedMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public InitMethod newInitMethod() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public RemoveMethod newRemoveMethod() throws VersionNotSupportedException {
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
    
    public void setHome(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setRemote(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLocal(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setLocalHome(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSecurityRoleRef(int index, SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityRoleRef getSecurityRoleRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSecurityRoleRef(SecurityRoleRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityRoleRef[] getSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int sizeSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int removeSecurityRoleRef(SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int addSecurityRoleRef(SecurityRoleRef value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SecurityRoleRef newSecurityRoleRef() {
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

}

