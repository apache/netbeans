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

package org.openide.filesystems;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

/** This class defines the capabilities of a filesystem to
* take part in different operations. Some filesystems are
* not designed to allow compilation on them, some do not want
* to be present in class path when executing or debugging
* a program.
* <P>
* Moreover there can be additional capabilities to check
* and this class defines ways how one can communicated with
* a filesystem to find out whether the system is "capable"
* enough to be used in the operation.
*
* @author Jaroslav Tulach
 * @deprecated Now useless.
*/
@Deprecated
public class FileSystemCapability extends Object {
    /** Object that is capable of every thing.
    */
    public static final FileSystemCapability ALL = new FileSystemCapability() {
            @Override
            public boolean capableOf(FileSystemCapability c) {
                return true;
            }
        };

    /** Well known capability of being compiled.
     * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
     */
    @Deprecated
    public static final FileSystemCapability COMPILE = new FileSystemCapability();

    /** Well known ability to be executed.
     * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
     */
    @Deprecated
    public static final FileSystemCapability EXECUTE = new FileSystemCapability();

    /** Well known ability to be debugged.
     * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
     */
    @Deprecated
    public static final FileSystemCapability DEBUG = new FileSystemCapability();

    /** Well known ability to contain documentation files
     * @deprecated Please use <a href="@org-netbeans-api-java@/org/netbeans/api/queries/JavadocForBinaryQuery.html"><code>JavadocForBinaryQuery</code></a> instead.
     */
    @Deprecated
    public static final FileSystemCapability DOC = new FileSystemCapability();

    public FileSystemCapability() {
        if (DOC == null) {
            // do not report static initializers
            return;
        }

        assert false : "Deprecated.";
    }

    /** Basic operation that tests whether this object
    * is capable to do different capability.
    * <P>
    * The default implementation claims that it is
    * capable to handle only identical capability (==).
    *
    * @param c capability to test
    * @return true if yes
    */
    public boolean capableOf(FileSystemCapability c) {
        return c == this;
    }

