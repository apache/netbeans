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
package org.openide.options;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;


/** 
* Base class for all system options.
* Provides methods for adding
* and working with property change and guarantees
* that all instances of the same class will share these listeners.
* <P>
* When a new option is created, it should subclass
* <CODE>SystemOption</CODE>, add <em>static</em> variables to it that will hold
* the values of properties, and write non-static setters/getters that will
* notify all listeners about property changes via
* {@link #firePropertyChange}.
* <p>JavaBeans introspection is used to find the properties,
* so it is possible to use {@link BeanInfo}.
*
* @author Jaroslav Tulach
 * @deprecated Use {@link org.openide.util.NbPreferences} instead.
*/
@Deprecated
public abstract class SystemOption extends SharedClassObject implements HelpCtx.Provider {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 558589201969066966L;

    /** property to indicate that the option is currently loading its data */
    private static final Object PROP_LOADING = new Object();

    /** property to indicate that the option is currently loading its data */
    private static final Object PROP_STORING = new Object();

    /** property that holds a Map<String,Object> that stores old values */
    private static final Object PROP_ORIGINAL_VALUES = new Object();

    /** this represent null in the map in PROP_ORIGINAL_VALUES */
    private static final Object NULL = new Object();

    /** Default constructor. */
    public SystemOption() {
    }

    /** Fire a property change event to all listeners. Delays
    * this loading when readExternal is active till it finishes.
    *
    * @param name the name of the property
    * @param oldValue the old value
    * @param newValue the new value
    */
    protected void firePropertyChange(String name, Object oldValue, Object newValue) {
        if ((name != null) && (getProperty("org.openide.util.SharedClassObject.initialize") == null)) { // NOI18N

            Map originalValues = (Map) getProperty(PROP_ORIGINAL_VALUES);

            if (originalValues == null) {
                originalValues = new HashMap<>();
                putProperty(PROP_ORIGINAL_VALUES, originalValues);
            }

            if (originalValues.get(name) == null) {
                if (getProperty(name) == null) {
                    // this is supposed to be setter
                    originalValues.put(name, new Box(oldValue));
                } else {
                    // regular usage of putProperty (....);
                    originalValues.put(name, (oldValue == null) ? NULL : oldValue);
                }
            }
        }

        if (getProperty(PROP_LOADING) != null) {
            // somebody is loading, assign any object different than
            // this to indicate that firing should occure
            putProperty(PROP_LOADING, PROP_LOADING);

            // but do not fire the change now
            return;
        }

        super.firePropertyChange(name, oldValue, newValue);
    }

