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


package org.openidex.search;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;


/**
 * Class which groups individual search types. It provides several services
 * to provide search on them. The services are scanning node system to
 * provide search object for group of search types -> efficient search.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 */
public abstract class SearchGroup {

    /**
     * Property name which is fired when performing search and searched object 
     * passed criteria.
     */
    public static final String PROP_FOUND = "org.openidex.search.found"; // NOI18N

    /**
     * Property name which is fired for in for the case original <code>node</code>'s has
     * changed the way <code>result</code> was changed based on set criteria.
     * Interested listeners should then get the event with values 
     * <UL>
     * <LI>property change name = PROP_RESULT
     * <LI>property source = this search type instance
     * <LI>old value = detail which was changed or <code>null</code> there wasn't before for the node -> new value has to be non-null
     * for the latter case.
     * <LI>new value = detail which was changed or null if the node was removed from the result -> old value has to be non-null
     * for that case
     * </UL>
     * This allows implementation of the dynamic changing of result suggested
     * by Jesse and Sebastian (at least partially implemented now).
     */
    public static final String PROP_RESULT = "org.openidex.search.result"; // NOI18N
    
    
    /** Property change support. */
    private PropertyChangeSupport propChangeSupport;

    /** search types added to this search group */
    protected SearchType[] searchTypes = new SearchType[0];

    /** Set of nodes on which sub-system to search. */
    protected final Set<Node> searchRoots = new HashSet<Node>(5);
    
    /** Set of objects which passed the search criteria (searchtypes).*/
    protected final Set<Object> resultObjects = new LinkedHashSet<Object>(50);

    /** Flag indicating the search should be stopped. */
    protected volatile boolean stopped = false;

    private PropertyChangeListener propListener;   


    /**
     * Adds a search type to this search group.
     * If the group already contains the search type, the group is left
     * unmodified.
     *
     * @param  searchType  search type to be added
     */
    protected void add(SearchType searchType) {

        /* Check whether the search type is already in the list: */
        for (int i = 0; i < searchTypes.length; i++) {
            if (searchType.equals(searchTypes[i])) {
                return;
            }
        }
        
        /* Add the search type to the list: */
        SearchType[] temp = new SearchType[searchTypes.length + 1];
        System.arraycopy(searchTypes, 0, temp, 0, searchTypes.length);
        temp[searchTypes.length] = searchType;
        searchTypes = temp;
    }

    /**
     * Returns list of search types.
     *
     * @return  search types added to this group
     * @see  #add
     */
    public SearchType[] getSearchTypes() {
        return searchTypes;
    }
    
    /**
     * Sets roots of nodes in which its interested to search. 
     * This method is called at the first search type in the possible created
     * chain of search types.
     */
    public void setSearchRootNodes(Node[] roots) {
        
        /*
         * Gives a chance for individual search types to exclude some
         * node systems. E.g. CVS search type is not interested
         * in non CVS node systems.
         */
        for (SearchType searchType : searchTypes) {
            roots = searchType.acceptSearchRootNodes(roots);
        }
        searchRoots.clear();
        searchRoots.addAll(Arrays.asList(roots));
    }

    /** Gets search root nodes.  */
    public Node[] getSearchRoots() {
        return searchRoots.toArray(new Node[0]);
    }
    
    /** This method is invoked when the current search is being stopped.
     * 
     * You can override it to terminate any internal tasks that have been 
     * started to process individual found items and that take a long time 
     * to finish.
     * 
     * The default implementation does nothing.
     * 
     * @since  org.openidex.util/3 3.31
     */
    protected void onStopSearch() {
    }
    
    /** Stops searching. */
    public final void stopSearch() {
        stopped = true;
        onStopSearch();
    }

    /**
     * Does search.
     *
     * @throws RuntimeException USER level annotated runtime exception
     *        on low memory condition (instead of OutOfMemoryError)
     */
    public void search() {
        resultObjects.clear();
        prepareSearch();
        doSearch();
    }

