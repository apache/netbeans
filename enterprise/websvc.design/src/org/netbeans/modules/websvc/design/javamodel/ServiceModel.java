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

package org.netbeans.modules.websvc.design.javamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
public class ServiceModel {
    
    public static final int STATUS_OK=0;
    public static final int STATUS_NOT_SERVICE=1;
    public static final int STATUS_INCORRECT_SERVICE=2;
    
    String serviceName;
    String portName;
    String name;
    String endpointInterface;
    String wsdlLocation;
    String targetNamespace;
    int status = STATUS_OK;
    
    List<MethodModel> operations;    
    
    private FileObject implementationClass;
    private FileChangeListener fcl;
    private List<ServiceChangeListener> serviceChangeListeners
            = new ArrayList<ServiceChangeListener>();
    
    private static final RequestProcessor FILE_CHANGE_RP = 
            new RequestProcessor(ServiceModel.class);
    
    /** Creates a new instance of ServiceModel */
    private ServiceModel(FileObject implementationClass, boolean populate) {
        this.implementationClass=implementationClass;
        if ( populate ){
            assert implementationClass != null;
            populateModel();
        }
    }
    
    private boolean changeSource=true;
    
    public static ServiceModel getServiceModel(FileObject implClass) {
        return new ServiceModel(implClass, true);
    }
    
    static ServiceModel getServiceModel() {
        return new ServiceModel(null, false);
    }

    public FileObject getImplementationClass() {
        return implementationClass;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name != null && !this.name.equals(name)) {
            if (changeSource) {
                JaxWsUtils.setWebServiceAttrValue(implementationClass, "name", name); //NOI18N
            }
            String oldName = this.name;
            this.name = name==null?implementationClass.getName():name;
            firePropertyChanged("name", oldName, name);
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        
        if (this.serviceName!= null && !this.serviceName.equals(serviceName)) {
            if (changeSource) {
                JaxWsUtils.setWebServiceAttrValue(implementationClass, "serviceName", serviceName); //NOI18N
            }
            String oldName = this.serviceName;
            this.serviceName = serviceName==null?implementationClass.getName()+"Service":serviceName; //NOI18N
            firePropertyChanged("serviceName", oldName, this.serviceName);
        }
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        if (this.portName != null && !this.portName.equals(portName)) {
            if (changeSource) {
                JaxWsUtils.setWebServiceAttrValue(implementationClass, "portName", portName); //NOI18N
            }
            String oldName = this.portName;
            this.portName = portName==null?this.serviceName+"Port":portName; //NOI18N
            firePropertyChanged("portName", oldName, portName);
        }
    }

    public String getEndpointInterface() {
        return endpointInterface;
    }

    void setEndpointInterface(String endpointInterface) {
        if (this.endpointInterface==null) {
            if (endpointInterface!=null) {
                this.endpointInterface=endpointInterface;
                firePropertyChanged("endpointInterface", null, endpointInterface);
            }
        } else if (!this.endpointInterface.equals(endpointInterface)) {
            String oldName = this.endpointInterface;
            this.endpointInterface = endpointInterface;
            firePropertyChanged("endpointInterface", oldName, endpointInterface);
        }
    }
    
    public String getWsdlLocation() {
        return wsdlLocation;
    }

   void setWsdlLocation(String wsdlLocation) {
        if (this.wsdlLocation==null) {
            if (wsdlLocation!=null) {
                this.wsdlLocation=wsdlLocation;
                firePropertyChanged("wsdlLocation", null, wsdlLocation);
            }
        } else if (!this.wsdlLocation.equals(wsdlLocation)) {
            String oldName = this.wsdlLocation;
            this.wsdlLocation = wsdlLocation;
            firePropertyChanged("wsdlLocation", oldName, wsdlLocation);
        }
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        if (this.targetNamespace==null) {
            if (targetNamespace!=null) {
                this.targetNamespace=targetNamespace;
                firePropertyChanged("targetNamespace", null, targetNamespace);
            }
        } else if (!this.targetNamespace.equals(targetNamespace)) {
            String oldName = this.targetNamespace;
            this.targetNamespace = targetNamespace;
            firePropertyChanged("targetNamespace", oldName, targetNamespace);
        }
    }

    public List<MethodModel> getOperations() {
        return operations;
    }
    
    void addOperation(MethodModel operation) {
        operations.add(operation);
        fireOperationAdded(operation);
    }
    
    void removeOperation(MethodModel operation) {
        operations.remove(operation);
        fireOperationRemoved(operation);
    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (this.status!=status) {
            int oldStatus = this.status;
            this.status = status;
            firePropertyChanged("status", String.valueOf(oldStatus),String.valueOf(status));
        }
        this.status = status;
    }
    
