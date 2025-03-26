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
import java.beans.*;
import org.w3c.dom.*;
import java.text.MessageFormat;

// To dynamically instanciate a wrapper object
import java.lang.reflect.*;


/**
 *  The DOMBinding class binds the bean properties of the bean graph to
 *  the nodes of the DOM graph. There is one DOMBinding object for
 *  one DOM node.
 *
 *  A bean property never accesses a DOM node directly, but always through
 *  a DOMBinding object. In one bean graph, only one property will be linked
 *  to a DOMBinding object. However, if another graph is created for the same
 *  DOM graph, two bean properties will share the same DOM node using the
 *  DOMBinding object.
 *
 *  As the DOMBinding object might be shared amoung different bean properties,
 *  it keeps a list of all the properties using it. This is how, for example,
 *  events can be fired to the beans of different graphs referencing a same
 *  node, when its value is changed.
 *
 *  The beans of the bean graph lives there.
 */
public class DOMBinding {

    //	This integer uniquely identify this BeanProp in the graph
    int			id;

    Node		node;

    //	This is used by the BeanProp.setter(obj[]) to optimize its parsing
    int			pos;
    //	The same purpose but for ordering the DOM nodes
    int			posDOM;

    private static final Class charArrayClass =
	java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, 0).getClass();

    class BeanProperty {
        //boolean 	changed;
	// BeanProp object containing the property
	BeanProp	beanProp;

	// Null if the property is a String.
	Object		value;

	// Last known real index into the BeanProp array before the removal
	int		lastIndex;

	ArrayList	attributes;

	BeanProperty(BeanProp b) {
	    this.beanProp = b;
	    this.value = null;
	    //this.changed = false;
	    this.lastIndex = -1;
	    this.attributes = null;
	}
    }

    class CacheAttr {
        String name;
        String value;

        CacheAttr(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private BeanProperty prop;

    public DOMBinding() {
        this.id = DDFactory.getUniqueId();
    }

    public DOMBinding(Node node) {
        this();
        // DOM node this DOMBinding refers to
        this.node = node;
    }

    void setNode(Node node) {
        this.node = node;
    }

    void moveBefore(BeanProp prop, Node node) {
        Node parent = prop.getParentNode();

        parent.removeChild(this.node);
        parent.insertBefore(this.node, node);
    }

    public int getId() {
	return this.id;
    }

    String idToString() {
	return Integer.toHexString(this.id);
    }

    Node getNode() {
	return this.node;
    }

    /**
     */
    void register(BeanProp prop, Object value) {
	BeanProperty bp = new BeanProperty(prop);

	if (Common.isBean(prop.type))
	    ((BaseBean)value).setDomBinding(this);

	this.prop = bp;

	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG,
			    TraceLogger.SVC_DD,
			    DDLogFlags.DBG_BLD, 1,
			    DDLogFlags.BINDPROP,
			    "property " + prop.getDtdName() +
			    " bound to B(" + this.hashCode() + ")");
	}

	//
	// Following is a little trick to deal with attribute that are not
	// defined in the dtd. When we register this new element, we ask
	// for all the attributes and add them dynamically, as transient,
	// to the BeanProp list of attributes.
	//
	if (this.node != null) {
	    NamedNodeMap l = this.node.getAttributes();
	    for (int i=0; i<l.getLength(); i++) {
		Node n = l.item(i);
		prop.createTransientAttribute(n.getNodeName());
	    }
	}
    }

    /**
     *  Look in the list of BeanProperty, the one corresponding
     *  to the BeanProp object.
     */
    private BeanProperty getBeanProperty(BeanProp prop) {
	if (this.prop != null && this.prop.beanProp == prop)
	    return this.prop;
	else
	    return null;
    }


    /**
     *  Returns the property BeanProp object associated to the bean object
     *  instance bean. Return null if not found.
     */
    BeanProp getBeanProp(BaseBean bean) {
	if (this.prop != null && this.prop.value == bean)
	    return this.prop.beanProp;
	else
	    return null;
    }

    /**
     *  Used when we remove a property to set the index it used to be
     *  in the BeanProp bindings array. This is how when we build on event
     *  that reference an element which has been removed we know about
     *  its previous index.
     */
    void setLastKnownIndex(BeanProp prop, int index) {
	BeanProperty bp = this.getBeanProperty(prop);
	if (bp != null)
	    bp.lastIndex = index;
    }

    /**
     *  Retreive the last known index stored before removal.
     */
    int getLastKnownIndex(BeanProp prop) {
	BeanProperty bp = this.getBeanProperty(prop);

	if (bp != null)
	    return bp.lastIndex;
	else
	    return -1;
    }

    /**
     *  This method returns the bean associated to the BeanProp object.
     *  Since there can be only one Node per BeanProp (single content as
     *  one bean or one distinct Node per each element if indexed prop),
     *  there is only one entry for this BeanProp object.
     *
     *  The object returned can be either a bean or a wrapper object.
     */
    Object getBean(BeanProp prop) {
	BeanProperty bp = this.getBeanProperty(prop);

	if (bp != null)
	    return bp.value;
	else
	    return null;
    }

    /**
     *	Return the value of the attribute. Get the value from the DOM Node
     *	or from the cache, depending on the existance of the DOM Node.
     */
    String getAttributeValue(BeanProp prop, String name) {
        if (this.node != null) {
            //	Get the value from the DOM Node
            Attr a = ((Element)this.node).getAttributeNode(name);
            if (a != null)
                return a.getValue();
            else
                return null;
        } else {
            //	Get the value from the cache
            BeanProperty bp = this.getBeanProperty(prop);
            if (bp != null && bp.attributes != null) {
                CacheAttr ca = findCacheAttr(bp, name);
                if (ca != null)
                    return ca.value;
            }
        }
        return null;
    }

    private CacheAttr findCacheAttr(BeanProperty bp, String name) {
        for (int i = 0; i < bp.attributes.size(); i++) {
            CacheAttr ca = (CacheAttr)bp.attributes.get(i);
            if (ca.name.equals(name))
                return ca;
        }
        return null;
    }

    /**
     *	Return the value of the attribute. Get the value from the DOM Node
     *	or from the cache, depending on the existance of the DOM Node.
     */
    void setAttributeValue(BeanProp prop, String name, String value) {
        if (this.node != null) {
            if (value != null)
                ((Element)this.node).setAttribute(name, value);
            else {
                String v = ((Element)this.node).getAttribute(name);
                if (v != null) {
                    //
                    //	An empty string might either mean that the attribute
                    //	has an empty value or that the attribute is not
                    //	defined at all (ID type for example).
                    //	The following tries to remove it and ignore the
                    //	fact that the attribute might not be defined.
                    //
                    try {
                        ((Element)this.node).removeAttribute(name);
                    } catch(DOMException e) {
                        // Ignore it
                    }
                }
            }
        } else {
            //	There is no DOM Node, cache the value
            BeanProperty bp = this.getBeanProperty(prop);
            if (bp.attributes == null)
                bp.attributes = new ArrayList();
            CacheAttr ca = findCacheAttr(bp, name);
            if (ca == null) {
                ca = new CacheAttr(name, value);
                bp.attributes.add(ca);
            } else {
                ca.value = value;
            }
        }
    }

    /**
     *	Fill the attribute with the default values - this is typically
     *	called when a new property is created, before it is synced into
     *	a DOM Node. This makes the attributes cached and when the property
     *	is turned into a DOM node, the attributes are also created.
     */
    void setDefaultAttributeValues(BeanProp prop) {
	BeanProperty bp = this.getBeanProperty(prop);
	if (bp != null) {
	    //	Set the default values
	    BaseAttribute[] ap = prop.getAttributes();
	    for (int i=0; i<ap.length; i++) {
		String value = ap[i].getDefaultValue();
		if (value != null)
		    this.setAttributeValue(prop, ap[i].getDtdName(), value);
	    }
	}
    }

    /**
     * Get the value of the DOM node
     */
    public String getDomValue(Node n) {
	StringBuffer str = new StringBuffer();
	this.nodeToString(str, n, true);
	return str.toString();
    }

    //	Method related to XmlToString (see DDFactory)
    private void nodeToString(StringBuffer str, Node n, boolean root) {
	if (root)
	    //	Don't go for siblings on the root
	    nodeChildrenToString(str, n);
	else {
	    for (;n != null; n = n.getNextSibling())
		nodeChildrenToString(str, n);
	}
    }


    //	Method related to XmlToString (see DDFactory)
    private void nodeChildrenToString(StringBuffer str, Node n) {
	String 	value = n.getNodeValue();
	short 	type = n.getNodeType();

	if ((type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) && (value != null))
	    str.append(value);

	if (n.getFirstChild() != null)
	    nodeToString(str, n.getFirstChild(), false);
    }


    /**
     *	Return the value of the property prop. If the property is of type
     *	String, we have to read the value from the DOM Node. If the
     *	type is bean, we just have to return the bean associated to the
     *	DOM Node (we don't have to access the DOM graph in this case).
     */
    Object getValue(BeanProp prop) {
		String ret = null;
	switch(prop.getType() & Common.MASK_TYPE) {
	    case Common.TYPE_STRING:
		//
		//  If this binding is not attached to a DOM tree yet, return
		//  the cached value.
		//
		Class cls = prop.getPropClass();

		if (this.node != null)
		    ret = this.getDomValue(this.node);
		else
		    ret = (String)this.getBean(prop);

        //System.out.println("cls="+cls+" node="+node+" ret="+ret);
		if (!(java.lang.String.class).isAssignableFrom(cls)
		    && (ret != null)) {
            String clsName = cls.getName().intern();
		    //
		    //  Build an instance of the wrapper class (STRING type
		    //  but not a String class). The wrapper must have a
		    //  String constructor or implements the Wrapper interface.
		    //

		    try {
                //  If cls implements Wrapper, use it first
                if ((Wrapper.class).isAssignableFrom(cls)) {
                    Wrapper w = (Wrapper)cls.getDeclaredConstructor().newInstance();
                    w.setWrapperValue(ret);
                    return w;
                }
                //
                //  Not very nice but we want to support the
                //  Character/char case and there is no String
                //  constructor for this core class.
                //
                if ((java.lang.Character.class).isAssignableFrom(cls)) {
                    String 	s = ret.trim();
                    char	c = '\0';
                    if (s.length() == 0) {
                        if (ret.length() != 0)
                            c = ret.charAt(0);
                    }
                    else
                        c = s.charAt(0);

                    return new Character(c);
                }
                if (charArrayClass.isAssignableFrom(cls))
                    return ret.toCharArray();
                if (clsName == "org.netbeans.modules.schema2beans.QName"
                    || clsName == "javax.xml.namespace.QName") {
                    String ns = "";
                    String localPart = null;
                    String prefix = "";
                    int colonPos = ret.indexOf(':');
                    if (colonPos < 0) {
                        localPart = ret;
                    } else {
                        prefix = ret.substring(0, colonPos);
                        localPart = ret.substring(colonPos+1);
                        ns = findNamespace(prefix);
                    }
                    //System.out.println("localPart="+localPart+" ns="+ns+" prefix="+prefix);
                    if (clsName == "org.netbeans.modules.schema2beans.QName") {
                        return new
                            org.netbeans.modules.schema2beans.QName(ns,
                                                                    localPart,
                                                                    prefix);
                    }
                    Constructor c =
                        cls.getDeclaredConstructor(new Class[] {String.class,
                                                                String.class,
                                                                String.class});
                    return c.newInstance(new Object[] {ns, localPart, prefix});
                }
                return JavaBeansUtil.convertValue(cls, ret.trim());
		    } catch(Exception e) {
                //TraceLogger.error(e);
                throw new Schema2BeansRuntimeException(
                             MessageFormat.format(Common.getMessage(
                                         "CantInstantiatePropertyClass_msg"),
                                         new Object[] {cls.getName(), prop.getName(),
                                                        ret, e.getLocalizedMessage()}), e);
		    }
		}
		return ret;

	    case Common.TYPE_BEAN:
		return this.getBean(prop);
		//case BeanProp.TYPE_BOOLEAN:
	    case Common.TYPE_BOOLEAN:
            return nodeToBoolean(prop);
	    default:
            throw new Schema2BeansRuntimeException(Common.getMessage(
                          "TypeNotSupported_msg",
		                  prop.getPropClass(), Integer.valueOf(prop.getType())));
	}
    }

    protected Boolean nodeToBoolean(BeanProp prop) {
        // If there is a node, that means the empty node exists (true)
        // If there is no node set, we might just not be attached to
        // a parent node right now, check our prop
        //System.out.println("node="+node+" getBean(prop)="+this.getBean(prop));
        if (node == null) {
            Object result = getBean(prop);
            if (result == null)
                return Boolean.FALSE;
            //System.out.println("!!! node was null, but prop has something.  prop="+prop);
            return (Boolean) result;
        } else {
            String ret = getDomValue(node);
            //System.out.println("ret="+ret);
            if (ret == null)
                return Boolean.TRUE;
            ret = ret.toLowerCase().intern();
            if (ret == "false" || ret == "0")
                return Boolean.FALSE;
            // Just the node being there and not saying false is enough to
            // be true.
            return Boolean.TRUE;
        }
    }

    protected String findNamespace(String prefix) {
        String targetName = "xmlns:"+prefix;
        for (Node n = node; n != null; n = n.getParentNode()) {
            NamedNodeMap nodeMap = n.getAttributes();
            if (nodeMap == null)
                continue;
            Attr a = (Attr) nodeMap.getNamedItem(targetName);
            if (a != null) {
                return a.getValue();
            }
        }
        return "";
    }

    /**
     *	Return the value as a String of the object
     */
    private String getWrapperValue(Object value) {
        if (value.getClass().isInstance(Wrapper.class))
            return ((Wrapper)value).getWrapperValue();
        else if (value.getClass().isAssignableFrom(charArrayClass)) {
            //System.out.println("It's a char[]");
            return new String((char[])value);
        } else if (value instanceof java.util.Calendar) {
            return calendarToString((java.util.Calendar) value);
        } else if (value instanceof org.netbeans.modules.schema2beans.QName) {
            org.netbeans.modules.schema2beans.QName q =
                (org.netbeans.modules.schema2beans.QName) value;
            if ("".equals(q.getPrefix()))
                return q.getLocalPart();
            else
                return q.getPrefix() + ":" + q.getLocalPart();
        } else {
            Class cls = value.getClass();
            String clsName = cls.getName();
            if (clsName.equals("javax.xml.namespace.QName")) {
                try {
                    Method prefixMethod = cls.getDeclaredMethod("getPrefix",
                                                                new Class[0]);
                    String prefix = (String) prefixMethod.invoke(value,
                                                                 new Object[0]);
                    Method localPartMethod = cls.getDeclaredMethod("getLocalPart",
                                                                   new Class[0]);
                    String localPart = (String) localPartMethod.invoke(value,
                                                                       new Object[0]);
                    if ("".equals(prefix))
                        return localPart;
                    else
                        return prefix + ":" + localPart;
                } catch (java.lang.NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (java.lang.IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            return value.toString();
        }
    }

	public static String calendarToString(java.util.Calendar cal) {
		java.util.Date date = cal.getTime();
		java.text.SimpleDateFormat formatter;
		if (cal.get(java.util.Calendar.HOUR) == 0 && cal.get(java.util.Calendar.MINUTE) == 0 && cal.get(java.util.Calendar.SECOND) == 0) {
			formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");	// NOI18N
		} else if (cal.get(java.util.Calendar.MILLISECOND) == 0) {
			formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");	// NOI18N
		} else {
			formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");	// NOI18N
		}
		String result = formatter.format(date);
		if (java.util.TimeZone.getDefault().hasSameRules(cal.getTimeZone())) {
			return result;
		}
                java.util.TimeZone tz = cal.getTimeZone();
		int offset = timeZoneOffset(tz, 0);
		if (offset == 0)
            return result+"Z";
		int seconds = offset / 1000;
		if (seconds > 0) {
			result += "+";
		} else {
			seconds = -1 * seconds;
			result += "-";
		}
		int hours = seconds / 3600;
		if (hours < 10)
            result += "0";
		result += hours + ":";
		int minutes = (seconds / 60) % 60;
		if (minutes < 10)
            result += "0";
		result += minutes;
		return result;
	}

    /**
     *	The value is only stored locally. A later call to syncNodes()
     *	updates the DOM nodes with this local value.
     */
    Object setValue(BeanProp prop, Object value) {
	Object		oldValue = null;
	BeanProperty 	bp = this.getBeanProperty(prop);

	if (bp != null) {
	    oldValue = bp.value;

	    //	Use the value cache, otherwise get the Node tree value
	    if ((oldValue == null) && (this.node != null))
            oldValue = this.getValue(prop);

	    if (Common.isBean(prop.type))
            bp.value = value;
	    else
            if (Common.isString(prop.type) && (value != null)) {
                if (value instanceof org.netbeans.modules.schema2beans.QName) {
                    org.netbeans.modules.schema2beans.QName q =
                        (org.netbeans.modules.schema2beans.QName) value;
                    String prefix = q.getPrefix();
                    String declaredNS = "";
                    if ("".equals(prefix)) {
                        prefix = prop.getDtdName()+"_ns__";
                        q = new org.netbeans.modules.schema2beans.QName(q.getNamespaceURI(),
                                                    q.getLocalPart(),
                                                    prefix);
                    } else {
                        declaredNS = findNamespace(prefix);
                    }
                    if ("".equals(declaredNS)) {
                        // It's undeclared, so declare it.
                        ((Element)node).setAttribute("xmlns:"+prefix,
                                                     q.getNamespaceURI());
                        prop.createTransientAttribute("xmlns:"+prefix);
                    }
                } else {
                    Class cls = value.getClass();
                    String clsName = cls.getName();
                    if (clsName.equals("javax.xml.namespace.QName")) {
                        try {
                        Method prefixMethod = cls.getDeclaredMethod("getPrefix",
                                                                new Class[0]);
                        String prefix = (String) prefixMethod.invoke(value,
                                                                 new Object[0]);
                        Method nsMethod = cls.getDeclaredMethod("getNamespaceURI",
                                                                new Class[0]);
                        String ns = (String) nsMethod.invoke(value,
                                                             new Object[0]);
                        String declaredNS = "";
                        if ("".equals(prefix)) {
                            Method localPartMethod = cls.getDeclaredMethod("getLocalPart",
                                                                   new Class[0]);
                            String localPart = (String) localPartMethod.invoke(value,
                                                                       new Object[0]);
                            Constructor c = cls.getDeclaredConstructor(new Class[] {String.class, String.class, String.class});
                            prefix = prop.getDtdName()+"_ns__";
                            value = c.newInstance(new Object[] {ns, localPart,
                                                                prefix});
                        } else {
                            declaredNS = findNamespace(prefix);
                        }
                        if ("".equals(declaredNS)) {
                            // It's undeclared, so declare it.
                            ((Element)node).setAttribute("xmlns:"+prefix,
                                                         ns);
                            prop.createTransientAttribute("xmlns:"+prefix);
                        }
                        } catch (java.lang.NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        } catch (java.lang.IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (java.lang.InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                bp.value = this.getWrapperValue(value);
            } else
                bp.value = value;
	}

	return oldValue;
    }

    /** Workaround for TimeZone.getOffset which is not in JDK1.3 */
    private static int timeZoneOffset (java.util.TimeZone tz, long date) {
	if (tz.inDaylightTime(new Date(date))) {
	    return tz.getRawOffset() + (tz.useDaylightTime()? 3600000: 0);
	}
	return tz.getRawOffset();
    }


    /**
     *	Removes the reference to the property prop and delete the DOM Nodes.
     *
     */
    void remove(BeanProp prop) {
    }


    void removeProp(BeanProp prop) {
	if (this.prop != null && this.prop.beanProp == prop)
	    this.prop = null;
    }

    //	Remove a DOM node
    void removeNode(BeanProp prop) {
	if (this.node != null) {
	    Node parent = prop.getParentNode();

	    if (DDLogFlags.debug) {
		TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
				DDLogFlags.DBG_BLD, 1,
				DDLogFlags.DELETENODE,
				this.node.getNodeName() + " from " +
				parent.getNodeName());
	    }
        removeSurroundingSpace(parent, node);
	    parent.removeChild(this.node);
	    this.node = null;
	}
    }

    /**
     * Removes space surrounding node appointed to removal to keep proper document layout
     * @param parent
     * @param node
     */
    private static void removeSurroundingSpace(Node parent, Node node) {
        Node nextNode = node.getNextSibling();
        if (nextNode != null && nextNode.getNodeType() == Node.TEXT_NODE) {
            String s = nextNode.getNodeValue();
            if (s.trim().length() == 0) {
                int i = s.indexOf('\n');
                if (i == -1) {
                    parent.removeChild(nextNode);
                } else {
                    s = s.substring(i);
                    Node previousNode = node.getPreviousSibling();
                    if (previousNode != null && previousNode.getNodeType() == Node.TEXT_NODE) {
                        String s1 = previousNode.getNodeValue();
                        if (previousNode.getPreviousSibling() != null) {
                            if (s1.trim().length() == 0) {
                                i = s1.lastIndexOf('\n');
                                if (i > 0) {
                                    s = s1.substring(0, i) + s;
                                } else {
                                    parent.removeChild(previousNode);
                                }
                            }
                        } else {
                            parent.removeChild((previousNode));
                        }
                    }
                    nextNode.setNodeValue(s);
                }
            }
        }
    }

    /**
     *	The BeanProp that changed the value calls the DOMBinding after
     *	the setValue in order to propagate the Changed event to all the
     *	BeanProp that share this same Node.
     */
    void notifyBeansForChange(Object oldValue, Object newValue,
			      String attrName) {

	if (this.prop != null) {
	    PropertyChangeEvent e = this.prop.beanProp.
		prepareForChangeEvent(this, oldValue, newValue, attrName);

	    this.prop.beanProp.notifyInternal(e, true);
	}
    }

    /**
     *  This method is called when time has come to update the DOM nodes
     *  with the local value of the DOMBinding.
     *  Typically, this happens when the user updates a final value of the
     *  bean tree.
     *
     *  This method is called when a property has been set and the bean
     *  holding this property is already attached to a DOM Node. Only
     *  this condition allows the value to be flushed into the DOM tree.
     *
     */
    void syncNodes(BeanProp prop, BeanProp.Action a) {
        BeanProperty 	bp = this.getBeanProperty(prop);

        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_BLD, 1,
                            DDLogFlags.SYNCNODES,
                            a.toString() + " " + prop.getDtdName() +
                            (bp==null?" - unknown prop!":""));
        }

        if (bp == null)
            return;

        if(a.action == a.REMOVE) {
            int i = prop.idToIndex(this.id);
            if (i != -1)
                bp.lastIndex = i;

            PropertyChangeEvent e =
                prop.prepareForChangeEvent(this, bp.value, null, null);

            if (Common.isBean(prop.type)) {
                //  Recurse on all the properties of the bean
                BaseBean bean = ((BaseBean)bp.value);
                bean.syncNodes(a);
            }
            //System.out.println("this.prop="+this.prop+" this.prop.beanProp="+this.prop.beanProp+" this.prop.beanProp==prop "+(this.prop.beanProp==prop)+" this.prop.value="+this.prop.value+" bp.value="+bp.value);
            if (this.prop != null && this.prop.beanProp==prop) {
                // See IZ#19802
                if (node != null &&
                    (prop.getType() & Common.MASK_TYPE) == Common.TYPE_STRING) {
                    // Since it's a String, the value is stored in the DOM
                    // graph, and not in our BeanProperty.  Stash the contents
                    // of the DOM node into our BeanProperty,
                    // before we remove the node and lose the value.
                    bp.value = getDomValue(node);
                }
                //this.removeProp(prop);
            }
            this.removeNode(prop);
            prop.notifyInternal(e, false);
        }
        else
            if(a.action == a.ADD) {
                if (Common.isBean(prop.type)) {
                    NodeFactory f = prop.getNodeFactory();

                    if (this.node != null) {
                        System.out.println("Removing from old graph.");
                        BeanProp.Action a2;
                        a2 = new BeanProp.Action(a.REMOVE);
                        syncNodes(this.prop.beanProp, a2);
                        /*
                        throw new IllegalStateException(Common.getMessage(
                                     "DOMBindingAlreadyHasNode_msg",
                                     node.toString()));
                        */
                    }

                    Node parent = prop.getParentNode();
                    this.node = f.createElement(prop);

                    if (DDLogFlags.debug) {
                        TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_BLD, 1,
                                        DDLogFlags.SYNCING,
                                        "adding new child " +
                                        this.node.getNodeName() +
                                        " to node " + parent.getNodeName());
                    }

                    Node sibling = prop.getFollowingSibling(this);
                    parent.insertBefore(this.node, sibling);

                    //  Recurse the syncNodes on all the properties of the bean
                    BaseBean bean = ((BaseBean)bp.value);
                    bean.setGraphManager(prop.bean.graphManager());
                    bean.syncNodes(a);
                } else if (Common.isBoolean(prop.type)) {
                    boolean v = false;

                    if (bp.value != null)
                        v = ((Boolean)bp.value).booleanValue();

                    if (Common.shouldNotBeEmpty(prop.type) || node == null ||
                        (nodeToBoolean(prop)).booleanValue() != v) {
                        //  Current node and expected value are not the same
                        if (DDLogFlags.debug) {
                            TraceLogger.put(TraceLogger.DEBUG,
                                            TraceLogger.SVC_DD,
                                            DDLogFlags.DBG_BLD, 1,
                                            DDLogFlags.SYNCING,
                                            (v?"adding new":"removing") +
                                            " tag " +
                                            prop.getDtdName());
                        }

                        Node parent = prop.getParentNode();
                        if (v || Common.shouldNotBeEmpty(prop.type)) {
                            NodeFactory f = prop.getNodeFactory();
                            if (node == null) {
                                node = f.createElement(prop);
                                Node sibling = prop.getFollowingSibling(this);
                                parent.insertBefore(this.node, sibling);
                            }
                            if (Common.shouldNotBeEmpty(prop.type)) {
                                CharacterData text =
                                    (CharacterData) node.getFirstChild();
                                if (text == null) {
                                    text = (CharacterData)f.createText();
                                    node.appendChild(text);
                                    if (DDLogFlags.debug) {
                                        TraceLogger.put(TraceLogger.DEBUG,
                                                        TraceLogger.SVC_DD,
                                                        DDLogFlags.DBG_BLD, 1,
                                                        DDLogFlags.SYNCING,
                                                        "adding new text node " +
                                                        text.getNodeName() +
                                                        " to node " +
                                                        this.node.getNodeName());
                                    }
                                }
                                text.setData(""+v);
                            }
                        } else if (node != null) {
                            parent.removeChild(this.node);
                            this.node = null;
                        }
                    }
                    else {
                        if (DDLogFlags.debug) {
                            TraceLogger.put(TraceLogger.DEBUG,
                                            TraceLogger.SVC_DD,
                                            DDLogFlags.DBG_BLD, 1,
                                            DDLogFlags.SYNCING,
                                            "keeping same boolean value");
                        }
                    }
                } else {
                    NodeFactory f = prop.getNodeFactory();
                    if (this.node == null) {
                        Node parent = prop.getParentNode();
                        this.node = f.createElement(prop);

                        if (DDLogFlags.debug) {
                            TraceLogger.put(TraceLogger.DEBUG,
                                            TraceLogger.SVC_DD,
                                            DDLogFlags.DBG_BLD, 1,
                                            DDLogFlags.SYNCING,
                                            "adding new child " +
                                            this.node.getNodeName() +
                                            " to node " +
                                            parent.getNodeName());
                        }

                        Node sibling = prop.getFollowingSibling(this);
                        parent.insertBefore(this.node, sibling);
                    }

                    CharacterData text =
                        (CharacterData)this.node.getFirstChild();

                    if (text == null) {
                        text = (CharacterData)f.createText();
                        this.node.appendChild(text);
                        if (DDLogFlags.debug) {
                            TraceLogger.put(TraceLogger.DEBUG,
                                            TraceLogger.SVC_DD,
                                            DDLogFlags.DBG_BLD, 1,
                                            DDLogFlags.SYNCING,
                                            "adding new text node " +
                                            text.getNodeName() +
                                            " to node " +
                                            this.node.getNodeName());
                        }
                    }

                    text.setData(bp.value.toString());
                }

                //  Add any attribute cached for this new node
                if (this.node != null) {
                    if (bp.attributes != null) {
                        for (int i=0; i<bp.attributes.size(); i++) {
                            CacheAttr ca = (CacheAttr)bp.attributes.get(i);
                            // null value means, remove.
                            // Ignore remove for a new node.
                            if (ca.value != null)
                                ((Element)this.node).setAttribute(ca.name, ca.value);
                        }
                        bp.attributes = null;
                    }
                }
            }
            else
                throw new IllegalArgumentException(Common.getMessage(
                                                                     "UnknownAction_msg", Integer.valueOf(a.action)));
    }

    boolean hasDomNode() {
	return (this.node != null);
    }
}