    /**
     * Prepares search.
     */
    protected void prepareSearch() {
    }

    /**
     * Provides actual search. The subclasses implementating this method should scan the node system
     * specified by <code>searchRoots</code>, extract search objects from them, add them
     * to the search object set, test over all search type items in this group,
     * in case if satisfied all it should fire <code>PROP_FOUND</code> property change and add
     * the object to <code>resultObjects</code> set.
     * The method implemenatation should call {@link #processSearchObject} method for each
     * search object in the node systems.
     */
    protected abstract void doSearch();

    /**
     * Provides search on one search object instance. The object is added to
     * set of searched objects and passed to all search types encapsulated by
     * this search group. In the case the object passes all search types is added
     * to the result set and fired an event <code>PROP_FOUND</code> about successful
     * match to interested property change listeners. 
     *
     * @param searchObject object to provide actuall test on it. The actual instance
     * has to be of type returned by all <code>SearchKey.getSearchObjectType</code>
     * returned by <code>SearchType</code> of this <code>SearchGroup</code>
     */
    protected void processSearchObject(Object searchObject) {

        /*
         * Give chance to individual search types to exclude some
         * non interesting search objects from search. E.g. Java data
         * object search will be not interested in non Java data objects.
         */
        for (int i = 0; i < searchTypes.length; i++) {
            if (!searchTypes[i].acceptSearchObject(searchObject)) {
                return;
            }
        }

        /*
         * Give chance to provide additional things.
         */
        for (int i = 0; i < searchTypes.length; i++) {
            searchTypes[i].prepareSearchObject(searchObject);
        }
        
        /* Actually test the search object against all search types. */
        for (int i = 0; i < searchTypes.length; i++) {
            if (!searchTypes[i].testObject(searchObject)) {
                return;
            }
        }

        /*
         * In case the search object passed the search add it to the result set
         * and fire an event about successful search to interested listeners.
         */
        resultObjects.add(searchObject);
        firePropertyChange(PROP_FOUND, null, searchObject);
    }

    
    /** Gets node for found object. */
    public abstract Node getNodeForFoundObject(Object object);

    /** Getter for result object property. */
    public Set<Object> getResultObjects() {
        return new LinkedHashSet<Object>(resultObjects);
    }

