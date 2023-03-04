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

package org.netbeans.modules.debugger.jpda.jdi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Generates wrapper methods for JDI calls.
 * Use "ant generate" to run this class and generate JDI wrapper classes.
 */
public class Generate {

    private static final String JDI_CLASSES_PATH = "com/sun/jdi";

    private static final Class[] RUNTIME_EXCEPTIONS = new Class[]
        { com.sun.jdi.ClassNotPreparedException.class, com.sun.jdi.InconsistentDebugInfoException.class,
          com.sun.jdi.InternalException.class, com.sun.jdi.InvalidStackFrameException.class,
          com.sun.jdi.NativeMethodException.class, com.sun.jdi.ObjectCollectedException.class,
          com.sun.jdi.VMCannotBeModifiedException.class, com.sun.jdi.VMDisconnectedException.class,
          com.sun.jdi.VMOutOfMemoryException.class,
          com.sun.jdi.request.DuplicateRequestException.class,
          com.sun.jdi.request.InvalidRequestStateException.class };

    private static final String PACKAGE = "org.netbeans.modules.debugger.jpda.jdi";

    private static final Map<Class, String> EXCEPTION_WRAPPERS = new LinkedHashMap<Class, String>();

    private static final Map<String/*class name*/, Map<String/*method*/, Set<Class/*exception*/>>> EXCEPTIONS_BY_METHODS = new LinkedHashMap<String, Map<String, Set<Class>>>();

    // Fake values can be returned if these exceptions are thrown:
    private static final Set<Class> SILENT_EXCEPTIONS = Collections.unmodifiableSet(new LinkedHashSet<Class>(Arrays.asList(new Class[] {
           com.sun.jdi.InternalException.class, com.sun.jdi.ObjectCollectedException.class, com.sun.jdi.VMDisconnectedException.class })));

