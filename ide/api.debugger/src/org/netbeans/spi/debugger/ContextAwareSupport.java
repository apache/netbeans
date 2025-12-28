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

package org.netbeans.spi.debugger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.debugger.registry.ContextAwareServiceHandler;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Support class for context-aware debugger services.
 *
 * @author Martin Entlicher
 * @since 1.16
 */
public final class ContextAwareSupport {

    private ContextAwareSupport() {}

    /**
     * A helper method that creates a context-aware debugger service.
     * The service must be a public class with public constructor or a public
     * static method. The constructor or method takes
     * no arguments or takes {@link ContextProvider} as an argument.
     * <p>
     * This method is typically called by the implementation of {@link ContextAwareService}
     * to create an instance with the given context.
     *
     * @param service The full class name or static method that ends with '()'
     * @param context The context with which is the service to be created
     * @return The instance of the service with the given context.
     * @since 1.16
     */
    public static Object createInstance(String service, ContextProvider context) {
        try {
            ClassLoader cl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
            String method = null;
            if (service.endsWith("()")) {
                int lastdot = service.lastIndexOf('.');
                if (lastdot < 0) {
                    Exceptions.printStackTrace(
                            new IllegalStateException("Bad service - dot before method name is missing: " +
                            "'" + service + "'."));
                    return null;
                }
                method = service.substring(lastdot + 1, service.length() - 2).trim();
                service = service.substring(0, lastdot);
            }
            Class cls = cl.loadClass (service);

            Object o = null;
            if (method != null) {
                Method m = null;
                if (context != null) {
                    try {
                        m = cls.getDeclaredMethod(method, new Class[] { ContextProvider.class });
                    } catch (NoSuchMethodException nsmex) {}
                }
                if (m == null) {
                    try {
                        m = cls.getDeclaredMethod(method, new Class[] { });
                    } catch (NoSuchMethodException nsmex) {}
                }
                if (m != null) {
                    o = m.invoke(null, (m.getParameterTypes().length == 0)
                                 ? new Object[] {} : new Object[] { context });
                }
            }
            if (o == null && context != null) {
                Constructor[] cs = cls.getConstructors ();
                int i, k = cs.length;
                for (i = 0; i < k; i++) {
                    Constructor c = cs [i];
                    if (c.getParameterTypes ().length != 1 ||
                        !ContextProvider.class.isAssignableFrom(c.getParameterTypes()[0])) {
                        continue;
                    }
                    try {
                        o = c.newInstance (new Object[] {context});
                    } catch (IllegalAccessException e) {
                        Exceptions.printStackTrace(Exceptions.attachMessage(e, "service: " + service));
                    } catch (IllegalArgumentException e) {
                        Exceptions.printStackTrace(Exceptions.attachMessage(e, "service: " + service));
                    }
                }
            }
            if (o == null)
                o = cls.getDeclaredConstructor().newInstance();
            if (Logger.getLogger(ContextAwareSupport.class.getName()).isLoggable(Level.FINE)) {
                Logger.getLogger(ContextAwareSupport.class.getName()).fine("instance "+o+" created.");
            }
            return o;
        } catch (ClassNotFoundException e) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                        e,
                        "The service "+service+" not found."));
        } catch (InstantiationException | NoSuchMethodException e) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    e,
                    "The service "+service+" can not be instantiated. Context = "+context));
        } catch (IllegalAccessException e) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    e,
                    "The service "+service+" can not be accessed. Context = "+context));
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof ThreadDeath) {
                throw (ThreadDeath) ex.getCause();
            }
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    ex,
                    "The service "+service+" can not be created. Context = "+context));
        } catch (ExceptionInInitializerError ex) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    ex,
                    "The service "+service+" can not be initialized. Context = "+context));
        }
        return null;
    }

    /**
     * Creates instance of <code>ContextAwareService</code> based on layer.xml
     * attribute values
     *
     * @param attrs attributes loaded from layer.xml
     * @return new <code>ContextAwareService</code> instance
     */
    static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
        String serviceName = (String) attrs.get(ContextAwareServiceHandler.SERVICE_NAME);
        String serviceClasses = (String) attrs.get(ContextAwareServiceHandler.SERVICE_CLASSES);
        if (serviceClasses == null) {
            serviceClasses = (String) attrs.get("instanceOf");
        }
        String[] serviceClassNames = splitClasses(serviceClasses);

        //Map methodValues = new HashMap(attrs); - MUST NOT DO THAT! Creates a loop initializing the entries from XML
        //methodValues.remove(SERVICE_NAME);
        //methodValues.remove(SERVICE_CLASS);

        ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
        Class[] classes = new Class[serviceClassNames.length + 1];
        classes[0] = ContextAwareService.class;
        for (int i = 0; i < serviceClassNames.length; i++) {
            classes[i+1] = Class.forName(serviceClassNames[i], true, cl);
        }
        return (ContextAwareService)
                Proxy.newProxyInstance(
                    cl,
                    classes,
                    new ContextAwareServiceHandler(serviceName, classes, Collections.emptyMap()));
    }

    private static String[] splitClasses(String classes) {
        return classes.split("[, ]+");
    }

}
