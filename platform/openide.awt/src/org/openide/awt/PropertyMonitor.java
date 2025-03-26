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
package org.openide.awt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.ContextAction.StatefulMonitor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Enabler, which will work against a certain property on a target object. It attaches to the object when it
 * changes, and monitors its property's state using either {@link PropertyChangeListener} or {@link ChangeListener}.
 * If the monitored property's state changes, it fires a change to alert the owner Action.
 * 
 * @param <T> data type
 */
class PropertyMonitor<T> implements ContextAction.StatefulMonitor<T>, PropertyChangeListener, ChangeListener {
    private static final Logger LOG = Logger.getLogger(PropertyMonitor.class.getName());
    
    static final String KEY_CHECKED_VALUE = "Value"; // NOI18N
    static final String KEY_LISTEN_INTERFACE = "ChangeListener"; // NOI18N
    static final String KEY_INTERFACE_METHOD = "Method"; // NOI18N
    static final String KEY_CUSTOM_CHECK = "ActionProperty"; // NOI18N
    static final String KEY_NULL = "Null"; // NOI18N
            
    /**
     * Reflection not initialized
     */
    private static final int UNINITIALIZED = -1;
    /**
     * Listeners not supported
     */
    private static final int NONE = 0;
    /**
     * Property change listener registered against specific property name
     */
    private static final int PROPERTY_NAME = 1;
    /**
     * Listener registered using general add method
     */
    private static final int PROPERTY_ALL = 2;
    /**
     * ChangeListener is used,
     */
    private static final int CHANGE = 3;

    /**
     * Custom listener interace.
     */
    private static final int CUSTOM = 4;
    
    /**
     * Type being monitored, {@link Action} treated specially.
     */
    private final Class<T> type;
    
    /**
     * The property being monitored
     */
    private final String property;
    
    /**
     * The value which makes the action selected.
     */
    private final Object checkedValue;
    
    private Class valType;
    
    /**
     * Reflective access to the property's value
     */
    private Method refGetter;
    
    /**
     * Reflective access to add listener
     */
    private Method refAddListener;
    
    /**
     * Reflective access to remove listener
     */
    private Method refRemoveListener;
    
    /**
     * Detected listener type
     */
    private int listenerType = UNINITIALIZED;
    
    /**
     * The Weak listener attached to the monitored data
     */
    private EventListener weakListener;
    
    /**
     * The last data being monitored
     */
    private Reference<T> attachedTo;

    /**
     * Change Listeners added to this monitor.
     */
    private List<ChangeListener> listeners = null;
    
    /**
     * Listener interface to listen
     */
    private Class listenerInterface;
    
    /**
     * Method name to intercept; null for all methods
     */
    private final String methodName;
    
    private final StatefulMonitor actionMonitor;
    
    private final Function<Object, Object> valueFactory;
    
    public PropertyMonitor(Class<T> type, String property) {
        this(type, property, "", Collections.emptyMap());
    }
    
    public PropertyMonitor(Class<T> type, String property, String keyPrefix, Map data) {
        this.type = type;
        this.property = property;
        
        Object cv = data.get(keyPrefix + KEY_CHECKED_VALUE);
        if (cv == null) {
            Object b= data.get(keyPrefix + KEY_NULL);
            if (b instanceof Boolean) {
                cv = ((Boolean)b).booleanValue() ? ActionState.NULL_VALUE : ActionState.NON_NULL_VALUE;
            }
        }
        checkedValue = cv;

        valueFactory = initValueAccess();

        Object o = data.get(keyPrefix + KEY_LISTEN_INTERFACE);
        String mn = null;
        if (o instanceof String) {
            listenerInterface = GeneralAction.readClass(o);
            o = data.get(keyPrefix + KEY_INTERFACE_METHOD);
            if (o instanceof String) {
                mn = (String)o;
            }
        }
        Object customCheck = data.get(keyPrefix + KEY_CUSTOM_CHECK);
        if (customCheck != null) {
            actionMonitor = new PropertyMonitor(Action.class, customCheck.toString());
        } else {
            if (property == null) {
                throw new IllegalArgumentException("Delegate or guard property must be specified");
            }
            actionMonitor = null;
        }
        methodName = mn;
    }
    
