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

package org.netbeans.modules.debugger.jpda.heapwalk;

import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAArrayType;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;

import org.openide.ErrorManager;

/**
 *
 * @author Martin Entlicher
 */
public class JavaClassImpl implements JavaClass {
    
    private JPDAClassType classType;
    private long instanceCount = -1L;
    private HeapImpl heap;
    private String className;
    
    /** Creates a new instance of JavaClassImpl */
    public JavaClassImpl(HeapImpl heap, JPDAClassType classType) {
        if (classType == null) {
            throw new NullPointerException("classType == null");
        }
        this.classType = classType;
        this.heap = heap;
    }
    
    /** For the case where the class type is not loaded yet. */
    public JavaClassImpl(String className) {
        this.className = className;
    }

    public JavaClassImpl(HeapImpl heap, JPDAClassType classType, long instanceCount) {
        this.classType = classType;
        this.instanceCount = instanceCount;
        this.heap = heap;
    }

    @Override
    public long getJavaClassId() {
        // TODO ??
        if (classType != null) {
            return classType.hashCode();
        } else {
            return className.hashCode();
        }
    }

    @Override
    public Instance getClassLoader() {
        if (classType != null) {
            return InstanceImpl.createInstance(heap, classType.getClassLoader());
        } else {
            return null;
        }
    }

    @Override
    public JavaClass getSuperClass() {
        if (classType != null) {
            Super superClass = classType.getSuperClass();
            if (superClass != null) {
                return new JavaClassImpl(heap, superClass.getClassType());
            }
        }
        return null;
    }

    @Override
    public int getInstanceSize() {
        return -1;
    }

    @Override
    public long getAllInstancesSize() {
        return -1;
    }

    @Override
    public List<Field> getFields() {
        return Collections.emptyList();
        // TODO
        /*List<com.sun.jdi.Field> refFields = classType.fields();
        List<Field> fields = new ArrayList<Field>(refFields.size());
        for (com.sun.jdi.Field field : refFields) {
            fields.add(new FieldImpl(field));
        }
        return fields;*/
    }

    @Override
    public List<FieldValue> getStaticFieldValues() {
        if (classType == null) {
            return Collections.emptyList();
        }
        List<org.netbeans.api.debugger.jpda.Field> refFields = classType.staticFields();
        List<FieldValue> fields = new ArrayList<FieldValue>(refFields.size());
        for (org.netbeans.api.debugger.jpda.Field field : refFields) {
            if (field.isStatic()) {
                if (field instanceof ObjectVariable) {
                    Instance instance;
                    if (((ObjectVariable) field).getUniqueID() == 0L) {
                        instance = null;
                    } else {
                        instance = InstanceImpl.createInstance(heap, (ObjectVariable) field);
                    }
                    fields.add(new ObjectFieldValueImpl(heap, null, field, instance));
                } else {
                    fields.add(new FieldValueImpl(heap, null, field));
                }
            }
        }
        return fields;
    }

    @Override
    public Object getValueOfStaticField(String name) {
        Iterator fIt = getStaticFieldValues().iterator();

        while (fIt.hasNext()) {
            FieldValue fieldValue = (FieldValue) fIt.next();

            if (fieldValue.getField().getName().equals(name)) {
                if (fieldValue instanceof ObjectFieldValue) {
                    return ((ObjectFieldValue) fieldValue).getInstance();
                } else {
                    return fieldValue.getValue();
                }
            }
        }

        return null;
    }

    @Override
    public List<Instance> getInstances() {
        if (classType == null) {
            return Collections.emptyList();
        }
        List<ObjectVariable> typeInstances = classType.getInstances(0);
        List<Instance> instances = new ArrayList<Instance>(typeInstances.size());
        for (ObjectVariable inst : typeInstances) {
            Instance instance = InstanceImpl.createInstance(heap, inst);
            instances.add(instance);
        }
        return instances;
    }

    @Override
    public Iterator getInstancesIterator() {
        return getInstances().iterator();
    }

    @Override
    public int getInstancesCount() {
        if (instanceCount != -1L) {
            return (int) instanceCount;
        }
        //return (int) Java6Methods.instanceCounts(refType.virtualMachine(), Collections.singletonList(refType))[0];
        if (classType == null) {
            return 0;
        }
        return (int) classType.getInstanceCount();
    }

    @Override
    public String getName() {
        if (classType != null) {
            return classType.getName();
        } else {
            return className;
        }
    }

    @Override
    public boolean isArray() {
        return classType instanceof JPDAArrayType;
    }
    
    @Override
    public List<JavaClass> getSubClasses() {
        if (classType != null) {
            try {
                java.lang.reflect.Method getSubClassesMethod = classType.getClass().getMethod("getSubClasses", new Class[0]);
                List<JPDAClassType> subclasses = (List<JPDAClassType>) getSubClassesMethod.invoke(classType, new Object[0]);
                if (subclasses.size() > 0) {
                    long[] counts = heap.getDebugger().getInstanceCounts(subclasses);
                    List<JavaClass> subClasses = new ArrayList<JavaClass>(subclasses.size());
                    int i = 0;
                    for (JPDAClassType subclass : subclasses) {
                        subClasses.add(new JavaClassImpl(heap, subclass, counts[i++]));
                    }
                    return Collections.unmodifiableList(subClasses);
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        /*
        if (classType != null) {
            List<JPDAClassType> subclasses = classType.getSubClasses();
            if (subclasses.size() > 0) {
                List<JavaClass> subClasses = new ArrayList<JavaClass>(subclasses.size());
                for (JPDAClassType subclass : subclasses) {
                    subClasses.add(new JavaClassImpl(heap, subclass));
                }
                return Collections.unmodifiableList(subClasses);
            }
        }
         */
        return Collections.emptyList();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JavaClassImpl)) {
            return false;
        }
        JavaClassImpl jc = (JavaClassImpl) obj;
        if (classType != null && classType.equals(jc.classType)) {
            return true;
        }
        if (className != null && className.equals(jc.className)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (classType != null) {
            return classType.hashCode();
        }
        return className.hashCode() + 1024;
    }

    @Override
    public long getRetainedSizeByClass() {
        return -1;
    }

}
