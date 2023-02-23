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

package org.netbeans.modules.schema2beansdev;

import org.netbeans.modules.schema2beans.*;

import java.util.*;

/**
 *  This class is a node of the schema graph that the TreeBuilder is creating
 *  from the parsed schema.  For every type we get 1 GraphNode.  Every java
 *  bean property (or java class attribute), has an associated GraphLink.
 *  Groupings are represented in GraphLink's.
 *
 *  Each element of the graph tree is a node (root, intermediate node and
 *  leaves). Each node references a GraphLink object which takes care
 *  of linking the different nodes of the graph.
 *
 *  The schema instances information (*, + or ?) and the relationship
 *  as a sequence (,) or choice (|) information are held in the GraphLink
 *  object. There is only one GraphNode instance of an element in the graph
 *  event if the element is referenced by several graph links.
 *
 *  A GrapNode is a leaf if its element GraphLink is null.
 *
 *  To see a pretty graph of GraphNode's and GraphLink's, run with these
 *  options: -t dot -genDotGraph out.dot
 *  And run `dotty out.dot`.
 *
 *  <xsd:element name="bill">
 *    <xsd:complexType>
 *       <xsd:sequence maxOccurs="unbounded">
 *          <xsd:element name="shipTo" type="addressType"/>
 *          <xsd:element name="billTo" type="addressType"/>
 *       </xsd:sequence>
 *    </xsd:complexType>
 *  </xsd:element>
 *  ...
 *  1 GraphNode for type "bill"
 *  1 GraphLink for xsd:sequence (storing the grouping info)
 *  1 GraphNode for type "addressType"
 *  1 GraphLink for "shipTo" java bean property
 *  1 GraphLink for "billTo" java bean property
 */
public class GraphNode {
    /**
     *	The name of the element as it appears in the schema.  In a DTD, this
     *	is unique within the tree since each DTD element
     *	can only be defined once.  An XML Schema may reuse an element name.
     */
    private String 			name;
    private String			namespace = null;

    /**
     * The unique name of the element in the schema.
     */
    private String uniqueName;

    /**
     *	The reference to the link structure defining all the siblings and
     *	subnode of this element, as defined in the DTD.
     *
     *	For example, if we have <!ELEMENT a (b?, (c | d)+)>
     *		This GraphNode name is a, and the link points to a GraphLink
     *		object reference the GraphNode b and pointing to another
     *		graphLink object (a subnode link), defining the (c | d)
     *		declaration:
     *
     *		Node[a]
     *		     |------> Link(null, ',')
     *                     |
     *                   /    \
     *                 /         \
     *                V             V
     *      Link(b, '?')->Node(b)   Link(null, '+')
     *                               |
     *                              Link(null, '|')
     *                              /  \
     *                           /        \
     *                        V             V
     *              Link(c)->Node(c)      Link(d)->Node(d)
     *
     *	@see GraphLink
     *
     */
    private GraphLink		link;
    
    /**
     *	Set to true by the TreeBuilder class when the schema parser calls back
     *	with the element name definition. (This is object might be created
     *	when referenced, before the element definition is found in the DTD)
     *  If this stays false, then it's created outside of our code generation
     *  (e.g., java.lang.String).
     */
    private boolean			created;
    
    /**
     *	Number of time this node is referenced in the graph.
     */
    private int				refCount;
    
    /**
     *	Object reference where the bean builder can store whatever it needs
     */
    private Object			object;
    
    /**
     *	List of the attributes associated to this element (might be null)
     */
    private List		attributes;
    
    //  Used to mark an element to avoid infinite loop for recurse parsing
    private boolean			marked;

    // Used to suggest what type the of the property should be, only
    // really useful on leaf nodes.
    private String javaType;

    private Map extendedProperties = new LinkedHashMap();

    private Set extraData = new LinkedHashSet();
    private boolean isUnion = false;
    private boolean isAbstract = false;

    private GraphNode extendsNode;
    private GraphNode alias;
    
    GraphNode(String name, String uniqueName) {
        //System.out.println("Created GraphNode "+uniqueName+" name="+name);
        setName(name);
        this.uniqueName = uniqueName;
        this.created = false;
        this.attributes = null;
        this.marked = false;
    }
    
    void addAttribute(AttrProp attr) {
        if (alias != null) {
            alias.addAttribute(attr);
            return;
        }
        if (this.attributes == null)
            this.attributes = new ArrayList();
        this.attributes.add(attr);
        //attr.setSourceGraphNode(this);
    }
    
    AttrProp[] getAttributes() {
        if (alias != null) {
            return alias.getAttributes();
        }
        int size = 0;
	
        if (this.attributes != null)
            size = this.attributes.size();
	
        AttrProp[] ret = new AttrProp[size];
	
        if (size > 0)
            return (AttrProp[])this.attributes.toArray(ret);
        else
            return ret;
    }
    
    void setObject(Object obj) {
        this.object = obj;
    }
    