    public Class<T> getType() {
        return type;
    }

    private T data() {
        synchronized (this) {
            return attachedTo != null ? attachedTo.get() : null;
        }
    }

    public void clear() {
        Object o = data();
        if (o != null) {
            clearListeners(o);
        }
        if (actionMonitor != null) {
            actionMonitor.clear();
        }
        synchronized (this) {
            attachedTo = null;
        }
    }

    public void addChangeListener(ChangeListener l) {
        boolean start = false;
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList<>();
                start = true;
            }
            listeners.add(l);
        }
        if (start) {
            T d = data();
            LOG.log(Level.FINER, "{0}: attaching listener to {1}", new Object[] { this, d });
            if (d != null) {
                addListeners(d);
            }
        }
    }

    public void removeChangeListener(ChangeListener l) {
        boolean stop = false;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            listeners.remove(l);
            stop = listeners.isEmpty();
            if (stop) {
                listeners = null;
            }
        }
        if (stop) {
            T d = data();
            if (d != null) {
                clearListeners(d);
            }
        }
    }
    
    private void clearListeners(Object data) {
        if (weakListener == null || refRemoveListener == null) {
            return;
        }
        LOG.log(Level.FINER, "{0}: adding listener to {1}", new Object[] { this, data });
        try {
            switch (listenerType) {
                case PROPERTY_NAME:
                    refRemoveListener.invoke(data, property, weakListener);
                    break;
                case CUSTOM:
                    ((ProxyListener)Proxy.getInvocationHandler(weakListener)).unregister(data);
                    break;
                case PROPERTY_ALL:
                case CHANGE:
                    refRemoveListener.invoke(data, weakListener);
                    break;
                case NONE:
                    break;
                default:
                    throw new IllegalStateException();
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        weakListener = null;
        if (actionMonitor != null) {
            actionMonitor.removeChangeListener(this);
        }
    }

    /**
     * Initializes listener reflective access.
     * @param data
     */
    private void initListenerReflection() {
        if (listenerType != UNINITIALIZED) {
            return;
        }
        Method add = null;
        try {
            if (listenerInterface != null) {
                add = type.getMethod("add" + listenerInterface.getSimpleName(), listenerInterface);
                listenerType = CUSTOM;
            } else {
                try {
                    if (property != null) {
                        add = type.getMethod("addPropertyChangeListener", String.class, PropertyChangeListener.class);
                        listenerType = PROPERTY_NAME;
                    }
                } catch (NoSuchMethodException ex) {
                    // expected, ignore
                }
                if (add == null) {
                    try {
                        add = type.getMethod("addPropertyChangeListener", PropertyChangeListener.class);
                        listenerType = PROPERTY_ALL;
                    } catch (NoSuchMethodException ex2) {
                        add = type.getMethod("addChangeListener", ChangeListener.class);
                        listenerType = CHANGE;
                    }
                }
            }
        } catch (NoSuchMethodException | SecurityException ex3) {
            listenerType = NONE;
            return;
        }
        Method remove = null;
        try {
            switch (listenerType) {
                case PROPERTY_NAME:
                    remove = type.getMethod("removePropertyChangeListener", String.class, PropertyChangeListener.class);
                    break;
                case PROPERTY_ALL:
                    remove = type.getMethod("removePropertyChangeListener", PropertyChangeListener.class);
                    break;
                case CHANGE:
                    remove = type.getMethod("removeChangeListener", ChangeListener.class);
                    break;
                case CUSTOM:
                    remove = type.getMethod("remove" + listenerInterface.getSimpleName(), listenerInterface);
                    break;
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            listenerType = -1;
            return;
        }
        refAddListener = add;
        refRemoveListener = remove;
    }

    // method accessed by reflection
    public boolean falseGetter(Object data) {
        return false;
    }

    // method accessed by reflection
    public boolean trueGetter(Object data) {
        return true;
    }
    
    private void addListeners(Object data) {
        if (weakListener != null || listenerType == NONE) {
            return;
        }
        initListenerReflection();
        synchronized (this) {
            if (listeners == null) {
                return;
            }
        }
        PropertyChangeListener pcl;
        ChangeListener chl;
        LOG.log(Level.FINER, "{0}: adding listener to {1}", new Object[] { this, data });
        try {
            switch (listenerType) {
                case PROPERTY_NAME:
                    weakListener = pcl = WeakListeners.propertyChange(this, property, data);
                    refAddListener.invoke(data, property, pcl);
                    break;
                case PROPERTY_ALL:
                    weakListener = pcl = WeakListeners.propertyChange(this, data);
                    refAddListener.invoke(data, pcl);
                    break;
                case CHANGE:
                    weakListener = chl = WeakListeners.change(this, data);
                    refAddListener.invoke(data, chl);
                    break;
                case NONE:
                    return;
                case CUSTOM: {
                    ProxyListener pl = new ProxyListener(data, methodName, refRemoveListener, this);
                    Object o = Proxy.newProxyInstance(listenerInterface.getClassLoader(), new Class[] { listenerInterface, EventListener.class }, pl);
                    pl.proxy = weakListener = (EventListener)o;
                    refAddListener.invoke(data, weakListener);
                    break;
                }
                    
                default:
                    throw new IllegalStateException();
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ex) {
            listenerType = NONE;
        }

        if (actionMonitor != null) {
            actionMonitor.addChangeListener(this);
        }
    }

    private Function<Object, Object> initValueAccess() {
        Method getter = null;
        if (property != null) {
            String capitalizedName = Character.toUpperCase(property.charAt(0)) + property.substring(1);
            String isGetter = "is" + capitalizedName; // NOI18N
            String getGetter = "get" + capitalizedName; // NOI18N
            try {
                try {
                    getter = type.getMethod(isGetter);
                } catch (NoSuchMethodException ex) {
                    getter = type.getMethod(getGetter);
                }
                Class c = getter.getReturnType();
                if (!(c != Boolean.TYPE || c != Boolean.class || c != String.class || !c.isEnum()) && 
                    !(checkedValue == ActionState.NULL_VALUE || checkedValue == ActionState.NON_NULL_VALUE)) {
                    getter = null;
                }
                valType = c;
                this.refGetter = getter;
                return (o) -> reflectiveGet(o);
            } catch (SecurityException | NoSuchMethodException ex) {
            }
        }
        if (type == Action.class) {
            return (o) -> inspectAction((Action)o);
        } else {
            return (o) -> property == null;
        }
    }

    private void update() {
        ChangeListener[] ll;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            ll = listeners.toArray(new ChangeListener[0]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ll) {
            l.stateChanged(ev);
        }
    }

    private void refreshListeners(T data) {
        Object prevData = data();
        if (prevData == data) {
            return;
        }
        if (actionMonitor != null) {
            actionMonitor.clear();
        }
        if (prevData != null) {
            clearListeners(prevData);
        }
        if (data != null) {
            addListeners(data);
        }
        attachedTo = new WeakReference<>(data);
    }
    
    private Object reflectiveGet(Object instance) {
        try {
            return refGetter.invoke(instance);
        } catch (ReflectiveOperationException | IllegalArgumentException ex) {
            return false;
        }
    }
    
    public boolean enabled(List<? extends T> data, Supplier<Action> aFactory) {
        T first = data.isEmpty() ? null : data.get(0);
        if (data.isEmpty()) {
            return false;
        }
        refreshListeners(first);
        if (first == null) {
            return false;
        }
        if (type == Action.class) {
            return inspectAction((Action)first);
        }
        
        Object o = valueFactory.apply(first);
        if (!interpretAsBoolean(o)) {
            return false;
        }
        if (aFactory != null && actionMonitor != null) {
            return actionMonitor.enabled(Collections.singletonList(aFactory.get()), null);
        } else {
            return true;
        }
    }
    
    public boolean inspectAction(Action a) {
        if (a == null) {
            return false;
        }
        if ("enabled".equals(property)) { // NOI18N
            return a.isEnabled();
        }
        return a.getValue(property) == Boolean.TRUE;
    }
    
    private boolean interpretAsBoolean(Object v) {
        if (v == null) {
            if (checkedValue == ActionState.NULL_VALUE) {
                return true;
            }
            return false;
        } 
        if (valType == null || valType == Boolean.TYPE || valType == Boolean.class) {
            if (checkedValue == null) {
                return Boolean.TRUE.equals(v);
            } else {
                return checkedValue.equals(v.toString());
            }
        }
        if (checkedValue == null) {
            // v is not null;
            if (v instanceof Collection) {
                return !((Collection) v).isEmpty();
            } else if (v instanceof Map) {
                return !((Map) v).isEmpty();
            } else if (Number.class.isInstance(v)) {
                return ((Number)v).intValue() > 0;
            }
            return false;
        }
        if (checkedValue == ActionState.NON_NULL_VALUE) {
            return true;
        }
        if (!(checkedValue instanceof String)) {
            return checkedValue.equals(v);
        }
        return checkedValue.equals(v.toString());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() != null && property != null && !property.equals(evt.getPropertyName())) {
            return;
        }
        update();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        update();
    }

    public PropertyMonitor(PropertyMonitor other) {
        this.type = other.type;
        this.property = other.property;
        this.checkedValue = other.checkedValue;
        this.listenerType = other.listenerType;
        this.refGetter = other.refGetter;
        this.valueFactory = other.valueFactory;
        this.valType = other.valType;
        this.refAddListener = other.refAddListener;
        this.refRemoveListener = other.refRemoveListener;
        this.listenerInterface = other.listenerInterface;
        this.methodName = other.methodName;
        if (other.actionMonitor == null) {
            this.actionMonitor = null;
        } else {
            this.actionMonitor = other.actionMonitor.createContextMonitor(Lookup.EMPTY);
        }
    }

    @Override
    public ContextAction.StatefulMonitor<T> createContextMonitor(Lookup context) {
        return new PropertyMonitor<>(this);
    }
    
    private static final Method OBJECT_EQUALS = getObjectMethod("equals", Object.class); // NOI18N
    private static final Method OBJECT_HASHCODE = getObjectMethod("hashCode"); // NOI18N
    
    private static Method getObjectMethod(String name, Class... types) {
        try {
            return Object.class.getMethod(name, types);
        } catch (ReflectiveOperationException | SecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static class ProxyListener extends WeakReference<ChangeListener> implements EventListener, InvocationHandler, Runnable {
        private final Reference   theData;
        private final String  methodName;
        private final Method  removeMethod;
        volatile EventListener proxy;
        
        public ProxyListener(Object theData, String methodName, Method removeMethod, ChangeListener referent) {
            super(referent, Utilities.activeReferenceQueue());
            this.theData = new WeakReference<>(theData);
            this.methodName = methodName;
            this.removeMethod = removeMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                // a method from object => call it on your self
                if (method == OBJECT_EQUALS) {
                    return equals(args[0]);
                }  else if (method == OBJECT_HASHCODE) {
                    return proxy.hashCode();
                }
                return method.invoke(this, args);
            }
            ChangeListener target = get();
            Object data = theData.get();
            if (data == null) {
                return null;
            }
            if (target == null) {
                return null;
            }
            if (methodName == null || method.getName().equals(methodName)) {
                ChangeEvent ev = new ChangeEvent(data);
                target.stateChanged(ev);
            }
            return null;
        }
        
        private void unregister(Object data) {
            if (data == null) {
                return;
            }
            if (removeMethod != null) {
                try {
                    removeMethod.invoke(data, proxy);
                } catch (ReflectiveOperationException | SecurityException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            theData.clear();
        }

        @Override
        public void run() {
            unregister(theData.get());
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PropertyMonitor@").append(System.identityHashCode(this)).append("{")
                .append("class = ").append(type.getName())
                .append(", property = ").append(property)
                .append(", valtype = ").append(valType == null ? "null" : valType.getName())
                .append(", checkval = ").append(checkedValue)
                .append("}");
        return sb.toString();
    }
} 
