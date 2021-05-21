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

package org.openide;

import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/** This class represents an abstract subclass for services
* (compilation, execution, debugging, etc.) that can be registered in
* the system.
*
* @author Jaroslav Tulach
* @deprecated The prefered way to register and lookup services
 *   is now {@link Lookup} as described in <a href="util/doc-files/api.html#lookup">
 *   services registration and lookup</a> page.
*/
@Deprecated
public abstract class ServiceType extends Object implements Serializable, HelpCtx.Provider {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -7573598174423654252L;

    /** Name of property for the name of the service type. */
    public static final String PROP_NAME = "name"; // NOI18N
    private static final Logger err = Logger.getLogger("org.openide.ServiceType"); // NOI18N

    /** name of the service type */
    private String name;

    /** listeners support */
    private transient PropertyChangeSupport supp;

    /** Default human-presentable name of the service type.
    * In the default implementation, taken from the bean descriptor.
    * @return initial value of the human-presentable name
    * @see java.beans.FeatureDescriptor#getDisplayName
    */
    protected String displayName() {
        try {
            return Introspector.getBeanInfo(getClass()).getBeanDescriptor().getDisplayName();
        } catch (Exception e) {
            // Catching IntrospectionException, but also maybe NullPointerException...?
            Logger.getLogger(ServiceType.class.getName()).log(Level.WARNING, null, e);

            return getClass().getName();
        }
    }

    /** Method that creates a cloned instance of this object. Subclasses
     * are encouraged to implement the {@link Cloneable}
     * interface, in such case the <code>clone</code> method is called as a result
     * of calling this method. If the subclass does not implement
     * <code>Cloneable</code>, it is serialized and deserialized,
     * thus new instance created.
     *
     * @return new instance
     * @exception IllegalStateException if something goes wrong, but should not happen
     * @deprecated Service instance files should instead be copied in order to clone them.
     */
    @Deprecated
    public final ServiceType createClone() {
        Exception anEx;

        if (this instanceof Cloneable) {
            try {
                return (ServiceType) clone();
            } catch (CloneNotSupportedException ex) {
                anEx = ex;
            }
        } else {
            try {
                org.openide.util.io.NbMarshalledObject m = new org.openide.util.io.NbMarshalledObject(this);

                return (ServiceType) m.get();
            } catch (IOException ex) {
                anEx = ex;
            } catch (ClassNotFoundException ex) {
                anEx = ex;
            }
        }

        // the code can get here only if an exception occured
        // moreover it should never happen that this code is executed
        IllegalStateException ex = new IllegalStateException();

        ex.initCause(anEx);
        Exceptions.attachLocalizedMessage(ex, "Cannot createClone for " + this); // NOI18N

        throw ex;
    }

    /** Correctly implements the clone operation on this object. In
     * order to work really correctly, the subclass has to implement the
     * Cloneable interface.
     *
     * @return a new cloned instance that does not have any listeners
     * @deprecated Service instance files should instead be copied in order to clone them.
     */
    @Deprecated
    protected Object clone() throws CloneNotSupportedException {
        ServiceType t = (ServiceType) super.clone();

        // clear listeners
        t.supp = null;

        // clear name
        t.name = null;

        return t;
    }

    /** Set the name of the service type.
    * Usually it suffices to override {@link #displayName},
    * or just to provide a {@link java.beans.BeanDescriptor} for the class.
    * @param name the new human-presentable name
    */
    public void setName(String name) {
        String old = this.name;
        this.name = name;

        if (supp != null) {
            supp.firePropertyChange(PROP_NAME, old, name);
        }
    }

    /** Get the name of the service type.
    * The default value is given by {@link #displayName}.
    * @return a human-presentable name for the service type
    */
    public String getName() {
        return (name == null) ? displayName() : name;
    }

    /** Get context help for this service type.
    * @return context help
    */
    public abstract HelpCtx getHelpCtx();

    /** Add a property change listener.
    * @param l the listener to add
    */
    public final synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (supp == null) {
            supp = new PropertyChangeSupport(this);
        }