    void setOperations(List<MethodModel> operations) {
        if (this.operations == null) {
            //this.operations = operations;
            this.operations = new ArrayList<MethodModel>();
            if ( operations != null ){
                for (MethodModel op:operations) {
                addOperation(op);
                }
            }
        } else {
            Map<String, MethodModel> op1 = new HashMap<String, MethodModel>();
            Map<String, MethodModel> op2 = new HashMap<String, MethodModel>();
            for (MethodModel model:this.operations) {
                op1.put(model.getOperationName(), model);
            }
            if ( operations != null ){
                for (MethodModel model:operations) {
                    op2.put(model.getOperationName(), model);
                }
            }
            // looking for common operations (operationName)
            Set<String> commonOperations = new HashSet<String>();
            Set<String> keys1 = op1.keySet();
            Set<String> keys2 = op2.keySet();
            for(String key:keys1) {
                if (keys2.contains(key)) commonOperations.add(key);
            }

            for (String key:commonOperations) {
                MethodModel method1 = op1.get(key);
                MethodModel method2 = op2.get(key);
                op1.remove(key);
                op2.remove(key);
                // comparing if something has changed in method
                if (!method1.isEqualTo(method2)) {
                    this.operations.set(this.operations.indexOf(method1), method2);
                    fireOperationChanged(method1,method2);
                }
            }
            // op1 contains methods present in model1 that are not in model2
            // op2 contains methods present in model2 that are not in model1
            op1.values().forEach(this::removeOperation);
            op2.values().forEach(this::addOperation);
        }
    }

    
    public synchronized void addServiceChangeListener(ServiceChangeListener pcl) {
        if (serviceChangeListeners.size()==0) {
            fcl = new AnnotationChangeListener();
            implementationClass.addFileChangeListener(fcl);
            if ( getEndpointInterface() != null ){
                String endpoint = getEndpointInterface();
                ClassPath sourceCP = ClassPath.getClassPath(implementationClass, 
                        ClassPath.SOURCE);
                FileObject endpointFo = sourceCP.findResource(
                        endpoint.replace('.', '/')+".java"); //NOI18N
                if ( endpointFo != null ){
                    endpointFo.addFileChangeListener(fcl);
                }
            }
        }
        serviceChangeListeners.add(pcl);
    }
    
    public synchronized void removeServiceChangeListener(ServiceChangeListener pcl) {
        serviceChangeListeners.remove(pcl);
        if (serviceChangeListeners.size()==0) {
            implementationClass.removeFileChangeListener(fcl);
            if ( getEndpointInterface() != null ){
                String endpoint = getEndpointInterface();
                ClassPath sourceCP = ClassPath.getClassPath(implementationClass, 
                        ClassPath.SOURCE);
                FileObject endpointFo = sourceCP.findResource(
                        endpoint.replace('.', '/')+".java"); //NOI18N
                if ( endpointFo != null ){
                    endpointFo.removeFileChangeListener(fcl);
                }
            }
        }
    }
    
    void firePropertyChanged(String propName, String oldValue, String newValue) {
        for (ServiceChangeListener listener:serviceChangeListeners) {
            listener.propertyChanged(propName, oldValue, newValue);
        }
    }
    void fireOperationAdded(MethodModel method) {
        for (ServiceChangeListener listener:serviceChangeListeners) {
            listener.operationAdded(method);
        }
    }
    void fireOperationRemoved(MethodModel method) {
        for (ServiceChangeListener listener:serviceChangeListeners) {
            listener.operationRemoved(method);
        }
    }
    void fireOperationChanged(MethodModel oldMethod, MethodModel newMethod) {
        for (ServiceChangeListener listener:serviceChangeListeners) {
            listener.operationChanged(oldMethod,newMethod);
        }
    }
    
    private class AnnotationChangeListener extends FileChangeAdapter {
        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        @Override
        public void fileChanged(final FileEvent fe) {
            
            Runnable runnable = new Runnable() {
                
                @Override
                public void run() {
                    FileObject implClass = fe.getFile();
                    ServiceModel newModel = new ServiceModel(implClass,true);
                    ServiceModel.this.mergeModel(newModel);
                }
            };
            FILE_CHANGE_RP.post(runnable);
        }
        
        /** Fired when a file is deleted.
        * @param fe the event describing context where action has taken place
        */
        @Override
        public void fileDeleted(FileEvent fe) {
        }
         /** Fired when a file is renamed.
        * @param fe the event describing context where action has taken place
        *           and the original name and extension.
        */
        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }
    }
    
    private void populateModel() {
        ServiceModel model = Utils.populateModel(implementationClass);
        if ( model == null ){
            return;
        }
        serviceName = model.serviceName;
        portName = model.portName;
        name = model.name;
        endpointInterface = model.endpointInterface;
        wsdlLocation = model.wsdlLocation;
        targetNamespace = model.targetNamespace;
        status = model.status;
        operations = model.operations;  
    }
    
    /** package private due to test functionality */ 
    void mergeModel(ServiceModel model2) {
        if (model2.status == STATUS_INCORRECT_SERVICE || model2.status == STATUS_NOT_SERVICE) {
           if (this.operations != null && (model2.operations == null || model2.operations.size() == 0)) {
               int size=this.operations.size();
               for (int i=size-1;i>=0;i--) {
                   removeOperation(this.operations.get(i));
               }
           }
        }
        else {
            changeSource=false;
            setStatus(model2.status);
            setName(model2.name);
            setServiceName(model2.serviceName);
            setPortName(model2.portName);
            setEndpointInterface(model2.endpointInterface);
            setWsdlLocation(model2.wsdlLocation);
            setTargetNamespace(model2.targetNamespace);
            setOperations(model2.operations);
            changeSource=true;
        }
    }

    /* probably not needed
    public boolean isEqualTo(ServiceModel model) {
        if (!serviceName.equals(model.serviceName)) return false;
        if (!portName.equals(model.portName)) return false;
        if (!name.equals(model.name)) return false;
        if (!Utils.isEqualTo(wsdlLocation, model.wsdlLocation)) return false;
        if (!Utils.isEqualTo(targetNamespace, model.targetNamespace)) return false;
        if (!Utils.isEqualTo(endpointInterface, model.endpointInterface)) return false;
        if (operations.size()!=model.operations.size()) return false;
        for(int i = 0;i<operations.size();i++) {
            if (!operations.get(i).isEqualTo(model.operations.get(i))) return false;
        }
        return true;
    }
    */ 
}
