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

package org.netbeans.modules.schema2beans;

import java.util.*;


/**
 *  The DDRegistryParser is a parser/Iterator on a set of graphs, as
 *  registered in the schema2beans registry (DDRegistry).
 *
 *  DDParser is a parser/Iterator on a single schema2beans graph, using
 *  a schema2beans path description to define what should be parsed.
 *  DDRegistryParser extend the functiionality of DDParser by providing
 *  a parsing mechanism on a set of graphs (instead of a single one) and
 *  by adding more syntax to the DDParser schema2beans path syntax.
 *
 *  Where DDParser defined a DDLocation to define a location reference in
 *  the parsed graph, DDRegistryParser defines a DDCursor. The DDCursor
 *  defines a location on a set of graphs (a DDCursor might have a parent
 *  root defined in another graph).
 *
 *  The DDRegistryParser instances are created by the DDRegistry.
 */
public class DDRegistryParser implements Iterator {
    
    static final String CURRENT_CURSOR = ".";	// NOI18N
    
    /**
     *	Analyze and resolve the vriable references specified in the path
     */
    public static class PathResolver {
	
	static final char VARBEGIN 		= '{';
	static final char VAREND 		= '}';
	static final char VALUE 		= '#';
	
	//  Current module (display/non unique) name
	static final String VAR_MODNAME	= "mname";	// NOI18N
	
	//  Current module unique name
	static final String VAR_UNAME	= "uname";	// NOI18N
	
	//  Parent schema2beans type of the specified type {#ptype.EnvEntry}
	//  would be either Session or Entity.
	static final String VAR_PTYPE	= "ptype";	// NOI18N
	
	static final String VAR_TYPE	= "type";	// NOI18N
	
	
	String result = null;

	//
	public PathResolver() {
	}
	
	//
	public PathResolver(DDCursor cursor, String path) {
	    this.result = this.resolvePath(cursor, path);
	}
	
	
	static boolean needResolving(String path) {
	    if (path != null)
		return (path.indexOf(VARBEGIN) != -1);
	    else
		return false;
	}
	
	/**
	 *  This resolve all the variables referenced in the path string
	 *  using the knowledge of its current location. A variable is
	 *  defined with braces {}, and might use any of the values:
	 *
	 *	#mname, #uname, #ptype, #type
	 *
	 *  or any reference to a property of the current graph:
	 *
	 *     {NodeName}   // no # in this case
	 *
	 */
	String resolvePath(DDCursor cur, String path) {
	    if(path.indexOf(VARBEGIN) != -1) {
		int i1 = path.indexOf(VARBEGIN);
		int i2 = path.indexOf(VAREND);
		String v =
		(String)this.resolvePathVar(cur, path.substring(i1+1, i2));
		return path.substring(0, i1).trim() + v +
		this.resolvePath(cur, path.substring(i2+1));
	    }
	    return path.trim();
	}
	
	Object resolvePathVar(DDCursor cur, String path) {
	    //path = path.trim();
	    
	    if (path.indexOf(VARBEGIN) != -1 || path.indexOf(VAREND) != -1) {
		throw new IllegalArgumentException(Common.getMessage(
		"CannotNestDeclaration_msg"));
	    }
	    
	    if (path.indexOf('#') == 0 ) {
		path = path.substring(1);
		
		String remSuffix = null;
		int idx = path.indexOf('-');
		if (idx != -1) {
		    //	Might have to remove a suffix from the value
		    remSuffix = path.substring(idx+1);
		    path = path.substring(0, idx);
		}
		
		if (path.startsWith(VAR_MODNAME)) {
		    int in = path.indexOf(':');
		    if (in != -1) {
			String name = path.substring(in+1);
			path = this.getDDNameValue(cur, name).toString();
			path = cur.getRegistry().getName(path);
		    } else {
			path = cur.getRegistry().getName(cur);
		    }
		} else
		    if (path.startsWith(VAR_UNAME)) {
			path = cur.getRegistry().getID(cur);
		    } else
			if (path.startsWith(VAR_PTYPE)) {
			    int i = path.indexOf('.');
			    if (i != -1) {
				String t = path.substring(i+1);
				DDCursor pc = cur;
				BaseBean bean;
				do {
				    bean = this.getBean(pc.getRoot(), t);
				    if (bean == null) {
					pc = pc.getParent();
				    }
				} while(bean == null && pc != null);
				
				if (bean != null) {
				    path = bean.parent().name();
				}
			    }
			} else
			    if (path.startsWith(VAR_TYPE)) {
				path = cur.getRoot().name();
			    }
		
		if (remSuffix != null) {
		    if (path.endsWith(remSuffix)) {
			path = path.substring(0, path.length() -
			remSuffix.length());
		    }
		}
		
		return path;
	    } else {
		return this.getDDNameValue(cur, path);
	    }
	}
	