    Object getObject() {
        return this.object;
    }
    
    String getNameWithNamespace() {
        if (namespace == null)
            return name;
        return "{"+namespace+"}"+name;
    }

    /**
     * This method will return the name without any namespace info.
     */
    String getName() {
        return name;
    }

    /**
     * May return null.
     */
    String getNamespace() {
        if (alias != null)
            return alias.getNamespace();
        return namespace;
    }
    
    void setName(String name) {
        // separate out the namespace
        if (name.charAt(0) == '{') {
            int closingBracket = name.indexOf('}');
            this.name = name.substring(closingBracket+1);
            this.namespace = name.substring(1, closingBracket);
        } else {
            this.name = name;
            this.namespace = null;
        }
    }
    
    GraphLink getGraphLink() {
        if (alias != null)
            return alias.getGraphLink();
        return this.link;
    }

    void setGraphLink(GraphLink l) {
        if (alias != null) {
            alias.setGraphLink(l);
            return;
        }
        link = l;
    }

    void setMarked(boolean value) {
        if (alias != null) {
            alias.setMarked(value);
            return;
        }
        marked = value;
    }

    boolean getMarked() {
        if (alias != null) {
            return alias.getMarked();
        }
        return marked;
    }
    
    boolean isAbstract() {
        return isAbstract;
    }
    
    void setAbstract(boolean value) {
        isAbstract = value;
    }
    
    void setCreated(boolean value) {
        if (alias != null) {
            alias.setCreated(value);
            return;
        }
        created = value;
    }

    boolean isCreated() {
        if (alias != null) {
            return alias.isCreated();
        }
        return created;
    }

    /**
     * These are generic properties that the schema parser wants to pass
     * to the code generation phase.
     */
    public void setExtendedProperty(String name, Object value) {
        if (alias != null) {
            alias.setExtendedProperty(name, value);
            return;
        }
        extendedProperties.put(name, value);
    }

    /**
     * These are generic properties that the schema parser wants to pass
     * to the code generation phase.
     */
    public Object getExtendedProperty(String name) {
        if (alias != null) {
            return alias.getExtendedProperty(name);
        }
        return extendedProperties.get(name);
    }

    void incrRefCount() {
        /*
        if (alias != null) {
            alias.incrRefCount();
            return;
        }
        */
        ++refCount;
    }

    int getRefCount() {
        /*
        if (alias != null) {
            return alias.getRefCount();
        }
        */
        return refCount;
    }

    void setAlias(GraphNode n) {
        alias = n;
    }

    GraphNode getAlias() {
        return alias;
    }

    /**
     *	Returns the list of all the GraphNodes used directly by this
     *	element (this represent all the elements used to declare the
     *	current GraphNode definition in the DTD (only one level
     *	of the subtree of this element).
     */
    GraphNode[] getNodes() {
        if (alias != null)
            return alias.getNodes();
        Map list = new HashMap();
        gatherElements(this.link, list);
        GraphNode[] ret = new GraphNode[list.size()];
        return (GraphNode[])list.values().toArray(ret);
    }
    
    //
    //	Fill the hash table with the elements referenced by this element
    //	(All the elements in the right part of the DTD declaration)
    //
    private void gatherElements(GraphLink l, Map list) {
        while (l != null) {
            if (l.element != null)
                list.put(l.element.getName(), l.element);
	    
            gatherElements(l.getFirstChild(), list);
	    
            l = l.getSibling();
        }
    }

    public void setJavaType(String jt) {
        if (alias != null) {
            alias.setJavaType(jt);
            return;
        }
        javaType = jt;
    }

    public String getJavaType() {
        if (alias != null) {
            return alias.getJavaType();
        }
        return javaType;
    }
    
    public void setExtension(GraphNode extendsNode) {
        if (alias != null) {
            alias.setExtension(extendsNode);
            return;
        }
        this.extendsNode = extendsNode;
    }
    
    public GraphNode getExtension() {
        if (alias != null)
            return alias.extendsNode;
        return extendsNode;
    }

    public boolean isUnion() {
        return isUnion;
    }

    public void setUnion(boolean value) {
        isUnion = value;
    }

    public void addExtraData(Object data) {
        extraData.add(data);
    }

    public void addExtraDataIncludeAlias(Object data) {
        extraData.add(data);
        if (alias != null)
            alias.addExtraData(data);
    }

    public Object searchExtraData(Class type) {
        for (Iterator it = extraData.iterator(); it.hasNext(); ) {
            Object o = it.next();
            //System.out.println("searchExtraData: o="+o);
            if (type.isAssignableFrom(o.getClass()))
                return o;
        }
        return null;
    }

    public Iterator extraDataIterator() {
        return extraData.iterator();
    }

    public Set getExtraData() {
        return extraData;
    }
    
    public String toString() {
        if (alias != null) {
            return name+" (is a "+alias.toString()+")";
        }
        return getNameWithNamespace();
    }
}