        supp.addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
    * @param l the listener to remove
    */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (supp != null) {
            supp.removePropertyChangeListener(l);
        }
    }

    /** Fire information about change of a property in the service type.
    * @param name name of the property
    * @param o old value
    * @param n new value
    */
    protected final void firePropertyChange(String name, Object o, Object n) {
        if (supp != null) {
            supp.firePropertyChange(name, o, n);
        }
    }

    /**
     * The registry of all services. This class is provided by the NetBeans core
    * and should hold all of the services registered to the system.
    * <P>
    * This class can be serialized to securely save settings of all
    * services in the system.
    * @deprecated Use lookup instead.
    */
    @Deprecated
    public static abstract class Registry implements Serializable {
        /** suid */
        final static long serialVersionUID = 8721000770371416481L;

        /** Get all available services managed by the engine.
        * @return an enumeration of {@link ServiceType}s
        */
        public abstract Enumeration<ServiceType> services();

        /** Get all available services that are assignable to the given superclass.
        * @param clazz the class that all services should be subclass of
        * @return an enumeration of all matching {@link ServiceType}s
        */
        public <T extends ServiceType> Enumeration<T> services(final Class<T> clazz) {
            class IsInstance implements Enumerations.Processor<ServiceType,T> {
                public T process(ServiceType obj, Collection ignore) {
                    return clazz.isInstance(obj) ? clazz.cast(obj) : null;
                }
            }

            return Enumerations.filter(services(), new IsInstance());
        }

        /** Getter for list of all service types.
        * @return a list of {@link ServiceType}s
        */
        public abstract List getServiceTypes();

        /** Setter for list of service types. This permits changing
        * instances of the objects but only within the types that are already registered
        * in the system by manifest sections. If an instance of any other type
        * is in the list it is ignored.
        *
        * @param arr a list of {@link ServiceType}s
        * @deprecated Better to change service instance files instead.
        */
        @Deprecated
        public abstract void setServiceTypes(List arr);

        /** Find the service type implemented as a given class.
         * The whole registry is searched for a service type of that exact class (subclasses do not count).
        * <P>
        * This could be used during (de-)serialization
        * of a service type: only store its class name
        * and then try to find the type implemented by that class later.
        *
        * @param clazz the class of the service type looked for
        * @return the desired type or <code>null</code> if it does not exist
         * @deprecated Just use lookup.
        */
        @Deprecated
        public ServiceType find(Class clazz) {
            Enumeration<ServiceType> en = services();

            while (en.hasMoreElements()) {
                ServiceType o = en.nextElement();

                if (o.getClass() == clazz) {
                    return o;
                }
            }

            return null;
        }

        /** Find a service type of a supplied name in the registry.
        * <P>
        * This could be used during (de-)serialization
        * of a service type: only store its name
        * and then try to find the type later.
        *
        * @param name (display) name of service type to find
        * @return the desired type or <code>null</code> if it does not exist
        */
        public ServiceType find(String name) {
            Enumeration<ServiceType> en = services();

            while (en.hasMoreElements()) {
                ServiceType o = en.nextElement();

                if (name.equals(o.getName())) {
                    return o;
                }
            }

            return null;
        }
    }

    /** Handle for a service type. This is a serializable class that should be used
    * to store types and to recreate them after deserialization.
    * @deprecated The prefered way to register and lookup services
    *   is now {@link Lookup} as described in <a href="util/doc-files/api.html#lookup">
    *   services registration and lookup</a> page.
    */
    @Deprecated
    public static final class Handle extends Object implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 7233109534462148872L;

        /** name executor */
        private String name;

        /** name of class of the executor */
        private String className;

        /** kept ServiceType may be <tt>null</tt> after deserialization */
        private transient ServiceType serviceType;

        /** Create a new handle for an service.
        * @param ex the service to store a handle for
        */
        public Handle(ServiceType ex) {
            name = ex.getName();
            className = ex.getClass().getName();
            serviceType = ex;
        }

        /** Find the service for this handle.
        * @return the reconstituted service type, or <code>null</code> in case of problems
        */
        public ServiceType getServiceType() {
            if (serviceType == null) {
                // the class to search for
                Class<? extends ServiceType> clazz;

                // the first subclass of ServiceType to search for
                Class<?> serviceTypeClass;

                // try to find it by class
                try {
                    serviceTypeClass = Class.forName(className, true, Lookup.getDefault().lookup(ClassLoader.class));
                    clazz = serviceTypeClass.asSubclass(ServiceType.class);

                    while (serviceTypeClass.getSuperclass() != ServiceType.class) {
                        serviceTypeClass = serviceTypeClass.getSuperclass();
                    }
                } catch (ClassNotFoundException ex) {
                    // #32140 - do not notify user about this exception. This exception
                    // should be only thrown when module providing the service
                    // was uninstalled and in that case the exception must be ignored.
                    err.log(Level.FINE, "Service not found", ex); //NOI18N

                    // nothing better to use
                    clazz = ServiceType.class;
                    serviceTypeClass = ServiceType.class;
                }

                // try to find the executor by name
                ServiceType.Registry r = Lookup.getDefault().lookup(ServiceType.Registry.class);
                Enumeration<? extends ServiceType> en = r.services(clazz);
                ServiceType some = r.find(clazz);

                while (en.hasMoreElements()) {
                    ServiceType t = en.nextElement();

                    if (!serviceTypeClass.isInstance(t)) {
                        // ignore non instances
                        continue;
                    }

                    String n = t.getName();

                    if ((n != null) && n.equals(name)) {
                        return t;
                    }

                    // remember it for later use
                    if ((some == null) || ((some.getClass() != clazz) && (t.getClass() == clazz))) {
                        // remember the best match
                        some = t;
                    }
                }

                // if clazz does not exist and there is no service with same name -> return null
                if (serviceTypeClass == ServiceType.class) {
                    return null;
                }

                return some;
            }

            return serviceType;
        }

        /** Old compatibility version.
        */
        private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            name = (String) ois.readObject();

            String clazz = (String) ois.readObject();
            className = (clazz == null) ? null : org.openide.util.Utilities.translate(clazz);
        }

        /** Has also save the object.
        */
        private void writeObject(ObjectOutputStream oos)
        throws IOException {
            oos.writeObject(name);
            oos.writeObject(className);
        }

        // for debugging purposes
        public String toString() {
            return "Handle[" + className + ":" + name + "]"; // NOI18N
        }
    }
}
