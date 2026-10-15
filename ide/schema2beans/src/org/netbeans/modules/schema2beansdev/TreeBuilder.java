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

import java.util.*;
import java.io.*;

import org.netbeans.modules.schema2beans.*;

//******************************************************************************
// BEGIN_NOI18N
// This class does not (and will not) cantain strings that need to be localized.
//******************************************************************************

/**
 *
 *  This class implements the Document Definition handler in order to build
 *  the internal tree representation of the DD DTD.
 *
 */
public class TreeBuilder implements DocDefHandler, TreeParser, HasPrefixGuesser {
    //	root element of the DTD graph
    GraphNode		rootNode;
    String 		docRoot;
    GenBeans.Config 	config;
    
    //	Current parsing pointers
    private Stack curParentGroupStack = new Stack();
    private GraphLink		curParentGroup;
    
    //	Global value of the type currently parsed (ELEMENT, ATTLIST, COMMENT)
    private Stack curElementTypeStack = new Stack();
    private int			curElementType;
    
    //	Current parsed attribute (<!ATTLIST element)
    private Stack curAttrStack = new Stack();
    private AttrProp		curAttr;
    
    private PrefixGuesser prefixGuesser;

    void pushLevel() {
        curParentGroupStack.push(curParentGroup);
        curElementTypeStack.push(Integer.valueOf(curElementType));
        curAttrStack.push(curAttr);
    }

    void popLevel() {
        curParentGroup = (GraphLink) curParentGroupStack.pop();
        curElementType = ((Integer) curElementTypeStack.pop()).intValue();
        curAttr = (AttrProp) curAttrStack.pop();
    }
    
    //
    //	Where the element nodes (GraphNode objects) are stored during
    //	the graph construction. We use this hash table to make sure that
    //	an element GraphNode is created only once (unicity on the name).
    //	This table is also useful to get the list of all the nodes.
    //
    Map			nameHash;	// Map<String, GraphNode>

    private String defaultNamespace = null;
    
    
    TreeBuilder(GenBeans.Config config) {
        this.nameHash = new HashMap();
        this.curAttr = null;
        this.config = config;
    }
    
    /**
     *	Called once, when the DTD is started to be parsed.
     *	Create the GraphNode root element.
     *
     *	@param root root elemement name of the document (as the DOCTYPE
     *	specifies in the XML document)
     */
    public void startDocument(String root) {
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_DTD, 1,
                            DDLogFlags.STARTDOC, root);