	private Object getDDNameValue(DDCursor pc, String path) {
	    Object val = null;
	    
	    //	Look for the value in the DDCursors and current graph
	    //	hierarchy (look first in the graph then in other DDCursors)
	    do {
		val = this.getValue(pc.getRoot(), path);
		if (val == null) {
		    pc = pc.getParent();
		}
	    } while(val == null && pc != null);
	    return val;
	}
	
	BaseBean getBean(BaseBean root, String name) {
	    while (root != null && !root.isRoot()) {
		if (root.hasName(name))
		    return root;
		root = root.parent();
	    }
	    return null;
	}
	
	String getValue(BaseBean root, String name) {
	    String val = null;
	    if (root != null) {
		do {
		    try {
			val = (String)root.getValue(name);
			break;
		    } catch(Exception e) {
			// Unknown property name - ignore it
		    }
		    root = root.parent();
		} while (root != null && !root.isRoot());
	    }
	    return val;
	}
	
	public String toString() {
	    return this.result;
	}
    }

    /**
     *	DDCursor is a location reference in one of the DDRegistry graphs.
     *  Note that DDCursor can be created in two different ways: from a schema2beans
     *  path or from a schema2beans node (BaseBean).
     */
    public static class DDCursor {
	DDCursor 	parent;
	BaseBean 	root;
	DDRegistry 	registry;
	
	public DDCursor(DDCursor parent, String path) {
	    this(parent, (BaseBean)null);
	    this.resolve(path);
	}
	
	public DDCursor(DDCursor parent, BaseBean root) {
	    this.parent = parent;
	    this.root = root;
	    if (this.registry == null && parent != null)
		this.registry = parent.registry;
	}
	
	public DDCursor(DDRegistry reg, String path) {
	    this.parent = null;
	    this.root = null;
	    this.registry = reg;
	    this.resolve(path);
	}
	
	public DDCursor(DDRegistry reg, BaseBean root) {
	    this.parent = null;
	    this.root = root;
	    this.registry = reg;
	}
	
	public DDRegistry getRegistry() {
	    return this.registry;
	}
	
	public BaseBean getRoot() {
	    return this.root;
	}
	
	public DDCursor getParent() {
	    return this.parent;
	}
	
	public Object getValue(String name) {
	    if (root != null)
		return this.root.getValue(name);
	    else
		return null;
	}
	
	void resolve(String path) {
	    
	    if (path == null) return;
	    path = path.trim();
	    if (path.equals("")) return;	// NOI18N
	    
	    if (path.startsWith("[") && path.endsWith("]")) {	// NOI18N
		this.resolveGraph(path.substring(1,path.length()-1));
		return;
	    }
	    
	    //	Find the proper root
	    if (this.parent == null) {
		throw new IllegalStateException(Common.getMessage(
		"CantResolveBecauseMissingParent_msg", path));
	    }
	    
	    //  Resolve any embeded {} variables
	    if (PathResolver.needResolving(path))
		path = (new PathResolver(this.parent, path)).toString();
	    
	    BaseBean root = this.parent.getRoot();
	    
	    if (root != null) {
		DDParser p = new DDParser(root, path);
		if (p.hasNext()) {
		    Object o = p.next();
		    if (o instanceof BaseBean) {
			this.root = (BaseBean)o;
		    } else {
			throw new IllegalStateException(
			Common.getMessage(
			"ParsingPathDoesntResolveToGraphNodeElement_msg",
			path, o.getClass().getName(), o.toString()));
		    }
		} else {
		    throw new IllegalStateException(Common.getMessage(
		    "NoElementFoundPath_msg", path));
		}
	    } else {
		throw new IllegalStateException(Common.getMessage(
		"NoRootFoundForPath_msg", path));
	    }
	}
	
