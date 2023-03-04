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

package org.openide.execution;

import java.io.File;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.util.Lookup;

/** Property that can hold informations about class path and
* that can be used to create string representation of the
* class path.
*/
public final class NbClassPath extends Object implements java.io.Serializable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -8458093409814321744L;

    /** Fuj: This is the most overloaded variable in this class.
    * It can hold Object[] with elements of String or Exception
    * or later Exception[] array.
    *
    * Also the array can hold File[] array.
    */
    private Object[] items;
    /** the prepared classpath */
    private String classpath;

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param classpathItems  the classpath to be passed to the process 
    */
    public NbClassPath (String[] classpathItems) {
        this.items = classpathItems;
    }

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param classpathItems  the classpath to be passed to the process 
    */
    public NbClassPath (File[] classpathItems) {
        this.items = classpathItems;
    }

    /** Private constructor
    * @param arr array of String and Exceptions
    */
    NbClassPath (Object[] arr) {
        this.items = arr;
    }

    /** Create a class path from the usual string representation.
    * @param path a class path separated by {@link File#pathSeparatorChar}s
    */
    public NbClassPath (String path) {
        this.items = new Exception[0];
        this.classpath = path;
        // [PENDING] what is this here for? *Users* of the classpath should quote it as needed to
        // pass thru shells, according to the type of shell. Right?
        if (path.indexOf(' ') >= 0) {
            if (path.startsWith("\"")) { // NOI18N
                return;
            } else {
                StringBuffer buff = new StringBuffer(path);
                buff.insert(0, '"');
                buff.append('"');
                classpath = buff.toString();
            }
        }
    }

    /** Creates class path describing additional libraries needed by the system.
     * Never use this class path as part of a user project!
     * For more information consult the <a href="../doc-files/classpath.html">Module Class Path</a> document.
     * @deprecated There are generally no excuses to be using this method as part of a normal module;
     * its exact meaning is vague, and probably not what you want.
    */
    @Deprecated
    public static NbClassPath createLibraryPath () {
        Thread.dumpStack();
        // modules & libs
        ExecutionEngine ee = (ExecutionEngine)Lookup.getDefault().lookup(ExecutionEngine.class);
        if (ee != null) {
            return ee.createLibraryPath();
        } else {
            return new NbClassPath(new File[0]);
        }
    }

    /** Creates class path of the system.
     * Never use this class path as part of a user project!
     * For more information consult the <a href="../doc-files/classpath.html">Module Class Path</a> document.
     * @deprecated There are generally no excuses to be using this method as part of a normal module;
     * its exact meaning is vague, and probably not what you want.
    */
    @Deprecated
    public static NbClassPath createClassPath () {
        Thread.dumpStack();
        // ${java.class.path} minus openide-compat.jar
        String cp = System.getProperty ("java.class.path"); // NOI18N
        if (cp == null || cp.length () == 0) return new NbClassPath (""); // NOI18N
        StringBuffer buf = new StringBuffer (cp.length ());
        StringTokenizer tok = new StringTokenizer (cp, File.pathSeparator);
        boolean appended = false;
        while (tok.hasMoreTokens ()) {
            String piece = tok.nextToken ();
            if (piece.endsWith ("openide-compat.jar")) continue; // NOI18N
            if (appended) {
                buf.append (File.pathSeparatorChar);
            } else {
                appended = true;
            }
            buf.append (piece);
        }
        return new NbClassPath (buf.toString ());
    }

    /** Creates path describing boot class path of the system.
     * Never use this class path as part of a user project!
     * There are generally no excuses to be using this method as part of a normal module.
     * For more information consult the <a href="../doc-files/classpath.html">Module Class Path</a> document.
    * @return class path of system class including extensions
     * @deprecated Use the Java Platform API instead.
    */
    @Deprecated
    public static NbClassPath createBootClassPath () {
        Thread.dumpStack();
        // boot
        String boot = System.getProperty("sun.boot.class.path"); // NOI18N
        StringBuffer sb = (boot != null ? new StringBuffer(boot) : new StringBuffer());

        // std extensions
        String extensions = System.getProperty("java.ext.dirs"); // NOI18N
        if (extensions != null) {
            for (StringTokenizer st = new StringTokenizer(extensions, File.pathSeparator); st.hasMoreTokens();) {
                File dir = new File(st.nextToken());
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        String name = entries[i].getName().toLowerCase(Locale.US);
                        if (name.endsWith(".zip") || name.endsWith(".jar")) { // NOI18N
                            if (sb.length() > 0) {
                                sb.append(File.pathSeparatorChar);
                            }
                            sb.append(entries[i].getPath());
                        }
                    }
                }
            }
        }

        return new NbClassPath (sb.toString());
    }

    /** Take one file object and try to convert it into a local file.
    * @param fo file object to convert
    * @return disk file for that file object, or <code>null</code> if there is no corresponding disk file
    * @deprecated You should use {@link org.openide.filesystems.FileUtil#toFile} instead.
    */
    @Deprecated
    public static File toFile (FileObject fo) {
        Thread.dumpStack();
        return FileUtil.toFile(fo);
    }

    /** If there were some problems during creation of the class path, they can be identified
    * by asking the method. So this method can be called to test whether it is correct to
    * use the path or there can be some errors.
    * <P>
    * This can happen especially when creating NbClassPath for filesystems in repository and
    * they are not stored on locally accessible disks.
    *
    * @return array of exceptions thrown during creation of the path
    */ 
    public Exception[] getExceptions () {
        try {
            return (Exception[])items;
        } catch (ClassCastException ex) {
            // we have to convert the array first
        }

        synchronized (this) {
            // creates class path
            getClassPath ();

            int first = 0;
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    // should be exception
                    items[first++] = items[i];
                }
            }

            Exception[] list = new Exception[first];
            System.arraycopy (items, 0, list, 0, first);
            items = list;
            return list;
        }
    }



    /** Create class path representation. The implementation <i>will return the string quoted 
     *  (using doublequotes)</i>, if it contains a space character.
    * @return string representing the classpath items separated by File.separatorChar, possibly quoted.
    */
    public String getClassPath () {
        if (classpath != null) return classpath;
        synchronized (this) {
            if (classpath != null) return classpath;

            if (items.length == 0) {
                return classpath = ""; // NOI18N
            } else {
                StringBuffer sb = new StringBuffer ();
                boolean haveone = false;
                for (int i = 0; i < items.length; i++) {
                    Object o = items[i];
                    if (o == null || (! (o instanceof String) && ! (o instanceof File))) {
                        // we accept only strings/files
                        continue;
                    }

                    if (haveone) {
                        sb.append (File.pathSeparatorChar);
                    } else {
                        haveone = true;
                    }
                    sb.append (o.toString ());
                    items[i] = null;
                }
                String clsPth;
                if ((clsPth = sb.toString()).indexOf(' ') >= 0) {
                    sb.insert(0, '"');
                    sb.append('"');
                    classpath = sb.toString();
                } else {
                    classpath = clsPth;
                }
                return classpath;
            }
        }
    }

    /* equals */
    public boolean equals(Object o) {
        if (! (o instanceof NbClassPath)) return false;
        NbClassPath him = (NbClassPath) o;
        return getClassPath ().equals (him.getClassPath ());
    }
}
