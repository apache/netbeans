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

//  To dynamically build an array of the bean clastype (class[] getter method)
import java.lang.reflect.*;

import org.w3c.dom.*;


/**
 *  The BeanProp class is the internal representation of a schema2beans property.
 *  The three main classes that make the schema2beans architecture are: BaseBean,
 *  BeanProp and DOMBinding. BaseBean is the schema2beans java beans that maps
 *  a tag DTD element (BaseBean is the class extended by the generated code,
 *  specific to the DTD names). BaseBean contains as much as BeanProp object
 *  instances as it containes properties (element tags). A BeanProp handle
 *  both single and array properties. A single property is represented
 *  by one BeanProp instance that will be of type 'single' (TYPE_0_1 or TYPE_1).
 *  An array property is represented also by one BeanProp instance, 
 *  but of type 'array' (TYPE_0_N or TYPE_1_N).
 *  
 *  The BeanProp class is handling properties, and is acting between 
 *  the BaseBean (user view) and the DOMBinding (access to the DOM tree).
 *  The events, add/change/remove code and logic is on the BeanProp class.
 */
public class BeanProp implements BaseProperty {
    
    
    //
    //	The goal of the GroupProp class is to provide a common container
    //	for DTD or'd elements. Each or'd element of a same group references
    //	the same GroupProp instance.
    //	For example, (a | b | c) in a DTD generates 3 properties A, B, C. For
    //	each property, a BeanProp is created. One GroupProp is created
    //	for these 3 properties, and each BeanProp reference this GroupProp.
    //
    static class GroupProp {
        ArrayList group;
	
        public GroupProp(BeanProp prop) {
            this.group = new ArrayList();
            this.add(prop);
        }
	
        void add(BeanProp prop) {
            this.group.add(prop);
        }
	
        BeanProp[] list() {
            int size = this.group.size();
            BeanProp[] ret = new BeanProp[size];
            for (int i=0; i<size; i++) {
                ret[i] = (BeanProp)this.group.get(i);
            }
            return ret;
        }
    }
    
    //
    //	Class used to identify the type of action to do in the recursive
    //	syncNodes() call.
    //
    static class Action {
        public static final int 				ADD	= 1;
        public static final int 				REMOVE	= 2;
	
        int 							action;
        //java.beans.PropertyChangeEvent 	event;
	
	
        public Action(int action) {
            this.action = action;
            //this.event = null;
        }
	
        public Action(int action, java.beans.PropertyChangeEvent event) {
            this(action);
            //this.event = event;
        }
	
        public String toString() {
            if (this.action == ADD)
                return "add";	// NOI18N
            else
                return "remove";// NOI18N
        }
    }
    
    //
    //	We manage events only if someone adds a listener. This is a trivial
    //	optimization since we just turn on the events when they are used,
    //	not trying to keep track of how many listeners are registered.
    //
    static boolean handleEvents = false;
    static boolean handleVetoEvents = false;
    
    
    /*
     *	The Evt mgr takes care of queuing the events in order to trigger them
     *	only when all the changes are performed.
     */
    class EventMgr {
        class Evt {
            PropertyChangeEvent event;
            boolean				propagate;
	    
            public Evt(PropertyChangeEvent evt, boolean p) {
                this.event = evt;
                this.propagate = p;
            }
        }
	
        private int			delayed;
        private BeanProp 	bp;
        ArrayList 			events;
	
        public EventMgr(BeanProp bp) {
            this.bp = bp;
            this.delayed = 0;
            this.events = new ArrayList(2);
        }
	
        boolean isDelayed() {
            return (this.delayed > 0);
        }
	
        void delay() {
            if (useEvents())
                this.delayed++;
        }
	
        void addEvent(PropertyChangeEvent e, boolean propagate) {
            if (useEvents()) {
                if (this.isDelayed()) {
                    this.events.add(new Evt(e, propagate));
                } else {
                    this.bp.notifyInternal(e, propagate);
                }
            }
        }
	
        void fireEvents() {
            if (useEvents()) {
                if (this.delayed == 0)
                    return;
		
                this.delayed--;
		
                if (this.delayed == 0) {
                    int size = this.events.size();
                    if (size > 0) {
                        for (int i=0; i<size; i++) {
                            Evt e = (Evt)this.events.get(i);
                            this.bp.notifyInternal(e.event, e.propagate);
                        }
                        this.events.clear();
                    }
                }
            }
        }
    }
    
    static class InternalEvent {
        static final int CHANGED = 1;
        static final int VETOABLE = 2;
	
        int type;
        Object obj;
	
        public InternalEvent(int type, Object obj) {
            this.type = type;
            this.obj = obj;
        }
	
        PropertyChangeEvent getPropertyChangeEvent() {
            return (PropertyChangeEvent)this.obj;
        }
    }
    
    //
    //	Used to define the setter operation of an indexed property.
    //	This is used when we call the local raiseVetoableEvent() method
    //	to know what the user plan to do in order to build the new value.
    //
    private static final int OP_SETTER_SETARRAY 	= 1;
    private static final int OP_SETTER_SETELT 		= 2;
    private static final int OP_SETTER_ADD		= 4;
    private static final int OP_SETTER_REMOVE		= 8;
    
    //	DTD name of the property
    public String 	dtdName;
    
    //	Java bean name of the property
    public String 	beanName;
    public int		type;

    //	The java class that represent this property (for example, the schema2beans 
    //	generated class)
    public Class	propClass;

    //	Array of DOMBinding, might contain only one element if TYPE_1
    ArrayList		bindings;

    ArrayList		knownValues;
    
    //	List of attributes this property has
    ArrayList		attributes;
    
    //	Bean this property belongs to
    BaseBean		bean;
    
    PropertyChangeSupport	changeListeners;
    VetoableChangeSupport	vetoableListeners;
    
    EventMgr		eventMgr;
    
    boolean		isRoot;
    
    //
    //	This number represents the order of this property amoung all its
    //	sibling properties. This is used to create the DOM elements in
    //	the right place, with respect to the DTD declaration.
    //
    //	For example, if we have <!ELEMENT a (b, c, d)>, then the order
    //	values for the properties b, c and d are: 1, 2 and 3.
    //	If we need to insert the property c, we know that is should
    //	be inserted before the property d, simply looking at its order
    //	number (see the BaseBean.createProperty for comment on the order
    //	value)
    //
    private int				order;
    
    //
    //	If this property is defined as or'd (char |) in the DTD file,
    //	the following GroupProp instance holds references to the other
    //	BeanProp sharing the same or'd definition (a | b | c ...)
    //	The following group reference should be null if the property
    //	is simply defined as an 'and sequence' (a, b, c ...)
    //
    GroupProp		group;
    
    
    public BeanProp(BaseBean bean, String dtdName, String beanName,
		    int type, Class propClass) {
        this(bean, dtdName, beanName, type, propClass, 10);
    }
    
