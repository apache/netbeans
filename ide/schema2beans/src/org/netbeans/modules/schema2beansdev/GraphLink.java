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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.schema2beans.*;

/**
 * @see GraphNode
 *
 * Roughtly, a GraphLink is used to hold attribute definitions or groupings.
 */
public class GraphLink {
    /*
     *	The element that this link is associated to. This element might
     *	be referenced by more than one GraphLink object.
     */
    GraphNode	element;

    /*
     *	Where this object is coming from.
     */
    private GraphLink	parent;
    
    /*
     *	Point to the next link holding information about the element
     *	in the sequence.
     *
     *	For example <!ELEMENT elt (a, b, c)>
     *
     *	If this link element references a, the sibling references a link
     *	object defining the element b.
     *
     */
    private GraphLink	sibling;
    
    /*
     *	Point to a subnode link defining information about an element
     *	defined in a "one-more level of parenthese".
     *
     *	For example <!ELEMENT elt (a, (b | c)+, d>
     *
     *	If this link element references a, the sibling references a link
     *	object defining the element d and the child link element references
     *	a link defining the element b (this link has itself a sibling link
     *	on a link object defining c). Note that the prop of the child link
     *	element will be: SEQUENCE_OR | INSTANCE_1N.
     *
     */
    private GraphLink	child;

    private transient GraphLink lastChild;  // cache
    
    /*
     *	The prop integers define some properties on the link or element.
     *	The link might either start to define a list of nodes as
     *	a sequence of nodes: a and b and c, or it might define
     *	a list of node choices: a or b or c.
     *
     *	For example <!ELEMENT elt (a, b, c)>     -> sequence
     *	            <!ELEMENT elt (a | b | c)>   -> choice
     *
     *
     *	Also, the set of nodes defined by the link might have some
     *	rules as they should be instanciated (as defined by the ?, * and +
     *	DTD characters).
     *
     *	propElement applies to the element of this link (one element of the
     *	DTD represented by a unique GraphNode might have different
     *	property values depending where and how the element is referenced
     *	by other elements).
     *	propChildren applies to the set of children of this GraphLink.
     *
     *	For example, <ELEMENT! elt a?, (b, c, d)+ e>
     *
     *	The link associated to the element a has the propElement set to the
     *	? value and the propChildren set to the + value.
     */
    int			propElement;
    int			propChildren;

    /**
     * Name of the java bean property (java class attribute) that we're
     * defining.  If it's null, then we're used for grouping.
     */
    String name;
    private String schemaName = null;
    private String namespace = null;
    private String defaultValue;
    private boolean nillable;
    
    private Object			object;

    List extraData = new ArrayList();

    GraphLink(String name) {
        //  Those are the default values
        this.propElement = Common.SEQUENCE_AND | Common.TYPE_1;
        this.propChildren = Common.SEQUENCE_AND | Common.TYPE_1;
        this.name = name;
        this.schemaName = name;
    }