    /** Adds property change listener. */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertySupport().addPropertyChangeListener(l);
    }
    
    /** Removes property change listener. */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        getPropertySupport().removePropertyChangeListener(l);
    }

    /** Fires property change event. */
    protected void firePropertyChange(String name, Object oldValue, Object newValue) {
        getPropertySupport().firePropertyChange(name, oldValue, newValue);
    }
    
    /** Gets lazy initialized property change support. */
    private synchronized PropertyChangeSupport getPropertySupport() {
        if(propChangeSupport == null)
            propChangeSupport = new PropertyChangeSupport(this);
        
        return propChangeSupport;
    }

    /**
     * Creates a search group for each type of object searchable by all
     * the specified search types.
     * <p>
     * At first, a set of object types common to all search types
     * (i.e. <code>Class</code>s representing object types, common
     * to all search types) is computed. Then a search group is created
     * for each of the <code>Class</code>s.
     *
     * @param items search types to create search groups for
     * @return  created search groups
     * @see  SearchType#getSearchTypeClasses()
     */
    public static SearchGroup[] createSearchGroups(SearchType[] items) {

        /*
         * Build a list of Class's searchable by every search type
         * from the specified list of search types.
         * In other words: Build a list of Class'es common to all search types.
         */
        Set<Class> classSet = new HashSet<Class>(items.length);
        for (int i = 0; i < items.length; i++) {
            List<Class> classes = Arrays.asList(items[i].getSearchTypeClasses());
            if (i == 0) {
                classSet.addAll(classes);
            } else {
                classSet.retainAll(classes);
            }
        }

        /* Try to create a search group for each of the Class'es: */
        if (classSet.isEmpty()) {
            return new SearchGroup[0];
        }
        Set<SearchGroup> groupSet = new HashSet<SearchGroup>(classSet.size());
        for (Class clazz : classSet) {
            SearchGroup group = Registry.createSearchGroup(clazz);
            if (group != null) {
                for (SearchType item : items) {
                    group.add(item);
                }
                groupSet.add(group);
            }
        }
        return groupSet.toArray(new SearchGroup[0]);
    }


    /**
     * Factory which creates <code>SearchGroup</code>. It's used in
     * <code>Registry</code> 
     * @see SearchGroup.Registry
     */
    public interface Factory {
        /** Creates new <code>SearchGroup</code> object. */
        public SearchGroup createSearchGroup();
    } // End of interface Factory.

    
    /**
     * Registry which registers search group factories
     * ({@link SearchGroup.Factory}) for search object types.
     * <p>
     * Initially, factories for search object types {@link DataObject}
     * and {@link FileObject} are already registered
     * (<code>DataObjectSearchGroup</code> and
     * <code>FileObjectSearchGroup</code>).
     *
     * @see SearchGroup.Factory
     * @see DataObjectSearchGroup
     * @see FileObjectSearchGroup
     */
    public static final class Registry extends Object {

        /** Private constructor so nobody could access it. */
        private Registry() {}
        
        
        /** Maps search object types to registered factories. */
        private static final Map<Class, Factory> registry
                = new HashMap<Class, Factory>(2);

        static {
            registry.put(DataObject.class, new Factory() {
                public SearchGroup createSearchGroup() {
                    return new DataObjectSearchGroup();
                }
            });
            registry.put(FileObject.class, new Factory() {
                public SearchGroup createSearchGroup() {
                    return new FileObjectSearchGroup();
                }
            });
        }

        
        /**
         * Registers a search group factory for a search object type
         * (<code>Class</code>).
         * If a factory has already been registered for the specified
         * search object type, the old registration is kept (the registration
         * fails).
         *
         * @param  searchObjectClass  search object type the factory is
         *                            to be registered for
         * @param  factory  factory to be registered
         * @return  <code>true</code> if the registration was successful,
         *          <code>false</code> if the registration failed
         *          (i.&nbsp;e. if some factory has already been registered
         *          for the specified search object type)
         */
        public static synchronized boolean registerSearchGroupFactory(
                Class searchObjectClass,
                Factory factory) {
            Factory oldFactory = registry.put(searchObjectClass, factory);
            if (oldFactory != null) {
                
                /* 
                 * Oops! A factory for the specified search object class
                 * have already been registered. Retain the old registration:
                 */
                registry.put(searchObjectClass, oldFactory);
                return false;
            }
            return true;
        }
        
        /**
         * Creates a <code>SearchGroup</code> for the specified search object
         * type.
         * The search group is created using
         * the {@linkplain SearchGroup.Factory factory} registered for
         * the specified search object type.
         *
         * @param  searchObjectType  search object type to create
         *                           a search group for
         * @return  search group created by the registered factory,
         *          or <code>null</code> if no factory has been registed
         *          for the specified search object type
         * @see  #registerSearchGroupFactory registerSearchGroupFactory
         */
        public static SearchGroup createSearchGroup(Class searchObjectType) {
            Factory factory = registry.get(searchObjectType);
            
            if (factory == null) {
                return null;
            }
            return factory.createSearchGroup();
        }

        /**
         * Tests whether there is a <code>Factory</code> registered for the
         * specified search object class type.
         *
         * @param  searchObjectType  search object type
         * @return  <code>true</code> if some factory has been registered
         *          for the specified search object type,
         *          <code>false</code> otherwise
         */
        public static boolean hasFactory(Class searchObjectType) {
            return registry.containsKey(searchObjectType);
        }
        
    } // End of class Registry.
   
}