    public BeanProp(BaseBean bean, String dtdName, String beanName,
		    int type, Class propClass, int initialCapacity) {
        //System.out.println("intitialCapacity="+initialCapacity);
	this.dtdName = dtdName;
	this.beanName = beanName;
	this.type = type;
	this.propClass = propClass;
	this.bean = bean;
    if (initialCapacity >= 0) {
        this.bindings = new ArrayList(initialCapacity);
        this.attributes = new ArrayList(initialCapacity);
    }
	this.changeListeners = null;
	this.vetoableListeners = null;
	this.knownValues = null;
	this.isRoot = false;
	this.order = 0;
	this.eventMgr = new EventMgr(this);
	this.group = null;
    }
    
    public BeanProp(BaseBean bean, String dtdName, String beanName,
		    int type, Class propClass, boolean isRoot) {
        this(bean, dtdName, beanName, type, propClass, isRoot, 10);
    }

    public BeanProp(BaseBean bean, String dtdName, String beanName,
		    int type, Class propClass, boolean isRoot, int initialCapacity) {
	this(bean, dtdName, beanName, type, propClass, initialCapacity);
	this.isRoot = isRoot;
    }

    /**
     *	Called by the BaseBean class when the property is fully created.
     *	(no more creation work needs to be done in the BaseBean for this
     *	property.). This is where any extra initialization work should take
     * 	place if this couldn't be done in the constructor.
     */
    public void initialize() {
	//
	//  If the property us or'd with others, group these properties
	//  together. The following assumes that the property are created
	//  through the BaseBean in the DTD order (which is how the GenBeans
	//  works), and also assumes that two sets of or'd properties never
	//  follow each others. (for example: (a | b) (c | d)).
	//
	if (Common.isSequenceOr(this.type)) {
	    //	Try to get the previous reference, else create a new one
	    //	(first element in the list of or'd elements)
	    BeanProp previousProp = null;
	    
	    if (this.order > 1) {
		previousProp = this.bean.beanProp(this.order-1);
	    }
	    
	    if ((previousProp != null) && (previousProp.group != null)) {
		//  The previous property already have one - use it
		previousProp.group.add(this);
		this.group = previousProp.group;
	    }
	    
	    if (this.group == null) {
		this.group = new GroupProp(this);
	    }
	}
    }
    
    boolean useEvents() {
	return handleEvents;
    }
    
    boolean useVetoEvents() {
	return handleVetoEvents;
    }
    
    void setOrder(int order) {
	this.order = order;
    }
    
    public Class getPropClass() {
	return this.propClass;
    }
    
    public int getType() {
	return this.type;
    }
    
    public String getBeanName() {
	return this.beanName;
    }
    
    public BaseBean getBean() {
	return this.bean;
    }
    
    public BaseBean getParent() {
	return this.getBean();
    }

    NodeFactory getNodeFactory() {
	return this.bean.graphManager().getNodeFactory();
    }
    
    Node getParentNode() {
	//  If we are the root, there is no parent node
	if (this.isRoot) {
	    return null;
	}
	
	Node	    n = null;
	DOMBinding  b = this.bean.domBinding();
	
	if (b != null) {
	    n = b.getNode();
	}
	
	if (n == null) {
	    throw new IllegalStateException(Common.getMessage("ParentNodeCantBeNull_msg"));
	} else {
	    return n;
	}
    }
    
    //
    //	This method is called by the DOMBinding class when a DOM object
    //	needs to be inserted into the DOM graph. The goal of this method
    //	is to find out the next property (using the order value of the
    //	BeanProp) that has a DOM node, and if this is an indexed property,
    //	which DOM Node is the first of the list. This method makes sure that
    //	the new elements are inserted in the right property order (as specified
    //	by the DTD)
    //
    Node getFollowingSibling(DOMBinding binding) {
	BeanProp bp = null;
	int	 next = this.order+1;
	
	bp = this.bean.beanProp(next++);
	
    //System.out.println("getFollowingSibling next="+next+" bp="+bp);
	while (bp != null) {
	    boolean	found = false;
	    int 	size = bp.size();
	    
	    //	Find out if there is any DOM Node
	    for (int i=0; i<size; i++) {
		DOMBinding b = bp.getBinding(i);
		if ((b != null) && (b.getNode() != null)) {
		    found = true;
		    break;
		}
	    }
	    
	    if (found)
		break;
	    
	    bp = this.bean.beanProp(next++);
	}
	
	if (bp != null) {
	    if (Common.isArray(bp.type)) {
            //
            //  We have a list of DOMBindings (then DOM nodes) that
            //  might be mixed (the first elt might not be the first node
            //  in the DOM graph). So, we have to find out in the list
            //  of element which one is the first one in the graph.
            //
            int size = bp.size();
            for (int i=0; i<size; i++) {
                DOMBinding b1 = bp.getBinding(i);
                if (b1 != null) {
                    boolean found = false;
                    Node n1 = b1.getNode();
                    Node p = n1.getPreviousSibling();
                    // skip the whitespace between nodes
                    while (p instanceof Text)
                        p = p.getPreviousSibling();
			
                    //  If p is not amough the other, we have the first elt
                    for (int j=0; j<size; j++)
                        if ( i != j) {
                            DOMBinding b2 = bp.getBinding(j);

                            if ((b2 != null) && (b2.getNode() == p)) {
                                //	n is not the first one
                                found = true;
                                break;
                            }
                        }
			
                    //  We got the first element of our indexed property
                    if (!found) {
                        return n1;
                    }
                }
            }
	    }
	    else {
            //  Single property - only one node, return it
            return bp.getBinding(0).getNode();
	    }
	}
	
	return null;
    }
    
    
    DOMBinding getBinding(int index) {
	return (DOMBinding)this.bindings.get(index);
    }

    protected int bindingsSize() {
        return bindings.size();
    }
    
    //////
    //
    //	Add, Get, Remove values
    //
    
    public Object getValue(int index) {
        if (!Common.isArray(this.type)) {
            //	Value not set for single type property - return null
            if ((index > 0) || (this.bindingsSize() == 0))
                return null;
        }
        if (bindingsSize() == 0) {
            // Ok no value set so far. return null.
            return null;
        } // end of if (this.bindingsSize() == 0)

        DOMBinding b = (DOMBinding)this.bindings.get(index);

        if (b != null)
            return b.getValue(this);
        else
            return null;
    }
    
    
    /**
     *	Return the element which as the unique DOMBinding identifier id
     *	(Every DOMBinding has a unique identifier which identifies uniquely
     *	the DOM element referenced by the DOMBinding object. This id is,
     *	for example, used to build the absolute name of an elt of the graph)
     */
    public Object getValueById(int id) {
        int size = this.bindingsSize();
        for (int i=0; i<size; i++) {
            DOMBinding b = (DOMBinding)this.bindings.get(i);
	    
            if (b.id == id)
                return b.getValue(this);
        }
        return null;
    }
    
