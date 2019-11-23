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

package org.netbeans.modules.editor.lib;

import java.awt.Dimension;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public final class SettingsConversions {

    private static final Logger LOG = Logger.getLogger(SettingsConversions.class.getName());
    
    /** Coverts Insets to String representation */
    public static String insetsToString(Insets ins) {
        StringBuilder sb = new StringBuilder();
        sb.append(ins.top);
        sb.append(','); //NOI18N

        sb.append(ins.left);
        sb.append(','); //NOI18N

        sb.append(ins.bottom);
        sb.append(','); //NOI18N

        sb.append(ins.right);

        return sb.toString();
    }

    /** Converts textual representation of Insets */
    public static Insets parseInsets(String s) {
        StringTokenizer st = new StringTokenizer(s, ","); //NOI18N

        int arr[] = new int[4];
        int i = 0;
        while (st.hasMoreElements()) {
            if (i > 3) {
                return null;
            }
            try {
                arr[i] = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, null, nfe);
                return null;
            }
            i++;
        }
        if (i != 4) {
            return null;
        } else {
            return new Insets(arr[0], arr[1], arr[2], arr[3]);
        }
    }
    
    public static String dimensionToString(Dimension dim) {
        StringBuilder sb = new StringBuilder();
        sb.append(dim.width);
        sb.append(','); //NOI18N
        sb.append(dim.height);
        return sb.toString();
    }

    public static Dimension parseDimension(String s) {
        StringTokenizer st = new StringTokenizer(s, ","); // NOI18N

        int arr[] = new int[2];
        int i = 0;
        while (st.hasMoreElements()) {
            if (i > 1) {
                return null;
            }
            try {
                arr[i] = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, null, nfe);
                return null;
            }
            i++;
        }
        if (i != 2) {
            return null;
        } else {
            return new Dimension(arr[0], arr[1]);
        }
    }

    public static Object callFactory(String factoryRef, MimePath mimePath) {
        int lastDot = factoryRef.lastIndexOf('.'); //NOI18N
        assert lastDot != -1 : "Need fully qualified name of class with the setting factory method."; //NOI18N

        String classFqn = factoryRef.substring(0, lastDot);
        String methodName = factoryRef.substring(lastDot + 1);

        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class factoryClass = loader.loadClass(classFqn);
            Method factoryMethod = factoryClass.getDeclaredMethod(methodName, MimePath.class);

            return factoryMethod.invoke(null, mimePath);
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
            return null;
        }
    }

    private static volatile boolean noSettingsChangeCalls = false;
    private static final Map<Class, Boolean> settingsChangeAvailable = Collections.synchronizedMap(new WeakHashMap<Class, Boolean>());
    public static void callSettingsChange(Object instance) {
        assert instance != null : "The instance parameter should not be null"; //NOI18N
        
        if (noSettingsChangeCalls) {
            return;
        }
        
        Class eventClass;
        try {
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            if (loader == null) {
                // fallback on to the classloader that loaded this class, eg useful in tests
                loader = SettingsConversions.class.getClassLoader();
            }
            eventClass = loader.loadClass("org.netbeans.editor.SettingsChangeEvent"); //NOI18N
        } catch (ClassNotFoundException e) {
            // editor/deprecated/pre61settings was not loaded at all, there is nobody implementing
            // public void settingsChange(SettingsChangeEvent evt) method
            noSettingsChangeCalls = true;
            return;
        }

        Class<?> clazz = instance.getClass();
        Boolean hasMethod = settingsChangeAvailable.get(clazz);
        if (hasMethod == null || hasMethod.booleanValue()) {
            Method method = null;
            try {
                method = clazz.getMethod("settingsChange", eventClass);
            } catch (NoSuchMethodException e) {
                // the instance class does not implement public void settingsChange(SettingsChangeEvent evt) method,
                // remember and ignore
            }

            if (method != null && method.isAccessible()) {
                settingsChangeAvailable.put(clazz, true);
            } else {
                settingsChangeAvailable.put(clazz, false);
                return;
            }
            
            try {
                method.invoke(instance, (Object) null);
            } catch (InvocationTargetException ite) {
                // client code exception, we should rethrow it
                throw new RuntimeException(ite.getCause());
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
    }
    
    private SettingsConversions() {
        
    }
}