	void resolveGraph(String path) {
	    String pathRoot = null;
	    
	    if (PathResolver.needResolving(path))
		path = (new PathResolver(this.parent, path)).toString();
	    
	    int idx = path.indexOf(':');
	    if (idx != -1) {
		pathRoot = path.substring(idx+1);
		path = path.substring(0, idx);
	    }
	    
	    BaseBean[] beans = this.registry.getRoots(path);

	    if (beans.length > 0) {
		this.root = beans[0];
		if (pathRoot != null) {
		    DDCursor cur = new DDRegistryParser.DDCursor(this,
								 pathRoot);
		    this.root = cur.getRoot();
		}
	    }
	}
	
	public String toString() {
	    String p, r;
	    
	    if (this.parent != null)
		p = this.parent.toString();
	    else
		p = "-";	// NOI18N
	    
	    if (this.root != null)
		r = root.name();
	    else
		r = "-";	// NOI18N
	    return "Parent:"+p+" Root:"+r;	// NOI18N
	}
	
	public String dump() {
	    if (this.root != null)
		return this.root.dumpBeanNode();
	    else
		return "<null graph>";	// NOI18N
	}
    }
    
    
    DDRegistry		registry;
    
    //
    //	The root of the parsing can be defined by:
    //		- a parent parser
    //		- a parent cursor
    //		- a graph reference in the scope definition: [graph_name]
    //
    DDRegistryParser	parentParser = null;
    DDCursor		parentCursor = null;
    DDRegistryParser	parserRoot = null;
    
    ParserSet		parser = null;
    
    public DDRegistryParser(DDRegistry reg, DDRegistryParser rp,
			    String path) {
	this.registry = reg;
	this.initialize(path, rp, null);
    }
    
    public DDRegistryParser(DDRegistry reg, DDCursor cursor,
			    String path) {
	this.registry = reg;
	this.initialize(path, null, cursor);
    }
    
    public DDRegistryParser(DDRegistry reg, String path) {
	this.registry = reg;
	this.initialize(path, null, null);
    }
    
    public DDRegistry getRegistry() {
	return this.registry;
    }
    
