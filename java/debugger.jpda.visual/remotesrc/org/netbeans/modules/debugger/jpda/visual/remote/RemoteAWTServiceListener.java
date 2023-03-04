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
package org.netbeans.modules.debugger.jpda.visual.remote;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RemoteAWTServiceListener implements InvocationHandler {
    
    private final Component c;
    private final Class listenerClass;
    
    public RemoteAWTServiceListener(Component c, Class listenerClass) {
        this.c = c;
        this.listenerClass = listenerClass;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("equals".equals(methodName) && args.length == 1) {
            return (proxy == args[0]) ? Boolean.TRUE : Boolean.FALSE;
        }
        logEventData(methodName, args.length > 0 ? args[0] : null, listenerClass);
        return null;
    }
    
    public EventListener createLoggingListener(Class c) {
        return (EventListener) Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, this);
    }
    
    public static Object add(Component c, Class listenerClass) {
        String addName = "add"+listenerClass.getSimpleName();
        Method addListenerMethod;
        try {
            addListenerMethod = c.getClass().getMethod(addName, new Class[] { listenerClass });
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ex) {
            return null;
        }
        RemoteAWTServiceListener rl = new RemoteAWTServiceListener(c, listenerClass);
        Object listener = rl.createLoggingListener(listenerClass);
        try {
            addListenerMethod.invoke(c, new Object[] { listener });
        } catch (Exception ex) {
            return null;
        }
        return listener;
    }
    
    public static boolean remove(Component c, Class listenerClass, Object listener) {
        String removeName = "remove"+listenerClass.getSimpleName();
        Method removeListenerMethod;
        try {
            removeListenerMethod = c.getClass().getMethod(removeName, new Class[] { listenerClass });
        } catch (NoSuchMethodException ex) {
            return false;
        } catch (SecurityException ex) {
            return false;
        }
        try {
            Object removed = removeListenerMethod.invoke(c, new Object[] { listener });
            if (removed instanceof Boolean) {
                return ((Boolean) removed).booleanValue();
            } else {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }
    
    private void logEventData(String methodName, Object event, Class listenerClass) {
        //System.err.println("RemoteServiceListener.logEventData("+methodName+", "+event+")");
        String toString = String.valueOf(event);
        Map properties = new HashMap();
        if (event != null) {
            Method[] methods = event.getClass().getMethods();
            for (int mi = 0; mi < methods.length; mi++) {
                Method m = methods[mi];
                String mname = m.getName();
                if ((mname.startsWith("get") || mname.startsWith("is") || mname.equals("paramString")) &&
                    m.getParameterTypes().length == 0) {

                    if (mname.startsWith("get") && mname.length() > 3) {
                        char c1 = mname.charAt(3);
                        if (mname.length() <= 4 || !Character.isUpperCase(mname.charAt(4))) {
                            c1 = Character.toLowerCase(c1);
                        }
                        mname = c1 + mname.substring(4);
                    }
                    if (mname.startsWith("is") && mname.length() > 2) {
                        mname = Character.toLowerCase(mname.charAt(2)) + mname.substring(3);
                    }
                    Object value;
                    try {
                        value = m.invoke(event, new Object[] {});
                    } catch (Exception ex) {
                        continue;
                    }
                    String valueStr = String.valueOf(value);
                    properties.put(mname, valueStr);
                }
            }
        }
        String[] data = new String[2 + 2*properties.size()];
        int i = 0;
        data[i++] = methodName;
        data[i++] = toString;
        for (Iterator it = properties.keySet().iterator(); it.hasNext(); ) {
            String name = (String) it.next();
            String value = (String) properties.get(name);
            data[i++] = name;
            data[i++] = value;
        }
        String[] stack = retrieveStack();
        RemoteAWTService.pushEventData(c, listenerClass, data, stack);
    }
    
    private static final int STACK_OFFSET = 8;
    
    private static String[] retrieveStack() {
        //Thread.currentThread().getStackTrace(); JDK 5 call
        Thread t = Thread.currentThread();
        try {
            Method getStackTrace;
            try {
                getStackTrace = Thread.class.getMethod("getStackTrace", new Class[] {});
            } catch (Exception ex) {
                // JDK 5 methods not available
                // new Exception().getStackTrace(); JDK 1.4 call
                getStackTrace = Exception.class.getMethod("getStackTrace", new Class[] {});
            }
            Object[] stackTraceElements = (Object[]) getStackTrace.invoke(t, new Object[] {});
            int n = stackTraceElements.length;
            int s = Math.min(STACK_OFFSET, n);
            String[] elements = new String[n - s];
            for (int i = s; i < n; i++) {
                Method getClassName = stackTraceElements[i].getClass().getMethod("getClassName", new Class[] {});
                String className = (String) getClassName.invoke(stackTraceElements[i], new Object[] {});
                Method getMethodName = stackTraceElements[i].getClass().getMethod("getMethodName", new Class[] {});
                String methodName = (String) getMethodName.invoke(stackTraceElements[i], new Object[] {});
                Method getFileName = stackTraceElements[i].getClass().getMethod("getFileName", new Class[] {});
                String fileName = (String) getFileName.invoke(stackTraceElements[i], new Object[] {});
                Method getLineNumber = stackTraceElements[i].getClass().getMethod("getLineNumber", new Class[] {});
                Integer lineNumber = (Integer) getLineNumber.invoke(stackTraceElements[i], new Object[] {});
                elements[i - s] = className + "." + methodName + "(" + fileName + ":" + lineNumber + ")";
            }
            return elements;
        } catch (Exception ex) {
        }
        // JDK 1.3 or older
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new Exception().printStackTrace(pw);
        pw.close();
        StringBuffer sb = sw.getBuffer();
        // TODO
        return new String[] { sb.toString() };
    }
    
}