            config.messageOut.println("Building the schema object graph.");
        }
        this.docRoot = root;
    }
    
    /**
     *	Called when the DTD parsing is over.
     *
     *	At this time, the DTD object graph is entirely built. The method
     *	checks the consitency of the built graph, and cleans things up a bit.
     *
     */
    public void endDocument() {
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_DTD, 1,
                            DDLogFlags.ENDDOC);
	
            config.messageOut.println("schema Object graph built.");
        }
	
        // Remove the starter groupings
        for (Iterator it = nameHash.values().iterator(); it.hasNext(); ) {
            GraphNode node = (GraphNode)it.next();
            GraphLink l = node.getGraphLink();
            if (l == null || l.name != null || l.getSibling() != null
                || l.isSequenceOr() || l.getGroupInstance() != Common.TYPE_1)
                continue;
            GraphLink firstChild = l.getFirstChild();
            if (firstChild != null && firstChild.getSibling() != null)
                continue;
            if (DDLogFlags.debug)
                config.messageOut.println("Removing starter group: "+l);
            node.setGraphLink(firstChild);
        }

        //
        //	We're done building the tree graph
        //	It's time now to generate the beans
        //
        try {
            findRootNode();
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        }
	
        if (DDLogFlags.debug) {
            config.messageOut.println(this.dump());
        }
    }
    
    /**
     *	Either create the GraphNode for the element named name, or
     *	get it from the hash table. This method can be called either
     *	by the startElement() method (an element definition has been
     *	found in the DTD) or by the element() method (an element is
     *	referenced by another one).
     *
     *	@param name name of the element as the DTD parser reads it
     *	@param original true if the element definition has been
     *	read, false if we are just asking to reference the element.
     *	This parameter allows to check than an element is not
     *	defined twice and is at least defined once.
     *	@return the unique GraphNode object of the named element
     */
    private static final int CREATE 	= 1;
    private static final int GET 		= 2;
    private static final int REFERENCE 	= 3;
    private GraphNode getGraphNode(String uniqueName, String name, int mode) throws Schema2BeansException {
        //String uniqueName = name;
        uniqueName = name;
        
        //  Find out if we already know about it
        GraphNode node = (GraphNode)this.nameHash.get(uniqueName);
	
        if (node != null) {
            //	We know about it
            if (false && node.isCreated() && (mode == CREATE)) {
                throw new Schema2BeansException(Common.getMessage("DuplicateElement_msg", uniqueName));
            }
        }
        else {
            //
            //  First time we hear about this element. Create the GraphNode
            //  Object and its GraphLink link. The purpose of this GraphLink
            //  object is to hold the siblings/children links for this node.
            //  This GraphLink does _not_ reference the element.
            //  (graphLink.element = null).
            //
            node = new GraphNode(name, uniqueName);
            node.setGraphLink(new GraphLink(null));
            this.nameHash.put(uniqueName, node);
            //System.out.println("Created new GraphNode: "+node);
        }
	
        //
        //  Called to get the original: mark it. If we are later called again
        //  to get the original that means we have two element definition in
        //  the DTD and we can throw an exception (see above).
        //  Called to get a reference: increment the node usage. The root
        //  of the node is never referenced and will keep a refCount=0.
        //
        if (mode == CREATE)
            node.setCreated(true);
        else
            if (mode == REFERENCE)
                node.incrRefCount();
	
        return node;
    }
    
    
    /**
     *	Called each time a DTD <!element definition is read.
     *
     *	@param name the name of the element
     *  @param typeName is the name to use for the attribute
     *	@param type the type (as a constant) of the element (for example
     *	ELEMENT or ATTLIST)
     */
    public void startElement(String uniqueName,
                             String typeName, int type) {
	
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_DTD, 1,
                            DDLogFlags.STARTELT,
                            uniqueName + " - " + typeName + " - " + typeToString(type));
        }
        pushLevel();

        //  Keep track of what we are building over all the method calls
        this.curElementType = type;
	
        //
        //  For now, ignore comments
        //
        try {
            if (type == Common.ELEMENT) {
                //  Get the node and start building beneath it
                GraphNode node = getGraphNode(uniqueName, typeName,
                                              CREATE);
                curParentGroup = node.getGraphLink();
            } else if (type == Common.ATTLIST) {
                //  Get the node and add this attribute
                GraphNode node = getGraphNode(uniqueName, typeName,
                                              GET);
                curAttr = new AttrProp(typeName);
                node.addAttribute(curAttr);
            }
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        }
    }
    
    public boolean doesElementExist(String typeName) {
        return nameHash.containsKey(typeName);
    }
    
    /**
     *	Done with an element
     */
    public void endElement() {
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_DTD, 1,
                            DDLogFlags.ENDELT);
        }
	
        if (curElementType == Common.ATTLIST) {
            curAttr.validate();
        } else if (curElementType == Common.ELEMENT) {
            //System.out.println("endElement: curParentGroup="+curParentGroup);
        }
	
        curElementType = Common.NONE;
        popLevel();
    }
    
    /**
     *  Called each time a character | is found.
     */
    public void character(char c) {
        //System.out.println("character: c="+c+" curParentGroup="+curParentGroup);
        if (this.curElementType == Common.ELEMENT) {
            if (c == '|')
                curParentGroup.setSequence(Common.SEQUENCE_OR);
        }
        else
            if (this.curElementType == Common.ATTLIST) {
                if (c == '|') {
                    this.curAttr.checkEnum();
                }
            }
    }
    
    /**
     *	Called for each word found in a DTD definition. This can be a
     *	comment, element or attlist definition. For example, this method is
     *	called for each name element found within the scope of an element
     *	(<!element (element1, element2, ...)>. The first element name doesn't
     *	generate a call to this method (@see startElement).
     *
     *	This is where the subtree of the element definition is built.
     *	The element to add might be a child or sibling to the previous
     *	element. If the element is preceded by a '(', this is child
     *	(@see startGroupElements), otherwise the element is a sibling.
     *
     *	@param name the name of the element defined within the <!element ...>
     *	declaration.
     *	@param instance has one of the three values: TYPE_0_1,
     *	TYPE_1, TYPE_0_N, TYPE_1_N
     *
     */
    public void element(String uniqueName, String typeName,
                        String attrName, String attrNamespace,
                        int instance, boolean externalType,
                        String defaultValue) {
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_DTD, 1,
                            DDLogFlags.ELEMENT,
                            attrName + " : " + typeName + instanceToString(instance, false));
        }
        try {
            if (curElementType == Common.NONE && !externalType) {
                if (DDLogFlags.debug)
                    System.out.println("Top element def for "+attrName);
                GraphNode attrNode = getGraphNode(uniqueName, attrName, CREATE);
                GraphNode node = getGraphNode(uniqueName, typeName, REFERENCE);
                attrNode.setAlias(node);
            } else if (curElementType == Common.ELEMENT) {
                //	Get the element reference
                GraphNode node = getGraphNode(uniqueName, typeName, REFERENCE);
	    
                GraphLink link = new GraphLink(attrName, attrNamespace);
                link.setDefaultValue(defaultValue);
                //System.out.println("curParentGroup="+curParentGroup+" curParentGroup.sibling="+curParentGroup.getSibling());
                curParentGroup.addChild(link);
                link.element = node;
                link.setElementInstance(instance);

                if (externalType)
                    node.setJavaType(typeName);
                //System.out.println("Created new GraphLink: "+link+" link.parent="+link.parent+" link.sibling="+link.sibling);
            } else if (curElementType == Common.ATTLIST) {
                //	If the current attribute is completly built, signal the
                //	Parser by throwing the exception.
                if (this.curAttr.isComplete())
                    throw new DocDefParser.MissingEndOfEltException(curAttr.getPropertyName());
		
                if (defaultValue != null)
                    this.curAttr.setDefaultValue(defaultValue);
                this.curAttr.addValue(attrName, attrNamespace);
                if (externalType)
                    curAttr.setJavaType(typeName);
            }
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        }
    }
    public void element(String uniqueName, String typeName, int instance) {
        element(uniqueName, typeName, typeName, null, instance, false, null);
    }

    public void addExtraDataNode(String uniqueName, String typeName, Object data) throws Schema2BeansException {
        //System.out.println("** addExtraDataNode: typeName="+typeName+" data="+data);
        GraphNode node = getGraphNode(uniqueName, typeName, GET);
        node.addExtraData(data);
    }

    public void addExtraDataCurLink(Object data) {
        //System.out.println("** addExtraDataCurLink: data="+data+" curParentGroup.name="+((curParentGroup == null) ? "curParentGroup null" : curParentGroup.name));
        if (curElementType == Common.ATTLIST) {
            //System.out.println("curElementType == Common.ATTLIST, curAttr="+curAttr);
            if (curAttr != null)
                curAttr.addExtraData(data);
        } else {
            //System.out.println("lastChild="+curParentGroup.getLastChild());
            if (curParentGroup != null && curParentGroup.getLastChild() != null) {
                curParentGroup.getLastChild().extraData.add(data);
            }
        }
    }

    public void addExtraDataNode(String uniqueName, String typeName, Object[] data) throws Schema2BeansException {
        //System.out.println("** addExtraDataNode: typeName="+typeName+" data="+data);
        GraphNode node = getGraphNode(uniqueName, typeName, GET);
        if (data != null)
            for (int i=0; i < data.length; i++)
                node.addExtraData(data[i]);
    }

    public void setUnion(String uniqueName, String typeName, boolean value) throws Schema2BeansException {
        GraphNode node = getGraphNode(uniqueName, typeName, GET);
        node.setUnion(value);
    }

    public void addExtraDataCurLink(Object[] data) {
        //System.out.println("** addExtraDataCurLink: data="+data+" curParentGroup.name="+((curParentGroup == null) ? "curParentGroup null" : curParentGroup.name));
        if (curElementType == Common.ATTLIST) {
            //System.out.println("curElementType == Common.ATTLIST, curAttr="+curAttr);
            if (curAttr != null && data != null)
                for (int i=0; i < data.length; i++)
                    curAttr.addExtraData(data[i]);
        } else {
            //System.out.println("lastChild="+curParentGroup.getLastChild());
            if (curParentGroup != null && curParentGroup.getLastChild() != null) {
                if (data != null)
                    for (int i=0; i < data.length; i++)
                        curParentGroup.getLastChild().extraData.add(data[i]);
            }
        }
    }

    public void nillable(boolean value) {
        //System.out.println("nillable="+value);
        if (curParentGroup != null && curParentGroup.getLastChild() != null)
            curParentGroup.getLastChild().setNillable(value);
        /*
        else
            System.err.println("no parent group for nillable");
        */
    }
    
    public void setAbstract(String uniqueName, String name, boolean value) {
        //System.out.println("Setting javaType of "+name+" to "+javaType);
        if (this.curElementType == Common.ATTLIST) {
        } else {
            //	Get the element reference
            GraphNode node;
            try {
                node = getGraphNode(uniqueName, name, GET);
            } catch (Schema2BeansException e) {
                throw new Schema2BeansRuntimeException(e);
            }
            node.setAbstract(value);
        }
    }
    
    /**
     * set an extended property on a GraphNode
     */
    public void setExtendedProperty(String uniqueName, String typeName, String propertyName,
                                    Object value) throws Schema2BeansException {
        GraphNode node = getGraphNode(uniqueName, typeName, GET);
        node.setExtendedProperty(propertyName, value);
    }

    /**
     * Called to request that the graph node named name be of a certain
     * Java class.  If the current element type is an attribute, then
     * we set the javaType of that attribute instead.
     * @param javaType is the name of a Java class (eg, "java.lang.Integer", or "int").
     */
    public void javaType(String uniqueName, String name, String javaType) {
        //System.out.println("Setting javaType of "+name+" to "+javaType);
        if (this.curElementType == Common.ATTLIST) {
            curAttr.setJavaType(javaType);
        } else {
            //	Get the element reference
            GraphNode node;
            try {
                node = getGraphNode(uniqueName, name, GET);
            } catch (Schema2BeansException e) {
                throw new Schema2BeansRuntimeException(e);
            }
            node.setJavaType(javaType);
            node.setCreated(false);
        }
    }
    
    public void setExtension(String uniqueName, String typeName, String extendsName) throws Schema2BeansException {
        if (curElementType == Common.ATTLIST) {
        } else {
            GraphNode node;
            GraphNode extendsNode;
            try {
                node = getGraphNode(uniqueName, typeName, GET);
                extendsNode = getGraphNode(null, extendsName, REFERENCE);
            } catch (Schema2BeansException e) {
                throw new Schema2BeansRuntimeException(e);
            }
            node.setExtension(extendsNode);
        }
    }

    /**
     *	Called when a parenthese is found, meaning that the following
     *	elements (element() calls) should be considered as semantically
     *	grouped.
     *
     *	Creates a child GraphLink from the current link to group
     *	all the further elements of this group. If any propriety
     *	is defined for this group (as *, ? or + or |) this will be set later
     *	on the current link (as the parent of any of the elements graph link
     *	objects).
     */
    public void startGroupElements() {
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_DTD, 5,
                            DDLogFlags.STARTGRP);
        }
	
        //	A new parenthese in the parsing makes a new GraphLink.
        if (this.curElementType == Common.ELEMENT) {
            GraphLink link = new GraphLink(null);
            curParentGroup.addChild(link);
            curParentGroup = link;
            //System.out.println("curParentGroup="+curParentGroup);
        }
        else {
            if (this.curElementType == Common.ATTLIST)
                this.curAttr.setEnum(true);
        }
    }
    
    /**
     *	We are done creating the elements of a same group,
     *	set the current link to the parent of the group.
     *	This will allow either to start creating siblings (if element()
     *	is called) or go the next parent level (if this same method
     *	is called again).
     */
    public void endGroupElements(int instance) {
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG, TraceLogger.SVC_DD,
                            DDLogFlags.DBG_DTD, 5,
                            DDLogFlags.ENDGRP,
                            instanceToString(instance, false));
        }
	
        if (curElementType == Common.ELEMENT) {
            curParentGroup.setGroupInstance(instance);
            //System.out.println("curParentGroup="+curParentGroup+" instance="+instance);
            curParentGroup = curParentGroup.getParent();
        }
        else
            if (this.curElementType == Common.ATTLIST)
                this.curAttr.setEnum(false);
    }

    public void setDefaultNamespace(String ns) {
        defaultNamespace = ns;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    private void findRootNode() throws Schema2BeansException {
        //
        //  Find out who's the root of the graph
        //  The root of the graph is the graph node that has not been
        //  referenced. We might find zero, one or several nodes that
        //  could be the root:
        //	0: throw an exception
        //	1: use the node as the root (check with doc root if specified)
        //	1-n: use the doc root value if specified or ask
        //	for the node to use.
        //
        Iterator	 	it = this.nameHash.keySet().iterator();
        GraphNode		node;
        int			count = 0;
        List		list = new ArrayList();
	
        while (it.hasNext()) {
            String uniqueName = (String) it.next();
            node = (GraphNode) nameHash.get(uniqueName);
            if (DDLogFlags.debug) {
                System.out.println("refCount="+node.getRefCount()+" created="+node.isCreated()+" javaType="+node.getJavaType()+" uniqueName="+uniqueName+" node="+node);
            }
            if (node.isCreated() && node.getRefCount() == 0) {
                count++;
                list.add(node);
            }
        }

        if (count > 1) {
            // Attempt to find 1 that is most qualified
            int highestPoints = 0;
            GraphNode highestNode = null;
            int tieCount = 0;
            for (Iterator highit = list.iterator(); highit.hasNext(); ) {
                int points = 0;
                node = (GraphNode) highit.next();
                if (node.getAlias() != null) {
                    ++points;
                    if (node.getAlias().getRefCount() == 1)
                        ++points;
                }
                // See if the default namespace is the same as this node's.
                if (defaultNamespace == null ? node.getNamespace() == null : defaultNamespace.equals(node.getNamespace()))
                    ++points;
                GraphLink link = node.getGraphLink();
                //System.out.println("link="+link+" link.name="+link.name);
                if (link != null && !"#PCDATA".equals(link.name)) {
                    ++points;
                    GraphLink firstChild = link.getFirstChild();
                    if (firstChild != null) {
                        ++points;
                        if (firstChild.getSibling() != null)
                            ++points;
                        if (firstChild.getFirstChild() != null)
                            ++points;
                    }
                    GraphLink sibling = link.getSibling();
                    if (sibling != null) {
                        ++points;
                        if (sibling.getSibling() != null)
                            ++points;
                        if (sibling.getFirstChild() != null)
                            ++points;
                    }
                }
                //System.out.println("points="+points+" node="+node);
                if (points > highestPoints) {
                    highestPoints = points;
                    highestNode = node;
                    tieCount = 0;
                } else if (points == highestPoints) {
                    ++tieCount;
                }
            }
            if (tieCount == 0 && highestNode != null) {
                count = 1;
                list.clear();
                list.add(highestNode);
            }
        }

        if (count == 1) {
            this.rootNode = (GraphNode)list.get(0);
            //	Only one element not referenced in the graph
            if (this.docRoot != null) {
                if (!this.docRoot.equals(this.rootNode.getName())) {
                    String str = "Mismatch between doc root name specified (" +
                        this.docRoot +
                        ") and the root name found in the DTD graph (" +
                        this.rootNode.getName() +")";
                    throw new IllegalStateException(str);
                }
            }
        } else if (count == 0) {
            this.rootNode = null;
            if (docRoot != null) {
                it = this.nameHash.values().iterator();
                while (it.hasNext()) {
                    node = (GraphNode)it.next();
                    if (docRoot.equals(node.getName())) {
                        rootNode = node;
                        break;
                    }
                }
            }
            if (rootNode == null)
                throw new IllegalStateException(Common.getMessage("NoRootElementCandidate"));
        } else {
            //	List the elements and pick the root (if specified) or ask
            config.messageOut.println("The following elements could be the root "
                                      + "of the document:");
            for (int i=0; i<list.size(); i++) {
                GraphNode n = (GraphNode)list.get(i);
                config.messageOut.print( (i+1) + ". " + n);
                if ((this.docRoot != null)
                    && (this.docRoot.equals(n.getName()))) {

                    this.rootNode = n;
                    config.messageOut.println(" <= parameter value");
                }
                else
                    config.messageOut.println("");
            }
	    
            if (this.rootNode == null) {
                //	We still don't know - ask for the element to use
		
                String errStr = "Could not find the root of the document. "
                    + "Use the -d option to specify the doc root";
		
                if (config.isAuto()) {
                    throw new IllegalStateException(errStr);
                }
		
                try {
                    BufferedReader rd =
                        new BufferedReader(new InputStreamReader(System.in));
		    
                    config.messageOut.print("Enter the element that should be used "
                                            + "as the root: ");
                    String str = rd.readLine();
		    
                    int i = Integer.parseInt(str)-1;
		    
                    if (i<0 || i>=list.size()) {
                        throw new IllegalStateException(errStr);
                    }
                    else {
                        this.rootNode = (GraphNode)list.get(i);
                    }
                }
                catch(Exception e) {
                    TraceLogger.error(e);
                    throw new Schema2BeansNestedException(errStr, e);
                }
            }
        }
	
        if (DDLogFlags.debug)
            config.messageOut.println("Using " + this.rootNode.getName()
                                      + " as the root of the document.");
    }
    
    //
    static String instanceToString(int instance, boolean bean) {
        switch (instance) {
	    case Common.TYPE_0_1:
            if (bean)
                return "[0,1]";
            else
                return "?";
	    case Common.TYPE_0_N:
            if (bean)
                return "[0,n]";
            else
                return "*";
	    case Common.TYPE_1_N:
            if (bean)
                return "[1,n]";
            else
                return "+";
        }
        return "";
    }
    
    //
    static String typeToString(int type) {
        switch (type) {
	    case Common.COMMENT:
            return "comment";
	    case Common.ELEMENT:
            return "element";
	    case Common.ATTLIST:
            return "attlist";
        }
        return "unknown value: " + type;
    }
    
    /**
     *	TreeParser interface. This is what the BeanBuilder uses to get
     *	elements of the tree. The goal is to try to keep separated
     *	the object graph implementation from its usage.
     *	Not sure, this is very useful though, since the tree builder
     *	knows the gory details of the graph. Just a gentle way to ask
     *	for the graph.
     */
    public GraphNode[] getNodes() {
        //
        // Try to give the results back with some order from top to bottom
        // (right now, it's BFS (Breadth First Search)).
        //
        int maxSize = nameHash.values().size();
        List ret = new ArrayList(maxSize);
        Map insertedNodes = new HashMap();
        Map ignoredNodes = new HashMap();
        getNodesInsertNode(rootNode, ret, insertedNodes, ignoredNodes);
        getNodes(rootNode.getGraphLink(), ret, insertedNodes, ignoredNodes);
        if (!config.isRemoveUnreferencedNodes()) {
            for (Iterator it = nameHash.values().iterator();
                 it.hasNext(); ) {
                GraphNode node = (GraphNode) it.next();
                if (!insertedNodes.containsKey(node) && !ignoredNodes.containsKey(node)) {
                    config.messageOut.println(Common.getMessage("MSG_FoundUnreferencedNode", node.toString()));
                    ret.add(node);
                }
            }
        }
        /*
            for (int i = 0; i < maxSize; ++i)
                System.out.println("ret["+i+"]="+ret[i]);
        */
        return (GraphNode[]) ret.toArray(new GraphNode[0]);
    }

    private void getNodes(GraphLink l, List ret,
                          Map insertedNodes, Map ignoredNodes) {
        Stack linkStack = new Stack();
        linkStack.push(l);
        while (!linkStack.isEmpty()) {
            l = (GraphLink) linkStack.pop();
            for (; l != null; l = l.getSibling()) {
                if (l.element != null) {
                    if (!insertedNodes.containsKey(l.element)) {
                        getNodesInsertNode(l.element, ret, insertedNodes, ignoredNodes);
                        linkStack.push(l.element.getGraphLink());
                    }
                }
                linkStack.push(l.getFirstChild());
            }
        }
    }

    private void getNodesInsertNode(GraphNode node, List ret,
                                    Map insertedNodes, Map ignoredNodes) {
        if (insertedNodes.containsKey(node)) {
            //System.out.println("Found a duplicate in my insert journey: "+node);
            return;
        }
        ret.add(node);
        insertedNodes.put(node, null);
        if (node.getExtension() != null)
            getNodesInsertNode(node.getExtension(), ret, insertedNodes, ignoredNodes);
        GraphNode alias = node.getAlias();
        if (alias != null && !insertedNodes.containsKey(alias)) {
            if (alias.getRefCount() <= 1) {
                // Only referenced by the thing which has an alias pointer to it.
                ignoredNodes.put(alias, null);
            } else {
                ret.add(alias);
                insertedNodes.put(alias, null);
            }
        }
        return;
    }
    
    public GraphNode getNode(String uniqueName) {
        return (GraphNode)this.nameHash.get(uniqueName);
    }
    
    public GraphNode getRoot() {
        return this.rootNode;
    }
    
    private static final String INDENT = "  ";
    
    static void dumpAttributes(GraphNode elt, StringBuffer str, String indent) {
        AttrProp[] attrList = elt.getAttributes();
	
        for (int i=0; i<attrList.length; i++)
            str.append(indent + INDENT+"[attr: " + attrList[i] + "]\n");
    }
    
    static void dumpTree(List children, StringBuffer str, String indent, boolean tree) {
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            GraphLink l = (GraphLink) it.next();
            dumpTree(l, str, indent, tree);
        }
    }
    
    static void dumpTree(GraphLink l, StringBuffer str, String indent, boolean tree) {
        if (l == null)
            return;
        //str.append("dumpTree: l.name="+l.name+" l="+l+"\n");
        if (l.element != null) {
            //str.append("dumpTree: l.element="+l.element+"\n");
            str.append(indent);
            str.append(l.name + " : "+l.element.toString());
            str.append(instanceToString(l.getElementInstance(), false) +
                       "\n");
		
            dumpAttributes(l.element, str, indent);
		
            if (tree && (l.element.getMarked() == false)) {
                l.element.setMarked(true);
                dumpTree(l.element.getGraphLink(), str, indent + INDENT +
                         instanceToString(l.getGroupInstance(), false) +
                         (l.isSequenceOr()?"| ":" "), tree);
                l.element.setMarked(false);
            }
        }
	    
        if (l.isSequenceOr() || (l.getGroupInstance() != Common.TYPE_1)) {
            str.append(indent+instanceToString(l.getGroupInstance(), false)+
                       (l.isSequenceOr()?"|\n":"\n"));
        }
	    
        dumpTree(l.getChildren(), str, indent + INDENT, tree );
    }

    //
    public String dump() {
        StringBuffer	str = new StringBuffer();
        GraphNode		n;
	
        str.append("Tree:\n");
        str.append(this.rootNode.toString());
        str.append("\n");
	
        dumpAttributes(rootNode, str, INDENT);
        dumpTree(this.rootNode.getGraphLink(), str, INDENT, true);
	
        return str.toString();
    }

    public void setPrefixGuesser(PrefixGuesser guesser) {
        prefixGuesser = guesser;
    }

    public PrefixGuesser getPrefixGuesser() {
        return prefixGuesser;
    }
}

//******************************************************************************
// END_NOI18N
// This class does not (and will not) cantain strings that need to be localized.
//******************************************************************************
