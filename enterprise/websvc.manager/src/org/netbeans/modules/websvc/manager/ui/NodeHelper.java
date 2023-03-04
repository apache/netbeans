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
package org.netbeans.modules.websvc.manager.ui;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.ErrorManager;

/**
 *
 * @author quynguyen
 */
public final class NodeHelper {
    private final URLClassLoader loader;
    
    private static NodeHelper instance;
    
    private NodeHelper(URLClassLoader loader) {
        this.loader = loader;
    }
    
    public static NodeHelper createInstance(URLClassLoader loader) {
        NodeHelper result = new NodeHelper(loader);
        NodeHelper.instance = result;
        
        return result;
    }
    
    public static NodeHelper getInstance() {
        return instance;
    }
    
    private URLClassLoader getRuntimeClassLoader() {
        return loader;
    }
    
    /**
     * Creates the node and all the child nodes with their associated TypeNodeData objects
     * 
     * @param data the top-level node data
     * @return a tree node with fully initialized children
     */
    public DefaultMutableTreeNode createNodeFromData(TypeNodeData data) 
            throws WebServiceReflectionException {
        DefaultMutableTreeNode node = null;
        
        /**
         * Now create the appropriate node to be added.
         */
        String parameterClass = data.getTypeClass();
        if (ReflectionHelper.isHolder(parameterClass)) {
            node = new HolderTypeTreeNode(data, this.getRuntimeClassLoader());
            
            DefaultMutableTreeNode childNode = null;

            String holderType = data.getGenericType();
            if (holderType == null) {
                holderType = "java.lang.Object"; // NOI18N
                data.setGenericType(holderType);
            }
            
            TypeNodeData heldData = ReflectionHelper.createTypeData(holderType, "value"); // NOI18N
            heldData.setTypeValue(getParameterDefaultValue(heldData));
            heldData.setAssignable(data.isAssignable());
            
            // also initialize the Holder.value field in the parent data
            ReflectionHelper.setHolderValue(data.getTypeValue(), heldData.getTypeValue());
            
            childNode = createNodeFromData(heldData);

            node.add(childNode);
        } else if (ReflectionHelper.isJAXBElement(parameterClass)) {
            node = new JAXBElementTreeNode(data, this.getRuntimeClassLoader());
            
            TypeNodeData localPartData = ReflectionHelper.createTypeData("java.lang.String", "localPart"); // NOI18N
            DefaultMutableTreeNode localPartNode = new DefaultMutableTreeNode(localPartData);
            node.add(localPartNode);
            
            String valueType = data.getGenericType();
            if (valueType == null) {
                valueType = "java.lang.Object"; // NOI18N
                data.setGenericType(valueType);
            }
            
            TypeNodeData heldData = ReflectionHelper.createTypeData(valueType, "value"); // NOI18N
            heldData.setTypeValue(getParameterDefaultValue(heldData));
            heldData.setAssignable(data.isAssignable());
            
            DefaultMutableTreeNode valueNode = createNodeFromData(heldData);
            node.add(valueNode);
        } else if (ReflectionHelper.isArray(parameterClass)) {
            node = new ArrayTypeTreeNode(data, this.getRuntimeClassLoader());
            
            addParameterArrayInstances(node);
        } else if (ReflectionHelper.isCollection(data.getTypeClass(), getRuntimeClassLoader())) {
            node = new ListTypeTreeNode(data, this.getRuntimeClassLoader());

            addParameterArrayInstances(node);
        } else if (ReflectionHelper.isComplexType(data.getTypeClass(), getRuntimeClassLoader())) {
            node = new StructureTypeTreeNode(data, this.getRuntimeClassLoader());

            traverseType(node);
        } else {
            node = new DefaultMutableTreeNode(data);
        }
        
        return node;
    }
    
    public DefaultMutableTreeNode createResultNodeFromData(TypeNodeData data) throws WebServiceReflectionException {
        DefaultMutableTreeNode node = null;
        
        String typeClass = data.getTypeClass();
        if (ReflectionHelper.isCollection(typeClass, getRuntimeClassLoader())) {
            node = new ListTypeTreeNode(data, this.getRuntimeClassLoader());
            addResultArrayInstances(node);
        }else if (ReflectionHelper.isComplexType(typeClass, getRuntimeClassLoader())) {
            node = new StructureTypeTreeNode(data, this.getRuntimeClassLoader());
            traverseResultType(node);
        }else {
            node = new DefaultMutableTreeNode(data);
        }
        
        return node;
    }
    
    private void traverseType(DefaultMutableTreeNode inNode) throws WebServiceReflectionException {
        
        if(null == inNode) {
            return;
        }
        
        TypeNodeData inData = (TypeNodeData)inNode.getUserObject();
        String inType = inData.getTypeClass();
        
        List<String> members = ReflectionHelper.getPropertyNames(inType, getRuntimeClassLoader());
        for (String fieldName : members) {
            String fieldType = ReflectionHelper.getPropertyType(inType, fieldName, getRuntimeClassLoader());
            TypeNodeData childData = ReflectionHelper.createTypeData(fieldType, fieldName);
            childData.setTypeValue(getParameterDefaultValue(childData));
            
            if (!ReflectionHelper.isPropertySettable(inType, fieldName, getRuntimeClassLoader())) {
                childData.setAssignable(false);
            }
            
            DefaultMutableTreeNode childNode = createNodeFromData(childData);
            inNode.add(childNode);
        }
    }
    