    /**
     *	Convert the relative index value (the property index value) into
     *	the unique DOMBinding id.
     */
    public int indexToId(int index) {
        if (index>=0 && index<this.bindingsSize()) {
            DOMBinding b = (DOMBinding)this.bindings.get(index);
            if (b != null)
                return b.id;
        }
        return -1;
    }
    
    /**
     *	Convert a unique DOMBinding Id into a relative index value
     *  This method may return -1 if we cannot figure out the index.
     */
    public int idToIndex(int id) {
        int size = this.bindingsSize();
        for (int i=0; i<size; i++) {
            DOMBinding b = (DOMBinding)this.bindings.get(i);
            
            if ((b != null) && (b.id == id))
                return i;
        }
        return -1;
    }
    
    //
    //	Build an Object[] of the current indexed property value
    //	Make room for extra elements at the end of the array.
    //
    protected Object[] getObjectArray(int extraElements) {
	int size = this.bindingsSize();
	Object a = 
	    Array.newInstance(this.propClass,
			((size+extraElements>=0)?(size+extraElements):size));
	
	//	If we have enough space
		int i=0;
		try {
            if (extraElements >= 0) {
                for (i=0; i<size; i++)
                    Array.set(a, i, this.getValue(i));
            }
		} catch(IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("bean: " + this.getName() + "dtdname: " +
                               this.getDtdName() +
                               "class: " + this.getPropertyClass());
            Object o = this.getValue(i);
            if (o != null)
                System.err.println("elt: " + o.toString() + " - " +
                                   o.getClass());
            else
                System.err.println("elt is null for index " + i);

            try {
                this.getBean().write(System.err);
                System.err.println(this.getBean().dumpDomNode());
            } catch (java.io.IOException e2) {
            }
            throw e;
		}
	
	return (Object[])a;
    }
    
    
    /**
     *	If the property is an indexed property, return all the values
     *	as an array of its type.
     */
    public Object[] getValues() {
	if (!Common.isArray(this.type))
	    throw new IllegalStateException(Common.
		getMessage("NotIndexedProperty_msg"));
	
	return this.getObjectArray(0);
    }
    
    /**
     *	Set the whole index property. Element might have changed, been removed
     *	added or just mixed.
     */
    public void setValue(Object[] value) {
        if (Common.isVetoable(this.type) && useVetoEvents()) {
            this.raiseVetoableEvent(value, 0, OP_SETTER_SETARRAY);
	    
            //	If we reach this point, no exception has been raised,
            //	and the change can happen.
        }
	
        //
        //	Remove deleted element, add the new ones then sort the new array.
        //
        DOMBinding b;
        int newSize = 0;
        int size = this.bindingsSize();
        boolean found;
        boolean	skipNew[] = null;
        int i, j;
        boolean changed = false; // track of any change
        Object oldValue = null;
	
        if (useEvents()) {
            oldValue = Array.newInstance(this.propClass, size);
        }
	
        //	Do not send any event while updating the array
        this.eventMgr.delay();
	
        if (value != null) {
            newSize = value.length;
            skipNew = new boolean[newSize];
            //	Remove all unused elements
            Arrays.fill(skipNew, false);
        }
	
        //  Mark the original position of the DOM nodes
        for (i=0; i<size; i++) {
            DOMBinding d = (DOMBinding)this.bindings.get(i);
            if (d != null)
                d.posDOM = i;
        }
	
        for (i=0; i<size; i++) {
            found = false;
            Object o = this.getValue(i);
            if (o == null)
                continue;
	    
            //	Do a complete loop search for same object ref
            for (j=0; j<newSize; j++)
                if (!skipNew[j]) {
                    if (value[j] == null)
                        continue;
                    if (o == value[j]) {
                        found = true;
                        break;
                    }
                }
	    
            //	Go now for same object content
            if (!found) {
                for (j=0; j<newSize; j++)
                    if (!skipNew[j]) {
                        if (value[j] == null)
                            continue;
                        if (o.equals(value[j])) {
                            found = true;
                            break;
                        }
                    }
            }
	    
            if (!found) {
                //  No more in the new list - remove it
                if (useEvents()) {
                    if (Common.isBean(this.type))
                        Array.set(oldValue, i, ((BaseBean)o).clone());
                    else
                        Array.set(oldValue, i, o);
		    
                    changed = true;
                }
                this.removeElement(i, false);
            }
            else {
                //  Also in the original list - skip this one
                if (useEvents()) {
                    Array.set(oldValue, i, o);
                    if (i != j)
                        changed = true;
                }
                skipNew[j] = true;
                b = (DOMBinding)this.bindings.get(i);
                //  Will live at position j
                b.pos = j;
            }
        }
	
        //  Add the new ones
        for (i=0; i<newSize; i++)
            if (!skipNew[i]) {
                if (value[i] != null) {
                    //	Add value
                    int idx = this.setElement(0, value[i], true);
                    b = (DOMBinding)this.bindings.get(idx);
                    //	Should live at position i
                    b.pos = i;
                    // currently located at position idx in the DOM
                    b.posDOM = idx;
                    changed = true;
                }
            }
	
        //  Sort the final array to match the parameter elements order
        ArrayList newBindings = new ArrayList(newSize);
        for (i=0; i<newSize; i++)
            newBindings.add(null);
        newBindings.ensureCapacity(newSize+1);
        size = this.bindingsSize();
        for (i=0; i<size; i++) {
            b = (DOMBinding)this.bindings.get(i);
            if (b != null) {
                newBindings.set(b.pos, b);
            }
        }
        //  This should match the parameter order
        this.bindings = newBindings;
	
        if (changed && this.bean.binding != null) {
            //	Also trigger an event for the whole indexed property
            PropertyChangeEvent e = this.createEvent(this.bean.binding,
                                                     oldValue, value, null);
            this.notifyInternal(e, true);
        }
	
        //
        //  We have now to reorder the DOM nodes
        //
        if (changed) {
            for (i=0; i<newSize; i++) {
                DOMBinding d1 = (DOMBinding)this.bindings.get(i);
                if (d1 == null)
                    continue;
		
                DOMBinding db = null;

                //  Do we have anyone before us (more left than us)
                int min = d1.posDOM;
                for (j=i+1; j<newSize; j++) {
                    DOMBinding d2 = (DOMBinding)this.bindings.get(j);
                    if (d2.posDOM < min) {
                        min = d2.posDOM;
                        db = d2;
                    }
                }
                //  If found one, insert before as re-order from the left
                if (db != null)
                    d1.moveBefore(this, db.getNode());
            }
        }
	
        //  If anything changed, fire the events now
        this.eventMgr.fireEvents();
    }
    
