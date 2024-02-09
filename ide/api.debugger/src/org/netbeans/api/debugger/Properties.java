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

package org.netbeans.api.debugger;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Utility class helps store properties.
 *
 * @author Jan Jancura
 */
public abstract class Properties {
    
    private static final Logger LOG = Logger.getLogger(Properties.class.getName());

    private static Properties defaultProperties;

    /**
     * Returns shared instance of Properties class.
     *
     * @return shared instance of Properties class
     */
    public static synchronized Properties getDefault () {
        if (defaultProperties == null) {
            defaultProperties = new PropertiesImpl ();
        }
        return defaultProperties;
    }

    /**
     * Reads String property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract String getString (String propertyName, String defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setString (String propertyName, String value);

    /**
     * Reads int property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract int getInt (String propertyName, int defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setInt (String propertyName, int value);

    /**
     * Reads char property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract char getChar (String propertyName, char defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setChar (String propertyName, char value);

    /**
     * Reads float property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract float getFloat (String propertyName, float defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setFloat (String propertyName, float value);

    /**
     * Reads long property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract long getLong (String propertyName, long defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setLong (String propertyName, long value);

    /**
     * Reads double property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract double getDouble (String propertyName, double defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setDouble (String propertyName, double value);

    /**
     * Reads boolean property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract boolean getBoolean (String propertyName, boolean defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setBoolean (String propertyName, boolean value);

    /**
     * Reads byte property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract byte getByte (String propertyName, byte defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setByte (String propertyName, byte value);

    /**
     * Reads short property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract short getShort (String propertyName, short defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setShort (String propertyName, short value);
    /**
     * Reads Object property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */

