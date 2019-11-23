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

package org.openide.loaders;


import java.io.*;
import java.lang.reflect.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.*;

/** An instance cookie implementation that works with files or entries.
*
* @author   Jan Jancura, Jaroslav Tulach
*/
public class InstanceSupport extends Object implements InstanceCookie.Of {
    /** entry to work with */
    private MultiDataObject.Entry entry;

    /** throw exception during loading of the class */
    private Throwable clazzException;

    /** the class of the instance */
    private Class clazz;

    /** the class is applet */
    private Boolean applet;
    /** the class is bean */
    private Boolean bean;

    /** New support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    */
    public InstanceSupport(MultiDataObject.Entry entry) {
        this.entry = entry;
    }
    
    /** Accessor for the entry. Needed in InstanceDataObject.Ser
     * @return the entry
     */
    MultiDataObject.Entry entry () {
        return entry;
    }
    

    // main methods .........................................................................................................

    /* The bean name for the instance.
    * @return the name for the instance
    */
    public String instanceName () {
        // XXX does this make any sense? Is this method useful for anything?
        String p = instanceOrigin().getPath();
        int x = p.lastIndexOf('.');
        if (x != -1 && x > p.lastIndexOf('/')) {
            p = p.substring(0, x);
        }
        return p.replace('/', '.');
    }