    private static final Set<String> NOT_USED_CLASSES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {
            com.sun.jdi.Accessible.class.getName(), com.sun.jdi.Bootstrap.class.getName(),
            com.sun.jdi.ClassLoaderReference.class.getName(),
            com.sun.jdi.PathSearchingVirtualMachine.class.getName(),
            com.sun.jdi.VoidValue.class.getName(),
            // Connectors are used in API and UI modules.
            // Classes starting with "com.sun.jdi.connect" are not generated
            com.sun.jdi.event.EventIterator.class.getName(),
    })));
    
    private static final String METHODS_BY_JDK = "MethodsByJDK";

    private static String license = null;

    // Runtime exceptions:
    // VMDisconnectedException on all JDI calls
    // InternalException on all JDI calls
    // ObjectCollectedException on all calls on ObjectReference
    // Other exceptions on selected methods

    static {
        Map<String, Set<Class>> AllClassExceptions = new LinkedHashMap<String, Set<Class>>();
        AllClassExceptions.put("*", new LinkedHashSet<Class>(Arrays.asList(
                new Class[] { com.sun.jdi.InternalException.class,
                              com.sun.jdi.VMDisconnectedException.class })));
        EXCEPTIONS_BY_METHODS.put("*", AllClassExceptions);

        Map<String, Set<Class>> ReferenceTypeExceptions = new LinkedHashMap<String, Set<Class>>();
        ReferenceTypeExceptions.put("fields", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("visibleFields", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("allFields", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("fieldByName", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("methods", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("visibleMethods", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("allMethods", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("methodsByName", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("allLineLocations", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("locationsOfLine", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ReferenceTypeExceptions.put("classObject", Collections.singleton((Class) java.lang.UnsupportedOperationException.class));
        ReferenceTypeExceptions.put("*", Collections.singleton((Class) com.sun.jdi.ObjectCollectedException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.ReferenceType.class.getName(), ReferenceTypeExceptions);
        Map<String, Set<Class>> ClassTypeExceptions = new LinkedHashMap<String, Set<Class>>();
        ClassTypeExceptions.put("interfaces", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ClassTypeExceptions.put("allInterfaces", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ClassTypeExceptions.put("setValue", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class)); // JDWP protocol says that this can be thrown!
        ClassTypeExceptions.put("concreteMethodByName", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        ClassTypeExceptions.put("invokeMethod", Collections.singleton((Class) com.sun.jdi.ObjectCollectedException.class));
        ClassTypeExceptions.put("newInstance", Collections.singleton((Class) com.sun.jdi.ObjectCollectedException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.ClassType.class.getName(), ClassTypeExceptions);
        Map<String, Set<Class>> InterfaceTypeExceptions = new LinkedHashMap<String, Set<Class>>();
        InterfaceTypeExceptions.put("superinterfaces", Collections.singleton((Class) com.sun.jdi.ClassNotPreparedException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.InterfaceType.class.getName(), InterfaceTypeExceptions);
        Map<String, Set<Class>> TypeExceptions = new LinkedHashMap<String, Set<Class>>();
        TypeExceptions.put("*", Collections.singleton((Class) com.sun.jdi.ObjectCollectedException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.Type.class.getName(), TypeExceptions);

        Map<String, Set<Class>> ObjectReferenceExceptions = new LinkedHashMap<String, Set<Class>>();
        ObjectReferenceExceptions.put("setValue", Collections.singleton((Class) java.lang.IllegalArgumentException.class));
        ObjectReferenceExceptions.put("disableCollection", Collections.singleton((Class) java.lang.UnsupportedOperationException.class));
        ObjectReferenceExceptions.put("enableCollection", Collections.singleton((Class) java.lang.UnsupportedOperationException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.ObjectReference.class.getName(), ObjectReferenceExceptions);

        Map<String, Set<Class>> FieldExceptions = new LinkedHashMap<String, Set<Class>>();
        FieldExceptions.put("type", Collections.singleton((Class) com.sun.jdi.ObjectCollectedException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.Field.class.getName(), FieldExceptions);

        Map<String, Set<Class>> ThreadReferenceExceptions = new LinkedHashMap<String, Set<Class>>();
        // IllegalThreadStateException is thrown through JDWPException when INVALID_THREAD is received from JDWP.
        ThreadReferenceExceptions.put("*", Collections.singleton((Class) IllegalThreadStateException.class));
        ThreadReferenceExceptions.put("popFrames", new LinkedHashSet<Class>(Arrays.asList(
                new Class[] { com.sun.jdi.NativeMethodException.class,
                              com.sun.jdi.InvalidStackFrameException.class })));
        ThreadReferenceExceptions.put("forceEarlyReturn", new LinkedHashSet<Class>(Arrays.asList(
                new Class[] { com.sun.jdi.NativeMethodException.class,
                              com.sun.jdi.InvalidStackFrameException.class })));
        ThreadReferenceExceptions.put("frameCount", new LinkedHashSet<Class>(Arrays.asList(
                new Class[] { com.sun.jdi.InvalidStackFrameException.class })));// #233070
        ThreadReferenceExceptions.put("threadGroup", new LinkedHashSet<Class>(Arrays.asList(
                new Class[] { com.sun.jdi.VMOutOfMemoryException.class })));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.ThreadReference.class.getName(), ThreadReferenceExceptions);

        Map<String, Set<Class>> StackFrameExceptions = new LinkedHashMap<String, Set<Class>>();
        StackFrameExceptions.put("*", Collections.singleton((Class) com.sun.jdi.InvalidStackFrameException.class));
        StackFrameExceptions.put("setValue", Collections.singleton((Class) com.sun.jdi.ObjectCollectedException.class));
        StackFrameExceptions.put("visibleVariableByName", Collections.singleton((Class) com.sun.jdi.NativeMethodException.class));
        StackFrameExceptions.put("visibleVariables", Collections.singleton((Class) com.sun.jdi.NativeMethodException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.StackFrame.class.getName(), StackFrameExceptions);

        Map<String, Set<Class>> MonitorInfoExceptions = new LinkedHashMap<String, Set<Class>>();
        MonitorInfoExceptions.put("*", Collections.singleton((Class) com.sun.jdi.InvalidStackFrameException.class));
        EXCEPTIONS_BY_METHODS.put("com.sun.jdi.MonitorInfo", MonitorInfoExceptions);

        Map<String, Set<Class>> VirtualMachineExceptions = new LinkedHashMap<String, Set<Class>>();
        // UnsupportedOperationException can be thrown on J2ME:
        VirtualMachineExceptions.put("mirrorOf(java.lang.String)", Collections.singleton((Class) java.lang.UnsupportedOperationException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.VirtualMachine.class.getName(), VirtualMachineExceptions);

        Map<String, Set<Class>> EventRequestExceptions = new LinkedHashMap<String, Set<Class>>();
        // ObjectCollectedException can be thrown
        EventRequestExceptions.put("disable", new LinkedHashSet<Class>(Arrays.asList(
                new Class [] { com.sun.jdi.ObjectCollectedException.class,
                               com.sun.jdi.request.InvalidRequestStateException.class })));
        EventRequestExceptions.put("enable", new LinkedHashSet<Class>(Arrays.asList(
                new Class [] { com.sun.jdi.ObjectCollectedException.class,
                               com.sun.jdi.request.InvalidRequestStateException.class })));
        EventRequestExceptions.put("setEnabled", new LinkedHashSet<Class>(Arrays.asList(
                new Class [] { com.sun.jdi.ObjectCollectedException.class,
                               com.sun.jdi.request.InvalidRequestStateException.class })));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.request.EventRequest.class.getName(), EventRequestExceptions);
        Map<String, Set<Class>> EventRequestManagerExceptions = new LinkedHashMap<String, Set<Class>>();
        EventRequestManagerExceptions.put("deleteEventRequest", Collections.singleton((Class)
                com.sun.jdi.request.InvalidRequestStateException.class));
        EventRequestManagerExceptions.put("deleteEventRequests", Collections.singleton((Class)
                com.sun.jdi.request.InvalidRequestStateException.class));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.request.EventRequestManager.class.getName(), EventRequestManagerExceptions);

        Map<String, Set<Class>> EventSetExceptions = new LinkedHashMap<String, Set<Class>>();
        // IllegalThreadStateException is thrown through JDWPException when INVALID_THREAD is received from JDWP.
        // ObjectCollectedException can be thrown when the thread is collected.
        EventSetExceptions.put("resume", new LinkedHashSet<Class>(Arrays.asList(
                new Class [] { IllegalThreadStateException.class,
                               com.sun.jdi.ObjectCollectedException.class })));
        EXCEPTIONS_BY_METHODS.put(com.sun.jdi.event.EventSet.class.getName(), EventSetExceptions);
    }


    private static String readLicense() throws IOException {
        StringBuilder sb = new StringBuilder();
        File f = new File(System.getProperty("user.dir"));
        f = new File(f, "gensrc/"+Generate.class.getName().replace('.', '/')+".java");
        BufferedReader r = new BufferedReader(new FileReader(f));
        while (true) {
            String line = r.readLine();
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
            if (line.trim().indexOf("*/") >= 0) {
                break;
            }
        }
        r.close();
        return sb.toString();
    }

    private static String getLicense() {
        if (license == null) {
            try {
                license = readLicense();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
        return license;
    }

    private static String generateWrapperException(File dir, Class jdiException) throws IOException {
        String name = jdiException.getSimpleName();
        String cName = name + "Wrapper";
        String eName = jdiException.getName();
        File cf = new File(dir, cName+".java");
        Writer w = new BufferedWriter(new FileWriter(cf));
        w.write(getLicense());
        w.write("\npackage "+PACKAGE+";\n\n");
        w.write("/**\n * Wrapper for "+name+" JDI exception.\n * The calling code must count with this exception being thrown.\n */\n");
        w.write("public final class "+cName+" extends Exception {\n");
        w.write("\n    public "+cName+"("+eName+" ex) {\n");
        w.write("        super(ex);\n");
        w.write("    }\n\n");
        w.write("    @Override\n");
        w.write("    public "+eName+" getCause() {\n");
        w.write("        return ("+eName+") super.getCause();\n");
        w.write("    }\n\n");
        w.write("}\n");
        w.close();
        return PACKAGE+"."+cName;
    }

    private static List<Class> getJDIClasses() throws IOException {
        List<Class> classes = new ArrayList<Class>();
        URL resource = ClassLoader.getSystemClassLoader().getResource("com/sun/jdi/VirtualMachine.class");
        String jarFileName = resource.getFile();
        jarFileName = jarFileName.substring("file:".length(), jarFileName.indexOf('!'));
        ZipFile jar = new ZipFile(jarFileName);
        Enumeration<? extends ZipEntry> classEntries = jar.entries();
        while (classEntries.hasMoreElements()) {
            ZipEntry classEntry = classEntries.nextElement();
            String name = classEntry.getName();
            if (name.startsWith(JDI_CLASSES_PATH) && name.endsWith(".class")) {
                String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                //className = className.replace('$', '.');
                //System.err.println("Have class from JAR: '"+className+"'");
                Class c;
                try {
                    c = Class.forName(className);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                if (Throwable.class.isAssignableFrom(c)) {
                    continue;
                }
                classes.add(c);
            }
        }
        Collections.sort(classes, new Comparator<Class>() {
            public int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return classes;
    }

    private static void generate(File dir) throws IOException {
        List<Class> classes = getJDIClasses();
        for (Map<String, Set<Class>> map : EXCEPTIONS_BY_METHODS.values()) {
            for (Set<Class> set : map.values()) {
                for (Class ex : set) {
                    if (!EXCEPTION_WRAPPERS.containsKey(ex)) {
                        EXCEPTION_WRAPPERS.put(ex, generateWrapperException(dir, ex));
                    }
                }
            }
        }
        EXCEPTION_WRAPPERS.put(com.sun.jdi.ObjectCollectedException.class, generateWrapperException(dir, com.sun.jdi.ObjectCollectedException.class));

        // Add classes and methods that are in JDK 1.6 and higher versions and generate reflection calls
        String jdkVersion = System.getProperty("java.version");
        System.err.println("jdkVersion = "+jdkVersion);
        File rootResource;
        try {
            rootResource = new File(Generate.class.getResource("").toURI());
        } catch (Exception ex) {
            IOException ioex = new IOException();
            ioex.initCause(ex);
            throw ioex;
        }

        File jdkLogFile = new File(rootResource, METHODS_BY_JDK+jdkVersion);
        Writer log = new BufferedWriter(new FileWriter(jdkLogFile));
        // Information about all JDI methods of the current JDK version are stored
        // in the following form:
        // <class name>:<wrapper class simple name> [isObjectReference]
        //  <method name>(<generic parameter types>):<generic return type>:<default return value> throws <exception classes>

        Map<String, List<String>> higherVersionMethods = getHigherVersionMethods(rootResource, jdkVersion);
        int[] higherVersionMethodIndexes = new int[higherVersionMethods.size()];

        Set<String> higherVersionClasses = new HashSet<String>();
        int[] indexes = new int[higherVersionMethods.size()];
        for (Class c : classes) {
            if (c.getDeclaredMethods().length == 0) {
                continue;
            }
            int ii = 0;
            for (Iterator<String> it = higherVersionMethods.keySet().iterator(); it.hasNext(); ii++) {
                String version = it.next();
                List<String> methodsLog = higherVersionMethods.get(version);
                int i;
                for (i = indexes[ii]; i < methodsLog.size(); i++) {
                    String loggedClass = methodsLog.get(i);
                    if (loggedClass.startsWith(" ")) continue;
                    int colonIndex = loggedClass.indexOf(':');
                    if (!loggedClass.substring(0, colonIndex).equals(c.getName())) {
                        higherVersionClasses.add(loggedClass.substring(0, colonIndex).replace('$', '.'));
                    } else {
                        i++;
                        break;
                    }
                }
                indexes[ii] = i;
            }
        }
        System.out.println("\nHigher version classes: "+higherVersionClasses+"\n");
        
        for (Class c : classes) {
            String name = c.getSimpleName();
            String className = c.getName().replace('$', '.');
            String classPackage = c.getPackage().getName();
            String cName;
            Class enclosingClass = c.getEnclosingClass();
            if (enclosingClass != null) {
                cName = enclosingClass.getSimpleName() + name + "Wrapper";
                name = enclosingClass.getSimpleName() + "." + name;
            } else {
                cName = name + "Wrapper";
            }
            Method[] methods = c.getDeclaredMethods();
            if (methods.length == 0) {
                System.err.println("Class: "+c.getName());
                System.err.println(" - ignored, have no methods.");
                continue;
            }
            writeHigherVersionClasses(dir, c, higherVersionMethods, higherVersionMethodIndexes, higherVersionClasses);
            System.err.println("Class: "+c.getName());
            Arrays.sort(methods, new Comparator<Method>() {
                public int compare(Method m1, Method m2) {
                    int c = m1.getName().compareTo(m2.getName());
                    if (c == 0) {
                        StringBuilder p1 = new StringBuilder();
                        for (Class pt : m1.getParameterTypes()) {
                            p1.append(pt.getName());
                            p1.append(" ");
                        }
                        StringBuilder p2 = new StringBuilder();
                        for (Class pt : m2.getParameterTypes()) {
                            p2.append(pt.getName());
                            p2.append(" ");
                        }
                        c = p1.toString().compareTo(p2.toString());
                    }
                    return c;
                }
            });
            log.write(c.getName()+":"+cName+"\n");
            Writer w;
            if (NOT_USED_CLASSES.contains(c.getName()) || c.getName().startsWith("com.sun.jdi.connect")) {
                w = null;
            } else {
                w = writeClassHeader(dir, name, classPackage, cName, null);
            }
            for (Method m : methods) {
                writeHigherVersionMethods(w, m, className, higherVersionMethods, higherVersionMethodIndexes, higherVersionClasses);
                String mName = m.getName();
                Type[] paramTypes = m.getGenericParameterTypes();
                String rType = translateType(m.getGenericReturnType());
                String defaultReturn = getDefaultReturn(m.getReturnType());
                Class[] exceptionTypes = m.getExceptionTypes();
                
                System.err.println("  Method: "+mName);
                logMethod(log, mName, paramTypes, exceptionTypes, rType, defaultReturn);
                if (w != null) {
                    if (defaultReturn != null) {
                        writeMethod(w, c, className, mName, mName+"0", paramTypes, exceptionTypes, rType, defaultReturn);
                    }
                    writeMethod(w, c, className, mName, mName, paramTypes, exceptionTypes, rType, null);
                }
            }
            writeHigherVersionMethods(w, null, className, higherVersionMethods, higherVersionMethodIndexes, higherVersionClasses);
            if (w != null) {
                w.write("}\n");
                w.close();
            }
        }
        writeHigherVersionClasses(dir, null, higherVersionMethods, higherVersionMethodIndexes, higherVersionClasses);
        log.close();
    }

    private static Writer writeClassHeader(File dir, String name, String classPackage, String cName, String jdkVersion) throws IOException {
        String classDir = classPackage.replace('.', '/').substring(JDI_CLASSES_PATH.length());
        if (classDir.startsWith("/")) classDir = classDir.substring(1);
        File cDir = (classDir.length() == 0) ? dir : new File(dir, classDir);
        cDir.mkdirs();
        File cf = new File(cDir, cName+".java");
        Writer w = new BufferedWriter(new FileWriter(cf));
        String cPackage = classPackage.substring(JDI_CLASSES_PATH.length());
        w.write(getLicense());
        w.write("\npackage "+PACKAGE+cPackage+";\n\n");
        w.write("// DO NOT MODIFY THIS CODE, GENERATED AUTOMATICALLY\n");
        w.write("// Generated by "+Generate.class.getName()+" class located in 'gensrc' folder,\n// perform the desired modifications there and re-generate by \"ant generate\".\n\n");
        w.write("/**\n * Wrapper for "+name+" JDI class");
        if (jdkVersion != null) {
            w.write(" from JDK ");
            w.write(jdkVersion);
        }
        w.write(".\n * Use methods of this class instead of direct calls on JDI objects.\n"+
                " * These methods assure that exceptions thrown from JDI calls are handled appropriately.\n"+
                " *\n * @author Martin Entlicher\n */\n");
        w.write("public final class "+cName+" {\n");
        w.write("\n    private "+cName+"() {}\n\n");
        return w;
    }

    private static void logMethod(Writer log, String mName,
                                  Type[] paramTypes, Class[] exceptionTypes,
                                  String rType, String defaultReturn) throws IOException {
        log.write(" "+mName+"(");
        for (int i = 0; i < paramTypes.length; i++) {
            String paramType = translateType(paramTypes[i]);
            if (i > 0) log.write(", ");
            log.write(paramType);
        }
        log.write("):"+rType+":"+((defaultReturn != null) ? defaultReturn : ""));
        if (exceptionTypes.length > 0) {
            log.write(" throws ");
            for (int i = 0; i < exceptionTypes.length; i++) {
                if (i > 0) log.write(", ");
                log.write(exceptionTypes[i].getName());
            }
        }
        log.write("\n");
    }

    private static void writeMethod(Writer w, Class c, String className,
                                    String mName, String mGenName, Type[] paramTypes, Class[] exceptionTypes,
                                    String rType, String defaultReturn) throws IOException {
        w.write("    // DO NOT MODIFY THIS CODE, GENERATED AUTOMATICALLY\n");
        w.write("    public static "+rType+" "+mGenName+"("+className+" a");
        String[] paramNames = new String[paramTypes.length];
        StringBuilder paramTypesList = new StringBuilder("(");
        for (int i = 0; i < paramTypes.length; i++) {
            //Class t = paramTypes[i];
            paramNames[i] = Character.toString((char) ('a'+(i+1)));
            String paramType = translateType(paramTypes[i]);
            w.write(", "+paramType+" "+paramNames[i]);
            if (i > 0) paramTypesList.append(", ");
            paramTypesList.append(paramType);
        }
        w.write(")");
        paramTypesList.append(")");
        String mNameWithParamTypes = mName + paramTypesList.toString();

        // Add wrappers of JDI runtime exceptions...
        Set<Class> thrownExceptions = new LinkedHashSet<Class>();
        thrownExceptions.addAll(EXCEPTIONS_BY_METHODS.get("*").get("*"));
        if (com.sun.jdi.Value.class.isAssignableFrom(c) &&
            !com.sun.jdi.PrimitiveValue.class.isAssignableFrom(c)) {

            thrownExceptions.add(com.sun.jdi.ObjectCollectedException.class);
        }
        Map<String, Set<Class>> excByMethods = EXCEPTIONS_BY_METHODS.get(c.getName());
        if (excByMethods != null) {
            Set<Class> excs = excByMethods.get("*");
            if (excs != null) {
                thrownExceptions.addAll(excs);
            }
            excs = excByMethods.get(mNameWithParamTypes);
            if (excs == null) {
                excs = excByMethods.get(mName);
            }
            if (excs != null) {
                thrownExceptions.addAll(excs);
            }
        }

        Set<Class> caughtExceptions = new LinkedHashSet<Class>(thrownExceptions);
        if (defaultReturn != null) thrownExceptions.removeAll(SILENT_EXCEPTIONS);


        if (exceptionTypes.length > 0 || thrownExceptions.size() > 0) {
            w.write(" throws ");
            for (int i = 0; i < exceptionTypes.length; i++) {
                if (i > 0) w.write(", ");
                w.write(exceptionTypes[i].getName());
            }
            if (exceptionTypes.length > 0 && thrownExceptions.size() > 0) {
                w.write(", ");
            }
            int i = 0;
            for (Iterator it = thrownExceptions.iterator(); it.hasNext(); i++) {
                if (i > 0) w.write(", ");
                w.write(EXCEPTION_WRAPPERS.get(it.next()));
            }
        }
        w.write(" {\n");
        String preCode = methodPreCode(className, mName);
        if (preCode != null) {
            w.write(preCode);
        }
        w.write("        if (org.netbeans.modules.debugger.jpda.JDIExceptionReporter.isLoggable()) {\n");
        w.write("            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.logCallStart(\n"+
                "                    \""+className+"\",\n"+
                "                    \""+mName+"\",\n"+
                "                    \"JDI CALL: "+className+"({0})."+mName+"(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) {
                w.write(", ");
            }
            w.write("{"+Integer.toString(i+1)+"}");
        }
        w.write(")\",\n"+
                "                    new Object[] {a");
        for (int i = 0; i < paramNames.length; i++) {
            w.write(", ");
            w.write(paramNames[i]);
        }
        w.write("});\n");
        w.write("        }\n"); // if
        
        boolean isVoidReturn = "void".equals(rType);
        if (!isVoidReturn) {
            w.write("        Object retValue = null;\n");
        }

        w.write("        try {\n");

        StringBuffer exec = new StringBuffer();
        if (!isVoidReturn) {
            exec.append("            ").append(rType).append(" ret;\n");
            exec.append("            ret = ");
        } else {
            exec.append("            ");
        }
        exec.append("a."+mName+"(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) {
                exec.append(", ");
            }
            exec.append(paramNames[i]);
        }
        exec.append(");\n");
        if (!isVoidReturn) {
            exec.append("            retValue = ret;\n");
            exec.append("            return ret;\n");
        }
        w.write(methodImpl(className, mName, exec.toString()));

        w.write("        }");
        /*// First re-throw the checked exceptions:
        for (int i = 0; i < exceptionTypes.length; i++) {
            w.write(exceptionTypes[i].getName());
            w.write(" ex) {\n");
            w.write("            throw ex;\n");
            w.write("        } catch (");
        }*/
        for (Iterator<Class> it = caughtExceptions.iterator(); it.hasNext(); ) {
            Class cex = it.next();
            w.write(" catch (");
            w.write(cex.getName());
            w.write(" ex) {\n");
            if (!isVoidReturn) {
                w.write("            retValue = ex;\n");
            }
            if (com.sun.jdi.InternalException.class.equals(cex)) {
                w.write(reportOfJDIException(className, mName, "ex"));
            }
            if (com.sun.jdi.VMDisconnectedException.class.equals(cex)) {
                w.write(disposeVMOnVMDisconnectedException(className, mName));
            }
            if (defaultReturn != null && SILENT_EXCEPTIONS.contains(cex)) {
                w.write("            return "+defaultReturn+";\n");
            } else {
                w.write("            throw new "+EXCEPTION_WRAPPERS.get(cex)+"(ex);\n");
            }
            w.write("        }");
        }
        if (!isVoidReturn) {
            for (int i = 0; i < exceptionTypes.length; i++) {
                w.write(" catch (");
                w.write(exceptionTypes[i].getName());
                w.write(" ex) {\n");
                w.write("            retValue = ex;\n");
                w.write("            throw ex;\n        }");
            }
            w.write(" catch (Error err) {\n");
            w.write("            retValue = err;\n");
            w.write("            throw err;\n");
            w.write("        } catch (RuntimeException rex) {\n");
            w.write("            retValue = rex;\n");
            w.write("            throw rex;\n        }");

        }
        w.write(" finally {\n");
        w.write("            if (org.netbeans.modules.debugger.jpda.JDIExceptionReporter.isLoggable()) {\n");
        w.write("                org.netbeans.modules.debugger.jpda.JDIExceptionReporter.logCallEnd(\n"+
                "                        \""+className+"\",\n"+
                "                        \""+mName+"\",\n"+
                (isVoidReturn ?
                "                        org.netbeans.modules.debugger.jpda.JDIExceptionReporter.RET_VOID);\n" :
                "                        retValue);\n"));
        //w.write("                logger.log(java.util.logging.Level.FINER, \"          returned after {0} ns\", (t2 - t1));\n");
        w.write("            }\n");
        w.write("        }\n");
        w.write("    }\n\n");
    }

    public static void writeHigherVersionClasses(File dir, Class c,
                                                 Map<String, List<String>> higherVersionMethods,
                                                 int[] higherVersionMethodIndexes,
                                                 Set<String> higherVersionClasses) throws IOException {
        int versionIndex = 0;
        for (Iterator<String> it = higherVersionMethods.keySet().iterator(); it.hasNext(); versionIndex++) {
            String version = it.next();
            List<String> methodsLog = higherVersionMethods.get(version);
            while (higherVersionMethodIndexes[versionIndex] < methodsLog.size()) {
                String loggedClass = methodsLog.get(higherVersionMethodIndexes[versionIndex]++);
                int colonIndex = loggedClass.indexOf(':');
                if (c == null || !loggedClass.substring(0, colonIndex).equals(c.getName())) {
                    String loggedClassBinaryName = loggedClass.substring(0, colonIndex);
                    int lastDotIndex = loggedClassBinaryName.lastIndexOf('.');
                    String loggedClassPackage = loggedClassBinaryName.substring(0, lastDotIndex);
                    String loggedClassName = loggedClassBinaryName.replace('$', '.');
                    String loggedName = loggedClassName.substring(loggedClassName.lastIndexOf('.') + 1);
                    System.err.println("Have class: "+loggedName+" from JDK "+version);
                    Writer w;
                    if (NOT_USED_CLASSES.contains(loggedClassBinaryName) || loggedClassBinaryName.startsWith("com.sun.jdi.connect")) {
                        w = null;
                    } else {
                        w = writeClassHeader(dir, loggedName, loggedClassPackage, loggedClass.substring(colonIndex + 1), version);
                    }
                    int i;
                    for (i = higherVersionMethodIndexes[versionIndex]; i < methodsLog.size(); i++) {
                        String method = methodsLog.get(i);
                        if (!method.startsWith(" ")) {
                            break;
                        }
                        if (w != null) {
                            writeHigherVersionMethod(w, loggedClassName, method, version, higherVersionClasses);
                        }
                    }
                    higherVersionMethodIndexes[versionIndex] = i;
                    if (w != null) {
                        w.write("}\n");
                        w.close();
                    }
                } else {
                    break;
                }
            }
        }
    }

    private static void writeHigherVersionMethods(Writer w, Method m,
                                                  String className,
                                                  Map<String, List<String>> higherVersionMethods,
                                                  int[] higherVersionMethodIndexes,
                                                  Set<String> higherVersionClasses) throws IOException {
        //System.err.println("writeHigherVersionMethods("+m+")");
        int versionIndex = 0;
        for (Iterator<String> it = higherVersionMethods.keySet().iterator(); it.hasNext(); versionIndex++) {
            String version = it.next();
            List<String> methodsLog = higherVersionMethods.get(version);
            while (higherVersionMethodIndexes[versionIndex] < methodsLog.size()) {
                String loggedMethod = methodsLog.get(higherVersionMethodIndexes[versionIndex]);
                if (loggedMethod.startsWith(" ")) {
                    higherVersionMethodIndexes[versionIndex]++;
                    if (w != null && (m == null || !isLoggedMethod(m, loggedMethod))) {
                        //System.out.println(" Method "+loggedMethod.substring(1, loggedMethod.indexOf("):") + 1)+" from JDK "+version);
                        writeHigherVersionMethod(w, className, loggedMethod, version, higherVersionClasses);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            };
        }
    }

    private static boolean isLoggedMethod(Method m, String loggedMethod) {
        String mName = m.getName();
        Type[] paramTypes = m.getGenericParameterTypes();
        StringBuilder mLog = new StringBuilder(" ");
        mLog.append(mName);
        mLog.append("(");
        for (int i = 0; i < paramTypes.length; i++) {
            String paramType = translateType(paramTypes[i]);
            if (i > 0) mLog.append(", ");
            mLog.append(paramType);
        }
        mLog.append("):");
        return loggedMethod.startsWith(mLog.toString());
    }


    private static void writeHigherVersionMethod(Writer w, String className,
                                                 String methodLine, String jdkVersion,
                                                 Set<String> higherVersionClasses) throws IOException {
        methodLine = methodLine.trim();
        System.err.println("  Method: "+methodLine+" from JDK "+jdkVersion);
        int index = methodLine.indexOf('(');
        String mName = methodLine.substring(0, index);
        List<String> paramTypes = new ArrayList<String>();
        index++;
        int index2 = index;
        char c;
        while ((c = methodLine.charAt(index2)) != ')') {
            if (c == ',') {
                paramTypes.add(substituteHigherClasses(methodLine.substring(index, index2), higherVersionClasses));
                index = index2 + 2; // ", "
                index2 = index;
            } else if (c == '<') {
                index2 = findPair(methodLine, index2 + 1, '<', '>');
            } else {
                index2++;
            }
            //System.err.println("  c = "+c+", index = "+index+", index2 = "+index2);
        }
        if (index2 > index) {
            paramTypes.add(substituteHigherClasses(methodLine.substring(index, index2), higherVersionClasses));
        }
        String mNameWithParamTypes = methodLine.substring(0, index2 + 1);
        index = index2 + 2; // "):"
        index2 = methodLine.indexOf(":", index);
        String rType = methodLine.substring(index, index2);
        rType = substituteHigherClasses(rType, higherVersionClasses);
        index = index2 + 1; // ":"
        index2 = methodLine.indexOf(" throws ", index);
        String defaultReturn;
        List<String> exceptionTypes;
        if (index2 < 0) {
            defaultReturn = methodLine.substring(index).trim();
            exceptionTypes = Collections.emptyList();
        } else {
            defaultReturn = methodLine.substring(index, index2).trim();
            index = index2 + " throws ".length();
            exceptionTypes = new ArrayList<String>();
            for (index2 = index; index2 < methodLine.length(); index2++) {
                if (methodLine.charAt(index2) == ',') {
                    exceptionTypes.add(methodLine.substring(index, index2).trim());
                    index = index2 + 2; // ", "
                    index2 = index;
                }
            }
            exceptionTypes.add(methodLine.substring(index).trim());
        }
        if (defaultReturn.length() == 0) {
            defaultReturn = null;
        }

        Set<Class> thrownExceptions = new LinkedHashSet<Class>();
        thrownExceptions.addAll(EXCEPTIONS_BY_METHODS.get("*").get("*"));
        //if (com.sun.jdi.ObjectReference.class.isAssignableFrom(c)) {
        //    thrownExceptions.add(com.sun.jdi.ObjectCollectedException.class);
        //}
        Map<String, Set<Class>> excByMethods = EXCEPTIONS_BY_METHODS.get(className);
        if (excByMethods != null) {
            Set<Class> excs = excByMethods.get("*");
            if (excs != null) {
                thrownExceptions.addAll(excs);
            }
            excs = excByMethods.get(mNameWithParamTypes);
            if (excs == null) {
                excs = excByMethods.get(mName);
            }
            if (excs != null) {
                thrownExceptions.addAll(excs);
            }
        }

        className = substituteHigherClasses(className, higherVersionClasses);

        Set<Class> caughtExceptions = new LinkedHashSet<Class>(thrownExceptions);

        writeHigherVersionMethod(w, className, mName, mName, paramTypes, exceptionTypes, thrownExceptions, caughtExceptions, rType, null, jdkVersion);
        if (defaultReturn != null) {
            thrownExceptions.removeAll(SILENT_EXCEPTIONS);
            writeHigherVersionMethod(w, className, mName, mName+"0", paramTypes, exceptionTypes, thrownExceptions, caughtExceptions, rType, defaultReturn, jdkVersion);
        }

    }

    private static void writeHigherVersionMethod(Writer w, String className,
                                                 String mName, String mGenName,
                                                 List<String> paramTypes, List<String> exceptionTypes,
                                                 Set<Class> thrownExceptions, Set<Class> caughtExceptions,
                                                 String rType, String defaultReturn,
                                                 String jdkVersion) throws IOException {
        w.write("    // DO NOT MODIFY THIS CODE, GENERATED AUTOMATICALLY\n");
        w.write("    /** Wrapper for method "+mName+" from JDK "+jdkVersion+". */\n");
        w.write("    public static "+rType+" "+mGenName+"("+className+" a");
        String[] paramNames = new String[paramTypes.size()];
        for (int i = 0; i < paramTypes.size(); i++) {
            paramNames[i] = Character.toString((char) ('a'+(i+1)));
            String paramType = paramTypes.get(i);
            w.write(", "+paramType+" "+paramNames[i]);
        }
        w.write(")");

        if (exceptionTypes.size() > 0 || thrownExceptions.size() > 0) {
            w.write(" throws ");
            for (int i = 0; i < exceptionTypes.size(); i++) {
                if (i > 0) w.write(", ");
                w.write(exceptionTypes.get(i));
            }
            if (exceptionTypes.size() > 0 && thrownExceptions.size() > 0) {
                w.write(", ");
            }
            int i = 0;
            for (Iterator it = thrownExceptions.iterator(); it.hasNext(); i++) {
                if (i > 0) w.write(", ");
                w.write(EXCEPTION_WRAPPERS.get(it.next()));
            }
        }
        w.write(" {\n");
        String higherVersionClass;
        String higherVersionClassName;
        int higherVersionClassIndex = className.indexOf("/*");
        if (higherVersionClassIndex > 0) {
            higherVersionClassIndex += 2;
            higherVersionClassName = className.substring(higherVersionClassIndex, className.indexOf("*/"));
            higherVersionClass = higherVersionClassName.substring(higherVersionClassName.lastIndexOf('.') + 1);
            higherVersionClass = Character.toLowerCase(higherVersionClass.charAt(0)) + higherVersionClass.substring(1) + "Class";
            w.write("        Class "+higherVersionClass+";\n"+
                    "        try {\n"+
                    "            "+higherVersionClass+" = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class).loadClass(\""+higherVersionClassName+"\");\n"+
                    "        } catch (ClassNotFoundException ex) {\n"+
                    "            throw new IllegalStateException(ex);\n"+
                    "        }\n");

        } else {
            higherVersionClass = null;
            higherVersionClassName = null;
        }
        w.write("        if (org.netbeans.modules.debugger.jpda.JDIExceptionReporter.isLoggable()) {\n");
        w.write("            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.logCallStart(\n"+
                "                    \""+className+"\",\n"+
                "                    \""+mName+"\",\n"+
                "                    \"JDI CALL: "+className+"({0})."+mName+"(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) {
                w.write(", ");
            }
            w.write("{"+Integer.toString(i+1)+"}");
        }
        w.write(")\",\n"+
                "                    new Object[] {a");
        for (int i = 0; i < paramNames.length; i++) {
            w.write(", ");
            w.write(paramNames[i]);
        }
        w.write("});\n");
        w.write("        }\n"); // if

        boolean isVoidReturn = "void".equals(rType);
        if (!isVoidReturn) {
            w.write("        Object retValue = null;\n");
        }

        w.write("        try {\n");

        StringBuffer exec = new StringBuffer();
        if (!isVoidReturn) {
            if ("boolean".equals(rType)) rType = "Boolean";
            if ("int".equals(rType)) rType = "Integer";
            if ("long".equals(rType)) rType = "Long";
            exec.append("            ").append(rType).append(" ret;\n");
            exec.append("            ret = (").append(rType).append(") ");
        } else {
            exec.append("            ");
        }
        // Use reflection to invoke method from higher JDK version
        /* instead of:
        w.write("a."+mName+"(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) {
                w.write(", ");
            }
            w.write(paramNames[i]);
        }
        w.write(");\n");
         use reflection: */
        if (higherVersionClass != null) {
            exec.append(higherVersionClass+".getMethod(\""+mName+"\"");
        } else {
            exec.append(className+".class.getMethod(\""+mName+"\"");
        }
        for (int i = 0; i < paramNames.length; i++) {
            exec.append(", ");
            String type = paramTypes.get(i);
            if (type.indexOf('<') > 0) type = type.substring(0, type.indexOf('<'));
            exec.append(type);
            exec.append(".class");
        }
        exec.append(").invoke(a");
        for (int i = 0; i < paramNames.length; i++) {
            exec.append(", ");
            exec.append(paramNames[i]);
        }
        exec.append(");\n");
        if (!isVoidReturn) {
            exec.append("            retValue = ret;\n");
            exec.append("            return ret;\n");
        }
        w.write(methodImpl(className, mName, exec.toString()));


        w.write("        } catch (NoSuchMethodException ex) {\n");
        if (!isVoidReturn) {
            w.write("            retValue = ex;\n");
        }
        w.write("            throw new IllegalStateException(ex);\n");
        w.write("        } catch (SecurityException ex) {\n");
        if (!isVoidReturn) {
            w.write("            retValue = ex;\n");
        }
        w.write("            throw new IllegalStateException(ex);\n");
        w.write("        } catch (IllegalAccessException ex) {\n");
        if (!isVoidReturn) {
            w.write("            retValue = ex;\n");
        }
        w.write("            throw new IllegalStateException(ex);\n");
        w.write("        } catch (IllegalArgumentException ex) {\n");
        if (!isVoidReturn) {
            w.write("            retValue = ex;\n");
        }
        w.write("            throw new IllegalStateException(ex);\n");
        w.write("        } catch (java.lang.reflect.InvocationTargetException ex) {\n");
        w.write("            Throwable t = ex.getTargetException();\n");
        if (!isVoidReturn) {
            w.write("            retValue = t;\n");
        }

        // First re-throw the checked exceptions:
        for (int i = 0; i < exceptionTypes.size(); i++) {
            w.write("            if (t instanceof "+exceptionTypes.get(i)+") {\n");
            //w.write(" ex) {\n");
            w.write("                throw ("+exceptionTypes.get(i)+") t;\n");
            w.write("            }\n");
            //w.write("        } catch (");
        }
        for (Iterator<Class> it = caughtExceptions.iterator(); it.hasNext(); ) {
            Class cex = it.next();
            w.write("            if (t instanceof "+cex.getName()+") {\n");
            if (com.sun.jdi.InternalException.class.equals(cex)) {
                String c = higherVersionClassName;
                if (c == null) c = className;
                w.write("    "+reportOfJDIException(c, mName, "("+com.sun.jdi.InternalException.class.getName()+") t"));
            }
            if (defaultReturn != null && SILENT_EXCEPTIONS.contains(cex)) {
                w.write("                return "+defaultReturn+";\n");
            } else {
                w.write("                throw new "+EXCEPTION_WRAPPERS.get(cex)+"(("+cex.getName()+") t);\n");
            }
            w.write("            }\n");
            //w.write("        } catch (");
        }
        w.write("            throw new IllegalStateException(t);\n");
        w.write("        }");
        w.write(" finally {\n");
        w.write("            if (org.netbeans.modules.debugger.jpda.JDIExceptionReporter.isLoggable()) {\n");
        w.write("                org.netbeans.modules.debugger.jpda.JDIExceptionReporter.logCallEnd(\n"+
                "                        \""+className+"\",\n"+
                "                        \""+mName+"\",\n"+
                (isVoidReturn ?
                "                        org.netbeans.modules.debugger.jpda.JDIExceptionReporter.RET_VOID);\n" :
                "                        retValue);\n"));
        w.write("            }\n");
        w.write("        }\n");
        w.write("    }\n\n");
    }

    private static Map<String, List<String>> getHigherVersionMethods(File rootResource, String jdkVersion) throws IOException {
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        for (String resourceName : rootResource.list()) {
            if (resourceName.startsWith(METHODS_BY_JDK)) {
                String version = resourceName.substring(METHODS_BY_JDK.length());
                if (version.compareTo(jdkVersion) > 0) {
                    map.put(version, readMethodsLog(new File(rootResource, resourceName)));
                }
            }
        }
        return map;
    }

    private static List<String> readMethodsLog(File resource) throws IOException {
        List<String> list = new ArrayList<String>();
        BufferedReader log = new BufferedReader(new FileReader(resource));
        String line;
        while ((line = log.readLine()) != null) {
            list.add(line);
        }
        log.close();
        return list;
    }

    private static String substituteHigherClasses(String type, Set<String> higherVersionClasses) {
        int l = type.length();
        for (String higherVersionClass : higherVersionClasses) {
            int i0 = 0;
            int i1;
            StringBuilder sb = null;
            while ((i1 = type.indexOf(higherVersionClass, i0)) >= 0) {
                int i2 = i1 + higherVersionClass.length();
                if (i2 == l || type.charAt(i2) == ',' || type.charAt(i2) == '>') {
                    if (sb == null) {
                        sb = new StringBuilder(type.substring(i0, i1));
                    }
                    sb.append("Object/*");
                    sb.append(higherVersionClass);
                    sb.append("*/");
                }
                i0 = i2;
            }
            if (sb != null) {
                sb.append(type.substring(i0));
                type = sb.toString();
            }
        }
        return type;
    }

    private static String translateType(Type t) {
        if (t instanceof Class) {
            Class ct = ((Class) t).getComponentType();
            if (ct != null) {
                return translateType(ct)+"[]";
            }
            return ((Class) t).getName().replace('$', '.');
        }
        return t.toString().replace('$', '.');
    }

    private static String getDefaultReturn(Class returnType) {
        if (Integer.TYPE.equals(returnType)) {
            return "0";
        }
        if (Boolean.TYPE.equals(returnType)) {
            return "false";
        }
        if (java.util.List.class.equals(returnType)) {
            return "java.util.Collections.emptyList()";
        }
        if (java.util.Set.class.equals(returnType)) {
            return "java.util.Collections.emptySet()";
        }
        if (java.util.Map.class.equals(returnType)) {
            return "java.util.Collections.emptyMap()";
        }
        return null;
    }

    private static int findPair(String text, int index, char co, char cc) {
        int l = text.length();
        int ci = 1; // Expecting that opening character was already
        while (index < l) {
            char c = text.charAt(index);
            if (c == co) ci++;
            if (c == cc) ci--;
            if (ci != 0) index++;
            else break;
        }
        if (index < l) return index;
        else return -1;
    }
    
    private static String methodPreCode(String className, String methodName) {
        if (com.sun.jdi.ThreadReference.class.getName().equals(className)) {
            if (methodName.equals("currentContendedMonitor")) {
                String statusCheck        = "        int status = status0(a);\n"+
                                            "        if (status == com.sun.jdi.ThreadReference.THREAD_STATUS_NOT_STARTED ||\n" +
                                            "            status == com.sun.jdi.ThreadReference.THREAD_STATUS_ZOMBIE ||\n" +
                                            "            status == com.sun.jdi.ThreadReference.THREAD_STATUS_UNKNOWN) {\n" +
                                            "            \n" +
                                            "            // #253058, JDWP Error: 15\n" +
                                            "            return null;\n" +
                                            "        }\n";
                return statusCheck;
            }
        }
        return null;
    }

    // Custom code can be provided here to override the original invocation
    private static String methodImpl(String className, String methodName, String exec) {
        if (com.sun.jdi.ThreadReference.class.getName().equals(className)) {
            if (methodName.equals("popFrames")) {
                String catchJDWPException = "            try {\n"+
                                            "    "+exec+
                                            "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                            "                if (iex.errorCode() == 32) { // OPAQUE_FRAME\n"+
                                            "                    // "+com.sun.jdi.NativeMethodException.class.getSimpleName()+" should be thrown here!\n"+
                                            "                    throw new "+com.sun.jdi.NativeMethodException.class.getName()+"(iex.getMessage());\n"+
                                            "                } else {\n"+
                                            "                    throw iex; // re-throw the original\n"+
                                            "                }\n"+
                                            "            }\n";
                return catchJDWPException;
            }
            if (methodName.equals("currentContendedMonitor")) {
                String catchJDWPException = "            try {\n"+
                                            "    "+exec+
                                            "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                            "                if (iex.errorCode() == 13) { // THREAD_NOT_SUSPENDED\n"+
                                            "                    // "+com.sun.jdi.IncompatibleThreadStateException.class.getSimpleName()+" should be thrown here!\n"+
                                            "                    throw new "+com.sun.jdi.IncompatibleThreadStateException.class.getName()+"(iex.getMessage());\n"+
                                            "                } else {\n"+
                                            "                    throw iex; // re-throw the original\n"+
                                            "                }\n"+
                                            "            }\n";
                return catchJDWPException;
            }
            if (methodName.equals("frame")) {
                String catchNPE = "            try {\n"+
                                  "    "+exec+
                                  "            } catch ("+NullPointerException.class.getName()+" npex) {\n"+
                                  "                // See http://www.netbeans.org/issues/show_bug.cgi?id=159887\n"+
                                  "                throw new "+com.sun.jdi.IncompatibleThreadStateException.class.getName()+"(npex.getMessage());\n"+
                                  "            }\n";
                return catchNPE;
            }
            if (methodName.equals("resume")) {
                String catchJDWPException = "            try {\n"+
                                            "    "+exec+
                                            "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                            "                if (iex.errorCode() == 13) { // THREAD_NOT_SUSPENDED\n"+
                                            "                    // Ignore, as we're resuming the thread.\n"+
                                            "                } else {\n"+
                                            "                    throw iex; // re-throw the original\n"+
                                            "                }\n"+
                                            "            }\n";
                return catchJDWPException;
            }
        }
        if (com.sun.jdi.ReferenceType.class.getName().equals(className) && methodName.equals("constantPool")) {
            String catchNPE = "            try {\n"+
                              "    "+exec+
                              "            } catch (NullPointerException ex) {\n"+
                              "                // JDI defect http://bugs.sun.com/view_bug.do?bug_id=6822627\n"+
                              "                return null;\n"+
                              "            }\n";
            return catchNPE;
        }
        if (com.sun.jdi.StackFrame.class.getName().equals(className) && methodName.equals("thisObject")) {
            String catchJDWPException = "            try {\n"+
                                        "    "+exec+
                                        "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                        "                if (iex.errorCode() == 35) { // INVALID_SLOT, see http://www.netbeans.org/issues/show_bug.cgi?id=163652\n"+
                                        "                    return null;\n"+
                                        "                } else {\n"+
                                        "                    throw iex; // re-throw the original\n"+
                                        "                }\n"+
                                        "            }\n";
            return catchJDWPException;
        }
        if (com.sun.jdi.Location.class.getName().equals(className)) {
            if (methodName.equals("sourcePath") || methodName.equals("sourceName")) {
                String catchJDWPException = "            try {\n"+
                                            "    "+exec+
                                            "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                            "                if (iex.errorCode() == 101) { // ABSENT_INFORMATION\n"+
                                            "                    throw new com.sun.jdi.AbsentInformationException(iex.getMessage());\n"+
                                            "                } else {\n"+
                                            "                    throw iex; // re-throw the original\n"+
                                            "                }\n"+
                                            "            }\n";
                return catchJDWPException;
            }
            if (methodName.equals("lineNumber")) {
                String catchJDWPException = "            try {\n"+
                                            "    "+exec+
                                            "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                            "                if (iex.errorCode() == 101) { // ABSENT_INFORMATION\n"+
                                            "                    return -1;\n"+
                                            "                } else {\n"+
                                            "                    throw iex; // re-throw the original\n"+
                                            "                }\n"+
                                            "            }\n";
                return catchJDWPException;
            }
        }
        if (com.sun.jdi.ClassType.class.getName().equals(className) || com.sun.jdi.ObjectReference.class.getName().equals(className)) {
            if (methodName.equals("invokeMethod")) {
                String catchJDWPException = "            try {\n"+
                                            "    "+exec+
                                            "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                            "                if (iex.errorCode() == 502) { // ALREADY_INVOKING\n"+
                                            "                    iex = ("+com.sun.jdi.InternalException.class.getName()+") org.openide.util.Exceptions.attachLocalizedMessage(iex, org.openide.util.NbBundle.getMessage(org.netbeans.modules.debugger.jpda.JPDADebuggerImpl.class, \"JDWPError502\"));\n"+
                                            "                    org.openide.util.Exceptions.printStackTrace(iex);\n"+
                                            "                    return null;\n"+
                                            "                } else {\n"+
                                            "                    throw iex; // re-throw the original\n"+
                                            "                }\n"+
                                            "            }\n";
                return catchJDWPException;
            }
        }
        if (com.sun.jdi.Method.class.getName().equals(className)) {
            if (methodName.equals("bytecodes")) {
                String catchJDWPException = "            try {\n"+
                                            "    "+exec+
                                            "            } catch ("+com.sun.jdi.InternalException.class.getName()+" iex) {\n"+
                                            "                if (iex.errorCode() == 113) { // INTERNAL, see https://netbeans.org/bugzilla/show_bug.cgi?id=255298\n"+
                                            "                    throw new InternalExceptionWrapper(iex);\n"+
                                            "                } else {\n"+
                                            "                    throw iex; // re-throw the original\n"+
                                            "                }\n"+
                                            "            }\n";
                return catchJDWPException;
            }
        }
        return exec;
    }
    
    private static final String JDI_EXC_REPORT1 =
            "            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.report(";
    private static final String JDI_EXC_REPORT2 =
            ");\n";
    
    private static String reportOfJDIException(String className, String methodName, String ex) {
        if (com.sun.jdi.Method.class.getName().equals(className) && methodName.equals("bytecodes")) {
            return "            if (ex.errorCode() != 101) { // Ignore ABSENT_INFORMATION\n"+
                   "    "+JDI_EXC_REPORT1+ex+JDI_EXC_REPORT2+
                   "            }\n";
        } else {
            return JDI_EXC_REPORT1+ex+JDI_EXC_REPORT2;
        }
    }
    
    private static String disposeVMOnVMDisconnectedException(String className, String methodName) {
        if (com.sun.jdi.VirtualMachine.class.getName().equals(className) &&
            (methodName.equals("exit") || methodName.equals("dispose"))) {
            return ""; // Ignore
        }
        // We should not have to do this. See #243837
        return "            if (a instanceof com.sun.jdi.Mirror) {\n" +
               "                com.sun.jdi.VirtualMachine vm = ((com.sun.jdi.Mirror) a).virtualMachine();\n" +
               "                try {\n"+
               "                    vm.dispose();\n" +
               "                } catch (com.sun.jdi.VMDisconnectedException vmdex) {}\n"+
               "            }\n";
    }

    public static void main(String[] args) {
        /*if (args.length == 0) {
            System.err.println("Usage: Generate <output dir>");
        }*/
        File dir;
        if (args.length > 0) {
            dir = new File(args[0]);
        } else {
            dir = new File(System.getProperty("user.dir"));
        }
        dir = new File(dir, "src/"+PACKAGE.replace('.', '/'));
        dir.mkdirs();
        System.err.println("Generating into "+dir+" for JDK "+System.getProperty("java.version"));
        try {
            generate(dir);
        } catch (IOException ex) {
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
