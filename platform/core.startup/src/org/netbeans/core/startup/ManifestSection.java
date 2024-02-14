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

package org.netbeans.core.startup;

import java.beans.Beans;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.Util;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.SharedClassObject;
import org.openide.util.Lookup;

// XXX synchronization?

/** Class representing one specially-treated section in a module's manifest file.
 * For example, one section may describe a single action provided by the module.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public abstract class ManifestSection<T> {
    /** superclass of section either Class or String name of the class*/
    private final Object superclazz;
    /** name of the class file, e.g. foo/Bar.class, or foo/bar.ser */
    private final String name;
    /** name of the class, e.g. foo.bar */
    private final String className;
    /** the class involved */
    private Class<?> clazz;
    /** instance of the class if possible */
    private Object result;
    /** any exception associated with loading the object */
    private Exception problem;
    /** associated module */
    private final Module module;
    
    /** Create a manifest section generally.
     * @param name name of section, should be a class file, e.g. foo/Bar.class
     * @param module associated module
     * @param superclazz super-class of instances of this section
     * @throws InvalidException if the name is not valid for an OpenIDE section
     *         (exception must include module for reference)
     */
    protected ManifestSection(String name, Module module, Object superclazz) throws InvalidException {
        this.name = name;
        this.module = module;
        this.superclazz = superclazz;
        try {
            className = Util.createPackageName(name);
        } catch (IllegalArgumentException iae) {
            InvalidException ie = new InvalidException(module, iae.toString());
            ie.initCause(iae);
            throw ie;
        }
    }
    
    /** Get the associated module. */
    public final Module getModule() {
        return module;
    }
    
    /** Get the classloader used to load this section. */
    protected final ClassLoader getClassLoader() {
        return module != null ? module.getClassLoader() : getClass().getClassLoader();
    }
    
    /** Does this section represent a default instance?
     * Normally true, but false when deserializing beans.
     */
    public final boolean isDefaultInstance() {
        return name.endsWith(".class"); // NOI18N
    }
    
    /** Get the class which the generated instances will have.
     * @return the class
     * @throws Exception for various reasons
     */
    public final Class<?> getSectionClass() throws Exception {
        if (clazz != null) {
            return clazz;
        }
        if (problem != null) {
            throw problem;
        }
        if (isDefaultInstance()) {
            try {
                clazz = getClassLoader().loadClass(className);
                if (! getSuperclass().isAssignableFrom(clazz)) {
                    throw new ClassCastException("Class " + clazz.getName() + " is not a subclass of " + getSuperclass().getName()); // NOI18N
                }
                // Don't try to check .ser files: it is quite legitimate to
                // serialize in a module objects whose class is from elsewhere
                // (e.g. the core).
                if (clazz.getClassLoader() != getClassLoader()) { // NOI18N
                    Events ev = module.getManager().getEvents();
                    ev.log(Events.WRONG_CLASS_LOADER, module, clazz, getClassLoader());
                }
                return clazz;
            } catch (ClassNotFoundException cnfe) {
                Exceptions.attachMessage(cnfe,
                                         "Loader for ClassNotFoundException: " +
                                         getClassLoader());
                problem = cnfe;
                throw problem;
            } catch (Exception e) {
                problem = e;
                throw problem;
            } catch (LinkageError t) {
                problem = new ClassNotFoundException(t.toString(), t);
                throw problem;
            }
        } else {
            return (clazz = getInstance().getClass());
        }
    }
    
    /** Same as {@link #getSectionClass}, but only provides the name of the class.
     * Could be more efficient because it will not try to load the class unless
     * a serialized bean is in use.
     */
    public String getSectionClassName() throws Exception {
        if (isDefaultInstance()) {
            return className;
        } else {
            return getSectionClass().getName();
        }
    }
    
    /** Create a fresh instance.
     * @return the instance
     * @exception Exception if there is an error
     */
    protected final Object createInstance() throws Exception {
        if (! isDefaultInstance()) {
            try {
                Object o = Beans.instantiate(getClassLoader(), className);
                clazz = o.getClass();
                if (! getSectionClass().isAssignableFrom(clazz)) {
                    throw new ClassCastException("Class " + clazz.getName() + " is not a subclass of " + getSuperclass().getName()); // NOI18N
                }
                return o;
            } catch (ClassNotFoundException cnfe) {
                Exceptions.attachMessage(cnfe,
                                         "Loader for ClassNotFoundException: " +
                                         getClassLoader());
                throw cnfe;
            } catch (LinkageError le) {
                throw new ClassNotFoundException(le.toString(), le);
            }
        } else {
            getSectionClass(); // might throw some exceptions
            if (SharedClassObject.class.isAssignableFrom(clazz)) {
                return SharedClassObject.findObject(clazz.asSubclass(SharedClassObject.class), true);
            } else {
                return clazz.getDeclaredConstructor().newInstance();
            }
        }
    }
    
    /** Get a single cached instance.
     * @return the instance
     * @exception Exception if there is an error
     */
    public final Object getInstance() throws Exception {
        if (problem != null) {
            problem.fillInStackTrace(); // XXX is this a good idea?
            throw problem;
        }
        if (result == null) {
            try {
                result = createInstance();
            } catch (Exception ex) {
                // remember the exception
                problem = ex;
                throw problem;
            } catch (LinkageError t) {
                problem = new ClassNotFoundException(t.toString(), t);
                throw problem;
            }
        }
        return result;
    }
    
    /** Get the superclass which all instances of this section are expected to
     * be assignable to.
     */
    public final Class<?> getSuperclass() {
        if (superclazz instanceof Class) {
            return (Class)superclazz;
        } else {
            try {
                return getClazz ((String)superclazz, module);
            } catch (InvalidException ex) {
                throw (IllegalStateException)new IllegalStateException (superclazz.toString()).initCause (ex);
            }
        }
    }
    
    /** Dispose of a section. Used when a module will be uninstalled and all its
     * resources should be released.
     */
    public void dispose() {
        result = null;
        problem = null;
        clazz = null;
    }
    
    /** String representation for debugging. */
    public String toString() {
        return "ManifestSection[" + className + "]"; // NOI18N
    }
    
    /** Parse a manifest section and make an object representation of it.
     * @param name name of the section (i.e. file to load)
     * @param attr attributes of the manifest section
     * @param module the associated module
     * @return the section or null if this manifest section is not related to module installation
     * @exception InvalidException if the attributes are not valid
     */
    public static ManifestSection create(String name, Attributes attr, Module module) throws InvalidException {
        String sectionName = attr.getValue("OpenIDE-Module-Class"); // NOI18N
        if (sectionName == null) {
            // no section tag
            return null;
        } else if (sectionName.equalsIgnoreCase("Action")) { // NOI18N
            warnObsolete(sectionName, module);
            return new ActionSection(name, module);
        } else if (sectionName.equalsIgnoreCase("Option")) { // NOI18N
            warnObsolete(sectionName, module);
            return null;
        } else if (sectionName.equalsIgnoreCase("Loader")) { // NOI18N
            warnObsolete(sectionName, module);
            return new LoaderSection(name, attr, module);
        } else if (sectionName.equalsIgnoreCase("Filesystem")) { // NOI18N
            warnObsolete(sectionName, module);
            return null;
        } else if (sectionName.equalsIgnoreCase("Node")) { // NOI18N
            warnObsolete(sectionName, module);
            Util.err.warning("(See http://www.netbeans.org/issues/show_bug.cgi?id=19609, last comment, for howto.)");
            return null;
        } else if (sectionName.equalsIgnoreCase("Service")) { // NOI18N
            warnObsolete(sectionName, module);
            return null;
        } else if (sectionName.equalsIgnoreCase("Debugger")) { // NOI18N
            // XXX should support for this be dropped entirely?
            warnObsolete(sectionName, module);
            return new DebuggerSection(name, module);
        } else if (sectionName.equalsIgnoreCase("ClipboardConvertor")) { // NOI18N
            // XXX should support for this be dropped entirely?
            warnObsolete(sectionName, module);
            return new ClipboardConvertorSection(name, module);
        } else {
            throw new InvalidException(module, "Illegal manifest section type: " + sectionName); // NOI18N
        }
    }
    
    private static void warnObsolete(String sectionName, Module module) {
        if (module == null) {
            return; // NbLoaderPoolResolverChangeTest
        }
        Util.err.warning("Use of OpenIDE-Module-Class: " + sectionName + " in " + module.getCodeNameBase() + " is obsolete.");
        Util.err.warning("(Please use layer-based installation of objects instead.)");
    }
    
    /** Module section for an Action.
     * @see SystemAction
     */
    public static final class ActionSection extends ManifestSection {
        ActionSection(String name, Module module) throws InvalidException {
            super(name, module, SystemAction.class);
        }
    }
    
    /** Module section for a Data Loader.
     */
    public static final class LoaderSection extends ManifestSection {
        /** class name(s) of data object to
         * be inserted after loader that recognizes its
         */
        private final String[] installAfter;
        /** class name(s) of data object to be inserted before its recognizing
         * data loader
         */
        private final String[] installBefore;
        
        LoaderSection(String name, Attributes attrs, Module module) throws InvalidException {
            super (name, module, "org.openide.loaders.DataLoader"); // NOI18N
            String val = attrs.getValue("Install-After"); // NOI18N
            StringTokenizer tok;
            List<String> res;
            // XXX validate classnames etc.
            if (val != null) {
                tok = new StringTokenizer(val, ", "); // NOI18N
                res = new LinkedList<String>();
                while (tok.hasMoreTokens()) {
                    String clazz = tok.nextToken();
                    if (! clazz.equals("")) // NOI18N
                        res.add(clazz);
                }
                installAfter = res.toArray(new String[0]);
            } else {
                installAfter = null;
            }
            val = attrs.getValue("Install-Before"); // NOI18N
            if (val != null) {
                tok = new StringTokenizer(val, ", "); // NOI18N
                res = new LinkedList<String>();
                while (tok.hasMoreTokens()) {
                    String clazz = tok.nextToken();
                    if (! clazz.equals("")) // NOI18N
                        res.add(clazz);
                }
                installBefore = res.toArray(new String[0]);
            } else {
                installBefore = null;
            }
        }
        
        /** Get the representation class(es) of the loader(s) that this one should be installed after.
         * @return a list of class names, or <code>null</code>
         */
        public String[] getInstallAfter() {
            return installAfter;
        }
        
        /** Get the representation class(es) of the loader(s) that this one should be installed before.
         * @return a list of class names, or <code>null</code>
         */
        public String[] getInstallBefore() {
            return installBefore;
        }
    }
    
    /** @deprecated use new debugger API
     */
    @Deprecated
    public static final class DebuggerSection extends ManifestSection {
        DebuggerSection(String name, Module module) throws InvalidException {
            super(name, module, getClazz("org.openide.debugger.Debugger", module)); // NOI18N
        }
    }
    
    /** @deprecated use META-INF/services to register convertors.
     */
    @Deprecated
    public static final class ClipboardConvertorSection extends ManifestSection {
        ClipboardConvertorSection(String name, Module module) throws InvalidException {
            super(name, module, ExClipboard.Convertor.class);
        }
    }

    /** Loads class of given name.
     */
    static Class<?> getClazz(String name, Module m) throws InvalidException {
        try {
            return Lookup.getDefault().lookup(ClassLoader.class).loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            InvalidException e = new InvalidException(m, "Unable to locate class: " + name + " maybe you do not have its module enabled!?"); // NOI18N
            e.initCause(cnfe);
            throw e;
        }
    }
    
}