    /** Implements the reset by setting back all properties that were
     * modified. A <em>modified property</em> has fired a
     * <code>PropertyChangeEvent</code> with
     * non-null name and non-null old value. The name and value are
     * remembered and this method sets them back to original value.
     * <p>
     * Subclasses are free to override this method and reimplement the
     * reset by themselves.
     *
     * @since 4.46
     */
    protected void reset() {
        synchronized (getLock()) {
            Map m = (Map) getProperty(PROP_ORIGINAL_VALUES);

            if ((m == null) || m.isEmpty()) {
                return;
            }

            Iterator<Map.Entry> it = m.entrySet().iterator();
WHILE: 
            while (it.hasNext()) {
                Map.Entry e = it.next();

                if (e.getValue() instanceof Box) {
                    Object value = ((Box) e.getValue()).value;

                    try {
                        // gets info about all properties that were added by subclass
                        BeanInfo info = org.openide.util.Utilities.getBeanInfo(getClass(), SystemOption.class);
                        PropertyDescriptor[] desc = info.getPropertyDescriptors();

                        for (int i = 0; i < desc.length; i++) {
                            if (e.getKey().equals(desc[i].getName())) {
                                // our property
                                Method write = desc[i].getWriteMethod();

                                if (write != null) {
                                    write.invoke(this, new Object[] { value });
                                }

                                continue WHILE;
                            }
                        }
                    } catch (InvocationTargetException ex) {
                        // exception thrown
                        Logger.getLogger(SystemOption.class.getName()).log(Level.WARNING, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(SystemOption.class.getName()).log(Level.WARNING, null, ex);
                    } catch (IntrospectionException ex) {
                        Logger.getLogger(SystemOption.class.getName()).log(Level.WARNING, null, ex);
                    }
                } else {
                    putProperty(e.getKey(), (e.getValue() == NULL) ? null : e.getValue());
                }
            }

            // reset all remembered values
            putProperty(PROP_ORIGINAL_VALUES, null);
        }

        super.firePropertyChange(null, null, null);
    }

    /** Write all properties of this object (or subclasses) to an object output.
    * @param out the output stream
    * @exception IOException on error
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            // gets info about all properties that were added by subclass
            BeanInfo info = org.openide.util.Utilities.getBeanInfo(getClass(), SystemOption.class);
            PropertyDescriptor[] desc = info.getPropertyDescriptors();

            putProperty(PROP_STORING, this);

            Object[] param = new Object[0];

            synchronized (getLock()) {
                // write all properties that have getter to stream
                for (int i = 0; i < desc.length; i++) {
                    // skip readonly Properties
                    if (desc[i].getWriteMethod() == null) {
                        continue;
                    }
                    String propName = desc[i].getName();
                    Object value = getProperty(propName);
                    boolean fromRead;
                    // JST: this code handles the case when somebody needs to store
                    // different value then is the value of get/set method.
                    // in such case value (from getProperty) is not of the type
                    // of the getter/setter and is used instead of the value from getXXXX
                    Method read = desc[i].getReadMethod();

                    if (read == null) {
                        continue;
                    }
                    if ((value == null) || isInstance(desc[i].getPropertyType(),
                                                      value)) {
                        fromRead = true;
                        try {
                            value = read.invoke(this, param);
                        }
                        catch (InvocationTargetException ex) {
                            throw (IOException) new IOException(NbBundle.getMessage(SystemOption.class,
                                                                                    "EXC_InGetter",
                                                                                    getClass(),
                                                                                    desc[i].getName())).initCause(ex);
                        }
                        catch (IllegalAccessException ex) {
                            throw (IOException) new IOException(NbBundle.getMessage(SystemOption.class,
                                                                                    "EXC_InGetter",
                                                                                    getClass(),
                                                                                    desc[i].getName())).initCause(ex);
                        }
                    } else {
                        fromRead = false;
                    }
                    // writes name of the property
                    out.writeObject(propName);
                    // writes its value
                    out.writeObject(value);
                    // from getter or stored prop?
                    out.writeObject(fromRead ? Boolean.TRUE
                                             : Boolean.FALSE);
                }
            }
        } catch (IntrospectionException ex) {
            // if we cannot found any info about properties
        } finally {
            putProperty(PROP_STORING, null);
        }

        // write null to signal end of properties
        out.writeObject(null);
    }

    /** Returns true if the object is assignable to the class.
     * Also if the class is primitive and the object is of the matching wrapper type.
     */
    private static boolean isInstance(Class c, Object o) {
        return c.isInstance(o) || ((c == Byte.TYPE) && (o instanceof Byte)) ||
        ((c == Short.TYPE) && (o instanceof Short)) || ((c == Integer.TYPE) && (o instanceof Integer)) ||
        ((c == Long.TYPE) && (o instanceof Long)) || ((c == Float.TYPE) && (o instanceof Float)) ||
        ((c == Double.TYPE) && (o instanceof Double)) || ((c == Boolean.TYPE) && (o instanceof Boolean)) ||
        ((c == Character.TYPE) && (o instanceof Character));
    }

    /** Read all properties of this object (or subclasses) from an object input.
    * If there is a problem setting the value of any property, that property will be ignored;
    * other properties should still be set.
    * @param in the input stream
    * @exception IOException on error
    * @exception ClassNotFoundException if a class used to restore the system option is not found
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // hashtable that maps names of properties to setter methods
        HashMap<String, Method> map = new HashMap<>();

        try {
            synchronized (getLock()) {
                // indicate that we are loading files
                putProperty(PROP_LOADING, this);

                try {
                    // gets info about all properties that were added by subclass
                    BeanInfo info = org.openide.util.Utilities.getBeanInfo(getClass(), SystemOption.class);
                    PropertyDescriptor[] desc = info.getPropertyDescriptors();

                    // write all properties that have getter to stream
                    for (int i = 0; i < desc.length; i++) {
                        Method m = desc[i].getWriteMethod();

                        /*if (m == null) {
                          System.out.println ("HOW HOW HOW HOWHOWHOWHOWHWO: " + desc[i].getName() + " XXX " + getClass());
                          throw new IOException (new MessageFormat (NbBundle.getBundle (SystemOption.class).getString ("EXC_InSetter")).
                            format (new Object[] {getClass (), desc[i].getName ()})
                                                );
                        } */
                        map.put(desc[i].getName(), m);
                    }
                } catch (IntrospectionException ex) {
                    // if we cannot found any info about properties
                    // leave the hashtable empty and only read stream till null is found
                    Logger.getLogger(SystemOption.class.getName()).log(Level.WARNING, null, ex);
                }

                String preread = null;

                do {
                    // read the name of property
                    String name;

                    if (preread != null) {
                        name = preread;
                        preread = null;
                    } else {
                        name = (String) in.readObject();
                    }

                    // break if the end of property stream is found
                    if (name == null) {
                        break;
                    }

                    // read the value of property
                    Object value = in.readObject();

                    // read flag - use the setter method or store as property?
                    Object useMethodObject = in.readObject();
                    boolean useMethod;
                    boolean nullRead = false; // this should be last processed property?

                    if (useMethodObject == null) {
                        useMethod = true;
                        nullRead = true;
                    } else if (useMethodObject instanceof String) {
                        useMethod = true;
                        preread = (String) useMethodObject;
                    } else {
                        useMethod = ((Boolean) useMethodObject).booleanValue();
                    }

                    if (useMethod) {
                        // set the value
                        Method write = map.get(name);

                        if (write != null) {
                            // if you have where to set the value
                            try {
                                write.invoke(this, new Object[] { value });
                            } catch (Exception ex) {
                                String msg = "Cannot call " + write + " for property " + getClass().getName() + "." +
                                    name; // NOI18N
                                Exceptions.attachMessage(ex, msg);
                                Logger.getLogger(SystemOption.class.getName()).log(Level.WARNING, null, ex);
                            }
                        }
                    } else {
                        putProperty(name, value, false);
                    }

                    if (nullRead) {
                        break;
                    }
                } while (true);
            }
        } finally {
            // get current state
            if (this != getProperty(PROP_LOADING)) {
                // some changes should be fired
                // loading finished
                putProperty(PROP_LOADING, null);
                firePropertyChange(null, null, null);
            } else {
                // loading finished
                putProperty(PROP_LOADING, null);
            }
        }
    }

    protected boolean clearSharedData() {
        return false;
    }

    /**
    * Get the name of this system option.
    * The default implementation just uses the {@link #displayName display name}.
    * @return the name
    */
    public final String getName() {
        return displayName();
    }

    /**
    * Get the display name of this system option.
    * @return the display name
    */
    public abstract String displayName();

    /** Get context help for this system option.
    * @return context help
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SystemOption.class);
    }

    /** Allows subclasses to test whether the change of a property
    * is invoked from readExternal method or by external change invoked
    * by any other program.
    *
    * @return true if the readExternal method is in progress
    */
    protected final boolean isReadExternal() {
        return getProperty(PROP_LOADING) != null;
    }

    /** Allows subclasses to test whether the getter of a property
    * is invoked from writeExternal method or by any other part of the program.
    *
    * @return true if the writeExternal method is in progress
    */
    protected final boolean isWriteExternal() {
        return getProperty(PROP_STORING) != null;
    }

    /** A wrapper object to indicate that a setter should be called
     * when reseting to default.
     */
    private static final class Box extends Object {
        public Object value;

        public Box(Object v) {
            this.value = v;
        }
    }
     // end of Box
}
