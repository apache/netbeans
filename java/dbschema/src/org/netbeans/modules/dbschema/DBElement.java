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

package org.netbeans.modules.dbschema;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.Collator;

/** Base class for representations of elements corresponding to the database metadata.
 */
public abstract class DBElement implements Comparable, DBElementProperties {

    /** Implementation */
    Impl impl;

    /** Default constructor.
     */
    public DBElement() {
    }

    /** Creates a new element with the provided implementation. The implementation
     * is responsible for storing all properties of the object.
     * @param impl the implementation to use
     */
    protected DBElement(Impl impl)    {
        this.impl = impl;
        impl.attachToElement(this);
    }

    /** Returns the implementation of the element.
     * @return implementation for the element
     */
    public final Impl getElementImpl() {
        return impl;
    }

   /** Sets the implementation factory of this database element.
     * This method should only be used internally and for cloning
     * and archiving.
     * @param impl the implementation to use
     */
    public void setElementImpl (DBElement.Impl anImpl) {
        impl = anImpl;

        if (impl != null)
            impl.attachToElement(this);
    }

    /** Gets the name of this element.
     * @return the name
     */
    public DBIdentifier getName() { //cannot be final because of overriding in ColumnPairElement
        DBIdentifier name = getElementImpl().getName();
        
        return name;
    }
    
    /** Sets the name of this element.
    * @param name the name
    * @throws DBException if impossible
    */
    public final void setName(DBIdentifier name) throws DBException {
        getElementImpl().setName(name);
    }

    /** Add a property change listener.
     * @param l the listener to add
     * @see DBElementProperties
     */
    public final void addPropertyChangeListener(PropertyChangeListener l)    {
        getElementImpl().addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
     * @param l the listener to remove
     * @see DBElementProperties
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        getElementImpl().removePropertyChangeListener(l);
    }

    /** Gets a string representation of the element.
     * @return the string
     */
    public String toString() {
        return getName().toString();
    }

    /** Compares two elements.
     * @param obj the reference object with which to compare.
     * @return the value 0 if the argument object is equal to
     * this object; -1 if this object is less than the object
     * argument; and 1 if this object is greater than the object argument.
     * Null objects are "smaller".
     */
    public int compareTo(Object obj) {
        // null is not allowed
        if (obj == null)
            throw new ClassCastException();
        if (obj == this)
            return 0;

        String thisName = getName().getFullName();
        String otherName = ((DBElement) obj).getName().getFullName();
        
        if (thisName == null)
            return (otherName == null) ? 0 : -1;
            
        if (otherName == null)
            return 1;
            
        int ret = Collator.getInstance().compare(thisName, otherName);
        // if both names are equal, both objects might have different types.
        // If so order both objects by their type names 
        // (necessary to be consistent with equals)
        if ((ret == 0) && (getClass() != obj.getClass()))
            ret = getClass().getName().compareTo(obj.getClass().getName());
        
        return ret;
    }
    
    /** Indicates whether some other object is "equal to" this one.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;

        // check for the right class and then do the name check
        // by calling compareTo.
        return (getClass() == obj.getClass()) && (compareTo(obj) == 0);
    }
    
    /** Returns a hash code value for the element.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (getName() != null && getName().getFullName() != null) ? getName().getFullName().hashCode() : 0;
    }
    
    
    /** Pluggable implementation of the storage of element properties.
    * @see DBElement#DBElement
    */
    public interface Impl {
        /** Add some items. */
        public static final int ADD = 1;
        /** Remove some items. */
        public static final int REMOVE = -1;
        /** Set some items, replacing the old ones. */
        public static final int SET = 0;

        /** Called to attach the implementation to a specific
         * element. Will be called in the element's constructor.
         * Allows implementors of this interface to store a reference to the
         * holder class, useful for implementing the property change listeners.
         *
         * @param element the element to attach to
         */
        public void attachToElement(DBElement el);

        /** Get the name of this element.
         * @return the name
         */
        public DBIdentifier getName();

        /** Set the name of this element.
         * @param name the name
         * @throws DBException if impossible
         */
        public void setName(DBIdentifier name) throws DBException;

        /** Add a property change listener.
        * @param l the listener to add
        */
        public void addPropertyChangeListener(PropertyChangeListener l);

        /** Remove a property change listener.
         * @param l the listener to remove
         */
        public void removePropertyChangeListener(PropertyChangeListener l);
    }

    /** Default implementation of the Impl interface.
     * It just holds the property values.
     */
    static abstract class Memory implements DBElement.Impl {
        /** the element for this implementation */
        protected DBElement _element;

        /** Name of this element */
        private DBIdentifier _name;

        /** Property change support */
        private PropertyChangeSupport support;

        /** Constructor */
        public Memory() {
            super();
        }

        /** Copy */
        public Memory(DBElement el) {
            super();
            _name = el.getName();
        }

        /** Attaches to element */
        public void attachToElement(DBElement element) {
            _element = element;
        }

        /** Getter for name of the element.
         * @return the name
         */
        public final synchronized DBIdentifier getName() {
            if (_name == null)        // lazy initialization !?
                _name = DBIdentifier.create(""); //NOI18N

            return _name;
        }

        /** Setter for name of the element.
         * @param name the name of the element
         */
        public synchronized void setName(DBIdentifier name) {
            DBIdentifier old = _name;

            _name = name;
            firePropertyChange(PROP_NAME, old, name);
        }

        /** Fires property change event.
         * @param name property name
         * @param o old value
         * @param n new value
         */
        protected final void firePropertyChange(String name, Object o, Object n) {
            if (support != null)
                support.firePropertyChange(name, o, n);
        }

        /** Adds property listener */
        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            if (support == null)
                synchronized (this) {
                    // new test under synchronized block
                    if (support == null)
                        support = new PropertyChangeSupport(_element);
                }

            support.addPropertyChangeListener(l);
        }

        /** Removes property listener */
        public void removePropertyChangeListener (PropertyChangeListener l) {
            if (support != null)
                support.removePropertyChangeListener(l);
        }
    }
}
