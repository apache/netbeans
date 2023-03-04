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
package org.netbeans.lib.profiler.server.system;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author Tomas Hurka
 */
class Histogram18 {
    private static final String LIVE_OBJECTS_OPTION = "-live";  // NOI18N
    private static final String ALL_OBJECTS_OPTION = "-all";    // NOI18N    
    private static final String VIRTUAL_MACHINE_CLASS = "com.sun.tools.attach.VirtualMachine"; // NOI18N
    private static final String HS_VIRTUAL_MACHINE_CLASS = "sun.tools.attach.HotSpotVirtualMachine"; // NOI18N
    private static final String VIRTUAL_MACHINE_ATTACH_METHOD = "attach";   // NOI18N
    private static final String VIRTUAL_MACHINE_HEAPHISTO_METHOD = "heapHisto";   // NOI18N    
    private static String selfPid;
    private static Method vmAttach;
    private static Method vmHisto;
    private static Object virtualMachine;
   
    static InputStream getRawHistogram() {
        try {
            if (virtualMachine == null) {
                virtualMachine = vmAttach.invoke(null, selfPid);
            }
            Object ret = vmHisto.invoke(virtualMachine, new Object[]{new Object[]{ALL_OBJECTS_OPTION}});
            if (ret instanceof InputStream) {
                return (InputStream)ret;
            }
        } catch (IllegalAccessException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (InvocationTargetException ex) {
            return null;
        }
        return null;
    }

    private static ClassLoader getToolsJar() {
        File home = getJavaHome();
        File toolsJar = new File(home,"lib/tools.jar"); // NOI18N
        if (toolsJar.exists() && toolsJar.isFile()) {
            try {
                return new URLClassLoader(new URL[] {toolsJar.toURI().toURL()});
            } catch (MalformedURLException ex) {
                return null;
            }
        }
        return null;
    }
    
    private static File getJavaHome() {
        File jdkHome = new File(System.getProperty("java.home")); // NOI18N
        if ("jre".equals(jdkHome.getName())) {  // NOI18N
           jdkHome = jdkHome.getParentFile(); 
        }
        return jdkHome;
    }

    static boolean initialize() {
        try {
            Class vmClass = loadClass(VIRTUAL_MACHINE_CLASS);
            Class hsVmClass = Class.forName(HS_VIRTUAL_MACHINE_CLASS, true, vmClass.getClassLoader()); 
            vmAttach = vmClass.getMethod(VIRTUAL_MACHINE_ATTACH_METHOD, String.class);
            vmHisto = hsVmClass.getMethod(VIRTUAL_MACHINE_HEAPHISTO_METHOD, Object[].class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return false;
        } catch (SecurityException ex) {
            ex.printStackTrace();
            return false;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
        String selfName = ManagementFactory.getRuntimeMXBean().getName();
        selfPid = selfName.substring(0, selfName.indexOf('@')); // NOI18N
        return true;
    }
    
    /** load class from tools.jar
     * 
     * @param className class name
     * @return class
     * @throws ClassNotFoundException 
     */
    private static Class loadClass(String className) throws ClassNotFoundException {
        // try boot classloader first
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
//            ex.printStackTrace();
        }
        // locate and try to use tools.jar
        ClassLoader toolsJar = getToolsJar();
        if (toolsJar == null) {
            throw new ClassNotFoundException(className);
        }
        return Class.forName(className, true, toolsJar);
    }
}
