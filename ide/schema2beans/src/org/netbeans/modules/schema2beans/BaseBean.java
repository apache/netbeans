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

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;
import java.beans.*;

import org.w3c.dom.*;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  This class is the base class for any generated bean. This class provides
 *  the property book-keeping and access as a set of dynamic creation/accessor
 *  property methods.
 *
 *  BaseBean is the class that any generated schema2beans class extends. The
 *  generated code implements the specific accessors methods while the BaseBean
 *  class provides the generic ones. For example, a generated bean would have
 *
 *    public String getDescription() {
 *        return (String)this.getValue(DESCRIPTION);
 *    }
 *
 *  BaseBean handles any value as an Object, where the generated schema2beans
 *  class handles a specific class/scalar type depending on its corresponding
 *  DTD element (and optional mdd values. See user document for mdd info).
 *
 *  The idea of BaseBean is to provide the unique schema2beans java bean on
 *  which all generated schema2beans class rely. This is the user entry point
 *  of the schema2beans runtime. This is where you find all the generic methods
 *  that apply to all schema2beans classes: merge, find, clone, get/set attributes,
 *  etc.
 *
 *  If we had to draw a gereric overview of the schema2beans architecture, we would
 *  draw three parts: BaseBean, BeanProp and DOMBinding. This is where most
 *  of the schema2beans runtime is implemented.
 *
 *  There is one BaseBean instance per schema2beans node (one non final element
 *  of the DTD has one generated class that extends BaseBean).
 *  Each BaseBean holds its properties as BeanProp objects. For each property,
 *  BaseBean has one instance of BeanProp. For example, if we have as a DTD:
 *
 *  <!ELEMENT book (chapter+, summary?)
 *  <!ELEMENT summary (#PCDATA)>
 *  <!ELEMENT line (#PCDATA)>
 *  <!ELEMENT chapter (line*)>
 *
 *  The we would have two classes that would extend BaseBean: Book and Chapter.
 *  The book BaseBean would handle two instance of BeanProp, one for the
 *  property chapter and one of summary.
 *  The BeanProp for summary would handle a single String property, and the
 *  BeanProp for chapter would handle an indexed property of Chapter classes.
 *  Also, the chapter BaseBean (one instance for each element of the indexed
 *  property), would have one instance of BeanProp, that would handle the
 *  String indexed property line.
 *
 *  The internal graph representation of the XML document is handled using
 *  a DOM tree. DOMBinding is the class that takes care of linking the
 *  BeanProp instances to the DOM nodes.
 *
 */
public abstract class BaseBean implements Cloneable, Bean {
    
    /*
     *	Because schema2beans doesn't generate user information about the
     *	relationship between the properties, BaseBean provides some
     *	method to give the user this information.
     */
    public class IterateChoiceProperties implements java.util.Iterator {
        private ArrayList groups;
        private int index;
        
        public IterateChoiceProperties() {
            this.groups = new ArrayList();
            this.index = 0;
        }
        
        void add(BeanProp prop) {
            if (prop.group != null && !this.groups.contains(prop.group))
                this.groups.add(prop.group);
        }
        
        public boolean hasNext() {
            return (this.index < this.groups.size());
        }
        
        public Object next() throws NoSuchElementException {
            if (hasNext()) {
                BeanProp.GroupProp gp =
                        (BeanProp.GroupProp)this.groups.get(this.index++);
                return (BaseProperty[])gp.list();
            } else
                throw new NoSuchElementException();
        }
        
        public void remove()
        throws UnsupportedOperationException, IllegalStateException {
            throw new UnsupportedOperationException();
        }
    }
    
    //	The binding object that links us to the DOM node. This might be null
    //	if there is no DOM Node yet (brand new object not attached to a graph)
    protected DOMBinding 		binding;
    
    //	A unique instance for all the nodes of the schema2beans tree
    protected GraphManager 		graphManager;
    
    //
    //	HashMaps of the properties - those are the same properties but are
    //	sorted by Bean name (propByName) and by dtd name (propByDtdName).
    //
    private Map 			propByName;
    private Map 			propByOrder;
    
    //
    //	If we use this hashMap, we have better performances whenever we
    //	access the element using the dtd name. Since schema2beans generate classes
    //	using Bean names, we can assume that very few accesses are performed
    //	using the dtd name and we can then save memory commenting out this
    //	hashMap. If performance is an issue while accessing the element using
    //	dtd names, we should consider using this hashmap again.
    //
    //private HashMap 			propByDtdName;
    
    //	When an attribute is added, we might not be part of a graph yet.
    //  In this case, we want to cache the attribute information and defer
    //	its processing. Whenever the node is added to a schema2beans tree,
    //	the values of the cache are automatically check to populate
    //	the attribute values.
    private HashMap			attrCache;
    
    //	We can define an array of comparator, even though most of the usage
    //	is to have one comparator.
    private ArrayList			comparators;
    
    // This is a provision to mark at runtime the version of each node
    // of the schema2beans tree. As this is not of much use for now, we simply
    // save the memory space.
    //private Version 		version;
    
    //	True if this node is the root of the schema2beans graph.
    private boolean 			isRoot;
    
    //	When the properties are created (calls from the generated class
    //	constructor), we give a number to each property so we know what
    //	their order is. This is how we know, when we add a new property
    //	where it should be added (it has to match the DTD order declaration)
    private int				propertyOrder;
    
    private String			defaultNamespace;
    
    /*
    public BaseBean() {
        this(null, new Version(Version.MAJVER, Version.MINVER,
                               Version.PTCVER));
        System.out.println("warning: schema2beans.BaseBean: unknown version of generated beans being used.");
    }
     */
    
    /**
     * @param comps the comparators to use.  Can be null
     * @param version which runtime version to be compatible with.  This should be the version of the generated beans.
     */
    public BaseBean(Vector comps, Version version) {
        init(comps, version);
    }
    
    protected void init(Vector comps, Version version) {
        if (version.getMajor() < 3) {
            initPropertyTables(13);
        }
        this.comparators = new ArrayList(2);
        //this.version = version;
        this.isRoot = false;
        this.propertyOrder = 0;
        this.attrCache = null;
        
        if ((comps == null) || (comps.size()==0)) {
            //	Use the default comparator
            this.comparators.add(new BeanComparator());
        } else {
            int size = comps.size();
            for (int i=0; i<size; i++)
                this.comparators.add(comps.get(i));
        }
    }
    
    protected void initPropertyTables(int propertyCount) {
        //
        // In order to avoid a rehash, the initial capacity of a HashMap
        // should be > the number of expected elements / load factor.
        // If we make the load factor=1, then the initial capacity could be
        // just the expected elements + 1.  However, if 2 elements map to
        // the same bucket, then they start getting strung together in
        // a list, and that slows things down (you lose the O(1) performance).
        // So, double the expected size to make it less likely that 2
        // elements will map to the same bucket.
        //
        int hashTableSize = propertyCount * 2;
        this.propByName = new HashMap(hashTableSize, 1.0f);
        this.propByOrder = new HashMap(hashTableSize, 1.0f);
        //this.propByDtdName = new HashMap(hashTableSize, 1.0f);
    }
    
    //
    //	Dynamically add/remove a comparator. Comparator are used to compare
    //	and merge graphs. There are usually populated in the constructor
    //
    public synchronized void addBeanComparator(BeanComparator cmp) {
        if (cmp != null)
            this.comparators.add(cmp);
    }
    
    public synchronized void removeBeanComparator(BeanComparator cmp) {
        int i = this.comparators.indexOf(cmp);
        if (i != -1)
            this.comparators.remove(i);
    }
    
    
    /**
     *	Create a new property for the current bean, selecting default option
     *	values.
     */
    public void createProperty(String dtdName, String beanName, Class type) {
        int o = Common.TYPE_0_1;
        
        if (type.isInstance(java.lang.String.class))
            o |= Common.TYPE_STRING;
        else
            o |= Common.TYPE_BEAN;
        
        this.createProperty(dtdName, beanName, o, type);
    }
    
    /**
     *	Create the root element of the graph
     */
    public void createRoot(String dtdName, String beanName,
            int option, Class type) throws Schema2BeansRuntimeException {
        BeanProp prop = new BeanProp(this, dtdName, beanName,
                option, type, true);
        try {
            this.graphManager.createRootBinding(this, prop, null);
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        }
        this.isRoot = true;
    }
    
    /**
     *	Create a new property for the current bean. This creates a BeanProp
     *	object (which is the internal representation of a single/indexed
     *	property) and adds it to the bean property hash table.
     *
     *	This property is later accessed by its bean name.
     */
    public void createProperty(String dtdName, String beanName,
            int option, Class type) throws
            Schema2BeansRuntimeException {
        
        //  This number represents the order of this property amoung
        //  its siblings. The beans generator generates the createProperty()
        //  method calls in the order of the DTD declaration. Therefore, we
        //  can simply relies on the order of the createProperty method calls
        //  to affect the order value for the property.
        this.propertyOrder++;
        
        BeanProp prop = new BeanProp(this, dtdName, beanName, option, type);
        prop.setOrder(this.propertyOrder);
        Object 	 obj1 = this.propByName.put(beanName, prop);
        //Object 	 obj2 = this.propByDtdName.put(dtdName, prop);
        this.propByOrder.put(String.valueOf(this.propertyOrder), prop);
        
        if (obj1 != null) // || obj2 != null)
            throw new Schema2BeansRuntimeException(Common.
                    getMessage("DuplicateProperties_msg"));
        
        prop.initialize();
    }
    
    public void setDefaultNamespace(String namespace) {
        defaultNamespace = namespace;
        createAttribute("xmlns", "xmlns", AttrProp.CDATA | AttrProp.IMPLIED,
                null, namespace);
        setAttributeValue("xmlns", namespace);
        if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
            createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
            createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
        }
    }
    
    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    /**
     *	Returns the list of properties of this bean as an array.
     */
    public BeanProp[] beanProps() {
        int size = this.propByOrder.size();
        BeanProp[] ret = new BeanProp[size];
        for (int i=1; i<=size; i++)
            ret[i-1] = (BeanProp)this.propByOrder.get(String.valueOf(i));
        return ret;
    }
    
    /**
     *	Returns the list of properties of this bean as an array.
     */
    protected Iterator beanPropsIterator() {
        return propByName.values().iterator();
    }
    
    /**
     *	Return the internal object representation of the property. This method
     *	cannot return null. If there is no object available for the specified
     *	property name, an exception is thrown.
     */
    public BeanProp beanProp(String name) {
        BeanProp prop = (BeanProp)this.propByName.get(name);
        
        if (prop == null) {
            //	Search using the dtd name
            String beanName = Common.convertName(name);
            prop = (BeanProp)this.propByName.get(beanName);
            
            if (prop == null)
                throw new IllegalArgumentException(Common.
                        getMessage("BeanPropertyDoesntExist_msg",
                        this.getClass().getName(), name));
        }
        return prop;
    }
    
    /**
     *	Return the internal object representation of the property. This method
     *	cannot return null. If there is no object available for the specified
     *	property name, an exception is thrown.
     */
    public BeanProp beanProp(int order) {
        return (BeanProp)this.propByOrder.get(String.valueOf(order));
    }
    
    /**
     *	Return the value of the single property named name.
     */
    public Object getValue(String name) {
        return this.beanProp(name).getValue(0);
    }
    
    /**
     *	Return one element of the indexed property named name.
     */
    public Object getValue(String name, int index) {
        return this.beanProp(name).getValue(index);
    }
    
    /**
     *	Return one element of the index property using the internal
     *	id index value. This index is unique and doesn't change over
     *	the time of the graph life. This method allows to get an element
     *	of an indexed property without keeping track of index shifting,
     *	and other elements removal.
     */
    public Object getValueById(String name, int id) {
        return this.beanProp(name).getValueById(id);
    }
    
    /**
     *	Convert an unique internal index value into the user visible
     *	index value. The property name specified must be an indexed
     *	property.
     * This method may return -1 if we cannot figure out the index.
     */
    public int idToIndex(String name, int id) {
        return this.beanProp(name).idToIndex(id);
    }
    
    /**
     *	Convert the user index value into the internal unique index value.
     *	The property name specified must be an indexed property.
     */
    public int indexToId(String name, int index) {
        return this.beanProp(name).indexToId(index);
    }
    
    /**
     *	Return true if this property is null
     */
    public boolean isNull(String name) {
        return (this.getValue(name) == null);
    }
    
    /**
     *	Return true if this property is null
     */
    public boolean isNull(String name, int index) {
        return (this.getValue(name, index) == null);
    }
    
    /**
     *	Return the values of the indexed property named name. The result
     *	can be cast as an array of the property type.
     */
    public Object[] getValues(String name) {
        return this.beanProp(name).getValues();
    }
    
    /**
     *	Set the value for the single property named name.
     */
    public void setValue(String name, Object value) {
        setValue(beanProp(name), 0, value);
    }
    
    /**
     *	Set the value of an element for the indexed property named name.
     */
    public void setValue(String name, int index, Object value) {
        setValue(beanProp(name), index, value);
    }
    
    protected void setValue(BeanProp prop, int index, Object value) {
        prop.setValue(index, value);
    }
    
    protected int addValue(BeanProp prop, Object value) {
        return prop.addValue(value);
    }
    
    protected int removeValue(BeanProp prop, Object value) {
        return prop.removeValue(value);
    }
    
    protected void removeValue(BeanProp prop, int index) {
        prop.removeValue(index);
    }
    
    /**
     *	Set the value of an element for the indexed property named name,
     *	using the unique internal index.
     */
    public void setValueById(String name, int id, Object value) {
        BeanProp bp = this.beanProp(name);
        int index = bp.idToIndex(id);
        bp.setValue(index, value);
    }
    
    /**
     *	Set the values for the indexed property named name.
     */
    public void setValue(String name, Object[] value) {
        this.beanProp(name).setValue(value);
    }
    
    /**
     *	Add a value to the indexed property named name.
     */
    public int addValue(String name, Object value) {
        return addValue(beanProp(name), value);
    }
    
    /**
     *	Remove a value from the indexed property named name.
     */
    public int removeValue(String name, Object value) {
        return removeValue(beanProp(name), value);
    }
    
    /**
     *	Remove a value from the indexed property named name.
     */
    public void removeValue(String name, int index) {
        removeValue(beanProp(name), index);
    }
    
    /**
     *	Returns the position of the indexed property element.
     *	If the type of the property is a bean, use the == comparison,
     *	else use the equals() method.
     *	If the element is not found, return -1.
     */
    public int indexOf(String name, Object value) throws
            Schema2BeansRuntimeException {
        BeanProp 	bp = this.beanProp(name);
        
        if (bp == null)
            throw new Schema2BeansRuntimeException(Common.
                    getMessage("UnknownPropertyName_msg", name));
        
        if (Common.isArray(bp.type)) {
            boolean isBean = Common.isBean(bp.type);
            
            int size = bp.size();
            for (int i=0; i<size; i++) {
                Object obj = bp.getValue(i);
                if (isBean && (obj == value))
                    return i;
                else
                    if (!isBean && (obj.equals(value)))
                        return i;
            }
        }
        return -1;
    }
    
    /**
     *	Return the size of the indexed property named name (the size
     *	might be greater than the number of elements if the indexed
     *	property array contains null elements).
     */
    public int size(String name) {
        return this.beanProp(name).size();
    }
    
    /**
     *	Return true if the property name is a choice property (defined
     *	in the DTD with the | char, such as (a | b | c ...)
     */
    public boolean isChoiceProperty(String name) {
        return this.beanProp(name).isChoiceProperty();
    }
    
    /**
     *	Equivalent to isChoiceProperty(name) on the current bean property.
     */
    public boolean isChoiceProperty() {
        return this.beanProp().isChoiceProperty();
    }
    
    /**
     *	If the property name is a choice property, returns the list of
     *	all the properties associated to this property (this one included).
     *	Return null otherwise.
     */
    public BaseProperty[] listChoiceProperties(String name) {
        return this.beanProp(name).getChoiceProperties();
    }
    
    /**
     *	Returns an iterator on the list of the sets of choice properties.
     *	Each object of the returned iterator is an array of BaseProperty.
     *	The returned value is never null be can be empty.
     */
    public Iterator listChoiceProperties() {
        IterateChoiceProperties it = new IterateChoiceProperties();
        Iterator i = beanPropsIterator();
        while (i.hasNext())
            it.add((BeanProp)i.next());
        return it;
    }
    
    /**
     *	Return the list of the properties
     */
    public BaseProperty[] listProperties() {
        return (BaseProperty[])this.beanProps();
    }
    
    /**
     *  Return the BaseProperty object for the current bean
     */
    public BaseProperty getProperty() {
        return (BaseProperty)this.beanProp();
    }
    
    /**
     *  Return the BaseProperty object for the specified property
     */
    public BaseProperty getProperty(String propName) {
        return (BaseProperty)this.beanProp(propName);
    }
    
    /**
     *	Return the known values as declared in the mdd file.
     */
    public Object[] knownValues(String name) {
        return this.beanProp(name).knownValues();
    }
    
    protected void addKnownValue(String name, Object value) {
        this.beanProp(name).addKnownValue(value);
    }
    
    /**
     *	Create a new attribute on the current bean.
     */
    public void createAttribute(String dtdName, String name, int type,
            String[] values, String defValue) {
        BeanProp bp = this.beanProp();
        if (bp != null)
            bp.createAttribute(dtdName, name, type, values, defValue);
        else
            System.out.println(Common.getMessage("beanPropIsNull_msg", name));
    }
    
    /**
     *	Create a new attribute for the property propertyName. This creates
     *	the attribute within the BeanProp associated to the property.
     */
    public void createAttribute(String propName, String dtdName, String name,
            int type, String[] values, String defValue) {
        this.beanProp(propName).createAttribute(dtdName, name, type,
                values, defValue);
    }
    
    /**
     *	Set the value of the attribute (see the BeanClass class)
     */
    public void setAttributeValue(String propName, String name,
            String value) {
        this.beanProp(propName).setAttributeValue(0, name, value);
    }
    
    /**
     *	Set the attribute value on the current property bean
     */
    public void setAttributeValue(String name, String value) {
        if (name == null)
            return;
        
        BeanProp bp = this.beanProp();
        if (bp != null) {
            //	Find out what our index is within the BeanProp object
            int i = bp.idToIndex(this.binding.getId());
            bp.setAttributeValue(i, name, value);
        } else {
            //
            //	There is no BeanProp/DOMBinding for this bean yet,
            //	cache the value.
            //
            if (this.attrCache == null)
                this.attrCache = new HashMap();
            this.attrCache.put(name, value);
        }
    }
    
    /**
     *	Return the cached attribute values (when setAttributeValue is called
     *	before a newly created bean is part of a graph, the bean has no
     *	BeanProp/DOMBinding yet and the value has to be cached, waiting
     *	the element to be inserted into the graph. Only at this point
     *	the cached attribute values are ready to be really created).
     */
    String[] cachedAttributeNames() {
        int size = (this.attrCache==null)?0:this.attrCache.size();
        String[] ret = new String[size];
        if (size >0) {
            Iterator it = this.attrCache.keySet().iterator();
            int i = 0;
            while (it.hasNext())
                ret[i++] = it.next().toString();
        }
        
        return ret;
    }
    
    /**
     *	Return the value cached for the attribute named name.
     */
    String cachedAttributeValue(String name) {
        if (this.attrCache != null)
            return (String)this.attrCache.get(name);
        else
            return null;
    }
    
    /**
     *	Return the value cached for the attribute named name.
     */
    void cachedAttributeClear() {
        this.attrCache = null;
    }
    
    /**
     *	Get the attribute value on the current property bean.
     *  If there is no current attribute (or element for that matter),
     *  then null is returned.
     */
    public String getAttributeValue(String name) {
        BeanProp bp = this.beanProp();
        if (bp != null) {
            //	Find out what our index is within the BeanProp object
            int i = bp.idToIndex(this.binding.getId());
            if (i < 0)   // I guess we're not part of the BeanProp yet.
                return null;
            return bp.getAttributeValue(i, name);
        } else {
            //
            //	That's a brand new bean not attached yet to a graph. Try
            //	to get the value from the cache.
            //
            if (this.attrCache != null)
                return (String)this.attrCache.get(name);
            else
                return null;
        }
    }
    
    /**
     *	Get the attribute value (see BeanProp class)
     */
    public String getAttributeValue(String propName, String name) {
        return this.beanProp(propName).getAttributeValue(0, name);
    }
    
    /**
     *	Set the value of the attribute (see the BeanClass class)
     */
    public void setAttributeValue(String propName, int index, String name,
            String value) {
        this.beanProp(propName).setAttributeValue(index, name, value);
    }
    
    /**
     *	Get the attribute value (see BeanProp class)
     */
    public String getAttributeValue(String propName, int index, String name) {
        return this.beanProp(propName).getAttributeValue(index, name);
    }
    
    /**
     *	Return the list of all known attribute names for this property
     *	(even if they are not set).
     */
    public String[] getAttributeNames(String propName) {
        return this.beanProp(propName).getAttributeNames();
    }
    
    /**
     *	Return the list of all known attribute names for the current bean
     */
    public String[] getAttributeNames() {
        BeanProp bp = this.beanProp();
        if (bp != null)
            return bp.getAttributeNames();
        else
            return null;
    }
    
    
    /**
     *	Return the list of all known attribute names for this property
     *	(even if they are not set).
     */
    public BaseAttribute[] listAttributes(String propName) {
        return this.beanProp(propName).getAttributes();
    }
    
    /**
     *	Return the list of all known attribute names for the current bean
     */
    public BaseAttribute[] listAttributes() {
        BeanProp bp = this.beanProp();
        if (bp != null)
            return bp.getAttributes();
        else
            return null;
    }
    
    
    //	Called by find() method. Where the attributes are searched for.
    private void lookForAttribute(ArrayList found, BeanProp bp, BaseBean bean,
            BaseAttribute[] attrs, String attrName,
            Object value) {
        
        for (int j=0; j<attrs.length; j++) {
            if (attrName == null || attrs[j].hasName(attrName)) {
                String name = attrs[j].getName();
                
                if (DDLogFlags.debug) {
                    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_UBN, 1, DDLogFlags.FINDATTR,
                            bp.getName() + "." +  name +
                            " for value " + value);
                }
                
                int size = bp.size();
                if (bp.isIndexed()) {
                    for (int k=0; k<size; k++) {
                        String v = bp.getAttributeValue(k, name);
                        v = (String)Common.getComparableObject(v);
                        
                        if (DDLogFlags.debug) {
                            TraceLogger.put(TraceLogger.DEBUG,
                                    TraceLogger.SVC_DD,
                                    DDLogFlags.DBG_UBN, 1,
                                    DDLogFlags.FINDCMP,
                                    bp.getName() + "." +
                                    name + " = " + v);
                        }
                        
                        if ((bean == null || bean == bp.getValue(k))
                        && value.equals(v)) {
                            if (DDLogFlags.debug) {
                                TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_UBN, 1,
                                        DDLogFlags.FNDATTR);
                            }
                            found.add(bp.buildFullName(k, name));
                        }
                    }
                } else {
                    String v = bp.getAttributeValue(0, name);
                    v = (String)Common.getComparableObject(v);
                    
                    if (DDLogFlags.debug) {
                        TraceLogger.put(TraceLogger.DEBUG,
                                TraceLogger.SVC_DD,
                                DDLogFlags.DBG_UBN, 1,
                                DDLogFlags.FINDCMP,
                                bp.getName() + "." +
                                name + " = " + v);
                    }
                    
                    if (value.equals(v)) {
                        if (DDLogFlags.debug) {
                            TraceLogger.put(TraceLogger.DEBUG,
                                    TraceLogger.SVC_DD,
                                    DDLogFlags.DBG_UBN, 1,
                                    DDLogFlags.FNDATTR);
                        }
                        
                        found.add(bp.buildFullName(0, name));
                    }
                }
            }
        }
    }
    
    /**
     *	Parse recursively the tree to find out a property value or attribute
     *	value.
     *
     *		propName != null & attrName == null, find for property value
     *		propName == null & attrName != null, find for attribute value
     *		propName == null & attrName == null, find for any value
     */
    void find(BaseBean bean, ArrayList found, String propName,
            String attrName, Object value) {
        
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                    DDLogFlags.DBG_UBN, 1, DDLogFlags.FIND,
                    (bean==null?"<null>":bean.getClass().getName()) +
                    " - " + propName + "/" + attrName + " for value " +
                    ((value == null)?"<null>":value.toString()));
        }
        
        if (bean == null || value == null)
            return;
        
        BaseProperty[] props = bean.listProperties();
        
        //
        //  Search our own attributes first (as any node might be the
        //  root of the search, we have to start by ourself first)
        //
        BaseAttribute[] attrs = bean.listAttributes();
        if (propName == null && attrs != null && attrs.length > 0) {
            BeanProp bp = bean.beanProp();
            this.lookForAttribute(found, bp, bean, attrs, attrName, value);
        }
        
        //
        //  Look for the properties and the attributes of the non-bean
        //  properties (bean property attributes are searched as the root
        //  case explained above)
        //
        for (int i=0; i<props.length; i++) {
            BaseProperty 	p = props[i];
            String  	 	name = p.getName();
            BeanProp	 	bp = (BeanProp)p;
            int  		size = p.size();
            
            //
            //	Skip if this is a node (bean): we do not try to look for
            //	a node (we are searching a final value), and the attributes
            //	of a bean are searched at the beginning of the method.
            //
            if (!p.isBean()) {
                // Prop name & size
                if (((propName != null && p.hasName(propName)) ||
                        (propName == null && attrName == null))) {
                    if (DDLogFlags.debug) {
                        TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                                DDLogFlags.DBG_UBN, 1,
                                DDLogFlags.FINDPROP,
                                name + " for value " + value);
                    }
                    
                    if (p.isIndexed()) {
                        //	Search for a specific one
                        for (int j=0; j<size; j++) {
                            Object v = bp.getValue(j);
                            v = Common.getComparableObject(v);
                            
                            if (DDLogFlags.debug) {
                                TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_UBN, 1,
                                        DDLogFlags.FINDCMP,
                                        name + "[" + j + "] = " +
                                        v.toString());
                                
                            }
                            if (value.equals(v)) {
                                if (DDLogFlags.debug)
                                    TraceLogger.put(TraceLogger.DEBUG,
                                            TraceLogger.SVC_DD,
                                            DDLogFlags.DBG_UBN, 1,
                                            DDLogFlags.FNDPROP);
                                
                                found.add(bp.getFullName(j));
                            }
                        }
                    } else {
                        Object v = bp.getValue(0);
                        v = Common.getComparableObject(v);
                        
                        if (DDLogFlags.debug) {
                            TraceLogger.put(TraceLogger.DEBUG,
                                    TraceLogger.SVC_DD,
                                    DDLogFlags.DBG_UBN, 1,
                                    DDLogFlags.FINDCMP,
                                    name + " = " +
                                    ((v==null)?"null":v.toString()));
                        }
                        
                        if (value.equals(v)) {
                            if (DDLogFlags.debug)
                                TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_UBN, 1,
                                        DDLogFlags.FNDPROP);
                            
                            found.add(bp.getFullName());
                        }
                    }
                }
                
                // Prop attributes
                attrs = p.getAttributes();
                if (propName == null && attrs.length > 0)
                    this.lookForAttribute(found, bp, null, attrs,
                            attrName, value);
            }
            
            // recurse
            if (p.isBean() && p.isIndexed()) {
                BaseBean[] ba = (BaseBean[])bean.getValues(name);
                for (int k=0; k<ba.length; k++)
                    find(ba[k], found, propName, attrName, value);
            } else
                if (p.isBean()) {
                BaseBean b = (BaseBean)bean.getValue(name);
                find(b, found, propName, attrName, value);
                }
        }
    }
    
    /**
     *  Search for a specific attribute name/value.
     */
    public String[] findAttributeValue(String attrName, String value) {
        ArrayList list = new ArrayList();
        this.find(this, list, null, attrName, value);
        String[] ret = new String[list.size()];
        return (String[])list.toArray(ret);
    }
    
    /**
     * Search for a specfic property name/value.
     */
    public String[] findPropertyValue(String propName, Object value) {
        ArrayList list = new ArrayList();
        this.find(this, list, propName, null, value);
        String[] ret = new String[list.size()];
        return (String[])list.toArray(ret);
    }
    
    /**
     *	Search for any property or attribute of the specified value.
     */
    public String[] findValue(Object value) {
        ArrayList list = new ArrayList();
        this.find(this, list, null, null, value);
        String[] ret = new String[list.size()];
        return (String[])list.toArray(ret);
    }
    
    public void write(File f) throws IOException, Schema2BeansRuntimeException {
        OutputStream out = new FileOutputStream(f);
        try {
            write(out);
        } finally {
            out.close();
        }
    }
    
    /**
     *  Write the current schema2beans graph as an XML document.
     */
    public void write(OutputStream out)	throws IOException, Schema2BeansRuntimeException {
        try {
            reindent();
            if (this.graphManager != null) {
                this.graphManager.write(out);
            } else
                throw new IllegalStateException(Common.
                        getMessage("CantWriteBeanNotInDOMTree_msg"));
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        }
    }
    
    /**
     * Write the current schema2beans graph as an XML document.  The
     * parameter @param encoding tells us to write to @param out using
     * that encoding.
     */
    public void write(OutputStream out, String encoding) throws IOException, Schema2BeansException {
        reindent();
        if (this.graphManager != null) {
            this.graphManager.write(out, encoding);
        } else
            throw new IllegalStateException(Common.
                    getMessage("CantWriteBeanNotInDOMTree_msg"));
    }
    
    /**
     * If you call this method, you're responsible for setting up the
     * right encoding for the Writer @param w.
     */
    public void write(java.io.Writer w)	throws IOException, Schema2BeansException {
        write(w, null);
    }
    
    /**
     * If you call this method, you're responsible for setting up the
     * right encoding for the Writer @param w.  The @param encoding
     * parameter is there to let us know which encoding you picked so
     * that it can be written to the header.
     */
    public void write(java.io.Writer w, String encoding) throws IOException, Schema2BeansException {
        reindent();
        if (this.graphManager != null) {
            this.graphManager.write(w, encoding);
        } else
            throw new IllegalStateException(Common.
                    getMessage("CantWriteBeanNotInDOMTree_msg"));
    }
    
    public void writeNoReindent(OutputStream out) throws IOException, Schema2BeansException {
        if (this.graphManager != null) {
            this.graphManager.write(out);
        } else
            throw new IllegalStateException(Common.
                    getMessage("CantWriteBeanNotInDOMTree_msg"));
    }
    
    public void writeNode(java.io.Writer out) throws IOException {
        if (graphManager == null) {
            throw new IllegalStateException(Common.
                    getMessage("CantWriteBeanNotInDOMTree_msg"));
        }
        Node myNode = binding.getNode();
        try {
            graphManager.write(out, myNode);
        } catch (Schema2BeansException e) {
            // This exception does not make sense for our signature.
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Rearrange the internal whitespace so that when it gets printed
     * out, it looks pretty.
     */
    public void reindent() {
        reindent("  ");
    }
    
    public void reindent(String indent) {
        if (graphManager != null) {
            graphManager.reindent(indent);
        } else
            throw new IllegalStateException(Common.
                    getMessage("CantWriteBeanNotInDOMTree_msg"));
    }
    
    protected boolean hasDomNode() {
        if (this.binding == null)
            return false;
        else
            return this.binding.hasDomNode();
    }
    
    protected DOMBinding domBinding() {
        return this.binding;
    }
    
    protected void setDomBinding(DOMBinding binding) {
        this.binding = binding;
    }
    
    /**
     *  Return the unique graphManager instance of the schema2beans graph
     */
    public GraphManager graphManager() {
        return this.graphManager;
    }
    
    protected void setGraphManager(GraphManager graphMgr) {
        this.graphManager = graphMgr;
        if (this.changeListeners != null) {
            // XXXX - move these listeners to the final place.
        }
    }
    
    /*
     *	The methods syncNodes, buildPathName and notifyChange are recursivly
     *	called by the BeanProp objects to parse the entire tree, either
     *	from the current bean up to the root, or from the current bean
     *	down to any sub-nodes.
     */
    
    /**
     *	This method is called to update the DOM graph with the set
     *	of newly created beans. When a new bean subgraph is created,
     *	all the beans of this subgraph are created independently from the
     *	DOM graph. When the root of the bean subgraph is finally attached
     *	to the original bean graph, we can find out where it fits on the
     *	DOM graph, and this method is called to force all the beans cached
     *	values to be flushed into the DOM graph (see also BeanProp and
     *	DOMBinding syncNodes() methods).
     */
    void syncNodes(BeanProp.Action a) {
        Iterator i = beanPropsIterator();
        
        while (i.hasNext()) {
            BeanProp prop = (BeanProp)i.next();
            if (prop != null)
                prop.syncNodes(a);
        }
    }
    
    /**
     *	Build the current path up to the root node. See the BeanProp object
     *	for more details.
     */
    protected void buildPathName(StringBuffer str) {
        if (this.binding != null) {
            BeanProp p = this.binding.getBeanProp(this);
            if (p != null)
                p.buildPathName(this.binding, str);
        }
    }
    
    /**
     *	This method is called by one of the properties of the bean
     *	to notify of a change. This is the way the property uses to
     *	signal all its ancestors about a property that has changed.
     */
    void notifyInternal(BeanProp.InternalEvent ie) {
        if (this.changeListeners != null
                && ie.type == BeanProp.InternalEvent.CHANGED) {
            
            boolean addedGM = false;
            
            if (this.graphManager == null) {
                this.graphManager = new GraphManager(this);
                addedGM = true;
            }
            this.changeListeners.
                    firePropertyChange(ie.getPropertyChangeEvent());
            if (addedGM) {
                this.graphManager = null;
            }
        }
        if (this.binding != null) {
            BeanProp p = this.binding.getBeanProp(this);
            if (p != null)
                p.notifyInternal(ie, true);
        }
    }
    
    
    /**
     *	Process a deep clone() of the current bean
     */
    public Object clone() {
        BaseBean bean = null;
        try {
            // FIXME this seriosly breaks the clone contract :(
            // Create a new instance of ourself
            bean = (BaseBean)this.getClass().getDeclaredConstructor().newInstance();
        } catch(Exception e) {
            TraceLogger.error(e);
            throw new Schema2BeansRuntimeException(Common.
                    getMessage("CantInstantiateBean_msg", e.getMessage()));
        }
        
        //  If we are cloning the root - we need some extra initialization
        if (this.graphManager != null && this.graphManager.root == this) {
            //  Get the info on the current root
            BeanProp 	p = this.binding.getBeanProp(this);
            String	dtdName = p.getDtdName();
            String 	beanName = p.getDtdName();
            Class	beanClass = p.getPropClass();
            
            //  Create the initial DOM Node element
            Node n = GraphManager.createRootElementNode(dtdName);
            
            //  Initialize the graph manager
            bean.graphManager.setXmlDocument(n);
            n = GraphManager.getElementNode(dtdName, n);
            bean.graphManager.completeRootBinding(bean, n);
        }
        
        //  Copy the attributes of the root
        String[] attrs = this.getAttributeNames();
        if (attrs != null) {
            for(int j=0; j<attrs.length; j++) {
                String a = attrs[j];
                if (!this.beanProp().getAttrProp(a).isFixed()) {
                    String v = this.getAttributeValue(a);
                    if (bean.getAttributeValue(a) != v)
                        bean.setAttributeValue(a, v);
                }
            }
        }
        
        if (attrCache != null)
            bean.attrCache = (HashMap) attrCache.clone();  // This does a shallow clone of the HashMap, but that's fine since they're all just Strings in there.
        
        Iterator it = beanPropsIterator();
        
        //  Parse our attributes and copy them
        while (it.hasNext()) {
            BeanProp prop = (BeanProp)it.next();
            
            if (prop == null)
                continue;
            
            String name = prop.getBeanName();
            
            if (Common.isArray(prop.type)) {
                int size = prop.size();
                if (Common.isBean(prop.type)) {
                    for(int i=0; i<size; i++) {
                        BaseBean b = (BaseBean)prop.getValue(i);
                        if (b != null)
                            b = (BaseBean)b.clone();
                        bean.addValue(name, b);
                    }
                } else {
                    for(int i=0; i<size; i++)
                        bean.addValue(name, prop.getValue(i));
                    
                    //	Copy the attributes
                    attrs = prop.getAttributeNames();
                    for(int j=0; j<attrs.length; j++) {
                        String a = attrs[j];
                        if (!prop.getAttrProp(a).isFixed()) {
                            for(int i=0; i<size; i++) {
                                String v = prop.getAttributeValue(i, a);
                                if (bean.getAttributeValue(name, i, a) != v)
                                    bean.setAttributeValue(name, i, a, v);
                                
                            }
                        }
                    }
                }
            } else {
                if (Common.isBean(prop.type)) {
                    BaseBean b = (BaseBean)prop.getValue(0);
                    if (b != null)
                        b = (BaseBean)b.clone();
                    bean.setValue(name, b);
                } else {
                    bean.setValue(name, prop.getValue(0));
                    
                    //	Copy the attributes
                    attrs = prop.getAttributeNames();
                    for(int j=0; j<attrs.length; j++) {
                        String a = attrs[j];
                        if (!prop.getAttrProp(a).isFixed()) {
                            String v = prop.getAttributeValue(0, a);
                            if (bean.getAttributeValue(name, 0, a) != v)
                                bean.setAttributeValue(name, a, v);
                        }
                    }
                }
            }
        }
        
        return bean;
    }
    
    /**
     *	Merge the bean tree with ourself
     *
     *	Let's define:
     *		G1 the current graph and G2 the new graph we want to merge
     *		E1 the set of element of G1 that don't exist anymore in G2.
     *		E2 the set of new elements of G2 that don't exist in G1.
     *
     *	Then,
     *		UPDATE is 	G1 - E1 + E2	(G1 becomes G2)
     *		UNION is 	G1 U G2 <=> G1 + E2
     *		INTERSECT is	G1 n G2 <=> (G1 U G2) - E1 - E2
     */
    public static final int MERGE_NONE 		= 0x00;
    public static final int MERGE_INTERSECT	= 0x01;
    public static final int MERGE_UNION		= 0x02;
    public static final int MERGE_UPDATE 	= (MERGE_UNION|MERGE_INTERSECT);
    public static final int MERGE_COMPARE 	= 0x04;
    
    
    static String mergeModeToString(int mode) {
        switch(mode) {
            case MERGE_NONE: return "MERGE_NONE";		// NOI18N
            case MERGE_INTERSECT: return "MERGE_INTERSECT";	// NOI18N
            case MERGE_UNION: return "MERGE_UNION";		// NOI18N
            case MERGE_UPDATE: return "MERGE_UPDATE";	// NOI18N
            case MERGE_COMPARE: return "MERGE_COMPARE";	// NOI18N
            default: return "Unknown merge mode: " + mode;	// NOI18N
        }
    }
    
    /**
     *	Merge the specified bean schema2beans graph into the current graph using
     *	the specified mode.
     */
    public void merge(BaseBean bean, int mode) {
        if (mode == MERGE_UPDATE)
            mergeUpdate(bean);
        else
            mergeTreeRoot(bean, mode);
    }
    
    /**
     *	Merge the bean tree with the current graph using the default
     *	merging option.
     */
    public void merge(BaseBean bean) {
        mergeUpdate(bean);
    }
    
    /**
     * Same as merge(BaseBean bean).
     * It's possible to override this method and make it more efficient,
     * than the generic one.
     */
    public void mergeUpdate(BaseBean sourceBean) {
        mergeTreeRoot(sourceBean, MERGE_UPDATE);
    }
    
    //	Called by mergeTree to set the default hasKey value
    private boolean setHasKeyDefaultValue(BeanProp prop) {
        BeanComparator cmp = (BeanComparator)this.comparators.get(0);
        return cmp.hasKeyDefined(prop);
    }
    
    /*
     *	Copy a property from the graph 'bean' to the graph 'prop', making
     *	sure that both property and attributes are copied.
     *	This method is used to copy non BaseBean properties
     *	(a clone on a BaseBean automatically copies the attributes).
     */
    protected void copyProperty(BeanProp prop, BaseBean bean,
            int index, Object value) {
        
        boolean isArray = Common.isArray(prop.type);
        String name = prop.getName();
        
        //  Copy the property value
        if (value == null) {
            if (isArray)
                value = bean.getValue(name, index);
            else
                value = bean.getValue(name, 0);
        }
        
        int newIndex = 0;
        
        if (isArray) {
            newIndex = addValue(prop, value);
        } else {
            setValue(prop, 0, value);
            index = 0;
        }
        
        this.copyAttributes(prop, newIndex, bean, index);
    }
    
    /*
     *	This copies the attributes of a property from 'bean' (whole BaseBean)
     *	to a property BeanProp (the specific BeanProp of the other BaseBean
     *	where the copy as to occur).
     */
    private void copyAttributes(BeanProp prop, int propIndex,
            BaseBean bean, int beanIndex) {
        
        //	Copy the attributes
        String name = prop.getName();
        BaseAttribute[] ba = bean.listAttributes(name);
        if (ba != null) {
            for(int j=0; j<ba.length; j++) {
                if (!ba[j].isFixed()) {
                    String attrName = ba[j].getName();
                    String v =
                            bean.getAttributeValue(name, beanIndex, attrName);
                    if (v != prop.getAttributeValue(propIndex, attrName)) {
                        prop.setAttributeValue(propIndex, attrName, v);
                    }
                }
            }
        }
    }
    
    /*
     *	Entry point for the merge processing. This takes care of processing
     *	the attribites of the root. This is necessary because the algorithm
     *	assumes that we only merge two graphs that are identicals on their
     *	first level (same simple property values and same attribute values).
     */
    synchronized boolean mergeTreeRoot(BaseBean bean, int mode) {

        //  Process the whole graph now
        return this.mergeTree(bean, mode);
    }

    private boolean mergeAttributes(BaseBean bean, int mode) {
        boolean result = true;
        //  We need to process the attributes of the root first
        BaseAttribute[] ba = bean.listAttributes();

        //  We might have no attribute on the root
        if (ba != null) {
            for (int j = 0; j < ba.length; j++) {
                BaseAttribute baseAttribute = ba[j];
                if (!baseAttribute.isFixed() && !baseAttribute.isTransient()) {
                    String attrName = baseAttribute.getName();
                    String curValue = this.getAttributeValue(attrName);
                    String otherValue = bean.getAttributeValue(attrName);

                    if (!Objects.equals(curValue, otherValue)) {
                        // Might have one of the two null, not both
                        if ((mode & MERGE_COMPARE) == MERGE_COMPARE) {
                            return false;
                        }
                        if ((mode & MERGE_UNION) == MERGE_UNION) {
                            this.setAttributeValue(attrName, otherValue);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     *	Merge the bean tree with ourself
     */
    //
    //	A little note about merge/comparison: if we compare the graphs
    //	(option MERGE_COMPARE), we stop the comparison as soon as we find
    //	two different elements, returning false. If we do not compare,
    //	we always return true, until we parsed the whole graph.
    //	Therefore, when recurse calls mergeTree we simply test the
    //	result. If this is false, we can return immediatly (because this
    //	is necessarily a COMPARE operation). If not, we have to continue
    //	the merging/comparison.
    //
    synchronized boolean mergeTree(BaseBean bean, int mode) {
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                    DDLogFlags.DBG_UBN, 1, DDLogFlags.MERGE,
                    this.getClass().getName() + "/" +
                    (bean==null?"<getAttributeValue(\"Id\")null>":bean.getClass().getName()) +
                    " - " + mergeModeToString(mode));
        }

        //
        //	The merge method is called only when two beans are logically
        //	identical. Therefore, this method doesn't try to check if it
        //	is equal to the other beans, but find out which properties
        //	have to be updated.
        //
        if (this.getClass().isInstance(bean)) {
            if (!mergeAttributes(bean, mode)) {
                return false;
            }


            //	We got the same as ourself in another graph
            Iterator it = beanPropsIterator();

            //
            //	Parse our attributes
            //
            //	To have the following code easier to read, we could
            //	call the setter/getter method of the BaseBean object for
            //	both our attributes and the bean-to-merge attributes.
            //	However, since we get the BeanProp objects from our
            //	properties hashtable, we can call directly the BeanProp
            //	getter/setter methods for our properties and the
            //	BaseBean getter/setter for the bean we have to merge.
            //
            while (it.hasNext()) {
                //	Get our next property (as a BeanProp)
                BeanProp prop = (BeanProp)it.next();

                if (prop == null)
                    continue;

                String 		name = prop.getBeanName();
                boolean 	isArray = Common.isArray(prop.type);
                boolean 	isBean = Common.isBean(prop.type);
                Object 		o1, o2, o3;
                boolean		hasKey = false;
                boolean		hasKeyDefined = false;

                if (isArray) {
                    //
                    //	For each element of the index property, we have to
                    //	find if there is a matching element in the other
                    //	indexed property. If there is, merge the two
                    //	elements if this is a bean. If there are no
                    //	matching elements, remove it. At the end,
                    //	add any new elements of the other indexed property.
                    //
                    int		i, j = 0;
                    int 	size1 = prop.size();
                    int 	size2 = bean.size(name);
                    boolean 	toRemove[] = new boolean[size1];
                    boolean 	toAdd[] = new boolean[size2];
                    boolean 	compared[] = new boolean[size2];

                    //	To keep track of that need to be removed
                    Arrays.fill(toRemove, false);

                    //	To keep track of what we'll need to add after the loop
                    Arrays.fill(toAdd, true);

                    //	To make sure that we do not match twice the same elt
                    Arrays.fill(compared, false);

                    if (DDLogFlags.debug) {
                        TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                                DDLogFlags.DBG_UBN, 5,
                                DDLogFlags.MERGEPROP,
                                this.getClass().getName() + "." +
                                name + "[" + size1 + "] / " +
                                bean.getClass().getName() + "." +
                                name + "[" + size2 + "]");
                    }

                    //	For each of our current property elts ...
                    for (i=0; i<size1; i++) {
                        o1 = prop.getValue(i);
                        //System.out.println("looking at prop "+i+" o1="+o1);

                        if (isBean && o1 == null)
                            continue;	// Nothing to compare

                        boolean found = false;

                        // ... try each comparator ...
                        for (int c=0; c<this.comparators.size() && !found; c++){
                            BeanComparator cmp =
                                    (BeanComparator)this.comparators.get(c);

                            //	... with every new property elts
                            for (j=0; j<size2; j++) {
                                if (!compared[j]) {
                                    o2 = bean.getValue(name, j);

                                    if (isBean) {
                                        if (o2 == null) {
                                            // Ignore null elt
                                            compared[j] = true;
                                            toAdd[j] = false;
                                            continue;
                                        }

                                        o3 = cmp.compareBean(name,
                                                (BaseBean)o1,
                                                (BaseBean)o2);

                                        if (!hasKey) {
                                            hasKey = cmp.hasKey();
                                            hasKeyDefined = true;
                                        }

                                        if (o3 == o1) {
                                            //	Beans identicals - recurse
                                            boolean ret = ((BaseBean)o1).
                                                    mergeTree((BaseBean)o2, mode);

                                            if (!ret) return ret;
                                            compared[j] = true;
                                            found = true;
                                            break;
                                        }
                                    } else {
                                        o3 = cmp.compareProperty(name,
                                                this, o1, i,
                                                bean, o2, j);
                                        if (!hasKey) {
                                            hasKey = cmp.hasKey();
                                            hasKeyDefined = true;
                                        }

                                        if (o3 == o1) {
                                            compared[j] = true;
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (found) {
                            toAdd[j] = false;	// already have it

                            if (DDLogFlags.debug) {
                                TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_UBN, 5,
                                        DDLogFlags.MERGEFOUND,
                                        name + "[" + i + "] <=> " +
                                        name + "[" + j + "]");
                            }
                        } else {
                            toRemove[i] = true;	//	no more exists

                            if (DDLogFlags.debug) {
                                TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_UBN, 5,
                                        DDLogFlags.MERGENTFND,
                                        name + "[" + i +
                                        "] to be removed");
                            }
                        }
                    }

                    //
                    //	We want to make sure that we set a proper value
                    //	to hasKey when one of the two array is empty
                    //	(either null or containing null elements)
                    //
                    if (!hasKeyDefined)
                        hasKey = this.setHasKeyDefaultValue(prop);

                    if ((mode & MERGE_COMPARE) == MERGE_COMPARE) {
                        //	Any diff returns false
                        for (i=0; i<size1; i++)
                            if (toRemove[i] && hasKey)
                                return false;

                        for (j=0; j<size2; j++)
                            if (toAdd[j] && hasKey)
                                return false;
                    }

                    //	Remove, taking care of the index shifting
                    if ((mode & MERGE_INTERSECT) == MERGE_INTERSECT) {
                        for (i=0, j=0; i<size1; i++) {
                            if (toRemove[i] && hasKey) {
                                //System.out.println("MERGE_INTERSECT: "+name+": removeValue("+prop.getBeanName()+", "+ (i-j)+")");
                                removeValue(prop, i-j);
                                j++;
                            }
                        }
                    }

                    //	Add all the new elements
                    if ((mode & MERGE_UNION) == MERGE_UNION) {
                        for (j=0; j < size2; j++) {
                            if (toAdd[j] && hasKey) {
                                //System.out.println("MERGE_UNION: "+name+": add j="+j);
                                if (isBean) {
                                    //	Attrs are within the BaseBean
                                    BaseBean srcBean = (BaseBean) bean.getValue(name, j);
                                    o2 = srcBean.clone();
                                    addValue(prop, o2);
                                    // Make sure that whitespace & comments get brought over.
                                    ((BaseBean)o2).mergeTree(srcBean, mode);
                                } else {
                                    //	We need to explicitely copy the attrs
                                    this.copyProperty(prop, bean, j, null);
                                }
                            }
                        }
                    }
                } else {
                    Object  newValue = null;
                    boolean found = false;

                    if (DDLogFlags.debug) {
                        TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                                DDLogFlags.DBG_UBN, 5,
                                DDLogFlags.MERGEPROP,
                                this.getClass().getName() + "." +
                                name);
                    }

                    //	This is a single value property
                    o1 = prop.getValue(0);	//	ourself
                    o2 = bean.getValue(name);	//	the other one

                    //
                    //	We have two properties to compare. Go over all the
                    //	comparators. Stop the comparison if any of them
                    //	find the two properties identical.
                    //	If they are beans, recurse calling the merge method
                    //	on these two beans. If they are not beans, simply
                    //	keep our current property value, ignoring the new one.
                    //
                    //	If no comparator find the two properties identical,
                    //	update our current property with the value returned
                    //	by the very first comparator.
                    //
                    for (int c=0; c<this.comparators.size() && !found; c++) {
                        BeanComparator cmp =
                                (BeanComparator)this.comparators.get(c);

                        if (isBean) {
                            if (o1 != null && o2 != null) {
                                //	Recurse merging if they are the same
                                o3 = cmp.compareBean(name, (BaseBean)o1,
                                        (BaseBean)o2);

                                if (!hasKey)
                                    hasKey = cmp.hasKey();

                                if (o3 != o1)
                                    newValue = (c==0)?o3:newValue;
                                else {
                                    found = true;
                                    boolean ret = ((BaseBean)o1).
                                            mergeTree((BaseBean)o2, mode);
                                    if (!ret) return ret;
                                }
                            } else {
                                if (o1 == o2)
                                    found = true;
                                else {
                                    hasKey = cmp.hasKeyDefined(prop);
                                    newValue = (c==0)?o2:newValue;
                                }
                            }
                        } else {
                            o3 = cmp.compareProperty(name, this, o1, -1,
                                    bean, o2, -1);

                            if (!hasKey)
                                hasKey = cmp.hasKey();

                            if (o3 != o1)
                                newValue = (c==0)?o3:newValue;
                            else
                                found = true;
                        }

                        if (!found && ((mode & MERGE_COMPARE)==MERGE_COMPARE)) {
                            //	Any diff - return false
                            return false;
                        }

                        if (!found && ((mode & MERGE_UNION) == MERGE_UNION)
                        && hasKey) {

                            if (isBean) {
                                if (newValue != null) {
                                    setValue(prop, 0,
                                            ((BaseBean)newValue).clone());
                                } else {
                                    setValue(prop, 0, newValue);
                                }
                            } else {
                                //  We need to explicitely copy the attrs
                                this.copyProperty(prop, bean, 0, newValue);
                            }

                            if (DDLogFlags.debug) {
                                TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_UBN, 5,
                                        DDLogFlags.MERGENTFND,
                                        "updating with new value");
                            }
                        } else
                            if (found) {
                            if (DDLogFlags.debug) {
                                TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_UBN, 5,
                                        DDLogFlags.MERGEFOUND,
                                        "keeping current value");
                            }
                            }
                    }
                }
            }


            if ((mode == MERGE_UPDATE)) {
                // For MERGE_UPDATE we additionally merge elements which have
                // no representation in model and exist only in DOMBinding
                if (isRoot) {
                    Schema2BeansUtil.mergeUnsupportedElements(this, bean);
                }
            }

            //
            //	For the MERGE_COMPARE option: if we reach this point, that
            //	means we didn't find any diff. We can therefore return true.
            //	Any other option returns always true.
            //
            return true;
        } else
            throw new IllegalArgumentException(Common.getMessage(
                    "MergeWrongClassType_msg", this.getClass().getName(),
                    (bean==null ? "<null>" : bean.getClass().getName())));
    }

    /**
     * Compare 2 Node's and tell me if they're roughly equivalent.
     * By roughly equivalent, attributes and children are ignored.
     */
    private boolean areNodesEqual(Node node1, Node node2) {
        if (node1 == null && node2 == null)
            return true;
        if (node1 == null || node2 == null)
            return false;
        if (node1.getNodeType() != node2.getNodeType())
            return false;
        if (!node1.getNodeName().equals(node2.getNodeName()))
            return false;
        String value1 = node1.getNodeValue();
        String value2 = node2.getNodeValue();
        if (value1 == null) {
            if (value2 != null)
                return false;
        } else if (!value1.equals(value2))
            return false;
        return true;
    }

    /**
     * Search in @param nodes for an equivalent node to @param node
     * (equivalent as defined by areNodesEqual) starting the search
     * at position @param start.
     */
    private int findInNodeList(NodeList nodes, Node node, int start) {
        return findInNodeList(nodes, node, start, nodes.getLength());
    }
    
    /**
     * Search in @param nodes for an equivalent node to @param node
     * (equivalent as defined by areNodesEqual) starting the search
     * at position @param start, giving up the search at position
     * @param maxPosition.
     */
    private int findInNodeList(NodeList nodes, Node node,
            int start, int maxPosition) {
        for (; start < maxPosition; ++start) {
            if (areNodesEqual(nodes.item(start), node))
                return start;
        }
        return -1;
    }
    
    /**
     *	Perform a deep recursive comparison. Return true if the two graphs
     *	are equals, false otherwise.
     *
     *	The comparison is using the comparators and therfore returns a
     *	result wrt to the comparators. That means a true return value
     *	only means that the graphs are logically equals depending on the
     *	comparator implementation.
     *
     *	Note that the default comparator compares every property value.
     *
     */
    
    public boolean isEqualTo(Object obj) {
        boolean ret = false;
        
        try {
            if (this == obj)
                return true;
            else
                if (obj instanceof BaseBean)
                    ret = this.mergeTreeRoot((BaseBean)obj, MERGE_COMPARE);
        } catch(Exception e) {
            //	Equals method only returns either true or false
            if (DDLogFlags.debug) {
                TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                        DDLogFlags.DBG_UBN, 1, DDLogFlags.EQUALS,
                        "got exception while comparing: " +
                        e + "\n");
                e.printStackTrace();
            }
            ret = false;
        }
        
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                    DDLogFlags.DBG_UBN, 1, DDLogFlags.EQUALS,
                    (ret?"true":"false"));
        }
        return ret;
    }
    
    /**
     *	Get the bean using its unique identifier. This identifier is not
     *	the indexed position of the element in the array but a unique id
     *	associated to the DOMBinding object (see the BeanProp and DOMBinding
     *	classes)
     */
    public Bean propertyById(String name, int id) {
        BeanProp bp = this.beanProp(name);
        
        if (Common.isBean(bp.type)) {
            if (Common.isArray(bp.type))
                return (BaseBean)bp.getValueById(id);
            else
                return (BaseBean)bp.getValue(0);
        } else
            throw new IllegalStateException(Common.
                    getMessage("PropertyIsNotABean_msg", name));
    }
    
    /**
     *	Return the BeanProp object where this bean belongs.
     */
    public BeanProp beanProp() {
        if (this.binding != null)
            return this.binding.getBeanProp(this);
        else
            return null;
    }
    
    /**
     *  Return the BaseBean parent of the current bean. This might return null
     *	either because this is the root of the graph or because this node is not
     *	part of a schema2beans graph yet.
     */
    public BaseBean parent() {
        BeanProp bp = this.beanProp();
        
        if (bp != null)
            return this.beanProp().getBean();
        else
            return null;
    }
    
    public Bean _getParent() {
        return parent();
    }
    
    /**
     * Return the root of the graph.  If the graph is not connected to a
     * generated root, then the topmost bean is returned.
     */
    public Bean _getRoot() {
        Bean b = this;
        Bean bParent;
        while (!b.isRoot()) {
            bParent = b._getParent();
            if (bParent == null)
                break;
            b = bParent;
        }
        return b;
    }
    
    /**
     *	Return the path name of this element in the graph.
     */
    public String fullName() {
        StringBuffer str = new StringBuffer();
        this.buildPathName(str);
        return str.toString();
    }
    
    public boolean hasName(String name) {
        if (name != null)
            return (name.equals(this.name()) || name.equals(this.dtdName()));
        else
            return false;
    }
    
    /**
     *	Return true if this element is the root of the schema2beans tree.
     */
    public boolean isRoot() {
        return this.isRoot;
    }
    
    /**
     *	Return the bean name of this schema2beans graph node.
     */
    public String name() {
        BeanProp bp = this.beanProp();
        
        if (bp != null)
            return this.beanProp().getBeanName();
        else
            return "";	// NOI18N
    }
    
    /**
     *	Return the DTD name of this schema2beans graph node.
     */
    public String dtdName() {
        return this.beanProp().getDtdName();
    }
    
    public void createBean(Node node, GraphManager mgr) throws Schema2BeansRuntimeException {
        if (this.isRoot) {
            mgr.completeRootBinding(this, node);
        }
        
        this.graphManager = mgr;
        try {
            mgr.fillProperties(this.beanProps(), node);
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        }
    }
    
    /**
     *	Return a new instance of the specified bean property
     */
    public BaseBean newInstance(String name) {
        return this.beanProp(name).newBeanInstance();
    }
    
    
    public abstract void dump(StringBuffer str, String indent);
    
    // The old abstract verify method never did anything.
    // a validate method will get generated if the -validate option is on.
    //public abstract void validate() throws org.netbeans.modules.schema2beans.ValidateException;
    
    /**
     *	Dump the DOM content of this bean. If nodeName is specified,
     *	dump only from the subname named nodeName.
     */
    public String dumpDomNode(String nodeName, int depth) {
        Node n = null;
        
        if (this.binding == null)
            return "<no binding>";	// NOI18N
        else {
            n = this.binding.node;
            if (n == null)
                return "<no node>";	// NOI18N
        }
        
        return DDFactory.XmlToString(n, depth, nodeName);
    }
    
    public String dumpDomNode(int depth) {
        return this.dumpDomNode(null, depth);
    }
    
    public String dumpDomNode() {
        return this.dumpDomNode(null, 99999);
    }
    
    public String dumpBeanNode() {
        return null;
    }
    
    public void dumpAttributes(String name, int index, StringBuffer str,
            String indent) {
        String[] names = this.getAttributeNames(name);
        
        for (int i=0; i<names.length; i++) {
            String v = this.getAttributeValue(name, index, names[i]);
            if (v != null) {
                str.append(indent + "\t  attr: ");	// NOI18N
                str.append(names[i]);
                str.append("=");	// NOI18N
                str.append(v);
            }
        }
    }
    
    public String toString() {
        return this.name();
    }
    
    
    public void dumpXml() {
        try {
            write(System.out);
        } catch (IOException ee) {
            ee.printStackTrace();
        }
    }
    
    
    /**
     *	Create the schema2beans graph of root bean clazz, from the input stream.
     */
    public static BaseBean createGraph(Class clazz, InputStream in)
    throws Schema2BeansException {
        return createGraph(clazz, in, false, null, null);
    }
    
    /**
     *	Create the schema2beans graph of root bean clazz, from the input stream
     *	in using the validate option.
     */
    public static BaseBean createGraph(Class clazz, InputStream in,
            boolean validate) throws Schema2BeansException {
        return createGraph(clazz, in, validate, null, null);
    }
    
    /**
     *	Create the schema2beans graph of root bean clazz, from the input stream
     *	in using the validate option and the entity resolver er.
     */
    public static BaseBean createGraph(Class clazz, InputStream in,
            boolean validate,
            org.xml.sax.EntityResolver er) throws
            Schema2BeansException {
        return createGraph(clazz, in, validate, er, null);
    }
    
    /**
     *	Create the schema2beans graph of root bean clazz, from the input stream in
     *  and using the validate, er and sh options.
     */
    public static BaseBean createGraph(Class clazz, InputStream in,
            boolean validate,
            org.xml.sax.EntityResolver er,
            org.xml.sax.ErrorHandler   eh) throws
            Schema2BeansException {
        
        Constructor 	c = null;
        Document 	doc = null;
        BaseBean	bean = null;
        
        doc = GraphManager.createXmlDocument(new org.xml.sax.InputSource(in),
                validate, er, eh);
        try {
            Class[] cc = new Class[] {org.w3c.dom.Node.class,
                    int.class};
                    c = clazz.getDeclaredConstructor(cc);
        } catch(NoSuchMethodException me) {
            throw new RuntimeException(Common.
                    getMessage("CantGetConstructor_msg"));
        }
        
        Object[] p = new Object[] {doc, Integer.valueOf(Common.NO_DEFAULT_VALUES)};
        
        try {
            bean = (BaseBean)c.newInstance(p);
        } catch (InstantiationException e) {
            throw new Schema2BeansNestedException(Common.getMessage(
                    "CantInstanciateBeanClass_msg"), e);
        } catch (IllegalAccessException e) {
            throw new Schema2BeansNestedException(Common.getMessage(
                    "CantInstanciateBeanClass_msg"), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new Schema2BeansNestedException(Common.getMessage(
                    "CantInstanciateBeanClass_msg"), e);
        }
        
        return bean;
    }
    
    
    //
    PropertyChangeSupport changeListeners;
    
    
    //
    public void addPropertyChangeListener(PropertyChangeListener l) {
        BeanProp p = this.beanProp();
        if (p != null) {
            p.addPCListener(l);
        } else {
            if (this.changeListeners == null) {
                this.changeListeners = new PropertyChangeSupport(this);
            }
            this.changeListeners.addPropertyChangeListener(l);
        }
    }
    
    //
    public void removePropertyChangeListener(PropertyChangeListener l) {
        BeanProp p = this.beanProp();
        if (p != null) {
            p.removePCListener(l);
        } else if (this.changeListeners != null) {
            this.changeListeners.removePropertyChangeListener(l);
        }
    }
    
    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l) {
        BeanProp p = this.beanProp(n);
        if (p != null)
            p.addPCListener(l);
    }
    
    //
    public void removePropertyChangeListener(String n, PropertyChangeListener l) {
        BeanProp p = this.beanProp(n);
        if (p != null)
            p.removePCListener(l);
    }
    
    /**
     * @return all Comment nodes found in this particular bean.
     *         If there are no comments, then a 0 sized array will be
     *         returned.  null will never be returned.
     * If you want to change a comment, use Comment.setData().
     */
    public org.w3c.dom.Comment[] comments() {
        if (graphManager == null)
            return new org.w3c.dom.Comment[0];
        Document doc = graphManager.getXmlDocument();
        Node node = binding.getNode();
        NodeList children = node.getChildNodes();
        List foundComments = new LinkedList();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child instanceof org.w3c.dom.Comment) {
                foundComments.add(child);
            }
        }
        org.w3c.dom.Comment[] result = new org.w3c.dom.Comment[foundComments.size()];
        result = (org.w3c.dom.Comment[]) foundComments.toArray(result);
        return result;
    }
    
    /**
     * The new comment will be added to the first of the bean's children.
     * @return the newly added Comment
     * @throws NullPointerException if this is a nonroot bean that is
     * not part of a rooted graph yet.
     */
    public org.w3c.dom.Comment addComment(String comment) {
        if (graphManager == null) {
            // ?
        }
        Document doc = graphManager.getXmlDocument();
        org.w3c.dom.Comment commentNode = doc.createComment(comment);
        Node node = binding.getNode();
        Node firstChild = node.getFirstChild();
        if (firstChild == null) {
            node.appendChild(commentNode);
        } else {
            node.insertBefore(commentNode, firstChild);
        }
        return commentNode;
    }
    
    /**
     * Remove @param comment from this bean.
     */
    public void removeComment(org.w3c.dom.Comment comment) {
        comment.getParentNode().removeChild(comment);
    }
    
    /**
     * @return child beans.
     */
    public BaseBean[] childBeans(boolean recursive) {
        List children = new LinkedList();
        childBeans(recursive, children);
        BaseBean[] result = new BaseBean[children.size()];
        return (BaseBean[]) children.toArray(result);
    }
    
    /**
     * Store all child beans into @param beans.
     */
    public void childBeans(boolean recursive, List beans) {
        BeanProp[] props = beanProps();
        BaseBean nextBean;
        for (int propPosition = 0; propPosition < props.length; ++propPosition) {
            BeanProp prop = props[propPosition];
            if (!prop.isBean()) {
                continue;
            }
            if (prop.isIndexed()) {
                for (int i = 0; i < prop.size(); ++i) {
                    nextBean = (BaseBean) prop.getValue(i);
                    if (nextBean == null)
                        continue;
                    beans.add(nextBean);
                    if (recursive)
                        nextBean.childBeans(true, beans);
                }
            } else {
                nextBean = (BaseBean) prop.getValue(0);
                if (nextBean == null)
                    continue;
                if (recursive)
                    nextBean.childBeans(true, beans);
                beans.add(nextBean);
            }
        }
    }
    
    public String nameSelf() {
        return beanProp().getFullName();
    }
    
    public String nameChild(Object childObj) {
        return nameChild(childObj, false, false);
    }
    
    public String nameChild(Object childObj, boolean returnConstName,
            boolean returnSchemaName) {
        return nameChild(childObj, returnConstName, returnSchemaName, false);
    }
    
    public String nameChild(Object childObj, boolean returnConstName,
            boolean returnSchemaName,
            boolean returnXPathName) {
        BeanProp[] props = beanProps();
        Object propValue;
        BeanProp prop = null;
        boolean found = false;
        int index = -2;
        propLoop:
            for (int propPosition = 0; propPosition < props.length; ++propPosition) {
                prop = props[propPosition];
                if (prop.isIndexed()) {
                    for (int i = 0; i < prop.size(); ++i) {
                        propValue = prop.getValue(i);
                        if (propValue == null ? childObj == null : propValue.equals(childObj)) {
                            found = true;
                            index = i;
                            break propLoop;
                        }
                    }
                } else {
                    propValue = prop.getValue(0);
                    if (propValue == null ? childObj == null : propValue.equals(childObj)) {
                        found = true;
                        break propLoop;
                    }
                }
            }
            if (found) {
                if (returnConstName)
                    return prop.getBeanName();
                else if (returnSchemaName)
                    return prop.dtdName;
                else if (returnXPathName) {
                    if (index < 0)
                        return prop.dtdName;
                    else
                        return prop.dtdName+"[position()="+index+"]";
                } else
                    return prop.getBeanName()+"."+Integer.toHexString(prop.indexToId(index));
            }
            return null;
    }
    
    public void changeDocType(String publicId, String systemId) {
        graphManager().setDoctype(publicId, systemId);
    }
    
    public void _setChanged(boolean changed) {
        throw new UnsupportedOperationException();
    }
    
    public String _getXPathExpr() {
        if (parent() == null) {
            return "/"+dtdName();
        } else {
            String parentXPathExpr = parent()._getXPathExpr();
            String myExpr = parent().nameChild(this, false, false, true);
            return parentXPathExpr + "/" + myExpr;
        }
    }
    
    public String _getXPathExpr(Object childObj) {
        String childName = nameChild(childObj, false, false, true);
        if (childName == null) {
            throw new IllegalArgumentException("childObj ("+childObj.toString()+") is not a child of this bean ("+dtdName()+")");
        }
        return _getXPathExpr() + "/" + childName;
    }
}
