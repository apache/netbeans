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


package org.openidex.search;

import java.util.Collections;
import java.util.Enumeration;

import org.openide.ServiceType;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


/**
 * Search type is service which provides search functionality on set of nodes.
 * It has to provide GUI presentation so user can have the possibility to
 * set/modify criteria.
 * It performs search according to that.
 *
 * @author  Peter Zavadsky
 */
public abstract class SearchType extends ServiceType implements Cloneable {

    /** Serial version UID. */ // PENDING How to change this silly number? Can be done by using Utilities.translate
    static final long serialVersionUID = 1L;
    
    /** Name of valid property. */
    public static final String PROP_VALID = "valid"; // NOI18N
    
    /** Name of object changed property. */
    protected static final String PROP_OBJECT_CHANGED = "org.openidex.search.objectChanged"; // NOI18N
    
    /** Property valid. */
    private boolean valid;

    
    /** Class types of object on which this search type is able to search. */
    private Class[] searchTypeClasses;
    

    /**
     * Gets class types of objects this search type can search (test) on.
     * The classes are used for associating search types working on the same
     * object types to create <code>SearchGroup</code>. 
     * <em>Note: </em> the order of classes declares also priority.
     */
    public final synchronized Class[] getSearchTypeClasses() {
        if (searchTypeClasses == null) {
            searchTypeClasses = createSearchTypeClasses();
        }
        return searchTypeClasses;
    }

    /**
     * Actually creates array of class types of objects this search type can search.
     * <em>Note: </em> the order of classes declares also priority.
     */
    protected abstract Class[] createSearchTypeClasses();
    

    /**
     * Accepts search root nodes. Subclasses have a chance to exclude some of
     * the non interesting node systems. E.g. CVS search type can exclude non
     * CVS node systems.
     */
    protected Node[] acceptSearchRootNodes(Node[] roots) {
        return roots;
    }

    /**
     * Accepts search object to the search. Subclasses have a chance to exclude
     * the non interesting objects from the search. E.g. Java search type will
     * exclude non Java data objects.
     * <em>Note:</em> the search object instance is of the class type
     * returned by SearchKey.getSearchObjectType method. So there is no necessity
     * to do additional check for that search type. 
     * @return <code>true</code> */
    protected boolean acceptSearchObject(Object searchObject) {
        return true;
    }

    /**
     * Prepares search object. Dummy implementation.
     */
    protected void prepareSearchObject(Object searchObject) {}
    
    /**
     * Checks whether an object matches the criteria defined in this search
     * type.
     *
     * @param  searchObject  object to be tested
     * @return  <code>true</code> if the object matches the criteria,
     *          <code>false</code> it it does not
     */
    protected abstract boolean testObject(Object searchObject);

    /**
     * Creates nodes representing matches found within the specified object.
     * <p>
     * This is a dummy implementation, subclasses should provide a real
     * implementation.
     *
     * @param  resultObject  object to create the nodes for
     * @return  <code>null</code> (subclasses should return the created nodes)
     */
    public Node[] getDetails(Object resultObject) {
        return null;
    }
    
    /**
     * Creates nodes representing matches found withing an object
     * represented by the specified node.
     * <p>
     * This is a dummy implementation, subclasses should provide a real
     * implementation. The typical implementation is that the node is validated,
     * an object is extracted from it and passed to method
     * {@link #getDetails(Object)}.
     *
     * @param  node  node representing object with matches
     * @return <code>null</code> (subclasses should return the created nodes)
     * @see  #getDetails(Object)
     */
    public Node[] getDetails(Node node) {
        return null;
    }

    /**
     * Checks that this search type is able to search the specified set
     * of nodes.
     * <p>
     * This method is usually implemented such that it returns <code>true</code>
     * if it is possible to search at least one of the nodes.
     *
     * @param  nodes  nodes to be searched
     * @return  <code>true</code> if this search type is able to search
     *          the nodes, <code>false</code> otherwise
     */
    public abstract boolean enabled(Node[] nodes);
    
    /** Now the custonized criterion changed validity state. */
    public final void setValid(boolean state) {
        boolean old = valid;
        valid = state;
        firePropertyChange(PROP_VALID, Boolean.valueOf(old), Boolean.valueOf(state));
    }

    /** @return true if the criterion is currently valid. */
    public final boolean isValid() {
        return valid;
    }
    
    /** Clones seach type. */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("SearchType must be cloneable."); // NOI18N
        }
    }


    /**
     * Enumeration of all SearchTypes in the system.
     *
     * @return enumeration of SearchType instances
     * @deprecated Please use {@link Lookup} instead.
     */
    @Deprecated
    public static Enumeration<? extends SearchType> enumerateSearchTypes () {
        return Collections.enumeration(Lookup.getDefault().lookup(
                new Lookup.Template<SearchType>(SearchType.class)).allInstances());
    }
    
}