    public abstract Object getObject (String propertyName, Object defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setObject (String propertyName, Object value);

    /**
     * Reads array property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract Object[] getArray (String propertyName, Object[] defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setArray (String propertyName, Object[] value);

    /**
     * Reads Collection property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract Collection getCollection (String propertyName, Collection defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setCollection (String propertyName, Collection value);

    /**
     * Reads Map property from storage.
     *
     * @param propertyName a propertyName of property
     * @param defaultValue a default value to be used if property is not set
     * @return a current value of property
     */
    public abstract Map getMap (String propertyName, Map defaultValue);

    /**
     * Sets a new value of property with given propertyName.
     *
     * @param propertyName a propertyName of property
     * @param value the new value of property
     */
    public abstract void setMap (String propertyName, Map value);

    /**
     * Returns Properties instance for some "subfolder" in properties file.
     *
     * @param propertyName a subfolder name
     * @return a Properties instance for some "subfolder" in properties file
     */
    public abstract Properties getProperties (String propertyName);

    /**
     * Add a property change listener to this properties instance.
     * The listener fires a property change event when a new value of some property
     * is set.
     * <p>
     * Please note, that this properties object is not collected from memory
     * sooner than all it's listeners. Therefore it's not necessray to
     * keep a strong reference to this object while holding the listener.
     *
     * @param l The property change listener
     * @throws UnsupportedOperationException if not supported. The default
     * properties implementation retrieved by {@link #getDefault()} supports
     * adding/removing listeners.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Unsupported listening on "+getClass()+" properties."); // NOI18N
    }

    /**
     * Remove a property change listener from this properties instance.
     * <p>
     * Please note, that this properties object is not collected from memory
     * sooner than all it's listeners. Therefore it's not necessray to
     * keep a strong reference to this object while holding the listener.
     * OTOH it is necessary to remove all listeners or release all strong
     * references to the listeners to allow collection of this properties
     * object.
     *
     * @param l The property change listener
     * @throws UnsupportedOperationException if not supported. The default
     * properties implementation retrieved by {@link #getDefault()} supports
     * adding/removing listeners.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Unsupported listening on "+getClass()+" properties."); // NOI18N
    }


    // innerclasses ............................................................

    /**
     * This class helps to store and read custom types using
     * {@link Properties#setObject} and {@link Properties#getObject} methods.
     * Implementations of this class should be stored in "META_INF\debugger"
     * folder.
     */
    public interface Reader {

        /**
         * Returns array of classNames supported by this reader.
         *
         * @return array of classNames supported by this reader
         */
        public String[] getSupportedClassNames ();

        /**
         * Reads object with given className.
         *
         * @param className a name of class to be readed
         * @param properties a properties subfloder containing properties
         *        for this object
         * @return a new instance of className class
         */
        public Object read (String className, Properties properties);

        /**
         * Writes given object to given properties subfolder.
         *
         * @param object a object to be saved
         * @param properties a properties subfolder to be used
         */
        public void write (Object object, Properties properties);
    }

    /**
     * Implementing this interface one can define initial values of properties.
     */
    public interface Initializer {

        /**
         * The list of supported property names.
         * @return the property names supported by this initializer
         */
        String[] getSupportedPropertyNames();

        /**
         * Get the default value of property.
         * @param propertyName The name of the property
         * @return The default value
         */
        Object getDefaultPropertyValue(String propertyName);

    }


    private static final class PrimitiveRegister {

        private Map<String, String> properties = new HashMap<>();
        private boolean isInitialized = false;
        private Map<String, ReentrantReadWriteLock> propertyRWLocks = new HashMap<>();
        private Map<String, Integer> propertyRWLockCounts = new HashMap<>();


        public String getProperty (String propertyName, String defaultValue) {
            synchronized (this) {
                if (!isInitialized) {
                    load ();
                    isInitialized = true;
                }
                String value = (String) properties.get (propertyName);
                if (value != null) {
                    return value;
                }
            }
            return defaultValue;
        }

        public void setProperty (String propertyName, String value) {
            synchronized (this) {
                if (!isInitialized) {
                    load ();
                    isInitialized = true;
                }
                properties.put (propertyName, value);
            }
            save ();
        }

        void removeProperty(String propertyName) {
            synchronized (this) {
                if (!isInitialized) {
                    load ();
                    isInitialized = true;
                }
                properties.remove (propertyName);
            }
            save ();
        }

        private synchronized ReentrantReadWriteLock getRWLock(String propertyName) {
            ReentrantReadWriteLock rwl = propertyRWLocks.get(propertyName);
            if (rwl == null) {
                rwl = new ReentrantReadWriteLock(true);
                propertyRWLocks.put(propertyName, rwl);
            }
            Integer c = propertyRWLockCounts.get(propertyName);
            if (c == null) {
                c = 1;
            } else {
                c = c + 1;
            }
            propertyRWLockCounts.put(propertyName, c);
            return rwl;
        }

        public void lockRead(String propertyName) {
            ReentrantReadWriteLock rwl = getRWLock(propertyName);
            ReadLock rl = rwl.readLock();
            rl.lock();
        }

        public void lockWrite(String propertyName) {
            ReentrantReadWriteLock rwl = getRWLock(propertyName);
            WriteLock wl = rwl.writeLock();
            wl.lock();
        }

        public synchronized void unLockRead(String propertyName) {
            ReentrantReadWriteLock rwl = propertyRWLocks.get(propertyName);
            if (rwl == null) {
                throw new IllegalStateException("No lock for property "+propertyName);
            }
            rwl.readLock().unlock();
            RWUnlock(propertyName);
        }

        public synchronized void unLockWrite(String propertyName) {
            ReentrantReadWriteLock rwl = propertyRWLocks.get(propertyName);
            if (rwl == null) {
                throw new IllegalStateException("No lock for property "+propertyName);
            }
            rwl.writeLock().unlock();
            RWUnlock(propertyName);
        }

        private synchronized void RWUnlock(String propertyName) {
            Integer c = propertyRWLockCounts.get(propertyName);
            if (c.intValue() == 1) {
                propertyRWLockCounts.remove(propertyName);
                propertyRWLocks.remove(propertyName);
            } else {
                c = c - 1;
                propertyRWLockCounts.put(propertyName, c);
            }
        }

        private synchronized void load () {
            BufferedReader br;
            try {
                FileObject fo = findSettings(false);
                if (fo == null) {
                    return ;
                }
                InputStream is = fo.getInputStream ();
                br = new BufferedReader (new InputStreamReader (is));

                String l = br.readLine ();
                while (l != null) {
                    int i = l.indexOf (':');
                    if (i > 0) {
                        String value = l.substring (i + 1);
                        value = translateSingleLineStringToMultiLine(value);
                        properties.put (l.substring (0, i), value);
                    }
                    l = br.readLine ();
                }
                br.close ();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }

        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        private final boolean[] scheduledLock = new boolean[] { false };

        private void save () {
            synchronized (scheduledLock) {
                if (task == null) {
                    task = new RequestProcessor("Debugger Properties Save RP", 1).create(  // NOI18N
                            new Runnable() {
                                @Override
                                public void run () {
                                    saveIn ();
                                }
                            }
                    );
                }
                if (!scheduledLock[0]) {    // Do such hacks because task.schedule() is very slow when RP$SlowItem is used.
                    task.schedule(1000);
                    scheduledLock[0] = true;
                }
            }
        }

        @NbBundle.Messages("MSG_CanNotSaveSettings=Can not save debugger settings.")
        private void saveIn () {
            synchronized (scheduledLock) {
                scheduledLock[0] = false;
            }
            PrintWriter pw = null;
            FileLock lock = null;
            try {
                FileObject fo = findSettings(true);
                lock = fo.lock ();
                OutputStream os = fo.getOutputStream (lock);
                pw = new PrintWriter (os);

                HashMap props;
                synchronized (this) {
                    props = new HashMap(properties);
                }
                Set s = props.keySet ();
                List<String> l = new ArrayList<>(s);
                Collections.sort (l);
                int i, k = l.size ();
                for (i = 0; i < k; i++) {
                    String key = l.get(i);
                    Object value = props.get(key);
                    if (value != null) {
                        // Do not write null values
                        value = translateMultiLineStringToSingleLine(value.toString());
                        pw.println ("" + key + ":" + value); // NOI18N
                    }
                }
                pw.flush ();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, Bundle.MSG_CanNotSaveSettings(), ex);
            } finally {
                try {
                    if (pw != null) {
                        pw.close ();
                    }
                } finally {
                    if (lock != null) {
                        lock.releaseLock ();
                    }
                }
            }
        }

        private static String translateMultiLineStringToSingleLine(String line) {
            /* Do replace of newline and \\n:
            line = line.replace("\\n", "\\\\n");
            return line.replace("\n", "\\n");
            Do it in one pass and minimize garbage: */
            StringBuilder sb = null;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\n' || c == 'n' && i > 0 && line.charAt(i-1) == '\\') {
                    if (sb == null) {
                        sb = new StringBuilder(line.substring(0, i));
                    }
                    sb.append("\\n");  // Replaces "\n" with "\\n" or "\\n" with "\\\\n"  // NOI18N
                } else if (sb != null) {
                    sb.append(c);
                }
            }
            if (sb == null) {
                return line;
            } else {
                return sb.toString();
            }
        }

