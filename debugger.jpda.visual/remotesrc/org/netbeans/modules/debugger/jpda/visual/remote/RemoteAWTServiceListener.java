/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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