    /** All filesystems that are capable of this capability.
    * @return enumeration of FileSystems that satifies this capability
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public Enumeration<? extends FileSystem> fileSystems() {
        class FFS implements org.openide.util.Enumerations.Processor<FileSystem, FileSystem> {
            @Deprecated
            public FileSystem process(FileSystem fs, Collection<FileSystem> ignore) {
                return FileSystemCompat.compat(fs).getCapability().
                        capableOf(FileSystemCapability.this) ? fs : null;
            }
        }

        return org.openide.util.Enumerations.filter(Repository.getDefault().fileSystems(), new FFS());
    }

    /** Find a resource in repository, ignoring not capable filesystems.
    * @param resName name of the resource
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public FileObject findResource(String resName) {
        Enumeration<? extends FileSystem> en = fileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.findResource(resName);

            if (fo != null) {
                // object found
                return fo;
            }
        }

        return null;
    }

    /** Searches for the given resource among all filesystems
    * that satifies this capability, returning all matches.
    * @param name name of the resource
    * @return enumeration of {@link FileObject}s
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public Enumeration<? extends FileObject> findAllResources(String name) {
        Vector<FileObject> v = new Vector<FileObject>(8);
        Enumeration<? extends FileSystem> en = fileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.findResource(name);

            if (fo != null) {
                v.addElement(fo);
            }
        }

        return v.elements();
    }

    /** Finds file when its name is provided. It scans in the list of
    * filesystems and asks them for the specified file by a call to
    * {@link FileSystem#find find}. The first object that is found is returned or <CODE>null</CODE>
    * if none of the filesystems contain such a file.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return {@link FileObject} that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final FileObject find(String aPackage, String name, String ext) {
        Enumeration<? extends FileSystem> en = fileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.find(aPackage, name, ext);

            if (fo != null) {
                // object found
                return fo;
            }
        }

        return null;
    }

    /** Finds all files among all filesystems with this capability
    * that match a given name, returning all matches.
    * All filesystems are queried with {@link FileSystem#find}.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return enumeration of {@link FileObject}s
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileObject> findAll(String aPackage, String name, String ext) {
        Enumeration<? extends FileSystem> en = fileSystems();
        Vector<FileObject> ret = new Vector<FileObject>();

        while (en.hasMoreElements()) {
            FileSystem fs = (FileSystem) en.nextElement();
            FileObject fo = fs.find(aPackage, name, ext);

            if (fo != null) {
                ret.addElement(fo);
            }
        }

        return ret.elements();
    }

    /** Adds PropertyChange listener. Every class which implements changes of capabilities
    * has to implement it's property change support.
    * @param l the listener to be added.
    */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
    }

    /** Removes PropertyChange listener. Every class which implements changes of capabilities
    * has to implement it's property change support.
    * @param l the listener to be removed.
    */
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /** Default implementation of capabilities, that behaves like
    * JavaBean and allows to set whether the well known
    * capabilities (like compile, execute) should be enabled
    * or not.
     * @deprecated For the same reason the whole class is.
    */
    @Deprecated
    public static class Bean extends FileSystemCapability implements java.io.Serializable {
        static final long serialVersionUID = 627905674809532736L;

        /** change listeners */
        private transient PropertyChangeSupport supp;

        /** compilation */
        private boolean compilation = true;

        /** execution */
        private boolean execution = true;

        /** debugging */
        private boolean debug = true;

        /** doc */
        private boolean doc = false;

        /** Checks for well known capabilities and if they are allowed.
        *
        * @param c capability to test
        * @return true if yes
        */
        @Override
        public boolean capableOf(FileSystemCapability c) {
            if (c == COMPILE) {
                return compilation;
            }

            if (c == EXECUTE) {
                return execution;
            }

            if (c == DEBUG) {
                return debug;
            }

            if (c == DOC) {
                return doc;
            }

            if (c == ALL) {
                return true;
            }

            if (!(c instanceof Bean)) {
                return false;
            }

            // try match of values
            Bean b = (Bean) c;

            return (compilation == b.compilation) && (execution == b.execution) && (debug == b.debug) &&
            (doc == b.doc);
        }

        /** Getter for value of compiling capability.
         * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
        */
        @Deprecated
        public boolean getCompile() {
            return compilation;
        }

        /** Setter for allowing compiling capability.
         * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
        */
        @Deprecated
        public void setCompile(boolean val) {
            if (val != compilation) {
                compilation = val;

                if (supp != null) {
                    supp.firePropertyChange(
                        "compile", // NOI18N
                        (!val) ? Boolean.TRUE : Boolean.FALSE, val ? Boolean.TRUE : Boolean.FALSE
                    );
                }
            }
        }

        /** Getter for value of executiong capability.
         * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
        */
        @Deprecated
        public boolean getExecute() {
            return execution;
        }

        /** Setter for allowing executing capability.
         * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
        */
        @Deprecated
        public void setExecute(boolean val) {
            if (val != execution) {
                execution = val;

                if (supp != null) {
                    supp.firePropertyChange(
                        "execute", // NOI18N
                        (!val) ? Boolean.TRUE : Boolean.FALSE, val ? Boolean.TRUE : Boolean.FALSE
                    );
                }
            }
        }

        /** Getter for value of debugging capability.
         * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
        */
        @Deprecated
        public boolean getDebug() {
            return debug;
        }

        /** Setter for allowing debugging capability.
         * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
        */
        @Deprecated
        public void setDebug(boolean val) {
            if (val != debug) {
                debug = val;

                if (supp != null) {
                    supp.firePropertyChange(
                        "debug", // NOI18N
                        (!val) ? Boolean.TRUE : Boolean.FALSE, val ? Boolean.TRUE : Boolean.FALSE
                    );
                }
            }
        }

        /** Getter for value of doc capability.
         * @deprecated Please use <a href="@org-netbeans-api-java@/org/netbeans/api/queries/JavadocForBinaryQuery.html"><code>JavadocForBinaryQuery</code></a> instead.
        */
        @Deprecated
        public boolean getDoc() {
            return doc;
        }

        /** Setter for allowing debugging capability.
         * @deprecated Please use <a href="@org-netbeans-api-java@/org/netbeans/api/queries/JavadocForBinaryQuery.html"><code>JavadocForBinaryQuery</code></a> instead.
        */
        @Deprecated
        public void setDoc(boolean val) {
            if (val != doc) {
                doc = val;

                if (supp != null) {
                    supp.firePropertyChange(
                        "doc", // NOI18N
                        (!val) ? Boolean.TRUE : Boolean.FALSE, val ? Boolean.TRUE : Boolean.FALSE
                    );
                }
            }
        }

        @Override
        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            if (supp == null) {
                supp = new PropertyChangeSupport(this);
            }

            supp.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            if (supp != null) {
                supp.removePropertyChangeListener(l);
            }
        }
    }
}