        private static String translateSingleLineStringToMultiLine(String line) {
            StringBuilder sb = null;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == 'n' && i > 0 && line.charAt(i-1) == '\\') {
                    if (sb == null) {
                        sb = new StringBuilder(line.substring(0, i - 1));
                    } else {
                        int l = sb.length();
                        sb.delete(l - 1, l); // Delete the last slash
                    }
                    if (i > 1 && line.charAt(i-2) == '\\') {
                        sb.append("n"); // Replaces "\\\\n" to "\\n" // NOI18N
                    } else {
                        sb.append("\n"); // Replaces "\\n" to "\n" // NOI18N
                    }
                } else if (sb != null) {
                    sb.append(c);
                }
            }
            if (sb == null) {
                return line;
            } else {
                return sb.toString();
            }
        }

        private static FileObject findSettings(boolean create) throws IOException {
            FileObject r = FileUtil.getConfigFile("Services"); // NOI18N
            if (r == null) {
                if (!create) {
                    return null;
                }
                r = FileUtil.getConfigRoot().createFolder("Services"); // NOI18N
            }
            FileObject fo = r.getFileObject
                ("org-netbeans-modules-debugger-Settings", "properties"); // NOI18N
            if (fo == null) {
                if (!create) {
                    return null;
                }
                fo = r.createData
                    ("org-netbeans-modules-debugger-Settings", "properties"); // NOI18N
            }
            return fo;
        }
    }

    // package-private because of tests
    static class PropertiesImpl extends Properties {

        private static final Object BAD_OBJECT = new Object ();
        private static final String BAD_STRING = ""; // NOI18N
        private static final Map BAD_MAP = new HashMap ();
        private static final Collection BAD_COLLECTION = new ArrayList ();
        private static final Object[] BAD_ARRAY = new Object [0];

        private final Map<String, Reference<Properties>> childProperties = new HashMap<String, Reference<Properties>>();
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Map<PropertyChangeListener, Properties> propertiesHeldByListener = new WeakHashMap<PropertyChangeListener, Properties>();

        ServicesHolder<Reader>      readers      = new ReaderHolder();
        ServicesHolder<Initializer> initializers = new InitializerHolder();

        private abstract static class ServicesHolder<T> {

            private Class<T> clazz;
            // Holds the list to prevent from garbage-collect. Do not remove!
            private List<? extends T> servicesList;
            protected HashMap<String, T> register;

            public ServicesHolder(Class<T> clazz) {
                this.clazz = clazz;
            }

            protected void initServices() {
            }

            private void init() {
                register = new HashMap<String, T>();
                initServices();
                final List<? extends T> list = DebuggerManager.getDebuggerManager().lookup(null, clazz);
                servicesList = list;
                synchronized (list) {
                    for (T s : list) {
                        registerService(s);
                    }
                }
                ((Customizer) list).addPropertyChangeListener(
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                synchronized (ServicesHolder.this) {
                                    Set<T> registeredServices = new HashSet<T>(register.values());
                                    synchronized (list) {
                                        for (T s : list) {
                                            if (!registeredServices.remove(s)) {
                                                registerService(s);
                                            }
                                        }
                                    }
                                    for (T s : registeredServices) {
                                        unregisterService(s);
                                    }
                                }
                            }
                        });
                ((Customizer) list).setObject(Lookup.NOTIFY_LOAD_FIRST);
                ((Customizer) list).setObject(Lookup.NOTIFY_UNLOAD_LAST);
            }

            protected abstract void registerService(T s);

            protected abstract void unregisterService(T s);

            public synchronized T find(String name) {
                if (register == null) {
                    init();
                }
                return register.get(name);
            }

        }

        private static final class ReaderHolder extends ServicesHolder<Reader> {
            
            private final DefaultReader defaultReader = new DefaultReader();

            public ReaderHolder() {
                super(Reader.class);
            }

            @Override
            protected void initServices() {
                registerService(defaultReader);
            }

            @Override
            protected void registerService(Reader r) {
                //System.err.println("registerReader("+r+")");
                String[] ns = r.getSupportedClassNames ();
                int j, jj = ns.length;
                for (j = 0; j < jj; j++) {
                    register.put (ns [j], r);
                }
            }

            @Override
            protected void unregisterService(Reader r) {
                if (r == defaultReader) {
                    // The default reader should not be unregistered.
                    return ;
                }
                //System.err.println("unregisterReader("+r+")");
                String[] ns = r.getSupportedClassNames ();
                int j, jj = ns.length;
                for (j = 0; j < jj; j++) {
                    register.remove (ns [j]);
                }
            }

            @Override
            public synchronized Reader find(String typeID) {

                Reader r = super.find(typeID);
                if (r != null) {
                    return r;
                }

                Class c;
                try {
                    c = getClassLoader ().loadClass (typeID);
                } catch (ClassNotFoundException e) {
                    LOG.log(Level.WARNING, e.getLocalizedMessage(), e);
                    return null;
                }
                while ((c != null) && (register.get (c.getName ()) == null)) {
                    c = c.getSuperclass ();
                }
                if (c != null) {
                    r = (Reader) register.get (c.getName ());
                }
                return r;
            }

        }

        private static final class InitializerHolder extends ServicesHolder<Initializer> {

            public InitializerHolder() {
                super(Initializer.class);
            }

            @Override
            protected void registerService(Initializer i) {
                //System.err.println("registerInitializer("+i+")");
                String[] ns = i.getSupportedPropertyNames();
                int j, jj = ns.length;
                for (j = 0; j < jj; j++) {
                    register.put (ns [j], i);
                }
            }

            @Override
            protected void unregisterService(Initializer i) {
                //System.err.println("unregisterInitializer("+i+")");
                String[] ns = i.getSupportedPropertyNames ();
                int j, jj = ns.length;
                for (j = 0; j < jj; j++) {
                    register.remove (ns [j]);
                }
            }
        }


        private final PrimitiveRegister impl = new PrimitiveRegister ();


        private <T> T getInitialValue(String propertyName, Class<T> clazz) {
            Initializer initializer = initializers.find(propertyName);
            if (initializer != null) {
                Object value = initializer.getDefaultPropertyValue(propertyName);
                if (value != null && !clazz.isAssignableFrom(value.getClass())) {
                    Exceptions.printStackTrace(new IllegalStateException(
                            "Value ("+value+") of a bad type ("+value.getClass()+") returned by "+initializer+ // NOI18N
                            " for property '"+propertyName+"'. It can not be cast to "+clazz));                // NOI18N
                    value = null;
                }
                return (T) value;
            } else {
                return null;
            }
        }

        // primitive properties ....................................................................................

        @Override
        public String getString (String propertyName, String defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                String initialValue = getInitialValue(propertyName, String.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            if (value.equals ("# null")) {                                      // NOI18N
                return null;
            }
            if (!value.startsWith ("\"")) { // NOI18N
                LOG.config("Can not read string " + value + ".");               // NOI18N
                return defaultValue;
            }
            return value.substring (1, value.length () - 1);
        }

        @Override
        public void setString (String propertyName, String value) {
            if (value != null) {
                impl.setProperty (propertyName, "\"" + value + "\""); // NOI18N
            } else {
                impl.setProperty (propertyName, "# null"); // NOI18N
            }
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public int getInt (String propertyName, int defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Integer initialValue = getInitialValue(propertyName, Integer.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            try {
                int val = Integer.parseInt (value);
                return val;
            } catch (NumberFormatException nfex) {
                return defaultValue;
            }
        }

        @Override
        public void setInt (String propertyName, int value) {
            impl.setProperty (propertyName, Integer.toString (value));
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public char getChar (String propertyName, char defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Character initialValue = getInitialValue(propertyName, Character.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            char val = value.charAt (0);
            return val;
        }

        @Override
        public void setChar (String propertyName, char value) {
            impl.setProperty (propertyName, Character.toString(value));
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public float getFloat (String propertyName, float defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Float initialValue = getInitialValue(propertyName, Float.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            try {
                float val = Float.parseFloat (value);
                return val;
            } catch (NumberFormatException nfex) {
                return defaultValue;
            }
        }

        @Override
        public void setFloat (String propertyName, float value) {
            impl.setProperty (propertyName, Float.toString (value));
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public long getLong (String propertyName, long defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Long initialValue = getInitialValue(propertyName, Long.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            try {
                long val = Long.parseLong (value);
                return val;
            } catch (NumberFormatException nfex) {
                return defaultValue;
            }
        }

        @Override
        public void setLong (String propertyName, long value) {
            impl.setProperty (propertyName, Long.toString (value));
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public double getDouble (String propertyName, double defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Double initialValue = getInitialValue(propertyName, Double.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            try {
                double val = Double.parseDouble (value);
                return val;
            } catch (NumberFormatException nfex) {
                return defaultValue;
            }
        }

        @Override
        public void setDouble (String propertyName, double value) {
            impl.setProperty (propertyName, Double.toString (value));
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public boolean getBoolean (String propertyName, boolean defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Boolean initialValue = getInitialValue(propertyName, Boolean.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            boolean val = value.equals ("true"); // NOI18N
            return val;
        }

        @Override
        public void setBoolean (String propertyName, boolean value) {
            impl.setProperty (propertyName, value ? "true" : "false"); // NOI18N
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public byte getByte (String propertyName, byte defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Byte initialValue = getInitialValue(propertyName, Byte.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            try {
                byte val = Byte.parseByte (value);
                return val;
            } catch (NumberFormatException nfex) {
                return defaultValue;
            }
        }

        @Override
        public void setByte (String propertyName, byte value) {
            impl.setProperty (propertyName, Byte.toString (value));
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public short getShort (String propertyName, short defaultValue) {
            String value = impl.getProperty (propertyName, null);
            if (value == null) {
                Short initialValue = getInitialValue(propertyName, Short.class);
                if (initialValue == null) {
                    initialValue = defaultValue;
                }
                return initialValue;
            }
            try {
                short val = Short.parseShort (value);
                return val;
            } catch (NumberFormatException nfex) {
                return defaultValue;
            }
        }

        @Override
        public void setShort (String propertyName, short value) {
            impl.setProperty (propertyName, Short.toString (value));
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public Object getObject (String propertyName, Object defaultValue) {
            try {
                impl.lockRead(propertyName);
                String typeID = impl.getProperty (propertyName, null);
                if (typeID == null) {
                    Object initialValue = getInitialValue(propertyName, Object.class);
                    if (initialValue == null) {
                        initialValue = defaultValue;
                    }
                    return initialValue;
                }
                if (typeID.equals ("# null")) {
                    return null;
                }
                if (!typeID.startsWith ("# ")) { // NOI18N
                    if (typeID.startsWith ("\"")) { // NOI18N
                        String s = getString (propertyName, BAD_STRING);
                        if (s == BAD_STRING) {
                            return defaultValue;
                        }
                        return s;
                    }
                    LOG.config("Can not read object " + typeID + ". No reader registered for type " + typeID + "."); // NOI18N
                    return defaultValue;
                }
                typeID = typeID.substring (2);
                Class c = null;
                try {
                    c = Class.forName (typeID, true, org.openide.util.Lookup.getDefault().lookup(ClassLoader.class));
                } catch (ClassNotFoundException e) {
                }
                if (c != null) {
                    if (Map.class.isAssignableFrom (c)) {
                        Map m = getMap (propertyName, BAD_MAP);
                        if (m == BAD_MAP) {
                            return defaultValue;
                        }
                        return m;
                    }
                    if (Object [].class.isAssignableFrom (c)) {
                        Object[] os = getArray (propertyName, BAD_ARRAY);
                        if (os == BAD_ARRAY) {
                            return defaultValue;
                        }
                        return os;
                    }
                    if (Collection.class.isAssignableFrom (c)) {
                        Collection co = getCollection (propertyName, BAD_COLLECTION);
                        if (co == BAD_COLLECTION) {
                            return defaultValue;
                        }
                        return co;
                    }
                }
                Reader r = readers.find(typeID);
                if (r == null) {
                    LOG.config("Can not read object. No reader registered for type " + typeID + "."); // NOI18N
                    return defaultValue;
                }
                return r.read (typeID, getProperties (propertyName));
            } finally {
                impl.unLockRead(propertyName);
            }
        }

        @Override
        public void setObject (String propertyName, Object value) {
            try {
                impl.lockWrite(propertyName);
                if (value == null) {
                    impl.setProperty (propertyName, "# null"); // NOI18N
                } else if (value instanceof String) {
                    setString (propertyName, (String) value);
                } else if (value instanceof Map) {
                    setMap (propertyName, (Map) value);
                } else if (value instanceof Collection) {
                    setCollection (propertyName, (Collection) value);
                } else if (value instanceof Object[]) {
                    setArray (propertyName, (Object[]) value);
                } else {

                    // find register
                    Reader r = readers.find(value.getClass ().getName ());
                    if (r == null) {
                        LOG.config("Can not write object " + value); // NOI18N
                        return;
                    }

                    // write
                    r.write (value, getProperties (propertyName));
                    impl.setProperty (propertyName, "# " + value.getClass ().getName ()); // NOI18N

                }
            } finally {
                impl.unLockWrite(propertyName);
            }
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public Object[] getArray (String propertyName, Object[] defaultValue) {
            try {
                impl.lockRead(propertyName);
                String arrayType = impl.getProperty (propertyName + ".array_type", null); // NOI18N
                if (arrayType == null) {
                    Object[] initialValue = getInitialValue(propertyName, Object[].class);
                    if (initialValue == null) {
                        initialValue = defaultValue;
                    }
                    return initialValue;
                }
                Properties p = getProperties (propertyName);
                int l = p.getInt ("length", -1); // NOI18N
                if (l < 0) {
                    return defaultValue;
                }
                boolean fixArrayType = false;
                ClassNotFoundException arrayTypeNotFoundEx = null;
                Object[] os;
                try {
                    os = (Object[]) Array.newInstance (
                        getClassLoader ().loadClass (arrayType),
                        l
                    );
                } catch (ClassNotFoundException ex) {
                    arrayTypeNotFoundEx = ex;
                    os = new Object [l];
                    fixArrayType = true;
                }
                Class aType = null;
                for (int i = 0; i < l; i++) {
                    Object o = p.getObject ("" + i, BAD_OBJECT); // NOI18N
                    if (o == BAD_OBJECT) {
                        return defaultValue;
                    }
                    os [i] = o;
                    if (fixArrayType && o != null) {
                        Class oType = o.getClass();
                        if (aType == null) {
                            aType = oType;
                        } else {
                            if (aType != oType) {
                                fixArrayType = false;
                                aType = null;
                            }
                        }
                    }
                }
                if (fixArrayType && aType != null) {
                    Object[] newOS = (Object[]) Array.newInstance(aType, l);
                    System.arraycopy(os, 0, newOS, 0, l);
                    os = newOS;
                } else if (arrayTypeNotFoundEx != null) {
                    Exceptions.printStackTrace(arrayTypeNotFoundEx);
                }
                return os;
            } finally {
                impl.unLockRead(propertyName);
            }
        }

        @Override
        public void setArray (String propertyName, Object[] value) {
            try {
                impl.lockWrite(propertyName);
                impl.setProperty (propertyName, "# array"); // NOI18N
                impl.setProperty (propertyName + ".array_type", value.getClass ().getComponentType ().getName ()); // NOI18N
                Properties p = getProperties (propertyName);
                int i, k = value.length;
                p.setInt ("length", k); // NOI18N
                for (i = 0; i < k; i++) {
                    p.setObject ("" + i, value [i]); // NOI18N
                }
            } finally {
                impl.unLockWrite(propertyName);
            }
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public Collection getCollection (String propertyName, Collection defaultValue) {
            try {
                impl.lockRead(propertyName);
                String typeID = impl.getProperty (propertyName, null);
                if (typeID == null) {
                    Collection initialValue = getInitialValue(propertyName, Collection.class);
                    if (initialValue == null) {
                        initialValue = defaultValue;
                    }
                    return initialValue;
                }
                if (!typeID.startsWith ("# ")) {  // NOI18N
                    return defaultValue;
                }
                Collection c;
                try {
                    c = (Collection) Class.forName(typeID.substring(2)).getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException ex) {
                    LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    return defaultValue;
                }
                Properties p = getProperties (propertyName);
                int i, k = p.getInt ("length", 0); // NOI18N
                for (i = 0; i < k; i++) {
                    Object o = p.getObject ("" + i, BAD_OBJECT); // NOI18N
                    if (o == BAD_OBJECT) {
                        return defaultValue;
                    }
                    c.add (o);
                }
                return c;
            } finally {
                impl.unLockRead(propertyName);
            }
        }

        @Override
        public void setCollection (String propertyName, Collection value) {
            try {
                impl.lockWrite(propertyName);
                if (value == null) {
                    impl.setProperty (propertyName, null);
                } else {
                    impl.setProperty (propertyName, "# " + value.getClass ().getName ()); // NOI18N
                    Properties p = getProperties (propertyName);
                    Iterator it = value.iterator ();
                    int i = 0;
                    p.setInt ("length", value.size ()); // NOI18N
                    while (it.hasNext ()) {
                        p.setObject ("" + i, it.next ()); // NOI18N
                        i++;
                    }
                }
            } finally {
                impl.unLockWrite(propertyName);
            }
            pcs.firePropertyChange(propertyName, null, value);
        }

        @Override
        public Map getMap (String propertyName, Map defaultValue) {
            try {
                impl.lockRead(propertyName);
                String typeID = impl.getProperty (propertyName, null);
                if (typeID == null) {
                    Map initialValue = getInitialValue(propertyName, Map.class);
                    if (initialValue == null) {
                        initialValue = defaultValue;
                    }
                    return initialValue;
                }
                if (!typeID.startsWith ("# ")) {    // NOI18N
                    return defaultValue; 
                }
                Map m;
                try {
                    m = (Map) Class.forName(typeID.substring(2)).getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException ex) {
                    LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    return defaultValue;
                }
                Properties p = getProperties (propertyName);
                int i, k = p.getInt ("length", 0); // NOI18N
                for (i = 0; i < k; i++) {
                    Object key = p.getObject ("" + i + "-key", BAD_OBJECT); // NOI18N
                    if (key == BAD_OBJECT) {
                        return defaultValue;
                    }
                    Object value = p.getObject ("" + i + "-value", BAD_OBJECT); // NOI18N
                    if (value == BAD_OBJECT) {
                        return defaultValue;
                    }
                    m.put (key, value);
                }
                return m;
            } finally {
                impl.unLockRead(propertyName);
            }
        }

        @Override
        public void setMap (String propertyName, Map value) {
            try {
                impl.lockWrite(propertyName);
                if (value == null) {
                    impl.setProperty (propertyName, null);
                } else {
                    impl.setProperty (propertyName, "# " + value.getClass ().getName ()); // NOI18N
                    Properties p = getProperties (propertyName);
                    Iterator<Map.Entry> it = value.entrySet ().iterator ();
                    int i = 0;
                    p.setInt ("length", value.size ()); // NOI18N
                    while (it.hasNext ()) {
                        Map.Entry e = it.next ();
                        p.setObject ("" + i + "-key", e.getKey()); // NOI18N
                        p.setObject ("" + i + "-value", e.getValue()); // NOI18N
                        i++;
                    }
                }
            } finally {
                impl.unLockWrite(propertyName);
            }
            pcs.firePropertyChange(propertyName, null, value);
        }

        public void unset (String propertyName) {
            impl.removeProperty (propertyName);
            pcs.firePropertyChange(propertyName, null, null);
        }

        @Override
        public Properties getProperties (String propertyName) {
            synchronized (childProperties) {
                Reference<Properties> propRef = childProperties.get(propertyName);
                if (propRef != null) {
                    Properties p = propRef.get();
                    if (p != null) {
                        return p;
                    }
                }
                Properties p = new DelegatingProperties (this, propertyName);
                propRef = new WeakReference<Properties>(p);
                childProperties.put(propertyName, propRef);
                return p;
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
            synchronized (propertiesHeldByListener) {
                propertiesHeldByListener.put(l, this);
            }
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
            synchronized (propertiesHeldByListener) {
                propertiesHeldByListener.remove(l);
            }
        }


        private static ClassLoader classLoader;
        private static ClassLoader getClassLoader () {
            if (classLoader == null) {
                //Thread.currentThread ().getContextClassLoader ();
                classLoader = (ClassLoader) org.openide.util.Lookup.
                    getDefault ().lookup (ClassLoader.class);
            }
            return classLoader;
        }


        private static class DefaultReader implements Reader {

            private static final String[] classNames =
                new String[] {
                    Boolean.class.getName(),
                    Byte.class.getName(),
                    Character.class.getName(),
                    Short.class.getName(),
                    Integer.class.getName(),
                    Long.class.getName(),
                    Float.class.getName(),
                    Double.class.getName()
                };
            private static final String propertyName = "primitiveWrapper"; // NOI18N
            
            @Override
            public String[] getSupportedClassNames() {
                return classNames;
            }

            @Override
            public Object read(String className, Properties properties) {
                if (classNames[0].equals(className)) {
                    return Boolean.valueOf(properties.getBoolean(propertyName, false));
                } else if (classNames[1].equals(className)) {
                    return properties.getByte(propertyName, (byte)0);
                } else if (classNames[2].equals(className)) {
                    return new Character(properties.getChar(propertyName, (char) 0));
                } else if (classNames[3].equals(className)) {
                    return properties.getShort(propertyName, (short)0);
                } else if (classNames[4].equals(className)) {
                    return Integer.valueOf(properties.getInt(propertyName, 0));
                } else if (classNames[5].equals(className)) {
                    return new Long(properties.getLong(propertyName, 0l));
                } else if (classNames[6].equals(className)) {
                    return properties.getFloat(propertyName, 0f);
                } else if (classNames[7].equals(className)) {
                    return properties.getDouble(propertyName, 0D);
                }
                throw new IllegalArgumentException("Class = '"+className+"'.");
            }

            @Override
            public void write(Object object, Properties properties) {
                if (object instanceof Boolean) {
                    properties.setBoolean(propertyName, ((Boolean) object).booleanValue());
                } else if (object instanceof Byte) {
                    properties.setByte(propertyName, ((Byte) object).byteValue());
                } else if (object instanceof Character) {
                    properties.setChar(propertyName, ((Character) object).charValue());
                } else if (object instanceof Short) {
                    properties.setShort(propertyName, ((Short) object).shortValue());
                } else if (object instanceof Integer) {
                    properties.setInt(propertyName, ((Integer) object).intValue());
                } else if (object instanceof Long) {
                    properties.setLong(propertyName, ((Long) object).longValue());
                } else if (object instanceof Float) {
                    properties.setFloat(propertyName, ((Float) object).floatValue());
                } else if (object instanceof Double) {
                    properties.setDouble(propertyName, ((Double) object).doubleValue());
                }
            }
            
        }

    }

    private static class DelegatingProperties extends Properties {

        private PropertiesImpl delegatingProperties;
        private String root;
        private final Map<String, Reference<Properties>> childProperties =
                new HashMap<String, Reference<Properties>>();
        private final Map<PropertyChangeListener, PropertyChangeListener> delegatingListeners =
                new WeakHashMap<PropertyChangeListener, PropertyChangeListener>();


        DelegatingProperties (PropertiesImpl properties, String root) {
            delegatingProperties = properties;
            this.root = root;
        }

        @Override
        public String getString (String propertyName, String defaultValue) {
            return delegatingProperties.getString (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setString (String propertyName, String value) {
            delegatingProperties.setString (root + '.' + propertyName, value);
        }

        @Override
        public int getInt (String propertyName, int defaultValue) {
            return delegatingProperties.getInt (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setInt (String propertyName, int value) {
            delegatingProperties.setInt (root + '.' + propertyName, value);
        }

        @Override
        public byte getByte (String propertyName, byte defaultValue) {
            return delegatingProperties.getByte (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setByte (String propertyName, byte value) {
            delegatingProperties.setByte (root + '.' + propertyName, value);
        }

        @Override
        public char getChar (String propertyName, char defaultValue) {
            return delegatingProperties.getChar (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setChar (String propertyName, char value) {
            delegatingProperties.setChar (root + '.' + propertyName, value);
        }

        @Override
        public boolean getBoolean (String propertyName, boolean defaultValue) {
            return delegatingProperties.getBoolean (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setBoolean (String propertyName, boolean value) {
            delegatingProperties.setBoolean (root + '.' + propertyName, value);
        }

        @Override
        public short getShort (String propertyName, short defaultValue) {
            return delegatingProperties.getShort (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setShort (String propertyName, short value) {
            delegatingProperties.setShort (root + '.' + propertyName, value);
        }

        @Override
        public long getLong (String propertyName, long defaultValue) {
            return delegatingProperties.getLong (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setLong (String propertyName, long value) {
            delegatingProperties.setLong (root + '.' + propertyName, value);
        }

        @Override
        public double getDouble (String propertyName, double defaultValue) {
            return delegatingProperties.getDouble (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setDouble (String propertyName, double value) {
            delegatingProperties.setDouble (root + '.' + propertyName, value);
        }

        @Override
        public float getFloat (String propertyName, float defaultValue) {
            return delegatingProperties.getFloat (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setFloat (String propertyName, float value) {
            delegatingProperties.setFloat (root + '.' + propertyName, value);
        }

        @Override
        public Object getObject (String propertyName, Object defaultValue) {
            return delegatingProperties.getObject (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setObject (String propertyName, Object value) {
            delegatingProperties.setObject (root + '.' + propertyName, value);
        }

        @Override
        public Object[] getArray (String propertyName, Object[] defaultValue) {
            return delegatingProperties.getArray (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setArray (String propertyName, Object[] value) {
            delegatingProperties.setArray (root + '.' + propertyName, value);
        }

        @Override
        public Collection getCollection (String propertyName, Collection defaultValue) {
            return delegatingProperties.getCollection (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setCollection (String propertyName, Collection value) {
            delegatingProperties.setCollection (root + '.' + propertyName, value);
        }

        @Override
        public Map getMap (String propertyName, Map defaultValue) {
            return delegatingProperties.getMap (root + '.' + propertyName, defaultValue);
        }

        @Override
        public void setMap (String propertyName, Map value) {
            delegatingProperties.setMap (root + '.' + propertyName, value);
        }

        public void unset (String propertyName) {
            delegatingProperties.unset (root + '.' + propertyName);
        }

        @Override
        public Properties getProperties (String propertyName) {
            synchronized (childProperties) {
                Reference<Properties> propRef = childProperties.get(propertyName);
                if (propRef != null) {
                    Properties p = propRef.get();
                    if (p != null) {
                        return p;
                    }
                }
                Properties p = new DelegatingProperties (delegatingProperties, root + '.' + propertyName);
                propRef = new WeakReference<Properties>(p);
                childProperties.put(propertyName, propRef);
                return p;
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            PropertyChangeListener delegate = new DelegatingPropertyChangeListener(l);
            synchronized (delegatingListeners) {
                delegatingListeners.put(l, delegate);
            }
            delegatingProperties.addPropertyChangeListener(delegate);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            PropertyChangeListener delegate;
            synchronized (delegatingListeners) {
                delegate = delegatingListeners.get(l);
            }
            if (delegate != null) {
                delegatingProperties.removePropertyChangeListener(delegate);
            }
        }

        private class DelegatingPropertyChangeListener implements PropertyChangeListener {

            private PropertyChangeListener delegate;
            private String r = root + '.';
            private int rl = r.length();

            public DelegatingPropertyChangeListener(PropertyChangeListener delegate) {
                this.delegate = delegate;
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (propertyName.length() <= rl || !propertyName.startsWith(r)) {
                    // not a listener in this root
                    return ;
                }
                PropertyChangeEvent delegateEvt = new PropertyChangeEvent(
                        DelegatingProperties.this,
                        evt.getPropertyName().substring(rl),
                        evt.getOldValue(),
                        evt.getNewValue());
                delegateEvt.setPropagationId(evt.getPropagationId());
                delegate.propertyChange(delegateEvt);
            }
            
        }

    }
}
