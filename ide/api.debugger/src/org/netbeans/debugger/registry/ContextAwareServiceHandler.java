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

package org.netbeans.debugger.registry;

import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextAwareService;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Lookup;

/**
 * Handler of context aware services that implement one or more interfaces.
 * The services are registered through {@link DebuggerServiceRegistration} annotation.
 *
 * This handler guarantees that it creates only one service for given context and given set of interfaces.
 * Method {@link ContextAwareService#forContext(org.netbeans.spi.debugger.ContextProvider)}
 * returns the instance of the actual registered class. However, if necessary,
 * this can be changed to return another proxy implementation, that will delegate
 * to an instance of the actual registered class. This will allow to mask some
 * methods with attributes and also can prevent from too early class loading.
 *
 * @author Martin Entlicher
 */
public class ContextAwareServiceHandler implements InvocationHandler {

    public static final String SERVICE_NAME = "serviceName"; // NOI18N
    public static final String SERVICE_CLASSES = "serviceClasses"; // NOI18N
    public static final String SERVICE_ACTIONS = "debugger_actions"; // NOI18N
    public static final String SERVICE_ENABLED_MIMETYPES = "debugger_activateForMIMETypes"; // NOI18N

    private String serviceName;
    private Class[] serviceClasses;
    private Map methodValues;
    //private ContextProvider context;
    private Object delegate;

    private Map<ContextProvider, WeakReference<Object>> contextInstances = new WeakHashMap<ContextProvider, WeakReference<Object>>();
    private WeakReference<Object> noContextInstance = new WeakReference<Object>(null);

    public ContextAwareServiceHandler(String serviceName, Class[] serviceClasses,
                                       Map methodValues) {
        this(serviceName, serviceClasses, methodValues, null);
    }

    private ContextAwareServiceHandler(String serviceName, Class[] serviceClasses,
                                       Map methodValues, ContextProvider context) {
        this.serviceName = serviceName;
        this.serviceClasses = serviceClasses;
        this.methodValues = methodValues;
        //this.context = context;
    }
    
    /*
    private synchronized Object getDelegate() {
        if (delegate == null) {
            delegate = ContextAwareSupport.createInstance(serviceName, context);
        }
        return delegate;
    }
    */

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("forContext")) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Have "+args.length+" arguments, expecting one argument.");
            }
            if (!(args[0] == null || args[0] instanceof ContextProvider)) {
                throw new IllegalArgumentException("Argument "+args[0]+" is not an instance of ContextProvider.");
            }
            ContextProvider context = (ContextProvider) args[0];
            /*if (context == this.context) {
                return proxy;
            }*/
            synchronized (this) {
                Object instance;
                if (context == null) {
                    instance = noContextInstance.get();
                } else {
                    WeakReference<Object> ref = contextInstances.get(context);
                    instance = (ref != null) ? ref.get() : null;
                }
                if (instance == null) {
                    instance = ContextAwareSupport.createInstance(serviceName, context);
                    if (context == null) {
                        noContextInstance = new WeakReference(instance);
                    } else {
                        contextInstances.put(context, new WeakReference(instance));
                    }
                }
                return instance;
            }
            /* If total laziness is necessary:
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            return Proxy.newProxyInstance(
                    cl,
                    serviceClasses,//new Class[] { serviceClass },
                    new ContextAwareServiceHandler(serviceName, serviceClasses, methodValues, context));
             */
        //} else if (methodValues.containsKey(methodName) && args.length == 0) {
        //    return methodValues.get(methodName);
        } else {
            if (method.getName().equals("toString")) {
                return ContextAwareServiceHandler.class.getSimpleName()+" for "+serviceName;
            }
            /*
            try {
                return method.invoke(getDelegate(), args);
            } catch (IllegalArgumentException exc) {
                throw new UnsupportedOperationException(
                        "Method "+method.getName()+                             // NOI18N
                        " with arguments "+java.util.Arrays.asList(args)+       // NOI18N
                        " can not be called on this virtual object!", exc);     // NOI18N
            }
             */
            throw new UnsupportedOperationException(
                    "Method "+method.getName()+                             // NOI18N
                    " with arguments "+                                   // NOI18N
                    ((args == null) ? null : java.util.Arrays.asList(args))+
                    " can not be called on this virtual object!");     // NOI18N
        }
    }

}
