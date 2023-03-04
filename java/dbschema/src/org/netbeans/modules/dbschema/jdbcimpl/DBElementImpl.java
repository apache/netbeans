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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.beans.*;

import org.netbeans.modules.dbschema.*;

abstract class DBElementImpl implements DBElement.Impl, DBElementProperties {

	/** Element */
	DBElement element;

    protected DBIdentifier _name;

	/** Property change support */
	transient private PropertyChangeSupport support;

    /** Creates new DBElementImpl */
	public DBElementImpl () {
	}

	/** Creates new DBElementImpl with the specified name */
    public DBElementImpl (String name) {
		if (name != null)
	        _name = DBIdentifier.create(name);
	}

    /** Called to attach the implementation to a specific
    * element. Will be called in the element's constructor.
    * Allows implementors of this interface to store a reference to the
    * holder class, useful for implementing the property change listeners.
    *
    * @param element the element to attach to
    */
    public void attachToElement(DBElement el) {
        element = el;
    }
  
    /** Get the name of this element.
    * @return the name
    */
    public DBIdentifier getName() {
        return _name;
    }

    /** Set the name of this element.
    * @param name the name
    * @throws DBException if impossible
    */
    public void setName(DBIdentifier name) throws DBException {
        _name = name;
    }
    
    protected boolean comp(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            if (obj1 == obj2)
                return true;
        } else
            if (obj1.equals(obj2))
                return true;
            
        return false;
    }
  
	/** Fires property change event.
	 * @param name property name
	 * @param o old value
	 * @param n new value
	 */
	protected final void firePropertyChange (String name, Object o, Object n)	{
		if (support != null)
			support.firePropertyChange(name, o, n);
	}
  
    /** Add a property change listener.
    * @param l the listener to add
    */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		if (support == null)
			synchronized (this)  {
				// new test under synchronized block
				if (support == null)
					support = new PropertyChangeSupport(element);
			}

		support.addPropertyChangeListener(l);
    }
  
    /** Remove a property change listener.
    * @param l the listener to remove
    */
    public void removePropertyChangeListener(PropertyChangeListener l) {
		if (support != null)
			support.removePropertyChangeListener(l);
    }
}