    /**
     *	Set the value for one element of an indexed property
     */
    public void setValue(int index, Object value) {
	if (Common.isVetoable(this.type) && useVetoEvents()) {
	    //	Check before doing any change if this is ok
	    this.raiseVetoableEvent(value, index, OP_SETTER_SETELT);
	    
	    //	If we reach this point, no exception has been raised,
	    //	and the change can happen.
	}
	
	this.setElement(index, value, false);
    }
    
    /**
     * Add a value at the end of the indexed property
     */
    public int addValue(Object value) {
	if (Common.isVetoable(this.type) && useVetoEvents()) {
	    //	Check before doing any change if this is ok
	    this.raiseVetoableEvent(value, 0, OP_SETTER_ADD);
	    
	    //	If we reach this point, no exception has been raised,
	    //	and the change can happen.
	}
	
	return this.setElement(0, value, true);
    }
    
    /**
     *	Remove the object that have the bean value (.equals call)
     *
     *	removeValue do not leave null slots as the setValue does.
     *
     *	Remove tries first to find out the same object reference
     *	within the list of properties values. If not, it tries
     *	to find out an object which is equal.
     */
    public int removeValue(Object value) {
	if (Common.isVetoable(this.type) && useVetoEvents()) {
	    //	Check before doing any change if this is ok
	    this.raiseVetoableEvent(value, 0, OP_SETTER_REMOVE);
	    
	    //	If we reach this point, no exception has been raised,
	    //	and the change can happen.
	}
	
	
	int size = this.bindingsSize();
	int index = -1;
	
	for (int i=0; (i<size) && (index == -1); i++) {
	    DOMBinding 	b = (DOMBinding)this.bindings.get(i);
	    if (b != null) {
		Object o = b.getValue(this);
		if ((o!=null) && (o == value))
		    index = i;
	    }
	}
	
	for (int i=0; (i<size) && (index == -1); i++) {
	    DOMBinding 	b = (DOMBinding)this.bindings.get(i);
	    if (b != null) {
		Object	o = b.getValue(this);
		if ((o!=null) && (o.equals(value)))
		    index = i;
	    }
	}
	
	if (index != -1)
	    this.removeElement(index, true);
	else
	    index = 0;
	
	return index;
    }
    
    public void removeValue(int index) {
	if (index >= this.bindingsSize() && 
	    Common.isVetoable(this.type) && useVetoEvents()) {

	    DOMBinding b = (DOMBinding)this.bindings.get(index);
	    if (b != null) {
		Object value = b.getValue(this);

		//	Check before doing any change if this is ok
		this.raiseVetoableEvent(value, 0, OP_SETTER_REMOVE);
	    }
	    
	    //	If we reach this point, no exception has been raised,
	    //	and the change can happen.
	}

	this.removeElement(index, true);
    }
    
    //	Common sanity check on the parameters
    private void checkParams(int index, Object value, boolean add) {
	if (add) {
	    if (value == null)
		throw new IllegalArgumentException(Common.
		    getMessage("CannotAddNullValue_msg"));
	}
	else {
	    if (!Common.isArray(this.type)) {
		if (index > 0)
		    throw new IllegalArgumentException(Common.
			getMessage("InvalidIndexForTypeProperty_msg"));
	    }
	    else {
		if ((index < 0) || (index >= this.bindingsSize()))
		    throw new IndexOutOfBoundsException();
	    }
	}
    }
    