    GraphLink(String name, String namespace) {
        //  Those are the default values
        this.propElement = Common.SEQUENCE_AND | Common.TYPE_1;
        this.propChildren = Common.SEQUENCE_AND | Common.TYPE_1;
        this.name = name;
        this.namespace = namespace;
        this.schemaName = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isUnion() {
        return element.isUnion();
    }

    public void setUnion(boolean value) {
        element.setUnion(value);
    }

    public List getChildren() {	// List<GraphLink>
        List result = new ArrayList();
        for (GraphLink l = child; l != null; l = l.sibling)
            result.add(l);
        return result;
    }

    /**
     * Return our parents children minus this.
     */
    public List getSiblings() {	// List<GraphLink>
        List result = new ArrayList();
        if (parent == null) {
            // The graph isn't well connected, assume I'm first child.
            for (GraphLink l = this; l != null; l = l.sibling)
                result.add(l);
            return result;
        }
        for (GraphLink l = parent.child; l != null; l = l.sibling) {
            if (l == this)
                continue;
            result.add(l);
        }
        return result;
    }

    /**
     * Return our parents children.
     */
    public List getSiblingsAndMe() {	// List<GraphLink>
        if (parent == null) {
            // The graph isn't well connected, assume I'm first child.
            List result = new ArrayList();
            for (GraphLink l = this; l != null; l = l.sibling)
                result.add(l);
            return result;
        }
        return parent.getChildren();
    }

    /**
     * Get next sibling.  This might return null.
     */
    public GraphLink getSibling() {
        return sibling;
    }

    public GraphLink getLastSibling() {
        // Check if our parent has kept track of it.
        if (parent != null && parent.lastChild != null)
            return parent.getLastChild();
        if (sibling == null)
            return this;
        else
            return sibling.getLastSibling();
    }

    /**
     * This might return null.
     */
    public GraphLink getFirstChild() {
        return child;
    }

    /**
     * This might return null.
     */
    public GraphLink getLastChild() {
        if (lastChild == null) {
            if (child != null)
                lastChild = child.getLastSibling();
        }
        return lastChild;
    }

    /**
     * Replaces child list.
     */
    public void setChild(GraphLink l) {
        child = l;
        child.parent = this;
        lastChild = null;
    }

    private void addSibling(GraphLink l) {
        if (sibling != null)
            throw new RuntimeException("I am not the last sibling!");
        sibling = l;
        // Siblings share the same parent
        l.parent = parent;
        if (parent != null)
            parent.lastChild = l;
    }

    /**
     * Adds child to end of child list.
     */
    public void addChild(GraphLink l) {
        if (child == null)
            setChild(l);
        else
            child.getLastSibling().addSibling(l);
    }
    
    /**
     * This might return null.
     */
    public GraphLink getParent() {
        return parent;
    }

    private void setParent(GraphLink l) {
        parent = l;
    }

    boolean isSequenceAnd() {
	//  Apply only to the set of children
	return ((this.propChildren & Common.SEQUENCE_AND) ==
	    Common.SEQUENCE_AND);
    }
    
    boolean isSequenceOr() {
	//  Apply only to the set of children
	return ((this.propChildren & Common.SEQUENCE_OR) ==
	    Common.SEQUENCE_OR);
    }
    
    void setSequence(int prop) {
	this.propChildren = (this.propChildren & Common.MASK_TYPE) | prop;
    }
    
    void setElementInstance(int instance) {
	this.propElement =
	    (this.propElement & Common.MASK_SEQUENCE) | instance;
    }
    
    int getElementInstance() {
	return (this.propElement & Common.MASK_INSTANCE);
    }
    
    void setGroupInstance(int instance) {
        this.propChildren =
            (this.propChildren & Common.MASK_SEQUENCE) | instance;
    }
    
    int getGroupInstance() {
        return (this.propChildren & Common.MASK_INSTANCE);
    }

    /**
     * Fill links with GraphLink's that we're mutually exclusive with.
     */
    public void getMutuallyExclusiveLinks(List links) {	// List<GraphLink>
        if (parent == null)
            return;
        //
        // Mutual exclusion is caused by xsd:choice or '|': (a|b|c)
        // From b's perspective, it's mutually exclusive with a and c.
        // From a's perspective, it's mutually exclusive with b and c, etc.
        // However, if the groupInstance can be more than 1, then
        // we're not mutually exclusive: (a|b|c)*
        //
        if (parent.isSequenceOr() &&
            (parent.getGroupInstance() == Common.TYPE_1 ||
             parent.getGroupInstance() == Common.TYPE_0_1)) {
            for (Iterator it = getSiblings().iterator(); it.hasNext(); ) {
                GraphLink l = (GraphLink) it.next();
                l.findAllBelowBranch(links);
            }
        }
        parent.getMutuallyExclusiveLinks(links);
    }

    /**
     * Starting from this GraphLink, fill in all links below it, including
     * this one.
     */
    public void findAllBelowBranch(List links) { // List<GraphLink>
        links.add(this);
        if (child != null)
            child.findAllBelowBranchAndSiblings(links);
    }

    public void findAllBelowBranchAndSiblings(List links) { // List<GraphLink>
        GraphLink l = this;
        for (; l != null; l = l.sibling) {
            links.add(l);
            if (l.child != null)
                l.child.findAllBelowBranchAndSiblings(links);
        }
    }
    
    String getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(String d) {
        defaultValue = d;
    }

    void setObject(Object obj) {
        this.object = obj;
    }
    
    Object getObject() {
        return this.object;
    }

    boolean isNillable() {
        return nillable;
    }

    void setNillable(boolean value) {
        nillable = value;
    }
    
    String getSchemaName() {
        return schemaName;
    }
    
    public XPathIterator xPathIterator(String xpath) {
        return new XPathIterator(this, xpath);
    }

    public static class XPathIterator implements java.util.Iterator {
        private String xpath;
        private String step;
        private GraphLink curLink;
        private int position;
        
        public XPathIterator(GraphLink startingLink, String xpath) {
            this.xpath = xpath;
            curLink = startingLink;
            findNextStep();
        }

        // Figure out the next step, and advance the state.
        private void findNextStep() {
            if (position >= xpath.length()) {
                step = null;
                return;
            }
            int startingPos = position;
            for (; position < xpath.length(); ++position) {
                if (xpath.charAt(position) == '/') {
                    step = xpath.substring(startingPos, position);
                    ++position;
                    if (".".equals(step)) {
                        // skip over intermediate '.'s
                        startingPos = position;
                    } else {
                        return;
                    }
                }
            }
            step = xpath.substring(startingPos, position);
            if (startingPos > 0 && ".".equals(step)) {
                step = null;
            }
        }

        public boolean hasNext() {
            if (step == null)
                return false;
            return true;
        }

        /**
         * Returns GraphLink's
         */
        public Object next() {
            if (step == null)
                throw new java.util.NoSuchElementException();
            GraphLink result = curLink;
            while (".".equals(step)) {
                findNextStep();
                if (step == null)
                    return result;
            }
            // Don't deal with namespaces
            int colonPos = step.indexOf(':');
            if (colonPos >= 0)
                step = step.substring(colonPos+1);
            //System.out.println("step="+step);
            // Now go look for step
            result = result.lookForChild(step);
            if (result == null) {
                // Not found.
                step = null;
                position = xpath.length();
                return null;
            }
            if (result.element == null) {
                curLink = null;
            } else {
                curLink = result.element.getGraphLink();
            }

            findNextStep();
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Look amongst out children to see if we have one where
     * searchName.equals(getSchemaName())
     */
    GraphLink lookForChild(String searchName) {
        /*
        System.out.println("this="+this);
        System.out.println("this.element="+this.element);
        System.out.println("this.schemaName="+this.getSchemaName());
        System.out.println("this.sibling="+this.sibling);
        System.out.println("this.child="+this.child);
        */
        
        if (searchName.equals(getSchemaName()))
            return this;
        if (name == null) {
            //System.out.println("Grouping");
            if (child != null) {
                GraphLink childResult = child.lookForChild(searchName);
                if (childResult != null)
                    return childResult;
            }
        }
        if (sibling != null)
            return sibling.lookForChild(searchName);
        if (child != null)
            return child.lookForChild(searchName);
        return null;
    }
}