    /**
     *	Initialize the parser. Either the parser or cursor is set, not both.
     */
    void initialize(String path, DDRegistryParser regParser, DDCursor cursor) {
	String graphName = null;
	String subpath = null;
	String parsingPath = null;
	
	//
	//	A scope ([NAME]) refers to a graph or set of graphs in the
	//	registry. In such case, the scope is the root of the parser
	//	(and parserRoot = null) since we'll get our root beans from the
	//	scope and not from the parent parser or cursor 
	//	(if any specified).
	//
	//path = path.trim();	
	
	DDCursor cur = cursor;
	if (cur == null && regParser != null)
	    cur = regParser.getCursor();
	
	if (path.startsWith("[")) { // NOI18N
	    int idx = path.indexOf(']');
	    graphName = path.substring(1, idx);
	    
	    if (path.length() > idx+1)
		path = path.substring(idx+1);
	    else
		path = ".";	// NOI18N
	    
	    idx = graphName.indexOf(':');
	    if (idx != -1) {
		subpath = graphName.substring(idx+1, graphName.length()-1);
		graphName = graphName.substring(0, idx);

		if (PathResolver.needResolving(subpath))
		    subpath = (new PathResolver(cur, subpath)).toString();
	    }
	    
	    if (PathResolver.needResolving(graphName))
		graphName = (new PathResolver(cur, graphName)).toString();
	    
	    if (graphName.equals(CURRENT_CURSOR) && cursor != null)
		graphName = null;
	}
	
	if (PathResolver.needResolving(path))
	    parsingPath = (new PathResolver(cur, path)).toString();
	else
	    parsingPath = path;
	
	
	if (graphName == null && regParser == null && cursor == null) {
	    throw new IllegalStateException(Common.getMessage(
	    "CantFindRootForParser_msg"));
	}
	
	//
	//	We know that we have a parent root - if the graphName 
	//	is specified, then we get the root from the registry
	//
	if (graphName != null) {
	    
	    //
	    //	The parser is initialized with an absolute graph
	    //	name reference, such as [ejbmodule]
	    //	That means that we get 1-n BaseBean(s) from the registry
	    //	that we use as the root of the parsing.
	    //
	    BaseBean[] beans = this.registry.getRoots(graphName);
	    this.parser = new ParserSet(beans, null, parsingPath);
	    this.parser.setRoot();
	} else if (regParser != null) {
	    
	    //
	    //	The parser is initialized from another parser. That means
	    //	that the current position of the other parser is used
	    //	as the root of this new parser. However, we need to consider
	    //	two cases: 1. the other parser has defined a set of roots
	    //	(previous case when the parser is initialized with []),
	    //	2. the other parser has only one root.
	    //	If the other parser was initialized as a set of roots,
	    //	we need to get all of them to initialize this parser (or
	    //	we'll miss the 2-n graphs in the parsing).
	    //
	    if (regParser.isRoot() && regParser.getRoots().length > 1) {
		BaseBean[] beans = regParser.getRoots();
		
		//	If the other parser has a parsingPath, we need to
		//	get the beans using this parsingPath
		if (regParser.hasParsingPath()) {
		    String pp = regParser.getParsingPath();
		    ArrayList tmpArr = new ArrayList();
		    
		    for (int i=0; i<beans.length; i++) {
			DDParser tmp = new DDParser(beans[i], pp);
			if (tmp.hasNext())
			    tmpArr.add(tmp.next());
		    }
		    BaseBean[] newBeans = new BaseBean[tmpArr.size()];
		    beans = (BaseBean[])tmpArr.toArray(newBeans);
		}
		
		this.parser = new ParserSet(beans, null, parsingPath);
	    } else {
		while (regParser.current() == null && regParser.hasNext())
		    regParser.next();
		
		this.parser = new ParserSet((BaseBean)regParser.current(), cur,
					    parsingPath);
		
	    }
	    
	} else if (cursor != null) {
	    //
	    //	The parser is initialized from a DDCursor position.
	    //
	    this.parser = new ParserSet(cursor.getRoot(), cur, parsingPath);
	} else {
	    throw new IllegalStateException( Common.getMessage(
	    "NoParentSpecified_msg"));
	}
    }
    
    
    boolean isRoot() {
	return this.parser.isRoot();
    }
    
    boolean hasParsingPath() {
	return this.parser.hasParsingPath();
    }
    
    String getParsingPath() {
	return this.parser.getParsingPath();
    }
    
    BaseBean[] getRoots() {
	return this.parser.getRoots();
    }
    
    //	Reset the current ParsetSet to use the next available root from the
    //	parent.
    public Object next() {
	return this.parser.next();
    }
    
    public boolean hasNext() {
	return this.parser.hasNext();
    }
    
    public DDCursor getCursor() {
	Object o = this.current();
	
	if (o instanceof BaseBean) {
	    BaseBean b = (BaseBean)o;
	    if (b == null && this.hasNext())
		b = (BaseBean)this.next();
	    if (b != null)
		return new DDCursor(this.registry, b);
	} else {
	    //	Return our parent cursor or build a new one
	    DDCursor cur = this.parser.getParentCursor();
	    if (cur == null) {
		BaseBean[] beans = this.parser.getRoots();
		cur = new DDCursor(this.registry, beans[0]);
	    }
	    return cur;
	}
	return null;
    }
    
    public DDParser.DDLocation getLocation() {
	return this.parser.getLocation();
    }
    
    public Object current() {
	return this.parser.current();
    }
    