    private void traverseResultType(DefaultMutableTreeNode inParentNode) throws WebServiceReflectionException {
        if(null == inParentNode) {
            return;
        }
        
        TypeNodeData parentData = (TypeNodeData)inParentNode.getUserObject();
        String parentType = parentData.getTypeClass();
        Object parentValue = parentData.getTypeValue();
        
        if (parentValue != null) {
            List<String> members = ReflectionHelper.getPropertyNames(parentType, getRuntimeClassLoader());
            for (String fieldName : members) {
                String fieldType = ReflectionHelper.getPropertyType(parentType, fieldName, getRuntimeClassLoader());
                Object fieldValue = ReflectionHelper.getPropertyValue(parentValue, fieldName, getRuntimeClassLoader());
                
                TypeNodeData resultData = ReflectionHelper.createTypeData(fieldType, fieldName, fieldValue);
                DefaultMutableTreeNode resultNode = createResultNodeFromData(resultData);
                
                inParentNode.add(resultNode);
            }
        }
    }
    
    private void addParameterArrayInstances(DefaultMutableTreeNode parentNode) throws WebServiceReflectionException {
        /**
         * Now add some instances of the array.
         * TODO: figure out some better way to pick the number of instances of the array to create
         */
        TypeNodeData inData = (TypeNodeData)parentNode.getUserObject();
        //String inTypeClass = inData.getTypeClass();
        String elementType = inData.getGenericType();
        
        for(int ii=0; ii < 9; ii++) {
            TypeNodeData data = ReflectionHelper.createTypeData(elementType, "[" + ii + "]"); // NOI18N
            data.setTypeValue(getParameterDefaultValue(data));
            data.setAssignable(inData.isAssignable());
            
            DefaultMutableTreeNode childNode = createNodeFromData(data);
            parentNode.add(childNode);
        }
    }
    
    private void addResultArrayInstances(DefaultMutableTreeNode parentNode) throws WebServiceReflectionException {
        /**
         * The result value is an array of a certain type that needs to be shown.
         *  1. first get the type of the array.
         *  2. create a node for each occurance of the array.
         *
         */
        TypeNodeData parentData = (TypeNodeData)parentNode.getUserObject();
        //String parentType = parentData.getTypeClass();

        String elementType = parentData.getGenericType();
        if (elementType == null) {
            elementType = "java.lang.Object"; // NOI18N
            parentData.setGenericType(elementType);
        }
        
        Collection collection = (Collection)parentData.getTypeValue();
        if (collection == null) return;
        
        int i = 0;
        for (Object nextValue : collection) {
            TypeNodeData childData = ReflectionHelper.createTypeData(elementType, "[" + i + "]", nextValue);
            DefaultMutableTreeNode childNode = createResultNodeFromData(childData);
            parentNode.add(childNode);
            
            i += 1;
        }
    }
    
    /**
     * This method will create a default value from a TypeNodeData object
     */
    public Object getParameterDefaultValue(TypeNodeData data) {
        assert data != null;
        
        try {
            String typeClass = data.getTypeClass();

            if (ReflectionHelper.isComplexType(typeClass, getRuntimeClassLoader())) {
                return ReflectionHelper.makeComplexType(typeClass, getRuntimeClassLoader());
            } else if (ReflectionHelper.isCollection(typeClass, getRuntimeClassLoader())) {
                return ReflectionHelper.makeCollection(typeClass, getRuntimeClassLoader());
            } else if (ReflectionHelper.isHolder(typeClass)) {
                return ReflectionHelper.makeHolder(getRuntimeClassLoader());
            }else if (ReflectionHelper.isEnumeration(typeClass, getRuntimeClassLoader())) {
                return ReflectionHelper.makeEnumeration(typeClass, getRuntimeClassLoader());
            }else if (ReflectionHelper.isArray(typeClass)) {
                return ReflectionHelper.makeGenericArray(data.getGenericType(), 0, getRuntimeClassLoader());
            } else {
                String currentType = typeClass;
                Object value = null;

                if (currentType.equals(int.class.getName()) || currentType.equals(Integer.class.getName())) {
                    value = 0;
                } else if (currentType.equals(byte.class.getName()) || currentType.equals(Byte.class.getName())) {
                    value = Byte.valueOf("0");
                } else if (currentType.equals(boolean.class.getName()) || currentType.equals(Boolean.class.getName())) {
                    value = false;
                } else if (currentType.equals(float.class.getName()) || currentType.equals(Float.class.getName())) {
                    value = Float.valueOf(0);
                } else if (currentType.equals(double.class.getName()) || currentType.equals(Double.class.getName())) {
                    value = Double.valueOf(0);
                } else if (currentType.equals(long.class.getName()) || currentType.equals(Long.class.getName())) {
                    value = Long.valueOf(0);
                } else if (currentType.equals(short.class.getName()) || currentType.equals(Short.class.getName())) {
                    value = Short.valueOf("0");
                } else if (currentType.equals(String.class.getName())) {
                    value = "";
                } else if (currentType.equals(BigDecimal.class.getName())) {
                    value = BigDecimal.valueOf(0);
                } else if (currentType.equals(BigInteger.class.getName())) {
                    value = BigInteger.valueOf(0);
                } else if (currentType.equals(URI.class.getName())) {
                    try {
                        value = new URI("http://java.sun.com");
                    } catch (URISyntaxException uri) {
                    }
                } else if (currentType.equals(Calendar.class.getName())) {
                    value = Calendar.getInstance();
                } else if (currentType.equalsIgnoreCase(Date.class.getName())) {
                    value = new Date();
                }

                return value;
            }
        } catch (WebServiceReflectionException wsre) {
            Throwable cause = wsre.getCause();
            ErrorManager.getDefault().notify(cause);
            ErrorManager.getDefault().log(this.getClass().getName() + ": Error trying to do Class.forName on: " + data.getRealTypeName() + "WebServiceReflectionException=" + cause);

            return null;
        }
    }

}