    /* The class of the instance represented by this cookie.
    * Can be used to test whether the instance is of valid
    * class before it is created.
    *
    * <p>Note that <code>SecurityException</code> could be thrown
    * if an attempt was made e.g. to create an instance of a class
    * in a <code>java.*</code> package. Clients of <code>InstanceSupport</code>
    * which expect that this might happen (e.g. creating instances of
    * freeform user classes) should explicitly catch security exceptions
    * and convert them into whatever else as needed.
    *
    * @return the class of the instance
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Class<?> instanceClass()
    throws java.io.IOException, ClassNotFoundException {
        return instanceClass(null);
    }
    
    final Class<?> instanceClass(ClassLoader cl)
    throws java.io.IOException, ClassNotFoundException {
        if (clazzException != null) {
            if (clazzException instanceof IOException)
                throw (IOException)clazzException;
            else if (clazzException instanceof ClassNotFoundException)
                throw (ClassNotFoundException)clazzException;
            else
                throw (ThreadDeath)clazzException;
        }
        if (clazz != null) return clazz;
        //System.out.println ("getClass " + fileName ); // NOI18N
        try {
            if (isSerialized ()) { // NOI18N
                // read class from ser file
                InputStream is = instanceOrigin ().getInputStream ();
                try {
                    clazz = readClass (is);
                    return clazz;
                } finally {
                    is.close ();
                }
            } else {
                // find class by class loader
                clazz = findClass (instanceName (), cl);
                if (clazz == null) throw new ClassNotFoundException (instanceName());
                return clazz;
            }
        } catch (IOException ex) {
            Exceptions.attachMessage(ex, "From file: " + entry.getFile()); // NOI18N
            clazzException = ex;
            throw ex;
        } catch (ClassNotFoundException ex) {
            Exceptions.attachMessage(ex, "From file: " + entry.getFile()); // NOI18N
            clazzException = ex;
            throw ex;
        } catch (RuntimeException re) {
            // turn other throwables into class not found ex.
            clazzException = new ClassNotFoundException("From file: " + entry.getFile() + " due to: " + re.toString(), re);  // NOI18N
            throw (ClassNotFoundException) clazzException;
        } catch (LinkageError le) {
            clazzException = new ClassNotFoundException("From file: " + entry.getFile() + " due to: " + le.toString(), le);  // NOI18N
            throw (ClassNotFoundException) clazzException;
        }
    }

    /*Query to found out if the object created by this cookie is 
    * instance of given type. Does:
    * <pre>
    *    Class actualClass = instanceClass ();
    *    result = type.isAsignableFrom (actualClass);
    * </pre>
    *
    * @param type the class type we want to check
    * @return true if this cookie can produce object of given type
    */
    public boolean instanceOf(Class<?> type) {
        try {
            return type.isAssignableFrom (instanceClass ());
        } catch (IOException ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /** Returns the origin of the instance.
    * @return the origin
    */
    public FileObject instanceOrigin () {
        //    return getEntry ().getFile ();
        return entry.getFile ();
    }

    /*
    * @return an object to work with
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Object instanceCreate ()
    throws java.io.IOException, ClassNotFoundException {
        try {
            if (isSerialized ()) {
                // create from ser file
                BufferedInputStream bis = new BufferedInputStream(instanceOrigin().getInputStream(), 1024);
                org.openide.util.io.NbObjectInputStream nbis = new org.openide.util.io.NbObjectInputStream(bis);
                Object o = nbis.readObject();
                nbis.close();
                return o;
            } else {
                Class<?> c = instanceClass ();
                if (SharedClassObject.class.isAssignableFrom (c)) {
                    // special support
                    return SharedClassObject.findObject (c.asSubclass(SharedClassObject.class), true);
                } else {
                    // create new instance
                    Constructor<?> init = c.getDeclaredConstructor();
                    init.setAccessible(true);
                    return init.newInstance((Object[]) null);
                }
            }
        } catch (IOException ex) {
            // [PENDING] annotate with localized message
            Exceptions.attachLocalizedMessage(ex, instanceName());
            throw ex;
        } catch (ClassNotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            // turn other throwables into class not found ex.
            throw new ClassNotFoundException("Cannot instantiate " + instanceName() + " for " + entry.getFile(), e); // NOI18N
        } catch (LinkageError e) {
            throw new ClassNotFoundException("Cannot instantiate " + instanceName() + " for " + entry.getFile(), e); // NOI18N
        }
    }

    /** Is this an applet?
    * @return <code>true</code> if this class is a <code>java.applet.Applet</code>
    * @deprecated This method probably should not be used, as it catches a variety of potentially
    *             serious exceptions and errors, and swallows them so as to produce a simple boolean
    *             result. (Notifying them all would be inappropriate as they typically come from user
    *             code.) Better to directly parse the bytecode, using e.g. the classfile module,
    *             which is immune to this class of errors.
    */
    @Deprecated
    public boolean isApplet () {
        if (applet != null) return applet.booleanValue ();
        boolean b = instanceOf (java.applet.Applet.class);
        applet = b ? Boolean.TRUE : Boolean.FALSE;
        return b;
    }

    /** Is this a standalone executable?
    * @return <code>true</code> if this class has main method
    * (e.g., <code>public static void main (String[] arguments)</code>).
    * @deprecated This method probably should not be used, as it catches a variety of potentially
    *             serious exceptions and errors, and swallows them so as to produce a simple boolean
    *             result. (Notifying them all would be inappropriate as they typically come from user
    *             code.) Better to directly parse the bytecode, using e.g. the classfile module,
    *             which is immune to this class of errors.
    */
    @Deprecated
    public boolean isExecutable () {
        try {
            Method main = instanceClass ().getDeclaredMethod ("main", new Class[] { // NOI18N
                              String[].class
                          });

            int m = main.getModifiers ();
            return Modifier.isPublic (m) && Modifier.isStatic (m) && Void.TYPE.equals (
                       main.getReturnType ()
                   );
        } catch (Exception ex) {
            return false;
        } catch (LinkageError re) {
            // false when other errors occur (NoClassDefFoundError etc...)
            return false;
        }
    }

    /** Is this a JavaBean?
    * @return <code>true</code> if this class represents JavaBean (is public and has a public default constructor).
    * @deprecated This method probably should not be used, as it catches a variety of potentially
    *             serious exceptions and errors, and swallows them so as to produce a simple boolean
    *             result. (Notifying them all would be inappropriate as they typically come from user
    *             code.) Better to directly parse the bytecode, using e.g. the classfile module,
    *             which is immune to this class of errors.
    */
    @Deprecated
    public boolean isJavaBean () {
        if (bean != null) return bean.booleanValue ();
        
        // if from ser file => definitely it is a java bean
        if (isSerialized ()) {
            bean = Boolean.TRUE;
            return true;
        }
        
        // try to find out...
        try {
            Class<?> clazz = instanceClass();
            int modif = clazz.getModifiers ();
            if (!Modifier.isPublic (modif) || Modifier.isAbstract (modif)) {
                bean = Boolean.FALSE;
                return false;
            }
            Constructor c;
            try {
                c = clazz.getConstructor (new Class [0]);
            } catch (NoSuchMethodException e) {
                bean = Boolean.FALSE;
                return false;
            }
            if ((c == null) || !Modifier.isPublic (c.getModifiers ())) {
                bean = Boolean.FALSE;
                return false;
            }
            // check: if the class is an inner class, all outer classes have
            // to be public and in the static context:
            
            for (Class outer = clazz.getDeclaringClass(); outer != null; outer = outer.getDeclaringClass()) {
                // check if the enclosed class is static
                if (!Modifier.isStatic(modif))
                    return false;
                modif = outer.getModifiers();
                // ... and the enclosing class is public
                if (!Modifier.isPublic(modif))
                    return false;
            }
        } catch (Exception ex) {
            bean = Boolean.FALSE;
            return true;
        } catch (LinkageError e) {
            // false when other errors occur (NoClassDefFoundError etc...)
            bean = Boolean.FALSE;
            return false;
        }
        // okay, this is bean...
        //    return isBean = java.io.Serializable.class.isAssignableFrom (clazz);
        bean = Boolean.TRUE;
        return true;
    }

    /** Is this an interface?
    * @return <code>true</code> if the class is an interface
    * @deprecated This method probably should not be used, as it catches a variety of potentially
    *             serious exceptions and errors, and swallows them so as to produce a simple boolean
    *             result. (Notifying them all would be inappropriate as they typically come from user
    *             code.) Better to directly parse the bytecode, using e.g. the classfile module,
    *             which is immune to this class of errors.
    */
    @Deprecated
    public boolean isInterface () {
        try {
            return instanceClass ().isInterface ();
        } catch (IOException ex) {
            return false;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }

    public String toString () {
        return instanceName ();
    }

    /** Find context help for some instance.
    * Helper method useful in nodes or data objects that provide an instance cookie;
    * they may choose to supply their own help context based on this.
    * All API classes which can provide help contexts will be tested for
    * (including <code>HelpCtx</code> instances themselves).
    * <code>JComponent</code>s are checked for an attached help ID property,
    * as with {@link HelpCtx#findHelp(java.awt.Component)} (but not traversing parents).
    * <p>Also, partial compliance with the JavaHelp section on JavaBeans help is implemented--i.e.,
    * if a Bean in its <code>BeanInfo</code> provides a <code>BeanDescriptor</code> which
    * has the attribute <code>helpID</code>, this will be returned. The value is not
    * defaulted (because it would usually be nonsense and would mask a useful default
    * help for the instance container), nor is the help set specification checked,
    * since someone should have installed the proper help set anyway, and the APIs
    * cannot add a new reference to a help set automatically.
    * See <code>javax.help.HelpUtilities.getIDStringFromBean</code> for details.
    * <p>Special IDs are added, corresponding to the class name, for all standard visual components.
    * @param instance the instance to check for help (it is permissible for the {@link InstanceCookie#instanceCreate} to return <code>null</code>)
    * @return the help context found on the instance or inferred from a Bean,
    * or <code>null</code> if none was found (or it was {@link HelpCtx#DEFAULT_HELP})
     *
     *
     * @deprecated use org.openide.util.HelpCtx.findHelp (Object)
    */
    @Deprecated
    public static HelpCtx findHelp (InstanceCookie instance) {
        try {
            Class<?> clazz = instance.instanceClass();
            // [a.n] I have moved the code here as those components's BeanInfo do not contain helpID
            // - it is faster
            // Help on some standard components. Note that borders/layout managers do not really work here.
            if (java.awt.Component.class.isAssignableFrom (clazz) || java.awt.MenuComponent.class.isAssignableFrom (clazz)) {
                String name = clazz.getName ();
                String[] pkgs = new String[] { "java.awt.", "javax.swing.", "javax.swing.border." }; // NOI18N
                for (int i = 0; i < pkgs.length; i++) {
                    if (name.startsWith (pkgs[i]) && name.substring (pkgs[i].length ()).indexOf ('.') == -1)
                        return new HelpCtx (name);
                }
            }
            Object o = instance.instanceCreate();
            if (o != null && o != instance) {
                HelpCtx h = HelpCtx.findHelp(o);
                if (h != HelpCtx.DEFAULT_HELP) {
                    return h;
                }
            }
        } catch (Exception e) {
            // #63238: Ignore - generally not important context for us to be checking errors.
        }
        return null;
    }
    
    /** Test whether the instance represents serialized version of a class
     * or not.
     * @return true if the file entry extension is ser
     */
    private boolean isSerialized () {
        return instanceOrigin ().getExt ().equals ("ser"); // NOI18N
    }
    
    /** Reads a class from input stream. Expects a serialized object to be stored
    * in the stream and reads only a class from it.
    * @param is input stream to read from
    * @return the class of that stream
    * @exception IOException if something fails
    */
    private Class readClass (InputStream is) throws IOException, ClassNotFoundException {
        /** object input stream */
        class OIS extends ObjectInputStream {
            public OIS (InputStream iss) throws IOException {
                super (iss);
            }

            /** Throws exception to signal the kind of class found.
            */
            public Class resolveClass (ObjectStreamClass osc)
            throws IOException, ClassNotFoundException {
                Class c = findClass (osc.getName (), null);
                if (c == writeRepl) {
                    // if this is write replace of shared object then 
                    // continue in reading
                    return c;
                }
                
                // stop the reading expecting that we have read the class
                // of the primary object
                throw new ClassEx (c);
            }
        };

        ObjectInputStream ois = new OIS (new BufferedInputStream (is));

        try {
            ois.readObject ();
            // should not happen
            throw new ClassNotFoundException ();
        } catch (ClassEx ex) {
            // good, we found the class
            return ex.clazz;
        }
    }
    
    /** the variable for access to SharedClassObject$WriteReplace */
    private static Class writeRepl;
    static {
        try {
            writeRepl = Class.forName ("org.openide.util.SharedClassObject$WriteReplace"); // NOI18N
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Finds a class for given name.
    * @param name name of the class
    * @return the class for the name
    * @exception ClassNotFoundException if the class cannot be found
    */
    private Class findClass (String name, ClassLoader customLoader) throws ClassNotFoundException {
        ClassLoader loader = null;
        try {
            Class c;
            try {
                if (customLoader != null) {
                    c = customLoader.loadClass(name);
                } else {
                    // to save the space with wasting classloaders, try the system first
                    loader = Lookup.getDefault().lookup(ClassLoader.class);
                    if (loader == null) {
                        loader = getClass ().getClassLoader ();
                    }
                    c = loader.loadClass (name);
                }
            } catch (ClassNotFoundException ex) {
                // ok, ignore and try our class loader
                c = createClassLoader().loadClass(name);
            }
            return c;
        } catch (ClassNotFoundException ex) {
            Exceptions.attachMessage(ex, "ClassLoader: " + loader); // NOI18N
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (LinkageError le) {
            throw new ClassNotFoundException(le.toString(), le);
        }
    }
    
    /** Creates new NbClassLoader with restricted PermissionCollection
     * that contains only:
     * java.io.FilePermission("&lt;&lt;ALL FILES>>", "read")
     * java.util.PropertyPermission("*", "read")
     *
     * @return ClassLoader
     */
    protected ClassLoader createClassLoader() {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        return l;
    }

    /** Trivial supporting instance cookie for already-existing objects.
    */
    public static class Instance extends Object implements InstanceCookie.Of {
        /** the object to represent */
        private Object obj;


        /** Create a new instance cookie.
         * @param obj the object to represent in this cookie
        */
        public Instance (Object obj) {
            this.obj = obj;
        }

        /* The bean name for the instance.
        * @return the name for the instance
        */
        public String instanceName () {
            return obj.getClass ().getName ();
        }

        /* The class of the instance represented by this cookie.
        * Can be used to test whether the instance is of valid
        * class before it is created.
        *
        * @return the class of the instance
         */
        public Class<?> instanceClass() {
            return obj.getClass ();
        }

        /*
        * @return an object to work with
        */
        public Object instanceCreate () {
            return obj;
        }

        public boolean instanceOf(Class<?> type) {
            return type.isAssignableFrom (instanceClass ());
        }
    }

    /** The exception to use to signal succesful find of a class.
    * Used in method readClass.
    */
    private static class ClassEx extends IOException {
        /** founded class */
        public Class clazz;

        static final long serialVersionUID =4810039297880922426L;
        /** @param c the class
        */
        public ClassEx (Class c) {
            clazz = c;
        }
    }
}