    public void remove() {
	throw new UnsupportedOperationException();
    }
    
    public Object getValue(String ddName) {
	BaseBean b = (BaseBean)this.current();
	if (b == null && this.hasNext())
	    b = (BaseBean)this.next();
	
	if (b != null) {
	    //	This will seach for the ddName element in the current
	    //	graph and will also search in the ParentCursor graph
	    //	if the element is not found
	    DDCursor cur = new DDCursor(this.parser.getParentCursor(), b);
	    PathResolver p = new PathResolver();
	    Object obj = p.resolvePathVar(cur, ddName);
	    return obj;
	}
	
	return null;
    }
    
    
    /**
     *  ParserSet handle the parsing of a set of graph through the usage
     *  of DDParser. DDRegistryParser delegates the multi-graph parsing to
     *  this inner class.
     */
    class ParserSet {
	private BaseBean[] 	roots;
	private int		cur;
	private String		parsingPath;
	private DDParser	curParser;
	private boolean		isRoot;
	private DDCursor	parentCursor;
	
	ParserSet(BaseBean[] roots, DDCursor cur, String path) {
	    if (roots != null && roots.length > 0 && roots[0] != null) {
		this.cur = 0;
		this.isRoot = false;
		this.roots = roots;
		this.parsingPath = path;
		this.parentCursor = cur;
		this.adjustPathRoot();
		this.newParser();
	    } else {
		throw new IllegalArgumentException(Common.getMessage(
		"NoRootSpecified_msg", path));
	    }
	}
	
	ParserSet(BaseBean root, DDCursor cur, String path) {
	    this(new BaseBean[] {root}, cur, path);
	}
	
	void adjustPathRoot() {
	    if (this.parsingPath.startsWith("../")) {	// NOI18N
		int i = this.parsingPath.lastIndexOf("../");	// NOI18N
		int n = i/3;
		do {
		    for (int j=0; j<this.roots.length; j++) {
			if (this.roots[j].isRoot())
			    throw new Schema2BeansRuntimeException(Common.getMessage(
			    "CantAccessBaseBeanNode_msg", this.parsingPath));
			this.roots[j] = this.roots[j].parent();
		    }
		} while(n-- > 0);
		this.parsingPath = this.parsingPath.substring(i+3);
	    }
	}
	
	BaseBean[] getRoots() {
	    return this.roots;
	}
	
	DDCursor getParentCursor() {
	    return this.parentCursor;
	}
	
	void setRoot() {
	    this.isRoot = true;
	}
	
	boolean isRoot() {
	    return this.isRoot;
	}
	
	boolean hasParsingPath() {
	    if (this.parsingPath != null)
		return !this.parsingPath.equals(".");	// NOI18N
	    else
		return false;
	}
	
	String getParsingPath() {
	    return this.parsingPath;
	}
	
	private boolean newParser() {
	    if (this.cur < this.roots.length) {
		try {
		    this.curParser =
			new DDParser(this.roots[this.cur++], this.parsingPath);
		    return true;
		} catch(NoSuchElementException e) {
		    //	If the element is not found, try with our parent root,
		    //	this might be a linked graph.
		    if(this.parentCursor != null) {
			this.cur = 0;
			this.roots =
			    new BaseBean[] {this.parentCursor.getRoot()};
			this.parentCursor = this.parentCursor.getParent();
			return this.newParser();
		    }
		    else
			throw e;
		}
	    }
	    return false;
	}
	
	//
	boolean hasNext() {
	    boolean more = this.curParser.hasNext();
	    
	    while (!more && this.newParser())
		more = this.curParser.hasNext();
	    
	    return more;
	}
	
	DDParser.DDLocation getLocation() {
	    return this.curParser.getLocation();
	}
	
	//	Return what the parser has currently
	Object current() {
	    return this.curParser.current();
	}
	
	//	Get the next element available from our set of parsers
	Object next() {
	    if (this.hasNext()) {
		return this.curParser.next();
	    } else {
		throw new NoSuchElementException();
	    }
	}
    }
}