    /**
     *	Different cases might happen in this method, depending on the
     *	type of the property, if the property is an array or not,
     *	and also depending on the type of the value parameter.
     *
     *	If the property is a single property, the value replaces the
     *	previous value.
     *
     *	If the property is an indexed value, the operation is not allowed.
     *
     *	If the propery didn't have any value (no DOM node yet), a DOMBinding
     *	is created and takes care of setting the new value.
     *
     */
    protected int setElement(int index, Object value, boolean add) {
	this.checkParams(index, value, add);
	
	if ((value != null) && Common.isBean(this.type)
	    && ((BaseBean)value).hasDomNode()) {
        Document doc1 = null;
        Document doc2 = null;
        DOMBinding domBinding1 = ((BaseBean)value).domBinding();
        DOMBinding domBinding2 = bean.domBinding();
        //System.out.println("domBinding1="+domBinding1);
        //System.out.println("domBinding2="+domBinding2);
        if (domBinding1 != null && domBinding2 != null) {
            Node node1 = domBinding1.getNode();
            Node node2 = domBinding2.getNode();
            //System.out.println("node1="+node1);
            //System.out.println("node2="+node2);
            if (node1 != null && node2 != null) {
                doc1 = node1.getOwnerDocument();
                doc2 = node2.getOwnerDocument();
            }
        }
        //System.out.println("doc1="+doc1);
        //System.out.println("doc2="+doc2);
        //System.out.println("doc1==doc2="+(doc1==doc2));
        if (doc1 != null && doc1 == doc2) {
            //
            //	For now, reject any attempt to insert an element which
            //	is already part of the DOM graph.
            //	We could clone the element automatically, but for now,
            //	we just reject it.
            //
            throw new IllegalArgumentException(Common.
                                               getMessage("CannotInsertElementAlreadyInGraph_msg"));
        }
	}

    /*
	//  If String, store a copy of the string
	if (value instanceof java.lang.String) {
	    
	    //	An empty optional String is equivalement to a null string
	    if (!this.isBean()
	        && ((this.type & Common.MASK_INSTANCE) == Common.TYPE_0_1)
            && value.equals("")) { // NOI18N    
		    
            value = null;
	    } else {
            value = new String((String)value);
	    }
	}
    */
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 1, DDLogFlags.SETVALUE,
	    this.dtdName + "[" + index + "] - " +
	    (value==null?"null":value.toString()));
	}
	
	//
	//  If the object is null, the setValue call is equivalent to a remove
	//  Otherwise, the object might be a bean or a wrapper object (String,
	//  Boolean, Date, ...)
	//
	//  If the object is a bean, we have to remove first the previous bean.
	//  Then, we affect the new bean to the DOMBinding object.
	//  (See the comment in the delete() operation to see how this
	//  involves the BeanProp and DOMBinding data structure).
	//
	//  If the object is a wrapper, simply call the DomBinding to change
	//  the DOM node value (there is no change in the internal structure).
	//
	
	//  Do not send any event while updating
	this.eventMgr.delay();
	
	if (value != null) {
	    //	Before adding the new value, remove the previous one
	    if (Common.isBean(this.type) && !add)
            this.removeElement(index, false);
	    
	    DOMBinding b = null;
	    boolean empty = true;
	    Object	oldValue = null;
	    
	    if (!add) {
            empty = (this.bindingsSize() == 0);
            if (!empty)
                b = (DOMBinding)this.bindings.get(index);
	    }
	    
	    if (b == null) {
            //
            //  This is a brand new property - create the DOMBinding
            //  for this object. The DOMBinding will take care
            //  later on to create the proper DOM tree structures.
            //
            b = new DOMBinding();
            b.register(this, value);
            b.setDefaultAttributeValues(this);
	    }
	    
	    if (add)
            index = this.bindingsSize();
	    
	    if (empty)
            this.bindings.add(b);
	    else
            this.bindings.set(index, b);
	    
	    if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_BLD, 1, DDLogFlags.NEWBIND,
                            this.dtdName + "[" + (empty?0:index) + "]");
	    }
	    
	    oldValue = b.setValue(this, value);
	    
	    //
	    //	If the parent bean is attached to a DOM Node, this new
	    //	value has to be attached to the DOM Node. Otherwise,
	    //	the new value stays cached in the DOMBinding.
	    //	The algorithm is: if the parent is not anchored to the DOM
	    //	tree, cache the value. If the parent is attached,
	    //	flush into the DOM tree all the cached values.
	    //
	    if (this.bean.hasDomNode()) {
            b.syncNodes(this, new Action(Action.ADD));
	    }
	    else {
            if (DDLogFlags.debug) {
                TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                                DDLogFlags.DBG_BLD, 1, DDLogFlags.CACHING,
                                this.dtdName);
            }
	    }
	    
	    //
	    //	Now that the setValue is done, ask the DOMBinding to signal
	    //	all the properties bound to this Node (including ourself).
	    //
	    b.notifyBeansForChange(oldValue, value, null);
	    
	    //	Attribute values might have been cached in the bean
	    if (Common.isBean(this.type) && (value != null)) {
            BaseBean bb = (BaseBean)value;
            String[] names = bb.cachedAttributeNames();
            for (int i=0; i<names.length; i++) {
                this.setAttributeValue(index, names[i],
                                       bb.cachedAttributeValue(names[i]));
            }
            bb.cachedAttributeClear();
	    }
	}
	else
	    this.removeElement(index, false);
	
	//	If anything changed, fire the events now
	this.eventMgr.fireEvents();
	
	return index;
    }
    
    
    /**
     *	Tell the binding to remove the Node reference.
     */
    private void removeElement(int index, boolean remove) {
	DOMBinding b;
	
	if (index >= this.bindingsSize()) {
	    // There is nothing here so do nothing.
	    return;
	}
	
	b = (DOMBinding)this.bindings.get(index);
	if (b != null) {
	    Object oldValue = b.getValue(this);
	    b.setLastKnownIndex(this, index);
	    
	    PropertyChangeEvent e = this.createEvent(b, oldValue, null, null);
	    
	    if (DDLogFlags.debug) {
		TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
		DDLogFlags.DBG_EVT, 1, DDLogFlags.CREATEREM,
		(e==null?"no-event":e.getPropertyName()) +
		" - source " + this.beanName +
		"\n\t\toldValue is " +
		(oldValue==null?"<null>":"<"+
		oldValue.toString()+">") +
		"\n\t\tnewValue is null");
	    }
	    
	    if (remove)
		this.bindings.remove(index);
	    else
		this.bindings.set(index, null);
	    
	    //	Remove and notify the subtree included ourself
	    b.syncNodes(this, new Action(Action.REMOVE, e));
	    
	    //	Notify the parents
	    this.notifyInternal(e, true);
	}
    }
    
    //	Return the list of well known values
    public Object[] knownValues() {
	if (this.knownValues == null)
	    return null;
	
	int size = this.knownValues.size();
	Object a = Array.newInstance(this.propClass, size);
	
	for (int i=0; i<size; i++)
	    Array.set(a, i, this.knownValues.get(i));
	
	return (Object[])a;
    }
    
    //	Add a value to the list of well known defined values
    protected void addKnownValue(Object value) {
	if (this.knownValues == null)
	    this.knownValues = new ArrayList();
	
	if (this.propClass.isAssignableFrom(value.getClass()))
	    this.knownValues.add(value);
    }
    
    
    //
    //	Values methods.
    //
    //////////////////
    
    
    /////////////////////
    //
    //	Attributes methods
    //
    
    /**
     *	Attributes definition live in the BeanProp object while attribute
     *	values live in the DOMBinding objects.
     */
    public void createAttribute(String dtdName, String name, int type,
				String[] values, String defValue) {
	AttrProp ap = new AttrProp(this.dtdName, dtdName, name, type,
	values, defValue);
	this.attributes.add(ap);
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 5, DDLogFlags.CREATEATTR,
	    this.beanName + ": " + ap.toString());
	}
    }
    
    /**
     *	When the bean tree is built from an XML document and an attribute
     *	not defined in the DTD is found, the attribute is dynamically added
     *	to the list of defined attribute with the TRANSIENT status.
     */
    public void createTransientAttribute(String dtdName) {
	//	Add the attribute only if we don't know about it
	AttrProp ap = this.getAttrProp(dtdName, true);
	
	if (ap != null)
	    return;
	
	ap = new AttrProp(this.dtdName, dtdName, Common.convertName(dtdName),
	AttrProp.CDATA | AttrProp.IMPLIED |
	AttrProp.TRANSIENT, null, null);
	
	this.attributes.add(ap);
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 5, DDLogFlags.CREATEATTR,
	    this.beanName + ": " + ap.toString());
	}
    }
    
    /**
     *	Misc: convert the ArrayList into an array of AttrProp
     */
    public BaseAttribute[] getAttributes() {
	int size = this.attributes.size();
	AttrProp[] ret = new AttrProp[size];
	return (AttrProp[])this.attributes.toArray(ret);
    }
    
    /**
     *	Misc: returns an array of the attribute names
     */
    public String[] getAttributeNames() {
	int	    size = this.attributes.size();
	String[]    ret = new String[size];
	
	for (int i=0; i<size; i++) {
	    AttrProp ap = (AttrProp)this.attributes.get(i);
	    ret[i] = ap.getName();
	}
	return ret;
    }
    
    /**
     *	Return the property name AttrProp
     */
    public AttrProp getAttrProp(String name, boolean quiet) {
	int	size = this.attributes.size();
	
	for (int i=0; i<size; i++) {
	    AttrProp ap = (AttrProp)this.attributes.get(i);
	    if (ap.hasName(name))
		return ap;
	}
	
	if (!quiet)
	    throw new IllegalArgumentException(Common.
		getMessage("UnknownAttributeForProperty_msg", name, 
			   this.beanName));
	else
	    return null;
    }
    
    /**
     *	Misc: return the property of name 'name'
     */
    public AttrProp getAttrProp(String name) {
	return this.getAttrProp(name, false);
    }
    
    /**
     *	Set the value of the attribute
     */
    public void setAttributeValue(int index, String name, String value) {
	AttrProp ap = this.getAttrProp(name);
	
	this.checkParams(index, null, false);

    // If it's fixed and they're changing it to a different value than the
    // default fixed value, then we've got an error.  Note that the
    // comparison ought to be a value base comparison, and not a string
    // based comparison.
	if (ap.isFixed() && (ap.defaultValue == null || !ap.defaultValue.equals(value)))
	    throw new IllegalStateException(Common.
		getMessage("CannotChangeFIXEDAttribute_msg"));
	
	if (ap.isEnum() && (value != null)) {
	    String[] values = ap.getValues();
	    boolean found = false;
	    for (int i=0; i<values.length; i++) {
		if (values[i].equals(value)) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		throw new IllegalArgumentException(Common.
		    getMessage("ValueDoesNotMatchEnumValues_msg",
			       value, ap.enumsToString()));
	    }
	}
	
	DOMBinding b = null;
	
	//	The attribute value lives in the DOMBinding object
	if (index != 0 || this.bindingsSize() != 0) {
	    b = (DOMBinding)this.bindings.get(index);
    } else if (DDLogFlags.debug) {
        System.err.println("What DOMBinding should I use for BeanProp.setAttributeValue?!?");
    }
	
	if (b != null) {
	    Object oldValue = b.getAttributeValue(this, ap.getDtdName());
	    b.setAttributeValue(this, ap.getDtdName(), normalizedAttrValue(value));
	    
	    //	Notify about this attribute changed value
	    b.notifyBeansForChange(oldValue, value, name);
	}
    }
    /** Checking attr value for correct chars, replace illegal chars with '?'
     */
    private String normalizedAttrValue(String value) {
        if (value==null) return null;
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<value.length();i++) {
            char ch = value.charAt(i);
            if (XMLUtil.isAttrContent((int)ch)) {
                sb.append(ch);
            } else {
                sb.append('?'); // illegal character replaced by '?'
            }
        }
        return sb.toString();
    }
    
    /**
     *	Get the attribute value
     */
    public String getAttributeValue(int index, String name) {
	String 		ret = null;
	AttrProp 	ap = this.getAttrProp(name);
	
	if (!Common.isArray(this.type)) {
	    //	Value not set for single type property - return null
	    if ((index > 0) || (this.bindingsSize() == 0))
		return null;
	}
	
	DOMBinding b = (DOMBinding)this.bindings.get(index);
	
	if (b != null)
	    ret = b.getAttributeValue(this, ap.getDtdName());
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 10, DDLogFlags.GETATTR,
	    this.beanName + "[" + index + "]." +
	    ap.getDtdName() + ": " + ret + " " +
	    ((b==null)?"<no binding>":("B("+b.hashCode()+")"))+
	    " BP(" + this.hashCode() + ")");
	}
	
	return ret;
    }
    
    //
    //	Attributes methods
    //
    ///////////////////////
    
    
    
    /**
     *	Called back by the DOMBinding when another bean removed the binding.
     *	This allows an automatic update of all the bean trees sharing the
     *	same DOM nodes.
     */
    void removeBinding(DOMBinding binding) {
	throw new UnsupportedOperationException(Common.
	    getMessage("NotImplementedYet_msg"));
    }
    
    
    /**
     *	Create the DOM Nodes for all the properties previously cached.
     *	(Cached because no beans/values created could be attached
     *	to the real DOM tree: no element was attached to the DOM tree
     *	at the time of their creation - see the setElement() method)
     */
    void syncNodes(Action a) {
	int 		size = this.bindingsSize();
	
	for (int i=0; i<size; i++) {
	    DOMBinding b = (DOMBinding)this.bindings.get(i);
	    if (b != null)
		b.syncNodes(this, a);
	}
    }
    
    /**
     *	This method is recursively called to build the full path name
     *	of a property.
     *
     *	The full path name starts at the root of the graph and consists in:
     *
     *		- the concatenation of each property name from the root
     *		- a unique identification index for each indexed property
     *
     *	For example /Book/Chapter.23a/Paragraph.34c/Comment
     *
     *	The unique identifier is the hexadecimal representation of the
     *	unique integer identifier of the DOM Binding, which is independant
     *	from the position of the property in the array of the indexed prop.
     *	If the array change, the name still represents the same object,
     *	as long as every DOMBinding object of the path exists.
     */
    void buildPathName(DOMBinding binding, StringBuffer str) {
	if (binding == null)
	    return;
	
	if (Common.isArray(this.type)) {
	    DOMBinding b = null;
	    int size = this.bindingsSize();
	    int index = binding.getLastKnownIndex(this);
	    
	    for (int i=0; i<size; i++) {
		b = (DOMBinding)this.bindings.get(i);
		if (b == binding)
		    break;
		else
		    b = null;
	    }
	    
	    //
	    //	Used when an element is deleted: we store its index
	    //	since we won't be able to find it later from
	    //	its unique id
	    //
	    if (index != -1) {
		//  We didn't find any index - mark as removed with the
		//  last known index
		str.insert(0, "i" + index);	// NOI18N
	    }
	    
	    //
	    //	We might not find the original index of the element if it has
	    //	been already removed. Use the default value -1 in this case.
	    //	Note that this value doesn't really matter since the
	    //	index parameter value (prefixed with 'i') is the meaningful
	    //	value in this situation.
	    //
	    if (b != null)
		str.insert(0, b.idToString());
	    else
		str.insert(0, "-1");	// NOI18N
	    
	    //	Unique id for the index
	    str.insert(0, ".");	// NOI18N
	}
	str.insert(0, this.beanName);
	str.insert(0, "/");	// NOI18N
	
	if (!this.isRoot && (this.bean != null))
	    this.bean.buildPathName(str);
    }
    
    /**
     *	Create a change event for the current property
     */
    PropertyChangeEvent createEvent(DOMBinding b, Object oldValue,
				    Object newValue, String attrName) {
	if (!useEvents() && !useVetoEvents())
	    return null;
	
	StringBuffer name = new StringBuffer();
	
	if (attrName != null) {
	    name.append(":");	// NOI18N
	    name.append(attrName);
	}
	
	this.buildPathName(b, name);
	
	return new PropertyChangeEvent(this.bean, name.toString(),
					oldValue, newValue);
    }
    
    /**
     *	When a DOMBinding has changed (either DOM Node modification
     *	or DOMBinding cached value change), the DOMBinding signals
     *	any property bound to itself about the change.
     *	This is where the event notification take place.
     *
     *	Note that the full path name of the property is used as the
     *	property name of the event.
     */
    PropertyChangeEvent prepareForChangeEvent(DOMBinding b, Object oldValue,
					     Object newValue, String attrName) {
	if (!useEvents())
	    return null;
	
	PropertyChangeEvent e =
	    this.createEvent(b, oldValue, newValue, attrName);
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
	    DDLogFlags.DBG_EVT, 1, DDLogFlags.CREATECHG,
	    (e==null?"no-event":e.getPropertyName()) +
	    " - source " + this.beanName +
	    "\n\t\toldValue is " +
	    (oldValue==null?"<null>":"<"+
	    oldValue.toString()+">") +
	    "\n\t\tnewValue is " +
	    (newValue==null?"<null>":"<"+
	    newValue.toString()+">"));
	}
	return e;
    }
    
    //	Helper method
    void notifyInternal(PropertyChangeEvent e, boolean propagate) {
	this.notifyInternal(new InternalEvent(InternalEvent.CHANGED, e),
	propagate);
    }
    
    //	Mechanism used to propagate an event through the tree up to the root
    void notifyInternal(InternalEvent ie, boolean propagate) {
	if (ie.type == InternalEvent.CHANGED) {
	    if (!useEvents())
		return;
	    
	    if (this.eventMgr.isDelayed()) {
		this.eventMgr.addEvent(ie.getPropertyChangeEvent(), propagate);
		return;
	    }
	    
	    if (DDLogFlags.debug) {
		TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
		DDLogFlags.DBG_EVT, 1, DDLogFlags.NOTIFYCHG,
		ie.getPropertyChangeEvent().getPropertyName()+
		(propagate?"(P)":"") +
		" in " + this.beanName + " - " +
		(this.changeListeners==null?"null listener":
		    (this.changeListeners.hasListeners(null)?
		    "has listeners":"no listener")) );
	    }
	    
	    if (this.changeListeners != null)
		//  Notify the object listening on this property
		this.changeListeners.firePropertyChange(
		    ie.getPropertyChangeEvent());
	}
	else
	    if (ie.type == InternalEvent.VETOABLE) {
		if( !useVetoEvents())
		    return;
		
		if (DDLogFlags.debug) {
		    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
		    DDLogFlags.DBG_EVT, 1, DDLogFlags.NOTIFYVETO,
		    ie.getPropertyChangeEvent().getPropertyName()+
		    (propagate?"(P)":"") +
		    " in " + this.beanName + " - " +
		    (this.vetoableListeners==null?"null listener":
			(this.vetoableListeners.hasListeners(null)?
			"has listeners":"no listener")) );
		}
		
		try {
		    if (this.vetoableListeners != null)
			//  Notify the object listening on this property
			this.vetoableListeners.fireVetoableChange(
			    ie.getPropertyChangeEvent());
		}
		catch(PropertyVetoException ve) {
		    //	This has been vetoed, raise our internal runtime exception
		    //	holding this ve. We'll rethrow it later on, from the
		    //	generated beans.
		    throw new BaseProperty.VetoException(ve,
			Common.getMessage("ChangeForPropertyVetoed_msg",
					  this.beanName));
		}
	    }
	
	//  Notify the parent (this will go up to the root)
	if (!this.isRoot && (this.bean != null) && propagate)
	    this.bean.notifyInternal(ie);
    }
    
    private void raiseVetoableEvent(Object value, int index, int op) {
	Object curValue = null;
	Object newValue = null;
	
	//  Get the current and new value
	if (Common.isArray(this.type)) {
	    Object []arrValue;
	    
	    curValue = this.getValues();
	    switch(op) {
		case OP_SETTER_SETARRAY:
		    //	The new value is the new array
		    newValue = value;
		    break;
		case OP_SETTER_SETELT:
		    //	One element only will change
		    arrValue = this.getObjectArray(0);
		    arrValue[index] = value;
		    newValue = arrValue;
		    break;
		case OP_SETTER_ADD:
		    //	Add the new element at the end of the array
		    arrValue = this.getObjectArray(1);
		    arrValue[this.bindingsSize()] = value;
		    newValue = arrValue;
		    break;
		case OP_SETTER_REMOVE:
		    //	One element removed
		    
		    //	This call only allocate the array (no values)
		    int i, j;
		    Object[] curValues = (Object[])curValue;
		    arrValue = this.getObjectArray(-1);
		    
		    for (i=0; i<curValues.length; i++) {
			if (curValues[i].equals(value))
			    break;
		    }
		    
		    if (i < curValues.length) {
			//  We found it
			for (i=0, j=0; i<curValues.length; i++)
			    if (!curValues[i].equals(value))
				arrValue[j++] = curValues[i];
			
		    }
		    else {
			arrValue = curValues;
		    }
		    
		    newValue = arrValue;
		    break;
	    }
	}
	else {
	    curValue = this.getValue(0);
	    newValue = value;
	}
	
	//  Create the event and wait for an exception
	PropertyChangeEvent e = this.createEvent(this.bean.binding,
				    curValue, newValue, null);
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
		DDLogFlags.DBG_EVT, 1, DDLogFlags.VETOABLE,
		(e==null?"no-event":e.getPropertyName()) +
		" - source " + this.beanName +
		"\n\t\toldValue is " +
		(curValue==null?"<null>":"<"+
		curValue.toString()+">") +
		"\n\t\tnewValue is " +
		(newValue==null?"<null>":"<"+
		newValue.toString()+">"));
	}
	
	//  Propagate this vetoable event up to the root
	this.notifyInternal(new InternalEvent(InternalEvent.VETOABLE, e),
			    true);
    }
    
    
    /**
     *	Book-keeping of the listeners
     */
    public void addPCListener(PropertyChangeListener l) {
	handleEvents = true;
	if (this.changeListeners == null) {
	    Object obj = (this.bean==null)?(Object)this:(Object)this.bean;
	    this.changeListeners = new PropertyChangeSupport(obj);
	}
	this.changeListeners.addPropertyChangeListener(l);
    }
    
    public void removePCListener(PropertyChangeListener l) {
	if (this.changeListeners != null)
	    this.changeListeners.removePropertyChangeListener(l);
    }
    
    public void addVCListener(VetoableChangeListener l) {
	if (this.checkVetoable(true)) {
	    handleVetoEvents = true;
	    if (this.vetoableListeners == null) {
		Object obj = (this.bean==null)?(Object)this:(Object)this.bean;
		this.vetoableListeners = new VetoableChangeSupport(obj);
	    }
	    this.vetoableListeners.addVetoableChangeListener(l);
	}
    }
    
    public void removeVCListener(VetoableChangeListener l) {
	if (this.checkVetoable(false))
	    this.vetoableListeners.removeVetoableChangeListener(l);
    }
    
    
    //
    //	A property might not support the veto mechanism either because
    //	it has not been generated to support it, or because it is a bean.
    //
    private boolean checkVetoable(boolean raise) {
	if (Common.isVetoable(this.type) || Common.isBean(this.type))
	    return true;
	
	if (raise) {
	    if (!Common.isBean(this.type)) {
		throw new Schema2BeansRuntimeException(Common.
		    getMessage("PropertyDoesntSupportVeto_msg", this.beanName));
	    }
	}
	
	return false;
    }
    
    /**
     *	If the current property is a bean, returns an instance of the bean
     */
    BaseBean newBeanInstance() {
	if (Common.isBean(this.type)) {
	    try {
		Constructor c = null;
		
		try {
		    Class[] cc = new Class[] {int.class};
		    c = this.propClass.getDeclaredConstructor(cc);
		}
		catch(NoSuchMethodException me) {
		    return (BaseBean)this.propClass.getDeclaredConstructor().newInstance();
		}
		
		//  Do not initialize the default values
		Object[] p =
		    new Object[] {Integer.valueOf(Common.NO_DEFAULT_VALUES)};
		
		return (BaseBean)c.newInstance(p);
	    }
	    catch(Exception e) {
		TraceLogger.error(e);
		throw new Schema2BeansRuntimeException(Common.
		    getMessage("CantInstantiateBean_msg", e.getMessage()));
	    }
	}
	return null;
    }
    
    /**
     *	This method is called to associate this bean property to a DOM Node.
     *	When the bean graph is built from the DOM graph, the DOM graph is
     *	parsed while trying to match the element names of the DOM graph
     *	with the property names of the bean graph.
     *
     *	When a match is found between a bean property name and a DOM element
     *	name, this method is called to associate the DOM node to the bean.
     *
     *	The bean property might be a simple String (#PCDATA in DTD) as the
     *	leaf of the bean graph, or a bean object, containing other beans.
     *	(One node of the bean graph). Whatever the case is, there is a DOM Node
     *	that correspond to this property.
     *
     *	Also, the propery might be either a single value property or an
     *	array. In the first case, only one Node of the DOM graph can match
     *	the property (if not, this is fatal error in building the bean graph).
     *	In the second case, this property contains an array referencing the
     *	DOM graph.
     *
     *	Note that the property might be of type: optional, mandatory,
     *	an array possibly empty or an array with at least one element.
     *	The verify() method	might be called to check the consistency of the
     *	type of the property, especially after the bean graph is built or
     *	before a modified bean graph has to be saved.
     *
     *	See also the DOMBinding class comments.
     */
    public DOMBinding registerDomNode(Node node, DOMBinding binding,
				      BaseBean bean) throws Schema2BeansException {
	int 		count = 0;
	int 		size = this.bindingsSize();
	
	//	Check that we don't already know this node
	for (int i=0; i<size; i++) {
	    DOMBinding b = (DOMBinding)this.bindings.get(i);
	    if ((b.getNode() == node) || (binding == b))
		throw new Schema2BeansException(Common.
		    getMessage("NodeAlreadyReferenced_msg", node));
	    
	    if (b.getNode() != null) {
		count++;
	    }
	}
	
	
	//	Check we can accept the new node
	if (count==0 || Common.isArray(this.type)) {
	    //
	    //	Where is only one DOMBinding object per node. Use it if
	    //	it already exists. When we are building the first bean
	    //	graph, all DOMBindings are now objects. However, any
	    //	view on the bean graph which is already created,
	    //	uses an existing DOMBinding.
	    //
	    if (binding == null)
		binding = new DOMBinding(node);
	    
	    if (bean == null)
		bean = this.newBeanInstance();
	    
	    binding.register(this, bean);
	    binding.setValue(this, bean);
	    
	    this.bindings.add(binding);
	}
	else {
	    if (DDLogFlags.debug) {
		TraceLogger.put(TraceLogger.DEBUG,
		TraceLogger.SVC_DD,
		DDLogFlags.DBG_BLD, 1,
		DDLogFlags.EXCEEDED,
		"exceeding capacity for " + this.dtdName +
		"(not an array)");
	    }
	    //	A property of type 0_1 or 1 can be bound to only one DOM Node.
	    throw new Schema2BeansException(Common.
		getMessage("PropertyAlreadyBoundToDOMNode_msg", this.dtdName));
	}
	
	return binding;
    }
    
    public String toString() {
	return this.dtdName;
    }
    

    public boolean isRoot() {
	return this.isRoot;
    }
    
    //	BeanProperty implementation
    public String getName() {
	return this.beanName;
    }
    
    public String getDtdName() {
	return this.dtdName;
    }
    
    public boolean isIndexed() {
	return Common.isArray(this.type);
    }
    
    public Class getPropertyClass() {
	return this.propClass;
    }
    
    public boolean isBean() {
	return Common.isBean(this.type);
    }
    
    public int size() {
	return this.bindingsSize();
    }
    
    public String getFullName(int index) {
        return this.buildFullName(index, null);
    }
    
    public String getFullName() {
        return this.buildFullName(0, null);
    }
    
    String buildFullName(int index, String attr) {
        StringBuffer name = new StringBuffer();
	
        if (!Common.isArray(this.type)) {
            //	Value not set for single type property - return null
            if ((index > 0) || (this.bindingsSize() == 0))
                return null;
        }
	
        DOMBinding b = (DOMBinding)this.bindings.get(index);
	
        if (b != null) {
            if (attr != null) {
                name.append(":");	// NOI18N
                name.append(attr);
            }
            this.buildPathName(b, name);
        }
        else
            return null;
	
        return name.toString();
    }
    
    public int getInstanceType() {
	return (this.type & Common.MASK_INSTANCE);
    }
    
    public boolean isChoiceProperty() {
	return (this.group != null);
    }
    
    public BaseProperty[] getChoiceProperties() {
	if (this.isChoiceProperty()) {
	    return this.group.list();
	}
	return null;
    }
    
    //	true if the name is either the mangled name or the dtd name
    public boolean hasName(String name) {
	if (name.equals(this.beanName) || name.equals(this.dtdName))
	    return true;
	else
	    return false;
    }

    public boolean isKey() {
	return Common.isKey(this.type);
    }
}


