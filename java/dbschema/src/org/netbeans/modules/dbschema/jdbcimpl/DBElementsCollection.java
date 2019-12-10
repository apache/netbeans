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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.util.*;

import org.netbeans.modules.dbschema.*;

/** Support class that manages set of objects and fires events
 * about its changes.
 */
public class DBElementsCollection implements DBElementProperties {

    /** Object to fire info about changes to */
    DBElementImpl owner;

    DBElement[] _elms;

    /** Array template for typed returns */
    private Object[] _template;

    //workaround for bug #4396371
    //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
    protected static transient HashSet instances = new HashSet();
    
	public DBElementsCollection () {
        this(null, null);
    }

    /**
     * @param owner owner of this array at which to fire changes
     */
    DBElementsCollection (DBElementImpl owner, Object[] template) {
        this.owner = owner;
        _template = template;
    }

    public DBElement[] getElements () {
        if (_elms != null)
            return _elms;
        else
            return Arrays.asList(_template).toArray(new DBElement[_template.length]);

    }
    
    public void changeElements(DBElement[] elements, int action) {
            changeElements(Arrays.asList(elements), action);
    }

    public void changeElements(List elems, int action) {
        boolean changed = false;
        DBElement[] oldElements = getElements();
        int oldLength = (oldElements == null) ? 0 : oldElements.length;
        int newLength = (elems == null) ? 0 : elems.size();
        List list = null;
            
        switch (action) {
            case DBElement.Impl.ADD:
                if (newLength > 0) {
                    list = ((oldLength == 0) ? new ArrayList() : new ArrayList(Arrays.asList(oldElements)));
                    list.addAll(elems);
                    changed = true;
                }
                break;
            case DBElement.Impl.REMOVE:
                break;
            case DBElement.Impl.SET:
                list = elems;
                changed = true;
                break;
        }
        if (changed)
            _elms = (DBElement[]) list.toArray(_template);//getEmptyArray());

    }

		/** Find method that looks in member elements
		 * @param id the identifier (or null)
		 * @param types array of types to test (or null)
		 * @return the element or null
		 */
        public DBElement find(DBIdentifier id) {
            DBElement[] me = getElements();

            if (me == null)            
                return null;
            
            for (int i = 0; i < me.length; i++)
                if (id.compareTo(me[i].getName(), false))
                    return me[i];

            return null;
        }

	//=============== extra methods needed for xml archiver ==============

	/** Returns the owner of this collection.  This method should only 
	 * be used internally and for cloning and archiving.
	 * @return the owner of this collection
	 */
	DBElementImpl getOwner () {
            return owner;
        }

	/** Set the owner of this collection to the supplied implementation.  
	 * This method should only be used internally and for cloning and 
	 * archiving.
	 * @param owner the owner of this collection
	 */
	void setOwner (DBElementImpl owner) {
            this.owner = owner;
	}

	/** Set the collection of elements maintained by this holder to the 
	 * supplied array.  This method should only be used internally and for 
	 * cloning and archiving.
	 * @param elements the collection of elements maintained by this holder
	 */
	public void setElements (DBElement[] elements)
	{
		_elms = elements;
	}
        
       /** Returns the template for the array of this collection.  This method
         * should only be used internally and for cloning and archiving.
         * @return the typed template of this collection
         */
        public Object[] getTemplate () { return _template; }

        /** Set the template for the array of this collection to the supplied
         * array.  This template is used so the array returned by getElements is
         * properly typed.  This method should only be used internally and
         * for cloning and archiving.
         * @param template the typed template of this collection
         */
        public void setTemplate (Object[] template) { _template = template; }        
}
