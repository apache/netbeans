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
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class JavaComponentInfo implements ComponentInfo {

    private static final JavaComponentInfo[] NO_SUBCOMPONENTS = new JavaComponentInfo[]{};
    private static final int MAX_TEXT_LENGTH = 80;
    private static RequestProcessor RP = new RequestProcessor(JavaComponentInfo.class);
    
    //private AWTComponentInfo parent;
    private Rectangle bounds;
    private Rectangle windowBounds;
    private String name;
    private String type;
    private JavaComponentInfo[] subComponents;
    private List<PropertySet> propertySets = new ArrayList<PropertySet>();
    private PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    private JPDAThreadImpl thread;
    private ObjectReference component;
    private FieldInfo fieldInfo;
    private String componentText;
    private RemoteServices.ServiceType sType;
    long uid;

    public JavaComponentInfo(JPDAThreadImpl t, ObjectReference component, RemoteServices.ServiceType sType) throws RetrievalException {
        this.thread = t;
        this.component = component;
        try {
            this.type = ReferenceTypeWrapper.name(ObjectReferenceWrapper.referenceType(component));
        } catch (InternalExceptionWrapper ex) {
            throw new RetrievalException(ex.getLocalizedMessage(), ex);
        } catch (VMDisconnectedExceptionWrapper ex) {
            throw RetrievalException.disconnected();
        } catch (ObjectCollectedExceptionWrapper ex) {
            throw new RetrievalException(ex.getLocalizedMessage(), ex);
        }
        this.sType = sType;
        this.uid = component.uniqueID();
    }
    
    final protected void init() throws RetrievalException {
        retrieve();
        addProperties();
        if (!RemoteAWTScreenshot.FAST_FIELDS_SEARCH) {
            findComponentFields();
        }
    }
    
    abstract protected void retrieve() throws RetrievalException;
    
    final public JPDAThreadImpl getThread() {
        return thread;
    }

    final public ObjectReference getComponent() {
        return component;
    }
    
    /** Provide the stack information about where this component was added into the hierarchy */
    public Stack getAddCallStack() {
        return VisualDebuggerListener.getStackOf(thread.getDebugger(), component);
    }
    
    final public String getName() {
        return name;
    }

    final public String getTypeName() {
        int d = type.lastIndexOf('.');
        String typeName;
        if (d > 0) {
            typeName = type.substring(d + 1);
        } else {
            typeName = type;
        }
        return typeName;
    }

    final public void setComponentText(String componentText) {
        if (componentText.length() > MAX_TEXT_LENGTH) {
            this.componentText = componentText.substring(0, MAX_TEXT_LENGTH) + "...";
        } else {
            this.componentText = componentText;
        }
    }

    @Override
    public String getDisplayName() {
        String typeName = getTypeName();
        String text = (componentText != null) ? " \"" + componentText + "\"" : "";
        return getFieldName() + "[" + typeName + "]" + text;
    }

    @Override
    public String getHtmlDisplayName() {
        if (isCustomType() || componentText != null) {
            String typeName = getTypeName();
            if (isCustomType()) {
                typeName = "<b>" + typeName + "</b>";
            }

            String text;
            if (componentText != null) {
                text = escapeHTML(componentText);
                text = " <font color=\"#A0A0A0\">\"" + text + "\"</font>";
            } else {
                text = "";
            }
            return getFieldName() + "[" + typeName + "]" + text;
        } else {
            return null;
        }
    }
    
    protected String getFieldName() {
        return (fieldInfo != null) ? fieldInfo.getName() + " " : "";
    }

    final public String getType() {
        return type;
    }

    final public FieldInfo getField() {
        return fieldInfo;
    }

    final public boolean isCustomType() {
        return isCustomType(type);
    }
    
    public static boolean isCustomType(String type) {
        return !(type.startsWith("java.awt.") || 
                 type.startsWith("javax.swing.") || 
                 type.startsWith("javafx.") ||
                 type.startsWith("com.sun."));  // NOI18N
    }

    @Override
    final public Rectangle getBounds() {
        return bounds;
    }

    @Override
    final public Rectangle getWindowBounds() {
        if (windowBounds == null) {
            return bounds;
        } else {
            return windowBounds;
        }
    }

    final public void addPropertySet(PropertySet ps) {
        propertySets.add(ps);
    }

    @Override
    final public PropertySet[] getPropertySets() {
        return propertySets.toArray(new PropertySet[]{});
    }

    final protected void setSubComponents(JavaComponentInfo[] subComponents) {
        this.subComponents = subComponents;
    }

    @Override
    final public JavaComponentInfo[] getSubComponents() {
        if (subComponents == null) {
            return NO_SUBCOMPONENTS;
        } else {
            return subComponents;
        }
    }

    @Override
    final public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pchs.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    final public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pchs.removePropertyChangeListener(propertyChangeListener);
    }

    final protected void firePropertyChange(String name, Object o, Object n) {
        pchs.firePropertyChange(name, o, n);
    }

    final public void setFieldInfo(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }
    
    final public void setBounds(Rectangle r) {
        this.bounds = r;
    }
    
    
    final public void setWindowBounds(Rectangle rectangle) {
        this.windowBounds = rectangle;
    }
    
    
    final public void setName(String value) {
        this.name = value;
    }
    
    
    final public void setComponent(ObjectReference component) {
        this.component = component;
    }
    
    
    final public void setType(String name) {
        this.type = name;
    }
    
    private void addProperties() {
        addPropertySet(new PropertySet("main", NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropMain"),
                                               NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropMainDescr")) {
            @Override
            public Property<?>[] getProperties() {
                return new Property[] {
                    new ReadOnly("name", String.class,
                                 NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropName"),
                                 NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropNameDescr")) {
                        @Override
                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
                            return JavaComponentInfo.this.getName();
                        }
                    },
                    new ReadOnly("type", String.class,
                                 NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropType"),
                                 NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropTypeDescr")) {
                        @Override
                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
                            return JavaComponentInfo.this.getType();
                        }
                    },
                    new ReadOnly("bounds", String.class,
                                 NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropBounds"),
                                 NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropBoundsDescr")) {
                        @Override
                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
                            Rectangle r = JavaComponentInfo.this.getWindowBounds();
                            return "[x=" + r.x + ",y=" + r.y + ",width=" + r.width + ",height=" + r.height + "]";
                        }
                    },
                };
            }
        });
        final LazyProperties lazyProperties = new LazyProperties();
        addPropertySet(
            new PropertySet("Properties",
                            NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentProps"),
                            NbBundle.getMessage(JavaComponentInfo.class, "MSG_ComponentPropsDescr")) {

                @Override
                public Property<?>[] getProperties() {
                    //System.err.println("JavaComponentInfo.Properties PropertySet.getProperties()");
                    // To fix https://netbeans.org/bugzilla/show_bug.cgi?id=243065
                    //        https://netbeans.org/bugzilla/show_bug.cgi?id=241154
                    // Do not compute properties above, compute them on demand in a separate thread now.
                    // When done, fire Node.PROP_PROPERTY_SETS, null, null.
                    Property<?>[] props = lazyProperties.getProperties();
                    //System.err.println("\nprops = "+props.length+"\n");
                    return props;
                }
            });
        try {
            Method getTextMethod = ClassTypeWrapper.concreteMethodByName(
                    (ClassType) ObjectReferenceWrapper.referenceType(component), "getText", "()Ljava/lang/String;");
            //Method getTextMethod = methodsByName.get("getText");    // NOI18N
            if (getTextMethod != null) {
                try {
                    Value theText = ObjectReferenceWrapper.invokeMethod(component, getThread().getThreadReference(), getTextMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                    if (theText instanceof StringReference) {
                        setComponentText(StringReferenceWrapper.value((StringReference) theText));
                    }
                } catch (VMDisconnectedExceptionWrapper vmdex) {
                    return;
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper cnpe) {
        }
    }
    
    private class LazyProperties implements Runnable {
        
        private volatile Reference<Property<?>[]> propertiesRef;
        
        Property<?>[] getProperties() {
            Property<?>[] properties;
            if (propertiesRef == null) {
                propertiesRef = new SoftReference<>(null);
                properties = null;
            } else {
                properties = propertiesRef.get();
            }
            if (properties == null) {
                properties = new Property[] {};
                propertiesRef = new SoftReference<>(properties);
                RP.post(this);
            }
            return properties;
        }

        @Override
        public void run() {
            // TODO: Try to find out the BeanInfo of the class
            //System.err.println("Computing the component properties...");
            List<Method> allMethods;
            Map<String, Method> methodsByName;
            try {
                allMethods = ReferenceTypeWrapper.allMethods(ObjectReferenceWrapper.referenceType(component));
                //System.err.println("Have "+allMethods.size()+" methods.");
                methodsByName = new HashMap<String, Method>(allMethods.size());
                for (Method m : allMethods) {
                    String mName = TypeComponentWrapper.name(m);
                    if ((mName.startsWith("get") || mName.startsWith("set")) && mName.length() > 3 ||
                         mName.startsWith("is") && mName.length() > 2) {
                        if ((mName.startsWith("get") || mName.startsWith("is")) && m.argumentTypeNames().size() == 0 ||
                            mName.startsWith("set") && MethodWrapper.argumentTypeNames(m).size() == 1 && "void".equals(MethodWrapper.returnTypeName(m))) {

                            methodsByName.put(mName, m);
                        }
                    }
                }
            } catch (ClassNotPreparedExceptionWrapper cnpex) {
                // no class - no properties
                return ;
            } catch (InternalExceptionWrapper iex) {
                // no go
                return ;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                // gone
                return ;
            } catch (VMDisconnectedExceptionWrapper vmdex) {
                // gone
                return ;
            }
            Map<String, Property> sortedProperties = new TreeMap<String, Property>();
            //final List<Property> properties = new ArrayList<Property>();
            for (String mName : methodsByName.keySet()) {
                //System.err.println("  Have method '"+name+"'...");
                if (mName.startsWith("set")) {
                    continue;
                }
                String property;
                String setName;
                if (mName.startsWith("is")) {
                    property = Character.toLowerCase(mName.charAt(2)) + mName.substring(3);
                    setName = "set" + mName.substring(2);
                } else { // startsWith("get"):
                    property = Character.toLowerCase(mName.charAt(3)) + mName.substring(4);
                    setName = "set" + mName.substring(3);
                }
                Property p = new ComponentProperty(property, methodsByName.get(mName), methodsByName.get(setName),
                                                   JavaComponentInfo.this, component, getThread(), getThread().getDebugger(), sType);
                sortedProperties.put(property, p);
                //System.err.println("    => property '"+property+"', p = "+p);
            }
            Property<?>[] properties = sortedProperties.values().toArray(new Property[] {});
            propertiesRef = new SoftReference<>(properties);
            //System.err.println("Properties Computed: "+properties.length);
            firePropertyChange(Node.PROP_PROPERTY_SETS, null, null);
        }
    }
    
    protected static boolean isInstanceOfClass(ClassType c1, ClassType c2) {
        if (c1.equals(c2)) {
            return true;
        }
        c1 = c1.superclass();
        if (c1 == null) {
            return false;
        }
        return isInstanceOfClass(c1, c2);
    }
    
    private void findComponentFields() {
        List<JavaComponentInfo> customParents = new ArrayList<JavaComponentInfo>();
        fillCustomParents(customParents, this);
        findFieldsInParents(customParents, this);
    }

    private static void fillCustomParents(List<JavaComponentInfo> customParents, JavaComponentInfo ci) {
        ComponentInfo[] subs = ci.getSubComponents();
        if (subs.length > 0 && ci.isCustomType()) {
            customParents.add(ci);
        }
        for (ComponentInfo sci : subs) {
            fillCustomParents(customParents, (JavaComponentInfo)sci);
        }
    }

    private static void findFieldsInParents(List<JavaComponentInfo> customParents, JavaComponentInfo ci) {
        ComponentInfo[] subComponents = ci.getSubComponents();
        ObjectReference component = ci.getComponent();
        for (JavaComponentInfo cp : customParents) {
            try {
                ObjectReference c = cp.getComponent();
                Map<Field, Value> fieldValues = ObjectReferenceWrapper.getValues(c, ReferenceTypeWrapper.fields(ObjectReferenceWrapper.referenceType(c)));
                for (Map.Entry<Field, Value> e : fieldValues.entrySet()) {
                    if (component.equals(e.getValue())) {
                        ci.setFieldInfo(new JavaComponentInfo.FieldInfo(e.getKey(), cp));
                    }
                }
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
                return ;
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (ClassNotPreparedExceptionWrapper ex) {
            }
        }
        for (ComponentInfo sci : subComponents) {
            findFieldsInParents(customParents, (JavaComponentInfo)sci);
        }
    }
    
    private static class ComponentProperty extends Node.Property {
        
        private String propertyName;
        private Method getter;
        private Method setter;
        private JavaComponentInfo ci;
        private ObjectReference component;
        private JPDAThreadImpl t;
        private ThreadReference tawt;
        private JPDADebuggerImpl debugger;
        private String value;
        private final Object valueLock = new Object();
        private final String valueCalculating = "calculating";
        private final RemoteServices.ServiceType sType;
        private boolean valueIsEditable;
        private Type valueType;
        
        ComponentProperty(String propertyName, Method getter, Method setter,
                          JavaComponentInfo ci, ObjectReference component,
                          JPDAThreadImpl t, JPDADebuggerImpl debugger, RemoteServices.ServiceType sType) {
            super(String.class);
            this.propertyName = propertyName;
            this.getter = getter;
            this.setter = setter;
            this.ci = ci;
            this.component = component;
            this.t = t;
            this.tawt = t.getThreadReference();
            this.debugger = debugger;
            this.sType = sType;
        }

        @Override
        public String getName() {
            return propertyName;
        }
        
        @Override
        public String getDisplayName() {
            return propertyName;
        }
        
        @Override
        public boolean canRead() {
            return getter != null;
        }
        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            synchronized (valueLock) {
                if (value == null) {
                    value = valueCalculating;
                    debugger.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RemoteServices.runOnStoppedThread(t, new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean[] isEditablePtr = new boolean[] { false };
                                        Type[] typePtr = new Type[] { null };
                                        String v = getValueLazy(isEditablePtr, typePtr);
                                        synchronized (valueLock) {
                                            value = v;
                                            valueIsEditable = isEditablePtr[0];
                                            valueType = typePtr[0];
                                        }
                                        ci.firePropertyChange(propertyName, null, v);
                                    }
                                }, sType);
                            } catch (PropertyVetoException ex) {
                                value = ex.getLocalizedMessage();
                            }
                        }
                    });
                }
                return value;
            }
        }
        
        private String getValueLazy(boolean[] isEditablePtr, Type[] typePtr) {
            Lock l = t.accessLock.writeLock();
            l.lock();
            try {
                Value v = ObjectReferenceWrapper.invokeMethod(component, tawt, getter, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                if (v != null) {
                    typePtr[0] = ValueWrapper.type(v);
                }
                if (v instanceof StringReference) {
                    isEditablePtr[0] = true;
                    return StringReferenceWrapper.value((StringReference) v);
                }
                if (v instanceof ObjectReference) {
                    isEditablePtr[0] = false;
                    Type t = ValueWrapper.type(v);
                    if (t instanceof ClassType) {
                        Method toStringMethod = ClassTypeWrapper.concreteMethodByName((ClassType) t, "toString", "()Ljava/lang/String;");
                        v = ObjectReferenceWrapper.invokeMethod((ObjectReference) v, tawt, toStringMethod, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
                        if (v instanceof StringReference) {
                            return StringReferenceWrapper.value((StringReference) v);
                        }
                    }
                } else if (v instanceof PrimitiveValue) {
                    isEditablePtr[0] = true;
                }
                return (v == null) ? "null" : MirrorWrapper.toString(v);
            } catch (InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
                return ex.getMessage();
            } catch (ClassNotPreparedExceptionWrapper ex) {
                Exceptions.printStackTrace(ex);
                return ex.getMessage();
            } catch (ClassNotLoadedException ex) {
                return ex.getMessage();
            } catch (IncompatibleThreadStateException ex) {
                Exceptions.printStackTrace(ex);
                return ex.getMessage();
            } catch (InternalExceptionWrapper ex) {
                return ex.getMessage();
            } catch (ObjectCollectedExceptionWrapper ocex) {
                return ocex.getLocalizedMessage();
            } catch (VMDisconnectedExceptionWrapper vmdex) {
                return vmdex.getLocalizedMessage();
            } catch (final InvocationException ex) {
                final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, debugger);
                iextr.setPreferredThread(t);
                /*
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        iextr.getMessage();
                        iextr.getLocalizedMessage();
                        iextr.getCause();
                        iextr.getStackTrace();
                        Exceptions.printStackTrace(iextr);
                        Exceptions.printStackTrace(ex);
                    }
                }, 100);
                 */
                //Exceptions.printStackTrace(iextr);
                //Exceptions.printStackTrace(ex);
                return iextr.getMessage();
            } finally {
                l.unlock();
            }
        }
        
       private String setValueLazy(String val, String oldValue, Type type) {
            Value v;
            VirtualMachine vm = type.virtualMachine();
            try {
                if (type instanceof PrimitiveType) {
                    String ts = TypeWrapper.name(type);
                    try {
                        if (Boolean.TYPE.getName().equals(ts)) {
                            v = VirtualMachineWrapper.mirrorOf(vm, Boolean.parseBoolean(val));
                        } else if (Byte.TYPE.getName().equals(ts)) {
                            v = VirtualMachineWrapper.mirrorOf(vm, Byte.parseByte(val));
                        } else if (Character.TYPE.getName().equals(ts)) {
                            if (val.length() == 0) {
                                throw new NumberFormatException("Zero length input.");
                            }
                            v = VirtualMachineWrapper.mirrorOf(vm, val.charAt(0));
                        } else if (Short.TYPE.getName().equals(ts)) {
                            v = VirtualMachineWrapper.mirrorOf(vm, Short.parseShort(val));
                        } else if (Integer.TYPE.getName().equals(ts)) {
                            v = VirtualMachineWrapper.mirrorOf(vm, Integer.parseInt(val));
                        } else if (Long.TYPE.getName().equals(ts)) {
                            v = VirtualMachineWrapper.mirrorOf(vm, Long.parseLong(val));
                        } else if (Float.TYPE.getName().equals(ts)) {
                            v = VirtualMachineWrapper.mirrorOf(vm, Float.parseFloat(val));
                        } else if (Double.TYPE.getName().equals(ts)) {
                            v = VirtualMachineWrapper.mirrorOf(vm, Double.parseDouble(val));
                        } else {
                            throw new IllegalArgumentException("Unknown type '"+ts+"'");
                        }
                        val = MirrorWrapper.toString(v);
                    } catch (NumberFormatException nfex) {
                        NotifyDescriptor msg = new NotifyDescriptor.Message(nfex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                        return oldValue;
                    }
                } else {
                    if ("java.lang.String".equals(TypeWrapper.name(type))) {
                        v = VirtualMachineWrapper.mirrorOf(vm, val);
                    } else {
                        throw new IllegalArgumentException("Unknown type '"+type.name()+"'");
                    }
                }
            } catch (InternalExceptionWrapper iex) {
                return oldValue;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                NotifyDescriptor msg = new NotifyDescriptor.Message(ocex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return oldValue;
            } catch (UnsupportedOperationExceptionWrapper uex) {
                NotifyDescriptor msg = new NotifyDescriptor.Message(uex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return oldValue;
            } catch (VMDisconnectedExceptionWrapper vmd) {
                return oldValue;
            }
            Lock l = t.accessLock.writeLock();
            l.lock();
            try {
                ObjectReferenceWrapper.invokeMethod(component, tawt, setter, Collections.singletonList(v), ObjectReference.INVOKE_SINGLE_THREADED);
                return val;
            } catch (InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
                return oldValue;
            } catch (ClassNotLoadedException ex) {
                Exceptions.printStackTrace(ex);
                return oldValue;
            } catch (IncompatibleThreadStateException ex) {
                Exceptions.printStackTrace(ex);
                return oldValue;
            } catch (InternalExceptionWrapper iex) {
                return oldValue;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                NotifyDescriptor msg = new NotifyDescriptor.Message(ocex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return oldValue;
            } catch (VMDisconnectedExceptionWrapper vmd) {
                return oldValue;
            } catch (final InvocationException ex) {
                final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, debugger);
                iextr.setPreferredThread(t);
                
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        iextr.getMessage();
                        iextr.getLocalizedMessage();
                        iextr.getCause();
                        iextr.getStackTrace();
                        Exceptions.printStackTrace(iextr);
                        //Exceptions.printStackTrace(ex);
                    }
                }, 100);
                
                //Exceptions.printStackTrace(iextr);
                //Exceptions.printStackTrace(ex);
                return oldValue;
            } finally {
                l.unlock();
            }
        }

        @Override
        public boolean canWrite() {
            synchronized (valueLock) {
                return setter != null && valueIsEditable;
            }
        }

        @Override
        public void setValue(final Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (!(val instanceof String)) {
                throw new IllegalArgumentException("val = "+val);
            }
            final String oldValue;
            final Type type;
            synchronized (valueLock) {
                oldValue = value;
                type = valueType;
                value = valueCalculating;
            }
            debugger.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        RemoteServices.runOnStoppedThread(t, new Runnable() {
                            @Override
                            public void run() {
                                String v;
                                Throwable t = null;
                                try {
                                    v = setValueLazy((String) val, oldValue, type);
                                } catch (Throwable th) {
                                    if (th instanceof ThreadDeath) {
                                        throw (ThreadDeath) th;
                                    }
                                    t = th;
                                    v = oldValue;
                                    
                                }
                                synchronized (valueLock) {
                                    value = v;
                                }
                                ci.firePropertyChange(propertyName, null, v);
                                if (t != null) {
                                    Exceptions.printStackTrace(t);
                                }
                            }
                        }, sType);
                    } catch (PropertyVetoException ex) {
                        NotifyDescriptor msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                }
            });
        }
    }
    
    public static class FieldInfo {

        private String name;
        private Field f;
        private JavaComponentInfo parent;

        FieldInfo(Field f, JavaComponentInfo parent) {
            this.name = f.name();
            this.f = f;
            this.parent = parent;
        }

        public String getName() {
            return name;
        }

        public Field getField() {
            return f;
        }

        public JavaComponentInfo getParent() {
            return parent;
        }
    }
    
    public static class Stack {
        
        private Frame[] frames;
        
        public Stack(Frame[] frames) {
            this.frames = frames;
        }
        
        public Stack(CallStackFrame[] stackFrames) {
            int n = stackFrames.length;
            frames = new Frame[n];
            for (int i = 0; i < n; i++) {
                CallStackFrame sf = stackFrames[i];
                try {
                    frames[i] = new Frame(sf.getClassName(), sf.getMethodName(), sf.getSourceName(null), sf.getLineNumber(null));
                } catch (AbsentInformationException ex) {
                    frames[i] = new Frame(sf.getClassName(), sf.getMethodName(), null, sf.getLineNumber(null));
                }
            }
        }
        
        public Frame[] getFrames() {
            return frames;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Stack with "+frames.length+" elements.");
            for (Frame f : frames) {
                sb.append("\n    ");
                sb.append(f.toString());
            }
            return sb.toString();
        }
        
        public static class Frame {
            
            private String className;
            private String methodName;
            private String fileName;
            private int lineNumber;
            
            public Frame(String className, String methodName, String fileName, int lineNumber) {
                this.className = className;
                this.methodName = methodName;
                this.fileName = fileName;
                this.lineNumber = lineNumber;
            }

            /** Parse a stack trace frame line */
            static Frame parseLine(String line) {
                if (line.startsWith("at ")) {
                    line = line.substring(3);
                }
                int p = line.indexOf('(');
                if (p < 0) {
                    return new Frame(line, "", "", 1);
                }
                int d = line.lastIndexOf('.', p);
                int start = line.lastIndexOf(' ', p);
                if (start < 0) start = 0;
                String cn;
                String mn;
                if (d >= 0) {
                    cn = line.substring(start, d);
                    mn = line.substring(d + 1, p);
                } else {
                    cn = line.substring(start, p);
                    mn = "";
                }
                p++;
                int col = line.indexOf(':', p);
                String fn;
                int ln;
                if (col > 0) {
                    fn = line.substring(p, col);
                    String lns = line.substring(col + 1);
                    if (lns.endsWith(")")) lns = lns.substring(0, lns.length() - 1);
                    try {
                        ln = Integer.parseInt(lns);
                    } catch (NumberFormatException nfex) {
                        ln = 1;
                    }
                } else {
                    fn = line.substring(p);
                    if (fn.endsWith(")")) fn = fn.substring(0, fn.length() - 1);
                    ln = 1;
                }
                return new Frame(cn, mn, fn, ln);
            }
            
            public String getClassName() {
                return className;
            }

            public String getMethodName() {
                return methodName;
            }

            public String getFileName() {
                return fileName;
            }

            public int getLineNumber() {
                return lineNumber;
            }

            @Override
            public String toString() {
                return "Frame "+className+"."+methodName+"("+fileName+":"+lineNumber+")";
            }
        }
    }
    
    private static String escapeHTML(String message) {
        if (message == null) {
            return null;
        }
        int len = message.length();
        StringBuilder result = new StringBuilder(len + 20);
        char aChar;

        for (int i = 0; i < len; i++) {
            aChar = message.charAt(i);
            switch (aChar) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(aChar);
            }
        }
        return result.toString();
    }
}
